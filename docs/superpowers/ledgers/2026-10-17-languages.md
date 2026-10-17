# SDD ledger — plan: docs/superpowers/plans/2026-10-17-languages-tengwar.md
Pre-flight: T1 spike gate → T2 transcriber → T3 infra (consumes Tengwar.transcribe) → T4 en strings + engine texts → T5 story en (loader + 3 translation batches; batches touch disjoint files, may run in parallel) → T6 apply+verify. Scan clean.
Task 1: GO (Alcarin Tengwar 0.83, GPOS mark-to-base ok; screenshot tengwar-spike.png verified by controller). Complete.
Plan amended per user mid-run: T3 now an Options screen (sound + language + script + tengwar equivalence table); new T7 «Acerca de» (author Oscar Martínez Gómez, programmer, Tolkien fan, retired LOTRO player).
Task 2: minor (deferred): formatting typo `cache =object`; tier regex [1-4]; triples/apostrophes.
Task 2: complete (snap t2base..t2done, review clean)
Ruling: T3 (app) and T5 loader + translation batches (engine resources) run in parallel — disjoint files; T5's app wiring (reload content on language change) deferred to T6 — cost if wrong: a merge fix in T6.
Task 5a: reviewed — Approved (loader + 6 zones). Deferred to post-5b/5c polish pass: quebradas_7_0 121 words; "Black Riders"/"Men" capitalisation; awkward phrasings (bree_1_0, bree_3_3, bree_3_1, bree_4_4, moscagua_4_1, moscagua_4_6, cima_7_0, bree_4_0, cima_2_1 title, chet_1_0 title); harden TranslationCoverageTest (text != Spanish for all nodes; run distinct-label validation on English content; compare against Route.zones once all 24 exist); app must take zone names from ZoneContent.name (T6).
Task 3: Ruling: the Language and Script toggle buttons (and the menu entry «Opciones/Options») always render in Latin script, so a player can always switch back from tengwar — cost if wrong: tiny inconsistency.
Task 3: fix round 1/5 (4 addressed — Latin toggles/Opciones; tick out of transcription; vowel-cell test; 48 dp). Task 3: complete (review clean after round 1). Deferred minor: startup flash of device language; contentDescriptions not transcribed; device visual check pending (T6).
Task 5c: fix round 1 (2 Important + 13 minor applied; verified by controller grep). Batch 5c approved.
Task 5b: reviewed — Approved; polish fixes sent to translator. Note for user: Spanish source moria_5_0/7_0 puts the Warden one step AHEAD of the Fellowship in Moria (tomb before them; Balrog turns to the white light behind) — content canon inconsistency to surface.
Task 4: fix round 1/5 (4 addressed — off-main story load, combat setup from Spanish engine, plurals, imports). Task 4: complete. Deferred minor → T7: guard translation load failure (runCatching; fall back to Spanish, don't cache failed Deferred).
Task 5: complete (loader + 24 overlays + hardened coverage; 3 batch reviews approved; fixes applied).
Task 7: complete (About + load-failure guard; tests green). Task 6: complete (menu scroll, 4-combination matrix tests, CLAUDE.md; controller screenshots docs/screenshots/v3/lang-*.png, options-tengwar.png).
Final review: ready; Important 1 (device check of long tengwar text) closed by controller: node + map screenshot docs/screenshots/v3/node-tengwar.png shows no clipping. Minor fix wave dispatched.
Final fix wave applied (LaunchedEffect, removed tr helpers, apostrophes, typo, qu/gu note). PLAN COMPLETE.
