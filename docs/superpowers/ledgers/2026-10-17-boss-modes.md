# SDD ledger — plan: docs/superpowers/plans/2026-10-17-boss-modes.md
User away (autonomy): apply recommended defaults. Pre-flight: T1→T2→T3 sequential; scan clean.
Task 1: Ruling: attack restarts at index 0 after defense; defense loops while HP < 70 % — natural reading, capped by maxDefenses — cost if wrong: balance tweak.
Task 1: minor (deferred → T2/T3): dazed boss in defense queues/rejects every tick (event noise; consider nextCast null while dazed in DEFENSE); ContentRules must require defense to end with a damage combo (T2); MAESTRO must read bossMode == DEFENSE (T3).
Task 1: complete (snap t1base..t1done, review clean)
Task 2: minor (deferred): DAMAGE_FAMILIES alias in test; DAMAGE set duplicated in script; isShieldOrHeal set per call; nimrodel attack has 31,31.
Task 2: complete (snap t2base..t2done, review clean)
Task 3: Ruling: Bree (tutorial) must never stall a NOVATO — change Bree modes so its defense can't loop (e.g. attackAbovePct just above defenseBelowPct, or defense pattern ends and returns to attack) and require 0 NOVATO fights at the tick cap in bree; spec values for Bree may change — cost if wrong: Bree slightly easier.
Task 3: Ruling: engine rule — on the last allowed defense (defenses == maxDefenses) the defense runs ONE full cycle and returns to ATTACK (general anti-loop, fixes Bree NOVATO stall); Bree spec values otherwise unchanged — cost if wrong: last defenses slightly shorter for all bosses; re-sim to confirm targets.
Task 3: fix round 1/5 (3 addressed, 0 open — last-defense rule, re-sim 0 cap all skills, hardcoded test table; snap t3done..t3fix1)
Task 3: minor (deferred): duplicated tuning table in python script; quiet-while-dazed only in DEFENSE; EXPERTO beats MAESTRO on some bosses; KDoc placement in ContentRulesTest.
Task 3: complete (snap t3base..t3fix1, review clean after round 1)
Final review: ready; controller fixed help text, CLAUDE.md stale line, spec note. PLAN COMPLETE.
