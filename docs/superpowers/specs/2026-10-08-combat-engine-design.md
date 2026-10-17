# Motor de combate — Diseño

Subproyecto 1 de 4 (combate → historia → app → contenido). Fuente: `descripcion.md`.

## 1. Objetivo y alcance

Librería Kotlin/JVM pura (`:engine`) que simula un combate de Warden-Gambits: un jugador contra 1–4 rivales, en tiempo real discretizado en **ticks de 1 segundo**, con combos formados por los inputs 1-2-3 (SPEAR-SHIELD-FIST) y lanzados con el botón **Acción**.

**Dentro:** stats derivados del nivel, catálogo de combos, buffer de inputs, cola de ejecución, resolución por tick, efectos temporales, estados Daze/Fear, regeneración de power, IA de rivales por patrones (con interfaz enchufable), resultado del combate.

**Fuera (otros subproyectos):** reloj real (la app llama a `tick` cada segundo), mapa/nodos/historia, persistencia, experiencia acumulada entre combates (el motor solo expone "nivel → stats"), UI, patrones concretos de IA por rival (se describirán más adelante; aquí solo un patrón de ejemplo).

**Criterio de éxito:** un combate completo se puede jugar desde tests con acciones (`Input`, `Execute`, `SelectTarget`, `Tick`) y siempre termina; misma semilla + mismas acciones ⇒ mismo resultado.

## 2. Arquitectura

```
:engine (kotlin("jvm"), sin Android)
  model/    Fighter, Stats (incluye LevelStats), CombatState, Side, Outcome, ActiveEffect, EffectKind (Daze/Fear son valores de EffectKind: DAZED, FEARED; no hay StatusKind aparte)
  combo/    Input(1,2,3), ComboId, ComboDefinition, ComboCatalog
  action/   CombatAction (sealed): Press, Execute, ClearBuffer, SelectTarget, Tick
  event/    CombatEvent (sealed): ComboQueued, ComboRejected, ComboResolved, DamageDealt,
            Healed, EffectApplied, StatusResisted, EffectTicked, EffectExpired,
            FighterDefeated, CombatEnded
  rules/    CombatEngine.reduce(), TickResolver, ComboApplier, DamageFormula
  ai/       RivalBrain (interfaz), PatternBrain (ejemplo)
  random/   GameRandom (interfaz), SeededRandom (xorshift/SplitMix64 propio)
```

API pública única:

```kotlin
fun CombatEngine.start(setup: CombatSetup, seed: Long): CombatState
fun CombatEngine.reduce(state: CombatState, action: CombatAction): StepResult
data class StepResult(val state: CombatState, val events: List<CombatEvent>)
```

- `CombatState` es inmutable e incluye la semilla/estado del RNG y el número de tick.
- Acciones inválidas → mismo estado + `ComboRejected(reason)`; nunca excepciones en flujo normal.
- Todo en enteros. Porcentajes en enteros (`150` = ×1.5). Redondeo: división entera hacia abajo.

## 3. Modelo

### 3.1 Stats (iguales para todas las entidades)

| Stat | Tipo | Notas |
|---|---|---|
| Nivel | Int ≥ 1 | Determina los stats base |
| Vida | actual / máx | 0 ⇒ derrotado. "Moral" = Vida |
| Power | actual / máx | Se gasta al lanzar combos; se regenera cada tick |
| Ataque | Int | Base de todo el daño |
| Cura | Int | Base de toda la curación |
| Defensa | Int | Resta plana al daño directo |
| Escudo físico (ShM) | efecto temporal | −50% del daño recibido |
| Escudo táctico (ShT) | efecto temporal | −50% de probabilidad de entrar en Daze/Fear |
| Dazed / Feared | estado temporal | ver 3.4 |

### 3.2 Stats por nivel (valores iniciales, ajustables)

| Stat | Fórmula (L = nivel) | L1 | L5 | L10 |
|---|---|---|---|---|
| Vida máx | (100 + 20·(L−1)) × 100 | 10000 | 18000 | 28000 |
| Power máx | 300 | 300 | 300 | 300 |
| Regen. power/tick | 5 | 5 | 5 | 5 |
| Ataque | (10 + 2·(L−1)) × 100 | 1000 | 1800 | 2800 |
| Cura | (8 + 2·(L−1)) × 100 | 800 | 1600 | 2600 |
| Defensa | (2 + (L−1)) × 100 | 200 | 600 | 1100 |

Desde el 2026-10-12 la vida, el ataque, la cura y la defensa se multiplican por `LevelStats.STAT_SCALE = 100` (todo el daño y las curas escalan igual) y el power máximo es `LevelStats.MAX_POWER = 300` (antes 100); la regeneración y los costes de los combos no cambian. Ver «Power ×3 y escala ×100» en `docs/balance/tuning.md`.

Al empezar el combate todas las entidades tienen vida y power al máximo.

### 3.3 Efectos temporales

`ActiveEffect(kind, sourceId, magnitude, remainingTicks, activeFrom)`:

| Kind | Efecto por tick / mientras dure | Acumulación |
|---|---|---|
| DoT | resta `magnitude` de vida cada tick | **se acumula** (cada lanzamiento es una instancia) |
| CuT | suma `magnitude` de vida cada tick (hasta máx) | se acumula |
| Pot | +30% daño infligido | refresca la duración sin acortarla |
| Def | +50% defensa | refresca sin acortar |
| Pow | regen. de power ×2 | refresca sin acortar |
| ShM | −50% daño recibido | refresca sin acortar |
| ShT | −50% prob. Daze/Fear recibida | refresca sin acortar |

**Retardo de activación.** DoT y CuT tienen `activeFrom` = tick actual (hacen tick en ese mismo tick). Los demás efectos (Pot, Def, Pow, ShM, ShT, Dazed, Feared) se crean con `activeFrom` = tick actual + 1: no tienen efecto ni decrementan `remainingTicks` hasta ese tick, así que duran exactamente `ticks` ticks completos. Refrescar un efecto existente conserva su `activeFrom`.

### 3.4 Daze y Fear (decisión de diseño — revisar)

- **Dazed** (dormido): no puede lanzar **ningún** combo.
- **Feared** (miedo): no puede lanzar combos **de daño** (Dam, AoE, DoE), sí curas, buffs, escudos y Rpo.
- Puede seguir pulsando inputs; el combo se rechaza al resolver. Al refrescar un efecto ya activo en el tick t, queda activo hasta t+N (sin acortar nunca la duración restante); el inicio de actividad no se retrasa. No se rompen al recibir daño.

## 4. Combos

### 4.1 Mecánica de input

1. `Press(fighter, input)` añade 1/2/3 al **buffer** (máx 5). Un 6º input se ignora (`ComboRejected(BufferFull)`).
2. `Execute(fighter)` busca la secuencia **exacta** del buffer en el catálogo:
   - existe → pasa a la **cola** (`queued`), el buffer se vacía, `ComboQueued`.
   - no existe → buffer se vacía, `ComboRejected(UnknownSequence)`, no gasta power.
   - ya había un combo en cola → lo sustituye.
3. `ClearBuffer(fighter)` vacía el buffer.
4. El combo en cola se resuelve en el **siguiente tick**. Máximo un combo por entidad por tick.

Como el lanzamiento es explícito, no hay ambigüedad entre `31` y `313`.

### 4.2 Catálogo (valores iniciales, ajustables)

Notación: `A` = Ataque, `C` = Cura del lanzador. `Mul` multiplica el daño/cura inmediato y el del efecto por tick: ×1.5 (3 inputs), ×2.0 (4), ×2.5 (5). Los efectos por tick duran 3 ticks.

**Daño (objetivo seleccionado)**

| Seq | Efectos | Coste | Inmediato | DoT/tick ×3 | Extra |
|---|---|---|---|---|---|
| 12 | Dam+DoT | 10 | 100%A | 30%A | — |
| 121 | +Mul ×1.5 | 18 | 150%A | 45%A | — |
| 1212 | +Mul ×2.0 | 26 | 200%A | 60%A | — |
| 12121 | +Mul ×2.5 | 34 | 250%A | 75%A | — |
| 31 | Dam+DoT+Daz | 12 | 80%A | 20%A | Daze 25%, 2 ticks |
| 313 | +Mul ×1.5 | 20 | 120%A | 30%A | Daze 30%, 2 ticks |
| 3131 | +Mul ×2.0 | 28 | 160%A | 40%A | Daze 35%, 2 ticks |
| 31313 | +Mul ×2.5 | 36 | 200%A | 50%A | Daze 40%, 3 ticks |

**Área (todos los rivales del lanzador)**

| Seq | Efectos | Coste | Inmediato c/u | DoT/tick ×3 c/u | Extra c/u |
|---|---|---|---|---|---|
| 32 | Dam+DoT+AoE+Fea | 15 | 60%A | 20%A | Fear 20%, 2 ticks |
| 323 | +Mul ×1.5 | 24 | 90%A | 30%A | Fear 25%, 2 ticks |
| 3232 | +Mul ×2.0 | 33 | 120%A | 40%A | Fear 30%, 2 ticks |
| 32323 | +Mul ×2.5 | 42 | 150%A | 50%A | Fear 35%, 3 ticks |
| 23 | DoE | 15 | roba 40%A de vida a cada rival | — | se cura lo robado |
| 232 | +Mul ×1.5 | 24 | roba 60%A | — | ídem |
| 2323 | +Mul ×2.0 | 33 | roba 80%A | — | ídem |
| 23232 | +Mul ×2.5 | 42 | roba 100%A | — | ídem |

**Curación (sobre sí mismo)**

| Seq | Efectos | Coste | Inmediato | CuT/tick ×3 |
|---|---|---|---|---|
| 21 | Cur+CuT | 10 | 100%C | 30%C |
| 212 | +Mul ×1.5 | 18 | 150%C | 45%C |
| 2121 | +Mul ×2.0 | 26 | 200%C | 60%C |
| 21212 | +Mul ×2.5 | 34 | 250%C | 75%C |

(En `descripcion.md` pone "Cur + DoT" en curas; se interpreta como cura en el tiempo, CuT.)

**Utilidad (sobre sí mismo)**

| Seq | Efecto | Coste | Duración |
|---|---|---|---|
| 11 | Pot: +30% daño | 15 | 5 ticks |
| 22 | Def: +50% defensa | 15 | 5 ticks |
| 33 | Pow: regen. power ×2 | 5 | 5 ticks |
| 31232 | Rpo: +50 power inmediato | 10 | — |
| 2132 | ShM: −50% daño recibido | 20 | 4 ticks |
| 2131 | ShT: −50% prob. Daze/Fear | 15 | 4 ticks |

Las siglas "Cur: potenciador de curas" y "Mul" sueltos no tienen combo propio; no se implementan como efecto aparte. El catálogo es **dato** (`ComboCatalog`), con test de validación: no hay secuencias duplicadas, la longitud está entre 2 y 5 y los costes son mayores que 0.

## 5. Resolución de un tick

`Tick` avanza `tickNumber` y ejecuta, en este orden fijo:

1. **Lanzamientos.** Se procesa primero el jugador y después los rivales por índice. Para cada entidad viva con combo en cola:
   - bloqueado por Daze/Fear (solo si el estado está activo: `activeFrom` ≤ tick actual) → `ComboRejected(Dazed|Feared)`, no gasta power;
   - power < coste → `ComboRejected(NotEnoughPower)`;
   - si no, paga el power, aplica los efectos inmediatos y crea los efectos temporales (`ComboResolved` + eventos de detalle).
   Una entidad que llega a 0 de vida durante este paso queda derrotada y ya no lanza su combo. Al ser derrotada se eliminan todos sus efectos activos (no habrá más `EffectTicked`/`EffectExpired` para ella).
2. **Efectos activos.** Para cada entidad viva se aplican sus DoT y CuT (sumados), se decrementa `remainingTicks` de los efectos ya activos (`activeFrom` ≤ tick) y estados, y se eliminan los que llegan a 0 (`EffectExpired`). Los DoT y CuT creados en el paso 1 hacen tick en el paso 2 del **mismo** tick; los demás efectos se activan en el tick siguiente.
3. **Regeneración de power**: +5 por entidad viva (×2 con Pow), hasta el máximo.
4. **Fin de combate**: si el jugador tiene 0 de vida → `Defeat`; si no, y todos los rivales tienen 0 → `Victory`. Se emite `CombatEnded` y cualquier acción posterior se rechaza.

### 5.1 Fórmulas

```
dañoBase      = A × %combo × (Pot ? 130 : 100) / 100
defensaEf     = Defensa × (Def ? 150 : 100) / 100
dañoDirecto   = max(1, dañoBase − defensaEf)  × (ShM ? 50 : 100) / 100   (mínimo 1)
dañoDoT/tick  = A × %dot × (Pot ? 130 : 100) / 100, fijado al lanzar;
                al aplicarse:  × (ShM del receptor ? 50 : 100) / 100     (ignora Defensa, mínimo 1)
drain (DoE)   = igual que dañoDirecto por rival; el lanzador se cura la suma (hasta su máximo)
cura          = C × %combo / 100 (CuT fijado al lanzar), sin superar la vida máxima
probStatus    = %combo × (ShT del receptor ? 50 : 100) / 100 → tirada con GameRandom (0..99)
```

## 6. Objetivos

- El jugador tiene `targetId`. `SelectTarget(id)` solo acepta rivales vivos. Si el objetivo muere, pasa automáticamente al primer rival vivo por índice.
- Los rivales siempre tienen como objetivo al jugador.
- AoE y DoE afectan a todos los oponentes vivos del lanzador. Curas y utilidades se aplican sobre el propio lanzador.

## 7. IA de rivales

```kotlin
interface RivalBrain {
    fun decide(state: CombatState, self: FighterId, random: GameRandom): ComboId?
}
```

- Se consulta **antes** del paso 1 de cada tick para cada rival vivo que no tiene combo en cola. Si devuelve un combo, entra directamente en cola (la IA no pulsa inputs uno a uno).
- `CombatSetup` asigna un `RivalBrain` a cada rival. Implementación de ejemplo `PatternBrain(pattern: List<ComboId>, everyNTicks: Int)`: recorre el patrón en ciclo, lanzando un combo cada N ticks y esperando si no le llega el power.
- Los patrones concretos de cada rival se definen en otra spec, cuando los describas.

## 8. Errores y casos límite

- Acciones sobre entidades derrotadas o con el combate terminado → `ComboRejected`, el estado no cambia.
- `SelectTarget` inválido → rechazado.
- Vida y power se limitan siempre a `0..máx`.
- Victoria y derrota en el mismo tick (por ejemplo, por DoT) → **gana la derrota**.

## 9. Testing

- JUnit 5 + kotlin.test, `./gradlew :engine:test`, siguiendo TDD.
- Builders de test (`fighter(level=1) { hp = 50 }`, `combat(player, rivals)`).
- Tests por regla: buffer y Execute, cada familia de combo, fórmulas, acumular y refrescar efectos, Daze/Fear/ShT con RNG controlado (`FixedRandom`), orden del tick, retargeting, fin de combate.
- Test de catálogo: unicidad, longitudes y costes.
- Determinismo: la misma partida con la misma semilla se ejecuta dos veces y da estados iguales.
- Terminación: `PatternBrain` contra un jugador con un `PatternBrain` (spam de `12`) termina en < 500 ticks para niveles iguales del 1 al 10.

## 10. Scaffold incluido en este subproyecto

- Proyecto Gradle (wrapper, `settings.gradle.kts`, `gradle/libs.versions.toml`) con **solo** el módulo `:engine` (`kotlin("jvm")`, `jvmToolchain(21)`). `:app` se añade en el subproyecto 3.

## 11. Decisiones tomadas por mí (a revisar)

1. Fear bloquea solo los combos de daño; Daze los bloquea todos.
2. Los combos se resuelven en el siguiente tick, no al pulsar Acción.
3. El jugador resuelve antes que los rivales dentro del tick (ligera ventaja para el jugador).
4. DoT y CuT se acumulan; los buffs y escudos refrescan su duración.
5. Los DoT ignoran la Defensa pero no el escudo físico ShM.
6. La IA mete combos completos en la cola, sin simular la pulsación de inputs.
7. Todos los números de las secciones 3.2 y 4.2.
