# SDD ledger — plan: docs/superpowers/plans/2026-10-15-combat-polish.md
No git: snapshots + diff.sh (see ../combat-cards/snap.sh pattern).
## Pre-flight scan
| pair/task | finding |
|---|---|
| T1↔T4 | both touch CombatCards.kt (T1 stage layout, T4 passes running to EffectIcons) — sequential, ok |
| T1↔T3 | both touch CombatScreen.kt (clock passing vs ButtonRow swap) — sequential, ok |
| T2 self | StatBar deletion + engine/sim message fixes — ok |
| T3 self | testTags must stay btn_<input.name.lowercase()> — ok |
| T4 self | EffectIcons new `running` param defaulted so help screen compiles — ok |
| T5 | screenshots only on emulator; phone install only — ok |
Scan clean.
Task 1: Ruling: keep spec fallback (stage < 40 dp → lower half of card) — on the user's Moto G85 rival cards are ~250 dp tall so the stage is ample; only small 640 dp phones hit the fallback — cost if wrong: numbers overlap icons on small phones.
Task 1: ⚠ FloatingNumbersScreenTest/CombatLayoutTest untouched — resolved: implementer's full app suite (225) green.
Task 1: minor (deferred): tracer-based recomposition test fragile to Compose upgrades; fullGridFallsBackToLeastUsed tail assert vacuous; small-card stage falls back (ruled); outline styles rebuilt per recomposition.
Task 1: complete (snap t1base..t1done, review clean)
Task 2: fix round 1/5 (3 addressed, 0 open — line endings; icons vanish at 640dp; memoize fitFontSize; snap t2done..t2fix1)
Task 2: minor (deferred): largeFontDoesNotClip near-vacuous; numbersStayInsideStage test card 200→220 dp; 4-rival badge test uses assertIsDisplayed (partial clip passes).
Task 2: complete (snap t2base..t2fix1, review clean after round 1)
Task 3: Ruling: testTags stay btn_spear|btn_shield|btn_fist|btn_execute (what the code generates today); plan's btn_one/two/three was a plan typo — cost if wrong: none.
Task 3: minor (deferred): rapid-tap release race (face springs back while held); KeyButton in ComboKeys.kt unused; no test for running=false freeze of sparks/halo; extra ×0.8 dim when disabled; repeated trail expr; halo overlaps neighbour key edge.
Task 3: complete (snap t3base..t3done, review clean)
Task 4: minor (deferred): pop frozen mid-way if paused within 300 ms; medallion clock runs at 60 fps while any badge exists; occurrence-key spurious beat; debuff rim loses tier cue (red #F08A7A→#7A1E16 chosen by implementer).
Task 4: complete (snap t4base..t4done, review clean)
Ruling: user asked mid-run for key sounds (1 espada, 2 escudo, 3 grito) — added as Task 6 (bounded: generated SFX + play on key press, respecting sound toggle); Task 5 verification runs after it, on the EMULATOR (user unplugged the phone).
Task 6: ⚠ ActionButtons onPress gating when disabled — resolved: Task 3 test disabledKeysDoNothing covers it.
Task 6: minor (deferred): disabledKeysPlayNothing uses pause (overlay may swallow clicks); long KDoc line; sounds not listened to by a human yet.
Task 6: complete (snap t6base..t6done, review clean)
Task 5 (verification, controller): emulator screenshots docs/screenshots/v3/combat-polish.png + emulator-shots.png. Observed: (a) effect badges sit vertically centred in the Effects area, leaving a gap under the bars and shrinking the number stage to a thin strip at the card bottom; (b) two numbers overlap on the rival card ("+147" / "−147"); (c) "+0 CU1" shown when a heal does nothing; (d) buttons 3D/wave/sparks look right; medallions render.
Final review: 3 Important + 2 fix-now minors → one fix wave, re-review all ADDRESSED (snap fixbase..fixdone). Side-changes accepted: clipToBounds removed from FloatingNumbersLayer; noBadgesStageStartsBelowBars on 220 dp card.
PLAN COMPLETE.
