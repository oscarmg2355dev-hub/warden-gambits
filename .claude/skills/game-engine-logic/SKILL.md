---
name: game-engine-logic
description: Use when designing or changing the combat logic engine (the pure-Kotlin :engine module) - game state, actions, turn/timing resolution, damage, status effects, AI opponents, RNG, or anything that decides the outcome of a fight.
---

# Motor de lógica de combate

El juego no tiene motor gráfico: **el motor de lógica ES el juego**. La UI solo lo dibuja y le pasa entradas.

## Principios no negociables

1. **Kotlin puro**: el módulo `:engine` no importa nada de `android.*`, `androidx.*` ni Compose. Es una librería JVM (`kotlin("jvm")`). Si necesitas algo de Android, define una interfaz en `:engine` e impleméntala en `:app`.
2. **Determinista**: misma semilla + mismas entradas ⇒ mismo resultado, siempre. Prohibido `Random()` sin semilla, `System.currentTimeMillis()`, `Math.random()`, iterar `HashMap`/`HashSet` cuando el orden afecte al resultado.
   - RNG: interfaz propia (`GameRandom`) con implementación sembrada; la semilla forma parte del estado.
   - Tiempo: si hay ventanas de tiempo, el motor recibe el instante como dato (`Long` de ms o ticks lógicos) en la entrada; nunca lee el reloj.
3. **Estado inmutable + reductor**: `data class` inmutables y una función pura
   `fun reduce(state: CombatState, action: Action): StepResult` que devuelve el nuevo estado y la lista de `GameEvent` ocurridos (golpe, combo completado, KO…). La UI anima/reproduce los eventos; el estado es la verdad.
4. **Datos, no código**: personajes, movimientos y combos se definen como datos (data classes / JSON en recursos), no con `if` por personaje. Añadir un movimiento no debería requerir tocar el reductor.
5. **Sin efectos colaterales** en el motor: nada de logging a consola, IO ni singletons mutables.

## Estructura orientativa del módulo

```
engine/src/main/kotlin/<pkg>/engine/
  model/      CombatState, Fighter, Stats, Move, StatusEffect, Phase
  action/     Action (sealed interface): entradas del jugador/IA y eventos de sistema
  event/      GameEvent (sealed interface): lo que ha pasado, para la UI
  rules/      reduce(), cálculo de daño, resolución de combos, efectos
  combo/      ver skill `combo-system`
  ai/         Opponent strategies (funciones puras state -> Action)
  random/     GameRandom + implementación sembrada
  data/       definiciones de personajes/movimientos (o carga desde JSON)
```

## Modelado

- Usa `sealed interface` para `Action`, `GameEvent`, fases (`Phase`) — así `when` es exhaustivo y el compilador avisa al añadir casos.
- `value class` para IDs y magnitudes (`MoveId`, `Hp`, `Damage`) cuando evite confundir enteros.
- Valores numéricos: enteros (o punto fijo) para HP/daño; evita `Double` en reglas para no tener sorpresas de redondeo en balance y tests.
- Validación: acciones ilegales en el estado actual devuelven el mismo estado + un `GameEvent.Rejected(reason)`; nunca lanzan excepciones en flujo normal.

## TDD obligatorio

Cada regla nueva entra con test primero (skill `test-driven-development`). Patrones de test:

- **Escenario**: estado inicial construido con builders de test → lista de acciones → assert del estado final y de los eventos emitidos.
- **Determinismo**: ejecutar la misma partida dos veces con la misma semilla y comparar igualdad completa.
- **Propiedades** (cuando aplique): HP nunca < 0, el combate siempre termina en N acciones, etc.
- Tests en `engine/src/test/kotlin`, JUnit 5 + kotlin.test (o Kotest si se decide). Se ejecutan con `./gradlew :engine:test` (sin emulador).

## Integración con la app

`:app` envuelve el motor en un `ViewModel` que mantiene `StateFlow<CombatState>` y un `SharedFlow<GameEvent>`; ver skill `compose-game-ui`.
