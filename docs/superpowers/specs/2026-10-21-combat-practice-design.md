# Modo combate (práctica) — diseño

Fecha: 2026-10-21. Estado: aprobado en conversación.

Elecciones del usuario:
- **Solo práctica:** repetir un combate no da niveles ni victorias, no cuenta muertes y no cambia la historia.
- **Nivel de aquel momento:** el Warden lucha con el nivel esperado en ese nodo, no con su nivel actual.
- **Desbloqueo por progreso:** entran también las ramas no jugadas de las zonas ya pasadas.

## 1. Objetivo

Un nuevo botón del menú, «Modo combate», abre un grid con los combates de todos los nodos de la historia. Los que el jugador ya ha alcanzado están desbloqueados y se pueden repetir cuantas veces quiera para entrenar combos o volver a disfrutar de los jefes.

Fuera de alcance:
- recompensas y récords;
- elegir el nivel;
- combates personalizados (mezclar rivales);
- anuncios: el banner sigue solo en el mapa.

## 2. Motor (`:engine`, puro y determinista)

- **`ExpectedLevels`** se mueve de `:sim` a `:engine`, en `com.wardengambits.engine.story`, con el mismo comportamiento: nivel esperado al llegar a un nodo = 1 + victorias esperadas antes, con todas las rutas de una zona igual de probables. `:sim` pasa a importarlo del motor.
- **`PracticeCatalog`:** `fun entries(content: StoryContent, profile: StoryProfile): List<PracticeEntry>`.
  - Devuelve todos los nodos con `encounter`, en orden de ruta (zona, nivel, índice).
  - `PracticeEntry`:
    - `node: NodeId`
    - `zoneId: String`
    - `level: Int` (nivel del nodo, 1..7)
    - `rivals: List<RivalContent>`
    - `isBoss: Boolean` (el nodo es de nivel 7)
    - `unlocked: Boolean`
    - `playerLevel: Int` (= `ExpectedLevels.expectedLevel(node)`)

  Los textos (título, nombre de zona, nombres de rivales) los pone la app con el contenido traducido, como en el resto de pantallas.
- **Regla de desbloqueo:** un combate está desbloqueado si se cumple alguna de estas condiciones:
  - `profile.history` tiene alguna vuelta completada (`RunResult.COMPLETED`): entonces todos;
  - su zona va antes, en `Route`, que la zona del nodo actual de la vuelta;
  - está en la zona actual con `nivel < nivel del nodo actual`, o es el nodo actual con la fase ya pasada (`CHOOSING` tras ganarlo).

  Una partida nueva (Bree 1_0) no tiene ninguno desbloqueado.
- **`PracticeCatalog.setup(content, node)`:** devuelve el `CombatSetup` del combate de práctica:
  - el jugador `FighterSpec("Warden", expectedLevel(node))`;
  - los rivales como `StoryEngine.combatSetupFor` en la primera vuelta (`lapStartLevel = 1`), con el mismo marcado de jefe.

  Se comparte la construcción de rivales con `StoryEngine` para no duplicarla.
- **No toca la partida:** no hay acción nueva en `StoryEngine`; un combate de práctica nunca llega a `StoryAction.CombatFinished`.

## 3. Pantalla «Modo combate» (`:app`)

- **Menú:** botón «Modo combate» / «Combat mode», debajo de «Modo historia». Ruta nueva `practice`.
- **`PracticeViewModel`:** lee el perfil de la sesión y el contenido traducido, y expone `PracticeUiState`, una lista de secciones por zona con sus casillas.
- **Grid:**
  - `LazyVerticalGrid` con columnas adaptativas (unos 104 dp; 3 en móvil, más en tablet) y una cabecera por zona que ocupa todo el ancho.
  - **Casilla desbloqueada:** retrato del primer rival (el jefe con su franja «ELITE»), el título del nodo, «Nv X» con el nivel del Warden y «×N» si hay varios rivales. Al tocarla empieza el combate.
  - **Casilla bloqueada:** retrato oscurecido y en gris con un candado, sin título (para no destripar la historia) y sin respuesta al toque.
  - El grid se abre desplazado hasta la zona actual.
- **Textos:** en `strings.xml` es/en; en tengwar pasan por `tr`/`trText` como el resto de la app.
- **Sin banner de anuncios.**

## 4. El combate de práctica

- **Pantalla:** se reutiliza la pantalla de combate (`CombatRoute`/`CombatScreen`/`CombatViewModel`) con un modo práctica.
  - El `CombatSetup` viene de `PracticeCatalog.setup`.
  - Al terminar **no** se llama a la sesión ni al `StoryEngine`.
- **Final:**
  - **Victoria:** «¡Victoria!»; **derrota:** «Has caído».
  - Dos botones: «Repetir» (el mismo combate con una semilla nueva) y «Volver al grid».
  - Abandonar desde la pausa vuelve al grid.
- **Igual que en la historia:** sonido, pausa, ayuda de combos y números flotantes.
- **La partida no cambia:** nivel, victorias, muertes, nodo, fase y camino siguen idénticos después de cualquier número de combates de práctica.

## 5. Pruebas

- **Motor:**
  - `ExpectedLevelsTest`, movido, da los mismos valores que antes;
  - `PracticeCatalogTest`:
    - partida nueva → ninguno desbloqueado;
    - a mitad de Moria → todas las zonas anteriores y los niveles inferiores de Moria desbloqueados, incluidas las ramas no jugadas, y el resto bloqueado;
    - con una vuelta completada → todos;
    - `playerLevel` igual a `expectedLevel`;
    - `setup` con el nivel esperado y los rivales de la primera vuelta, y el jefe marcado.
- **App:**
  - el grid muestra casillas bloqueadas y desbloqueadas y las cabeceras de zona;
  - tocar una bloqueada no hace nada;
  - tocar una desbloqueada abre el combate;
  - el final de práctica ofrece «Repetir» y «Volver al grid»;
  - tras un combate de práctica el perfil de la sesión no cambia;
  - en inglés y en tengwar.
- **Verificación:**
  - `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`;
  - capturas en el emulador del grid y de un combate de práctica (`docs/screenshots/v3/practice-grid.png`, `practice-combat.png`).
