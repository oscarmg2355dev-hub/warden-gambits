# Combate v2 — Diseño

Sustituye el sistema de combos del motor de combate (spec `2026-10-08-combat-engine-design.md` §3.3, §3.4, §4, §5.1) por el sistema de tiers y cadenas que propuso el usuario tras las pruebas del 2026-10-12. Suposiciones B1–B10 aprobadas por el usuario.

Orden acordado: **B (este documento) → A (interfaz v2)**.

## 1. Alcance

**Dentro:**
- `:engine`:
  - catálogo de combos;
  - tipos de efecto;
  - cadena de combos por luchador;
  - resolución de efectos por tick (DoT, CoT, robo de vida, tirada de Daze);
  - multiplicadores y escudos excluyentes;
  - costes.
- Bestiario y patrones de IA de los rivales (contenido JSON + spec de contenido §3 + `ContentRules`).
- `:sim`: política del jugador simulado con las nuevas reglas, y re-balance para volver al objetivo "Exigente".
- Adaptación **mínima** de `:app` para que compile y funcione con el nuevo catálogo: insignias de efectos, `ComboDescriber`, etiqueta del combo. El rediseño visual es la spec A.

**Fuera:** cambios visuales (spec A), contenido narrativo (solo cambian los rivales), nuevas mecánicas no listadas aquí.

**Se mantiene igual:**
- ticks de 1 s;
- buffer de hasta 5 inputs + `Execute`;
- un combo en cola por luchador, resuelto en el siguiente tick (primero el jugador, después los rivales);
- objetivo seleccionado;
- regeneración de power;
- derrota y victoria;
- determinismo (RNG sembrado);
- `LevelStats` y `RivalScaling` (los valores se reajustan).

## 2. Glosario y valores base

Todos los valores son enteros; los porcentajes se expresan como Int (100 = ×1).

| Símbolo | Definición |
|---|---|
| `SPDMG` | `Ataque × SPEAR_BASE_PCT / 100`. Inicial `SPEAR_BASE_PCT = 20`. |
| `FIDMG` | `Ataque × FIST_BASE_PCT / 100` por objetivo. Inicial `FIST_BASE_PCT = 12`. |
| `CUR` | `Cura × CURE_BASE_PCT / 100`. Inicial `CURE_BASE_PCT = 25`. |

Los tres porcentajes viven en un objeto de datos `ComboTuning`, que el simulador puede barrer. Los valores finales los fija el re-balance (§9).

## 3. Familias, tiers y catálogo

Cada `ComboDefinition` tiene una familia (`ComboFamily`), un tier (1–4, o 0 si no tiene tiers) y su coste. Hay 29 combos (28 del diseño original más `321` Recuperar, añadido el 2026-10-12).

| Familia | Secuencias (tier 1→4) | Efecto por tier n | Duración base | Bonus de cadena (solo si viene encadenado) | Coste n=1..4 |
|---|---|---|---|---|---|
| `SPEAR_A` (Lanza) | 12, 121, 1212, 12121 | DoT sobre el objetivo: `n × SPDMG` por tick | 10 ticks | +5 / +10 / +20 ticks (tiers 2/3/4) | 10/18/26/34 |
| `SPEAR_B` (Lanza cruzada) | 13, 131, 1313, 13131 | igual que SPEAR_A, efecto **independiente** (se acumula con el de SPEAR_A) | 10 | +5 / +10 / +20 | 10/18/26/34 |
| `DAZE` (Aturdir) | 31, 313, 3131, 31313 | Efecto "probabilidad de Daze" sobre el objetivo: +5 % | 10 | tier 3: +5 %; tier 4: +10 % y +20 % (+30 %) | 10/18/26/34 |
| `AREA` (Puño en área) | 32, 323, 3232, 32323 | DoT sobre **todos** los oponentes vivos: `n × FIDMG` por tick a cada uno | 10 | +5 / +10 / +20 ticks | 15/24/33/42 |
| `HEAL` (Cura) | 21, 212, 2121, 21212 | CoT sobre uno mismo: `n × CUR` por tick | 10 | +5 / +10 / +20 ticks | 10/18/26/34 |
| `DRAIN` (Robo de vida) | 3123, 31232 | Cada 5 ticks durante 30 ticks: quita `CUR` (×2 en el tier 2 si viene encadenado tras 3123) a cada oponente vivo y cura al lanzador lo mismo por cada uno | 30 (6 pulsos) | tier 2: CUR×2 | 20/30 |
| `MULTIPLIER` | 11 Furia, 22 Guardia, 33 Brío | 11: +20 % a SPDMG (solo combos de lanza: SPEAR_A, SPEAR_B y 123). 22: −20 % al daño recibido. 33: +20 % a la regeneración de power | 30 | — | 15 |
| `SHIELD` | 2132 Escudo maestro, 2131 Escudo táctico | 2132: −50 % al daño recibido. 2131: inmunidad total al Daze (`ComboTuning.tacticsDazePct` = 0, decisión del usuario del 2026-10-17 según `descripcion.md` «impide entrar en modo daze»): mientras está activo no hay tirada y Aturdir no le deja probabilidad, así que al caducar no queda nada acumulado. Entre el 2026-10-16 y el 2026-10-17 multiplicaba la probabilidad ×0,5 | 60 | — | 20 |
| `DIRECT` | 123 Golpe directo | Golpe inmediato al objetivo: `max(1, 10 × SPDMG − Defensa)`, con las reducciones del objetivo | — | — | 30 |
| `RECOVER` | 321 Recuperar | Sobre uno mismo, al instante: recupera el 10 % del power máximo (30 con 300), sin pasar del máximo. Sin efecto duradero (sin insignia). Sin tiers: rompe la cadena como cualquier combo sin tiers | — | — | 0 |

**Nombres en español** (para la UI y la ayuda):
- tiers: "Lanza I–IV", "Lanza cruzada I–IV", "Aturdir I–IV", "Puño en área I–IV", "Cura I–IV", "Robo de vida I–II";
- combos sueltos: "Furia", "Guardia", "Brío", "Escudo maestro", "Escudo táctico", "Golpe directo", "Recuperar".

**Desaparecen:**
- el Miedo (`FEARED`);
- los antiguos Pot/Def/Pow/ShM/ShT con su semántica anterior;
- el antiguo `23…` (DoE);
- "Recupera power" (`31232` es ahora Robo de vida II);
- el daño inmediato de todos los combos excepto `123`.

## 4. Cadena de combos

Cada luchador tiene una `ComboChain(family: ComboFamily?, tier: Int, full: Boolean)`. Al **resolverse con éxito** el combo C (familia f, tier t):

- **Avanza** si `t ≥ 2`, `chain.family == f`, `chain.tier == t − 1` y la cadena no está rota. C se aplica **encadenado** (con bonus) y la cadena pasa a `(f, t)`. `full` = la cadena empezó en el tier 1 y no se ha roto; con la regla de avance siempre es así.
- **Tier 4 repetido:** si `t == 4`, `chain == (f, 4, full = true)` y C es el mismo tier 4, se aplica encadenado con todos los bonus y la cadena sigue en `(f, 4, full)`.
- **Inicio de cadena:** si `t == 1`, C se aplica sin bonus (el tier 1 no tiene bonus) y la cadena pasa a `(f, 1)`.
- **Ruptura:** en cualquier otro caso, C se aplica **sin bonus** y la cadena queda rota (`family = null`), así que hay que volver a empezar por el tier 1. Esto incluye:
  - repetir un tier distinto del 4;
  - saltarse un tier;
  - lanzar cualquier combo de otra familia: multiplicadores, escudos, `123`, curas…;
  - lanzar un combo sin tiers.
- Los combos **rechazados** (falta de power, Dazed, objetivo inválido) no tocan la cadena.

**Bonus de cadena:**
- duración: +5 ticks en el tier 2, +10 en el tier 3 y +20 en el tier 4, aplicados a la duración del efecto creado;
- Daze: probabilidad extra del tier 3 (+5 %) y del tier 4 (+30 %);
- Robo de vida II: CUR×2.

## 5. Efectos

`ActiveEffect(kind, sourceId, sourceCombo: String, magnitude, remainingTicks, activeFrom)`. Los tipos de efecto (`EffectKind`) son:

| Tipo | Uso |
|---|---|
| `DOT` | daño por tick |
| `COT` | cura por tick |
| `DAZE_CHANCE` | probabilidad de Daze (magnitud = %) |
| `DAZED` | aturdido |
| `FURY`, `GUARD`, `VIGOR` | multiplicadores 11, 22 y 33 |
| `SHIELD_MASTER`, `SHIELD_TACTICS` | escudos 2132 y 2131 |
| `DRAIN` | robo de vida (en el lanzador; magnitud = CUR por pulso; lanzado en el tick c, pulsa en c+5, c+10, …, c+30: 6 pulsos) |

- **Identidad de un efecto:** `(objetivo, sourceId, sourceCombo)`. Si se relanza el mismo combo sobre el mismo objetivo, el efecto existente **se reinicia**: nueva duración y nueva magnitud. No se duplica.
  - Combos distintos (por ejemplo `12` y `121`, o `12` y `13`) crean efectos distintos que se acumulan.
- **Exclusión:** al aplicar un multiplicador se eliminan los otros dos multiplicadores del luchador; al aplicar un escudo se elimina el otro escudo.
- **Activación:**
  - `DOT`, `COT` y `DAZE_CHANCE` actúan desde el tick en que se aplican, igual que hoy los DoT.
  - Multiplicadores, escudos, `DAZED` y `DRAIN` están activos desde el tick t+1 hasta el t+N (regla §8 de la spec de historia).
  - Al renovarse un efecto ya activo, no se retrasa su activación.
- `DAZE_CHANCE` y `DAZED` no se aplican a un objetivo con `SHIELD_TACTICS` activo.
- `DAZE_CHANCE` tampoco se aplica a un objetivo que ya tiene `DAZED` (activo o pendiente): mientras dura el aturdimiento no se acumula probabilidad, así que no hay bucle de Daze al terminar (2026-10-13).

## 6. Resolución de un tick (nuevo orden)

0. **IA** de los rivales: igual que ahora, con una **poción** previa (2026-10-13): si a un rival vivo, sin combo en cola y no `DAZED` le toca lanzar este tick y no puede pagar el combo que le toca de su patrón, bebe una poción: `power += 50 % del power máximo` (sin pasar del máximo; usos ilimitados) y se emite `PotionUsed(rival, cantidad)`. Después decide como siempre. El jugador no bebe pociones.
1. **Lanzamientos:**
   - Se procesa el jugador y luego los rivales.
   - Un combo se rechaza si el luchador está `DAZED`, si no tiene power suficiente o si el objetivo no es válido.
   - Si no se rechaza: se paga el coste, se actualiza la cadena (§4), se aplica el efecto y se emite `ComboResolved(combo, chained: Boolean)`.
   - `123` hace su daño en este paso.
   - **Desaparece la regla de que Fear bloquea los combos ofensivos.**
2. **Efectos por tiempo**, para cada luchador vivo en orden de índice:
   - `delta = Σ COT − Σ daño de los DOT`.
   - Cada DOT se aplica con la reducción de **Guardia** (−20 %) y de **Escudo maestro** (−50 %) del receptor, que se multiplican entre sí, y con un mínimo de 1 por DOT. Ignora la Defensa, como ahora.
   - Se aplican las pulsaciones de `DRAIN` de cualquier luchador cuyo pulso caiga en este tick (cada 5 ticks desde su activación).
   - Se emiten `EffectTicked` y `FighterDefeated`, como ahora.
3. **Tirada de Daze**, para cada luchador vivo y no `DAZED`, por orden de índice:
   - `chance = min(55, Σ DAZE_CHANCE activos)`.
   - Si `chance > 0`: `roll = random.nextInt(100)`. Si `roll < chance`, el luchador queda `DAZED` durante 10 ticks (activo desde el siguiente) y se eliminan todos sus `DAZE_CHANCE`.
   - Eventos: `EffectApplied(id, DAZED, 10)`; si no entra, no se emite nada.
4. **Caducidad:** se decrementan los efectos activos y se eliminan los que llegan a 0. Igual que ahora.
5. **Regeneración de power:** `regen × (Brío ? 120 : 100) / 100`.
6. **Fin del combate:** igual que ahora (empate = derrota).

## 7. Fórmulas de daño y cura

- **Magnitud de DoT al lanzar** (fijada en el efecto):
  - lanza: `n × SPDMG × (Furia ? 120 : 100) / 100`;
  - área: `n × FIDMG`.
- **Daño de DoT recibido por tick:** `max(1, magnitud × (Guardia ? 80 : 100) / 100 × (EscudoMaestro ? 50 : 100) / 100)`.
- **CoT por tick:** `n × CUR` (sin multiplicadores); se cura hasta el máximo de vida.
- **Golpe directo:** `max(1, 10 × SPDMG × (Furia ? 120 : 100) / 100 − Defensa)` con las mismas reducciones del receptor; mínimo 1.
- **Robo de vida por pulso:** `quitado = min(CUR_efectivo, vida del rival)` por rival vivo; el lanzador se cura `Σ quitado` hasta su máximo. No le afectan Defensa ni escudos (es magia de vida).

## 8. IA y bestiario

- `PatternBrain` no cambia: recorre su patrón y espera si no tiene power (con la poción de §6, paso 0, ya no le falta nunca cuando le toca).
- **Bestiario:** se reescriben los patrones de los 12 comunes y los 13 jefes con el catálogo nuevo, manteniendo su sabor:
  - lobos: lanza encadenada;
  - espías: Lanza cruzada encadenada y un Aturdir (2026-10-13; antes solo Aturdir);
  - todos los rivales, comunes y jefes (2026-10-13): al menos la mitad de sus combos hacen daño (Lanza, Lanza cruzada, Puño en área, Golpe directo o Robo de vida) y recorren al menos una cadena de daño real (tier n justo después del tier n−1);
  - jefes: además abren con daño y como mucho tienen una cura.
- Los patrones deberían seguir cadenas reales (`12, 121, 1212…`) para aprovechar los bonus.
- Se actualizan la spec de contenido §3, `ContentRules.Bestiary` y los JSON. Las reglas §5 (offsets, número de combates por camino) no cambian.

## 9. Re-balance

- Se usa el `:sim` existente.
- La política del jugador simulado se reescribe para el combate v2:
  - encadena tiers;
  - mantiene un DoT de lanza activo;
  - usa Aturdir contra jefes;
  - se cura con `21…` por debajo del 40 %;
  - usa Guardia o Escudo maestro contra jefes y área contra grupos.
- La cadencia humana sigue igual (Novato cada 2 ticks, Medio 1,5, Experto 1).
- **Se barren:** `ComboTuning` (los tres %), `RivalScaling.DEFAULT` / `BOSS` y, si hace falta, los offsets de jefe dentro de +2..+8.
- **Objetivos "Exigente"** (sin cambios):
  - aventura completa: Novato 2–10 %, Medio 20–30 %, Experto 50–70 %;
  - el Medio gana ≥ 95 % de cada combate común;
  - cada jefe en el rango 75–98 %;
  - casi ningún combate llega al tope de 600 ticks.
- Se documenta en `docs/balance/tuning.md` ("Combate v2") con la tabla antes/después.
- **Dificultad base subida (2026-10-12, decisión del usuario):** los objetivos pasan a ser aventura completa Medio 10–15 % y Experto 30–40 % (Novato sin objetivo: se informa), el Medio gana ≥ 93 % de cada combate común, cada jefe Medio entre el 65 y el 92 % (salvo bree, tutorial) y casi ningún combate al tope. La política simulada Medio/Experto lanza `321` (Recuperar) cuando no puede pagar el siguiente combo de su plan, fuera de peligro (vida ≥ 40 %) y sin romper una cadena cuyo siguiente tier llegue en 3 ticks o menos; el Novato no lo usa. Detalle en `docs/balance/tuning.md`, «Dificultad base subida (+ combo 321)».

## 10. Adaptación mínima de `:app`

- `CombatViewModel` mapea los nuevos `EffectKind` a insignias en español:

  | Efecto | Insignia |
  |---|---|
  | DOT | DoT |
  | COT | CoT |
  | DAZE_CHANCE | Daze X% |
  | DAZED | Aturdido |
  | FURY | Furia |
  | GUARD | Guardia |
  | VIGOR | Brío |
  | SHIELD_MASTER | Esc. maestro |
  | SHIELD_TACTICS | Esc. táctico |
  | DRAIN | Robo |

- Expone el nombre en español del combo que coincide con el buffer y si la cadena está activa (familia/tier) para la UI.
- `ComboDescriber` se reescribe con descripciones generadas desde los datos, por ejemplo:
  - "Lanza II · 2× daño de lanza por tick durante 10 ticks (+5 si encadenas Lanza I)";
  - "Golpe directo · 10× daño de lanza de una vez".
- El rediseño visual va en la spec A.

## 11. Testing (TDD en `:engine`)

- **Catálogo:** 29 combos, secuencias únicas, familia/tier/coste según la tabla de §3.
- **Cadena:**
  - avanza en orden 1→2→3→4;
  - la repetición, el salto o un combo de otra familia la rompen;
  - el tier 4 repetido mantiene el bonus;
  - un combo rechazado no la toca.
- **Efectos:**
  - duración con y sin bonus;
  - relanzar el mismo combo reinicia el efecto;
  - combos distintos se acumulan;
  - exclusión de multiplicadores y de escudos;
  - activación diferida de buffs y escudos.
- **Daze:**
  - suma con tope del 55 %;
  - tirada controlada con `FixedRandom`;
  - al entrar en Daze se borran los `DAZE_CHANCE` y el luchador queda bloqueado 10 ticks;
  - Escudo táctico multiplica la probabilidad de la tirada por `tacticsDazePct`; con 0 (el valor por defecto) da inmunidad: sin tirada y sin probabilidad nueva de Aturdir mientras está activo.
- **Robo de vida:** 6 pulsos, cada 5 ticks; II encadenado = ×2; topes de vida.
- **Fórmulas:** Furia solo sobre lanza; Guardia y Escudo maestro se multiplican; mínimo 1; `123` con Defensa.
- **Determinismo y terminación:** se mantienen los tests de simulación existentes, adaptados.
- **Contenido:** el bestiario nuevo con su test de reglas, y `RealContentTest` en verde.
- **App:** se actualizan los tests de `ComboDescriber`, ViewModel y pantallas afectados.
