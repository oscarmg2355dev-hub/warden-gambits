# Combat Engine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Pure-Kotlin `:engine` module that runs a deterministic, tick-based Warden-Gambits fight (player vs 1–4 rivals) driven by combo inputs.

**Architecture:** Immutable `CombatState` + `CombatEngine.reduce(state, action) -> StepResult(state, events)`. Combos are data (`Combos` catalog with pre-multiplied percentages). A `Tick` action runs the fixed phase order of spec §5. RNG state lives inside `CombatState`; time enters only as `Tick` actions.

**Tech Stack:** Kotlin 2.4.20 (JVM, toolchain 21), Gradle 9.7.1 wrapper, kotlin.test on JUnit 5.

**Spec:** `docs/superpowers/specs/2026-10-08-combat-engine-design.md`

## Global Constraints

- `:engine` uses `kotlin("jvm")` only; no `android.*`, `androidx.*`, no third-party runtime deps.
- No `System.currentTimeMillis`, `Random()`, `Math.random`; all randomness through `GameRandom`.
- All gameplay numbers are `Int`; percentages as Int (`150` = ×1.5); integer division truncates.
- Invalid actions never throw: return the unchanged state + `CombatEvent.ComboRejected(fighter, reason)`.
- Package root: `com.wardengambits.engine`. Sources `engine/src/main/kotlin/com/wardengambits/engine/`, tests `engine/src/test/kotlin/com/wardengambits/engine/`.
- Gradle must run with `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"` (JDK 21). Test command: `./gradlew :engine:test`.
- Exhaustive `when` over sealed types/enums, no `else` branches.
- Code/commits in English; user-facing docs in Spanish.

## Review Focus

1. `Execute` with an empty buffer → `ComboRejected(UNKNOWN_SEQUENCE)`, no crash, no power spent. (Task 4)
2. Player's target dies mid-tick (AoE kills several) → target moves to first alive rival; `null` when none alive, and the player's single-target combo is then rejected with `INVALID_TARGET` instead of crashing. (Task 7)
3. Drain larger than a rival's remaining HP → caster heals only what was actually taken. (Task 6)
4. Power exactly equal to cost → combo is cast and power ends at 0 (before regen). (Task 7)
5. Any action (including `Tick`) after `outcome != null` → rejected with `COMBAT_OVER`, state unchanged. (Task 7)

---

## File Structure

```
settings.gradle.kts, build.gradle.kts, gradle.properties, gradle/libs.versions.toml, gradlew(.bat), gradle/wrapper/*
engine/build.gradle.kts
engine/src/main/kotlin/com/wardengambits/engine/
  random/GameRandom.kt        GameRandom interface + SeededRandom (SplitMix64)
  combo/Input.kt              Input enum
  combo/ComboDefinition.kt    ComboDefinition, TargetKind, StatusRoll, BuffSpec
  combo/Combos.kt             the catalog (all 26 combos) + find()
  model/FighterId.kt          FighterId value class, PLAYER_ID
  model/Stats.kt              Stats + LevelStats.forLevel()
  model/ActiveEffect.kt       EffectKind, ActiveEffect
  model/Fighter.kt            Fighter
  model/CombatState.kt        CombatState, Outcome, CombatSetup, FighterSpec, RivalSpec
  action/CombatAction.kt      sealed CombatAction
  event/CombatEvent.kt        sealed CombatEvent, RejectReason
  rules/DamageFormula.kt      pure number formulas (§5.1)
  rules/ComboApplier.kt       applies one resolved combo
  rules/TickResolver.kt       Tick phases 0–4
  rules/CombatEngine.kt       start() + reduce() dispatch + input actions
  ai/RivalBrain.kt            RivalBrain interface + PatternBrain
engine/src/test/kotlin/com/wardengambits/engine/
  TestFixtures.kt             FixedRandom, builders
  (one *Test.kt per task, named below)
```

---

### Task 1: Gradle scaffold + LevelStats

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `engine/build.gradle.kts`, wrapper files
- Create: `engine/src/main/kotlin/com/wardengambits/engine/model/Stats.kt`
- Test: `engine/src/test/kotlin/com/wardengambits/engine/LevelStatsTest.kt`

**Interfaces:**
- Produces: `data class Stats(val maxHp: Int, val maxPower: Int, val powerRegen: Int, val attack: Int, val heal: Int, val defense: Int)`; `object LevelStats { fun forLevel(level: Int): Stats }`

- [ ] **Step 1: Scaffold Gradle**
  - `settings.gradle.kts`: `rootProject.name = "warden-gambits"`, `include(":engine")`, `pluginManagement`/`dependencyResolutionManagement` with `mavenCentral()` + `gradlePluginPortal()`.
  - `gradle/libs.versions.toml`: `kotlin = "2.4.20"`; plugin `kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }`.
  - Root `build.gradle.kts`: `plugins { alias(libs.plugins.kotlin.jvm) apply false }`.
  - `engine/build.gradle.kts`: `alias(libs.plugins.kotlin.jvm)`; `kotlin { jvmToolchain(21) }`; `dependencies { testImplementation(kotlin("test")) }`; `tasks.test { useJUnitPlatform() }`.
  - `gradle.properties`: `org.gradle.jvmargs=-Xmx2g`, `kotlin.code.style=official`.
  - Wrapper from the cached distribution:
    ```bash
    export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
    ~/.gradle/wrapper/dists/gradle-9.7.1-bin/1w1c7tv4s851m17nbqdsro2tv/gradle-9.7.1/bin/gradle wrapper --gradle-version 9.7.1
    ```
  - Add `.gitattributes`: `* text=auto eol=lf`, `*.bat text eol=crlf`, `*.jar binary`.

- [ ] **Step 2: Write failing test `LevelStatsTest`**

```kotlin
@Test fun level1() = assertEquals(Stats(maxHp = 100, maxPower = 100, powerRegen = 5, attack = 10, heal = 8, defense = 2), LevelStats.forLevel(1))
@Test fun level5() = assertEquals(Stats(180, 100, 5, 18, 16, 6), LevelStats.forLevel(5))
@Test fun level10() = assertEquals(Stats(280, 100, 5, 28, 26, 11), LevelStats.forLevel(10))
@Test fun levelBelowOneIsClampedToOne() = assertEquals(LevelStats.forLevel(1), LevelStats.forLevel(0))
```

- [ ] **Step 3: Run** `./gradlew :engine:test` → FAIL (unresolved `LevelStats`).
- [ ] **Step 4: Implement** formulas from spec §3.2 (L = max(1, level)).
- [ ] **Step 5: Run** `./gradlew :engine:test` → BUILD SUCCESSFUL, 4 tests passed.
- [ ] **Step 6: Commit** `build: scaffold gradle with pure kotlin engine module` (include wrapper, `.gitattributes`).

---

### Task 2: Deterministic random

**Files:**
- Create: `engine/src/main/kotlin/com/wardengambits/engine/random/GameRandom.kt`
- Create: `engine/src/test/kotlin/com/wardengambits/engine/TestFixtures.kt` (FixedRandom only for now)
- Test: `engine/src/test/kotlin/com/wardengambits/engine/SeededRandomTest.kt`

**Interfaces:**
- Produces:
  - `interface GameRandom { fun nextInt(bound: Int): Int; val state: Long }` — returns `0 until bound`.
  - `class SeededRandom(seed: Long) : GameRandom` — SplitMix64; `state` is the current internal 64-bit state, so `SeededRandom(r.state)` continues the same sequence.
  - Test-only `class FixedRandom(vararg rolls: Int) : GameRandom` — returns rolls in order, cycling; `state = 0`.

- [ ] **Step 1: Failing tests**

```kotlin
@Test fun sameSeedSameSequence() { val a = SeededRandom(42); val b = SeededRandom(42); assertEquals(List(20) { a.nextInt(100) }, List(20) { b.nextInt(100) }) }
@Test fun stateResumesSequence() { val a = SeededRandom(7); repeat(5) { a.nextInt(100) }; val b = SeededRandom(a.state); assertEquals(List(10) { a.nextInt(100) }, List(10) { b.nextInt(100) }) }
@Test fun valuesWithinBound() { val r = SeededRandom(1); repeat(1000) { assertTrue(r.nextInt(100) in 0..99) } }
@Test fun differentSeedsDiffer() { val a = SeededRandom(1); val b = SeededRandom(2); assertNotEquals(List(10) { a.nextInt(1000) }, List(10) { b.nextInt(1000) }) }
```

- [ ] **Step 2: Run** → FAIL. **Step 3: Implement** SplitMix64 (`state += 0x9E3779B97F4A7C15`; mix with the standard 30/27/31 shifts and constants `0xBF58476D1CE4E5B9`, `0x94D049BB133111EB`); `nextInt(bound) = ((mix ushr 1) % bound).toInt()`. Constructor stores `seed` as state.
- [ ] **Step 4: Run** → PASS. **Step 5: Commit** `feat(engine): add seeded deterministic random`.

---

### Task 3: Input and combo catalog

**Files:**
- Create: `combo/Input.kt`, `combo/ComboDefinition.kt`, `combo/Combos.kt`, `model/ActiveEffect.kt`
- Test: `ComboCatalogTest.kt`

**Interfaces:**
- Produces:
  - `enum class Input(val code: Char) { SPEAR('1'), SHIELD('2'), FIST('3') }`
  - `enum class EffectKind { DOT, CUT, POT, DEF, POW, SHM, SHT, DAZED, FEARED }` (in `model/ActiveEffect.kt`, together with `data class ActiveEffect(val kind: EffectKind, val sourceId: FighterId, val magnitude: Int, val remainingTicks: Int)` — create `model/FighterId.kt` with `@JvmInline value class FighterId(val value: Int)` and `val PLAYER_ID = FighterId(0)` here as well)
  - `enum class TargetKind { SELECTED, ALL_OPPONENTS, SELF }`
  - `data class StatusRoll(val kind: EffectKind, val chancePct: Int, val ticks: Int)`
  - `data class BuffSpec(val kind: EffectKind, val ticks: Int)`
  - `data class ComboDefinition(val sequence: String, val cost: Int, val target: TargetKind, val damagePct: Int = 0, val dotPct: Int = 0, val drainPct: Int = 0, val healPct: Int = 0, val cutPct: Int = 0, val status: StatusRoll? = null, val buff: BuffSpec? = null, val powerRestore: Int = 0) { val isOffensive: Boolean get() = damagePct > 0 || drainPct > 0 }`
  - `object Combos { const val OVER_TIME_TICKS = 3; val all: List<ComboDefinition>; fun find(sequence: String): ComboDefinition? }`
  - Percentages are stored **already multiplied by Mul** (e.g. `121` → `damagePct = 150, dotPct = 45`).

- [ ] **Step 1: Failing tests**

```kotlin
@Test fun has26Combos() = assertEquals(26, Combos.all.size)
@Test fun sequencesAreUnique() = assertEquals(Combos.all.size, Combos.all.map { it.sequence }.toSet().size)
@Test fun sequencesAreValid() = Combos.all.forEach { assertTrue(it.sequence.length in 2..5 && it.sequence.all { c -> c in "123" }, it.sequence) }
@Test fun costsArePositive() = Combos.all.forEach { assertTrue(it.cost > 0, it.sequence) }
@Test fun findUnknownReturnsNull() { assertNull(Combos.find("111")); assertNull(Combos.find("")) }
@Test fun samples() {
    assertEquals(ComboDefinition("121", 18, TargetKind.SELECTED, damagePct = 150, dotPct = 45), Combos.find("121"))
    assertEquals(ComboDefinition("31313", 36, TargetKind.SELECTED, damagePct = 200, dotPct = 50, status = StatusRoll(EffectKind.DAZED, 40, 3)), Combos.find("31313"))
    assertEquals(ComboDefinition("32", 15, TargetKind.ALL_OPPONENTS, damagePct = 60, dotPct = 20, status = StatusRoll(EffectKind.FEARED, 20, 2)), Combos.find("32"))
    assertEquals(ComboDefinition("2323", 33, TargetKind.ALL_OPPONENTS, drainPct = 80), Combos.find("2323"))
    assertEquals(ComboDefinition("21212", 34, TargetKind.SELF, healPct = 250, cutPct = 75), Combos.find("21212"))
    assertEquals(ComboDefinition("31232", 10, TargetKind.SELF, powerRestore = 50), Combos.find("31232"))
    assertEquals(ComboDefinition("2132", 20, TargetKind.SELF, buff = BuffSpec(EffectKind.SHM, 4)), Combos.find("2132"))
    assertEquals(ComboDefinition("33", 5, TargetKind.SELF, buff = BuffSpec(EffectKind.POW, 5)), Combos.find("33"))
}
```

- [ ] **Step 2: Run** → FAIL. **Step 3: Implement** all 26 rows from spec §4.2 tables verbatim. **Step 4: Run** → PASS.
- [ ] **Step 5: Commit** `feat(engine): add combo catalog`.

---

### Task 4: State model, start, input actions

**Files:**
- Create: `model/Fighter.kt`, `model/CombatState.kt`, `action/CombatAction.kt`, `event/CombatEvent.kt`, `rules/CombatEngine.kt`, `ai/RivalBrain.kt` (interface only)
- Modify: `TestFixtures.kt` (builders)
- Test: `CombatEngineInputTest.kt`

**Interfaces:**
- Consumes: `LevelStats`, `Combos`, `GameRandom`, `FighterId`, `ActiveEffect`.
- Produces:
  - `enum class Side { PLAYER, RIVAL }`
  - `data class Fighter(val id: FighterId, val name: String, val level: Int, val side: Side, val stats: Stats, val hp: Int, val power: Int, val buffer: String = "", val queued: ComboDefinition? = null, val effects: List<ActiveEffect> = emptyList(), val castCount: Int = 0) { val isAlive get() = hp > 0; fun has(kind: EffectKind): Boolean }`
  - `interface RivalBrain { fun decide(state: CombatState, self: FighterId, random: GameRandom): ComboDefinition? }`
  - `data class FighterSpec(val name: String, val level: Int)`; `data class RivalSpec(val name: String, val level: Int, val brain: RivalBrain)`; `data class CombatSetup(val player: FighterSpec, val rivals: List<RivalSpec>)` (`require(rivals.size in 1..4)` — a setup error, not gameplay)
  - `enum class Outcome { VICTORY, DEFEAT }`
  - `data class CombatState(val tick: Int, val fighters: List<Fighter>, val targetId: FighterId?, val brains: Map<FighterId, RivalBrain>, val rngState: Long, val outcome: Outcome? = null) { val player: Fighter; val rivals: List<Fighter>; fun fighter(id: FighterId): Fighter }` — `fighters[0]` is the player (`PLAYER_ID`), rivals get ids 1..n in setup order.
  - `sealed interface CombatAction { data class Press(val fighter: FighterId, val input: Input); data class Execute(val fighter: FighterId); data class ClearBuffer(val fighter: FighterId); data class SelectTarget(val target: FighterId); data object Tick }`
  - `enum class RejectReason { BUFFER_FULL, UNKNOWN_SEQUENCE, DAZED, FEARED, NOT_ENOUGH_POWER, FIGHTER_DEFEATED, COMBAT_OVER, INVALID_TARGET }`
  - `sealed interface CombatEvent { ComboQueued(fighter, combo: ComboDefinition); ComboRejected(fighter, reason); ComboResolved(fighter, combo); DamageDealt(source: FighterId, target: FighterId, amount: Int); Healed(target, amount); EffectApplied(target, kind, ticks); StatusResisted(target, kind); EffectTicked(target: FighterId, hpDelta: Int); EffectExpired(target, kind); FighterDefeated(id); CombatEnded(outcome) }` (all fighter params are `FighterId`)
  - `data class StepResult(val state: CombatState, val events: List<CombatEvent>)`
  - `class CombatEngine(private val randomFactory: (Long) -> GameRandom = ::SeededRandom) { fun start(setup: CombatSetup, seed: Long): CombatState; fun reduce(state: CombatState, action: CombatAction): StepResult }` — `start`: everyone at full hp/power, `tick = 0`, `targetId` = first rival, `rngState = seed`. In this task `Tick` returns the state unchanged (filled in Task 7).

- [ ] **Step 1: Failing tests** (builders in `TestFixtures.kt`: `fun setup(playerLevel: Int = 1, vararg rivalLevels: Int = intArrayOf(1)): CombatSetup` with a `NoopBrain` returning null; `fun CombatEngine.press(state, id, seq: String): CombatState` pressing each char)

```kotlin
@Test fun startFillsHpAndPower() { val s = engine.start(setup(1, 1, 5), seed = 1); assertEquals(listOf(100, 100, 180), s.fighters.map { it.hp }); assertTrue(s.fighters.all { it.power == 100 }); assertEquals(FighterId(1), s.targetId); assertEquals(0, s.tick) }
@Test fun pressAppendsToBuffer() = assertEquals("12", engine.press(start, PLAYER_ID, "12").player.buffer)
@Test fun sixthPressRejected() { val r = engine.reduce(engine.press(start, PLAYER_ID, "12121"), Press(PLAYER_ID, Input.SPEAR)); assertEquals("12121", r.state.player.buffer); assertEquals(listOf(ComboRejected(PLAYER_ID, BUFFER_FULL)), r.events) }
@Test fun executeKnownQueuesAndClears() { val r = engine.reduce(engine.press(start, PLAYER_ID, "121"), Execute(PLAYER_ID)); assertEquals(Combos.find("121"), r.state.player.queued); assertEquals("", r.state.player.buffer); assertEquals(100, r.state.player.power) }
@Test fun executeUnknownRejectsAndClears() { val r = engine.reduce(engine.press(start, PLAYER_ID, "111"), Execute(PLAYER_ID)); assertNull(r.state.player.queued); assertEquals("", r.state.player.buffer); assertEquals(listOf(ComboRejected(PLAYER_ID, UNKNOWN_SEQUENCE)), r.events) }
@Test fun executeEmptyBufferRejected() = assertEquals(listOf(ComboRejected(PLAYER_ID, UNKNOWN_SEQUENCE)), engine.reduce(start, Execute(PLAYER_ID)).events)   // Review Focus 1
@Test fun executeReplacesQueued() { /* queue "12", then "21" → queued == "21" */ }
@Test fun clearBufferEmpties()
@Test fun selectTargetAliveRival() = assertEquals(FighterId(2), engine.reduce(start2Rivals, SelectTarget(FighterId(2))).state.targetId)
@Test fun selectTargetInvalid() { /* PLAYER_ID, FighterId(9), and a rival with hp=0 → unchanged + ComboRejected(PLAYER_ID, INVALID_TARGET) */ }
@Test fun pressOnDefeatedFighterRejected() { /* rival hp=0 → ComboRejected(rivalId, FIGHTER_DEFEATED) */ }
```

- [ ] **Step 2: Run** → FAIL. **Step 3: Implement** models + `start` + `reduce` for Press/Execute/ClearBuffer/SelectTarget (Tick: unchanged state, no events). **Step 4: Run** → PASS.
- [ ] **Step 5: Commit** `feat(engine): add combat state and input actions`.

---

### Task 5: Damage formulas

**Files:**
- Create: `rules/DamageFormula.kt`
- Test: `DamageFormulaTest.kt`

**Interfaces:**
- Produces `object DamageFormula`:
  - `fun base(attack: Int, pct: Int, pot: Boolean): Int = attack * pct * (if (pot) 130 else 100) / 10_000`
  - `fun direct(base: Int, defense: Int, defBuff: Boolean, shield: Boolean): Int` — `effDef = defense * (150|100) / 100`; `max(1, max(1, base - effDef) * (50|100) / 100)`
  - `fun overTimeTick(magnitude: Int, shield: Boolean): Int` — `max(1, magnitude * (50|100) / 100)`
  - `fun heal(healStat: Int, pct: Int): Int = healStat * pct / 100`
  - `fun statusChance(chancePct: Int, tactical: Boolean): Int` — `chancePct * (50|100) / 100`

- [ ] **Step 1: Failing tests**

```kotlin
@Test fun baseNoPot() = assertEquals(10, DamageFormula.base(10, 100, false))
@Test fun baseWithPot() = assertEquals(13, DamageFormula.base(10, 100, true))
@Test fun baseMul() = assertEquals(25, DamageFormula.base(10, 250, false))
@Test fun directSubtractsDefense() = assertEquals(8, DamageFormula.direct(10, 2, false, false))
@Test fun directDefBuff() = assertEquals(7, DamageFormula.direct(10, 2, true, false))
@Test fun directShield() = assertEquals(4, DamageFormula.direct(10, 2, false, true))
@Test fun directMinimumOne() { assertEquals(1, DamageFormula.direct(2, 6, false, false)); assertEquals(1, DamageFormula.direct(2, 6, false, true)) }
@Test fun overTime() { assertEquals(3, DamageFormula.overTimeTick(3, false)); assertEquals(1, DamageFormula.overTimeTick(3, true)); assertEquals(1, DamageFormula.overTimeTick(1, true)) }
@Test fun heal() = assertEquals(20, DamageFormula.heal(8, 250))
@Test fun status() { assertEquals(40, DamageFormula.statusChance(40, false)); assertEquals(20, DamageFormula.statusChance(40, true)) }
```

- [ ] **Step 2–4:** Run → FAIL; implement; run → PASS.
- [ ] **Step 5: Commit** `feat(engine): add damage formulas`.

---

### Task 6: Combo applier

**Files:**
- Create: `rules/ComboApplier.kt`
- Test: `ComboApplierTest.kt`

**Interfaces:**
- Consumes: `DamageFormula`, `ComboDefinition`, `CombatState`, `GameRandom`.
- Produces: `object ComboApplier { fun apply(state: CombatState, casterId: FighterId, targets: List<FighterId>, combo: ComboDefinition, random: GameRandom): StepResult }` — power payment and validation are **not** here (Task 7). `targets` is already resolved by the caller (SELECTED → `[target]`, ALL_OPPONENTS → alive opponents in index order, SELF → `[caster]`). Increments caster `castCount`, emits `ComboResolved` first.

Rules (spec §3.3, §5.1), in this order per combo:
1. `damagePct`: per target `direct(base(A, pct, casterPot), targetDef, targetDEF, targetSHM)` → `DamageDealt`; target at 0 hp → `FighterDefeated`.
2. `dotPct`: per **still-alive** target add `ActiveEffect(DOT, caster, base(A, dotPct, casterPot), 3)` (always a new instance) → `EffectApplied`.
3. `drainPct`: per target `taken = min(direct(...), target.hp)`; caster heals `sum(taken)` clamped to maxHp → `DamageDealt` per target + `Healed(caster, actualHealed)`.
4. `healPct`: caster heals `heal(C, pct)` clamped → `Healed`. `cutPct`: add `ActiveEffect(CUT, caster, heal(C, cutPct), 3)`.
5. `status`: per alive target `roll = random.nextInt(100)`; applied if `roll < statusChance(chance, target has SHT)` → refresh-or-add effect (`EffectApplied`), else `StatusResisted`.
6. `buff`: refresh-or-add on caster (`EffectApplied`). Refresh = replace existing same-kind effect's `remainingTicks` and `sourceId`.
7. `powerRestore`: caster power `min(maxPower, power + n)`.

- [ ] **Step 1: Failing tests** (level-1 fighters: A=10, C=8, Def=2, hp 100)

```kotlin
@Test fun combo12() { /* rival hp 92, one DOT magnitude 3 ticks 3 */ }
@Test fun combo121() { /* base 15 → rival hp 87, DOT magnitude 4 */ }
@Test fun potBoostsDamageAndDot() { /* caster has POT; "12" → rival hp 89 (13-2), DOT magnitude 3 (10*30*130/10000=3) */ }
@Test fun shieldHalvesDirect() { /* rival has SHM; "12" → rival hp 96 */ }
@Test fun aoeHitsAllAliveOpponents() { /* 3 rivals, one dead; "32" → two alive at 96 (6-2), dead untouched, 2 DOTs of 2 */ }
@Test fun dotsStack() { /* apply "12" twice → 2 DOT effects on rival */ }
@Test fun dazeAppliedWhenRollBelowChance() { /* FixedRandom(24), "31" → rival has DAZED ticks 2 */ }
@Test fun dazeResistedWhenRollAtChance() { /* FixedRandom(25) → StatusResisted(rival, DAZED) */ }
@Test fun tacticalShieldHalvesChance() { /* rival has SHT, FixedRandom(12), "31" (25% → 12) → resisted; FixedRandom(11) → applied */ }
@Test fun drainHealsCaster() { /* caster hp 50, 2 rivals, "23" (40% of 10 = 4, -2 def = 2 each) → caster 54, rivals 98 */ }
@Test fun drainCappedByTargetHp() { /* rival hp 1, "23232" → caster heals exactly 1 */ }   // Review Focus 3
@Test fun healClampedToMax() { /* caster hp 95, "21" (8) → 100, Healed amount 5; CUT magnitude 2 */ }
@Test fun buffRefreshesNotStacks() { /* "11" twice with POT at 2 remaining → single POT, remaining 5 */ }
@Test fun powerRestoreClamped() { /* power 70, "31232" → 100 */ }
@Test fun lethalDamageEmitsDefeated() { /* rival hp 5, "12" → hp 0, FighterDefeated, no DOT added */ }
```

- [ ] **Step 2–4:** Run → FAIL; implement; run → PASS.
- [ ] **Step 5: Commit** `feat(engine): apply combo effects`.

---

### Task 7: Tick resolution

**Files:**
- Create: `rules/TickResolver.kt`
- Modify: `rules/CombatEngine.kt` (route `Tick` to `TickResolver`; reject every action when `outcome != null` with `COMBAT_OVER` attributed to `PLAYER_ID`)
- Test: `TickResolverTest.kt`

**Interfaces:**
- Consumes: `ComboApplier.apply`, `RivalBrain.decide`.
- Produces: `object TickResolver { fun resolve(state: CombatState, random: GameRandom): StepResult }`. `CombatEngine.reduce(Tick)` builds `random = randomFactory(state.rngState)`, calls `resolve`, then stores `random.state` into `rngState`.

Phases (spec §5 + §7), on `tick = state.tick + 1`:
0. **AI**: for each alive rival with `queued == null`: `brains[id].decide(stateWithNewTick, id, random)` → if non-null set `queued` + `ComboQueued`.
1. **Casts**: player, then rivals by index; skip dead (their `queued` is cleared). For each `queued` combo, clear it, then reject if `DAZED` → `DAZED`; `FEARED && combo.isOffensive` → `FEARED`; `power < cost` → `NOT_ENOUGH_POWER`; player SELECTED with `targetId == null` → `INVALID_TARGET`. Otherwise pay cost and `ComboApplier.apply` (rival SELECTED targets the player). After each cast: if `targetId` is dead → first alive rival or `null`.
2. **Over time**: per alive fighter in index order: `delta = Σ heal(CUT) − Σ overTimeTick(DOT, has SHM)`; hp = clamp(hp + delta, 0, maxHp) → `EffectTicked(id, delta)` when effects exist; 0 hp → `FighterDefeated`. Then for **every** fighter decrement all `remainingTicks`, remove those at 0 → `EffectExpired`.
3. **Regen**: alive fighters `power = min(maxPower, power + powerRegen * (POW ? 2 : 1))`.
4. **End**: player dead → `DEFEAT`; else all rivals dead → `VICTORY`; on either → `CombatEnded(outcome)`.

- [ ] **Step 1: Failing tests**

```kotlin
@Test fun tickIncrements()
@Test fun queuedComboResolvesOnTick() { /* queue "12", tick → rival hp 92 then DOT tick → 89; DOT remaining 2; player power 100-10+5 = 95 */ }
@Test fun exactPowerAllowed() { /* power 10, "12" → cast, power 0 then regen 5 */ }   // Review Focus 4
@Test fun notEnoughPowerRejected() { /* power 9 → ComboRejected NOT_ENOUGH_POWER, queued cleared, power 14 */ }
@Test fun dazedCannotCast() { /* player DAZED → DAZED, power not spent */ }
@Test fun fearedCannotAttackButCanHeal() { /* FEARED: "12" → FEARED; "21" → cast */ }
@Test fun playerResolvesBeforeRivals() { /* rival hp 5 with queued "12"; player "12" kills it → rival combo not cast, player hp 100 */ }
@Test fun dotAndCutNetted() { /* fighter with DOT 3 and CUT 2 at hp 50 → 49, EffectTicked(-1) */ }
@Test fun effectsExpire() { /* DAZED remaining 1 → removed after tick, EffectExpired */ }
@Test fun powBuffDoublesRegen() { /* power 50 with POW → 60 */ }
@Test fun retargetWhenTargetDies() { /* target rival 1 dies → targetId == FighterId(2) */ }
@Test fun aoeKillsAllTargetsNull() { /* both rivals die → targetId null, VICTORY */ }   // Review Focus 2
@Test fun singleTargetWithNoTargetRejected() { /* targetId null with queued "12" (state built directly) → INVALID_TARGET */ }
@Test fun victory() { /* last rival dies → outcome VICTORY + CombatEnded */ }
@Test fun defeatWinsTie() { /* player and last rival both die from DOT same tick → DEFEAT */ }
@Test fun actionsAfterEndRejected() { /* Tick and Press after VICTORY → state unchanged, ComboRejected(PLAYER_ID, COMBAT_OVER) */ }   // Review Focus 5
@Test fun aiQueuedComboCastSameTick() { /* rival brain returns "12" → player hp 92 after DOT 89 */ }
```

- [ ] **Step 2–4:** Run → FAIL; implement; run → PASS.
- [ ] **Step 5: Commit** `feat(engine): resolve combat ticks`.

---

### Task 8: PatternBrain + determinism + termination

**Files:**
- Modify: `ai/RivalBrain.kt` (add `PatternBrain`)
- Test: `PatternBrainTest.kt`, `CombatSimulationTest.kt`
- Modify: `CLAUDE.md` "Estado" section → engine done, next subproject: story engine.

**Interfaces:**
- Produces: `data class PatternBrain(val pattern: List<String>, val everyNTicks: Int = 1) : RivalBrain` — stateless: returns null if `state.tick % everyNTicks != 0`; next combo = `Combos.find(pattern[self.castCount % pattern.size])`; returns null if self `power < cost` (waits, same combo next time). `init { require(pattern.isNotEmpty() && pattern.all { Combos.find(it) != null }) }`.

- [ ] **Step 1: Failing tests**

```kotlin
// PatternBrainTest
@Test fun cyclesByCastCount() { /* castCount 0 → "12", 1 → "21", 2 → "12" */ }
@Test fun waitsOnCadence() { /* everyNTicks 2, tick 3 → null, tick 4 → combo */ }
@Test fun waitsForPower() { /* power 5, next "12" → null */ }
@Test fun rejectsUnknownCombo() = assertFailsWith<IllegalArgumentException> { PatternBrain(listOf("999")) }

// CombatSimulationTest — helper fun runFight(seed, level): List<StepResult>: each tick player presses "12"+Execute when power >= 10, then Tick; rivals PatternBrain(listOf("12", "31", "21")); stop at outcome or 500 ticks
@Test fun sameSeedSameFight() = assertEquals(runFight(99, 3).last().state, runFight(99, 3).last().state)
@Test fun fightsTerminate() = (1..10).forEach { lvl -> assertNotNull(runFight(lvl.toLong(), lvl).last().state.outcome, "level $lvl") }
@Test fun fourRivalsTerminate() { /* player level 5 vs 4 rivals level 1 → outcome != null within 500 ticks */ }
```

- [ ] **Step 2–4:** Run → FAIL; implement; run `./gradlew :engine:test` → all suites PASS.
- [ ] **Step 5: Full verification** `./gradlew :engine:build` → BUILD SUCCESSFUL, no compiler warnings in `:engine`.
- [ ] **Step 6: Commit** `feat(engine): add pattern AI and simulation tests`.
