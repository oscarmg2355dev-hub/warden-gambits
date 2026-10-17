# Story Engine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Pure-Kotlin story-mode logic (route, node trees, JSON content, run state, navigation, combat hookup, laps, history) inside `:engine`, plus the combat effect-duration fix.

**Architecture:** New package `com.wardengambits.engine.story` with immutable `StoryProfile` + `StoryEngine.reduce(profile, action) -> StoryStep(profile, events)`, mirroring the combat engine. Tree topology is computed (`TreeShape`), content is data (`ZoneContent` JSON via kotlinx.serialization), combat is reached through `combatSetupFor()` and reported back with `CombatFinished`.

**Tech Stack:** Kotlin 2.4.20 JVM (toolchain 21), Gradle 9.7.1, kotlin.test/JUnit 5, kotlinx.serialization (JSON).

**Spec:** `docs/superpowers/specs/2026-10-08-story-engine-design.md` (combat spec: `docs/superpowers/specs/2026-10-08-combat-engine-design.md`)

## Global Constraints

- Everything stays in `:engine` (`kotlin("jvm")`); no `android.*`/`androidx.*`. The only allowed runtime dependency is `kotlinx-serialization-json` (approved exception).
- No `System.currentTimeMillis`, `Random()`, `Math.random`; time enters as `nowMillis` parameters.
- Gameplay numbers are `Int`.
- Invalid story actions never throw: unchanged profile + `StoryEvent.StoryRejected(reason)`. Only content loading of a missing/malformed file throws (packaging error).
- Packages: story code in `com.wardengambits.engine.story` (content types in `com.wardengambits.engine.story.content`); tests in `engine/src/test/kotlin/com/wardengambits/engine/story/`.
- Gradle with `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`; tests `./gradlew :engine:test`.
- Exhaustive `when`, no `else`. Code in English; content strings and docs in Spanish.
- No git.

## Review Focus

1. A run whose current node has an encounter cannot advance with `Choose`/`Continue` before `CombatFinished` → `StoryRejected(WRONG_PHASE)`. (Task 5)
2. Defeat after several laps resets to lap 1 / level 1 / `bree_1_0` but keeps the full history (all prior records, in order). (Task 6)
3. Winning the combat on the final node is impossible because the validator forbids an encounter there; `StartNextLap` outside `LAP_COMPLETE` is rejected. (Tasks 4, 6)
4. Choice from a closing level with wrap-around (`(4,7) → {5_0, 5_3}`) returns destinations sorted ascending so option 0/1 are stable. (Task 2)
5. A refreshed, already-active status keeps counting down normally (refresh does not re-delay activation). (Task 1)

---

## File Structure

```
gradle/libs.versions.toml, build.gradle.kts, engine/build.gradle.kts   (+ serialization plugin & lib)
engine/src/main/kotlin/com/wardengambits/engine/
  model/ActiveEffect.kt, model/Fighter.kt, rules/ComboApplier.kt, rules/TickResolver.kt   (Task 1 modify)
  story/Route.kt            ZoneInfo, Route
  story/NodeId.kt           NodeId
  story/TreeShape.kt        level sizes + destinations
  story/content/StoryContent.kt          @Serializable ZoneContent/NodeContent/EncounterContent/RivalContent + StoryContent
  story/content/StoryContentLoader.kt    parse + loadFromResources
  story/content/ContentValidator.kt
  story/StoryState.kt       StoryProfile, RunState, Phase, RunRecord, RunResult
  story/StoryAction.kt      sealed StoryAction
  story/StoryEvent.kt       sealed StoryEvent, StoryRejectReason
  story/StoryEngine.kt      StoryEngine, StoryStep, Choice, ZoneProgress
engine/src/test/kotlin/com/wardengambits/engine/story/
  StoryFixtures.kt          fakeContent(...)
  (one *Test.kt per task, named below)
engine/src/test/resources/story-sample/bree.json   (Task 3)
```

---

### Task 1: Effect activation delay (combat fix, spec §8)

**Files:**
- Modify: `model/ActiveEffect.kt`, `model/Fighter.kt`, `rules/ComboApplier.kt`, `rules/TickResolver.kt`
- Modify: `docs/superpowers/specs/2026-10-08-combat-engine-design.md` (§3.3, §5: new rule, Spanish)
- Test: `engine/src/test/kotlin/com/wardengambits/engine/EffectTimingTest.kt`

**Interfaces:**
- Produces: `ActiveEffect(kind, sourceId, magnitude, remainingTicks, activeFrom: Int = 0)`; `Fighter.isActive(kind: EffectKind, tick: Int): Boolean = effects.any { it.kind == kind && it.activeFrom <= tick }`. `Fighter.has(kind)` stays (presence only).

Rules:
- In `ComboApplier` (current tick = `state.tick`): new POT/DEF/POW/SHM/SHT/DAZED/FEARED effects get `activeFrom = state.tick + 1`; DOT/CUT get `activeFrom = state.tick`. Refreshing an existing effect keeps its `activeFrom` (only `remainingTicks = max(...)` and `sourceId` change).
- Every rule check of a non-over-time kind uses `isActive(kind, state.tick)`: POT/DEF/SHM in damage, SHT in status chance, SHM in DOT tick, DAZED/FEARED in cast check, POW in regen.
- Expiry decrements only effects with `activeFrom <= state.tick`.

- [ ] **Step 1: Failing tests** (`CombatEngine { FixedRandom(0) }` so statuses always land; level-1 fighters; all rivals use `NoopBrain`; combos are queued for either side with `press(state, id, seq)` + `Execute(id)` before each `Tick`)

```kotlin
@Test fun playerDazeBlocksRivalExactlyTwoCasts() { /* tick 1: player "31", rival "12" → rival ComboResolved in tick 1; ticks 2,3: rival "12" → ComboRejected(rival, DAZED); tick 4: rival "12" → ComboResolved */ }
@Test fun rivalDazeBlocksPlayerExactlyTwoCasts() { /* tick 1: rival "31", player "12" → player resolves in tick 1 (acts first); ticks 2,3: player "12" → ComboRejected(PLAYER_ID, DAZED); tick 4: player resolves */ }
@Test fun potActiveExactlyFiveTicks() { /* player "11" at tick 1 → after tick 1: POT present, activeFrom 2, remainingTicks 5, isActive(POT, 1) == false; after tick 5: remainingTicks 1; after tick 6: no POT */ }
@Test fun dotStillHitsImmediately() { /* player "12" at tick 1 → rival hp 89 after tick 1, 86 after tick 2, 83 after tick 3, no DOT left after tick 3 */ }
@Test fun refreshKeepsActivation() { /* rival starts with ActiveEffect(DAZED, PLAYER_ID, 0, remainingTicks = 2, activeFrom = 2) at state.tick 1; player "31" in tick 2 → after tick 2 the single DAZED has activeFrom 2 and remainingTicks 1 (refresh max(2,2)=2, then active so decremented) */ }   // Review Focus 5
```

  Existing tests whose expectations change because of the delay may be updated; list each one and why in the report.
- [ ] **Step 2–4:** RED; implement; full `./gradlew :engine:test` GREEN.
- [ ] **Step 5:** Update combat spec §3.3/§5 with the rule above (Spanish, minimal).

---

### Task 2: Route, NodeId, TreeShape

**Files:**
- Create: `story/Route.kt`, `story/NodeId.kt`, `story/TreeShape.kt`
- Test: `story/TreeShapeTest.kt`

**Interfaces:**
- Produces:
  - `data class ZoneInfo(val id: String, val name: String)`; `object Route { val zones: List<ZoneInfo>; const val FINAL_ZONE_ID = "lothlorien"; val treeZones: List<ZoneInfo> /* first 14 */; fun zone(id: String): ZoneInfo; fun indexOf(zoneId: String): Int /* 0-based */ }` — the 15 ids/names of spec §2 verbatim, in order.
  - `data class NodeId(val zoneId: String, val level: Int, val index: Int) { val key: String; companion object { val START: NodeId /* bree,1,0 */; val FINAL: NodeId /* lothlorien,1,0 */ } }` — `key` = `"${zoneId}_${level}_$index"`, except FINAL → `"lothlorien_final"`.
  - `object TreeShape { val LEVEL_SIZES = listOf(1, 2, 4, 8, 4, 2, 1); const val LEVELS = 7; fun size(level: Int): Int; fun nodesOf(zoneId: String): List<NodeId> /* 22, by level then index; FINAL zone → [FINAL] */; fun destinations(node: NodeId): List<NodeId> }` — rules of spec §3; result sorted by index, distinct; FINAL → empty.

- [ ] **Step 1: Failing tests**

```kotlin
@Test fun routeHas15ZonesInOrder() { assertEquals(15, Route.zones.size); assertEquals("bree", Route.zones.first().id); assertEquals(ZoneInfo("caradhras", "Caradhras"), Route.zones[9]); assertEquals("lothlorien", Route.zones.last().id); assertEquals(14, Route.treeZones.size) }
@Test fun keys() { assertEquals("bree_4_5", NodeId("bree", 4, 5).key); assertEquals("lothlorien_final", NodeId.FINAL.key) }
@Test fun nodesPerZone() { assertEquals(22, TreeShape.nodesOf("moria").size); assertEquals(listOf(NodeId.FINAL), TreeShape.nodesOf("lothlorien")) }
@Test fun opening() { assertEquals(listOf(n(2,0), n(2,1)), TreeShape.destinations(n(1,0))); assertEquals(listOf(n(4,6), n(4,7)), TreeShape.destinations(n(3,3))) }   // n(l,i) = NodeId("bree", l, i)
@Test fun closingWrapsAndSorts() { assertEquals(listOf(n(5,0), n(5,3)), TreeShape.destinations(n(4,7))); assertEquals(listOf(n(5,1), n(5,2)), TreeShape.destinations(n(4,2))); assertEquals(listOf(n(6,0), n(6,1)), TreeShape.destinations(n(5,2))) }   // Review Focus 4
@Test fun singleDestinations() { assertEquals(listOf(n(7,0)), TreeShape.destinations(n(6,1))); assertEquals(listOf(NodeId("chet",1,0)), TreeShape.destinations(n(7,0))); assertEquals(listOf(NodeId.FINAL), TreeShape.destinations(NodeId("nimrodel",7,0))); assertEquals(emptyList(), TreeShape.destinations(NodeId.FINAL)) }
@Test fun everyNodeReachableAndLevels1to5HaveTwoOptions() { /* BFS from bree_1_0 within bree reaches all 22; every node with level in 1..5 has 2 distinct destinations */ }
```

- [ ] **Step 2–4:** RED; implement; GREEN.

---

### Task 3: Content model, serialization setup, loader

**Files:**
- Modify: `gradle/libs.versions.toml`, root `build.gradle.kts`, `engine/build.gradle.kts`
- Create: `story/content/StoryContent.kt`, `story/content/StoryContentLoader.kt`
- Create test resource: `engine/src/test/resources/story-sample/bree.json`
- Test: `story/StoryContentLoaderTest.kt`

**Interfaces:**
- Produces (all `@Serializable`, field names = spec §4 JSON keys):
  - `data class RivalContent(val name: String, val levelOffset: Int = 0, val pattern: List<String>, val everyNTicks: Int = 1)`
  - `data class EncounterContent(val rivals: List<RivalContent>)`
  - `data class NodeContent(val level: Int, val index: Int, val title: String, val text: String, val imagePrompt: String, val choiceLabel: String? = null, val encounter: EncounterContent? = null)`
  - `data class ZoneContent(val id: String, val name: String, val baseLevel: Int, val nodes: List<NodeContent>)`
  - `class StoryContent(val zones: List<ZoneContent>) { fun zone(id: String): ZoneContent?; fun node(id: NodeId): NodeContent? }` (not serializable)
  - `object StoryContentLoader { fun parse(fileName: String, json: String): ZoneContent; fun loadFromResources(resourceDir: String = "story"): StoryContent }` — `parse` wraps any parse error in `IllegalStateException` whose message contains `fileName`; `loadFromResources` reads `/<resourceDir>/<id>.json` for every `Route.zones` id via the classloader, throwing `IllegalStateException("Missing story file: <dir>/<id>.json")` if absent. Json config: `ignoreUnknownKeys = false`.
- Gradle: add `kotlin("plugin.serialization")` with version = kotlin version (`org.jetbrains.kotlin.plugin.serialization`) and `org.jetbrains.kotlinx:kotlinx-serialization-json` at the latest stable version compatible with Kotlin 2.4.20 (record the chosen version in the report), as `implementation`.

- [ ] **Step 1: Failing tests** — `bree.json` sample has 2 nodes: `(1,0)` without choiceLabel/encounter, and `(2,1)` with choiceLabel "Entrar en la posada" and one rival `{"name":"Bandido de Bree","levelOffset":0,"pattern":["12","21"],"everyNTicks":2}`.

```kotlin
@Test fun parsesSample() { val z = StoryContentLoader.parse("bree.json", resource("story-sample/bree.json")); assertEquals("bree", z.id); assertEquals(2, z.nodes.size); assertEquals(RivalContent("Bandido de Bree", 0, listOf("12", "21"), 2), z.nodes[1].encounter!!.rivals.single()); assertNull(z.nodes[0].choiceLabel) }
@Test fun defaultsApplied() { /* rival JSON without levelOffset/everyNTicks → 0 and 1 */ }
@Test fun malformedNamesFile() { val e = assertFailsWith<IllegalStateException> { StoryContentLoader.parse("moria.json", "{ not json") }; assertTrue("moria.json" in e.message!!) }
@Test fun unknownKeyRejected() { assertFailsWith<IllegalStateException> { StoryContentLoader.parse("x.json", """{"id":"x","name":"X","baseLevel":1,"nodes":[],"extra":1}""") } }
@Test fun missingResourceNamesFile() { val e = assertFailsWith<IllegalStateException> { StoryContentLoader.loadFromResources("story-sample") }; assertTrue("story-sample/chet.json" in e.message!!) }
@Test fun contentLookup() { /* StoryContent(listOf(sample)).node(NodeId("bree",2,1))?.title == sample title; unknown → null */ }
```

- [ ] **Step 2–4:** RED; implement; GREEN.

---

### Task 4: Content validator + fake content fixture

**Files:**
- Create: `story/content/ContentValidator.kt`
- Create: `engine/src/test/kotlin/com/wardengambits/engine/story/StoryFixtures.kt`
- Test: `story/ContentValidatorTest.kt`

**Interfaces:**
- Produces:
  - `object ContentValidator { fun validate(content: StoryContent): List<String> }` — checks of spec §4 "Validación", plus: the FINAL node has no encounter (Review Focus 3). Each error message names the zone id and, when relevant, the node key (e.g. `"bree_3_2: missing choiceLabel"`).
  - Test fixture: `fun fakeContent(encounters: Map<String, EncounterContent> = emptyMap(), baseLevels: Map<String, Int> = emptyMap()): StoryContent` — all 15 zones with every node of `TreeShape.nodesOf`, title/text/imagePrompt = `"T <key>"`, `"X <key>"`, `"P <key>"`, choiceLabel `"C <key>"` on levels 2..6, encounter from the map keyed by node key, `baseLevel` from the map or `1 + 3 * zoneIndex` (final zone 0). Also `fun rival(name: String = "Orco", offset: Int = 0) = RivalContent(name, offset, listOf("12"), 1)`.

- [ ] **Step 1: Failing tests**

```kotlin
@Test fun fakeContentIsValid() = assertEquals(emptyList(), ContentValidator.validate(fakeContent()))
@Test fun missingZone() { /* drop "moria" → an error containing "moria" */ }
@Test fun wrongZoneName() { /* bree name "Breee" → error containing "bree" */ }
@Test fun missingNode() { /* remove bree_4_5 → error containing "bree_4_5" */ }
@Test fun duplicateNode() { /* duplicate bree_2_0 → error containing "bree_2_0" */ }
@Test fun emptyText() { /* blank title/text/imagePrompt on bree_1_0 → 3 errors containing "bree_1_0" */ }
@Test fun missingChoiceLabel() { /* bree_3_2 choiceLabel null → error "bree_3_2"; bree_7_0 null → no error */ }
@Test fun encounterRivalCount() { /* 0 rivals and 5 rivals → errors */ }
@Test fun badPattern() { /* pattern [] → error; ["999"] → error; everyNTicks 0 → error */ }
@Test fun finalNodeEncounterRejected() { /* encounter on lothlorien_final → error containing "lothlorien_final" */ }   // Review Focus 3
```

- [ ] **Step 2–4:** RED; implement; GREEN.

---

### Task 5: Story state, newGame, navigation

**Files:**
- Create: `story/StoryState.kt`, `story/StoryAction.kt`, `story/StoryEvent.kt`, `story/StoryEngine.kt`
- Test: `story/StoryNavigationTest.kt`

**Interfaces:**
- Produces:
  - Spec §5 types verbatim (`StoryProfile`, `RunState`, `Phase`, `RunRecord`, `RunResult`).
  - `sealed interface StoryAction { data class CombatFinished(val outcome: Outcome, val nowMillis: Long); data class Choose(val option: Int); data object Continue; data class StartNextLap(val nowMillis: Long) }`
  - `enum class StoryRejectReason { WRONG_PHASE, INVALID_OPTION }`
  - `sealed interface StoryEvent { data class NodeEntered(val node: NodeId); data class ZoneEntered(val zoneId: String); data class LeveledUp(val newLevel: Int); data class RunEnded(val record: RunRecord); data class LapStarted(val lap: Int); data class StoryRejected(val reason: StoryRejectReason) }`
  - `data class StoryStep(val profile: StoryProfile, val events: List<StoryEvent>)`; `data class Choice(val destination: NodeId, val label: String?)`; `data class ZoneProgress(val zone: ZoneInfo, val level: Int, val total: Int)`
  - `class StoryEngine(private val content: StoryContent) { fun newGame(): StoryProfile; fun reduce(profile: StoryProfile, action: StoryAction): StoryStep; fun options(profile: StoryProfile): List<Choice>; fun node(id: NodeId): NodeContent; fun zoneProgress(profile: StoryProfile): ZoneProgress; fun combatSetupFor(profile: StoryProfile): CombatSetup? }` — in this task `CombatFinished`, `StartNextLap` and `combatSetupFor` are stubs: the actions return `StoryRejected(WRONG_PHASE)` and `combatSetupFor` returns `null` (filled in Task 6).
  - Entering a node (shared helper used here and in Task 6): append to `path`, set phase per spec §5, emit `NodeEntered`, plus `ZoneEntered` when level == 1 or node == FINAL.
  - `options`: `TreeShape.destinations(run.node)` mapped to `Choice(dest, label)` where label = destination's `choiceLabel` if there are 2 destinations, else `null`; empty unless phase is `CHOOSING`.
  - `node(id)` uses `content.node(id)!!` (content is validated beforehand).

- [ ] **Step 1: Failing tests** (`StoryEngine(fakeContent(...))`)

```kotlin
@Test fun newGame() { val p = engine.newGame(); assertEquals(RunState(1, 1, 1, 0, NodeId.START, listOf(NodeId.START), Phase.CHOOSING), p.run); assertEquals(emptyList(), p.history) }
@Test fun newGameOnEncounterNodeIsCombatPending() { /* fakeContent(encounters = mapOf("bree_1_0" to EncounterContent(listOf(rival())))) → phase COMBAT_PENDING */ }
@Test fun chooseMovesAndRecordsPath() { /* Choose(1) from bree_1_0 → node bree_2_1, path [bree_1_0, bree_2_1], events [NodeEntered(bree_2_1)] */ }
@Test fun optionsWithLabels() { /* at bree_1_0: [Choice(bree_2_0, "C bree_2_0"), Choice(bree_2_1, "C bree_2_1")] */ }
@Test fun continueOnSingleDestination() { /* walk to bree_6_0; options = [Choice(bree_7_0, null)]; Choose(0) → INVALID_OPTION; Continue → bree_7_0 */ }
@Test fun continueRejectedOnTwoDestinations() { /* at bree_1_0 Continue → StoryRejected(INVALID_OPTION), unchanged */ }
@Test fun chooseOutOfRange() { /* Choose(2) and Choose(-1) → INVALID_OPTION */ }
@Test fun zoneChangeEmitsZoneEntered() { /* from bree_7_0 Continue → chet_1_0, events [NodeEntered(chet_1_0), ZoneEntered("chet")]; zoneProgress → ZoneProgress(ZoneInfo("chet","Bosque de Chet"), 1, 7) */ }
@Test fun cannotAdvanceWhileCombatPending() { /* encounter on bree_2_0; Choose(0) into it; then Choose(0) → StoryRejected(WRONG_PHASE); Continue → WRONG_PHASE */ }   // Review Focus 1
@Test fun reachingFinalIsLapComplete() { /* walk option 0 / Continue from START until node == FINAL (98 actions) → phase LAP_COMPLETE, last events contain ZoneEntered("lothlorien"); options empty */ }
```

- [ ] **Step 2–4:** RED; implement; GREEN.

---

### Task 6: Combat hookup, defeat, laps, history

**Files:**
- Modify: `story/StoryEngine.kt`
- Modify: `CLAUDE.md` (Global note: kotlinx-serialization allowed in `:engine`; "Estado": story engine done, next = contenido)
- Test: `story/StoryProgressionTest.kt`

**Interfaces:**
- Consumes: `CombatSetup`, `FighterSpec`, `RivalSpec`, `PatternBrain`, `Outcome` from the combat engine.
- Produces: real `CombatFinished`, `StartNextLap`, `combatSetupFor` per spec §6–§7:
  - `combatSetupFor`: `null` unless `COMBAT_PENDING`; player `FighterSpec("Warden", run.level)`; rivals in content order with level `max(1, zone.baseLevel + offset + (run.lapStartLevel - 1))` and `PatternBrain(pattern, everyNTicks)`.
  - VICTORY: `level + 1`, `wins + 1`, phase `CHOOSING`, events `[LeveledUp(newLevel)]`.
  - DEFEAT: `record = RunRecord(lap, node, level, wins, DEFEATED, nowMillis)`; new profile = `newGame()` run with `history = old history + record`; events `[RunEnded(record), NodeEntered(START), ZoneEntered("bree")]`.
  - `StartNextLap`: `record = RunRecord(lap, FINAL, level, wins, COMPLETED, nowMillis)`; run = `RunState(level, lap + 1, level, 0, START, [START], phase-of-START)`; events `[RunEnded(record), LapStarted(lap + 1), NodeEntered(START), ZoneEntered("bree")]`.

- [ ] **Step 1: Failing tests**

```kotlin
@Test fun combatSetupLap1() { /* baseLevels bree=4; encounter on bree_1_0 with rival("Lobo", -1) and rival("Huargo", 2) → player level 1; rival levels [3, 6]; names ["Lobo", "Huargo"]; brains PatternBrain(listOf("12"), 1) */ }
@Test fun combatSetupClampsToOne() { /* bree base 1, offset -3 → level 1 */ }
@Test fun combatSetupNullWhenNotPending() = assertNull(engine.combatSetupFor(engine.newGame()))
@Test fun victoryLevelsUp() { /* COMBAT_PENDING at START; CombatFinished(VICTORY, 0) → level 2, wins 1, phase CHOOSING, events [LeveledUp(2)] */ }
@Test fun combatFinishedRejectedWhenNotPending() { /* phase CHOOSING → WRONG_PHASE */ }
@Test fun defeatResetsAndRecords() { /* encounter on bree_2_0; win at none, walk Choose(0) → bree_2_0; CombatFinished(DEFEAT, 1234) → run == newGame().run, history == [RunRecord(1, bree_2_0, 1, 0, DEFEATED, 1234)] */ }
@Test fun startNextLapOnlyWhenComplete() { /* newGame StartNextLap(0) → WRONG_PHASE */ }   // Review Focus 3
@Test fun fullLapThenNextLap() { /* encounters on every zone's level-1 node (14), fakeContent baseLevels default; play: win every combat, Choose(0)/Continue otherwise, until LAP_COMPLETE → level 15, wins 14; StartNextLap(99) → history [RunRecord(1, FINAL, 15, 14, COMPLETED, 99)], run lap 2, lapStartLevel 15, level 15, wins 0, node START, phase COMBAT_PENDING; combatSetupFor rival level at bree = 1 + 0 + 14 = 15 */ }
@Test fun defeatInLap2KeepsHistory() { /* continue previous scenario: lose at START → run lap 1 level 1; history has 2 records [COMPLETED lap1, DEFEATED lap2 level 15] in that order */ }   // Review Focus 2
```

- [ ] **Step 2–4:** RED; implement; full `./gradlew :engine:build` GREEN, no `w:` lines.
- [ ] **Step 5:** Update `CLAUDE.md` as listed (Spanish, few lines).
