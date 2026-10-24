# Tutorial de combate — diseño

Fecha: 2026-10-23. Estado: aprobado en conversación.

Elecciones del usuario:
- **Lugar:** el tutorial es la **primera parada del mapa**, «Campo de entrenamiento», antes de Bree. Se puede saltar y repetir.
- **No se puede perder.**
- **No da nada:** ni nivel ni victorias, y no cambia la historia.
- **Lecciones y combos:** las 5 que propuso el usuario (§3).

## 1. Objetivo

Enseñar a jugar con cinco lecciones de combate guiadas. Cada una explica un concepto y pide al jugador combos concretos; solo avanza cuando los lanza.

Fuera de alcance:
- narración en voz de las lecciones;
- recompensas;
- lecciones obligatorias;
- el puñetazo de área (`32`…) y Recuperar (`321`), que siguen en la ayuda de combos.

## 2. Dónde vive

- **Mapa:** una parada nueva, «Campo de entrenamiento» / «Training ground», dibujada a la izquierda de Bree sobre su terreno. Es un marcador de la pantalla del mapa, **no una zona de `Route`**, así que la historia, el guardado y el simulador no cambian.
- **Partida nueva:** si el tutorial no se ha completado ni se ha saltado, el mapa se abre centrado en la parada, con una tarjeta: «Aprende a combatir en 5 lecciones» y los botones «Empezar entrenamiento» y «Saltar».
- **Tocar la parada:** abre la **lista de lecciones** (ruta `tutorial`), con ✔ en las completadas. Se puede jugar cualquiera, en cualquier orden.
- **Progreso:** se guarda en los ajustes (DataStore), no en la partida:
  - `tutorial_done`: un conjunto con los ids de las lecciones completadas;
  - `tutorial_dismissed`: sí o no.

## 3. Lecciones

| id | Lección | Pasos (combos pedidos, en orden) | Rivales |
|---|---|---|---|
| `attack` | Ataque | `11`, `12`, `121`, `1212`, `12121` | 1 muñeco |
| `defense` | Defensa | `2132`, `22`, `21`, `212`, `2121`, `21212` | 1 muñeco que golpea |
| `daze` | Aturdimiento | `2131`, `33`, `31`, `313`, `3131`, `31313` | 1 muñeco que intenta aturdir |
| `drain` | Robo de vida | `22`, `3123`, `31232` | 3 muñecos |
| `finisher` | Remate | `11`, `123`, `13`, `131`, `1313`, `13131` | 1 muñeco |

- **Texto de cada lección:** una introducción y, para cada paso, una explicación corta del combo:
  - qué hace: por ejemplo, Furia da +20 % de daño de lanza, Guardia −20 % de daño recibido, Escudo maestro −50 %, Escudo táctico inmunidad al Daze y Brío +20 % de regeneración de power;
  - cómo se marca;
  - si encadena con el anterior.

  La lección 1 explica además qué es un combo: teclas, ⚔ para lanzar, coste de power, ticks y efectos que duran varios ticks, y que encadenar los tiers en orden da bonus mientras que repetir o cambiar de familia rompe la cadena. La lección 3 explica la estrella del Daze.
- **Final:** «¡Lección completada!», con «Siguiente lección» o «Volver al mapa». Tras la quinta: «¡Listo para Bree!».

## 4. Motor (`:engine`)

- **Datos:** `engine/src/main/resources/tutorial/lessons.json` con las lecciones del §3 (id, rivales y pasos). Los textos no van en el JSON: los pone la app desde `strings.xml`, con claves derivadas del id y del índice del paso.
- **`TutorialLesson`** (id, rivales y pasos como lista de secuencias) y **`TutorialProgress`:**
  - función pura `fun advance(progress, events: List<CombatEvent>): TutorialProgress`;
  - avanza un paso cuando hay un `ComboCast` del jugador con la secuencia pedida;
  - si el combo es otro, cuenta un fallo, que la app usa para mostrar una pista;
  - la lección se completa al terminar el último paso.
- **No se puede perder:** `CombatSetup` gana `hpFloorPct: Int? = null`. Si tiene valor, `CombatEngine` no deja que la vida de **ningún** luchador baje de ese porcentaje de su máximo, así que el combate nunca termina por muerte. El tutorial usa un 10 %. Los combates normales no cambian (`null`).
- **Rivales de entrenamiento:** «Muñeco de entrenamiento» / «Training dummy», retrato `dummy`, nivel 1, con un patrón propio por lección:
  - golpes flojos en `defense` y `drain`;
  - algún `31` en `daze`;
  - casi inofensivo en `attack` y `finisher`.

  El Warden lucha a nivel 1. Las reglas de contenido de la historia (`ContentRules`) no se aplican al tutorial.

## 5. App

- **`TutorialCombatSource`:** una nueva fuente de combate, como la de práctica, que no informa a la sesión ni toca la partida.
- **Panel del tutorial** sobre las teclas:
  - el título de la lección y el paso actual («Paso 2 de 5»);
  - el texto del paso;
  - el combo pedido como fichas de teclas (`1` `2` `⚔`, con los colores de los botones).

  Al acertar sale un ✔ breve y se pasa al siguiente paso. Al fallar sale la pista «Ese no: marca 1 2 y lanza ⚔».
- **Pausa al leer:** cada texto nuevo pausa el combate hasta pulsar «Entendido».
- **Lista de lecciones:** pantalla `tutorial` con las 5 lecciones y su ✔. El botón «Volver» lleva al mapa.
- **Retrato `dummy`:**
  - sprite 16×16 por código en `Portraits.kt`;
  - prompt nuevo en `docs/content/prompts.md` (sección Retratos: muñeco de entrenamiento de paja y madera con escudo, pixel art);
  - entra en `imagenes-pendientes.md` para que el usuario lo genere en Gemini.
- **Textos:** en es y en (`strings.xml`), y en tengwar por `tr`/`trText`.
- **Sin anuncios.** La música se calla, como en todo combate.

## 6. Pruebas

- **Motor:**
  - `lessons.json` carga las 5 lecciones con los pasos exactos del §3;
  - `TutorialProgress` avanza con el combo pedido, cuenta los fallos sin avanzar y completa la lección en el último paso;
  - con `hpFloorPct = 10`, ni el jugador ni los rivales bajan del 10 % y el combate no termina por muerte;
  - sin `hpFloorPct`, los combates normales siguen igual (los tests existentes, en verde).
- **App:**
  - el panel muestra el texto y las teclas del paso actual, se pausa hasta «Entendido» y avanza al lanzar el combo correcto;
  - un combo incorrecto muestra la pista;
  - al completar, aparece «Siguiente lección»;
  - el progreso se guarda;
  - la tarjeta de partida nueva aparece en el mapa y «Saltar» la oculta;
  - el tutorial no cambia la partida;
  - en inglés y en tengwar.
- **Verificación:**
  - `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`;
  - en el emulador, las 5 lecciones completas, con capturas en `docs/screenshots/v3/tutorial-*.png`.
