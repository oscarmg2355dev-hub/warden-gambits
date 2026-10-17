# Android App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A playable Android app (`:app`) on top of `:engine`: menu, route map, node screen, real-time combat, character/history, combo help, persistent saves.

**Architecture:** Single Activity + Navigation Compose + Material 3. `AppContainer` (manual DI) builds content, engines, `SaveRepository` (DataStore) and `GameSession` (single owner of `StoryProfile` as `StateFlow`, every story action → `StoryEngine.reduce` → save). Screen ViewModels read the session; `CombatViewModel` owns a `CombatState` and ticks every 1000 ms while active.

**Tech Stack:** Kotlin 2.4.20, AGP 9.4.1 (cached), Gradle 9.7.1, Compose BOM + Material 3, Navigation Compose, Lifecycle ViewModel Compose, DataStore Preferences, kotlinx-serialization-json 1.11.0, kotlinx-coroutines-test, Turbine, Robolectric + Compose ui-test-junit4.

**Spec:** `docs/superpowers/specs/2026-10-09-android-app-design.md`

## Global Constraints

- `:app` depends on `:engine`; never the reverse. No game rules in composables.
- `applicationId` `com.wardengambits`, package `com.wardengambits.app`, minSdk 26, compile/targetSdk 36, portrait only, single `MainActivity`.
- All versions in `gradle/libs.versions.toml`; choose the latest stable versions compatible with AGP 9.4.1 / Kotlin 2.4.20 and record them in the report. No DI framework, no extra icon/image libraries (only `material-icons-core` if needed).
- `local.properties` with `sdk.dir=C\:\\Users\\pakmg\\AppData\\Local\\Android\\Sdk` (gitignored). Gradle with `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`.
- Time and randomness: `nowMillis` comes from an injected `() -> Long` clock (`System::currentTimeMillis` in production only); tick interval injected (default 1000 ms).
- UI strings in Spanish (`res/values/strings.xml` for fixed copy); code in English. Colors/typography per spec §7.
- Tests: `./gradlew :engine:test :app:testDebugUnitTest`; UI tests run on the JVM with Robolectric (`testOptions.unitTests.isIncludeAndroidResources = true`). No git.

## Review Focus

1. App killed mid-combat → reopening shows the node in `COMBAT_PENDING` and combat restarts (no saved combat state). (Tasks 3, 6)
2. Corrupt or old-version save → new game, no crash. (Tasks 1, 3)
3. App backgrounded during combat → ticks stop; resume continues; pause button too. (Task 6)
4. Back press during combat → confirm dialog; confirming counts as defeat (history record, reset to Bree). (Task 7)
5. Missing node image → placeholder with zone name, never a crash. (Task 5)

---

### Task 1: Engine — serializable profile, isValid, SaveData

**Files:**
- Modify: `engine/src/main/kotlin/com/wardengambits/engine/story/{StoryState.kt, NodeId.kt, StoryEngine.kt}`
- Create: `engine/src/main/kotlin/com/wardengambits/engine/story/SaveData.kt`
- Test: `engine/src/test/kotlin/com/wardengambits/engine/story/SaveDataTest.kt`

**Interfaces:**
- Produces: `@Serializable` on `StoryProfile`, `RunState`, `RunRecord`, `NodeId`, `Phase`, `RunResult` (`NodeId.key` not serialized); `@Serializable data class SaveData(val version: Int = CURRENT_VERSION, val profile: StoryProfile) { companion object { const val CURRENT_VERSION = 1 } }`; `object SaveCodec { fun encode(profile: StoryProfile): String; fun decode(json: String): SaveData? /* null on any parse error */ }`; `fun StoryEngine.isValid(profile: StoryProfile): Boolean` (member) per spec §3.2.

- [ ] **Step 1: Failing tests**

```kotlin
@Test fun roundTrip() { /* profile after a few actions incl. a RunRecord in history → decode(encode(p))!!.profile == p, version 1 */ }
@Test fun corruptJsonIsNull() { assertNull(SaveCodec.decode("{oops")); assertNull(SaveCodec.decode("")) }
@Test fun newGameIsValid() = assertTrue(engine.isValid(engine.newGame()))
@Test fun invalidProfiles() { /* each false: node (bree,9,0); node unknown zone "mordor"; phase COMBAT_PENDING on a node without encounter; LAP_COMPLETE on non-final; level 0; lap 0; wins -1; empty path; path last != node */ }
```
- [ ] **Step 2–4:** RED; implement; `./gradlew :engine:test` GREEN.

---

### Task 2: `:app` scaffold, theme, fonts, Robolectric

**Files:**
- Modify: `settings.gradle.kts` (`include(":app")`), `gradle/libs.versions.toml`, root `build.gradle.kts`, `.gitignore` if needed
- Create: `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `local.properties`
- Create: `app/src/main/java/com/wardengambits/app/{WardenApp.kt, MainActivity.kt, AppContainer.kt}`, `ui/theme/{Color.kt, Type.kt, Theme.kt}`, `ui/menu/MenuScreen.kt` (static for now)
- Create: `app/src/main/res/font/cinzel.ttf` (download the OFL variable font `Cinzel[wght].ttf` from `https://github.com/google/fonts/raw/main/ofl/cinzel/` and rename), `app/src/main/assets/licenses/cinzel-OFL.txt` (its `OFL.txt`), `res/values/strings.xml`, `res/values/themes.xml` (dark, no action bar)
- Test: `app/src/test/java/com/wardengambits/app/MenuScreenTest.kt`

**Interfaces:**
- Produces: `WardenTheme { }` composable; `WardenColors` (spec §7 hex values); `CinzelFamily: FontFamily`; `class AppContainer(context: Context)` exposing `val content: StoryContent` (`StoryContentLoader.loadFromResources()` — engine resources are packaged in the APK), `val storyEngine: StoryEngine`, `val combatEngine: CombatEngine`, `val clock: () -> Long`; `WardenApp.container`.
- `MenuScreen(onStory: () -> Unit, onCharacter: () -> Unit, onHelp: () -> Unit, onNewAdventure: () -> Unit)` — title "Warden-Gambits" + 4 buttons ("Modo Historia", "Personaje", "Ayuda de combos", "Nueva aventura").

- [ ] **Step 1: Failing test** (Robolectric + `createComposeRule`): `menuShowsButtonsAndCallsBack` — the 4 labels are displayed; clicking "Personaje" invokes `onCharacter`.
- [ ] **Step 2–4:** RED; implement scaffold; `./gradlew :app:testDebugUnitTest :app:assembleDebug` GREEN. Also verify `StoryContentLoader.loadFromResources()` works inside the app by a Robolectric test `appContainerLoadsContent` (`ContentValidator.validate(container.content)` is empty).

---

### Task 3: SaveRepository + GameSession

**Files:**
- Create: `app/src/main/java/com/wardengambits/app/data/SaveRepository.kt`, `app/src/main/java/com/wardengambits/app/GameSession.kt`
- Modify: `AppContainer.kt` (DataStore `preferencesDataStore("warden_save")`, repository, session)
- Test: `app/src/test/java/com/wardengambits/app/data/SaveRepositoryTest.kt`, `GameSessionTest.kt`

**Interfaces:**
- Produces:
  - `class SaveRepository(private val dataStore: DataStore<Preferences>, private val engine: StoryEngine) { suspend fun load(): StoryProfile; suspend fun save(profile: StoryProfile) }` — key `stringPreferencesKey("save_json")`; load rules spec §4 (missing/corrupt/version != 1/!isValid → `engine.newGame()`, `Log.w`).
  - `class GameSession(private val engine: StoryEngine, private val repository: SaveRepository, private val scope: CoroutineScope, private val clock: () -> Long) { val profile: StateFlow<StoryProfile>; suspend fun load(); fun dispatch(action: StoryAction): StoryStep; fun newAdventure() }` — `dispatch` reduces, updates the flow and launches `save` when the profile changed; `newAdventure()` = `newGame()` run with the current history kept, saved.

- [ ] **Step 1: Failing tests** (DataStore via `PreferenceDataStoreFactory.create(scope = testScope.backgroundScope) { tmpFolder.newFile("t.preferences_pb") }`)

```kotlin
@Test fun emptyLoadsNewGame()
@Test fun saveThenLoadRoundTrip() { /* profile after Choose(0) */ }
@Test fun corruptJsonLoadsNewGame() { /* write "{oops" under the key */ }
@Test fun invalidProfileLoadsNewGame() { /* valid JSON whose node is (bree,9,0) */ }
@Test fun wrongVersionLoadsNewGame() { /* version 2 */ }
// GameSessionTest (runTest)
@Test fun dispatchUpdatesFlowAndSaves() { /* Choose(0) → profile.value.run.node == bree_2_0; repository.load() returns it after advanceUntilIdle */ }
@Test fun rejectedActionDoesNotSave() { /* Continue at START → events StoryRejected; stored profile unchanged */ }
@Test fun newAdventureKeepsHistory() { /* profile with 1 history record → newAdventure → run == newGame().run, history size 1 */ }
@Test fun killedMidCombatRestartsCombat() { /* reach a COMBAT_PENDING node, new session with same repository → load() → phase COMBAT_PENDING */ }   // Review Focus 1
```
- [ ] **Step 2–4:** RED; implement; GREEN.

---

### Task 4: Navigation, Menu wiring, Map screen

**Files:**
- Create: `navigation/WardenNavHost.kt` (routes: `menu`, `map`, `node`, `combat`, `character`, `help`), `ui/map/{MapScreen.kt, MapViewModel.kt}`
- Modify: `MainActivity.kt` (load session then show NavHost), `ui/menu/MenuScreen.kt` (new-adventure confirm dialog)
- Test: `ui/map/MapViewModelTest.kt`, `ui/map/MapScreenTest.kt`

**Interfaces:**
- Produces: `data class ZoneRowUi(val name: String, val status: ZoneStatus, val level: Int?, val total: Int?)`; `enum class ZoneStatus { DONE, CURRENT, LOCKED }`; `MapViewModel(session: GameSession, engine: StoryEngine)` with `val rows: StateFlow<List<ZoneRowUi>>` (15 rows in route order; CURRENT row has level/total from `zoneProgress`); `MapScreen(rows, onContinue, onBack)`.
- ViewModel factories via `viewModelFactory { initializer { ... } }` reading `WardenApp.container`.

- [ ] **Step 1: Failing tests**
```kotlin
@Test fun newGameRows() { /* rows[0] CURRENT level 1 total 7; rows[1..14] LOCKED */ }
@Test fun midRouteRows() { /* profile at chet_3_x → bree DONE, chet CURRENT level 3, rest LOCKED */ }
@Test fun finalNodeRows() { /* at lothlorien_final → first 14 DONE, lothlorien CURRENT level 1 total 1 */ }
@Test fun mapScreenShowsProgressAndContinue() { /* Robolectric: "Pantanos de Moscagua" shown, "2/7" shown, click "Continuar" calls back */ }
@Test fun newAdventureAsksConfirmation() { /* MenuScreen: click "Nueva aventura" → dialog; confirm calls onNewAdventure; cancel doesn't */ }
```
- [ ] **Step 2–4:** RED; implement; GREEN; `:app:assembleDebug` GREEN.

---

### Task 5: Node screen + NodeImage

**Files:**
- Create: `ui/node/{NodeScreen.kt, NodeViewModel.kt}`, `ui/common/NodeImage.kt`
- Modify: `navigation/WardenNavHost.kt`
- Test: `ui/node/NodeViewModelTest.kt`, `ui/node/NodeScreenTest.kt`

**Interfaces:**
- Produces:
  - `sealed interface NodeActions { data class Fight(val rivals: List<String> /* "Nombre · Nv X" */) : NodeActions; data class Choices(val options: List<Choice>) : NodeActions /* 2 */; data object Continue : NodeActions; data class NextLap(val nextLap: Int) : NodeActions }`
  - `data class NodeUi(val key: String, val zoneId: String, val zoneName: String, val level: Int, val total: Int, val title: String, val text: String, val actions: NodeActions)`
  - `NodeViewModel(session, engine)`: `val node: StateFlow<NodeUi>`; `fun choose(index: Int)`; `fun continueOn()`; `fun startNextLap()` (uses `clock()`).
  - `NodeImage(key: String, zoneId: String, zoneName: String, modifier)` — `resources.getIdentifier(key, "drawable", packageName)`; 0 → placeholder (vertical gradient of a per-zone color from a fixed 15-color list + zone name in Cinzel). Remembered per key.
  - `NodeScreen(ui: NodeUi, onFight, onChoose, onContinue, onNextLap, onMap)`.

- [ ] **Step 1: Failing tests**
```kotlin
@Test fun choosingNodeActions() { /* START → Choices with labels "…" from content of bree_2_0/2_1 */ }
@Test fun fightActionsListRivalLevels() { /* a COMBAT_PENDING node → Fight(["<name> · Nv <level from combatSetupFor>", …]) */ }
@Test fun continueAndNextLap() { /* level-6 node → Continue; final node → NextLap(2) */ }
@Test fun chooseDispatches() { /* choose(1) → session node == bree_2_1 */ }
@Test fun nodeScreenRendersAndClicks() { /* Robolectric: title/text shown, both choice buttons, click second → onChoose(1); Fight shows "¡Luchar!" */ }
@Test fun missingImageShowsPlaceholder() { /* NodeImage with key "nope_1_0" shows the zone name, no crash */ }   // Review Focus 5
```
- [ ] **Step 2–4:** RED; implement; GREEN.

---

### Task 6: CombatViewModel

**Files:**
- Create: `ui/combat/{CombatViewModel.kt, CombatUiState.kt}`
- Test: `ui/combat/CombatViewModelTest.kt`

**Interfaces:**
- Produces:
  - `data class FighterUi(val id: FighterId, val name: String, val level: Int, val hp: Int, val maxHp: Int, val power: Int, val maxPower: Int, val effects: List<EffectKind>, val isAlive: Boolean, val isTarget: Boolean)`
  - `data class CombatUiState(val zoneName: String, val levelLabel: String /* "3/7" */, val tick: Int, val paused: Boolean, val rivals: List<FighterUi>, val player: FighterUi, val buffer: String, val bufferMatch: ComboDefinition?, val outcome: Outcome?, val levelBefore: Int, val record: RunRecord? /* filled after defeat is confirmed */)`
  - `class CombatViewModel(session: GameSession, storyEngine: StoryEngine, combatEngine: CombatEngine, clock: () -> Long, tickMillis: Long = 1000L) : ViewModel` with `val state: StateFlow<CombatUiState>`, `val events: SharedFlow<CombatEvent>`, `fun press(input: Input)`, `fun execute()`, `fun clearBuffer()`, `fun selectTarget(id: FighterId)`, `fun togglePause()`, `fun setActive(active: Boolean)` (called by the screen from lifecycle STARTED/STOPPED), `fun finish()` (sends `CombatFinished(outcome, clock())` once, after outcome != null), `fun abandon()` (sends `CombatFinished(DEFEAT, clock())`).
  - Seed = `clock()` at construction; setup = `storyEngine.combatSetupFor(session.profile.value)`; if null, `state.outcome` stays null and `val invalid = true` (screen navigates back).
  - Tick loop: a single coroutine in `viewModelScope` that, while `active && !paused && outcome == null`, delays `tickMillis` then reduces `Tick`. Every reduce emits its events to `events`. Effects list shows only kinds that are active (`isActive(kind, tick)`) plus DOT/CUT.

- [ ] **Step 1: Failing tests** (`StandardTestDispatcher` as Main via `Dispatchers.setMain`; fake clock; session on an in-memory DataStore; profile at a COMBAT_PENDING node with one weak rival)
```kotlin
@Test fun ticksEverySecondWhileActive() { /* setActive(true); advanceTimeBy(3000); runCurrent → tick == 3 */ }
@Test fun noTicksWhenInactiveOrPaused() { /* setActive(false) → tick stays 0; active+togglePause → stays; togglePause again → advances */ }   // Review Focus 3
@Test fun inputsReachEngine() { /* press(SPEAR), press(SHIELD) → buffer "12", bufferMatch = Combos.find("12"); execute → buffer "" ; next tick → rival hp drops */ }
@Test fun selectTargetMovesMarker()
@Test fun victoryFinishLevelsUp() { /* drive until outcome VICTORY (strong player via profile level), finish() → session phase CHOOSING, level +1; second finish() no-op */ }
@Test fun defeatFinishResets() { /* weak player vs strong rival; outcome DEFEAT; finish() → session node START, history +1 */ }
@Test fun abandonCountsAsDefeat() { /* abandon() → history +1, run reset */ }   // Review Focus 4
@Test fun ticksStopAfterOutcome()
```
- [ ] **Step 2–4:** RED; implement; GREEN.

---

### Task 7: Combat screen

**Files:**
- Create: `ui/combat/CombatScreen.kt`, `ui/common/{StatBar.kt, EffectIcons.kt, InputIcons.kt}`
- Modify: `navigation/WardenNavHost.kt` (node "¡Luchar!" → combat; result → node or map; `BackHandler` → confirm dialog → `abandon()` → map)
- Test: `ui/combat/CombatScreenTest.kt`

**Interfaces:**
- Consumes: `CombatViewModel`, `CombatUiState`.
- Produces: `CombatScreen(state: CombatUiState, onPress: (Input) -> Unit, onExecute, onClear, onSelectTarget: (FighterId) -> Unit, onTogglePause, onFinish, onAbandon)` — stateless; layout per spec §5 "Combate" (header, 2×2 rival grid with empty slots, player card with buffer icons + match name, button row SPEAR/SHIELD/FIST/⌫/⚔ ≥56dp, result overlay: victory "Nivel N → N+1" + "Continuar", defeat "Has caído" + record summary + "Volver a Bree"). A wrapper `CombatRoute(viewModel, …)` collects state with `collectAsStateWithLifecycle`, calls `setActive` from a `LifecycleEventEffect`/`repeatOnLifecycle(STARTED)`, collects `events` for haptics (`LocalHapticFeedback`) and floating damage numbers, and shows the back-confirm dialog ("¿Abandonar el combate? Contará como derrota.").
- `EffectIcons`: short Spanish badge per `EffectKind` (DoT, CuT, Pot, Def, Pow, Esc.F, Esc.T, Daze, Miedo) with colors per spec §7.

- [ ] **Step 1: Failing tests** (Robolectric, stateless screen with sample states)
```kotlin
@Test fun showsRivalCardsAndPlayer() { /* 3 rivals → 3 names + "Nv" labels, empty 4th slot, target marker on the target */ }
@Test fun buttonsInvokeCallbacks() { /* SPEAR/SHIELD/FIST → onPress(SPEAR/SHIELD/FIST); ⚔ → onExecute; ⌫ → onClear */ }
@Test fun tapRivalSelectsTarget()
@Test fun victoryOverlay() { /* outcome VICTORY, levelBefore 3 → "Nivel 3 → 4", "Continuar" → onFinish */ }
@Test fun defeatOverlay() { /* outcome DEFEAT → "Has caído", "Volver a Bree" → onFinish */ }
@Test fun bufferShowsMatch() { /* buffer "121", match → shows a label containing "121" */ }
```
- [ ] **Step 2–4:** RED; implement; GREEN; `:app:assembleDebug` GREEN.

---

### Task 8: Character + Help screens

**Files:**
- Create: `ui/character/{CharacterScreen.kt, CharacterViewModel.kt}`, `ui/help/{HelpScreen.kt, ComboDescriber.kt}`
- Modify: `navigation/WardenNavHost.kt`
- Test: `ui/help/ComboDescriberTest.kt`, `ui/character/CharacterViewModelTest.kt`, `ui/character/CharacterScreenTest.kt`

**Interfaces:**
- Produces:
  - `enum class ComboFamily(val title: String) { DAMAGE("Daño"), AREA("Área"), HEAL("Curación"), UTILITY("Utilidad") }`
  - `object ComboDescriber { fun family(def: ComboDefinition): ComboFamily; fun describe(def: ComboDefinition): String; fun grouped(): Map<ComboFamily, List<ComboDefinition>> }` — family: SELECTED+damage → DAMAGE; ALL_OPPONENTS → AREA; healPct>0 → HEAL; else UTILITY.
  - `data class CharacterUi(val level: Int, val lap: Int, val wins: Int, val zoneName: String, val progress: String, val stats: Stats, val history: List<HistoryRowUi>)`, `data class HistoryRowUi(val lap: Int, val result: String /* "Completada" | "Caído" */, val place: String /* zone name */, val level: Int, val wins: Int, val date: String)` (history newest first; date formatted with `DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")` in the device zone).
  - `HelpScreen(groups)` with intro text (spec §5 "Ayuda").

- [ ] **Step 1: Failing tests**
```kotlin
@Test fun describe121() = assertEquals("Daño 150% del ataque + 45% por tick (3 ticks)", ComboDescriber.describe(Combos.find("121")!!))
@Test fun describe31313() = assertEquals("Daño 200% del ataque + 50% por tick (3 ticks) · Aturdir 40% (3 ticks)", ComboDescriber.describe(Combos.find("31313")!!))
@Test fun describe23() = assertEquals("Roba 40% del ataque a cada rival", ComboDescriber.describe(Combos.find("23")!!))
@Test fun describeUtility() { assertEquals("Escudo físico: −50% daño recibido (4 ticks)", ComboDescriber.describe(Combos.find("2132")!!)); assertEquals("Recupera 50 de power", ComboDescriber.describe(Combos.find("31232")!!)) }
@Test fun groupedCoversAll26() = assertEquals(26, ComboDescriber.grouped().values.sumOf { it.size })
@Test fun characterUi() { /* profile level 5 lap 2 with 2 history records → stats == LevelStats.forLevel(5); history newest first; result strings */ }
@Test fun characterScreenShowsHistory() { /* Robolectric: "Caído" and "Completada" rows displayed */ }
```
  (Other combo descriptions follow the same style: area "Daño en área 60% del ataque + 20% por tick (3 ticks) · Miedo 20% (2 ticks)"; heal "Cura 100% de la cura + 30% por tick (3 ticks)"; Pot "Potenciar daño +30% (5 ticks)"; Def "Defensa +50% (5 ticks)"; Pow "Regeneración de power ×2 (5 ticks)"; ShT "Escudo táctico: −50% prob. de Daze/Miedo (4 ticks)".)
- [ ] **Step 2–4:** RED; implement; GREEN.

---

### Task 9: Emulator smoke run + docs

**Files:**
- Modify: `CLAUDE.md` ("Estado": app jugable; cómo añadir imágenes en `app/src/main/res/drawable-nodpi/<key>.webp`; siguiente: imágenes + ajuste de dificultad jugando)
- Create: `docs/screenshots/` (PNG captures: menu, map, node, combat, character, help)

- [ ] **Step 1:** `./gradlew :engine:test :app:testDebugUnitTest :app:assembleDebug` → all GREEN, no `w:` lines from `:app`/`:engine` Kotlin compilation.
- [ ] **Step 2:** Start emulator `Medium_Phone_API_36.1` in background (`emulator -avd Medium_Phone_API_36.1 -no-snapshot-save`), wait for `adb wait-for-device` + `sys.boot_completed == 1`, `./gradlew :app:installDebug`, launch `adb shell am start -n com.wardengambits/com.wardengambits.app.MainActivity`.
- [ ] **Step 3:** Drive with `adb shell input tap` (coordinates from `adb shell uiautomator dump`) through: menu → map → node → choose → … until a combat node → combat (press 1,2, ⚔, wait 3 s) ; also open Personaje and Ayuda. Capture each screen with `adb exec-out screencap -p > docs/screenshots/<name>.png`. Check `adb logcat -d -s AndroidRuntime:E` has no crash.
- [ ] **Step 4:** Look at each screenshot (Read tool) and report visual problems (overlaps, cut text, unreadable contrast). Fix only blocking issues; list the rest in the report.
- [ ] **Step 5:** Update `CLAUDE.md`.
