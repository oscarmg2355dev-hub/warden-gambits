"""Genera una web local con todos los nodos de la historia, su imagen y sus elecciones, para revisar qué nodos faltan
por probar en el juego.

Uso (desde la raíz del proyecto):
    python tools/node-browser/build_node_browser.py
    python -m http.server 8765        # y abre http://localhost:8765/build/node-browser/

Las imágenes se leen de app/src/main/res/drawable-nodpi; un nodo sin imagen se pinta como en el móvil (degradado del
color de la zona con su nombre). Las casillas «Probado» solo se guardan en el navegador (localStorage).
"""
import argparse
import html
import json
import os
import re

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
STORY = os.path.join(ROOT, "engine", "src", "main", "resources", "story")
ROUTE = os.path.join(ROOT, "engine", "src", "main", "kotlin", "com", "wardengambits", "engine", "story", "Route.kt")
NODE_IMAGE = os.path.join(ROOT, "app", "src", "main", "java", "com", "wardengambits", "app", "ui", "common", "NodeImage.kt")
DRAWABLES = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")
OUT_DIR = os.path.join(ROOT, "build", "node-browser")

LEVEL_SIZES = [1, 2, 4, 8, 4, 2, 1]
FINAL_ZONE = "orodruin"


def route_zones():
    src = open(ROUTE, encoding="utf-8").read()
    zones = re.findall(r'ZoneInfo\((?:"(\w+)"|FINAL_ZONE_ID), "([^"]+)"\)', src)
    rest = set(re.findall(r'"(\w+)"', re.search(r"REST_ZONES[^\n]*", src).group(0)))
    return [(zid or FINAL_ZONE, name) for zid, name in zones], rest


def zone_colors():
    src = open(NODE_IMAGE, encoding="utf-8").read()
    block = src[src.index("ZoneColors"):src.index(")\n\n", src.index("ZoneColors"))]
    return ["#" + h[2:] for h in re.findall(r"Color\(0x([0-9A-Fa-f]{8})\)", block)]


def next_in_zone(level, index):
    cur, nxt = LEVEL_SIZES[level - 1], LEVEL_SIZES[level]
    if nxt > cur:
        idx = [2 * index, 2 * index + 1]
    else:
        half = index // 2
        idx = [half, (half + 1) % nxt]
    return [(level + 1, i) for i in sorted(set(idx))]


def node_key(zone, level, index):
    return f"{FINAL_ZONE}_final" if zone == FINAL_ZONE else f"{zone}_{level}_{index}"


def build():
    zones, rest = route_zones()
    colors = zone_colors()
    images = {os.path.splitext(f)[0] for f in os.listdir(DRAWABLES)}
    content = {}
    for zid, _ in zones:
        data = json.load(open(os.path.join(STORY, f"{zid}.json"), encoding="utf-8"))
        content[zid] = {(n["level"], n["index"]): n for n in data["nodes"]}

    def label(zid, level, index):
        n = content[zid].get((level, index))
        return (n or {}).get("choiceLabel")

    def title(zid, level, index):
        n = content[zid].get((level, index))
        return (n or {}).get("title", "")

    nodes_total = sum(len(c) for c in content.values())
    with_image = 0
    sections = []
    for zi, (zid, zname) in enumerate(zones):
        color = colors[zi] if zi < len(colors) else "#555555"
        rows = []
        for level in range(1, 8):
            cards = []
            for index in range(LEVEL_SIZES[level - 1]):
                n = content[zid].get((level, index))
                if n is None:
                    continue
                key = node_key(zid, level, index)
                has_img = key in images
                with_image += has_img
                if zid == FINAL_ZONE:
                    dests = []
                elif level == 7:
                    nz = zones[zi + 1][0]
                    dests = [(nz, 1, 0)]
                else:
                    dests = [(zid, l, i) for l, i in next_in_zone(level, index)]
                cards.append(card(zid, zname, color, key, level, index, n, has_img, dests, label, title, zones))
            rows.append(f'<div class="level"><div class="lvl">Nivel {level}</div><div class="cards">{"".join(cards)}</div></div>')
        tags = '<span class="tag rest">zona de descanso</span>' if zid in rest else ""
        sections.append(
            f'<section class="zone" id="z-{zid}" data-zone="{zid}" style="--zone:{color}">'
            f'<h2><span class="zn">{zi + 1}. {html.escape(zname)}</span> {tags}'
            f'<span class="zstat" data-zstat="{zid}"></span></h2>{"".join(rows)}</section>'
        )

    options = "".join(f'<option value="z-{z}">{i + 1}. {html.escape(n)}</option>' for i, (z, n) in enumerate(zones))
    page = TEMPLATE.replace("{{SECTIONS}}", "".join(sections)).replace("{{OPTIONS}}", options)
    page = page.replace("{{TOTAL}}", str(nodes_total)).replace("{{WITH_IMAGE}}", str(with_image))
    os.makedirs(OUT_DIR, exist_ok=True)
    out = os.path.join(OUT_DIR, "index.html")
    open(out, "w", encoding="utf-8").write(page)
    print(f"{nodes_total} nodos, {with_image} con imagen -> {out}")


def card(zid, zname, color, key, level, index, n, has_img, dests, label, title, zones):
    esc = html.escape
    img_rel = f"../../app/src/main/res/drawable-nodpi/{key}.webp"
    if has_img:
        picture = f'<img loading="lazy" src="{img_rel}" alt="{esc(n["title"])}">'
    else:
        picture = f'<div class="fallback"><span>{esc(zname)}</span></div>'
    enc = n.get("encounter")
    combat = ""
    if enc:
        rivals = []
        for r in enc["rivals"]:
            boss = "modes" in r or "boss" in r.get("portrait", "")
            heals = " · se cura" if "reaction" in r else ""
            rivals.append(f'<li class="{"boss" if boss else ""}">{"👑 " if boss else ""}{esc(r["name"])}'
                          f' <small>({r.get("levelOffset", 0):+d}{heals})</small></li>')
        combat = f'<div class="combat">⚔ Combate<ul>{"".join(rivals)}</ul></div>'
    arrive = f'<div class="arrive">Se llega eligiendo: «{esc(n["choiceLabel"])}»</div>' if n.get("choiceLabel") else ""
    links = []
    for dz, dl, di in dests:
        dkey = node_key(dz, dl, di)
        lbl = label(dz, dl, di)
        tt = title(dz, dl, di)
        prefix = "" if dz == zid else f"{next(name for z, name in zones if z == dz)} · "
        shown = f"«{lbl}»" if lbl else esc(tt)
        links.append(f'<a href="#n-{dkey}">{esc(prefix)}{dkey.split("_", 1)[1] if dz != FINAL_ZONE else "final"}: '
                     f'{esc(shown) if lbl else shown}</a>')
    dest_html = f'<div class="dests">Lleva a:{"".join(links)}</div>' if links else '<div class="dests">Fin de la aventura</div>'
    flags = ("combat" if enc else "") + (" noimg" if not has_img else "")
    short = "final" if zid == FINAL_ZONE else f"{level}_{index}"
    return (
        f'<article class="node {flags}" id="n-{key}" data-key="{key}">'
        f'<div class="pic">{picture}{"" if has_img else "<span class=nobadge>sin imagen</span>"}</div>'
        f'<div class="body"><div class="head"><code>{short}</code>'
        f'<label class="tested"><input type="checkbox" data-check="{key}"> Probado</label></div>'
        f'<h3>{esc(n["title"])}</h3>{arrive}{combat}'
        f'<details><summary>Texto</summary><p>{esc(n["text"])}</p></details>{dest_html}'
        f'</div></article>'
    )


TEMPLATE = """<!doctype html>
<html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Nodos de Warden-Gambits</title>
<style>
@font-face { font-family: Cinzel; src: url("../../app/src/main/res/font/cinzel.ttf"); }
:root { --bg:#15110D; --surface:#241C14; --gold:#C9A44C; --text:#EDE3CF; --muted:#A89C86; --ok:#6FA35A; }
* { box-sizing: border-box; }
body { margin:0; background:var(--bg); color:var(--text); font:15px/1.4 system-ui, sans-serif; }
header { position:sticky; top:0; z-index:5; background:#0f0c09ee; border-bottom:1px solid #3a2f22; padding:10px 16px;
  display:flex; flex-wrap:wrap; gap:10px 18px; align-items:center; backdrop-filter: blur(4px); }
header h1 { font-family:Cinzel, serif; color:var(--gold); font-size:20px; margin:0; }
.stats b { color:var(--gold); }
.filters button, select { background:var(--surface); color:var(--text); border:1px solid #4a3c2a; border-radius:6px;
  padding:5px 10px; font:inherit; cursor:pointer; }
.filters button.on { background:var(--gold); color:#1a140c; border-color:var(--gold); }
main { padding:8px 16px 60px; }
.zone { margin:26px 0; padding-top:4px; border-top:3px solid var(--zone); }
.zone h2 { font-family:Cinzel, serif; color:var(--gold); font-size:22px; display:flex; flex-wrap:wrap; gap:10px; align-items:baseline; }
.zstat { font:14px system-ui, sans-serif; color:var(--muted); }
.zstat.done { color:var(--ok); }
.tag { font:12px system-ui, sans-serif; padding:2px 8px; border-radius:10px; background:#3a4a3a; color:#cfe3c0; }
.level { display:flex; gap:10px; margin:10px 0; align-items:flex-start; }
.lvl { flex:0 0 64px; color:var(--muted); font-size:12px; padding-top:6px; }
.cards { display:flex; flex-wrap:wrap; gap:10px; justify-content:center; flex:1; }
.node { width:230px; background:var(--surface); border:1px solid #3a2f22; border-radius:8px; overflow:hidden; position:relative; }
.node.done { border-color:var(--ok); box-shadow:0 0 0 1px var(--ok) inset; }
.node.done .pic { opacity:.55; }
.node:target { outline:3px solid var(--gold); }
.pic { position:relative; aspect-ratio:4/3; background:#000; }
.pic img { width:100%; height:100%; object-fit:cover; display:block; }
.fallback { width:100%; height:100%; display:flex; align-items:center; justify-content:center; text-align:center;
  background:linear-gradient(var(--zone), var(--bg)); padding:12px; }
.fallback span { font-family:Cinzel, serif; font-size:20px; color:var(--text); }
.nobadge { position:absolute; top:6px; right:6px; background:#7a2a1a; color:#fff; font-size:11px; padding:1px 6px; border-radius:8px; }
.body { padding:8px 10px 10px; }
.head { display:flex; justify-content:space-between; align-items:center; }
code { color:var(--gold); font-size:13px; }
.tested { font-size:13px; cursor:pointer; user-select:none; }
.tested input { accent-color:var(--ok); width:16px; height:16px; vertical-align:-3px; }
h3 { margin:4px 0; font-size:15px; }
.arrive { font-size:12px; color:var(--muted); font-style:italic; }
.combat { margin-top:6px; font-size:12px; color:#e0a08a; }
.combat ul { margin:2px 0 0; padding-left:16px; color:var(--text); }
.combat li.boss { color:var(--gold); font-weight:600; }
details { margin-top:6px; font-size:13px; }
summary { cursor:pointer; color:var(--muted); }
details p { margin:4px 0 0; color:#d8ccb4; }
.dests { margin-top:6px; font-size:12px; color:var(--muted); display:flex; flex-direction:column; gap:2px; }
.dests a { color:var(--text); text-decoration:none; border-left:2px solid var(--gold); padding-left:6px; }
.dests a:hover { color:var(--gold); }
.hidden { display:none !important; }
@media (max-width:600px) { .level { flex-direction:column; } .node { width:100%; max-width:340px; } }
</style></head>
<body>
<header>
  <h1>Nodos de Warden-Gambits</h1>
  <span class="stats">Probados <b id="nTested">0</b>/{{TOTAL}} · Con imagen <b>{{WITH_IMAGE}}</b>/{{TOTAL}}</span>
  <span class="filters">
    <button data-filter="all" class="on">Todos</button>
    <button data-filter="untested">Sin probar</button>
    <button data-filter="noimg">Sin imagen</button>
    <button data-filter="combat">Con combate</button>
  </span>
  <select id="jump"><option value="">Ir a zona…</option>{{OPTIONS}}</select>
</header>
<main>{{SECTIONS}}</main>
<script>
const STORE = "warden-node-tested";
let tested = {};
try { tested = JSON.parse(localStorage.getItem(STORE) || "{}"); } catch (e) { tested = {}; }
let filter = "all";

function isDone(key) { return !!tested[key]; }

function refresh() {
  let total = 0;
  document.querySelectorAll(".node").forEach(n => {
    const key = n.dataset.key, done = isDone(key);
    n.classList.toggle("done", done);
    n.querySelector("[data-check]").checked = done;
    if (done) total++;
    const show = filter === "all" || (filter === "untested" && !done) || (filter === "noimg" && n.classList.contains("noimg"))
      || (filter === "combat" && n.classList.contains("combat"));
    n.classList.toggle("hidden", !show);
  });
  document.getElementById("nTested").textContent = total;
  document.querySelectorAll(".zone").forEach(z => {
    const nodes = z.querySelectorAll(".node"), done = [...nodes].filter(n => isDone(n.dataset.key)).length;
    const s = z.querySelector("[data-zstat]");
    s.textContent = `probados ${done}/${nodes.length}`;
    s.classList.toggle("done", done === nodes.length);
    z.querySelectorAll(".level").forEach(l => l.classList.toggle("hidden", !l.querySelector(".node:not(.hidden)")));
    z.classList.toggle("hidden", !z.querySelector(".node:not(.hidden)"));
  });
}

document.addEventListener("change", e => {
  const key = e.target.dataset && e.target.dataset.check;
  if (!key) return;
  if (e.target.checked) tested[key] = true; else delete tested[key];
  try { localStorage.setItem(STORE, JSON.stringify(tested)); } catch (err) {}
  refresh();
});
document.querySelectorAll("[data-filter]").forEach(b => b.addEventListener("click", () => {
  filter = b.dataset.filter;
  document.querySelectorAll("[data-filter]").forEach(x => x.classList.toggle("on", x === b));
  refresh();
}));
document.getElementById("jump").addEventListener("change", e => { if (e.target.value) location.hash = e.target.value; });
refresh();
</script>
</body></html>
"""


def main():
    build()


if __name__ == "__main__":
    main()
