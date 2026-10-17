# SDD ledger — plan: docs/superpowers/plans/2026-10-16-rival-healing.md
Pre-flight: T1 engine → T2 content (consumes ReactionContent) → T3 sim (consumes reaction state) → T4 balance → T5 verify. Sequential, no shared-file conflicts. Scan clean.
Task 1: minor (deferred → carry to T3): RivalSpec.reaction duplicates the brain's reaction (derive or require consistency); diesOnThresholdTick weaker than name; long KDoc line; import order.
Task 1: complete (snap t1base..t1done, review clean)
Task 2: Ruling: boss escorts never react (only the boss heals in boss fights) — matches the user's "en un grupo, solo uno se cura" — cost if wrong: add a healer escort later.
Task 2: minor (deferred): beastsNeverReact redundant; BOSS_SHIELDS duplicated in migration script; spec bandit count wording.
Task 2: complete (snap t2base..t2done, review clean)
Ruling (for Task 4): all boss reaction shields become 2132 (Escudo maestro, damage reduction) instead of 2131 — 2131 makes the boss Daze-immune, which removes the designed counterplay (Daze cuts the heal chain) — cost if wrong: bosses a bit less sturdy during reaction; update ContentRules BOSS_SHIELDS, spec, migration script note.
Task 3: fix round 1/5 (3 addressed, 0 open — no pattern peeking; heal before reaction-Daze; focus expr; snap t3done..t3fix1). MAESTRO smoke: 15.1 % no-death, 7.34 deaths/adv, chet 62.7 %.
Task 3: Ruling: MAESTRO may use moveset knowledge (does the boss have 31, reaction thresholds) — a human learns these — but never the next combo — cost if wrong: MAESTRO slightly stronger than a human.
Task 3: minor (deferred): lastShieldTick dead state for MAESTRO; shields re-cast on expiry (more than spec's "tier≥2 queued").
Task 3: complete (snap t3base..t3fix1, review clean after round 1)
Task 4: Ruling (targets conflict, NEEDS_CONTEXT answered): (1) the user's own complaint ("con mis curas y escudos tengo demasiada ventaja") + data (MAESTRO immune to boss Daze via permanent Escudo táctico) → the player's Escudo táctico (2131) is the main lever: allowed to change its cost, duration and/or turn full Daze immunity into a strong reduction; cureBasePct may also drop. (2) Targets rewritten: MAESTRO commons ≥ 97 %, bosses 75–92 % each, 1–4 deaths/adventure; MEDIO commons ≥ 85 %, bosses ≥ 40 % each (Gorgoroth ≥ 35 %), ≤ 20 deaths/adventure, 0 abandoned; 0 at tick cap. (3) Allowed: boss patterns changes (keep rules), offsets up to +15 (update rule/tests/spec), reactions without shield or with one threshold. Bree stays tutorial. — cost if wrong: player shields feel weaker; user can ask to revert; surface to user.
Task 4: Ruling: tacticsDazePct 30 → 50 to match descripcion.md («Escudo táctico reduce 50% de que te pongas en Daze»; philosophy wins per CLAUDE.md) — makes player shield weaker (user wanted less advantage) — cost if wrong: re-tune if MAESTRO falls below 75 %.
Task 4: fix round 1/5 (5 addressed, 0 open — Escudo táctico −50 % per philosophy + re-tune; docs; MAESTRO daze under 2131; expiry test; snap t4done..t4fix1)
Task 4: minor (deferred): MEDIO/EXPERTO skip opening Daze vs bosses with 2131; EXPERTO > MAESTRO on bosses (89 vs 85) because 2132 now protects more; most bosses at +2..+3; MEDIO 0,1 % no-death.
Task 4: complete (snap t4base..t4fix1, review clean after round 1)
Task 5: complete (controller: CLAUDE.md line, full build green, installed on phone with -r; boss-heal emulator screenshot skipped — heals visible as floating numbers)
Final review: ready; controller added help_intro sentence about rival heals + Daze counter. PLAN COMPLETE.
