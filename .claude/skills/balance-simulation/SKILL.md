---
name: balance-simulation
description: Use when tuning game numbers (damage, HP, costs, combo scaling, AI difficulty) or when asked whether a character/move/combo is overpowered or useless - runs headless simulated fights against the engine to get evidence instead of guessing.
---

# Simulación y balance

Como el motor es puro y determinista, se pueden simular miles de combates en segundos en la JVM, sin Android.

## Herramienta

- Vive en el source set de test o en un módulo `:sim` (JVM) que depende de `:engine`. Nunca en `:app`.
- Entrada: matchup (personaje A vs B), estrategia de cada lado (IA aleatoria, IA greedy, IA de "spam del mejor combo"), nº de partidas, semilla base.
- Salida (tabla en consola o CSV en `build/`): % victorias por lado, duración media en acciones/turnos, daño medio por combo, frecuencia de uso de cada combo, partidas que no terminan.

## Proceso

1. Antes de cambiar números, ejecuta la simulación y guarda la línea base.
2. Cambia **un** parámetro.
3. Re-simula con las mismas semillas y compara.
4. Señales de alarma: matchup > 60/40 entre personajes pensados como iguales; un combo con > 50% de uso (dominante); un combo con ~0% de uso (inútil); combates que no terminan.
5. Reporta al usuario la tabla antes/después; las decisiones de diseño son suyas.

## Reglas

- Las estrategias de IA usadas en simulación son las mismas funciones puras de `engine/ai`, para no medir algo distinto de lo que juega el usuario.
- Los números de balance viven en datos (ver `game-engine-logic`), así la simulación puede parametrizarlos sin recompilar reglas.
