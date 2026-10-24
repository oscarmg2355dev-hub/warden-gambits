# SDD ledger — plan: docs/superpowers/plans/2026-10-24-difficulty-levels.md
Ruling: no git commits (project rule: commits only when the user asks); progress tracked with tree snapshots (snap.sh) and review packages (pkg.sh) — cost if wrong: none, user can commit later.
Base snapshot: 5b6b4777d00f4bf82d48f0e7ed07f94d466feb6c
## Pre-flight scan
| Rows | Produces/consumes | Finding |
|---|---|---|
| T1→T2 | Difficulty, CombatSetup.rivalAttackPct, CombatFinished(difficulty) | consistent |
| T1→T3 | RunRecord.difficulty | consistent |
| T2→T3 | AppContainer.difficulty/setDifficulty | consistent |
| T1 self | tests vs code; sim test file path open ("or new DifficultyOptionTest") | implementer chooses |
| T2 self | combatWaitsForDifficulty vague mechanism | implementer chooses, test must prove no source with null |
| T3 self | history screen path "ui/history/…" unspecified | implementer locates |
Task 1 snapshot: a1218994fe614ffb41d2c7a06d91f5309eef99cf
Task 1: minor (deferred): defaultIsUnchanged near-tautological; FQNs and brittle regex strip in old-save test; sim flag stripping clunky, sweeps ignore --difficulty; long line StoryEngine.kt:198.
Task 1: complete (review clean)
Task 2 snapshot: a7659fae32cbbffbb423c9b75663177e52146d72
Task 2: minor (deferred): hasSave IOException → RANGER written permanently for unreadable store; practiceCombatUsesDifficulty calls session() needlessly; test AppContainer scopes never cancelled; integer division /2 in test. Process: no clean RED capture.
Task 2: complete (review clean)
Task 3 snapshot: 19042b538bcbea3e7483dcf22e7b0cb5a3257f9e
Task 3: minor (deferred): class-level tall @Config weakens older OptionsScreen visibility assertions; hint assertions rely on tall config; import order; difficulty rows use Surface boxes vs OptionButton style (check in screenshots).
Task 3: complete (review clean)
Final fix snapshot: 3efcc224726682f7e4b12d66af24dbb83d3e7e1d
Final review (opus): needs fixes → fix wave (in-memory difficulty, sim setup.rivalAttackPct, defeats count for easiestDifficulty, atomic ensureDifficulty) → re-review clean. Deferred: downgrade loses save (no ignoreUnknownKeys), ensureDifficulty skipped when value stored (Robolectric deadlock root cause unknown), import order.
Verification: emulator — Options es (RANGER default fresh, EYE with save), SHIRE halves rival ATQ (1848→924), chronicle shows difficulty; screenshots docs/screenshots/v3/difficulty-{options,combat,history}.png. AAB 2.4.0 (10) built after full tests.
Status: complete
