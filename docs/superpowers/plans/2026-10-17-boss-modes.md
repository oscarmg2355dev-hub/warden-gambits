# Jefes con modo ataque y defensa — plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:** que los jefes alternen entre modo ataque (vida > 50 %) y modo defensa (< 50 %: escudo, cadena de curas y dos golpes), vuelvan a ataque al 70 % y entren en defensa como mucho 3 veces por combate; después, re-balance.

**Architecture:** `modes` es un dato nuevo del rival que `PatternBrain` ejecuta, con su estado en `Fighter`. Sustituye a la `reaction` de los jefes. El contenido se migra con un script y el balance se ajusta con `:sim`.

**Spec:** `docs/superpowers/specs/2026-10-17-boss-modes-design.md`

## Global Constraints

- **Sin git:** se omiten los pasos de commit.
- **Entorno:** `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`.
- **Idiomas:** código en inglés; docs en español.
- **Finales de línea:** se conservan.
- **JSON:** UTF-8 sin BOM.
- **`:engine`:** puro y determinista, `when` exhaustivo y sin warnings nuevos.
- **Modos (valores de la spec):**
  - `defenseBelowPct 50`, `attackAbovePct 70`, `maxDefenses 3`;
  - defensa = `2132, 21, 212, 2121, 21212` + los 2 primeros combos de daño del patrón de ataque;
  - el patrón de ataque es el `pattern` sin `2131`, `2132` ni `21…`;
  - en defensa lanza cada tick; DAZED salta a los combos de daño finales;
  - Bree: `40/60`, `maxDefenses 1`, defensa `21, 212` + un combo de daño.
- **Balance:**
  - MAESTRO: comunes ≥ 97 %, cada jefe 75–92 %, 1–4 muertes por aventura;
  - MEDIO: comunes ≥ 85 %, cada jefe ≥ 40 % (Gorgoroth ≥ 35 %), ≤ 20 muertes por aventura;
  - 0 abandonadas y 0 al tope; Bree exento;
  - no se tocan las herramientas del jugador.

## Review Focus

1. **Jefe que alterna en el límite** (se cura justo al 70 % y recibe daño): no hay bucle de modo dentro de un mismo tick; el cambio solo ocurre en `prepare`. Test en la Tarea 1.
2. **Jefe aturdido justo al entrar en defensa:** al despertar empieza por los combos de daño, sin curas, y la entrada cuenta para `maxDefenses`. Test en la Tarea 1.
3. **Combate muy largo:** con `maxDefenses` y el tope de ticks, ningún combate del simulador llega al tope. Se comprueba en la Tarea 3.

---

### Task 1: Modos en el motor

**Files:**
- Modify:
  - `engine/.../story/content/StoryContent.kt` (`RivalContent.modes: ModesContent? = null`)
  - `engine/.../model/Fighter.kt` (`bossMode`, `defenses`, `modeCasts`)
  - `engine/.../model/CombatState.kt` (`RivalSpec.modes`, derivado del `brain`)
  - `engine/.../ai/RivalBrain.kt` (`PatternBrain.modes`, `prepare`/`afterCast`)
  - `StoryEngine.kt`
- Create:
  - `engine/.../ai/BossModes.kt` (modelo `BossModes` con validación)
  - `engine/src/test/.../ai/BossModesTest.kt`

**Interfaces:**
- Produces:
  - `@Serializable data class ModesContent(val defenseBelowPct: Int, val attackAbovePct: Int, val maxDefenses: Int, val defense: List<String>)`;
  - `data class BossModes(...)`, con la misma forma y `init` que valida `0 < defenseBelowPct < attackAbovePct <= 100`, `maxDefenses >= 1`, `defense` no vacío y combos existentes;
  - `PatternBrain(pattern, everyNTicks, reaction = null, modes: BossModes? = null)`, que exige `reaction` y `modes` no simultáneos;
  - `enum class BossMode { ATTACK, DEFENSE }`.

- [ ] **Step 1: Write the failing tests** (`BossModesTest`), con `CombatEngine.reduce` real:
  - `attacksAboveHalf`;
  - `entersDefenseBelowHalfAndCastsEveryTick` (secuencia exacta, cada tick, con `chained` en 212, 2121 y 21212);
  - `returnsToAttackAt70`;
  - `atMostMaxDefenses`;
  - `dazedInDefenseSkipsRemainingHeals`;
  - `dazedOnEntryStartsWithDamage`;
  - `noFlipWithinOneTick`;
  - `potionCoversDefenseCasts`;
  - `rivalsWithoutModesUnchanged` (las huellas de `HpRegressionTest` no cambian);
  - `sameSeedSameEvents`.
- [ ] **Step 2: Run** `./gradlew :engine:test --tests "*BossModesTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest`. Expected: PASS.

### Task 2: Modos en el contenido y reglas

**Files:**
- Create: `tools/content/add_boss_modes.py` (idempotente)
- Modify:
  - el JSON de las 21 zonas con jefe
  - `ContentRules.kt`, `ContentRulesTest.kt`, `RealContentTest.kt`
  - la spec de contenido §3 y §5
  - la spec `2026-10-16-rival-healing-design.md` (nota: los jefes pasan a `modes`)

- [ ] **Step 1: Write the failing tests**:
  - `everyBossHasModes` y `noBossHasReaction`;
  - `attackPatternsHaveNoShieldsOrHeals` (y cumplen daño ≥ 50 %, cadena real y `31`);
  - `defenseFollowsRule` (escudo, curas y los 2 primeros daños del ataque; Bree, su versión);
  - `commonsUnchanged` (reacciones de curandero intactas).
- [ ] **Step 2: Run** `./gradlew :engine:test --tests "*Content*"`. Expected: FAIL.
- [ ] **Step 3:** ejecuta el script. Si al quitar escudos o curas un patrón de ataque incumple las reglas, sustituye cada combo quitado por el siguiente combo de daño natural de su cadena y documéntalo. Actualiza `ContentRules` y las specs.
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test`. Expected: PASS.

### Task 3: Re-balance y documentación

**Files:**
- Modify:
  - offsets y modos por jefe (JSON, `ContentRules`, spec de contenido §3)
  - `docs/balance/tuning.md` (sección «Jefes con modo ataque y defensa»)
  - `docs/balance/final-*.csv`
  - `CLAUDE.md` (Estado)
  - `:sim` si hace falta que el MAESTRO entienda el modo defensa: aturdir al jefe al entrar en defensa (antes miraba `reactionStep`)

- [ ] **Step 1:** `./gradlew :sim:run`. Mide el antes (contenido de la Tarea 2 con `modes` ignorados, o la tabla de la sección anterior de `tuning.md`) y el después.
- [ ] **Step 2:** ajusta con las palancas de la spec §3 hasta cumplir los objetivos.
- [ ] **Step 3:** documenta la tabla antes/después por zona (MEDIO y MAESTRO), la duración media de los combates contra jefes y las entradas medias en defensa.
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.
