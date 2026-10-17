# Pulido del combate: números en su escenario, barras grandes, botones 3D y medallones — diseño

Fecha: 2026-10-15. Estado: aprobado en conversación. Continúa la spec `2026-10-15-combat-cards-design.md`.

## Objetivo

Lo que pidió el usuario tras ver las cartas en el móvil:

1. Los números flotantes deben salir **en el espacio de la carta que queda debajo de los iconos de efecto**.
2. Las barras deben ser **más altas** y su texto **más grande**.
3. Los **botones de acción** deben tener aspecto **3D**, con un efecto espectacular al pulsar.
4. Los **iconos de efecto** deben ser **mucho más espectaculares** y estar a la altura del nuevo diseño.

Además se corrigen los hallazgos de la revisión final del trabajo anterior: la legibilidad de los números, que la pantalla se recomponga en cada fotograma y los restos de la regla antigua de offsets.

Fuera de alcance: cambiar las reglas del juego o el balance, y el mapa (spec propia `2026-10-15-continuous-map-design.md`).

## 1. Números flotantes (`ui/combat/FloatingNumbers.kt`, `CombatCards.kt`)

- **Escenario.** Cada carta expone el rectángulo libre que hay debajo de su fila de `EffectIcons`. Es todo el espacio entre el borde inferior de los iconos (o de las barras, si no hay iconos) y el borde inferior de la carta, menos 4 dp de margen. Los números solo se colocan ahí. Si el escenario mide menos de 40 dp de alto, se usa la mitad inferior de la carta.
- **Colocación por casillas.** El escenario se divide en una rejilla: 3 columnas × 2 filas en la carta de rival y 4 × 2 en la del jugador.
  - Cada número nuevo toma, con el `Random` de UI, una casilla que no ocupe ningún número vivo de esa carta. Si están todas ocupadas, usa la menos ocupada.
  - Dentro de la casilla se mueve un poco al azar, hasta ±25 % de su tamaño.
  - Se guarda el **centro** del número, como fracción del escenario.
  - La función de ajuste existente (`floatingNumberPlacement`) sigue garantizando que el número no se corta.
- **Legibilidad:**
  - opacidad completa hasta el 70 % de la vida (1100 ms) y luego se desvanece hasta 0;
  - la escala (0,3 → 1,25 → 1,0) se mantiene y la subida de 26 dp ocurre durante el desvanecimiento;
  - todo número lleva **contorno sólido**: el texto se dibuja primero con `Stroke` de 3 dp en `#E6000000` y después el relleno;
  - el halo de color se reserva a `rollPct ≥ 130`.
- **Rendimiento.**
  - El reloj de los números se lee solo dentro de la fase de dibujo o colocación (`graphicsLayer` / `placeWithLayer`), nunca en la composición. `FloatingNumbersLayer` recibe `clock: () -> Long` en lugar de `now: Long`.
  - Las cartas no se recomponen por el paso del tiempo.
  - Un número se mantiene en la lista hasta que se poda; no se filtra por alfa en la composición.

## 2. Barras (`ui/combat/GameBar.kt`)

- **Tamaño:** de 14 a **22 dp** de alto.
- **Texto:** 13 sp en negrita y blanco, con sombra (`Shadow(Color.Black, blurRadius = 3f, offset = (0, 1))`).
- **Power.** El destello y el halo solo se disparan si el cambio es de al menos el 5 % del máximo; la regeneración de +5 por tick no destella. El halo de una subida de power es azul (`#8FC4FF`); el verde se queda para la vida.
- **Halo.** Se dibuja metido hacia dentro medio trazo, para que no se recorte.
- **Limpieza:** se borra `ui/common/StatBar.kt`, que ya no se usa.

## 3. Botones de acción 3D (`ui/combat/ActionButtons.kt`, nuevo)

Sustituye a `ButtonRow` de `CombatScreen.kt`. Los colores base son los actuales (1 rojo, 2 turquesa, 3 ámbar y ⚔ verde).

- **Forma:** tecla redondeada (12 dp) con **canto** inferior de 6 dp en el color base al 55 % de brillo.
  - **Cara:** degradado vertical del color base al 115 % arriba y al 90 % abajo.
  - **Brillo:** blanco al 35 % en el 40 % superior, con forma de píldora.
  - **Borde:** biselado de 1 dp, más claro arriba y más oscuro abajo.
  - **Etiqueta:** grande, con sombra.
- **Al pulsar** (`interactionSource`, toque inmediato):
  1. la cara baja 4 dp y el canto pasa de 6 dp a 2 dp, en 60 ms;
  2. destello: brillo ×1,6 que vuelve a ×1 en 250 ms;
  3. onda: un anillo que nace en el punto del toque y crece hasta 1,4 veces el ancho de la tecla en 350 ms, desvaneciéndose;
  4. **chispas**: 10 partículas del color base aclarado salen del punto de toque en direcciones al azar (UI) con velocidad de 120–260 dp/s, caen con «gravedad» y se apagan en 450 ms. Pueden salir fuera de la tecla: la fila de botones no recorta;
  5. al soltar, la tecla sube con un rebote de muelle (`spring(dampingRatio = 0.45f)`).
- **⚔ listo.** Cuando `bufferMatch != null`, el botón de acción late:
  - halo dorado (`#F4D98A`) que pulsa con alfa entre 0,25 y 0,6 cada 900 ms;
  - el icono de la espada brilla.

  Al pulsarlo en ese estado, la explosión es mayor: 18 chispas doradas y una onda de 1,8 veces.
- **Desactivados** (aturdido, pausa o fin): la tecla queda plana (sin canto ni brillo), desaturada al 40 % y sin efectos.
- **Hápticos:** sin cambios respecto a lo que hay.

## 4. Medallones de efecto (`ui/common/EffectBadges.kt`)

El mismo dato (`EffectBadgeUi`: code, debuff, remainingFraction, shape, color) con un dibujo nuevo:

- **Forma.** Medallón de 26 dp de alto, con el ancho de la forma actual.
  - **Pastilla, de familia SP/LC/AT/PA/CU:** gema con degradado radial del color de la familia, más claro arriba a la izquierda.
  - **Escudo, rombo, gota y estrella:** mantienen su silueta con el mismo degradado.
- **Borde metálico.**
  - Plateado (`#C8CCD4` → `#6A6E78`) en tier 1–2.
  - Dorado (`#F4D98A` → `#8A6420`) en tier 3–4, que se saca del dígito final del código.
  - Rojizo en los perjuicios (`debuff`).
- **Duración.** Un anillo exterior de 2 dp muestra `remainingFraction` y se vacía en sentido horario. Sustituye a la barra o al relleno que hubiera.
- **Brillo.** Un barrido diagonal blanco cruza el medallón al aparecer y después cada 3 s.
- **Animaciones** (con el reloj de pausa del combate, congeladas en pausa):
  - al aparecer un código nuevo en la lista: escala 0 → 1,2 → 1 en 300 ms y destello;
  - cada vez que `remainingFraction` baja (un tick que hace efecto): latido 1 → 1,12 → 1 en 200 ms;
  - cuando queda poco (`remainingFraction ≤ 0,25`; `EffectBadgeUi` no lleva el total de ticks): parpadeo suave de alfa entre 0,6 y 1;
  - `DZ` (estrella): gira despacio (360° cada 4 s);
  - escudos: un reflejo metálico recorre el borde;
  - gotas de robo de vida: brillo interno que pulsa.
- **Texto:** el código en blanco con contorno oscuro, 11 sp en negrita.

## 5. Arreglos de la revisión anterior

- `engine/src/test/.../story/ContentRules.kt`: el mensaje de error dice «+2..+12» y se construye desde `BOSS_OFFSETS`.
- `sim/.../Sweep.kt`: los offsets por defecto pasan a `2..12`.

## 6. Pruebas

- **Números:**
  - la colocación usa casillas distintas mientras haya libres (6 números en un lote quedan en 6 casillas distintas);
  - todos caen dentro del escenario;
  - `floatingNumberAlpha` vale 1 hasta 0,7 y 0 al final.
- **Barras:**
  - un cambio de power < 5 % no destella y uno ≥ 5 % sí;
  - el texto «65/100» existe;
  - la altura es de 22 dp.
- **Botones:**
  - pulsar llama a `onPress` o `onExecute` como antes;
  - desactivados, no llaman;
  - el botón de acción expone el estado «listo» como semántica (`stateDescription = "listo"`) cuando hay combo;
  - función pura `sparkBurst(count, seed)` que devuelve 10 o 18 partículas con velocidades en rango.
- **Medallones:**
  - el tier sale bien del código («SP3» → 3, «EM» → 0, es decir, plateado);
  - el color de borde es el correcto por tier y perjuicio;
  - el anillo es proporcional a `remainingFraction`;
  - test Compose de que se muestra el código.
- **Verificación:**
  - `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug` en verde;
  - instalación en el móvil;
  - captura `docs/screenshots/v3/combat-polish.png`, sacada en el **emulador** con una partida nueva. Nunca se juega en la partida del usuario: en el móvil solo se instala.
