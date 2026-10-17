# SDD ledger — plan: docs/superpowers/plans/2026-10-15-continuous-map.md
No git: snapshots + diff.sh (see ../combat-cards/snap.sh pattern).
Pre-flight: T1 (pure generator) → T2 (screen consumes WorldGen/WorldMap/WorldCache) → T3 verify. No shared-file conflicts. Scan clean.
Task 1: Ruling: seam test compares each border to a LOCAL baseline (mean column diff of columns 56k−9..56k+7 excluding the border pair, factor 1.6) instead of the spec's whole-map mean — the global mean measures texture density, not seams, and left a 0.6 % tripwire — cost if wrong: none (stricter at catching real seams); update spec §4 wording in Task 3.
Task 1: fix round 1/5 (3 addressed, 0 open — local-baseline seam test; doorLeft derived; ford comment; snap t1done..t1fix1)
Task 1: minor (deferred): DENSITY/BAYER mutable arrays in singletons; ArrayList<Float> boxing once per call; cold ART timing to be measured on device (Task 3).
Task 1: complete (snap t1base..t1fix1, review clean after round 1)
Task 2: fix round 1/5 (3 addressed, 0 open — vacuous pause test; 4 s generation → disk cache + parallel bands; ZoneRowUi.id; snap t2done..t2fix1). Emulator debug build: first launch 2613 ms, second launch from disk.
Task 2: minor (deferred): disk cache no checksum / no range-check on water indices; shared tmp name and prune may delete in-flight tmp; WorldGenTimingTest env-gated clutter; stale world shown while a new rows key generates.
Task 2: complete (snap t2base..t2fix1, review clean after round 1)
Task 3: complete (controller: emulator screenshots map.png/map-scrolled.png, CLAUDE.md line, spec §4 seam wording)
Final review: ready; 3 minors fixed in one wave, re-review all ADDRESSED. PLAN COMPLETE.
