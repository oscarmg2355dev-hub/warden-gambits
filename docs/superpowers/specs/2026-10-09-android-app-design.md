# App Android — Diseño

Subproyecto 4 de 4 (combate ✓ → historia ✓ → contenido ✓ → **app**). Diseño aprobado en la sesión del 2026-10-09. Base: `descripcion.md` y las specs anteriores en `docs/superpowers/specs/`.

## 1. Objetivo y alcance

App Android nativa (módulo `:app`) que hace jugable Warden-Gambits con los motores existentes:
- menú,
- mapa de ruta,
- pantalla de nodo,
- combate en tiempo real con ticks de 1 s,
- personaje e historial,
- ayuda de combos,
- guardado de la partida.

**Fuera:** sonido, publicación (firma, Play Store), ajuste de dificultad (después, jugando y con simulaciones), imágenes reales (las aporta el usuario).

**Criterio de éxito:**
- se instala en el emulador `Medium_Phone_API_36.1`;
- se puede jugar desde Bree: decidir, luchar, ganar o perder;
- se ven el personaje y la ayuda;
- al cerrar y reabrir la app se conserva el progreso;
- todos los tests pasan.

## 2. Arquitectura

```
:app  (Android, Compose)  ──depende de──▶  :engine (Kotlin puro)
```

- Una sola `MainActivity`, Navigation Compose y Material 3. Orientación vertical fija. Tema oscuro (§7).
- Paquete `com.wardengambits.app`; `applicationId` `com.wardengambits`.
- **Versiones:** minSdk 26, compile/targetSdk 36, AGP 9.x (9.4.1 está en la caché local) compatible con Gradle 9.7.1 y Kotlin 2.4.20. Todas las versiones en `gradle/libs.versions.toml`.
- **Dependencias de objetos:** `AppContainer` creado en `WardenApp : Application`, que construye a mano el contenido, los motores y el repositorio. Sin Hilt ni Koin.
- **Paquetes:**

```
app/src/main/java/com/wardengambits/app/
  WardenApp.kt, MainActivity.kt, AppContainer.kt
  data/        SaveRepository (DataStore), SaveData
  navigation/  WardenNavHost, rutas
  ui/theme/    colores, tipografía (Cinzel), Theme
  ui/common/   barras, iconos de efectos y de inputs, imagen de nodo
  ui/menu/     MenuScreen
  ui/map/      MapScreen + MapViewModel
  ui/node/     NodeScreen + NodeViewModel
  ui/combat/   CombatScreen + CombatViewModel + CombatUiState
  ui/character/ CharacterScreen
  ui/help/     HelpScreen + texto de combos
```

- **Estado compartido:** un `StoryViewModel` con ámbito de navegación (o un `GameSession` en el `AppContainer`) es el único dueño del `StoryProfile` y lo expone como `StateFlow`. Cada acción de historia pasa por `StoryEngine.reduce` y se guarda en disco.
- Los ViewModels de pantalla leen ese estado y mandan acciones. Ningún composable decide reglas del juego.

## 3. Cambios en `:engine`

1. Las clases `StoryProfile`, `RunState`, `RunRecord`, `NodeId`, `Phase` y `RunResult` pasan a ser `@Serializable`. El plugin ya está aplicado; la propiedad derivada `NodeId.key` no se serializa.
2. `StoryEngine.isValid(profile): Boolean` es `true` si:
   - el nodo actual existe en la forma del árbol y en el contenido;
   - la fase es coherente con el nodo (COMBAT_PENDING solo con encuentro; LAP_COMPLETE solo en el nodo final);
   - nivel, vuelta y `lapStartLevel` son ≥ 1, y victorias ≥ 0;
   - `path` no está vacío y termina en el nodo actual.
3. `@Serializable data class SaveData(val version: Int = 1, val profile: StoryProfile)`, en `:engine` (paquete `story`) para poder testearla en la JVM.

Todo con TDD en `:engine`.

## 4. Guardado

- `SaveRepository` usa DataStore (Preferences) con una clave de texto que contiene el JSON de `SaveData`.
- `load()`:
  - sin datos → `newGame()`;
  - JSON inválido, versión desconocida o `!isValid(profile)` → `newGame()` y el error se registra con `Log.w`. La app nunca se cierra por esto.
- `save(profile)` se llama tras cada `StoryStep` que cambie el perfil.
- **No se guarda el estado del combate.** Si la app muere en mitad de un combate, el nodo sigue en `COMBAT_PENDING` y el combate se repite desde el principio.
- **"Nueva aventura"** desde el menú, con diálogo de confirmación: `newGame()` conservando el historial.

## 5. Pantallas

**Menú.**
- Título "Warden-Gambits" en Cinzel.
- Botones: Modo Historia, Personaje, Ayuda de combos y Nueva aventura (con confirmación).

**Mapa (ruta con progreso).**
- Lista vertical de las 15 zonas como un camino:
  - completadas: ✓ dorado;
  - actual: destacada, con 7 puntos (nivel actual/7) y botón **Continuar ▶**, que abre el nodo actual;
  - futuras: gris.
- El nodo final aparece como "Lothlórien".
- La zona actual se calcula con `zoneProgress`.

**Nodo.**
- Imagen del nodo a ancho completo con proporción 4:3 (§6), título, texto con scroll y, abajo, los botones según la fase:
  - `COMBAT_PENDING`: botón **"¡Luchar!"**, que abre el combate, y la lista de rivales (nombre y nivel calculado).
  - `CHOOSING` con 2 opciones: dos botones con las etiquetas `choiceLabel`.
  - `CHOOSING` con 1 opción: **"Continuar"**.
  - `LAP_COMPLETE`: **"Emprender la vuelta N+1"**, que envía `StartNextLap(now)`.
- Cabecera: zona y "nivel/7", con botón para volver al mapa.

**Combate (rejilla de cartas).** Ocupa toda la pantalla, sin scroll.
- **Cabecera:** zona, nivel y contador de ticks, y botón de pausa.
- **Rejilla 2×2 de cartas de rival:**
  - cada carta: nombre, "Nv X", barra de vida con número, barra de power e iconos de efectos activos;
  - la carta objetivo lleva borde dorado y "◎";
  - un rival derrotado aparece atenuado con "✝";
  - tocar una carta viva envía `SelectTarget`.
- **Carta del Warden:** nivel, vida y power (barra + valor/máximo), efectos activos y **combo en curso**: el buffer mostrado con iconos y, si coincide con un combo, su nombre corto (por ejemplo "121 · Daño ×1.5").
- **Botonera:** SPEAR, SHIELD y FIST (`Press`), botón ⌫ para borrar el combo (`ClearBuffer`) y el botón grande **⚔ Acción** (`Execute`). Al menos 56 dp de alto, con vibración al pulsar.
- **Feedback de eventos** (mediante un `SharedFlow` de `CombatEvent`):
  - `ComboRejected`: texto breve y vibración de error;
  - `DamageDealt`: número flotante sobre la carta;
  - estados aplicados: icono destacado;
  - `CombatEnded`: aparece el resultado.
- **Resultado:**
  - **Victoria:** "Nivel N → N+1" y botón Continuar. Envía `CombatFinished(VICTORY, now)` y vuelve al nodo, que pasa a `CHOOSING`.
  - **Derrota:** "Has caído", con la zona, el nivel y las victorias, y el botón "Volver al nodo anterior". Envía `CombatFinished(DEFEAT, now)` y vuelve a la pantalla del nodo, que ya es el anterior (regla de derrota del 2026-10-13, spec de historia §6; antes: "Volver a Bree" y al mapa).
- **Ticks:**
  - un bucle en el `viewModelScope` envía `Tick` cada 1000 ms solo mientras no está en pausa y la pantalla está al menos en `STARTED`;
  - al pasar a segundo plano se pausa automáticamente;
  - el intervalo y el reloj se inyectan para los tests.
- **Semilla del combate:** `nowMillis` al crear el `CombatViewModel`. El `CombatSetup` viene de `combatSetupFor(profile)`.

**Personaje.**
- Nivel, vuelta, victorias de la partida actual y zona actual con progreso.
- Stats de `LevelStats.forLevel(level)`: vida, power, regeneración, ataque, cura y defensa.
- Historial, de más reciente a más antiguo: vuelta, resultado (Completada/Caído), zona y nodo alcanzados, nivel, victorias y fecha (`finishedAtMillis` formateada en local).

**Ayuda de combos.**
- Generada a partir de `Combos.all`, agrupada por familia:
  - Daño: `12…`, `31…`;
  - Área: `32…`, `23…`;
  - Curación: `21…`;
  - Utilidad: `11`, `22`, `33`, `31232`, `2132`, `2131`.
- Cada fila muestra la secuencia con iconos de input, el coste de power y la descripción en español calculada del `ComboDefinition`. Por ejemplo, "Daño 150% ataque + 45%/tick durante 3 ticks · Aturdir 30% (2 ticks)".
- Arriba, una breve explicación: pulsar los inputs y luego Acción; ticks de 1 s; Daze bloquea todos los combos y Fear los de daño.

## 6. Imágenes de nodo

- `NodeImage(key)` busca un drawable llamado `key` (`bree_4_5`, `lothlorien_final`) mediante `resources.getIdentifier`. Las imágenes del usuario irán en `app/src/main/res/drawable-nodpi/<key>.webp`.
- Si no existe, muestra una imagen provisional: un degradado con el color de la zona y el nombre de la zona en Cinzel. Se resuelve una vez por clave.

## 7. Estilo

- **Paleta oscura:**
  - fondo #15110D;
  - superficie #241C14 (pergamino oscuro);
  - primario dorado #C9A44C;
  - texto #EDE3CF;
  - vida #B23A3A;
  - power #3A6EB2;
  - estados: verde para buffs y morado para Daze/Fear.
- **Tipografía:** Cinzel (fuente OFL descargada de Google Fonts e incluida en `res/font`) para títulos y botones; `FontFamily.Serif` para el texto.
- **Iconos:**
  - inputs (lanza, escudo, puño) e iconos de efecto: vectores propios sencillos o símbolos tipográficos;
  - sin librerías de iconos externas salvo `material-icons-core`, si hace falta.

## 8. Errores y casos límite

- Guardado corrupto o incompatible → partida nueva (§4).
- Acciones rechazadas por los motores → feedback breve, nunca un cierre de la app.
- Rotación y cambios de configuración: orientación fija; los ViewModels conservan el estado.
- `combatSetupFor` devuelve `null` al entrar al combate (no debería ocurrir) → se vuelve al nodo.
- Volver atrás en mitad del combate → diálogo "¿Abandonar el combate?" que cuenta como derrota: vuelve al nodo anterior, como perder (2026-10-13).

## 9. Testing

- **`:engine`:** serialización de ida y vuelta de `SaveData`, casos de `isValid` y JSON corrupto.
- **`:app` (tests JVM con coroutines-test y Turbine):**
  - `SaveRepository` con un DataStore temporal: guardar y cargar, JSON corrupto → partida nueva.
  - `CombatViewModel` con `StandardTestDispatcher`:
    - el tick avanza cada 1000 ms virtuales;
    - la pausa detiene los ticks;
    - las pulsaciones llegan al motor;
    - la victoria y la derrota llaman a la historia correctamente;
    - semilla y reloj inyectados.
  - Lógica de historia y nodo: las acciones pasan al motor, se guarda el perfil y se calculan los botones según la fase.
  - Texto de la ayuda: la descripción generada de 3 o 4 combos representativos.
- **Compose UI (Robolectric, en la JVM):**
  - el menú navega;
  - el mapa muestra la zona actual y el progreso;
  - el nodo muestra las opciones o "¡Luchar!";
  - el combate pinta las cartas y responde a las pulsaciones.
- **Verificación manual automatizada:** `assembleDebug`, `installDebug` en el emulador `Medium_Phone_API_36.1`, abrir la app y capturar pantallas de menú, mapa, nodo y combate con `adb exec-out screencap` para revisarlas.
