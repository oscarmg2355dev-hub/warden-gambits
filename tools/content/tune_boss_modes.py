"""Re-balance of the bosses (tuning.md «Jefes con modo ataque y defensa» and «Escudo táctico: inmunidad total»).

Sets "defenseBelowPct" and "maxDefenses" of the boss "modes" of each zone to the TUNING table (the zones it does not
list keep, or get back, the spec values 50 and 3; Bree is never touched), and the "levelOffset", "pattern" and
"everyNTicks" of each boss to the ATTACK table. Each zone file has a single "modes" object, on the boss line, so the
values are replaced in place: the layout, UTF-8 without BOM and the line endings are kept. The defense tail (the first
two damage combos of the pattern) is not touched, so a new pattern must keep them.
Must match ContentRules.BOSS_MODE_TUNING and Bestiary.bosses (RealContentTest checks the JSON against them). Idempotent.
Usage: python tools/content/tune_boss_modes.py
"""
import re
from pathlib import Path

STORY = Path(__file__).resolve().parents[2] / "engine" / "src" / "main" / "resources" / "story"

SPEC = (50, 3)
TUNING = {
    "chet": (10, 1),
    "moscagua": (10, 1),
    "cima": (15, 1),
    "puente": (50, 1),
    "quebradas": (20, 2),
    "bruinen": (15, 1),
    "eregion": (15, 1),
    "caradhras": (15, 1),
    "durin": (15, 1),
    "moria": (15, 1),
    "sombrio": (20, 2),
    "nimrodel": (20, 1),
    "anduin": (15, 1),
    "argonath": (15, 1),
    "amonhen": (15, 1),
    "emynmuil": (10, 1),
    "cienagas": (15, 1),
    "ithilien": (15, 1),
    "cirithungol": (15, 1),
    "gorgoroth": (15, 1),
}

# zone -> (levelOffset, attack pattern, everyNTicks) of the boss (tactical-shield immunity re-balance).
ATTACK = {
    "chet": (3, "12 121 1212 12121 31", 1),
    "moscagua": (2, "32 323 3232 13 131 1313 31", 1),
    "cima": (5, "32 323 3232 12 121 31", 1),
    "puente": (2, "12 121 1212 13 131 31 11", 1),
    "quebradas": (6, "12 121 1212 13 131 31", 1),
    "bruinen": (3, "32 323 3232 13 131 3123 31232 31", 1),
    "eregion": (3, "12 121 1212 13 131 31 123", 1),
    "caradhras": (3, "32 323 3232 12 121 1212 31 123", 1),
    "durin": (5, "13 131 1313 12 121 1212 31 123", 1),
    "moria": (3, "32 323 3232 13 131 31 123", 1),
    "sombrio": (6, "12 121 1212 13 131 31 123", 1),
    "nimrodel": (4, "32 323 3232 12 121 31", 1),
    "anduin": (8, "12 121 1212 13 131 31 32 323 123", 1),
    "argonath": (11, "12 121 1212 13 131 1313 31 123", 1),
    "amonhen": (12, "13 131 1313 12 121 1212 31 123", 1),
    "emynmuil": (2, "12 121 1212 32 323 3232 31 123", 1),
    "cienagas": (10, "3123 31232 13 131 1313 31 32 323 123", 1),
    "ithilien": (2, "32 323 3232 12 121 123 31", 1),
    "cirithungol": (8, "13 131 1313 12 121 1212 31 123", 1),
    "gorgoroth": (4, "32 323 3232 3123 31232 31 123", 1),
}


BOSS_ATTACK = r'("portrait": "boss_{zone}",\s*"levelOffset": )-?\d+(,\s*"pattern": )\[([^\]]*)\](,\s*"everyNTicks": )\d+'


def set_attack(text, zone, offset, pattern, every):
    """Replaces the levelOffset, pattern and everyNTicks of the boss of [zone], keeping a one-line or multi-line list."""

    def replace(m):
        old = m.group(3)
        combos = [f'"{c}"' for c in pattern.split()]
        if "\n" in old:
            item = re.search(r"\n([ \t]*)\"", old).group(1)
            close = old[old.rindex("\n"):]
            eol = "\r\n" if "\r\n" in old else "\n"
            body = "".join(f"{eol}{item}{c}," for c in combos)[:-1] + close
        else:
            body = ", ".join(combos)
        return f"{m.group(1)}{offset}{m.group(2)}[{body}]{m.group(4)}{every}"

    result, count = re.subn(BOSS_ATTACK.format(zone=zone), replace, text)
    assert count == 1, zone
    return result


def main():
    for path in sorted(STORY.glob("*.json")):
        zone = path.stem
        raw = path.read_bytes().decode("utf-8")
        if '"modes"' not in raw or zone == "bree":
            continue
        assert raw.count('"modes"') == 1, zone
        below, max_defenses = TUNING.get(zone, SPEC)
        text = re.sub(r'"defenseBelowPct": \d+', f'"defenseBelowPct": {below}', raw)
        text = re.sub(r'"maxDefenses": \d+', f'"maxDefenses": {max_defenses}', text)
        if zone in ATTACK:
            text = set_attack(text, zone, *ATTACK[zone])
        if text != raw:
            path.write_bytes(text.encode("utf-8"))
        print(f"{zone}: defenseBelowPct {below}, maxDefenses {max_defenses}, attack {ATTACK.get(zone)}")


if __name__ == "__main__":
    main()
