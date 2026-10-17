# Jefes con modo ataque y modo defensa — diseño

Fecha: 2026-10-17. Estado: aprobado en conversación. El usuario dijo «si, continua con lo que decidas»; se aplican los valores propuestos.

## Objetivo

Que los combates contra jefes sean más duros y más largos. Cada jefe alterna entre dos modos según su vida:
- **ataque**, con su vida por encima del 50 %;
- **defensa**, por debajo del 50 %: se protege y se cura.

Solo para los jefes. Los comunes conservan su reacción de curandero (spec `2026-10-16-rival-healing-design.md`).

> Nota de implementación: tras el balance, 14 jefes usan umbrales y número de defensas propios (15/20/50 %, 1–2 defensas); la tabla está en `ContentRules.BOSS_MODE_TUNING`, en §3 de la spec de contenido y en `docs/balance/tuning.md` («Jefes con modo ataque y defensa»). Los valores 50/70/3 de abajo son los de partida.

## 1. Datos (`RivalContent.modes`, sustituye a `reaction` en los jefes)

```json
"modes": { "defenseBelowPct": 50, "attackAbovePct": 70, "maxDefenses": 3,
           "defense": ["2132", "21", "212", "2121", "21212", "<daño A>", "<daño B>"] }
```

- **Patrón de ataque:** el `pattern` del jefe sin combos de escudo (`2131`, `2132`) ni de cura (`21…`). Debe seguir cumpliendo las reglas: daño ≥ 50 %, cadena de daño real y al menos un `31`.
- **Patrón de defensa:**
  - escudo `2132`;
  - la cadena de curas completa;
  - los dos primeros combos de daño del patrón de ataque, en ese orden.
- Lo genera un script de migración (`tools/content/add_boss_modes.py`) y se valida en `ContentRules`.
- El jefe de Bree (tutorial) lleva una versión suave: `defenseBelowPct 40`, `attackAbovePct 60`, `maxDefenses 1` y una defensa sin escudo: `21, 212` más un combo de daño.

## 2. Motor (`PatternBrain`)

- **Estado en `Fighter`:**
  - `mode` (ATTACK o DEFENSE);
  - `defenses` (cuántas veces ha entrado en defensa);
  - `modeCasts` (para avanzar el patrón del modo).

  Es inmutable y determinista, sin RNG nuevo.
- **Cuándo cambia de modo** (en `prepare`, en los ticks en que le toca):
  - pasa a defensa si `mode == ATTACK`, su vida es ≤ `defenseBelowPct` y `defenses < maxDefenses`;
  - vuelve a ataque si `mode == DEFENSE` y su vida es ≥ `attackAbovePct`.
- **Al entrar en defensa,** el patrón de defensa empieza desde el principio (escudo y luego curas).
- **En defensa** lanza cada tick, ignorando `everyNTicks`, para no perder la cadena de curas.
- **En ataque** lanza cada `everyNTicks`, como siempre.
- **Aturdir:** si el jefe queda aturdido (DAZED) en defensa, el patrón de defensa se reinicia en el primer combo de daño del final y se salta lo que quedara de la cadena de curas. Es la contra: aturdirle corta la curación.
- **Freno anti-bucle:** tras `maxDefenses` entradas en defensa, el jefe ya no vuelve a entrar en ese combate y sigue en ataque.
- **Última defensa** (re-balance, `docs/balance/tuning.md`, «Jefes con modo ataque y defensa»): en la última defensa permitida (`defenses == maxDefenses`) el jefe lanza el patrón de defensa una sola vez (`defense.size` combos; si un Daze le hace saltar a los combos de daño finales, cuenta como llegar a ellos) y vuelve al ataque tenga la vida que tenga. Las defensas anteriores siguen hasta el `attackAbovePct`. Sin esta regla, la defensa de Bree se repetía sin fin contra un jugador que apenas la supera.
- **Power:** si no puede pagar, bebe poción, como ahora.
- `RivalSpec` deriva `modes` del `brain`, como ya hace con `reaction`.

## 3. Balance

Se re-simula con el MAESTRO y el MEDIO. Los objetivos son los de la ronda anterior:

| métrica | MAESTRO | MEDIO |
|---|---|---|
| comunes | ≥ 97 % | ≥ 85 % |
| cada jefe | 75–92 % | ≥ 40 % (Gorgoroth ≥ 35 %) |
| muertes por aventura | 1–4 | ≤ 20 |

Además: 0 aventuras abandonadas, 0 combates al tope de ticks y Bree exento como tutorial.

**Palancas:**
- offsets de los jefes (+2..+15);
- los parámetros de modo por jefe: umbrales, `maxDefenses` y los combos de daño de la defensa.

No se tocan las herramientas del jugador.

Se documenta en `docs/balance/tuning.md`, sección «Jefes con modo ataque y defensa», con la tabla antes/después, la duración media de los combates contra jefes y el número medio de entradas en defensa.

## 4. Pruebas

- **Motor:**
  - un jefe a más del 50 % lanza solo su patrón de ataque;
  - al bajar del 50 % lanza `2132`, `21`, `212`, `2121`, `21212`, daño y daño, cada tick y con bonus de cadena;
  - al curarse por encima del 70 % vuelve a ataque;
  - nunca entra en defensa más de `maxDefenses` veces;
  - aturdido en defensa, se salta las curas restantes;
  - con la misma semilla da los mismos eventos;
  - los rivales sin `modes` no cambian.
- **Contenido:**
  - los 21 jefes llevan `modes` válido y ya no llevan `reaction`;
  - los patrones de ataque no tienen escudos ni curas y cumplen las reglas;
  - los comunes no cambian.
- **Verificación:**
  - `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`;
  - instalación en el móvil con `-r`, si está conectado.
