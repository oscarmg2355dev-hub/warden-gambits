# Pantalla final «¡Has completado la aventura!» (2026-10-13)

## Problema

Informe del usuario: «al llegar al final, se ha reiniciado a bree, pensaba que habría una pantalla de que me he pasado el juego». Al entrar en el nodo final (`lothlorien_final`) el motor pone la fase `LAP_COMPLETE`, y la app mostraba la pantalla normal del nodo con un único botón «Emprender la vuelta N», que lleva directamente a Bree. No había celebración ni resumen.

## Diseño

- Nueva ruta `ending` en `WardenNavHost` con `EndingScreen` + `EndingViewModel` (`ui/ending/`).
- Contenido: título «¡Has completado la aventura!», imagen del nodo final (`NodeImage` con `lothlorien_final`), título y epílogo del nodo final y un resumen de la vuelta: «Vuelta N completada», «Nivel X (+Y en esta vuelta)» (`level - lapStartLevel`), «Victorias: N» y «Muertes: N» (`RunState.deaths`). No hay datos de tiempo en el estado, así que no se muestra tiempo; el motor no cambia.
- Botones: «Emprender la vuelta N+1» (despacha `StoryAction.StartNextLap` y va al mapa, con la pila `menú → mapa`) y «Volver al menú» (vuelve al menú sin tocar la partida, que sigue en `LAP_COMPLETE`). Atrás del sistema hace lo mismo que «Volver al menú».
- `EndingViewModel.ui` es `null` fuera de `LAP_COMPLETE`; en ese caso la ruta vuelve al menú.

## Navegación

- Nodo: si la fase es `LAP_COMPLETE` (al pulsar «Continuar» en el nodo 7 de Nimrodel tras ganar al jefe), la ruta `node` se sustituye por `ending` (`popUpTo(node, inclusive)`), sin pintar el nodo.
- Mapa («Continuar ▶») y menú («Modo Historia») usan `storyRouteFor(profile)`: con una vuelta completada guardada van directos a `ending`.
- Combate: tras ganar el último combate se vuelve al nodo (fase `CHOOSING`); el nodo final no tiene combate, así que el final siempre se alcanza desde el nodo.
- La pantalla de nodo conserva el caso `NodeActions.NextLap` (el `when` sigue siendo exhaustivo), aunque ya no se ve en la navegación real.

## Tests

`EndingViewModelTest` (mapeo y `startNextLap`), `EndingScreenTest` (Robolectric/Compose) y `EndingRoutingTest` (`WardenNavHost` real: nodo → final, menú → final, mapa → final, volver al menú conserva `LAP_COMPLETE`, nueva vuelta → mapa).
