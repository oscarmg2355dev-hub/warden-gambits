# Niveles de dificultad — diseño

Fecha: 2026-10-24. Estado: aprobado en conversación; pendiente de revisar esta spec.

Elecciones del usuario:
- **Cuatro dificultades en Opciones,** con nombres propios en vez de fácil/medio/difícil/extremo.
- **El juego actual es la más difícil** (×1). Las otras bajan solo el **daño de los rivales**, hasta ×0,5 en la más fácil.
- **Nombres, valores por defecto y crónica:** aceptada la propuesta (§2, §4 y §5).

## 1. Objetivo

Que cada jugador elija cuánto castigan los rivales, sin tocar combos, patrones ni el contenido. El balance actual queda intacto como modo más difícil.

Fuera de alcance:
- cambiar la vida, el power o los patrones de los rivales;
- dificultad distinta por zona;
- logros o recompensas por dificultad.

## 2. Las cuatro dificultades

De más fácil a más difícil:

| id | es | en | Ataque de los rivales |
|---|---|---|---|
| `SHIRE` | Paseo por la Comarca | A Stroll in the Shire | ×0,50 |
| `RANGER` | Senda del Montaraz | Ranger's Path | ×0,60 |
| `MARK` | Guardián de la Marca | Warden of the Mark | ×0,75 |
| `EYE` | El Ojo de Sauron | The Eye of Sauron | ×1,00 (el juego actual) |

En Opciones, cada una lleva una línea corta: «Los rivales hacen la mitad de daño», «… un 40 % menos», «… un 25 % menos» y «El reto tal como fue forjado».

**Por qué ×0,60 y ×0,75.** El simulador baja el ataque de jefes y comunes (barridos `sweep boss attack=…` y `sweep attack=…`, 2026-10-24). El porcentaje de victorias del jugador MEDIO contra los jefes, de media, sube de forma bastante pareja (unos 15 puntos por paso):

| Multiplicador | Jefes MEDIO (media) | Peor jefe MEDIO | MAESTRO sin muertes |
|---|---|---|---|
| ×1,00 | 31 % | 11 % (quebradas) | 0 % |
| ×0,85 | 38 % | 15 % | 9,6 % |
| ×0,75 | 46 % | 26 % | 42,2 % |
| ×0,67 | 55 % | 37 % | 70,8 % |
| ×0,60 | 65 % | 37 % | 86,4 % |
| ×0,50 | 79 % | 63 % (gorgoroth) | 92,0 % |

Los comunes ya están al 99,8–100 % para el MEDIO en todos los niveles. Aviso: el NOVATO del simulador sigue perdiendo casi siempre contra los jefes incluso a ×0,50 (0–28 %). Si las pruebas reales lo confirman, se puede bajar `SHIRE` más adelante cambiando solo su número.

## 3. Motor (`:engine`)

- **`enum class Difficulty(val rivalAttackPct: Int)`** con `SHIRE(50)`, `RANGER(60)`, `MARK(75)` y `EYE(100)`, en este orden (de más fácil a más difícil).
- **`CombatSetup`** gana `rivalAttackPct: Int = 100`. `CombatEngine.start` multiplica el ataque de cada rival, después de `RivalScaling`, por ese porcentaje, con un mínimo de 1. Todo el daño de los rivales sale de su ataque (lanza, puño, golpe directo y robo de vida), así que todo baja por igual. Las curas y los escudos de los rivales no cambian.
- **Sin cambios** con `rivalAttackPct = 100`: los combates de ahora dan exactamente lo mismo.
- **Tutorial:** sigue con 100, porque ya no se puede perder.
- **Crónica de vueltas:**
  - `RunState` gana `easiestDifficulty: Difficulty? = null`, la dificultad más fácil con la que se ha jugado un combate de la historia en esa vuelta;
  - pasa a `RunRecord.difficulty` al completar la vuelta;
  - el resultado de un combate que la app informa a la sesión lleva la dificultad usada;
  - en los guardados anteriores queda `null`, que no se muestra.

## 4. App

- **Ajustes:** clave de DataStore `difficulty` (el nombre del enum).
- **Valor por defecto,** cuando la clave no existe todavía:
  - `EYE` si ya hay una partida guardada, para que a los jugadores actuales no les cambie el juego;
  - `RANGER` en una instalación nueva.

  Ese valor se escribe la primera vez que se resuelve.
- **Opciones:** una sección «Dificultad» / «Difficulty» con las cuatro, de más fácil a más difícil, con la marcada resaltada y su línea corta. Se puede cambiar en cualquier momento y aplica desde el siguiente combate.
- **Dónde aplica:**
  - los combates de la historia;
  - el Modo combate;
  - el tutorial no.

  La fuente de combate pone `rivalAttackPct` en el `CombatSetup` al crearlo.
- **En combate:** la carta del rival ya muestra su ATQ, que sale reducido; no hace falta más indicador.
- **Crónica de vueltas:** cada vuelta completada muestra su dificultad (por ejemplo «Dificultad: Senda del Montaraz»), si la tiene.
- **Textos:** en es y en (`strings.xml`), y en tengwar por `tr`/`trText`.

## 5. Simulador (`:sim`)

- El informe normal (`./gradlew :sim:run`) sigue en `EYE`, el balance de referencia.
- Opción nueva `--difficulty <id>`: corre el mismo informe con el ataque de comunes y jefes multiplicado por esa dificultad.
- `docs/balance/tuning.md` gana una sección «Niveles de dificultad» con la tabla del §2.

## 6. Pruebas

- **Motor:**
  - con `rivalAttackPct = 50`, el ataque de un rival es la mitad del de 100 (mínimo 1), y lo es en comunes y en jefes;
  - el daño de un mismo combo de un rival baja en la misma proporción;
  - con 100, el resultado de un combate sembrado es idéntico al de antes;
  - el ataque del jugador no cambia;
  - `easiestDifficulty` guarda la más fácil de la vuelta y pasa a `RunRecord`.
- **App:**
  - el valor por defecto es `EYE` con partida guardada y `RANGER` sin ella;
  - el selector guarda la elección, en es, en y tengwar;
  - el combate de la historia y el del Modo combate usan la dificultad elegida;
  - el tutorial usa 100;
  - la crónica muestra la dificultad de cada vuelta.
- **Simulador:** `--difficulty SHIRE` da un ataque de rivales de la mitad.
- **Verificación:**
  - `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`;
  - en el emulador:
    - el selector de Opciones;
    - la carta del rival con el ATQ reducido en «Paseo por la Comarca»;
    - la crónica;
    - capturas en `docs/screenshots/v3/difficulty-*.png`.
