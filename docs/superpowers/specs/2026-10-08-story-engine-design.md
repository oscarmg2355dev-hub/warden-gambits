# Motor de historia — Diseño

Subproyecto 2 de 4. Orden acordado: combate (hecho) → **historia** → contenido → app. Fuente: `descripcion.md` y las decisiones de la sesión de brainstorming del 2026-10-08.

## 1. Objetivo y alcance

Lógica pura (Kotlin, paquete `com.wardengambits.engine.story` dentro de `:engine`) del modo historia:
- la ruta de zonas,
- la forma de los árboles de nodos,
- la carga y validación del contenido,
- el estado de la partida (nivel, vuelta, nodo, fase),
- la navegación por decisiones,
- el enlace con el motor de combate,
- la derrota con reinicio, la vuelta completada y el historial.

**Dentro:** todo lo anterior, además del **ajuste de duraciones del motor de combate** (§8) y contenido de ejemplo **solo para tests**.

**Fuera:**
- Textos reales de los nodos y prompts de imagen: subproyecto 3, contenido.
- Pantallas, persistencia en disco e imágenes: subproyecto 4, app. El motor define qué se guarda (data classes) y la app decide cómo.
- Reloj real: la app pasa `nowMillis` en las acciones que lo necesitan.

**Criterio de éxito:** una partida completa (Bree → Lothlórien → vuelta 2) se puede recorrer desde tests con acciones. Una derrota devuelve al nodo anterior y suma una muerte (desde 2026-10-13; antes reiniciaba la partida, ver §6). El validador rechaza contenido incompleto.

## 2. Ruta

15 zonas en orden fijo, definidas en código (`Route`):

| # | id | Nombre |
|---|---|---|
| 1 | `bree` | Bree |
| 2 | `chet` | Bosque de Chet |
| 3 | `moscagua` | Pantanos de Moscagua |
| 4 | `cima` | Cima de los Vientos |
| 5 | `puente` | Último Puente |
| 6 | `quebradas` | Quebradas de los Trolls |
| 7 | `bruinen` | Vado del Bruinen |
| 8 | `rivendel` | Rivendel |
| 9 | `eregion` | Eregion |
| 10 | `caradhras` | Caradhras |
| 11 | `durin` | Puertas de Durin |
| 12 | `moria` | Moria |
| 13 | `sombrio` | Valle del Arroyo Sombrío |
| 14 | `nimrodel` | Nimrodel |
| 15 | `lothlorien` | Lothlórien |

- Las zonas 1 a 14 tienen un **árbol** de 7 niveles. El nivel 1 es la llegada a la zona y el nivel 7 la salida hacia la siguiente.
- La zona 15 es un único **nodo final**.
- El mapa muestra "zona X, nivel/7".

## 3. Forma del árbol (generada por código)

Tamaños por nivel: `[1, 2, 4, 8, 4, 2, 1]` (22 nodos). Cada nodo se identifica por `(zona, nivel 1..7, índice 0..tamaño-1)`, con id `"<zona>_<nivel>_<índice>"` (por ejemplo, `bree_4_5`). El nodo final se llama `lothlorien_final`.

Destinos del nodo `(n, i)`, siendo `m` el tamaño del nivel `n+1`:
- **Se abre** (`m > tamaño(n)`): `{2i, 2i+1}`.
- **Se cierra** (`m < tamaño(n)`): `{⌊i/2⌋, (⌊i/2⌋ + 1) mod m}`. Si los dos valores coinciden, hay un solo destino.
- **Nivel 7**: un solo destino, que es el nivel 1 de la zona siguiente o, desde `nimrodel`, `lothlorien_final`.

Los destinos se ordenan por índice ascendente. Con 2 destinos el jugador **elige**; con 1 hay **Continuar**. Del nivel 1 al 6 hay elección, excepto del 6 al 7. Resultado: toda ruta por una zona visita exactamente 7 nodos.

## 4. Contenido (datos)

Un JSON por zona en `engine/src/main/resources/story/<id>.json`, leído con `kotlinx.serialization`. Es una excepción aprobada a la regla de "ninguna dependencia de terceros" del motor.

```json
{
  "id": "bree", "name": "Bree", "baseLevel": 1,
  "nodes": [
    {
      "level": 2, "index": 1,
      "title": "El Poney Pisador",
      "text": "Texto breve de la historia...",
      "imagePrompt": "Prompt en inglés para generar la imagen...",
      "choiceLabel": "Entrar en la posada",
      "encounter": {
        "rivals": [
          { "name": "Bandido de Bree", "levelOffset": 0, "pattern": ["12", "21"], "everyNTicks": 2 }
        ]
      }
    }
  ]
}
```

- `choiceLabel` es el texto del botón que **lleva a** este nodo. Es obligatorio en los niveles 2 a 6 y opcional en el resto.
- `encounter` es opcional; si existe, tiene entre 1 y 4 rivales.
- `lothlorien.json` tiene un único nodo (`level: 1, index: 0`) sin `encounter`, con `baseLevel: 0`.
- Imagen de cada nodo: un archivo con el id del nodo (`bree_4_5.webp`) que añadirá la app. El motor solo expone el id.
- Guía para el subproyecto de contenido: `baseLevel` de la zona k ≈ `1 + 3·(k−1)`, porque una ruta por zona trae unos 3 o 4 combates. Se ajustará con simulaciones.

### Validación (`ContentValidator.validate(content): List<String>`)

Devuelve una lista de errores legibles. Lista vacía significa contenido válido. Comprueba:
- están las 15 zonas, con los ids y nombres de la ruta;
- las zonas 1 a 14 tienen exactamente los 22 nodos (sin huecos ni duplicados) y `lothlorien` tiene 1;
- `title`, `text` e `imagePrompt` no están vacíos, y hay `choiceLabel` donde es obligatorio;
- los encuentros tienen entre 1 y 4 rivales; cada `pattern` no está vacío y todos sus combos existen en `Combos`; `everyNTicks` ≥ 1.

La carga (`StoryContentLoader`) lee los 15 recursos. Si falta un archivo o el JSON está mal formado, lanza una excepción con el nombre del archivo: es un error de empaquetado, no de juego.

## 5. Estado de la partida

```kotlin
data class StoryProfile(val run: RunState, val history: List<RunRecord>)
data class RunState(
    val level: Int, val lap: Int, val lapStartLevel: Int, val wins: Int,
    val node: NodeId, val path: List<NodeId>, val phase: Phase,
    val deaths: Int = 0, // muertes de la vuelta actual (2026-10-13)
)
enum class Phase { COMBAT_PENDING, CHOOSING, LAP_COMPLETE }
data class RunRecord(
    val lap: Int, val node: NodeId, val level: Int, val wins: Int,
    val result: RunResult, val finishedAtMillis: Long,
    val deaths: Int = 0, // muertes de la vuelta (2026-10-13)
)
enum class RunResult { DEFEATED, COMPLETED } // DEFEATED solo aparece en partidas guardadas antiguas
```

- **Partida nueva:** nivel 1, vuelta 1, `lapStartLevel` 1, 0 victorias, nodo `bree_1_0` y historial vacío.
- **Al entrar en un nodo:** la fase es `COMBAT_PENDING` si tiene encuentro, `LAP_COMPLETE` si es el nodo final y `CHOOSING` en el resto. El nodo se añade a `path`.
- `wins`, `path` y `deaths` son de la partida (vuelta) actual. El historial guarda todas las partidas terminadas, las más recientes al final.
- `deaths` y `RunRecord.deaths` tienen valor por defecto 0, así que las partidas guardadas antes de 2026-10-13 se cargan sin cambios.

## 6. Acciones

API: `StoryEngine(content).newGame(): StoryProfile` y `reduce(profile, action): StoryStep(profile, events)`.

| Acción | Válida en | Efecto |
|---|---|---|
| `CombatFinished(outcome, nowMillis)` | `COMBAT_PENDING` | **Victoria:** nivel +1, victorias +1, fase `CHOOSING` (o `LAP_COMPLETE` si el nodo es el final). **Derrota:** el Warden vuelve al nodo anterior de `path` (ver «Regla de derrota» abajo). |
| `Choose(option: Int)` | `CHOOSING` con 2 destinos | Entra en el destino `option` (0 o 1). |
| `Continue` | `CHOOSING` con 1 destino | Entra en el único destino. |
| `StartNextLap(nowMillis)` | `LAP_COMPLETE` | Añade `RunRecord(COMPLETED)` con las muertes de la vuelta, pone `deaths` a 0, suma 1 a la vuelta, fija `lapStartLevel` al nivel actual, pone `path` a `[bree_1_0]` y las victorias a 0 (conserva el nivel), y entra en `bree_1_0`. |

- Una acción no válida devuelve el perfil sin cambios y el evento `StoryRejected(reason)`. Nunca lanza excepciones.
- **Eventos:** `NodeEntered(node)`, `ZoneEntered(zoneId)` (al entrar en el nivel 1 de una zona o en el nodo final), `ReturnedToPreviousNode(from, to)`, `LeveledUp(newLevel)`, `RunEnded(record)` y `LapStarted(lap)`.

**Regla de derrota** (cambiada por el usuario el 2026-10-13; sustituye a la anterior, «perder reinicia la partida desde Bree», que sigue en `descripcion.md`):

- Al perder un combate (`CombatFinished(DEFEAT)` en `COMBAT_PENDING`) la partida **no** termina: el nodo pasa a ser el anterior de `path` (`path` pierde su último nodo) y la fase es `CHOOSING`, porque ese nodo ya estaba superado; las opciones son las mismas que cuando se estuvo allí.
- Nivel, victorias, vuelta y `lapStartLevel` no cambian; `deaths` suma 1. No se escribe ningún `RunRecord`: el historial solo crece al completar una vuelta.
- Eventos: `ReturnedToPreviousNode(from, to)` seguido de `NodeEntered(to)`. No hay `ZoneEntered`: el nodo anterior siempre es de la misma zona.
- El nodo anterior siempre existe: los nodos de nivel 1 nunca tienen encuentro. Si aun así `path` tiene menos de 2 nodos, el Warden vuelve a entrar en `bree_1_0` (como al entrar en un nodo normal, con sus eventos).
- En la app, abandonar un combate cuenta como derrota; tras la derrota se vuelve a la pantalla del nodo (botón «Volver al nodo anterior»).
- `isValid` exige además `deaths ≥ 0` en la partida y en cada registro.

**Consultas** (funciones puras para la app):
- `options(profile): List<Choice>`: cada opción con su destino y etiqueta. Con un solo destino, la etiqueta es `null` y la app muestra "Continuar".
- `node(id)`: el nodo con su contenido.
- `zoneProgress(profile)`: zona, nivel y total (7).
- `combatSetupFor(profile): CombatSetup?`: devuelve `null` si la fase no es `COMBAT_PENDING`.

## 7. Combate y dificultad

`combatSetupFor` construye un `CombatSetup` con:
- **Jugador:** `FighterSpec("Warden", run.level)`.
- **Rivales:** uno por cada rival del encuentro, en orden:
  - nivel = `max(1, zona.baseLevel + rival.levelOffset + (run.lapStartLevel − 1))`;
  - IA = `PatternBrain(rival.pattern, rival.everyNTicks)`.

La app juega el combate con `CombatEngine` y envía `CombatFinished(outcome)`. Cada combate empieza con vida y power al máximo, como ya hace el motor de combate. Los nodos sin combate son solo historia y decisión.

## 8. Ajuste de duraciones en el motor de combate

**Problema actual:** un efecto aplicado en el tick *t* ya descuenta tiempo en el tick *t*, y el jugador actúa antes que los rivales. Por eso Daze, Fear y los buffs de "N ticks" afectan a un número de acciones distinto según quién los aplique.

**Nueva regla:**
- Los efectos que **no** son por tiempo (POT, DEF, POW, SHM, SHT, DAZED, FEARED) aplicados o refrescados en el tick *t* **están activos desde el tick t+1 hasta el t+N, ambos incluidos**.
- En el tick *t* no cuentan para ninguna regla (bloqueo de lanzamiento, daño, defensa, escudo, regeneración ni probabilidad de estado) y no descuentan tiempo.
- Así, "Daze 2" bloquea exactamente 2 lanzamientos del afectado, sea quien sea quien lo aplique. "Pot 5" potencia los lanzamientos de los 5 ticks siguientes.
- **DoT y CuT no cambian:** hacen efecto en los ticks *t*, *t+1* y *t+2*.

La spec del combate (§3.3, §5) se actualiza en consecuencia.

## 9. Errores y casos límite

- Contenido inválido: el validador lo detecta en los tests de contenido, así que la app nunca arranca con contenido roto.
- `Choose` con una opción fuera de rango, o en un nodo con un solo destino → `StoryRejected`.
- `CombatFinished` fuera de `COMBAT_PENDING` → `StoryRejected`.
- Varias derrotas seguidas: cada una añade su propio registro al historial.
- El nodo final no tiene encuentro (lo garantiza el validador).

## 10. Testing

- **Contenido falso para tests**, generado por código: rellena las 15 zonas con textos de relleno y encuentros en los nodos que elija cada test.
- **Forma del árbol:** se comprueban explícitamente los destinos de cada nivel (por ejemplo, `(4,7) → {0,3}` y `(6,i) → {0}`), que todos los nodos son alcanzables desde el nivel 1 y que los nodos de los niveles 1 a 5 tienen exactamente 2 destinos distintos.
- **Validador:** un caso por cada tipo de error y el contenido falso completo como válido.
- **Loader:** un JSON de ejemplo se parsea a lo esperado y un JSON malformado da una excepción con el nombre del archivo.
- **Partida:**
  - recorrido completo hasta `LAP_COMPLETE` siempre por la opción 0, ganando todos los combates;
  - `StartNextLap` (nivel conservado, vuelta 2, registro COMPLETED);
  - derrota (reinicio y registro DEFEATED);
  - acciones inválidas.
- **Dificultad:** nivel de los rivales en la vuelta 1 y en la 2, y el mínimo de 1.
- **Duraciones (§8):**
  - un Daze 2 aplicado por el jugador bloquea los lanzamientos del rival en t+1 y t+2, pero no en t ni en t+3;
  - lo mismo aplicado por el rival al jugador;
  - Pot 5 activo exactamente 5 ticks.
- Todo en `./gradlew :engine:test`, siguiendo TDD.
