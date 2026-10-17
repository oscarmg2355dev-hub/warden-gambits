---
name: combo-system
description: Use when implementing or modifying combo mechanics - input sequences, input buffering, combo matching, timing windows, chains/cancels, combo damage scaling, or combo definitions data.
---

# Sistema de combos

Los combos son el núcleo del juego. Todo esto vive en `:engine` (Kotlin puro, determinista — ver `game-engine-logic`).

## Conceptos

| Concepto | Definición |
|---|---|
| `Input` | Entrada atómica del jugador (p.ej. botón/símbolo/carta). `enum` o `sealed`. |
| `InputBuffer` | Últimas N entradas del jugador con su instante lógico. Inmutable. |
| `ComboDefinition` | Secuencia de `Input` + requisitos (recurso, estado, ventana) + efecto. Datos. |
| `ComboMatcher` | Dado el buffer, decide: combo completado, prefijo válido (sigue esperando) o roto. |
| `ComboResult` | Combo resuelto + eventos derivados (daño, efectos, escalado). |

## Matching

- Indexa las definiciones en un **trie** por secuencia de inputs: permite saber en O(longitud) si el buffer es prefijo de algún combo (para dar feedback "combo en curso" a la UI) y detectar el combo más largo que encaja.
- Política de solapamiento explícita y testeada: si `A B` y `A B C` existen, ¿se dispara `A B` inmediatamente o se espera? Documentarla en `docs/` y en el test.
- Si hay ventanas de tiempo: el instante viene en la `Action` (nunca reloj del sistema). Expiración = el reductor descarta el buffer cuando `now - last > window`.
- Si el juego es por turnos (sin tiempo real), el buffer se resetea por reglas de turno, no por tiempo. Elige una, no mezcles.

## Escalado y balance

- El daño de combo se calcula con una fórmula pura y aislada (`ComboScaling`) para poder testearla y ajustarla sin tocar el matcher.
- Escalado típico: daño de cada golpe × factor decreciente por posición en la cadena, con mínimo. Parametriza, no hardcodees.
- Cualquier cambio de números → re-ejecutar la simulación de `balance-simulation`.

## Datos

- Definiciones de combos como datos (`ComboDefinition` en Kotlin o JSON con `kotlinx.serialization`).
- Test de validación de datos: sin secuencias duplicadas, sin IDs duplicados, ningún combo inalcanzable (prefijo de otro que siempre se dispara antes, según la política elegida).

## Tests mínimos para cualquier cambio

- Secuencia exacta → combo.
- Prefijo → estado "en curso", sin efecto todavía.
- Input erróneo a mitad → combo roto/reiniciado según la regla.
- Solapamientos (combo corto vs largo).
- Expiración de ventana / fin de turno.
- Requisitos no cumplidos (recurso insuficiente, estado inválido) → `Rejected`.
