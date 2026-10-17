# Rivales que se curan y jugador simulado MAESTRO — diseño

> **Nota (2026-10-17):** los jefes ya no llevan `reaction`: pasan a `modes` (modo ataque y modo defensa, spec `2026-10-17-boss-modes-design.md`), con el escudo y la cadena de curas en su defensa. Lo que sigue sobre las reacciones de los jefes es historia; las reacciones de curandero de los comunes no cambian.

Fecha: 2026-10-16. Estado: aprobado en conversación.

Elecciones del usuario:
- opción A: combates más largos e igualados, con los porcentajes de victoria reajustados;
- un jugador simulado que juegue como él, «sobre todo los jefes»;
- en un grupo, solo uno de los rivales se cura.

## 1. Objetivo

El usuario gana «muy muy fácil». Conoce los combos y aprovecha a fondo su juego de curas y escudos, así que quiere «más guerra». Hoy los rivales casi no se curan: solo 11 de 96, con una Cura I metida en el patrón que se lanza aunque estén a tope de vida. Además, el balance se ajustó con jugadores simulados que no usan curas y escudos tan bien como el usuario.

Cambios:
1. **Jefes:** al bajar de umbrales de vida se escudan y lanzan la cadena completa de curas.
2. **Comunes humanoides:** uno por encuentro lanza la Cura IV al quedarse con poca vida.
3. **Jugador simulado `MAESTRO`:** juega como el usuario y se convierte en la referencia del balance, junto al MEDIO.
4. **Re-balance:** se reajusta la dificultad para que el MAESTRO tenga que pelear y el MEDIO siga en sus objetivos. Las curas y escudos del jugador solo se debilitan como último recurso.

Fuera de alcance: rivales que curan a sus aliados, IA nueva fuera de estas reacciones y cambios de UI. Las curas ya se ven en la tira de números flotantes.

## 2. Reacciones por vida (`:engine`)

**Datos.** `RivalContent` gana un campo opcional `reaction`:

```json
"reaction": { "atHpPct": [50, 25], "shield": "2132", "combos": ["21", "212", "2121", "21212"] }
```

- `atHpPct`: umbrales de vida, en %, en orden descendente.
- `shield`: opcional; se lanza antes de los `combos`.
- `combos`: la secuencia de la reacción.

`RivalSpec` y `PatternBrain` reciben esa reacción.

**Reglas de `PatternBrain`:**
- **Disparo.** Cuando le toca lanzar (respeta `everyNTicks`) y su vida está en o por debajo de un umbral todavía no usado, el rival marca ese umbral como usado y empieza la reacción: primero el `shield`, si lo tiene, y luego los `combos` en orden. Mientras dura la reacción, cada combo de la secuencia sustituye al siguiente del patrón. Al terminarla, retoma el patrón en el punto donde lo dejó.
- **Cadena de curas.** La cadena se resuelve con las reglas normales (`ComboChain`), así que se aplica el bonus de cadena. Para que no se rompa, durante la reacción el rival lanza en **cada tick** y no cada `everyNTicks`. Así la cadena de 4 curas dura 4 ticks, o 5 con el escudo.
- **Aturdir.** Si el rival queda aturdido (DAZED) durante la reacción, **se cancela lo que le quedaba**. El umbral sigue usado. Es la contra que se recompensa: aturdirlo cuando empieza a curarse.
- **Power.** Si no puede pagar el combo de la reacción, bebe poción como ahora (+50 %) y lo lanza.
- **Umbrales sin repetición.** Cada umbral se usa una vez por combate. Si un golpe lo baja de dos umbrales a la vez, solo se dispara uno: el más bajo, y el otro queda marcado como usado.
- **Estado.** Va en `Fighter`: umbrales usados, posición en la reacción y si está en reacción. Es inmutable y determinista, sin RNG nuevo.
- **Eventos.** Como ahora (`ComboCast`, `EffectApplied`, `EffectResolved`). No hace falta ningún evento nuevo.

## 3. Contenido (`engine/src/main/resources/story/*.json`)

- **Jefes (21).** Todos llevan `reaction` con la cadena completa `21, 212, 2121, 21212`. El diseño inicial era `atHpPct [50, 25]` con escudo para todos. En el re-balance (tarea 4, `docs/balance/tuning.md`, «Rivales que se curan y MAESTRO») cada jefe quedó con sus propios umbrales (`[50, 25]`, `[40]`, `[30]` o `[20]`) y con o sin escudo (tabla en la spec de contenido §3).
  - El escudo, si lo hay, es siempre `2132` (Escudo maestro). El `2131` (Escudo táctico) se descartó en el re-balance: protege del aturdimiento y debilitaría la contra de aturdirlo para cortarle las curas.
  - Excepción: el jefe de Bree, que es tutorial, solo lleva `atHpPct [40]`, sin escudo y con `21, 212`.
- **Comunes.**
  - En cada encuentro con al menos un rival **humanoide**, solo el primero de ellos lleva `reaction` con `atHpPct [30]` (`[40]` en el diseño inicial) y `combos ["21212"]`, sin escudo. Así solo se cura uno por grupo.
  - Las **bestias** y los **espectros** nunca la llevan: lobos, huargos, crebain, enjambres, arañas, fuegos fatuos, espectros de la ciénaga y olog-hai.
  - Los humanoides son los rivales cuyo retrato es `bandit`, `dunlending`, `goblin`, `orc`, `orc_archer`, `uruk`, `mordor_orc` o `haradrim`.
  - Se quitan las Cura I (`21`) que hoy llevan en el patrón los 11 bandidos de Bree y Chet.
- **Reglas** (`ContentRules`, `ContentRulesTest` y la spec de contenido §3 y §5):
  - el patrón normal mantiene sus reglas: daño ≥ 50 %, cadena de daño real y los jefes con `31`;
  - `reaction` es obligatoria en los jefes y en exactamente un humanoide de cada encuentro con humanoides;
  - las bestias y los espectros no la llevan;
  - los combos de `reaction` existen y su orden forma una cadena válida.

Se hace con un script de migración único (`tools/content/add_reactions.py`) y luego se revisa a mano el resultado.

## 4. Jugador simulado `MAESTRO` (`:sim`)

`Skill.MAESTRO` se suma a NOVATO, MEDIO y EXPERTO. Actúa cada tick, como el EXPERTO, y además:
1. **Cura:** encadena la Cura hasta el **tier 4** cuando baja del 50 % (el EXPERTO llega al tier 2 o 3), y Recuperar (`321`) cuando le falta power.
2. **Escudo:** se escuda (`2132`, o `2131` si el jefe aturde) cuando un jefe tiene en cola un combo de daño de tier ≥ 2, o en cuanto empieza el combate contra un jefe.
3. **Aturdir:** aturde (`31`, encadenando hasta el tier 3) **al jefe en cuanto empieza su reacción**. Es lo que haría el usuario para cortarle la cadena de curas.
4. **Grupos:** usa el área contra grupos y concentra el daño en el rival que tiene la reacción (el curandero) mientras no la haya gastado.

El informe y los CSV muestran el MAESTRO junto a los demás.

## 5. Balance

**Objetivos:**

| métrica | MEDIO | MAESTRO |
|---|---|---|
| comunes | ≥ 88 % | ≥ 97 % |
| jefes | 55–85 % (Gorgoroth 45–65 %) | 75–90 % |
| aventura completa | se mantiene el 1,1 % sin muertes, con margen de ±1 punto | ≥ 1 muerte de media por aventura |

Además: 0 combates al tope de ticks y 0 abandonadas.

**Objetivos revisados (decisión del controlador en la tarea 4).** Los de arriba no se pueden cumplir a la vez: el MAESTRO ganaba el 99–100 % de los jefes del Libro II incluso a +12, y el 1,1 % de aventuras sin muertes del MEDIO exige jefes que no se curen. El usuario juega como el MAESTRO y dijo que sus curas y escudos le daban demasiada ventaja, así que los objetivos pasan a ser:

| métrica | MEDIO | MAESTRO |
|---|---|---|
| comunes | ≥ 85 % | ≥ 97 % |
| jefes | ≥ 40 % (Gorgoroth ≥ 35 %) | 75–92 % cada uno (Bree, tutorial, exento) |
| aventura completa | ≤ 20 muertes de media y 0 abandonadas | de 1 a 4 muertes de media |

Primera palanca: el Escudo táctico del jugador (`2131`), que daba inmunidad total al aturdimiento. También se permiten cambios de patrón en los jefes que el MAESTRO no puede perder, offsets de hasta +15 y bajar `cureBasePct`. (Revertido el 2026-10-17 por decisión del usuario: el Escudo táctico vuelve a dar inmunidad total, `tacticsDazePct` = 0; ver «Escudo táctico: inmunidad total» en `docs/balance/tuning.md`.)

**Palancas, en este orden:**
1. offsets de los jefes (+2..+12; +2..+15 desde la revisión);
2. umbrales y escudo de la reacción por jefe;
3. `RivalScaling`;
4. solo si el MAESTRO sigue por encima del 95 % contra jefes: bajar `ComboTuning.cureBasePct` (35) y la duración de los escudos del jugador. Se documenta con una tabla antes/después y se avisa al usuario.

Se documenta en `docs/balance/tuning.md`, sección «Rivales que se curan y MAESTRO».

## 6. Pruebas

- **Motor:**
  - el jefe que baja del 50 % lanza escudo y luego `21`, `212`, `2121` y `21212` en ticks seguidos, con bonus de cadena, y después retoma su patrón;
  - con el 25 % lo vuelve a hacer una sola vez;
  - aturdido a mitad de reacción, la cancela;
  - un golpe que salta dos umbrales dispara solo uno;
  - sin poder pagar, bebe poción y lanza;
  - el combate es reproducible con la misma semilla.
- **Contenido:** los 21 jefes llevan reacción y exactamente un humanoide por encuentro con humanoides la lleva; ninguna bestia ni espectro.
- **Simulador:** el MAESTRO aturde al jefe cuando empieza su reacción (test con una traza) y encadena la cura hasta el tier 4.
- **Verificación:**
  - `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`;
  - captura en el emulador de un jefe curándose;
  - instalación en el móvil (`com.oscarmg.warden`) conservando la partida.
