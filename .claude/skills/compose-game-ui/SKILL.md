---
name: compose-game-ui
description: Use when building or changing the Android UI layer (:app module) - Jetpack Compose screens, combat screen, input controls, ViewModels wrapping the engine, haptics, static images/sprites, navigation, or UI tests.
---

# UI del juego con Jetpack Compose

No hay gráficos en movimiento ni game loop de render: la UI es **Compose estático** que se recompone cuando cambia el estado del motor. Nada de SurfaceView, Canvas a 60 fps, LibGDX, Unity ni OpenGL.

## Arquitectura (unidireccional)

```
Usuario ─(toque)─▶ Composable ─(intent)─▶ CombatViewModel ─(Action)─▶ engine.reduce()
                                                │
          Composable ◀─(StateFlow<UiState>)─────┘◀── nuevo CombatState + List<GameEvent>
                    ◀─(SharedFlow<GameEvent>: haptics, sonidos, mensajes efímeros)
```

- El `ViewModel` es la única pieza que llama al motor. Mapea `CombatState` → `UiState` (modelo pensado para pintar: textos, porcentajes de barra, qué botones habilitados).
- Composables **sin lógica de juego**: solo reciben `UiState` y lambdas `onXxx`. Si un composable decide quién gana algo, está mal ubicado → muévelo a `:engine`.
- Reloj real (si hay ventanas de tiempo) se lee en el ViewModel y se pasa al motor dentro de la `Action`; inyecta un `Clock` para poder testear.
- IA del rival: el ViewModel pide la acción a la estrategia del motor; si quieres "pausa dramática", es un `delay` en el ViewModel, no lógica.

## Pautas de Compose

- `collectAsStateWithLifecycle()` para el estado. State hoisting estricto.
- Pantalla fija en orientación elegida (declarada en el manifest) y layouts que funcionen en distintos tamaños (usa `WindowSizeClass` si hace falta).
- Feedback de combo: transiciones simples (`AnimatedVisibility`, `animateColorAsState`, cambio de imagen). Animaciones cortas y opcionales: el juego debe ser correcto sin ellas.
- Imágenes estáticas de personajes/golpes en `res/drawable` (preferir vector o WebP). Cambiar de imagen según el evento = suficiente "animación".
- Botones de input grandes (≥ 48dp), respuesta háptica (`HapticFeedback` / `Vibrator`) en el `SharedFlow` de eventos.
- Textos en `res/values/strings.xml` (español por defecto; preparado para traducir).
- Tema Material 3 propio en `ui/theme/`.

## Tests

- ViewModel: tests JVM con `kotlinx-coroutines-test` (`runTest`, `StandardTestDispatcher`) y `Turbine` para flows. Motor real (es puro y rápido), reloj falso.
- Composables: `createComposeRule()` para pantallas clave (que pulsar inputs llega al ViewModel y que el estado se pinta). Previews `@Preview` con `UiState` de ejemplo para cada pantalla.
- Verificación visual: instalar en emulador y `adb exec-out screencap -p` (ver `android-build`).
