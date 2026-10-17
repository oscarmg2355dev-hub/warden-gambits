"""Migration (boss-modes spec section 1): the zone bosses swap their HP "reaction" for attack/defense "modes".

For the boss (first rival of the level-7 node) of every zone:
- the "pattern" becomes the attack pattern: the pattern without shields (2131, 2132) or heals (21...);
- the "reaction" is removed;
- "modes" is added as the last key: defenseBelowPct 50, attackAbovePct 70, maxDefenses 3 and the defense
  2132, 21, 212, 2121, 21212 plus the first two damage combos of the attack pattern, in that order.
  Bree's tutorial boss: defenseBelowPct 40, attackAbovePct 60, maxDefenses 1 and the defense 21, 212 plus the first
  damage combo of its attack pattern.
The commons (and their healer reactions) are not touched.

The boss object is rewritten in the layout it already had (one-line rivals stay on one line; rivals written by
json.dump with indent 2 keep that block form); the script checks that re-serializing the original object gives the
original text before replacing it. UTF-8 without BOM, line endings kept.
Idempotent: a boss that already has the expected modes and no reaction is left alone.
Prints the attack pattern and the defense of each boss.
Usage: python tools/content/add_boss_modes.py
"""
import json
import sys
from pathlib import Path

STORY = Path(__file__).resolve().parents[2] / "engine" / "src" / "main" / "resources" / "story"

SHIELDS = {"2131", "2132"}
HEALS = {"21", "212", "2121", "21212"}
# Damage families of the combo catalog (engine Combos): Lanza, Lanza cruzada, Puno en area, Robo de vida, Golpe directo.
DAMAGE = {
    "12", "121", "1212", "12121",
    "13", "131", "1313", "13131",
    "32", "323", "3232", "32323",
    "3123", "31232",
    "123",
}
BOSS_DEFENSE_OPENING = ["2132", "21", "212", "2121", "21212"]
BREE_DEFENSE_OPENING = ["21", "212"]


def attack_pattern(pattern):
    return [c for c in pattern if c not in SHIELDS and c not in HEALS]


def boss_modes(zone_id, attack):
    damage = [c for c in attack if c in DAMAGE]
    if zone_id == "bree":
        return {"defenseBelowPct": 40, "attackAbovePct": 60, "maxDefenses": 1, "defense": BREE_DEFENSE_OPENING + damage[:1]}
    return {"defenseBelowPct": 50, "attackAbovePct": 70, "maxDefenses": 3, "defense": BOSS_DEFENSE_OPENING + damage[:2]}


def inline(value):
    """One-line form of the content files: { "a": 1, "b": ["12", "121"] }."""
    if isinstance(value, dict):
        return "{ " + ", ".join(f"{json.dumps(k)}: {inline(v)}" for k, v in value.items()) + " }"
    if isinstance(value, list):
        return "[" + ", ".join(inline(v) for v in value) + "]"
    return json.dumps(value, ensure_ascii=False)


def block(value, indent):
    return json.dumps(value, indent=2, ensure_ascii=False).replace("\n", "\n" + indent)


def boss_span(text):
    """Start and end offsets of the boss object of the level-7 node."""
    level = text.index('"level": 7')
    start = text.index("{", text.index('"rivals"', level))
    _, end = json.JSONDecoder().raw_decode(text, start)
    return start, end


def migrate(text, zone_id):
    start, end = boss_span(text)
    original = text[start:end]
    boss = json.loads(original)
    one_line = "\n" not in original
    indent = text[text.rfind("\n", 0, start) + 1:start]
    render = inline if one_line else (lambda v: block(v, indent))
    assert render(boss) == original, f"{zone_id}: unexpected layout of the boss object"
    attack = attack_pattern(boss["pattern"])
    migrated = {k: v for k, v in boss.items() if k not in ("reaction", "modes")}
    migrated["pattern"] = attack
    migrated["modes"] = boss_modes(zone_id, attack)
    return text[:start] + render(migrated) + text[end:], boss["pattern"], migrated


def main() -> int:
    for path in sorted(STORY.glob("*.json")):
        raw = path.read_bytes()
        assert not raw.startswith(b"\xef\xbb\xbf"), f"{path.name} has a BOM"
        text = raw.decode("utf-8")
        data = json.loads(text)
        zone_id = data["id"]
        if not any(n["level"] == 7 and n.get("encounter") for n in data["nodes"]):
            continue
        migrated, before, boss = migrate(text, zone_id)
        if migrated != text:
            json.loads(migrated)
            path.write_bytes(migrated.encode("utf-8"))
        changed = "" if before == boss["pattern"] else f" (was {before})"
        print(f"{zone_id}: attack {boss['pattern']}{changed}; defense {boss['modes']['defense']}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
