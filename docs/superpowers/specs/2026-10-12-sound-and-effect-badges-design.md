# Sonido e iconos de efectos — Diseño

Peticiones del usuario del 2026-10-12, tras probar la interfaz v2. El usuario está fuera y pidió aplicar las propuestas por defecto sin consultar. Casi todo es `:app`. Único cambio en el motor: `ActiveEffect.totalTicks` (la duración con la que se creó o se renovó el efecto), para dibujar la barra de duración.

## 1. Sonido

**Efectos de sonido**, cortos y generados por código. Ver §1.1.

| Evento | Sonido | Duración |
|---|---|---|
| Pulsar ⚔ Acción | "swoosh" metálico corto (ruido filtrado con barrido descendente + clic) | ~0,15 s |
| Rival derrotado (`FighterDefeated` de un rival) | golpe grave + campanada descendente | ~0,5 s |
| Victoria (`CombatEnded(VICTORY)`) | fanfarria corta: arpegio mayor ascendente con onda cuadrada suave | ~1,5 s |
| Derrota (`CombatEnded(DEFEAT)`) | tres notas menores descendentes, grave | ~1,5 s |

**Ambiente de combate:** un bucle de unos 12 s con tambor grave rítmico, viento filtrado y un bordón grave suave, a volumen bajo.
- Suena solo en la pantalla de combate mientras el combate está en curso.
- Se pausa con la pausa del juego y en segundo plano (`setActive(false)`).
- Se detiene al terminar el combate (suena la fanfarria) o al salir de la pantalla.

**Botón Sonido:**
- Va en el menú principal y alterna entre "Sonido: sí" y "Sonido: no". Por defecto, activado.
- Se guarda en el mismo DataStore, en una clave aparte de la partida.
- Si está desactivado, no suena nada.

**Implementación:**
- `SoundPlayer` es una interfaz con una implementación Android:
  - `SoundPool` para los efectos;
  - `MediaPlayer` en bucle para el ambiente;
  - creado en `AppContainer` y liberado con el ciclo de vida de la app.
- La decisión de qué sonar ante cada evento es una función pura (`soundFor(event, playerId): Sfx?`), con tests.
- `CombatRoute` llama al `SoundPlayer` junto a la vibración.
- En los tests se usa un `FakeSoundPlayer` que graba las llamadas.

### 1.1 Archivos de audio

- Los genera un script `tools/sfx/generate_sfx.py` (Python, solo biblioteca estándar: `wave`, `math`, `struct`, `random` sembrado). Son WAV PCM de 16 bits, mono, a 22.050 Hz.
- Se guardan en `app/src/main/res/raw/`: `sfx_action.wav`, `sfx_defeat_enemy.wav`, `sfx_victory.wav`, `sfx_defeat.wav` y `amb_combat.wav`.
- El script queda en el repositorio para poder regenerarlos.
- Son audio original, sin licencias de terceros.

## 2. Iconos de efectos

**2.1 Insignias por combo de origen.** Cada efecto activo se muestra como **una insignia por instancia**, con el código de su combo de origen: 2 letras y el tier. Así se ve cuántos efectos hay y cómo van caducando.

| Origen | Código |
|---|---|
| `12…` Lanza | SP1–SP4 |
| `13…` Lanza cruzada | LC1–LC4 |
| `31…` Aturdir (probabilidad) | AT1–AT4 |
| `32…` Puño en área | PA1–PA4 |
| `21…` Cura | CU1–CU4 |
| `3123`/`31232` Robo de vida | RV1–RV2 |
| `11` Furia | FU |
| `22` Guardia | GU |
| `33` Brío | BR |
| `2132` Escudo maestro | EM |
| `2131` Escudo táctico | ET |
| Aturdido (`DAZED`, sin combo de origen) | DZ |

- Los colores siguen siendo los de debuff y buff (Combate v2 §10): DoT, Aturdir y Aturdido son debuff; el resto, buff.
- Bajo el texto de cada insignia hay una **barrita de duración** con los ticks que le quedan respecto a su duración total, que se vacía tick a tick.
- La insignia de Aturdir muestra además el % en una insignia resumen ("AT 15%") solo si hay varias; si no, basta con el código.
- Orden de las insignias: debuffs primero, luego buffs, y dentro de cada grupo por tiempo restante, de menor a mayor.
- Se mantiene "+N" cuando no caben.

**2.2 Rival derrotado.** Su card muestra "✝ Derrotado", las dos barras a 0 (también el power) y sin insignias. El overlay de resultado baja su opacidad del 80% al 65%, para que se vea mejor que el rival está a 0.

## 3. Tests

- `soundFor`:
  - ⚔ → acción;
  - `FighterDefeated` de un rival → `DEFEAT_ENEMY`; del jugador → nada;
  - `CombatEnded` → victoria o derrota.
- `FakeSoundPlayer` en `CombatRoute`/ViewModel:
  - el ambiente arranca al estar activo y se detiene con pausa, fin o salida;
  - con el sonido desactivado no suena nada.
- La preferencia de sonido se guarda y se carga.
- Insignias:
  - código por combo (por ejemplo, "121" → "SP2" y "2132" → "EM");
  - una insignia por instancia;
  - fracción de la barra de duración;
  - orden;
  - DZ para Aturdido.
- Rival derrotado: power a 0, texto "✝ Derrotado" y ninguna insignia.
