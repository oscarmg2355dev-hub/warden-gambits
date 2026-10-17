# DESCRIPCIÓN FUNCIONAL DEL JUEGO WARDEN-GAMBITS
- Es un juego basado en el mundo del señor de los anillos, por lo que habrá que ambientar el luego como medieval-fantástico.
- El juego tendrá un único modo, modo história, donde se irán recorriendo el trazado con elecciones del personaje, tipo un libro de "elije tu propia aventura", donde se decidirá siempre entre dos caminos.
- El personaje es un warden, un humano con ganas de aventura, que parte desde una aldea de Bree, en busca de aventuras destino a Lorien.

## CAMINO O MAPA MODO HISTÓRIA
- La ruta de la aventura será un camino completo desde Bree, hasta Lorien.
- Tiene que pasar por: Bree → Bosque de Chet → Pantanos de Moscagua → Cima de los Vientos → Último Puente → Quebradas de los Trolls → Vado del Bruinen → Rivendel → Eregion → (Caradhras) → Puertas de Durin → Moria → Valle del Arroyo Sombrío → Nimrodel → Lothlórien
- Em cada fase anterior, habrá un arbol de 1-2-4-8-4-2-1 nodos, en cada una una breve história, y un combate o no, dependiendo del camino.
- Tienes que formar todo el camino, en cada fase, por ejemplo:
- Bree -> (1-2-4-8-4-2-1) -> Chet -> (1-2-4-8-4-2-1) -> Pantanos -> (1-2-4-8-4-2-1) -> Cima -> (1-2-4-8-4-2-1) -> Puente -> (1-2-4-8-4-2-1) -> ...etc
- Genera el arbol de nodos y la história de cada nodo.
- Cada nodo tendrá una imagen, pero no la hagas, te las proporcionaré yo, haz la história en texto y para cada nodo haz un prompt para poder describir la imagen que iría y ya la generaré yo.

## COMBATE
- El sistema de combate será a base de combos, que el jugador tendra que ir haciendo, al igual que los rivales, que tendrán IA para combatir, muy simple, siguiendo unos patronos que describiremos mas adelante.
- Dependiendo del nodo, el jugador tendrá que enfrentarse de entre 1 a 4 rivales.
- El timming del combaté será cada 1 segundo, que se aplicarán los efectos de los combos en rivales y jugador.
- No será por turnos, el combate será vivo, lanzando combos por ambas partes.
- Si hay varios rivales, el jugador podrá seleccionar al rival que está atacando, pero habrá combos AoE que afectarán a todos.
- También habrá combos de curación, Daze/Fear, Power y Multiplicadores de daño y cura.
- La pantalla de combate será primero una lista de rivales tipo cartas, con los stats de cada rival, luego la carta del jugador con sus stats y debajo los botones básicos de los combos, que serán SPEAR-SHIELD-FIST, que los definiremos internamente como 1-2-3.

## STATS
- Los personajes y los rivales, tendrán todos accesible la lista de combos.
- Todos tendrán los mismos estats:
    - Nivel (nivel de la entidad, afectará a los stats base y al daño/cura base)
    - Vida (vida de la entidad, a 0 se muere)
    - Power (power de la entidad, necesaria mínima para lanzar combo)
    - Ataque (poder de ataque base para todos los combos de daño, en cada ciclo)
    - Cura (cura base a aplicar a la entidad en cada ciclo)
    - Escudo Físico (resta al ataque de otra entidad, lo activa el combo de ShM)
    - Escudo Táctico (impide entrar en modo daze o fear, lo activa el combo de ShT)
    - Feared (estado en el que no puedes atacar)
    - Dazed (estado en el que no puedes atacar)
    - Defensa (resta de daño)

## COMBOS
- Siglas de los combos:
    - Dam: Daño
    - Cur: Cura
    - Mul: Multiplicador
    - ShM: Escudo físico, reduce 50% de daño, durante un tiempo
    - ShT: Escudo táctico, reduce 50% de que te pongas en Daze o Fear, durante un tiempo
    - Daz: Posibilidad de entrar en modo Daze (dormido, no ataca durante un tiempo)
    - Fea: Posibilidad de entrar en modo Fear (miedo, no ataca durante un tiempo)
    - DoT: Daño cada segundo por un tiempo
    - CuT: Cura cada segundo por un tiempo
    - AoE: Daño en área a todos los rivales
    - DoE: Drain, roba moral a todos los rivales en área para sumarsela.
    - Pot: Potenciador de daño
    - Cur: Potenciador de curas
    - Pow: Potenciador de Power 
    - Def: Potenciador de defensa
    - Rpo: Recupera power

- Te describo la lista de combos, con su descripción y efectos (cada combo gasta power)
    - 11 - Pot
    - 12 - Dam + DoT
    - 121 - Dam + DoT + Mul
    - 1212 - Dam + DoT + Mul
    - 12121 - Dam + DoT + Mul

    - 33 - Pow
    - 31 - Dam + DoT + Daz
    - 313 - Dam + DoT + Mul + Daz
    - 3131 - Dam + DoT + Mul + Daz
    - 31313 - Dam + DoT + Mul + Daz

    - 32 - Dam + DoT + AoE + Fea
    - 323 - Dam + DoT + AoE + Mul + Fea
    - 3232 - Dam + DoT + AoE + Mul + Fea
    - 32323 - Dam + DoT + AoE + Mul + Fea

    - 22 - Def
    - 21 - Cur + DoT
    - 212 - Cur + DoT + Mul
    - 2121 - Cur + DoT + Mul
    - 21212 - Cur + DoT + Mul

    - 23 - DoE
    - 232 - DoE + Mul
    - 2323 - DoE + Mul
    - 23232 - DoE + Mul

    - 31232 - Rpo
    - 2132 - ShM    
    - 2131 - ShT

- Decide tu inicialmente el daño, cura y duración de los efectos.

## MENÚ DEL JUEGO
- Modo História
    - Iniciará el modo história, con un mapa de en que zona está y las pantallas que le quedan en cada zona para pasar a la siguiente, tipo 2/7
    - Al pulsar en el mapa donde esté, se representará en otra pantalla el dibujo del nodo, y la decisión a tomar.
    - Dependiendo del nodo, si tiene que commbatir, se abrirá una ventana de combate con uno o varios rivales.
    - Si gana pasa al siguiente nodo.
    - Si pierde, se reiniciará todo el progreso y experiéncia del jugador, iniciando su aventura desde el inicio.

- Personaje
    - Mostrará los stats que tiene el juegaod y la experiéncia, junto con la zona actual de progreso, y el historial de partidas, y hasta donde ha llegado.

- Ayuda combos
    - Mostrará la ayuda de los combos, mostrando como se forman y los efectos que tienen.

## MOTOR DEL JUEGO
- El tiempo de cada tic del juego será de 1 segundo
- Los jugadores y rivales podrán ir pulsando los 3 botones de acción, 1-2-3, para formar los combos.
- Habrá un botón de acción, que encadenará la ejecución del combo en curso.
- Cada botón de acción no hará nada, solo hará la ejecución del combo almacenado.
- Cada segundo, el motor del juego tiene que aplicar los efectos inmediatos si hay combo ejecutado
- Cada segundo, el motor del juego tieme que aplicar los efectos que cada jugador tiene activos
- Los efectos pueden ser de todas los diferentes tipos, daño, cura, escudo...etc
- Todos los efectos se tienen que sumar en cada entidad y aplicarse cada sengundo, afectando a los stats.
- El combate acaba cuando el jugador deje sin vida a los rivales, o cuando el jugador muera.
- Si gana el jugador, se sumará nivel a sus stats
