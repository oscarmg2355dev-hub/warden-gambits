#!/usr/bin/env python3
"""Build docs/content/narration-prompts.md: the text of every story node, ready to paste
into Google AI Studio ("Generate speech") to record the narration.

Reads engine/src/main/resources/story/<zone>.json (Spanish) and story/en/<zone>.json (English),
in the zone order of Route.kt. One entry per node: key + "title." + text.

Usage: python tools/narration/build_prompts.py
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
STORY = ROOT / "engine" / "src" / "main" / "resources" / "story"
ROUTE = ROOT / "engine" / "src" / "main" / "kotlin" / "com" / "wardengambits" / "engine" / "story" / "Route.kt"
OUT = ROOT / "docs" / "content" / "narration-prompts.md"
FINAL_KEY = "orodruin_final"
ENTRY = re.compile(r"^#### `([a-z0-9_]+)`\s*$")


def zone_ids() -> list[str]:
    src = ROUTE.read_text(encoding="utf-8")
    final = re.search(r'FINAL_ZONE_ID\s*=\s*"([a-z0-9_]+)"', src)
    ids = []
    for m in re.finditer(r'ZoneInfo\(\s*(?:"([a-z0-9_]+)"|(FINAL_ZONE_ID))\s*,', src):
        ids.append(m.group(1) or (final.group(1) if final else "orodruin"))
    return ids


def spoken(title: str, text: str) -> str:
    title = title.strip()
    if title and title[-1] not in ".!?…:":
        title += "."
    return f"{title}\n{text.strip()}"


def load_nodes() -> dict[str, dict[str, str]]:
    """{lang: {node key: spoken text}} in route order."""
    result: dict[str, dict[str, str]] = {"es": {}, "en": {}}
    for zone in zone_ids():
        es = json.loads((STORY / f"{zone}.json").read_text(encoding="utf-8"))
        en = json.loads((STORY / "en" / f"{zone}.json").read_text(encoding="utf-8"))["nodes"]
        for n in es["nodes"]:
            key = FINAL_KEY if zone == "orodruin" else f"{zone}_{n['level']}_{n['index']}"
            result["es"][key] = spoken(n["title"], n["text"])
            result["en"][key] = spoken(en[key]["title"], en[key]["text"])
    return result


def zone_of(key: str) -> str:
    return "orodruin" if key == FINAL_KEY else key.split("_")[0]


def parse_entries(section: str) -> dict[str, str]:
    """Inverse of the entry layout: {key: spoken text}."""
    entries: dict[str, list[str]] = {}
    current = None
    for line in section.splitlines():
        m = ENTRY.match(line)
        if m:
            current = entries.setdefault(m.group(1), [])
        elif line.startswith("## "):
            current = None
        elif current is not None:
            current.append(line)
    return {k: "\n".join(v).strip() for k, v in entries.items()}


HEADER = """# Prompts de narración

Texto de los 507 nodos de la historia, en español e inglés, para grabar la narración con
**Google AI Studio → «Generate speech»** (Gemini TTS). Este fichero lo genera
`python tools/narration/build_prompts.py`; no lo edites a mano.

## Procedimiento

1. Abre <https://aistudio.google.com/> y elige **Generate speech** (voz de un solo hablante, «Single-speaker audio»).
2. Elige **una sola voz para todo el juego**. Recomendada: **Gacrux** (femenina, madura) o **Kore** (femenina, firme).
   Usa la misma en los dos idiomas.
3. Pega el **bloque de estilo** del idioma en el cuadro de estilo / instrucciones (o encima del texto) y, debajo,
   el texto de la entrada (el título y el cuerpo juntos, tal como aparecen).
4. Genera y escucha. Si no convence, vuelve a generar la misma entrada.
5. Descarga el audio como **WAV** y guárdalo con el nombre de la clave, `<clave>.wav`, en:
   - español: `docs/content/narration/es/<clave>.wav`
   - inglés: `docs/content/narration/en/<clave>.wav`

   Ejemplo: `docs/content/narration/es/bree_1_0.wav`. Si el navegador añade « (1)» al nombre, no pasa nada:
   se acepta, y si existen las dos versiones gana la que no lleva sufijo. También valen mp3, m4a, ogg y flac.
6. Ejecuta `python tools/narration/convert.py` (recorta silencios, normaliza y escribe AAC mono en
   `app/src/main/assets/narration/<es|en>/<clave>.m4a`). Lo que aún falta queda en `docs/content/narracion-pendientes.md`.

Claves: `<zona>_<nivel>_<índice>` y `orodruin_final` (507 por idioma).

## Bloque de estilo (español)

```
Narradora de fantasía épica, voz femenina grave y cálida. Ritmo lento y pausado, con suspense:
baja la voz y haz pausas dramáticas en los momentos de tensión; más aire en las descripciones.
Interpreta con convicción pero sin exagerar. Solo voz, sin música ni efectos de sonido.
```

## Style block (English)

```
Epic fantasy narrator, female voice, deep and warm. Slow, measured pace with suspense:
lower your voice and use dramatic pauses at tense moments; leave room to breathe in descriptions.
Perform with conviction but without overacting. Voice only, no music or sound effects.
```
"""


def build(out: Path = OUT) -> None:
    nodes = load_nodes()
    names = {"es": "Español", "en": "English"}
    parts = [HEADER]
    for lang in ("es", "en"):
        parts.append(f"\n# {names[lang]}\n\n<!-- lang:{lang} -->\n")
        zone = None
        for key, text in nodes[lang].items():
            if zone_of(key) != zone:
                zone = zone_of(key)
                parts.append(f"\n## {zone}\n")
            parts.append(f"\n#### `{key}`\n\n{text}\n")
        parts.append("\n<!-- end -->\n")
    out.write_text("".join(parts), encoding="utf-8", newline="\n")
    print(f"{sum(len(v) for v in nodes.values())} entradas -> {out}")


if __name__ == "__main__":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    build()
