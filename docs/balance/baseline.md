# Línea base de balance (2026-10-09)

Simulador: módulo `:sim` (`./gradlew :sim:run`). Motor, contenido y números **sin cambios**.
Datos completos: `baseline-encounters.csv` y `baseline-adventures.csv` (copias de `sim/build/reports/`).

## Método

- **Jugador simulado** (`HumanPlayer`): NOVATO decide cada 2 ticks, MEDIO cada 1,5 de media (1, 2, 4, 5, …), EXPERTO cada tick. Política fija por perfil (curar por debajo del 30 % / 40 %, combos de área con ≥2 rivales, `11` y luego el mejor daño asequible; EXPERTO guarda 26 de poder para curarse y usa `33`). Detalle en `sim/src/main/kotlin/com/wardengambits/sim/HumanPlayer.kt`.
- **Por encuentro**: 300 combates con semilla por cada nodo con encuentro (116) y perfil, con el jugador en el **nivel esperado** = 1 + victorias esperadas antes del nodo (todos los caminos igual de probables).
- **Aventuras**: 2000 partidas por perfil desde `newGame`, elecciones uniformes, `StoryEngine` real con subidas de nivel; la primera derrota termina la partida. Un combate de más de 600 ticks cuenta como derrota.

## Aventura completa

| Perfil | Completan | Objetivo | Combates al tope de 600 ticks |
|---|---|---|---|
| NOVATO | 0,0 % | ≈5 % | 0 |
| MEDIO | 0,0 % | ≈25 % | 285 |
| EXPERTO | 0,0 % | ≈60 % | 362 |

### % de partidas que llegan a cada zona (nivel medio al entrar)

| Zona | NOVATO | MEDIO | EXPERTO |
|---|---|---|---|
| bree | 100 % (L1) | 100 % (L1) | 100 % (L1) |
| chet | 6,0 % (L4,0) | 12,1 % (L4,3) | 9,6 % (L4,4) |
| moscagua | 0 % | 1,3 % (L8,1) | 1,6 % (L7,9) |
| cima | 0 % | 1,2 % (L11,2) | 1,5 % (L10,9) |
| puente | 0 % | 0,4 % (L13,6) | 0,4 % (L13,3) |
| quebradas … lothlorien | 0 % | 0 % | 0 % |

Nodos donde termina más partidas MEDIO: `bree_3_3` 25,7 %, `bree_3_1` 24,7 %, `bree_3_2` 14,8 %, `bree_7_0` 9,4 %, `bree_4_5` 5,3 %, `bree_6_1` 5,3 %, `chet_2_1` 4,1 %, `chet_7_0` 3,9 %.

## MEDIO por zona (combates sueltos al nivel esperado)

Objetivo: combates normales ≈99 %, jefes ≈90 %.

| Zona | Jefe % victoria | Normales: media % | Normales: mínimo % |
|---|---|---|---|
| bree | 100,0 | 48,9 | 0,0 |
| chet | 0,0 | 53,4 | 6,3 |
| moscagua | 100,0 | 82,0 | 38,0 |
| cima | 100,0 | 47,6 | 0,0 |
| puente | 0,0 | 42,6 | 0,0 |
| quebradas | 100,0 | 33,4 | 0,0 |
| bruinen | 100,0 | 36,0 | 0,0 |
| rivendel | — | — | — |
| eregion | 0,0 | 7,0 | 0,0 |
| caradhras | 100,0 | 6,1 | 0,0 |
| durin | 100,0 | 22,6 | 0,0 |
| moria | 100,0 | 37,5 | 0,0 |
| sombrio | 2,0 | 28,6 | 0,0 |
| nimrodel | 0,0 | 28,9 | 0,0 |

Por número de rivales (MEDIO, encuentros normales): 1 rival 82,0 % (38 nodos), 2 rivales 12,9 % (45), 3 rivales 4,4 % (19), 4 rivales 0 % (1); jefes 61,7 % (13). 46 de 103 encuentros normales se pierden siempre.

## Top 15 encuentros más letales (MEDIO)

Hay 46 encuentros con 0 %; esta es la lista ordenada por % de victoria y, en caso de empate, por orden de ruta.

| Nodo | Nivel jugador | Rivales | % victoria | Ticks medios |
|---|---|---|---|---|
| bree_3_3 | 1 | Bandido del foso L1 + Lobo hambriento L1 | 0,0 | 7,7 |
| bree_4_5 | 2 | Lobo hambriento L1 ×2 | 0,0 | 7,1 |
| chet_7_0 | 6 | Lobo gris alfa L6 + Lobo joven L2 | 0,0 | 7,6 |
| cima_3_0 | 10 | Lobo hambriento L9 ×2 | 0,0 | 6,7 |
| cima_3_2 | 11 | Lobo hambriento L8 ×3 | 0,0 | 6,0 |
| puente_4_0 | 13 | Huargo L12 ×2 | 0,0 | 6,1 |
| puente_4_4 | 14 | Huargo L11 ×3 | 0,0 | 4,6 |
| puente_4_7 | 14 | Espía dunlendino L12 + Huargo L12 | 0,0 | 9,0 |
| puente_6_1 | 15 | Huargo L12 ×2 | 0,0 | 6,5 |
| puente_7_0 | 15 | Jefe de la manada L15 + Huargo L11 | 0,0 | 6,0 |
| quebradas_3_1 | 16 | Orco de las Montañas L15 ×2 | 0,0 | 5,8 |
| quebradas_5_0 | 16 | Orco de las Montañas L15 + Huargo L15 | 0,0 | 5,8 |
| quebradas_5_2 | 17 | Orco de las Montañas L14 ×3 | 0,0 | 4,8 |
| quebradas_6_1 | 18 | Huargo L15 ×2 | 0,0 | 6,3 |
| bruinen_3_2 | 19 | Huargo L18 ×2 | 0,0 | 6,0 |

## Lectura rápida

- Hoy nadie termina la aventura: el 88 % de las partidas MEDIO muere ya en Bree.
- Los rivales que atacan **cada tick** con daño (lobos, huargos, lobos de Saruman, orcos de las Montañas) ganan a un jugador de su mismo nivel; los que atacan cada 2 ticks o abren con mejoras (bandidos, espías, moscardones, trolls, la mayoría de jefes en solitario) caen siempre.
- Con 2 o más rivales casi siempre se pierde (13 % con 2, 4 % con 3), aunque lleven offset −1/−2.
- Los jefes en solitario se ganan al 100 % (sus patrones empiezan con `11`/`2131`/`2132` y pierden el ritmo); los jefes acompañados se pierden al 0 %.
