# Contenido de la historia — Diseño

Subproyecto 3 de 4 (combate ✓ → historia ✓ → **contenido** → app). Formato de datos: spec del motor de historia §4 (`2026-10-08-story-engine-design.md`). Diseño aprobado en la sesión del 2026-10-08.

## 1. Objetivo

Escribir los 15 JSON reales de `engine/src/main/resources/story/` (309 nodos):
- texto en español;
- prompt de imagen en inglés;
- etiquetas de opción;
- encuentros con rivales del bestiario (§3).

Todo debe pasar el validador y las reglas de contenido (§5), comprobadas por tests.

## 2. Ambientación y estilo

- **Protagonista:** el Warden, un humano de Bree con ganas de aventura, viaja de Bree hasta el Monte del Destino (Libro II) entre finales de 3018 y principios de 3019 de la Tercera Edad, en paralelo a la Comunidad del Anillo. Oye rumores de ellos y encuentra sus huellas, pero **no altera el canon** ni sustituye sus hechos (no mata al Balrog, no se une a la Comunidad).
- **Texto:** historia original. Prohibido citar o parafrasear pasajes de los libros. Segunda persona del presente ("Cruzas el portón…"), de 60 a 120 palabras por nodo y tono épico-sobrio.
- **Nodo de llegada (nivel 1):** presenta la zona.
- **Nodos de nivel 2 a 6:** un episodio cada uno: encuentro, hallazgo, descanso o peligro.
- **Nodo de nivel 7:** el clímax de la zona; es el combate contra el jefe, salvo en las zonas de descanso (Rivendel, Lothlórien).
- **Etiquetas `choiceLabel`:** de 2 a 6 palabras, en imperativo y describiendo la acción hacia **el destino** ("Seguir el arroyo"). Cada destino lo ofrecen 2 o 4 nodos padre distintos, así que la etiqueta debe tener sentido desde cualquiera de ellos: nada de referencias a lo que acaba de pasar en un nodo concreto. Las dos opciones de un mismo nodo deben ser distintas.
- **`imagePrompt`:** en inglés, de 25 a 60 palabras. Describe la escena (lugar, luz, personajes o criaturas visibles, acción) sin nombres propios de personas reales ni de actores, y termina **siempre** con el sufijo exacto:
  `, painterly dark fantasy illustration, Middle-earth, muted earthy palette, 2D game card art, no text`
- **Monte del Destino (`orodruin_final`):** epílogo de llegada al Monte del Destino y cierre de la aventura, de 100 a 160 palabras.

## 3. Bestiario

Cada rival del contenido debe usar exactamente el `pattern` y el `everyNTicks` de una entrada de esta tabla. El `name` puede variar (por ejemplo, "Bandido de Bree"), pero el tipo de rival tiene que reconocerse.

**Rivales comunes:**

| Rival | pattern | everyNTicks | Zonas |
|---|---|---|---|
| Bandido del camino | `12, 121` | 2 | bree, chet |
| Lobo hambriento | `12, 121, 1212` | 2 | bree, chet, cima |
| Moscardones de Moscagua | `32, 323, 13` | 2 | moscagua |
| Espía dunlendino | `13, 131, 31` | 2 | moscagua, cima, puente, bruinen, eregion |
| Huargo | `12, 121, 1212, 12121` | 2 | puente, quebradas, bruinen |
| Crebain | `12, 121, 1212, 31` | 2 | eregion, caradhras |
| Lobo de Saruman | `13, 131, 1313` | 2 | eregion, caradhras, durin |
| Trasgo de Moria | `13, 131` | 1 | durin, moria, sombrio |
| Orco capitán | `11, 12, 121, 1212` | 1 | moria |
| Trol de las cavernas | `2132, 12, 121, 1212, 12121` | 2 | moria |
| Orco de las Montañas Nubladas | `11, 12, 121, 1212, 22` | 2 | quebradas, sombrio, nimrodel |
| Arquero orco | `32, 323` | 1 | sombrio, nimrodel |

**Jefes** (solo en el nivel 7 de su zona; `levelOffset` entre +2 y +15, ajustado por jefe con el simulador; ver `docs/balance/tuning.md`, pasos 4 a 6, «Combate v2», «Power ×3 y escala ×100», «Rivales que se curan y MAESTRO», «Dificultad base subida (+ combo 321)», «Pociones, cadenas de daño y Daze sin bucle», «Más enemigos y dificultad +», «Daño variable 50–150 %» y, para los jefes del camino a Mordor, «Libro II: hasta el Monte del Destino»). El motor marca al jefe (primer rival de un encuentro de nivel 7, `RivalSpec.isBoss`, que pasa a `Fighter.isBoss` para que quede disponible para la UI y el simulador) y le aplica su propia curva de stats, `RivalScaling.BOSS`, en lugar de la de los comunes (`RivalScaling.DEFAULT`):

| Zona | Jefe | pattern | everyNTicks | levelOffset |
|---|---|---|---|---|
| bree | Cabecilla de los bandidos | `12, 121, 11, 31` | 4 | +2 |
| chet | Lobo gris alfa | `12, 121, 1212, 12121, 31` | 1 | +3 |
| moscagua | Reina del enjambre | `32, 323, 3232, 13, 131, 1313, 31` | 1 | +2 |
| cima | Espectro del Anillo | `32, 323, 3232, 12, 121, 31` | 1 | +5 |
| puente | Jefe de la manada de huargos | `12, 121, 1212, 13, 131, 31, 11` | 1 | +2 |
| quebradas | Trol de piedra | `12, 121, 1212, 13, 131, 31` | 1 | +6 |
| bruinen | Jinete Negro | `32, 323, 3232, 13, 131, 3123, 31232, 31` | 1 | +3 |
| eregion | Capitán dunlendino | `12, 121, 1212, 13, 131, 31, 123` | 1 | +3 |
| caradhras | Furia del Caradhras | `32, 323, 3232, 12, 121, 1212, 31, 123` | 1 | +3 |
| durin | Guardián del Agua | `13, 131, 1313, 12, 121, 1212, 31, 123` | 1 | +5 |
| moria | Sombra de Durin | `32, 323, 3232, 13, 131, 31, 123` | 1 | +3 |
| sombrio | Capitán orco de las Montañas Nubladas | `12, 121, 1212, 13, 131, 31, 123` | 1 | +6 |
| nimrodel | Jefe de los arqueros orcos | `32, 323, 3232, 12, 121, 31` | 1 | +4 |

Criterios de diseño de los patrones (combate v2, spec `2026-10-12-combat-v2-design.md` §8): solo usan combos del catálogo v2 y los combos con tier siguen cadenas reales (el tier n va justo después del tier n−1 de su familia en el orden cíclico del patrón) para aprovechar los bonus de cadena; la mayoría de los comunes recorre una sola cadena. **Todos los rivales hacen daño** (2026-10-13): al menos la mitad de los combos de cada patrón, común o jefe, son de daño (Lanza, Lanza cruzada, Puño en área, Golpe directo o Robo de vida) y cada patrón recorre al menos una cadena de daño real; lo comprueba `ContentRulesTest`. Los lobos y huargos encadenan Lanza; el Lobo de Saruman y el Trasgo, Lanza cruzada; los espías encadenan Lanza cruzada y aturden una vez; los crebain picotean con una cadena de Lanza hasta el tier 3 y aturden una vez; los arqueros usan Puño en área y los moscardones encadenan Puño en área y pican con Lanza cruzada. Los rivales que no pueden pagar el combo que les toca beben una poción (+50 % del power máximo; spec de combate v2, §6), así que ninguno se queda sin atacar. Cada jefe conserva su carácter (el Espectro y el Jinete Negro, sin el antiguo Miedo, castigan en área, el Espectro aturde y el Jinete roba vida; el Trol y los capitanes orcos encadenan Lanza con Furia; el Guardián del Agua golpea con sus tentáculos y aturde; la Sombra de Durin quema en área y remata con el Golpe directo), abre con un combo de daño (familias Lanza, Lanza cruzada, Puño en área, Golpe directo o Robo de vida), al menos la mitad de sus combos hacen daño y como mucho tiene una cura propia, para que el combate no se atasque. Los patrones, `everyNTicks` y `levelOffset` de la tabla son los del re-balance v2 (`docs/balance/tuning.md`, «Combate v2»; los offsets de cima y durin, de «Power ×3 y escala ×100»; el `everyNTicks` del Lobo gris alfa y del Jefe de los arqueros orcos y los offsets de chet, caradhras y sombrío, de «Dificultad base subida (+ combo 321)»; los patrones del Lobo gris alfa, el Jinete Negro y el Jefe de los arqueros orcos y los offsets de chet, cima, puente y caradhras, de «Más enemigos y dificultad +»; los offsets de eregion y durin, de «Daño variable 50–150 %»; desde el 2026-10-17 los patrones, `everyNTicks` y offsets de todos los jefes salvo Bree son los de «Escudo táctico: inmunidad total», ver la sección de modos más abajo): todos los jefes llevan al menos un Aturdir (`31`), que es lo que da variedad a sus combates, y ninguno encadena más allá del tier 3 salvo el Lobo gris alfa. Bree es una zona tutorial: su jefe solo llega al tier 2 y lanza cada 4 ticks, el foso del poniente (bree_3_3) y la manada (bree_4_5) son combates de un solo rival y ningún grupo pasa de 2 rivales. El puente pelea sin escolta.

**Más enemigos (2026-10-13, `docs/balance/tuning.md`, «Más enemigos y dificultad +»):** desde Moscagua (la 3.ª zona) cada zona con combates tiene al menos un encuentro de 4 rivales, y las zonas tardías (Caradhras, Durin, Moria, Sombrío y Nimrodel) al menos dos; en total hay 16 grupos de 4 y 12 de 3 (antes 0 y 10). Lo comprueba `RealContentTest.groupSizesPerZone`. Escoltas de jefe (offset −4): Bree, un bandido; chet, dos lobos jóvenes; moscagua, una guardiana del enjambre y un espía dunlendino; quebradas, un huargo; eregion, un espía y un lobo de Saruman; moria, un trol de las cavernas y un trasgo (la Sombra de Durin alcanza al Warden en el puente con sus perseguidores detrás); sombrío, un arquero y un trasgo; nimrodel, un orco de las montañas y un arquero. Los demás jefes (cima, puente, bruinen, caradhras, durin) pelean solos.

La Sombra de Durin es un encuentro de supervivencia en el Puente de Khazad-dûm: el Warden la contiene, no la derrota ni la sustituye a ojos del canon. El texto lo presenta como escapar o resistir en lugar de matarla, aunque mecánicamente sea un combate normal.

**Camino a Mordor (spec `2026-10-16-road-to-mordor-design.md`, ya en la ruta):** las zonas nuevas están en `engine/src/main/resources/story/<id>.json` y `RealContentTest` las valida con estas tablas (`ContentRules`/`Bestiary`). Los patrones, `everyNTicks` y offsets de las tablas son los del ajuste con el simulador (`docs/balance/tuning.md`, «Libro II: hasta el Monte del Destino»): los comunes que forman grupos de 3 y 4 lanzan cada 3 ticks (con nivel 40 o más, el offset −4/−5 de un grupo debilita menos que en el Libro I) y los Huargos del Anduin son una variante de los Huargos que también lanza cada 3 ticks (los de los Argonath siguen siendo los Huargos de siempre). Lothlórien pasa a ser una zona de descanso como Rivendel (`Route.REST_ZONES`): sin comunes ni jefe.

Comunes nuevos o reutilizados:

| Rival | pattern | everyNTicks | portrait | Zonas |
|---|---|---|---|---|
| Arquero orco | `32, 323` | 1 | `orc_archer` | anduin |
| Huargo | `12, 121, 1212, 12121` | 2 | `warg` | argonath |
| Huargo del Anduin | `12, 121, 1212, 12121` | 3 | `warg` | anduin |
| Crebain | `12, 121, 1212, 31` | 2 | `crebain` | amonhen |
| Uruk-hai | `11, 12, 121, 1212, 12121` | 3 | `uruk` | argonath, amonhen |
| Orco de Mordor | `13, 131, 1313, 11` | 3 | `mordor_orc` | emynmuil, gorgoroth |
| Lobo de las rocas | `12, 121, 1212, 12121, 31` | 3 | `wolf` | emynmuil |
| Fuego fatuo | `32, 323, 3232, 31` | 3 | `wisp` | cienagas |
| Espectro de la ciénaga | `3123, 31232, 13, 31` | 2 | `marsh_wraith` | cienagas |
| Guerrero haradrim | `11, 12, 121, 1212, 123` | 3 | `haradrim` | ithilien |
| Arquero haradrim | `32, 323, 3232` | 3 | `haradrim` | ithilien |
| Orco de Morgul | `13, 131, 1313, 31` | 3 | `mordor_orc` | cirithungol |
| Araña | `13, 131, 31, 313` | 2 | `spider` | cirithungol |
| Uruk negro | `11, 12, 121, 1212, 12121, 22` | 2 | `uruk` | gorgoroth |
| Olog-hai | `2132, 13, 131, 1313, 13131` | 2 | `troll` | gorgoroth |

Jefes nuevos (retrato `boss_<zona>`; todos abren con daño, al menos la mitad de sus combos hacen daño, llevan al menos un `31` y no pasan del tier 3):

| Zona | Jefe | pattern | everyNTicks | levelOffset |
|---|---|---|---|---|
| anduin | Cabecilla de los saqueadores del río | `12, 121, 1212, 13, 131, 31, 32, 323, 123` | 1 | +8 |
| argonath | Capitán uruk de la Mano Blanca | `12, 121, 1212, 13, 131, 1313, 31, 123` | 1 | +11 |
| amonhen | Caudillo uruk-hai | `13, 131, 1313, 12, 121, 1212, 31, 123` | 1 | +12 |
| emynmuil | Trol de las colinas | `12, 121, 1212, 32, 323, 3232, 31, 123` | 1 | +2 |
| cienagas | Señor de los ahogados | `3123, 31232, 13, 131, 1313, 31, 32, 323, 123` | 1 | +10 |
| ithilien | Olifante de guerra | `32, 323, 3232, 12, 121, 123, 31` | 1 | +2 |
| cirithungol | Ella-Laraña | `13, 131, 1313, 12, 121, 1212, 31, 123` | 1 | +8 |
| gorgoroth | Nazgûl en su bestia alada | `32, 323, 3232, 3123, 31232, 31, 123` | 1 | +4 |

**Jefes con modo ataque y modo defensa (spec `2026-10-17-boss-modes-design.md` §1–§2):** los 21 jefes ya no llevan `reaction`; llevan `modes` (`defenseBelowPct`, `attackAbovePct`, `maxDefenses` y `defense`), última clave del rival en el JSON. El `pattern` de la tabla es su **patrón de ataque**: sin escudos (`2131`, `2132`) ni curas (`21…`), que pasan a la defensa. Al quitarlos, el Capitán dunlendino (eregion) y el Jefe de los arqueros orcos (nimrodel) pierden su Escudo táctico y el Trol de las colinas (emynmuil) su Escudo maestro; los tres siguen cumpliendo las reglas (daño ≥ 50 %, cadena de daño real y `31`), así que no hizo falta sustituirlos por otro combo.

- **Todos los jefes salvo el de Bree:** `defenseBelowPct 50`, `attackAbovePct 70`, `maxDefenses 3` (umbral y número de defensas ajustados por jefe en el re-balance, tabla de abajo) y `defense` = `2132, 21, 212, 2121, 21212` más los dos primeros combos de daño de su patrón de ataque, en ese orden (`ContentRules.bossModes`). El escudo es el Escudo maestro (`2132`, reduce el daño a la mitad), nunca el Escudo táctico (`2131`): este da inmunidad al aturdimiento y anularía la contra prevista, aturdir al jefe para cortarle la cadena de curas.
- **Bree (tutorial):** `defenseBelowPct 40`, `attackAbovePct 60`, `maxDefenses 1` y `defense` = `21, 212, 12`, sin escudo.
- Toda defensa termina con al menos un combo de daño: es adonde salta el jefe si queda aturdido en defensa.

La migración se hizo con `tools/content/add_boss_modes.py` (idempotente). Los umbrales y la defensa de cada jefe pueden ajustarse en el re-balance (`docs/balance/tuning.md`, «Jefes con modo ataque y defensa»).

**Re-balance de los modos** (`docs/balance/tuning.md`, «Jefes con modo ataque y defensa»; `ContentRules.BOSS_MODE_TUNING`, aplicado al JSON con `tools/content/tune_boss_modes.py`): con 50/70/3 los jefes eran demasiado duros (MAESTRO 47–89 %, MEDIO 14–82 %), así que 14 jefes defienden más tarde o menos veces. `attackAbovePct` sigue en 70 y la defensa (escudo, curas y los dos primeros combos de daño) no cambia; los offsets tampoco. En su última defensa permitida (`defenses == maxDefenses`) todo jefe lanza el patrón de defensa una sola vez y vuelve al ataque tenga la vida que tenga (spec de modos §2), así que con `maxDefenses 1` la defensa es un único ciclo.

**Escudo táctico: inmunidad total** (2026-10-17, `docs/balance/tuning.md`, sección homónima; prevalece sobre los patrones, offsets y umbrales anteriores): el Escudo táctico del jugador vuelve a dar inmunidad total al Daze, así que el MAESTRO, que se escuda con él contra todo jefe que aturde, ganaba el 96–100 % de los combates de jefe. Se re-ajustaron los 20 jefes salvo Bree: casi todos los patrones de ataque añaden una segunda cadena de daño (Lanza, Lanza cruzada o Puño en área hasta el tier 2 o 3) y pierden el `31` repetido, la Furia o la Guardia que llevaban (siguen con un solo `31`, daño ≥ 50 %, abren con daño y no pasan del tier 3 salvo el Lobo gris alfa); los dos primeros combos de daño, y con ellos la cola de la defensa, no cambian. El Capitán dunlendino (eregion) lanza ahora cada tick. Offsets de +2 a +12 y umbrales de defensa de las tablas de arriba (`tools/content/tune_boss_modes.py`, tablas `TUNING` y `ATTACK`). Varios jefes pierden algo de carácter (el Guardián del Agua o la Furia del Caradhras suman una cadena de Lanza y un Golpe directo).

| Jefe | `defenseBelowPct` | `maxDefenses` |
|---|---|---|
| chet, moscagua, emynmuil | 10 | 1 |
| cima, bruinen, eregion, caradhras, durin, moria, anduin, argonath, amonhen, cienagas, ithilien, cirithungol, gorgoroth | 15 | 1 |
| nimrodel | 20 | 1 |
| quebradas, sombrio | 20 | 2 |
| puente | 50 | 1 |
| bree (tutorial) | 40 (`attackAbovePct` 60) | 1 |

**Reacciones por vida (rivales que se curan, spec `2026-10-16-rival-healing-design.md` §2–§3):** algunos comunes llevan, además del patrón, una `reaction` (`atHpPct`, `shield` opcional y `combos`) que lanzan en ticks seguidos al bajar de cada umbral de vida (una vez por umbral; si quedan aturdidos, la cortan). Va como última clave del rival en el JSON. Hasta los modos (2026-10-17) también la llevaban los jefes.

- **Comunes:** en cada encuentro de los niveles 2 a 6 con algún humanoide (retratos `bandit`, `dunlending`, `goblin`, `orc`, `orc_archer`, `uruk`, `mordor_orc` y `haradrim`), solo el primero de ellos lleva `atHpPct [30]` y `combos ["21212"]`, sin escudo: se cura uno por grupo. Las escoltas de un jefe no la llevan (en ese grupo ya se cura el jefe, en su modo defensa), ni tampoco las bestias y los espectros (lobos, huargos, crebain, enjambres, arañas, fuegos fatuos, espectros de la ciénaga y olog-hai).
- Los bandidos de Bree y Chet ya no llevan la Cura I (`21`) en el patrón (`12, 121`, que sigue cumpliendo los criterios: todo daño y cadena de Lanza): ningún patrón lleva `21`.

La migración de las reacciones se hizo con `tools/content/add_reactions.py` (única e idempotente).

Retratos comunes: a los 12 actuales se suman `uruk`, `mordor_orc`, `wisp`, `marsh_wraith`, `haradrim` y `spider` (18 en total, `ContentRules.COMMON_PORTRAITS`).

## 4. Zonas y sinopsis

`baseLevel` = `1 + 3·(k−1)` para k ≤ 8 y `1 + 3·(k−2)` para k ≥ 9 (la zona final vale 0). Rivendel no da combates, así que a partir de Eregion la curva se desplaza una zona.

| # | id | baseLevel | Sinopsis (guía de continuidad) | Comunes |
|---|---|---|---|---|
| 1 | bree | 1 | Pueblo de hombres y hobbits; la posada del Poney Pisador, rumores de jinetes negros y de un montaraz; el Warden decide partir hacia el este por el Camino. | bandido, lobo hambriento |
| 2 | chet | 4 | Bosque denso al este de Bree; senderos de cazadores, campamentos abandonados y lobos al acecho. | lobo hambriento, bandido |
| 3 | moscagua | 7 | Pantanos de niebla y moscardones; agua estancada y huellas de viajeros recientes. Aparecen espías. | moscardones, espía |
| 4 | cima | 10 | Amon Sûl en ruinas; frío, viento, señales de fuego y la sombra de los Nueve. | espía, lobo hambriento |
| 5 | puente | 13 | El Último Puente sobre el Fontegrís; tierras desoladas y aullidos de huargos. | huargo, espía |
| 6 | quebradas | 16 | Colinas boscosas con cuevas de trolls, tesoros olvidados y estatuas de piedra. | huargo, orco de las Montañas |
| 7 | bruinen | 19 | El vado del Bruinen; persecución, agua crecida y el límite del poder de Elrond. | huargo, espía |
| 8 | rivendel | 22 | Refugio: descanso, consejo, forja y saber élfico. **Sin combates.** El nivel 7 es la despedida. | (ninguno) |
| 9 | eregion | 22 | Hollin, antigua tierra élfica en ruinas; acebos, crebain espiando desde el cielo. | crebain, lobo de Saruman, espía |
| 10 | caradhras | 25 | Paso nevado del Cuerno Rojo; ventisca, aludes y la montaña hostil. | lobo de Saruman, crebain |
| 11 | durin | 28 | El lago oscuro ante las Puertas de Moria; el muro, las palabras ocultas y el agua que se mueve. | lobo de Saruman, trasgo |
| 12 | moria | 31 | Khazad-dûm: salas inmensas, la Cámara de Mazarbul, tambores en lo profundo y el Puente. | trasgo, orco capitán, trol de las cavernas |
| 13 | sombrio | 34 | Valle del Arroyo Sombrío (Azanulbizar) a la salida de Moria; el Espejo del lago y orcos persiguiendo. | orco de las Montañas, arquero orco, trasgo |
| 14 | nimrodel | 37 | El río Nimrodel y la linde de Lórien; aguas que sanan el ánimo y orcos que acechan entre los árboles. | arquero orco, orco de las Montañas |
| 15 | lothlorien | 0 | Nodo final: llegada a Caras Galadhon, final de la aventura. | — |

## 5. Reglas de encuentros (comprobadas por tests)

1. **Nivel 1:** sin encuentro.
2. **Nivel 7 (zonas 1–14 salvo `rivendel`):** encuentro con el jefe de la zona como primer rival, con un offset de +2 a +15 que se ajusta por jefe (tabla de §3), opcionalmente acompañado de 1 o 2 comunes de la zona con offset −4 (escoltas de §3).
3. **Niveles 2 a 6:** encuentros de 1 a 4 rivales comunes de la zona. Offsets:
   - 1 rival: 0 o +1;
   - 2 rivales: −2;
   - 3 rivales: −4;
   - 4 rivales: −5.

   Desde la 3.ª zona (Moscagua), cada zona con combates tiene al menos un encuentro de 4 rivales (dos en Caradhras, Durin, Moria, Sombrío y Nimrodel); en Bree ningún encuentro pasa de 2 rivales (`RealContentTest.groupSizesPerZone`).
4. **Combates por camino:** todo camino posible del nivel 1 al 7 de una zona tiene entre **2 y 4** nodos con encuentro, contando el del jefe. En `rivendel` son 0.
5. **Cada rival** usa un `pattern` y un `everyNTicks` del bestiario. Los comunes deben pertenecer a la lista de su zona; el jefe, al de su zona.
6. **`baseLevel`** según §4.
7. **`imagePrompt`** termina con el sufijo exacto de §2. Los textos (`text`) tienen entre 40 y 160 palabras; el de `orodruin_final`, entre 100 y 160. El rango de los tests es algo más amplio que el objetivo de §2.
8. **Reacciones** (spec `2026-10-16-rival-healing-design.md` §3): en cada encuentro de los niveles 2 a 6 con humanoides la lleva exactamente el primer humanoide (`[30]`, `21212`) y ningún otro rival; los jefes, las escoltas, las bestias y los espectros no la llevan; los combos de la reacción existen y curan (`ContentRules`, `ContentRulesTest.detectsReactionRules` y `reactionCombosFormChain`, `RealContentTest.commonsUnchanged`).
9. **Modos de los jefes** (spec `2026-10-17-boss-modes-design.md` §1): cada jefe lleva los `modes` de §3 y ninguna `reaction`; su patrón de ataque no tiene escudos ni curas y mantiene las reglas (daño ≥ 50 %, cadena de daño real y `31`); su defensa es `2132`, la cadena de Cura completa y los dos primeros combos de daño del ataque (Bree: `21, 212` y uno) y termina siempre con un combo de daño; ningún común ni escolta lleva `modes` (`ContentRules.bossModes` y `modesErrors`, `ContentRulesTest.attackPatternsHaveNoShieldsOrHeals`, `defenseFollowsRule` y `detectsModesRules`, `RealContentTest.everyBossHasModes` y `noBossHasReaction`).

## 6. Organización del trabajo

- Se escribe en 5 tandas de 3 zonas, en orden de ruta, con revisión de cada tanda (continuidad, canon, reglas).
- Un test de contenido real (`RealContentTest`) comprueba, para cada zona ya escrita, el validador (errores de esa zona), las reglas de §5 y que el bestiario se use bien. Al final verifica los 15 archivos juntos con `StoryContentLoader.loadFromResources()` + `ContentValidator.validate` sin errores.
- Al final se genera `docs/content/prompts-imagenes.md`: una tabla con el id de nodo (nombre del archivo de imagen) y el prompt, lista para que el usuario genere las imágenes.
