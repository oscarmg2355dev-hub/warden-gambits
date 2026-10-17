# Rivales que se curan y MAESTRO — plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:** que los jefes se escuden y lancen la cadena completa de curas al bajar del 50 % y del 25 % de vida, que un humanoide por encuentro lance la Cura IV al bajar del 40 %, añadir el jugador simulado MAESTRO y reajustar el balance con él.

**Architecture:**
- **Motor:** la reacción es un dato del rival (`reaction`) que `PatternBrain` ejecuta de forma determinista, con su estado en `Fighter`.
- **Contenido:** se migra con un script.
- **Simulador:** `:sim` gana una política nueva.
- **Balance:** el ajuste se hace solo con datos.

**Tech Stack:** Kotlin, JUnit 5, kotlinx-serialization, `:sim`.

**Spec:** `docs/superpowers/specs/2026-10-16-rival-healing-design.md`

## Global Constraints

- **Sin git:** se omiten los pasos de commit.
- **Entorno:** `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`.
- **Idiomas:** código en inglés; docs en español.
- **Finales de línea:** se conservan.
- **JSON:** UTF-8 sin BOM.
- **`:engine`:** puro y determinista, `when` exhaustivo y sin warnings nuevos.
- **Reacción de jefe:** `atHpPct [50, 25]`, escudo `2132` (acorazados o grandes) o `2131` (resto), y `combos ["21","212","2121","21212"]`. El jefe de Bree: `[40]`, sin escudo, `["21","212"]`.
- **Reacción de común:** solo el primer humanoide de cada encuentro con humanoides, con `atHpPct [40]` y `combos ["21212"]`.
  - Humanoides: retratos `bandit`, `dunlending`, `goblin`, `orc`, `orc_archer`, `uruk`, `mordor_orc`, `haradrim`.
  - Nunca: bestias, espectros, fuegos fatuos, arañas ni olog-hai.
  - Se quitan los `21` de los patrones de los bandidos de Bree y Chet.
- **Durante la reacción:** el rival lanza cada tick; DAZED cancela lo que queda; cada umbral se usa una vez; un salto de dos umbrales dispara solo uno.
- **Objetivos:**
  - MEDIO: comunes ≥ 88 %, jefes 55–85 % (Gorgoroth 45–65 %), aventura sin muertes 1,1 % ± 1;
  - MAESTRO: comunes ≥ 97 %, jefes 75–90 %, ≥ 1 muerte de media por aventura;
  - 0 al tope de ticks y 0 abandonadas.

## Review Focus

1. **Aturdido justo en el tick del umbral:** la reacción no empieza mientras está aturdido. Al despertar, si sigue por debajo del umbral, ese umbral ya contó (se marcó al dispararse, o se marca al despertar), sin bucles. Test en la Tarea 1.
2. **Rival que muere en el mismo tick del umbral:** no lanza nada después. Test en la Tarea 1.
3. **Reacción con el rival sin power y sin poción posible:** no se bloquea; la poción cubre siempre el coste. Test en la Tarea 1.
4. **Encuentro con un solo humanoide entre bestias:** ese humanoide lleva la reacción. Test en la Tarea 2.
5. **Guardado en mitad de un combate:** los combates no se guardan (se reinician al volver al nodo), así que no hay que migrar nada. Confirmar.

---

### Task 1: Reacciones en el motor

**Files:**
- Modify:
  - `engine/.../story/content/StoryContent.kt` (`RivalContent.reaction: ReactionContent? = null`)
  - `engine/.../model/CombatState.kt` (`RivalSpec.reaction`)
  - `engine/.../model/Fighter.kt` (estado de la reacción)
  - `engine/.../ai/RivalBrain.kt` (`PatternBrain`)
  - `engine/.../rules/TickResolver.kt` (si el lanzamiento cada tick o la cancelación por DAZED lo requieren)
  - `StoryEngine.kt` (pasa `reaction` a `RivalSpec`)
- Create: `engine/src/test/.../ai/ReactionTest.kt`

**Interfaces:**
- Produces:
  - `@Serializable data class ReactionContent(val atHpPct: List<Int>, val shield: String? = null, val combos: List<String>)`;
  - `data class Reaction(val atHpPct: List<Int>, val shield: String?, val combos: List<String>)` en el modelo;
  - `PatternBrain(pattern, everyNTicks, reaction: Reaction? = null)`;
  - el estado en `Fighter`: `reactionUsed: Int` (bits por umbral) y `reactionStep: Int` (−1 si no hay reacción en curso).

- [ ] **Step 1: Write the failing tests** (en `ReactionTest`):
  - `bossShieldsThenFullHealChain`: un jefe con la reacción de la spec, la vida forzada al 49 % y su turno. Lanza `2131`, `21`, `212`, `2121` y `21212` en 5 ticks seguidos. Los cuatro de cura después del primero van encadenados (`chained = true`). Después retoma el patrón donde lo dejó.
  - `secondThresholdOnce`: al bajar del 25 % lo repite una vez; una tercera bajada no dispara nada.
  - `dazeCancelsReaction`: con DAZED aplicado tras el escudo, no lanza las curas restantes y, al despertar, vuelve al patrón.
  - `doubleThresholdJumpFiresOnce`: de 60 % a 20 % en un tick, solo hay una reacción y ambos umbrales quedan usados.
  - `potionCoversReaction`: con power 0, bebe poción (`PotionUsed`) y lanza el combo de la reacción.
  - `diesOnThresholdTick`: si muere en el tick del umbral, no hay ningún `ComboCast` posterior.
  - `noReactionNoChange`: un rival sin `reaction` se comporta igual que antes; las huellas de `HpRegressionTest` no cambian con el contenido actual.
  - `sameSeedSameEvents`.
- [ ] **Step 2: Run** `./gradlew :engine:test --tests "*ReactionTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest`. Expected: PASS.

### Task 2: Reacciones en el contenido y reglas

**Files:**
- Create: `tools/content/add_reactions.py` (migración única, idempotente; imprime el recuento por zona)
- Modify:
  - los 24 JSON de `engine/src/main/resources/story/`
  - `engine/src/test/.../story/ContentRules.kt`, `ContentRulesTest.kt`, `RealContentTest.kt`
  - `docs/superpowers/specs/2026-10-08-story-content-design.md` §3 y §5

- [ ] **Step 1: Write the failing tests**:
  - `everyBossHasReaction`: los 21 jefes tienen la reacción de la spec (Bree, la suya);
  - `oneHealerPerHumanoidEncounter`: en cada encuentro con humanoides, exactamente el primer humanoide tiene la reacción `[40] / ["21212"]`; ninguno más;
  - `beastsNeverReact`;
  - `banditsLostTheirCureOne`: ningún patrón contiene `21`, salvo los de las reacciones;
  - `reactionCombosFormChain`.
- [ ] **Step 2: Run** `./gradlew :engine:test --tests "*Content*"`. Expected: FAIL.
- [ ] **Step 3:** ejecuta el script. Revisa que el patrón de los bandidos, tras quitar el `21`, siga cumpliendo daño ≥ 50 % y su cadena. Si un patrón queda inválido, sustituye el `21` por su siguiente combo de daño natural (`12` o `13`), según los criterios de la spec de contenido. Actualiza `ContentRules` y la spec §3 y §5.
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test`. Expected: PASS. Regraba `HpRegressionTest` desde el motor si cambia.

### Task 3: Jugador simulado MAESTRO (`:sim`)

**Files:**
- Modify:
  - `sim/src/main/kotlin/com/wardengambits/sim/HumanPlayer.kt` (`Skill.MAESTRO` y su política)
  - `Main.kt`, `Sweep.kt` (informe, CSV y barridos con MAESTRO)
  - los tests de `:sim`

- [ ] **Step 1: Write the failing tests**:
  - `maestroDazesBossWhenReactionStarts`: traza de combate contra un jefe con reacción; en el tick siguiente al escudo del jefe, el MAESTRO lanza `31` (o lo encadena) contra él;
  - `maestroChainsCureToTierFour`: por debajo del 50 %, lanza `21`, `212`, `2121` y `21212`;
  - `maestroShieldsAgainstBoss`: lanza `2132` (o `2131` si el jefe aturde) al empezar el combate contra un jefe;
  - `maestroFocusesHealer`: en un grupo, el objetivo es el rival con la reacción sin gastar;
  - `reportIncludesMaestro`.
- [ ] **Step 2: Run** `./gradlew :sim:test`. Expected: FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** `./gradlew :sim:test`. Expected: PASS.

### Task 4: Re-balance con MAESTRO y MEDIO

**Files:**
- Modify:
  - offsets y reacciones por jefe en el JSON, `ContentRules` y la spec de contenido §3
  - `engine/.../model/Stats.kt` solo si hace falta
  - `ComboTuning` solo como último recurso (spec §5)
  - `docs/balance/tuning.md` (sección «Rivales que se curan y MAESTRO»)
  - `docs/balance/final-*.csv`

- [ ] **Step 1:** `./gradlew :sim:run`. Guarda las métricas MEDIO y MAESTRO antes y después de las tareas 1–3: el «antes» con el contenido sin reacciones, `--no-reactions` si añades esa opción, o la tabla de la sección anterior de `tuning.md`.
- [ ] **Step 2:** ajusta siguiendo el orden de palancas de la spec §5 hasta cumplir los objetivos de Global Constraints.
- [ ] **Step 3:** documenta la tabla antes/después por zona (MEDIO y MAESTRO), la duración media de los combates contra jefes antes y después («más guerra») y los pendientes.
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest`. Expected: PASS.

### Task 5: Verificación, captura e instalación

- [ ] **Step 1: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 2:** en el emulador, con una partida nueva, juega el jefe de Bree hasta que se cure y saca una captura (`docs/screenshots/v3/boss-heal.png`).
- [ ] **Step 3:** instala en el móvil (`com.oscarmg.warden`, `adb install -r`, que conserva los datos), sin abrir combates.
- [ ] **Step 4:** actualiza `CLAUDE.md` (Estado) con las reacciones, el MAESTRO y el balance.
