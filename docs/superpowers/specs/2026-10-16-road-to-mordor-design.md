# Libro II: de Lothlórien al Monte del Destino — diseño

Fecha: 2026-10-16. Estado: aprobado en conversación. Elecciones del usuario: ruta A «hacia Mordor», una sola aventura más larga y final A «a la sombra de la Comunidad hasta el Monte del Destino».

## 1. Objetivo

El usuario se ha pasado la aventura dos veces y se le queda corta. Se alarga la historia de Bree hasta el **Monte del Destino**.

- Lothlórien deja de ser el final y pasa a ser una zona de descanso, como Rivendel.
- Se añaden 8 zonas con combates y un nodo final.
- La ruta pasa de 15 a **24 zonas**.

**Canon:** el Warden sigue el rastro de la Comunidad un paso por detrás y **nunca** hace lo que hacen ellos. No destruye el Anillo, no lucha junto a Frodo y no mata a ningún personaje canónico. En Gorgoroth atrae a orcos y nazgûl lejos del camino del portador. El epílogo muestra la erupción del Orodruin, la caída de Barad-dûr, las águilas y al Warden, que sobrevive.

Fuera de alcance:
- un selector de libros;
- cambiar las 14 zonas existentes, salvo Lothlórien;
- generar las imágenes, que hará el usuario con Gemini.

## 2. Ruta (`engine/.../story/Route.kt`)

| # | id | nombre | baseLevel | tipo |
|---|---|---|---|---|
| 1–14 | (las actuales) | | | sin cambios |
| 15 | `lothlorien` | Lothlórien | 40 | **descanso**: árbol 1-2-4-8-4-2-1 sin combates; el nivel 7 es la partida en barcas |
| 16 | `anduin` | Río Anduin | 40 | combate |
| 17 | `argonath` | Sarn Gebir y los Argonath | 43 | combate |
| 18 | `amonhen` | Amon Hen | 46 | combate |
| 19 | `emynmuil` | Emyn Muil | 49 | combate |
| 20 | `cienagas` | Ciénagas de los Muertos | 52 | combate |
| 21 | `ithilien` | Ithilien | 55 | combate |
| 22 | `cirithungol` | Valle de Morgul y Cirith Ungol | 58 | combate |
| 23 | `gorgoroth` | Gorgoroth | 61 | combate |
| 24 | `orodruin` | Monte del Destino | 0 | **final**: un solo nodo `orodruin_final` (epílogo) |

- **Final y descanso.** `Route.FINAL_ZONE_ID = "orodruin"` y la clave del nodo final es `"${FINAL_ZONE_ID}_final"`. `Route.REST_ZONES = setOf("rivendel", "lothlorien")` sustituye todos los casos especiales de `"rivendel"` en el código y en los tests.
- **Curva de niveles.** `baseLevel` sigue la curva desplazada una zona por cada zona de descanso que la precede: Lothlórien hereda el nivel de Nimrodel + 3 y Anduin comparte el de Lothlórien, igual que Rivendel y Eregion.

## 3. Contenido (`engine/src/main/resources/story/*.json`)

Mismas reglas que la spec de contenido (`2026-10-08-story-content-design.md` §2 y §5):
- texto en segunda persona, de 60 a 120 palabras;
- `choiceLabel` de 2 a 6 palabras;
- `imagePrompt` en inglés con el sufijo exacto;
- encuentros de 1 a 4 rivales con sus offsets;
- el jefe en el nivel 7 con escoltas opcionales;
- todo rival hace daño y lleva `portrait`;
- JSON en UTF-8 sin BOM.

**Sinopsis (guía de continuidad):**
- **Lothlórien (descanso):** Caras Galadhon, la Dama, el espejo, el saber de los Galadhrim, los regalos (capa, lembas, cuerda), los rumores de que la Comunidad partió río abajo y la partida en barcas al amanecer. *El antiguo nodo final pasa a ser `lothlorien_1_0`; su imagen se renombra a `lothlorien_1_0.webp`.*
- **Anduin:** días de río entre bosques. Desde la orilla este disparan orcos, huargos rondan las riberas y hay restos de un campamento de la Comunidad.
- **Argonath:** los rápidos de Sarn Gebir, el porteo, los uruk-hai de la Mano Blanca y el paso entre los dos reyes de piedra.
- **Amon Hen:** la colina del Ojo, el asiento de piedra, la batalla reciente, cuernos lejanos, crebain y uruk-hai rezagados.
- **Emyn Muil:** laberinto de roca gris, frío, cuerdas, grietas, lobos de las rocas, orcos de Mordor y un trol de las colinas.
- **Ciénagas:** caras bajo el agua, luces pálidas, niebla y frío. Fuegos fatuos y espectros de la ciénaga; el nazgûl vuela alto.
- **Ithilien:** bosque de hierbas y ruinas de Gondor; montaraces que no se dejan ver, haradrim y un olifante de guerra.
- **Cirith Ungol:** el valle de Morgul con su luz verde, la escalera, el túnel, las telarañas y orcos de Morgul. El jefe es Ella-Laraña: el Warden la hiere y huye, sin matarla.
- **Gorgoroth:** ceniza, grietas de lava, campamentos de orcos, uruk negros, olog-hai y el nazgûl en su bestia alada en las faldas del Orodruin.
- **Monte del Destino (final, de 100 a 160 palabras):** la montaña estalla, la torre cae, las águilas surcan el cielo y el Warden sobrevive y emprende el regreso. La pantalla de fin se muestra aquí.

**Bestiario nuevo** (patrones, `everyNTicks` y offsets se fijan en el plan y se ajustan con el simulador).

Comunes, con su retrato entre paréntesis:

| tipo | retrato |
|---|---|
| arquero orco | `orc_archer` (existe) |
| huargo | `warg` (existe) |
| crebain | `crebain` (existe) |
| uruk-hai | `uruk` |
| orco de Mordor | `mordor_orc` |
| lobo de las rocas | `wolf` (existe) |
| fuego fatuo | `wisp` |
| espectro de la ciénaga | `marsh_wraith` |
| guerrero haradrim | `haradrim` |
| arquero haradrim | `haradrim` |
| orco de Morgul | `mordor_orc` |
| araña | `spider` |
| uruk negro | `uruk` |
| olog-hai | `troll` (existe) |

Retratos comunes nuevos: `uruk`, `mordor_orc`, `wisp`, `marsh_wraith`, `haradrim`, `spider`. Con los 12 actuales suman 18.

Jefes, cada uno con su retrato `boss_<zona>`:
- Cabecilla de los saqueadores del río;
- Capitán uruk de la Mano Blanca;
- Caudillo uruk-hai;
- Trol de las colinas;
- Señor de los ahogados;
- Olifante de guerra;
- Ella-Laraña;
- Nazgûl en su bestia alada.

Todos llevan al menos un Aturdir (`31`), como los actuales.

## 4. Motor y partida

- **Pasos de motor.** `TreeShape`, `StoryEngine` (`isValid`, la fase `LAP_COMPLETE`, `ZoneEntered`), `ContentValidator` y `StoryContentLoader` usan `Route.REST_ZONES` y `NodeId.FINAL`, ya movido a `orodruin`.
- **Migración de guardados.** Un guardado en `NodeId("lothlorien", 1, 0)` con fase `LAP_COMPLETE` (el antiguo final) pasa a fase `CHOOSING` en ese mismo nodo, que ahora es el primero del Lothlórien de descanso. Conserva nivel, victorias, vuelta, muertes e historial.
  - Cualquier otro guardado no cambia.
  - La migración es una función pura de `:engine` con test, y se aplica al cargar.
- **Simulador.** `:sim` recorre la ruta de 24 zonas sin cambios de código más allá de las referencias a `"rivendel"`.

## 5. App

- **Retratos.** `PortraitSprites` suma los 6 tipos nuevos, de 16×16 y reconocibles, y la tabla de tipo base de los 8 jefes nuevos:
  - río → `orc_archer`;
  - argonath → `uruk`;
  - amonhen → `uruk`;
  - emynmuil → `troll`;
  - cienagas → `marsh_wraith`;
  - ithilien → `haradrim`;
  - cirithungol → `spider`;
  - gorgoroth → `marsh_wraith`, como figura espectral, mientras no haya imagen.
- **Prompts para Gemini.**
  - `docs/content/prompts-retratos-pixelart.md` suma los 6 tipos y los 8 jefes.
  - `docs/content/prompts-imagenes-pixelart.md` suma una fila por cada nodo nuevo, unos 200, con el mismo estilo pixel-art que los actuales, más `orodruin_final`.
  - Los `imagePrompt` del JSON siguen el estilo pintado de la spec de contenido.
- **Mapa.** `ZoneKeys` incorpora las 9 zonas nuevas con biomas nuevos:

  | bioma | aspecto |
  |---|---|
  | `bigriver` | gran río que ocupa una banda ancha |
  | `cliffs` | acantilados con dos estatuas gigantes en los Argonath |
  | `hillruins` | colina boscosa con asiento de piedra |
  | `greyrock` | laberinto de roca gris |
  | `deadmarsh` | ciénaga muerta, agua oscura y luces |
  | `herbs` | Ithilien: verde intenso, flores, ruinas |
  | `morgul` | roca oscura con resplandor verde pálido |
  | `ash` | ceniza gris |
  | `lava` | grietas incandescentes |

  - El Orodruin es un cono volcánico grande al final, con un río de lava.
  - **Animación:** la lava brilla y palpita, los fuegos fatuos flotan en la ciénaga y el humo sube del Orodruin.
  - `WORLD_GEN_VERSION` sube.
  - El test de continuidad y el de camino continuo cubren las 24 zonas.
- **Pantalla de fin.** Se muestra al llegar a `orodruin_final`, sin otros cambios.

## 6. Balance (`:sim`, `docs/balance/tuning.md`)

Se re-simula la aventura de 24 zonas y se documenta la sección «Libro II: hasta el Monte del Destino» con la tabla antes/después.

**Objetivos:**
- comunes MEDIO ≥ 88 % en todas las zonas;
- jefes MEDIO entre 55 y 85 %, salvo el Nazgûl de Gorgoroth, entre 45 y 65 %;
- aventura completa MEDIO sin muertes: 2–5 %; EXPERTO: 10–20 %;
- 0 combates al tope de ticks y 0 aventuras abandonadas.

Se ajusta solo el contenido nuevo (patrones, offsets y grupos). `RivalScaling` no cambia salvo que sea imprescindible, para no tocar el balance de las 14 zonas ya probadas.

## 7. Pruebas

- **Motor:**
  - `Route` tiene 24 zonas y `REST_ZONES`;
  - el nodo final es `orodruin_final`;
  - la migración del guardado antiguo, con y sin `LAP_COMPLETE`;
  - `RealContentTest` cubre las 24 zonas: sin combates en las de descanso, jefes y retratos, grupos y reglas;
  - una aventura completa del simulador llega a `orodruin_final`.
- **App:**
  - sprites de los tipos nuevos;
  - la tabla de tipo base de los jefes;
  - el mapa de 24 zonas (continuidad, camino, Orodruin y lava presentes);
  - la pantalla de fin en `orodruin_final`.
- **Verificación:**
  - `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`;
  - capturas del mapa nuevo en el emulador;
  - instalación en el móvil, solo si está conectado; sin jugar en la partida del usuario.
