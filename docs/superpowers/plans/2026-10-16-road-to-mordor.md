# Libro II: hasta el Monte del Destino — plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:** alargar la aventura de 15 a 24 zonas, hasta el Monte del Destino. Lothlórien pasa a ser una zona de descanso, entran 8 zonas de combate y el final pasa a `orodruin_final`. Incluye contenido, bestiario, migración de guardados, retratos, prompts, mapa y balance.

**Architecture:**
- **Antes del cambio de ruta:** el motor se generaliza (zonas de descanso y final derivados de `Route`) sin cambiar aún la ruta. El contenido nuevo se escribe y se valida como «zonas pendientes».
- **Cambio de ruta (un solo paso):** se activa la ruta de 24 zonas, la migración y el nuevo final.
- **Después:** la app (retratos, mapa) y el balance.

**Tech Stack:** Kotlin 2.4.20, JUnit 5 (engine), kotlinx-serialization, Compose + Robolectric (app), `:sim`.

**Spec:** `docs/superpowers/specs/2026-10-16-road-to-mordor-design.md`

## Global Constraints

- **Sin git:** se omiten los pasos de commit.
- **Entorno:** `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`.
- **Idiomas:** código en inglés; textos, docs y strings en español.
- **Finales de línea:** se conservan los de cada fichero.
- **JSON:** UTF-8 sin BOM.
- **`:engine`:** puro y determinista. La única dependencia externa es kotlinx-serialization-json. `when` exhaustivo, sin `else`.
- **Reglas de contenido** (spec de contenido §2 y §5):
  - texto en segunda persona del presente, de 60 a 120 palabras por nodo, historia original sin citar los libros;
  - `choiceLabel` de 2 a 6 palabras, en imperativo y con sentido desde cualquier padre;
  - `imagePrompt` en inglés, de 25 a 60 palabras, terminando en `, painterly dark fantasy illustration, Middle-earth, muted earthy palette, 2D game card art, no text`;
  - nivel 1 sin encuentro;
  - niveles 2–6 con 1–4 comunes (offsets: 1 rival 0/+1, 2 rivales −2, 3 rivales −4, 4 rivales −5);
  - nivel 7 con el jefe primero (offset +2..+12) y 0–2 escoltas a −4;
  - de 2 a 4 combates por camino;
  - todo rival con `portrait` y al menos la mitad de sus combos de daño con una cadena de daño real;
  - todo jefe con al menos un `31`.
- **Canon:** el Warden va un paso por detrás de la Comunidad y nunca hace sus hechos. Ella-Laraña es herida, no muerta. Nadie canónico muere a manos del Warden.
- **Ruta nueva:**
  - ids y baseLevel: `lothlorien` 40 (descanso), `anduin` 40, `argonath` 43, `amonhen` 46, `emynmuil` 49, `cienagas` 52, `ithilien` 55, `cirithungol` 58, `gorgoroth` 61, `orodruin` 0 (final, nodo `orodruin_final`);
  - `REST_ZONES = {rivendel, lothlorien}`.
- **Retratos nuevos:** `uruk`, `mordor_orc`, `wisp`, `marsh_wraith`, `haradrim`, `spider`; jefes `boss_<zona>` para las 8 zonas de combate nuevas.
- **Objetivos de balance:**
  - comunes MEDIO ≥ 88 %;
  - jefes MEDIO 55–85 % (el de Gorgoroth, 45–65 %);
  - aventura MEDIO sin muertes 2–5 %, EXPERTO 10–20 %;
  - 0 al tope de ticks y 0 abandonadas;
  - `RivalScaling` sin cambios salvo que sea imprescindible.

## Review Focus

1. **Guardado en el antiguo final con fase `LAP_COMPLETE`:** tras actualizar, continúa en `lothlorien_1_0` en `CHOOSING` con nivel, victorias, vuelta y muertes intactos. Un guardado en mitad de Nimrodel no cambia. Test en la Tarea 5.
2. **Historial de vueltas antiguas** (registros con `NodeId.FINAL` antiguo): se siguen mostrando en la pantalla de personaje sin fallar. Test en la Tarea 5.
3. **Zona de descanso Lothlórien:** ningún nodo tiene encuentro y su nivel 7 lleva a `anduin_1_0`. Test en las tareas 2 y 5.
4. **Imagen del antiguo final:** `lothlorien_1_0` usa la imagen de Caras Galadhon, y `orodruin_final` sin imagen muestra el degradado. Test en la Tarea 6.
5. **Mapa de 24 zonas en pantallas bajas** (40 filas): camino continuo, paradas dentro y Orodruin visible. Test en la Tarea 7.

---

### Task 1: Generalizar el motor y preparar el bestiario (sin cambiar la ruta)

**Files:**
- Modify:
  - `engine/.../story/Route.kt` (`REST_ZONES`; la clave del final se deriva de `FINAL_ZONE_ID`)
  - `NodeId.kt` (`key` del final = `"${Route.FINAL_ZONE_ID}_final"`)
  - `TreeShape.kt`, `StoryEngine.kt`, `content/ContentValidator.kt`, `content/StoryContentLoader.kt`
  - los tests que usan `"rivendel"` (`RealContentTest`, `ContentRulesTest`, …), que pasan a `Route.REST_ZONES`
  - `engine/src/test/.../story/ContentRules.kt` (bestiario nuevo)
  - `docs/superpowers/specs/2026-10-08-story-content-design.md` §3 (tablas nuevas)
- Create:
  - `engine/.../story/SaveMigration.kt` (función pura, aún inactiva)
  - `engine/src/test/.../story/PendingContentTest.kt`

**Interfaces:**
- Produces:
  - `Route.REST_ZONES: Set<String>`;
  - `object SaveMigration { fun migrate(profile: StoryProfile): StoryProfile }`. Si `run.node == NodeId("lothlorien", 1, 0) && run.phase == Phase.LAP_COMPLETE`, devuelve fase `CHOOSING`; si no, lo deja igual. Solo tiene efecto con la ruta nueva, y la Tarea 5 la conecta al cargar.
  - `StoryContentLoader.loadZone(resourceDir: String, zoneId: String, baseLevel: Int): ZoneContent` (o equivalente), que usará `PendingContentTest`.
  - `PendingContentTest`: valida, con el validador y `ContentRules`, cada zona nueva cuyo JSON ya exista. Usa la tabla id → baseLevel de Global Constraints. Los ficheros pendientes viven en `engine/src/main/resources/story-pending/<id>.json` hasta la Tarea 5.
  - `ContentRules`:
    - las entradas de comunes nuevos (nombre de tipo, patrón, `everyNTicks`, `portrait`): uruk-hai, orco de Mordor, lobo de las rocas, fuego fatuo, espectro de la ciénaga, guerrero haradrim, arquero haradrim, orco de Morgul, araña, uruk negro, olog-hai, más los existentes (arquero orco, huargo, crebain);
    - las 8 entradas de jefe;
    - `COMMON_PORTRAITS` con los 6 nuevos (18 en total).

  Diseña los patrones con las familias del catálogo v2, siguiendo los criterios de la spec de contenido §3: daño ≥ 50 %, cadena real, jefes con `31`. Los offsets iniciales de jefe van de +4 a +8; la Tarea 8 los ajusta.

- [ ] **Step 1: Write the failing tests**
  - `restZonesHaveNoEncounters`, sobre `REST_ZONES` en lugar de `"rivendel"`;
  - `finalKeyDerivesFromRoute`, `NodeId.FINAL.key == "${Route.FINAL_ZONE_ID}_final"` (hoy sigue siendo «lothlorien_final»);
  - `migrateOldFinal`: un perfil en `NodeId("lothlorien",1,0)` con `LAP_COMPLETE` sale en `CHOOSING`, con los mismos `level`, `wins`, `lap`, `deaths` e historial;
  - `migrateLeavesOthers`: un perfil en `nimrodel_3_1` en `CHOOSING` sale igual;
  - `newBestiaryRulesHold`: cada entrada nueva de `ContentRules` cumple daño ≥ 50 % y cadena real, y los jefes tienen `31`;
  - `pendingZonesValidate`: no hace nada si no hay ficheros pendientes.
- [ ] **Step 2: Run** `./gradlew :engine:test`. Expected: FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest`. Expected: PASS. La ruta y el contenido actuales no cambian.

### Task 2: Contenido I — Lothlórien (descanso), Anduin y Argonath

**Files:**
- Create:
  - `engine/src/main/resources/story-pending/lothlorien.json`: árbol de 22 nodos sin encuentros. Su `lothlorien_1_0` es la llegada a Caras Galadhon; reutiliza y adapta el texto del antiguo final, de 60 a 120 palabras. El nivel 7 es la partida en barcas.
  - `story-pending/anduin.json`
  - `story-pending/argonath.json`
- Modify: `docs/content/prompts-imagenes-pixelart.md`, con una fila por nodo nuevo `| <key>.webp | <título> | <prompt pixel-art> |` en el mismo estilo que las existentes. La fila de `lothlorien_1_0` reutiliza la de `lothlorien_final`.

**Interfaces:** consume el bestiario y `PendingContentTest` de la Tarea 1.

- [ ] **Step 1:** escribe los tres JSON siguiendo la sinopsis de la spec §3 y las reglas de Global Constraints. Los grupos de 4 aparecen al menos una vez por zona de combate.
- [ ] **Step 2: Run** `./gradlew :engine:test --tests "*PendingContentTest*"`. Expected: PASS (las tres zonas validan).
- [ ] **Step 3:** comprueba con un script (Python) el recuento de palabras (60–120) de cada nodo y la terminación de cada `imagePrompt`, y pega el resumen en el informe.

### Task 3: Contenido II — Amon Hen, Emyn Muil y Ciénagas

Igual que la Tarea 2, para `story-pending/amonhen.json`, `emynmuil.json` y `cienagas.json`, con sus filas en `prompts-imagenes-pixelart.md`.

- [ ] **Step 1:** escribe los JSON.
- [ ] **Step 2: Run** `PendingContentTest`. Expected: PASS.
- [ ] **Step 3:** comprobación de palabras y prompts.

### Task 4: Contenido III — Ithilien, Cirith Ungol, Gorgoroth y el final

Igual que la Tarea 2, para `story-pending/ithilien.json`, `cirithungol.json` y `gorgoroth.json`, más `story-pending/orodruin.json`. Este último es la zona final, con el único nodo `orodruin_final` y un epílogo de 100 a 160 palabras: la erupción, la caída de la torre, las águilas y el Warden que sobrevive. Ella-Laraña, en `cirithungol_7_0`, es herida y huye. El Nazgûl es el jefe de `gorgoroth_7_0`.

- [ ] **Step 1:** escribe los JSON.
- [ ] **Step 2: Run** `PendingContentTest`. Expected: PASS.
- [ ] **Step 3:** comprobación de palabras y prompts, con el epílogo entre 100 y 160 palabras.

### Task 5: Activar la ruta de 24 zonas, la migración y el nuevo final

**Files:**
- Modify:
  - `Route.kt` (24 zonas, `FINAL_ZONE_ID = "orodruin"`, `REST_ZONES`)
  - mover los 10 JSON de `story-pending/` a `story/`, sustituyendo el `lothlorien.json` antiguo
  - conectar `SaveMigration.migrate` en la carga del guardado (`app/.../data/SaveRepository.kt` o `GameSession`, donde se deserialice el `StoryProfile`)
  - `RealContentTest` (`WRITTEN_ZONES` con 24 y conteos)
  - `app/src/main/res/drawable-nodpi/lothlorien_final.webp` → renombrar a `lothlorien_1_0.webp`
  - `:sim` si referencia `"lothlorien"` o `"rivendel"` a mano
  - los tests de app con `lothlorien_final` (`EndingRoutingTest`, `NodeScreenTest`, …)
- Delete: `PendingContentTest`, o déjalo vacío sin ficheros pendientes.

**Interfaces:** consume `SaveMigration` y los JSON de las tareas 1–4.

- [ ] **Step 1: Write the failing tests**
  - `routeHas24Zones`: `Route.zones.size == 24`, `Route.zones.last().id == "orodruin"`, `NodeId.FINAL.key == "orodruin_final"`.
  - `fullLapReachesOrodruin`: el test existente de vuelta completa llega a `orodruin_final` en `LAP_COMPLETE`.
  - `oldFinalSaveContinuesInLothlorien` (app): un guardado serializado en el antiguo final con `LAP_COMPLETE` se carga en `lothlorien_1_0` con `CHOOSING`, y la pantalla de mapa (no la de fin) es la ruta inicial.
  - `oldHistoryRecordsStillRender` (app): la pantalla de personaje con un `RunRecord` antiguo (nodo `lothlorien` 1/0) se pinta sin fallar.
  - `simAdventureReachesOrodruin`: una aventura MEDIO del simulador termina en `orodruin_final`.
- [ ] **Step 2: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest`. Expected: FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.

### Task 6: Retratos nuevos y prompts de retrato (`:app`, docs)

**Files:**
- Modify:
  - `app/.../ui/common/Portraits.kt` (6 sprites nuevos de 16×16 y la tabla de tipo base de los 8 jefes de la spec §5)
  - `PortraitsTest.kt`
  - `docs/content/prompts-retratos-pixelart.md` (6 tipos y 8 jefes, mismo formato y estilo)

- [ ] **Step 1: Write the failing tests:**
  - los sprites nuevos tienen 16×16 con colores de paleta;
  - `baseType("boss_cirithungol") == "spider"` y `baseType("boss_gorgoroth") == "marsh_wraith"`;
  - `TYPES` tiene 18;
  - `lothlorien_1_0` tiene imagen empaquetada y `orodruin_final` muestra el degradado (test Compose de `NodeImage`).
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*Portraits*" --tests "*NodeScreen*"`. Expected: FAIL.
- [ ] **Step 3: Implement.** Dibuja siluetas reconocibles:
  - uruk: casco y mano blanca;
  - orco de Mordor: ojo rojo;
  - fuego fatuo: luz pálida;
  - espectro de la ciénaga: rostro bajo el agua;
  - haradrim: turbante rojo;
  - araña.

  Renderiza la hoja de contactos en `.superpowers/sdd/road-to-mordor/sprites.png`.
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest`. Expected: PASS. Comprueba también que `python tools/gemini/generate_images.py generate --set portraits --dry-run` lista las 40 filas.

### Task 7: Mapa de 24 zonas con biomas nuevos (`:app`)

**Files:**
- Modify:
  - `app/.../ui/map/world/ZoneKeys.kt`, `WorldGen.kt`, `WorldSprites.kt`
  - `ui/map/MapAmbience.kt`
  - `WorldGenTest.kt`, `MapAmbienceTest.kt`

**Interfaces:**
- **Biomas nuevos** con rampas de 5 colores: `bigriver`, `cliffs`, `hillruins`, `greyrock`, `deadmarsh`, `herbs`, `morgul`, `ash`, `lava`, con los pesos por zona de la spec §5.
- **Elementos:**
  - el Anduin como banda ancha de agua a lo largo de su zona; el camino va por la orilla y lo cruza en barca, con píxeles de barca;
  - dos estatuas gigantes en los Argonath, a ambos lados del río;
  - el asiento de piedra en lo alto de Amon Hen;
  - el Orodruin como cono volcánico grande en la zona final, con grietas y un río de lava;
  - `WorldMap` gana `lava: IntArray` y `wisps: List<Tile>`.
- **Animación:**
  - la lava palpita (brillo naranja a rojo con ruido que se desplaza);
  - los fuegos fatuos flotan con un leve vaivén en la ciénaga;
  - humo denso sale del Orodruin.
- `WORLD_GEN_VERSION = 3`.

- [ ] **Step 1: Write the failing tests:**
  - el ancho es `24 × 56`;
  - los tests de continuidad (base local) y de camino pasan en las 24 zonas, con 200 y con 40 filas;
  - hay píxeles de lava en `gorgoroth` y `orodruin`;
  - hay `wisps` en `cienagas`;
  - las estatuas de los Argonath existen como anclas;
  - la zona final tiene el cono del Orodruin: elevación máxima en su centro.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*World*" --tests "*Map*"`. Expected: FAIL.
- [ ] **Step 3: Implement.** Renderiza el mundo a `.superpowers/sdd/road-to-mordor/world-render.png` y revísalo: sin cortes, y el Orodruin y la lava a la vista.
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS. Anota el tiempo de generación en la JVM.

### Task 8: Balance del Libro II (`:sim`, docs)

**Files:**
- Modify:
  - patrones y offsets del contenido nuevo (JSON, `ContentRules`, spec de contenido §3)
  - `docs/balance/tuning.md` (sección «Libro II: hasta el Monte del Destino»)
  - `docs/balance/final-*.csv`

- [ ] **Step 1:** `./gradlew :sim:run` con la ruta de 24 zonas. Guarda las métricas por zona y las de la aventura.
- [ ] **Step 2:** ajusta solo el contenido nuevo (offsets de jefe dentro de +2..+12, patrones, tamaño de grupos) con los barridos `sweep offsets <zona>` hasta cumplir los objetivos de Global Constraints. Toca `RivalScaling` solo si es imprescindible, y justifícalo.
- [ ] **Step 3:** documenta la tabla antes/después: las 14 zonas antiguas sin cambios y las nuevas por zona.
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test`. Expected: PASS.

### Task 9: Verificación, capturas e instalación

**Files:**
- Create: `docs/screenshots/v3/map-mordor.png`
- Modify: `CLAUDE.md` (Estado)

- [ ] **Step 1: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 2:** en el emulador, con una partida nueva, abre el mapa, desliza hasta Gorgoroth y el Orodruin y saca la captura.
- [ ] **Step 3:** instala en el móvil del usuario solo si está conectado, sin abrir combates ni avanzar su partida.
- [ ] **Step 4:** actualiza `CLAUDE.md`: 24 zonas, Libro II, `REST_ZONES`, la migración, los retratos nuevos, los biomas del mapa y el balance.
