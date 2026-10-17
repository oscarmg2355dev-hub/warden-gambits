# Ajuste de dificultad (2026-10-09)

El objetivo es la dificultad "Exigente", con estas metas:

| Perfil | Aventura completa | Rango aceptado |
|---|---|---|
| NOVATO | ≈5 % | 2–10 % |
| MEDIO | ≈25 % | 20–30 % |
| EXPERTO | ≈60 % | 50–70 % |

Para MEDIO hay además metas por combate:

- combates normales ≈99 % de media, y ninguno por debajo del 95 %;
- jefes ≈90 %, y ninguno por debajo del 75 % ni por encima del 98 %.

Se usa el mismo simulador y las mismas semillas que en la línea base (`baseline.md`): 300 combates por encuentro al nivel esperado y 2000 aventuras por perfil.

## Cambios aplicados

### Paso 1: curva de stats propia para los rivales

El código está en `engine/.../model/Stats.kt`. Hay una clase nueva, `RivalScaling(hpPct, attackPct, healPct, defensePct, startPowerPct, regenPct)`, con porcentajes enteros. Los stats del rival salen de `LevelStats.forRival(level, scaling)`, que aplica esos porcentajes sobre `LevelStats.forLevel(level)`:

- Las cuentas son enteras.
- PV, ataque, curación y regeneración valen como mínimo 1; la defensa, como mínimo 0.
- El poder máximo no cambia.

`CombatEngine(rivalScaling = RivalScaling.DEFAULT)` usa esa curva solo con los rivales. Cada rival empieza con `maxPower · startPowerPct / 100` de poder. El jugador sigue usando `LevelStats.forLevel` y empieza con todo el poder.

### Paso 2: cadencia del bestiario

Lobo hambriento, Huargo, Lobo de Saruman y Orco de las Montañas Nubladas pasan a `everyNTicks = 2`; antes era 1. El cambio está en tres sitios:

- la tabla §3 de `docs/superpowers/specs/2026-10-08-story-content-design.md`;
- el `Bestiary` de los tests;
- los 96 rivales con esos patrones en `engine/src/main/resources/story/*.json`.

Después se volvió a barrer y a ajustar `RivalScaling.DEFAULT`.

## Valores elegidos

```kotlin
RivalScaling.DEFAULT = RivalScaling(hpPct = 90, attackPct = 52, healPct = 100, defensePct = 100, startPowerPct = 92, regenPct = 100)
```

Los rivales tienen el 90 % de los PV del jugador del mismo nivel y el 52 % de su ataque, y empiezan con 92 de poder. La curación, la defensa y la regeneración son las mismas que las del jugador.

### Por qué estos valores

Es el punto del barrido más cercano a las tres metas de aventura a la vez. La suma de las distancias a 5/25/60 es 4,5 puntos, la menor del barrido fino. Los puntos vecinos quedaban así:

| Punto (PV/ataque/poder inicial) | NOVATO | MEDIO | EXPERTO |
|---|---|---|---|
| 90/50/100 | 4,2 | 26,8 | 56,8 |
| 95/52/85 | 6,9 | 21,5 | 58,5 |
| 90/50/92 | 8,1 | 28,4 | 60,4 |

Algunas observaciones del barrido:

- **El poder inicial es la palanca que separa los perfiles.** Con un poder inicial bajo, los rivales tardan en arrancar y hasta NOVATO gana casi todo. Con 85–100, EXPERTO saca a MEDIO unos 30 puntos.
- **El ataque es la palanca más sensible.** Subir 3 puntos de ataque cuesta unos 8 puntos de aventura completa a MEDIO.
- **No hay punto con MEDIO ≈25 % y EXPERTO ≈60 % exactos.** Con esta política de jugador simulado, EXPERTO queda unos 30–33 puntos por encima de MEDIO, así que con MEDIO a 25 % EXPERTO se queda en ≈57 %, dentro del rango.

Para el paso 1, antes del cambio de cadencia, se había elegido `RivalScaling(75, 50, 100, 100, 85, 100)`.

## Resultados

En las tablas, "Base" es la línea base sin cambios, "Paso 1" es solo la curva de rivales 75/50/85 y "Paso 2" es la cadencia más la curva 90/52/92, que es el estado final.

### Aventura completa (% de partidas que llega a Lothlórien)

| Perfil | Base | Paso 1 | Paso 2 | Meta |
|---|---|---|---|---|
| NOVATO | 0,0 % | 4,9 % | **6,0 %** | 2–10 % |
| MEDIO | 0,0 % | 27,6 % | **24,9 %** | 20–30 % |
| EXPERTO | 0,0 % | 57,5 % | **56,6 %** | 50–70 % |

Combates de las aventuras que llegan al tope de 600 ticks y cuentan como derrota:

| Perfil | Base | Paso 2 |
|---|---|---|
| MEDIO | 285 | 192 |
| EXPERTO | 362 | 15 |

Casi todos son parejas de bandidos que se curan (`bree_3_1`, `chet_4_1`).

Solo con el cambio de cadencia, sin reajustar la curva (75/50/85), las aventuras completas quedaban en NOVATO 22,2 %, MEDIO 68,0 % y EXPERTO 82,3 %. Por eso se volvió a ajustar la curva.

### MEDIO: % de partidas que llega a cada zona

| Zona | Base | Paso 1 | Paso 2 |
|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 |
| chet | 12,1 | 100,0 | 94,6 |
| moscagua | 1,3 | 99,2 | 90,4 |
| cima | 1,2 | 99,2 | 90,4 |
| puente | 0,4 | 95,8 | 90,4 |
| quebradas | 0,0 | 90,5 | 85,9 |
| bruinen | 0,0 | 78,5 | 80,1 |
| rivendel / eregion | 0,0 | 68,4 | 74,4 |
| caradhras | 0,0 | 56,9 | 56,4 |
| durin | 0,0 | 41,3 | 47,1 |
| moria | 0,0 | 35,8 | 40,6 |
| sombrio | 0,0 | 34,3 | 34,5 |
| nimrodel | 0,0 | 32,4 | 28,3 |
| lothlorien | 0,0 | 27,6 | 24,9 |

### MEDIO por zona (jefe % / media de normales % / mínimo de normales %)

| Zona | Base | Paso 1 | Paso 2 |
|---|---|---|---|
| bree | 100 / 48,9 / 0 | 100 / 100 / 100 | 100 / 97,9 / 80,7 |
| chet | 0 / 53,4 / 6,3 | 100 / 100 / 100 | 100 / 97,4 / 77,0 |
| moscagua | 100 / 82,1 / 38,0 | 100 / 100 / 100 | 100 / 100 / 100 |
| cima | 100 / 47,6 / 0 | 100 / 100 / 100 | 100 / 100 / 100 |
| puente | 0 / 42,6 / 0 | 100 / 98,5 / 87,7 | 100 / 99,8 / 99,0 |
| quebradas | 100 / 33,4 / 0 | 100 / 90,3 / 32,3 | 100 / 99,0 / 92,7 |
| bruinen | 100 / 36,0 / 0 | 100 / 93,2 / 54,7 | 100 / 98,5 / 90,3 |
| eregion | 0 / 7,0 / 0 | 99,7 / 95,6 / 88,0 | 96,3 / 93,5 / 82,7 |
| caradhras | 100 / 6,1 / 0 | 100 / 89,9 / 38,0 | 100 / 93,0 / 71,0 |
| durin | 100 / 22,6 / 0 | 100 / 90,7 / 20,7 | 100 / 89,8 / 44,3 |
| moria | 100 / 37,5 / 0 | 100 / 97,4 / 88,0 | 100 / 87,4 / 53,7 |
| sombrio | 2,0 / 28,6 / 0 | 100 / 97,1 / 85,7 | 100 / 90,4 / 65,7 |
| nimrodel | 0 / 28,9 / 0 | 100 / 87,6 / 70,3 | 92,0 / 91,6 / 74,7 |

### MEDIO global

| Métrica | Base | Paso 1 | Paso 2 | Meta |
|---|---|---|---|---|
| Media de combates normales | 36,7 % | 95,5 % | 95,2 % | ≈99 % |
| Media de jefes | 61,7 % | 100,0 % | 99,1 % | ≈90 % |
| Normales < 95 % | 80 de 103 | 15 de 103 | 22 de 103 | 0 |
| Jefes fuera de 75–98 % | 13 de 13 | 13 de 13 | 11 de 13 | 0 |

### Los 10 encuentros MEDIO más letales

**Base** (hay 46 con 0 %; estos son los primeros en orden de ruta):

| Nodo | Nivel | Rivales | % |
|---|---|---|---|
| bree_3_3 | 1 | Bandido del foso L1 + Lobo hambriento L1 | 0,0 |
| bree_4_5 | 2 | Lobo hambriento L1 ×2 | 0,0 |
| chet_7_0 | 6 | Lobo gris alfa L6 + Lobo joven L2 | 0,0 |
| cima_3_0 | 10 | Lobo hambriento L9 ×2 | 0,0 |
| cima_3_2 | 11 | Lobo hambriento L8 ×3 | 0,0 |
| puente_4_0 | 13 | Huargo L12 ×2 | 0,0 |
| puente_4_4 | 14 | Huargo L11 ×3 | 0,0 |
| puente_4_7 | 14 | Espía dunlendino L12 + Huargo L12 | 0,0 |
| puente_6_1 | 15 | Huargo L12 ×2 | 0,0 |
| puente_7_0 | 15 | Jefe de la manada L15 + Huargo L11 | 0,0 |

**Paso 1:**

| Nodo | Nivel | Rivales | % |
|---|---|---|---|
| durin_4_4 | 29 | Lobo de Saruman L26 ×3 | 20,7 |
| quebradas_5_2 | 17 | Orco de las Montañas L14 ×3 | 32,3 |
| caradhras_6_0 | 27 | Lobo de Saruman L23 ×3 | 38,0 |
| bruinen_5_2 | 20 | Huargo L17 ×3 | 54,7 |
| nimrodel_2_1 | 37 | Orco de las Montañas L36 ×2 | 70,3 |
| nimrodel_3_2 | 38 | Orco de las Montañas L35 ×2 + Arquero orco L35 | 71,3 |
| nimrodel_5_2 | 39 | Orco de las Montañas L35 ×2 + Arquero orco L35 | 71,7 |
| durin_4_2 | 29 | Trasgo de Moria L26 ×3 | 84,7 |
| sombrio_3_3 | 34 | Orco de las Montañas + Arquero orco + Trasgo L32 | 85,7 |
| puente_4_4 | 14 | Huargo L11 ×3 | 87,7 |

**Paso 2 (final):**

| Nodo | Nivel | Rivales | % | Ticks |
|---|---|---|---|---|
| durin_4_2 | 29 | Trasgo de Moria L26 ×3 | 44,3 | 37,9 |
| moria_4_2 | 32 | Trasgo de Moria L29 ×3 | 53,7 | 41,6 |
| moria_5_1 | 33 | Orco capitán L29 + Trasgo de Moria L29 ×2 | 55,7 | 42,1 |
| sombrio_3_3 | 34 | Orco de las Montañas + Arquero orco + Trasgo L32 | 65,7 | 58,1 |
| durin_4_4 | 29 | Lobo de Saruman L26 ×3 | 69,3 | 40,1 |
| caradhras_6_0 | 27 | Lobo de Saruman L23 ×3 | 71,0 | 32,3 |
| nimrodel_3_2 | 38 | Orco de las Montañas L35 ×2 + Arquero orco L35 | 74,7 | 54,6 |
| chet_4_1 | 4 | Bandido del bosque L3 ×2 | 77,0 | 152,4 |
| sombrio_6_0 | 36 | Orco de las Montañas + Arquero orco + Trasgo L32 | 77,3 | 50,8 |
| nimrodel_5_2 | 39 | Orco de las Montañas L35 ×2 + Arquero orco L35 | 79,0 | 51,0 |

## Desajustes que quedan (estado final)

Las metas de aventura completa se cumplen en los tres perfiles. Las metas por combate de MEDIO, no:

1. **Jefes demasiado fáciles.** 11 de 13 jefes se ganan al 100 %. Solo `eregion_7_0` (96,3 %) y `nimrodel_7_0` (92,0 %) están en rango. Los jefes en solitario abren con `11`, `2131` o `2132`, pierden el ritmo y, con +2 niveles, no compensan.
2. **Grupos de 3 que atacan cada tick.** El Trasgo de Moria (`12, 31`, cada tick) en trío gana el 44–56 % (`durin_4_2`, `moria_4_2`, `moria_5_1`). Los tríos de Lobo de Saruman, Orco de las Montañas y Huargo, aunque ya van cada 2 ticks, siguen entre el 69 y el 93 %. Con 3 rivales los DOT se acumulan: cada `12`/`121` añade un DOT nuevo que ignora la defensa.
3. **Parejas de bandidos que se curan** (`bree_3_1` 80,7 %, `chet_4_1` 77,0 %). Son empates que llegan al tope de 600 ticks: la política simulada reparte daño de área y los rivales se curan. En el juego real el jugador concentraría el daño, así que este desajuste es en parte un artefacto de la política.
4. Hay 22 encuentros normales por debajo del 95 %. La lista completa está en `final-encounters.csv`.

Las propuestas del paso 3, que **no se han aplicado**, están en el informe de la tarea:

- offsets de grupo más fuertes;
- DOT que se refresca por fuente en lugar de acumularse;
- offset de jefe más alto o una curva propia para jefes.

## Cómo volver a ejecutarlo

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :sim:run                      # informe completo; los CSV quedan en sim/build/reports/
./gradlew :sim:run --args="sweep hp=85,90,95 attack=50,52,55 start=85,92,100 runs=2000 fights=300"
./gradlew :sim:run --args="sweep boss attack=70,72,74 runs=2000 fights=300"   # barre RivalScaling.BOSS
./gradlew :sim:run --args="sweep spear=18,20 fist=15,18 cure=25,30"           # barre ComboTuning (combate v2)
./gradlew :sim:run --args="sweep offsets moria 2,4,6,8 levels=-1,0,1"          # offset de un jefe (ver «Combate v2»)
```

El barrido acepta estas claves: `hp`, `attack`, `heal`, `def`, `start`, `regen` y `power` (de `RivalScaling`) y, desde el combate v2, `spear`, `fist` y `cure` (de `ComboTuning`), con listas separadas por comas. Las claves que no se dan conservan el valor actual de la curva barrida y de `ComboTuning.DEFAULT` (desde el combate v2; antes había una rejilla por defecto). También acepta `runs=` (aventuras por perfil; por defecto 1000) y `fights=` (combates por encuentro; por defecto 100). Las semillas son las mismas que las del informe completo. Por cada punto del barrido imprime:

- el % de aventuras completas de cada perfil;
- la media y el mínimo de los combates normales MEDIO;
- la media, el mínimo y el máximo de los jefes MEDIO y cuántos quedan fuera de 75–98 %;
- los combates al tope de ticks;
- por jefe, el % de NOVATO/MEDIO/EXPERTO.

Para aplicar un punto, cambia `RivalScaling.DEFAULT` en `Stats.kt` y vuelve a ejecutar `./gradlew :sim:run`. Los CSV finales están en `final-encounters.csv` y `final-adventures.csv`.

## Paso 3 (2026-10-09)

Cuatro cambios, uno cada vez y con las mismas semillas: offsets de grupo, menos carga en Eregion, curva propia para los jefes y una mejora de la política simulada contra parejas que se curan. El paso 4 (reajustar `RivalScaling.DEFAULT`) no hizo falta: la aventura completa quedó dentro de rango en los tres perfiles.

### 3.1 Offsets de grupo (regla §5)

| Rivales en un encuentro de nivel 2–6 | Antes | Ahora |
|---|---|---|
| 1 | 0 / +1 | 0 / +1 |
| 2 | −1 | −2 |
| 3 | −2 | −4 |
| 4 | −3 | −5 |
| Escoltas del jefe | −2 | −4 |

Se cambiaron la §5 de la especificación, `ContentRules` (tabla `GROUP_OFFSETS` y `ESCORT_OFFSET`, con test nuevo `ContentRulesTest.groupOffsets`) y 160 `levelOffset` en los JSON de las 13 zonas con combates.

### 3.2 Menos carga en Eregion

| Nodo | Antes | Ahora | Texto |
|---|---|---|---|
| `eregion_3_0` | Crebain ×3 (−4) | Crebain ×2 (−2) | "Tres de ellos" → "Dos de ellos"; prompt "three" → "two" |
| `eregion_5_1` | Espía + Lobo de Saruman ×2 (−4) | Espía + Lobo de Saruman (−2) | "dos lobos grises que obedecen…" → "un lobo gris que obedece…"; prompt igual |
| `eregion_6_0` | Espía ×2 + Lobo de Saruman ×2 (−5) | Espía + Lobo de Saruman ×2 (−4) | "dos hombres" → "un hombre", "uno de los hombres" → "el hombre"; prompt igual |

El encuentro del jefe (`eregion_7_0`, con dos escoltas) no se tocó. Ningún camino cambia de número de combates, porque solo cambia el número de rivales.

### 3.3 Curva propia para los jefes

El motor no sabía qué rival es un jefe. Ahora:

- `RivalSpec` tiene `isBoss: Boolean = false`.
- `StoryEngine.combatSetupFor` lo pone a `true` en el primer rival de un encuentro de nivel 7.
- `CombatEngine(rivalScaling, bossScaling, randomFactory)` usa `bossScaling` (por defecto `RivalScaling.BOSS`) con los jefes y `rivalScaling` con el resto.
- `:sim` barre la curva de jefes con `./gradlew :sim:run --args="sweep boss hp=... attack=..."`, que deja los comunes en `DEFAULT` e imprime además el % de cada jefe.

Lo que enseñó el barrido (MEDIO, 300 combates por jefe):

- **Los PV del jefe casi no influyen** (100–400 % dan el mismo resultado). El daño directo del rival es `ataque·% − defensa` y, con el ataque de los comunes (52 %), los jefes apenas pasan la defensa del jugador: el jugador se cura más rápido de lo que el jefe pega. La palanca es el ataque.
- **Cada jefe tiene su propio "precipicio"**. Los de daño directo (chet, puente, eregion, sombrio, nimrodel) pasan del 100 % a menos del 75 % entre un 80 y un 90 % de ataque. Los de área o utilidad (moscagua, cima, bruinen, caradhras) siguen al 100 % incluso con un 150 % de ataque; bree, quebradas, durin y moria solo bajan con un 130–150 % de ataque o el triple de regeneración, cuando los de daño directo ya están cerca del 0 %. Con una sola curva no hay ningún punto con los 13 jefes entre 75 y 98 %.
- Se eligió el punto con la media más cercana a 90 sin dejar ningún jefe por debajo de 75:

```kotlin
RivalScaling.BOSS = RivalScaling(hpPct = 150, attackPct = 80, healPct = 100, defensePct = 100, startPowerPct = 100, regenPct = 100)
```

| Punto de jefe (PV/ataque/poder inicial) | NOVATO | MEDIO | EXPERTO | Jefes media / mín / máx |
|---|---|---|---|---|
| 100/52/92 (igual que los comunes) | 23,8 | 51,3 | 78,5 | 99,8 / 98,0 / 100 |
| 100/70/100 | 9,6 | 44,8 | 76,2 | 98,2 / 87,7 / 100 |
| 100/80/100 | 3,3 | 28,4 | 66,1 | 93,1 / 79,7 / 100 |
| **150/80/100** | **3,2** | **27,6** | **66,1** | **92,8 / 78,0 / 100** |
| 100/82/100 | 2,5 | 25,4 | 64,1 | 92,5 / 77,7 / 100 |
| 100/85/92 | 3,6 | 24,8 | 69,0 | 91,3 / 56,7 / 100 |
| 100/90/92 | 1,2 | 15,2 | 58,8 | 89,5 / 56,0 / 100 |

### 3.4 Política simulada contra parejas que se curan

Los empates de 600 ticks contra parejas de bandidos (`bree_3_1`) eran un artefacto de la política: MEDIO y EXPERTO usaban daño de área y los dos rivales se curaban. Ahora, **si quedan vivos 2 o más rivales con una curación en su patrón**, la política no usa área y concentra el daño de un objetivo en el sanador con menos PV (tests nuevos en `HumanPlayerTest`). Con un solo sanador no cambia nada: una primera versión que siempre atacaba primero al sanador hundía al jefe de Nimrodel (que se cura) hasta el 27 % y `moria_5_1` hasta el 54 %, y se descartó. NOVATO no cambia.

Efecto aislado (mismas semillas, con 3.1–3.3 ya aplicados): `bree_3_1` pasa del 80,7 al 100 %; los combates de MEDIO al tope de ticks en las aventuras bajan de 111 a 3 y en los encuentros (los 3 perfiles) de 58 a 0; MEDIO completa 29,2 % en lugar de 27,6 %. NOVATO y EXPERTO no cambian.

### 3.5 `RivalScaling.DEFAULT`

No se cambió (sigue en 90/52/100/100/92/100), porque la aventura completa está en rango. Barrido de referencia con el resto del paso 3 aplicado:

| Comunes (PV/ataque) | NOVATO | MEDIO | EXPERTO | Normales media / mín |
|---|---|---|---|---|
| 90/50 | 3,7 | 32,7 | 69,3 | 98,7 / 80,0 |
| **90/52 (actual)** | **3,2** | **29,2** | **66,1** | **98,3 / 79,7** |
| 90/54 | 2,2 | 24,7 | 63,4 | 97,8 / 64,3 |
| 100/50 | 3,3 | 21,0 | 61,4 | 97,2 / 58,7 |

90/54 acercaría MEDIO a 25 % y EXPERTO a 60 %, pero hunde el peor encuentro normal hasta el 64 % y deja NOVATO en el borde (2,2 %).

### Resultados del paso 3 (MEDIO salvo que se diga otra cosa)

| Métrica | Antes (paso 2) | 3.1 offsets | 3.2 Eregion | 3.3 jefes | 3.4 política |
|---|---|---|---|---|---|
| NOVATO aventura completa | 6,0 % | 18,3 % | 23,9 % | 3,2 % | 3,2 % |
| MEDIO aventura completa | 24,9 % | 48,7 % | 51,3 % | 27,6 % | 29,2 % |
| EXPERTO aventura completa | 56,6 % | 76,3 % | 78,5 % | 66,1 % | 66,1 % |
| MEDIO combates al tope (aventuras) | 192 | 108 | 108 | 111 | 3 |
| MEDIO normales: media | 95,2 | 98,1 | 98,2 | 98,2 | 98,3 |
| MEDIO normales: mínimo | 44,3 | 79,7 | 79,7 | 79,7 | 79,7 |
| MEDIO normales < 95 % | 22 de 103 | 10 de 103 | 10 de 103 | 10 de 103 | 9 de 103 |
| MEDIO jefes: media | 99,1 | 99,8 | 99,8 | 92,8 | 92,8 |
| MEDIO jefes: mín / máx | 92,0 / 100,0 | 98,0 / 100,0 | 98,0 / 100,0 | 78,0 / 100,0 | 78,0 / 100,0 |
| MEDIO jefes fuera de 75–98 % | 11 de 13 | 12 de 13 | 12 de 13 | 8 de 13 | 8 de 13 |

**Jefes MEDIO por zona**

| Jefe MEDIO | Antes (paso 2) | 3.1 offsets | 3.2 Eregion | 3.3 jefes | 3.4 política |
|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 100,0 | 78,0 | 78,0 |
| moscagua | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| cima | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| puente | 100,0 | 100,0 | 100,0 | 85,0 | 85,0 |
| quebradas | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| bruinen | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| eregion | 96,3 | 100,0 | 100,0 | 80,0 | 80,0 |
| caradhras | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| durin | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| moria | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| sombrio | 100,0 | 100,0 | 100,0 | 79,7 | 79,7 |
| nimrodel | 92,0 | 98,0 | 98,0 | 84,0 | 84,0 |

**NOVATO, % que llega a cada zona**

| Zona | Antes (paso 2) | 3.1 offsets | 3.2 Eregion | 3.3 jefes | 3.4 política |
|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| moscagua | 100,0 | 100,0 | 100,0 | 68,9 | 68,9 |
| cima | 100,0 | 100,0 | 100,0 | 68,9 | 68,9 |
| puente | 100,0 | 100,0 | 100,0 | 68,9 | 68,9 |
| quebradas | 90,5 | 95,9 | 95,9 | 21,6 | 21,6 |
| bruinen | 84,6 | 95,6 | 95,6 | 21,6 | 21,6 |
| rivendel | 72,0 | 91,6 | 91,6 | 21,6 | 21,6 |
| eregion | 72,0 | 91,6 | 91,6 | 21,6 | 21,6 |
| caradhras | 31,7 | 57,1 | 78,8 | 8,9 | 8,9 |
| durin | 20,2 | 45,7 | 62,6 | 8,6 | 8,6 |
| moria | 15,2 | 38,5 | 51,9 | 7,7 | 7,7 |
| sombrio | 11,5 | 30,3 | 39,8 | 6,3 | 6,3 |
| nimrodel | 7,4 | 20,9 | 27,4 | 3,6 | 3,6 |
| lothlorien | 6,0 | 18,3 | 23,9 | 3,2 | 3,2 |

**MEDIO, % que llega a cada zona**

| Zona | Antes (paso 2) | 3.1 offsets | 3.2 Eregion | 3.3 jefes | 3.4 política |
|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 94,6 | 94,6 | 94,6 | 94,6 | 100,0 |
| moscagua | 90,4 | 94,6 | 94,6 | 81,1 | 85,5 |
| cima | 90,4 | 94,6 | 94,6 | 81,0 | 85,4 |
| puente | 90,4 | 94,6 | 94,6 | 81,0 | 85,4 |
| quebradas | 85,9 | 94,1 | 94,1 | 61,7 | 65,2 |
| bruinen | 80,1 | 92,8 | 92,8 | 61,4 | 64,9 |
| rivendel | 74,4 | 91,0 | 91,0 | 60,9 | 64,3 |
| eregion | 74,4 | 91,0 | 91,0 | 60,9 | 64,3 |
| caradhras | 56,4 | 80,4 | 86,5 | 48,1 | 50,9 |
| durin | 47,1 | 74,4 | 79,7 | 45,7 | 48,4 |
| moria | 40,6 | 67,3 | 72,0 | 42,6 | 45,0 |
| sombrio | 34,5 | 61,0 | 64,8 | 39,3 | 41,5 |
| nimrodel | 28,3 | 53,1 | 56,2 | 31,8 | 33,5 |
| lothlorien | 24,9 | 48,7 | 51,3 | 27,6 | 29,2 |

**EXPERTO, % que llega a cada zona**

| Zona | Antes (paso 2) | 3.1 offsets | 3.2 Eregion | 3.3 jefes | 3.4 política |
|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| moscagua | 99,3 | 100,0 | 100,0 | 100,0 | 100,0 |
| cima | 99,3 | 100,0 | 100,0 | 100,0 | 100,0 |
| puente | 99,3 | 100,0 | 100,0 | 100,0 | 100,0 |
| quebradas | 98,6 | 99,9 | 99,9 | 93,9 | 94,0 |
| bruinen | 98,5 | 99,9 | 99,9 | 93,9 | 94,0 |
| rivendel | 95,7 | 99,8 | 99,8 | 93,8 | 93,9 |
| eregion | 95,7 | 99,8 | 99,8 | 93,8 | 93,9 |
| caradhras | 86,7 | 96,6 | 99,6 | 87,6 | 87,6 |
| durin | 77,8 | 93,3 | 95,9 | 85,1 | 85,1 |
| moria | 71,6 | 89,2 | 91,8 | 81,7 | 81,7 |
| sombrio | 64,8 | 83,5 | 86,1 | 76,9 | 76,9 |
| nimrodel | 58,3 | 77,7 | 79,9 | 68,5 | 68,5 |
| lothlorien | 56,6 | 76,3 | 78,5 | 66,1 | 66,1 |

**Los 10 encuentros normales MEDIO más letales (final)**

| Nodo | Nivel | Rivales | % | Ticks |
|---|---|---|---|---|
| sombrio_3_3 | 34 | Orco de las Montañas Nubladas L30 + Arquero orco L30 + Trasgo de Moria L30 | 79,7 | 52,6 |
| moria_4_2 | 32 | Trasgo de Moria L27 + Trasgo de Moria L27 + Trasgo de Moria L27 | 80,0 | 37,1 |
| durin_4_4 | 29 | Lobo de Saruman L24 + Lobo de Saruman L24 + Lobo de Saruman L24 | 80,3 | 33,2 |
| moria_5_1 | 33 | Orco capitán L27 + Trasgo de Moria L27 + Trasgo de Moria L27 | 83,0 | 40,2 |
| durin_4_2 | 29 | Trasgo de Moria L24 + Trasgo de Moria L24 + Trasgo de Moria L24 | 84,0 | 38,1 |
| nimrodel_3_2 | 38 | Orco de las Montañas Nubladas L33 + Orco de las Montañas Nubladas L33 + Arquero orco L33 | 85,3 | 44,8 |
| nimrodel_5_2 | 39 | Orco de las Montañas Nubladas L33 + Orco de las Montañas Nubladas L33 + Arquero orco L33 | 86,7 | 42,0 |
| sombrio_6_0 | 36 | Orco de las Montañas Nubladas L30 + Arquero orco L30 + Trasgo de Moria L30 | 90,7 | 43,1 |
| moria_2_0 | 31 | Trasgo de Moria L29 + Trasgo de Moria L29 | 93,7 | 24,1 |
| durin_3_1 | 28 | Trasgo de Moria L26 + Trasgo de Moria L26 | 95,0 | 25,9 |

**MEDIO por zona (final)**

| Zona | Jefe | Media normales | Mínimo normales |
|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 |
| chet | 78,0 | 100,0 | 100,0 |
| moscagua | 100,0 | 100,0 | 100,0 |
| cima | 100,0 | 100,0 | 100,0 |
| puente | 85,0 | 100,0 | 100,0 |
| quebradas | 100,0 | 100,0 | 100,0 |
| bruinen | 100,0 | 100,0 | 99,7 |
| eregion | 80,0 | 99,5 | 97,3 |
| caradhras | 100,0 | 99,5 | 98,0 |
| durin | 100,0 | 95,1 | 80,3 |
| moria | 100,0 | 94,5 | 80,0 |
| sombrio | 79,7 | 94,9 | 79,7 |
| nimrodel | 84,0 | 95,9 | 85,3 |

### Valores finales

```kotlin
RivalScaling.DEFAULT = RivalScaling(hpPct = 90, attackPct = 52, healPct = 100, defensePct = 100, startPowerPct = 92, regenPct = 100)
RivalScaling.BOSS    = RivalScaling(hpPct = 150, attackPct = 80, healPct = 100, defensePct = 100, startPowerPct = 100, regenPct = 100)
```

- Offsets de grupo: 1 rival 0/+1, 2 rivales −2, 3 rivales −4, 4 rivales −5; escoltas del jefe −4; jefe +2.
- Aventura completa: NOVATO 3,2 %, MEDIO 29,2 %, EXPERTO 66,1 % (las tres en rango).
- Los CSV de `final-encounters.csv` y `final-adventures.csv` son los de este estado final.

### Desajustes que quedan

1. **8 jefes al 100 %**: bree, moscagua, cima, quebradas, bruinen, caradhras, durin y moria. No bajan con ninguna curva común de jefes sin hundir a los de daño directo (ver 3.3). Haría falta tocar contenido por jefe (patrón u offset propio), lo que queda fuera de este paso.
2. **9 encuentros normales por debajo del 95 %**: todos son tríos de nivel 24–33 (Trasgo de Moria, que va cada tick; Lobo de Saruman; Orco de las Montañas + Arquero) y dos parejas de Trasgo (`moria_2_0` 93,7 %). El peor es `sombrio_3_3` con 79,7 %.
3. **MEDIO queda en 29,2 %**, dentro del rango pero cerca del techo. La curva de jefes concentra ahora las muertes en los jefes de chet (78 %) y puente (85 %), sobre todo para NOVATO (solo el 69 % pasa a Moscagua).


## Paso 4 (2026-10-09): ajuste por jefe y por nodo

Tres tipos de cambio, uno cada vez y con las mismas semillas (300 combates por encuentro, 2000 aventuras por perfil): offset propio por jefe, grupos de comunes más pequeños y, al final, el ataque de `RivalScaling.DEFAULT`. Después hubo que retocar dos nodos más de Durin (4.4), porque el ataque nuevo dejó dos parejas por debajo del 95 %.

### 4.1 Offset propio por jefe

La regla §5 ya no fija el jefe en +2: ahora admite de **+2 a +8** (`ContentRules.BOSS_OFFSETS`). El test nuevo `ContentRulesTest.bossOffsetRange` se escribió antes del cambio y se vio fallar. Para los 8 jefes al 100 % se probaron todos los offsets de +2 a +8 y, como permite el encargo, todas las reordenaciones de su patrón (barrido temporal, 300 combates por punto, mismas semillas que el informe).

| Jefe | MEDIO por offset (+2 … +8), patrón original | ¿Reordenar ayuda? | Decisión |
|---|---|---|---|
| bree | 100 / 100 / 0 / 0 / 0 / 0 / 0 | No: la mejor reordenación (`121, 11, 21`) da 19,7 % a +4 | +2 (sin cambio) |
| moscagua | 100 / 100 / 99,0 / 96,0 / 75,7 / 8,3 / 0 | No hace falta | **+5** (96,0 %) |
| cima | 100 en todos | No: los 24 órdenes dan 100 % | +2 (sin cambio) |
| quebradas | 100 / 100 / 100 / 100 / 100 / 81,3 / 88,7 | — | +2 (sin cambio): desde +4 el NOVATO gana el 0 % (con cualquier orden), y eso deja su aventura en 0 % |
| bruinen | 100 en todos | No: los 24 órdenes dan 100 % | +2 (sin cambio) |
| caradhras | 100 en todos | No: los 24 órdenes dan 100 % (ya abre con daño, `32`) | +2 (sin cambio) |
| durin | 100 en todos | No: el mejor orden (`3131, 31, 2132, 31313`) solo baja a 98,3 % | +2 (sin cambio) |
| moria | 100 en todos | No: los 120 órdenes dan 100 % | +2 (sin cambio) |

Por qué no bajan:

- **Salto al 0 % (bree y, para NOVATO, quebradas).** La política MEDIO/EXPERTO solo trata a un rival como jefe si su nivel es al menos el del jugador + 2. Con bree a +4 (jefe L5 contra jugador L3), el jugador empieza a lanzar `2132` cada 6 ticks. A nivel 3 eso le deja sin poder para atacar, el jefe se cura más rápido de lo que recibe daño y el combate llega al tope de 600 ticks. Es un salto de la política simulada, no una dificultad continua: no hay ningún offset intermedio.
- **100 % en cualquier offset (cima, bruinen, caradhras, durin, moria).** Son jefes de daño de área o de estados que van cada tick, pero esperan a tener poder para su siguiente combo. Su daño lo limita el poder, no el nivel. Subir 6 niveles les da muchos más PV y alarga el combate, pero no lo vuelve peligroso, y el escudo `2132` del jugador les quita la mitad del daño. Para bajarlos haría falta tocar el motor o la curva de jefes (por ejemplo, el poder inicial o la regeneración de `RivalScaling.BOSS`, o una curva por jefe), lo que queda fuera de este paso.
- **Moscagua a +5.** Al nivel esperado, el MEDIO gana el 96,0 %. En la aventura real, con niveles que varían según el camino, solo pasa el 75 % de los que llegan (de 85,5 % a 64,1 %). Además, las aventuras MEDIO tienen 428 combates al tope de ticks (antes, 3): son partidas en las que el jugador llega con poco nivel y la política entra en el bucle del escudo. Con +4, el encuentro da 99,0 % y en la aventura pasa el 90 % (171 combates al tope). Se dejó +5 porque la métrica pedida es el encuentro al nivel esperado; si se prefiere la tasa real, +4 es la alternativa.

### 4.2 Comunes débiles

En cada trío se quitó un rival y la pareja que queda pasa a offset −2. Si el trío era mixto, se quitó el Trasgo, que va cada tick; en Nimrodel, uno de los dos orcos. `moria_2_0` (una pareja de Trasgos) pasa a un solo Trasgo a +1. Ningún nodo pierde su encuentro, así que ninguna zona cambia su número de combates por camino (2–4). Los textos y los `imagePrompt` solo se tocaron donde daban la cuenta de enemigos.

| Nodo | Antes | Ahora | MEDIO antes → después | Texto / prompt |
|---|---|---|---|---|
| sombrio_3_3 | Orco Montañas + Arquero + Trasgo (−4) | Orco Montañas + Arquero (−2) | 79,7 → 99,7 | "tres figuras" → "dos figuras"; en vez del trasgo que olfatea, es el orco quien ve primero al jugador y gruñe; prompt sin el trasgo |
| moria_4_2 | 3 × Trasgo (−4) | 2 × Trasgo (−2) | 80,0 → 97,0 | "tres trasgos" → "dos"; "Tres contra uno" → "Dos contra uno"; prompt "Two grinning goblins" |
| durin_4_4 | 3 × Lobo de Saruman (−4) | 2 × Lobo de Saruman (−2) | 80,3 → 99,7 | "bajan tres más, abiertos en abanico" → "bajan dos más, uno por cada flanco"; "los demás" → "el otro"; prompt "Two grey wolves" |
| moria_5_1 | Orco capitán + 2 × Trasgo (−4) | Orco capitán + Trasgo (−2) | 83,0 → 99,7 | "dos trasgos y un orco capitán" → "un trasgo y un orco capitán"; prompt "a goblin" |
| durin_4_2 | 3 × Trasgo (−4) | 2 × Trasgo (−2) | 84,0 → 97,7 | "otros dos trasgos… Ahora son tres" → "otro trasgo… Ahora son dos"; "El tercer trasgo" → "El otro trasgo" (en 4.4); prompt "two goblins" |
| nimrodel_3_2 | 2 × Orco Montañas + Arquero (−4) | Orco Montañas + Arquero (−2) | 85,3 → 100,0 | "tres orcos, dos de las montañas" → "dos orcos, uno de las montañas"; el prompt cambia igual |
| nimrodel_5_2 | 2 × Orco Montañas + Arquero (−4) | Orco Montañas + Arquero (−2) | 86,7 → 100,0 | "tres orcos… dos grandes" → "dos orcos… uno grande"; el prompt cambia igual |
| sombrio_6_0 | Orco Montañas + Arquero + Trasgo (−4) | Orco Montañas + Arquero (−2) | 90,7 → 100,0 | sin el trasgo (ahora es el arquero quien va delante siguiendo el rastro); "estos tres" → "estos dos"; el prompt cambia igual |
| moria_2_0 | 2 × Trasgo (−2) | 1 × Trasgo (+1) | 93,7 → 100,0 | título "El centinela de la escalera"; "dos trasgos de guardia… Uno grita y el otro te arroja la lanza" → un solo trasgo que grita y arroja la lanza; prompt "a goblin sentry" |

### 4.3 `RivalScaling.DEFAULT`: ataque 52 → 54

Tras 4.2, la aventura completa quedó en MEDIO 31,5 % y EXPERTO 71,4 %, los dos por encima del rango. Barrido de ataque (`sweep hp=90 attack=… start=92`, 100 combates y 1000 aventuras por punto):

| Ataque | NOVATO | MEDIO | EXPERTO | Normales MEDIO media / mín. | Jefes MEDIO media / mín. / máx. |
|---|---|---|---|---|---|
| 50 | 5,8 | 35,4 | 73,2 | 99,6 / 93,0 | 93,2 / 80,0 / 100,0 |
| 52 | 5,4 | 32,1 | 71,3 | 99,6 / 94,0 | 93,2 / 80,0 / 100,0 |
| 53 | 4,6 | 31,0 | 71,2 | 99,5 / 94,0 | 93,2 / 79,0 / 100,0 |
| **54** | 3,9 | 27,9 | 69,7 | 99,3 / 93,0 | 93,2 / 79,0 / 100,0 |
| 55 | 2,2 | 26,3 | 65,6 | 99,1 / 91,0 | 92,5 / 74,0 / 100,0 |
| 56 | 1,8 | 25,0 | 63,0 | 99,0 / 89,0 | 92,5 / 74,0 / 100,0 |
| 58 | 0,8 | 23,6 | 57,0 | 98,6 / 82,0 | 91,5 / 65,0 / 100,0 |

Se eligió 54. Con 56 el MEDIO llega al 25 %, pero el NOVATO baja al 1,8 % (fuera de 2–10 %) y hay comunes al 89 %. En el informe completo, 55 deja 4 comunes por debajo del 95 % y el jefe de Eregion al 73 %. Con 54, dos parejas de Trasgos de Durin quedan por debajo del 95 %: `durin_3_1` (93,3 %) y `durin_4_6` (94,7 %).

### 4.4 Dos parejas de Durin

Es el mismo tipo de cambio que en 4.2: las dos parejas de Trasgos pasan a un solo Trasgo a +1 (100 % MEDIO). Con un Trasgo y un Lobo seguían en 94–95 %.

| Nodo | Antes | Ahora | MEDIO | Texto / prompt |
|---|---|---|---|---|
| durin_3_1 | 2 × Trasgo (−2) | 1 × Trasgo (+1) | 93,3 → 100,0 | "tres figuras" → "dos figuras"; "los otros dos… suben" → "el otro… sube"; prompt "A hunched goblin…, a second goblin hanging back" |
| durin_4_6 | 2 × Trasgo (−2) | 1 × Trasgo (+1) | 94,7 → 100,0 | "Dos trasgos trepan… sacan las armas… ellos buscan" → "Un trasgo trepa… saca el arma… él busca"; prompt "a goblin" |
| durin_4_2 | — | — | — | "El tercer trasgo" → "El otro trasgo", para que siga encajando con `durin_3_1` |

### Resultados del paso 4

| Métrica | Antes (paso 3) | 4.1 jefes | 4.2 comunes | 4.3 ataque 54 | 4.4 durin |
|---|---|---|---|---|---|
| NOVATO aventura completa | 3,2 % | 3,2 % | 5,7 % | 4,2 % | 4,2 % |
| MEDIO aventura completa | 29,2 % | 26,5 % | 31,5 % | 27,5 % | 28,0 % |
| EXPERTO aventura completa | 66,1 % | 62,6 % | 71,4 % | 70,1 % | 70,9 % |
| MEDIO combates al tope (aventuras) | 3 | 428 | 428 | 428 | 428 |
| MEDIO normales: media | 98,3 | 98,3 | 99,6 | 99,3 | 99,4 |
| MEDIO normales: mínimo | 79,7 | 79,7 | 95,0 | 93,3 | 95,0 |
| MEDIO normales < 95 % | 9 de 103 | 9 de 103 | 0 de 103 | 2 de 103 | 0 de 103 |
| MEDIO jefes: media | 92,8 | 92,5 | 92,5 | 92,4 | 92,4 |
| MEDIO jefes: mín / máx | 78,0 / 100,0 | 78,0 / 100,0 | 78,0 / 100,0 | 78,0 / 100,0 | 78,0 / 100,0 |
| MEDIO jefes fuera de 75–98 % | 8 de 13 | 7 de 13 | 7 de 13 | 7 de 13 | 7 de 13 |

**Jefe MEDIO por zona**

| Jefe MEDIO | Antes (paso 3) | 4.1 jefes | 4.2 comunes | 4.3 ataque 54 | 4.4 durin |
|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 78,0 | 78,0 | 78,0 | 78,0 | 78,0 |
| moscagua | 100,0 | 96,0 | 96,0 | 96,0 | 96,0 |
| cima | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| puente | 85,0 | 85,0 | 85,0 | 82,3 | 82,3 |
| quebradas | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| bruinen | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| eregion | 80,0 | 80,0 | 80,0 | 79,7 | 79,7 |
| caradhras | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| durin | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| moria | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| sombrio | 79,7 | 79,7 | 79,7 | 79,3 | 79,3 |
| nimrodel | 84,0 | 84,0 | 84,0 | 86,0 | 86,0 |

**NOVATO, % que llega a cada zona**

| Zona | Antes (paso 3) | 4.1 jefes | 4.2 comunes | 4.3 ataque 54 | 4.4 durin |
|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| moscagua | 68,9 | 68,9 | 68,9 | 68,9 | 68,9 |
| cima | 68,9 | 68,9 | 68,9 | 68,9 | 68,9 |
| puente | 68,9 | 68,9 | 68,9 | 68,9 | 68,9 |
| quebradas | 21,6 | 21,6 | 21,6 | 16,0 | 16,0 |
| bruinen | 21,6 | 21,6 | 21,6 | 16,0 | 16,0 |
| rivendel | 21,6 | 21,6 | 21,6 | 16,0 | 16,0 |
| eregion | 21,6 | 21,6 | 21,6 | 16,0 | 16,0 |
| caradhras | 8,9 | 8,9 | 8,9 | 6,2 | 6,2 |
| durin | 8,6 | 8,6 | 8,6 | 6,1 | 6,1 |
| moria | 7,7 | 7,7 | 8,6 | 6,1 | 6,1 |
| sombrio | 6,3 | 6,3 | 8,6 | 6,1 | 6,1 |
| nimrodel | 3,6 | 3,6 | 6,5 | 4,7 | 4,7 |
| lothlorien | 3,2 | 3,2 | 5,7 | 4,2 | 4,2 |

**MEDIO, % que llega a cada zona**

| Zona | Antes (paso 3) | 4.1 jefes | 4.2 comunes | 4.3 ataque 54 | 4.4 durin |
|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| moscagua | 85,5 | 85,5 | 85,5 | 85,5 | 85,5 |
| cima | 85,4 | 64,1 | 64,1 | 64,1 | 64,1 |
| puente | 85,4 | 64,1 | 64,1 | 64,1 | 64,1 |
| quebradas | 65,2 | 53,3 | 53,3 | 51,1 | 51,1 |
| bruinen | 64,9 | 53,2 | 53,2 | 51,0 | 51,0 |
| rivendel | 64,3 | 53,0 | 53,0 | 50,8 | 50,8 |
| eregion | 64,3 | 53,0 | 53,0 | 50,8 | 50,8 |
| caradhras | 50,9 | 43,7 | 43,7 | 40,6 | 40,6 |
| durin | 48,4 | 42,0 | 42,0 | 38,2 | 38,2 |
| moria | 45,0 | 39,4 | 40,2 | 36,4 | 37,0 |
| sombrio | 41,5 | 36,7 | 39,9 | 36,2 | 36,7 |
| nimrodel | 33,5 | 30,1 | 35,2 | 31,3 | 31,8 |
| lothlorien | 29,2 | 26,5 | 31,5 | 27,5 | 28,0 |

**EXPERTO, % que llega a cada zona**

| Zona | Antes (paso 3) | 4.1 jefes | 4.2 comunes | 4.3 ataque 54 | 4.4 durin |
|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| moscagua | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| cima | 100,0 | 91,3 | 91,3 | 91,3 | 91,3 |
| puente | 100,0 | 91,3 | 91,3 | 91,3 | 91,3 |
| quebradas | 94,0 | 86,8 | 86,8 | 88,5 | 88,5 |
| bruinen | 94,0 | 86,8 | 86,8 | 88,5 | 88,5 |
| rivendel | 93,9 | 86,7 | 86,7 | 88,1 | 88,1 |
| eregion | 93,9 | 86,7 | 86,7 | 88,1 | 88,1 |
| caradhras | 87,6 | 81,5 | 81,5 | 83,5 | 83,5 |
| durin | 85,1 | 79,6 | 79,6 | 79,2 | 79,2 |
| moria | 81,7 | 76,8 | 78,2 | 77,7 | 78,7 |
| sombrio | 76,9 | 72,5 | 77,8 | 77,1 | 78,0 |
| nimrodel | 68,5 | 64,8 | 73,2 | 72,2 | 73,0 |
| lothlorien | 66,1 | 62,6 | 71,4 | 70,1 | 70,9 |

**Los 10 encuentros normales MEDIO más letales (final)**

| Nodo | Nivel | Rivales | % | Ticks |
|---|---|---|---|---|
| sombrio_3_0 | 34 | Trasgo de Moria L32 + Trasgo de Moria L32 | 95,0 | 25,3 |
| durin_4_7 | 28 | Lobo de Saruman L26 + Trasgo de Moria L26 | 95,3 | 22,5 |
| caradhras_3_2 | 25 | Lobo de Saruman L23 + Lobo de Saruman L23 | 95,7 | 18,7 |
| caradhras_3_3 | 25 | Crebain L21 + Crebain L21 + Crebain L21 | 95,7 | 26,5 |
| durin_3_2 | 28 | Lobo de Saruman L26 + Lobo de Saruman L26 | 95,7 | 19,4 |
| durin_4_3 | 29 | Lobo de Saruman L26 + Trasgo de Moria L26 | 95,7 | 21,4 |
| moria_4_2 | 32 | Trasgo de Moria L29 + Trasgo de Moria L29 | 95,7 | 22,8 |
| durin_4_2 | 29 | Trasgo de Moria L26 + Trasgo de Moria L26 | 96,0 | 23,2 |
| eregion_3_3 | 22 | Lobo de Saruman L20 + Lobo de Saruman L20 | 96,3 | 17,0 |
| durin_4_4 | 29 | Lobo de Saruman L26 + Lobo de Saruman L26 | 97,3 | 18,1 |

**MEDIO por zona (final)**

| Zona | Jefe | Media normales | Mínimo normales |
|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 |
| chet | 78,0 | 100,0 | 100,0 |
| moscagua | 96,0 | 100,0 | 100,0 |
| cima | 100,0 | 100,0 | 100,0 |
| puente | 82,3 | 99,9 | 99,3 |
| quebradas | 100,0 | 100,0 | 100,0 |
| bruinen | 100,0 | 99,8 | 99,3 |
| eregion | 79,7 | 99,1 | 96,3 |
| caradhras | 100,0 | 98,3 | 95,7 |
| durin | 100,0 | 98,1 | 95,3 |
| moria | 100,0 | 99,0 | 95,7 |
| sombrio | 79,3 | 98,9 | 95,0 |
| nimrodel | 86,0 | 99,8 | 99,3 |

**Jefes por perfil (final, nivel esperado)**

| Zona | Jefe NOVATO | Jefe MEDIO | Jefe EXPERTO |
|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 78,0 | 100,0 |
| moscagua | 100,0 | 96,0 | 100,0 |
| cima | 100,0 | 100,0 | 100,0 |
| puente | 0,0 | 82,3 | 99,0 |
| quebradas | 100,0 | 100,0 | 100,0 |
| bruinen | 100,0 | 100,0 | 100,0 |
| eregion | 23,7 | 79,7 | 95,0 |
| caradhras | 100,0 | 100,0 | 100,0 |
| durin | 100,0 | 100,0 | 100,0 |
| moria | 100,0 | 100,0 | 100,0 |
| sombrio | 67,7 | 79,3 | 92,0 |
| nimrodel | 66,0 | 86,0 | 96,3 |

### Valores finales del paso 4

```kotlin
RivalScaling.DEFAULT = RivalScaling(hpPct = 90, attackPct = 54, healPct = 100, defensePct = 100, startPowerPct = 92, regenPct = 100)
RivalScaling.BOSS    = RivalScaling(hpPct = 150, attackPct = 80, healPct = 100, defensePct = 100, startPowerPct = 100, regenPct = 100)
```

- Offsets de jefe: todos +2 salvo moscagua, que está a **+5**. La regla admite de +2 a +8.
- No se reordenó ningún patrón de jefe, porque ninguna reordenación servía (ver 4.1). El bestiario §3 no cambia.
- 11 nodos de contenido cambiados: sombrio_3_3, sombrio_6_0, moria_2_0, moria_4_2, moria_5_1, durin_3_1, durin_4_2, durin_4_4, durin_4_6, nimrodel_3_2 y nimrodel_5_2.
- Aventura completa: NOVATO 4,2 %, MEDIO 28,0 %, EXPERTO 70,9 %.
- Normales MEDIO: media 99,4 % y mínimo 95,0 % (`sombrio_3_0`); ninguno queda por debajo del 95 %.
- Jefes MEDIO: media 92,4 %, mínimo 78,0 % (chet), máximo 100,0 %.
- Los CSV `final-encounters.csv` y `final-adventures.csv` son los de este estado.

### Desajustes que quedan tras el paso 4

1. **Siguen 7 jefes al 100 % MEDIO**: bree, cima, quebradas, bruinen, caradhras, durin y moria. Con el contenido no basta: ni subir el offset hasta +8 ni cambiar el orden del patrón los baja. Haría falta una de estas dos cosas:
   - en bree, que la política simulada no detecte al jefe solo por la diferencia de nivel (`BOSS_LEVEL_GAP`; el motor ya marca al jefe con `RivalSpec.isBoss`) o que no gaste en escudo a nivel bajo;
   - en los jefes de área, más poder o regeneración en la curva de jefes, o una curva propia por jefe.
2. **EXPERTO 70,9 %**: 0,9 puntos por encima del techo del 70 % (con 2000 aventuras, el error típico es de un punto). Se dejó así porque el siguiente paso de ataque (55) deja comunes por debajo del 95 %.
3. **Moscagua a +5**: provoca 428 combates al tope de ticks en las aventuras MEDIO y 175 en las EXPERTO, por el bucle del escudo de la política a nivel bajo (ver 4.1). Con +4 serían 171 y 21.
4. Con el jefe de Moscagua más duro, las muertes MEDIO se concentran ahora en los tres primeros jefes: chet (78 %), moscagua (96 %) y puente (82 %). En la aventura, 1 de cada 4 MEDIO que llega a Moscagua muere ahí.


## Paso 5 (final, 2026-10-09): política realista, curva de jefes y cierre

Última iteración. Cada cambio se hizo de uno en uno, con las mismas semillas que el paso 4 (300 combates por encuentro y 2000 aventuras por perfil en el informe completo; 100 combates y 1000 aventuras por punto en los barridos). La línea base de este paso reproduce exactamente los CSV finales del paso 4.

### 5.1 Política simulada: el jugador ve al jefe

Antes, MEDIO y EXPERTO trataban a un rival como jefe si su nivel era al menos el del jugador + 2, y lanzaban `2132` (escudo) cada 6 ticks con solo 20 de poder. Eso no lo hace una persona: en la pantalla de combate se ve quién es el jefe, y nadie se queda sin poder para atacar por poner el escudo.

- Motor (TDD; el test `startMarksBossFightersSoThePlayerCanSeeThem` se escribió antes y se vio fallar): `Fighter` tiene un campo nuevo, `isBoss: Boolean = false`. `CombatEngine.start` lo copia de `RivalSpec.isBoss`.
- Simulador (TDD; cuatro tests nuevos en `HumanPlayerTest`, tres de ellos vistos fallar antes del cambio; el cuarto, que el escudo siempre deje poder para el combo de daño más barato, ya se cumplía): MEDIO y EXPERTO reconocen al jefe por `isBoss`. El escudo se lanza solo con un jefe vivo, sin SHM activo, con al menos 40 de poder y como mucho una vez cada 8 ticks. Nunca se lanza si dejaría el poder por debajo del combo de daño más barato (40 − 20 ≥ 10, el coste de `12`). Desaparece `BOSS_LEVEL_GAP`.

Efecto: el MEDIO deja de gastar poder en escudos contra jefes de su nivel y contra comunes con más nivel. Su aventura completa sube del 28,0 % al 43,8 %. Chet, moscagua y sombrío pasan al 100 % o casi, y puente baja al 74,3 %, porque contra ese jefe el MEDIO se escuda menos que antes. EXPERTO apenas cambia (71,0 %), pero ahora pierde un 5,8 % de sus partidas en el jefe de bree: son combates bloqueados cuando llega a nivel 2 (ver 5.3a).

### 5.2 Moscagua vuelve a +4

Con la política nueva, a +5 el MEDIO ya ganaba el 100 % al nivel esperado; a +4 también. Los combates al tope de ticks bajan de 39 a 0 en MEDIO y de 265 a 132 en EXPERTO. Cambio en `moscagua.json`: `"levelOffset": 5` → `4` (UTF-8 sin BOM, tildes intactas).

### 5.3 Presupuesto de poder de los jefes

`RivalScaling` tiene un campo nuevo, `powerPct` (poder máximo; 100 por defecto). El poder inicial es `startPowerPct` de ese máximo. Se hizo con TDD en el motor (`powerPctScalesMaxPowerAndStartPower`) y en el barrido (`sweep boss ... power=...`, test `grid parses max power percent`). Barridos de `RivalScaling.BOSS` con `hp=150 start=100`:

**Regeneración × ataque** (`power=100`; la regeneración es entera: 100 → 5, 150 → 7, 200 → 10 y 250 → 12 por tick)

| Ataque | Regen | NOVATO | MEDIO | EXPERTO | Jefes MEDIO media / mín. | puente | chet | eregion |
|---|---|---|---|---|---|---|---|---|
| 70 | 100 | 14,1 | 57,5 | 85,2 | 98,6 / 90 | 97 | 100 | 90 |
| 70 | 150 | 1,4 | 35,4 | 52,8 | 97,2 / 84 | 84 | 100 | 90 |
| 70 | 200 | 0,0 | 15,4 | 15,3 | 86,6 / 21 | 21 | 77 | 59 |
| 80 | 100 | 3,9 | 45,1 | 75,9 | 97,0 / 78 | 78 | 100 | 92 |
| 80 | 150 | 0,3 | 18,5 | 26,5 | 89,2 / 24 | 24 | 100 | 76 |
| 90 | 100 | 0,5 | 24,5 | 51,9 | 93,8 / 52 | 52 | 100 | 81 |
| 90 | 150 | 0,0 | 1,9 | 10,2 | 67,2 / 0 | 1 | 78 | 39 |

**Poder máximo × ataque** (`regen=100`)

| Ataque | Poder | NOVATO | MEDIO | EXPERTO | Jefes MEDIO media / mín. | puente | eregion |
|---|---|---|---|---|---|---|---|
| 60 | 150 | 2,8 | 53,7 | 83,6 | 98,7 / 94 | 94 | 96 |
| 60 | 200 | 0,5 | 30,4 | 68,6 | 93,2 / 74 | 74 | 76 |
| 60 | 300 | 0,0 | 12,8 | 41,1 | 85,7 / 45 | 45 | 55 |
| 70 | 150 | 0,1 | 34,3 | 73,6 | 94,9 / 63 | 63 | 85 |
| 74 | 115 | 2,3 | 50,4 | 80,6 | 98,0 / 86 | 86 | 93 |
| 77 | 115 | 0,6 | 43,6 | 79,7 | 96,2 / 70 | 70 | 89 |
| 80 | 115 | 0,5 | 35,4 | 69,9 | 95,3 / 61 | 61 | 82 |
| 80 | 130 | 0,1 | 22,9 | 60,9 | 92,8 / 32 | 32 | 81 |

Conclusión de los barridos:

- **Ninguna curva baja a los jefes solitarios.** Cima, quebradas (a +2), bruinen, caradhras, durin y moria siguen al 100 % incluso con ataque 90 y regeneración 250, o con 300 % de poder máximo. La traza de un combate de cima a +8 lo explica: el jefe gasta su poder en combos baratos de área o de estado (`2131`, `323`, `23`), se queda sin poder hacia el tick 7 y desde entonces solo lanza un combo de vez en cuando. Mientras, el MEDIO le quita 660 PV en 27 ticks con `12121` + `31313` y sus DOT. Más poder solo le da más combos baratos. El límite está en su patrón, no en la curva.
- **La aventura la decide puente** (y, menos, eregion y nimrodel). Toda curva que baja al MEDIO al 20–30 % deja puente muy por debajo del 75 % y al NOVATO por debajo del 2 %, porque el NOVATO muere casi siempre en puente.

**5.3a, descartado: ataque 74 y regeneración 120 (6 por tick).** En el barrido era el mejor punto (NOVATO 3,1 %, MEDIO 42,7 %, EXPERTO 62,0 %; puente 83 % y eregion 81 %). En el informe completo, el EXPERTO pierde el 25,6 % de sus partidas en el jefe de bree y el MEDIO, el 10,8 %, con 578 y 324 combates al tope de ticks. Cuando el jugador llega al jefe de bree con nivel 2 (rutas con un solo combate antes), el jefe, que se cura con `21` y regenera 6 por tick, aguanta más de lo que recibe y el combate no se puede ganar. Que el EXPERTO quede en rango así es un artefacto, no dificultad, así que se descartó toda regeneración por encima de 100.

**5.3b, elegido: ataque 78** (`regen=100`, `power=100`). Es el punto que deja puente con margen por encima del 75 % (87,7 % en el informe completo; con 80 quedaba en 74,3 %). Además elimina los bloqueos de bree (EXPERTO: 11 combates al tope en vez de 132). El precio es que la aventura sube: NOVATO 6,2 %, MEDIO 48,7 % y EXPERTO 83,3 %.

**Barrido de offset por jefe con ataque 78** (MEDIO al nivel esperado, 200 combates; formato NOVATO/MEDIO/EXPERTO cuando importa):

| Jefe | +2 | +3 | +4 | +5 | +6 | +7 | +8 |
|---|---|---|---|---|---|---|---|
| bree | 100 | 100 | 0 | 0 | 0 | 0 | 0 |
| chet | 100 | 100 | 100 | 74,5 | 74,5 | 66,0 | 43,5 |
| moscagua | 100 | 100 | 100 | 100 | 100 | 100 | 100 (EXPERTO 86) |
| quebradas | 100 | 100 | 100 | 100 (NOV 0) | 100 | **92,0** | 47,0 |
| eregion | 90,0 | 86,5 | 89,0 | 86,0 | 76,5 | 72,5 | 38,5 |
| sombrio | 99,5 | 100 | 99,5 (NOV 0) | 99,5 | 97,5 | 97,5 | 97,0 |
| nimrodel | 92,0 | 92,5 | 93,0 | 93,5 | 91,0 | 90,5 | 88,5 |
| cima, bruinen, caradhras, durin, moria | 100 en todos | | | | | | |

Bree salta del 100 al 0 % (bloqueo a nivel bajo) y chet del 100 al 74,5 %, sin término medio. El único jefe al 100 % que entra limpio en rango es quebradas a +7.

**5.3c: quebradas a +7** (`quebradas.json`: `"levelOffset": 2` → `7`). En el informe completo, MEDIO 89,0 % y EXPERTO 100 % al nivel esperado; NOVATO 0 % al nivel esperado, pero en la aventura pasa el 20 % de los NOVATO que llegan (van por encima del nivel esperado). Aventura: NOVATO 2,2 %, MEDIO 42,9 %, EXPERTO 83,3 %. Con esto el NOVATO queda en el suelo de su rango, así que ya no se puede endurecer ningún jefe más (sombrío a +6 o eregion a +5 le quitarían otra parte).

### 5.4 Ataque de `RivalScaling.DEFAULT`: sin cambio

Barrido con el estado de 5.3c (`sweep hp=90 attack=… start=92 fights=300 runs=2000`, las mismas semillas que el informe; el punto 54 reproduce exactamente el informe completo):

| Ataque | NOVATO | MEDIO | EXPERTO | Normales MEDIO media / mín. | Jefes MEDIO media / mín. |
|---|---|---|---|---|---|
| 52 | 3,6 | 46,6 | 86,6 | 99,7 / 96,0 | 96,6 / 89,0 |
| 53 | 3,3 | 45,6 | 86,0 | 99,7 / 95,0 | 96,8 / 89,0 |
| **54** | 2,2 | 42,9 | 83,3 | 99,4 / 95,0 | 96,7 / 87,7 |
| 55 | 1,4 | 40,1 | 79,4 | 99,3 / 94,3 | 96,5 / 87,3 |
| 56 | 1,5 | 39,0 | 76,2 | 99,2 / 88,3 | 96,4 / 87,3 |

Subir a 55 o 56 baja al MEDIO solo 3–4 puntos, pero deja al NOVATO por debajo del 2 % y comunes por debajo del 95 %. Bajarlo aleja aún más al MEDIO y al EXPERTO. Se queda en 54.

### Resultados del paso 5

Columnas acumulativas: cada una incluye los cambios anteriores. La 5.3a se midió y se descartó; la 5.3b la sustituye.

| Métrica | Antes (paso 4) | 5.1 política | 5.2 moscagua +4 | 5.3a jefe 74/regen 120 (descartado) | 5.3b jefe ataque 78 | 5.3c quebradas +7 (final) |
|---|---|---|---|---|---|---|
| NOVATO aventura completa | 4,2 % | 4,2 % | 4,2 % | 3,0 % | 6,2 % | 2,2 % |
| MEDIO aventura completa | 28,0 % | 43,8 % | 44,0 % | 40,4 % | 48,7 % | 42,9 % |
| EXPERTO aventura completa | 70,9 % | 71,0 % | 75,2 % | 61,8 % | 83,3 % | 83,3 % |
| MEDIO combates al tope (aventuras) | 428 | 39 | 0 | 324 | 0 | 0 |
| EXPERTO combates al tope (aventuras) | 175 | 265 | 132 | 578 | 11 | 11 |
| MEDIO normales: media / mínimo | 99,4 / 95,0 | 99,4 / 95,0 | 99,4 / 95,0 | 99,4 / 95,0 | 99,4 / 95,0 | 99,4 / 95,0 |
| MEDIO normales < 95 % | 0 de 103 | 0 de 103 | 0 de 103 | 0 de 103 | 0 de 103 | 0 de 103 |
| MEDIO jefes: media | 92,4 | 96,4 | 96,4 | 96,6 | 97,5 | 96,7 |
| MEDIO jefes: mín / máx | 78,0 / 100,0 | 74,3 / 100,0 | 74,3 / 100,0 | 82,3 / 100,0 | 87,7 / 100,0 | 87,7 / 100,0 |
| MEDIO jefes fuera de 75–98 % | 7 de 13 | 11 de 13 | 11 de 13 | 10 de 13 | 10 de 13 | 9 de 13 |

| Jefe MEDIO | Antes (paso 4) | 5.1 política | 5.2 moscagua +4 | 5.3a jefe 74/regen 120 (descartado) | 5.3b jefe ataque 78 | 5.3c quebradas +7 (final) |
|---|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 78,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| moscagua | 96,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| cima | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| puente | 82,3 | 74,3 | 74,3 | 82,3 | 87,7 | 87,7 |
| quebradas | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 89,0 |
| bruinen | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| eregion | 79,7 | 88,7 | 88,7 | 82,3 | 90,7 | 90,7 |
| caradhras | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| durin | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| moria | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| sombrio | 79,3 | 99,7 | 99,7 | 99,3 | 99,0 | 99,0 |
| nimrodel | 86,0 | 91,0 | 91,0 | 91,7 | 90,7 | 90,7 |

**NOVATO, % que llega a cada zona**

| Zona | Antes (paso 4) | 5.1 política | 5.2 moscagua +4 | 5.3a jefe 74/regen 120 (descartado) | 5.3b jefe ataque 78 | 5.3c quebradas +7 (final) |
|---|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| moscagua | 68,9 | 68,9 | 68,9 | 93,6 | 100,0 | 100,0 |
| cima | 68,9 | 68,9 | 68,9 | 93,6 | 100,0 | 100,0 |
| puente | 68,9 | 68,9 | 68,9 | 93,6 | 100,0 | 100,0 |
| quebradas | 16,0 | 16,0 | 16,0 | 16,0 | 26,7 | 26,7 |
| bruinen | 16,0 | 16,0 | 16,0 | 16,0 | 26,7 | 5,4 |
| rivendel | 16,0 | 16,0 | 16,0 | 16,0 | 26,7 | 5,4 |
| eregion | 16,0 | 16,0 | 16,0 | 16,0 | 26,7 | 5,4 |
| caradhras | 6,2 | 6,2 | 6,2 | 4,5 | 10,4 | 2,9 |
| durin | 6,1 | 6,1 | 6,1 | 4,2 | 9,6 | 2,8 |
| moria | 6,1 | 6,1 | 6,1 | 4,2 | 9,6 | 2,8 |
| sombrio | 6,1 | 6,1 | 6,1 | 4,2 | 9,6 | 2,8 |
| nimrodel | 4,7 | 4,7 | 4,7 | 3,3 | 7,6 | 2,6 |
| lothlorien | 4,2 | 4,2 | 4,2 | 3,0 | 6,2 | 2,2 |

**MEDIO, % que llega a cada zona**

| Zona | Antes (paso 4) | 5.1 política | 5.2 moscagua +4 | 5.3a jefe 74/regen 120 (descartado) | 5.3b jefe ataque 78 | 5.3c quebradas +7 (final) |
|---|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 100,0 | 89,2 | 100,0 | 100,0 |
| moscagua | 85,5 | 98,4 | 98,4 | 89,2 | 100,0 | 100,0 |
| cima | 64,1 | 96,5 | 98,4 | 83,8 | 100,0 | 100,0 |
| puente | 64,1 | 96,5 | 98,4 | 83,8 | 100,0 | 100,0 |
| quebradas | 51,1 | 67,7 | 68,2 | 64,5 | 77,0 | 77,0 |
| bruinen | 51,0 | 67,2 | 67,6 | 64,1 | 75,8 | 59,2 |
| rivendel | 50,8 | 66,4 | 66,7 | 63,5 | 74,8 | 58,9 |
| eregion | 50,8 | 66,4 | 66,7 | 63,5 | 74,8 | 58,9 |
| caradhras | 40,6 | 56,2 | 56,5 | 50,6 | 63,6 | 52,9 |
| durin | 38,2 | 52,8 | 53,0 | 48,0 | 59,7 | 50,5 |
| moria | 37,0 | 51,3 | 51,5 | 46,6 | 57,7 | 49,1 |
| sombrio | 36,7 | 50,7 | 50,9 | 46,0 | 56,9 | 48,7 |
| nimrodel | 31,8 | 48,8 | 49,0 | 44,5 | 54,3 | 47,2 |
| lothlorien | 28,0 | 43,8 | 44,0 | 40,4 | 48,7 | 42,9 |

**EXPERTO, % que llega a cada zona**

| Zona | Antes (paso 4) | 5.1 política | 5.2 moscagua +4 | 5.3a jefe 74/regen 120 (descartado) | 5.3b jefe ataque 78 | 5.3c quebradas +7 (final) |
|---|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 94,2 | 94,2 | 74,4 | 100,0 | 100,0 |
| moscagua | 100,0 | 94,2 | 94,2 | 74,4 | 100,0 | 100,0 |
| cima | 91,3 | 86,8 | 93,4 | 71,1 | 99,5 | 99,5 |
| puente | 91,3 | 86,8 | 93,4 | 71,1 | 99,5 | 99,5 |
| quebradas | 88,5 | 82,9 | 88,8 | 69,5 | 96,5 | 96,5 |
| bruinen | 88,5 | 82,9 | 88,8 | 69,5 | 96,5 | 96,4 |
| rivendel | 88,1 | 82,5 | 88,4 | 69,2 | 96,1 | 96,1 |
| eregion | 88,1 | 82,5 | 88,4 | 69,2 | 96,1 | 96,1 |
| caradhras | 83,5 | 78,2 | 83,5 | 66,9 | 92,8 | 92,8 |
| durin | 79,2 | 74,6 | 79,1 | 64,3 | 87,7 | 87,7 |
| moria | 78,7 | 74,2 | 78,6 | 64,0 | 87,2 | 87,2 |
| sombrio | 78,0 | 73,7 | 78,1 | 63,6 | 86,5 | 86,5 |
| nimrodel | 73,0 | 72,8 | 77,0 | 62,9 | 85,3 | 85,3 |
| lothlorien | 70,9 | 71,0 | 75,2 | 61,8 | 83,3 | 83,3 |

**Jefes por perfil (final, nivel esperado, 300 combates)**

| Zona | Nivel | Jefe | NOVATO | MEDIO | EXPERTO |
|---|---|---|---|---|---|
| bree | 3 | El Tuerto, cabecilla de los bandidos L3+Salteador del Tuerto L1 | 100,0 | 100,0 | 100,0 |
| chet | 6 | Lobo gris alfa L6+Lobo joven de la manada L1 | 100,0 | 100,0 | 100,0 |
| moscagua | 9 | Reina del enjambre L11+Guardianas del enjambre L3 | 100,0 | 100,0 | 100,0 |
| cima | 12 | Espectro del Anillo L12 | 100,0 | 100,0 | 100,0 |
| puente | 15 | Jefe de la manada de huargos L15+Huargo L9 | 0,0 | 87,7 | 98,3 |
| quebradas | 18 | Trol de piedra L23 | 0,0 | 89,0 | 100,0 |
| bruinen | 21 | Jinete Negro L21 | 100,0 | 100,0 | 100,0 |
| eregion | 24 | Capitán dunlendino L24+Espía dunlendino L18+Lobo de Saruman L18 | 32,0 | 90,7 | 98,7 |
| caradhras | 27 | Furia del Caradhras L27 | 100,0 | 100,0 | 100,0 |
| durin | 30 | Guardián del Agua L30 | 100,0 | 100,0 | 100,0 |
| moria | 33 | Sombra de Durin L33 | 100,0 | 100,0 | 100,0 |
| sombrio | 36 | Capitán orco de las Montañas Nubladas L36+Arquero orco L30 | 54,7 | 99,0 | 100,0 |
| nimrodel | 39 | Jefe de los arqueros orcos L39+Orco de las Montañas Nubladas L33+Arquero orco L33 | 80,0 | 90,7 | 96,7 |

**MEDIO por zona (final)**

| Zona | Jefe | Media normales | Mínimo normales |
|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 100,0 |
| moscagua | 100,0 | 100,0 | 100,0 |
| cima | 100,0 | 100,0 | 100,0 |
| puente | 87,7 | 99,9 | 99,3 |
| quebradas | 89,0 | 100,0 | 100,0 |
| bruinen | 100,0 | 99,8 | 99,3 |
| eregion | 90,7 | 99,1 | 96,3 |
| caradhras | 100,0 | 98,3 | 95,7 |
| durin | 100,0 | 98,1 | 95,3 |
| moria | 100,0 | 99,0 | 95,7 |
| sombrio | 99,0 | 98,9 | 95,0 |
| nimrodel | 90,7 | 99,8 | 99,3 |

**Los 10 encuentros normales MEDIO más letales (final)**

| Nodo | Nivel | Rivales | % | Ticks |
|---|---|---|---|---|
| sombrio_3_0 | 34 | Trasgo de Moria L32 + Trasgo de Moria L32 | 95,0 | 25,3 |
| durin_4_7 | 28 | Lobo de Saruman L26 + Trasgo de Moria L26 | 95,3 | 22,5 |
| caradhras_3_2 | 25 | Lobo de Saruman L23 + Lobo de Saruman L23 | 95,7 | 18,7 |
| caradhras_3_3 | 25 | Crebain L21 + Crebain L21 + Crebain L21 | 95,7 | 26,5 |
| durin_3_2 | 28 | Lobo de Saruman L26 + Lobo de Saruman L26 | 95,7 | 19,4 |
| durin_4_3 | 29 | Lobo de Saruman L26 + Trasgo de Moria L26 | 95,7 | 21,4 |
| moria_4_2 | 32 | Trasgo de Moria L29 + Trasgo de Moria L29 | 95,7 | 22,8 |
| durin_4_2 | 29 | Trasgo de Moria L26 + Trasgo de Moria L26 | 96,0 | 23,2 |
| eregion_3_3 | 22 | Lobo de Saruman L20 + Lobo de Saruman L20 | 96,3 | 17,0 |
| durin_4_4 | 29 | Lobo de Saruman L26 + Lobo de Saruman L26 | 97,3 | 18,1 |

### Valores finales del paso 5

```kotlin
RivalScaling.DEFAULT = RivalScaling(hpPct = 90, attackPct = 54, healPct = 100, defensePct = 100, startPowerPct = 92, regenPct = 100)            // powerPct = 100
RivalScaling.BOSS    = RivalScaling(hpPct = 150, attackPct = 78, healPct = 100, defensePct = 100, startPowerPct = 100, regenPct = 100)          // powerPct = 100
```

- Offsets de jefe: +2 salvo moscagua (**+4**) y quebradas (**+7**). Los patrones no cambian.
- Código nuevo: `Fighter.isBoss`, `RivalScaling.powerPct` (sin usar en los valores finales, pero disponible para barridos: `sweep boss ... power=...`), y la política del escudo de `PlayerPolicy` (jefe por `isBoss`, poder ≥ 40, cada 8 ticks).
- Aventura completa: NOVATO 2,2 %, MEDIO 42,9 %, EXPERTO 83,3 %.
- Normales MEDIO: media 99,4 % y mínimo 95,0 % (`sombrio_3_0`); ninguno por debajo del 95 %.
- Jefes MEDIO: media 96,7 %, mínimo 87,7 % (puente) y máximo 100 %. Quedan 9 de 13 fuera de 75–98 %, todos por arriba (al 99–100 %); ninguno por debajo del 75 %.
- `final-encounters.csv` y `final-adventures.csv` son los de este estado.

### Pendiente para pruebas reales

1. **MEDIO 42,9 % y EXPERTO 83,3 %, por encima de su rango (20–30 % y 50–70 %).** Con la política realista, solo 4 jefes (puente, quebradas, eregion y nimrodel) frenan la aventura, y no hay curva ni offset que endurezca más sin dejar al NOVATO por debajo del 2 % o a puente por debajo del 75 %. Hay que comprobar con jugadores reales si un MEDIO de verdad juega tan bien como el simulado (escudo solo con jefe, sin errores de pulsación). Si es así, el siguiente paso es de diseño, no de números.
2. **9 jefes al 99–100 % MEDIO**: bree, chet, moscagua, cima, bruinen, caradhras, durin, moria y sombrío. Los cinco solitarios (cima, bruinen, caradhras, durin y moria) no bajan con ninguna curva ni offset (+2 a +8), porque su patrón son combos baratos de área o de estado y se quedan sin poder. Haría falta cambiar sus patrones en el bestiario (§3), por ejemplo meter un combo de daño fuerte a un objetivo (`1212`, `12121`), o una curva propia por jefe. Bree, chet y moscagua cambian a saltos: con el offset, bree pasa del 100 al 0 % (de +3 a +4) y chet del 100 al 74,5 % (de +4 a +5), y moscagua no baja ni a +8.
3. **Combates bloqueados a nivel bajo.** Si el jugador llega a un jefe que se cura (por ejemplo, el de bree con nivel 2) muy por debajo del nivel esperado, el jefe se cura más rápido de lo que recibe daño y el combate no termina (en el simulador se corta a los 600 ticks). En la versión final quedan 11 combates así en 2000 aventuras EXPERTO. Hay que confirmar en el juego si un jugador real también se atasca, y si hace falta un límite de tiempo o que la cura del jefe baje.
4. **El EXPERTO simulado es demasiado conservador a nivel bajo**: guarda 26 de poder y recarga con `33`, y por eso contra un jefe que se cura juega peor que el MEDIO. Conviene revisar la reserva en la política del simulador antes de usar el porcentaje del EXPERTO para decidir.
5. **El NOVATO depende de puente**: el 72 % de los NOVATO muere en el jefe de puente y otro 21 % en el de quebradas. Hay que comprobar en pruebas reales si ese muro en la zona 5 es aceptable o si conviene suavizarlo, por ejemplo quitando el Huargo que acompaña al jefe.


## Paso 6 (2026-10-09): rediseño de los patrones de jefe

Con la política realista del paso 5, nueve jefes caían al 99–100 % MEDIO y ninguna curva ni offset los bajaba (ver "Pendiente para pruebas reales" del paso 5). La causa está en el patrón: combos baratos de área o de estado, y un jefe que se queda sin poder hacia el tick 7. En este paso se rediseñan esos patrones. Como en los pasos anteriores, cada cambio se hizo de uno en uno y se volvió a simular con las mismas semillas: 300 combates por encuentro y 2000 aventuras por perfil en el informe completo, y 300 combates por punto en los barridos por jefe. La línea base de este paso reproduce exactamente los CSV finales del paso 5.

Para cada jefe se midió también el combate con el jugador entre dos niveles por debajo y dos por encima del esperado. Ahí se ve algo que la tabla al nivel esperado no muestra: el NOVATO es determinista (si no hay estados que tiren dados, no hay variación), así que contra cada jefe gana siempre o pierde siempre según su nivel. Por eso, en la aventura, importa sobre todo cuántos jugadores llegan al jefe con uno o dos niveles menos de lo esperado.

### 6.1 Puente sin escolta

En `puente_7_0` se quita el Huargo que acompañaba al jefe: ahora el jefe pelea solo. El texto cambia lo mínimo: "Se planta en mitad del puente y te cierra el paso." (antes, "…mientras otro huargo te cierra la retirada"). En el `imagePrompt` (y en `docs/content/prompts-imagenes.md`), "another warg behind a young traveler" pasa a "standing alone … facing a young traveler".

Efecto: puente pasa al 100 % en los tres perfiles al nivel esperado. El porcentaje de NOVATO que llega a quebradas sube del 26,7 % al 86,2 %, pero el muro se traslada al jefe de quebradas (+7), donde el NOVATO pierde siempre. La aventura completa NOVATO solo sube del 2,2 % al 3,3 %; MEDIO queda en 46,9 % y EXPERTO en 85,2 %. Ahora son 10 los jefes al 99–100 % MEDIO (puente incluido).

### 6.2 Patrones nuevos

Reglas que sigue cada patrón nuevo (las comprueba el test `redesignedBossesOpenWithDamageAndMostlyDamage`):

- Conserva el carácter que el jefe tiene en el bestiario: el Espectro y el Jinete Negro asustan y drenan, el Trol y los capitanes orcos pegan fuerte, el Guardián del Agua aturde con sus tentáculos y la Sombra de Durin quema en área.
- Abre con un combo de daño directo, y al menos la mitad de sus combos hacen daño.
- Mezcla combos caros con baratos y con `33` (Pow) o `31232` (Rpo), para no quedarse sin poder.
- Tiene como mucho una cura propia (`21`), para que no haya bucles de curación.
- Solo usa combos del catálogo.

TDD: primero se cambiaron las expectativas del test del bestiario (`redesignedBossPatterns` y `redesignedBossesOpenWithDamageAndMostlyDamage`) y se vio que fallaban. Después se cambió `Bestiary.bosses` (en `ContentRules.kt`), con lo que pasaba a fallar `RealContentTest`, y por último los JSON.

Barridos por jefe: unas 390 combinaciones de patrón, `everyNTicks` y offset (con los dos ataques de jefe, 78 y 80). Algunos ejemplos de lo que se aprendió:

| Jefe | Prueba | NOVATO / MEDIO / EXPERTO (nivel esperado) | Qué enseña |
|---|---|---|---|
| chet | `1212, 121, 33, 1212, 22` cada tick, +2 | 0 / 90,7 / 100 | Una apertura en ráfaga (todo el poder en los primeros ticks) mata al NOVATO en 8 ticks. |
| chet | `1212, 121, 31232, 12121, 22` cada tick, +2 | 0 / 9,3 / 0 | Con `31232` y combos caros, el jefe no se queda nunca sin poder: demasiado. Los 300 combates EXPERTO llegan al tope de ticks. |
| cima | `2131, 323, 23, 3232` (original), +8 | 100 / 100 / 100 | El patrón de área solo no baja aunque suba el offset. |
| cima | `1212, 3232, 2323, 31232`, +6 y +8 | 100 / 63 / 100 y 0 / 17 / 98 | Con un golpe fuerte a un solo objetivo el jefe ya hace daño, pero con el offset cambia a saltos. |
| quebradas | `11, 1212, 12121` (original), +4 a +7 | NOVATO 0 o 100 según el nivel | Sin estados no hay azar: el resultado depende del nivel, no de la suerte, y es un muro para el NOVATO. |
| quebradas | `12121, 31, 11, 1212, 22` cada 2 ticks, +7 | 100 / 93 / 100 (a L16: 0 / 90 / 100) | Un aturdimiento (`31`) suaviza la curva por niveles. |
| bree | `121, 1212, 11, 12` sin cura, +2 | 100 / 90 / 94 (29 combates MEDIO al tope) | Sin cura, el combate a nivel bajo se atasca igual: el jugador se cura y apenas hace daño. |
| moscagua | `323, 1212, 32, 121, 21, 33`, +6 | 3 / 86 / 33 (200 combates EXPERTO al tope) | Con `21` y `33` juntos, la Reina se cura sin parar contra el EXPERTO de entonces. |

Primero se aplicó un patrón por jefe (6.2a) y después se ajustó en tres iteraciones, cada una medida con la aventura completa:

| Iteración | Cambio | NOVATO | MEDIO | EXPERTO | Jefes MEDIO media / mín. / máx. | Fuera de 75–98 |
|---|---|---|---|---|---|---|
| 6.2a | Patrones nuevos para bree, chet, moscagua, cima, bruinen, caradhras, durin, moria y sombrío. Offsets: puente +4, cima +5, moscagua +5, bree +3, bruinen +6, caradhras +6, durin +3 y moria +8 | 1,1 % | 26,7 % | 76,5 % | 91,1 / 83,7 / 94,3 | 0 de 13 |
| 6.2b | Bree a +2 y moscagua a +4 (el 43 % de los NOVATO moría en bree) | 1,8 % | 29,7 % | 77,0 % | 91,8 / 83,7 / 98,0 | 0 de 13 |
| 6.2c | Bree abre con `12` en lugar de `121`. Chet pasa a `1212, 31, 121, 22, 12, 33` cada 2 ticks, a +3: es más suave si se llega con un nivel menos (MEDIO a L5: 81,7 % en vez de 56,3 %) | 2,3 % | 29,6 % | 75,7 % | 91,7 / 83,7 / 97,3 | 0 de 13 |
| 6.2d | Quebradas: `12121, 31, 11, 1212, 22` cada 2 ticks, a +7 (el NOVATO perdía siempre entre L16 y L20) | 4,3 % | 31,8 % | 75,8 % | 92,0 / 83,7 / 97,3 | 0 de 13 |

Quebradas no estaba en la lista de jefes triviales. Pero tras 6.1 era el único jefe que dejaba al NOVATO a cero en todos los niveles cercanos al esperado, y el objetivo es que ningún jefe lo haga.

**Patrones de jefe (antes y después)**

| Zona | Jefe | Antes: pattern / cada N ticks / offset | Después: pattern / cada N ticks / offset | Combos de daño |
|---|---|---|---|---|
| bree | Cabecilla de los bandidos | `11, 121, 21` / 2 / +2 | `12, 313, 1212, 11, 21, 31` / 2 / +2 | 4 de 6 |
| chet | Lobo gris alfa | `121, 1212, 22` / 1 / +2 | `1212, 31, 121, 22, 12, 33` / 2 / +3 | 4 de 6 |
| moscagua | Reina del enjambre | `32, 323, 21` / 2 / +4 | `323, 121, 32, 1212, 33` / 1 / +4 | 4 de 5 |
| cima | Espectro del Anillo | `2131, 323, 23, 3232` / 1 / +2 | `1212, 323, 232, 31232, 3232` / 1 / +5 | 4 de 5 |
| puente | Jefe de la manada de huargos | `1212, 31, 11` / 1 / +2, con Huargo | mismo patrón / 1 / +3, solo | 2 de 3 |
| quebradas | Trol de piedra | `11, 1212, 12121` / 3 / +7 | `12121, 31, 11, 1212, 22` / 2 / +7 | 3 de 5 |
| bruinen | Jinete Negro | `2131, 3232, 23, 32323` / 1 / +2 | `1212, 12121, 32323, 31232, 2323` / 1 / +6 | 4 de 5 |
| caradhras | Furia del Caradhras | `32, 3232, 22, 32323` / 1 / +2 | `1212, 32323, 313, 31232, 22` / 1 / +6 | 3 de 5 |
| durin | Guardián del Agua | `31, 3131, 2132, 31313` / 1 / +2 | `3131, 31313, 31232, 313, 2132` / 1 / +3 | 3 de 5 |
| moria | Sombra de Durin | `2131, 11, 32323, 23232, 33` / 1 / +2 | `32323, 12121, 31232, 1212, 11` / 1 / +7 | 3 de 5 |
| sombrio | Capitán orco de las Montañas Nubladas | `11, 12121, 22, 2132` / 1 / +2 | `1212, 11, 12121, 22, 2132, 121` / 1 / +2 | 3 de 6 |
| eregion, nimrodel | | sin cambios (ya estaban en rango) | | |

La Reina del enjambre pierde su cura (`21`): con ella, los combates de Moscagua duraban de 120 a 170 ticks y, a +5 o +6, se atascaban. Las columnas "Después" son los valores finales, tras 6.3 y 6.5.

### 6.3 Ataque de los jefes: de 78 a 80

Tras 6.2, el MEDIO (31,8 %) y el EXPERTO (75,8 %) quedaban por encima de su rango. Barridos con las mismas semillas que el informe:

**`sweep boss hp=150 attack=76,78,80,82 start=100 fights=300 runs=2000`**

| Ataque de jefe | NOVATO | MEDIO | EXPERTO | Jefes MEDIO media / mín. | Fuera de 75–98 |
|---|---|---|---|---|---|
| 76 | 6,2 | 40,8 | 80,9 | 95,4 / 90,7 | 3 (moscagua, cima y puente, al 99,7–100) |
| **78** | 4,3 | 31,8 | 75,8 | 92,0 / 83,7 | 0 |
| 80 | 2,1 | 25,0 | 71,9 | 88,9 / 74,3 (puente) | 1 (puente) |
| 82 | 1,0 | 19,9 | 67,8 | 87,7 / 59,0 (moria) | 3 (moria, puente y caradhras) |

**`sweep hp=90 attack=52,53,54,55,56 start=92 fights=300 runs=2000` (comunes)**

| Ataque de comunes | NOVATO | MEDIO | EXPERTO | Normales MEDIO media / mín. |
|---|---|---|---|---|
| 52 | 4,7 | 34,2 | 79,2 | 99,7 / 96,0 |
| 53 | 4,3 | 33,3 | 78,3 | 99,7 / 95,0 |
| **54** | 4,3 | 31,8 | 75,8 | 99,4 / 95,0 |
| 55 | 3,3 | 29,8 | 72,4 | 99,3 / 94,3 |
| 56 | 3,2 | 29,1 | 69,6 | 99,2 / 88,3 |

Subir el ataque de los comunes deja combates normales por debajo del 95 %, así que se descarta. Se elige **ataque de jefe 80** (`RivalScaling.BOSS`, en `Stats.kt`). Con 80, puente baja al 74,3 % y moria al 78,3 %, así que se reajustan sus offsets con el barrido por jefe: puente pasa de +4 a **+3** (93,3 %) y moria de +8 a **+7** (87,7 %, con el NOVATO al 44 %). Resultado: NOVATO 2,3 %, MEDIO 26,8 % y EXPERTO 72,0 %.

### 6.4 Política simulada: reserva del EXPERTO (punto 4 de "Pendiente" del paso 5)

Tras 6.3 quedaban 105 combates EXPERTO al tope de ticks en las aventuras. La traza de uno de ellos (jefe de chet con el jugador a nivel 4, dos menos de lo esperado) lo explica. El EXPERTO no gasta poder si con ello baja de 26, el coste de `2121`. Con 20–37 de poder y la vida entre el 40 y el 60 %, ni ataca ni se cura; cuando baja del 40 %, se cura, y vuelta a empezar. El jefe pega cada 2 ticks y no consigue matarlo, así que el combate llega a los 600 ticks.

El cambio es mínimo y se hizo con TDD en `HumanPlayerTest`: el test nuevo `experto does not wait with power between the cheapest heal and a big heal` y la expectativa actualizada de `experto keeps a heal reserve and recharges with 33` se vieron fallar antes del cambio. La reserva del EXPERTO pasa de 26 al coste de la cura más barata (`21`, 10 de poder): `EXPERT_RESERVE = HEALS.minOf { it.cost }`. Se mantienen la recarga con `33` y la excepción del golpe que mata.

**Antes y después de este cambio solo.** Combates de jefe con los patrones de 6.3, 300 combates por nivel; se compara el EXPERTO con el MEDIO del mismo nivel.

| Jefe | Nivel esperado | MEDIO | EXPERTO antes | EXPERTO después | Niveles (−2 a +2) con EXPERTO < MEDIO, antes | Después | Combates EXPERTO al tope (−2 a +2), antes | Después |
|---|---|---|---|---|---|---|---|---|
| bree | 3 | 97,0 | 100,0 | 100,0 | ninguno | ninguno | 0 | 0 |
| chet | 6 | 89,7 | 100,0 | 100,0 | ninguno | ninguno | 230 | 0 |
| moscagua | 9 | 93,0 | 100,0 | 100,0 | ninguno | ninguno | 11 | 0 |
| cima | 12 | 91,0 | 100,0 | 100,0 | ninguno | ninguno | 0 | 0 |
| puente | 15 | 93,3 | 100,0 | 100,0 | ninguno | ninguno | 0 | 0 |
| quebradas | 18 | 87,3 | 100,0 | 100,0 | ninguno | ninguno | 0 | 0 |
| bruinen | 21 | 91,7 | 100,0 | 100,0 | ninguno | ninguno | 0 | 0 |
| eregion | 24 | 88,7 | 98,3 | 95,7 | ninguno | ninguno | 0 | 0 |
| caradhras | 27 | 97,0 | 100,0 | 100,0 | ninguno | ninguno | 0 | 0 |
| durin | 30 | 86,3 | 95,7 | 95,0 | L32 (92,7 < 96,3) | ninguno | 0 | 0 |
| moria | 33 | 87,7 | 100,0 | 100,0 | ninguno | ninguno | 0 | 0 |
| sombrio | 36 | 91,0 | 99,3 | 99,0 | ninguno | ninguno | 0 | 0 |
| nimrodel | 39 | 91,0 | 97,0 | 99,7 | ninguno | ninguno | 0 | 0 |

| Aventura completa | Antes (6.3) | Después (6.4) |
|---|---|---|
| NOVATO / MEDIO | 2,3 % / 26,8 % | 2,3 % / 26,8 % (sin cambio: su política no cambia) |
| EXPERTO | 72,0 % | 60,6 % |
| EXPERTO, combates al tope | 105 | 1 |
| EXPERTO, normales: media / mínimo | 99,7 / 93,0 | 99,6 / 91,3 |

Con la reserva nueva, el EXPERTO gana al menos tanto como el MEDIO en todos los combates de jefe, desde dos niveles por debajo del esperado hasta dos por encima, y los bloqueos casi desaparecen. A cambio, la aventura EXPERTO baja 11 puntos: ahora gasta más poder y muere más en algunos combates comunes de área. El caso más claro es `caradhras_6_0` (tres Lobos de Saruman): el EXPERTO gana el 91,3 %, frente al 100 % de antes y al 98,7 % del MEDIO, y es el nodo donde más EXPERTO mueren (9,6 % de las partidas). Se acepta, porque el porcentaje del EXPERTO ya no sale de un artefacto (los bloqueos) y queda dentro de su rango.

### 6.5 Espectro del Anillo: `23` pasa a `232`

Con el ataque 80, el jefe de cima dejaba al NOVATO al 0 % al nivel esperado y era un precipicio para el MEDIO que llegaba con un nivel menos (a L11, 11,3 %; en la aventura, el 21 % de los MEDIO moría ahí). Al cambiar el drenaje corto `23` por `232` (más caro, alarga el ciclo del patrón), al nivel esperado quedan NOVATO 100 %, MEDIO 91,7 % y EXPERTO 100 %, y el MEDIO a L11 sube al 65,7 %. Se hizo con TDD, igual que en 6.2. Aventura: NOVATO 2,3 %, MEDIO 27,8 % y EXPERTO 60,7 %.

### Resultados del paso 6

Columnas acumulativas: cada una incluye los cambios anteriores.

| Métrica | Antes (paso 5) | 6.1 puente sin escolta | 6.2 patrones nuevos | 6.3 ataque de jefe 80 | 6.4 reserva EXPERTO | 6.5 Espectro con 232 (final) |
|---|---|---|---|---|---|---|
| NOVATO aventura completa | 2,2 % | 3,3 % | 4,3 % | 2,3 % | 2,3 % | 2,3 % |
| MEDIO aventura completa | 42,9 % | 46,9 % | 31,8 % | 26,8 % | 26,8 % | 27,8 % |
| EXPERTO aventura completa | 83,3 % | 85,2 % | 75,8 % | 72,0 % | 60,6 % | 60,7 % |
| NOVATO combates al tope (aventuras) | 0 | 0 | 0 | 0 | 0 | 0 |
| MEDIO combates al tope (aventuras) | 0 | 0 | 0 | 0 | 0 | 0 |
| EXPERTO combates al tope (aventuras) | 11 | 11 | 110 | 105 | 1 | 1 |
| MEDIO normales: media / mínimo | 99,4 / 95,0 | 99,4 / 95,0 | 99,4 / 95,0 | 99,4 / 95,0 | 99,4 / 95,0 | 99,4 / 95,0 |
| MEDIO normales < 95 % | 0 de 103 | 0 de 103 | 0 de 103 | 0 de 103 | 0 de 103 | 0 de 103 |
| MEDIO jefes: media | 96,7 | 97,6 | 92,0 | 91,1 | 91,1 | 91,2 |
| MEDIO jefes: mín / máx | 87,7 / 100,0 | 89,0 / 100,0 | 83,7 / 97,3 | 86,3 / 97,0 | 86,3 / 97,0 | 86,3 / 97,0 |
| MEDIO jefes fuera de 75–98 % | 9 de 13 | 10 de 13 | 0 de 13 | 0 de 13 | 0 de 13 | 0 de 13 |
| MEDIO jefes ≥ 99 % | 9 de 13 | 10 de 13 | 0 de 13 | 0 de 13 | 0 de 13 | 0 de 13 |

| Jefe MEDIO | Antes (paso 5) | 6.1 puente sin escolta | 6.2 patrones nuevos | 6.3 ataque de jefe 80 | 6.4 reserva EXPERTO | 6.5 Espectro con 232 (final) |
|---|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 97,0 | 97,0 | 97,0 | 97,0 |
| chet | 100,0 | 100,0 | 89,7 | 89,7 | 89,7 | 89,7 |
| moscagua | 100,0 | 100,0 | 97,3 | 93,0 | 93,0 | 93,0 |
| cima | 100,0 | 100,0 | 93,0 | 91,0 | 91,0 | 91,7 |
| puente | 87,7 | 100,0 | 93,3 | 93,3 | 93,3 | 93,3 |
| quebradas | 89,0 | 89,0 | 93,0 | 87,3 | 87,3 | 87,3 |
| bruinen | 100,0 | 100,0 | 91,7 | 91,7 | 91,7 | 91,7 |
| eregion | 90,7 | 90,7 | 90,7 | 88,7 | 88,7 | 88,7 |
| caradhras | 100,0 | 100,0 | 93,7 | 97,0 | 97,0 | 97,0 |
| durin | 100,0 | 100,0 | 91,0 | 86,3 | 86,3 | 86,3 |
| moria | 100,0 | 100,0 | 83,7 | 87,7 | 87,7 | 87,7 |
| sombrio | 99,0 | 99,0 | 91,0 | 91,0 | 91,0 | 91,0 |
| nimrodel | 90,7 | 90,7 | 90,7 | 91,0 | 91,0 | 91,0 |

| Jefe NOVATO | Antes (paso 5) | 6.1 puente sin escolta | 6.2 patrones nuevos | 6.3 ataque de jefe 80 | 6.4 reserva EXPERTO | 6.5 Espectro con 232 (final) |
|---|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 72,7 | 72,7 | 72,7 | 72,7 |
| moscagua | 100,0 | 100,0 | 47,7 | 20,7 | 20,7 | 20,7 |
| cima | 100,0 | 100,0 | 100,0 | 0,0 | 0,0 | 100,0 |
| puente | 0,0 | 100,0 | 76,7 | 76,7 | 76,7 | 76,7 |
| quebradas | 0,0 | 0,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| bruinen | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| eregion | 32,0 | 32,0 | 32,0 | 23,7 | 23,7 | 23,7 |
| caradhras | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| durin | 100,0 | 100,0 | 36,7 | 49,0 | 49,0 | 49,0 |
| moria | 100,0 | 100,0 | 44,0 | 44,0 | 44,0 | 44,0 |
| sombrio | 54,7 | 54,7 | 47,3 | 51,0 | 51,0 | 51,0 |
| nimrodel | 80,0 | 80,0 | 80,0 | 66,0 | 66,0 | 66,0 |

| Jefe EXPERTO | Antes (paso 5) | 6.1 puente sin escolta | 6.2 patrones nuevos | 6.3 ataque de jefe 80 | 6.4 reserva EXPERTO | 6.5 Espectro con 232 (final) |
|---|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| moscagua | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| cima | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| puente | 98,3 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| quebradas | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| bruinen | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| eregion | 98,7 | 98,7 | 98,7 | 98,3 | 95,7 | 95,7 |
| caradhras | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| durin | 100,0 | 100,0 | 91,0 | 95,7 | 95,0 | 95,0 |
| moria | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| sombrio | 100,0 | 100,0 | 99,3 | 99,3 | 99,0 | 99,0 |
| nimrodel | 96,7 | 96,7 | 96,7 | 97,0 | 99,7 | 99,7 |

**NOVATO, % que llega a cada zona**

| Zona | Antes (paso 5) | 6.1 puente sin escolta | 6.2 patrones nuevos | 6.3 ataque de jefe 80 | 6.4 reserva EXPERTO | 6.5 Espectro con 232 (final) |
|---|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 88,9 | 92,0 | 92,0 | 92,0 |
| moscagua | 100,0 | 100,0 | 56,3 | 56,6 | 56,6 | 56,6 |
| cima | 100,0 | 100,0 | 37,0 | 27,8 | 27,8 | 27,8 |
| puente | 100,0 | 100,0 | 35,2 | 21,4 | 21,4 | 27,2 |
| quebradas | 26,7 | 86,2 | 28,6 | 18,6 | 18,6 | 21,8 |
| bruinen | 5,4 | 7,9 | 28,2 | 18,6 | 18,6 | 21,8 |
| rivendel | 5,4 | 7,9 | 26,0 | 17,5 | 17,5 | 19,8 |
| eregion | 5,4 | 7,9 | 26,0 | 17,5 | 17,5 | 19,8 |
| caradhras | 2,9 | 4,3 | 10,3 | 6,3 | 6,3 | 6,7 |
| durin | 2,8 | 4,2 | 9,9 | 6,0 | 6,0 | 6,3 |
| moria | 2,8 | 4,2 | 8,0 | 4,2 | 4,2 | 4,4 |
| sombrio | 2,8 | 4,2 | 6,4 | 3,8 | 3,8 | 3,8 |
| nimrodel | 2,6 | 3,8 | 4,8 | 2,5 | 2,5 | 2,5 |
| lothlorien | 2,2 | 3,3 | 4,3 | 2,3 | 2,3 | 2,3 |

**MEDIO, % que llega a cada zona**

| Zona | Antes (paso 5) | 6.1 puente sin escolta | 6.2 patrones nuevos | 6.3 ataque de jefe 80 | 6.4 reserva EXPERTO | 6.5 Espectro con 232 (final) |
|---|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 96,7 | 96,2 | 96,2 | 96,2 |
| moscagua | 100,0 | 100,0 | 83,2 | 82,9 | 82,9 | 82,9 |
| cima | 100,0 | 100,0 | 79,4 | 75,0 | 75,0 | 75,0 |
| puente | 100,0 | 100,0 | 68,2 | 54,1 | 54,1 | 61,4 |
| quebradas | 77,0 | 95,2 | 64,3 | 51,9 | 51,9 | 58,3 |
| bruinen | 59,2 | 66,3 | 60,5 | 49,1 | 49,1 | 54,4 |
| rivendel | 58,9 | 65,7 | 57,6 | 45,3 | 45,3 | 49,2 |
| eregion | 58,9 | 65,7 | 57,6 | 45,3 | 45,3 | 49,2 |
| caradhras | 52,9 | 57,9 | 50,9 | 40,5 | 40,5 | 42,9 |
| durin | 50,5 | 55,2 | 47,0 | 38,2 | 38,2 | 40,3 |
| moria | 49,1 | 53,7 | 42,5 | 34,1 | 34,1 | 35,8 |
| sombrio | 48,7 | 53,3 | 36,4 | 30,8 | 30,8 | 32,3 |
| nimrodel | 47,2 | 51,7 | 34,2 | 29,0 | 29,0 | 30,1 |
| lothlorien | 42,9 | 46,9 | 31,8 | 26,8 | 26,8 | 27,8 |

**EXPERTO, % que llega a cada zona**

| Zona | Antes (paso 5) | 6.1 puente sin escolta | 6.2 patrones nuevos | 6.3 ataque de jefe 80 | 6.4 reserva EXPERTO | 6.5 Espectro con 232 (final) |
|---|---|---|---|---|---|---|
| bree | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| chet | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 | 100,0 |
| moscagua | 100,0 | 100,0 | 95,4 | 95,4 | 99,8 | 99,8 |
| cima | 99,5 | 99,5 | 94,4 | 94,1 | 97,2 | 97,2 |
| puente | 99,5 | 99,5 | 94,4 | 94,1 | 93,4 | 94,6 |
| quebradas | 96,5 | 99,4 | 94,0 | 93,7 | 93,0 | 94,1 |
| bruinen | 96,4 | 99,1 | 94,0 | 93,7 | 92,9 | 93,9 |
| rivendel | 96,1 | 98,7 | 93,6 | 93,2 | 90,7 | 91,6 |
| eregion | 96,1 | 98,7 | 93,6 | 93,2 | 90,7 | 91,6 |
| caradhras | 92,8 | 95,3 | 90,6 | 87,8 | 84,0 | 84,6 |
| durin | 87,7 | 89,7 | 85,5 | 83,0 | 72,4 | 72,6 |
| moria | 87,2 | 89,1 | 80,3 | 76,7 | 66,5 | 66,7 |
| sombrio | 86,5 | 88,4 | 79,3 | 75,7 | 63,7 | 63,9 |
| nimrodel | 85,3 | 87,2 | 77,6 | 73,8 | 61,7 | 61,8 |
| lothlorien | 83,3 | 85,2 | 75,8 | 72,0 | 60,6 | 60,7 |

**Jefes por perfil (final, nivel esperado, 300 combates)**

| Zona | Nivel | Jefe | NOVATO | MEDIO | EXPERTO | Ticks MEDIO |
|---|---|---|---|---|---|---|
| bree | 3 | El Tuerto, cabecilla de los bandidos L3 + Salteador del Tuerto L1 | 100,0 | 97,0 | 100,0 | 66,0 |
| chet | 6 | Lobo gris alfa L7 + Lobo joven de la manada L1 | 72,7 | 89,7 | 100,0 | 46,1 |
| moscagua | 9 | Reina del enjambre L11 + Guardianas del enjambre L3 | 20,7 | 93,0 | 100,0 | 77,4 |
| cima | 12 | Espectro del Anillo L15 | 100,0 | 91,7 | 100,0 | 46,2 |
| puente | 15 | Jefe de la manada de huargos L16 | 76,7 | 93,3 | 100,0 | 25,0 |
| quebradas | 18 | Trol de piedra L23 | 100,0 | 87,3 | 100,0 | 38,6 |
| bruinen | 21 | Jinete Negro L25 | 100,0 | 91,7 | 100,0 | 47,3 |
| eregion | 24 | Capitán dunlendino L24 + Espía dunlendino L18 + Lobo de Saruman L18 | 23,7 | 88,7 | 95,7 | 64,1 |
| caradhras | 27 | Furia del Caradhras L31 | 100,0 | 97,0 | 100,0 | 60,4 |
| durin | 30 | Guardián del Agua L31 | 49,0 | 86,3 | 95,0 | 46,1 |
| moria | 33 | Sombra de Durin L38 | 44,0 | 87,7 | 100,0 | 51,4 |
| sombrio | 36 | Capitán orco de las Montañas Nubladas L36 + Arquero orco L30 | 51,0 | 91,0 | 99,0 | 95,0 |
| nimrodel | 39 | Jefe de los arqueros orcos L39 + Orco de las Montañas Nubladas L33 + Arquero orco L33 | 66,0 | 91,0 | 99,7 | 141,9 |

**MEDIO por zona (final)**

| Zona | Jefe | Media normales | Mínimo normales |
|---|---|---|---|
| bree | 97,0 | 100,0 | 100,0 |
| chet | 89,7 | 100,0 | 100,0 |
| moscagua | 93,0 | 100,0 | 100,0 |
| cima | 91,7 | 100,0 | 100,0 |
| puente | 93,3 | 99,9 | 99,3 |
| quebradas | 87,3 | 100,0 | 100,0 |
| bruinen | 91,7 | 99,8 | 99,3 |
| eregion | 88,7 | 99,1 | 96,3 |
| caradhras | 97,0 | 98,3 | 95,7 |
| durin | 86,3 | 98,1 | 95,3 |
| moria | 87,7 | 99,0 | 95,7 |
| sombrio | 91,0 | 98,9 | 95,0 |
| nimrodel | 91,0 | 99,8 | 99,3 |

**Los 10 encuentros normales MEDIO más letales (final)**

| Nodo | Nivel | Rivales | % | Ticks |
|---|---|---|---|---|
| sombrio_3_0 | 34 | Trasgo de Moria L32 + Trasgo de Moria L32 | 95,0 | 25,3 |
| durin_4_7 | 28 | Lobo de Saruman L26 + Trasgo de Moria L26 | 95,3 | 22,5 |
| caradhras_3_2 | 25 | Lobo de Saruman L23 + Lobo de Saruman L23 | 95,7 | 18,7 |
| caradhras_3_3 | 25 | Crebain L21 + Crebain L21 + Crebain L21 | 95,7 | 26,5 |
| durin_3_2 | 28 | Lobo de Saruman L26 + Lobo de Saruman L26 | 95,7 | 19,4 |
| durin_4_3 | 29 | Lobo de Saruman L26 + Trasgo de Moria L26 | 95,7 | 21,4 |
| moria_4_2 | 32 | Trasgo de Moria L29 + Trasgo de Moria L29 | 95,7 | 22,8 |
| durin_4_2 | 29 | Trasgo de Moria L26 + Trasgo de Moria L26 | 96,0 | 23,2 |
| eregion_3_3 | 22 | Lobo de Saruman L20 + Lobo de Saruman L20 | 96,3 | 17,0 |
| durin_4_4 | 29 | Lobo de Saruman L26 + Lobo de Saruman L26 | 97,3 | 18,1 |

### Valores finales del paso 6

```kotlin
RivalScaling.DEFAULT = RivalScaling(hpPct = 90, attackPct = 54, healPct = 100, defensePct = 100, startPowerPct = 92, regenPct = 100)            // sin cambio
RivalScaling.BOSS    = RivalScaling(hpPct = 150, attackPct = 80, healPct = 100, defensePct = 100, startPowerPct = 100, regenPct = 100)          // ataque de 78 a 80
```

- Patrones, `everyNTicks` y offsets de los jefes: tabla "Patrones de jefe" de 6.2 y bestiario (§3) de la spec de contenido. Offsets: bree +2, chet +3, moscagua +4, cima +5, puente +3, quebradas +7, bruinen +6, eregion +2, caradhras +6, durin +3, moria +7, sombrío +2 y nimrodel +2.
- `puente_7_0`: el jefe pelea solo.
- Simulador: la reserva del EXPERTO es el coste de `21` (10), en lugar de 26.
- Aventura completa: NOVATO 2,3 %, MEDIO 27,8 % y EXPERTO 60,7 % (objetivos: 2–10 %, 20–30 % y 50–70 %).
- Normales MEDIO: media 99,4 % y mínimo 95,0 % (`sombrio_3_0`); ninguno por debajo del 95 %.
- Jefes MEDIO: media 91,2 %, mínimo 86,3 % (durin) y máximo 97,0 % (bree y caradhras). Ninguno queda fuera de 75–98 % ni al 99–100 %.
- Combates al tope de ticks en las aventuras: 0 NOVATO, 0 MEDIO y 1 EXPERTO (de 2000 por perfil).
- `final-encounters.csv` y `final-adventures.csv` son los de este estado.

### Pendiente para pruebas reales

1. **El NOVATO depende de los primeros jefes.** Completa el 2,3 %, en el borde bajo de su rango (2–10 %; el objetivo era ≈ 5 %). Muere sobre todo en chet (35 % de las partidas), moscagua (29 %) y eregion (13 %). Al nivel esperado gana el 72,7 % en chet, el 20,7 % en moscagua y el 23,7 % en eregion, pero con un nivel menos pierde casi siempre (chet a L5, moscagua a L8). Ningún jefe lo deja a cero al nivel esperado; por debajo del 25 % quedan moscagua y eregion. Hay que comprobar si a los jugadores novatos reales les pasa lo mismo, porque el NOVATO simulado es determinista: sin estados de por medio, contra cada jefe gana siempre o pierde siempre según su nivel.
2. **Saltos por nivel.** Con los patrones nuevos, varios jefes siguen pasando de casi imposibles a fáciles con un solo nivel de diferencia: cima, chet y moscagua para el NOVATO, y cima (L11 frente a L12) para el MEDIO. Los aturdimientos y el miedo suavizan la curva. Si en el juego real los saltos se notan, el siguiente paso sería añadir un `31` o un `313` a los patrones que aún no lo tienen.
3. **EXPERTO frente a grupos de área.** Con la reserva nueva, el EXPERTO simulado gana menos que el MEDIO en `caradhras_6_0` (91,3 % frente a 98,7 %), y ese nodo se lleva el 9,8 % de sus partidas. Es un artefacto de la política (gasta en combos de área y se queda sin poder para la cura grande), no del contenido.
4. **Combates largos.** El MEDIO tarda 66 ticks en bree, 77 en moscagua, 95 en sombrío y 142 en nimrodel (más de dos minutos de juego, a un tick por segundo). Hay que confirmar en pruebas reales que no se hacen pesados.
5. **Una persona real no es la política simulada.** El 27,8 % del MEDIO y el 60,7 % del EXPERTO dependen de que el MEDIO simulado se escude solo contra jefes y no falle pulsaciones. Si los jugadores reales rinden menos, los porcentajes reales bajarán y habrá que aflojar; lo más directo es volver a bajar el ataque de los jefes de 80 a 78.

## Combate v2 (2026-10-12): re-balance con tiers y cadenas

El combate v2 (spec `2026-10-12-combat-v2-design.md`) cambia todo el modelo de daño: ya no hay daño inmediato salvo `123`, el daño viene de DoT por tiers que se acumulan, las curas son CoT y los combos se encadenan. Los números del paso 6 dejan de valer, así que se rehízo el balance con el mismo método:

- mismas semillas, 300 combates por encuentro al nivel esperado y 2000 aventuras por perfil;
- un solo tipo de ajuste cada vez, en este orden: `ComboTuning`, `RivalScaling.DEFAULT`, `RivalScaling.BOSS`, y después offsets y patrones de jefe;
- objetivos "Exigente" sin cambios (tabla del principio de este documento).

Herramientas nuevas del simulador:

- `sweep` acepta `spear`, `fist` y `cure` (`ComboTuning`) además de las claves de `RivalScaling`;
- `sweep offsets <zona> [offsets] [pattern=a;b;c] [every=N] [levels=-1,0,1]` prueba un jefe con otro offset, otro patrón u otro `everyNTicks` (en memoria, sin tocar los JSON) y con el jugador por encima o por debajo del nivel esperado;
- cada punto de barrido imprime también el % de NOVATO/MEDIO/EXPERTO de cada jefe.

### Cómo es un combate v2 (lo que se aprendió)

1. **Los combates son carreras cortas limitadas por el poder.** Los dos bandos gastan los 100 puntos de poder iniciales en una cadena y luego viven de la regeneración (5 por tick). Un combate MEDIO dura de media entre 6 y 32 ticks (en el paso 6, entre 17 y 142).
2. **Casi no hay azar.** Solo lo da el Aturdir (la tirada de Daze). Sin Aturdir, un combate al mismo nivel se gana siempre o se pierde siempre. Por eso todos los jefes llevan ahora al menos un `31`.
3. **El daño por tick va en escalones.** SPDMG = ataque × 20 / 100 es entero, así que subir el offset de un jefe solo cambia algo cada 2–3 niveles (por ejemplo, durin con +3 y +4 da exactamente lo mismo). Por debajo de `spear` 20, los DoT de los rivales débiles se quedan en el mínimo de 1 y aparecen combates atascados contra rivales que se curan (502–1145 combates al tope de 600 ticks con `spear` 15–18).
4. **El escudo y la cura del jugador ganan a un jefe solitario que se queda sin poder.** Escudo maestro (−50 % durante 60 ticks, por 20 de poder) y la cadena de Cura deshacen la ráfaga inicial del jefe. Seis jefes seguían al 100 % MEDIO aunque se les diera +8 de offset, 2,5 veces más PV o un 80 % más de regeneración de poder. Lo que los cambia es el patrón.
5. **En los jefes con escolta, el tier 3 de Lanza es lo que mata.** Con `1212` en el patrón, el jefe de eregion se quedaba en el 40–50 % aunque lanzara cada 2 ticks; sin él, sube al 79 %.

### v2.0 Política simulada (TDD, `HumanPlayerTest`)

Reglas (la cadencia no cambia: NOVATO cada 2 ticks, MEDIO 1,5 y EXPERTO 1):

- **Paso de cadena:** se lanza el tier siguiente de la cadena actual (tier t+1) o el tier 1 si la cadena está rota o en su tier máximo. Si el siguiente tier no se puede pagar, el jugador **espera** para no romper la cadena.
- **NOVATO** (desde la ronda de corrección 1, ver «NOVATO»): cadena de Lanza hasta el tier 3 (`12, 121, 1212` y vuelta a `12`) y, por debajo del 30 % de vida, cadena de Cura hasta el tier 2 (`21, 212`). Espera al siguiente tier como el MEDIO. No usa apertura contra jefes, área, multiplicadores, escudos, `123` ni Robo de vida.
- **MEDIO y EXPERTO** (objetivo: el rival con menos vida, o el sanador con menos vida si quedan 2 o más sanadores), por orden:
  1. por debajo del 40 % de vida, cadena de Cura;
  2. contra un jefe (`Fighter.isBoss`), una apertura de Aturdir sobre el jefe, una sola vez;
  3. (EXPERTO) `123` si mata al objetivo;
  4. contra un jefe y sin Escudo maestro activo: `2132` con 40 de poder o más y al menos 8 ticks desde el anterior; si no, `22` si no hay Guardia;
  5. con 2 o más rivales vivos (y menos de 2 sanadores), cadena de Puño en área. El EXPERTO lanza la cadena de Robo de vida al completar una cadena de área, si no tiene un Robo activo;
  6. (EXPERTO) `11` antes de empezar una cadena de Lanza, si a la Furia le quedan menos de 15 ticks y no hay Guardia, para que los tiers 3 y 4 salgan con Furia;
  7. cadena de Lanza (`12…12121`) sobre el objetivo.

Ajustes de la primera versión, medidos con los números de partida (`ComboTuning` 20/12/25, DEFAULT 90/54, BOSS 150/80, patrones de la tarea 2):

| Versión de la política | NOVATO | MEDIO | EXPERTO | Qué enseña |
|---|---|---|---|---|
| P0: apertura completa `31…31313`, reserva del EXPERTO (coste de `21`), Robo de vida al empezar contra grupos | 0,0 | 2,1 | 0,6 | El EXPERTO queda por debajo del MEDIO. |
| P1: la apertura termina en cuanto el jefe queda aturdido | 0,0 | 3,9 | 1,0 | |
| P2: sin reserva del EXPERTO; Robo de vida solo tras completar una cadena de área | 0,0 | 3,9 | 0,3 | Con la regla de esperar al siguiente tier, la reserva solo retrasaba la ráfaga. El Robo de vida al principio perdía la carrera. |
| P3: apertura solo hasta el tier 3 (`31, 313, 3131`) | 0,0 | 7,0 | 14,3 | `31313` cuesta 34 y la apertura completa (88 de poder) costaba más de lo que daba. Sin apertura, MEDIO 14,8 y EXPERTO 23,4; con tope en el tier 1, 2 o 3, el EXPERTO duplica al MEDIO, y con el tier 4 cae por debajo. |
| P4: no se aturde a un jefe con Escudo táctico activo (inmune) | 0,0 | 7,0 | 14,3 | Sin efecto medible (el jefe de eregion activa el escudo cuando la apertura ya ha terminado). |

Las tres reglas propias del EXPERTO (`123`, Furia y Robo de vida) quedan cada una dentro de ±2 puntos de la aventura (se midió quitando cada una); se mantienen porque son las de la spec.

### v2.1 `ComboTuning`

`sweep spear=16,18,20,22,25 fist=12,15,18 cure=25,35,50 fights=300 runs=2000` (política P3, resto sin cambios). Extracto:

| spear / fist / cure | NOVATO | MEDIO | EXPERTO | Normales MEDIO media / mín. | Jefes MEDIO media / mín. / máx. | Fuera de 75–98 | Al tope |
|---|---|---|---|---|---|---|---|
| 16 / 18 / 25 | 0,0 | 25,8 | 35,0 | 100,0 / 99,0 | 91,1 / 44,3 / 100 | 9 | 502 |
| 18 / 18 / 25 | 0,0 | 16,6 | 28,5 | 99,9 / 95,3 | 89,1 / 39,3 / 100 | 9 | 502 |
| **20 / 12 / 25 (antes)** | 0,0 | 7,0 | 14,3 | 97,8 / 0,0 | 86,5 / 36,3 / 100 | 8 | 0 |
| 20 / 15 / 25 | 0,0 | 9,3 | 17,3 | 98,9 / 0,0 | 86,4 / 37,3 / 100 | 8 | 0 |
| **20 / 18 / 25 (elegido)** | 0,0 | 11,9 | 21,8 | 99,9 / 95,3 | 86,9 / 38,7 / 100 | 8 | 0 |
| 20 / 18 / 50 | 0,0 | 20,8 | 31,8 | 99,9 / 95,7 | 89,9 / 44,3 / 100 | 8 | 0 |
| 22 / 18 / 25 | 0,0 | 7,5 | 12,1 | 99,7 / 75,3 | 83,5 / 32,3 / 100 | 7 | 0 |
| 25 / 18 / 25 | 0,0 | 3,1 | 2,6 | 99,6 / 75,3 | 79,5 / 27,7 / 100 | 10 | 0 |

- `spear` por debajo de 20 crea combates atascados (al tope de ticks). Por encima de 20, los rivales ganan peso por redondeo.
- `fist` 18 es necesario para los grupos: con 12 o 15, dos encuentros de tres huargos o trol + trasgo (bruinen_5_2, moria_4_5) quedan al 0 % MEDIO.
- `cure`: con 50 la cadena de Cura del jugador rinde 2,5 veces lo que el daño por poder gastado y aplana la diferencia entre perfiles (EXPERTO/MEDIO = 1,5). Con 25, esa relación es 1,8 y los jefes castigan más el retraso del MEDIO. Se elige 25 (el valor inicial).

| Barrido de `cure` (spear 20, fist 18) | MEDIO | EXPERTO | EXPERTO / MEDIO |
|---|---|---|---|
| 25 | 11,9 | 21,8 | 1,83 |
| 30 | 13,1 | 22,0 | 1,68 |
| 35 | 15,6 | 22,9 | 1,47 |
| 40 | 17,6 | 28,0 | 1,59 |
| 50 | 20,8 | 31,8 | 1,53 |

Valor elegido: `ComboTuning(spearBasePct = 20, fistBasePct = 18, cureBasePct = 25)`.

### v2.2 `RivalScaling.DEFAULT` (comunes)

Barrido con los valores finales de todo lo demás (`sweep attack=48,51,54,57,60 fights=300 runs=2000`):

| Ataque de comunes | NOVATO | MEDIO | EXPERTO | Normales MEDIO media / mín. | Jefes fuera de rango | Al tope |
|---|---|---|---|---|---|---|
| 48 | 0,0 | 29,0 | 58,3 | 99,9 / 95,3 | 0 | 735 |
| 51 | 0,0 | 28,3 | 57,7 | 99,9 / 95,3 | 0 | 0 |
| **54 (sin cambio)** | 0,0 | 28,4 | 57,2 | 99,9 / 95,3 | 0 | 0 |
| 57 | 0,0 | 22,6 | 50,2 | 99,7 / 75,3 | 1 | 0 |
| 60 | 0,0 | 21,8 | 49,9 | 99,7 / 75,3 | 1 | 0 |

El barrido de PV (80/90/100) movía la aventura menos de 2 puntos. Los comunes casi no deciden la aventura MEDIO/EXPERTO (la deciden los jefes). Se mantiene `RivalScaling.DEFAULT` = PV 90, ataque 54, poder inicial 92.

### v2.3 `RivalScaling.BOSS` global: no basta

Con los patrones de la tarea 2, ninguna curva global mete a los 13 jefes en rango. Barridos con `ComboTuning` 20/18/25 (MEDIO por jefe):

| Curva de jefe | MEDIO | EXPERTO | Jefes al 100 % MEDIO | eregion | nimrodel |
|---|---|---|---|---|---|
| PV 150, ataque 80 (antes) | 11,9 | 21,8 | moscagua, cima, quebradas, bruinen, moria, sombrío | 39 | 73 |
| PV 150, ataque 80, regen 180 % | 7,1 | 17,6 | moscagua, cima, quebradas, bruinen, moria | 39 | 69 |
| PV 150, ataque 80, poder 150 % | 8,5 | 18,0 | moscagua, quebradas, bruinen, moria, sombrío | 39 | 71 |
| PV 150, ataque 110 | 1,8 | 2,0 | moscagua, bruinen, moria | 37 | 84 |
| PV 150, ataque 130 | 0,3 | 0,5 | bruinen | 33 | 73 |

Además, con `cure` 50, subir los PV de jefe a 200 o 250 no movía a esos seis jefes del 100 %. El ataque global solo los baja cuando ya ha hundido a los demás (con ataque 110, sombrío pasa del 100 al 15 %). La conclusión es que hay que cambiar los patrones (v2.4).

### v2.4 Patrones y offsets de jefe

Se probaron unas 50 variantes con `sweep offsets` (200–300 combates, jugador en el nivel esperado y a ±1). Para cada jefe se eligió el cambio más pequeño que lo dejara en 80–97 % MEDIO, que fuera estable a ±1 nivel y en el que el EXPERTO ganara al menos tanto como el MEDIO. Todas las variantes cumplen las reglas del bestiario (abren con daño, al menos la mitad de daño, como mucho una cura y cadenas reales). TDD: primero `ContentRulesTest.v2BossPatterns` (rojo), después `Bestiary.bosses` y después los JSON (`RealContentTest` en verde).

Iteraciones (aventura completa; las columnas de jefes son MEDIO):

| Iteración | Cambio | Ataque de jefe | NOVATO | MEDIO | EXPERTO | Jefes MEDIO media / mín. / máx. | Fuera de 75–98 |
|---|---|---|---|---|---|---|---|
| inicio | patrones de la tarea 2 | 80 | 0,0 | 11,9 | 21,8 | 86,9 / 38,7 / 100 | 8 |
| 4a | un `31` en moscagua, cima, quebradas, bruinen, moria y sombrío; eregion sin `1212` y cada 2 ticks; nimrodel con `2131` antes de `31`; offsets: quebradas +4, durin +2, sombrío +3 | 80 | 0,0 | 14,3 | 23,0 | 85,1 / 79,7 / 93,3 | 0 |
| 4a + ataque 70 | barrido `sweep boss attack=60..80`: con 70, MEDIO entra en rango | 70 | 0,0 | 23,2 | 32,4 | 89,3 / 79,7 / 100 | 1 (bree) |
| 4b | offsets que dan más estabilidad a ±1 nivel: chet +3 → +2 (a L−1, 93 % en lugar de 63 %), caradhras +6 → +7, quebradas +4 → +5 | 70 | 0,0 | 26,6 | 36,4 | 90,3 / 81,7 / 100 | 1 (bree) |
| 4c | bree +2 → +3; moria sin `32323`; sombrío sin `12121`; nimrodel cada 2 ticks | 70 | 0,0 | 27,3 | 41,9 | 90,0 / 82,7 / 97,3 | 0 |
| 4d | quebradas sin `12121`; caradhras sin `32323` | 70 | 0,0 | 26,4 | 50,9 | 90,6 / 82,7 / 97,3 | 0 |
| 4e | sombrío `12, 121, 31, 11` a +5 | 70 | 0,0 | 30,9 | 58,7 | 91,4 / 85,0 / 97,3 | 0 |
| **v2.5 (final)** | ataque de jefe 72 | 72 | 0,0 | 28,4 | 57,2 | 91,1 / 80,7 / 97,3 | 0 |

`sweep boss attack=70,71,72,73,74` sobre 4e: 70 y 71 dan 30,9 / 58,7 (MEDIO por encima de 30), 72 y 73 dan 28,4 / 57,2, y 74 da 23,0 / 53,2. Se elige **72**.

**Patrones de jefe (antes → después)**

| Zona | Antes: pattern / cada N ticks / offset | Después: pattern / cada N ticks / offset |
|---|---|---|
| bree | `12, 121, 1212, 11, 31, 21` / 2 / +2 | mismo patrón / 2 / **+3** |
| chet | `12, 121, 1212, 12121, 31, 33` / 2 / +3 | mismo patrón / 2 / **+2** |
| moscagua | `32, 323, 3232, 13, 33` / 1 / +4 | `32, 323, 3232, 13, 31` / 1 / +4 (`33` → `31`) |
| cima | `32, 323, 3123, 31, 313` / 1 / +5 | `32, 323, 3232, 31, 313` / 1 / +5 (`3123` → `3232`) |
| puente | `12, 121, 1212, 31, 11` / 1 / +3 | sin cambios |
| quebradas | `12, 121, 1212, 12121, 11` / 2 / +7 | `12, 121, 1212, 31, 11` / **1** / **+5** (`12121` → `31`) |
| bruinen | `32, 323, 3232, 3123, 31232` / 1 / +6 | `32, 323, 3232, 3123, 31232, 31` / 1 / +6 (se añade `31`) |
| eregion | `12, 121, 1212, 11, 2131` / 1 / +2 | `12, 121, 31, 2131` / **2** / +2 (sin `1212`; `11` → `31`) |
| caradhras | `32, 323, 3232, 32323, 31, 313, 22` / 1 / +6 | `32, 323, 3232, 31, 313, 22` / 1 / **+7** (sin `32323`) |
| durin | `13, 131, 1313, 31, 313, 3131` / 1 / +3 | mismo patrón / 1 / **+2** |
| moria | `32, 323, 3232, 32323, 11, 123` / 1 / +7 | `32, 323, 3232, 31, 123` / 1 / +7 (sin `32323`; `11` → `31`) |
| sombrio | `12, 121, 1212, 12121, 11, 2132` / 1 / +2 | `12, 121, 31, 11` / 1 / **+5** (sin `1212` ni `12121`; `2132` → `31`) |
| nimrodel | `32, 323, 3232, 31, 2131, 21` / 1 / +2 | `32, 323, 3232, 2131, 31, 21` / **2** / +2 (`2131` antes de `31`) |

Por qué: el `31` da el azar que mete al jefe en el rango (sin él, 0 o 100 %), y quitar el tier 3 o 4 de Lanza o de Área a los jefes en grupo o con ráfagas evita que el combate se decida en los cuatro primeros ticks. La Furia de moria (`11`), que no afecta al área, pasa a ser un `31`.

### v2.6 Bree, zona tutorial (ronda de corrección 2)

Decisión del coordinador: Bree pasa a ser una zona tutorial suave, para que el NOVATO la supere en la mayoría de las partidas. Solo se toca Bree, un cambio cada vez y con las mismas semillas:

| Paso | Cambio | NOVATO llega a chet | NOVATO | MEDIO | EXPERTO | Jefe de bree N / M / E |
|---|---|---|---|---|---|---|
| inicio | ronda de corrección 1 | 0,0 | 0,0 | 28,4 | 57,2 | 0 / 87,7 / 90,7 |
| 1 | jefe `12, 121, 11, 31` cada 2 ticks a +2 (antes `12, 121, 1212, 11, 31, 21` a +3) | 15,3 | 0,0 | 30,5 | 62,6 | 62 / 99,7 / 100 |
| 2 | bree_3_3 con un solo rival: el Bandido del foso (offset 0); el lobo huye | 23,8 | 0,0 | 30,5 | 62,6 | |
| 3 | bree_4_5 con un solo Lobo hambriento (offset 0); el otro se escabulle | 31,9 | 0,0 | 30,5 | 62,6 | |
| 4 | jefe de bree cada 4 ticks | 66,7 | 0,0 | 30,6 | 62,8 | 75 / 100 / 100 |
| 5 | ataque de jefe 72 → 74 (73 da lo mismo que 72 por redondeo) | 66,7 | 0,0 | **24,8** | **58,8** | 75,3 / 100 / 100 |

- Los rivales de bree ya están en el nivel 1 (base 1; los offsets negativos se recortan a 1), así que bajar offsets no los aligera: solo se puede quitar un rival. Con un rival, §5 permite offset 0 o +1; se usa 0. Los textos y los `imagePrompt` de los dos nodos cambian lo mínimo, y también sus filas en `docs/content/prompts-imagenes.md`.
- Ninguna variante del jefe de bree deja al MEDIO por debajo del 98 % y a la vez deja ganar al NOVATO: con `1212` en el patrón el NOVATO pierde siempre; sin él, el MEDIO gana el 99–100 % con cualquier offset. Se acepta **bree al 100 % MEDIO como excepción de zona tutorial** (el único jefe fuera de 75–98 %).
- Con el jefe cada 4 ticks, el NOVATO también le gana si llega con un nivel menos (84 %; cada 2 ticks, 0 %).
- El NOVATO sigue completando el 0 %: ahora lo para chet (chet_7_0, el 51,9 % de las partidas, y chet_5_0, el 14,8 %), y bree_6_1 (Vigía + Lobo, fuera de los cambios permitidos) se lleva el 24,5 %. Llevarlo a 2–10 % exigiría tocar zonas posteriores, fuera de esta decisión.
- Con Bree más fácil, MEDIO y EXPERTO subieron a 30,6 / 62,8; un paso de ataque de jefe (74) los devuelve a 24,8 / 58,8. Con 74, cima baja a 83,0 % MEDIO y durin a 87,0 %.

### Resultados del combate v2

| Métrica | Inicio (P0, números del paso 6) | P3 (política final) | v2.1 `ComboTuning` | v2.4a patrones | v2.5 | Final (v2.6, Bree tutorial) |
|---|---|---|---|---|---|---|
| NOVATO aventura completa | 0,0 % | 0,0 % | 0,0 % | 0,0 % | 0,0 % | 0,0 % |
| MEDIO aventura completa | 2,1 % | 7,0 % | 11,9 % | 14,3 % | 28,4 % | **24,8 %** |
| EXPERTO aventura completa | 0,6 % | 14,3 % | 21,8 % | 23,0 % | 57,2 % | **58,8 %** |
| Combates al tope en aventuras (los tres perfiles) | 0 | 0 | 0 | 0 | 0 | 0 |
| MEDIO normales: mínimo | 0,0 | 0,0 | 95,3 | 95,3 | 95,3 | 95,3 |
| MEDIO normales < 95 % | 4 de 103 | al menos 2 de 103 | 0 de 103 | 0 de 103 | 0 de 103 | 0 de 103 |
| MEDIO jefes: media / mín. / máx. | — / 18,3 / 100 | 86,5 / 36,3 / 100 | 86,9 / 38,7 / 100 | 85,1 / 79,7 / 93,3 | 91,1 / 80,7 / 97,3 | 90,1 / 80,7 / 100 |
| MEDIO jefes fuera de 75–98 % | 9 de 13 | 8 de 13 | 8 de 13 | 0 de 13 | 0 de 13 | 1 de 13 (bree, tutorial) |

**Jefes por perfil (nivel esperado, 300 combates). Inicio de v2.4 (patrones de la tarea 2, `ComboTuning` final, ataque de jefe 80) → final** (final = v2.6, con la política NOVATO de la ronda de corrección 1)

| Zona | Nivel | NOVATO inicio → final | MEDIO inicio → final | EXPERTO inicio → final | Ticks MEDIO (final) |
|---|---|---|---|---|---|
| bree | 3 | 0 → 75,3 | 88 → 100,0 | 91 → 100,0 | 23,1 |
| chet | 6 | 0 → 0,0 | 80 → 91,0 | 93 → 95,0 | 22,6 |
| moscagua | 9 | 0 → 0,0 | 100 → 94,0 | 100 → 98,3 | 27,4 |
| cima | 12 | 0 → 0,0 | 100 → 83,0 | 100 → 91,3 | 26,1 |
| puente | 15 | 0 → 0,0 | 84 → 90,3 | 92 → 99,7 | 22,4 |
| quebradas | 18 | 0 → 0,0 | 100 → 87,3 | 91 → 92,0 | 23,3 |
| bruinen | 21 | 0 → 0,0 | 100 → 91,7 | 100 → 96,7 | 22,7 |
| eregion | 24 | 0 → 5,0 | 39 → 80,7 | 49 → 93,0 | 32,2 |
| caradhras | 27 | 0 → 41,7 | 90 → 91,0 | 91 → 99,3 | 28,0 |
| durin | 30 | 0 → 20,7 | 76 → 87,0 | 98 → 99,3 | 26,0 |
| moria | 33 | 0 → 72,7 | 100 → 90,0 | 100 → 94,7 | 25,4 |
| sombrio | 36 | 0 → 54,3 | 100 → 93,0 | 100 → 100,0 | 25,9 |
| nimrodel | 39 | 0 → 0,0 | 73 → 91,7 | 80 → 93,0 | 25,4 |

Jefes MEDIO (final): media 90,1 %, mínimo 80,7 % (eregion); fuera de 75–98 % solo bree (100 %, zona tutorial); el resto llega como mucho al 94,0 % (moscagua). EXPERTO: media 96,3 %, mínimo 91,3 % (cima). El NOVATO gana a bree (75,3 %), moria (72,7), sombrío (54,3), caradhras (41,7), durin (20,7) y eregion (5,0).

**% que llega a cada zona: inicio (P0) → final**

| Zona | NOVATO | MEDIO | EXPERTO |
|---|---|---|---|
| bree | 100 → 100 | 100 → 100 | 100 → 100 |
| chet | 0 → 66,7 | 89,3 → 100 | 77,9 → 100 |
| moscagua | 0 → 0 | 67,8 → 93,1 | 52,4 → 94,3 |
| cima | 0 → 0 | 67,8 → 88,1 | 52,4 → 90,0 |
| puente | 0 → 0 | 67,7 → 75,1 | 52,4 → 85,4 |
| quebradas | 0 → 0 | 46,2 → 67,0 | 27,0 → 83,3 |
| bruinen | 0 → 0 | 46,1 → 54,7 | 13,5 → 76,5 |
| rivendel | 0 → 0 | 37,6 → 50,2 | 9,2 → 73,1 |
| eregion | 0 → 0 | 37,6 → 50,2 | 9,2 → 73,1 |
| caradhras | 0 → 0 | 10,9 → 39,4 | 1,9 → 66,5 |
| durin | 0 → 0 | 9,3 → 36,3 | 1,7 → 65,4 |
| moria | 0 → 0 | 5,9 → 31,7 | 1,2 → 64,6 |
| sombrio | 0 → 0 | 4,9 → 28,7 | 1,1 → 62,8 |
| nimrodel | 0 → 0 | 3,0 → 27,1 | 0,8 → 62,4 |
| lothlorien | 0 → 0 | 2,1 → 24,8 | 0,6 → 58,8 |

Dónde terminan las aventuras (final): NOVATO, en chet_7_0 (51,9 % de las partidas), bree_6_1 (24,5 %), chet_5_0 (14,8 %) y bree_7_0 (8,8 %); MEDIO, en cima (13,0 %), quebradas (12,3 %), eregion (9,0 %) y puente (7,8 %); EXPERTO, en quebradas (6,8 %), eregion (5,8 %), chet (5,7 %) y cima (4,6 %).

**Los encuentros normales MEDIO más difíciles (final)**: eregion_6_0 (Espía + 2 Lobos de Saruman, L24) 95,3 %; puente_4_7 (Espía + Huargo, L14) 95,7 %; eregion_5_1 (Espía + Lobo de Saruman, L23) 99,0 %. El resto está al 100 %. EXPERTO: mínimo 98,3 % en normales.

### NOVATO: fuera de rango (0 %)

**Primera versión** (`12`, `121` y `21`, cada 2 ticks, nunca más allá del tier 2): mantiene 1 + 2 = 3 SPDMG por tick frente a los hasta 10 del MEDIO, pierde 56 de 103 encuentros normales al nivel esperado y no gana a ningún jefe. Exploración (con `cure` 50, comunes a PV 50 / ataque 30 / cura 50):

| Jefes (PV / ataque / cura) | NOVATO | MEDIO | EXPERTO |
|---|---|---|---|
| 60 / 20 / 50 | 11,3 | 98,8 | 99,8 |
| 60 / 30 / 50 | 7,0 | 97,1 | 98,3 |
| 100 / 20 / 50 | 5,1 | 98,7 | 99,8 |
| 100 / 30 / 50 | 0,2 | 97,1 | 98,3 |

Con esa política, el NOVATO solo llega a 2–10 % cuando el MEDIO pasa del 97 %.

**Ronda de corrección 1** (decisión del coordinador: la UI muestra la pista de cadena ⛓, así que un principiante real encadena). Política nueva (TDD: `novato heals with the cura chain up to tier 2…`, `novato chains the spear up to tier 3…` y `novato uses no boss tricks and no area`, en rojo antes del cambio): cadencia igual (cada 2 ticks); cadena de Lanza hasta el tier 3 y vuelta a `12`; cadena de Cura hasta el tier 2 por debajo del 30 %; espera al siguiente tier si no puede pagarlo; nada más. Mismas semillas, sin tocar ningún número:

| Política NOVATO | NOVATO | MEDIO | EXPERTO | Encuentros normales NOVATO < 95 % | Jefes NOVATO > 0 % | Al tope en aventuras |
|---|---|---|---|---|---|---|
| Primera versión (`12`/`121`/`21`) | 0,0 | 28,4 | 57,2 | 56 de 103 | ninguno | 0 (600 en la tabla de encuentros) |
| **Ronda 1: Lanza hasta el tier 3, Cura hasta el tier 2 (final)** | 0,0 | 28,4 | 57,2 | 32 de 103 | cima 38,7; eregion 5,0; caradhras 41,7; durin 14,3; moria 66,0; sombrío 54,3 | 0 (0 en la tabla de encuentros) |
| Lanza hasta el tier 2, Cura hasta el tier 2 | 0,0 | 28,4 | 57,2 | | | 0 |
| Lanza hasta el tier 4 | 0,0 | 28,4 | 57,2 | | | 0 |
| Cura hasta el tier 3 | 0,0 | 28,4 | 57,2 | | | 0 |
| Cura por debajo del 40 % (o del 50 %) | 0,0 | 28,4 | 57,2 | | | 379 |

El NOVATO mejora mucho en los combates (gana 71 de 103 normales y 6 jefes), pero todas las aventuras siguen terminando en bree, que lo bloquea de forma determinista a nivel 1–3: bree_7_0 (42,9 % de las partidas; el jefe le gana siempre), bree_3_3 (Bandido del foso + Lobo hambriento L1, 26,5 %), bree_6_1 (19,0 %) y bree_4_5 (dos Lobos hambrientos L1, 11,8 %). Las variantes se midieron solo en la aventura. Ninguna de un solo cambio lo saca de bree, así que se acepta la política de la ronda 1 (la ordenada). Después, la ronda de corrección 2 convierte Bree en zona tutorial (v2.6). El MEDIO y el EXPERTO no cambian (su política no cambia). Para un NOVATO en rango haría falta suavizar bree para él (por ejemplo, el jefe o los encuentros de dos rivales a nivel 1), lo que también subiría al MEDIO y al EXPERTO en bree.

### Valores finales del combate v2

```kotlin
ComboTuning.DEFAULT  = ComboTuning(spearBasePct = 20, fistBasePct = 18, cureBasePct = 25)                                           // fist de 12 a 18
RivalScaling.DEFAULT = RivalScaling(hpPct = 90, attackPct = 54, healPct = 100, defensePct = 100, startPowerPct = 92, regenPct = 100)  // sin cambio
RivalScaling.BOSS    = RivalScaling(hpPct = 150, attackPct = 74, healPct = 100, defensePct = 100, startPowerPct = 100, regenPct = 100) // ataque de 80 a 74
```

- Offsets de jefe: bree +2, chet +2, moscagua +4, cima +5, puente +3, quebradas +5, bruinen +6, eregion +2, caradhras +7, durin +2, moria +7, sombrío +5 y nimrodel +2. Los patrones están en la tabla de v2.4 (bree: `12, 121, 11, 31` cada 4 ticks, v2.6) y en el bestiario (§3) de la spec de contenido. bree_3_3 y bree_4_5 tienen un solo rival.
- Aventura completa: NOVATO 0,0 % (objetivo 2–10 %, fuera; el 66,7 % pasa Bree y lo para chet), MEDIO 24,8 % (20–30 %) y EXPERTO 58,8 % (50–70 %).
- Normales MEDIO: media 99,9 %, mínimo 95,3 % (eregion_6_0); ninguno por debajo del 95 %.
- Jefes MEDIO: media 90,1 %, entre 80,7 % (eregion) y 94,0 % (moscagua), salvo bree al 100 % (zona tutorial, excepción aceptada).
- Combates al tope de 600 ticks: 0 en las 6000 aventuras y 0 en la tabla de encuentros (con la primera política NOVATO había 600 en encuentros NOVATO, chet_4_1 y nimrodel_5_0).
- `final-encounters.csv` y `final-adventures.csv` son los de este estado.

### Pendiente para pruebas reales (combate v2)

1. **NOVATO.** Con Bree como tutorial, el 66,7 % la supera, pero completa el 0 %: lo para chet (jefe y chet_5_0). Hay que ver qué hace un jugador novato real en chet; si se atasca, el siguiente paso sería suavizar chet como se hizo con Bree.
2. **Bree es un tutorial.** Su jefe gana el 100 % de los combates MEDIO y EXPERTO, fuera del rango de jefes; es intencionado.
3. **El azar sale solo del Aturdir.** Los porcentajes de jefe dependen de que el jugador quede aturdido en mal momento (10 ticks sin actuar). Esto iguala a MEDIO y EXPERTO dentro de cada combate; la diferencia entre ellos se acumula sobre todo en la aventura. Si en el juego real el Daze se percibe como injusto, la alternativa es bajar la probabilidad de `31` o dar al jugador una respuesta (Escudo táctico) en la política.
4. **Combates cortos.** Un combate MEDIO dura de media entre 6 y 32 ticks (el más largo, eregion_7_0: 32). Hay que confirmar que no se hacen demasiado cortos ni que la ráfaga inicial lo decida todo.
5. **Escalones de offset.** Como el daño por tick es entero, el offset de un jefe solo cambia algo cada 2–3 niveles. Para afinar más, el siguiente paso es tocar el patrón, no el offset.
6. **Una persona real no es la política simulada.** El MEDIO simulado espera al siguiente tier en lugar de romper la cadena y se escuda al ver al jefe. Si los jugadores reales rinden menos, lo más directo es bajar el ataque de los jefes (74 → 72 sube el MEDIO al 30,6 % y el EXPERTO al 62,8 %).

## Power ×3 y escala ×100 (2026-10-12)

Petición del usuario: «el power se acaba a los rivales y dejan de atacar… debería tener por lo menos 3 veces más de power, al jugador y a los rivales. También es más vistoso ver ataques más altos, así que multiplica por 100 la vida y hazlo proporcionalmente al daño y curas de los combos».

Cambios en el motor (`LevelStats`, TDD con `LevelStatsTest` y `RivalScalingTest` en rojo primero):

- `LevelStats.MAX_POWER = 300` (antes 100) para todos. Los rivales siguen usando `RivalScaling.powerPct` (100 %) y empiezan con `startPowerPct` de ese máximo (comunes 92 % = 276; jefes 100 % = 300). La regeneración (5 por tick) y los costes de los combos no cambian.
- `LevelStats.STAT_SCALE = 100` multiplica vida, ataque, cura y defensa. Todas las fórmulas (DoT, CoT, Robo de vida, `123`) salen ×100 sin tocarlas; los porcentajes (Daze, multiplicadores, escudos) y los mínimos de 1 no cambian. Nivel 1: PV 10000, ataque 1000, cura 800, defensa 200.
- Efecto secundario de la escala: el redondeo entero deja de pesar (antes SPDMG = 10 × 20 / 100 = 2 y FIDMG = 10 × 18 / 100 = 1 a nivel 1; ahora 200 y 180). Los «escalones» del punto 3 de «Cómo es un combate v2» desaparecen y los `spear` bajos ya no atascan combates.

Mismo método que en el combate v2: mismas semillas, 300 combates por encuentro al nivel esperado, 2000 aventuras por perfil y un solo tipo de ajuste cada vez (`ComboTuning`, después `RivalScaling.DEFAULT` y `RivalScaling.BOSS`, después offsets de jefe).

### Rivales sin power: antes y después

Medido con la política MEDIO en todos los encuentros (30 combates por encuentro): % de turnos de lanzamiento de un rival (`tick % everyNTicks == 0`) en que no puede pagar el siguiente combo de su patrón y no ataca.

| | Turnos de rival sin power | Turnos de jefe sin power |
|---|---|---|
| Antes (power 100, combate v2 final) | 11,9 % | 21,8 % |
| Después (power 300, valores finales) | 1,1 % | 4,1 % |

### Línea base: cada cambio por separado (sin re-balance)

| Configuración | NOVATO | MEDIO | EXPERTO | Jefes MEDIO fuera de 75–98 % |
|---|---|---|---|---|
| Combate v2 final (escala 1, power 100) | 0,0 | 24,8 | 58,8 | 1 (bree, tutorial) |
| Solo escala ×100 | 0,0 | 13,4 | 40,5 | eregion 68,0; bree |
| Solo power ×3 | 0,0 | 29,1 | 84,8 | chet 98,7; bree |
| **Escala ×100 + power ×3 (línea base)** | 0,0 | **14,8** | **67,3** | eregion 73,3; bree |

En la línea base el MEDIO queda por debajo del rango (14,8 %), el mínimo de comunes MEDIO baja a 87,3 % (eregion_6_0) y el jefe de eregion queda en 73,3 %. El power ×3 ayuda sobre todo al EXPERTO (que lanza cada tick y ahora no se queda sin power), y la escala ×100 sube el daño real de los rivales débiles (antes redondeado hacia abajo).

### 1. `ComboTuning`

`sweep spear=16,18,20,22 fist=15,18,21 cure=20,25,30` y después `sweep spear=15,16,17 fist=19,20,21 cure=30,35` (resto sin cambios). Extracto:

| spear / fist / cure | NOVATO | MEDIO | EXPERTO | Normales MEDIO media / mín. | Jefes MEDIO media / mín. / máx. | Fuera de 75–98 | Al tope |
|---|---|---|---|---|---|---|---|
| **20 / 18 / 25 (antes)** | 0,0 | 14,8 | 67,3 | 99,8 / 87,3 | 87,4 / 73,3 / 100 | 2 | 0 |
| 20 / 21 / 25 | 0,0 | 10,1 | 51,7 | 99,8 / 87,3 | 86,0 / 66,7 / 100 | 3 | 0 |
| 18 / 18 / 30 | 0,0 | 25,3 | 78,6 | 99,9 / 96,3 | 90,3 / 81,7 / 100 | 1 | 0 |
| 16 / 18 / 25 | 0,0 | 29,8 | 80,7 | 100,0 / 96,3 | 90,9 / 78,3 / 100 | 2 | 0 |
| 16 / 19 / 35 | 0,0 | 30,2 | 74,7 | 100,0 / 96,7 | 90,9 / 77,7 / 100 | 2 | 0 |
| 16 / 20 / 35 | 0,0 | 27,6 | 68,4 | 100,0 / 96,7 | 90,3 / 70,7 / 100 | 3 | 0 |
| 16 / 21 / 30 | 0,0 | 22,6 | 59,7 | 100,0 / 97,3 | 90,1 / 72,3 / 100 | 3 | 0 |
| **16 / 21 / 35 (elegido)** | 0,0 | **24,8** | **60,5** | 100,0 / 96,7 | 90,4 / 74,3 / 100 | 3 | 0 |
| 17 / 21 / 35 | 0,0 | 22,6 | 60,0 | 99,9 / 96,7 | 89,9 / 74,3 / 100 | 3 | 0 |
| 15 / 21 / 35 | 0,0 | 27,9 | 59,7 | 100,0 / 96,7 | 90,8 / 74,3 / 100 | 4 | 0 |

- `spear` y `fist` los usan los dos bandos: subirlos ayuda más a los rivales (que son más y lanzan sin esperar) que al jugador. Con power ×3 el MEDIO y el EXPERTO se separan mucho (con `fist` 18 y `spear` 16–18 el EXPERTO pasa del 75 %); `fist` 21 castiga al EXPERTO, que pelea contra más grupos al llegar más lejos, y deja la relación EXPERTO / MEDIO en 2,4.
- `spear` 16 sin combates atascados: con la escala ×100 el DoT mínimo de un rival débil ya no es 1 (0 combates al tope en todo el barrido).
- `cure` 35 (antes 25) sube al MEDIO unos 2 puntos y al EXPERTO menos de 1.

Valor elegido: `ComboTuning(spearBasePct = 16, fistBasePct = 21, cureBasePct = 35)`. Queda el jefe de cima en 74,3 % y chet (99 %) y durin (98 %) en el borde alto.

### 2. `RivalScaling.DEFAULT` (comunes): sin cambio

| Barrido | NOVATO | MEDIO | EXPERTO | Normales MEDIO mín. |
|---|---|---|---|---|
| ataque 48 | 0,0 | 26,2 | 61,8 | 97,0 |
| **ataque 54 (sin cambio)** | 0,0 | 24,8 | 60,5 | 96,7 |
| ataque 60 | 0,0 | 23,7 | 59,7 | 96,7 |
| PV 80 / 100 | 0,0 | 24,9 / 24,4 | 60,8 / 60,5 | 99,0 / 96,7 |
| poder inicial 80 / 100 % | 0,0 | 24,8 / 24,8 | 60,5 / 60,5 | 96,7 / 96,7 |

Los comunes casi no mueven la aventura y el poder inicial ya no importa (con 300 de máximo, el 80 % sobra para la cadena inicial). Se mantiene PV 90, ataque 54, poder inicial 92.

### 3. `RivalScaling.BOSS`: sin cambio

| Barrido de jefes | NOVATO | MEDIO | EXPERTO | Jefes MEDIO mín. / máx. | Fuera de 75–98 |
|---|---|---|---|---|---|
| ataque 70 | 0,0 | 29,9 | 71,3 | 72,3 / 100 | 4 |
| ataque 72 | 0,0 | 26,7 | 65,1 | 75,3 / 100 | 3 |
| **ataque 74 (sin cambio)** | 0,0 | 24,8 | 60,5 | 74,3 / 100 | 3 |
| ataque 76 | 0,0 | 22,5 | 55,0 | 74,0 / 100 | 3 |
| ataque 78 | 0,0 | 20,2 | 51,9 | 69,0 / 100 | 4 |
| PV 130 / 140 | 0,0 | 25,5 / 25,0 | 64,0 / 62,5 | 74,3 / 100 | 3 |
| PV 160 / 170 | 0,0 | 24,2 / 23,8 | 58,2 / 55,8 | 74,3 / 100 | 3 |

Ninguna curva global arregla a la vez el jefe de cima (demasiado difícil) y los de chet y durin (demasiado fáciles), así que se mantiene PV 150 / ataque 74 y se ajustan offsets.

### 4. Offsets de jefe

`sweep offsets <zona> ... levels=-1,0,1` (300 combates, NOVATO/MEDIO/EXPERTO con el jugador a L−1, L y L+1):

| Zona | Offset | L−1 | L | L+1 |
|---|---|---|---|---|
| chet | +2 (antes) | 0/96/100 | 0/99/100 | 0/99/100 |
| chet | +3 | 0/96/100 | 0/97/100 | 0/99/100 |
| chet | **+4 (elegido)** | 0/96/99 | 0/96/100 | 0/99/100 |
| durin | +2 (antes) | 14/97/99 | 16/98/100 | 23/99/100 |
| durin | +3 | 14/97/100 | 14/97/99 | 16/98/100 |
| durin | **+4 (elegido)** | 14/96/100 | 14/97/100 | 14/97/99 |
| cima | +5 (antes) | 0/64/86 | 0/74/93 | 0/72/99 |
| cima | +4 | 0/74/88 | 0/72/99 | 45/82/100 |
| cima | **+3 (elegido)** | 0/71/97 | 45/82/100 | 45/89/100 |

chet y durin se saturan en torno al 96–97 % MEDIO (como en el combate v2, el offset deja de mover a un jefe solitario cuando el escudo y la cura del jugador deshacen su ráfaga); con +4 quedan dentro del rango. Cambiar sus patrones para centrarlos más queda pendiente de las pruebas reales. Los tres offsets cambian en los JSON (`chet.json`, `cima.json`, `durin.json`) y en la tabla de jefes de la spec de contenido (§3); `RealContentTest` y `ContentRulesTest` siguen en verde (offset de jefe entre +2 y +8).

### Resultados

| Métrica | Combate v2 final (antes) | Línea base (escala + power) | 1. `ComboTuning` | **Final (4. offsets)** |
|---|---|---|---|---|
| NOVATO aventura completa | 0,0 % | 0,0 % | 0,0 % | **0,0 %** |
| MEDIO aventura completa | 24,8 % | 14,8 % | 24,8 % | **27,8 %** |
| EXPERTO aventura completa | 58,8 % | 67,3 % | 60,5 % | **64,7 %** |
| Combates al tope (aventuras y encuentros) | 0 | 0 | 0 | **0** |
| MEDIO normales: media / mínimo | 99,9 / 95,3 | 99,8 / 87,3 | 100,0 / 96,7 | **100,0 / 96,7** (eregion_6_0) |
| MEDIO normales < 95 % | 0 de 103 | 1 de 103 | 0 de 103 | **0 de 103** |
| EXPERTO normales: mínimo | 98,3 | | | **96,0** (puente_4_7) |
| MEDIO jefes: media / mín. / máx. (sin bree) | 89,2 / 80,7 / 94,0 | | | **90,0 / 79,7 / 97,0** |
| MEDIO jefes fuera de 75–98 % | 1 (bree) | 2 (eregion, bree) | 3 (cima, chet, bree) | **1 (bree, tutorial)** |
| Ticks medios de un combate MEDIO (mín. – máx. por encuentro) | 6,0 – 32,2 | | | 6,0 – 29,2 |

**Jefes por perfil (nivel esperado, 300 combates): combate v2 final → final**

| Zona | Nivel | NOVATO | MEDIO | EXPERTO | Ticks MEDIO |
|---|---|---|---|---|---|
| bree | 3 | 75,3 → 100,0 | 100,0 → 100,0 | 100,0 → 100,0 | 23,1 → 15,7 |
| chet | 6 | 0,0 → 0,0 | 91,0 → 96,3 | 95,0 → 100,0 | 22,6 → 16,8 |
| moscagua | 9 | 0,0 → 0,0 | 94,0 → 85,7 | 98,3 → 86,3 | 27,4 → 21,2 |
| cima | 12 | 0,0 → 45,0 | 83,0 → 82,3 | 91,3 → 99,7 | 26,1 → 23,9 |
| puente | 15 | 0,0 → 59,3 | 90,3 → 95,7 | 99,7 → 100,0 | 22,4 → 21,4 |
| quebradas | 18 | 0,0 → 64,3 | 87,3 → 93,7 | 92,0 → 100,0 | 23,3 → 21,7 |
| bruinen | 21 | 0,0 → 0,0 | 91,7 → 87,3 | 96,7 → 90,3 | 22,7 → 24,8 |
| eregion | 24 | 5,0 → 14,0 | 80,7 → 90,3 | 93,0 → 98,0 | 32,2 → 29,2 |
| caradhras | 27 | 41,7 → 10,7 | 91,0 → 80,0 | 99,3 → 91,3 | 28,0 → 26,9 |
| durin | 30 | 20,7 → 13,7 | 87,0 → 97,0 | 99,3 → 99,7 | 26,0 → 27,5 |
| moria | 33 | 72,7 → 0,0 | 90,0 → 79,7 | 94,7 → 93,7 | 25,4 → 22,4 |
| sombrio | 36 | 54,3 → 59,3 | 93,0 → 96,7 | 100,0 → 100,0 | 25,9 → 20,0 |
| nimrodel | 39 | 0,0 → 0,0 | 91,7 → 95,7 | 93,0 → 100,0 | 25,4 → 17,7 |

Jefes MEDIO (final): media 90,8 % con bree; entre 79,7 % (moria) y 97,0 % (durin), salvo bree al 100 % (tutorial). EXPERTO: media 96,6 % sin bree, mínimo 86,3 % (moscagua, prácticamente igual que el MEDIO, 85,7 %).

**% que llega a cada zona: combate v2 final → final**

| Zona | NOVATO | MEDIO | EXPERTO |
|---|---|---|---|
| bree | 100 → 100 | 100 → 100 | 100 → 100 |
| chet | 66,7 → 94,3 | 100 → 100 | 100 → 100 |
| moscagua | 0 → 0 | 93,1 → 96,8 | 94,3 → 99,3 |
| cima | 0 → 0 | 88,1 → 83,2 | 90,0 → 92,1 |
| puente | 0 → 0 | 75,1 → 68,7 | 85,4 → 89,0 |
| quebradas | 0 → 0 | 67,0 → 65,1 | 83,3 → 88,7 |
| bruinen | 0 → 0 | 54,7 → 60,4 | 76,5 → 88,5 |
| rivendel / eregion | 0 → 0 | 50,2 → 52,7 | 73,1 → 80,3 |
| caradhras | 0 → 0 | 39,4 → 47,2 | 66,5 → 77,5 |
| durin | 0 → 0 | 36,3 → 37,3 | 65,4 → 70,3 |
| moria | 0 → 0 | 31,7 → 35,7 | 64,6 → 70,3 |
| sombrio | 0 → 0 | 28,7 → 30,0 | 62,8 → 65,3 |
| nimrodel | 0 → 0 | 27,1 → 28,9 | 62,4 → 65,0 |
| lothlorien | 0 → 0 | 24,8 → **27,8** | 58,8 → **64,7** |

Dónde terminan las aventuras (final): NOVATO, en chet_7_0 (93,1 %), bree_7_0 (5,7 %) y chet_5_0 (1,2 %); MEDIO, en cima (14,5 %), moscagua (13,6 %), caradhras (9,9 %) y bruinen (7,8 %); EXPERTO, en bruinen (8,2 %), moscagua (7,2 %), caradhras (7,2 %) y moria (5,0 %).

**NOVATO**: sigue en 0 % (objetivo 2–10 %, fuera, como en el combate v2). Ahora el 94,3 % pasa Bree (antes el 66,7 %) y el jefe de chet lo para en el 93,1 % de las partidas. El siguiente paso, si las pruebas reales lo confirman, sería suavizar chet para el NOVATO como se hizo con Bree.

### Interfaz

Los números grandes caben en la pantalla de combate (TDD con `CombatLayoutTest.lateGameNumbersFitTheCards` y `CombatTextStripTest.lateGameNumbersFitTheirStripsWhateverTheFontScale`, que calculan con el motor los números del final de la primera vuelta, nivel 45, y miden el texto con Robolectric en modo gráfico nativo):

- tira de números: la del rival pasa de 48 a 56 dp y la del jugador de 56 a 72 dp, con texto sans-serif en negrita (10 y 12 dp, sin cambio de tamaño) en lugar de la serif del tema. Peor caso calculado: «−14716 GD» (Golpe directo con Furia del jugador sobre un común de nivel 40) y «+26880 RV2» (pulso de Robo de vida encadenado contra cuatro rivales);
- barras: «150000/150000» (jefe de nivel 46) cabe en la barra de una tarjeta de rival;
- la línea de stats («ATQ 7400 · DEF 4700») usa espacios de no separación entre la etiqueta y el número y puede ocupar dos líneas, partiendo solo tras «·».

### Valores finales

```kotlin
LevelStats.STAT_SCALE = 100   // vida, ataque, cura y defensa ×100
LevelStats.MAX_POWER  = 300   // antes 100; regeneración 5 y costes sin cambio
ComboTuning.DEFAULT   = ComboTuning(spearBasePct = 16, fistBasePct = 21, cureBasePct = 35)                                       // antes 20 / 18 / 25
RivalScaling.DEFAULT  = RivalScaling(hpPct = 90, attackPct = 54, healPct = 100, defensePct = 100, startPowerPct = 92, regenPct = 100)  // sin cambio
RivalScaling.BOSS     = RivalScaling(hpPct = 150, attackPct = 74, healPct = 100, defensePct = 100, startPowerPct = 100, regenPct = 100) // sin cambio
```

- Offsets de jefe: bree +2, chet **+4** (antes +2), moscagua +4, cima **+3** (antes +5), puente +3, quebradas +5, bruinen +6, eregion +2, caradhras +7, durin **+4** (antes +2), moria +7, sombrío +5 y nimrodel +2. Patrones sin cambio.
- Aventura completa: NOVATO 0,0 % (fuera de 2–10 %; lo para chet), MEDIO 27,8 % (20–30 %) y EXPERTO 64,7 % (50–70 %).
- Normales MEDIO: media 100,0 %, mínimo 96,7 % (eregion_6_0); ninguno por debajo del 95 %.
- Jefes MEDIO: entre 79,7 % (moria) y 97,0 % (durin), salvo bree al 100 % (tutorial).
- Combates al tope de 600 ticks: 0.
- Las huellas de `HpRegressionTest` se regrabaron desde el motor nuevo (la de derrota pasa de rivales de nivel 9 a 12, porque a nivel 9 ya no acababa en derrota). Los tests de mecánica usan un `TEST_TUNING` fijo (20 / 18 / 25) para que el re-balance no los cambie; `DamageFormulaTest` fija el valor por defecto.
- `final-encounters.csv` y `final-adventures.csv` son los de este estado.

### Pendiente para pruebas reales (power ×3 y escala ×100)

1. **NOVATO**: 0 %, lo para chet (93,1 %). Igual que en el combate v2.
2. **chet y durin al 96–97 % MEDIO**: dentro del rango pero en el borde alto; el offset ya no los mueve. Si se quieren más centrados, hay que tocar su patrón.
3. **moscagua**: el EXPERTO gana lo mismo que el MEDIO (86 %). Es el jefe de área con escolta; si el jugador real lo nota injusto, el patrón es el sitio.
4. **Combates más cortos contra jefes** (de 25,6 a 22,8 ticks de media MEDIO): con 3 veces más power los dos bandos encadenan más tiers al principio.
5. **Textos en pantalla**: los anchos se midieron con Robolectric; falta confirmarlos en el Moto G85 con un combate de final de vuelta (y en una segunda vuelta, donde los números siguen siendo de 5 cifras).

## Dificultad base subida (+ combo 321) (2026-10-12)

Dos peticiones del usuario:

1. **Combo nuevo `321` «Recuperar»**: coste 0, recupera al instante el 10 % del power máximo del lanzador (30 con 300) sin pasar del máximo; sin tiers (familia `RECOVER`, tier 0), así que rompe la cadena como cualquier combo sin tiers; objetivo uno mismo; sin efecto duradero ni insignia. Los rivales no lo usan (bestiario sin cambios).
2. **Subir la dificultad base** un nivel para todos. Objetivos nuevos: aventura completa MEDIO 10–15 % (≈ 12) y EXPERTO 30–40 % (≈ 35); NOVATO, solo se informa; cada común MEDIO ≥ 93 %; cada jefe MEDIO entre el 65 y el 92 % salvo bree (tutorial); combates al tope ≈ 0.

### 1. Combo `321` y política simulada

Motor (TDD, rojo primero: `ComboCatalogTest` con 29 combos, `ComboChainTest.untieredComboBreaks` con `321`, `ComboApplierTest.recoverRestoresTenPercentOfMaxPowerClamped`): `ComboFamily.RECOVER` y `ComboEffect.PowerRestore(pct = 10)`; el efecto suma `maxPower × 10 / 100` al power y lo limita al máximo. Solo emite `ComboResolved`.

App: `ComboDescriber` («Recuperar · recupera el 10% de tu power al instante»), familia nueva «Recuperar» en la ayuda (fila `combo_row_321`, «Coste 0») y sin código de insignia (no deja efecto).

Política (`HumanPlayerTest`): MEDIO y EXPERTO lanzan `321` cuando no pueden pagar el siguiente paso de la cadena que han decidido (Aturdir de apertura, Puño en área / Robo de vida o Lanza), siempre que no estén en peligro inmediato (vida < 40 %: la rama de Cura espera, como antes) y que no rompan una cadena en marcha cuyo siguiente tier llegue en 3 ticks de regeneración o menos (en ese caso esperan, como antes). El NOVATO no lo usa.

Efecto medido: ninguno sobre el balance. Con 300 de power, el MEDIO no se queda nunca sin power para su plan (power mínimo observado antes de decidir: 93 con los valores anteriores y 56 con los finales; 0 lanzamientos de `321` en 19 182 decisiones, 20 combates por encuentro). El EXPERTO, que lanza cada tick, lo usa 28 veces en 24 059 decisiones con los valores finales (0 con los anteriores). La línea base con la política nueva es idéntica al final de «Power ×3 y escala ×100»: NOVATO 0,0 %, MEDIO 27,8 %, EXPERTO 64,7 %.

### 2. Re-balance (mismas semillas, 300 combates por encuentro, 2000 aventuras por perfil, un tipo de ajuste cada vez)

**`RivalScaling.DEFAULT` (comunes)**

| PV / ataque | NOVATO | MEDIO | EXPERTO | Normales MEDIO media / mín. |
|---|---|---|---|---|
| **90 / 54 (antes)** | 0,0 | 27,8 | 64,7 | 100,0 / 96,7 |
| 90 / 60 | 0,0 | 26,7 | 63,9 | 99,9 / 96,7 |
| 90 / 66 | 0,0 | 25,2 | 61,4 | 99,9 / 89,3 |
| 90 / 72 | 0,0 | 23,0 | 59,0 | 99,8 / 87,3 |
| 90 / 80 | 0,0 | 20,1 | 54,7 | 99,6 / 75,3 |
| 100 / 60 | 0,0 | 26,4 | 63,6 | 99,9 / 96,7 |
| 100 / 63 | 0,0 | 25,7 | 62,2 | 99,9 / 89,3 |
| **115 / 60 (elegido)** | 0,0 | **26,2** | **63,5** | 99,9 / 95,7 |
| 130 / 60 | 0,0 | 25,5 | 63,3 | 99,9 / 95,7 |
| 130 / 63 | 0,0 | 24,0 | 61,9 | 99,8 / 89,3 |

Los comunes casi no mueven la aventura; el límite es el peor común (eregion_6_0, tres rivales con un espía), que cae por debajo del 93 % en cuanto el ataque pasa de 60. Se suben PV (90 → 115) y ataque (54 → 60): combates comunes algo más largos y duros sin romper el mínimo.

**`RivalScaling.BOSS`** (con los comunes ya en 115 / 60)

| PV / ataque | NOVATO | MEDIO | EXPERTO | Jefes MEDIO mín. / máx. |
|---|---|---|---|---|
| **150 / 74 (antes)** | 0,0 | 26,2 | 63,5 | 79,7 / 100 |
| 150 / 80 | 0,0 | 19,3 | 52,6 | 72,0 / 100 |
| 150 / 86 | 0,0 | 16,0 | 42,5 | 64,3 / 100 |
| 150 / 92 | 0,0 | 13,1 | 36,1 | 63,0 / 100 |
| 170 / 80 | 0,0 | 17,7 | 48,8 | 72,0 / 100 |
| **170 / 86 (elegido)** | 0,0 | **14,7** | **39,8** | 64,0 / 100 |
| 190 / 80 | 0,0 | 16,7 | 46,4 | 71,7 / 100 |
| 190 / 86 | 0,0 | 13,7 | 38,2 | 61,0 / 100 |
| 210 / 80 | 0,0 | 15,3 | 43,8 | 70,3 / 100 |
| 210 / 86 | 0,0 | 12,6 | 36,3 | 61,0 / 100 |

Con 170 / 86 la aventura ya cae cerca del objetivo, pero quedan jefes fuera de 65–92 % por los dos lados: caradhras (64 %) por abajo y chet (96 %), nimrodel (98 %) y sombrío (93 %) por arriba.

**`ComboTuning`: sin cambio.** `sweep cure=20,25,30,35 fist=21,24`: bajar la cura deja al MEDIO en 10–12 % pero hunde al peor común (87 % con cura 20) y no baja a chet (93–96 %) ni a nimrodel (95–98 %). Se mantiene 16 / 21 / 35.

**Jefes: offsets y cadencia**

`sweep offsets <zona> 2,...,8 levels=-1,0,1` (NOVATO/MEDIO/EXPERTO con el jugador a L−1, L y L+1):

| Zona | Cambio | L−1 | L | L+1 |
|---|---|---|---|---|
| chet | +4, cada 2 ticks (antes) | 0/96/99 | 0/96/99 | 0/96/100 |
| chet | +2..+8, cada 2 ticks | 0/96–97/99–100 | 0/96–97/98–100 | 0/96–99/98–100 |
| chet | **+3, cada tick (elegido)** | 0/92/85 | 0/89/83 | 0/89/85 |
| nimrodel | +2..+8, cada 2 ticks (antes +2) | 0/98/99–100 | 0/98/99–100 | 0/98/99–100 |
| nimrodel | **+2, cada tick (elegido)** | 0/91/92 | 0/91/92 | 0/91/92 |
| sombrío | +5 (antes) | 63/92/96 | 62/93/96 | 61/93/98 |
| sombrío | **+8 (elegido)** | 56/90/96 | 57/92/96 | 60/92/96 |
| caradhras | +7 (antes) | 0/74/80 | 0/64/82 | 0/64/82 |
| caradhras | **+5 (elegido)** | 0/64/82 | 0/72/86 | 0/72/85 |

chet y nimrodel están saturados con el offset: lanzan cada 2 ticks y el escudo y la cura del jugador deshacen su ráfaga sea cual sea su nivel (ya pasaba en «Power ×3 y escala ×100»). La única palanca que los mueve es la cadencia: el Lobo gris alfa y el Jefe de los arqueros orcos pasan de `everyNTicks` 2 a 1 (patrones sin cambio). Es un cambio de contenido fuera de la lista inicial (offsets), así que queda anotado como decisión a revisar. Se actualizan los JSON (`chet.json`, `nimrodel.json`, `caradhras.json`, `sombrio.json`), la tabla de jefes de la spec de contenido (§3) y `ContentRules.Bestiary`; `RealContentTest` y `ContentRulesTest` siguen en verde (offsets de jefe entre +2 y +8).

### Resultados

| Métrica | Antes (power ×3 y escala ×100) | Línea base con `321` | 1. comunes | 2. jefes | **Final (offsets y cadencia)** |
|---|---|---|---|---|---|
| NOVATO aventura completa | 0,0 % | 0,0 % | 0,0 % | 0,0 % | **0,0 %** |
| MEDIO aventura completa | 27,8 % | 27,8 % | 26,2 % | 14,7 % | **11,8 %** |
| EXPERTO aventura completa | 64,7 % | 64,7 % | 63,5 % | 39,8 % | **32,8 %** |
| Combates al tope (aventuras y encuentros) | 0 | 0 | 0 | 0 | **0** |
| MEDIO normales: media / mínimo | 100,0 / 96,7 | 100,0 / 96,7 | 99,9 / 95,7 | 99,9 / 95,7 | **99,9 / 95,7** (puente_4_7) |
| MEDIO normales < 93 % | 0 de 103 | 0 | 0 | 0 | **0 de 103** |
| EXPERTO normales: mínimo | 96,0 | | | | **98,3** (puente_4_7) |
| MEDIO jefes: media / mín. / máx. (sin bree) | 90,0 / 79,7 / 97,0 | | | 87,0 / 64,0 / 98 | **85,3 / 69,0 / 91,7** |
| MEDIO jefes fuera de 65–92 % (sin bree) | 6 | | | 4 | **0** |
| Ticks medios de un combate de jefe MEDIO | 22,2 | | | | 23,6 |

Antes, con el rango nuevo (65–92 %), quedaban por encima chet 96,3, puente 95,7, quebradas 93,7, durin 97,0, sombrío 96,7 y nimrodel 95,7.

**Jefes por perfil (nivel esperado, 300 combates): antes → final**

| Zona | Nivel | NOVATO | MEDIO | EXPERTO | Ticks MEDIO |
|---|---|---|---|---|---|
| bree | 3 | 100,0 → 84,3 | 100,0 → 100,0 | 100,0 → 100,0 | 15,7 → 16,5 |
| chet | 6 | 0,0 → 0,0 | 96,3 → 89,0 | 100,0 → 82,7 | 16,8 → 21,8 |
| moscagua | 9 | 0,0 → 0,0 | 85,7 → 89,3 | 86,3 → 87,0 | 21,2 → 21,6 |
| cima | 12 | 45,0 → 0,0 | 82,3 → 69,0 | 99,7 → 81,7 | 23,9 → 23,1 |
| puente | 15 | 59,3 → 0,0 | 95,7 → 85,3 | 100,0 → 99,7 | 21,4 → 21,8 |
| quebradas | 18 | 64,3 → 0,0 | 93,7 → 88,0 | 100,0 → 96,7 | 21,7 → 23,4 |
| bruinen | 21 | 0,0 → 0,0 | 87,3 → 88,7 | 90,3 → 91,0 | 24,8 → 27,6 |
| eregion | 24 | 14,0 → 10,7 | 90,3 → 84,7 | 98,0 → 95,0 | 29,2 → 28,1 |
| caradhras | 27 | 10,7 → 0,0 | 80,0 → 71,7 | 91,3 → 86,3 | 26,9 → 26,0 |
| durin | 30 | 13,7 → 20,7 | 97,0 → 91,3 | 99,7 → 100,0 | 27,5 → 28,6 |
| moria | 33 | 0,0 → 0,0 | 79,7 → 84,0 | 93,7 → 83,7 | 22,4 → 24,0 |
| sombrio | 36 | 59,3 → 57,3 | 96,7 → 91,7 | 100,0 → 96,3 | 20,0 → 20,6 |
| nimrodel | 39 | 0,0 → 0,0 | 95,7 → 91,3 | 100,0 → 91,7 | 17,7 → 24,3 |

Jefes MEDIO (final, sin bree): media 85,3 %, entre 69,0 % (cima) y 91,7 % (sombrío); bree al 100 % (tutorial). EXPERTO (sin bree): media 91,0 %, mínimo 81,7 % (cima). moscagua y moria suben un poco con el MEDIO pese a los jefes más fuertes: con 170 de PV el combate dura más y la cura y el escudo del jugador rinden más.

**% que llega a cada zona: antes → final**

| Zona | NOVATO | MEDIO | EXPERTO |
|---|---|---|---|
| bree | 100 → 100 | 100 → 100 | 100 → 100 |
| chet | 94,3 → 90,0 | 100 → 100 | 100 → 100 |
| moscagua | 0 → 0 | 96,8 → 86,7 | 99,3 → 89,7 |
| cima | 0 → 0 | 83,2 → 77,3 | 92,1 → 81,2 |
| puente | 0 → 0 | 68,7 → 54,6 | 89,0 → 69,1 |
| quebradas | 0 → 0 | 65,1 → 46,4 | 88,7 → 67,8 |
| bruinen | 0 → 0 | 60,4 → 38,9 | 88,5 → 65,3 |
| rivendel / eregion | 0 → 0 | 52,7 → 34,2 | 80,3 → 60,0 |
| caradhras | 0 → 0 | 47,2 → 28,5 | 77,5 → 56,4 |
| durin | 0 → 0 | 37,3 → 20,4 | 70,3 → 45,8 |
| moria | 0 → 0 | 35,7 → 18,1 | 70,3 → 45,6 |
| sombrio | 0 → 0 | 30,0 → 15,0 | 65,3 → 38,9 |
| nimrodel | 0 → 0 | 28,9 → 13,4 | 65,0 → 37,4 |
| lothlorien | 0 → 0 | 27,8 → **11,8** | 64,7 → **32,8** |

Dónde terminan las aventuras (final): NOVATO, en chet_7_0 (83,4 %), bree_7_0 (10,1 %) y chet_5_0 (6,6 %); MEDIO, en cima (22,7 %), chet (13,3 %), moscagua (9,5 %), caradhras (8,1 %), puente (7,9 %) y quebradas (7,5 %); EXPERTO, en cima (12,2 %), caradhras (10,6 %), chet (10,4 %) y moscagua (8,5 %).

### Valores finales

```kotlin
LevelStats.STAT_SCALE = 100   // sin cambio
LevelStats.MAX_POWER  = 300   // sin cambio
ComboTuning.DEFAULT   = ComboTuning(spearBasePct = 16, fistBasePct = 21, cureBasePct = 35)                                        // sin cambio
RivalScaling.DEFAULT  = RivalScaling(hpPct = 115, attackPct = 60, healPct = 100, defensePct = 100, startPowerPct = 92, regenPct = 100)  // antes PV 90 / ataque 54
RivalScaling.BOSS     = RivalScaling(hpPct = 170, attackPct = 86, healPct = 100, defensePct = 100, startPowerPct = 100, regenPct = 100) // antes PV 150 / ataque 74
// Combos: "321" Recuperar (RECOVER, tier 0, coste 0, SELF, PowerRestore(10))
```

- Jefes: chet **+3 y cada tick** (antes +4, cada 2 ticks), caradhras **+5** (antes +7), sombrío **+8** (antes +5) y nimrodel **cada tick** (antes cada 2 ticks; offset +2 sin cambio). El resto sin cambio: bree +2, moscagua +4, cima +3, puente +3, quebradas +5, bruinen +6, eregion +2, durin +4 y moria +7.
- Aventura completa: NOVATO 0,0 % (sin objetivo; lo para chet), MEDIO 11,8 % (10–15 %) y EXPERTO 32,8 % (30–40 %).
- Normales MEDIO: media 99,9 %, mínimo 95,7 % (puente_4_7); ninguno por debajo del 93 %.
- Jefes MEDIO: entre 69,0 % (cima) y 91,7 % (sombrío), salvo bree al 100 % (tutorial).
- Combates al tope de 600 ticks: 0.
- Las huellas de `HpRegressionTest` se regrabaron desde el motor nuevo (el caso de derrota vuelve a rivales de nivel 9: a nivel 12 la derrota llegaba en 7 ticks).
- `final-encounters.csv` es el de este estado; `final-adventures.csv` se rehízo con la regla de «Muerte sin reinicio» (sección siguiente).

### Pendiente para pruebas reales (dificultad base subida)

1. **chet con el EXPERTO por debajo del MEDIO** (82,7 % frente a 89,0 %): con el lobo lanzando cada tick, el EXPERTO pierde más que el MEDIO (sin investigar a fondo; probablemente por cómo reparte el power entre Furia, Golpe directo y escudo). Si el jugador real lo nota, el sitio es el patrón del lobo o la política del EXPERTO.
2. **Cadencia de chet y nimrodel** (cada tick en lugar de cada 2): es un cambio de contenido, no solo de números; confirmar que el jugador real lo ve justo.
3. **cima (69 %) y caradhras (72 %)** son ahora los muros del MEDIO; dentro del rango, pero en el borde bajo.
4. **`321` casi no se usa** en la simulación (power 300); su valor real es para jugadores que gasten el power más deprisa que la política simulada.
5. **NOVATO**: 0 %, como antes; ahora el 90 % pasa Bree y lo para chet.

## Muerte sin reinicio (2026-10-13)

Cambio de regla (spec de historia, §6): perder un combate ya no reinicia la partida en Bree; el Warden vuelve al nodo anterior (ya superado), conserva nivel, victorias y vuelta y suma una muerte. El simulador hace lo mismo: tras una derrota la política vuelve a elegir (al azar y uniforme, así que puede volver al mismo combate) y la aventura sigue. Con **200 muertes** la aventura se da por **abandonada**. Sin re-balance: mismos números, mismas semillas, 2000 aventuras por perfil; las tablas de encuentros no cambian (`final-encounters.csv` es idéntico).

| Perfil | Completan | Sin muertes | Muertes por aventura | Abandonadas (200 muertes) |
|---|---|---|---|---|
| NOVATO | 0,0 % | 0,0 % | 200,00 | 100,0 % |
| MEDIO | 100,0 % | 11,8 % | 2,46 | 0,0 % |
| EXPERTO | 100,0 % | 32,8 % | 1,22 | 0,0 % |

- **«Sin muertes» es la métrica antigua de «aventura completa»** (con las mismas semillas, las aventuras sin muertes son exactamente las que antes terminaban): 11,8 % y 32,8 %, como en «Dificultad base subida». El barrido (`sweep`) usa ahora esta métrica en las columnas `NOV% MED% EXP%`, así que sus objetivos (MEDIO 10–15 %, EXPERTO 30–40 %) siguen valiendo.
- **NOVATO**: se atasca en el jefe de chet (`chet_7_0`, 199,73 muertes por aventura): morir no da nivel y a nivel 6 el NOVATO no gana nunca a ese jefe (0,0 % en la tabla de encuentros), así que repite hasta abandonar.
- Combates al tope de 600 ticks: 0.

**Muertes por zona (media por aventura)**

| Zona | NOVATO | MEDIO | EXPERTO |
|---|---|---|---|
| bree | 0,12 | 0,00 | 0,00 |
| chet | 199,88 | 0,16 | 0,12 |
| moscagua | — | 0,13 | 0,10 |
| cima | — | 0,42 | 0,19 |
| puente | — | 0,17 | 0,02 |
| quebradas | — | 0,20 | 0,05 |
| bruinen | — | 0,13 | 0,09 |
| rivendel | — | 0,00 | 0,00 |
| eregion | — | 0,20 | 0,06 |
| caradhras | — | 0,44 | 0,25 |
| durin | — | 0,13 | 0,01 |
| moria | — | 0,24 | 0,19 |
| sombrio | — | 0,13 | 0,03 |
| nimrodel | — | 0,11 | 0,11 |

Casi todas las muertes del MEDIO y el EXPERTO son en jefes (`<zona>_7_0`); los muros siguen siendo caradhras (0,44 MEDIO) y cima (0,42).

`final-adventures.csv` tiene ahora las columnas `skill,zone,reachedPct,entryLevel,deathsPerAdventure` (antes solo `skill,zone,reachedPct`). El informe de `./gradlew :sim:run` añade «sin muertes», «muertes por aventura», «abandonadas», la tabla de muertes por zona y los nodos con más muertes (media por aventura).

### Pendiente para pruebas reales (muerte sin reinicio)

1. Con esta regla el MEDIO y el EXPERTO terminan siempre; la dificultad se mide ahora en muertes (2,5 y 1,2 por aventura). Decidir con jugadores reales si eso basta o si morir debe costar algo más.
2. Un jugador que no puede con un jefe se queda atascado (el caso del NOVATO en chet): no hay forma de subir de nivel sin ganar. Valorar si hace falta una salida (combates repetibles, ayuda tras N muertes…).

## Pociones, cadenas de daño y Daze sin bucle (2026-10-13)

Informe del usuario tras una partida real (Eregion, nodo 3: dos «Crebain» de nivel 20 contra el Warden de nivel 18, tick 259): los rivales se quedan sin power y dejan de atacar, solo lanzan Aturdir y no hacen daño, y el jugador pasa el combate aturdido con varias insignias AT acumuladas; no se ve bajar la vida ni números rojos.

### Diagnóstico (traza del motor, `EregionCrebainTraceTest`)

Traza tick a tick de `eregion_3_0` (dos Crebain L20 con `31, 313, 3131` cada 2 ticks, Warden L18 pasivo, semilla 7), escrita en `engine/build/reports/trace/eregion_3_0.txt`:

| | Antes | Después |
|---|---|---|
| `EffectResolved` sobre el jugador (260 ticks) | 0 | DOT de lanza desde el tick 2, cada tick (−460 `SP1`, −920 `SP2`, −1380 `SP3` por Crebain) |
| Vida del jugador | 44 000 / 44 000 todo el combate | baja cada tick (pasivo, muere en el tick 12) |
| Ticks aturdido | 205 de 260 (79 %) | 0 en esos 12 ticks |
| Lanzamientos por Crebain | 87 en 260 ticks (le tocaban 130) | uno cada 2 ticks |
| Power de los Crebain en el tick 259 | 31 / 300 (como en la captura) | — |

- **No había fallo de la interfaz**: la tira de números pinta cada `EffectResolved`, y con ese patrón no había ninguno. El patrón de los Crebain (y el del Espía dunlendino) solo tenía Aturdir: cero daño por diseño. El test nuevo de ViewModel `rivalDamageShowsRedInThePlayerStrip` (mismo nodo, Warden L18) comprueba que el daño de los rivales llega como `EffectResolved` negativos y que sale como entradas rojas en la tira del jugador.
- **Bucle de Daze**: mientras el jugador estaba aturdido los Aturdir seguían añadiendo `DAZE_CHANCE` (hasta el 40 %), así que al acabar el Daze la primera tirada lo volvía a aturdir casi siempre.
- **Sin power**: con cadencia 2 y coste medio 18 frente a 10 de regeneración, el power baja 8 por ciclo; al no poder pagar, `PatternBrain` espera y pierde turnos.

### Cambios

1. **Poción de los rivales** (motor, TDD: `TickResolverTest.rivalDrinksPotionWhenNextComboIsUnaffordable`, `potionIsClampedToMaxPower`, `noPotionWhenAffordableOffCadenceOrDazed`, `playerNeverDrinksPotions`, `PatternBrainTest.nextCastIgnoresPowerButNotCadence`): en la fase de IA, si a un rival vivo, sin combo en cola y no aturdido le toca lanzar y no puede pagar el combo que le toca de su patrón, bebe una poción: +50 % de su power máximo (150 con 300; sin pasar del máximo; usos ilimitados) y se emite `CombatEvent.PotionUsed(fighter, amount)`. `RivalBrain.nextCast` dice qué combo toca este tick sin mirar el power. App: «+150 PO» en azul (color del power) en la tira de ese rival (`CombatTextTone.POWER`); sin sonido.
2. **Cadenas de daño para todos** (contenido, `ContentRulesTest.everyRivalIsMostlyDamageWithADamageChain`): todo patrón, común o jefe, tiene al menos la mitad de sus combos de daño (Lanza, Lanza cruzada, Puño en área, Golpe directo, Robo de vida) y al menos una cadena de daño real. Los 13 jefes y 9 de los 12 comunes ya cumplían; se reescriben tres:

| Común | Antes | Después |
|---|---|---|
| Crebain | `31, 313, 3131` cada 2 | `12, 121, 1212, 31` cada 2 (picotazos en cadena de Lanza y un Aturdir) |
| Espía dunlendino | `31, 313, 22` cada tick | `13, 131, 31` cada 2 (Lanza cruzada en cadena y un Aturdir) |
| Moscardones de Moscagua | `32, 13` cada 2 | `32, 323, 13` cada 2 (enjambre en área encadenado y un picotazo) |

3. **Daze sin bucle** (motor, `ComboApplierTest.dazeChanceNotAppliedToDazedTarget`): Aturdir no aplica `DAZE_CHANCE` a un objetivo que ya tiene `DAZED` (activo o pendiente).

### Línea base: cada cambio por separado (mismas semillas, 300 combates por encuentro, 2000 aventuras por perfil)

| Estado | MEDIO sin muertes | EXPERTO sin muertes | Muertes MEDIO / EXPERTO | Normales MEDIO media / mín. | Jefes MEDIO mín. / máx. (sin bree) |
|---|---|---|---|---|---|
| Antes (muerte sin reinicio) | 11,8 % | 32,8 % | 2,46 / 1,22 | 99,9 / 95,7 (puente_4_7) | 69,0 cima / 91,7 sombrío |
| Solo poción | 11,6 % | 32,7 % | 2,49 / 1,22 | 99,9 / 95,7 | 69,0 / 91,7 |
| Solo Daze sin bucle | 14,0 % | 34,6 % | 2,28 / 1,17 | 100,0 / 97,3 | 69,7 / **94,3 durin** |
| Solo patrones nuevos | 12,9 % | 34,8 % | 2,34 / 1,16 | 100,0 / 100,0 | 69,0 / 91,7 |
| Los tres juntos (sin re-balance) | 14,2 % | 35,5 % | 2,26 / 1,13 | 100,0 / 100,0 | 69,7 / **94,3 durin** |

La poción casi no mueve la simulación (la política MEDIO/EXPERTO mata antes de que los rivales se queden sin power); el bucle de Daze era lo que más pesaba y quitarlo facilita: el Guardián del Agua (durin), que aturde con `31, 313, 3131`, se sale por arriba. Los comunes nuevos hacen daño de verdad (en `eregion_3_0` el MEDIO acaba con 25 688 de vida de 52 000; antes, con 52 000), pero el MEDIO les gana siempre: el peligro de los comunes antiguos era el Daze del espía (`puente_4_7` y `eregion_6_0` eran los peores normales).

### Re-balance (un tipo de ajuste cada vez)

**`RivalScaling.DEFAULT` (comunes)**, con los jefes en 170 / 86:

| PV / ataque | MEDIO | EXPERTO | Normales MEDIO media / mín. | Jefes MEDIO mín. / máx. |
|---|---|---|---|---|
| 115 / 60 (antes) | 14,2 | 35,5 | 100,0 / 100,0 | 69,7 / 94,3 |
| **115 / 66 (elegido)** | **12,8** | **35,3** | 100,0 / 100,0 | 69,7 / 94,3 |
| 115 / 72 | 11,5 | 35,5 | 100,0 / 100,0 | 69,7 / 94,3 |
| 115 / 80 | 9,9 | 33,7 | 100,0 / 100,0 | 69,7 / 94,3 |
| 130 / 60 | 13,8 | 35,5 | 100,0 / 100,0 | 69,7 / 94,3 |
| 130 / 66 | 12,1 | 35,3 | 100,0 / 100,0 | 69,7 / 94,3 |
| 130 / 72 | 10,9 | 35,3 | 100,0 / 100,0 | 69,7 / 94,3 |
| 130 / 80 | 8,6 | 32,5 | 100,0 / 100,0 | 69,7 / 94,3 |

Los comunes ya no son el límite (todos al 100 % con el MEDIO); su ataque pesa sobre todo a través de las escoltas de los jefes. Se probó primero 130 / 66, pero con los jefes en 180 / 86 dejaba **2 aventuras MEDIO de 2000 abandonadas** (200 muertes): jugadores que llegan con varios niveles menos de lo esperado a `caradhras_3_2` / `caradhras_3_3` (tres Crebain) o a `quebradas_5_0` / `quebradas_5_1` y pierden siempre (los combates sin Aturdir son casi deterministas: `bruinen_5_2`, tres huargos, pasa del 100 % al 0 % con dos niveles menos). Con 115 de PV no queda ninguna abandonada, así que solo sube el ataque (60 → 66).

**`RivalScaling.BOSS`**, con los comunes ya en 115 / 66 (`sweep boss`):

| PV / ataque | MEDIO | EXPERTO | Jefes MEDIO mín. / máx. (sin bree) | durin MEDIO |
|---|---|---|---|---|
| 170 / 82 | 15,7 | 41,0 | 74,0 / 97 | 97 |
| 170 / 86 (antes) | 12,8 | 35,3 | 69,7 / 94 | 94 |
| 170 / 90 | 10,7 | 32,0 | 63,3 / 92 | 82 |
| 180 / 82 | 15,2 | 40,1 | 74,0 / 96 | 96 |
| **180 / 86 (elegido)** | **12,3** | **34,4** | **69,7 / 91** | **90** |
| 180 / 90 | 10,3 | 31,2 | 63,3 / 92 | 82 |
| 190 / 82 | 14,3 | 37,9 | 74,0 / 96 | 96 |
| 190 / 86 | 11,7 | 33,5 | 69,7 / 91 | 90 |
| 190 / 90 | 9,9 | 30,6 | 62,7 / 91 | 82 |

Con 180 / 86 todos los jefes MEDIO quedan entre el 65 y el 92 % (durin baja del 94 al 90) sin tocar patrones ni offsets de jefe; con ataque 90, cima y caradhras caen por debajo del 65 %. **Patrones y offsets de jefe: sin cambio** (no hizo falta el tercer tipo de ajuste).

### Resultados

| Métrica | Antes | **Final** |
|---|---|---|
| NOVATO: completan / sin muertes | 0,0 / 0,0 % (atascado en chet) | 0,0 / 0,0 % (atascado en chet) |
| MEDIO: sin muertes (objetivo 10–15 %) | 11,8 % | **12,3 %** |
| EXPERTO: sin muertes (objetivo 30–40 %) | 32,8 % | **34,4 %** |
| Muertes por aventura MEDIO / EXPERTO | 2,46 / 1,22 | **2,43 / 1,17** |
| Aventuras abandonadas (200 muertes) MEDIO / EXPERTO | 0 / 0 | **0 / 0** |
| Combates al tope de 600 ticks | 0 | **0** |
| MEDIO normales: media / mínimo | 99,9 / 95,7 (puente_4_7) | **100,0 / 100,0** |
| EXPERTO normales: mínimo | 98,3 | 100,0 |
| MEDIO jefes: media / mín. / máx. (sin bree) | 85,3 / 69,0 / 91,7 | **85,2 / 69,7 (cima) / 91,3 (nimrodel)** |
| MEDIO jefes fuera de 65–92 % (sin bree) | 0 | **0** |

**Jefes por perfil (nivel esperado, 300 combates): antes → final**

| Zona | Nivel | NOVATO | MEDIO | EXPERTO | Ticks MEDIO |
|---|---|---|---|---|---|
| bree | 3 | 84,3 → 75,3 | 100,0 → 100,0 | 100,0 → 100,0 | 16,5 → 16,7 |
| chet | 6 | 0,0 → 0,0 | 89,0 → 89,0 | 82,7 → 82,7 | 21,8 → 21,7 |
| moscagua | 9 | 0,0 → 0,0 | 89,3 → 90,7 | 87,0 → 87,0 | 21,6 → 22,4 |
| cima | 12 | 0,0 → 0,0 | 69,0 → 69,7 | 81,7 → 81,7 | 23,1 → 23,2 |
| puente | 15 | 0,0 → 0,0 | 85,3 → 86,0 | 99,7 → 99,7 | 21,8 → 22,7 |
| quebradas | 18 | 0,0 → 0,0 | 88,0 → 88,0 | 96,7 → 97,0 | 23,4 → 24,1 |
| bruinen | 21 | 0,0 → 0,0 | 88,7 → 87,3 | 91,0 → 91,0 | 27,6 → 27,9 |
| eregion | 24 | 10,7 → 0,0 | 84,7 → 85,3 | 95,0 → 100,0 | 28,1 → 20,1 |
| caradhras | 27 | 0,0 → 0,0 | 71,7 → 72,7 | 86,3 → 86,3 | 26,0 → 26,0 |
| durin | 30 | 20,7 → 19,3 | 91,3 → 90,3 | 100,0 → 100,0 | 28,6 → 29,0 |
| moria | 33 | 0,0 → 0,0 | 84,0 → 83,3 | 83,7 → 83,7 | 24,0 → 24,5 |
| sombrio | 36 | 57,3 → 39,7 | 91,7 → 89,0 | 96,3 → 94,7 | 20,6 → 20,8 |
| nimrodel | 39 | 0,0 → 0,0 | 91,3 → 91,3 | 91,7 → 91,7 | 24,3 → 25,9 |

**Muertes por zona (media por aventura): antes → final**

| Zona | MEDIO | EXPERTO |
|---|---|---|
| bree | 0,00 → 0,00 | 0,00 → 0,00 |
| chet | 0,16 → 0,15 | 0,12 → 0,12 |
| moscagua | 0,13 → 0,13 | 0,10 → 0,10 |
| cima | 0,42 → 0,41 | 0,19 → 0,19 |
| puente | 0,17 → 0,17 | 0,02 → 0,02 |
| quebradas | 0,20 → 0,19 | 0,05 → 0,05 |
| bruinen | 0,13 → 0,13 | 0,09 → 0,09 |
| eregion | 0,20 → 0,20 | 0,06 → 0,01 |
| caradhras | 0,44 → 0,44 | 0,25 → 0,26 |
| durin | 0,13 → 0,13 | 0,01 → 0,01 |
| moria | 0,24 → 0,23 | 0,19 → 0,18 |
| sombrio | 0,13 → 0,14 | 0,03 → 0,04 |
| nimrodel | 0,11 → 0,11 | 0,11 → 0,10 |

Los comunes con daño se notan en el NOVATO (los encuentros de Crebain y espías pasan del 100 % a entre el 0 y el 79 %), no en el MEDIO ni en el EXPERTO, cuyas muertes siguen siendo casi todas en jefes (caradhras 0,44 y cima 0,41 con el MEDIO).

### Valores finales

```kotlin
LevelStats.STAT_SCALE = 100   // sin cambio
LevelStats.MAX_POWER  = 300   // sin cambio
ComboTuning.DEFAULT   = ComboTuning(spearBasePct = 16, fistBasePct = 21, cureBasePct = 35)                                        // sin cambio
RivalScaling.DEFAULT  = RivalScaling(hpPct = 115, attackPct = 66, healPct = 100, defensePct = 100, startPowerPct = 92, regenPct = 100)  // antes ataque 60
RivalScaling.BOSS     = RivalScaling(hpPct = 180, attackPct = 86, healPct = 100, defensePct = 100, startPowerPct = 100, regenPct = 100) // antes PV 170
TickResolver.POTION_PCT = 50  // poción de los rivales: +50 % del power máximo si no pueden pagar el combo que les toca
```

- Comunes: Crebain `12, 121, 1212, 31` cada 2; Espía dunlendino `13, 131, 31` cada 2; Moscardones `32, 323, 13` cada 2 (JSON, `ContentRules.Bestiary` y §3 de la spec de contenido). El resto de comunes y los 13 jefes (patrones, cadencia y offsets), sin cambio.
- Aventura sin muertes: MEDIO 12,3 %, EXPERTO 34,4 %, NOVATO 0 %. Muertes por aventura: MEDIO 2,43, EXPERTO 1,17 (NOVATO 200: abandona en chet).
- Normales MEDIO: todos al 100 %. Jefes MEDIO: entre el 69,7 % (cima) y el 91,3 % (nimrodel), con bree al 100 % (tutorial). Combates al tope: 0. Aventuras abandonadas MEDIO/EXPERTO: 0.
- Las huellas de `HpRegressionTest` se regrabaron desde el motor nuevo; `final-encounters.csv` y `final-adventures.csv` son los de este estado.

### Pendiente para pruebas reales (pociones, cadenas de daño y Daze sin bucle)

1. **Comunes al 100 % con el MEDIO**: sin el Daze en bucle del espía, ningún común amenaza a la política MEDIO; la dificultad sigue estando en los jefes. Si el jugador real ve los comunes demasiado fáciles, la palanca es el ataque de `RivalScaling.DEFAULT` (con 80 el MEDIO baja al 9,9 %), sin subir sus PV (ver los atascos de 130 / 66).
2. **Poción**: en la simulación casi no se usa (los rivales mueren antes); se nota en combates largos como el de la captura. Confirmar que el «+150 PO» se entiende.
3. **Combates casi deterministas**: sin Aturdir de por medio, un combate común se gana o se pierde siempre igual al mismo nivel; un jugador muy por debajo del nivel esperado podría atascarse en un nodo común (en la simulación solo pasó con 130 de PV en los comunes).
4. Siguen abiertos los pendientes de «Dificultad base subida» (chet con el EXPERTO por debajo del MEDIO, cima y caradhras en el borde bajo, NOVATO al 0 %).

## Más enemigos y dificultad + (2026-10-13)

Petición del usuario tras jugar la aventura completa: «me parece algo fácil, añade algo de dificultad y más enemigos, solo he visto como máximo 3, podrían haber 4». Objetivos nuevos (más duros que los de «Dificultad base subida»):

| Métrica | Antes (objetivo) | Ahora (objetivo) |
|---|---|---|
| MEDIO: aventuras sin muertes | 10–15 % | **5–9 %** |
| EXPERTO: aventuras sin muertes | 30–40 % | **20–28 %** |
| MEDIO: muertes por aventura | (2,43) | **≈ 3,5–5** |
| MEDIO: cada común | ≥ 93 % | **≥ 88 %** (ninguno < 80 %) |
| MEDIO: cada jefe (salvo bree, tutorial) | 65–92 % | **55–85 %** (ninguno < 45 %) |
| Combates al tope / aventuras abandonadas | 0 | 0 |

### 1. Contenido: grupos de 4 y grupos más grandes

La regla de §5 de la spec de contenido ya admitía 4 rivales (offset −5) pero ningún nodo los usaba. Test nuevo (rojo primero): `RealContentTest.groupSizesPerZone` exige al menos un encuentro de 4 rivales en cada zona desde Moscagua (la 3.ª) salvo Rivendel, al menos dos en las zonas tardías (Caradhras, Durin, Moria, Sombrío y Nimrodel) y que Bree no pase de 2 rivales por grupo. Cambian 26 nodos de niveles 2–6 (texto, `imagePrompt`, rivales y offsets según §5; sin tocar Bree ni Chet) y los prompts de esos nodos en `docs/content/prompts-imagenes.md`. Los nodos con encuentro no cambian, así que la curva de niveles esperada es la misma.

| Rivales por encuentro (niveles 2–6) | Antes | Después |
|---|---|---|
| 1 | 43 | 43 |
| 2 | 50 | 32 |
| 3 | 10 | 12 |
| 4 | 0 | **16** |

| Zona | Grupos de 4 | Grupos de 3 nuevos (antes de 2) |
|---|---|---|
| moscagua | `4_1` (cuatro enjambres) | `4_4` (espía + dos enjambres) |
| cima | `3_2` (cuatro lobos) | `3_3` (tres espías) |
| puente | `4_4` (cuatro huargos) | `4_0` (tres huargos en la torre) |
| quebradas | `5_2` (cuatro orcos) | `3_1` (tres orcos) |
| bruinen | `5_2` (cuatro huargos) | `3_2` (tres huargos) |
| eregion | `6_0` (espía + tres lobos de Saruman) | `5_2` (tres crebain) |
| caradhras | `3_3` (cuatro crebain), `6_0` (cuatro lobos de Saruman) | — |
| durin | `4_2` (cuatro trasgos), `4_4` (cuatro lobos de Saruman) | `3_2` (tres lobos de Saruman) |
| moria | `4_1` (orco capitán + tres trasgos), `4_2` (cuatro trasgos) | `5_1` (orco capitán + dos trasgos) |
| sombrio | `3_0` (cuatro trasgos), `6_0` (dos orcos + dos arqueros) | `3_2` (orco + dos trasgos) |
| nimrodel | `2_1` (cuatro orcos), `5_0` (cuatro arqueros) | `3_1` (orco + dos arqueros) |

Efecto del contenido solo (sin re-balance, mismas semillas): MEDIO 12,3 → 11,5 % sin muertes y 2,43 → 2,71 muertes por aventura; EXPERTO 34,4 → 34,2 %. Los comunes siguen siendo casi deterministas al nivel esperado (o se ganan siempre o no se ganan nunca), así que los grupos de 4 pesan sobre todo a los jugadores que llegan con menos nivel y por la vida que se pierde (ver «Grupos por tamaño»).

### 2. Re-balance (mismas semillas; 300 combates por encuentro y 2000 aventuras por perfil salvo donde se indica)

El barrido (`sweep`) imprime ahora los cinco comunes más débiles con el MEDIO («peores normales MEDIO») y el informe de `:sim:run` dice dónde se atascan las aventuras abandonadas («abandonadas, atascadas en: …»).

**`RivalScaling.DEFAULT` (comunes)**, con el contenido nuevo y los jefes de antes (`sweep attack=… fights=100 runs=1000`):

| Ataque | MEDIO | EXPERTO | Peor común MEDIO |
|---|---|---|---|
| 66 (antes) | 10,0 | 36,0 | 100 |
| 70 | 8,8 | 34,6 | 100 |
| 74 | 6,5 | 34,3 | **0** (nimrodel_5_0, cuatro arqueros) |
| 78 | 4,6 | 32,8 | **0** (caradhras_3_3, durin_4_4, nimrodel_5_0) |
| 90 | 3,2 | 27,4 | **0** (cinco nodos) |

Los grupos de 4 son un precipicio: con ataque 74 o más, alguno pasa del 100 al 0 % al nivel esperado (imposible). Subir PV (130) baja la aventura pero acerca el precipicio (125 / 72 ya deja bruinen_5_2 al 0 %). Los comunes no bastan: el EXPERTO apenas se mueve.

**`RivalScaling.BOSS` global: saturado.** Con los comunes en 70, `sweep boss attack=86,90,94,98 hp=180,200`: el MEDIO solo baja de 8,8 a 6,9 % y los jefes MEDIO apenas se mueven (chet, moscagua, durin y nimrodel siguen en 88–93 %); con ataque 94 la Sombra de Durin cae al 31 %. Lo mismo con el offset de cada jefe (`sweep offsets <zona>`): moscagua da 88–90 % con cualquier offset de +3 a +8, bruinen 87–89 % y quebradas 86–90 %. El MEDIO pierde contra un jefe cuando le sale el Daze en mal momento, no por los números del jefe, así que las palancas útiles son **escoltas** (más enemigos en el combate de jefe) y **Aturdir** en el patrón.

**Jefes: escoltas, patrones y offsets** (`sweep offsets <zona> … levels=-1,0,1`, NOVATO/MEDIO/EXPERTO con el jugador a L−1, L y L+1; antes → elegido):

| Zona | Cambio | L−1 | L | L+1 |
|---|---|---|---|---|
| chet | +3, `…, 31, 33`, 1 lobo joven (antes) | — | 0/89/83 | — |
| chet | **+4, `…, 31, 313` (Brío → Aturdir II), 2 lobos jóvenes** | 0/77/79 | 0/80/79 | 0/86/79 |
| moscagua | +4, 1 guardiana (antes; con 2 guardianas, igual) | — | 0/90/90 | — |
| moscagua | **+4, guardiana + espía dunlendino** | 0/81/82 | 0/80/82 | 0/79/77 |
| cima | +3 (antes) | 0/63/79 | 0/70/82 | 0/76/96 |
| cima | **+2** (se alivia: era el muro del MEDIO) | 0/70/82 | 0/76/96 | 0/73/99 |
| puente | +3, sin escolta (antes) | — | 0/86/100 | — |
| puente | **+8, sin escolta** (con 1–2 huargos de escolta el EXPERTO bajaba a 80–82) | 0/75/84 | 0/78/86 | 0/82/87 |
| quebradas | +5, sin escolta (antes); +8 sin escolta: 0/86/90 | — | 0/88/97 | — |
| quebradas | **+5, 1 huargo de escolta** | 0/80/81 | 0/80/82 | 0/80/83 |
| bruinen | +6, `32, 323, 3232, 3123, 31232, 31` (antes) | — | 0/87/94 | — |
| bruinen | **+6, `32, 323, 3232, 31, 3123, 31232, 31`** (dos Aturdir) | 0/76/88 | 0/77/85 | 0/76/85 |
| eregion | +2 (antes) | 0/82/100 | 0/85/100 | 0/91/100 |
| eregion | **+4** | 0/71/100 | 0/77/100 | 0/88/100 |
| caradhras | +5 (antes) | 0/64/82 | 0/73/86 | 0/73/84 |
| caradhras | **+3** (se alivia: era el otro muro) | 0/73/82 | 0/80/89 | 0/80/91 |
| durin | +4 (antes) | — | 20/91/100 | — |
| durin | **+7** (con ataque de jefe 82) | 2/81/100 | 11/82/100 | 14/90/100 |
| moria | +7, sin escolta (antes); con 2 trasgos: 0/87/88; con orco capitán + trasgo: 0/33/88 | — | 0/83/84 | — |
| moria | **+7, trol de las cavernas + trasgo** | 0/79/83 | 0/79/83 | 0/82/83 |
| sombrío | +8, 1 arquero (antes) | 16/83/95 | 37/89/95 | 44/89/95 |
| sombrío | **+8, arquero + trasgo** (con orco + arquero, o dos arqueros: 72–75 / 71–78) | 0/78/78 | 0/79/81 | 0/79/81 |
| nimrodel | +2, `32, 323, 3232, 2131, 31, 21` (antes) | 0/91/92 | 0/91/92 | 0/91/92 |
| nimrodel | **+2, `32, 323, 3232, 31, 2131, 31`** (dos Aturdir, sin Cura) | 0/85/88 | 0/85/88 | 0/85/88 |

Las escoltas castigan más al EXPERTO que al MEDIO (con dos o más rivales juega la cadena de área en lugar de centrarse en el jefe): por eso puente queda sin escolta (sube el offset, que solo pesa sobre el MEDIO) y cima y caradhras, los muros de antes, se alivian.

**`RivalScaling.BOSS` y `DEFAULT` con los jefes nuevos** (`sweep boss` / `sweep`):

| Comunes / jefes (PV / ataque) | MEDIO | EXPERTO | Muertes MEDIO | Abandonadas MEDIO | Jefes MEDIO mín. / máx. (sin bree) |
|---|---|---|---|---|---|
| 115 / 70 · 180 / 86, primer reparto de escoltas (dos en puente, quebradas y sombrío) | 4,3 | 14,3 | — | — | 69,7 / 85 |
| 115 / 70 · 180 / 82 (durin +7, puente y quebradas +8 sin escolta) | 5,6 | 23,4 | 4,30 | **0,1 %** (caradhras_3_3, sombrio_3_3) | 72,7 / 85,7 (quebradas) |
| 115 / 66 · 180 / 82 | 6,8 | 24,9 | 3,50 | 0 | 72,7 / 85,7 |
| 115 / 68 · 180 / 82 | 6,5 | 24,2 | 3,67 | 0 | 72,7 / 85,7 (quebradas), 85,3 (eregion) |
| **115 / 68 · 180 / 82, eregion +4, quebradas +5 con huargo (elegido)** | **6,1** | **21,2** | **3,84** | **0** | **72,7 / 84,7** |
| 115 / 68 · 180 / 80 | 6,7 | 22,6 | — | — | durin 90 |
| 115 / 68 · 180 / 78 | 7,5 | 23,4 | — | — | durin 93, chet 86 |

Con ataque 70 en los comunes quedaban 2 aventuras MEDIO de 2000 abandonadas (jugadores con varios niveles menos, atascados en un grupo de cuatro Crebain o en `sombrio_3_3`); con 68, ninguna. Con el ataque de jefe en 80 o menos, durin se va por encima del 85 %.

### Resultados

| Métrica | Antes | **Final** | Objetivo |
|---|---|---|---|
| MEDIO: sin muertes | 12,3 % | **6,1 %** | 5–9 % |
| EXPERTO: sin muertes | 34,4 % | **21,2 %** | 20–28 % |
| Muertes por aventura MEDIO / EXPERTO | 2,43 / 1,17 | **3,84 / 1,85** | MEDIO ≈ 3,5–5 |
| Muertes en combates comunes por aventura MEDIO | ≈ 0,02 | ≈ 0,41 | — |
| NOVATO | 0 % (atascado en chet) | 0 % (atascado en chet) | sin objetivo |
| Aventuras abandonadas MEDIO / EXPERTO | 0 / 0 | **0 / 0** | 0 |
| Combates al tope de 600 ticks | 0 | **0** | 0 |
| MEDIO normales: media / mínimo | 100,0 / 100,0 | **100,0 / 100,0** | ≥ 88 % |
| MEDIO jefes: media / mín. / máx. (sin bree) | 85,2 / 69,7 (cima) / 91,3 (nimrodel) | **79,6 / 72,7 (cima) / 84,7 (nimrodel)** | 55–85 % |
| EXPERTO jefes: media / mín. (sin bree) | 91,3 / 81,7 (cima) | **87,9 / 77,0 (moscagua)** | — |

**Grupos por tamaño (comunes, nivel esperado, 300 combates): vida que le queda al jugador (% de su máximo)**

| Rivales | Encuentros | MEDIO: vida media / mínima | EXPERTO: vida media / mínima | NOVATO: % victoria |
|---|---|---|---|---|
| 1 | 43 → 43 | 60,2 → 59,0 / 31,8 → 28,7 | 65,5 → 64,4 | 100,0 → 95,3 |
| 2 | 50 → 32 | 56,7 → 56,9 / 18,8 → 16,2 | 62,7 → 62,2 | 44,7 → 43,5 |
| 3 | 10 → 12 | 52,7 → 46,1 / 24,8 → 19,2 | 64,1 → 57,6 | 10,0 → 7,6 |
| 4 | — → 16 | — → 33,3 / 8,5 (nimrodel_5_0) | — → 43,0 | — → 12,5 |

Al nivel esperado el MEDIO y el EXPERTO ganan todos los comunes, pero un grupo de cuatro les deja con un tercio de la vida (los cuatro arqueros de Nimrodel, con el 8,5 %); con un par de niveles menos se pierden, que es de donde salen las ≈ 0,4 muertes por aventura en comunes (bruinen_5_2, nimrodel_2_1, nimrodel_5_0 y caradhras_3_3 son los que más matan).

**Jefes por perfil (nivel esperado, 300 combates): antes → final**

| Zona | Nivel | NOVATO | MEDIO | EXPERTO | Ticks MEDIO |
|---|---|---|---|---|---|
| bree | 3 | 75,3 → 75,3 | 100,0 → 100,0 | 100,0 → 100,0 | 16,7 → 16,7 |
| chet | 6 | 0,0 → 0,0 | 89,0 → 83,3 | 82,7 → 79,3 | 21,7 → 23,7 |
| moscagua | 9 | 0,0 → 0,0 | 90,7 → 80,0 | 87,0 → 77,0 | 22,4 → 24,2 |
| cima | 12 | 0,0 → 0,0 | 69,7 → 72,7 | 81,7 → 99,3 | 23,2 → 23,3 |
| puente | 15 | 0,0 → 0,0 | 86,0 → 81,7 | 99,7 → 86,7 | 22,7 → 26,1 |
| quebradas | 18 | 0,0 → 0,0 | 88,0 → 80,3 | 97,0 → 82,3 | 24,1 → 22,2 |
| bruinen | 21 | 0,0 → 0,0 | 87,3 → 76,3 | 91,0 → 85,3 | 27,9 → 25,6 |
| eregion | 24 | 0,0 → 0,0 | 85,3 → 77,0 | 100,0 → 100,0 | 20,1 → 20,2 |
| caradhras | 27 | 0,0 → 0,0 | 72,7 → 82,3 | 86,3 → 92,3 | 26,0 → 27,5 |
| durin | 30 | 19,3 → 10,7 | 90,3 → 81,7 | 100,0 → 100,0 | 29,0 → 29,3 |
| moria | 33 | 0,0 → 0,0 | 83,3 → 82,3 | 83,7 → 84,7 | 24,5 → 23,4 |
| sombrio | 36 | 39,7 → 0,0 | 89,0 → 73,0 | 94,7 → 80,7 | 20,8 → 21,2 |
| nimrodel | 39 | 0,0 → 0,0 | 91,3 → 84,7 | 91,7 → 87,7 | 25,9 → 23,6 |

**Muertes por zona (media por aventura): antes → final**

| Zona | MEDIO | EXPERTO |
|---|---|---|
| bree | 0,00 → 0,00 | 0,00 → 0,00 |
| chet | 0,15 → 0,43 | 0,12 → 0,20 |
| moscagua | 0,13 → 0,30 | 0,10 → 0,24 |
| cima | 0,41 → 0,32 | 0,19 → 0,06 |
| puente | 0,17 → 0,27 | 0,02 → 0,13 |
| quebradas | 0,19 → 0,30 | 0,05 → 0,23 |
| bruinen | 0,13 → 0,43 | 0,09 → 0,20 |
| eregion | 0,20 → 0,26 | 0,01 → 0,01 |
| caradhras | 0,44 → 0,35 | 0,26 → 0,20 |
| durin | 0,13 → 0,22 | 0,01 → 0,02 |
| moria | 0,23 → 0,26 | 0,18 → 0,16 |
| sombrio | 0,14 → 0,35 | 0,04 → 0,21 |
| nimrodel | 0,11 → 0,35 | 0,10 → 0,19 |

La dificultad queda repartida: antes había dos muros (cima y caradhras, 0,41 y 0,44 muertes) y el resto apenas mataba; ahora todas las zonas desde chet cuestan entre 0,22 y 0,43 muertes al MEDIO.

### Valores finales

```kotlin
LevelStats.STAT_SCALE = 100   // sin cambio
LevelStats.MAX_POWER  = 300   // sin cambio
ComboTuning.DEFAULT   = ComboTuning(spearBasePct = 16, fistBasePct = 21, cureBasePct = 35)                                        // sin cambio
RivalScaling.DEFAULT  = RivalScaling(hpPct = 115, attackPct = 68, healPct = 100, defensePct = 100, startPowerPct = 92, regenPct = 100)  // antes ataque 66
RivalScaling.BOSS     = RivalScaling(hpPct = 180, attackPct = 82, healPct = 100, defensePct = 100, startPowerPct = 100, regenPct = 100) // antes ataque 86
```

| Jefe | Offset | Patrón | Escolta (offset −4) |
|---|---|---|---|
| bree | +2 | sin cambio | 1 bandido (sin cambio) |
| chet | +3 → **+4** | `…, 31, 33` → **`12, 121, 1212, 12121, 31, 313`** | 1 → **2 lobos jóvenes** |
| moscagua | +4 | sin cambio | guardianas → **guardianas + espía del pantano** |
| cima | +3 → **+2** | sin cambio | — |
| puente | +3 → **+8** | sin cambio | — |
| quebradas | +5 | sin cambio | — → **huargo de la cueva** |
| bruinen | +6 | → **`32, 323, 3232, 31, 3123, 31232, 31`** | — |
| eregion | +2 → **+4** | sin cambio | espía + lobo de Saruman (sin cambio) |
| caradhras | +5 → **+3** | sin cambio | — |
| durin | +4 → **+7** | sin cambio | — |
| moria | +7 | sin cambio | — → **trol de las cavernas + trasgo** |
| sombrio | +8 | sin cambio | arquero → **arquero + trasgo** |
| nimrodel | +2 | → **`32, 323, 3232, 31, 2131, 31`** (sin Cura) | orco + arquero (sin cambio) |

- JSON de las 12 zonas con cambios, `ContentRules.Bestiary` (patrones de chet, bruinen y nimrodel), §3 y §5 de la spec de contenido y `docs/content/prompts-imagenes.md`. Las imágenes de los nodos cambiados son las de antes (pueden mostrar menos criaturas que el texto) y `prompts-imagenes-pixelart.md` no se ha tocado.
- Las huellas de `HpRegressionTest` se regrabaron desde el motor nuevo; `final-encounters.csv` y `final-adventures.csv` son los de este estado.

### Pendiente para pruebas reales (más enemigos y dificultad +)

1. **EXPERTO en el borde bajo** (21,2 % para 20–28 %): las escoltas castigan a la política EXPERTO (juega área con dos rivales o más). Si un jugador hábil lo ve injusto, la palanca es quitar una escolta (sombrío o moscagua) o bajar el ataque de jefe a 80 (con durin a +8).
2. **Precipicio de los grupos de 4**: con el ataque de los comunes en 74 o más algún grupo de 4 pasa a imposible al nivel esperado; con 68 hay margen, pero un jugador que llegue varios niveles por debajo puede tener que elegir otro camino.
3. **Comunes deterministas**: siguen al 100 % con el MEDIO al nivel esperado; su dificultad se nota en la vida que quitan (un grupo de 4 deja al jugador con un tercio) y en los jugadores con menos nivel.
4. **cima y caradhras aliviados** (offset −1 y −2): dejan de ser los muros; confirmar que no se sienten blandos.
5. Siguen abiertos el NOVATO al 0 % (lo para chet, ahora con dos lobos jóvenes de escolta) y los pendientes anteriores.

## Daño variable 50–150 % (2026-10-15)

Spec `2026-10-15-combat-cards-design.md`: cada pulso de daño o cura (DOT, COT, robo de vida y golpe directo) tira entre el 50 y el 150 % de su valor (`ComboTuning(rollMinPct = 50, rollMaxPct = 150)`, ahora `ComboTuning.DEFAULT`). `ComboTuning.FIXED` (tirada 100–100, no consume el RNG) reproduce el motor anterior, y `./gradlew :sim:run --args="build/reports-fixed --fixed-roll"` simula con él para tener el «antes» con las mismas semillas. Objetivos sin cambio (los de «Más enemigos y dificultad +»).

### 1. Antes y después de la tirada (mismas semillas; 300 combates por encuentro y 2000 aventuras por perfil)

El «antes» con `--fixed-roll` coincide exactamente con «Más enemigos y dificultad +» (mismos `final-encounters.csv` y `final-adventures.csv`): la tirada fija no altera el orden de consumo del RNG.

| Métrica | Tirada fija (antes) | Tirada 50–150 %, sin re-balance | Objetivo |
|---|---|---|---|
| MEDIO: sin muertes | 6,1 % | **4,6 %** | 5–9 % |
| EXPERTO: sin muertes | 21,2 % | **18,5 %** | 20–28 % |
| Muertes por aventura MEDIO / EXPERTO | 3,84 / 1,85 | 4,12 / 1,91 | MEDIO ≈ 3,5–5 |
| Muertes en combates comunes por aventura MEDIO | ≈ 0,41 | ≈ 0,67 | — |
| MEDIO comunes: mínimo | 100,0 | 91,7 (caradhras_3_3) | ≥ 88 % |
| MEDIO jefes: mín. / máx. (sin bree) | 72,7 (cima) / 84,7 (nimrodel) | 76,0 (quebradas) / **87,0 (durin)** | 55–85 % |
| Combates al tope / abandonadas | 0 / 0 | 0 / 0 | 0 / 0 |

La tirada no cambia la media pero sí la dispersión: los grupos de cuatro comunes dejan de ser deterministas (caradhras_3_3, nimrodel_5_0, durin_4_4 y bruinen_3_2 bajan del 100 al 92–97 % con el MEDIO) y esas muertes nuevas en comunes rompen las aventuras sin muertes. Los jefes se aprietan hacia el centro (el más fácil para el MEDIO, durin, sube del 81,7 al 87,0 %; el más difícil, cima, sube del 72,7 al 80,3 %). Quedan fuera tres métricas: MEDIO y EXPERTO sin muertes por debajo y durin por encima del 85 %.

### 2. Re-balance (mismas semillas; `sweep … fights=300 runs=2000`)

**Una palanca sola no basta.** El ataque de los comunes (`sweep attack=64,66,68`: MEDIO 5,2 / 5,0 / 4,6 %, EXPERTO 19,1 / 19,0 / 18,5 %) y el de los jefes (`sweep boss attack=74,76,78,80,82`: MEDIO 6,5 / 5,7 / 5,4 / 5,1 / 4,6 %, EXPERTO 21,8 / 21,0 / 20,4 / 19,2 / 18,5 %) suben las aventuras sin muertes, pero al bajarlos se escapan por arriba los jefes que ya estaban altos y los que llevan escolta: durin 87–94 %, eregion 85–92 % (sus escoltas son comunes) y puente 84–87 %. Bajar los PV de los jefes (`hp=165,172`) mueve menos que el ataque. Los offsets de los jefes más duros apenas cambian nada (moscagua +2/+3/+4: 73/76/77 % MEDIO; quebradas +4/+5: 76/76; bruinen +5/+6: 77/77; nimrodel +1/+2: 84/85), así que no sirven para subir las aventuras sin muertes.

Se combina, por tanto, bajar los dos ataques con subir el offset de los dos jefes que se escapan (`sweep offsets <zona>` con los ataques nuevos; % de victoria MEDIO):

| Jefe | Offsets probados (MEDIO) | Elegido |
|---|---|---|
| durin (comunes 68, jefes 78) | +7: 89 · +8: 88 · +9: 87 · +10: 86 · +12: 82 | — |
| durin (comunes 64, jefes 78) | +10: 86 · +11: 83 · +12: 82 | **+11** |
| eregion (comunes 64, jefes 76) | +4: 93 · +5: 91 · +6: 87 | — |
| eregion (comunes 64, jefes 78) | +6: 86 · +7: 83 · +8: 81 | **+7** |

| Comunes / jefes (ataque), eregion, durin | MEDIO | EXPERTO | Muertes MEDIO | Jefes MEDIO mín. / máx. |
|---|---|---|---|---|
| 68 / 82, +4, +7 (tirada sin re-balance) | 4,6 | 18,5 | 4,12 | 76,0 / 87,0 (durin) |
| 68 / 78, +4, +12 | 4,7 | 19,2 | 3,96 | 75,7 / 84,7 |
| 64 / 78, +4, +7 (sweep) | 6,0 | 21,5 | — | 75,7 / 92 (eregion), 89 (durin) |
| 64 / 78, +7, +11 | 5,0 | 20,8 | 3,60 | 75,7 / 84,7 |
| **64 / 77, +7, +11 (elegido)** | **5,3** | **21,4** | **3,52** | **74,0 / 84,3** |
| 64 / 76, +7, +11 (sweep) | 6,0 | 21,2 | — | 73,7 / 86 (durin) |

Con 76 durin vuelve a pasar del 85 %; con 77 todo queda dentro, aunque las aventuras sin muertes del MEDIO y las muertes por aventura quedan cerca de sus bordes (5 % y 3,5): las dos métricas tiran en sentidos opuestos (aliviar sube las aventuras sin muertes pero baja las muertes por aventura).

### Resultados

| Métrica | Tirada fija (antes) | Tirada sin re-balance | **Final** | Objetivo |
|---|---|---|---|---|
| MEDIO: sin muertes | 6,1 % | 4,6 % | **5,3 %** | 5–9 % |
| EXPERTO: sin muertes | 21,2 % | 18,5 % | **21,4 %** | 20–28 % |
| Muertes por aventura MEDIO / EXPERTO | 3,84 / 1,85 | 4,12 / 1,91 | **3,52 / 1,70** | MEDIO ≈ 3,5–5 |
| Muertes en combates comunes por aventura MEDIO | ≈ 0,41 | ≈ 0,67 | **≈ 0,37** | — |
| NOVATO | 0 % (atascado en chet) | 0 % | **0 %** (atascado en chet) | sin objetivo |
| Aventuras abandonadas MEDIO / EXPERTO | 0 / 0 | 0 / 0 | **0 / 0** | 0 |
| Combates al tope de 600 ticks | 0 | 0 | **0** | 0 |
| MEDIO normales: media / mínimo | 100,0 / 100,0 | 99,8 / 91,7 | **99,9 / 93,3 (caradhras_3_3)** | ≥ 88 % |
| MEDIO jefes: media / mín. / máx. (sin bree) | 79,6 / 72,7 (cima) / 84,7 (nimrodel) | 79,8 / 76,0 (quebradas) / 87,0 (durin) | **79,8 / 74,0 (moscagua) / 84,3 (eregion)** | 55–85 % |
| EXPERTO jefes: media / mín. (sin bree) | 87,9 / 77,0 (moscagua) | 87,5 / 78,3 (quebradas) | **88,8 / 77,7 (moscagua)** | — |

**Jefes por perfil (nivel esperado, 300 combates; NOVATO / MEDIO / EXPERTO): tirada fija → final**

| Zona | Antes | Final |
|---|---|---|
| bree | 75,3 / 100,0 / 100,0 | 81,7 / 100,0 / 100,0 |
| chet | 0,0 / 83,3 / 79,3 | 0,0 / 82,0 / 84,3 |
| moscagua | 0,0 / 80,0 / 77,0 | 0,0 / 74,0 / 77,7 |
| cima | 0,0 / 72,7 / 99,3 | 16,7 / 79,0 / 99,3 |
| puente | 0,0 / 81,7 / 86,7 | 0,0 / 84,0 / 92,0 |
| quebradas | 0,0 / 80,3 / 82,3 | 0,0 / 75,0 / 79,7 |
| bruinen | 0,0 / 76,3 / 85,3 | 0,0 / 75,3 / 84,3 |
| eregion | 0,0 / 77,0 / 100,0 | 0,0 / 84,3 / 100,0 |
| caradhras | 0,0 / 82,3 / 92,3 | 1,3 / 80,0 / 96,0 |
| durin | 10,7 / 81,7 / 100,0 | 2,0 / 83,3 / 98,3 |
| moria | 0,0 / 82,3 / 84,7 | 0,0 / 79,3 / 83,7 |
| sombrio | 0,0 / 73,0 / 80,7 | 0,0 / 77,3 / 89,7 |
| nimrodel | 0,0 / 84,7 / 87,7 | 0,0 / 84,0 / 80,7 |

**Muertes por zona (media por aventura): tirada fija → final**

| Zona | MEDIO | EXPERTO |
|---|---|---|
| chet | 0,43 → 0,29 | 0,20 → 0,21 |
| moscagua | 0,30 → 0,33 | 0,24 → 0,32 |
| cima | 0,32 → 0,24 | 0,06 → 0,04 |
| puente | 0,27 → 0,23 | 0,13 → 0,08 |
| quebradas | 0,30 → 0,32 | 0,23 → 0,20 |
| bruinen | 0,43 → 0,39 | 0,20 → 0,19 |
| eregion | 0,26 → 0,22 | 0,01 → 0,02 |
| caradhras | 0,35 → 0,36 | 0,20 → 0,06 |
| durin | 0,22 → 0,25 | 0,02 → 0,04 |
| moria | 0,26 → 0,24 | 0,16 → 0,19 |
| sombrio | 0,35 → 0,37 | 0,21 → 0,16 |
| nimrodel | 0,35 → 0,27 | 0,19 → 0,19 |

### Valores finales

```kotlin
ComboTuning.DEFAULT   = ComboTuning(spearBasePct = 16, fistBasePct = 21, cureBasePct = 35, rollMinPct = 50, rollMaxPct = 150) // antes sin tirada (100–100)
RivalScaling.DEFAULT  = RivalScaling(hpPct = 115, attackPct = 64, healPct = 100, defensePct = 100, startPowerPct = 92, regenPct = 100)  // antes ataque 68
RivalScaling.BOSS     = RivalScaling(hpPct = 180, attackPct = 77, healPct = 100, defensePct = 100, startPowerPct = 100, regenPct = 100) // antes ataque 82
```

| Jefe | Offset |
|---|---|
| eregion (Capitán dunlendino) | +4 → **+7** |
| durin (Guardián del Agua) | +7 → **+11** |

- El rango de offsets de jefe de §5 de la spec de contenido pasa de +2..+8 a **+2..+12** (`ContentRules.BOSS_OFFSETS` y `ContentRulesTest.bossOffsetRange`), porque el Guardián del Agua necesita +11; la tabla de §3 recoge los offsets nuevos.
- Las huellas de `HpRegressionTest` se regrabaron desde el motor nuevo; el caso de derrota sube sus rivales del nivel 9 al 10 (con el ataque 64 de los comunes, a nivel 9 ya no ganaban). `final-encounters.csv` y `final-adventures.csv` son los de este estado.
- `:sim:run` admite `--fixed-roll` (simula con `ComboTuning.FIXED`); los barridos usan la tirada de `ComboTuning.DEFAULT`.

### Pendiente para pruebas reales (daño variable 50–150 %)

1. **Métricas en el borde**: MEDIO 5,3 % sin muertes (objetivo 5–9) con 3,52 muertes por aventura (objetivo 3,5–5); ya no hay margen para aliviar ni para endurecer sin salirse de uno de los dos. Si los jugadores reales mueren más que el MEDIO simulado, la palanca es el ataque de los jefes (77 → 76, vigilando que durin no pase del 85 %).
2. **Comunes ya no deterministas**: los grupos de cuatro (caradhras_3_3 al 93 %, bruinen_3_2, durin_4_4) pueden matar al nivel esperado por una mala racha de tiradas; confirmar que se siente como mala suerte y no como injusticia.
3. **Offsets altos con poco efecto**: los offsets de jefe apenas mueven al MEDIO (la tirada y el Daze pesan más que el nivel); durin necesitó +11 y eregion +7. Si el Guardián del Agua o el Capitán dunlendino se sienten esponjas de vida, revisar su patrón en lugar del offset.
4. Siguen abiertos los pendientes de «Más enemigos y dificultad +» (EXPERTO cerca del borde bajo, NOVATO al 0 % en chet, cima y caradhras aliviados).

## Libro II: hasta el Monte del Destino (2026-10-16)

Spec `2026-10-16-road-to-mordor-design.md` §6: la ruta pasa de 14 a 24 zonas. Lothlórien es ahora una zona de descanso y le siguen ocho zonas de combate nuevas: anduin 40, argonath 43, amonhen 46, emynmuil 49, cienagas 52, ithilien 55, cirithungol 58 y gorgoroth 61. La ruta termina en el nodo `orodruin_final`. Solo se ajusta el contenido nuevo (patrones, `everyNTicks`, offsets de jefe y grupos); `RivalScaling`, `ComboTuning` y las 14 zonas antiguas no cambian.

Objetivos (Global Constraints del plan):

| Métrica | Objetivo |
|---|---|
| MEDIO: cada común | ≥ 88 % |
| MEDIO: cada jefe | 55–85 %; Nazgûl de Gorgoroth, 45–65 % |
| Aventura MEDIO / EXPERTO sin muertes | 2–5 % / 10–20 % |
| Combates al tope / aventuras abandonadas (MEDIO y EXPERTO) | 0 / 0 |

Mismas semillas que en las secciones anteriores; 300 combates por encuentro y 2000 aventuras por perfil.

### 1. Línea base: 24 zonas con los patrones y offsets iniciales

Las 14 zonas antiguas dan exactamente lo mismo que en «Daño variable 50–150 %»: sus filas de `encounters.csv` son idénticas y sus muertes por zona y niveles de entrada en `adventures.csv` también. Las zonas nuevas van detrás, así que no consumen el RNG de las antiguas. Las zonas nuevas, en cambio, salían muy por encima de lo previsto:

- **Aventuras:** MEDIO 0,2 % sin muertes (10,59 muertes por aventura) y EXPERTO 3,4 % (4,43).
- **Comunes:** los grupos de 3 y 4 eran un precipicio. Los MEDIO ganaban de media un 84,3 % de los grupos de 3 y un 58,6 % de los de 4. Casos extremos: `cienagas_4_6` (cuatro fuegos fatuos) 0,3 %, `emynmuil_5_2` (cuatro lobos de las rocas) 5,7 %, `anduin_3_3` (cuatro huargos) 28,0 %, `gorgoroth_4_5` (cuatro orcos de Mordor) 29,3 % y `cirithungol_4_2` (cuatro orcos de Morgul) 31,3 %.
- **Jefes:** el Capitán uruk (55,3 %), el Señor de los ahogados (54,3 %) y Ella-Laraña (55,0 %) estaban en el borde bajo.

**Por qué los grupos son más duros en el Libro II.** Las stats crecen de forma lineal con el nivel (PV 100 + 20·(L−1), ataque 10 + 2·(L−1)), así que un offset fijo de −4 o −5 debilita cada vez menos. En el puente (jugador L14, huargos L9) cada rival tiene un 60 % de la vida del jugador; en el Anduin (L41 frente a L36), un 87 %. Cuatro huargos que en el puente se ganaban siempre, en el Anduin se ganaban el 28 % de las veces.

### 2. Comunes: los que van en grupo lanzan cada 3 ticks

La única palanca que no obliga a reescribir texto es el ritmo de los comunes (los encuentros y los textos no cambian). Pasan de `everyNTicks` 2 a **3**:

- Uruk-hai;
- Orco de Mordor;
- Lobo de las rocas;
- Fuego fatuo;
- Arquero haradrim;
- Orco de Morgul.

Los Huargos del Anduin son una variante nueva del bestiario, `huargoAnduin` («Huargo del Anduin»: mismo patrón y retrato `warg`, cada 3 ticks). Los Huargos de los Argonath y del Libro I siguen lanzando cada 2 ticks. Los Huargos del Anduin hacen falta porque el Huargo es un común del Libro I: cambiarlo movería el puente, quebradas y bruinen. Y cambiar los grupos del Anduin obligaba a reescribir tres nodos (texto, `imagePrompt` y el prompt pixel-art).

| Paso (MEDIO) | Sin muertes | Muertes por aventura | Peor común de las zonas nuevas |
|---|---|---|---|
| Línea base | 0,2 % | 10,59 | 0,3 % (`cienagas_4_6`) |
| Orco de Mordor, Lobo de las rocas, Fuego fatuo, Orco de Morgul y Arquero haradrim cada 3 ticks | 0,3 % | 8,04 | 74,3 % (`argonath_6_1`, uruks + huargos) |
| + Uruk-hai cada 3 ticks | 0,3 % | 7,43 | ≥ 95 % salvo el Anduin |
| + Huargo del Anduin cada 3 ticks (y jefes del §3) | 1,1 % | 5,74 | **100 %** |

Guerrero haradrim, Espectro de la ciénaga, Araña, Uruk negro y Olog-hai siguen cada 2 ticks: sus grupos ya se ganaban al 100 %. Ninguna zona pierde su grupo de 4 (`RealContentTest.groupSizesPerZone`).

**Grupos por tamaño (comunes de las zonas nuevas, nivel esperado): % de victoria MEDIO y vida que le queda al jugador (% de su máximo); entre paréntesis, el Libro I**

| Rivales | Encuentros | MEDIO % victoria: antes → final | MEDIO vida media: antes → final (Libro I) | EXPERTO vida media: antes → final (Libro I) |
|---|---|---|---|---|
| 1 | 15 | 100,0 → 100,0 | 53,0 → 65,2 (60,4) | 61,4 → 72,1 (65,5) |
| 2 | 12 | 100,0 → 100,0 | 44,6 → 58,3 (56,2) | 54,9 → 67,1 (62,6) |
| 3 | 8 | 84,3 → 100,0 | 30,8 → 58,6 (46,9) | 44,6 → 66,5 (57,0) |
| 4 | 15 | 58,6 → 100,0 | 10,7 → 45,5 (32,2) | 22,3 → 55,4 (42,6) |

Los comunes del Libro II quedan algo más suaves que los del Libro I: un grupo de 4 deja al MEDIO con el 45 % de la vida, frente al 32 % del Libro I. No hay un punto intermedio (`everyNTicks` es entero) y endurecerlos bajaría aún más las aventuras sin muertes (ver §4).

### 3. Jefes: patrones y offsets

Como en el Libro I, el offset mueve poco al MEDIO (`sweep offsets <zona> 2,…,12`: anduin 82–86 % con cualquier offset; cienagas 63–67 %; gorgoroth 64–75 %). Lo que decide es **dónde va el Aturdir dentro del patrón**: un `31` justo antes de una cadena de daño hace que el jefe la lance contra un Warden aturdido. Lo mismo pasa con la Lanza III (`1313`) de Ella-Laraña. Se prueban patrones con `sweep offsets <zona> <offsets> pattern=… every=1`. Los resultados son NOVATO / MEDIO / EXPERTO al nivel esperado y entre paréntesis va el offset:

| Jefe | Patrones probados | Elegido |
|---|---|---|
| anduin | `32, 323, 3232, 31, 12, 121` (+4): 0/83/84 · `32, 323, 3232, 12, 121, 31`: 0/94/94 · `32, 323, 12, 121, 31` (+6): 0/85/91 · `32, 323, 31, 12, 121, 22`: 0/78/91 | **`12, 121, 31, 32, 323` (+4): 0/80/93** |
| argonath | `…, 31, 313, 11` (+5): 0/64/95 · `12, 121, 1212, 31, 11` (+5): 0/72/95 · `…, 11, 31, 313` (+5): 0/72/94 | **`12, 121, 1212, 11, 31` (+5): 0/83/96** |
| amonhen | `…, 31, 11, 123` (+5): 0/72/92 · `…, 11, 123, 31` (+5): 0/84/97 | **`13, 131, 1313, 11, 31, 123` (+2): 0/80/96** |
| emynmuil | `…, 31, 2132` (+6): 0/74/89 · `12, 121, 2132, 31` (+3): 3/95/100 | **`12, 121, 1212, 2132, 31` (+3): 0/82/91** |
| cienagas | `…, 31, 32, 323, 3232` (+6): 0/66/80 · `32, 323, 3232, 3123, 31232, 31` (+3): 0/87/91 | **`3123, 31232, 31, 32, 323` (+3): 0/81/97** |
| ithilien | `…, 31, 22, 123` (+6): 0/81/83 · `32, 323, 3232, 22, 123, 31`: 0/90/91 · `32, 323, 22, 31, 123`: 0/75/96 | **`32, 323, 22, 123, 31` (+3): 0/82/96** |
| cirithungol | `13, 131, 1313, 31, 313, 123` (+2/+7): 0/65/96 · 0/55/93 · con `22` (+2): 0/70/96 · `13, 131, 123, 31, 313` (+4): 12/96/100 · `13, 131, 31, 313, 123` (+4/+8/+10): 13/91/99 · 8/88/99 · 7/86/99 | **`13, 131, 31, 313, 123` (+12): 5/84/98** |
| gorgoroth | patrón inicial (+8/+9/+10/+12): 0/66/83 · 0/65/82 · 0/64/82 · 0/65/82 · con `313` tras el segundo `31`: 0/63–65/80 · `3123, 31232, 31, 313, 32, 323, 3232`: 0/43–44/63–66 | **patrón inicial (+10): 0/64/82** |

El Nazgûl conserva su patrón: todas las variantes con el MEDIO por debajo del 65 % bajaban también al EXPERTO (63–83 %). Ningún jefe se queda sin su escolta, porque los textos de nivel 7 las nombran.

### 4. MEDIO sin muertes: el objetivo del 2–5 % no se puede alcanzar con estos límites

Las 14 zonas antiguas, que no se tocan, ya dejan al MEDIO en un 5,3 % de aventuras sin muertes. Cada aventura suma ahora ocho jefes más, siete de ellos con un 85 % como mucho y el Nazgûl con un 65 % como mucho. Así que, aunque ningún común matara, el MEDIO llegaría como mucho a un 5,3 % × 0,85⁷ × 0,65 ≈ 1,1 %. En las aventuras sale algo más (unos ×1,35), porque los jugadores llegan a cada jefe con niveles algo distintos: el techo real está en torno al 1,5 %.

Lo confirma una prueba de cota superior: con cuatro jefes por encima del límite (anduin 94 %, emynmuil 95 %, ithilien 90 % y cirithungol 96 % con el MEDIO), el MEDIO solo llega al 1,6 % sin muertes. El ajuste elegido deja al MEDIO en un 1,1 %, con los siete jefes entre el 79,7 y el 84,0 % y sin apurar el 85 % (los valores de las tablas son exactos con estas semillas, pero cualquier cambio futuro los moverá). Llegar al 2–5 % obligaría a:

- dejar los jefes nuevos del MEDIO por encima del 90 %;
- o suavizar las 14 zonas antiguas (con `RivalScaling` o con su contenido), que esta tarea no debía tocar.

La decisión es del usuario (ver «Pendiente»).

### Resultados

| Métrica | Libro I (14 zonas) | 24 zonas, línea base | **24 zonas, final** | Objetivo |
|---|---|---|---|---|
| MEDIO: sin muertes | 5,3 % | 0,2 % | **1,1 %** | 2–5 % ✗ (ver §4) |
| EXPERTO: sin muertes | 21,4 % | 3,4 % | **12,3 %** | 10–20 % |
| Muertes por aventura MEDIO / EXPERTO | 3,52 / 1,70 | 10,59 / 4,43 | **5,74 / 2,35** | — |
| Muertes en las zonas nuevas por aventura MEDIO / EXPERTO | — | 7,07 / 2,72 | **2,21 / 0,66** | — |
| NOVATO | 0 % (atascado en chet) | 0 % | **0 %** (atascado en chet) | sin objetivo |
| Aventuras abandonadas MEDIO / EXPERTO | 0 / 0 | 0 / 0 | **0 / 0** | 0 |
| Combates al tope de 600 ticks | 0 | 0 | **0** | 0 |
| MEDIO comunes de las zonas nuevas: media / mínimo | — | 85,1 / 0,3 (`cienagas_4_6`) | **100,0 / 100,0** | ≥ 88 % |
| MEDIO jefes nuevos sin Gorgoroth: media / mín. / máx. | — | 68,0 / 54,3 (cienagas) / 83,0 (anduin) | **81,7 / 79,7 (anduin, amonhen) / 84,0 (cirithungol)** | 55–85 % |
| MEDIO Nazgûl de Gorgoroth | — | 66,3 | **63,7** | 45–65 % |
| EXPERTO jefes nuevos (con Gorgoroth): media / mínimo | — | 80,8 / 68,0 (cienagas) | **93,7 / 82,3 (gorgoroth)** | — |

**Las 14 zonas antiguas, sin cambios.** Sus filas de `final-encounters.csv` son idénticas a las de «Daño variable 50–150 %» (348 filas). En `final-adventures.csv` coinciden sus muertes por zona y sus niveles de entrada. Por perfil, los jefes del Libro I siguen con los valores de la tabla de «Daño variable 50–150 %» (MEDIO de 74,0, moscagua, a 84,3, eregion), y los comunes, con un 99,9 % de media y un mínimo del 93,3 % (`caradhras_3_3`). Muertes por aventura MEDIO / EXPERTO por zona, idénticas antes y después:

| Zona | MEDIO | EXPERTO |
|---|---|---|
| bree | 0,00 | 0,00 |
| chet | 0,29 | 0,21 |
| moscagua | 0,33 | 0,32 |
| cima | 0,24 | 0,04 |
| puente | 0,23 | 0,08 |
| quebradas | 0,32 | 0,20 |
| bruinen | 0,39 | 0,19 |
| eregion | 0,22 | 0,02 |
| caradhras | 0,36 | 0,06 |
| durin | 0,25 | 0,04 |
| moria | 0,24 | 0,19 |
| sombrio | 0,37 | 0,16 |
| nimrodel | 0,27 | 0,19 |

**Jefes nuevos por perfil (nivel esperado, 300 combates; NOVATO / MEDIO / EXPERTO): línea base → final**

| Zona | Nivel | Línea base | Final | Ticks MEDIO |
|---|---|---|---|---|
| anduin | 42 | 0,0 / 83,0 / 83,7 | 0,0 / 79,7 / 93,3 | 21,2 → 20,7 |
| argonath | 45 | 0,0 / 55,3 / 73,0 | 0,0 / 83,0 / 95,7 | 17,7 → 17,9 |
| amonhen | 48 | 0,0 / 73,7 / 79,3 | 0,0 / 79,7 / 95,7 | 20,0 → 17,7 |
| emynmuil | 50 | 0,0 / 74,0 / 82,7 | 0,0 / 82,3 / 91,0 | 24,7 → 24,3 |
| cienagas | 53 | 0,0 / 54,3 / 68,0 | 0,0 / 80,7 / 97,3 | 20,6 → 20,1 |
| ithilien | 56 | 0,0 / 81,0 / 84,7 | 0,0 / 82,3 / 96,0 | 23,4 → 20,8 |
| cirithungol | 59 | 0,0 / 55,0 / 92,7 | 5,0 / 84,0 / 98,0 | 22,9 → 28,5 |
| gorgoroth | 62 | 0,0 / 66,3 / 82,7 | 0,0 / 63,7 / 82,3 | 20,0 → 19,8 |

**Comunes de las zonas nuevas (nivel esperado, 300 combates): MEDIO media / mínimo y peor común de la línea base**

| Zona | Línea base | Final | EXPERTO mínimo | Peor común de la línea base |
|---|---|---|---|---|
| anduin | 84,1 / 28,0 | 100,0 / 100,0 | 92,7 → 100,0 | `anduin_3_3` (cuatro huargos) |
| argonath | 95,7 / 74,3 | 100,0 / 100,0 | 100,0 → 100,0 | `argonath_6_1` (dos uruks + dos huargos) |
| amonhen | 97,9 / 87,7 | 100,0 / 100,0 | 100,0 → 100,0 | `amonhen_6_1` (dos uruks + dos crebain) |
| emynmuil | 68,8 / 5,7 | 100,0 / 100,0 | 78,3 → 100,0 | `emynmuil_5_2` (cuatro lobos de las rocas) |
| cienagas | 83,4 / 0,3 | 100,0 / 100,0 | 8,0 → 100,0 | `cienagas_4_6` (cuatro fuegos fatuos) |
| ithilien | 81,6 / 35,3 | 100,0 / 100,0 | 95,7 → 100,0 | `ithilien_4_1` (dos guerreros + dos arqueros) |
| cirithungol | 88,5 / 31,3 | 100,0 / 100,0 | 88,0 → 100,0 | `cirithungol_4_2` (cuatro orcos de Morgul) |
| gorgoroth | 81,4 / 29,3 | 100,0 / 100,0 | 86,3 → 100,0 | `gorgoroth_4_5` (cuatro orcos de Mordor) |

**Muertes por zona nueva (media por aventura): línea base → final**

| Zona | MEDIO | EXPERTO |
|---|---|---|
| anduin | 0,78 → 0,30 | 0,31 → 0,09 |
| argonath | 1,01 → 0,27 | 0,41 → 0,06 |
| amonhen | 0,63 → 0,24 | 0,26 → 0,05 |
| emynmuil | 1,11 → 0,22 | 0,38 → 0,09 |
| cienagas | 1,10 → 0,26 | 0,62 → 0,03 |
| ithilien | 0,56 → 0,21 | 0,22 → 0,08 |
| cirithungol | 1,03 → 0,20 | 0,24 → 0,03 |
| gorgoroth | 0,85 → 0,51 | 0,28 → 0,23 |

Las zonas nuevas cuestan al MEDIO entre 0,20 y 0,30 muertes cada una, como las del Libro I (0,22–0,39), salvo Gorgoroth (0,51), que es el muro final buscado.

### Valores finales

```kotlin
RivalScaling.DEFAULT  // sin cambio (PV 115 / ataque 64)
RivalScaling.BOSS     // sin cambio (PV 180 / ataque 77)
ComboTuning.DEFAULT   // sin cambio
```

| Común | Patrón | everyNTicks |
|---|---|---|
| Huargo del Anduin (nuevo, `huargoAnduin`; anduin) | `12, 121, 1212, 12121` | **3** |
| Uruk-hai | sin cambio | 2 → **3** |
| Orco de Mordor | sin cambio | 2 → **3** |
| Lobo de las rocas | sin cambio | 2 → **3** |
| Fuego fatuo | sin cambio | 2 → **3** |
| Arquero haradrim | sin cambio | 2 → **3** |
| Orco de Morgul | sin cambio | 2 → **3** |

| Jefe | Offset | Patrón |
|---|---|---|
| anduin | +4 | `32, 323, 3232, 31, 12, 121` → **`12, 121, 31, 32, 323`** |
| argonath | +5 | `12, 121, 1212, 31, 313, 11` → **`12, 121, 1212, 11, 31`** |
| amonhen | +5 → **+2** | `13, 131, 1313, 31, 11, 123` → **`13, 131, 1313, 11, 31, 123`** |
| emynmuil | +6 → **+3** | `12, 121, 1212, 31, 2132` → **`12, 121, 1212, 2132, 31`** |
| cienagas | +6 → **+3** | `3123, 31232, 31, 32, 323, 3232` → **`3123, 31232, 31, 32, 323`** |
| ithilien | +6 → **+3** | `32, 323, 3232, 31, 22, 123` → **`32, 323, 22, 123, 31`** |
| cirithungol | +7 → **+12** | `13, 131, 1313, 31, 313, 123` → **`13, 131, 31, 313, 123`** |
| gorgoroth | +8 → **+10** | sin cambio |

- Están sincronizados los JSON de las ocho zonas nuevas, `ContentRules.Bestiary` (entrada `huargoAnduin`; zona anduin = arquero + huargoAnduin) y las tablas de §3 de la spec de contenido. `ContentRulesTest` cuenta ahora 24 comunes y comprueba también al Huargo del Anduin.
- Los textos, los `imagePrompt` y los grupos no cambian: los nodos siguen diciendo «Huargo» y nombrando a sus escoltas.
- `HpRegressionTest` no cambia, porque sus huellas no dependen del contenido.
- `final-encounters.csv` y `final-adventures.csv` son ya de 24 zonas.

### Pendiente para pruebas reales (Libro II)

1. **MEDIO sin muertes al 1,1 %** (objetivo 2–5 %), inalcanzable con los límites de los jefes y sin tocar el Libro I (§4). Hay que decidir entre:
   - aceptar el 1,1 % (en la aventura larga, casi nadie llega sin morir);
   - subir el tope de los jefes nuevos al 90 % con el MEDIO;
   - o rebajar el objetivo.
2. **Comunes del Libro II algo más suaves que los del Libro I** (un grupo de 4 deja al MEDIO con el 45 % de la vida, frente al 32 %). Si se sienten blandos, la palanca es volver a 2 ticks una especie concreta y vigilar sus grupos de 4. Primero el Uruk-hai: con 2 ticks, `argonath_6_1` bajaba al 74 %.
3. **Ella-Laraña a +12** (L70 contra el Warden a L59), en el tope del rango: su dureza está en el patrón (la Lanza III la hacía casi imposible), así que podría sentirse como una esponja de vida.
4. **El Nazgûl al 63,7 %** con el MEDIO y al 82,3 % con el EXPERTO: es el jefe más duro de toda la aventura (0,51 muertes por aventura). Ninguna variante bajaba al MEDIO sin hundir también al EXPERTO.
5. Siguen abiertos los pendientes del Libro I (NOVATO al 0 % en chet, métricas del Libro I en el borde y comunes no deterministas).

## Rivales que se curan y MAESTRO (2026-10-16)

Specs `2026-10-16-rival-healing-design.md` (§2–§5) y `2026-10-08-story-content-design.md` §3.

Qué cambió:
- Los jefes y un humanoide de cada grupo de comunes lanzan una cadena de Cura (`21, 212, 2121, 21212`) al bajar de un umbral de vida.
- Se añade el jugador simulado **MAESTRO**: juega como el usuario y es la referencia para los jefes.

Mismas semillas que en las secciones anteriores; 300 combates por encuentro y 2000 aventuras por perfil.

Herramientas nuevas del simulador:
- `:sim:run --args="--no-reactions"`: la línea base sin reacciones;
- `sweep offsets <zona> … thresholds=40|50;25|none shield=2132|none`: prueba los umbrales y el escudo de un jefe;
- `sweep … tactics=0,30,50`: prueba el Escudo táctico.

### Objetivos

Los objetivos de la spec §5 no se podían cumplir a la vez:
- El MAESTRO ganaba el 99–100 % de los jefes del Libro II incluso a +12, con cualquier variante de reacción.
- El 1,1 % de aventuras MEDIO sin muertes exige jefes que no se curen.

El usuario juega como el MAESTRO y dijo que sus curas y escudos le daban demasiada ventaja, así que el controlador fijó estos objetivos:

| Métrica | MEDIO | MAESTRO |
|---|---|---|
| Comunes | ≥ 85 % | ≥ 97 % |
| Cada jefe | ≥ 40 % (Gorgoroth ≥ 35 %) | 75–92 % (Bree, tutorial, exento) |
| Aventura | ≤ 20 muertes de media y 0 abandonadas | de 1 a 4 muertes de media |
| Combates al tope de ticks | 0 | 0 |

### 1. Diagnóstico

| Paso | MEDIO: sin muertes / muertes por aventura | EXPERTO | MAESTRO | Jefes MEDIO / MAESTRO (media) |
|---|---|---|---|---|
| Sin reacciones (`--no-reactions`, la línea base) | 1,1 % / 5,74 | 12,3 % / 2,35 | 75,0 % / 0,58 | 79,6 / 99,3 |
| Reacciones de la spec (`[50, 25]`, escudo 2132 o 2131) | 0,0 % / 41,21 | 0,4 % / 12,61 | 15,1 % / 7,34 | — |
| Todas con Escudo maestro (`2132`, decisión del controlador) | 0,0 % / 56,02 (0,5 % abandonadas) | 0,2 % / 17,12 | 13,2 % / 10,39 (0,8 % abandonadas) | 42,5 / 90,6 |

Con Escudo maestro (−50 % de daño durante 60 ticks), la reacción hunde al MEDIO, que no corta las curas: 8 jefes con el MEDIO por debajo del 30 %. El MAESTRO, en cambio, seguía al 99–100 % contra una decena de jefes, incluso a +12.

**La causa es el Escudo táctico del jugador.** El MAESTRO lo mantiene activo todo el combate contra cualquier jefe con `31` (todos), y el Escudo táctico daba **inmunidad total** al aturdimiento. Los jefes cuya amenaza es «aturdir y pegar» quedaban desactivados. Es la ventaja de la que se quejaba el usuario. Ni bajar la cura (`cure=28,22,16`) ni subir el ataque de los jefes (`RivalScaling.BOSS` ataque 95 y 115) bajaban de ahí a esos jefes: solo hundían al MEDIO.

### 2. Cambio en una herramienta del jugador: Escudo táctico (`2131`)

| | Antes | Después |
|---|---|---|
| Coste | 20 | 20 (sin cambio) |
| Duración | 60 ticks | 60 ticks (sin cambio) |
| Cadena | — | — |
| Efecto | inmune al aturdimiento: no acumula probabilidad y no tira | **la probabilidad de aturdimiento se multiplica por 0,5 (−50 %)** mientras dura, como dice `descripcion.md` («reduce 50% de que te pongas en Daze»); la probabilidad se acumula de forma normal, la tirada usa la mitad y, cuando el escudo se acaba, lo acumulado tira entero |

- Va en `ComboTuning.tacticsDazePct = 50` (0 equivale a la inmunidad antigua), que se aplica en `TickResolver.rollDaze`. `ComboApplier` ya no bloquea la probabilidad.
- La ayuda de combos dice «−50% de probabilidad de Daze durante 60 ticks».
- Afecta igual a los rivales que usan `2131` (patrones de los jefes de eregion y nimrodel).
- El MEDIO y el EXPERTO no usan `2131`.
- **La política del MAESTRO.**
  - Sigue eligiendo `2131` contra los jefes que aturden. Se probó que use siempre `2132`, pero salía peor contra chet, moscagua, moria, nimrodel y gorgoroth (MAESTRO como mucho al 73–77 %), así que se descartó.
  - Único ajuste: ahora también aturde a un jefe en plena reacción aunque tenga el Escudo táctico, porque ya no es inmune (`MaestroTest.maestroDazesAReactingBossUnderEscudoTactico`).
  - El MEDIO y el EXPERTO siguen saltándose su apertura de Aturdir mientras el jefe tiene el Escudo táctico.

El mismo cambio, aislado (`sweep boss tactics=…`, 300 combates y 2000 aventuras):

| `tacticsDazePct` | Contenido | MAESTRO: sin muertes / muertes por aventura | MAESTRO jefes (mín.–máx.) | MEDIO |
|---|---|---|---|---|
| 0 (inmunidad, antes) | primer re-balance | 58,8 % / — | 94–100 % | sin cambio |
| 30 (primer re-balance) | primer re-balance | 5,6 % / 3,82 | 82–88 % | sin cambio |
| **50 (`descripcion.md`)** | primer re-balance | 1,0 % / 6,62 | 68,7–83,0 % (cima, durin, puente y moria por debajo del 75 %) | sin cambio (13,48 muertes) |
| **50** | **final (§3)** | **5,4 % / 3,87** | **80,7–90,3 %** | 9,30 muertes |

El primer re-balance se hizo con 30 (−70 %), pero `descripcion.md`, que manda, dice −50 %. Con 50, el MAESTRO se queda corto en cuatro jefes y muere 6,6 veces por aventura. Se re-ajustaron los jefes con las mismas palancas (offsets, umbrales, escudo y patrones), sin volver a tocar las herramientas del jugador. Con 60 o más, el Escudo táctico deja de servir y el MAESTRO cae por debajo del MEDIO en varios jefes.

### 3. Jefes: umbrales, escudo, offsets y patrones

**Primera búsqueda** (con 30): cinco variantes de reacción por jefe, con offsets de +2 a +15 y 200 combates por punto:
- `[30]` sin escudo;
- `[40]` sin escudo;
- `[30]` con `2132`;
- `[50, 25]` sin escudo;
- `[50, 25]` con `2132`.

**Segunda búsqueda** (con 50): offsets de +2 a +14 con la reacción del primer re-balance. También se probaron `[30]` y `[20]` y algunos patrones para los jefes que ya estaban a +2. Se mide al jugador en su nivel esperado y uno por debajo, porque en la aventura muchos llegan con un nivel menos.

Para cada jefe se eligió el punto con el MAESTRO entre el 80 y el 90 % y el mejor MEDIO.

- **Escudo.** La mayoría de los jefes se quedan sin escudo: el Escudo maestro hunde al MEDIO entre 10 y 25 puntos más. Lo conservan argonath y cienagas, en los que el MAESTRO, si no, gana más del 92 %.
- **Umbrales.** Los jefes que se le atragantaban al MEDIO o al MAESTRO tienen un solo umbral, de `[20]` a `[40]`.
- **Patrones**, con los criterios de `ContentRules` (daño ≥ 50 %, cadena de daño real y `31`): se quitan tiers de Aturdir.
  - Aturdir es lo que el MAESTRO ya no puede evitar del todo, y castigaba más a los jefes que aturden varias veces.
  - El Capitán orco gana un Golpe directo, porque con su patrón el MAESTRO estaba al 92–95 % con cualquier offset.
- **Offsets.** Ahora pueden ir de +2 a +15 (`ContentRules.BOSS_OFFSETS`). Con −50 % casi todos los jefes acaban entre +2 y +6.

| Jefe | Umbrales | Escudo | Offset | Patrón |
|---|---|---|---|---|
| chet | `[20]` | — | +4 → **+2** | `12, 121, 1212, 12121, 31, 313` → **`12, 121, 1212, 12121, 31`** |
| moscagua | `[30]` | — | +4 → **+2** | sin cambio |
| cima | `[30]` | — | +2 | `32, 323, 3232, 31, 313` → **`32, 323, 3232, 31`** |
| puente | `[40]` | — | +8 → **+3** | sin cambio |
| quebradas | `[30]` | — | +5 → **+2** | sin cambio |
| bruinen | `[20]` | — | +6 → **+2** | sin cambio |
| eregion | `[50, 25]` | — | +7 → **+6** | sin cambio |
| caradhras | `[40]` | — | +3 → **+2** | `32, 323, 3232, 31, 313, 22` → **`32, 323, 3232, 31, 22`** |
| durin | `[50, 25]` | — | +11 → **+2** | `13, 131, 1313, 31, 313, 3131` → **`13, 131, 1313, 31`** |
| moria | `[20]` | — | +7 → **+2** | sin cambio |
| sombrio | `[50, 25]` | — | +8 → **+2** | `12, 121, 31, 11` → **`12, 121, 31, 11, 123`** |
| nimrodel | `[40]` | — | +2 | sin cambio |
| anduin | `[30]` | — | +4 | sin cambio |
| argonath | `[50, 25]` | `2132` | +5 → **+3** | sin cambio |
| amonhen | `[40]` | — | +2 → **+3** | sin cambio |
| emynmuil | `[30]` | — | +3 → **+2** | sin cambio |
| cienagas | `[50, 25]` | `2132` | +3 | sin cambio |
| ithilien | `[50, 25]` | — | +3 → **+2** | sin cambio |
| cirithungol | `[40]` | — | +12 → **+2** | sin cambio |
| gorgoroth | `[20]` | — | +10 → **+2** | sin cambio |

El jefe de Bree no cambia: `[40]`, sin escudo, `21, 212`.

### 4. Comunes

- **Curandero.** La reacción del curandero (el primer humanoide de cada grupo) baja de `[40]` a `[30]`.
- **Guerrero haradrim.** Pasa de `everyNTicks` 2 a **3**. Con el curandero en el grupo, `ithilien_4_1` (dos guerreros y dos arqueros haradrim) bajaba al 77,7 % con el MEDIO. Con el umbral a `[25]` se quedaba en el 81 %, y con el Guerrero cada 3 ticks sube al 100 %.
- Los patrones de los comunes del Libro I no cambian (solo el umbral del curandero, de 40 a 30).

### Resultados

| Métrica | Sin reacciones | Reacciones con 2132, sin re-balance | **Final** | Objetivo |
|---|---|---|---|---|
| MEDIO: muertes por aventura | 5,74 | 56,02 | **9,30** | ≤ 20 ✓ |
| MEDIO: sin muertes | 1,1 % | 0,0 % | **0,1 %** | — (ver pendientes) |
| MEDIO: abandonadas | 0 | 0,5 % | **0** | 0 ✓ |
| MEDIO: jefes, media / mínimo | 79,6 / 63,7 | 42,5 / 8,7 | **73,5 / 52,7** (bruinen) | ≥ 40 ✓ |
| MEDIO: Nazgûl de Gorgoroth | 63,7 | 10,3 | **69,7** | ≥ 35 ✓ |
| MEDIO: comunes, media / mínimo | 99,9 / 93,3 | 99,5 / 77,7 | **99,7 / 87,3** (`gorgoroth_4_0`) | ≥ 85 ✓ |
| MAESTRO: muertes por aventura | 0,58 | 10,39 | **3,87** | 1–4 ✓ |
| MAESTRO: sin muertes | 75,0 % | 13,2 % | **5,4 %** | — |
| MAESTRO: jefes, media / mínimo / máximo | 99,3 / 88,7 / 100 | 90,6 / 47,3 / 100 | **85,4 / 80,7 (bruinen) / 90,3 (amonhen)** | 75–92 ✓ |
| MAESTRO: comunes, mínimo | 100 | 100 | **100** | ≥ 97 ✓ |
| EXPERTO: sin muertes / muertes por aventura | 12,3 % / 2,35 | 0,2 % / 17,12 | **10,3 % / 2,82** | — |
| Combates al tope de 600 ticks | 0 | 0 | **0** | 0 ✓ |
| **Duración media de un combate de jefe (ticks), MEDIO / MAESTRO** | 23,0 / 19,9 | 86,2 / 90,5 | **54,3 / 41,1** | «más guerra» |

**«Más guerra».** Los combates de jefe duran algo más del doble:
- MEDIO: de 23 a 54 ticks;
- MAESTRO: de 20 a 41 ticks.

Los combates contra comunes apenas cambian (MEDIO de 9,2 a 10,5 ticks).

**Jefes por zona (nivel esperado, 300 combates).** Cada celda da tres valores: sin reacciones → reacciones con 2132, sin re-balance → **final**. Las muertes son la media por aventura.

| Zona | MEDIO jefe | MAESTRO jefe | Ticks jefe MEDIO | Ticks jefe MAESTRO | Muertes MEDIO | Muertes MAESTRO |
|---|---|---|---|---|---|---|
| bree | 100,0 → 100,0 → **100,0** | 100,0 → 100,0 → **100,0** | 16,8 → 17,0 → **17,0** | 14,2 → 14,2 → **14,2** | 0,00 → 0,00 → **0,00** | 0,00 → 0,00 → **0,00** |
| chet | 82,0 → 9,3 → **80,0** | 88,7 → 47,3 → **83,3** | 24,0 → 72,2 → **26,1** | 20,9 → 91,7 → **35,7** | 0,29 → 10,39 → **0,24** | 0,46 → 7,45 → **0,27** |
| moscagua | 74,0 → 14,3 → **60,7** | 100,0 → 83,3 → **82,3** | 23,5 → 73,1 → **37,0** | 20,9 → 111,2 → **42,8** | 0,33 → 3,70 → **0,63** | 0,02 → 0,37 → **0,25** |
| cima | 79,0 → 36,0 → **74,0** | 100,0 → 99,0 → **87,7** | 24,1 → 83,8 → **56,0** | 20,4 → 105,3 → **49,4** | 0,24 → 2,45 → **0,45** | 0,00 → 0,02 → **0,23** |
| puente | 84,0 → 25,3 → **82,3** | 100,0 → 89,7 → **85,7** | 26,0 → 87,3 → **58,4** | 23,4 → 131,9 → **47,3** | 0,23 → 3,34 → **0,27** | 0,00 → 0,17 → **0,20** |
| quebradas | 75,0 → 41,0 → **73,0** | 100,0 → 99,7 → **88,3** | 20,9 → 85,1 → **34,4** | 18,9 → 89,6 → **28,9** | 0,32 → 1,90 → **0,45** | 0,00 → 0,02 → **0,15** |
| bruinen | 75,3 → 8,7 → **52,7** | 100,0 → 70,3 → **80,7** | 25,6 → 72,1 → **50,2** | 23,3 → 126,9 → **44,5** | 0,39 → 7,58 → **0,99** | 0,00 → 0,56 → **0,26** |
| eregion | 84,3 → 80,7 → **84,3** | 100,0 → 100,0 → **87,3** | 22,1 → 84,4 → **72,5** | 16,5 → 44,2 → **31,6** | 0,22 → 0,30 → **0,30** | 0,00 → 0,00 → **0,16** |
| caradhras | 80,0 → 23,3 → **70,0** | 100,0 → 98,3 → **83,7** | 27,3 → 88,3 → **59,9** | 22,4 → 128,2 → **60,8** | 0,36 → 3,38 → **0,57** | 0,00 → 0,03 → **0,26** |
| durin | 83,3 → 25,3 → **84,0** | 100,0 → 97,0 → **83,3** | 30,7 → 94,1 → **89,3** | 22,4 → 125,0 → **50,8** | 0,25 → 3,49 → **0,23** | 0,00 → 0,03 → **0,19** |
| moria | 79,3 → 21,3 → **72,3** | 99,3 → 65,7 → **88,7** | 22,4 → 80,2 → **29,5** | 21,6 → 99,1 → **25,2** | 0,24 → 4,09 → **0,35** | 0,05 → 0,87 → **0,14** |
| sombrio | 77,3 → 72,0 → **73,3** | 100,0 → 100,0 → **84,3** | 22,1 → 110,3 → **64,5** | 19,3 → 97,1 → **55,5** | 0,37 → 0,44 → **0,47** | 0,00 → 0,00 → **0,18** |
| nimrodel | 84,0 → 55,0 → **76,7** | 99,7 → 99,0 → **84,7** | 21,9 → 80,6 → **42,3** | 19,8 → 93,7 → **47,5** | 0,27 → 1,19 → **0,62** | 0,00 → 0,01 → **0,20** |
| anduin | 79,7 → 45,7 → **73,7** | 100,0 → 99,3 → **84,7** | 20,7 → 87,6 → **35,9** | 18,0 → 77,0 → **35,3** | 0,30 → 1,16 → **0,46** | 0,00 → 0,00 → **0,17** |
| argonath | 83,0 → 60,3 → **62,3** | 100,0 → 100,0 → **87,0** | 17,9 → 76,5 → **74,1** | 16,0 → 37,4 → **27,5** | 0,27 → 1,06 → **0,79** | 0,00 → 0,00 → **0,14** |
| amonhen | 79,7 → 48,0 → **72,7** | 100,0 → 99,7 → **90,3** | 17,7 → 69,5 → **37,0** | 16,3 → 35,0 → **24,1** | 0,24 → 1,17 → **0,43** | 0,00 → 0,01 → **0,12** |
| emynmuil | 82,3 → 66,3 → **74,3** | 100,0 → 100,0 → **83,0** | 24,3 → 109,5 → **69,1** | 23,6 → 110,3 → **65,3** | 0,22 → 0,60 → **0,37** | 0,00 → 0,00 → **0,20** |
| cienagas | 80,7 → 68,0 → **68,0** | 100,0 → 100,0 → **86,0** | 20,1 → 78,6 → **78,6** | 15,6 → 40,8 → **37,1** | 0,26 → 0,55 → **0,61** | 0,00 → 0,00 → **0,18** |
| ithilien | 82,3 → 71,3 → **76,7** | 100,0 → 100,0 → **85,7** | 20,8 → 110,4 → **85,7** | 18,7 → 86,7 → **60,0** | 0,21 → 0,62 → **0,33** | 0,00 → 0,00 → **0,17** |
| cirithungol | 84,0 → 67,0 → **89,0** | 100,0 → 100,0 → **87,3** | 28,5 → 112,7 → **59,8** | 17,0 → 58,3 → **23,3** | 0,20 → 0,54 → **0,12** | 0,00 → 0,01 → **0,23** |
| gorgoroth | 63,7 → 10,3 → **69,7** | 98,7 → 63,7 → **84,3** | 19,8 → 68,7 → **26,0** | 22,0 → 120,1 → **29,1** | 0,51 → 8,05 → **0,61** | 0,03 → 0,81 → **0,20** |

Bree sigue siendo tutorial: 100 % y 0 muertes para todos los perfiles salvo el NOVATO.

### Valores finales

```kotlin
ComboTuning.DEFAULT.tacticsDazePct  // nuevo: 50 (−50 % de probabilidad de Daze; antes, inmunidad total; con 0 se recupera)
ComboTuning.DEFAULT                 // lanza 16 / puño 21 / cura 35, sin cambio
RivalScaling.DEFAULT                // sin cambio (PV 115 / ataque 64)
RivalScaling.BOSS                   // sin cambio (PV 180 / ataque 77)
```

- Reacciones, offsets y patrones de los jefes: la tabla del §3.
- `ContentRules.bossReaction` usa una tabla por jefe.
- Curandero de los comunes: `[30]`.
- Guerrero haradrim: cada 3 ticks.
- `ContentRules.BOSS_OFFSETS` va de +2 a +15.
- Están sincronizados con la spec de contenido §3.
- `final-encounters.csv` y `final-adventures.csv` incluyen ya al MAESTRO.

### Pendiente para pruebas reales (rivales que se curan y MAESTRO)

1. **El MEDIO casi nunca llega sin morir** (0,1 %, antes 1,1 %): muere 9,3 veces por aventura, frente a 5,7 antes. Es la consecuencia buscada de «más guerra», con jefes MEDIO entre el 53 y el 89 %. Si a los jugadores medios les frustra, la palanca es quitar el segundo umbral de los jefes que lo conservan, empezando por argonath y cienagas (los dos con escudo).
2. **El MAESTRO es la referencia, pero es un modelo.**
   - El Escudo táctico se queda en el −50 % de `descripcion.md`.
   - Si el usuario sigue ganando muy fácil, las palancas son, por orden:
     - subir offsets: casi todos los jefes están a +2–+6, así que queda margen hasta +15;
     - volver a poner el segundo umbral o el escudo en la reacción.
3. **Casi todos los jefes están a +2–+3.** Con el Escudo táctico al −50 %, el Aturdir de los jefes vuelve a pesar mucho y los jefes que aturden varias veces pierden tiers de Aturdir. Si se sienten flojos, se puede subir el offset sin tocar el patrón.
4. **El EXPERTO sale por encima del MAESTRO en muchos jefes** (media del 89 %, frente al 85 %). Usa el Escudo maestro y no el táctico, que ahora protege menos. No tiene objetivo propio.
5. **El MEDIO y el EXPERTO siguen saltándose su apertura de Aturdir** mientras el jefe tiene el Escudo táctico; solo se ajustó el MAESTRO. Afecta a los jefes de eregion y nimrodel, que lo llevan en el patrón.
6. **Las imágenes y textos de los nodos no cambian.** Los jefes de chet, cima, caradhras, durin y sombrio solo cambian uno o dos combos del patrón.
7. Siguen abiertos los pendientes del Libro II y del Libro I.

## Jefes con modo ataque y defensa (2026-10-17)

Spec `2026-10-17-boss-modes-design.md` (§1–§3) y `2026-10-08-story-content-design.md` §3. Prevalece sobre los números de balance de «Rivales que se curan y MAESTRO».

Qué cambió:
- Los 21 jefes ya no llevan `reaction`: alternan **modo ataque** (su `pattern`, sin escudos ni curas, cada `everyNTicks`) y **modo defensa** (`2132`, la cadena de Cura completa `21`→`21212` y los dos primeros combos de daño del ataque, un combo **cada tick**). Entran en defensa al bajar de `defenseBelowPct`, vuelven al ataque al llegar a `attackAbovePct` y defienden como mucho `maxDefenses` veces. Aturdido en defensa, el jefe salta a los combos de daño finales. Valores de la spec: 50/70/3 (Bree, tutorial: 40/60/1 y `21, 212, 12`).
- **Motor:** un jefe aturdido en defensa ya no pone en cola un combo cada tick para que se lo rechacen (`PatternBrain.nextCast` devuelve null mientras el Daze está activo; `BossModesTest.dazedDefenderStaysQuiet`). Las tiradas y los lanzamientos no cambian: el combo se rechazaba igual y aturdido no se bebe poción; solo desaparecen los `ComboQueued`/`ComboRejected` de ruido.
- **Motor, regla de la última defensa:** en su última defensa permitida (`defenses == maxDefenses`) el jefe lanza el patrón de defensa una sola vez (`defense.size` combos; saltar por Daze a la cola de daño cuenta) y vuelve al ataque tenga la vida que tenga; las defensas anteriores siguen en bucle hasta el `attackAbovePct` (`BossModesTest.lastDefenseRunsOneCycleThenAttacks`, `earlierDefensesStillLoopUntilAttackAbove` y `breeDefenseCannotLoop`). Sin ella, la defensa de Bree (40/60/1, `21, 212, 12` cada tick) se repetía mientras el jefe no llegara al 60 % y el NOVATO, que apenas la superaba, tardaba 205 ticks de media: 17 de 300 combates al tope de 600 ticks y 43 en sus aventuras. Con la regla: 50 ticks, 99,7 % y 0 al tope.
- **Simulador:**
  - el MAESTRO aturde al jefe en cuanto lo ve en defensa y todavía con su escudo y sus curas (`bossMode == DEFENSE` y antes de los combos de daño finales); antes miraba `reactionStep` (`MaestroTest`);
  - el informe da, por zona y perfil, los ticks medios del combate de jefe y las **entradas medias en defensa** (`FightResult.bossDefenses`, columna `avgDefenses` de `encounters.csv`), y en qué encuentros hubo combates al tope;
  - `sweep modes [below=…] [above=…] [max=…] [offset=…] [tail=12;121] [zones=…] [fights=…]` aplica los modos a todos los jefes (salvo Bree) y da MEDIO/MAESTRO por zona; `sweep offsets <zona> … below= above= max= tail=` lo hace para un jefe.

Mismas semillas que en las secciones anteriores; 300 combates por encuentro y 2000 aventuras por perfil. Objetivos (spec §3, los de la ronda anterior): MAESTRO comunes ≥ 97 %, cada jefe 75–92 %, 1–4 muertes por aventura; MEDIO comunes ≥ 85 %, cada jefe ≥ 40 % (Gorgoroth ≥ 35 %), ≤ 20 muertes por aventura; 0 abandonadas y 0 combates al tope; Bree exento. No se tocan las herramientas del jugador ni `RivalScaling`.

### 1. Antes y con los valores de la spec (sin la regla de la última defensa)

Con 50/70/3 en todos los jefes los combates se alargan y se endurecen mucho más que con las reacciones:
- el escudo de la defensa (−50 % de daño durante 60 ticks) y la cadena de curas, lanzada cada tick, se repiten hasta tres veces por combate;
- el jefe que no llega al 70 % sigue en defensa: escudo, curas y daño en bucle.

El MAESTRO deja 10 de los 20 jefes por debajo del 75 % y el MEDIO se queda por debajo del 40 % en 8.

| Métrica | Anterior («Rivales que se curan y MAESTRO») | Modos 50/70/3 (spec) |
|---|---|---|
| MAESTRO: jefes, media / mínimo / máximo | 85,4 / 80,7 / 90,3 | 72,1 / 46,7 (chet) / 89,3 |
| MAESTRO: muertes por aventura | 3,87 | 10,82 |
| MEDIO: jefes, media / mínimo | 73,5 / 52,7 | 49,4 / 13,7 (bruinen) |
| MEDIO: muertes por aventura / abandonadas | 9,30 / 0 | 38,99 / 0,2 % |
| Duración media de un combate de jefe (ticks), MEDIO / MAESTRO | 54,3 / 41,1 | 93,5 / 62,8 |
| Entradas medias en defensa por combate de jefe, MEDIO / MAESTRO | — | 1,81 / 1,46 |

### 2. Re-balance: umbral y número de defensas por jefe

- **Offsets:** no sirven de palanca, porque 15 de los 20 jefes ya estaban en el mínimo (+2).
- **Cola de daño de la defensa:** tampoco; con `tail=12` (un solo Lanza I) los jefes difíciles no mejoraban (MAESTRO 70–83 %).
- **`attackAbovePct`:** apenas mueve nada (con 50/1, 55 y 60 dan lo mismo dentro del ruido).

Lo que decide es **cuántas veces** defiende el jefe y **cuándo** (`sweep modes below=15,20,30,40,50 max=1,2 fights=300`):

Barrido hecho antes de la regla de la última defensa, que apenas cambia los jefes ajustados (ver Resultados).

| `defenseBelowPct` / `maxDefenses` | MEDIO media / mínimo | MAESTRO media / mínimo / máximo |
|---|---|---|
| 50 / 3 (spec) | 49,4 / 13,7 | 72,1 / 46,7 / 89,3 |
| 50 / 2 | 57,6 / 25,0 | 76,8 / 56,7 / 90,3 |
| 50 / 1 | 65,0 / 43,0 | 82,8 / 68,7 / 92,3 |
| 30 / 1 | 65,1 / 41,0 | 82,0 / 67,0 / 92,7 |
| 20 / 2 | 67,1 / 26,3 | 83,1 / 61,7 / 92,0 |
| 20 / 1 | 72,5 / 43,7 | 86,8 / 75,3 / 94,3 |
| 15 / 1 | 77,3 / 57,7 | 89,6 / 84,7 / 96,0 |

Para cada jefe se eligió el punto más cercano a la spec con el MAESTRO entre el 84 y el 90 % y el mejor MEDIO. Hace falta una media del MAESTRO por encima del 85 % para no pasar de 4 muertes por aventura.

| Jefe | `defenseBelowPct` | `maxDefenses` | MAESTRO / MEDIO con ese punto (sweep) |
|---|---|---|---|
| eregion, argonath, amonhen, cienagas, ithilien, cirithungol | 50 | 3 (spec) | ya en rango con la spec |
| durin | 50 | **2** | 85 / 85 |
| puente, sombrio, emynmuil | 50 | **1** | 86–87 / 68–72 |
| quebradas, nimrodel, anduin | **20** | **2** | 86–90 / 72–74 |
| cima, moria | **20** | **1** | 88–89 / 63–68 |
| chet, moscagua, bruinen, caradhras, gorgoroth | **15** | **1** | 85–88 / 58–80 |

- Bree no cambia (40/60/1).
- Los 14 jefes ajustados están en `ContentRules.BOSS_MODE_TUNING` (los demás usan 50/70/3), en el JSON (`tools/content/tune_boss_modes.py`) y en la spec de contenido §3. `RealContentTest` comprueba que coinciden.
- Los jefes a 15 % hacen una «última resistencia»: escudo y curas cuando están casi muertos, una sola vez.

### Resultados

Final = umbrales del §2 + regla de la última defensa.

| Métrica | Anterior | Modos 50/70/3 | **Final** | Objetivo |
|---|---|---|---|---|
| MAESTRO: jefes, media / mínimo / máximo | 85,4 / 80,7 / 90,3 | 72,1 / 46,7 / 89,3 | **87,3 / 84,7 (caradhras) / 89,7 (quebradas, nimrodel)** | 75–92 ✓ |
| MAESTRO: comunes, mínimo | 100 | 100 | **100** | ≥ 97 ✓ |
| MAESTRO: muertes por aventura / sin muertes | 3,87 / 5,4 % | 10,82 / 0,3 % | **3,33 / 6,4 %** | 1–4 ✓ |
| MEDIO: jefes, media / mínimo | 73,5 / 52,7 | 49,4 / 13,7 | **70,2 / 58,3 (bruinen)** | ≥ 40 ✓ |
| MEDIO: Nazgûl de Gorgoroth | 69,7 | 15,0 | **71,7** | ≥ 35 ✓ |
| MEDIO: comunes, mínimo | 87,3 (`gorgoroth_4_0`) | 87,3 | **87,3** (`gorgoroth_4_0`) | ≥ 85 ✓ |
| MEDIO: muertes por aventura / sin muertes / abandonadas | 9,30 / 0,1 % / 0 | 38,99 / 0,0 % / 0,2 % | **10,70 / 0,1 % / 0** | ≤ 20 y 0 ✓ |
| EXPERTO: sin muertes / muertes por aventura | 10,3 % / 2,82 | 3,1 % / 5,25 | **9,8 % / 2,93** | — |
| Combates al tope de 600 ticks (todos los perfiles, NOVATO incluido) | 0 | 17 en encuentros (NOVATO, `bree_7_0`) y 43 en aventuras del NOVATO | **0** | 0 ✓ |
| NOVATO contra el jefe de Bree: victorias / ticks | — | 93,3 % / 204,9 | **99,7 % / 49,9** | Bree exento |
| Duración media de un combate de jefe (ticks), MEDIO / MAESTRO | 54,3 / 41,1 | 93,5 / 62,8 | **59,2 / 31,7** | — |
| Entradas medias en defensa por combate de jefe, MEDIO / MAESTRO | — | 1,81 / 1,46 | **1,18 / 0,99** | — |

Las medias de ticks y de entradas en defensa incluyen a Bree. Los comunes no cambian: sus curanderos siguen con su reacción.

**Jefes por zona (nivel esperado, 300 combates).** Cada celda: anterior («Rivales que se curan y MAESTRO») → modos 50/70/3 → **final**; las entradas en defensa, modos 50/70/3 → **final**. Las muertes son la media por aventura.

| Zona | MEDIO jefe | MAESTRO jefe | Ticks jefe MEDIO | Ticks jefe MAESTRO | Entradas en defensa MEDIO / MAESTRO | Muertes MEDIO | Muertes MAESTRO |
|---|---|---|---|---|---|---|---|
| bree | 100,0 → 100,0 → **100,0** | 100,0 → 100,0 → **100,0** | 17,0 → 17,0 → **17,0** | 14,2 → 14,2 → **14,2** | 0,97 / 0,47 → **0,97 / 0,47** | 0,00 → 0,00 → **0,00** | 0,00 → 0,00 → **0,00** |
| chet | 80,0 → 27,0 → **81,0** | 83,3 → 46,7 → **88,7** | 26,1 → 87,0 → **24,8** | 35,7 → 85,3 → **32,6** | 1,67 / 1,75 → **0,76 / 0,93** | 0,24 → 3,17 → **0,21** | 0,27 → 1,67 → **0,24** |
| moscagua | 60,7 → 24,3 → **65,3** | 82,3 → 54,0 → **85,3** | 37,0 → 83,9 → **28,8** | 42,8 → 97,7 → **26,7** | 1,57 / 1,91 → **0,69 / 0,86** | 0,63 → 3,11 → **0,51** | 0,25 → 0,95 → **0,17** |
| cima | 74,0 → 38,0 → **61,3** | 87,7 → 69,0 → **87,0** | 56,0 → 107,7 → **60,5** | 49,4 → 96,7 → **42,8** | 2,08 / 1,91 → **0,91 / 0,95** | 0,45 → 1,85 → **0,67** | 0,23 → 0,60 → **0,21** |
| puente | 82,3 → 47,0 → **71,0** | 85,7 → 72,0 → **87,3** | 58,4 → 107,5 → **59,8** | 47,3 → 73,7 → **37,1** | 2,10 / 1,67 → **0,96 / 0,94** | 0,27 → 1,22 → **0,38** | 0,20 → 0,46 → **0,18** |
| quebradas | 73,0 → 48,7 → **70,7** | 88,3 → 80,3 → **89,7** | 34,4 → 82,0 → **33,5** | 28,9 → 47,1 → **23,2** | 1,65 / 1,29 → **0,88 / 0,96** | 0,45 → 1,34 → **0,49** | 0,15 → 0,27 → **0,15** |
| bruinen | 52,7 → 13,7 → **58,3** | 80,7 → 53,0 → **87,3** | 50,2 → 89,8 → **41,1** | 44,5 → 93,7 → **28,9** | 1,67 / 1,73 → **0,79 / 0,91** | 0,99 → 6,66 → **0,82** | 0,26 → 1,26 → **0,16** |
| eregion | 84,3 → 78,3 → **78,3** | 87,3 → 87,0 → **87,0** | 72,5 → 114,1 → **114,1** | 31,6 → 32,7 → **32,5** | 2,08 / 1,12 → **2,08 / 1,12** | 0,30 → 0,36 → **0,33** | 0,16 → 0,15 → **0,16** |
| caradhras | 70,0 → 34,7 → **62,0** | 83,7 → 61,0 → **84,7** | 59,9 → 117,3 → **63,3** | 60,8 → 114,0 → **44,1** | 2,07 / 2,04 → **0,84 / 0,93** | 0,57 → 2,36 → **0,78** | 0,26 → 0,82 → **0,20** |
| durin | 84,0 → 75,3 → **85,0** | 83,3 → 82,0 → **85,3** | 89,3 → 130,4 → **99,2** | 50,8 → 43,8 → **37,3** | 2,44 / 1,29 → **1,76 / 1,14** | 0,23 → 0,43 → **0,23** | 0,19 → 0,21 → **0,18** |
| moria | 72,3 → 28,7 → **68,3** | 88,7 → 53,3 → **87,7** | 29,5 → 88,7 → **37,8** | 25,2 → 70,7 → **28,2** | 1,68 / 1,53 → **0,80 / 0,93** | 0,35 → 3,27 → **0,50** | 0,14 → 0,87 → **0,15** |
| sombrio | 73,3 → 68,7 → **71,3** | 84,3 → 78,7 → **89,3** | 64,5 → 117,3 → **57,3** | 55,5 → 67,4 → **33,2** | 2,06 / 1,56 → **0,79 / 0,91** | 0,47 → 0,65 → **0,46** | 0,18 → 0,24 → **0,13** |
| nimrodel | 76,7 → 37,3 → **72,7** | 84,7 → 63,7 → **89,7** | 42,3 → 101,8 → **37,8** | 47,5 → 96,1 → **26,7** | 1,92 / 1,89 → **0,97 / 0,98** | 0,62 → 2,14 → **0,63** | 0,20 → 0,54 → **0,12** |
| anduin | 73,7 → 45,3 → **72,7** | 84,7 → 74,7 → **85,7** | 35,9 → 99,5 → **38,2** | 35,3 → 61,2 → **26,1** | 1,84 / 1,45 → **0,95 / 0,97** | 0,46 → 1,40 → **0,46** | 0,17 → 0,31 → **0,15** |
| argonath | 62,3 → 61,0 → **61,0** | 87,0 → 86,0 → **86,0** | 74,1 → 63,4 → **63,4** | 27,5 → 29,6 → **29,6** | 1,51 / 1,15 → **1,51 / 1,15** | 0,79 → 0,77 → **0,79** | 0,14 → 0,13 → **0,12** |
| amonhen | 72,7 → 60,3 → **60,3** | 90,3 → 89,3 → **89,3** | 37,0 → 58,6 → **58,6** | 24,1 → 27,4 → **27,4** | 1,40 / 1,09 → **1,40 / 1,09** | 0,43 → 1,20 → **1,22** | 0,12 → 0,16 → **0,14** |
| emynmuil | 74,3 → 63,3 → **72,0** | 83,0 → 81,3 → **88,0** | 69,1 → 74,0 → **38,8** | 65,3 → 49,6 → **23,6** | 1,58 / 1,35 → **0,85 / 0,92** | 0,37 → 0,66 → **0,39** | 0,20 → 0,24 → **0,11** |
| cienagas | 68,0 → 69,7 → **69,7** | 86,0 → 87,7 → **87,7** | 78,6 → 88,5 → **88,5** | 37,1 → 31,5 → **31,1** | 1,79 / 1,13 → **1,79 / 1,13** | 0,61 → 0,50 → **0,55** | 0,18 → 0,15 → **0,16** |
| ithilien | 76,7 → 70,7 → **70,7** | 85,7 → 87,3 → **87,7** | 85,7 → 125,0 → **125,0** | 60,0 → 58,2 → **57,8** | 2,16 / 1,43 → **2,16 / 1,43** | 0,33 → 0,47 → **0,46** | 0,17 → 0,22 → **0,21** |
| cirithungol | 89,0 → 81,7 → **81,7** | 87,3 → 85,0 → **85,0** | 59,8 → 130,5 → **130,5** | 23,3 → 38,5 → **38,5** | 2,25 / 1,21 → **2,25 / 1,21** | 0,12 → 0,25 → **0,24** | 0,23 → 0,25 → **0,24** |
| gorgoroth | 69,7 → 15,0 → **71,7** | 84,3 → 50,0 → **88,3** | 26,0 → 79,9 → **24,3** | 29,1 → 89,3 → **24,3** | 1,44 / 1,72 → **0,72 / 0,88** | 0,61 → 7,18 → **0,60** | 0,20 → 1,34 → **0,16** |

### Valores finales

```kotlin
ContentRules.BOSS_MODE_TUNING   // nuevo: 14 jefes con defenseBelowPct 15/20/50 y maxDefenses 1/2 (tabla del §2)
ComboTuning.DEFAULT             // sin cambio (lanza 16 / puño 21 / cura 35, tacticsDazePct 50)
RivalScaling.DEFAULT            // sin cambio (PV 115 / ataque 64)
RivalScaling.BOSS               // sin cambio (PV 180 / ataque 77)
```

- Offsets y patrones de ataque de los jefes: sin cambio (spec de contenido §3).
- Regla de la última defensa en `PatternBrain` (un ciclo y vuelta al ataque).
- `attackAbovePct` 70 y la defensa (`2132, 21, 212, 2121, 21212` + los dos primeros combos de daño) en todos los jefes salvo Bree.
- `final-encounters.csv` (con la columna nueva `avgDefenses`) y `final-adventures.csv` regenerados.

### Pendiente para pruebas reales (jefes con modo ataque y defensa)

1. **Menos defensas de las que pedía la spec.** Solo 6 jefes defienden hasta 3 veces desde el 50 %; los otros 14 defienden una o dos veces, y 5 de ellos solo al 15 %. Con 50/70/3 el MAESTRO, que juega como el usuario, ganaba solo el 47–89 %. Si los jugadores ven poco la defensa, la palanca es subir el umbral (15 → 20 → 30) de chet, moscagua, bruinen, caradhras y gorgoroth, empezando por los que tienen al MAESTRO más alto.
2. **Combates de jefe más cortos para el MAESTRO** (32 ticks, antes 41) y algo más largos para el MEDIO (59, antes 54): el MAESTRO corta la cadena de curas con Aturdir. Si se quiere «más guerra» para el usuario, los candidatos son los jefes con el MAESTRO por encima del 88 % (nimrodel, quebradas, amonhen, sombrio): `maxDefenses` +1.
3. **El MAESTRO aturde en cuanto ve el escudo del jefe**, y con el Daze el jefe salta a sus combos de daño finales. Es la contra prevista, pero el modelo aturde con la cadena hasta el tier 3 cada vez; un jugador real puede tardar más en reaccionar.
4. **El EXPERTO sigue por encima del MAESTRO** en varios jefes (media del 89 %, frente al 87 %). No tiene objetivo propio.
5. Siguen abiertos los pendientes de «Rivales que se curan y MAESTRO» (el MEDIO casi nunca llega sin morir: 0,0 %), del Libro II y del Libro I.

## Escudo táctico: inmunidad total (2026-10-17)

Decisión del usuario: el Escudo táctico del jugador (`2131`) vuelve a dar **inmunidad total al Daze**, como dice `descripcion.md` («impide entrar en modo daze»; la línea del −50 % queda sustituida). Prevalece sobre los números de balance de «Jefes con modo ataque y defensa».

Qué cambió:
- **Motor:** `ComboTuning.tacticsDazePct` 50 → **0**. Con 0 no hay tirada de Daze mientras el escudo está activo (`TickResolver.rollDaze`) y, además, Aturdir no deja probabilidad sobre un objetivo con el Escudo táctico activo (`ComboApplier`). **Regla para la probabilidad acumulada:** no se acumula mientras dura el escudo, así que al caducar no queda nada ganado bajo el escudo que tire entero; la probabilidad que ya había antes de activarse no tira mientras dura y caduca sola (dura 10 ticks; el escudo, 60). Un escudo lanzado en este tick todavía no cuenta (los buffs actúan desde el siguiente). Con `tacticsDazePct` > 0 sigue la regla anterior (la probabilidad se guarda y la tirada se escala). Tests: `DazeRollTest.tacticalShieldGivesImmunityByDefault`, `noChanceGainedUnderImmunityRollsAfterTheShieldExpires`, `partialTacticalShieldScalesDazeChance`; `ComboApplierTest.tacticalShieldBlocksDazeChanceUnderImmunity`, `pendingTacticalShieldDoesNotBlockDazeChance`, `tacticalShieldStoresDazeChanceWhenItOnlyScales`.
- **Ayuda:** «Escudo táctico · inmune a Daze durante 60 ticks» (`ComboDescriber`).
- **Simulador:** el MAESTRO ya no intenta aturdir a un jefe en defensa que tenga Escudo táctico (ningún jefe lo usa hoy; `MaestroTest.maestroDoesNotDazeADefendingBossUnderEscudoTactico`). El resto de las políticas no cambia; el MAESTRO sigue escudándose con `2131` contra todo jefe que tenga un `31` y con `2132` contra los demás.

Mismas semillas que en las secciones anteriores; 300 combates por encuentro y 2000 aventuras por perfil. Objetivos (los de la ronda anterior): MAESTRO comunes ≥ 97 %, cada jefe 75–92 %, 1–4 muertes por aventura; MEDIO comunes ≥ 85 %, cada jefe ≥ 40 % (Gorgoroth ≥ 35 %), ≤ 20 muertes por aventura; 0 abandonadas y 0 combates al tope para todos los perfiles; Bree exento. No se tocan las herramientas del jugador ni `RivalScaling`.

### 1. Solo la inmunidad

El MEDIO y el EXPERTO no usan el Escudo táctico y no cambian (MEDIO 10,70 muertes por aventura, EXPERTO 2,93). El MAESTRO, que se escuda con él contra todos los jefes (todos llevan un `31`), deja de quedar aturdido y ya no pierde casi nunca: **jefes 97,0–100 %** (media 99,5), **0,24 muertes por aventura** y 81,9 % de aventuras sin muertes. Fuera de rango en los 20 jefes.

### 2. Re-balance de los jefes

- **Offsets solos no sirven.** Con un offset común a todos los jefes (`sweep modes offset=2,4,6,8,10`), el MAESTRO sigue en el 87–100 % de media con +10 y el MEDIO ya cae al 49 % (mínimo 14 %): subir el nivel castiga sobre todo al que se deja aturdir.
- **Lo que separa al MAESTRO del MEDIO es el escudo que eligen.** El MEDIO se protege con el Escudo maestro (−50 % de daño) y el MAESTRO, contra un jefe que aturde, con el táctico, que no quita daño. Un patrón de ataque con **más daño y un solo `31`** castiga al MAESTRO y alivia al MEDIO (menos Daze). Ejemplo, Trol de piedra (`sweep offsets quebradas`, 200 combates, MEDIO/MAESTRO): con el patrón anterior `12, 121, 1212, 31, 11`, 54/99 con +8; con `12, 121, 1212, 13, 131, 31`, 68/89 con +6.
- **Menos defensas** (umbral 10–20 %, una sola) dejan al MEDIO más margen para subir el offset.
- Se barrieron, por jefe, 2–4 patrones (añadir una segunda cadena de daño hasta el tier 2 o 3 y, si los había, quitar el `31` repetido, la Furia `11` o la Guardia `22`; siempre conservando los dos primeros combos de daño, y con ellos la cola de la defensa), los umbrales 10/15/20/50 con 1–3 defensas y los offsets +2..+15 (`sweep offsets <zona> … pattern= below= max= every=`). Para cada jefe se eligió el punto con el MAESTRO entre el 85 y el 91 % y el mejor MEDIO.
- Las muertes del MAESTRO en las aventuras son casi el doble de lo que dan los encuentros (unas 0,2 por jefe con un 88–90 % de victorias), así que para quedarse en ≤ 4 hace falta casi todos los jefes en el 85–91 %; por eso los últimos ajustes (chet, moscagua, cima, moria, nimrodel, argonath, emynmuil) buscan subir al MAESTRO dentro del rango.
- `RivalScaling.BOSS` no hizo falta.

| Jefe | patrón de ataque (antes → ahora) | `everyNTicks` | offset | `defenseBelowPct` / `maxDefenses` |
|---|---|---|---|---|
| chet | `12, 121, 1212, 12121, 31` (sin cambio) | 1 | +2 → **+3** | 15 / 1 → **10 / 1** |
| moscagua | `32, 323, 3232, 13, 31` → **`32, 323, 3232, 13, 131, 1313, 31`** | 1 | +2 | 15 / 1 → **10 / 1** |
| cima | `32, 323, 3232, 31` → **`32, 323, 3232, 12, 121, 31`** | 1 | +2 → **+5** | 20 / 1 → **15 / 1** |
| puente | `12, 121, 1212, 31, 11` → **`12, 121, 1212, 13, 131, 31, 11`** | 1 | +3 → **+2** | 50 / 1 |
| quebradas | `12, 121, 1212, 31, 11` → **`12, 121, 1212, 13, 131, 31`** | 1 | +2 → **+6** | 20 / 2 |
| bruinen | `32, 323, 3232, 31, 3123, 31232, 31` → **`32, 323, 3232, 13, 131, 3123, 31232, 31`** | 1 | +2 → **+3** | 15 / 1 |
| eregion | `12, 121, 31` → **`12, 121, 1212, 13, 131, 31, 123`** | 2 → **1** | +6 → **+3** | 50 / 3 → **15 / 1** |
| caradhras | `32, 323, 3232, 31, 22` → **`32, 323, 3232, 12, 121, 1212, 31, 123`** | 1 | +2 → **+3** | 15 / 1 |
| durin | `13, 131, 1313, 31` → **`13, 131, 1313, 12, 121, 1212, 31, 123`** | 1 | +2 → **+5** | 50 / 2 → **15 / 1** |
| moria | `32, 323, 3232, 31, 123` → **`32, 323, 3232, 13, 131, 31, 123`** | 1 | +2 → **+3** | 20 / 1 → **15 / 1** |
| sombrio | `12, 121, 31, 11, 123` → **`12, 121, 1212, 13, 131, 31, 123`** | 1 | +2 → **+6** | 50 / 1 → **20 / 2** |
| nimrodel | `32, 323, 3232, 31, 31` → **`32, 323, 3232, 12, 121, 31`** | 1 | +2 → **+4** | 20 / 2 → **20 / 1** |
| anduin | `12, 121, 31, 32, 323` → **`12, 121, 1212, 13, 131, 31, 32, 323, 123`** | 1 | +4 → **+8** | 20 / 2 → **15 / 1** |
| argonath | `12, 121, 1212, 11, 31` → **`12, 121, 1212, 13, 131, 1313, 31, 123`** | 1 | +3 → **+11** | 50 / 3 → **15 / 1** |
| amonhen | `13, 131, 1313, 11, 31, 123` → **`13, 131, 1313, 12, 121, 1212, 31, 123`** | 1 | +3 → **+12** | 50 / 3 → **15 / 1** |
| emynmuil | `12, 121, 1212, 31` → **`12, 121, 1212, 32, 323, 3232, 31, 123`** | 1 | +2 | 50 / 1 → **10 / 1** |
| cienagas | `3123, 31232, 31, 32, 323` → **`3123, 31232, 13, 131, 1313, 31, 32, 323, 123`** | 1 | +3 → **+10** | 50 / 3 → **15 / 1** |
| ithilien | `32, 323, 22, 123, 31` → **`32, 323, 3232, 12, 121, 123, 31`** | 1 | +2 | 50 / 3 → **15 / 1** |
| cirithungol | `13, 131, 31, 313, 123` → **`13, 131, 1313, 12, 121, 1212, 31, 123`** | 1 | +2 → **+8** | 50 / 3 → **15 / 1** |
| gorgoroth | `32, 323, 3232, 31, 3123, 31232, 31, 123` → **`32, 323, 3232, 3123, 31232, 31, 123`** | 1 | +2 → **+4** | 15 / 1 |
| bree | `12, 121, 11, 31` (sin cambio) | 4 | +2 | 40 / 1 (`attackAbovePct` 60) |

Los patrones nuevos cumplen las reglas de contenido (abren con daño, daño ≥ 50 %, una cadena de daño real, un `31`, ninguna cura ni escudo en el ataque y nada por encima del tier 3 salvo el Lobo gris alfa). Están en `Bestiary.bosses`, `ContentRulesTest.v2Bosses` (Libro I), la spec de contenido §3 y el JSON (`tools/content/tune_boss_modes.py`, tablas `TUNING` y `ATTACK`); los umbrales, en `ContentRules.BOSS_MODE_TUNING` y `ContentRulesTest.TUNED_MODES`.

### Resultados

| Métrica | Anterior («Jefes con modo ataque y defensa») | Solo la inmunidad | **Final** | Objetivo |
|---|---|---|---|---|
| MAESTRO: jefes, media / mínimo / máximo | 87,3 / 84,7 / 89,7 | 99,5 / 97,0 / 100 | **88,6 / 81,3 (cima) / 91,3 (nimrodel)** | 75–92 ✓ |
| MAESTRO: comunes, mínimo | 100 | 100 | **100** | ≥ 97 ✓ |
| MAESTRO: muertes por aventura / sin muertes | 3,33 / 6,4 % | 0,24 / 81,9 % | **3,94 / 14,3 %** | 1–4 ✓ |
| MEDIO: jefes, media / mínimo | 70,2 / 58,3 (bruinen) | 70,2 / 58,3 | **74,6 / 50,0 (cima)** | ≥ 40 ✓ |
| MEDIO: Nazgûl de Gorgoroth | 71,7 | 71,7 | **82,0** | ≥ 35 ✓ |
| MEDIO: comunes, mínimo | 87,3 (`gorgoroth_4_0`) | 87,3 | **87,3** (`gorgoroth_4_0`) | ≥ 85 ✓ |
| MEDIO: muertes por aventura / sin muertes / abandonadas | 10,70 / 0,1 % / 0 | 10,70 / 0,1 % / 0 | **9,58 / 0,2 % / 0** | ≤ 20 y 0 ✓ |
| EXPERTO: sin muertes / muertes por aventura | 9,8 % / 2,93 | 9,8 % / 2,93 | **7,3 % / 3,33** | — |
| Combates al tope de 600 ticks (todos los perfiles, NOVATO incluido) | 0 | 0 | **0** | 0 ✓ |
| NOVATO contra el jefe de Bree: victorias / ticks | 99,7 % / 49,9 | 99,7 % / 49,9 | **99,7 % / 49,9** | Bree exento |
| Duración media de un combate de jefe (ticks), MEDIO / MAESTRO | 59,2 / 31,7 | 59,2 / 33,3 | **33,8 / 28,0** | — |
| Entradas medias en defensa por combate de jefe, MEDIO / MAESTRO | 1,18 / 0,99 | 1,18 / 1,07 | **0,87 / 0,90** | — |

Las medias de ticks y de entradas en defensa incluyen a Bree. Los comunes no cambian. El NOVATO sigue atascado en el jefe de chet (100 % de aventuras abandonadas, como antes): no tiene objetivo de victorias.

**Jefes por zona (nivel esperado, 300 combates).** Cada celda: anterior («Jefes con modo ataque y defensa») → solo la inmunidad → **final**. Las muertes son la media por aventura.

| Zona | MEDIO jefe | MAESTRO jefe | Ticks jefe MEDIO | Ticks jefe MAESTRO | Muertes MEDIO | Muertes MAESTRO |
|---|---|---|---|---|---|---|
| bree | 100,0 → 100,0 → **100,0** | 100,0 → 100,0 → **100,0** | 17,0 → 17,0 → **17,0** | 14,2 → 14,2 → **14,2** | 0,00 → 0,00 → **0,00** | 0,00 → 0,00 → **0,00** |
| chet | 81,0 → 81,0 → **84,7** | 88,7 → 97,0 → **87,7** | 24,8 → 24,8 → **22,9** | 32,6 → 32,3 → **24,8** | 0,21 → 0,21 → **0,18** | 0,24 → 0,12 → **0,24** |
| moscagua | 65,3 → 65,3 → **79,7** | 85,3 → 100,0 → **85,7** | 28,8 → 28,8 → **25,8** | 26,7 → 26,4 → **20,0** | 0,51 → 0,51 → **0,29** | 0,17 → 0,00 → **0,20** |
| cima | 61,3 → 61,3 → **50,0** | 87,0 → 100,0 → **81,3** | 60,5 → 60,5 → **57,8** | 42,8 → 45,8 → **41,1** | 0,67 → 0,67 → **1,26** | 0,21 → 0,01 → **0,29** |
| puente | 71,0 → 71,0 → **61,3** | 87,3 → 100,0 → **90,0** | 59,8 → 59,8 → **55,1** | 37,1 → 33,5 → **47,8** | 0,38 → 0,38 → **0,81** | 0,18 → 0,00 → **0,17** |
| quebradas | 70,7 → 70,7 → **68,3** | 89,7 → 100,0 → **90,0** | 33,5 → 33,5 → **45,7** | 23,2 → 19,8 → **73,8** | 0,49 → 0,49 → **0,48** | 0,15 → 0,00 → **0,25** |
| bruinen | 58,3 → 58,3 → **69,3** | 87,3 → 99,3 → **90,0** | 41,1 → 41,1 → **52,4** | 28,9 → 34,9 → **34,8** | 0,82 → 0,82 → **0,77** | 0,16 → 0,02 → **0,15** |
| eregion | 78,3 → 78,3 → **69,0** | 87,0 → 100,0 → **85,3** | 114,1 → 114,1 → **27,3** | 32,5 → 43,6 → **20,3** | 0,33 → 0,33 → **0,50** | 0,16 → 0,00 → **0,15** |
| caradhras | 62,0 → 62,0 → **62,0** | 84,7 → 99,3 → **90,7** | 63,3 → 63,3 → **44,0** | 44,1 → 48,7 → **26,3** | 0,78 → 0,78 → **0,85** | 0,20 → 0,00 → **0,21** |
| durin | 85,0 → 85,0 → **50,3** | 85,3 → 100,0 → **87,0** | 99,2 → 99,2 → **51,1** | 37,3 → 38,8 → **30,2** | 0,23 → 0,23 → **0,82** | 0,18 → 0,00 → **0,23** |
| moria | 68,3 → 68,3 → **89,7** | 87,7 → 99,0 → **91,0** | 37,8 → 37,8 → **27,7** | 28,2 → 26,9 → **26,2** | 0,50 → 0,50 → **0,17** | 0,15 → 0,03 → **0,24** |
| sombrio | 71,3 → 71,3 → **80,7** | 89,3 → 100,0 → **90,0** | 57,3 → 57,3 → **38,1** | 33,2 → 40,4 → **26,6** | 0,46 → 0,46 → **0,40** | 0,13 → 0,00 → **0,18** |
| nimrodel | 72,7 → 72,7 → **83,7** | 89,7 → 99,7 → **91,3** | 37,8 → 37,8 → **33,6** | 26,7 → 24,0 → **27,6** | 0,63 → 0,63 → **0,51** | 0,12 → 0,01 → **0,27** |
| anduin | 72,7 → 72,7 → **91,7** | 85,7 → 100,0 → **90,7** | 38,2 → 38,2 → **24,8** | 26,1 → 30,6 → **20,1** | 0,46 → 0,46 → **0,17** | 0,15 → 0,00 → **0,07** |
| argonath | 61,0 → 61,0 → **84,3** | 86,0 → 100,0 → **90,7** | 63,4 → 63,4 → **27,3** | 29,6 → 33,7 → **23,2** | 0,79 → 0,79 → **0,27** | 0,12 → 0,00 → **0,22** |
| amonhen | 60,3 → 60,3 → **82,7** | 89,3 → 99,7 → **88,7** | 58,6 → 58,6 → **27,4** | 27,4 → 32,9 → **22,0** | 1,22 → 1,22 → **0,23** | 0,14 → 0,01 → **0,24** |
| emynmuil | 72,0 → 72,0 → **91,3** | 88,0 → 100,0 → **89,0** | 38,8 → 38,8 → **22,0** | 23,6 → 23,4 → **18,7** | 0,39 → 0,39 → **0,13** | 0,11 → 0,00 → **0,19** |
| cienagas | 69,7 → 69,7 → **66,0** | 87,7 → 100,0 → **87,0** | 88,5 → 88,5 → **30,7** | 31,1 → 32,2 → **26,3** | 0,55 → 0,55 → **0,54** | 0,16 → 0,00 → **0,23** |
| ithilien | 70,7 → 70,7 → **93,0** | 87,7 → 100,0 → **89,0** | 125,0 → 125,0 → **25,0** | 57,8 → 67,1 → **19,9** | 0,46 → 0,46 → **0,10** | 0,21 → 0,00 → **0,12** |
| cirithungol | 81,7 → 81,7 → **52,7** | 85,0 → 100,0 → **87,7** | 130,5 → 130,5 → **27,7** | 38,5 → 27,1 → **19,5** | 0,24 → 0,24 → **0,76** | 0,24 → 0,00 → **0,20** |
| gorgoroth | 71,7 → 71,7 → **82,0** | 88,3 → 97,0 → **89,7** | 24,3 → 24,3 → **25,4** | 24,3 → 23,6 → **24,1** | 0,60 → 0,60 → **0,37** | 0,16 → 0,03 → **0,08** |

### Valores finales

```kotlin
ComboTuning.DEFAULT             // tacticsDazePct 50 -> 0 (inmunidad); lanza 16 / puño 21 / cura 35 sin cambio
ContentRules.BOSS_MODE_TUNING   // los 20 jefes salvo Bree (tabla del §2)
Bestiary.bosses                 // patrones de ataque nuevos en 18 jefes; eregion cada tick (tabla del §2)
RivalScaling.DEFAULT            // sin cambio (PV 115 / ataque 64)
RivalScaling.BOSS               // sin cambio (PV 180 / ataque 77)
```

- Offsets de jefe de +2 a +12 (la mitad en +2/+3; argonath +11 y amonhen +12).
- `final-encounters.csv` y `final-adventures.csv` regenerados.

### Pendiente para pruebas reales (Escudo táctico: inmunidad total)

1. **El MEDIO gana más que el MAESTRO en 3 jefes** (ithilien 93,0 frente a 89,0, anduin 91,7 / 90,7 y emynmuil 91,3 / 89,0) y casi lo iguala en moria (89,7 / 91,0); en los barridos había patrones con el MEDIO 30 puntos por encima. El modelo del MAESTRO se escuda con el Escudo táctico contra cualquier jefe que tenga un `31` y renuncia al −50 % de daño del Escudo maestro; con jefes de mucho daño y un solo Aturdir, un jugador real seguramente elegiría el Escudo maestro y ganaría más. Si se ajusta la política del MAESTRO (elegir el escudo según la proporción de Aturdir del patrón), habrá que re-balancear.
2. **Los jefes se parecen más entre sí:** 18 de 20 añaden una segunda cadena de daño y varios (eregion, caradhras, durin, sombrio, amonhen, cirithungol) acaban con un patrón casi igual (`a, aa, aaa, b, bb, 31, 123`). Si se quiere recuperar carácter, la palanca es cambiar la segunda familia de cada uno, sin tocar los dos primeros combos de daño.
3. **Menos defensa:** 16 de 20 jefes defienden una sola vez y al 10–15 % de vida (entradas en defensa del MAESTRO 0,90 por combate). La defensa es casi una «última resistencia».
4. **El MEDIO sufre más en cima (50 %), durin (50 %) y cirithungol (53 %)**, con 1,26, 0,82 y 0,76 muertes por aventura; el MAESTRO, que es la referencia, está en 81–88 % en esos tres.
5. **Las muertes del MAESTRO (3,94) están en el borde del objetivo:** con el ruido de 2000 aventuras pueden pasar de 4 en otra semilla.
6. Siguen abiertos los pendientes de «Jefes con modo ataque y defensa» (su punto 1, «menos defensas», queda aún más marcado) y los anteriores.
