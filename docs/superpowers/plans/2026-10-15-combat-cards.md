# Combate con cartas, barras 3D, números flotantes y daño variable — plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Daño y cura variables por pulso (50–150 %) en el motor; retratos por tipo de enemigo; pantalla de combate con cartas estilo C, barras 3D animadas y números flotantes de tamaño proporcional.

**Architecture:**
- **Motor (`:engine`):** tira `rollPct` con el `GameRandom` del combate en cada pulso que mueve vida y lo publica en `EffectResolved`. El contenido aporta `portrait` por rival hasta `Fighter`.
- **App (`:app`):** pinta cartas (`CombatCards.kt`), barras (`GameBar.kt`), retratos (`Portraits.kt`) y números flotantes (`FloatingNumbers.kt`, que sustituye a la tira de `CombatText.kt`).

**Tech Stack:** Kotlin 2.4.20, JUnit 5 + kotlin.test (engine), Compose + Material 3, Robolectric 4.17 (app), Python 3 + Pillow (herramienta Gemini).

**Spec:** `docs/superpowers/specs/2026-10-15-combat-cards-design.md`

## Global Constraints

- **Sin git:** nada de `git add` ni `git commit`. El paso «Commit» de cada tarea se sustituye por «Verificar».
- **Entorno de build:** `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"` antes de `./gradlew`.
- **Idiomas:** código, identificadores y comentarios en inglés; strings de UI y docs en español.
- **`:engine` puro y determinista:** nada de `android.*`, `Random()` ni reloj; toda aleatoriedad del juego sale de `GameRandom`. La única dependencia externa es kotlinx-serialization-json.
- **`when` exhaustivo** sobre sealed/enum, sin `else`.
- **Advertencias:** ningún warning nuevo del compilador en `:engine`.
- **Valores de la spec:**
  - `rollPct ∈ [50, 150]` uniforme y entero; `ComboTuning.rollMinPct = 50`, `rollMaxPct = 150`;
  - valor del pulso = `max(1, round(magnitude × rollPct / 100))`;
  - tamaño de número = `13 + 23 × sqrt(clamp(amount / max / 0.15, 0, 1))` sp;
  - halo con `rollPct ≥ 130`; máximo 8 números por carta; vida del número 1100 ms;
  - estela dorada `#F3E0A0`.
- **Retratos:** 12 tipos comunes, 13 jefes `boss_<zona>` y `warden`. Las imágenes van en `drawable-nodpi/portrait_<clave>.webp`, de 256×256.

## Review Focus

1. **Pulso que mata con la tirada al 150 %:** la vida no puede quedar negativa y el número mostrado es el daño real recortado, no el teórico. Ya lo hacen `OverTimeReport.amounts` y `minOf(amount, target.hp)` en el golpe directo; hay que mantenerlo con la tirada. Test en la Tarea 1.
2. **Robo de vida a un oponente con poca vida:** la tirada escala el máximo, el robo se recorta a la vida que queda y el lanzador se cura solo lo robado. Test en la Tarea 1.
3. **Pausa con números y estelas en vuelo:** se congelan y siguen al reanudar; nada se acumula sin límite (8 por carta). Test en la Tarea 5.
4. **Rival derrotado con números en el aire:** la carta apagada sigue mostrando los números que ya estaban en vuelo hasta que expiran, y no crea números nuevos. Test en la Tarea 5.
5. **Guardado o contenido antiguo sin `portrait`:** si un JSON de contenido carece del campo, la carga falla con un mensaje claro; el contenido oficial lo valida `RealContentTest`. Los saves no guardan rivales, así que no les afecta. Test en la Tarea 2.

---

### Task 1: Daño y cura variables por pulso (`:engine`)

**Files:**
- Modify:
  - `engine/src/main/kotlin/com/wardengambits/engine/combo/ComboTuning.kt`
  - `engine/src/main/kotlin/com/wardengambits/engine/event/CombatEvent.kt` (`EffectResolved`)
  - `engine/src/main/kotlin/com/wardengambits/engine/rules/TickResolver.kt` (`resolveOverTime`, `resolveDrains`, la llamada a `ComboApplier.apply`)
  - `engine/src/main/kotlin/com/wardengambits/engine/rules/ComboApplier.kt` (`apply`, `directHit`)
- Create: `engine/src/main/kotlin/com/wardengambits/engine/rules/PulseRoll.kt`
- Test:
  - Create: `engine/src/test/kotlin/com/wardengambits/engine/rules/PulseRollTest.kt`
  - Modify: los tests que esperan valores exactos (pásales `ComboTuning.DEFAULT.copy(rollMinPct = 100, rollMaxPct = 100)` o `ComboTuning.FIXED`) y `HpRegressionTest` (regrabar las huellas desde el motor, como dice su comentario).

**Interfaces:**
- Produces:
  - `ComboTuning(spearBasePct, fistBasePct, cureBasePct, rollMinPct: Int = 50, rollMaxPct: Int = 150)` y `ComboTuning.FIXED = ComboTuning(rollMinPct = 100, rollMaxPct = 100)`.
  - `CombatEvent.EffectResolved(target, source, sourceCombo, kind, amount, rollPct: Int = 100)`.
  - `internal object PulseRoll { fun roll(random: GameRandom, tuning: ComboTuning): Int; fun scale(magnitude: Int, rollPct: Int): Int }`. Si `rollMinPct == rollMaxPct`, `roll` no consume el RNG.
  - `ComboApplier.apply(..., tuning: ComboTuning, random: GameRandom)`.
- **Orden de consumo del RNG** (fijo):
  1. en `resolveOverTime`, una tirada por efecto DOT/COT activo, en el orden de `overTime`;
  2. en `resolveDrains`, una tirada por pulso y oponente, en el orden de `opponentsOf`;
  3. en el golpe directo, una tirada por objetivo vivo.

- [ ] **Step 1: Write the failing tests** in `PulseRollTest`:
  - `rollStaysInRangeAndAveragesHundred`: `SeededRandom(7)`, 1000 tiradas con `ComboTuning.DEFAULT`. Todas en 50..150; media en 95.0..105.0.
  - `fixedTuningNeverConsumesRandom`: con un `GameRandom` que lanza si se le llama y `ComboTuning.FIXED`, `roll(...) == 100`.
  - `scaleRoundsAndKeepsMinimumOne`: `scale(10, 50) == 5`; `scale(3, 50) == 2` (1,5 redondea hacia arriba); `scale(1, 50) == 1`; `scale(200, 150) == 300`.
  - `overTimeAmountsVaryAcrossTicks`: combate jugador contra un rival con SP2 aplicado y `ComboTuning.DEFAULT`, 3 ticks. Los `EffectResolved` de DOT sobre el rival no tienen todos el mismo `amount` y su `rollPct` está en 50..150.
  - `sameSeedSameEvents`: dos `CombatEngine` con la misma semilla y las mismas acciones durante 20 ticks dan listas de eventos iguales.
  - `fixedTuningReproducesNominalValues`: con `ComboTuning.FIXED`, el `amount` del primer tick de DOT es `-DamageFormula.received(magnitude, false, false)`, igual que antes del cambio, y `rollPct == 100`.
  - `lethalPulseIsClampedToRemainingHp`: rival con 5 PV y un DOT de magnitud 100 con la tirada forzada al 150 % (un `GameRandom` stub que devuelve `rollMaxPct - rollMinPct`). La vida queda en 0 y el evento muestra `amount == -5`.
  - `drainTakesAtMostRemainingHp`: oponente con 3 PV y robo de magnitud 50 al 150 %. El oponente queda a 0, el evento del oponente es `-3` y la curación del lanzador es `≤ 3`.
  - `shieldsDazeAndMultipliersDoNotRoll`: lanzar `2132`, `31` y `11` con un `GameRandom` que cuenta llamadas. Las únicas llamadas son las de `rollDaze` existentes, y los `EffectApplied` y las magnitudes son iguales a las de `ComboTuning.FIXED`.
- [ ] **Step 2: Run** `./gradlew :engine:test --tests "*PulseRollTest*"`. Expected: FAIL (no compila: `PulseRoll`, `rollPct` y `FIXED` no existen).
- [ ] **Step 3: Implement**
  - `roll = rollMinPct + random.nextInt(rollMaxPct - rollMinPct + 1)`.
  - `scale = max(1, (magnitude * rollPct + 50) / 100)`.
  - **DOT/COT:** antes de `DamageFormula.received`, aplica `PulseRoll.scale(it.magnitude, roll)` a la magnitud, y pasa la tirada al `EffectResolved`.
  - **Robo:** la tirada escala `pulse.magnitude` antes del `minOf`. El evento del lanzador lleva `rollPct = 100`.
  - **Golpe directo:** escala `DamageFormula.directHit(...)` con la tirada antes del `minOf(amount, target.hp)`.
- [ ] **Step 4: Run** `./gradlew :engine:test`. Expected: PASS. Antes, adapta los tests de valores exactos a `ComboTuning.FIXED` y regraba las huellas de `HpRegressionTest` (no cambies sus asserts de forma).
- [ ] **Step 5: Verificar** que `./gradlew :engine:compileKotlin` no da warnings nuevos y que `:sim:test` y `:app:testDebugUnitTest` compilan (los usos de `EffectResolved` con argumentos posicionales siguen valiendo gracias al parámetro por defecto).

### Task 2: Retratos en el contenido (`:engine`, datos)

**Files:**
- Modify:
  - `engine/src/main/kotlin/com/wardengambits/engine/story/content/StoryContent.kt` (`RivalContent.portrait: String`, sin valor por defecto)
  - `engine/src/main/kotlin/com/wardengambits/engine/model/CombatState.kt` (`RivalSpec.portrait: String = "unknown"`)
  - `engine/src/main/kotlin/com/wardengambits/engine/model/Fighter.kt` (`portrait: String = "warden"`)
  - el sitio donde se crea `Fighter` a partir de `RivalSpec`
  - `engine/src/main/kotlin/com/wardengambits/engine/story/StoryEngine.kt:71`, que pasa `portrait`
  - los 15 JSON de `engine/src/main/resources/story/` (UTF-8 sin BOM)
  - `engine/src/test/kotlin/com/wardengambits/engine/story/ContentRules.kt`
  - `ContentRulesTest.kt`, `RealContentTest.kt`
- Create: `tools/content/add_portraits.py`, script de migración único que añade `portrait` según la tabla de la spec §2 y falla si queda algún nombre sin asignar.

**Interfaces:**
- Produces:
  - `Fighter.portrait: String`;
  - `RivalSpec(name, level, brain, isBoss = false, portrait = "unknown")`;
  - `ContentRules.COMMON_PORTRAITS: Set<String>` con los 12 tipos;
  - `ContentRules.bossPortrait(zoneId: String) = "boss_$zoneId"`.
- El jefe de cada zona es el rival que hoy marca `isBoss` (el que `StoryEngine` crea con `isBoss = true`). Su `portrait` es `boss_<zona>`.

- [ ] **Step 1: Write the failing tests**
  - En `RealContentTest`:
    - `everyRivalHasKnownPortrait`: todo rival no jefe tiene `portrait in ContentRules.COMMON_PORTRAITS`;
    - `everyBossHasItsZonePortrait`: todo jefe tiene `portrait == "boss_<zona>"`;
    - `portraitsCoverAllTypes`: las claves usadas en el contenido incluyen los 12 tipos comunes y los 13 jefes.
  - En `StoryEngine`: `combatSetupCarriesPortrait`, donde el `CombatSetup` de un nodo con encuentro lleva el `portrait` del JSON hasta `Fighter.portrait`, y el jugador tiene `"warden"`.
  - `missingPortraitFailsToLoad`: cargar un JSON mínimo sin `portrait` lanza `SerializationException`.
- [ ] **Step 2: Run** `./gradlew :engine:test --tests "*RealContentTest*" --tests "*StoryNavigationTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement** los campos y el paso hasta `Fighter`. Ejecuta `python tools/content/add_portraits.py`; debe imprimir cuántos rivales asignó por zona y terminar con 0 sin asignar.
- [ ] **Step 4: Run** `./gradlew :engine:test`. Expected: PASS (incluidas la regla de JSON sin BOM y `ContentRulesTest`).
- [ ] **Step 5: Verificar** que `:sim:test` y `:app:testDebugUnitTest` compilan y pasan.

### Task 3: Re-simulación y balance (`:sim`, docs)

**Files:**
- Modify: `docs/balance/tuning.md` (nueva sección «Daño variable 50–150 %»), `docs/balance/final-encounters.csv` y `docs/balance/final-adventures.csv` (regenerados).
- Modify (solo si hace falta): `engine/src/main/kotlin/com/wardengambits/engine/model/Stats.kt` (`RivalScaling`) y offsets del JSON.

**Interfaces:** consume la Tarea 1 (`ComboTuning.DEFAULT` ya con tirada). El simulador usa `ComboTuning.DEFAULT`; si alguna parte usaba valores exactos, no cambia.

- [ ] **Step 1:** Ejecuta el simulador con `ComboTuning.FIXED` inyectado (el «antes», equivalente al motor sin tirada; si `:sim` no permite elegir el tuning por argumento, añade la opción `--fixed-roll` en `sim/src/main/kotlin/com/wardengambits/sim/Main.kt`) y guarda las métricas:
  - MEDIO y EXPERTO sin muertes;
  - muertes por aventura;
  - mínimo de los comunes MEDIO;
  - jefes MEDIO (mínimo, media y máximo);
  - combates al tope de ticks;
  - aventuras abandonadas.
- [ ] **Step 2:** Ejecuta `./gradlew :sim:run` con la tirada activa y guarda las métricas «después».
- [ ] **Step 3:** Si algo sale de los objetivos (MEDIO 5–9 %, EXPERTO 20–28 %, 3,5–5 muertes MEDIO, comunes MEDIO ≥ 88 %, jefes MEDIO 55–85 %, 0 al tope), barre con `--args="sweep ..."` / `"sweep boss ..."` / `"sweep offsets <zona> ..."` y aplica el ajuste mínimo. Si todo está dentro, no toques números.
- [ ] **Step 4:** Documenta la tabla antes/después en `tuning.md` y los pendientes para pruebas reales.
- [ ] **Step 5: Verificar** que `./gradlew :engine:test :sim:test` pasa.

### Task 4: Barras 3D animadas (`:app`)

**Files:**
- Create:
  - `app/src/main/java/com/wardengambits/app/ui/combat/GameBar.kt`
  - `app/src/test/java/com/wardengambits/app/ui/combat/GameBarTest.kt`

**Interfaces:**
- Produces:
  - `enum class GameBarKind { HP, POWER }`;
  - `@Composable fun GameBar(value: Int, max: Int, kind: GameBarKind, running: Boolean, modifier: Modifier = Modifier)`, de altura 14 dp; muestra el texto `"$value/$max"`;
  - `internal fun barTrail(previous: Float, current: Float): BarTrail`, con `data class BarTrail(val ghostColor: Color, val fillFirst: Boolean)`.
- **Valores de la spec:**
  - al bajar: fill en 250 ms y estela `#F3E0A0` con retraso de 350 ms y duración de 900 ms;
  - al subir: la estela verde (`#7DFF9A`; azul claro `#8FC4FF` en power) va primero y el fill la sigue con 300 ms de retraso;
  - destello de brillo ×2 → ×1 en 350 ms;
  - halo verde 700 ms en las curas.
  - **Degradados:**
    - vida `#FF8A7A → #D8392B → #8E1C13`;
    - power `#8FC4FF → #2F74D0 → #163F80`;
    - canal `#1A1410 → #3A2F27`;
    - brillo blanco al 45 % en el 35 % superior.
  - Con `running = false` las animaciones en curso se detienen (`Animatable.stop()`) y se reanudan al volver a `true`.

- [ ] **Step 1: Write the failing tests**
  - `barTrailOnDamageIsGoldAndFillFirst`: `barTrail(0.8f, 0.5f) == BarTrail(Color(0xFFF3E0A0), fillFirst = true)`.
  - `barTrailOnHealIsGreenAndGhostFirst`: `barTrail(0.5f, 0.8f).fillFirst == false`.
  - `gameBarShowsValueText`: test Compose con `GameBar(65, 100, HP, running = true)`; existe el nodo con texto `"65/100"`.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*GameBarTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement** con `Canvas` y `Brush.verticalGradient`, `Animatable` para el fill y la estela, y `drawRoundRect`.
- [ ] **Step 4: Run** el mismo comando. Expected: PASS.
- [ ] **Step 5: Verificar** que compila (`:app:compileDebugKotlin`).

### Task 5: Números flotantes (`:app`)

**Files:**
- Create:
  - `app/src/main/java/com/wardengambits/app/ui/combat/FloatingNumbers.kt`
  - `app/src/test/java/com/wardengambits/app/ui/combat/FloatingNumbersTest.kt`
- Modify:
  - `app/src/main/java/com/wardengambits/app/ui/combat/CombatText.kt`: `CombatTextLine` gana `amount: Int` y `rollPct: Int`. Se conservan `combatTextFor`, los tonos y los colores; se eliminan la constante de stagger y `combatTextMinTravel` si quedan sin uso.
  - `CombatRoute.kt`, que pasa `rollPct`.
  - `CombatTextTest` (o como se llame el test actual), que se adapta.

**Interfaces:**
- Consumes: `EffectResolved.rollPct` (Tarea 1).
- Produces:
  - `fun floatingNumberSize(amount: Int, max: Int): Float`, que devuelve sp;
  - `data class FloatingNumber(val key: Long, val target: FighterId, val text: String, val tone: CombatTextTone, val sizeSp: Float, val strong: Boolean, val xFrac: Float, val yFrac: Float, val startMillis: Long)`;
  - `class FloatingNumbersState(val perCardLimit: Int = 8)`, con `fun add(lines: List<CombatTextLine>, maxOf: (FighterId) -> Int, now: Long, random: kotlin.random.Random): FloatingNumbersState`, `fun prune(now: Long)` y `fun forTarget(id: FighterId): List<FloatingNumber>`;
  - `@Composable fun FloatingNumbersLayer(numbers: List<FloatingNumber>, now: Long, modifier: Modifier)`, que dibuja encima de una carta;
  - `rememberFloatingNumbers(running: Boolean)` sustituye a `rememberCombatText`, con el mismo reloj que se congela en pausa.
- **Detalles:**
  - `maxOf` devuelve `maxHp` (o `maxPower` en el tono POWER) del objetivo;
  - `xFrac` en 0.05..0.65 e `yFrac` en 0.15..0.80, para que el número quepa;
  - **animación:** escala 0.3 → 1.25 (15 %) → 1.0 (30 %), sube 26 dp y alpha 1 → 0 desde el 30 %, con vida de 1100 ms;
  - el código del combo va a la mitad de tamaño (`SpanStyle`);
  - `strong = rollPct >= 130` da un halo (`Shadow` de `blurRadius` 12 del color del tono).

- [ ] **Step 1: Write the failing tests**
  - `sizeIsMinForZeroAndMaxAtFifteenPercent`:
    - `floatingNumberSize(0, 10000) == 13f`;
    - `floatingNumberSize(1500, 10000) == 36f`;
    - `floatingNumberSize(5000, 10000) == 36f`.
  - `sizeIsMonotonic`: para `a < b`, `size(a) <= size(b)`, comprobado con valores 0..2000 en pasos de 100 y max 10000.
  - `strongOnlyFromRoll130`: con `rollPct` 129 sale `strong == false` y con 130, `true`.
  - `limitsEightPerCard`: añadir 12 líneas al mismo objetivo deja 8 (las 8 más nuevas).
  - `positionsStayInsideCard`: 200 números con `Random(1)`; todos con `xFrac` en 0.05..0.65 e `yFrac` en 0.15..0.80.
  - `pausedClockFreezesNumbers`: con el reloj del controlador sin avanzar, `prune` no elimina nada. Tras avanzar 1100 ms se eliminan.
  - `defeatedTargetKeepsLiveNumbers`: los números ya añadidos a un objetivo siguen en `forTarget` hasta expirar, y `combatTextFor` de un `FighterDefeated` sigue dando `null`.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*FloatingNumbersTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement.** La aleatoriedad es de UI (`kotlin.random.Random`, inyectable para el test); no toca la partida.
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest`. Expected: PASS (los tests de la tira antigua ya adaptados).
- [ ] **Step 5: Verificar** que compila.

### Task 6: Retratos en la app y herramienta Gemini (`:app`, tools, docs)

**Files:**
- Create:
  - `app/src/main/java/com/wardengambits/app/ui/common/Portraits.kt`
  - `app/src/test/java/com/wardengambits/app/ui/common/PortraitsTest.kt`
  - `docs/content/prompts.md`
- Modify: `tools/gemini/generate_images.py`

**Interfaces:**
- Consumes: `Fighter.portrait` (Tarea 2).
- Produces:
  - `object PortraitSprites { val TYPES: Set<String>; fun spriteFor(key: String): PixelSprite; fun baseType(key: String): String }`, con 13 sprites: los 12 tipos y `warden`. `baseType("boss_bree") == "bandit"` y así con la tabla de jefes de la spec; una clave desconocida da `"warden"`.
  - `data class PixelSprite(val rows: List<String>, val palette: Map<Char, Color>)`: 16 filas de 16 caracteres, `'.'` es transparente.
  - `@Composable fun Portrait(key: String, size: Dp, dimmed: Boolean = false, modifier: Modifier = Modifier)`:
    - busca `portrait_<key>` en drawable (`getIdentifier`, como `NodeImage`) y la pinta con `FilterQuality.None`;
    - si no existe, pinta el sprite de `baseType(key)` en un `Canvas`;
    - `dimmed` desatura (`ColorMatrix` con saturación 0) y aplica alpha 0.6;
    - `contentDescription = "Retrato"`.
- **Tabla de jefes → tipo base:**

  | jefe | tipo base |
  |---|---|
  | bree | bandit |
  | chet | wolf |
  | cima | dunlending |
  | moscagua | swarm |
  | puente | warg |
  | quebradas | troll |
  | bruinen | dunlending |
  | eregion | dunlending |
  | caradhras | crebain |
  | durin | swarm |
  | moria | troll |
  | sombrio | orc |
  | nimrodel | orc_archer |

  El Jinete y el Espectro usan el sprite `dunlending` (figura encapuchada) mientras no haya imagen de Gemini; no hay sprites extra.
- **`prompts.md`:** 26 filas `| portrait_<clave>.webp | <título en español> | <prompt en inglés> |`, en el mismo estilo que `prompts.md`: «16-bit pixel art, SNES-era fantasy RPG style, bust portrait of …, centered, dark neutral background, square 1:1, no text». Las claves son los 12 tipos, los 13 `boss_<zona>` con el nombre del jefe en el título, y `warden` (joven viajero con capa marrón con capucha y espada corta, el mismo personaje de las imágenes de nodo).
- **`generate_images.py`:**
  - nueva opción `--set {nodes,portraits}` (por defecto `nodes`);
  - con `portraits`:
    - lee `prompts.md`;
    - pide `aspectRatio: "1:1"`;
    - deja las imágenes en `docs/content/gemini/portraits/`;
    - `convert` recorta al centro a 1:1, redimensiona a 256×256 con `Image.NEAREST` y guarda `drawable-nodpi/portrait_<clave>.webp` en calidad 90;
  - la regex de filas acepta el prefijo `portrait_`.

- [ ] **Step 1: Write the failing tests**
  - `everySpriteIs16x16WithKnownColors`: cada sprite de `TYPES ∪ {"warden"}` tiene 16 filas de 16 caracteres, y todo carácter que no sea `.` está en su paleta.
  - `bossesMapToBaseType`: `baseType("boss_chet") == "wolf"`, `baseType("boss_nimrodel") == "orc_archer"`, `baseType("orc") == "orc"` y `baseType("zzz") == "warden"`.
  - `portraitFallsBackToSprite`: test Compose `Portrait("orc", 44.dp)`; existe el nodo con `contentDescription` «Retrato» (sin imagen empaquetada, pinta el sprite sin fallar).
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*PortraitsTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement** los sprites (siluetas reconocibles: lobo, huargo, jinete sobre huargo, orco, arquero orco, trasgo, trol, bandido, dunlendino, cuervo, enjambre y warden), el composable, el fichero de prompts y la opción del script.
- [ ] **Step 4: Run** el test y `python tools/gemini/generate_images.py generate --set portraits --dry-run`. Expected: PASS y «26 prompts seleccionados, 26 por generar».
- [ ] **Step 5: Verificar** que `python tools/gemini/generate_images.py convert` (sin `--set`) sigue informando de «309/309».

### Task 7: Cartas de combate estilo C (`:app`)

**Files:**
- Create:
  - `app/src/main/java/com/wardengambits/app/ui/combat/CombatCards.kt` (`RivalCard`, `PlayerCard`, marco común `CardFrame`)
  - `app/src/test/java/com/wardengambits/app/ui/combat/CombatCardsTest.kt`
- Modify:
  - `app/src/main/java/com/wardengambits/app/ui/combat/CombatScreen.kt`: se eliminan `RivalCard`, `PlayerCard` y `CombatTextStrip` privados, y `RivalGrid` usa las nuevas cartas.
  - `CombatUiState.kt`: `FighterUi.portrait: String = "warden"`.
  - `CombatViewModel.kt`: `fighterUi` mapea `fighter.portrait`.
  - `CombatRoute.kt`: conecta `rememberFloatingNumbers` y pasa `now`, `running` y los números de cada carta.
  - Los tests existentes de `CombatScreen` que buscaban la tira o los textos antiguos.

**Interfaces:**
- Consumes:
  - `GameBar` (Tarea 4);
  - `FloatingNumbersLayer` y `FloatingNumbersState.forTarget` (Tarea 5);
  - `Portrait` (Tarea 6);
  - `FighterUi.portrait`.
- Produces:
  - `@Composable fun RivalCard(fighter: FighterUi, numbers: List<FloatingNumber>, now: Long, running: Boolean, onSelect: () -> Unit, modifier: Modifier)`;
  - `@Composable fun PlayerCard(fighter: FighterUi, numbers: List<FloatingNumber>, now: Long, running: Boolean, modifier: Modifier)`.
- **Aspecto (spec §3):**
  - **Marco:** degradado `#3A2D22 → #221A13`, borde de 1 dp `#9B7B3D` (2 dp dorado si `isTarget`), brillo interior superior y sombra inferior de 3 dp.
  - **Rival:** retrato de 44 dp; nombre en serif dorado `#F4D98A` de 2 líneas como máximo; línea «Nv N · ATQ X · DEF Y»; luego vida, power y `EffectBadges`.
  - **Jugador:** retrato de 58 dp; «Warden · Nv N»; «ATQ · CUR · DEF»; las dos barras y los iconos, a ancho completo.
  - **Se conservan:**
    - la marca de jefe;
    - el indicador de combo en cola;
    - el toque para elegir objetivo;
    - al morir: `Portrait(dimmed = true)`, barras grises y el texto «Derrotado» (string `combat_defeated`, crear si no existe).
  - `FloatingNumbersLayer` va encima de toda la carta (`Box` con `matchParentSize`).

- [ ] **Step 1: Write the failing tests**
  - `rivalCardShowsNameLevelBarsAndPortrait`: `RivalCard` de «Orco capitán», Nv 36, vida 8000/9000 y power 150/300. Existen los textos «Orco capitán», «Nv 36 · ATQ …», «8000/9000» y «150/300», y el nodo «Retrato».
  - `deadRivalShowsDefeated`: con `isAlive = false` aparece «Derrotado».
  - `tappingRivalSelectsIt`: `performClick` en la carta llama a `onSelect`.
  - `playerCardShowsStats`: `PlayerCard` de «Warden», Nv 34, con «Warden · Nv 34» y «ATQ … · CUR … · DEF …».
  - `viewModelMapsPortrait`: en `CombatViewModelTest`, un combate con un rival de `portrait = "goblin"` da `state.rivals[0].portrait == "goblin"` y el jugador da `"warden"`.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*CombatCardsTest*" --tests "*CombatViewModelTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement** las cartas y conéctalas en `CombatScreen` y `CombatRoute`.
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS y APK generado.
- [ ] **Step 5: Verificar** que compila sin warnings nuevos.

### Task 8: Verificación final, instalación y documentación

**Files:**
- Create: `docs/screenshots/v3/combat.png`
- Modify: `CLAUDE.md` (sección Estado)

- [ ] **Step 1: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 2:** Instala en el móvil con `./gradlew :app:installDebug` (adb en `$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe`; si no aparece, `adb kill-server` y `adb start-server`). Abre un combate de varios rivales y saca la captura con `adb exec-out screencap -p > docs/screenshots/v3/combat.png`.
- [ ] **Step 3:** Revisa la captura: cartas C, barras 3D, retratos por código y números de tamaños distintos repartidos por las cartas.
- [ ] **Step 4:** Actualiza `CLAUDE.md` (Estado): daño variable 50–150 %, retratos (`portrait`, sprites y prompts), cartas y barras 3D, números flotantes y el resultado del balance de la Tarea 3.
- [ ] **Step 5: Verificar** que las tareas 1–7 están marcadas y que la spec y el plan coinciden con lo construido.
