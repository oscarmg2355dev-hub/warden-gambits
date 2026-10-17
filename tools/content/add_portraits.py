"""One-off migration: add "portrait" after "name" to every rival of the story JSON.

Mapping: combat-cards spec section 2. The boss is the first rival of a level-7 node
(portrait boss_<zone>); every other rival is mapped by name to one of the 12 common types.
Fails (exit 1) if any rival name is unmapped. Idempotent: rivals that already have a portrait are skipped.
Usage: python tools/content/add_portraits.py
"""
import json
import re
import sys
from pathlib import Path

STORY = Path(__file__).resolve().parents[2] / "engine" / "src" / "main" / "resources" / "story"

COMMON = {
    "bandit": ["Ratero de Bree", "Ladrón de caballos", "Bandido del foso", "Bribón de la banda", "Matón de Abrojal",
               "Vigía de los salteadores", "Salteador del Tuerto", "Bandido del bosque", "Salteador rezagado",
               "Cobrador de peajes", "Bandido del puente"],
    "wolf": ["Lobo hambriento", "Lobo pardo de Chet", "Lobo de las pozas", "Lobo de la manada", "Lobo viejo",
             "Lobo joven de la manada"],
    "wolf_saruman": ["Lobo de Saruman"],
    "warg": ["Huargo", "Huargo viejo", "Huargo explorador", "Huargo grande", "Huargo de la cueva"],
    "warg_rider": ["Huargo de monta"],
    "dunlending": ["Espía dunlendino", "Espía dunlendino veterano", "Explorador dunlendino",
                   "Rastreador dunlendino", "Espía del pantano"],
    "crebain": ["Crebain"],
    "swarm": ["Nube de moscardones", "Enjambre de moscardones", "Moscardones de Moscagua", "Enjambre del ahogado",
              "Guardianas del enjambre"],
    "goblin": ["Trasgo de Moria"],
    "orc": ["Orco de las Montañas Nubladas", "Orco grande de las Montañas Nubladas", "Orco capitán"],
    "orc_archer": ["Arquero orco"],
    "troll": ["Trol de las cavernas"],
}
BY_NAME = {name: key for key, names in COMMON.items() for name in names}

LINE = re.compile(r'("name": "([^"]+)")(,)')


def main() -> int:
    unassigned = []
    for path in sorted(STORY.glob("*.json")):
        raw = path.read_bytes()
        assert not raw.startswith(b"\xef\xbb\xbf"), f"{path.name} has a BOM"
        text = raw.decode("utf-8")
        data = json.loads(text)
        # Rival objects sit on one line each, with "name" first; locate them in document order.
        rivals = [(n["level"], n["index"], i)
                  for n in data["nodes"] if n.get("encounter")
                  for i, _ in enumerate(n["encounter"]["rivals"])]
        names = [r["name"] for n in data["nodes"] if n.get("encounter") for r in n["encounter"]["rivals"]]
        portraits = [
            f"boss_{data['id']}" if level == 7 and i == 0 else BY_NAME.get(name)
            for (level, _, i), name in zip(rivals, names)
        ]
        counter = iter(range(len(portraits)))
        assigned = 0

        def repl(m):
            nonlocal assigned
            k = next(counter)
            if portraits[k] is None:
                unassigned.append((path.name, m.group(2)))
                return m.group(0)
            assigned += 1
            return f'{m.group(1)}, "portrait": "{portraits[k]}"{m.group(3)}'

        # Only rival lines: those containing "levelOffset".
        out_lines = []
        for line in text.split("\n"):
            if '"levelOffset"' in line and '"portrait"' not in line:
                line = LINE.sub(repl, line, count=1)
            elif '"levelOffset"' in line:
                next(counter)
            out_lines.append(line)
        out = "\n".join(out_lines)
        json.loads(out)
        path.write_bytes(out.encode("utf-8"))
        print(f"{path.stem}: {assigned} rivals assigned ({len(portraits)} total)")
    print(f"unassigned: {len(unassigned)}")
    for item in unassigned:
        print("  ", item)
    return 1 if unassigned else 0


if __name__ == "__main__":
    sys.exit(main())
