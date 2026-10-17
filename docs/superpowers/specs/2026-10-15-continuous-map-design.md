# Mapa continuo en alta resolución — diseño

Fecha: 2026-10-15. Estado: aprobado en conversación (enfoque A «un mundo generado por código», con movimiento suave, a partir del prototipo v2).

Referencias:
- Prototipo aprobado (JavaScript, desechable): `docs/superpowers/specs/assets/2026-10-15-map-proto-v2.html`.
- Captura del prototipo: `docs/superpowers/specs/assets/2026-10-15-map-proto-v2.png`.

## Objetivo

El mapa actual pinta cada zona como una franja con su propio color, textura y objetos, así que en cada frontera hay un corte. El usuario quiere **un mapa continuo de verdad**, con más resolución y más colores, que se vea como una sola Tierra Media de Bree a Lothlórien.

Fuera de alcance:
- cambiar qué zonas hay o su orden (`Route.zones`);
- cambiar las paradas, los marcadores, el cartucho «Continuar» o la lógica del `MapViewModel`, salvo moverlos a la nueva posición del camino;
- imágenes externas (Gemini).

## 1. Escala

- `TILE_DP` pasa de 8 a **4**.
- `ZONE_COLS` pasa de 28 a **56**, así que `ZONE_WIDTH_DP` no cambia (224 dp por zona).
- Filas: `ceil(alto disponible / 4 dp)`, como ahora pero con la baldosa nueva (unas 200 en el móvil).

## 2. Generador del mundo (`ui/map/world/`, Kotlin puro, sin Android)

Es un port del prototipo v2 a funciones puras y deterministas. No usa `Random` ni reloj: todo es hash y ruido de valor sobre (x, y) con semillas fijas.

- **`WorldNoise`.** `hash`, `h01`, `smooth`, `valueNoise(x, y, seed)` y `fbm(x, y, seed, octaves)`, idénticos en su forma a los del prototipo.
- **`ZoneKeys`.** Tabla `zoneId → pesos` de los campos `farm`, `meadow`, `forest`, `marsh`, `hills`, `rock`, `snow`, `mount`, `under`, `golden`, `holly`, `valley` y `elev`, con los valores del prototipo.
  - Es presentación: vive en `:app`. Las reglas del juego no dependen de ella.
  - Una zona de `Route.zones` que no esté en la tabla usa `meadow = 1`.
- **Campos continuos.** `field(name, x)` interpola con `smoothstep` entre los centros de zona. La x se deforma con ruido (`warpX`, ±17 baldosas), así que las fronteras no son líneas verticales.
- **Elevación.** Ruido de base, más colinas, más una cordillera que crece desde los bordes superior e inferior según `mount`, con crestas.
  - Sombreado con luz del noroeste (`1 − (dx + dy) × 3,2`, limitado a 0,55..1,35).
  - Nieve por encima de 0,95.
- **Color del suelo.**
  - Cada bioma tiene una rampa de 5 colores (la tabla `RAMP` del prototipo).
  - El color de una baldosa es la **mezcla ponderada** de todos los biomas presentes. Los pesos son el campo del bioma por la mancha de ruido, al cuadrado.
  - La nieve y la montaña pesan según la elevación.
- **Agua:**
  - ríos Fontegrís (puente), Bruinen, arroyo de Rivendel y Nimrodel, con trazado serpenteante;
  - lago ante las Puertas de Durin y el Espejo (sombrío);
  - charcas del pantano por ruido;
  - profundidad de somera a profunda, con tono frío en montaña y verdoso en pantano;
  - orilla húmeda alrededor.
- **Puertas de Durin.** Pared de roca dentada en la frontera durin|moria.
  - El camino la cruza por una puerta clara, de 3 baldosas de alto, con dos columnas talladas y un arco.
  - **Arreglo sobre el prototipo:** la puerta y el lago deben verse. Ni montañas ni sprites a menos de 10 baldosas de la puerta ni dentro del lago.
- **Camino.** `roadY(x)`: serpentea en torno al 47 % del alto, sustituye a `pathRow` y es continuo (`|roadY(x+1) − roadY(x)| ≤ 2` baldosas).
  - Tiene 5 baldosas de ancho: borde, banda clara y borde, con piedras y sombra inferior.
  - Sobre un río no vadeable es un puente de tablas con barandilla.
  - En el Bruinen y el Nimrodel es un vado de piedras.
  - Tono según el bioma: tierra, nieve, oro pálido o piedra oscura en Moria.
- **Moria.** Oscuridad que crece al alejarse del camino, con columnas.
  - **Arreglo sobre el prototipo:** la transición desde Durin y hacia Sombrío es gradual, a lo largo de unas 20 baldosas. El oscurecimiento sigue `field("under")` sin cortes.
- **Sprites procedurales** con sombreado y sombra proyectada hacia el sureste:
  - árbol, roble, pino;
  - casa con ventanas encendidas y chimenea;
  - roca, peña, ruina;
  - juncos, árbol muerto, acebo con bayas;
  - montaña nevada;
  - columna;
  - mallorn;
  - campo de cultivo.

  Se colocan en una rejilla de 7 baldosas con desplazamiento por hash, según la densidad de su bioma. Nunca caen en el agua, el camino, la pared ni junto a una parada. Las ruinas y los árboles muertos son minoría: un 7 % y un 10 % de sus biomas.
- **Cuantización.** Dither ordenado 4×4 con 32 niveles por canal (paso 8), para mantener el aspecto pixel-art.
- **Salida:**
  - `WorldMap(width, height, pixels: IntArray ARGB, water: IntArray, chimneys, torches, mallorns: List<Point>, stops: List<Point>)`;
  - una parada por zona, en `(centro de la zona, roadY)`.

## 3. Pantalla (`ui/map/MapScreen.kt`)

- **Generación.**
  - `WorldMap` se genera en `Dispatchers.Default` y se guarda en un caché de proceso indexado por (zonas, filas), así que volver al mapa no lo regenera.
  - Mientras se genera se muestra el fondo oscuro con «Dibujando el mapa…».
  - **Objetivo:** menos de 1,5 s en el Moto G85.
- **Dibujo.** La imagen se pinta escalada con `FilterQuality.None`. Las paradas y marcadores existentes se colocan en `stops`.
- **Animación de ambiente:**
  - un `Canvas` encima, solo de la parte visible (según el scroll), redibujado unas 10 veces por segundo;
  - se detiene cuando la pantalla no está en primer plano (`Lifecycle` STARTED).
  - **Agua:** brillos y sombras con ruido que se desplaza.
  - **Sombras de nubes:** avanzan hacia el este.
  - **Humo:** sale de las chimeneas.
  - **Antorchas de Moria:** parpadean con resplandor.
  - **Mallorn:** destellos.
  - Valores del prototipo.
- **Limpieza.** Se borran `PixelMap.kt` y `PixelMapTest.kt`, y se adaptan `MapScreenTest` y `StopPlacementTest`.

## 4. Pruebas (`app/src/test/.../ui/map/`)

- **Determinismo:** dos generaciones con los mismos (zonas, filas) dan los mismos píxeles.
- **Continuidad:** la diferencia media de color entre columnas vecinas en cada frontera de zona no supera 1,6 veces la diferencia media de las columnas vecinas (56k−9 … 56k+7, sin la propia frontera; decisión durante la implementación: la media global medía la densidad de textura, no los cortes). Es el test que prueba que no hay cortes.
- **Camino:**
  - continuo (salto máximo de 2 baldosas);
  - cada parada está en el camino y no en el agua;
  - donde el camino cruza un río hay puente o vado.
- **Puerta de Durin:** hay píxeles de puerta en la frontera durin|moria a la altura del camino, y no hay sprites a menos de 10 baldosas.
- **Lago:** existe agua en el lago de Durin y en el Espejo.
- **Tamaño:** `WorldMap.width == zonas × 56`.
- **Pantalla:** test Compose de que se muestran las paradas y el botón «Continuar» como hasta ahora.
- **Verificación:**
  - `./gradlew :app:testDebugUnitTest :app:assembleDebug`;
  - medir el tiempo de generación en el móvil (log de depuración);
  - capturas `docs/screenshots/v3/map.png` y `map-scrolled.png`, que se pueden sacar en el móvil porque el mapa no cambia la partida.
