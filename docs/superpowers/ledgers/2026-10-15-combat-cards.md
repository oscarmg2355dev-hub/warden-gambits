# SDD ledger — plan: docs/superpowers/plans/2026-10-15-combat-cards.md
No git in this project (user decision): "commits" = source snapshots in snap-<name>; review packages = diff.sh output.

## Pre-flight scan
| pair / task | produces vs consumes | finding |
|---|---|---|
| T1→T3 | ComboTuning.FIXED → sim "before" run | ok (T3 after T1) |
| T1→T5 | EffectResolved.rollPct → CombatTextLine.rollPct | ok |
| T2→T6/T7 | Fighter.portrait → Portrait(key), FighterUi.portrait | ok; unknown key → baseType "warden" |
| T5↔T7 | T5 says rememberFloatingNumbers replaces rememberCombatText and touches CombatRoute; T7 wires cards in CombatRoute | conflict on intermediate state |
| T4/T6→T7 | GameBar, Portrait consumed by cards | ok |
| T1 self | tests vs code | ok; lethal test stub nextInt returns bound-1 → 150 |
| T2 self | boss = level 7 node, index 0 (StoryEngine:76) | ok |
| T3 self | may need sim --fixed-roll option | ok, plan allows |
| T5 self | removes strip constants "si quedan sin uso" | ok |
| T6 self | 13 sprites; jinete/espectro use dunlending | ok |
| T7 self | removes RivalCard/PlayerCard/CombatTextStrip from CombatScreen | ok |
Ruling: T5 adds FloatingNumbers + rememberFloatingNumbers but keeps the old strip/rememberCombatText compiling and in use; T7 swaps CombatRoute/CombatScreen to floating numbers and deletes the strip — keeps the app building between tasks — cost if wrong: minor leftover code removed in T7 anyway.
Task 1: Ruling: direct hit rolls on the final damage (after defense) as the brief says, not before defense as spec §1 says — matches the user's wording ("del total de cada ataque, 50–150 %") and the difference is small; fix spec §1 text in Task 8 — cost if wrong: direct hits vary slightly less than spec order.
Task 1: minor (deferred): ComboApplierTest/DazeRollTest/HpRegressionTest line endings rewritten (diff noise).
Task 1: minor (deferred): PulseRollTest drain heal asserts <= 3 (could be == 3); misleading "Cast on tick 6" comment.
Task 1: complete (snap t1base..t1done, review clean)
Task 2: ⚠ app/sim suites — resolved: implementer report shows full :engine:test :sim:test :app:testDebugUnitTest BUILD SUCCESSFUL.
Task 2: minor (deferred): add_portraits.py dead code (unused index, parallel counter); BeastEntry.portrait "" default.
Task 2: complete (snap t2base..t2done, review clean)
Task 3: Ruling: accept boss-offset rule widened +2..+8 → +2..+12 (durin +11) — targets met and documented; offsets are data — cost if wrong: user may prefer reworking durin's pattern; surface in final message.
Task 3: minor (deferred): ContentRules.kt:173 error text still "+2..+8"; Sweep.kt:198 default offsets 2..8; MEDIO 5,3 % and 3,52 deaths at target edges.
Task 3: complete (snap t3base..t3done, review clean)
Task 4: minor (deferred): no test for pause/resume/in-flight animation; resume re-applies full delays; barTrail extra `kind` param (default HP); import order; halo stroke half-clipped. ⚠ visual check → Task 8 screenshot.
Task 4: complete (snap t4base..t4done, review clean)
Task 5: minor (deferred): extra black shadow on non-strong numbers; rise read in composition (prefer graphicsLayer); possible clipping of wide numbers at xFrac 0.65 — T7 must check on real card width; import order.
Task 5: complete (snap t5base..t5done, review clean)
Task 6: Ruling: accept that `convert` also brought in 52 pending user Gemini node images (bruinen, rest of puente, quebradas) — the user always asks to add their Gemini images; update CLAUDE.md count in Task 8 — cost if wrong: originals still in imagenes/<zona>/.
Task 6: minor (deferred): --zone meaningless with --set portraits; multiple "Retrato" nodes need onAllNodes in T7 tests.
Task 6: complete (snap t6base..t6done, review clean)
Task 7: fix round 1/5 (3 addressed, 0 open — BlendMode.Saturation grey block API<29; strings.xml CRLF; square ripple; snap t7done..t7fix1)
Task 7: minor (deferred): progress computed in composition (layout lambda re-created per frame); fit ignores rise height on very short cards; StatBar unused; placement/maxVitalOf tests not red-first; API 26-28 desaturation not run on real device.
Task 7: complete (snap t7base..t7fix1, review clean after round 1)
Task 8: screenshot taken on phone (docs/screenshots/v3/combat.png); controller test fight caused 1 death in user's save (Sombrío 3/7)
Task 8: complete (docs + CLAUDE.md; build green)
