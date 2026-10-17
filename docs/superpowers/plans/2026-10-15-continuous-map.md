# Mapa continuo en alta resolución — plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:** sustituir el mapa por franjas (`PixelMap.kt`) por un mundo continuo generado por código, a doble resolución, con relieve, agua, sprites sombreados y animación de ambiente.

**Architecture:**
- **Generador:** `ui/map/world/` es Kotlin puro, sin Android ni Compose. Es un port del prototipo `docs/superpowers/specs/assets/2026-10-15-map-proto-v2.html` y devuelve `WorldMap`.
- **Pantalla:** `MapScreen` lo genera en `Dispatchers.Default` con caché de proceso, lo pinta y dibuja encima la animación de la parte visible.

**Tech Stack:** Kotlin 2.4.20, Compose, Robolectric 4.17.

**Spec:** `docs/superpowers/specs/2026-10-15-continuous-map-design.md`

## Global Constraints

- **Sin git:** se omiten los pasos de commit.
- **Entorno:** `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`.
- **Idiomas:** código en inglés; strings de UI en español.
- **Finales de línea:** se conservan los de cada fichero tocado.
- **Generador determinista:** solo hash y ruido con semillas fijas; ni `Random`, ni reloj, ni `android.*` en `ui/map/world/`.
- **Escala:** `TILE_DP = 4`, `ZONE_COLS = 56`, `ZONE_WIDTH_DP` = 224 sin cambio.
- **Valores de la spec:**
  - `warpX` ±17 baldosas;
  - sombreado `clamp(1 − (dx+dy) × 3.2, 0.55, 1.35)`;
  - nieve con elevación > 0,95;
  - rampas de 5 colores de la tabla `RAMP` del prototipo;
  - dither 4×4 Bayer con paso 8;
  - rejilla de sprites de 7 baldosas;
  - ruinas al 7 % y árboles muertos al 10 % de sus biomas;
  - camino de 5 baldosas con `|ΔroadY| ≤ 2`;
  - zona libre de 10 baldosas alrededor de la puerta de Durin;
  - transición de Moria de unas 20 baldosas;
  - animación a ~10 fps, solo la parte visible, detenida fuera de STARTED;
  - generación < 1,5 s en el móvil;
  - continuidad: diferencia de color en las fronteras ≤ 1,6 × la media.

## Review Focus

1. **Pantallas muy bajas o muy altas** (40 filas o 300 filas): el camino, la puerta y los lagos siguen dentro del mapa y las paradas no se salen. Test en la Tarea 1.
2. **Una zona de `Route.zones` que falte en `ZoneKeys`:** usa `meadow` sin fallar. Test en la Tarea 1.
3. **Rotación o vuelta al mapa:** no se regenera (caché por zonas y filas) y la posición de scroll de la zona actual se mantiene. Test en la Tarea 2.
4. **Pantalla en segundo plano:** la animación se detiene (lifecycle). Test en la Tarea 2.
5. **Memoria:** el bitmap de unas 840×250 baldosas es de ~0,8 MB y no se duplica por recomposición; el caché guarda como mucho 2 entradas. Test en la Tarea 2.

---

### Task 1: Generador del mundo (`ui/map/world/`)

**Files:**
- Create:
  - `app/src/main/java/com/wardengambits/app/ui/map/world/WorldNoise.kt`
  - `ZoneKeys.kt`
  - `WorldGen.kt` (campos, elevación, suelo, agua, pared, camino, cuantización)
  - `WorldSprites.kt` (sprites procedurales)
  - `WorldMap.kt` (resultado)
- Test: `app/src/test/java/com/wardengambits/app/ui/map/world/WorldGenTest.kt`

**Interfaces:**
- Produces:
  - `data class Tile(val x: Int, val y: Int)`;
  - `class WorldMap(val width: Int, val height: Int, val pixels: IntArray, val water: IntArray, val chimneys: List<Tile>, val torches: List<Tile>, val mallorns: List<Tile>, val stops: List<Tile>)`;
  - `object WorldGen { fun generate(zoneIds: List<String>, rows: Int): WorldMap; fun roadY(x: Int, rows: Int): Float }`.

  El camino y las posiciones de ríos y lagos se escalan con `rows`: el prototipo usa `H = 200`, así que se usan fracciones de `rows`.
- **Puerta de Durin:** en la frontera durin|moria. Si la ruta no tiene esas zonas, no hay pared.
- **Ríos:** sus x dependen del índice de las zonas `puente`, `bruinen`, `rivendel` y `nimrodel` en `zoneIds`; si una no está, ese río no se dibuja.

- [ ] **Step 1: Write the failing tests** (todos con `Route.zones.map { it.id }` salvo que se diga otra cosa):
  - `deterministic`: dos `generate(ids, 200)` dan el mismo `pixels`.
  - `widthIs56PerZone`: `width == ids.size * 56` y `height == 200`.
  - `noSeamsAtZoneBorders`: media de |ΔRGB| entre las columnas `56k−1` y `56k` (k = 1..14) ≤ 1,6 × la media de |ΔRGB| entre columnas vecinas de todo el mapa.
  - `roadIsContinuous`: para todo x, `|roadY(x+1) − roadY(x)| ≤ 2`.
  - `stopsOnRoadAndDry`: cada parada está a ≤ 1 fila de `roadY` y su píxel no es de agua.
  - `riversCrossedByBridgeOrFord`: en cada columna donde el camino toca agua, los píxeles del camino no son de agua (puente o vado).
  - `durinDoorVisibleAndClear`: hay píxeles de puerta (color claro de la puerta) en la frontera durin|moria a la altura de `roadY`, y ningún sprite a menos de 10 baldosas. Para comprobarlo, `WorldMap` puede exponer una lista interna `spriteAnchors` con visibilidad `internal`.
  - `lakesExist`: hay agua cerca del centro del lago de Durin y del Espejo.
  - `extremeHeights`: con `rows` 40 y 300, las paradas quedan en `0 until rows` y el test de continuidad sigue pasando.
  - `unknownZoneFallsBackToMeadow`: `generate(listOf("bree", "zzz"), 120)` no lanza y el ancho es 112.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*WorldGenTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement** portando el prototipo, más los dos arreglos de la spec: la puerta y el lago visibles, y la transición gradual de Moria. Rendimiento: arrays planos (`FloatArray`/`IntArray`), sin objetos por baldosa.
- [ ] **Step 4: Run** el test. Expected: PASS. Anota en el informe cuánto tarda `generate(ids, 200)` en el JVM de test.

### Task 2: Pantalla del mapa con caché y animación

**Files:**
- Modify: `ui/map/MapScreen.kt`, para usar `WorldGen`/`WorldMap`, las paradas en `WorldMap.stops` y la nueva escala.
- Delete: `ui/map/PixelMap.kt` y `app/src/test/.../ui/map/PixelMapTest.kt`.
- Create: `ui/map/MapAmbience.kt`, la capa animada.
- Adapt: `MapScreenTest.kt` y `StopPlacementTest.kt`.

**Interfaces:**
- Consumes: `WorldGen.generate`, `WorldMap` (Tarea 1).
- Produces:
  - `object WorldCache { fun get(zoneIds: List<String>, rows: Int): WorldMap }`, un LRU de 2 entradas sincronizado;
  - `@Composable fun MapAmbience(world: WorldMap, scrollPx: () -> Int, viewportWidthPx: Int, tilePx: Float, running: Boolean)`.
- **Animación:**
  - agua: brillo y sombra con `valueNoise(x/4 + t×0,18, y/2,5)`;
  - nubes: `valueNoise((x − t×0,45)/44, y/28)` > 0,6 oscurece hasta 0,2;
  - humo: partículas desde `chimneys`;
  - antorchas: degradado radial que parpadea;
  - mallorn: destellos.

  Todo como en el prototipo, a unos 10 fps (`withFrameMillis` filtrado a 100 ms). Solo se recorren las baldosas visibles. `running` sale del lifecycle (`LocalLifecycleOwner`, STARTED).
- **Mientras genera:** fondo oscuro con «Dibujando el mapa…» (string nuevo `map_loading`).

- [ ] **Step 1: Write the failing tests**
  - `mapShowsStopsAndContinue`: el `MapScreenTest` adaptado; las paradas y el botón «Continuar ▶» se muestran.
  - `cacheReturnsSameInstance`: `WorldCache.get(ids, 200) === WorldCache.get(ids, 200)`; tras pedir 3 claves distintas, la primera se ha expulsado.
  - `loadingTextWhileGenerating`: con un generador lento inyectado, se ve «Dibujando el mapa…».
  - `ambiencePausedWhenNotRunning`: con `running = false`, el contador de fotogramas de `MapAmbience` (semántica de test) no avanza.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*Map*"`. Expected: FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.

### Task 3: Verificación en el móvil y documentación

**Files:**
- Create: `docs/screenshots/v3/map.png`, `docs/screenshots/v3/map-scrolled.png`
- Modify: `CLAUDE.md`

- [ ] **Step 1: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 2: Instala** en el móvil (`ZY22JZ2LL2`). Abre solo el mapa: «Modo Historia» lleva al mapa y no inicia combates. Mide el tiempo de generación con un log de depuración (`Log.d("WorldGen", …)` solo en debug) y saca las dos capturas, la segunda deslizando hacia Moria.
- [ ] **Step 3: Actualiza `CLAUDE.md`** con una línea sobre el mapa continuo (spec, `ui/map/world/`, escala, animación y tiempo medido).
