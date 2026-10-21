# SDD ledger — plan: docs/superpowers/plans/2026-10-21-combat-practice.md

Spec: docs/superpowers/specs/2026-10-21-combat-practice-design.md
Ruling: no commits; diffs between working-tree snapshots — project rule; cost if wrong: no commit messages for reviewers.
Base snapshot: 6d571ea960fb4d89f0979219ccd2b88bf1c42d22

## Pre-flight scan
| Pair / task | Produces vs consumes | Finding |
|---|---|---|
| T1→T2 | practiceSetup(node) → PracticeCombatSource | consistent |
| T1→T3 | practiceEntries(profile) → PracticeViewModel | consistent |
| T2→T3 | PracticeCombatSource, CombatRoute → practice_combat route | consistent |
| T2↔T3 | both edit WardenNavHost and strings.xml | sequential; strings CRLF warned |
| T1 self | tests use real content; ExpectedLevels moved with its test | consistent |
| T2 self | CombatSource sealed with report only on StoryCombatSource | consistent |
| T3 self | ELITE ribbon may need internal visibility | allowed by plan |
No rulings needed.
Task 1 snapshot: 41dfee31551303ff8324bc622c8e8483d5867e5a
Task 1: minor (deferred): PracticeCatalog.entries builds a new ExpectedLevels per call (StoryEngine has a cached lazy one) — pass it in.
Task 1: minor (deferred): isBoss by level only (agrees via content rules); `.unlocked.not()` readability in test.
Task 1: complete (review clean)
Task 2 snapshot: a6cd7284656309e98aaeac35a8b6f67ae5b7849d
Task 2: review approved with Important: practice abandon dialog still says it counts as a defeat → fix round 1 (plus clear floating numbers/message on retry).
Task 2: minor (deferred): practiceFinish/AbandonDoesNotReport tests near-tautological (practice VM has no session); retry test doesn't check target reset; `?: 1` levelBefore fallback.
Task 2 fix1 snapshot: abaee2c8c339a42e0717bdb47fce746246dae5ba
Task 2: fix round 1/5 (2 addressed, 0 open)
Task 2: complete (review clean after fix round 1)
Task 3 snapshot: 51daa8b49cb5fd927f76f6e42ebef466882fbba1
Task 3: review approved (minors). Controller manual check: menu title «WARDEN GAMBITS» fits one line; practice combat at Nv 31 (expected) works; save file byte-identical after practice; Repetir resets to tick 0 full HP; result overlay OK (practice-combat.png). FOUND (Important): grid cell title and «Bloqueado» text render BLACK on the dark background (unreadable) — fix round 1.
Task 3: minor (deferred): tengwar test only checks display; no test for locked boss without ribbon; currentZoneIndex only tested at 0; zonesFor computed twice at open.
Task 3 fix1 snapshot: 9ee80ca72e12cdd3083c37daf7e1310eedf2a00b
Task 3: fix round 1/5 (2 addressed, 0 open) — re-review clean. Controller verified on emulator: grid text readable (phone), 7 columns at tablet density 216, practice-grid.png saved.
Task 3: minor (deferred): Kotlin assert() in colour test; import order in PracticeScreen.
Task 3: complete (review clean after fix round 1)
Final snapshot: b3486068ee9709bd871ffebe80a9c9fdd20e515a
Final fix snapshot: 4cae27b25634eeaeee0061bd963c754288005e8f
Final: fix wave addressed (rest-zone grid start, ExpectedLevelsTest in engine, practice source fallbacks, tengwar test, assertTrue). Parked minors: runCatching fallback silent; currentZoneIndex one-time val; long line. Full verification green incl. bundleRelease.
