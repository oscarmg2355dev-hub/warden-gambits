# Tutorial de combate — plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** «Campo de entrenamiento» en el mapa, con 5 lecciones de combate guiadas que avanzan al lanzar los combos pedidos. En ellas nadie muere, no dan nada y no tocan la partida.

**Architecture:**
- **Motor:** las lecciones son datos (`tutorial/lessons.json`); el progreso es una función pura sobre los `CombatEvent`; el mínimo de vida es una opción del `CombatSetup`.
- **App:** una fuente de combate nueva, un panel de instrucciones en la pantalla de combate, una lista de lecciones y un marcador en el mapa.

**Tech Stack:** Kotlin (`:engine` puro, JUnit 5), Compose (Robolectric), DataStore.

**Spec:** `docs/superpowers/specs/2026-10-23-combat-tutorial-design.md`

## Global Constraints

- **`:engine` puro y determinista,** sin dependencias nuevas, sin warnings y con `when` exhaustivo. Los combates normales no cambian: `hpFloorPct` es `null` por defecto.
- **Lecciones, ids y pasos exactos:**

  | id | Lección | Pasos (en orden) | Rivales |
  |---|---|---|---|
  | `attack` | Ataque | `11`, `12`, `121`, `1212`, `12121` | 1 |
  | `defense` | Defensa | `2132`, `22`, `21`, `212`, `2121`, `21212` | 1 |
  | `daze` | Aturdimiento | `2131`, `33`, `31`, `313`, `3131`, `31313` | 1 |
  | `drain` | Robo de vida | `22`, `3123`, `31232` | 3 |
  | `finisher` | Remate | `11`, `123`, `13`, `131`, `1313`, `13131` | 1 |

- **Mínimo de vida del tutorial:** el 10 % del máximo, para todos los luchadores.
- **Niveles:** el Warden a nivel 1 y los muñecos «Muñeco de entrenamiento» / «Training dummy» (retrato `dummy`) a nivel 1.
- **Patrones de los muñecos:**
  - `attack` y `finisher`: `["12"]` cada 6 ticks;
  - `defense` y `drain`: `["12", "121"]` cada 3 ticks;
  - `daze`: `["31", "12"]` cada 3 ticks.
- **El tutorial no da nada ni toca la partida.**
- **Progreso en DataStore:**
  - `tutorial_done`, un conjunto de ids;
  - `tutorial_dismissed`, un booleano.
- **Textos:** es/en en `strings.xml` (paridad `StringResourcesParityTest`) y tengwar por `tr`/`trText`. `strings.xml` es CRLF: nada de `sed -i`.
- **Sin anuncios.** Sin commits.
- **Entorno:** `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`.

## Review Focus

1. **Un combo que se pone en cola pero no se resuelve** (rechazado por power o porque se rompe la cadena): no avanza el paso. Solo cuenta `ComboResolved` del jugador. → Task 2, `rejectedComboDoesNotAdvance`.
2. **Daño en un mismo tick que bajaría por debajo del mínimo** (DoT + área + directo): la vida se queda exactamente en el mínimo y no hay `FighterDefeated` ni `CombatEnded`. → Task 1, `floorHoldsUnderStackedDamage`.
3. **Lanzar el combo correcto antes de pulsar «Entendido»** (la pausa debe impedirlo): no se puede marcar en pausa. → Task 3, `stepWaitsForUnderstood`.
4. **Salir a mitad de lección:** el progreso de la lección no se marca y la partida no cambia. → Task 3.
5. **Lección `drain` con 3 muñecos:** el robo de vida a todos los oponentes cuenta como el paso, sea cual sea el objetivo seleccionado. → Task 2 y Task 3.

---

### Task 1: Motor — mínimo de vida (`hpFloorPct`)

**Files:**
- Modify:
  - `engine/.../model/CombatState.kt`: `CombatSetup(..., val hpFloorPct: Int? = null)` y el estado que lo guarda;
  - `engine/.../rules/CombatEngine.kt`, `ComboApplier.kt`, `TickResolver.kt`: todos los caminos que restan vida.
- Test: `engine/src/test/kotlin/com/wardengambits/engine/HpFloorTest.kt`

**Interfaces:**
- Produces: `CombatSetup.hpFloorPct: Int?`. Con un valor, la vida de cualquier luchador nunca baja de `maxHp * hpFloorPct / 100` (y nunca de 1).

- [ ] **Step 1: Write the failing tests** `HpFloorTest`:
  - `playerNeverDropsBelowFloor`: un rival fuerte contra un jugador de nivel 1 con `hpFloorPct = 10`, 200 ticks → la vida del jugador ≥ 10 % todo el rato, sin `FighterDefeated` ni `CombatEnded`;
  - `rivalsNeverDropBelowFloor`: el jugador fuerte contra un muñeco → lo mismo para el rival;
  - `floorHoldsUnderStackedDamage`: DoT, área y golpe directo en el mismo tick;
  - `noFloorByDefault`: el mismo combate sin `hpFloorPct` termina con un `CombatEnded` como hoy.
- [ ] **Step 2: Run** `./gradlew :engine:test --tests "*HpFloorTest*"`. Expected: FAIL (no compila).
- [ ] **Step 3: Implement.** Una sola función auxiliar de «restar vida con mínimo», usada en todos los sitios donde baja la vida.
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test`. Expected: PASS, sin warnings nuevos.

### Task 2: Motor — lecciones y progreso

**Files:**
- Create:
  - `engine/src/main/resources/tutorial/lessons.json`
  - `engine/.../tutorial/TutorialLesson.kt` (modelo y carga)
  - `engine/.../tutorial/TutorialProgress.kt`
- Test: `engine/src/test/kotlin/com/wardengambits/engine/tutorial/TutorialTest.kt`

**Interfaces:**
- Consumes: `CombatSetup.hpFloorPct` (Task 1).
- Produces:
  - `@Serializable data class TutorialLesson(val id: String, val steps: List<String>, val rivals: List<RivalContent>)`;
  - `object TutorialLessons { fun load(): List<TutorialLesson>; const val HP_FLOOR_PCT = 10; fun setup(lesson: TutorialLesson): CombatSetup }`. `setup` da el Warden a nivel 1, los rivales a nivel 1 marcados como no jefes y `hpFloorPct = 10`.
  - `data class TutorialProgress(val lessonId: String, val step: Int = 0, val misses: Int = 0, val completed: Boolean = false)`;
  - `fun TutorialProgress.advance(lesson: TutorialLesson, events: List<CombatEvent>): TutorialProgress`. Solo cuentan los `ComboResolved` del jugador (`PLAYER_ID`): con `combo.sequence == steps[step]` avanza; con otra secuencia suma un fallo; al pasar el último paso, `completed = true`.

- [ ] **Step 1: Write the failing tests:**
  - `lessonsMatchTheSpec`: los 5 ids, los pasos exactos de la tabla, el número de rivales (1, 1, 1, 3, 1) y que cada secuencia existe en `Combos`;
  - `advancesOnRequestedCombo`;
  - `otherComboCountsMiss`;
  - `rivalCombosIgnored`;
  - `rejectedComboDoesNotAdvance`: un `ComboRejected` o `ComboQueued` sin `ComboResolved` no avanza;
  - `completesAfterLastStep`;
  - `setupHasFloorAndLevelOne`;
  - `fullLessonPlayableWithRealEngine`: para cada lección, lanzar sus pasos en orden con `CombatEngine` real (esperando ticks para el power si hace falta) la completa sin que termine el combate.
- [ ] **Step 2: Run** `./gradlew :engine:test --tests "*TutorialTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement.** Carga como la del `StoryContentLoader`, con `kotlinx-serialization`, y los patrones de los muñecos de los Global Constraints.
- [ ] **Step 4: Run** `./gradlew :engine:test`. Expected: PASS.

### Task 3: App — combate guiado, textos y lista de lecciones

**Files:**
- Create:
  - `ui/combat/TutorialCombatSource.kt` (o una clase más en `CombatSource.kt`)
  - `ui/tutorial/TutorialPanel.kt`
  - `ui/tutorial/TutorialListScreen.kt` y `TutorialViewModel.kt`
- Modify:
  - `CombatViewModel.kt`, `CombatScreen.kt` y `CombatRoute.kt`: el modo tutorial, con panel, pausa al leer y resultado «¡Lección completada!»;
  - `data/SettingsRepository.kt` y `AppContainer.kt`: el progreso del tutorial;
  - `navigation/WardenNavHost.kt`: rutas `tutorial` y `tutorial_combat/{lesson}`;
  - `strings.xml` es/en.
- Test:
  - `app/src/test/.../ui/tutorial/TutorialFlowTest.kt`
  - `TutorialListScreenTest.kt`
  - `SettingsRepositoryTest`

**Interfaces:**
- Consumes: `TutorialLessons`, `TutorialProgress` (Task 2) y el patrón `CombatSource` (no informa a la sesión).
- Produces:
  - `SettingsRepository.tutorialDone: Flow<Set<String>>`, `markTutorialDone(id)`, `tutorialDismissed: Flow<Boolean>` y `dismissTutorial()`;
  - `AppContainer.tutorialDone` y `tutorialDismissed: StateFlow`;
  - `Routes.TUTORIAL = "tutorial"` y `Routes.TUTORIAL_COMBAT = "tutorial_combat/{lesson}"`.
- **Textos que escribe esta tarea**, en es y en:
  - `tutorial_title` («Campo de entrenamiento» / «Training ground»);
  - `tutorial_lesson_<id>` (nombre de la lección) y `tutorial_<id>_intro`;
  - `tutorial_<id>_step_<n>`, con n desde 0, en tono de instructor y en segunda persona: qué hace el combo y cómo se marca, con los datos del §3 de la spec (Furia +20 % de daño de lanza, Guardia −20 % de daño recibido, Escudo maestro −50 %, Escudo táctico inmune al Daze, Brío +20 % de regeneración de power, y cadenas: encadenar en orden da bonus y repetir o cambiar de familia la rompe);
  - `tutorial_understood` («Entendido»), `tutorial_hint` («Ese no: marca %1$s y lanza ⚔»), `tutorial_step_of` («Paso %1$d de %2$d»), `tutorial_completed` («¡Lección completada!»), `tutorial_next` («Siguiente lección»), `tutorial_back_to_map` («Volver al mapa») y `tutorial_ready` («¡Listo para Bree!»);
  - `tutorial_dummy` («Muñeco de entrenamiento» / «Training dummy»).

  La intro de `attack` explica qué es un combo (teclas, ⚔, coste de power, ticks y efectos que duran). Un paso de `daze` explica la estrella del Daze.

- [ ] **Step 1: Write the failing tests:**
  - `stepWaitsForUnderstood`: al entrar, el combate está en pausa con el texto de la intro; tras «Entendido» se puede marcar.
  - `correctComboAdvances`: lanzar `11` en `attack` pasa al paso 2 y muestra su texto.
  - `wrongComboShowsHint`.
  - `lessonCompletes`: completar los 5 pasos de `attack` muestra «¡Lección completada!» y «Siguiente lección», y marca `attack` en `tutorialDone`.
  - `tutorialNeverTouchesProfile`: el perfil de la sesión queda idéntico.
  - **Lista de lecciones:** 5 lecciones, con ✔ en las completadas; tocar una abre su combate.
  - **Ajustes:** `tutorialDone` y `tutorialDismissed` se guardan.
  - **Idiomas:** en inglés y en tengwar.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*Tutorial*" --tests "*Settings*"`. Expected: FAIL.
- [ ] **Step 3: Implement.**
  - **Panel:** encima de las teclas, con el título, «Paso n de m», el texto y las fichas de teclas del combo pedido (los mismos colores que `ActionButtons`); un ✔ breve al acertar.
  - **Lectura:** la pausa al mostrar texto nuevo usa la pausa existente del ViewModel.
  - **Fin:** el resultado de la lección sustituye al overlay de victoria o derrota; como nadie muere, el combate solo termina al completar la lección.
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.

### Task 4: App — parada en el mapa, tarjeta inicial y retrato

**Files:**
- Modify:
  - `ui/map/MapScreen.kt` y `MapMarkers.kt`: el marcador «Campo de entrenamiento» a la izquierda de Bree, al tocarlo `onTutorial()`, y la tarjeta de partida nueva;
  - `navigation/WardenNavHost.kt`;
  - `ui/common/Portraits.kt`: sprite 16×16 `dummy`;
  - `docs/content/prompts.md`: fila `portrait_dummy.webp` en Retratos (muñeco de entrenamiento de paja y madera con un escudo de madera, mismo estilo de busto pixel art), regenerando `imagenes-pendientes.md` con `python tools/gemini/generate_images.py pending`;
  - `strings.xml`: `tutorial_card_text` («Aprende a combatir en 5 lecciones» / «Learn to fight in 5 lessons»), `tutorial_start` («Empezar entrenamiento») y `tutorial_skip` («Saltar»);
  - `CLAUDE.md`: Estado.
- Test: `app/src/test/.../ui/map/TutorialStopTest.kt`

**Interfaces:**
- Consumes: `AppContainer.tutorialDone/tutorialDismissed`, `dismissTutorial()` y `Routes.TUTORIAL` (Task 3).
- Produces: `MapScreen(..., showTutorialCard: Boolean, onTutorial: () -> Unit, onSkipTutorial: () -> Unit)`, con valores por defecto que no cambian a quien ya lo llama.

- [ ] **Step 1: Write the failing tests:**
  - el mapa muestra el marcador «Campo de entrenamiento» y tocarlo llama a `onTutorial`;
  - con `showTutorialCard = true` se ve la tarjeta; «Empezar entrenamiento» llama a `onTutorial` y «Saltar» a `onSkipTutorial`;
  - con `false` no se ve;
  - `showTutorialCard` es `true` solo si `tutorialDone` no tiene las 5 lecciones y no se ha saltado;
  - el sprite `dummy` existe en `Portraits`.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*TutorialStop*"`. Expected: FAIL.
- [ ] **Step 3: Implement.** El marcador sigue el estilo de `ZoneMarker`. La tarjeta va sobre el mapa, cerca del marcador, y el mapa se desplaza hasta él cuando se muestra.
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.
- [ ] **Step 5: Manual en el emulador** (el controlador):
  - partida nueva → la tarjeta en el mapa;
  - las 5 lecciones completas;
  - «Saltar» oculta la tarjeta;
  - la partida no cambia;
  - capturas `docs/screenshots/v3/tutorial-map.png`, `tutorial-lesson.png` y `tutorial-list.png`.
