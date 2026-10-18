# Combate con cartas, barras 3D, números flotantes y daño variable — diseño

Fecha: 2026-10-15. Estado: aprobado en conversación (estilo de carta C «compacta», elegido sobre maquetas animadas).

## Objetivo

La pantalla de combate se ve plana. El usuario quiere:

1. Barras de vida y power «de juego»: relieve 3D y un efecto visible cuando suben o bajan.
2. Rivales como **cartas de rol** con retrato del tipo de enemigo, datos, barras y efectos.
3. La carta del jugador con el mismo estilo, más horizontal.
4. Que daños y curas **varíen**: cada valor sale entre el 50 % y el 150 % de su valor nominal.
5. Que los números de efecto salgan **en cualquier punto de la carta**, con un tamaño mayor cuanto mayor sea el valor.

Fuera de alcance: rediseñar el mapa, los nodos o el menú; animaciones de personajes (el juego sigue sin gráficos en movimiento: solo estado pintado más transiciones cortas de Compose); el modo apaisado para tablet.

## 1. Daño y cura variables (`:engine`)

- **Por pulso.** Cada pulso que mueve vida tira un porcentaje entero uniforme `rollPct ∈ [50, 150]` con el `GameRandom` sembrado del combate. Los pulsos son:
  - tick de DoT (lanza, área);
  - tick de CoT (cura);
  - pulso de robo de vida;
  - golpe directo (`123`).
- **Cómo se aplica.**
  - DoT, CoT y robo de vida: el valor del pulso pasa a `magnitude × rollPct / 100` (redondeo al entero más cercano, mínimo 1) **antes** de la guardia y el escudo, que se aplican como ahora.
  - Golpe directo (`123`): la tirada se aplica al daño **final**, después de la defensa (`DamageFormula.received`).
  - En el robo de vida, la tirada escala el máximo que se roba a cada oponente.
  - El lanzador se cura la suma de lo realmente robado.
- **Qué no varía:**
  - escudos (`2132`, `2131`);
  - probabilidad de Daze;
  - multiplicadores (`11`, `22`, `33`);
  - Recuperar (`321`);
  - pociones;
  - regeneración de power.
- **Constantes de datos.** `ComboTuning` gana `rollMinPct = 50` y `rollMaxPct = 150`. Con `ComboTuning(rollMinPct = 100, rollMaxPct = 100)` el comportamiento es el antiguo, y los tests que necesitan valores exactos lo usan.
- **Evento.** `CombatEvent.EffectResolved` gana `rollPct: Int`; vale 100 en los efectos que no tiran.
- **Determinismo.** La tirada consume el RNG del combate en un orden fijo: el orden actual de resolución de pulsos. Con la misma semilla se obtiene la misma partida. Las huellas de `HpRegressionTest` se regraban.
- **Balance.** Los offsets de jefe pasan a permitir +2..+12 (antes hasta +8). La media no cambia, pero la varianza mueve los porcentajes de victoria.
  - Se re-simula con `:sim` y se documenta en `docs/balance/tuning.md`, en la sección «Daño variable 50–150 %», con la tabla antes/después.
  - Solo se reajusta `RivalScaling` u offsets si algo sale de los objetivos vigentes («Más enemigos y dificultad +»):
    - MEDIO 5–9 % de aventuras sin muertes;
    - EXPERTO 20–28 %;
    - 3,5–5 muertes MEDIO por aventura;
    - comunes MEDIO ≥ 88 %;
    - jefes MEDIO 55–85 %;
    - 0 combates al tope de ticks.

## 2. Retratos por tipo (datos + `:app`)

- **Contenido.** Cada rival del JSON de historia gana `"portrait": "<clave>"`, obligatorio. `RivalSpec`, o su equivalente en `story/content`, lo expone, y `CombatSetup` lo lleva hasta `FighterUi.portrait`. El jugador usa `warden`.

- **Tipos comunes (12):**

  | clave | rivales |
  |---|---|
  | `bandit` | Ratero de Bree, Ladrón de caballos, Bandido del foso, Bribón de la banda, Matón de Abrojal, Vigía de los salteadores, Salteador del Tuerto, Bandido del bosque, Salteador rezagado, Cobrador de peajes, Bandido del puente |
  | `wolf` | Lobo hambriento, Lobo pardo de Chet, Lobo de las pozas, Lobo de la manada, Lobo viejo, Lobo joven de la manada |
  | `wolf_saruman` | Lobo de Saruman |
  | `warg` | Huargo, Huargo viejo, Huargo explorador, Huargo grande, Huargo de la cueva |
  | `warg_rider` | Huargo de monta |
  | `dunlending` | Espía dunlendino, Espía dunlendino veterano, Explorador dunlendino, Rastreador dunlendino, Espía del pantano |
  | `crebain` | Crebain |
  | `swarm` | Nube de moscardones, Enjambre de moscardones, Moscardones de Moscagua, Enjambre del ahogado, Guardianas del enjambre |
  | `goblin` | Trasgo de Moria |
  | `orc` | Orco de las Montañas Nubladas, Orco grande de las Montañas Nubladas, Orco capitán |
  | `orc_archer` | Arquero orco |
  | `troll` | Trol de las cavernas |

- **Jefes (13).** Cada uno tiene su `boss_<zona>`:

  | clave | jefe |
  |---|---|
  | `boss_bree` | El Tuerto |
  | `boss_chet` | Lobo gris alfa |
  | `boss_cima` | Espectro del Anillo |
  | `boss_moscagua` | Reina del enjambre |
  | `boss_puente` | Jefe de la manada de huargos |
  | `boss_quebradas` | Trol de piedra |
  | `boss_bruinen` | Jinete Negro |
  | `boss_eregion` | Capitán dunlendino |
  | `boss_caradhras` | Furia del Caradhras |
  | `boss_durin` | Guardián del Agua |
  | `boss_moria` | Sombra de Durin |
  | `boss_sombrio` | Capitán orco de las Montañas Nubladas |
  | `boss_nimrodel` | Jefe de los arqueros orcos |

- **Validación.** `ContentRulesTest` / `RealContentTest` comprueban que todo rival tiene `portrait` con el formato `[a-z_]+` y que todo jefe usa `boss_<su zona>`. La lista de claves válidas vive en `ContentRules`, en los tests: 12 comunes más los 13 jefes.
- **Imágenes.** `app/src/main/res/drawable-nodpi/portrait_<clave>.webp`, cuadradas de 256×256.
  - `docs/content/prompts.md` tiene una fila `| portrait_<clave>.webp | título | prompt |` por cada retrato: 26 en total (12 comunes, 13 jefes y `warden`), en pixel-art de 16 bits, retrato de busto cuadrado y fondo oscuro neutro.
  - `tools/gemini/generate_images.py` gana `--set portraits`, que lee ese fichero, genera con relación 1:1, deja las imágenes en `docs/content/gemini/portraits/` y las convierte a 256×256.
- **Retrato por código.** Mientras falte la imagen se dibuja un sprite de 16×16 por tipo (`PortraitSprites`, en `:app`).
  - Hay 13 sprites: los 12 tipos y `warden`.
  - Cada sprite es una lista de filas de caracteres más una paleta, y se pinta en un `Canvas` con píxeles cuadrados.
  - Un jefe sin imagen usa el sprite de su tipo base. La tabla `boss_<zona> → tipo` va en `PortraitSprites`; es presentación, no regla de juego.

## 3. Pantalla de combate (`:app`)

### Carta de rival (estilo C)

- **Marco:** fondo con degradado marrón oscuro, borde dorado fino, brillo interior superior y sombra inferior.
- **Contenido:**
  - Cabecera con el retrato de 44 dp a la izquierda, con marco dorado y sombra interior. A su derecha, el nombre en serif dorado (máximo 2 líneas) y la línea «Nv N · ATQ · DEF».
  - Debajo, barra de vida, barra de power e iconos de efecto (`EffectBadges`, sin cambios).
- **Se conserva lo de ahora:**
  - borde resaltado del objetivo y toque para elegirlo;
  - marca de jefe;
  - combo en cola;
  - carta apagada al morir (gris, retrato desaturado, «Derrotado»).
- **Rejilla:** 2×2 para hasta 4 rivales; las celdas vacías quedan como hasta ahora.

### Carta del jugador

- Misma estética, horizontal: retrato de 58 dp a la izquierda y, a la derecha, «Warden · Nv N», «ATQ · CUR · DEF», las dos barras y los iconos.
- Ocupa el ancho completo, como la tarjeta actual.

### Barras 3D (`GameBar`, sustituye a `StatBar` en combate)

- **Aspecto:**
  - canal hundido (degradado oscuro con sombra interior);
  - relleno con degradado vertical de 3 tonos (vida: rojo claro → rojo → granate; power: azul claro → azul → azul marino);
  - brillo blanco translúcido en el 35 % superior;
  - texto «actual/máx» centrado con sombra.
- **Al bajar:**
  - el relleno cae en unos 250 ms;
  - una **estela** dorada clara (`#F3E0A0`) se queda en el valor anterior y lo alcanza a los 350 ms, en unos 900 ms;
  - destello de brillo (×2 → ×1 en unos 350 ms).
- **Al subir:**
  - la estela verde clara va primero hasta el valor nuevo y el relleno la sigue a los 300 ms;
  - halo verde unos 700 ms.
- **Power:** igual, con estela azul clara.
- **Animación:** `Animatable` y `animateFloatAsState`; en pausa se congelan las animaciones en curso.

### Números flotantes (sustituyen a la tira de la derecha)

- **Origen.** Cada `EffectResolved` y `PotionUsed` crea un número en la carta del objetivo; la poción va en la carta del rival que la bebe.
- **Posición.** Punto aleatorio dentro de la carta, con margen para que el número quepa. Usa un `Random` de UI: es presentación y no afecta a la partida.
- **Texto.** Igual que hoy: «−1840 SP2», «+950 CU1», «−3200 GD», «+150 PO». El código del combo va a la mitad de tamaño.
- **Colores.** Los actuales: daño en rojo, cura en verde y poción en azul.
- **Tamaño.** `fontSizeSp = 13 + 23 × sqrt(clamp(amount / maxHpObjetivo / 0.15, 0, 1))`, es decir, de 13 sp a 36 sp; en la poción se usa el power máximo. Es una función pura (`floatingNumberSize`) con test.
- **Golpe fuerte.** Con `rollPct ≥ 130` el número lleva halo (sombra de color más ancha).
- **Animación** (unos 1,1 s):
  1. aparece a escala 0,3;
  2. pica a 1,25 al 15 % del tiempo;
  3. vuelve a 1 al 30 %;
  4. sube 26 dp mientras se desvanece.
- **Límites.** Máximo 8 números vivos por carta (los más antiguos se descartan). Se congelan en pausa. Sustituye a `CombatTextStrip` / `CombatTextController`; se reutiliza su lógica de cola y de pausa.

## 4. Pruebas

- **`:engine`, TDD:**
  - con 1000 pulsos, `rollPct` está siempre en [50, 150] y la media cae en 95–105;
  - la misma semilla da los mismos eventos;
  - con `rollMinPct = rollMaxPct = 100` se obtienen los valores exactos antiguos;
  - escudos, Daze y multiplicadores no varían;
  - todos los rivales del contenido tienen `portrait` válido y cada jefe tiene el suyo.
- **`:app`:**
  - `floatingNumberSize` (mínimo, máximo y monotonía);
  - test Compose de la carta de rival (nombre, «Nv», textos de las barras, retrato por código cuando no hay imagen);
  - test Compose de la carta del jugador;
  - el ViewModel lleva `portrait` y `rollPct` a la UI;
  - se adaptan los tests existentes de la tira de números.
- **`:sim`:** re-simulación con tabla antes/después.
- **Verificación:**
  - `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug` en verde;
  - instalación en el móvil;
  - captura nueva en `docs/screenshots/v3/combat.png`.
