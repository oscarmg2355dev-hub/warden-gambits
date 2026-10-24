# SDD ledger — plan: docs/superpowers/plans/2026-10-23-combat-tutorial.md

Spec: docs/superpowers/specs/2026-10-23-combat-tutorial-design.md
Ruling: no commits; diffs between working-tree snapshots (binaries excluded) — project rule; cost if wrong: no commit messages.
Base snapshot: 12e3d236cc46df327598652b1ab24e75e5e411f2

## Pre-flight scan
| Pair / task | Produces vs consumes | Finding |
|---|---|---|
| T1→T2 | CombatSetup.hpFloorPct → TutorialLessons.setup | consistent |
| T2→T3 | TutorialLesson/TutorialProgress.advance → TutorialCombatSource/VM | consistent |
| T3→T4 | tutorialDone/tutorialDismissed, Routes.TUTORIAL → map card/stop | consistent |
| T3↔T4 | both edit WardenNavHost and strings.xml | sequential; CRLF warned |
| T2 self | fullLessonPlayableWithRealEngine depends on power regen; may need waiting ticks | noted in brief |
| T3 self | many texts to write es/en | content work inside task |
Task 1 snapshot: 37b7e87e700ebf702d6f4542f0231d3b515fce17
Task 1: minor (deferred): over-time display amounts not capped when the floor clamps (COT may show inflated +N); floorHoldsUnderStackedDamage doesn't assert DoT+area+direct actually landed same tick; no drain floor test; hpFloor public on Fighter.
Task 1: complete (review clean)
Task 2 snapshot: 302f79eec85c3d6926d4673cf4103a34cc8d45d0
Task 2: minor (deferred): advance() no guard for step>=size / lessonId mismatch; full-lesson test loop readability (indexOf); lessonsMatchTheSpec doesn't assert dummy patterns; levelOffset in JSON unused; BOM literal invisible. Process: no clean RED capture (compile-only fail).
Task 2: complete (review clean)
Task 3 snapshot: 1e5247bb16b7e93cbfa31bc5a85e612142a46b33
Task 3 fix1 snapshot: 9bd962a5713642b86c89a3ca931127bee5fbf268
Task 3: fix round 1 (en combo names = combo_name_*, chain facts, wording) — re-review clean
Task 3: minor (deferred): repeating tier I restarts chain rather than "breaks"; no ✔ flash on last step; lessonCompletes polls Thread.sleep; names frozen at VM creation; panel height on small screens to check on emulator.
Task 3: complete
Task 4 snapshot: 34de4b52c7a2e6047d426b97e79897aec87905eb
Task 4: deviation accepted: card also requires new game (current zone Bree), per spec §2 «Partida nueva».
Task 4: minor (deferred): stop centre expression duplicated; recentres on stop every Bree visit while pending; card scrolls with world.
Task 4: complete (review clean)
Final review (opus): needs fixes → fix wave: ambience stops on lesson completion (CombatSound ended), tutorialDone nullable until loaded, unknown-sequence rejection = miss and 321 not a miss. Deferred: card returns on later laps while lessons pending; test gaps (persisted save, floor during whole lesson, back after «Siguiente lección»).
Controller: map card buttons stacked (Empezar entrenamiento broke mid-word on phone).
Verification: :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug green; emulator: card es/en, Saltar hides it, 5 lessons completed, list ✔, card hidden after completing, save holds only settings. Screenshots docs/screenshots/v3/tutorial-{map,lesson,list}.png.
Status: complete
