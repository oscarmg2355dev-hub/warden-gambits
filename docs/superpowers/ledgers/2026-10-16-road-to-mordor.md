# SDD ledger — plan: docs/superpowers/plans/2026-10-16-road-to-mordor.md
No git: snapshots + diff.sh.
## Pre-flight scan
| pair/task | finding |
|---|---|
| T1→T2-4 | bestiary + PendingContentTest + story-pending/ consumed by content tasks — ok |
| T2-4→T5 | pending JSON moved into story/ in T5 — ok |
| T5→T6 | lothlorien_1_0 image rename in T5, image test in T6 — ok |
| T5→T7 | map needs Route 24 zones (T5) — ok, T7 after T5 |
| T5→T8 | sim on 24-zone route — ok |
| T6 self | 40 portrait prompt rows = 26 old + 6 + 8 — ok |
| T2 self | lothlorien_1_0 reuses old final text adapted to 60–120 words — ok |
Scan clean.
Task 1: ⚠ StoryEngine untouched — resolved: grep shows no "rivendel" literal outside Route in engine/src/main.
Task 1: minor (deferred): isDamage/hasDamageChain duplicated in ContentRulesTest; 12-portrait literal list in test; two patterns at exactly 50 % damage.
Task 1: complete (snap t1base..t1done, review clean)
Note: parallel user work detected (another session): Play Store prep — applicationId com.oscarmg.warden, versionCode 5, new launcher icons, CLAUDE.md Play paragraph. Not ours; leave untouched. adb launch now uses package com.oscarmg.warden.
Task 2: fix round 1/5 (8 addressed, 0 open — three-boats grounding, canon distance, template repeats, boat continuity, details; snap t2done..t2fix1)
Task 2: minor (deferred): anduin_4_2/5_1 similar night-drift beat; argonath_6_1 rope tied to a root on gravel; lothlorien_3_0 "agradecen tus noticias" ungrounded.
Task 2: complete (snap t2base..t2fix1, review clean after round 1)
Task 3: fix round 1/5 (8 addressed, 0 open — marsh template repeats, rope recovery, wording/labels, uruk-hai plural, winged shadow; snap t3done..t3fix1)
Task 3: minor (deferred): cienagas_5_0 label definite article; emynmuil_5_0 abrupt dawn after 4_7. Handoff cienagas_7_0 (ash desert, black mountains) → ithilien_1_0 needs a bridge in Task 4.
Task 3: complete (snap t3base..t3fix1, review clean after round 1)
Task 4: fix round 1/5 (7 addressed; 1 new Important: gorgoroth_6_0 vs 5_0 contradiction)
Task 4: fix round 2/5 (3 addressed, 0 open — 6_0 valid from both parents, time scale, epilogue path-neutral; snap t4fix1..t4fix2)
Task 4: minor (deferred): slight tension 5_0 "llanura vacía" vs 6_0 torches; epilogue 158/160 words (thin margin); ithilien archers-from-above twice.
Task 4: complete (snap t4base..t4fix2, review clean after round 2)
Task 5: minor (deferred): ZoneColors length coupled to route (use modulo or a size test); redundant CharacterScreenTest; EndingScreenTest fixture title; CLAUDE.md Estado (→ Task 9).
Task 5: complete (snap t5base..t5done, review clean)
Task 6: minor (deferred): ZoneColorsTest checks size only; boss_gorgoroth borrows marsh_wraith sprite until Gemini image.
Task 6: complete (snap t6base..t6done, review clean)
Task 7: minor (deferred): landmarks (Argonath statues, Amon Hen seat) dropped silently on maps < ~66 rows; orphaned KDoc near `elevation`; cone has lava flows but no cracks (spec ok).
Task 7: complete (snap t7base..t7done, review clean)
Task 8: Ruling: accept MEDIO no-death 1,1 % (target 2–5 % unreachable under the spec's own constraints: old zones cap at 5,3 %, 8 new bosses ≤85 % + Nazgûl ≤65 %) — user asked for more challenge — cost if wrong: retune new bosses to ~90 % MEDIO; surface to user.
Task 8: minor (deferred): "Huargo" (JSON) vs "Huargo del Anduin" (bestiary) name mismatch; tuning.md EXPERTO boss mean includes Gorgoroth while MEDIO excludes; Libro I table 13 vs "14" note; Libro II commons softer than Libro I (3-tick rhythm); Ella-Laraña at +12.
Task 8: complete (snap t8base..t8done, review clean)
Task 9: complete (docs by subagent; controller emulator screenshot docs/screenshots/v3/map-mordor.png; generation 24 zones 4153 ms emulator debug; observed map title still 'El camino a Lothlórien')
Final review: ready after small fixes; one fix wave (map title, content spec, KDoc, CLAUDE.md timing, tuning label) verified by controller; strings.xml CRLF restored by controller. PLAN COMPLETE.
