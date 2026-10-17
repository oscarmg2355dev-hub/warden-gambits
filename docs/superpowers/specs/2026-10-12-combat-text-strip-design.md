# Tira de números de combate — Diseño

Petición del usuario del 2026-10-12: cada efecto debe mostrar su propio valor, en lugar de una suma por tick, en una tira vertical por entidad donde los números aparecen, suben y desaparecen. El usuario aprobó este diseño.

## 1. Motor (`:engine`)

- **Nuevo evento** `CombatEvent.EffectResolved(target: FighterId, source: FighterId, sourceCombo: String, kind: EffectKind, amount: Int)`, con `amount` negativo para daño y positivo para cura. En el tick se emite:
  - uno por cada **DOT** activo del receptor, con el daño ya reducido por Guardia y Escudo maestro (mínimo 1);
  - uno por cada **COT**, con la cura **real** aplicada en orden de efectos y con tope de vida máxima (puede ser +0);
  - por cada **pulso de robo de vida**, uno por cada rival con lo que le quita (negativo) y uno para el lanzador con lo que se cura (positivo).
- **Golpe directo** (`123`): se emite `EffectResolved(kind = null → usar sourceCombo "123", amount = −daño)`. Para no tener un `kind` nulo, `kind` es `EffectKind?`, y es `null` solo para golpes directos.
- **Orden de los eventos** dentro del tick: el orden en que se aplican los efectos. Es determinista: luchadores por índice y efectos por orden de lista.
- `EffectTicked` (la suma neta) **desaparece** y se sustituye por estos eventos.
  - Hay que revisar los usos en `:app` y `:sim` y adaptarlos.
  - La vida resultante y `FighterDefeated` no cambian.
  - Si un efecto deja al luchador a 0, los efectos siguientes de ese mismo tick no se aplican; es el comportamiento actual de "muere y se limpian sus efectos".
- `DamageDealt` y `Healed` se mantienen si los usa algo; si quedan redundantes, se eliminan.
- **Nota de implementación:** la vida sigue calculándose con la suma neta del tick (como antes, para no cambiar ningún resultado; lo fija `HpRegressionTest`). Los valores mostrados se reparten así (`OverTimeReport`): si el luchador sobrevive, cada DOT muestra su daño reducido y los COT se reparten, en orden, la cura que realmente entró (todo suma el cambio de vida; un COT con tope puede dar +0); si muere, se recorren en orden y no se muestran los efectos posteriores al letal. `DamageDealt`, `Healed` y `EffectTicked` se han eliminado.

## 2. App: tira de números

- **Dónde:** cada card (rivales y Warden) tiene una **columna estrecha a la derecha**, de unos 44 dp en las de rival y 56 dp en la del Warden, dentro de su tamaño fijo, con recorte (`clipToBounds`).
- **Formato de cada entrada:** el valor y el código del combo de origen (el mismo de las insignias): "−4 SP2" en rojo y "+3 CU1" en verde. El golpe directo se muestra como "−18 GD"; si no hay código, se pone solo el valor.
- **Animación de cada entrada:**
  - aparece abajo;
  - sube hasta arriba de la tira en unos 1,5 s y se desvanece en el último tercio;
  - con el mismo tick (mismo lote de eventos), las entradas se escalonan unos 150 ms para no solaparse.
- **Límite:** como mucho 6 vivas por entidad; si llegan más, se elimina la más antigua.
- Sustituye a los números flotantes actuales (`FloatingNumbers`).
- Con el combate en pausa, la animación se congela. Al terminar el combate, las entradas que quedan siguen hasta desaparecer.

## 3. Tests

- **Motor:**
  - dos DOT y un COT dan tres `EffectResolved` con sus valores;
  - Guardia y Escudo maestro reducen cada DOT;
  - el COT con tope muestra la cura real (incluido +0);
  - el robo de vida emite eventos por rival y por el lanzador;
  - `123` emite su evento;
  - la vida final es igual que antes, sobre los mismos escenarios de los tests existentes;
  - el orden es determinista.
- **App:**
  - el mapeo evento → entrada de tira (texto, color, entidad);
  - el límite de 6;
  - el escalonado dentro del mismo lote;
  - con la tira en la card del rival correcto (Robolectric con reloj controlado, `mainClock`), la entrada existe y luego desaparece.
- **Simulador y balance:** sin cambio de números. `./gradlew :sim:run` debe dar los mismos porcentajes que antes; solo cambian los eventos, no las reglas.
