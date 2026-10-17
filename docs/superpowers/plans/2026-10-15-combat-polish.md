# Pulido del combate — plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:**
- números flotantes legibles, repartidos en el hueco bajo los efectos;
- barras de 22 dp;
- botones 3D con efectos al pulsar;
- medallones de efecto animados;
- arreglos pendientes de la revisión anterior.

**Architecture:** solo `:app`, salvo dos arreglos en tests y en el simulador. Las funciones puras de colocación, partículas y tier se testean directamente. Los composables se prueban con Robolectric Compose.

**Tech Stack:** Kotlin 2.4.20, Compose + Material 3, Robolectric 4.17.

**Spec:** `docs/superpowers/specs/2026-10-15-combat-polish-design.md`

## Global Constraints

- **Sin git:** se omiten los pasos de commit.
- **Entorno:** `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`.
- **Idiomas:** código en inglés; strings de UI en español.
- **Finales de línea:** se conservan los de cada fichero tocado.
- **Reglas del juego:** la UI no decide nada del juego. La aleatoriedad visual usa `kotlin.random.Random` de UI, nunca `GameRandom`.
- **Valores de la spec:**
  - **Números:** opacidad completa hasta 0,70 de la vida (1100 ms); contorno `Stroke` de 3 dp en `#E6000000`; rejilla de 3×2 (rival) y 4×2 (jugador); jitter ±25 %; escenario mínimo de 40 dp.
  - **Barras:** 22 dp de alto, texto de 13 sp en negrita con `Shadow(Color.Black, 3f, (0,1))`; el destello de power solo con cambios ≥ 5 % del máximo; halo de power `#8FC4FF`.
  - **Botones:** canto de 6 dp → 2 dp, cara que baja 4 dp en 60 ms, destello ×1,6 → ×1 en 250 ms, onda hasta 1,4× el ancho en 350 ms, 10 chispas a 120–260 dp/s en 450 ms, rebote `spring(dampingRatio = 0.45f)`. Estado «listo»: halo `#F4D98A` con alfa 0,25–0,6 cada 900 ms, 18 chispas y onda de 1,8×. Desactivado: plano y desaturado al 40 %.
  - **Medallones:** 26 dp de alto; borde plateado `#C8CCD4`→`#6A6E78` en tier 0–2, dorado `#F4D98A`→`#8A6420` en tier 3–4, rojizo en perjuicios; anillo de 2 dp; pop 0 → 1,2 → 1 en 300 ms; latido 1 → 1,12 → 1 en 200 ms; parpadeo de alfa 0,6–1 con `remainingFraction ≤ 0,25`; DZ gira 360° cada 4 s; barrido de brillo cada 3 s; texto de 11 sp en negrita con contorno.

## Review Focus

1. **Pausa:** botones, medallones y números congelados; ninguna animación sigue en pausa. Test en las tareas 1 y 4.
2. **Pulsaciones rápidas:** pulsar muy seguido no debe perder llamadas a `onPress` ni acumular partículas sin límite (máximo 60 vivas por fila). Test en la Tarea 3.
3. **Carta sin iconos de efecto:** el escenario empieza bajo las barras; si mide menos de 40 dp, se usa la mitad inferior de la carta. Test en la Tarea 1.
4. **Fuente grande del sistema:** con la escala de fuente a 1,3, el texto de la barra de 22 dp no se corta (se reduce o se mide). Test en la Tarea 2.
5. **Botón de acción desactivado con combo válido** (aturdido): no late ni lanza chispas. Test en la Tarea 3.

---

### Task 1: Números en el escenario, legibles y sin recomponer (`:app`)

**Files:** Modify `ui/combat/FloatingNumbers.kt`, `ui/combat/CombatCards.kt`, `ui/combat/CombatScreen.kt` (paso del reloj), y los tests `FloatingNumbersTest.kt`, `FloatingNumbersScreenTest.kt`, `CombatCardsTest.kt`, `CombatLayoutTest.kt`.

**Interfaces:**
- Produces:
  - `fun stageCell(cols: Int, rows: Int, occupied: List<Int>, random: Random): Int` (casilla libre, o la menos ocupada);
  - `FloatingNumber` guarda el centro (`cx`, `cy`, fracciones del escenario) y la `cell`;
  - `FloatingNumbersState.add(lines, maxOf, now, random, gridOf: (FighterId) -> Pair<Int, Int>)`;
  - `@Composable fun FloatingNumbersLayer(numbers: List<FloatingNumber>, clock: () -> Long, modifier: Modifier)`, que lee el reloj solo en la fase de colocación o dibujo.
- **`floatingNumberAlpha`:** 1 hasta 0,70 y lineal hasta 0. La subida ocurre entre 0,70 y 1.
- **Cartas:** `RivalCard` y `PlayerCard` miden el borde inferior de su fila de efectos (`onGloballyPositioned` o un `Layout` propio) y colocan la capa de números en el escenario, por debajo, con 4 dp de margen. Si el escenario mide menos de 40 dp, se usa la mitad inferior de la carta.

- [ ] **Step 1: Write the failing tests**
  - `sixNumbersInOneBatchUseSixCells`: rejilla 3×2, 6 líneas al mismo objetivo; las 6 `cell` son distintas.
  - `fullGridFallsBackToLeastUsed`: con 6 ocupadas, la 7.ª va a una casilla con la ocupación mínima.
  - `alphaIsOpaqueUntilSeventyPercent`: `floatingNumberAlpha(0.69f) == 1f`, `floatingNumberAlpha(1f) == 0f`.
  - `numbersStayInsideStage`: test Compose de `RivalCard` con 2 iconos de efecto y 6 números; los nodos de los números quedan por debajo del borde inferior de los iconos y dentro de la carta.
  - `noBadgesStageStartsBelowBars`: igual sin iconos; los números quedan por debajo de la barra de power.
  - `clockDoesNotRecomposeCards`: el contador de recomposiciones de `RivalCard` (con `SideEffect`) no aumenta al avanzar el reloj 500 ms sin eventos nuevos.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*FloatingNumbers*" --tests "*CombatCards*"`. Expected: FAIL.
- [ ] **Step 3: Implement.** El contorno se dibuja con dos `Text` superpuestos: el primero con `style.copy(drawStyle = Stroke(...))` y el segundo con el relleno.
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest`. Expected: PASS.

### Task 2: Barras de 22 dp y power sin destello continuo; limpieza (`:app`, test engine, sim)

**Files:**
- Modify:
  - `ui/combat/GameBar.kt`
  - `GameBarTest.kt`
  - `engine/src/test/kotlin/com/wardengambits/engine/story/ContentRules.kt` (el mensaje usa `BOSS_OFFSETS`)
  - `sim/src/main/kotlin/com/wardengambits/sim/Sweep.kt` (offsets por defecto `2..12`)
- Delete: `ui/common/StatBar.kt` y su test, si existe.

**Interfaces:**
- Produces: `internal fun barFlashes(previous: Int, current: Int, max: Int, kind: GameBarKind): Boolean`. Para HP es cualquier cambio; para POWER, `|Δ| ≥ max × 5 / 100`.
- El color del halo de una subida de power es `#8FC4FF`. El halo se dibuja metido medio trazo hacia dentro.

- [ ] **Step 1: Write the failing tests**
  - `powerRegenDoesNotFlash`: `barFlashes(100, 105, 300, POWER) == false`.
  - `bigPowerChangeFlashes`: `barFlashes(100, 115, 300, POWER) == true`.
  - `hpAlwaysFlashes`: `barFlashes(100, 99, 300, HP) == true`.
  - `barIs22dpWithReadableText`: test Compose; el nodo de la barra mide 22 dp de alto y existe el texto «65/100».
  - `largeFontDoesNotClip`: con `LocalDensity` y `fontScale = 1.3f`, el texto «165600/165600» se muestra entero dentro de un ancho de 160 dp (comprobado con `assertWidthIsAtMost` o midiendo).
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*GameBarTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement.** Para que el texto no se corte, reduce la fuente si no cabe (mide con `TextMeasurer`; mínimo 9 sp).
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest`. Expected: PASS.

### Task 3: Botones de acción 3D (`:app`)

**Files:**
- Create:
  - `ui/combat/ActionButtons.kt`
  - `app/src/test/java/com/wardengambits/app/ui/combat/ActionButtonsTest.kt`
- Modify: `ui/combat/CombatScreen.kt`, para sustituir `ButtonRow`/`KeyButton` por `ActionButtons` y conservar los `testTag` `btn_one|btn_two|btn_three|btn_execute` (los nombres son los que genera hoy `"btn_${input.name.lowercase()}"`).

**Interfaces:**
- Produces:
  - `@Composable fun ActionButtons(onPress: (Input) -> Unit, onExecute: () -> Unit, enabled: Boolean, ready: Boolean, running: Boolean, modifier: Modifier = Modifier)`, donde `ready = state.bufferMatch != null`;
  - `data class Spark(val angle: Float, val speedDpPerSec: Float, val colorShift: Float)`;
  - `fun sparkBurst(count: Int, random: Random): List<Spark>`;
  - `const val MAX_LIVE_SPARKS = 60`.
- Valores de la spec (Global Constraints). Las chispas se dibujan en un `Canvas` que cubre la fila sin recortar (`graphicsLayer { clip = false }`). Las animaciones se pausan con `running = false`.

- [ ] **Step 1: Write the failing tests**
  - `pressingKeysCallsCallbacks`: los clics en `btn_one`, `btn_two`, `btn_three` y `btn_execute` llaman a `onPress(ONE/TWO/THREE)` y a `onExecute`.
  - `disabledKeysDoNothing`: con `enabled = false`, ningún callback.
  - `readyStateIsExposed`: con `ready = true`, `btn_execute` tiene `stateDescription == "listo"`; con `ready = false`, no.
  - `disabledReadyDoesNotPulse`: con `enabled = false, ready = true`, no existe la semántica «listo».
  - `sparkBurstHasCountAndSpeedRange`: `sparkBurst(10, Random(1))` da 10 chispas con velocidad en 120..260; `sparkBurst(18, Random(2))`, 18.
  - `rapidPressesKeepCallbacksAndCapSparks`: 30 clics seguidos en `btn_one` dan 30 llamadas, y el estado de chispas vivas nunca pasa de `MAX_LIVE_SPARKS` (expuesto con un `testTag` o `semantics` de depuración del recuento).
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*ActionButtonsTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement.** Usa `Animatable` para la presión, el destello y la onda, y una lista de chispas avanzada por `withFrameMillis` mientras haya vivas y `running`.
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest`. Expected: PASS (incluidos `CombatScreenTest` y `CombatLayoutTest`).

### Task 4: Medallones de efecto (`:app`)

**Files:** Modify `ui/common/EffectBadges.kt` y `EffectBadgesTest.kt` / `EffectIconsTest.kt`; `CombatCards.kt` si hay que pasar `running`.

**Interfaces:**
- Produces:
  - `internal fun badgeTier(code: String): Int` (último carácter si es dígito, si no 0);
  - `internal fun rimColors(tier: Int, debuff: Boolean): Pair<Color, Color>`;
  - `@Composable fun EffectIcons(badges: List<EffectBadgeUi>, running: Boolean = true, modifier: Modifier = Modifier)`. El parámetro nuevo tiene valor por defecto, así que la ayuda de combos sigue compilando.
- **Animaciones:**
  - el «pop» se dispara al aparecer un `code` que no estaba en la lista anterior;
  - el latido, cuando el `remainingFraction` del mismo código baja;
  - el parpadeo, con `remainingFraction ≤ 0.25f`;
  - DZ gira;
  - barrido de brillo cada 3 s.

  Todas se detienen con `running = false`. Las formas existentes (`BadgeShape`) mantienen su silueta, ahora con degradado radial de la familia.

- [ ] **Step 1: Write the failing tests**
  - `tierFromCode`: «SP3» → 3, «CU1» → 1, «EM» → 0, «RV2» → 2.
  - `rimColorsByTierAndDebuff`: los tiers 3 y 4 dan el dorado, los tiers 0–2 el plateado y `debuff = true` el rojizo, con los valores de la spec.
  - `badgeShowsCodeAt26dp`: test Compose de `EffectIcons(listOf(SP2))`; el texto «SP2» existe y el medallón mide 26 dp de alto.
  - `pausedBadgesDoNotAnimate`: con `running = false` y el reloj de test avanzado 1 s, la semántica de escala o rotación de depuración no cambia (expón `scale` en `semantics` con una clave propia de test).
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*EffectBadges*" --tests "*EffectIcons*"`. Expected: FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.

### Task 5: Verificación, emulador, instalación y documentación

**Files:**
- Create: `docs/screenshots/v3/combat-polish.png`
- Modify: `CLAUDE.md` (Estado)

- [ ] **Step 1: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 2: Captura en el emulador.** Arranca el AVD `Medium_Phone_API_36.1`, instala con `adb -s emulator-5554 install -r`, inicia una aventura nueva y saca la captura en un combate con varios rivales mientras pulsas una tecla. **Nunca** se juega en el móvil del usuario.
- [ ] **Step 3: Instala en el móvil** con `adb -s ZY22JZ2LL2 install -r app/build/outputs/apk/debug/app-debug.apk`, solo si está conectado; no se abre ningún combate en él.
- [ ] **Step 4: Actualiza `CLAUDE.md`** con una línea sobre el pulido del combate (spec y ficheros nuevos).

### Task 6: Sonidos de las teclas 1, 2 y 3 (petición del usuario durante la ejecución)

**Files:** Modify `tools/sfx/generate_sfx.py` (3 sonidos nuevos), `app/src/main/res/raw/` (wav generados), `audio/SoundPlayer.kt` (`Sfx`), `audio/AndroidSoundPlayer.kt`, `ui/combat/CombatRoute.kt` (o donde hoy se reproduce `Sfx.ACTION`), y tests de audio y de combate existentes.

**Interfaces:**
- Produces: `enum class Sfx { ACTION, DEFEAT_ENEMY, VICTORY, DEFEAT, KEY_SWORD, KEY_SHIELD, KEY_SHOUT }`.
- Recursos: `R.raw.sfx_key_sword`, `sfx_key_shield`, `sfx_key_shout`.
- **Sonidos** (generados por código, mono a 44,1 kHz, como los actuales):
  - **1 espada:** «shing» metálico al desenvainar, con un barrido de ruido agudo y un tintineo inarmónico corto. Dura ≤ 350 ms y debe distinguirse del `sfx_action` (silbido de hoja).
  - **2 escudo:** golpe seco de madera y metal, con un thump grave y un clonk de tabla. Dura ≤ 300 ms.
  - **3 grito:** grito corto de guerra («¡ha!»): voz sintética con un diente de sierra a 230 → 170 Hz pasado por formantes de «a» (unos 800 / 1200 / 2500 Hz), con ataque rápido, un poco de ruido de aliento y algo de reverberación. Dura ≤ 400 ms.
  - Normalizados a un pico de unos −3 dB, como los demás.
- **Reproducción:**
  - al pulsar 1, 2 o 3 en combate, si el sonido está activado (lo respeta `GatedSoundPlayer`), igual que hoy `ACTION` con ⚔;
  - suenan aunque la pulsación sea rechazada por el motor, porque es el sonido de la tecla;
  - no suenan si la tecla está desactivada.
- Los sonidos usan `SoundPool`, igual que las demás sfx, para que la latencia sea baja.

- [ ] **Step 1: Write the failing tests:**
  - en el test del ViewModel o la ruta de combate, pulsar ONE/TWO/THREE pide `KEY_SWORD`/`KEY_SHIELD`/`KEY_SHOUT` a un `SoundPlayer` falso;
  - con el sonido desactivado (`GatedSoundPlayer`), no se reproduce nada;
  - con las teclas desactivadas (aturdido), no se pide ningún sonido.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*Sound*" --tests "*Combat*"`. Expected: FAIL.
- [ ] **Step 3: Implement.** Genera los wav con `python tools/sfx/generate_sfx.py` (el script debe seguir generando los existentes idénticos o equivalentes) y conecta la reproducción.
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS. Anota en el informe la duración y el pico de cada wav nuevo.
