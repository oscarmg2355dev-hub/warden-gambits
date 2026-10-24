# Niveles de dificultad — plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** cuatro dificultades en Opciones que multiplican el ataque de los rivales (×0,5 / ×0,6 / ×0,75 / ×1).

**Architecture:** el motor recibe el porcentaje en `CombatSetup.rivalAttackPct` y lo aplica en `CombatEngine.start`. La app guarda la elección en los ajustes (DataStore) y la pone en el setup de la historia y del Modo combate. La historia anota en la vuelta la dificultad más fácil usada, y la crónica la muestra.

**Tech Stack:** Kotlin, `:engine` (JUnit 5), `:app` (Compose, Robolectric, DataStore), `:sim`.

**Spec:** `docs/superpowers/specs/2026-10-24-difficulty-levels-design.md`

## Global Constraints

- **Dificultades,** de más fácil a más difícil (`id` → es / en → % de ataque):
  - `SHIRE` → «Paseo por la Comarca» / «A Stroll in the Shire» → 50
  - `RANGER` → «Senda del Montaraz» / «Ranger's Path» → 60
  - `MARK` → «Guardián de la Marca» / «Warden of the Mark» → 75
  - `EYE` → «El Ojo de Sauron» / «The Eye of Sauron» → 100
- **Líneas cortas:**
  - es: «Los rivales hacen la mitad de daño», «Los rivales hacen un 40 % menos de daño», «Los rivales hacen un 25 % menos de daño», «El reto tal como fue forjado»;
  - en: «Rivals deal half damage», «Rivals deal 40% less damage», «Rivals deal 25% less damage», «The challenge as it was forged».
- **Por defecto,** si la clave `difficulty` del DataStore no existe: `EYE` si hay partida guardada, `RANGER` si no. Ese valor se escribe.
- **Dónde aplica:** la historia y el Modo combate usan la dificultad elegida; el tutorial, 100.
- **Con `rivalAttackPct = 100`** todo da exactamente lo mismo que hoy.
- **Proyecto:**
  - `:engine` puro, sin dependencias nuevas, sin warnings;
  - textos visibles con `tr`/`trText`/`trTextResource`;
  - `strings.xml` en CRLF (nunca `sed -i`);
  - sin commits (regla del proyecto).

## Review Focus

- **Ataque muy bajo** (nivel 1 a ×0,5): el ataque escalado nunca baja de 1 → test `attackNeverBelowOne` (T1).
- **Guardado antiguo** sin `easiestDifficulty` ni `difficulty` en los registros: carga con `null` y la crónica no muestra línea → tests `oldSaveLoadsWithoutDifficulty` (T1) y `historyHidesMissingDifficulty` (T3).
- **Cambiar la dificultad a mitad de vuelta:** la vuelta guarda la más fácil, no la última → test `easiestDifficultyKeepsTheEasiest` (T1).
- **Ajustes aún sin cargar al abrir un combate:** la dificultad es `null` hasta que carga, y el combate no arranca con un valor inventado → test `combatWaitsForDifficulty` (T2).
- **Volver a abrir la app con la clave ya escrita:** no se re-resuelve el valor por defecto aunque cambie la existencia del guardado → test `storedDifficultyIsNotReResolved` (T2).

---

### Task 1: Motor y simulador

**Files:**
- Create:
  - `engine/src/main/kotlin/com/wardengambits/engine/model/Difficulty.kt`
- Modify:
  - `engine/.../model/CombatState.kt` (`CombatSetup`) y `engine/.../rules/CombatEngine.kt` (`start`);
  - `engine/.../story/StoryState.kt` (`RunState`, `RunRecord`), `StoryAction.kt` (`CombatFinished`) y `StoryEngine.kt` (`finishCombat`, `startNextLap`);
  - `sim/src/main/kotlin/com/wardengambits/sim/Main.kt`;
  - `docs/balance/tuning.md`.
- Test:
  - `engine/src/test/kotlin/com/wardengambits/engine/DifficultyTest.kt`
  - `sim/src/test/...` (el test de opciones del simulador que ya exista, o uno nuevo `DifficultyOptionTest.kt`)

**Interfaces:**
- Produces:
  - `@Serializable enum class Difficulty(val rivalAttackPct: Int) { SHIRE(50), RANGER(60), MARK(75), EYE(100) }`, en este orden;
  - `CombatSetup(..., hpFloorPct: Int? = null, rivalAttackPct: Int = 100)`;
  - `RunState.easiestDifficulty: Difficulty? = null`;
  - `RunRecord.difficulty: Difficulty? = null`;
  - `StoryAction.CombatFinished(outcome, nowMillis, difficulty: Difficulty? = null)`.

- [ ] **Step 1: Write the failing tests** in `DifficultyTest`:
  - `attackScaledByRivalAttackPct`: con el mismo setup, `rivalAttackPct = 50` da en cada rival `attack == max(1, attack100 * 50 / 100)`, tanto en un común como en un jefe (`isBoss = true`).
  - `playerAttackUnchanged`: el ataque del Warden es el mismo con 50 y con 100.
  - `attackNeverBelowOne`: con un rival de ataque escalado 1 y `rivalAttackPct = 50`, el ataque es 1.
  - `rivalDamageScalesWithAttack`: con `ComboTuning.FIXED`, el daño del primer combo de daño de un rival a 50 es la mitad, ±1, que a 100.
  - `defaultIsUnchanged`: un combate sembrado de 30 ticks da el mismo estado con `CombatSetup` sin el parámetro y con `rivalAttackPct = 100`.
  - `easiestDifficultyKeepsTheEasiest`: `CombatFinished` con `MARK`, luego con `SHIRE` y luego con `EYE` deja `run.easiestDifficulty == SHIRE`. Sin dificultad (`null`) no cambia el valor.
  - `lapRecordCarriesDifficulty`: al empezar la vuelta siguiente, `RunRecord.difficulty` es la `easiestDifficulty` de la vuelta y la nueva vuelta empieza con `null`.
  - `oldSaveLoadsWithoutDifficulty`: un JSON de `StoryProfile` sin esas claves se decodifica con las dos en `null`. Usar el mismo codec de guardado que la app.
- [ ] **Step 2: Run** `./gradlew :engine:test --tests "*DifficultyTest*"`. Expected: FAIL, porque no compila.
- [ ] **Step 3: Implement.**
  - `CombatEngine.start` aplica `rivalAttackPct` sobre el ataque ya escalado por `RivalScaling`, con un mínimo de 1.
  - «Más fácil» significa menor `rivalAttackPct`; `null` cuenta como «sin dato».
- [ ] **Step 4: Run** `./gradlew :engine:test`. Expected: PASS.
- [ ] **Step 5: Simulador.**
  - Opción `--difficulty <ID>` en `Main.kt`, que se combina con las demás. Construye `CombatEngine(rivalScaling = DEFAULT.copy(attackPct = DEFAULT.attackPct * pct / 100), bossScaling = BOSS.copy(attackPct = …))` e imprime `Dificultad: <ID>`.
  - Test: una función pura `engineFor(difficulty: Difficulty): CombatEngine`, o la que decidas, cuyo `SHIRE` da `attackPct` 32 en comunes y 38 en jefes.
- [ ] **Step 6: Documentación.**
  - En `docs/balance/tuning.md`, añadir la sección «Niveles de dificultad» con la tabla del §2 de la spec, copiada tal cual.
  - En la cabecera de esa sección, una línea: la referencia de balance sigue siendo `EYE`.
- [ ] **Step 7: Run** `./gradlew :engine:test :sim:test`. Expected: PASS, sin warnings nuevos en `:engine`.

### Task 2: App — ajustes, valor por defecto y combates

**Files:**
- Modify:
  - `app/.../data/SettingsRepository.kt`, `data/SaveRepository.kt` y `AppContainer.kt`;
  - `ui/combat/CombatSource.kt` (`StoryCombatSource`, `PracticeCombatSource`), `CombatViewModel.kt` y `navigation/WardenNavHost.kt`, donde se crean las fuentes.
- Test:
  - `app/src/test/.../data/SettingsRepositoryTest.kt`
  - `app/src/test/.../DifficultyFlowTest.kt` (nuevo)

**Interfaces:**
- Consumes: `Difficulty`, `CombatSetup.rivalAttackPct` y `CombatFinished(..., difficulty)` (Task 1).
- Produces:
  - `SettingsRepository.difficulty: Flow<Difficulty?>` (null si la clave no existe) y `suspend fun setDifficulty(d: Difficulty)`;
  - `SaveRepository.hasSave(): Boolean`, que dice si existe un guardado escrito;
  - `AppContainer.difficulty: StateFlow<Difficulty?>`, null hasta resolverse;
  - `AppContainer.setDifficulty(d: Difficulty)`;
  - `StoryCombatSource(session, storyEngine, difficulty: Difficulty)` y `PracticeCombatSource(node, storyEngine, difficulty: Difficulty)`, cuyo `setup` lleva `rivalAttackPct = difficulty.rivalAttackPct`.

- [ ] **Step 1: Write the failing tests:**
  - `difficultyDefaultsToEyeWithSave` y `difficultyDefaultsToRangerWithoutSave`: el contenedor resuelve y escribe el valor.
  - `storedDifficultyIsNotReResolved`: con `MARK` guardado, sigue `MARK` haya guardado o no.
  - `setDifficultyPersists`.
  - `storyCombatUsesDifficulty`: con `SHIRE`, el rival del combate de la historia tiene la mitad de ataque que con `EYE`, y el `CombatFinished` despachado lleva `SHIRE`.
  - `practiceCombatUsesDifficulty`.
  - `tutorialIgnoresDifficulty`: el setup del tutorial tiene `rivalAttackPct == 100` con `SHIRE`.
  - `combatWaitsForDifficulty`: con la dificultad aún `null`, la ruta de combate no crea la fuente. Basta con una carga mínima, como el resto de valores `null` del contenedor.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*Difficulty*" --tests "*SettingsRepositoryTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement.** `StoryCombatSource.report` pasa su dificultad en `CombatFinished`.
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest`. Expected: PASS.

### Task 3: App — selector en Opciones y crónica

**Files:**
- Modify:
  - `ui/options/OptionsScreen.kt`, `ui/history/…` (la pantalla «Crónica de vueltas») y `navigation/WardenNavHost.kt`;
  - `res/values/strings.xml` y `res/values-en/strings.xml`;
  - `CLAUDE.md` (Estado).
- Test: `OptionsScreenTest.kt`, el test de la crónica y `LanguageScriptMatrixTest` si cubre Opciones.

**Interfaces:**
- Consumes: `AppContainer.difficulty` y `setDifficulty` (Task 2); `RunRecord.difficulty` (Task 1).
- Produces: strings `options_difficulty` («Dificultad» / «Difficulty»), `difficulty_<id>_name`, `difficulty_<id>_hint` (§Global Constraints, con `<id>` en minúsculas) y `history_difficulty` («Dificultad: %1$s» / «Difficulty: %1$s»).

- [ ] **Step 1: Write the failing tests:**
  - `optionsShowsFourDifficulties`: las cuatro en orden SHIRE → EYE, con su línea, y la elegida marcada (estado `selected`).
  - `tapDifficultySelectsIt`: tocar «Guardián de la Marca» llama a `setDifficulty(MARK)`.
  - `optionsDifficultyInEnglishAndTengwar`.
  - `historyShowsDifficulty`: un registro con `RANGER` muestra «Dificultad: Senda del Montaraz».
  - `historyHidesMissingDifficulty`: con `null`, no aparece «Dificultad».
- [ ] **Step 2: Run** los tests nuevos. Expected: FAIL.
- [ ] **Step 3: Implement.**
  - La sección «Dificultad» va en Opciones tras los interruptores de audio. Son cuatro filas seleccionables con el estilo de los botones de Opciones, con nombre y línea corta.
  - Añadir a `CLAUDE.md` un párrafo de Estado «Niveles de dificultad (2026-10-24, spec …)» con los 4 niveles y porcentajes, el valor por defecto y la referencia de balance en `EYE`.
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.
- [ ] **Step 5: Manual en el emulador** (lo hace el controlador):
  - Opciones con el selector en es y en;
  - en «Paseo por la Comarca», la carta del rival muestra el ATQ a la mitad;
  - la crónica con la dificultad;
  - capturas `docs/screenshots/v3/difficulty-options.png` y `difficulty-combat.png`.
