"""One-off migration (rival-healing spec section 3): add the HP "reaction" to the story rivals.

- Boss (first rival of a level-7 node): atHpPct [50, 25], shield 2132 (Escudo maestro) and the
  full Cura chain 21, 212, 2121, 21212. Bree's tutorial boss: atHpPct [40], no shield, 21, 212.
- Level 2-6 encounters: only the first humanoid rival (portrait in HUMANOIDS) gets atHpPct [40], combos [21212]
  (moved to [30] by the re-balance).
  Boss escorts, beasts and wraiths never react.
- The Bree and Chet bandits lose the Cura I ("21") of their pattern.

"reaction" is inserted as the last key of the rival object, keeping each file's layout (one-line rivals stay on one
line; rivals written by json.dump with indent 2 get an indented block). UTF-8 without BOM.
Idempotent: rivals that already have a reaction are left alone. Prints the number of reactions added per zone.
Usage: python tools/content/add_reactions.py
"""
import json
import re
import sys
from pathlib import Path

STORY = Path(__file__).resolve().parents[2] / "engine" / "src" / "main" / "resources" / "story"

HUMANOIDS = {"bandit", "dunlending", "goblin", "orc", "orc_archer", "uruk", "mordor_orc", "haradrim"}
FULL_CURE_CHAIN = ["21", "212", "2121", "21212"]
HEALER = {"atHpPct": [40], "combos": ["21212"]}
BREE_BOSS = {"atHpPct": [40], "combos": ["21", "212"]}
# Historical: the first migration gave every boss atHpPct [50, 25] with a shield (Escudo tactico 2131 for some) and
# the healers atHpPct [40]. The re-balance (docs/balance/tuning.md, "Rivales que se curan y MAESTRO") switched the boss
# shields to Escudo maestro (2132) or none (2131 cuts the boss's Daze chance and weakens the counterplay of stunning it
# to cut its heals), tuned thresholds, offsets and a few patterns per boss, and moved the healers to atHpPct [30].
# This one-off script therefore no longer reproduces the final content.
BOSS_SHIELD = "2132"

OLD_BANDIT = '"pattern": ["12", "121", "21"]'
NEW_BANDIT = '"pattern": ["12", "121"]'
EVERY = re.compile(r'"everyNTicks": \d+')


def boss_reaction(zone_id):
    if zone_id == "bree":
        return BREE_BOSS
    return {"atHpPct": [50, 25], "shield": BOSS_SHIELD, "combos": FULL_CURE_CHAIN}


def expected_reactions(zone_id, level, rivals):
    if level == 7:
        return [boss_reaction(zone_id)] + [None] * (len(rivals) - 1)
    healer = next((i for i, r in enumerate(rivals) if r["portrait"] in HUMANOIDS), None)
    return [HEALER if i == healer else None for i in range(len(rivals))]


def inline(reaction):
    """One-line form matching the one-line rivals: { "atHpPct": [40], "combos": ["21212"] }."""
    parts = []
    for key, value in reaction.items():
        if isinstance(value, list):
            parts.append(f'"{key}": [' + ", ".join(json.dumps(v) for v in value) + "]")
        else:
            parts.append(f'"{key}": {json.dumps(value)}')
    return "{ " + ", ".join(parts) + " }"


def migrate(text, zone_id):
    if zone_id in ("bree", "chet"):
        lines = text.split("\n")
        for n, line in enumerate(lines):
            if OLD_BANDIT in line:
                assert '"portrait": "bandit"' in line, f"{zone_id}: Cura I outside a bandit: {line.strip()}"
                lines[n] = line.replace(OLD_BANDIT, NEW_BANDIT)
        text = "\n".join(lines)
    data = json.loads(text)
    # Every rival has exactly one "everyNTicks"; locate them in document order.
    placed = [(node["level"], i, rival)
              for node in data["nodes"] if node.get("encounter")
              for i, rival in enumerate(node["encounter"]["rivals"])]
    expected = [r for node in data["nodes"] if node.get("encounter")
                for r in expected_reactions(zone_id, node["level"], node["encounter"]["rivals"])]
    matches = list(EVERY.finditer(text))
    assert len(matches) == len(placed), f"{zone_id}: {len(matches)} everyNTicks for {len(placed)} rivals"
    added = 0
    out = []
    last = 0
    for match, (_, _, rival), reaction in zip(matches, placed, expected):
        out.append(text[last:match.end()])
        last = match.end()
        if "reaction" in rival:
            assert rival["reaction"] == reaction, f"{zone_id}: {rival['name']} has {rival['reaction']}, expected {reaction}"
            continue
        if reaction is None:
            continue
        assert list(rival)[-1] == "everyNTicks", f"{zone_id}: everyNTicks is not the last key of {rival['name']}"
        rest = text[last:]
        if rest.startswith(" }"):
            out.append(', "reaction": ' + inline(reaction))
        else:
            line_start = text.rfind("\n", 0, match.start()) + 1
            indent = text[line_start:match.start()]
            block = json.dumps(reaction, indent=2, ensure_ascii=False).replace("\n", "\n" + indent)
            out.append(',\n' + indent + '"reaction": ' + block)
        added += 1
    out.append(text[last:])
    return "".join(out), added


def main() -> int:
    for path in sorted(STORY.glob("*.json")):
        raw = path.read_bytes()
        assert not raw.startswith(b"\xef\xbb\xbf"), f"{path.name} has a BOM"
        text = raw.decode("utf-8")
        zone_id = json.loads(text)["id"]
        migrated, added = migrate(text, zone_id)
        if migrated != text:
            json.loads(migrated)
            path.write_bytes(migrated.encode("utf-8"))
        print(f"{zone_id}: {added} reactions added")
    return 0


if __name__ == "__main__":
    sys.exit(main())
