# Modo combate (práctica) — plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** un botón «Modo combate» en el menú que abre un grid con todos los combates de la historia, con los desbloqueados por progreso. Se pueden repetir como práctica al nivel esperado de ese nodo, sin tocar la partida.

**Architecture:**
- **Motor:** calcula qué está desbloqueado y con qué nivel (`ExpectedLevels` pasa de `:sim` a `:engine`; `PracticeCatalog` es nuevo, con fachada en `StoryEngine`).
- **Combate:** la pantalla de combate existente se generaliza con una fuente de combate: historia o práctica. La práctica nunca informa a la sesión.
- **Pantalla nueva:** `PracticeScreen`, el grid por zonas.

**Tech Stack:** Kotlin, Jetpack Compose (`LazyVerticalGrid`), JUnit 5 (`:engine`), Robolectric (`:app`).

**Spec:** `docs/superpowers/specs/2026-10-21-combat-practice-design.md`

## Global Constraints

- **`:engine` puro y determinista,** sin dependencias nuevas, sin warnings nuevos y con `when` exhaustivo.
- **La práctica no toca la partida:**
  - no se dispara `StoryAction.CombatFinished`;
  - el perfil (nivel, victorias, muertes, nodo, fase, camino, historial) queda idéntico.
- **Nivel del Warden en práctica:** `ExpectedLevels.expectedLevel(node)`.
- **Rivales:** como en la primera vuelta (`lapStartLevel = 1`); jefe = primer rival de un nodo de nivel 7.
- **Desbloqueo por progreso:**
  - con alguna vuelta `COMPLETED` en el historial, todos;
  - si no, las zonas anteriores a la del nodo actual en `Route`;
  - en la zona actual, los nodos de nivel menor que el actual, y el nodo actual si la fase es `CHOOSING`.
  - Una partida nueva no tiene ninguno.
- **Textos:** es/en en `strings.xml` (paridad con `StringResourcesParityTest`) y tengwar por `tr`/`trText`.
- **Textos nuevos:**

  | clave | es | en |
  |---|---|---|
  | `menu_practice` | Modo combate | Combat mode |
  | `practice_title` | Modo combate | Combat mode |
  | `practice_level` | Nv %1$d | Lv %1$d |
  | `practice_locked` | Bloqueado | Locked |
  | `combat_practice_retry` | Repetir | Fight again |
  | `combat_practice_back` | Volver al grid | Back to the grid |

- **Sin anuncios** en ninguna pantalla nueva.
- **Sin commits** (regla del proyecto). Se conservan los finales de línea; `strings.xml` es CRLF, así que nada de `sed -i` sobre él.
- **Entorno:** `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`.

## Review Focus

1. **Cambiar de idioma con el grid abierto:** títulos y zonas se recomponen en el idioma nuevo, porque leen `storyTexts`. → Task 3, `gridFollowsLanguage`.
2. **Abandonar o salir con atrás a mitad de un combate de práctica:** vuelve al grid sin informar a la sesión. → Task 2, `practiceAbandonDoesNotReport`.
3. **«Repetir» varias veces seguidas:** cada combate empieza limpio, con vida y power llenos, tick 0 y semilla nueva. → Task 2, `retryStartsAFreshCombat`.
4. **Perfil en `LAP_COMPLETE` o en `orodruin_final`:** el catálogo no falla; con vuelta completada, todo desbloqueado. → Task 1, `lapCompleteUnlocksEverythingWithoutCrash`.
5. **Tablet:** el grid usa más columnas y las cabeceras de zona ocupan todo el ancho. → Task 3, verificación manual en el emulador con densidad de tablet.

---

### Task 1: Motor — nivel esperado y catálogo de práctica

**Files:**
- Move: `sim/src/main/kotlin/com/wardengambits/sim/ExpectedLevels.kt` → `engine/src/main/kotlin/com/wardengambits/engine/story/ExpectedLevels.kt` (paquete `com.wardengambits.engine.story`), y su test si existe en `:sim` → `engine/src/test/...`. Se actualizan los imports de `:sim`.
- Create: `engine/src/main/kotlin/com/wardengambits/engine/story/PracticeCatalog.kt`
- Modify: `engine/src/main/kotlin/com/wardengambits/engine/story/StoryEngine.kt`. Se extrae la construcción de rivales de `combatSetupFor` a una función privada `rivalSpecs(node: NodeId, lapStartLevel: Int): List<RivalSpec>`, que comparten la historia y la práctica.
- Test: `engine/src/test/kotlin/com/wardengambits/engine/story/PracticeCatalogTest.kt`

**Interfaces:**
- Produces:
  - `class ExpectedLevels(content: StoryContent)` con `fun expectedLevel(node: NodeId): Int`, igual que ahora;
  - `data class PracticeEntry(val node: NodeId, val zoneId: String, val level: Int, val rivals: List<RivalContent>, val isBoss: Boolean, val unlocked: Boolean, val playerLevel: Int)`;
  - `object PracticeCatalog { fun entries(content: StoryContent, profile: StoryProfile): List<PracticeEntry> }`;
  - `StoryEngine.practiceEntries(profile: StoryProfile): List<PracticeEntry>`;
  - `StoryEngine.practiceSetup(node: NodeId): CombatSetup?`, que da `null` si el nodo no tiene encuentro.

- [ ] **Step 1: Write the failing tests** `PracticeCatalogTest` (contenido real con `StoryContentLoader.loadFromResources()`):
  - `newGameUnlocksNothing()`: con `engine.newGame()`, todas las entradas tienen `unlocked = false`; el número de entradas es el de nodos con encuentro en todo el contenido, y van en orden de ruta (zona en `Route`, nivel, índice).
  - `midMoriaUnlocksEarlierZonesAndLowerLevels()`: con el perfil en `moria_4_1` en `COMBAT_PENDING`:
    - todos los encuentros de bree…durin están desbloqueados, incluidas las ramas no recorridas;
    - los de moria de nivel < 4, también;
    - `moria_4_1` no (está pendiente), ni nada de moria de nivel ≥ 4, ni de sombrio en adelante.
  - `currentNodeUnlocksOnceWon()`: con el mismo nodo en `CHOOSING`, `moria_4_1` sí está desbloqueado.
  - `lapCompleteUnlocksEverythingWithoutCrash()`:
    - con el perfil en `NodeId.FINAL` y `LAP_COMPLETE`, todo desbloqueado;
    - con un perfil en Bree con una vuelta `COMPLETED` en `history`, todo desbloqueado.
  - `playerLevelIsTheExpectedLevel()`: para cada entrada, `playerLevel == ExpectedLevels(content).expectedLevel(node)`.
  - `practiceSetupUsesExpectedLevelAndFirstLapRivals()`: para `moria_7_0`:
    - `setup.player.level == expectedLevel`;
    - los rivales y niveles son iguales a los de `combatSetupFor` con un perfil de vuelta 1 en ese nodo y `COMBAT_PENDING`;
    - `rivals.first().isBoss`.

    Para un nodo sin encuentro, `practiceSetup` da `null`.
- [ ] **Step 2: Run** `./gradlew :engine:test --tests "*PracticeCatalogTest*"`. Expected: FAIL (no compila).
- [ ] **Step 3: Implement.** Mueve `ExpectedLevels` (y su test), implementa `PracticeCatalog` y la fachada de `StoryEngine`, y extrae `rivalSpecs`. `ExpectedLevels` se construye una vez por `StoryEngine`, de forma perezosa.
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test`. Expected: PASS, sin warnings nuevos en `:engine`.

### Task 2: Combate de práctica (ViewModel y pantalla de combate)

**Files:**
- Create: `app/src/main/java/com/wardengambits/app/ui/combat/CombatSource.kt`
- Modify:
  - `ui/combat/CombatViewModel.kt`: recibe un `CombatSource` en lugar de `session` + `storyEngine`, y gana `fun retry()`;
  - `ui/combat/CombatScreen.kt`: el `ResultOverlay` en modo práctica muestra «Repetir» y «Volver al grid»;
  - `ui/combat/CombatRoute.kt`;
  - `navigation/WardenNavHost.kt`: la ruta de combate de la historia pasa a usar `StoryCombatSource`;
  - `res/values/strings.xml` y `res/values-en/strings.xml`: `combat_practice_retry` y `combat_practice_back`.
- Test: `app/src/test/java/com/wardengambits/app/ui/combat/PracticeCombatViewModelTest.kt`. Los tests actuales de `CombatViewModel` y de la pantalla siguen en verde, adaptados solo al nuevo constructor.

**Interfaces:**
- Consumes: `StoryEngine.practiceSetup(node)` (Task 1).
- Produces:
  - `sealed interface CombatSource { val setup: CombatSetup?; fun rivalNames(engine: StoryEngine): List<String> }`:
    - `class StoryCombatSource(session: GameSession, storyEngine: StoryEngine) : CombatSource`, con `fun report(outcome: Outcome)`, que hace lo de hoy (`session.dispatch(StoryAction.CombatFinished(...))`);
    - `class PracticeCombatSource(node: NodeId, storyEngine: StoryEngine) : CombatSource`, sin informar nada;
  - `CombatViewModel(source: CombatSource, story: StateFlow<StoryEngine>, combatEngine: CombatEngine, clock: () -> Long, tickMillis: Long = 1000L)`;
  - `CombatViewModel.isPractice: Boolean`;
  - `CombatViewModel.retry()`: solo en práctica; empieza un combate nuevo con `combatEngine.start(setup, clock())`, quita el resultado y deja de estar parado;
  - `CombatRoute(viewModel, soundPlayer, onBackToNode)`: en práctica, `onBackToNode` vuelve al grid.

- [ ] **Step 1: Write the failing tests** `PracticeCombatViewModelTest`, con `GameSession` real o falsa como en los tests actuales y reloj controlado:
  - `practiceFinishDoesNotReport()`: tras ganar o perder un combate de práctica y llamar a `finish()`, `session.profile.value` es igual al perfil de antes.
  - `practiceAbandonDoesNotReport()`: `abandon()` a mitad de combate no cambia el perfil.
  - `retryStartsAFreshCombat()`: tras terminar, `retry()` deja `outcome == null`, el tick a 0 y la vida y el power del jugador y de los rivales llenos. Se repite dos veces.
  - `storyCombatStillReports()`: con `StoryCombatSource`, `finish()` tras una victoria sube el nivel como hoy (regresión).
  - Compose: con `isPractice`, el overlay de resultado muestra «Repetir» y «Volver al grid»; con la historia, el botón de siempre.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*PracticeCombatViewModelTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement.** Introduce `CombatSource`, adapta el ViewModel y la ruta de combate de la historia, y añade la variante de práctica del overlay, con sus textos en es/en.
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest`. Expected: PASS (todos, también los existentes).

### Task 3: Grid «Modo combate», menú y navegación

**Files:**
- Create:
  - `app/src/main/java/com/wardengambits/app/ui/practice/PracticeViewModel.kt`
  - `app/src/main/java/com/wardengambits/app/ui/practice/PracticeScreen.kt`
- Modify:
  - `ui/menu/MenuScreen.kt`: botón `menu_practice` debajo de «Modo Historia», con el parámetro `onPractice: () -> Unit = {}`;
  - `navigation/WardenNavHost.kt`: rutas `Routes.PRACTICE = "practice"` y `Routes.PRACTICE_COMBAT = "practice_combat/{zone}/{level}/{index}"`;
  - `strings.xml` es/en: `menu_practice`, `practice_title`, `practice_level` y `practice_locked`.
- Test:
  - `app/src/test/java/com/wardengambits/app/ui/practice/PracticeScreenTest.kt`
  - `PracticeViewModelTest.kt`

**Interfaces:**
- Consumes: `StoryEngine.practiceEntries(profile)` (Task 1); `PracticeCombatSource` y `CombatRoute` (Task 2).
- Produces:
  - `data class PracticeCellUi(val node: NodeId, val title: String, val portrait: String, val isBoss: Boolean, val rivalCount: Int, val playerLevel: Int, val unlocked: Boolean)`;
  - `data class PracticeZoneUi(val zoneId: String, val name: String, val cells: List<PracticeCellUi>)`;
  - `PracticeViewModel(session: GameSession, storyTexts: StateFlow<StoryEngine>)` con `val zones: StateFlow<List<PracticeZoneUi>>` y `val currentZoneIndex: Int`;
  - `@Composable fun PracticeScreen(zones: List<PracticeZoneUi>, initialZoneIndex: Int, onFight: (NodeId) -> Unit, onBack: () -> Unit)`.

- [ ] **Step 1: Write the failing tests.**
  - `PracticeViewModelTest`:
    - mapea las entradas a zonas con el nombre traducido;
    - el título es el del nodo y el retrato el del primer rival;
    - `gridFollowsLanguage()`: al cambiar `storyTexts` a inglés, los nombres de zona y los títulos cambian.
  - `PracticeScreenTest`:
    - se ven las cabeceras de zona;
    - una casilla desbloqueada muestra su título, «Nv X» y «×4» en un grupo de 4;
    - una casilla bloqueada muestra «Bloqueado» y no su título, y tocarla no llama a `onFight`;
    - tocar una desbloqueada llama a `onFight(node)`;
    - un jefe muestra la franja «ELITE»;
    - en inglés y en tengwar.
  - `MenuScreenTest`: el botón «Modo combate» existe y llama a `onPractice`.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*Practice*" --tests "*MenuScreenTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement.**
  - **Grid:** `LazyVerticalGrid(GridCells.Adaptive(104.dp))`, cabeceras con `GridItemSpan(maxLineSpan)` y un estado inicial que lleva a la cabecera de la zona actual.
  - **Retratos:** se reutiliza `Portrait` de `ui/common/Portraits.kt`; la franja ELITE es la de `CombatCards.kt`, y si hace falta se hace `internal` para reutilizarla.
  - **Casilla bloqueada:** retrato en gris y oscurecido, con un candado dibujado o el carácter 🔒.
  - **Navegación:** `onFight` va a `practice_combat/{zone}/{level}/{index}`, que construye el `CombatViewModel` con `PracticeCombatSource` y vuelve al grid con atrás.
- [ ] **Step 4: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.
- [ ] **Step 5: Manual en el emulador**, con una partida de prueba a mitad de la historia (por ejemplo `moria_4_1`):
  - captura del grid (`docs/screenshots/v3/practice-grid.png`), de un combate de práctica contra un jefe y de su final con «Repetir» (`practice-combat.png`);
  - tras la práctica, el nodo y el nivel de la partida no han cambiado;
  - grid en densidad de tablet (`wm density 216`) con más columnas, y luego se restablece;
  - captura del menú en un ancho estrecho: el título «Warden Gambits» parte en dos líneas por palabras, sin dejar «ts» suelto.
- [ ] **Step 6: Docs.** Una línea en el Estado de `CLAUDE.md` sobre el modo combate (spec, rutas y capturas).
