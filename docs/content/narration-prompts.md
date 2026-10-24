# Prompts de narración

Texto de los 507 nodos de la historia, en español e inglés, para grabar la narración con
**Google AI Studio → «Generate speech»** (Gemini TTS). Este fichero lo genera
`python tools/narration/build_prompts.py`; no lo edites a mano.

## Procedimiento

1. Abre <https://aistudio.google.com/> y elige **Generate speech** (voz de un solo hablante, «Single-speaker audio»).
2. Elige **una sola voz para todo el juego**. Recomendada: **Gacrux** (femenina, madura) o **Kore** (femenina, firme).
   Usa la misma en los dos idiomas.
3. Pega el **bloque de estilo** del idioma en el cuadro de estilo / instrucciones (o encima del texto) y, debajo,
   el texto de la entrada (el título y el cuerpo juntos, tal como aparecen).
4. Genera y escucha. Si no convence, vuelve a generar la misma entrada.
5. Descarga el audio como **WAV** y guárdalo con el nombre de la clave, `<clave>.wav`, en:
   - español: `docs/content/narration/es/<clave>.wav`
   - inglés: `docs/content/narration/en/<clave>.wav`

   Ejemplo: `docs/content/narration/es/bree_1_0.wav`. Si el navegador añade « (1)» al nombre, no pasa nada:
   se acepta, y si existen las dos versiones gana la que no lleva sufijo. También valen mp3, m4a, ogg y flac.
6. Ejecuta `python tools/narration/convert.py` (recorta silencios, normaliza y escribe AAC mono en
   `app/src/main/assets/narration/<es|en>/<clave>.m4a`). Lo que aún falta queda en `docs/content/narracion-pendientes.md`.

Claves: `<zona>_<nivel>_<índice>` y `orodruin_final` (507 por idioma).

## Bloque de estilo (español)

```
Narradora de fantasía épica, voz femenina grave y cálida. Ritmo lento y pausado, con suspense:
baja la voz y haz pausas dramáticas en los momentos de tensión; más aire en las descripciones.
Interpreta con convicción pero sin exagerar. Solo voz, sin música ni efectos de sonido.
```

## Style block (English)

```
Epic fantasy narrator, female voice, deep and warm. Slow, measured pace with suspense:
lower your voice and use dramatic pauses at tense moments; leave room to breathe in descriptions.
Perform with conviction but without overacting. Voice only, no music or sound effects.
```

# Español

<!-- lang:es -->

## bree

#### `bree_1_0`

Una puerta que se cierra.
Cierras la puerta de tu casa en Bree y la llave pesa más de lo que esperabas. Es otoño del año 3018 y el pueblo hierve de rumores: jinetes vestidos de negro que hacen preguntas a medianoche, un montaraz callado que ronda la posada, hobbits forasteros que llegaron con la lluvia. Llevas años mirando el Camino del Este desde tu ventana. Hoy has decidido seguirlo. Ajustas la espada que heredaste de tu padre, te echas el petate al hombro y te detienes en la calle embarrada. Antes de partir, aún puedes despedirte de Bree.

#### `bree_2_0`

El Poney Pisador.
El calor de la sala común te recibe con olor a cerveza y a leña húmeda. Hombres y hobbits hablan en voz baja, mirando de reojo la puerta. Dicen que anoche alguien entró por la fuerza en las habitaciones de los forasteros y que los caballos del establo huyeron espantados. En un rincón, un hombre alto y curtido fuma sin quitarse la capucha. El posadero, sudoroso y distraído, te sirve una jarra y te desea buen viaje sin escucharte. Comprendes que el miedo ha llegado a Bree antes de que tú salgas al Camino.

#### `bree_2_1`

La Colina de Bree.
Subes por la ladera mientras el pueblo despierta a tus pies. Desde lo alto de la Colina de Bree ves el Camino del Este alejarse entre setos y campos hasta perderse en una mancha oscura de árboles: el Bosque de Chet. Más allá, dicen, solo hay pantanos y colinas desiertas. El viento trae olor a lluvia y a humo. Junto a una piedra encuentras huellas de botas recientes y la ceniza de una fogata mal apagada. Alguien más vigila el Camino desde aquí, y no parece querer que lo vean.

#### `bree_3_0`

El callejón de atrás.
Sales por la puerta trasera de la posada y tomas un callejón estrecho entre muros de piedra. Apenas das veinte pasos cuando una sombra se despega de un portal. Un hombre de cara cetrina te cierra el paso con un cuchillo en la mano. Quiere tu bolsa y tu espada, y sonríe como quien ya lo ha hecho otras veces. Nadie va a asomarse a ayudarte. Tu primer combate no te espera en el Camino, sino a la vuelta de la esquina de tu propio pueblo.

#### `bree_3_1`

El patio de los establos.
En el patio de los establos reina el desorden: puertas reventadas, heno pisoteado y un mozo que lamenta los ponis perdidos. Mientras le ayudas a cerrar los portones, dos tipos aparecen tras el abrevadero con un saco al hombro. Se han aprovechado del caos de la noche para robar arreos y bridas. Al verte, sueltan el botín y desenvainan. No son jinetes negros, solo ladrones vulgares, pero sus hojas cortan igual y no piensan dejar testigos.

#### `bree_3_2`

Los huertos de Entrada.
Bajas la colina por el sudeste, hacia los huertos que separan Bree de la aldea de Entrada. Las gallinas han enmudecido y un perro ladra a lo lejos sin atreverse a salir. Entre los manzanos, un lobo flaco y gris olfatea los restos de un corral. El hambre lo ha empujado lejos del bosque, demasiado cerca de las casas. Te ve, enseña los dientes y no retrocede. Si no lo detienes ahora, esta noche serán los niños quienes lo encuentren.

#### `bree_3_3`

El foso del poniente.
Sigues el gran foso que rodea Bree, con su seto espeso en lo alto, buscando un paso hacia el Camino sin cruzar el pueblo. En la hondonada encuentras a un hombre andrajoso agachado junto a un lobo flaco al que arroja pedazos de carne, como quien ceba a un perro de presa. Al descubrirte, el lobo huye seto arriba con su pedazo de carne y el bandido, solo, empuña un garrote. Hay gente en Bree que ha aprendido a convivir con cosas peores que los lobos.

#### `bree_4_0`

La fragua del herrero.
Con la mano aún temblorosa, llegas a la fragua del herrero, que te conoce desde niño. Examina tu espada, chasquea la lengua al ver la mella del combate y la pone sobre el yunque. Mientras golpea, te cuenta que unos forasteros del sur llevan semanas comprando cuchillos y preguntando por los caminos del este. Te devuelve la hoja bien afilada y se niega a cobrarte. Solo te pide que, si algún día vuelves, le traigas noticias del mundo de ahí fuera.

#### `bree_4_1`

La guarida de los bribones.
El ratero llevaba una marca de tiza en la manga, y la misma marca aparece en la puerta de un cobertizo junto al muro sur. Empujas la hoja y dentro encuentras bolsas robadas, mantas y un mapa tosco del Camino con cruces en los tramos más solitarios. Dos hombres se levantan de un jergón, sorprendidos y furiosos. No son simples rateros: forman parte de una banda que aguarda a los viajeros más allá de los campos de Bree.

#### `bree_4_2`

El mozo de cuadra.
El mozo de cuadra, agradecido, te ofrece pan y queso en el pajar mientras recoge lo que dejaron los ladrones. Te cuenta en susurros lo que sintió de madrugada: sombras junto a la cerca, un frío que helaba la sangre y luego los animales desbocados por las calles. Dice que el montaraz salió esta mañana con los hobbits por el Camino, y que alguien les vendió un poni flaco a precio de oro. Le das las gracias y vuelves a cargar el petate.

#### `bree_4_3`

La casa de los Abrojal.
Antes de huir maltrechos, los ladrones gritaron un nombre: Abrojal. Su casa está junto al camino del sur, una vivienda de ventanas cerradas y fama turbia. Te acercas al seto y escuchas voces: un forastero del sur, de pelo negro, discute sobre jinetes y monedas con alguien que no ves. Una rama cruje bajo tu bota. La puerta se abre de golpe y sale un matón corpulento con una porra. Lo que se trama ahí dentro no es asunto tuyo, pero ya no puedes marcharte sin pelear.

#### `bree_4_4`

La cabaña del cazador.
En el linde de Entrada vive un cazador viejo que vio caer al lobo desde su porche. Te invita a pasar, aviva el fuego y te enseña a leer las huellas marcadas en el barro de su puerta. Dice que este otoño los lobos bajan más flacos y atrevidos que nunca, y que en el Bosque de Chet ha visto a uno enorme, gris como la ceniza, guiando a los demás. Te regala un cuerno de caza y un consejo: nunca acampes donde no puedas ver el cielo.

#### `bree_4_5`

Tras la manada.
El lobo no venía solo. En el barro del huerto hay más huellas, y las sigues campo a través hasta un soto de espinos donde huele a carroña. Allí, entre huesos de oveja, dos lobos levantan la cabeza a la vez. Uno se escabulle entre los espinos; el otro, el más flaco, está en los huesos y no va a dejar escapar a una presa que viene a su encuentro. El sol se esconde tras una nube. Sientes el pulso en las sienes, afirmas los pies en la tierra y alzas la espada.

#### `bree_4_6`

El guardián de la Puerta Sur.
Llegas a la Puerta Sur, por donde el Camino abandona Bree. El guardián, un hombre nervioso que no suelta la tranca, te mira de arriba abajo antes de abrir. Te cuenta que anoche algo pasó junto a su caseta, algo que no quiere describir, y que desde entonces no duerme. También te advierte de una banda de salteadores que vigila el Camino más allá de los campos, al mando de un tipo al que llaman el Tuerto. Te desea suerte como quien no espera volver a verte.

#### `bree_4_7`

Al otro lado del seto.
Para evitar la puerta y sus preguntas, trepas por el seto y saltas a los campos del otro lado. La hierba alta te llega a la cintura y te empapa la capa. A medio camino, un movimiento entre los cardos te hace detenerte: un lobo grande, de pelaje sucio, avanza a tu par sin hacer ruido. Cuando te giras, ya está demasiado cerca. Fuera de la protección de Bree, el mundo no espera a que estés preparado.

#### `bree_5_0`

El Camino del Este.
Por fin pisas el Camino del Este, ancho y gastado por siglos de carretas. A ambos lados se extienden granjas, cercas de piedra y bosquecillos de abedules que amarillean. Te cruzas con un buhonero que regresa a toda prisa hacia Bree y no se detiene a saludar. El sol de otoño calienta poco, pero cada paso te aleja de la vida que conoces. Por primera vez desde que cerraste tu puerta, sonríes: el viaje ha empezado de verdad.

#### `bree_5_1`

Las casas de Entrada.
Atraviesas Entrada, la aldea de las casas excavadas en la ladera, donde viven tantos hobbits como hombres. Una anciana te ofrece agua del pozo y un puñado de nueces para el camino. Los niños juegan a perseguir jinetes negros con palos por espadas, y sus madres los llaman a gritos para que entren. En la plaza, un carretero comenta que el Camino está más vacío que nunca. Llenas la cantimplora, agradeces la bondad y sigues hacia el este.

#### `bree_5_2`

Los tejados de Archet.
Tomas un sendero que sube hacia Archet, la aldea más cercana al bosque. Sus casas se apiñan entre robles y el aire huele a resina. En la taberna, unos leñadores hablan de un montaraz que se internó con varios hobbits en el Bosque de Chet, lejos del Camino, como si huyeran de algo. Otros juran que de noche se oyen cascos junto a los árboles. Comes deprisa, escuchas mucho, hablas poco y vuelves a ponerte en marcha.

#### `bree_5_3`

Una noche al raso.
Cae la tarde y decides acampar en una vaguada a pocos pasos del Camino. Enciendes un fuego pequeño, como te enseñaron, y cenas pan duro mirando las primeras estrellas. A lo lejos ladra un zorro. Más tarde, ya medio dormido, oyes cascos que pasan despacio por el Camino y se detienen. Contienes el aliento hasta que el sonido se aleja. Al amanecer no queda rastro alguno, salvo un frío extraño en la hierba escarchada.

#### `bree_6_0`

Los campos desiertos.
Avanzas por el Camino, donde las últimas granjas de las tierras de Bree dan paso a matorrales y brezos. En una curva encuentras un carro volcado, sin bueyes ni dueño, con una rueda aún girando al viento. Hay sangre seca en el pescante y huellas que se pierden hacia el bosque. En Bree ya se murmuraba que los salteadores vigilan este tramo. Aprietas el paso con la mano en la empuñadura, atento a cada sombra de los árboles cercanos.

#### `bree_6_1`

Atajo entre zarzales.
Abandonas el Camino para ahorrar tiempo y te internas en un laberinto de zarzas y brezo. El atajo resulta peor de lo que esperabas: las espinas te desgarran la capa y el suelo se hunde en hoyos ocultos. En un claro tropiezas con un vigía de los salteadores, que da la alarma con un silbido. Antes de que puedas huir, un lobo hambriento, atraído por el ruido, se suma a la refriega. Esta vez no hay muros de Bree a tu espalda.

#### `bree_7_0`

El cabecilla de los bandidos.
Donde el Camino roza los primeros árboles del Bosque de Chet, un tronco caído corta el paso. No es casualidad. De entre los helechos sale un hombre de anchas espaldas, con un parche de cuero sobre el ojo y un hacha mellada en la mano: el Tuerto, cabecilla de los salteadores. Viene solo: sus hombres se han quedado en el campamento, seguros de que no le hará falta ayuda. El Tuerto te ofrece dejar el petate y volver a Bree con vida. Tras él, la sombra del bosque espera. Solo hay una manera de seguir adelante.

## chet

#### `chet_1_0`

Bajo los árboles de Chet.
Con el Tuerto vencido, apartas el tronco y miras la espesura. El Camino sigue hacia el este, pero los rumores hablan de un montaraz que guió a unos hobbits por el bosque, lejos de ojos indiscretos, y decides seguir su ejemplo. El Bosque de Chet te traga en pocos pasos: robles retorcidos, helechos hasta la cintura y una penumbra verde que apaga los sonidos. Aquí y allá, troncos marcados y senderos estrechos delatan a los cazadores. De lejos llega un aullido largo, y otro le responde.

#### `chet_2_0`

La senda de los cazadores.
Eliges la senda más marcada, la que han abierto generaciones de cazadores de Archet. Es estrecha pero firme, y alguien ha tallado muescas en los troncos para no perderse. Caminas durante horas bajo las copas, escuchando el crujir de las ramas y el aleteo de los pájaros. En un recodo encuentras una cuerda con nudos atada a un árbol, señal de que el sendero se bifurca: un ramal baja hacia el humo de una chimenea; el otro, hacia los restos de un fuego viejo.

#### `chet_2_1`

La espesura.
Prefieres no seguir caminos que otros puedan vigilar y te abres paso por la espesura, apartando ramas y zarzas con la espada. Pronto pierdes el sol. El bosque se cierra a tu alrededor y un olor acre te pone en guardia: un lobo ha marcado aquí su territorio. Lo oyes antes de verlo, un gruñido bajo a tu izquierda. Una bestia de pelo pardo surge entre los helechos, más grande que cualquier perro de Bree y mucho más segura de sí misma.

#### `chet_3_0`

El campamento abandonado.
El ramal te lleva a un claro donde alguien acampó hace poco, quizá un día o dos. Las cenizas están frías, pero el suelo cuenta una historia: huellas de pies pequeños, descalzos y anchos, junto a las de unas botas largas y los cascos de un poni. Hobbits, sin duda, y el montaraz del que hablaban en Bree. Borraron su rastro con cuidado, aunque no lo bastante para un ojo atento. Junto a un tronco ves además otras marcas: botas pesadas que llegaron después.

#### `chet_3_1`

La cabaña del trampero.
El humo sale de una cabaña de troncos medio enterrada en la ladera. Te abre un trampero barbudo y desconfiado, con un arco a medio tensar. Cuando le dices que vienes de Bree, baja el arma y te deja calentarte junto a la lumbre. Te habla de lobos que ya no temen al fuego y de un macho gris enorme que los guía. Hace dos noches le mataron el perro. Te mira con ojos cansados y te pregunta si de verdad sabes usar esa espada.

#### `chet_3_2`

El arroyo escondido.
Siguiendo el rumor del agua llegas a un arroyo que corre entre piedras cubiertas de musgo. Te lavas la sangre y el sudor, bebes hasta saciarte y, por un momento, el bosque parece amable. Martines pescadores azules cruzan como flechas sobre la corriente. Río abajo, el agua se remansa en pozas oscuras; río arriba, unas piedras planas permiten pasar a la otra orilla. Te concedes un respiro antes de decidir por dónde seguir.

#### `chet_3_3`

El roble caído.
Un roble gigantesco yace derribado por alguna tormenta antigua, con las raíces al aire como una muralla. Trepas por el tronco hasta un punto desde el que ves por encima de los helechos. Al norte se alza una columna de humo gris; al este, los árboles parecen aclararse sobre un terreno más bajo y brumoso. Una ardilla te riñe desde una rama, ofendida por la visita. Respiras hondo: al menos ahora sabes hacia dónde se abre el bosque.

#### `chet_4_0`

Tras las huellas.
Sigues las huellas pequeñas durante una buena legua. Quien guía al grupo conoce el bosque: elige suelos de hojarasca, cruza arroyos para despistar y evita los claros. Pierdes el rastro dos veces y lo recuperas otras tantas. Al final comprendes que no debes alcanzarlos; sea cual sea su misión, prefieren no ser encontrados. Respetas su silencio, pero tomas nota del rumbo: noreste, hacia unas tierras bajas y húmedas.

#### `chet_4_1`

Los de las botas pesadas.
Las botas pesadas te llevan a una hondonada donde dos hombres asan un conejo. Son de la misma calaña que la banda del Tuerto: cuchillos largos, ropas robadas y una lista de nombres garabateada en un trozo de cuero. Hablan de cobrar una recompensa por noticias de los hobbits. No piensas ayudarles a ganarla. Al pisar una rama seca, ambos se vuelven hacia ti y se levantan, sonriendo como lobos.

#### `chet_4_2`

La trampa rota.
Ofreces tu ayuda al trampero y te pide que revises sus trampas mientras él remienda redes. En la tercera encuentras un lobo atrapado por la pata trasera, que se revuelve con furia. Antes de que decidas qué hacer, la cadena cede con un chasquido. La bestia, herida y loca de dolor, se lanza sobre ti sin pensarlo. Un animal acorralado es el más peligroso del bosque, te había advertido el viejo, y ahora entiendes por qué.

#### `chet_4_3`

Junto a la lumbre.
Aceptas pasar la noche bajo el techo del trampero. Cenáis un guiso de setas y carne seca mientras el viento sacude las copas. El viejo te habla del pantano que hay más allá del bosque: agua quieta, nieblas y unos moscardones que te comen vivo. También te enseña a preparar un ungüento de hierbas amargas para espantarlos. Duermes como no dormías desde Bree y, al alba, te despide con un pan bajo el brazo.

#### `chet_4_4`

Las piedras del vado.
Saltas de piedra en piedra hasta la otra orilla. Una de ellas se mueve bajo tu pie y casi caes al agua helada, pero recuperas el equilibrio a tiempo. En la ribera opuesta encuentras un viejo poste de madera con signos sencillos que no sabes leer y una flecha tallada que señala al este. Alguien, hace mucho, marcó aquí una ruta. Siguiendo su dirección, el bosque parece de pronto un poco menos hostil.

#### `chet_4_5`

Ojos en la orilla.
Sigues la corriente hasta las pozas oscuras, donde el bosque se inclina sobre el agua. El silencio es total; ni un pájaro canta. Entonces los ves: dos pares de ojos amarillos entre los juncos de la orilla, esperando a que la presa baje a beber. Has caminado justo hacia su emboscada. Los lobos salen del agua salpicando y te rodean, uno por cada lado, con el lomo erizado y los colmillos al aire.

#### `chet_4_6`

El fuego de los salteadores.
El humo te conduce a un campamento improvisado bajo un saliente de roca. Un solo hombre lo guarda, rodeado de fardos robados: un rezagado de la banda del Tuerto. Por su mirada, ya sabe que su jefe ha caído, y también quién lo hizo. Su cara pasa del susto a la rabia en un instante. Agarra una lanza corta apoyada en la roca y carga contra ti gritando venganza, sin pararse a pensar.

#### `chet_4_7`

Hacia las tierras bajas.
Bajas del roble y caminas hacia donde viste la bruma. El terreno desciende poco a poco y los robles dan paso a alisos y sauces de raíces húmedas. El aire se vuelve pesado y aparecen los primeros mosquitos. Bajo un sauce encuentras un pequeño montón de piedras apiladas, cubierto de musgo, con ofrendas de hace siglos. Te detienes un momento ante él, sin saber muy bien por qué, y luego sigues adelante.

#### `chet_5_0`

El claro de los aullidos.
Desembocas en un claro amplio, cubierto de hierba alta y rodeado de abedules blancos. A mitad de camino comprendes el error: aquí no hay dónde esconderse. Un aullido se eleva a tu espalda, otro a tu derecha, un tercero delante. Tres lobos salen de la linde y empiezan a cerrar el círculo, sin prisa, como quien conoce bien el oficio. Plantas los pies en el suelo y esperas al primero.

#### `chet_5_1`

La hondonada del peaje.
El sendero baja hacia una hondonada cruzada por un tronco a modo de barrera. Junto a él, un hombre con una ballesta oxidada y un sombrero ladeado exige peaje a quien pase: dos monedas o la vida. Asegura que el bosque es suyo desde que los cazadores de Archet dejaron de venir. No tienes intención de pagar a nadie por caminar entre árboles, y se lo dices. Él escupe al suelo y se lanza a por ti.

#### `chet_5_2`

El muro de zarzas.
Un muro de zarzas cierra el bosque y no hay forma de rodearlo. Te abres paso a golpes de espada, con las espinas arañándote la cara y las manos. Cuando por fin asomas al otro lado, sangrando y sin aliento, un lobo viejo y cubierto de cicatrices te espera sentado, como si supiera que vendrías por ahí. Se levanta despacio, sin un gruñido. Es un cazador paciente, y tú estás cansado.

#### `chet_5_3`

El puente de troncos.
Un torrente profundo corta el bosque, y el único paso es un puente de troncos atados con cuerdas. Cuando estás a medio cruzar, dos bandidos salen de la maleza, uno en cada extremo. El más cercano sostiene un hacha junto a las cuerdas y sonríe: si das un paso más, cortará. Te ordenan dejar las armas sobre los troncos. Miras el agua espumosa allá abajo, luego a tu enemigo, y decides moverte tú primero.

#### `chet_6_0`

Un refugio de montaraces.
Cae la noche y encuentras un refugio excavado bajo las raíces de una haya, oculto tras una cortina de hiedra. Dentro hay leña seca, un cuenco de barro y una estrella sencilla grabada en la piedra. Los montaraces del norte usan estos escondrijos para recorrer las tierras salvajes sin ser vistos. Duermes al abrigo, escuchando aullidos que se acercan y se alejan. Antes del alba, uno suena muy cerca.

#### `chet_6_1`

Marcas en las cortezas.
En los troncos de esta parte del bosque aparecen marcas profundas de garras, a una altura que te hiela la sangre. Algún lobo enorme ha estado afilándolas aquí, marcando un territorio que se extiende hacia el este. Huesos roídos y mechones de pelo gris salpican el suelo. Los aullidos que te acompañan desde hace días parecen venir ahora de la misma dirección, como si el bosque entero te empujara hacia su guarida.

#### `chet_7_0`

El lobo gris alfa.
Al final del bosque, donde los árboles se vuelven ralos y el suelo huele ya a ciénaga, te espera la manada. En lo alto de una roca, el lobo gris alfa te observa: más alto que un poni, con el pelaje color ceniza y una cicatriz que le parte el hocico. Es el que guía a los demás, el terror de los cazadores de Archet. Con un gruñido aparta a los lobos jóvenes: esta presa es suya. Salta de la roca y empieza a rodearte despacio. Más allá, entre la bruma, comienzan los pantanos.

## moscagua

#### `moscagua_1_0`

Los Pantanos de Moscagua.
Con el alfa abatido, el bosque te deja marchar. Ante ti se extienden los Pantanos de Moscagua: un mar de juncos, aguas pardas y bancos de niebla que se mueven como rebaños. Cada paso se hunde en el fango y el aire vibra con el zumbido de incontables insectos que buscan tu piel. Aun así, el barro guarda huellas recientes: pies pequeños, unas botas largas, un poni. Y otras más pesadas, que no te gustan nada, siguiendo a las primeras.

#### `moscagua_2_0`

Entre los juncos.
Te metes entre los juncos, que te superan en altura, buscando el camino más directo. El agua te llega a las rodillas y está tibia, espesa y maloliente. De pronto, el zumbido se vuelve un rugido: una nube de moscardones se levanta de la superficie como humo negro y se lanza sobre ti. Pican, se meten en los ojos y en la boca, y no hay forma de huir con el agua por las rodillas. Tienes que hacerles frente.

#### `moscagua_2_1`

Huellas en el barro.
Decides seguir las huellas, que buscan siempre los bancos de tierra más firmes. Pronto descubres que no eres el único: un hombre de pelo negro y piel curtida camina agachado delante de ti, estudiando las mismas marcas. Viste ropas de las tierras del sur y lleva un cuchillo curvo al cinto. Cuando se da cuenta de tu presencia, no pregunta nada. Su mano vuela hacia el arma, y entiendes que un testigo es lo último que desea.

#### `moscagua_3_0`

La isleta de los sauces.
Escapas del agua hacia una isleta donde crecen tres sauces retorcidos. La tierra es firme y seca, un pequeño milagro en medio del pantano. Te sacudes los insectos, te untas la cara con barro para protegerte y comes algo de lo poco que te queda. Desde aquí oyes un zumbido grave y constante que viene del norte, como si algo enorme respirara bajo el agua. Prefieres no pensar demasiado en ello.

#### `moscagua_3_1`

El agua negra.
Te arriesgas a cruzar una laguna de agua negra que acorta el camino. A mitad de travesía el fondo desaparece bajo tus pies y el fango te atrapa hasta la cintura. Cuanto más te mueves, más te hundes. Con la calma que te queda, te inclinas hacia atrás, alcanzas una raíz con la punta de la espada y tiras de ella poco a poco. Sales cubierto de lodo, temblando y vivo, con una lección bien aprendida.

#### `moscagua_3_2`

El campamento del espía.
Siguiendo hacia atrás las huellas del espía, encuentras su campamento: una manta, un odre y un hatillo bien atado. Dentro hay pan negro, un puñado de monedas de plata y un retazo de tela con una mano blanca pintada. No sabes qué significa, pero tiene el tacto de algo importante. También hay un mapa con el Camino trazado y varias cruces junto a una colina alta, al este. Guardas el retazo y sigues adelante.

#### `moscagua_3_3`

La luz del pantano.
Al caer la tarde ves una luz pálida que flota sobre el agua, a poca distancia. Piensas que puede ser otro viajero con una linterna y la sigues, chapoteando. Pero la luz se aleja cada vez que te acercas: danza, se apaga y vuelve a encenderse más allá. Cuando por fin te detienes, no sabes dónde estás. A tu alrededor solo hay niebla, agua quieta y el croar de las ranas, que de repente calla.

#### `moscagua_4_0`

Noche en la isleta.
Decides pasar la noche en la isleta. No te atreves a encender fuego, así que te envuelves en la capa húmeda y escuchas el pantano: chapoteos, el gemido del viento entre los juncos y, muy lejos, hacia el este, destellos que iluminan las nubes sobre unas colinas, como relámpagos sin trueno. Te preguntas si serán tormentas o algo peor. Al amanecer, la niebla lo ha borrado todo y el camino sigue esperándote.

#### `moscagua_4_1`

El nido del zumbido.
Sigues el zumbido grave hasta un tocón hueco que se alza en mitad del agua. A su alrededor, el aire es una masa viva de insectos, y en su interior palpita algo parecido a un panal de barro. Al acercarte, cuatro enjambres se separan de la nube principal y se arrojan sobre ti desde distintos lados. Comprendes que este nido es solo uno de muchos, y que en algún lugar del pantano debe de haber uno mucho mayor.

#### `moscagua_4_2`

La pasarela hundida.
Recuperado del susto, exploras la orilla y encuentras los restos de una pasarela de madera, medio hundida pero todavía útil. Alguien la construyó hace siglos, cuando quizá estas tierras estaban habitadas. Avanzas por ella con cuidado, comprobando cada tabla. En un poste descubres una talla antigua, gastada por el agua: una corona rodeada de estrellas. Recuerdas que en las historias de Bree se hablaba de un reino perdido del norte.

#### `moscagua_4_3`

Ojos entre la niebla.
Mientras te limpias el lodo, oyes voces en una lengua áspera que no entiendes. Te agazapas en la niebla y ves a dos hombres morenos que rebuscan en el barro y señalan al este. Son espías, sin duda, gente del sur que nada bueno busca tan al norte. Uno de ellos olfatea el aire como un perro y se vuelve exactamente hacia tu escondite. No hay tiempo para huir con las piernas entumecidas por el frío del fango.

#### `moscagua_4_4`

El regreso del compañero.
Te escondes junto al campamento y esperas. Al anochecer llega otro hombre, compañero del primero, que maldice al ver el hatillo revuelto. Busca alrededor con una antorcha, y la luz despierta dos enjambres de moscardones dormidos en los juncos. El espía te descubre en el mismo instante en que la nube zumbante os envuelve a los dos. En el caos, él decide atacar, y los insectos no distinguen entre bandos.

#### `moscagua_4_5`

La colina de las cruces.
Estudias el mapa del espía a la luz del atardecer. Las cruces señalan una colina alta al este, junto al Camino, acompañadas de unas palabras en una lengua que no conoces. Fuera lo que fuese lo que buscaban, lo buscaban allí. Orientas tus pasos en esa dirección, eligiendo los bancos de juncos más firmes. Cuando la niebla se abre un instante, ves a lo lejos una cima pelada, coronada por un anillo de piedras.

#### `moscagua_4_6`

El ahogado del pantano.
La luz vuelve a encenderse justo delante y, esta vez, se queda quieta. Te acercas y descubres que brilla sobre un tronco podrido donde yace un esqueleto con restos de armadura, atrapado en el fango desde hace mucho. Al rozarlo, el tronco se parte y libera un enjambre furioso que dormía en su interior, mayor y más agresivo que ninguno de los que has visto. La luz se apaga y solo queda el zumbido.

#### `moscagua_4_7`

Destellos en las colinas.
Entre la niebla distingues un montículo de tierra y piedras que sobresale del agua. Subes a él para orientarte. Desde allí, por fin, ves algo más que juncos: hacia el este, una línea de colinas oscuras recorta el cielo, y sobre la más alta parpadean destellos blancos, como chispas de una tormenta lejana. Algo ocurre en esas cumbres. Esperas a que cesen y, con las estrellas como guía, fijas tu rumbo hacia ellas.

#### `moscagua_5_0`

Tierra más firme.
Poco a poco, el suelo se endurece bajo tus botas. Aparecen matas de brezo y alguna roca gris asomando entre el musgo. No es el final del pantano, pero sí un respiro: puedes caminar sin hundirte y secar la ropa al viento. Te sientas en una piedra y cuentas tus provisiones, más escasas de lo que quisieras. A lo lejos, las colinas se ven algo más cerca que ayer. Aún queda mucho barro por delante.

#### `moscagua_5_1`

El manantial escondido.
Después de días bebiendo agua turbia, la encuentras casi por casualidad: un manantial que brota entre piedras blancas, limpio y helado. Bebes hasta que te duelen los dientes y llenas la cantimplora. Alrededor crecen berros y menta silvestre, y los moscardones, extrañamente, no se acercan. Te lavas las picaduras, que arden menos, y descansas un rato escuchando el agua. Pocas veces algo tan pequeño te ha parecido un regalo tan grande.

#### `moscagua_5_2`

Un campamento reciente.
Encuentras un lugar donde alguien acampó hace muy poco: hierba aplastada para cuatro cuerpos pequeños y uno grande, ramas cortadas con cuidado y ni una brasa a la vista. Son los hobbits y su guía, seguro. Alguno debió de quejarse mucho de los mosquitos, porque en un rincón hay un montón de hierbas aromáticas chamuscadas. Sonríes sin querer. Al menos, de momento, nadie los ha alcanzado aquí.

#### `moscagua_5_3`

El cañaveral.
Te internas en un cañaveral tan alto y tan denso que apenas ves el cielo. Las cañas crujen y susurran con cada soplo de viento, y por momentos crees oír pasos a tu lado. Avanzas marcando las cañas con la espada para no caminar en círculos. Cuando por fin sales al otro lado, descubres que el rumbo es el correcto: el terreno sube suavemente y la niebla empieza a quedarse a tu espalda.

#### `moscagua_6_0`

La orilla del este.
Eliges el camino largo y bordeas la ciénaga por su orilla oriental, donde los juncos ralean y aparecen los primeros espinos. Tardas un día más, pero llegas entero. Desde un pequeño alto contemplas el último tramo de pantano: una laguna ancha rodeada de niebla, de la que sube un zumbido tan potente que hace vibrar el aire. Todos los enjambres parecen volver allí, como abejas a su colmena.

#### `moscagua_6_1`

El corazón de la ciénaga.
Atajas por el centro de la ciénaga, donde el agua es más profunda y la niebla más espesa. Es un error. Un espía dunlendino te ha seguido durante horas, esperando su oportunidad, y la encuentra cuando un enjambre te rodea en un paso estrecho. El hombre sale de los juncos con el cuchillo en alto mientras los insectos te ciegan. Peleas con el barro hasta el pecho y la certeza de que nadie vendrá a buscarte.

#### `moscagua_7_0`

La reina del enjambre.
La laguna del centro del pantano hierve de insectos. Sobre un islote de barro y huesos se agita la reina del enjambre: una criatura alada del tamaño de un perro, de ojos rojos y aguijón goteante, nacida del agua estancada y de siglos de olvido. Hasta los mosquitos se apartan de ella. Te ha sentido llegar, y despliega las alas con un zumbido que hace temblar los juncos de la orilla. Más allá de la laguna, la tierra sube hacia colinas peladas y una cima coronada de piedras. Para alcanzarla, primero tienes que cruzar su reino.

## cima

#### `cima_1_0`

Bajo la Cima de los Vientos.
Dejas atrás el pantano con el zumbido de la reina todavía en los oídos. El suelo se vuelve firme y pedregoso, y una cadena de colinas peladas se alza ante ti. La más alta, al sur, termina en un anillo de piedras rotas: la Cima de los Vientos, la Amon Sûl de las canciones de Bree, donde dicen que hubo una gran torre de vigía. El viento sopla helado y trae olor a ceniza. Al norte, una senda medio borrada recorre las lomas; al sur, el Camino del Este rodea las colinas. Ajustas el petate y eliges.

#### `cima_2_0`

La senda de las lomas.
Subes a la senda que serpentea por el lomo de las colinas. Es antigua: a trechos la bordean piedras talladas y restos de muros que alguien levantó hace siglos para defender estas alturas. Desde aquí lo ves todo y nadie te ve a ti. En el barro de un recodo descubres huellas recientes, varias pequeñas y unas largas de bota, todas en dirección a la cima. Los viajeros cuyo rastro sigues pasaron por aquí hace pocos días. Vas tarde, pero vas bien.

#### `cima_2_1`

Un viajero demasiado amable.
Bajas al Camino, ancho y despejado, que rodea las colinas por el sur. Avanzas deprisa, pero aquí cualquiera puede verte. Al poco, un hombre de pelo oscuro sale de entre unos espinos y te saluda como un viajero más. Habla demasiado y mira demasiado tu espada. Cuando le das la espalda para seguir, oyes el roce de un cuchillo saliendo de su vaina. Es un dunlendino, un hombre de las Tierras Brunas, y no está aquí por casualidad.

#### `cima_3_0`

Las murallas caídas.
La senda atraviesa los restos de un fuerte: torres desmochadas, muros cubiertos de líquenes, un patio donde crece la ortiga. Al pasar bajo un arco oyes un gruñido. Dos lobos flacos, con las costillas marcadas, salen de una grieta del muro donde han hecho su cubil. El hambre les ha quitado el miedo a los hombres. Se separan para atacarte por ambos lados, y tú buscas una pared a tu espalda antes de que salten.

#### `cima_3_1`

El explorador del arroyo.
Bajas a una hondonada donde un arroyo corre entre zarzas, buscando agua y abrigo del viento. Alguien ha tenido la misma idea: junto a la orilla, un hombre de las Tierras Brunas limpia un arco y repasa unas marcas en un trozo de corteza. Es un explorador veterano, con cicatrices en los brazos. Te ve antes de que puedas retroceder y no hace preguntas. Deja la corteza, desenvaina un hacha corta y viene hacia ti.

#### `cima_3_2`

El rastro de los cascos.
En el Camino encuentras huellas de cascos, grandes y profundas, de caballos pesados que galoparon hacia el este. No eres el único que las sigue: una manada de lobos husmea el mismo rastro, como si esos jinetes dejaran tras de sí una promesa de carroña. Cuatro de ellos levantan la cabeza al olerte. Uno gruñe; los otros se abren en abanico por la cuneta. Comprendes que hoy la presa vas a ser tú.

#### `cima_3_3`

La vieja atalaya.
Junto al Camino se levanta una atalaya en ruinas, poco más que un muñón de piedra. Desde lejos ves un hilo de humo: alguien la usa de refugio. Te acercas con sigilo y oyes a tres hombres discutir en una lengua áspera mientras vigilan el Camino. Hablan de jinetes, de colinas y de un pago que no llega. Uno de ellos sale a estirar las piernas y tropieza contigo. Su grito avisa a los otros, y los tres echan mano a las armas.

#### `cima_4_0`

La losa de la estrella.
Cuando el último lobo cae, recorres el fuerte en silencio. En el patio, bajo las ortigas, asoma una losa tallada con una estrella de muchas puntas, gastada por siglos de lluvia. Este lugar perteneció a un reino que ya nadie nombra en Bree, y aun así alguien lo recuerda: junto a la losa hay ramas secas apiladas con cuidado, como dejadas para quien venga detrás. Tomas solo las necesarias para una noche y das las gracias en voz baja.

#### `cima_4_1`

Un cuervo solitario.
Prefieres no quedarte entre las ruinas y sigues la senda hacia el sur. El viento arrecia y te obliga a caminar encorvado. Por la tarde, desde una loma, ves la Cima de los Vientos mucho más cerca: una colina redonda y pelada, con su corona de piedras. Sobre ella vuela un cuervo, solo uno, en círculos lentos. Hay algo en esa colina que te encoge el corazón, como si el aire allí pesara más. Aun así, sigues adelante.

#### `cima_4_2`

La corteza marcada.
Recoges la corteza que dejó el explorador. Lleva marcas toscas: una colina con un círculo, unas rayas que podrían ser días y cinco figuras, cuatro pequeñas y una alta. Alguien ha estado vigilando a los viajeros que sigues y contando sus pasos. Te guardas la corteza en el jubón. Si los espías saben tanto, otros enemigos peores pueden saberlo también, y la colina ya no te parece un refugio, sino una trampa.

#### `cima_4_3`

Agua y silencio.
Te lavas las manos en el arroyo y te sientas entre las zarzas a recuperar el aliento. El agua corre fría y clara, y el viento no llega hasta el fondo de la hondonada. Por primera vez en días, comes caliente: un poco de tocino tostado sobre una llama diminuta que apagas enseguida. Mientras masticas, ves en el barro de la orilla las huellas de un poni cargado. Los viajeros a los que sigues también bebieron aquí.

#### `cima_4_4`

Donde acaban las huellas.
Sigues las huellas de cascos hasta que se apartan del Camino y se pierden en un pedregal, al pie de las colinas. Allí la hierba está quemada en círculos, y el aire conserva un frío que no es del otoño. Los jinetes se separaron en varias direcciones, como si quisieran rodear algo. Sientes un escalofrío que nace en la nuca. No sabes qué buscaban, pero estás seguro de que todos miraban hacia la cima.

#### `cima_4_5`

El hito del Camino.
Herido por los mordiscos, buscas una fuente que brota junto a un viejo hito de piedra al borde del Camino. Te lavas las heridas y las vendas con tiras de la camisa. Mientras lo haces, ves en el hito unas letras casi borradas y, debajo, una señal reciente rayada con la punta de un cuchillo: una flecha que apunta a las colinas. Es una marca de montaraz, piensas. No es para ti, pero te dice hacia dónde ir.

#### `cima_4_6`

Lo que vigilaban.
Registras la atalaya. Los espías tenían provisiones para muchos días, una manta de buena lana que no es suya y un saco de monedas de plata acuñadas lejos de aquí. Alguien les paga bien por vigilar. En la pared han dibujado con carbón el perfil de la Cima de los Vientos y, junto a ella, una mano blanca. No sabes qué significa ese signo, pero lo grabas en tu memoria. Te quedas con la manta: las noches aquí son crueles.

#### `cima_4_7`

Una sombra en la corona.
Te alejas de la atalaya y buscas un repecho desde el que se domine la colina. Te tumbas entre los brezos y esperas a que caiga la tarde. En la cumbre no se mueve nadie, pero el viento trae a ratos un olor a madera quemada y algo más, agrio, que no sabes nombrar. Cuando la luz se apaga, te parece ver una sombra más negra que la noche deslizarse entre las piedras de la corona. Parpadeas, y ya no está.

#### `cima_5_0`

Al abrigo de las rocas.
El viento sopla tan fuerte que apenas puedes avanzar. Te refugias en un hueco entre grandes rocas, al pie de las colinas, y esperas a que amaine. Desde allí estudias la ladera: hay varias formas de subir y ninguna es fácil. Te calientas las manos con el aliento, repasas el filo de la espada y comes los últimos frutos secos. Cuando por fin el viento cede, la Cima de los Vientos está más cerca que nunca, oscura contra un cielo de nubes rasgadas.

#### `cima_5_1`

El brezal abierto.
Cortas camino por un brezal abierto que sube hacia la colina. No hay donde esconderse, y lo sabes. A medio camino, una figura se levanta entre los brezos donde llevaba horas tumbada: un rastreador de las Tierras Brunas, con la cara manchada de barro y una lanza corta. Te estaba esperando. No grita ni pide ayuda; quiere encargarse de ti él solo, y por la forma en que se mueve, sabe hacerlo.

#### `cima_5_2`

La fila de piedras.
Encuentras una fila de piedras hincadas que sube por la ladera, restos de un muro o de una calzada muy antigua. Las sigues, y te llevan por un paso oculto que nadie adivinaría desde abajo. En algunas el musgo está raspado por botas recientes. Te reconforta pensar que por aquí camina gente que conoce estas colinas mejor que tú. Subes despacio, sin ruido, y cuando miras atrás, el mundo entero parece pequeño y gris.

#### `cima_5_3`

La ladera oeste.
Subes por la ladera oeste, la más empinada, agarrándote a las matas y a las rocas sueltas. A media altura, un hedor espeso te avisa demasiado tarde: has llegado a la boca de una madriguera. Tres lobos salen de ella, flacos y furiosos, y te cierran el paso por arriba y por los lados. Debajo solo queda la pendiente. No puedes huir sin despeñarte, así que afirmas los pies en la roca y levantas la espada.

#### `cima_6_0`

El campamento abandonado.
Te asomas a una hondonada abrigada en la cara oeste de la colina, bajo la cumbre, y bajas a ella con cautela. Hay un manantial, leña apilada y los restos de una hoguera grande. La hierba está pisoteada y quemada a trechos, hay ramas partidas por todas partes y, en el suelo, marcas de lucha que no sabes leer. Huellas pequeñas y un rastro de poni salen del claro hacia el sur, apresuradas. Quienes acamparon aquí se fueron con prisa, y algo los obligó a hacerlo.

#### `cima_6_1`

La corona de piedras.
Subes el último tramo hasta la cumbre. Del gran torreón de Amon Sûl solo queda un anillo de piedras rotas cubiertas de hierba áspera. El suelo está ennegrecido, y algunas rocas parecen fundidas por un calor terrible, aunque el aire es helado. Algo ardió aquí hace muy pocos días. Sobre un montón de piedras descubres unos arañazos recientes, como letras, que no entiendes. Abajo se extienden tierras vacías bajo un cielo de plomo.

#### `cima_7_0`

El espectro de la cima.
Cae la noche sobre la colina y el viento se detiene de golpe. En el silencio, el frío te muerde los huesos y una angustia sin nombre te aprieta el pecho. Desde donde estás ves aparecer, entre las piedras de la corona, una figura alta envuelta en un manto negro, sin rostro bajo la capucha; solo un brillo pálido donde deberían estar los ojos. Viene hacia ti sin prisa. Es uno de los jinetes negros de los que hablaban en Bree, que ha vuelto a la colina como quien busca un rastro perdido. No es un enemigo al que puedas matar. Solo te queda resistir, con acero y fuego, hasta que la sombra se retire.

## puente

#### `puente_1_0`

Las Tierras Solitarias.
Cuando el alba asoma, la sombra se ha ido y tú sigues en pie, temblando, con la espada mellada y el alma helada. Dejas atrás la Cima de los Vientos sin volver la vista. Hacia el este se extienden las Tierras Solitarias: lomas pardas, brezo marchito, bosquecillos raquíticos y ni una casa en leguas. El Camino las cruza hacia el río Fontegrís y el Último Puente. De noche, desde muy lejos, llegan aullidos largos que no son de lobo corriente. Son huargos, y están cazando.

#### `puente_2_0`

Leguas de Camino.
Sigues el Camino, que al menos es firme y recto. Avanzas deprisa, durmiendo poco y mal, siempre con un ojo en el horizonte. El paisaje no cambia: lomas, piedras, viento. A ratos el Camino pasa junto a murallas derruidas o bajo colinas coronadas de ruinas, recuerdos de un tiempo en que esta tierra tuvo reyes. Ahora solo la habitan los cuervos. Al segundo día, el cielo se cierra y el aire se carga de lluvia.

#### `puente_2_1`

El explorador gris.
Abandonas el Camino y te internas en las tierras salvajes del sur, donde es más fácil pasar inadvertido. El terreno es áspero, lleno de barrancos y matorrales. Al segundo atardecer, un huargo solitario te sale al paso desde un bosquecillo: enorme, de pelaje gris sucio y ojos amarillos. No es un lobo hambriento cualquiera; es un explorador de su manada, y si escapa, traerá a los demás. No puedes dejar que se vaya.

#### `puente_3_0`

La torre caída.
La tormenta te sorprende en campo abierto y corres hacia la única sombra que ves: una torre en ruinas sobre un otero junto al Camino. Dentro, el suelo está seco y alguien dejó hace tiempo un montón de leña bajo una escalera derrumbada. Enciendes un fuego pequeño, el primero en días. Fuera, la lluvia azota las piedras. Desde la tronera ves el Camino brillante de agua y oyes, entre trueno y trueno, un aullido lejano.

#### `puente_3_1`

El enano del oeste.
Te cruzas con un viajero que camina hacia el oeste con un fardo enorme: un enano de barba trenzada y hacha al cinto, que vuelve a las Montañas Azules. Desconfía de ti hasta que compartes tu tabaco. Entonces habla: de los huargos que rondan el río, más numerosos que nunca, y de un jefe de manada grande como un poni que los guía. Te aconseja cruzar el puente de día y no fiarte de nadie que pregunte demasiado.

#### `puente_3_2`

Huellas pequeñas.
En un barranco arenoso das con un rastro que reconoces: pies pequeños y descalzos, botas de hombre y cascos de poni. Los viajeros pasaron por aquí, despacio, como si alguno no pudiera caminar deprisa. Los sigues un buen trecho entre zarzas y piedras. El rastro gira poco a poco hacia el norte, de vuelta al Camino, y comprendes que ellos también se dirigen al puente. Quizá no estés tan lejos como creías.

#### `puente_3_3`

El barranco de las zarzas.
Para evitar una loma pelada donde se te vería desde leguas, bajas a un barranco estrecho, cubierto de zarzas y espinos. El fondo es un laberinto de rocas caídas y regatos helados. Avanzas despacio, con la capa desgarrada, y por la tarde encuentras algo que te hiela la sangre: los restos de un ciervo devorado hace muy poco y, alrededor, huellas de zarpas del tamaño de tu mano abierta. Estás en territorio de huargos.

#### `puente_4_0`

Asedio en la torre.
Decides pasar la noche en la torre. Hacia la medianoche, el fuego se ha reducido a brasas y despiertas sobresaltado: algo araña la piedra fuera. Tres huargos han olido el humo y rondan la entrada, gruñendo en voz baja, como si hablaran entre ellos. Uno se cuela por la puerta derrumbada mientras los otros buscan la tronera. Agarras un tizón con la mano izquierda y la espada con la derecha. La noche va a ser larga.

#### `puente_4_1`

El hombre de la curva.
No quieres perder tiempo y sigues bajo la lluvia. En una curva del Camino, un hombre encapuchado espera sentado en una piedra, como si el aguacero no le importara. Te pregunta si vienes de las colinas y si has visto a unos medianos. Habla con el acento de las Tierras Brunas, y su mano no se aparta del puñal. Cuando respondes que no, sonríe con dientes torcidos y se levanta. De un modo u otro, piensa sacarte lo que sabes.

#### `puente_4_2`

Noche junto al enano.
Aceptas compartir el campamento del enano esa noche, al abrigo de unas rocas. Ronca como un fuelle, pero duerme con el hacha en la mano, y hace bien: antes del alba, los huargos atacan. Él se planta contra dos, maldiciendo en su lengua, y te grita que te ocupes del tercero, el más grande, que viene derecho hacia ti con las fauces abiertas. No hay tiempo para pensar ni para tener miedo.

#### `puente_4_3`

Los que preguntan.
Te despides del enano y sigues solo hacia el este a buen paso. Al mediodía oyes cascos detrás de ti: dos jinetes en caballos flacos te alcanzan y te rodean. Son hombres de las Tierras Brunas, y preguntan por un enano con mucho equipaje y por unos medianos que viajan con un montaraz. Les contestas que no has visto a nadie. No te creen. Desmontan sin prisa y desenvainan las espadas.

#### `puente_4_4`

Tras el mismo olor.
Sigues el rastro de los viajeros sin perderlo, aunque se borra a ratos entre las piedras. No eres el único: otras huellas, enormes, lo cruzan una y otra vez. Al atardecer, en un claro de abedules, cuatro huargos alzan el hocico del suelo que olfateaban. Iban tras el mismo olor que tú, y te miran como a un estorbo que se puede comer. Se separan sin prisa para cercarte. A tu espalda, solo hay un tronco caído.

#### `puente_4_5`

El cuerno del vigía.
Dejas el rastro y vuelves hacia el Camino, buscando terreno más llano. En lo alto de una loma ves a un hombre agazapado que vigila la calzada: un espía dunlendino con un cuerno colgado al cuello. Si lo hace sonar, cualquiera que esté cerca sabrá que vienes. Te acercas por detrás, despacio, pero una piedra se mueve bajo tu bota. El hombre se gira, se lleva el cuerno a los labios y, al ver tu espada, desenvaina.

#### `puente_4_6`

La guarida.
Sigues el fondo del barranco, decidido a salir de él cuanto antes. Pero el barranco se estrecha y termina en una pared de roca con una cueva al pie. El olor es inconfundible. De la oscuridad sale un huargo viejo y enorme, con el hocico canoso y una oreja arrancada, dispuesto a defender su guarida. Gruñe tan hondo que lo sientes en el pecho. El paso es demasiado angosto para darle la espalda.

#### `puente_4_7`

Emboscada en la cresta.
Trepas por la pared del barranco, mano tras mano, hasta salir a una cresta pelada. Allí te espera una sorpresa: un espía de las Tierras Brunas, que vigilaba desde arriba, te recibe con una patada que casi te devuelve al fondo. Te agarras a una raíz y logras subir. Mientras forcejeáis, un aullido responde muy cerca, y un huargo asoma por la cresta, atraído por el ruido. Ahora tienes dos enemigos y un precipicio a la espalda.

#### `puente_5_0`

El pozo del páramo.
Encuentras un pozo de piedra en mitad del páramo, junto a los cimientos de una casa que ya no existe. El agua del fondo es oscura, pero fresca. Te curas los rasguños, bebes y te sientas con la espalda contra el brocal. Durante un rato solo oyes el viento en el brezo. Piensas en Bree, en tu casa cerrada, en lo lejos que has llegado. Luego te levantas: el río ya no puede estar lejos.

#### `puente_5_1`

El puente a lo lejos.
Subes a una loma para orientarte. Desde arriba, por fin, ves el final de las Tierras Solitarias: una franja de árboles oscuros marca el curso del Fontegrís, y sobre él, pequeño como un juguete, un puente de piedra de tres arcos. Más allá, la tierra se alza en colinas boscosas. Pero también ves otra cosa: sombras grises que se mueven entre los árboles de la orilla, demasiadas para contarlas.

#### `puente_5_2`

El agua que borra.
Un arroyo ancho y helado te corta el paso, camino del Fontegrís. Lo vadeas con el agua hasta las rodillas, que se te entumecen al instante. En la otra orilla te secas bajo un sol pálido y vuelves a meterte en la corriente un buen trecho para borrar tu olor, como hacen los cazadores de Bree para despistar a los perros. Si los huargos te buscan, al menos tendrán que esforzarse. El aire ya huele a río grande.

#### `puente_5_3`

El campo de huesos.
Atraviesas un llano donde los huesos blanquean entre la hierba: caballos, lobos y algún hombre, caídos en una escaramuza de la que nadie cantará. Las armas están oxidadas, aunque algunas puntas de flecha parecen recientes. Recoges un escudo pequeño todavía útil y te lo cuelgas a la espalda. Mientras caminas, sientes los ojos de los cuervos sobre ti. Saben que este campo siempre tiene sitio para uno más.

#### `puente_6_0`

Entre los juncos.
Bajas hacia el río dando un rodeo amplio, siempre contra el viento, para que los huargos no te huelan. El Fontegrís corre gris y rápido, crecido por las lluvias, entre orillas de piedra. Te arrastras entre los juncos hasta tener el Último Puente a la vista. Sobre la calzada, entre los arcos, se tumban varias bestias grises. Una sombra mucho mayor y más oscura que las demás se levanta y olfatea el aire en tu dirección.

#### `puente_6_1`

Carrera hacia el río.
Decides no esperar y bajas a la carrera hacia el río por campo abierto, con la esperanza de llegar al puente antes de que te descubran. No lo consigues. Dos huargos que vigilaban desde un bosquecillo salen tras de ti, uno por cada lado, y te obligan a detenerte en una pendiente de piedras sueltas. Más abajo, junto al puente, un aullido profundo les responde. Tienes que acabar con estos dos antes de que lleguen los demás.

#### `puente_7_0`

El jefe de la manada.
El Último Puente cruza el Fontegrís en tres arcos de piedra antigua. Los viajeros a los que sigues pasaron por aquí hace días; ahora, sobre la calzada, te espera el jefe de la manada: un huargo gigantesco, negro como el carbón, con cicatrices de mil peleas y unos ojos que parecen entender demasiado. Se planta en mitad del puente y te cierra el paso. Al otro lado esperan las colinas boscosas y el camino hacia el este. Para alcanzarlos, tienes que pasar por encima de él.

## quebradas

#### `quebradas_1_0`

Colinas de mala fama.
Al otro lado del Último Puente, el Camino se adentra en una tierra distinta: colinas empinadas cubiertas de bosque, barrancos sombríos y, en lo alto de algunas cumbres, castillos en ruinas de aspecto siniestro. En Bree llaman a esta comarca las Quebradas de los Trolls, y no lo dicen en broma. Llueve a menudo, los árboles gotean y el musgo lo cubre todo. Entre los helechos ves senderos anchos, aplastados por pies demasiado grandes. Al este, muy lejos, asoman por fin cumbres nevadas.

#### `quebradas_2_0`

La calzada olvidada.
Sigues el Camino, que serpentea entre las colinas, encajonado por laderas de árboles altos. Aquí la calzada está descuidada: raíces que levantan las losas, charcos profundos, ramas caídas que nadie retira. Aun así, es lo único que parece seguro en esta tierra. Al mediodía encuentras una piedra con una muesca reciente, la señal de alguien que se detuvo aquí no hace mucho. A lo lejos, una columna de humo sube entre los árboles.

#### `quebradas_2_1`

Bajo los árboles viejos.
Abandonas el Camino y subes por una ladera boscosa, donde los árboles son tan viejos que sus copas apagan la luz del día. El suelo está cubierto de hojas podridas que amortiguan tus pasos. Avanzas entre troncos gigantescos, siguiendo una senda de animales que sube y baja sin descanso. De vez en cuando, un crujido de ramas a lo lejos te obliga a detenerte y escuchar. El bosque no está vacío, y lo que se mueve en él no es pequeño.

#### `quebradas_3_0`

El castillo de la colina.
Subes a una colina rocosa coronada por las ruinas de un castillo. Sus muros negros siguen en pie, agrietados, y por las ventanas vacías silba el viento. Quien lo construyó no era amigo de nadie: en las piedras del portón hay tallas de figuras encadenadas, borrosas por el tiempo. Desde las almenas dominas todo el valle. El Camino se pierde hacia el este entre los bosques, y en una hondonada cercana ves moverse figuras pequeñas y oscuras.

#### `quebradas_3_1`

Las hogueras del barranco.
Sigues el humo hasta un barranco junto al Camino. Te arrastras entre los helechos y ves un campamento de orcos de las Montañas Nubladas: chozas de ramas, huesos roídos y una hoguera humeante. Han bajado de las montañas con el otoño, buscando presas en los caminos. Antes de que puedas retirarte, tres de ellos, que volvían de cazar, te descubren por detrás. Lanzan un chillido y cargan contra ti con cimitarras dentadas.

#### `quebradas_3_2`

El huargo del remanso.
Bajas hasta un arroyo que corre entre rocas cubiertas de musgo y lo sigues aguas arriba, pensando que te llevará a terreno abierto. Pero el agua también atrae a otros. En un remanso, un huargo bebe con las orejas tiesas. Lleva al cuello restos de una correa de cuero: alguien lo ha usado como montura. Al verte enseña los colmillos, y salta de piedra en piedra por el arroyo directo hacia ti.

#### `quebradas_3_3`

La loma desnuda.
Subes a una loma donde el bosque se abre y solo crecen helechos y rocas. Desde allí ves la comarca entera: colinas y más colinas, oscuras de árboles, y entre ellas el valle por el que corre el Camino. Al otro lado de la loma, en un claro, distingues tres formas grandes y grises, quietas como rocas. Parecen gigantes agachados alrededor de una hoguera apagada. No se mueven. Aun así, se te seca la boca.

#### `quebradas_4_0`

Los sótanos.
Bajas por una escalera rota hasta los sótanos del castillo. Allí abajo huele a moho y a algo peor. Hay huesos amontonados, grandes y pequeños, y en las paredes, marcas de garras a una altura imposible para un hombre. En un rincón encuentras un cofre reventado, vacío salvo por unas monedas antiguas pegadas al fondo por el barro. Te las guardas y sales a la luz cuanto antes. Algo grande usó este lugar como guarida, y no hace tanto.

#### `quebradas_4_1`

Lo que temen los orcos.
Te quedas en las almenas observando la hondonada. Las figuras son orcos, una docena, que arrastran algo pesado entre los árboles hacia las colinas del norte. No vienen hacia ti, y das gracias. Esperas a que desaparezcan y, entretanto, te fijas en el terreno: los orcos evitan con cuidado ciertos barrancos, como si en ellos hubiera algo que incluso a ellos les diera miedo. Tomas buena nota de esos lugares.

#### `quebradas_4_2`

El botín de los orcos.
Cuando cae el segundo orco, nadie más aparece: el campamento estaba casi vacío. Lo registras deprisa. Entre pieles sucias y cacharros encuentras cosas robadas a viajeros: un odre de vino, una bota de enano, una capa verde rasgada, un puñado de clavos de herradura. Los orcos cazan en el Camino, y no todos sus encuentros terminan bien para los caminantes. Llenas tu bolsa con lo que pueda servirte y te alejas antes de que vuelvan los demás.

#### `quebradas_4_3`

Perdido en el bosque.
No te quedas a comprobar si hay más. Te internas en el bosque a la carrera, saltando raíces y apartando ramas, hasta que el humo del campamento queda muy atrás. Cuando te detienes, jadeando, estás perdido entre árboles idénticos. Te calmas, buscas el musgo de los troncos y la luz entre las copas, y poco a poco recuperas el rumbo hacia el este. Has perdido tiempo, pero estás vivo, y nadie te sigue.

#### `quebradas_4_4`

Huellas gigantes.
Tras la pelea, te arrodillas junto al remanso para lavarte y entonces las ves: huellas en el barro de la orilla, tan grandes que en cada una cabría un niño acostado. Tienen dedos gruesos y se hunden muy hondo. Son recientes, de la noche pasada como mucho, y se alejan arroyo arriba, hacia un barranco oscuro. Ya no te quedan dudas sobre el nombre de esta tierra. Te pones en pie despacio, con la espada en la mano, y escuchas.

#### `quebradas_4_5`

El abeto hueco.
Para alejarte del cadáver del huargo, que pronto atraerá a otros, cruzas el arroyo sobre unos troncos caídos, resbaladizos de musgo. En la otra orilla el bosque es más claro y huele a resina. Encuentras un abeto enorme con el tronco hueco y te metes dentro a descansar. Desde tu refugio ves pasar un zorro, una familia de ciervos y, muy alto entre las ramas, una ardilla que te regaña. Por un momento, el bosque parece casi amable.

#### `quebradas_4_6`

Los tres de piedra.
Bajas al claro con el corazón en la boca. Las tres formas son trolls, sí, pero de piedra: enormes, encorvados, uno con el brazo en alto como si fuera a golpear. El tiempo los ha cubierto de líquenes y musgo. Alrededor hay huellas recientes de botas y de pies descalzos, y la hierba está aplastada donde alguien se sentó a descansar. Los viajeros a los que sigues estuvieron aquí. Sonríes al imaginarlos ante estos gigantes, tan asustados como tú.

#### `quebradas_4_7`

La cueva de la losa.
Rodeas el claro y, entre rocas cubiertas de zarzas, encuentras la boca de una cueva. Una gran losa de piedra, que en otro tiempo debió de servir de puerta, yace partida a un lado. Dentro, el suelo está sembrado de huesos viejos, cacharros rotos y monedas verdes de óxido. Hace mucho que nadie vive aquí, o eso parece. Pero junto a la entrada hay un montón de ramas recién arrancadas y un hedor que no tiene nada de antiguo.

#### `quebradas_5_0`

El jinete de lobo.
Avanzas por la cresta de una colina, donde los árboles ralean y se camina mejor. Es un error: desde lejos se te ve recortado contra el cielo. Un orco montado en un huargo te ha visto y sube la ladera a toda velocidad, agitando una lanza sobre la cabeza. A pocos pasos, el orco salta de la montura para atacarte por un lado mientras la bestia te embiste por el otro. Tienes que dividir tu atención entre los dos.

#### `quebradas_5_1`

La hondonada de la niebla.
Cruzas una hondonada oscura y húmeda, donde la niebla se queda prendida entre los troncos. Un aullido rompe el silencio, muy cerca, y te deja helado. De la niebla surge un huargo enorme, con el lomo erizado y el hocico manchado de sangre fresca. Ha estado cazando y no le ha bastado. Da vueltas a tu alrededor, midiendo tus fuerzas, y luego se lanza. Plantas los pies en la tierra húmeda y esperas el golpe.

#### `quebradas_5_2`

El sendero ancho.
Encuentras un sendero ancho y pisoteado que atraviesa el bosque como una herida. Lo usan criaturas grandes, pero esta vez quienes lo recorren son pequeños, ruidosos y muchos: cuatro orcos que bajan cargados con un ciervo, discutiendo a gritos. Te ven a la vez que tú a ellos. Sueltan la presa, desenvainan y se abren para rodearte, gritando en su lengua algo que suena a burla. Esperas a que el primero se acerque.

#### `quebradas_5_3`

El orco de la orilla.
Bajas hacia el río que corre por el fondo del valle, buscando un lugar por donde cruzar. En la orilla, sentado en una roca, un orco grande y musculoso afila una espada robada. Lleva un casco abollado y un escudo negro sin adornos. Es más alto que tú y no parece tener prisa. Se levanta despacio, se cuelga el escudo al brazo y te hace una seña burlona para que te acerques.

#### `quebradas_6_0`

La voz de la cueva.
Decides no arriesgarte más y das un largo rodeo por la ladera, entre peñas y árboles caídos. Tardas casi todo el día, pero no te cruzas con nadie. Al caer la tarde, el bosque se abre de pronto ante un barranco profundo, al pie de una pared de roca. Huele a humo rancio y a carne podrida. En la pared se abre una gran cueva, y de ella sale un sonido que al principio tomas por un derrumbe: una voz enorme que canturrea.

#### `quebradas_6_1`

El valle a oscuras.
Quieres salir de estas colinas cuanto antes y cruzas el valle de noche, guiándote por las estrellas que asoman entre las copas. En la oscuridad, dos huargos te cierran el paso en un claro, atraídos por tu olor. Luchas casi a ciegas, adivinando sus saltos por el brillo de sus ojos. Más allá del claro, una pared de roca se alza contra el cielo, y en su base, la boca de una gran cueva deja escapar el resplandor rojo de una hoguera.

#### `quebradas_7_0`

El trol de piedra.
De la cueva sale un trol de piedra, vivo y bien despierto: casi el doble de alto que un hombre, con la piel gris y rugosa como una roca y una maza hecha con un tronco entero. Te ha olido, y le gusta lo que huele. Detrás de él, la hoguera ilumina un montón de huesos y tesoros robados. Sabes por los cuentos que la luz del sol convierte a los suyos en piedra, pero falta mucho para el alba. Hasta entonces, solo cuentas con tu espada. Más allá de estas colinas, el Camino desciende hacia un río rápido y hacia el vado que lleva a la tierra de los elfos: el vado del Bruinen.

## bruinen

#### `bruinen_1_0`

El valle del Bruinen.
Amanece cuando dejas atrás las colinas de los trolls, con el cuerpo dolorido y la espada mellada. El Camino desciende ahora por una larga garganta entre pinos oscuros, y el aire se llena de un rumor creciente: el Bruinen, el río que baja de las montañas y marca la frontera de la tierra de los elfos. En el barro de la calzada ves huellas profundas de cascos herrados, muchas y muy juntas, que corren todas hacia el este. Nadie cabalga así por gusto. Algo, o alguien, fue perseguido por aquí hace pocos días.

#### `bruinen_2_0`

La garganta del Camino.
Sigues la calzada, que baja en curvas cerradas entre paredes de roca. A cada paso el rumor del río suena más fuerte, aunque todavía no lo ves. Las huellas de los cascos no se detienen en ningún momento: ni una hoguera, ni un alto para abrevar a los caballos. Quienes las dejaron tenían prisa, o hambre de algo. Al mediodía, en un recodo, te parece ver una figura que se agacha tras una peña, por encima del Camino, y desaparece. Te quedas quieto un buen rato, con la mano en la empuñadura.

#### `bruinen_2_1`

La ladera del bosque.
Prefieres no caminar por donde han pasado esos jinetes, y te internas en el bosque que cubre la ladera. Bajas entre abedules y pinos, sorteando piedras resbaladizas y arroyos que corren hacia el valle. Es más lento, pero aquí nadie te verá desde el Camino. A media mañana encuentras el cadáver de un ciervo, medio devorado, con mordiscos de unas fauces demasiado grandes para un lobo corriente. Las huellas de la bestia se alejan entre los helechos. Más arriba, un risco de piedra gris domina todo el valle.

#### `bruinen_3_0`

Pies pequeños en el barro.
Te arrodillas junto a la calzada y estudias el barro con calma. Debajo de las huellas de los caballos, casi borradas por ellas, hay otras más antiguas: las de un poni cargado, botas de hombre alto y unos pies pequeños y descalzos, de dedos anchos. Pies de hobbit. Recuerdas los rumores de Bree sobre el montaraz que guiaba a unos medianos hacia el este. Pasaron por aquí antes que los jinetes, aunque no mucho antes. Lo que no ves por ninguna parte son huellas de vuelta, ni de unos ni de otros.

#### `bruinen_3_1`

El vigía del recodo.
Dejas la calzada y trepas hacia la peña donde viste moverse la sombra. Detrás hay un hombre agazapado, de pelo enmarañado y capa parda, con un cuchillo largo al cinto: un espía de las Tierras Brunas. Ha estado vigilando el Camino, y por el lecho de ramas y las migas de pan, lleva días haciéndolo. Al verte se lanza contra ti sin una palabra. Más abajo, entre los pinos, oyes ramas que se quiebran: alguien más huye ladera abajo, sin mirar atrás.

#### `bruinen_3_2`

Los huargos del helechal.
Sigues las huellas entre los helechos con la espada desnuda. No tardas en oír los gruñidos: en un claro, tres huargos se disputan los restos de otra presa. Son flacos y tienen el pelaje sucio de barro, pero están igual de rabiosos que cualquiera, y en cuanto el viento les lleva tu olor dejan la carne y se vuelven hacia ti. Uno te rodea por la izquierda, otro por la derecha, y el tercero te mira de frente, enseñando los dientes. No hay árbol al que subir ni sitio adonde correr. Solo te queda pelear.

#### `bruinen_3_3`

El risco gris.
Trepas por el risco, agarrándote a raíces y grietas, hasta una repisa desde la que se domina el valle entero. Abajo, el Bruinen corre ancho y turbio, de un color pardo que no se parece en nada al agua clara de montaña. Hay troncos enteros flotando en la corriente y, en las orillas, franjas de barro y piedras arrastradas mucho más arriba de lo normal. El río ha crecido de golpe hace poco, como si algo lo hubiera desbordado. Al este, en la otra orilla, el bosque sube hacia valles escondidos.

#### `bruinen_4_0`

La hondonada del campamento.
Las huellas pequeñas se apartan del Camino un trecho y te llevan a una hondonada al abrigo de unas rocas. Alguien acampó aquí: hay un círculo de ceniza fría, ramas de pino dispuestas como lecho y, entre ellas, un trozo de venda con manchas oscuras. Huele todavía, muy levemente, a unas hojas aplastadas de aroma dulce y limpio que no reconoces. Uno de los viajeros estaba herido, y alguien cuidaba de él con saber de montaraz. Te guardas la venda sin saber bien por qué y vuelves a la calzada.

#### `bruinen_4_1`

El galope.
Sigues el rastro de los caballos negros. Más adelante, las pisadas se alargan, se hunden más y salpican barro a los lados: ahí empezaron a galopar. Durante un tramo, entre ellas, ves otras distintas, de un caballo más ligero que apenas marca el suelo, como si corriera por delante de todos los demás. Al final del tramo, en una rama baja, ondea un jirón de tela negra. Al tocarlo se te hiela la mano, igual que si la hubieras metido en nieve. Conoces ese frío: lo sentiste en la Cima de los Vientos.

#### `bruinen_4_2`

El petate del espía.
Cuando el espía cae, revisas sus cosas. Lleva poco: pan duro, una manta raída, un pedernal y una bolsa con monedas de plata acuñadas lejos de aquí. Pero en el forro de la capa encuentras algo más: un trozo de cuero con un dibujo tosco del Camino, del río y del vado, y marcas junto a cada recodo. Alguien quiere saber quién cruza hacia la tierra de los elfos, y paga bien por ello. Te guardas el cuero. Quizá a los señores del otro lado del río les interese verlo.

#### `bruinen_4_3`

El que huyó.
Bajas la ladera tras el otro hombre, guiándote por las ramas rotas y las piedras removidas. Corre como alguien que conoce bien el terreno, y a ratos lo pierdes de vista. Por fin llegas a la orilla de un arroyo crecido y lo ves al otro lado, subiendo a toda prisa hacia el oeste, de vuelta por donde has venido. No vale la pena seguirlo: lo que vaya a contar, ya lo contará. Te quedas un momento mirando el agua, que baja turbia, cargada de ramas y barro.

#### `bruinen_4_4`

El agua fría.
Los huargos te han dejado arañazos en el brazo y un mordisco poco profundo en la pierna. Bajas hasta un arroyo que salta entre las piedras y te lavas las heridas en el agua helada, apretando los dientes. Luego las vendas con tiras de tu propia camisa. Mientras lo haces, notas que el arroyo arrastra hojas y ramas a una velocidad extraña, y que en las piedras de la orilla hay una línea de barro seco muy por encima del agua. Hace pocos días, todo este valle estuvo bajo una crecida.

#### `bruinen_4_5`

La guarida.
Sigues el rastro de los huargos ladera arriba hasta una grieta entre raíces de pino, donde la tierra está removida y apesta a carroña. Es su guarida. Dentro hay huesos de ciervo, de oveja y otros que prefieres no mirar de cerca. Junto a la entrada encuentras una correa de cuero cortada, de las que usan los jinetes de lobo de las montañas. Estas bestias no estaban aquí por casualidad: alguien las trajo o las empujó hacia el vado. Sales al aire limpio con una inquietud que no se va.

#### `bruinen_4_6`

Los árboles caídos.
Bajas del risco hasta la orilla del Bruinen. De cerca, la crecida es aún más impresionante: árboles enteros arrancados de raíz y amontonados como paja, rocas del tamaño de una carreta movidas de su sitio y la arena cubierta de ramas y espuma seca. El agua ha bajado algo, pero sigue rugiendo, rápida y fría. No ha llovido tanto como para esto. Un leñador de Bree diría que fue una tormenta en las montañas. Tú, después de todo lo que has visto este otoño, ya no estás tan seguro.

#### `bruinen_4_7`

La grieta del risco.
Encuentras una grieta en la roca, lo bastante honda para resguardarte del viento, y decides descansar un rato antes de seguir. Te duermes sin querer. Te despierta, ya de tarde, un sonido que viene de muy lejos, río abajo: un grito largo y agudo que no es de ningún animal. Se te eriza la piel, porque conoces ese grito. Esperas, inmóvil, con la espada sobre las rodillas, pero no se repite. Solo queda el rumor del agua y el latido de tu propio corazón.

#### `bruinen_5_0`

El huargo viejo.
Un sendero de ciervos baja hacia el vado entre abedules, y lo sigues porque parece el más corto. Es estrecho y está cubierto de hojas mojadas. A mitad de bajada, el bosque se queda en silencio de golpe. Un huargo viejo y enorme sale de detrás de un tronco y se planta en mitad del sendero. Tiene el hocico gris y una cicatriz le cruza el lomo. No ataca enseguida: te estudia, como si supiera que llevas días de camino y que estás cansado.

#### `bruinen_5_1`

Vigías junto al río.
Te guías por el ruido del río, que suena cada vez más cerca. En un claro junto a unas rocas, dos hombres de las Tierras Brunas están sentados ante un fuego pequeño, sin humo, con la vista puesta en la otra orilla. Hablan en voz baja en su lengua áspera. Uno de ellos se vuelve, te ve y da un grito. En un momento los dos están en pie, con los cuchillos en la mano. No piensan dejar que nadie cuente que estaban aquí vigilando.

#### `bruinen_5_2`

La manada del pedregal.
Para ahorrar camino, atajas por un pedregal: una ladera de piedras sueltas, sin un solo árbol, que baja directa hacia el valle del río. Las piedras ruedan bajo tus botas y el ruido delata cada paso. A medio cruzar oyes aullidos arriba. Cuatro huargos bajan saltando de roca en roca, mucho más ágiles que tú en este terreno. No hay dónde esconderse. Buscas una peña grande, apoyas la espalda contra ella para que no te rodeen y esperas el primer salto con la espada en alto.

#### `bruinen_5_3`

El hombre de la cicatriz.
Prefieres no bajar todavía al vado y trepas a unas peñas altas, desde donde puedes ver sin ser visto. O eso crees. Entre dos rocas, un hombre te está esperando: un espía de las Tierras Brunas, curtido y con aspecto de haber matado más de una vez. Lleva una espada corta de buen acero y una cicatriz le parte la mejilla. Te ha visto venir desde lejos. Sonríe sin ganas, se escupe en la palma de la mano y avanza hacia ti.

#### `bruinen_6_0`

El vado.
Al atardecer llegas por fin al vado del Bruinen, donde el Camino entra en el río y sale por la otra orilla, empinada y oscura de árboles. El agua corre todavía crecida y turbia. La orilla de este lado es un caos de barro, piedras y ramas, y en él ves cosas que te encogen el estómago: cascos negros hundidos en el lodo, jirones de tela oscura enganchados en las zarzas y, medio enterrado en la arena, el cuerpo de un gran caballo negro, ahogado. Decides esperar a que salga la luna para cruzar.

#### `bruinen_6_1`

La vigilia de la arboleda.
No te fías de acercarte al agua a cielo abierto, y te apostas en una arboleda desde la que se ve el vado entero. Cae la noche. Pasan las horas sin que nada se mueva, salvo el río. Entonces, a tu espalda, oyes un jadeo: dos huargos han seguido tu rastro hasta aquí y se separan para atacarte por los dos lados. Luchas entre los troncos, a la luz de una luna pálida. Y, por encima del fragor, notas que algo en la orilla parece estar escuchando.

#### `bruinen_7_0`

El Jinete Negro.
Bajo la luna, te acercas al borde del agua para cruzar. Entonces el barro de la orilla se mueve. Una figura alta se alza del lodo, envuelta en una capa negra hecha jirones y empapada: un Jinete Negro sin caballo, al que la crecida dejó atrás en la orilla oeste. Sientes el mismo frío que en la Cima de los Vientos, pero más amargo, como si la criatura, humillada, buscara en quién vengarse. Desenvaina una hoja pálida. A tu espalda el río ruge más fuerte, y en la otra orilla una luz blanca se mueve entre los árboles.

## rivendel

#### `rivendel_1_0`

Despertar en Rivendel.
Despiertas en una cama blanda, en una habitación llena de luz, con el rumor de una cascada entrando por la ventana. Tardas en recordar: el jinete en el vado, el frío, y luego el río alzándose de nuevo en crestas blancas que arrastraron al jinete aguas abajo entre alaridos. Después, según te dirán luego, un señor élfico de cabellos dorados te sacó de la orilla y te subió a su montura. Una elfa que cambia el agua de la jofaina te sonríe: estás en Rivendel, en la casa del señor del valle, y aquí estás a salvo. Fuera, el valle te espera.

#### `rivendel_2_0`

La gran casa.
Cuando puedes tenerte en pie, recorres la casa. Es grande y laberíntica, hecha de madera tallada y piedra clara, con galerías abiertas al valle y escaleras que nunca parecen llevar dos veces al mismo sitio. Los elfos te saludan con una cortesía tranquila, como si los viajeros heridos fueran algo de cada día. Desde una ventana ves llegar gente de muy lejos: enanos de barbas trenzadas, elfos vestidos de verde y gris, un hombre alto con un gran cuerno al costado. Algo importante se está preparando en esta casa.

#### `rivendel_2_1`

El valle escondido.
Sales al valle con la mañana. Rivendel está metido en una hondonada estrecha entre pinares, con el río corriendo al fondo y cascadas que caen por todas partes, blancas y ruidosas. El aire huele a resina y a hojas húmedas. Por los senderos pasan elfos que cantan mientras trabajan, y a lo lejos, más allá de los árboles, oyes el golpe rítmico de un martillo sobre un yunque. Una senda sube por la ladera junto a la cascada más alta. Respiras hondo. Por primera vez en semanas, no tienes prisa.

#### `rivendel_3_0`

La Sala del Fuego.
Al caer la noche te llevan a la Sala del Fuego, un salón largo de columnas donde siempre arde una hoguera y adonde los elfos van a cantar y a escuchar. Te sientas en un rincón, junto a las sombras. Las canciones son en una lengua que no entiendes, pero las imágenes llegan solas: mares grises, estrellas antiguas, barcos blancos. Por primera vez desde que saliste de Bree, el cansancio se te deshace en el pecho. Nadie te pregunta quién eres ni qué buscas. Por esta noche, basta con estar aquí.

#### `rivendel_3_1`

Los libros antiguos.
Un elfo de voz suave te lleva a una sala llena de estanterías, mapas enrollados y libros más viejos que cualquier reino de los hombres. Te enseña a leer un mapa de las tierras que se extienden al sur del valle, a lo largo de las Montañas Nubladas. Ahí, te dice, estuvo Eregion, a la que los hombres llaman Acebeda: una tierra de herreros élficos, famosos en otro tiempo, que acabó en ruinas tras una guerra antigua. Hoy solo quedan piedras, acebos y silencio. Algo en esas palabras te llama.

#### `rivendel_3_2`

La forja de los elfos.
Sigues el sonido hasta una forja abierta junto al río, donde un herrero élfico de brazos fuertes trabaja una hoja al rojo. Se fija en tu espada, que cuelga del cinto llena de mellas, y te pide que se la dejes. La sostiene un buen rato entre las manos, como quien escucha. Dice que es acero honrado, forjado por hombres, y que ha visto cosas que no debería haber visto. Promete devolvértela mejor de lo que llegó. Mientras tanto, te invita a quedarte y mirar.

#### `rivendel_3_3`

Junto a las cascadas.
Subes por la senda que trepa junto a la cascada, entre helechos empapados por la espuma. Arriba hay un mirador de piedra desde el que se ve todo el valle y, al norte y al este, las cumbres de las montañas. Allí encuentras a dos elfos altos, de cabello oscuro y aire de guerreros, que se preparan para salir de exploración. Se parecen tanto que podrían ser gemelos. Te saludan, sorprendidos de ver a un hombre de Bree tan lejos de su casa, y te preguntan por el Camino.

#### `rivendel_4_0`

El viejo hobbit.
En el centro de la sala, un hobbit muy anciano, de pelo blanco y ojos vivos, se levanta y recita unos versos que ha compuesto él mismo, entre las risas y los aplausos de los elfos. Luego se sienta a tu lado, curioso al saber que vienes de Bree. Te pregunta por la posada, por la cerveza, por el tiempo en el Camino. Cuando le hablas del montaraz y de los hobbits, sonríe con picardía y te dice que hay historias que es mejor dejar que cuenten sus dueños.

#### `rivendel_4_1`

El enano de la Montaña.
Sales a la terraza a tomar el aire y encuentras a un enano de barba blanca y trenzada, fumando en pipa bajo las estrellas. Viene de la Montaña Solitaria, te dice, muy al este, y ha hecho un largo viaje para pedir consejo. Habla con orgullo de su pueblo, pero cuando menciona a unos parientes que partieron hace años hacia las antiguas minas de las Montañas Nubladas, se le ensombrece la voz. Hace mucho que no saben nada de ellos. Te quedas callado, mirando las montañas oscuras.

#### `rivendel_4_2`

Los caminos del sur.
Pasas horas inclinado sobre los mapas. Aprendes los nombres: el Fontegrís, que ya cruzaste; el Bruinen; las Montañas Nubladas, que bajan como un espinazo hacia el sur; y en su ladera occidental, Eregion. Más allá se alza un pico de laderas rojizas que el elfo llama Caradhras, el Cuerno Rojo, con un paso alto que lleva al otro lado de las montañas. Copias como puedes el camino en un trozo de pergamino. Por primera vez, tu viaje tiene un dibujo y una dirección.

#### `rivendel_4_3`

Los herreros de antaño.
El elfo te cuenta la historia de Eregion. Hace mucho tiempo vivieron allí los mejores herreros élficos, en una ciudad llamada Ost-in-Edhil, amigos de los enanos que habitaban al otro lado de las montañas. Hicieron obras maravillosas, pero alguien de buenas palabras y malas intenciones los engañó, y la guerra los destruyó. No te cuenta más, y tú no preguntas. Solo te dice que en esa tierra todavía crecen los acebos que ellos plantaron, y que las piedras recuerdan.

#### `rivendel_4_4`

El temple.
Te pasas los días en la forja, tirando del fuelle y acarreando carbón. El herrero desmonta tu espada, endereza la hoja, borra las mellas y la templa en agua del río, murmurando palabras que no entiendes. Cuando te la devuelve, pesa igual pero parece otra: más viva, más firme en la mano. En la base de la hoja ha grabado un pequeño signo élfico. Significa, te dice, algo así como «el que vigila». Te parece un buen nombre para lo que eres.

#### `rivendel_4_5`

La cota ligera.
Mientras tu espada descansa en la forja, el herrero rebusca en un arcón y saca una cota de malla de anillas finas y oscuras. Se hizo aquí hace muchos años, te dice, para un montaraz que nunca volvió a reclamarla. Te la ajusta él mismo con tiras de cuero. Pesa menos que una camisa de lana y, sin embargo, el filo de un cuchillo resbala sobre ella sin morder. Al final del día te devuelve también la espada, limpia y afilada. Te sientes otro.

#### `rivendel_4_6`

Lecciones de esgrima.
Antes de salir de exploración, los dos elfos te invitan a practicar con ellos en un prado junto al río. Te corrigen la guardia, la forma de pisar, la manera de mirar al enemigo a los ojos y no a la espada. Son pacientes, pero no blandos: acabas en el suelo más veces de las que puedes contar. Mientras descansáis, te cuentan que cabalgan a menudo con los montaraces del norte, y que conocen bien al que trajo a los hobbits hasta aquí. No te dicen su nombre. Solo que es de los mejores.

#### `rivendel_4_7`

El caballo blanco.
Bajas a las caballerizas, donde los elfos cuidan de sus monturas como si fueran amigos. Un mozo te muestra un caballo blanco, esbelto y orgulloso, y te cuenta que fue él quien cruzó el vado hace pocos días llevando a lomos a un mediano herido, con los Nueve pisándole los talones. Llegó a la otra orilla justo antes de que el río se alzara. El caballo te olfatea la mano con calma. Te alegras de que aquel pequeño viajero, quienquiera que fuese, llegara a salvo.

#### `rivendel_5_0`

El consejo cerrado.
Una mañana, la casa amanece distinta. Los elfos hablan en voz baja y, en un pórtico junto al río, se reúnen muchos: el señor de la casa, un anciano vestido de gris, elfos, enanos, hombres y alguna figura pequeña. Un guardia te detiene con amabilidad a cierta distancia. No es un consejo para ti, te dice. Te quedas mirando de lejos durante horas. Cuando por fin se separan, nadie sonríe, y cada uno se marcha callado. Algo se ha decidido allí que pesa sobre todos.

#### `rivendel_5_1`

Las manos del sanador.
Las heridas del viaje se han cerrado, pero el frío del vado sigue en tus huesos: a veces te despiertas helado, con la sensación de una sombra a tu lado. Vas a ver a los sanadores de la casa. Una elfa de manos frías te escucha, prepara agua caliente y echa en ella unas hojas aplastadas de olor dulce y limpio. El vapor te llena la cabeza de luz. Cuando sales, el frío se ha ido. Te dice que tuviste suerte: otros cargan con ese mal mucho más tiempo.

#### `rivendel_5_2`

La noche de las estrellas.
Una noche subes a una terraza alta, abierta al cielo. Hay elfos sentados en silencio, mirando las estrellas, y uno de ellos canta en voz baja una canción a la que llaman la Reina de las Estrellas. Te tumbas en la piedra todavía tibia y piensas en Bree: en tu casa vacía, en la posada, en la calle embarrada. Te sorprende no echarlo de menos tanto como esperabas. Esta tierra no es tuya, lo sabes, y tarde o temprano tendrás que seguir tu camino.

#### `rivendel_5_3`

Noticias de los exploradores.
En el patio, unos exploradores élficos y un par de montaraces del norte se preparan para salir en busca de los jinetes negros. Te acercas a preguntar. Te cuentan que el río arrastró a los Nueve y que sus caballos se ahogaron, pero que nadie ha encontrado a los jinetes, porque esas criaturas no mueren como los demás. Saldrán en todas direcciones: al norte, al oeste y también al sur, siguiendo las montañas. Les cuentas lo del jinete de la orilla. Te escuchan con atención y te dan las gracias.

#### `rivendel_6_0`

La palabra del señor de la casa.
Antes de marcharte, pides ver al señor de la casa, y te recibe en una sala pequeña, sin ceremonia. Su rostro no tiene edad, pero sus ojos parecen haber visto el mundo entero. Escucha tu historia sin interrumpirte. Cuando le dices que piensas ir hacia el sur por la falda de las montañas, te advierte que ese camino no es seguro: hay aves negras que vigilan desde el cielo y hombres de las Tierras Brunas al servicio de un amo ajeno. No confíes en nadie que pregunte demasiado, te dice, y te regala una capa gris de tela élfica.

#### `rivendel_6_1`

Los preparativos.
Llega el invierno al valle, y sabes que es hora de partir. Preparas el petate con cuidado: una capa gris de tela élfica que te regalan y que abriga más de lo que pesa, pan de viaje que dura semanas, una cantimplora, cuerda y una bolsa de hierbas de los sanadores. Revisas la espada, las botas y las correas. Mientras trabajas, oyes que en la casa se prepara también otra partida, más importante que la tuya, pero nadie dice adónde irá ni cuándo. No es asunto tuyo, y lo sabes.

#### `rivendel_7_0`

La despedida.
Una mañana fría de invierno cruzas por última vez el puente estrecho sobre el río. Algunos elfos han salido a despedirte y cantan una canción breve por tu camino. Te vuelves una vez para mirar la casa, las cascadas, la luz entre los pinos, y te cuesta apartar los ojos. Luego sales del valle y tomas rumbo al sur, siguiendo la falda occidental de las Montañas Nubladas. Más allá de muchas leguas de tierras vacías te espera Eregion, la tierra de los acebos. Llevas el cuerpo descansado y el valle en la memoria.

## eregion

#### `eregion_1_0`

La tierra de los acebos.
Tras muchos días de marcha hacia el sur por la falda de las montañas, el paisaje cambia. Entras en una tierra de lomas suaves y matorrales, salpicada de acebos de hojas oscuras y frutos rojos, y aquí y allá asoman entre la hierba piedras talladas: restos de muros, columnas caídas, peldaños que no llevan a ninguna parte. Es Eregion, la vieja tierra de los elfos. El aire es limpio y frío, pero hay algo extraño: no se oye ni un pájaro. Al este, tres grandes cumbres nevadas dominan el horizonte.

#### `eregion_2_0`

La calzada élfica.
Encuentras los restos de una calzada antigua, de losas anchas y bien encajadas, que corre hacia el este entre las lomas. La sigues. Es cómodo caminar por ella, aunque te sientes expuesto: apenas hay árboles a los lados y el cielo es enorme. A un lado, no muy lejos, asoman las ruinas de un edificio cubierto de hiedra. Entonces ves en lo alto una mancha oscura que se mueve como humo contra el viento. No es humo. Es una bandada de pájaros negros que vuela en círculos amplios, como si buscara algo.

#### `eregion_2_1`

Entre los acebos.
Prefieres avanzar a cubierto y te metes entre los acebos, que aquí crecen altos y espesos como un bosque. Sus hojas pinchan y se te enganchan en la capa, pero desde arriba nadie podría verte. Avanzas despacio, siguiendo las vaguadas. A media tarde hueles humo de leña y, poco después, oyes un aullido largo que llega desde un arroyo cercano. Te detienes en seco. Nadie debería vivir aquí: esta tierra lleva siglos vacía.

#### `eregion_3_0`

La bandada.
Sigues por la calzada a pesar de todo, y la bandada te ve. Los pájaros bajan en espiral, graznando con voces roncas, y entonces los reconoces: crebain, grandes cuervos de las Tierras Brunas que, según los cuentos, sirven de ojos a quien los alimenta. Dos de ellos se lanzan contra ti con el pico y las garras por delante, buscándote la cara, mientras el resto te vigila desde arriba. Si alguno escapa, alguien sabrá muy pronto que un hombre armado camina por Eregion.

#### `eregion_3_1`

El lobo de las ruinas.
Dejas la calzada y corres a esconderte entre las ruinas cubiertas de hiedra. Los pájaros pasan de largo. Pero no estás solo: entre las piedras caídas hay un lobo enorme, de pelaje gris sucio y ojos amarillos, que te observa con una inteligencia que no te gusta nada. No es un lobo salvaje. Se mueve como si alguien le hubiera enseñado a cazar hombres. Gruñe sin prisa y se interpone entre tú y la única salida.

#### `eregion_3_2`

El fuego entre los acebos.
Te acercas al humo arrastrándote entre los acebos. En un claro hay un pequeño campamento: dos hombres de las Tierras Brunas, de pelo oscuro y enmarañado, asan algo en una hoguera junto a unas mantas y un fardo de provisiones. Por lo que alcanzas a oír, llevan semanas vigilando esta tierra y se quejan del frío y de la paga. Una rama cruje bajo tu rodilla. Los dos se levantan de un salto, cogen sus armas y vienen hacia ti con los ojos entornados.

#### `eregion_3_3`

Lobos en el arroyo.
Bajas con cuidado hacia el arroyo de donde vino el aullido. En la orilla, dos lobos grandes beben agua helada, flacos y de pelo gris. Pero no son lobos corrientes: son demasiado altos, demasiado silenciosos, y uno lleva en el cuello la marca de una vieja cuerda, como si lo hubieran tenido atado. Levantan la cabeza a la vez, te miran y se separan sin hacer ruido, uno por cada lado del arroyo. Saben exactamente lo que hacen.

#### `eregion_4_0`

Las piedras de la ciudad.
La calzada te lleva hasta los restos de una ciudad: muros derrumbados, plazas cubiertas de hierba y arcos rotos que todavía conservan grabados de hojas y estrellas. Quizá fue aquí donde vivieron los herreros élficos de los que hablan los cuentos. Encuentras una piedra con forma de yunque, partida en dos, y junto a ella un acebo enorme, más viejo que nada que hayas visto. Todo está en silencio. Te sientes como un intruso en una casa donde alguien murió hace mucho.

#### `eregion_4_1`

Bajo el acebo viejo.
La bandada vuelve a pasar, más baja esta vez, y te tiras bajo un acebo enorme cuyas ramas llegan hasta el suelo. Desde dentro, entre las hojas, la ves dar vueltas sobre la calzada durante mucho rato. Luego se aleja hacia el suroeste, hacia las Tierras Brunas y, más allá, hacia donde se dice que hay una torre de piedra negra junto a un bosque viejo. Cuando sales, tienes la certeza de que alguien, en algún sitio, ya sabe que estás aquí.

#### `eregion_4_2`

La torre caída.
Entre las ruinas queda en pie la mitad de una torre, y trepas por sus piedras hasta lo alto. Desde allí la vista es enorme: las lomas de acebos, la calzada que corre hacia el este y, al fondo, las montañas, con una cumbre de laderas rojizas que destaca sobre las demás. En una vaguada lejana ves subir el humo de una hoguera y, junto a él, figuras pequeñas que se mueven. Hay más gente en Eregion de la que debería haber.

#### `eregion_4_3`

El manantial.
Detrás de las ruinas, el murmullo del agua te lleva a un manantial que brota de una pila de piedra tallada, adornada con hojas de acebo. El agua está helada y es tan clara que ves cada grano de arena del fondo. Bebes, te lavas la cara y te sientas un rato. Por primera vez desde que entraste en esta tierra sientes algo parecido a la paz, como si los que hicieron esta fuente hubieran dejado en ella un poco de su cuidado. Recuperas fuerzas y sigues adelante.

#### `eregion_4_4`

El prisionero.
Uno de los hombres sigue vivo, herido en una pierna. Lo atas y le preguntas qué hacen aquí. Al principio te escupe; luego, con miedo, empieza a hablar. Su gente vive en las Tierras Brunas, y un señor poderoso les paga para vigilar los caminos: quién va, quién viene y hacia dónde. Habla de un capitán que los manda desde unas ruinas al pie de la montaña roja. Cuando le preguntas por ese señor, solo dice que vive en una torre y que lo sabe todo.

#### `eregion_4_5`

Las cosas del campamento.
Revuelves el campamento. Entre las mantas encuentras comida para semanas, una bolsa con plata y un trozo de pergamino con marcas toscas: la calzada, las ruinas, el paso de la montaña roja y, junto a cada sitio, rayas que podrían contar viajeros. Al pie del dibujo, alguien ha trazado una mano abierta con tinta blanca. No sabes qué significa, pero no te gusta. Echas tierra sobre el fuego, te guardas el pergamino y te alejas antes de que vuelva alguien.

#### `eregion_4_6`

El rastro de la manada.
Examinas el terreno junto al arroyo. Los dos lobos no estaban solos: hay huellas de muchos más, todas recientes, que llegan desde el suroeste y siguen hacia las montañas. Entre ellas, en el barro, encuentras pisadas de botas de hombre. Alguien camina con la manada, o detrás de ella, como un pastor con sus perros. La idea te hiela más que el agua del arroyo. Sigues el rastro un buen trecho, hasta que lo pierdes en un pedregal.

#### `eregion_4_7`

El refugio de piedra.
Encuentras una pequeña construcción élfica casi entera, con el techo de losas todavía en pie y estrellas grabadas en el dintel. Dentro está seco. Enciendes un fuego pequeño y pasas allí la noche, con la espada al alcance de la mano. Fuera empieza a nevar. Antes de dormirte, a la luz de las brasas, distingues en la pared unas letras élficas casi borradas. No sabes leerlas, pero tienes la extraña sensación de que alguien, hace mucho, escribió allí una despedida.

#### `eregion_5_0`

El mojón de los elfos.
Subes a una loma alta coronada por un mojón de piedra, tan antiguo que el musgo lo cubre casi entero. Al limpiarlo descubres una flecha grabada que apunta al este, hacia las montañas, y debajo unas runas que parecen enanas. Por aquí debía de pasar el camino que unía Eregion con los enanos del otro lado de la sierra. Desde la cima ves la montaña de laderas rojizas, más cerca ya, con nubes grises enganchadas en la cumbre. Hacia allí debes ir.

#### `eregion_5_1`

La emboscada del barranco.
Atajas por un barranco estrecho de paredes de roca, cubierto de nieve fresca. Es un error. A mitad de camino, un silbido corto rompe el silencio y por los dos extremos aparecen tus enemigos: un hombre de las Tierras Brunas con una lanza corta y, a su lado, un lobo gris que obedece a sus gestos como un perro de caza. Te habían visto venir desde arriba. No puedes trepar por las paredes ni volver atrás. Aprietas la empuñadura y eliges al primero.

#### `eregion_5_2`

Sombras en el cielo.
Cruzas un llano abierto, sin un árbol, para ganar tiempo. El cielo está bajo y gris, y parece vacío hasta que deja de estarlo: tres crebain bajan de las nubes en picado, sin un graznido, directos hacia ti. Son grandes, de plumas negras y brillantes, y atacan con una astucia que no esperas de unos pájaros, uno por delante y dos por detrás. Más arriba, otros dan vueltas, esperando. Tienes que acabar con estos antes de que vuelvan a contar lo que han visto.

#### `eregion_5_3`

La orilla del río.
Bajas hasta un río estrecho de aguas oscuras y rápidas, y sigues su orilla hacia el este, hacia las montañas, al abrigo de los sauces desnudos. Aquí no hay nadie. En la arena encuentras las huellas de una partida de hombres, más de una docena, que marchó en tu misma dirección hace un par de días, con botas pesadas y algo que arrastraban. Los sigues de lejos, con cuidado. Sea quien sea quien los manda, vigila el camino de las montañas.

#### `eregion_6_0`

La patrulla del collado.
Subes hacia un collado entre lomas, desde donde esperas ver el camino de las montañas. Al llegar arriba te topas de frente con una patrulla que bajaba: un hombre de las Tierras Brunas y tres lobos grises, sujetos con correas que sueltan en cuanto te ven. Por un instante nadie se mueve. Luego el hombre grita algo en su lengua y los lobos se lanzan sobre ti. Más allá del collado, al pie de la montaña roja, se ve el resplandor de una gran hoguera.

#### `eregion_6_1`

La calzada hacia la montaña.
Das con la vieja calzada élfica y la sigues hacia el este, mientras el terreno empieza a subir hacia las montañas. Las lomas se vuelven más empinadas, los acebos escasean y la nieve cubre ya las piedras. Al caer la tarde, el camino pasa junto a unas ruinas grandes, al pie de la montaña roja. Hay luz entre sus muros: una hoguera, voces de hombres, gruñidos de lobo. Para llegar al paso, tendrás que cruzar por delante de ellos.

#### `eregion_7_0`

El capitán dunlendino.
Al pie de la montaña roja, entre los muros de una antigua sala élfica, los hombres de las Tierras Brunas tienen su campamento. Desde allí vigilan el camino del paso. Su jefe sale a tu encuentro a la luz de la hoguera: un hombre enorme, de barba negra y brazos cubiertos de cicatrices, con un hacha en cada mano. Con un gesto ordena a los suyos que se aparten: este combate lo quiere para él solo. El capitán se ríe. Dice que su señor quiere saber quién cruza las montañas, y que tú no las vas a cruzar. Detrás de él, el Caradhras se pierde entre las nubes.

## caradhras

#### `caradhras_1_0`

Al pie del Cuerno Rojo.
Dejas atrás las ruinas del campamento dunlendino, donde las brasas aún humean, y empiezas a subir. El camino del paso trepa por la falda del Caradhras, cuyas laderas tienen bajo la nieve un tono rojizo, como de hierro viejo. Los acebos quedan abajo y, con ellos, el último abrigo. El viento baja de la cumbre con un silbido largo y el cielo del norte se ha vuelto del color del plomo. Te ciñes la capa gris de Rivendel y miras hacia arriba. Al otro lado de esas montañas sigue el rastro que persigues desde Bree.

#### `caradhras_2_0`

El camino del paso.
Sigues el viejo camino, que sube en zigzag por la ladera, medio borrado por la nieve. Al doblar un recodo te detienes: alguien ha pasado por aquí hace pocos días. Hay huellas hondas de botas de hombres altos, otras mucho más pequeñas, casi como de niños, y las pisadas de un poni cargado. Suben hacia el paso en fila, abriendo surco. Sean quienes sean, te han adelantado mientras peleabas en Eregion, y recuerdas los rumores que te acompañan desde Bree. Pisas donde ellos pisaron y sigues subiendo, con el viento de cara.

#### `caradhras_2_1`

La cañada helada.
Dejas el camino y subes por una cañada estrecha que corta la ladera, donde el viento apenas llega. Es más empinada, pero las paredes de roca te protegen y avanzas deprisa. El silencio es casi completo: solo crujen la nieve bajo tus botas y, de vez en cuando, un trozo de hielo que se suelta allá arriba. Entonces ves en la nieve huellas grandes de lobo, muchas, y unas plumas negras enganchadas en las rocas. Esta cañada no está tan vacía como parece. Aprietas la empuñadura de la espada.

#### `caradhras_3_0`

Ojos en la cresta.
Para no perder el rastro subes por una cresta desnuda desde donde se ve media montaña. También a ti se te ve. Dos crebain se separan de las nubes y bajan en picado sobre ti, graznando con voz ronca, mientras otros vigilan desde lo alto. Atacan por turnos, buscándote los ojos con el pico. Si uno de ellos vuelve con su amo, alguien sabrá muy pronto que un viajero solitario sube hacia el paso. No puedes permitir que escapen.

#### `caradhras_3_1`

El lobo del rastro.
Sigues las huellas del grupo paso a paso, sin apartarte del surco que abrieron. A media mañana notas que no eres el único: sobre las pisadas de los viajeros hay otras más recientes, de un lobo grande que también ha seguido el rastro. Lo ves poco después, plantado en mitad del camino, gris y flaco, con los ojos amarillos fijos en ti. No huye ni aúlla. Baja la cabeza y avanza despacio, con la calma de quien ya ha cazado hombres antes.

#### `caradhras_3_2`

La manada de la cañada.
Sigues las huellas de los lobos cañada arriba, con la espada desnuda. El rastro se estrecha entre dos rocas y, al otro lado, los encuentras: dos lobos enormes, de pelo gris sucio, que roen los huesos de una cabra montés. Levantan la cabeza a la vez. No son bestias salvajes; se mueven coordinados, y mientras uno se queda delante, el otro se desliza por la ladera para cerrarte la retirada. Alguien les ha enseñado a cazar así. Tendrás que pelear en la nieve, sin espacio para maniobrar.

#### `caradhras_3_3`

Plumas negras.
Prefieres no cruzarte con lo que dejó esas huellas y trepas por la pared de la cañada, agarrándote a salientes helados. Arriba, en una repisa, descubres de dónde venían las plumas: un nido de crebain, grande y sucio, lleno de huesos pequeños. Cuatro pájaros negros levantan el vuelo a la vez y se te echan encima, chillando, mientras te sujetas con una mano a la roca. Tienes que afirmarte, desenvainar y luchar en una repisa del ancho de una mesa.

#### `caradhras_4_0`

El refugio bajo la peña.
Bajo una peña que hace de tejado encuentras un hueco resguardado del viento, y no eres el primero en usarlo. La nieve está aplastada donde se tumbaron varias personas, hay cenizas de una hoguera y una rama de leña olvidada, y en un rincón, estiércol de poni. El fuego ardió con fuerza: la roca de encima está ennegrecida, como si hubiera soportado una llama más viva que la de la leña. Te sientas donde ellos se sentaron y comes algo. Por un momento te sientes menos solo en la montaña.

#### `caradhras_4_1`

La cornisa.
El camino se reduce a una cornisa que bordea un precipicio, apenas más ancha que tus botas. A la izquierda, la roca; a la derecha, un vacío blanco donde se pierde el viento. Avanzas de lado, con la espalda pegada a la pared y la capa batiendo como una vela. A mitad de camino, un bloque de nieve se desprende sobre tu cabeza y pasa rozándote hacia el abismo. Te quedas inmóvil, sin respirar, hasta que deja de sonar. Luego sigues, paso a paso, hasta pisar terreno firme.

#### `caradhras_4_2`

Voces en el viento.
Te refugias tras un grupo de rocas para recobrar el aliento. El viento ha cambiado: ya no silba, sino que gime, y entre sus ráfagas te parece oír voces lejanas, risas crueles, palabras en una lengua que no conoces. Te dices que es la fatiga. Cierras los ojos y las voces siguen ahí, más cerca, como si alguien hablara justo al otro lado de la roca. Te asomas: no hay nadie, solo nieve que gira. Es como si la propia montaña tuviera voz y no te quisiera aquí.

#### `caradhras_4_3`

La pendiente.
Para ganar tiempo atajas por una pendiente lisa de nieve dura en lugar de seguir el rodeo del camino. Al principio vas bien, clavando los talones. Luego la costra se rompe bajo tu peso y resbalas ladera abajo, cada vez más rápido, hasta que consigues frenar clavando la espada en el hielo. Te quedas tendido, jadeando, con el corazón golpeándote el pecho. Has perdido altura y casi una hora, pero sigues entero. Te levantas, sacudes la nieve de la capa y vuelves a subir.

#### `caradhras_4_4`

La guarida.
Sigues hacia arriba el rastro de la manada hasta una cueva baja que apesta a carne podrida. Entras con cuidado. Está vacía, pero no abandonada: hay huesos roídos, pelo gris en las rocas y, en un rincón, un collar de cuero con una mano blanca pintada. Esos lobos tienen dueño, y su dueño quiere que vigilen este paso. Antes de salir oyes, muy lejos, un aullido al que responden otros. La manada está de caza. Sales de la cueva y sigues subiendo, más deprisa.

#### `caradhras_4_5`

El puente de nieve.
Una grieta profunda corta la cañada de lado a lado, y solo hay un modo de pasar: un puente de nieve helada que la cruza como un arco frágil. Lo tanteas con la espada. Aguanta. Lo cruzas a gatas, repartiendo el peso, sin mirar el azul oscuro que se abre debajo. Cuando estás a punto de llegar, el arco cruje y se hunde detrás de ti con un estruendo sordo. Saltas a la otra orilla y te quedas mirando el hueco. Por aquí ya no hay vuelta atrás.

#### `caradhras_4_6`

La cueva de hielo.
Encuentras una cueva abierta en el hielo, con paredes azules y lisas que brillan con una luz extraña. Dentro no sopla el viento y el silencio es tan grande que oyes tu propia respiración. Te sientas a descansar y, a la luz que se filtra, descubres en la roca del fondo unas runas angulosas, talladas hace mucho. Son marcas enanas: alguien abrió este paso, o lo usó, cuando las montañas aún eran caminos de comercio. Te reconforta pensar que otros sobrevivieron aquí antes que tú.

#### `caradhras_4_7`

El espolón.
Trepas hasta lo alto de un espolón de roca desde donde se domina el paso. La vista te corta el aliento: al este, la cumbre del Caradhras se pierde en un remolino de nubes negras; al oeste, muy abajo, las lomas de Eregion parecen una alfombra gris. Y desde el norte avanza la tormenta, una pared blanca que se traga las montañas una a una. Calculas que tienes pocas horas. Bajas del espolón a toda prisa, decidido a llegar lo más alto posible antes de que te alcance.

#### `caradhras_5_0`

La nieve profunda.
Sigues subiendo hacia el paso. La nieve te llega ya a las rodillas y cada paso cuesta como diez. Empieza a nevar de verdad: copos gruesos, pesados, que caen sin ruido y borran el mundo a tu alrededor. Pronto no ves más allá de unos metros. Te guías por la pendiente, siempre hacia arriba, y por el viento, que te da en la cara. El frío se te mete en los huesos a pesar de la capa. Te repites que el paso no puede estar lejos, y no sabes si te lo crees.

#### `caradhras_5_1`

Lobos en la ventisca.
Bordeas la montaña por una ladera más baja, donde el viento golpea con menos fuerza. Es un respiro corto. Entre los remolinos de nieve aparecen dos siluetas grises que trotan hacia ti sin ruido, una por cada lado. Son lobos grandes y flacos, con los ojos amarillos clavados en tu garganta. Han esperado a que el frío te cansara. Con la nieve hasta las rodillas no puedes huir ni esquivar. Plantas los pies, levantas la espada y esperas el primer salto.

#### `caradhras_5_2`

Piedras desde lo alto.
Avanzas pegado a la pared de roca, buscando su abrigo. Es entonces cuando empieza a llover piedra. Primero cae un guijarro, luego otro, luego un bloque del tamaño de un barril que rebota a tu lado y sigue rodando ladera abajo. Miras arriba y no ves a nadie, solo roca y nubes. Te aplastas contra la pared hasta que cesa. Cuando por fin te mueves, tienes la certeza de que esta montaña no quiere a nadie en sus laderas, y menos a ti.

#### `caradhras_5_3`

Graznidos en la nieve.
Cruzas un campo de nieve abierto, sin una roca donde esconderte. A medio camino, un graznido rompe el silencio: un crebain, un pájaro negro y enorme, da vueltas sobre ti, llamando. Al momento, de la nieve sale un lobo gris que te seguía a distancia, y el crebain baja para atacar con él a la vez, uno por arriba y otro por abajo. Trabajan juntos, como el perro y el halcón de un mismo cazador. Te das la vuelta con la espada en alto; no hay otra salida que pelear.

#### `caradhras_6_0`

La retirada.
Comprendes que la montaña no te dejará pasar y das media vuelta. Bajar es casi peor que subir: la nieve nueva cubre los huecos y cada paso puede ser un resbalón. Al poco oyes aullidos a tu espalda, cada vez más cerca. Cuatro lobos grises bajan tras de ti por la ladera, saltando por la nieve con más soltura de la que tú tendrás nunca. Te alcanzan en un rellano. No tiene sentido correr más. Te vuelves hacia ellos con la espada en la mano.

#### `caradhras_6_1`

El paso cerrado.
Subes hasta la boca del paso, entre dos paredes de roca por donde el viento ruge como un río. Allí la nieve forma un muro más alto que tú. A sus pies encuentras un surco abierto a golpes, como si alguien hubiera intentado cavar un camino a través de él, y luego pisadas de muchos pies que dan media vuelta y bajan. Otros llegaron hasta aquí y no pudieron seguir. Tú tampoco podrás. Miras por última vez hacia el este y empiezas a bajar.

#### `caradhras_7_0`

La Furia del Caradhras.
Entonces la montaña descarga toda su furia. El cielo se cierra, el viento se vuelve un aullido continuo y la nieve llega de lado, tan densa que no ves tus propias manos. Allá arriba algo retumba: un alud se desprende y baja hacia ti como un mar blanco. Esto no es una tormenta cualquiera; hay malicia en ella, una voluntad vieja que quiere enterrarte. No puedes vencer a una montaña. Solo puedes resistir, mantenerte en pie y bajar hacia el oeste, hacia la falda que se curva al sur, donde se esconden los antiguos dominios de los enanos.

## durin

#### `durin_1_0`

El cauce seco.
Cuando por fin dejas atrás la nieve, tienes los dedos entumecidos y la capa tiesa de hielo, pero estás vivo. Bajas hasta las lomas y sigues hacia el sur, bordeando la sierra. Al segundo día das con el rastro de un grupo de viajeros: botas de hombres, pies pequeños y descalzos, las herraduras de un poni. También ellos venían de las montañas y torcieron hacia el sur. Las huellas te llevan a un valle por el que corre el lecho de un río seco, lleno de piedras redondas. Al fondo se alzan paredes de roca gris, cortadas a pico.

#### `durin_2_0`

La escalera del salto.
Sigues el cauce seco, como hicieron los viajeros. Sube entre rocas hasta un salto donde antes debía de caer una cascada; ahora solo gotea un hilo de agua por la piedra. Junto a él, unos peldaños tallados en la roca, gastados y resbaladizos, trepan por el acantilado. Los subes. Arriba te espera algo que no imaginabas: un lago oscuro, quieto como una losa, que llena el valle hasta el pie de las paredes. El viejo camino se hunde en él. No sopla el viento, y el agua huele a podrido.

#### `durin_2_1`

Las alturas del valle.
No te gusta lo encajonado del valle, así que trepas por la ladera para verlo desde arriba. Desde lo alto de un peñasco lo entiendes todo: el río se ha detenido porque algo ha cegado su cauce, y sus aguas forman un lago negro bajo unos acantilados enormes. Al pie de la pared, en la orilla, hay dos acebos enormes arrancados de cuajo y rocas amontonadas, como si un gigante hubiera tenido un ataque de rabia. Detrás de ti, en las colinas, suena un aullido. Te han olido.

#### `durin_3_0`

El poni solitario.
Bordeas el lago por una franja estrecha de tierra, entre el agua y la roca. A medio camino algo se mueve entre las piedras y te llevas la mano a la espada, pero es un poni: un animal flaco, sin carga ni silla, con los ojos desorbitados de terror. Por las herraduras sabes que es el del grupo que sigues. Lo llamas en voz baja, pero no se acerca. Mira el agua, relincha y sale al trote hacia el norte, lejos del lago. Algo lo ha asustado mucho.

#### `durin_3_1`

Trasgos en la escalera.
Te quedas un rato en lo alto de los peldaños, escondido, observando el lago. No tardas en ver movimiento: dos figuras encorvadas bajan por las rocas de la orilla, con armaduras negras mal ajustadas y cimitarras herrumbrosas. Son trasgos, criaturas de las minas, salidos de algún agujero de la montaña. Uno se queda atrás y el otro olfatea el aire y sube derecho hacia ti. Te han visto. En la escalera no hay sitio para huir; tendrás que pelear sobre los peldaños.

#### `durin_3_2`

Los lobos de la ladera.
Sigues por las alturas, saltando de roca en roca, pero los aullidos se acercan. Tres lobos grises aparecen en la cresta, recortados contra el cielo, y bajan hacia ti en línea recta. Son grandes y silenciosos, y se mueven como si alguien los hubiera enseñado a cazar hombres. No hay árbol al que subir ni cueva donde meterse. Buscas una roca que te cubra la espalda y te preparas. Más abajo, el lago negro sigue quieto, indiferente a todo.

#### `durin_3_3`

El borde del acantilado.
Desciendes con cuidado por la ladera hasta lo alto de los acantilados, justo encima del sitio donde viste los árboles arrancados. Desde aquí oyes el agua lamer la piedra allá abajo. Te asomas: la pared cae a plomo, lisa, sin una sola marca, y a su pie las rocas amontonadas forman una barrera junto a la orilla. Algo ha golpeado aquí con una fuerza terrible; hay grietas recientes en la roca, blancas como heridas. Buscas un camino para bajar y lo encuentras: una chimenea estrecha entre dos peñas.

#### `durin_4_0`

Los cazadores del poni.
Corres tras el poni por la orilla, temiendo que se rompa una pata entre las piedras. No eres el único que lo persigue. Un lobo gris sale de entre las rocas y se lanza a por el animal, que se encabrita y huye ladera arriba. El lobo se vuelve entonces hacia ti, con el pelo erizado: prefiere una presa que no corre. Te pones entre él y el camino por donde se fue el poni y levantas la espada. Ese animal va a llegar a casa, si de ti depende.

#### `durin_4_1`

El carroñero.
Sigues la orilla hacia los acantilados. Cerca del muro, el suelo está revuelto: rocas recién caídas, troncos partidos y un olor a cieno que te revuelve el estómago. Entre las piedras, un trasgo solitario hurga en el barro buscando algo de valor. Al verte suelta un chillido y se lanza contra ti con un cuchillo curvo en cada mano, más rabioso que valiente. Si escapa, avisará a los suyos. No puedes dejar que se vaya.

#### `durin_4_2`

La madriguera.
El otro trasgo, el que se quedó atrás, ha huido. Lo sigues entre las rocas de la orilla hasta una grieta de la ladera, medio tapada con ramas, por la que se cuela. Te asomas. Es una madriguera, no una entrada: un agujero estrecho que apesta a humo y a carne quemada, donde otros tres trasgos se levantan de un salto junto a una fogata. Ahora son cuatro, y la luz del fuego brilla en sus hojas. Ya no puedes retroceder sin darles la espalda.

#### `durin_4_3`

Disputa en la orilla.
Bajas por fin a la orilla del lago. Las piedras están cubiertas de un limo verde y resbaladizo, y el agua, tan cerca, parece todavía más negra. Junto a un tronco caído te sorprende una escena extraña: un trasgo y un lobo gris se disputan los restos de algo muerto, gruñéndose el uno al otro. Al verte, olvidan su pelea. El lobo se agacha para saltar y el trasgo, más listo, se aparta para atacarte por el costado. Tienes el lago a la espalda, y no te gusta nada.

#### `durin_4_4`

La manada.
Los dos primeros lobos eran solo la avanzada. Desde la cresta bajan cuatro más, dos por cada flanco, con el hocico pegado a tu rastro. Podrías esconderte, pero te encontrarían. Buscas un paso estrecho entre dos rocas, donde no puedan atacarte todos a la vez, y esperas con la espada en alto. El primero salta; detrás vienen los otros, gruñendo, buscando un hueco para llegar a ti. La pelea será larga, y no hay nadie que vaya a venir a ayudarte.

#### `durin_4_5`

Huida entre los peñascos.
Oyes más aullidos en la cresta y no te quedas a contarlos. Bajas a saltos entre los peñascos, hacia el lago, buscando un sitio donde los lobos no puedan rodearte. La mayoría pierde tu rastro entre las rocas, pero uno, el más grande, no se deja engañar. Te alcanza al pie de la ladera, donde las piedras dan paso a la orilla, y se planta frente a ti con los colmillos al aire. Este no se rendirá. Tendrás que acabar con él.

#### `durin_4_6`

Las puertas cegadas.
Bajas por la chimenea hasta el pie del muro. De cerca, el destrozo es aún mayor: rocas enormes apiladas contra la pared, acebos partidos, barro removido. Detrás de ese montón, sospechas, hay una puerta, aunque no ves ni una rendija. Un trasgo trepa por las rocas tratando de apartarlas, sin éxito, y maldice a gritos en su lengua. Al verte, saca el arma. Parece que también él busca una manera de entrar, o de salir.

#### `durin_4_7`

La grieta de la cumbre.
Recorres lo alto del acantilado buscando otra bajada. Encuentras una grieta reciente que corta la roca de arriba abajo, como si la montaña se hubiera rajado por un golpe. Huele a humedad y a algo rancio. No estás solo: por ella sube un trasgo que lleva un lobo atado con una cuerda. Al verte, el trasgo suelta al animal con una risa aguda y le grita una orden. El lobo salta hacia ti y su amo lo sigue con una lanza. Aquí arriba, un mal paso te mandaría al lago.

#### `durin_5_0`

Las últimas huellas.
Buscas el rastro del grupo de viajeros que bajó de las montañas. Lo encuentras cerca del muro, en una franja de tierra que el agua no ha cubierto: las botas, los pies pequeños, el poni. Las huellas se detienen al pie de la pared y allí se mezclan con otras marcas, anchas y viscosas, que salen del lago como si algo se hubiera arrastrado por el barro. Después, nada: ni sangre ni cuerpos, solo rocas amontonadas contra la piedra lisa. Entraron, comprendes, y algo cerró el camino detrás de ellos.

#### `durin_5_1`

El agua que se mueve.
Te sientas en una roca y observas el lago. Su superficie parece de cristal negro, sin una ola. Pero al rato ves que no está quieta del todo: cerca de la orilla aparecen anillos lentos, como si algo respirara debajo, y una línea de burbujas avanza y se detiene. Hay peces muertos flotando con el vientre al aire. En el barro de la orilla se marcan surcos anchos, recientes, que no ha hecho ningún animal que conozcas. Te alejas del agua sin hacer ruido y no vuelves a darle la espalda.

#### `durin_5_2`

El cauce antiguo.
Te detienes a pensar cómo se formó este lago. No es natural: aguas abajo, una barrera de piedras y tierra ciega el cauce y retiene el agua, que apenas se filtra por encima. Pero lo que te llama la atención es otra cosa. Siguiendo con la vista la línea del viejo cauce hacia los acantilados, comprendes que el río no nacía en un manantial cualquiera: brotaba de la propia montaña, por un canal que ahora queda bajo el agua, junto al muro. Si el agua pudo salir por ahí, quizá un hombre pueda entrar.

#### `durin_5_3`

Líneas a la luz de la luna.
Buscas un rincón seco lejos del agua y descansas hasta la noche, con la espalda contra una roca. Cuando sale la luna y las nubes se abren, ves algo que te deja sin aliento: en la pared lisa del acantilado se encienden unas líneas plateadas, finas como hilos, que dibujan un arco, unos árboles y una estrella. Es una puerta enorme que solo se deja ver a la luz de la luna. Pero está cerrada y tapada por unas rocas caídas, y no conoces su secreto. Al rato las nubes vuelven y el dibujo se apaga.

#### `durin_6_0`

La orilla bajo el muro.
Avanzas por la orilla hacia la base del acantilado, pisando con cuidado entre las piedras cubiertas de limo. El agua está a un paso, negra y quieta, y procuras no mirarla. A tu espalda, sin embargo, oyes de vez en cuando un chapoteo suave, como si algo te siguiera bajo la superficie, a tu mismo paso. Te vuelves y no hay nada, solo anillos que se ensanchan. Aprietas el paso. El muro se acerca, enorme, y a sus pies el agua se arremolina contra unas rocas caídas.

#### `durin_6_1`

Los últimos lobos.
Atajas hacia el muro por un caos de peñascos caídos, saltando de uno a otro. A medio camino te alcanzan dos lobos grises que llevaban rato siguiéndote sin un aullido. El primero salta desde una roca más alta y casi te derriba; el segundo busca tus piernas. Peleas en equilibrio sobre piedras resbaladizas, con el lago brillando abajo como un ojo negro. Sabes que, si caes al agua, quizá no vuelvas a salir.

#### `durin_7_0`

El Guardián del Agua.
Al pie de la pared, a ras del agua, la roca se ha partido: los golpes que han derribado árboles y amontonado rocas han rajado también un viejo canal enano, y por la grieta sale un soplo de aire frío del interior de la montaña. Es tu camino. Te metes en el agua hasta las rodillas para alcanzarla y, en ese momento, el lago hierve. Brazos larguísimos, pálidos y viscosos, surgen de la superficie y se enroscan buscándote. No podrás matar lo que vive ahí debajo; tienes que golpearlo hasta que se hunda y te deje pasar.

## moria

#### `moria_1_0`

Bajo la montaña.
El canal es estrecho, frío y medio inundado. Avanzas a gatas durante lo que parecen horas, hasta que el agua se queda atrás y la galería se ensancha. Enciendes una tea con manos temblorosas. La luz descubre paredes lisas, talladas con una precisión que ningún hombre podría igualar, y escalones que suben hacia la oscuridad. El canal te ha dejado en un nivel bajo; los caminos largos de la ciudad pasan por encima de ti. Estás dentro de Khazad-dûm, el viejo reino de los enanos, al que los hombres llaman Moria. No se oye nada. En algún lugar, al este, tiene que haber una salida.

#### `moria_2_0`

El centinela de la escalera.
Subes por una escalera ancha, de peldaños gastados por miles de pies enanos, que asciende a través de la roca durante un tiempo interminable. Arriba desemboca en un pasillo, y allí brilla una luz rojiza: la antorcha de un trasgo de guardia que se calienta las manos, aburrido, hasta que ve tu tea. Grita y te arroja la lanza. No puedes dejar que dé la alarma en estas salas. Subes corriendo los últimos peldaños, con la espada por delante.

#### `moria_2_1`

El trol de la cisterna.
Prefieres no subir y sigues un túnel bajo que corre hacia el este a ras de la roca. Al cabo de un rato desemboca en una sala con una cisterna de agua quieta, tallada en la piedra. Algo grande bebe en ella: un trol de las cavernas, de piel gris y escamosa, con brazos que casi tocan el suelo. Te huele antes de verte. Se gira, con el agua chorreándole por la boca, y levanta un garrote de hierro. La sala es pequeña y no hay dónde esconderse.

#### `moria_3_0`

La sala de columnas.
Más allá del pasillo se abre una sala tan grande que tu tea no alcanza ni el techo ni las paredes. Solo ves columnas, hileras de columnas talladas como troncos de árboles, que se pierden en la oscuridad como un bosque de piedra. Caminas entre ellas en silencio. De pronto, muy lejos, ves una luz blanca y pálida, distinta de cualquier fuego, que se mueve despacio por otra galería y desaparece. Alguien más camina por estas salas. Quizá sean los viajeros que entraron por la puerta del oeste.

#### `moria_3_1`

Las forjas frías.
Una puerta abierta te lleva a las forjas. Son enormes, con hornos tallados en la roca y yunques del tamaño de un carro, y todo lleva frío muchos años. Por el suelo hay herramientas tiradas, martillos, tenazas, un hacha enana partida por la mitad. Algunas paredes están ennegrecidas por un fuego que no salió de los hornos, y en un rincón ves flechas negras clavadas en la madera. Aquí se luchó. Pasas la mano por un yunque y piensas en quienes trabajaron en él.

#### `moria_3_2`

El gran pozo.
El túnel se abre al borde de un pozo inmenso, tan ancho que tu tea no ilumina el otro lado. Una pasarela de piedra sin barandilla lo rodea. Te asomas con cuidado. Abajo no hay nada, solo negrura, pero desde el fondo sube un aire caliente y seco que huele a hierro y a quemado, como el aliento de una fragua lejana. Durante un instante te parece notar un temblor en la roca. Te apartas del borde con el pelo erizado, sin saber bien de qué tienes miedo.

#### `moria_3_3`

Las minas.
Bajas a las minas por una rampa de piedra. Aquí los túneles son más toscos y se ramifican en todas direcciones, y en las paredes brillan vetas que reflejan la luz de la tea como escarcha. Hay vagonetas volcadas, picos oxidados y huesos que prefieres no mirar. Hace mucho que los enanos no trabajan aquí. Pero el silencio no es completo: muy lejos, en algún túnel, suena el golpe de un pico contra la roca, rítmico y paciente. Alguien sigue cavando en la oscuridad.

#### `moria_4_0`

Huellas en el polvo.
Te diriges hacia donde viste la luz, pero cuando llegas a la galería ya no hay nadie. En el polvo del suelo, sin embargo, quedan huellas que bajan de una escalera y cruzan la galería: botas grandes, pies pequeños y una marca redonda, como la de un bastón que se apoya a cada paso. Se pierden hacia el norte, por una escalera que sube. Entiendes que el canal te ha ahorrado mucho camino: ellos habrán dado un largo rodeo por los niveles de arriba. Durante un momento dudas si esperarlos. Luego te das la vuelta: sea cual sea su camino, no te corresponde cruzarte en él. Tú buscas tu propia salida.

#### `moria_4_1`

La patrulla.
Tomas un pasillo ancho que corre hacia el este. Avanzas deprisa, demasiado. Al doblar una esquina te das de bruces con una patrulla: tres trasgos encorvados con antorchas y, tras ellos, un orco mucho más grande, con cota de malla y un yelmo con cuernos, que gruñe órdenes. Es un capitán. Te mira, sonríe enseñando los colmillos y desenvaina una espada ancha y mellada. Los trasgos se escabullen para cortarte la retirada. Están acostumbrados a cazar en estos pasillos.

#### `moria_4_2`

Los saqueadores.
Registras los talleres que rodean las forjas, por si queda algo útil. Encuentras una lámpara enana sin aceite y poco más. Al salir del último, descubres que no eras el único que buscaba: cuatro trasgos cargados de chatarra entran por la otra puerta, discutiendo por un yelmo abollado. Se quedan quietos al verte. Luego sueltan el botín y sacan los cuchillos, sonriendo. Cuatro contra uno les parece un buen trato.

#### `moria_4_3`

El canal de las forjas.
Un canal de piedra que antes llevaba agua a las forjas corre todavía junto a una pared. Lo sigues. El agua está fría y limpia, y bebes hasta saciarte. El canal te lleva por un pasillo estrecho, lejos de las salas grandes, hasta una pequeña estancia donde debió de vivir algún enano: un banco de piedra, una hornacina vacía, runas en el dintel. Te sientas a descansar. Por primera vez desde que entraste en la montaña, cierras los ojos un momento.

#### `moria_4_4`

La sala de guardia.
Rodeas el pozo por la pasarela, pegado a la pared, hasta una puerta que da a una sala de guardia. Dentro encuentras restos de una batalla antigua: huesos de enanos todavía con sus cotas puestas, escudos partidos, flechas negras. Los últimos defensores murieron aquí, de cara a la puerta. En la pared alguien grabó unas runas apresuradas, torcidas, que no sabes leer. No hace falta: entiendes que es una despedida. Bajas la cabeza un momento antes de seguir.

#### `moria_4_5`

El puente del pozo.
Un puente de piedra estrecho cruza el pozo por el centro, sin barandilla, y decides arriesgarte. A mitad de camino oyes pasos pesados que hacen temblar la piedra. Por el otro extremo aparece un trol de las cavernas con un trasgo subido a la espalda, que lo pincha con una lanza para guiarlo. El trol te ve y ruge. No hay sitio para apartarse ni tiempo para volver. Afirmas los pies en la piedra y esperas, con el abismo a cada lado.

#### `moria_4_6`

Los picadores.
Sigues el golpeteo del pico por los túneles hasta una galería iluminada con antorchas. Allí trabajan varios trasgos, picando la roca bajo la mirada de un orco grande con un látigo. Al verte, los mineros huyen chillando por los túneles laterales, pero el orco no. Es un capitán, ancho de hombros y lleno de cicatrices, y parece casi contento de que algo rompa la monotonía. Restalla el látigo, saca un hacha y viene hacia ti sin prisa.

#### `moria_4_7`

La veta de plata.
Te metes por una veta estrecha que se aparta de los túneles principales, tan baja que tienes que avanzar agachado. Al final se ensancha en una pequeña cámara. En sus paredes brilla un metal que no es plata ni hierro: claro, frío, con un resplandor propio que crece cuando le acercas la tea. Has oído leyendas sobre ese metal y sobre la codicia que despierta. No lo tocas. Descansas un rato a su luz, en silencio, y luego vuelves sobre tus pasos.

#### `moria_5_0`

La tumba en la luz.
Subes por pasillos y escaleras hasta que ves luz de verdad: un rayo de día gris que entra por un hueco alto, al fondo de una sala. Bajo él, en el centro, hay una losa clara con runas grabadas: una tumba. Alrededor, el polvo cubre huesos de enanos y de orcos, hachas melladas, cofres reventados y un libro de hojas hendidas por un tajo. Nadie ha pasado por aquí desde hace años; en el polvo no hay ni una huella. Aquí resistieron los últimos de alguien. No sabes leer lo que dice la piedra, y no tocas nada. Te quedas un momento en silencio y luego sigues tu camino.

#### `moria_5_1`

Tambores en lo profundo.
Entonces empiezan los tambores. Suenan muy abajo, en lo profundo de la montaña, lentos y graves, y hacen vibrar la roca bajo tus pies. Te quedas helado. Al momento, por todas partes, oyes carreras, chillidos y órdenes ásperas: las minas despiertan. Echas a correr hacia el este, pero al salir a una sala te cortan el paso dos trasgos y un orco capitán que grita a sus tropas. No te esperaban, pero no dudan. Tendrás que abrirte camino a golpes.

#### `moria_5_2`

La sala escondida.
Te metes en una sala lateral, tapas la tea con la capa y esperas en la oscuridad. Al poco oyes pasos, muchos y pesados, y voces ásperas en una lengua horrible. Por la rendija de la puerta ves pasar una columna de orcos y trasgos con antorchas, cientos quizá, que marchan hacia el oeste con prisa, como si fueran de caza. Uno se detiene a olfatear junto a tu puerta. Contienes el aliento. Luego sigue a los demás. Cuando el último eco se apaga, tardas en atreverte a salir.

#### `moria_5_3`

El trol del pasillo.
Eliges el pasillo más directo hacia el este, aunque sea el más expuesto. Es ancho y recto, con estatuas de reyes enanos a los lados, y avanzas deprisa. Demasiado. Una de las estatuas se mueve: no es una estatua, sino un trol de las cavernas que dormía de pie en una hornacina. Se despierta con un gruñido, se sacude el polvo de la piel gris y te corta el paso con todo el cuerpo. No hay vuelta atrás: el pasillo es suyo, y tendrás que ganártelo.

#### `moria_6_0`

La sala de la grieta.
Bajas por una escalinata ancha hacia los niveles más hondos de la ciudad. El aire se vuelve más caliente a cada peldaño. Abajo te espera una sala enorme, de techo altísimo, cuyo suelo está partido por una grieta que despide un resplandor rojo, como de brasas. Al fondo adivinas una caída aún mayor y, más allá, un arco que podría llevar a la salida. Oyes tambores a lo lejos, cada vez más rápidos. Algo se acerca, y no son orcos. Aprietas el paso hacia el fondo de la sala.

#### `moria_6_1`

El pasadizo ardiente.
Te metes por un pasadizo estrecho que baja en rampa, retorciéndose como un gusano. Hace calor, cada vez más, y las paredes sudan. Por las grietas de la roca asoma de vez en cuando un brillo rojo, y el humo te hace toser. Oyes tambores a lo lejos, cada vez más rápidos, y otro sonido más hondo, como un fuelle enorme que respira. Al final el pasadizo se abre de golpe a un espacio inmenso y negro, y ante ti, sobre un abismo, se tiende un puente de piedra.

#### `moria_7_0`

La Sombra de Durin.
El puente es un arco de piedra estrecho, sin barandilla, tendido sobre un abismo del que sube un resplandor de fuego. Al otro lado, unas escaleras ascienden hacia la puerta del este. A tu espalda, los tambores callan de pronto y los trasgos huyen chillando. Estás a mitad del puente cuando el calor te golpea: de la grieta se alza una sombra inmensa con un corazón de llamas. No puedes vencerla. Solo puedes resistir sus golpes de fuego y seguir avanzando. En lo peor de la lucha, la sombra se detiene y vuelve la cabeza hacia las salas del oeste, hacia una luz blanca y unos pasos que se acercan, y te deja escapar. Subes las escaleras sin mirar atrás, hacia la puerta del este y la luz del día.

## sombrio

#### `sombrio_1_0`

La puerta del este.
Subes los últimos peldaños a ciegas y, de pronto, el aire cambia: frío, limpio, con olor a nieve. Atraviesas una puerta enorme y rota y sales a la luz del día. Es una luz gris, de cielo encapotado, pero después de tanta oscuridad te hace llorar los ojos. A tus pies se abre un valle largo entre montañas, el Arroyo Sombrío, al que los enanos llamaban Azanulbizar. Al fondo brilla un lago quieto y oscuro. A tu espalda, muy hondo, todavía retumban los tambores. No puedes quedarte aquí.

#### `sombrio_2_0`

La escalera del torrente.
Junto a la puerta nace un camino de escalones tallados que baja por la ladera al lado de un torrente blanco. Lo tomas sin pensarlo: es el camino más corto hacia el fondo del valle. Los peldaños están gastados y resbaladizos por la escarcha, y el agua salta a tu lado con un estruendo que tapa cualquier otro ruido. Eso te preocupa. Cada pocos pasos te vuelves a mirar la puerta, allá arriba, negra como una boca abierta.

#### `sombrio_2_1`

Entre los peñascos.
No te fías del camino de escalones: cualquiera que salga por la puerta lo verá. Te apartas por la ladera, saltando de roca en roca entre brezos secos y peñascos caídos. Es más lento y más cansado, pero desde aquí arriba dominas el valle entero: el lago, el torrente, el río que baja hacia el sureste. Te detienes a recuperar el aliento detrás de una piedra grande. Entonces oyes voces ásperas que bajan de la puerta.

#### `sombrio_3_0`

Trasgos en los peldaños.
Bajas tan rápido como te atreves. A media escalera, unas piedras ruedan desde arriba y te golpean los talones: cuatro trasgos han salido de la puerta tras de ti y bajan saltando los escalones, con los ojos entornados contra la luz. No les gusta el día, pero tienen hambre de caza y saben que estás solo. Te alcanzan en un rellano donde el torrente salpica la piedra. Te vuelves hacia ellos con la espada en la mano.

#### `sombrio_3_1`

El arquero del saliente.
Dejas la escalera y bajas por la orilla del torrente, saltando entre rocas mojadas para ganar tiempo. Una flecha negra se clava en el musgo a un palmo de tu mano. Levantas la vista: en un saliente, junto a la escalera, un arquero orco tensa otra vez el arco, envuelto en trapos para protegerse de la luz. Está solo, pero desde allí arriba puede acribillarte. No hay dónde cubrirse. Trepas hacia él, de roca en roca, antes de que acierte.

#### `sombrio_3_2`

Orcos en la ladera.
Sigues por la ladera hacia el lago, agachado entre los peñascos. Las voces se acercan y, al rodear una roca, te das de bruces con ellos: un orco grande de las montañas, de brazos largos y cimitarra corta, y dos trasgos, uno de ellos cargado con su lanza. Habían salido a rastrear la ladera. El orco se ríe al ver que estás solo y le arranca la lanza al trasgo de las manos. Luego los tres se lanzan contra ti.

#### `sombrio_3_3`

Los ojeadores del risco.
Subes hacia los riscos, pensando que nadie te buscará tan arriba. Te equivocas. En una cornisa, dos figuras vigilan el valle: un orco de las montañas y un arquero con el arco a la espalda. Son ojeadores, enviados a ver quién ha salido por la puerta. El orco te ve primero y gruñe un aviso. El arquero se descuelga el arco y el orco desenvaina. Estás demasiado cerca para huir.

#### `sombrio_4_0`

El lago espejo.
Al pie de la escalera, el camino pasa junto al lago. Es largo y estrecho, de un azul tan oscuro que parece negro, y ningún viento riza su superficie. Los enanos lo llamaban Kheled-zâram. Te asomas a la orilla sin saber muy bien por qué. En el agua ves las montañas del revés, nítidas como talladas, y entre sus cumbres unos destellos pálidos, como de gemas, que el cielo gris no explica. Te quedas mirando más tiempo del que querrías. Al final te apartas, con la sensación de haber visto algo que no era para ti.

#### `sombrio_4_1`

La piedra de Durin.
Cerca de la orilla del lago se alza una columna solitaria, partida por arriba, con runas gastadas por siglos de lluvia. No sabes leerlas, pero recuerdas lo que oíste contar en Rivendel: que aquí, en los primeros días, el primero de los reyes enanos se asomó al lago y decidió fundar su reino bajo estas montañas. Ese reino es la oscuridad de la que acabas de escapar. Apoyas la mano en la piedra fría un instante, como quien saluda a un muerto, y sigues tu camino.

#### `sombrio_4_2`

Las fuentes del río.
Siguiendo el torrente llegas a una hondonada donde brotan varias fuentes de un agua tan fría que te duelen los dientes al beber. De ellas nace un río claro que corre hacia el sureste entre piedras blancas: el Cauce de Plata, te dijeron en Rivendel, el río que baja hacia los bosques. Te lavas la cara y las manos, negras de hollín y de sangre, y por un momento el agua parece llevarse también algo de lo que viste ahí abajo. Llenas el odre y sigues.

#### `sombrio_4_3`

Restos de una vieja batalla.
El pedregal que cruzas no es solo piedra. Entre los brezos asoman yelmos oxidados, puntas de lanza y huesos de orco blanqueados, tan viejos que se deshacen al rozarlos. Aquí se libró una gran batalla hace mucho tiempo, enanos contra orcos, a las puertas mismas del reino perdido. Caminas despacio, con respeto, entre lo poco que queda. Sobre una roca encuentras un hacha enana entera, sin rastro de óxido, pero la dejas donde está. No es tuya, y aquí nadie ha olvidado de quién es.

#### `sombrio_4_4`

Los que salen de la puerta.
Desde lo alto de la ladera vigilas la puerta por la que saliste. Al cabo de un rato sale alguien más: un grupo pequeño de figuras que bajan despacio por la escalera, algunas altas y otras de la estatura de un niño. No corren como quien huye de una batalla, sino como quien lleva un peso encima. Al llegar abajo, uno se deja caer de rodillas y los demás se vuelven hacia la puerta, en silencio, mucho rato. Entiendes que han dejado a alguien allí dentro. No te acercas. No es tu duelo.

#### `sombrio_4_5`

La atalaya caída.
En la ladera encuentras los restos de una atalaya de los enanos: tres muros de piedra labrada y un techo medio hundido. Dentro hay ceniza vieja y un rincón seco, al abrigo del viento. Te sientas por primera vez desde el puente. Te tiemblan las manos. Comes un poco de pan duro de las provisiones de Rivendel y cierras los ojos. Cuando los abres, el cielo se ha oscurecido un poco más. Las sombras del valle se alargan y, con ellas, vuelve el miedo.

#### `sombrio_4_6`

El risco más alto.
Trepas hasta lo alto de un risco desde donde se ve todo el valle. Al oeste, la boca negra de la puerta; abajo, el lago, quieto como un ojo; al sureste, un río que baja entre colinas cada vez más suaves hasta perderse en una mancha lejana de árboles, grises y dorados a la vez, aunque estás en pleno invierno. Has oído historias de un bosque así, y no todas son tranquilizadoras. Pero es el único camino que no te devuelve a la montaña.

#### `sombrio_4_7`

La senda de las cabras.
Bajas de los riscos por una senda tan estrecha que solo pueden haberla abierto las cabras. En un recodo encuentras huellas frescas en el barro: botas de clavos, muchas, que bajan hacia el valle. Orcos. Han salido de la montaña por alguna puerta lateral y ahora andan por el valle, en algún lugar entre tú y el sureste. Sigues las huellas con cuidado, de lejos, hasta que se separan en dos grupos. Uno va hacia el río; el otro, hacia el lago.

#### `sombrio_5_0`

La orilla del río.
Bajas hasta el río que nace en el valle y empiezas a seguirlo hacia el sureste. Corre rápido y claro sobre piedras blancas, y su ruido es lo único que se oye. El camino de la orilla es antiguo, empedrado a trechos por los enanos, y avanzas bien. Poco a poco las montañas se van quedando atrás y el valle se ensancha. Te cruzas con un ciervo que bebe y te mira sin miedo. Por un rato casi olvidas que alguien puede venir detrás de ti.

#### `sombrio_5_1`

El vado vigilado.
Quieres pasar a la otra orilla del río, donde el terreno parece más abrigado, y buscas un vado. Lo encuentras donde el agua se abre sobre un lecho de cantos. Pero no eres el primero en llegar: en la orilla de enfrente, un orco de las montañas llena un odre mientras otro, con un arco negro, vigila. Te ven a la vez que tú a ellos. El arquero dispara; el orco cruza el agua chapoteando, con la cimitarra en alto. Te plantas en los cantos y esperas.

#### `sombrio_5_2`

Un alto en la hondonada.
Te refugias en una hondonada entre rocas grandes, donde el viento no llega, y te permites descansar. La tarde cae deprisa en el valle. Cuando el sol se pone tras las montañas, oyes otra vez los tambores, lejanos, y luego un clamor de voces y cuernos que viene de la puerta. Sabes lo que significa: con la oscuridad, los orcos salen de caza. No piensas quedarte a esperarlos. Te levantas, te ajustas la capa y echas a andar hacia el sureste, más deprisa que antes.

#### `sombrio_5_3`

La avanzadilla.
Apretas el paso para dejar el valle atrás antes de la noche. El camino baja en curvas entre peñas y, al salir de una de ellas, te encuentras con un orco de las montañas sentado en una piedra, comiéndose algo que prefieres no mirar. Es grande, con cota de anillas y un escudo de cuero, y por la forma en que se levanta no parece asustado. Quizá se adelantó al resto de su banda. Escupe, recoge su lanza y se te acerca.

#### `sombrio_6_0`

Los perseguidores.
Cuando cae la noche, decides no detenerte. Caminas junto al río, guiándote por su ruido, con la luna asomando a ratos entre las nubes. Al cabo de unas horas oyes algo a tu espalda: pasos rápidos, respiraciones, un tintineo de metal. Te alcanzan en un tramo abierto de la orilla: dos orcos de las montañas y dos arqueros que corren delante siguiendo tu rastro. Los manda alguien más grande que se ha quedado atrás, pero estos cuatro no esperan. Te das la vuelta para recibirlos.

#### `sombrio_6_1`

El bosquecillo de abetos.
Cuando cae la noche, te metes en un bosquecillo de abetos junto al río y te escondes entre las raíces, envuelto en la capa gris. Al rato oyes pasos en la orilla, muchos, y voces que discuten en su lengua horrible: una banda de orcos sigue el río hacia el sureste, buscando a alguien. Uno de ellos, enorme, da órdenes a gritos. Pasan de largo a pocos pasos de ti. Esperas hasta que el ruido se pierde y sales con cautela. Sabes que van por delante y que tu camino es el mismo que el suyo.

#### `sombrio_7_0`

El capitán de la montaña.
El río se ensancha y las colinas se suavizan. A lo lejos ya distingues, oscura contra el cielo, la línea de un bosque. Entonces, en la orilla, te sale al encuentro un capitán orco de las montañas, más alto que tú, con un yelmo abollado y una cimitarra enorme. Viene solo: ha dejado atrás a los suyos para cobrarse la presa con sus propias manos. El capitán sonríe al verte: llevaba toda la noche buscándote. Luchas con los pies en el barro y el río a tu espalda. Más allá de él, hacia el sureste, te esperan los árboles.

## nimrodel

#### `nimrodel_1_0`

La linde del bosque.
Con el capitán hundido en el río, el camino de la orilla te lleva por fin hasta los primeros árboles. No se parecen a ningún árbol que conozcas: troncos lisos y grises, altísimos, y ramas cargadas de hojas doradas en pleno invierno, que brillan débilmente bajo la luna. Bajo ellos el aire es distinto, más quieto y más limpio. Por el oeste baja otro río a unirse al que traes, cantando entre piedras. En Rivendel oíste su nombre: el Nimrodel. Detrás quedan los orcos; delante, un bosque del que pocos hombres hablan sin bajar la voz.

#### `nimrodel_2_0`

Bajo los árboles dorados.
Te adentras bajo los árboles. La hojarasca dorada cubre el suelo y apaga tus pasos, y la luz de la luna se filtra entre las ramas como polvo de oro. No se oye ningún animal. Tienes la sensación, cada vez más clara, de que no estás solo: de que algo, o alguien, te observa desde muy arriba. Levantas la vista y no ves más que hojas. Aun así, apartas la mano de la espada sin darte cuenta, como quien entra en una casa ajena.

#### `nimrodel_2_1`

Orcos en la orilla.
Prefieres no internarte de noche en un bosque desconocido y sigues la orilla, entre la linde y el agua. Pronto ves que no eres el único que lo ha pensado: cuatro orcos de las montañas, sucios del viaje, están acuclillados junto al río, discutiendo. Uno levanta la nariz, olfatea y se pone en pie. No quieren entrar entre los árboles, pero a ti sí quieren alcanzarte. Avanzan por la orilla, dos por cada lado.

#### `nimrodel_3_0`

Un arquero entre los troncos.
Un sendero apenas visible serpentea entre los troncos y lo sigues. En un claro pequeño, un silbido corta el aire y una flecha negra se clava en el árbol junto a tu cabeza. No viene de arriba, sino de detrás de un tronco caído: un arquero orco que se ha metido en el bosque, encogido y nervioso, con los ojos brillando en la oscuridad. Dispara otra vez, falla, y busca una tercera flecha con dedos torpes. Este lugar le da más miedo que tú. Corres hacia él.

#### `nimrodel_3_1`

La presa disputada.
Sigues el rumor del agua entre helechos secos hasta que oyes otra cosa: un gruñido y una risa áspera. Junto a un arroyo, un orco de las montañas y dos arqueros han acorralado a un ciervo herido y discuten quién se lo queda. Pisas una rama. Los tres se vuelven a la vez. El ciervo aprovecha y huye entre los árboles; los orcos, en cambio, parecen pensar que tú eres una presa mejor.

#### `nimrodel_3_2`

El fuego del claro.
Para alejarte de la orilla, cortas camino por un claro abierto entre la linde y los primeros árboles grandes. Es un error: en el centro arde un fuego pequeño y mal escondido, y alrededor dos orcos, uno de las montañas y un arquero, se calientan las manos. Uno se venda el brazo, donde todavía lleva clavada una flecha de plumas grises. Te ven. Se levantan sin prisa, y uno patea las brasas hacia ti.

#### `nimrodel_3_3`

El arquero del remanso.
Bajas a una orilla de cantos donde un arroyo forma un remanso. La luna brilla en el agua. Al otro lado, entre unas rocas, algo se mueve: un arquero orco agazapado, que vigila el agua y te ha visto. Su primera flecha salta en las piedras a tus pies. Podrías retroceder, pero entonces te seguiría disparando por la espalda. Cruzas el remanso con el agua helada por las rodillas, mientras él busca otra flecha.

#### `nimrodel_4_0`

Silbidos en lo alto.
Te detienes en mitad del sendero y escuchas. Al principio no hay nada. Luego, muy arriba, suena un silbido suave, como de pájaro, y otro que le contesta desde el otro lado. No son pájaros. Te quedas quieto, con las manos lejos de la espada, y dejas que te miren. Al cabo de un rato los silbidos callan, y una hoja dorada cae girando hasta tu hombro. No sabes si es una advertencia o un permiso. Decides tomarlo como un permiso y sigues, despacio.

#### `nimrodel_4_1`

El gran árbol.
Te sientas al pie del árbol más grande que has visto nunca: su tronco gris y liso es tan ancho que cinco hombres no lo abrazarían. Entre las raíces hay un hueco seco y mullido de hojas. Apoyas la espalda en la corteza y notas algo extraño, como un pulso lento y tranquilo, muy hondo. Por primera vez desde que entraste en la montaña, el miedo te suelta del todo. Duermes un poco, sin sueños. Al despertar, la luna se ha movido y tú te sientes más ligero.

#### `nimrodel_4_2`

La flecha de aviso.
Cruzas el arroyo saltando de piedra en piedra. En cuanto pisas la otra orilla, una flecha de plumas grises se clava en la tierra, justo delante de tu bota, y vibra. No es una flecha orca. Te quedas inmóvil. Una voz clara dice algo desde lo alto, en una lengua que suena como el agua; no la entiendes, pero el tono es firme. Levantas las manos despacio, abiertas, y das un paso atrás. Nadie más dispara. Lo que hagas a partir de ahora tendrá testigos.

#### `nimrodel_4_3`

El orco caído.
Sigues las gotas de sangre del ciervo herido entre los helechos, pero lo que encuentras es otra cosa: un orco muerto, tendido boca abajo junto a un tronco, con dos flechas de plumas grises en la espalda. Lleva poco tiempo muerto, y nadie le ha quitado las armas. Quien lo mató lo hizo desde lejos y en silencio, y luego se fue sin dejar huella. Este bosque tiene guardianes, y no les gustan los orcos. Esperas que los hombres les disgusten un poco menos.

#### `nimrodel_4_4`

La piedra de paso.
Te alejas de la linde hacia el interior del bosque. Aquí los árboles son más grandes y la hojarasca más espesa. Encuentras un círculo de piedras con ceniza fría: el fuego de unos viajeros que pasaron hace años. En una de las piedras alguien grabó una runa que reconoces de Rivendel, una señal élfica de paso seguro. Te sientas un momento junto a ella. Hace mucho tiempo que nadie te deja una señal amable en el camino.

#### `nimrodel_4_5`

La columna sin antorchas.
Te escondes entre los helechos altos cuando oyes pasos. Al poco pasa una columna de orcos por la linde, a pocos metros, con prisa y sin antorchas. No entran entre los árboles grandes: los rodean, mirando hacia arriba con miedo. Uno se queda atrás y olfatea el aire en tu dirección. Contienes la respiración. Desde lo alto llega un silbido corto; el orco se estremece, maldice y corre tras los demás. Te quedas quieto mucho rato antes de moverte.

#### `nimrodel_4_6`

La cascada.
Remontas el arroyo por la orilla hasta una cascada pequeña, donde el agua cae por escalones de piedra clara con un ruido que parece una canción. Te sientas en una roca a escucharla. No sabes por qué, pero se te hace un nudo en la garganta: piensas en Bree, en el camino, en todo lo que has visto morir desde entonces. Dejas que la cascada cante por ti un rato. Cuando te levantas, el peso que traías de la montaña es un poco menor.

#### `nimrodel_4_7`

El rastro de la banda.
Sigues la orilla hacia el sur y das con un rastro que no esconde nada: ramas rotas, barro removido, huesos roídos y un escudo abandonado. Una banda de orcos numerosa ha pasado por aquí hace poco, bordeando el bosque sin atreverse a entrar en él, como si buscara una puerta. Por las huellas, no son pocos. Decides apartarte de su camino y te alejas del rastro hacia los árboles, con la espada a mano y los oídos atentos.

#### `nimrodel_5_0`

Arqueros en la niebla.
Una niebla espesa sube del río y se enreda entre los troncos, y avanzas por ella casi a ciegas, guiándote por el rumor del agua. De pronto, cuatro sombras encorvadas surgen delante de ti: arqueros orcos, tan perdidos como tú en la niebla, con las flechas ya en la cuerda. Por un instante nadie se mueve. Luego uno de ellos chilla y los cuatro disparan a la vez. Una flecha te roza la capa. Te lanzas hacia ellos antes de que vuelvan a cargar.

#### `nimrodel_5_1`

Una plataforma en lo alto.
Buscas un árbol de ramas bajas y trepas, más alto de lo que nunca has subido. Entre las hojas doradas encuentras algo inesperado: una plataforma de madera clara, bien atada a las ramas, vacía. Te tumbas en ella, envuelto en la capa. Desde aquí el bosque es un mar de oro bajo la luna. Te duermes. Cuando abres los ojos, crees ver sombras grises sentadas en las ramas de más arriba, mirándote. Parpadeas, y ya no hay nadie. Bajas con cuidado, preguntándote si lo has soñado.

#### `nimrodel_5_2`

Los rezagados.
Atajas por la linde del bosque, donde los árboles son más bajos y avanzas más deprisa. Allí, entre unos matorrales, dos orcos discuten a media voz: uno grande, de las montañas, y un arquero flaco. Se han quedado atrás de su banda y no se atreven a entrar en el bosque solos. Al verte, la discusión se acaba: por fin tienen algo que hacer con su miedo. Te rodean despacio, sin dejarte llegar a los árboles grandes.

#### `nimrodel_5_3`

Una canción lejana.
Te detienes, te apoyas en un tronco y esperas en silencio, sin saber muy bien a qué. Al cabo de un rato llega, de muy lejos, una canción. Son voces claras, muchas, que cantan en lo alto de los árboles en una lengua que no entiendes. No es una canción alegre. Habla de agua, te parece, y de algo perdido hace mucho tiempo. La escuchas hasta el final. Cuando calla, notas las mejillas mojadas y el corazón, de algún modo, más tranquilo.

#### `nimrodel_6_0`

Las aguas del Nimrodel.
Llegas a la orilla del Nimrodel, que baja claro y rápido sobre piedras pálidas. Te quitas las botas y lo cruzas a pie. El agua está helada y te llega a las rodillas, pero no duele: al contrario, a cada paso sientes que se lleva algo, el cansancio, el polvo de la montaña, el recuerdo del fuego bajo el puente. Al llegar a la otra orilla te quedas un momento quieto, con el agua goteándote. Hacía semanas que no respirabas así.

#### `nimrodel_6_1`

El sendero escondido.
Entre dos raíces enormes descubres un sendero estrecho, tan limpio de hojas que alguien tiene que cuidarlo. Lo sigues. Cada vez que dudas en una bifurcación, encuentras una señal: una rama doblada, una piedra blanca, una hoja prendida en una corteza. Nunca ves a nadie, pero sabes que te están guiando, despacio, hacia el este, hacia el lugar donde se juntan los ríos. Te preguntas si es una invitación o una trampa. Decides confiar.

#### `nimrodel_7_0`

El jefe de los arqueros.
Llegas al lugar donde el Nimrodel se une al río que traes desde las montañas, más ancho y frío. Al otro lado, el bosque se vuelve más espeso y más dorado. Pero antes de que encuentres la manera de cruzar, te cierra el paso un orco enorme con un arco negro tan alto como él: el jefe de los arqueros, que ha dejado atrás a su partida para darte caza en persona. Luchas con la espalda contra el río. Muy arriba, entre las ramas doradas, unas figuras vestidas de gris observan la lucha en silencio, con los arcos tensos y una cuerda enrollada al hombro.

## lothlorien

#### `lothlorien_1_0`

Caras Galadhon.
Los elfos te guían en silencio por el bosque durante un par de días hasta una colina verde rodeada por un muro y un foso. Sobre ella se alzan los árboles más altos que has visto, y entre sus ramas brillan cientos de luces, plateadas, verdes y doradas: es Caras Galadhon, la ciudad de los árboles. Te dan de comer, ropa limpia y un lecho alto entre las hojas. Esa noche no sueñas con fuego. Piensas en Bree, en el viento de la Cima, en el vado y en la oscuridad bajo la montaña. En lo alto de la colina, una luz blanca y serena parece velar por todo el bosque, y sabes que alguien te ha visto llegar.

#### `lothlorien_2_0`

Las casas de los árboles.
Por la mañana subes por una escalera blanca que rodea un tronco inmenso. Arriba, entre las ramas, hay plataformas de madera clara, los talan, con paredes de tela gris que el viento mueve como velas. Ahí viven los galadhrim: duermen, cantan y trabajan por encima del suelo, sin que una sola rama parezca forzada. Un niño elfo te mira con curiosidad y se ríe cuando te agarras a la baranda. Desde lo alto, el bosque es un mar de oro que se pierde hacia el este, hasta un brillo lejano de agua.

#### `lothlorien_2_1`

Los caminos de la ciudad.
Recorres a pie los senderos que cruzan la colina. Están pavimentados de piedra blanca y bordeados de lámparas que nadie parece encender ni apagar. Los elfos te saludan con una inclinación breve, sin preguntas, y siguen con lo suyo. Oyes música en algún lugar alto, un arpa y una voz, y el murmullo del agua bajo el musgo. El muro verde rodea la ciudad como un abrazo. Por primera vez desde Rivendel no llevas la mano en la espada. Te das cuenta y la apartas despacio.

#### `lothlorien_3_0`

La sala de la Dama.
Te llevan a lo alto del árbol más grande, a una sala ovalada de paredes verdes y plata. Al fondo, sentados uno junto al otro, están el señor y la Dama del bosque. Te reciben solo un momento, con cortesía, entre otros asuntos más graves: te preguntan de dónde vienes y por qué caminos, y agradecen tus noticias. No es una audiencia larga; hay mensajeros que esperan y voces que hablan en voz baja junto a las columnas. Sales de la sala con la sensación de haber estado muy cerca de algo antiguo e inmenso, y de no haberlo entendido del todo.

#### `lothlorien_3_1`

Los cantos de duelo.
Al caer la tarde, el bosque entero canta. Son cantos lentos, en una lengua que no entiendes, pero la tristeza no necesita traducción. Un elfo que guarda la escalera te explica que lloran a alguien que cayó en las profundidades de la montaña, un viajero gris que muchos conocían desde hacía siglos. Recuerdas el fuego bajo el puente de Moria y el temblor de la piedra. Te sientas en una raíz y escuchas hasta que la última voz se apaga entre las hojas.

#### `lothlorien_3_2`

El guía de la linde.
Buscas al elfo que te guió desde el Nimrodel y lo encuentras afilando flechas al pie de un mallorn. Te recibe con más calidez que en el bosque, ahora que ya no eres un desconocido armado. Te cuenta que vigila la linde norte con sus hermanos y que, desde hace meses, los orcos rondan más que nunca las orillas. Te enseña unas pocas palabras en su lengua, para dar las gracias y para pedir paso. Las repites hasta que dejan de sonar torpes en tu boca.

#### `lothlorien_3_3`

La fuente del jardín.
Sigues el rumor del agua hasta una fuente que brota en lo alto de la colina y baja en un hilo plateado por un canal de piedra. Hay un jardín en silencio, sin muro ni guardias, y sin embargo nadie entra en él sin permiso. Te sientas en la hierba, a distancia. El agua es tan clara que se ve el fondo como si no hubiera agua. Bebes un sorbo del canal y te parece que se te aclara la cabeza, como después de dormir mucho.

#### `lothlorien_4_0`

El espejo.
Al anochecer, una doncella élfica te lleva hasta lo alto de unos escalones y te señala una hondonada del jardín, con una pila de plata sobre un pedestal. Te dice que es el espejo de la Dama, y que nadie se asoma a él sin su permiso. Te quedas en el borde, sin bajar. Desde allí solo ves el agua quieta, y en ella las estrellas. Pero por un instante te parece que tiemblan y dibujan otra cosa: un gran río, dos figuras de piedra enormes y, muy al este, un resplandor rojo. Parpadeas, y vuelven a ser estrellas.

#### `lothlorien_4_1`

El señor del bosque.
El señor del bosque te recibe a solas en una terraza alta. Le interesa tu camino: dónde viste orcos, por dónde rondan los lobos, qué se dice en Bree de las tierras del sur. Te escucha con paciencia y después te habla del río que nace en el norte y corre hacia el mar, y de las tierras que guarda cada orilla. Al despedirte comprendes que no te ha preguntado por curiosidad. Hay muchas fronteras que vigilar, y cada viajero trae noticias.

#### `lothlorien_4_2`

El enano y el elfo.
Junto al pabellón de los huéspedes ves pasar a dos figuras que no esperabas ver juntas: un enano de barba larga, con un hacha al cinto, y un elfo alto de cabellos claros que se ríe de algo que el enano ha dicho. Hablan de árboles y de cuevas como viejos amigos. Te quedas mirando más de la cuenta. El enano te devuelve la mirada con el ceño fruncido, y luego te saluda con la cabeza. No te acercas: son de otro camino.

#### `lothlorien_4_3`

Los tejedores.
En un talan bajo, unas elfas tejen capas en telares de madera clara. La tela es ligera como el aire, y su color cambia según la mires: gris al sol, verde entre las hojas, pardo como la tierra mojada. Te dejan pasar la mano por un paño a medio hacer. Es cálido y no pesa nada. Una de ellas te dice que esas capas no hacen invisible a nadie, pero que ayudan a que los ojos enemigos resbalen. Ojalá hubieras tenido una en la Cima.

#### `lothlorien_4_4`

Los arqueros.
Los guardianes de la linde practican en un claro al pie de la colina, y te dejan probar uno de sus arcos. Es más largo que tú y cuesta tensarlo. Tu primera flecha se pierde entre los helechos; la tercera, al menos, toca el tronco. Ellos no se ríen. Uno te corrige la postura de los pies y otro te enseña a respirar antes de soltar. Al final no has aprendido a tirar, pero sí a mirar con más calma lo que tienes delante.

#### `lothlorien_4_5`

Los cordeleros.
En un taller bajo las raíces, dos elfos trenzan cuerda gris con una fibra que brilla como seda. La cuerda es fina, apenas más gruesa que tu dedo, y sin embargo te aseguran que aguantaría el peso de tres hombres. Te dejan trenzar un palmo; te sale torcido y blando. Uno de ellos lo deshace con paciencia y lo vuelve a empezar delante de ti, más despacio. Piensas que, en el camino que te queda, algún día una cuerda así podría valer más que una espada.

#### `lothlorien_4_6`

Los hornos.
El olor a pan te lleva hasta unos hornos de piedra escondidos entre las raíces. Los elfos que trabajan allí preparan unas tortas finas y doradas que envuelven en hojas. Te dan a probar un pedazo. Es poco más que un bocado, pero al rato te sientes como después de una comida entera. Te cuentan que ese pan dura semanas en el camino y que no se da a cualquiera. Lo dicen sin mirarte, como si no fuera contigo.

#### `lothlorien_4_7`

La colina de las flores.
A media mañana de camino hacia el oeste hay una colina pequeña cubierta de hierba siempre verde y salpicada de flores doradas y blancas. En la cima crecen dos anillos de árboles, y en el centro un mallorn sostiene un talan antiguo. Los elfos la visitan poco y hablan de ella en voz baja, como de un lugar donde el tiempo no pasa. Te tumbas en la hierba. Por un momento no oyes nada, ni el viento, ni tu propia sangre.

#### `lothlorien_5_0`

Una noche en el talan.
Esa noche no puedes dormir y sales al borde del talan. El bosque está oscuro, pero arriba las estrellas parecen más cercanas que en ningún otro lugar de tu viaje. Abajo, alguien canta en voz muy baja. Piensas en el camino que te queda, que ya no sabes dónde acaba, y por primera vez no te asusta no saberlo. Te envuelves en la manta y te quedas allí sentado hasta que el cielo empieza a ponerse gris.

#### `lothlorien_5_1`

El saber de los galadhrim.
Un elfo de pelo gris, uno de los más viejos de la ciudad, accede a hablar contigo. Te cuenta la historia del bosque desde antes de que hubiera hombres en Bree, y te habla del Enemigo, que vuelve a levantarse en su tierra negra del este, más allá del río y de las montañas de ceniza. No te dice nada para asustarte; habla como quien describe el invierno. Cuando terminas de escucharle, el sol ya se ha puesto y tienes mucho en que pensar.

#### `lothlorien_5_2`

Los centinelas del río.
Un grupo de guardianes vuelve de la orilla del gran río y te sientas con ellos junto al fuego. Traen malas noticias: en la ribera este hay orcos que disparan a todo lo que se mueve en el agua, y huargos que bajan a beber al anochecer. Más al sur, dicen, unos rápidos cierran el río y solo se pasan por tierra, con las barcas a hombros. Escuchas en silencio y preguntas de vez en cuando. Pronto, piensas, ese será tu camino.

#### `lothlorien_5_3`

Los mapas del río.
En una sala baja, un elfo extiende ante ti un mapa del gran río dibujado con tinta plateada. Te señala los bosques de las orillas, las islas, los rápidos donde ninguna barca pasa sin cargarla a hombros y, más abajo, dos grandes figuras de piedra a ambos lados del cauce, y después un lago y una cascada que ruge. Más allá, el mapa se vuelve oscuro y vacío. Le preguntas qué hay allí. Se encoge de hombros y enrolla el pergamino.

#### `lothlorien_6_0`

La cena de despedida.
Los elfos preparan una cena sencilla bajo los árboles para despedirte, como si fueras un viejo conocido y no un viajero perdido de Bree. Hay pan, miel y un vino claro que sabe a verano. Al final, la Dama pasa entre las mesas y se detiene un instante a tu lado. Te dice muy poco: que el camino del sur está lleno de sombra, y que la luz que llevas dentro también cuenta. Luego sigue su camino entre las lámparas.

#### `lothlorien_6_1`

Rumores río abajo.
Preguntas por los huéspedes del pabellón gris y un guardián te cuenta que ya se han ido. Partieron hace pocos días, río abajo, en tres barcas grises, con capas de los galadhrim y regalos de la Dama. Iban hacia el sur, aunque nadie sabe adónde exactamente, y algunos dicen que ni ellos mismos lo sabían. Miras el pabellón vacío, con las cortinas recogidas. Otra vez llegas un paso por detrás, como en Bree, como en Rivendel.

#### `lothlorien_7_0`

La partida en barcas.
Al amanecer te llevan hasta la orilla, donde el río plateado se une al gran río. Allí te espera una barca gris, pequeña y ligera. Los elfos te dan una capa como la que viste tejer, pan envuelto en hojas y un rollo de cuerda gris, y te enseñan a remar y a remendar el casco. Un guardián te cuenta que hace pocos días partieron de esta misma orilla tres barcas como la tuya, río abajo, hacia el sur. Luego empujan la tuya y la corriente te lleva. Te vuelves una vez: entre la bruma, el bosque dorado brilla y se aleja.

## anduin

#### `anduin_1_0`

El Río Grande.
El gran río es ancho y lento, de un color gris verdoso que refleja el cielo de invierno. La barca élfica responde al menor golpe de remo, y pronto aprendes a dejar que la corriente haga casi todo el trabajo. A ambos lados se suceden bosques oscuros, cañaverales y orillas de barro. No ves a nadie. Comes un bocado del pan élfico y miras al sur, donde el río se curva y desaparece. Sabes que, en alguna parte delante de ti, tres barcas grises llevan el mismo rumbo.

#### `anduin_2_0`

Las islas.
El río se abre en varios brazos entre islas largas y bajas, cubiertas de sauces desnudos y de juncos. Eliges un canal estrecho y avanzas a golpe de remo entre las ramas que rozan el agua. Aquí la corriente es más mansa y el silencio pesa. Una garza levanta el vuelo delante de ti, y su graznido te sobresalta más que cualquier orco. En la orilla de un islote ves pisadas viejas en el barro, borradas casi por la lluvia.

#### `anduin_2_1`

Flechas desde el este.
Te acercas a la orilla oriental para huir de la corriente fuerte del centro. Es un error. Una flecha negra silba sobre tu cabeza y se clava en el agua, y luego otra, que se hunde en la borda. Entre los matorrales de la ribera asoman dos orcos con arcos cortos, riéndose. No puedes remar más rápido que sus flechas, así que embistes la orilla, saltas al barro y subes hacia ellos con la espada en la mano.

#### `anduin_3_0`

La ribera de los sauces.
Desembarcas en una ribera de sauces para estirar las piernas y buscar leña seca. El suelo está blando y huele a podrido. Entre los troncos encuentras los huesos de un ciervo, limpios y recientes, y alrededor, huellas de patas enormes. Te das la vuelta despacio. Entre las ramas, unos ojos amarillos te observan, y luego un huargo sale del sotobosque, bajo y silencioso, con el lomo erizado. Entre él y la barca solo estás tú.

#### `anduin_3_1`

Los bajíos.
El río se ensancha y pierde hondura, y tienes que bajar y arrastrar la barca por un bajío de piedras. Estás con el agua por las rodillas cuando oyes los aullidos. Dos huargos bajan corriendo por la ladera hacia la orilla, y detrás, un orco con un arco grita órdenes. Atas la cuerda de la barca a una roca y sales del agua a su encuentro antes de que te rodeen en los bajíos.

#### `anduin_3_2`

El carrizal.
Te metes por un brazo muerto del río, entre carrizos más altos que tú, buscando un sitio tranquilo para pasar la noche. El agua está quieta y negra. De pronto una flecha corta los tallos junto a tu cara. Un arquero orco te ha seguido por la orilla, agachado entre los carrizos, y vuelve a tensar el arco. Saltas a tierra firme y corres hacia él antes de que tenga tiempo de apuntar otra vez.

#### `anduin_3_3`

La manada de la orilla.
Acampas en una playa de grava bajo un talud, sin fuego, envuelto en la capa gris. A medianoche te despierta el ruido de piedras que ruedan. Arriba del talud hay sombras que se mueven, cuatro, una detrás de otra, y el viento te trae un olor a perro mojado y a carroña. Una manada de huargos ha olido tu rastro. No hay tiempo para empujar la barca al agua. Te pones en pie, con la espalda contra el río.

#### `anduin_4_0`

Un campamento junto al agua.
En una ensenada de la orilla oeste descubres los restos de un campamento. Hay ceniza fría bajo una capa de hojas, tres surcos en la arena donde alguien varó tres barcas, y huellas de pies pequeños, descalzos, junto a otras de botas. Entre las piedras encuentras una hoja doblada, verde y plata, como las que envolvían el pan de los elfos. No hay sangre ni señales de lucha. Quienes durmieron aquí se fueron tranquilos, río abajo.

#### `anduin_4_1`

Las tierras pardas.
La orilla este se vuelve llana y desnuda, una tierra parda de hierba seca y matorrales quemados que se extiende hasta perderse de vista. No crece un árbol, no canta un pájaro. Aquí y allá ves cenizas viejas y montones de huesos. Remas lo más pegado que puedes a la orilla contraria. Al atardecer, una columna de humo se levanta muy lejos, hacia el este, y luego se deshace. Alguien vive en ese desierto, y no quieres saber quién.

#### `anduin_4_2`

Hogueras en la otra orilla.
Esa noche, amarrado a una rama, ves encenderse hogueras en la orilla este, una tras otra, hasta formar una hilera de ojos rojos a lo largo del agua. Oyes cantos roncos y golpes de metal. No puedes quedarte tan cerca. Te cubres con la capa gris, sueltas la amarra y te dejas llevar por el centro del río, sin tocar el remo. Pasas frente a las hogueras tan despacio que te duele el pecho de contener el aliento. Nadie grita. Al amanecer, el río vuelve a estar vacío.

#### `anduin_4_3`

La niebla del amanecer.
Amanece con una niebla tan espesa que no ves la proa de tu propia barca. El río es solo un rumor y el golpe del remo en el agua. Avanzas a ciegas, guiándote por el sonido de la orilla, y en un momento oyes voces ásperas muy cerca, en una lengua que conoces de Moria. Dejas de remar y contienes la respiración. Las voces se alejan. Cuando la niebla se levanta, el río está vacío y el sol pálido brilla sobre el agua.

#### `anduin_4_4`

El islote de los juncos.
Encuentras un islote pequeño en medio del río, rodeado de juncos, y decides descansar allí un día entero. Nadie puede acercarse sin que lo oigas. Secas la ropa al sol pálido, remiendas la capa vieja y repasas el filo de la espada con la piedra. Comes poco, porque el pan élfico rinde mucho. Por la tarde, un martín pescador se posa en la proa de la barca. Lo miras sin moverte hasta que se va.

#### `anduin_4_5`

Huellas de orco.
En la orilla este, el barro conserva un rastro fresco: botas de clavos, muchas, y entre ellas las marcas de patas grandes. Lo sigues a pie un trecho. El rastro corre paralelo al río, hacia el sur, como si quienes lo dejaron siguieran algo que va por el agua. Encuentras un trapo negro enganchado en una zarza y una flecha rota. Vuelves a la barca deprisa. Lo que sea que persiguen, va delante de ti.

#### `anduin_4_6`

El bosque callado.
En la orilla oeste el bosque llega hasta el agua, viejo y oscuro, con troncos cubiertos de musgo. Desembarcas para buscar agua de un arroyo y te internas unos pasos. Todo está en silencio, pero no es un silencio hostil: es el de un lugar donde nadie ha entrado en mucho tiempo. Bebes, llenas el odre y te quedas un rato escuchando. Por un momento te acuerdas del Bosque Dorado, y te parece muy lejos.

#### `anduin_4_7`

La lluvia.
Una lluvia fría empieza a caer al mediodía y no para en todo el día. El río se pone gris y crecido, y la barca se llena de agua más rápido de lo que puedes achicarla. Te refugias bajo una roca saliente de la orilla, con la barca volcada encima. La capa élfica te mantiene más seco de lo que esperabas. Escuchas la lluvia golpear el casco de la barca, y te duermes pensando en la posada de Bree.

#### `anduin_5_0`

Emboscada en el recodo.
El río dobla en un recodo cerrado, bajo un risco de la orilla este, y la corriente te empuja hacia allí sin remedio. Cuando lo ves, ya es tarde: dos orcos con arcos esperan en lo alto, y en la playa, al pie del risco, dos huargos tiran de sus cadenas. Las flechas te obligan a varar. Saltas a la arena cubriéndote la cabeza con el brazo. Los orcos sueltan las cadenas.

#### `anduin_5_1`

Las estrellas sobre el agua.
Decides viajar de noche, cuando los ojos de la orilla ven menos. El cielo está despejado y las estrellas se reflejan en el río, de modo que parece que flotas entre dos cielos. Apenas tocas el remo. La corriente te lleva despacio y en silencio, y solo de vez en cuando oyes un chapoteo o el grito de un ave nocturna. Al amanecer has recorrido muchas leguas, y nadie te ha visto pasar.

#### `anduin_5_2`

Huargos en el agua.
El río se estrecha entre un islote y la orilla, y la corriente te acerca a tierra más de lo que quisieras. En la ribera, dos huargos te esperaban. Se lanzan al agua sin dudar y nadan hacia la barca, con las orejas aplastadas y los ojos fijos en ti. Uno llega a apoyar las patas en la borda y casi te vuelca. Lo apartas a golpes de remo, alcanzas una barra de grava y saltas a ella, espada en mano, mientras los dos salen del agua chorreando.

#### `anduin_5_3`

Señales en la orilla.
En una roca lisa de la orilla oeste alguien ha dejado una señal: tres piedras pequeñas, una encima de otra, y una flecha dibujada con carbón que apunta al sur. No es obra de orcos. Más allá, en un árbol, ves la marca fina de una cuerda que alguien ató para amarrar una barca. No sabes si las señales eran para ti o para otros que vienen detrás, pero te dan ánimo. Alguien ha pasado por aquí, y ha seguido adelante.

#### `anduin_6_0`

Las colinas de piedra.
Tras muchos días de río, la tierra empieza a alzarse a ambos lados. Los bosques se aclaran y aparecen colinas grises de roca desnuda, cada vez más altas y abruptas. El río se estrecha y la corriente gana fuerza; la barca corre ahora sola, y tienes que usar el remo para mantenerla lejos de las piedras. El aire es más frío. Delante, entre las colinas, el agua se vuelve blanca en algunos puntos.

#### `anduin_6_1`

Un rumor lejano.
Una mañana, al despertar, oyes un sonido nuevo: un rumor sordo, constante, que viene del sur y no se parece al viento. Pegas la oreja a la roca y lo sientes también allí, como un latido. Son rápidos, muchos y fuertes, y aún están lejos. En la orilla este se ven más hogueras que nunca. Comprendes que el río se va a cerrar delante de ti, y que quien quiera cruzar por ahí tendrá que pasar entre enemigos.

#### `anduin_7_0`

El cabecilla de los saqueadores.
En un paso estrecho, justo antes de los rápidos, el río corre entre dos peñas. De pronto la barca choca contra algo: una cadena tendida bajo el agua. En la peña este, un orco enorme con un arco largo y un alfanje al cinto se levanta riendo. Es el cabecilla de los saqueadores del río, que vive de lo que el agua le trae. Está solo en la peña y no necesita a nadie más: ya tiene una flecha en la cuerda. Saltas a la roca más cercana y trepas hacia él, con la barca atrapada.

## argonath

#### `argonath_1_0`

El rugido de Sarn Gebir.
El rumor se ha convertido en rugido. Delante, el río se rompe en un caos de rocas y espuma blanca que ninguna barca podría atravesar entera: son los rápidos de Sarn Gebir. Remas con todas tus fuerzas hacia la orilla oeste y varas la barca en una playa de piedras, con los brazos temblando. Las colinas se alzan a ambos lados, grises y peladas. Tendrás que llevar la barca a hombros hasta donde el río vuelva a calmarse.

#### `argonath_2_0`

Huargos en el porteo.
Buscas el viejo camino de porteo que te describieron los elfos y lo encuentras: una senda que sube por la orilla oeste, lejos del agua. No eres el único que la conoce. A medio camino, dos huargos te esperan sentados en mitad de la senda, como perros que guardan una puerta. Se levantan al verte, sin prisa. Dejas la barca en el suelo, con cuidado, y desenvainas.

#### `argonath_2_1`

La orilla de piedra.
No das con el camino de porteo, así que bordeas los rápidos por la misma orilla, cargando la barca y dejándola en el suelo a cada salto entre las rocas. La espuma te empapa la cara. En una piedra plana ves una flecha clavada en una grieta, negra y de plumas oscuras, que vino de la otra orilla. Más allá hay otras, decenas, esparcidas entre las rocas y hundidas en la arena. Te preguntas a quién buscaban tantas flechas, y si alguna dio en el blanco.

#### `argonath_3_0`

La senda de los porteadores.
Cargas la barca a hombros. Es ligera, pero el camino es largo y empinado, y pronto te arden las piernas. La senda es antigua: hay escalones tallados en la roca, gastados por siglos de pies. En algunos tramos ves marcas recientes, rozaduras en la piedra como las que dejaría la quilla de una barca al arrastrarla, y huellas de botas y de pies pequeños. Llegas a una explanada y te tumbas, con la barca a tu lado.

#### `argonath_3_1`

Restos de una lucha.
Desde lo alto de la senda miras hacia la orilla este. Entre las rocas del otro lado ves hogueras apagadas y arcos rotos, abandonados con prisa. En tu orilla, en cambio, hay flechas negras clavadas en la arena y en los troncos, todas tiradas desde allí. Alguien cruzó el río de noche bajo esa lluvia de flechas y llegó a este lado. No ves sangre en las piedras ni restos de barcas rotas. Quien pasó por aquí lo hizo deprisa y bien, y se marchó antes del alba.

#### `argonath_3_2`

El explorador uruk.
Dejas la barca un momento al pie y subes a las rocas altas para ver el camino desde arriba, y te encuentras de frente con un orco que no se parece a los de Moria. Es grande, de piel oscura y piernas largas, y no le molesta la luz del día. En el escudo lleva pintada una mano blanca. Te mira con desprecio, como a un insecto, y desenvaina una espada ancha y corta. No hay a dónde huir.

#### `argonath_3_3`

La Mano Blanca.
Sigues un rastro de botas que cruza el porteo de oeste a este y te topas con tres uruk-hai que bajan hacia el río. Llevan cascos con una runa blanca y escudos con una mano pintada. No vienen de Mordor, piensas: vienen del oeste, de alguien que no es el Ojo pero que también busca algo. Uno te señala y gruñe una palabra. Los otros dos se separan para rodearte.

#### `argonath_4_0`

La barca herida.
Al dejar la barca en el suelo ves que una roca le ha abierto una grieta en el costado. Te sientas a repararla con lo que tienes: resina de un pino, un trozo de la capa vieja y un palmo de cuerda élfica. Trabajas despacio, como te enseñaron en la orilla del bosque dorado. Al terminar, la grieta apenas se ve. La barca no es tuya, piensas, solo te la han prestado, y quieres devolverla al río entera.

#### `argonath_4_1`

La calzada antigua.
La senda se convierte durante un trecho en una calzada de losas, rota y cubierta de líquenes. A los lados hay restos de muros y un pilar caído con un emblema borrado: un árbol, quizá, y unas estrellas. Hombres de un reino antiguo construyeron este camino cuando el río era suyo. Ahora solo pasan orcos y viajeros perdidos. Acaricias la piedra fría y sigues, con la barca a cuestas.

#### `argonath_4_2`

La sombra alada.
Un frío repentino te para en seco. El sol se apaga como si una nube lo tapara, pero no hay nubes. Muy alto, sobre la orilla este, una sombra con alas enormes cruza el cielo hacia el sur. Te tiras bajo una roca y te quedas quieto, con el corazón golpeando. Conoces ese frío: lo sentiste en la Cima, y en el vado. La sombra se aleja. Pasa mucho rato antes de que te atrevas a salir.

#### `argonath_4_3`

La poza.
Bajas a una poza tranquila, a un lado de los rápidos, para llenar el odre. Al inclinarte ves en el agua el reflejo de algo detrás de ti. Un huargo solitario, más grande que los otros, con una cicatriz que le cruza el hocico, ha bajado sin hacer ruido. No ataca enseguida. Te mide, como si ya hubiera cazado hombres antes. Te incorporas despacio, con el agua goteándote de la mano.

#### `argonath_4_4`

El campamento uruk.
Sigues un hilo de humo hasta una hondonada entre las rocas. Hay un campamento: un fuego mal apagado, huesos roídos y armas apiladas, y cuatro uruk-hai que discuten en voz alta en una lengua áspera. Uno arroja un escudo con la mano blanca contra una piedra. Te agachas, pero el viento cambia, y el más cercano levanta la cabeza, olfatea y te mira directamente.

#### `argonath_4_5`

El escudo abandonado.
En la ladera encuentras un escudo negro abandonado entre las piedras, con una mano blanca pintada en el centro. Al lado hay un yelmo con una runa en forma de rayo, también blanca. Los dejas donde están. Ningún orco de las montañas lleva esas señales. Recuerdas lo que decían en Eregion de los cuervos y de la torre del mago en el sur. Alguien más, además del Ojo, ha enviado a sus siervos a este río.

#### `argonath_4_6`

La garganta.
La senda se mete en una garganta estrecha, con paredes de roca que se cierran sobre tu cabeza. El estruendo del río llega multiplicado por los ecos, y tienes que gritar para oírte a ti mismo. Llevas la barca de lado para que quepa. En algún punto la luz casi desaparece. Avanzas palmo a palmo, rozando la piedra con los codos, hasta que por fin ves claridad al otro lado.

#### `argonath_4_7`

El remanso.
Al pie de los últimos rápidos el río forma un remanso amplio y oscuro, donde la espuma gira despacio antes de seguir. Bajas la barca hasta el agua con cuidado y la dejas flotar. Aguanta. La pruebas remando en círculo, cerca de la orilla, hasta que confías en ella otra vez. Los brazos te duelen de llevarla, pero sentir de nuevo la corriente bajo el casco es como volver a respirar.

#### `argonath_5_0`

El fin de los rápidos.
Los rápidos quedan atrás. El río vuelve a ser uno solo, profundo y rápido, encajado entre colinas cada vez más altas. Vuelves a la barca y te dejas llevar, atento a las piedras. El rugido de Sarn Gebir se apaga a tu espalda y en su lugar oyes el silbido del viento entre las rocas. Comes un bocado de pan élfico y cuentas los días. Ya has perdido la cuenta, y eso te inquieta más que los orcos.

#### `argonath_5_1`

Los acantilados.
Las colinas se convierten en acantilados verticales que encajonan el río. El agua corre oscura y profunda entre paredes que te tapan el cielo. Arriba, muy arriba, ves pasar pájaros negros que no graznan. Apenas hay orillas donde varar: si algo te ataca aquí, casi solo te queda remar. Sigues adelante, pegado a una de las paredes, y la luz del atardecer pinta de rojo la roca.

#### `argonath_5_2`

Cuernos en la ladera.
Desde lo alto de una ladera oyes, lejos, el sonido de un cuerno ronco y áspero. Otro le responde más al sur, y luego un tercero, al oeste. No son señales de caza: son patrullas que se llaman unas a otras, muchas, repartidas por las colinas. Los siervos de la Mano Blanca peinan esta tierra en busca de algo, y tú estás en medio. Te quedas inmóvil, intentando adivinar de dónde vienen. El viento se lleva el sonido y no vuelve.

#### `argonath_5_3`

Noche en la grieta.
Encuentras una grieta en la roca, bastante honda para meterte en ella con la barca, y decides pasar allí la noche. No enciendes fuego. Desde dentro oyes pasar patrullas por la ladera: pasos pesados, gruñidos, el roce de una lanza contra la piedra. Te envuelves en la capa gris y te quedas quieto. Nadie se asoma. Al amanecer sales entumecido, pero entero, y sigues hacia el sur.

#### `argonath_6_0`

Figuras en la niebla.
El río se ensancha un poco y una niebla baja cubre el agua. A lo lejos, río abajo, dos formas enormes se alzan a ambos lados del cauce, más altas que cualquier torre que hayas visto. Al principio crees que son montañas, pero tienen hombros, y cabezas coronadas. Recuerdas el mapa de los elfos. Sientes miedo y algo parecido al respeto. Remas hacia ellas despacio.

#### `argonath_6_1`

La patrulla de la orilla.
Cuando la orilla oeste se abre en una franja de grava, bajas a tierra y sigues a pie, tirando de la barca con la cuerda. Una patrulla te corta el paso: dos uruk-hai con la mano blanca en el escudo y dos huargos que tiran de sus correas. Los uruk sonríen al verte solo. Uno suelta a su huargo y le grita algo. No hay tiempo para subir a la barca. Atas la cuerda a una raíz y te plantas en mitad de la senda.

#### `argonath_7_0`

Los reyes de piedra.
Las dos figuras ya no dejan dudas: son reyes de piedra, tallados en la roca, con la mano izquierda alzada hacia el norte como si ordenaran detenerse. La corriente te arrastra hacia la puerta que forman. Pero a sus pies, en una repisa de roca sobre el agua, te espera un uruk más grande que los demás, con el yelmo pintado de blanco: el capitán de la Mano Blanca, solo, con la espada desnuda. Varas como puedes y subes a la repisa a enfrentarte con él.

## amonhen

#### `amonhen_1_0`

El lago de los tres montes.
Más allá de los reyes de piedra, el río se abre en un lago largo y quieto, encerrado entre colinas. En el centro se alza una isla alta y escarpada, y al fondo retumba un trueno que no cesa: una gran cascada que todavía no ves. En la orilla oeste, al pie de una colina cubierta de bosque, un prado verde baja hasta el agua. Varas allí la barca y la dejas sobre la hierba. Los elfos llamaban a esa colina Amon Hen, la del Ojo. Ningún pájaro canta. Desde el oeste, muy lejos, llegan toques de cuerno roncos, cada vez más débiles, que se alejan.

#### `amonhen_2_0`

Los cuervos del prado.
Cruzas el prado hacia los árboles. La hierba está aplastada en anchas franjas, como si una multitud hubiera corrido por ella hace muy poco, y hay manchas oscuras sobre el verde. En el lindero, una bandada de cuervos negros se alza de golpe de entre los helechos. La mayoría se aleja hacia el oeste graznando, pero dos dan una vuelta sobre tu cabeza y bajan en picado hacia ti, con las garras por delante. Son crebain, pájaros espías que no se comportan como cuervos.

#### `amonhen_2_1`

La orilla del lago.
Prefieres no entrar todavía en el bosque y sigues la orilla hacia el sur, por una franja de guijarros. El agua del lago está tan quieta que refleja las colinas boca abajo. Encuentras los restos de un muro viejo, cubierto de musgo, y un poco más allá unos escalones de piedra que suben entre los árboles hacia la cumbre. Al sur, el trueno de la cascada crece, y una columna de vapor blanco se levanta sobre el agua. Te sientas un momento en una piedra. Todo está demasiado callado.

#### `amonhen_3_0`

Bajo los árboles.
Bajo los árboles el suelo está revuelto, lleno de ramas rotas y de flechas negras clavadas en los troncos. Entre los helechos yacen orcos muertos, muchos, de dos clases distintas. Unos son grandes, con escudos en los que hay pintada una mano blanca. Otros, más pequeños, llevan un ojo rojo en el yelmo. Dos señores, piensas, y ninguno amigo. No sabes quién luchó aquí contra ellos, ni si sobrevivió. Caminas despacio, sin tocar nada, con la mano en la empuñadura.

#### `amonhen_3_1`

El arroyo enturbiado.
Un arroyo baja de la colina hacia el lago, y quieres llenar el odre. Pero el agua viene turbia, revuelta por cientos de pies que la han cruzado más arriba. En el barro de la orilla hay huellas de botas herradas, tantas que se pisan unas a otras, y todas van en la misma dirección: hacia el oeste, lejos del lago. Algunas son muy hondas, como de quien carga un peso. Esperas a que el agua se aclare y bebes, mirando hacia donde se pierde el rastro.

#### `amonhen_3_2`

Los escalones antiguos.
Subes por los escalones antiguos. Son anchos y gastados, y alguien los talló hace siglos para que los reyes subieran a la cumbre sin fatigarse. Entre las piedras crecen raíces y musgo. A medida que asciendes, el bosque se aclara y el lago aparece debajo, cada vez más pequeño, con la isla en medio como la proa de un barco de piedra. Te detienes a recobrar el aliento en un rellano. Arriba, contra el cielo, ves algo que parece un asiento tallado.

#### `amonhen_3_3`

La senda de la cascada.
Sigues la orilla hacia el estruendo. La senda se estrecha entre el agua y una pared de roca, y el aire se llena de una llovizna fina. Al doblar una peña te encuentras con dos uruk-hai que vuelven de la orilla con las botas mojadas. Sobre el hombro de uno se posa un cuervo negro, que grazna y te señala con el pico. Los uruk-hai se separan sin decir palabra para cerrarte la senda por los dos lados, y el cuervo levanta el vuelo.

#### `amonhen_4_0`

La fuente del bosque.
Lejos de los muertos, en una hondonada del bosque, encuentras una fuente que brota de una piedra y cae a una pila tallada, cubierta de hojas. El agua está fría y limpia. Te lavas la cara y las manos, y luego te sientas con la espalda contra la piedra a comer un trozo de pan élfico. Por primera vez en todo el día oyes un pájaro. Cierras los ojos un momento. Cuando los abres, la luz ha cambiado y la tarde avanza.

#### `amonhen_4_1`

Polvo en el oeste.
Subes a un claro desde el que se ve el oeste. Más allá de la colina la tierra baja hacia praderas inmensas, verdes y pardas, que se pierden en el horizonte. Muy lejos, una mancha oscura avanza por ellas, levantando una nube de polvo. Es una tropa grande, que corre sin descanso. No puedes distinguir quiénes son, pero sabes de dónde vienen: de este bosque, de entre estos muertos. La sigues con la vista hasta que la mancha se confunde con la tierra.

#### `amonhen_4_2`

Tres rastros más.
Sigues el rastro de los orcos un trecho, hasta donde la colina se acaba. Allí ves algo que no habías visto: encima de las huellas herradas, más frescas, hay otras de solo tres caminantes. Dos son de botas largas y ligeras; el tercero, más corto y pesado, hunde el talón en el barro. Corren detrás de los orcos, y por la distancia entre los pasos, corren mucho. No los alcanzarías nunca. Te quedas mirando el rastro, y luego te das la vuelta hacia el lago.

#### `amonhen_4_3`

El rezagado.
Rodeas una espesura de acebos y casi tropiezas con él. Es un uruk-hai sentado contra un tronco, con una pierna vendada con trapos sucios. Se ha quedado atrás, solo, y lo sabe. Está afilando una espada corta sobre una piedra, y al verte no se asusta: sonríe, deja la piedra y se levanta apoyándose en el árbol. Cojea, pero la espada no le tiembla en la mano. No piensa dejarte marchar para que cuentes que lo viste.

#### `amonhen_4_4`

El asiento de piedra.
En la cumbre hay una plataforma de losas y, sobre cuatro pilares, un asiento de piedra al que se sube por una escalera. No te atreves a sentarte en él; no es para ti. Te quedas a su lado, con la mano en la piedra fría. Aun así, desde aquí se ve muy lejos. Al este, más allá del río, hay colinas grises y rotas; detrás, una llanura que brilla como agua sucia; y más allá, una línea de montañas oscuras bajo una nube que no se mueve.

#### `amonhen_4_5`

Los vigías de la ladera.
Te apartas de los escalones para explorar la ladera alta, entre rocas y pinos retorcidos. Llegas a un saliente y te encuentras con cuatro uruk-hai que han subido hasta allí a mirar el lago. Señalan las orillas, discuten, uno escupe hacia el agua. Buscaban algo desde lo alto y no lo han encontrado. Tu llegada les parece un premio de consolación. Se reparten a tu alrededor sobre la roca, y a tu espalda solo queda el vacío.

#### `amonhen_4_6`

El borde de la cascada.
Llegas al borde de la cascada. Aquí el lago se acaba de golpe y todo el río cae por un escalón de roca hacia un abismo de espuma, tan hondo que no ves el fondo. El ruido te llena el pecho y te impide pensar. Sobre la niebla se forma un arco de colores. Más abajo, el río sigue hacia el sur, ancho y lento, entre tierras bajas. Ninguna barca pasaría por aquí entera. Te quedas mucho rato mirando, empapado y sin poder apartar los ojos.

#### `amonhen_4_7`

El embarcadero antiguo.
Entre los juncos descubres un embarcadero de piedra, tan viejo que las olas han redondeado sus bordes. Tiene argollas de hierro comidas por el óxido y unos peldaños que bajan hasta el agua. Alguien amarraba aquí sus barcas cuando estas colinas tenían dueño. Te sientas en el último peldaño, con los pies cerca del agua, y miras la isla del centro del lago, inalcanzable, coronada de pájaros que dan vueltas sin posarse nunca.

#### `amonhen_5_0`

Noche sin fuego.
La noche te alcanza lejos del prado, y la pasas bajo una roca saliente, envuelto en la capa gris. No enciendes fuego. El frío baja de la colina y el estruendo de la cascada no se calla nunca. A medianoche te despiertas y ves, muy al este, un resplandor rojo bajo las nubes, que crece y mengua como una brasa a la que alguien sopla. No vuelves a dormirte. Cuando por fin amanece, el resplandor se ha borrado y el cielo es gris.

#### `amonhen_5_1`

Niebla en el bosque.
Al amanecer, una niebla blanca sube del lago y se mete en el bosque. Los troncos aparecen y desaparecen a tu alrededor como figuras que se acercan y se alejan. Caminas despacio, contando los pasos, guiándote por el rumor de la cascada. En un momento oyes un aleteo muy cerca, y un graznido, y luego nada. Te quedas quieto hasta que el silencio vuelve. Cuando la niebla se rasga, estás en la ladera, más cerca del agua de lo que creías.

#### `amonhen_5_2`

La atalaya en ruinas.
Entre los árboles de la ladera encuentras las ruinas de una atalaya: un círculo de piedras labradas y un pilar todavía en pie, con un yelmo alado tallado en lo alto. Los hombres que la construyeron vigilaban desde aquí el río y la frontera del este. Ahora crecen ortigas donde dormían sus centinelas. Te apoyas en el pilar y miras al este, como debieron de mirar ellos. La tierra del otro lado sigue siendo gris y hostil. Nadie la vigila ya.

#### `amonhen_5_3`

La senda de los ciervos.
Encuentras una senda estrecha, abierta por los ciervos, que baja en zigzag por la ladera entre helechos y piedras. La sigues porque es la única que no han pisado botas herradas. Al cabo de un rato ves a los ciervos: tres hembras y un macho joven, inmóviles entre los árboles, mirándote. No huyen. Pasas a su lado despacio, casi sin respirar, y ellos te siguen con los ojos hasta que la senda gira. Es lo único vivo y tranquilo que has visto en esta colina.

#### `amonhen_6_0`

El cuervo grande.
Bajas por el bosque hacia el prado. Desde hace rato te sigue un cuervo, más grande que los demás, saltando de rama en rama sin graznar. Cuando te detienes, se detiene. Cuando echas a andar, te adelanta y te espera. Al final, en un claro, se posa en una rama baja, tan cerca que ves sus ojos. Sabes que, si lo dejas volar, llevará tu cara a alguien. Antes de que levantes la espada, se lanza a picotearte los ojos.

#### `amonhen_6_1`

Saqueadores entre las piedras.
Bajas por un pedregal que se abre entre los árboles, sin cubierta, a la vista de todos. Al pie, dos uruk-hai rebuscan entre las cosas que dejaron sus muertos: cinturones, cuchillos, un odre. Sobre ellos, dos cuervos negros esperan su parte. Los pájaros te ven primero. Graznan, y los uruk-hai levantan la cabeza a la vez. Uno arroja lo que tiene en la mano y desenvaina; el otro silba, y los cuervos se dejan caer sobre ti.

#### `amonhen_7_0`

El caudillo de la orilla.
Vuelves al prado al caer la tarde. Tu barca sigue donde la dejaste. Pero un poco más allá hay un surco en la arena, donde alguien empujó otra barca al agua con la proa hacia la orilla de enfrente, y huellas pequeñas, descalzas, que entran en el lago. Inclinado sobre ellas está el uruk más grande que has visto, con un yelmo de cuernos y una espada ancha, olfateando el rastro como un perro. Los suyos ya marchan hacia el oeste con otra presa; solo él se ha quedado atrás. El caudillo te mira y se ríe.

## emynmuil

#### `emynmuil_1_0`

La orilla de enfrente.
Esa noche cruzas el lago en tu barca, hacia la orilla de enfrente, por donde se fueron los dueños de aquellas huellas pequeñas. Remas despacio, con la cascada rugiendo a tu derecha. Al amanecer varas en una playa de piedras, bajo una colina pelada, y escondes la barca entre las rocas. No volverás a por ella: adonde vas no hay río. Delante se alzan las colinas de Emyn Muil, un laberinto de roca gris cortado por barrancos y grietas, donde el viento silba y casi nada crece.

#### `emynmuil_2_0`

El barranco ciego.
Te metes por el primer barranco que se abre hacia el este. Las paredes son de roca gris, lisa y fría, y el suelo está lleno de piedras sueltas que ruedan bajo tus botas. Al cabo de una hora el barranco se cierra en un muro. Vuelves atrás, eliges otro, y también se cierra. A media tarde comprendes que estas colinas no se cruzan andando derecho: hay que leerlas como un acertijo. Te sientas, bebes un trago y miras las grietas con más paciencia.

#### `emynmuil_2_1`

La cresta del viento.
Decides subir antes de perderte, y trepas por una cresta de roca hasta lo alto. Allí el viento te golpea de lado, cortante, y tienes que agacharte para no perder el equilibrio. Desde arriba ves el laberinto entero: crestas paralelas, barrancos que se cruzan, pozos de sombra donde no llega el sol. Todo es gris. Eliges con la vista una línea que parece seguir hacia el este, y la memorizas piedra a piedra antes de bajar.

#### `emynmuil_3_0`

Ojos en la pared.
Sigues una grieta honda que parece correr hacia el este. Es tan estrecha que el cielo es solo una cinta blanca sobre tu cabeza. De pronto, algo se mueve en la pared, a la altura de tus ojos: hay una repisa, y en ella un lobo de pelaje gris, del mismo color que la piedra, tan quieto que habías pasado a su lado sin verlo. Ahora te enseña los dientes. Salta desde la repisa sin un aullido, directo a tu garganta.

#### `emynmuil_3_1`

El sendero de los orcos.
Entre las piedras encuentras un sendero pisado, el primero que ves en estas colinas. Hay huellas de botas pesadas y, en una roca, alguien ha pintado con algo rojo un ojo sin párpado. Lo sigues con cautela. En un recodo te das de frente con dos orcos que suben. Son distintos de los uruk-hai: más bajos, de brazos largos, con escudos negros y el mismo ojo rojo. Se quedan un instante quietos, sorprendidos. Luego chillan y cargan.

#### `emynmuil_3_2`

La roca de la cabeza de caballo.
Caminas por las alturas siguiendo la línea que memorizaste. Durante horas todo va bien. Pero al atardecer reconoces una roca partida con forma de cabeza de caballo, la misma junto a la que pasaste a mediodía. Has dado un rodeo enorme y has vuelto al principio. Te quedas mirándola, rabioso. El viento te mete el frío hasta los huesos. Esta vez marcas la roca con la punta de la espada, y te prometes no volver a verla.

#### `emynmuil_3_3`

La hondonada de los huesos.
Bajas a una hondonada resguardada del viento. El suelo está sembrado de huesos de cabra, limpios, y hay restos de una hoguera. Junto a ella, un orco de Mordor roe un hueso, y a sus pies duermen dos lobos de las rocas, enroscados como perros. Uno de ellos abre los ojos. No hace ruido: se levanta, despierta al otro con el hocico, y los dos te miran fijamente. El orco ve hacia dónde miran, escupe el hueso y coge su lanza.

#### `emynmuil_4_0`

El salto.
La grieta por la que vas desemboca de pronto en otra, perpendicular, que corta el paso de lado a lado. Es demasiado ancha para cruzarla de un paso y demasiado honda para ver el fondo: solo oyes, muy abajo, el goteo del agua. Te quitas la mochila, la lanzas al otro lado y retrocedes para tomar carrerilla. Saltas. Tus botas llegan al borde, resbalan, y te agarras a la piedra con las uñas. Subes. Te quedas tumbado, temblando, riéndote sin saber por qué.

#### `emynmuil_4_1`

Una sombra bajo la luna.
Duermes bajo una losa inclinada que te protege del viento. Te despierta un ruido de piedrecillas que caen. La luna ha salido, y por un instante, encima de una roca, ves recortada contra ella una sombra flaca, encogida, que mira hacia el este. Parpadeas y ya no está. No sabes si era un animal, un orco o un sueño. No vuelves a cerrar los ojos hasta el alba, con la espada sobre las rodillas y el oído atento a cada piedra.

#### `emynmuil_4_2`

Señales rojas.
El sendero de los orcos sigue hacia el este, y cada tanto, en una roca, vuelve a aparecer el ojo rojo, pintado con prisa. Junto a uno de ellos hay un montón de piedras con una lanza clavada y un trapo negro atado. Más allá, el sendero se divide en tres, y cada rama lleva su señal. Entiendes que estas colinas están vigiladas por gente que conoce el camino. Decides apartarte de los senderos, aunque te cueste el doble.

#### `emynmuil_4_3`

Huellas en la escarcha.
Te escondes entre las rocas para dejar pasar a una patrulla, y pasa: cuatro orcos que bajan a buen paso, sin hablar. Respiras. Pero uno se detiene, vuelve atrás y se agacha sobre la escarcha que cubre el sendero. Tus huellas están ahí, marcadas en blanco, claras como una firma. El orco olfatea, gruñe, y los otros tres se dan la vuelta. Antes de que te encuentren, sales a su encuentro en el paso estrecho, donde solo caben de dos en dos.

#### `emynmuil_4_4`

La noche helada.
La noche en las alturas es la más fría que recuerdas. No hay leña en estas colinas, ni un mal arbusto que quemar. Te envuelves en la capa gris y te acurrucas entre dos piedras, con las rodillas contra el pecho. La capa abriga más de lo que parece, pero el frío sube de la roca. Te frotas las manos, cuentas las estrellas, repites en voz baja las canciones de la posada. Al alba tienes escarcha en las cejas, pero sigues vivo.

#### `emynmuil_4_5`

El cortado.
El camino que elegiste se acaba en un cortado de roca, alto como diez hombres, sin otra bajada en mucha distancia. Sacas la cuerda gris de los elfos. La pasas doble alrededor de un saliente de piedra, para poder recuperarla desde abajo, y bajas de espaldas, apoyando las botas en la pared. La cuerda es fina como un cordel y, sin embargo, no cede ni un palmo. Abajo, tiras de un cabo y la cuerda entera cae a tus pies, enroscada como una serpiente gris.

#### `emynmuil_4_6`

El agua de la piedra.
Llevas un día entero sin encontrar agua y el odre está casi vacío. La lengua se te pega al paladar. Por fin, en una hendidura a la sombra, das con una pila natural en la roca, llena de agua de lluvia. Está fría y sabe a piedra, pero te parece mejor que la cerveza de la posada. Bebes despacio, llenas el odre hasta arriba y te quedas un rato allí, viendo cómo el agua vuelve a quedarse quieta.

#### `emynmuil_4_7`

El desprendimiento.
Cruzas una pedrera de rocas sueltas al pie de un risco. A medio camino oyes un chasquido arriba, y luego un rugido creciente: la ladera se ha soltado. Corres en diagonal, saltando de piedra en piedra, mientras a tu espalda bajan rodando bloques del tamaño de una casa. Uno pasa tan cerca que sientes el viento. Cuando se hace el silencio, estás al otro lado, cubierto de polvo gris, y el camino por el que viniste ya no existe.

#### `emynmuil_5_0`

Bruma entre las rocas.
Amanece con una bruma baja que llena los barrancos como leche. Solo asoman las crestas, como islas en un mar blanco. Caminas por ellas, de una a otra, mientras abajo la niebla se mueve despacio. Es hermoso y engañoso: un mal paso y caerías en lo blanco sin saber cuánto. Al mediodía el sol la deshace, y descubres que has avanzado más que en todo el día anterior. Por primera vez, al este, las colinas parecen bajar.

#### `emynmuil_5_1`

El arco de piedra.
El viento ha tallado en la roca un arco enorme, tan alto que bajo él cabría una torre. Pasas por debajo y te detienes en mitad, con el cuello torcido, mirando hacia arriba. La piedra está estriada como la madera, en capas grises y ocres. En la sombra del arco hay una piedra lisa con marcas antiguas: rayas, círculos, quizá un mapa. Alguien pasó por aquí hace mucho y quiso dejar constancia. Grabas a su lado una pequeña B, por Bree.

#### `emynmuil_5_2`

La manada de la meseta.
Sales a una meseta pelada, sin una roca donde cubrirte. A mitad de camino oyes el primer aullido, corto, a tu izquierda. Le responde otro a tu derecha, y otro detrás. Cuatro lobos de las rocas salen de las grietas del borde y se abren en abanico, sin prisa, sin dejar de mirarte. Cazan como cazan los lobos: en silencio, cerrando el círculo paso a paso. Te pones en el centro, giras despacio y esperas al primero.

#### `emynmuil_5_3`

El vigía del Ojo.
Rodeas una peña alta y ves, en lo más alto, un montón de leña seca y un orco sentado junto a ella. Es un vigía, y está ahí para encender una señal si ve algo. Todavía no te ha visto. Subes por detrás, agarrándote a cada grieta, con la respiración contenida. Te falta un tramo cuando una piedra se suelta bajo tu bota. El orco se vuelve y coge una tea; luego, al verte tan cerca, cambia la tea por el alfanje.

#### `emynmuil_6_0`

El borde de las colinas.
Por fin llegas al borde oriental. Las colinas terminan en un acantilado largo y roto, y a sus pies se extiende una llanura gris y parda que brilla aquí y allá como un espejo sucio: agua estancada, juncos, niebla. Más allá, en el horizonte, se alza una muralla de montañas negras. El viento trae un olor a podrido. Buscas durante horas una bajada, y al fin das con un barranco ancho que desciende, piedra a piedra, hacia la llanura.

#### `emynmuil_6_1`

Huellas enormes.
Bajas por un cauce seco que desciende hacia el este. En la arena del fondo hay huellas: enormes, de pies descalzos más largos que tu brazo, con dedos gruesos y uñas que arañan la roca. Hay también huesos rotos a golpes, no a mordiscos, y una piedra con manchas oscuras. Lo que dejó esas huellas pasó por aquí hace poco y es mucho más grande que tú. Sigues adelante, porque no hay otro camino, pero ahora miras cada sombra.

#### `emynmuil_7_0`

El trol de las colinas.
La bajada desemboca en una explanada al pie de las colinas, desde la que ya se ve la ciénaga. Allí, sentado sobre una roca, te espera algo que te hiela la sangre: un trol de las colinas, gris como la piedra, con una porra hecha de un tronco entero. No hay nadie más en la explanada: solo la bestia, que vuelve hacia ti la cabeza y olfatea el aire. El trol se levanta despacio, más alto que dos hombres, y la explanada entera parece encogerse.

## cienagas

#### `cienagas_1_0`

Tierra de agua muerta.
Dejas atrás las últimas rocas y el suelo se vuelve blando bajo tus botas. Delante se extiende la ciénaga: charcas de agua parda, matas de juncos podridos, lomas de barro que tiemblan al pisarlas. Un olor dulzón y rancio lo llena todo. La niebla se arrastra a ras del agua, y hace frío, un frío húmedo que no se va con el sol. Ningún pájaro vuela aquí. Al este, detrás de la bruma, adivinas la muralla oscura de las montañas.

#### `cienagas_2_0`

La primera luz.
Sigues una orilla de juncos mientras cae la tarde. Delante, sobre el agua, se enciende una luz pálida, verdosa, como la llama de una vela que nadie sostiene. Se queda quieta un momento. Luego se acerca, flotando, y al llegar a tu altura crece y se estira, y sientes en la cara un aliento helado en lugar de calor. Es una llama que quema de frío. Cuando la espada la atraviesa, chisporrotea, se encoge y vuelve a crecer.

#### `cienagas_2_1`

Los pozos negros.
Avanzas saltando de una mata de hierba a otra, porque entre ellas el agua es negra y no se ve el fondo. Al aterrizar en una mata grande, el agua de los pozos de alrededor empieza a temblar. Dos formas suben despacio, hechas de niebla y de agua sucia, con algo parecido a brazos. No tienen cara, solo dos hundimientos oscuros donde deberían estar los ojos. Huelen a fondo de pozo. Se inclinan hacia ti como si quisieran abrazarte.

#### `cienagas_3_0`

Caras bajo el agua.
Te detienes junto a una charca de agua quieta, tan clara que sorprende en este lugar. Al principio solo ves barro. Luego, bajo la superficie, ves un rostro: pálido, con un yelmo antiguo, los ojos abiertos como si soñara. Y otro detrás, y otro más, hombres de otros tiempos y criaturas que no reconoces, tendidos en el fondo como si los hubieran dejado allí tras una batalla muy antigua. No te inclinas más. Te apartas despacio, sin dar la espalda al agua.

#### `cienagas_3_1`

La vara de tantear.
La niebla se espesa tanto que dejas de verte los pies. Cortas una vara de un arbusto seco y tanteas el suelo antes de cada paso. A veces la vara se hunde sin encontrar fondo, y tienes que buscar otro camino. Avanzas así durante horas, en un mundo blanco y mudo, sin saber si vas hacia el este o das vueltas. Solo el frío te dice que estás despierto. Cuando la niebla se aclara un poco, ves que la vara está cubierta de barro hasta la mitad.

#### `cienagas_3_2`

El islote seco.
Encuentras un islote de tierra firme, apenas más grande que una habitación, con un arbusto torcido y unas piedras. Es el primer suelo seco en todo el día. Te quitas las botas, escurres los calcetines y comes un trozo de pan élfico, despacio, mirando el agua alrededor. Las hojas que lo envuelven todavía huelen al bosque dorado. Por un momento, el hedor de la ciénaga desaparece, y te acuerdas de los árboles de plata y de sus lámparas.

#### `cienagas_3_3`

La calzada hundida.
Entre las matas descubres una hilera de piedras planas, medio hundidas, que cruzan la ciénaga en línea recta. Es lo que queda de una calzada, hecha por gente que sabía construir, hace tanto tiempo que la ciénaga se la ha ido tragando. La sigues saltando de losa en losa. Algunas se mueven bajo tu peso. En una ves tallado un círculo con rayos, como un sol. Te preguntas adónde llevaba este camino cuando aquí no había agua.

#### `cienagas_4_0`

Lo que asoma del barro.
En una loma de barro asoma algo metálico. Lo desentierras con la punta de la espada: es un yelmo oxidado, alto y estrecho, con restos de un penacho que ya no tiene color. Debajo hay una punta de lanza y un trozo de escudo con un dibujo borrado. Aquí murió alguien hace muchísimo tiempo, en una guerra de la que nadie te ha hablado. Vuelves a dejarlo todo donde estaba, lo cubres con barro y sigues adelante en silencio.

#### `cienagas_4_1`

Las luces que engañan.
De noche ves luces delante, en fila, como las linternas de un campamento. Piensas en hombres, en fuego, en un poco de calor, y vas hacia ellas. Las luces retroceden a medida que avanzas, siempre a la misma distancia. Cuando quieres darte cuenta, tienes agua hasta la cintura. Entonces ves que las luces no flotan solas: dos las sostienen en alto unas figuras grises y quietas que creías troncos muertos. Las otras dos bajan hacia ti, y las figuras echan a andar por el agua. Has caído en su trampa como un niño.

#### `cienagas_4_2`

Voces en la niebla.
En la niebla empiezas a oír voces. Son débiles, lejanas, y no entiendes lo que dicen; parecen hablar en una lengua muy antigua, despacio, como quien llama a alguien que se ha ido. Vienen del agua. Te detienes a escuchar, y por un momento te parece que una de ellas dice tu nombre. Sacudes la cabeza con fuerza. Te tapas un oído con la mano y sigues tanteando con la vara, más deprisa, hasta que las voces quedan atrás.

#### `cienagas_4_3`

El lodazal.
Para ganar tiempo atajas por un lodazal que parece firme. No lo es. Al tercer paso te hundes hasta las rodillas, y luego hasta la cintura, y el barro te sujeta las piernas como unas manos. Cuanto más te mueves, más te hundes. Te quedas quieto, respiras, y lanzas la cuerda gris de los elfos hacia un tronco muerto que asoma a un lado. A la tercera vez se enrolla en una rama. Tiras palmo a palmo hasta sacarte, cubierto de barro negro y temblando, y desenrollas la cuerda de la rama antes de seguir.

#### `cienagas_4_4`

Los que suben del agua.
Decides pasar la noche en el islote. Te duermes con la espada al lado, y te despierta el frío: unas manos heladas te han agarrado los tobillos desde el agua de la orilla. Tiran de ti hacia el barro, despacio, sin ruido. Te sueltas a patadas y ruedas hasta el arbusto torcido. En el borde del islote, medio dentro del agua, tres espectros se arrastran hacia ti sobre los codos, con jirones de niebla por cabello. Te pones de espaldas al arbusto y alzas la espada.

#### `cienagas_4_5`

El alba sin sol.
Sales del islote antes de que amanezca, para aprovechar la poca luz. Pero el alba aquí no trae sol: solo un gris que se aclara un poco, como agua sucia en un vaso. Hacia el este, las montañas se recortan más nítidas que nunca, negras, con humo en sus cumbres. Delante, la ciénaga sigue y sigue. Caminas sin hablar, ni siquiera contigo mismo. Te sorprendes echando de menos hasta el viento helado de las colinas.

#### `cienagas_4_6`

Chispas de la calzada.
La calzada termina en una explanada de losas rotas, quizá una plaza o un puesto de guardia hundido. Al pisar la primera, unas chispas pálidas brotan de las rendijas entre las piedras, como de una fragua apagada que alguien removiera. Suben, se juntan y forman cuatro llamas, que empiezan a girar a tu alrededor. No queman: hielan. Donde pasan, el agua de los charcos se cubre de escarcha. El círculo se estrecha.

#### `cienagas_4_7`

Los montículos.
La calzada pasa junto a unos montículos alargados, cubiertos de hierba áspera, que asoman del agua en filas. Son demasiado regulares para ser naturales. En la cabecera de uno hay una piedra hincada, inclinada, con letras que el tiempo ha borrado. Son tumbas, comprendes, o lo que queda de ellas. Rodeas las filas con respeto y sin pisar ninguna. No sabes a quién pertenecen, pero alguien quiso que se les recordara.

#### `cienagas_5_0`

El grito en lo alto.
Cruzas una llanura de barro sin un solo junco. A mitad de camino el aire se vuelve de hielo y la luz se apaga. Muy alto, entre las nubes, pasa una sombra con alas enormes, y de ella cae un grito que te atraviesa. Te tiras de bruces en el barro, cubierto con la capa gris, sin moverte, sin respirar. Cuando el frío por fin se retira, el cielo vuelve a estar vacío, pero tardas mucho en ponerte en pie.

#### `cienagas_5_1`

La sed.
Estás rodeado de agua y no puedes beberla. La de las charcas sabe a hierro y a podrido, y te revuelve el estómago con solo olerla. Tu odre está casi vacío. Al final encuentras unas hojas anchas, dobladas como cuencos, que han recogido el rocío de la noche. Las vacías una a una en la boca, con cuidado de no perder ni una gota. Es poco, pero te basta para seguir hasta que vuelva a llover.

#### `cienagas_5_2`

Los juncos secos.
Te metes en un mar de juncos secos, más altos que tú, que crujen y se rozan con el viento como huesos. No ves nada más allá de un paso. El ruido es constante, y bajo él te parece oír otros: un chapoteo, un roce, algo que se arrastra. Cada vez que te paras, todo se para. Avanzas abriendo los tallos con el brazo, siempre hacia donde crees que está el este. Al salir, tienes la cara llena de cortes finos.

#### `cienagas_5_3`

La noche de las velas.
Al caer la noche te refugias en un montón de piedras, y desde allí ves encenderse luces por toda la ciénaga: cientos, pálidas, verdes y blancas, que suben y bajan sobre el agua como velas en una procesión sin fin. Ninguna se acerca. Te quedas inmóvil, con la capa sobre la cabeza, mirándolas por una rendija. Es hermoso y terrible a la vez. Al alba, una a una, se van apagando, y la ciénaga vuelve a ser gris y vacía.

#### `cienagas_6_0`

El espectro del canal.
Un canal ancho de agua lenta te corta el paso, y no hay forma de rodearlo. Lo vadeas con la espada en alto y el agua hasta el pecho, tanteando el fondo con los pies. La corriente trae hacia ti un bulto de trapos oscuros, flotando, como una capa vieja que alguien perdió. Te apartas para dejarlo pasar. Pero el bulto se detiene a tu lado, se despliega y se yergue: un espectro más grande que los que has visto, envuelto en jirones de una capa antigua. Y se lanza sobre ti.

#### `cienagas_6_1`

El fin de la ciénaga.
Bordeas una laguna ancha durante horas, y poco a poco la ciénaga cambia. Los juncos escasean, el barro se seca y se agrieta, y empiezan a aparecer montones de escoria gris y polvo. Más allá, la tierra es un desierto de ceniza que llega hasta el pie de las montañas negras. Por fin terreno firme, aunque muerto. Te detienes en la última orilla. Entre tú y esa tierra solo queda un gran estanque, oscuro y quieto como un ojo.

#### `cienagas_7_0`

El señor de los ahogados.
Llegas al último estanque, el que separa la ciénaga de la tierra seca. Cuando pones el pie en el agua, el estanque entero se estremece. Del centro se levanta una figura enorme, hecha de barro y agua negra, con una corona herrumbrosa hundida en la frente y los restos de una armadura antigua. Sus ojos son dos luces pálidas. El agua negra le chorrea de los brazos cuando los alza hacia ti. Es el señor de los que duermen bajo la ciénaga, y no quiere dejarte pasar.

## ithilien

#### `ithilien_1_0`

Al sur, bajo las montañas.
Dejas atrás el estanque del señor de los ahogados y sales al desierto de ceniza. Allí pierdes las huellas pequeñas entre la escoria, pero sabes adónde iban: al este, más allá de la muralla negra de las montañas. Por ese lado no hay paso, y al norte se adivinan torres oscuras cerrando un valle. Giras al sur y sigues la falda occidental de las montañas por una calzada antigua. Durante dos días todo es polvo. Al tercero la tierra cambia: hierba, matas, árboles, y un olor a tomillo y a resina. En el primer manantial limpio bebes hasta hartarte y llenas el odre.

#### `ithilien_2_0`

Los exploradores del sur.
La calzada, empedrada hace siglos por los hombres de Gondor, sigue hacia el sur entre árboles cada vez más altos. En una curva te topas con dos hombres que vienen en sentido contrario. Son altos y morenos, con capas rojas, anillos de oro en las orejas y una serpiente roja pintada en el escudo. Son los primeros hombres que ves en semanas, pero no traen buenas intenciones: te gritan algo en una lengua que no entiendes y desenvainan sus cimitarras a la vez.

#### `ithilien_2_1`

El jardín abandonado.
Dejas la calzada y bajas por unas laderas cubiertas de hierbas: tomillo, salvia, mejorana, matas que crujen bajo tus botas y sueltan su olor al pisarlas. Entre ellas asoman muros bajos de piedra, bancales que alguien cuidó hace mucho y que ahora nadie riega. Esto fue un jardín, comprendes, el jardín de un reino entero. En un arroyo te lavas el barro de la ciénaga de la cara y de las manos. Por primera vez en muchos días, te descubres sonriendo.

#### `ithilien_3_0`

La atalaya.
Sobre una colina asoma una torre de vigía, sin tejado, con la escalera interior medio derrumbada. En el dintel de la puerta hay tallado un árbol con siete estrellas encima, casi borrado por la hiedra. Subes con cuidado hasta lo alto. Desde allí ves bosques que ondulan hacia el sur, el brillo lejano del gran río al oeste y, al este, siempre, las montañas negras. Al sur, entre los árboles, se levantan varias columnas de humo. Alguien acampa en estas tierras, y no son los dueños de la torre.

#### `ithilien_3_1`

Silbidos en el bosque.
Te adentras en un bosque espeso de robles y helechos. Al poco oyes un silbido, como el de un pájaro, y otro que le responde desde el lado contrario. Luego un tercero, más cerca. No son pájaros. Te quedas quieto, con la mano en la espada, y durante un largo rato no pasa nada. Solo una vez, entre dos troncos, te parece ver moverse una sombra vestida de verde y pardo, que desaparece sin un ruido. Alguien te ha visto, y ha decidido dejarte pasar.

#### `ithilien_3_2`

Entre los cipreses.
Cruzas un bosquecillo de cipreses oscuros, altos y estrechos como lanzas, que huelen a resina caliente. Una flecha se clava en el tronco que tienes al lado, a un palmo de tu cabeza, y se queda temblando. Te tiras al suelo. Entre las ramas de un ciprés, más arriba, ves una capa roja y el brillo de un arco que se tensa otra vez. El arquero está solo y te ha elegido como blanco. Corres de tronco en tronco hacia él, mientras otra flecha silba a tu lado.

#### `ithilien_3_3`

El vado de piedras.
Un arroyo ancho y rápido baja de las montañas, y lo cruzas por un vado de piedras planas. En la otra orilla, tres hombres del sur llenan sus odres agachados junto al agua. Te ven al mismo tiempo que tú a ellos. El que lleva lanza se levanta de un salto y se mete en el arroyo para cortarte el paso, mientras los otros dos retroceden por la orilla y descuelgan los arcos. Estás en medio del agua, sin dónde esconderte. Avanzas hacia el lancero.

#### `ithilien_4_0`

La aldea vacía.
Al pie de la colina hay una aldea en ruinas: casas de piedra sin tejado, un pozo cegado y una fuente seca con una pila tallada en forma de concha. En una de las casas, la chimenea aún conserva hollín. Decides pasar allí la noche, sin encender fuego, porque el humo se vería desde lejos. Cenas pan élfico con unas hojas de acedera que encuentras en un huerto asilvestrado. Antes de dormir, te preguntas quién vivía aquí y adónde se fue.

#### `ithilien_4_1`

El campamento del humo.
Te acercas a una de las columnas de humo al caer la tarde, arrastrándote entre los helechos. Es un campamento de hombres del sur: tiendas rojas, lanzas clavadas en el suelo y cuatro guardias junto al fuego, asando algo. Quieres rodearlo sin que te vean, pero una rama seca se parte bajo tu rodilla. Las cuatro cabezas se vuelven a la vez. Dos de ellos echan mano de las cimitarras; los otros dos, de los arcos. Ya no tiene sentido esconderse.

#### `ithilien_4_2`

La flecha verde.
Sigues la dirección de los silbidos y llegas a un claro. Al borde, junto a un tronco caído, yace un hombre del sur, con su capa roja extendida sobre la hierba. Tiene clavada en el pecho una flecha larga, de plumas verdes, que no se parece a ninguna flecha de orco. Alguien defiende todavía estos bosques, sin dejarse ver, contra los que vienen del sur. No tocas nada. Te alejas del claro despacio, sintiendo en la nuca unos ojos que no encuentras.

#### `ithilien_4_3`

La columna roja.
Desde la ladera, oculto entre las matas, ves pasar una columna de hombres del sur por la calzada del valle. Marchan hacia el norte, cientos de ellos, con estandartes rojos y negros, lanzas, escudos con la serpiente y tambores que retumban en el pecho. En medio de la columna avanza algo que al principio confundes con una colina gris: una bestia enorme, con colmillos curvos y una torre de madera sobre el lomo. Contienes el aliento hasta que el último soldado se pierde de vista.

#### `ithilien_4_4`

El estanque de los nenúfares.
Entre unos cedros encuentras un estanque redondo, de agua clara, con nenúfares blancos y un borde de piedra labrada que el tiempo ha ido cubriendo de musgo. Libélulas azules vuelan sobre la superficie. Te quitas las botas y metes los pies en el agua fresca, y por un rato te olvidas de las flechas, de la ceniza y de las montañas. Una rana salta desde una hoja. Cuando por fin te levantas, el sol ya ha bajado y la sombra de los cedros cubre el agua.

#### `ithilien_4_5`

Cuernos en el oeste.
Hacia el oeste suenan cuernos, y después gritos, choques de acero y un bramido tan enorme que hace temblar las hojas. Corres hacia allí, pero el bosque es espeso y tardas horas en llegar. Cuando lo haces, todo ha terminado. Solo encuentras helechos aplastados, lanzas rotas, jirones de capas rojas colgando de las ramas y una franja de árboles tronchados que se pierde hacia el río. No hay nadie. Quienquiera que luchó aquí se ha ido, y tú llegas tarde, otra vez.

#### `ithilien_4_6`

Arqueros en el barranco.
Subes por un barranco seco, entre rocas blancas y arbustos de espino, buscando un paso hacia el sur. A mitad de la subida rueda una piedra desde arriba, y luego otra. Levantas la vista: en el borde del barranco, dos arqueros del sur te esperan con las flechas en la cuerda, uno a cada lado, como si supieran que ibas a pasar por aquí. Las flechas rebotan en las rocas junto a tus pies. Trepas hacia el más cercano, agachado, de roca en roca.

#### `ithilien_4_7`

La noche tibia.
Acampas bajo un cedro enorme, al abrigo de sus ramas bajas. Es la primera noche tibia desde que saliste del bosque dorado. El cielo está limpio y lleno de estrellas, y entre los arbustos canta un ruiseñor, una y otra vez, como si en el mundo no hubiera guerra. Te tumbas boca arriba sobre la capa. Piensas en Bree, en la posada y en su ruido de jarras, y te duermes antes de darte cuenta. Nadie te molesta en toda la noche.

#### `ithilien_5_0`

El puente roto.
Un torrente encajonado corta el bosque de norte a sur, y el único paso es un puente de piedra de un solo arco, roto por la mitad. Entre los dos extremos queda un hueco de varios pasos sobre el agua espumosa. Lanzas primero el zurrón a la otra orilla. Luego retrocedes, tomas carrerilla y saltas. Caes de rodillas en el borde, con medio cuerpo en el aire, y te arrastras hasta estar a salvo. Debajo, el agua sigue rugiendo, indiferente.

#### `ithilien_5_1`

La flecha que no viste.
Atraviesas un robledal antiguo, de troncos tan anchos que tres hombres no los abarcarían. Vas atento al suelo, a las ramas que crujen, y no miras hacia arriba. Un silbido corto, a tu espalda, te hace girar: desde una rama alta, un arquero del sur cae entre las hojas con una flecha de plumas verdes clavada en el hombro. Te apuntaba a ti. Se levanta como puede y huye cojeando hacia el sur. Buscas a quien disparó. Solo hay árboles, y un silencio largo.

#### `ithilien_5_2`

La tormenta.
El cielo se cubre de golpe y estalla una tormenta de primavera, con truenos que ruedan entre las colinas. Encuentras refugio en una sala de piedra medio enterrada en la ladera, con columnas cubiertas de hiedra. En la pared del fondo hay tallado un rey con corona alta y una espada sobre las rodillas, con la cara borrada por los siglos. Te sientas a su lado, empapado, y escuchas la lluvia golpear las hojas. Huele a tierra mojada y a romero.

#### `ithilien_5_3`

Huellas enormes.
En un claro encuentras huellas redondas, tan grandes que podrías sentarte dentro de una, hundidas en la tierra blanda. Alrededor, los árboles jóvenes están partidos a la altura de tu pecho y hay ramas arrancadas mucho más arriba de lo que alcanzarías con la espada. Algo enorme ha pasado por aquí hace poco, a paso lento, y se ha detenido a comer cortezas. Las huellas siguen hacia el sur. Las sigues con cautela, procurando no hacer ruido.

#### `ithilien_6_0`

El centinela del sur.
El bosque se aclara y llegas a un collado por el que pasa un sendero hacia el sur. En medio, apoyado en una lanza larga, te espera un guerrero del sur más alto que los demás, con un escudo redondo de bronce y un casco con penacho rojo. No se esconde ni llama a nadie. Te mira acercarte, clava la lanza en el suelo, desenvaina una cimitarra ancha y te hace un gesto con la cabeza. Quiere luchar contigo él solo.

#### `ithilien_6_1`

Bajo el humo del este.
Subes a una loma pelada para ver el camino. Mientras miras, el cielo del este empieza a oscurecerse: por encima de las montañas avanza una nube parda, enorme, que se extiende despacio sobre los bosques como una mancha de aceite sobre el agua. El sol se vuelve un disco pálido y luego desaparece. El canto de los pájaros se apaga. A media mañana parece que anochece. No sabes qué es, pero sabes de dónde viene, y sientes que algo grande ha empezado.

#### `ithilien_7_0`

El olifante de guerra.
El sendero te lleva a un claro junto a la calzada, lleno de árboles tronchados. Allí está: una bestia gris, grande como una casa, con colmillos curvos envueltos en anillas de bronce y una torre de madera sobre el lomo, de la que cuelgan jirones rojos. Debió de quedarse rezagada de su columna; la torre está vacía y sus jinetes han caído o huido. Sin nadie que la guíe, la bestia levanta la trompa, lanza un bramido que hace temblar el suelo y viene hacia ti.

## cirithungol

#### `cirithungol_1_0`

La luz verde.
Bajo un cielo pardo que ya no deja ver el sol, sigues hacia el sur hasta un cruce de calzadas en medio de un anillo de árboles. Allí tomas la que sube al este, hacia las montañas. Te lleva a un valle frío donde todo brilla con una luz verde y enferma: el río, los prados y, al fondo, una ciudad de murallas pálidas con una torre alta coronada de luz. No piensas acercarte. Por la pared norte del valle sube una senda de peldaños tallados, y en su polvo vuelves a ver huellas pequeñas.

#### `cirithungol_2_0`

Las flores pálidas.
Bordeas el valle por los prados que bajan al río, buscando el pie de la senda sin pasar cerca de la ciudad. Los prados están cubiertos de flores blancas que brillan débilmente en la penumbra, como pequeñas lunas, y huelen a podrido, a tumba abierta. El río corre negro y sin ruido. No te atreves a beber de él, aunque tienes sed. Te envuelves en la capa y caminas deprisa, con la vista baja, mientras la luz verde te sigue sobre la hierba.

#### `cirithungol_2_1`

La patrulla del valle.
Empiezas a subir por la senda y en el primer recodo te encuentras de frente con dos orcos que bajan. No son como los de las montañas: llevan cotas negras y en el pecho un emblema pintado, una luna blanca con una calavera por cara. Se paran en seco y olfatean el aire. Uno dice algo con voz ronca y se ríe; el otro ya ha sacado un cuchillo largo y curvo. Están entre tú y la subida, y no piensan apartarse.

#### `cirithungol_3_0`

El orco del arroyo.
Un arroyo baja de las alturas y cruza los prados para unirse al río. Su agua es tan fría que te duelen los tobillos al vadearlo, y tiene el mismo brillo verdoso que todo lo demás. A mitad del vado oyes un chapoteo río arriba: un orco con la luna blanca en el pecho se ha agachado a llenar un cubo. Levanta la cabeza, te ve y suelta el cubo, que se va flotando. Corre hacia ti por el agua, con la espada ya fuera.

#### `cirithungol_3_1`

Grietas con telas.
Para no ir por la senda, donde podrías encontrarte con alguien, trepas por las grietas de la pared, agarrándote a raíces y salientes. Las grietas están llenas de telarañas grises, viejas y polvorientas, tan gruesas que parecen trapos. Al apartar una con la mano, algo se mueve dentro. Dos arañas del tamaño de un perro salen de la grieta, negras y peludas, con ojos que brillan como cuentas. Una se descuelga sobre tu cabeza. La otra te corta la retirada.

#### `cirithungol_3_2`

La escalera recta.
La senda se convierte en una escalera recta, tallada en la roca, que sube y sube sin descanso hacia la oscuridad. Los peldaños son altos y estrechos, gastados en el centro. No hay barandilla ni nada a lo que agarrarse, solo la pared a un lado y el vacío al otro. Subes contando escalones hasta que pierdes la cuenta. Te arden las piernas. Cuando te vuelves a mirar, la ciudad de la luz verde ha quedado muy abajo, pequeña como un juguete.

#### `cirithungol_3_3`

El silencio del valle.
Te escondes entre unas rocas a esperar que pase el peligro. Al cabo de un rato te das cuenta de que no oyes nada: ni viento, ni agua, ni un pájaro, ni siquiera tu respiración. Das una palmada y el sonido muere en el aire como si nunca hubiera existido. La luz verde de la ciudad sube y baja despacio, como el pulso de algo dormido. No sabes cuánto tiempo pasas así. Cuando por fin te levantas, tienes las manos entumecidas.

#### `cirithungol_4_0`

El puesto de la luna.
Junto a la senda hay un puesto de guardia antiguo, de piedra blanca, construido por gentes mucho mejores que los orcos. Sobre la puerta estuvo tallada una luna creciente, hermosa, pero alguien la ha picado a golpes y ha pintado encima una calavera. Dentro hay huesos de animales, cenizas y basura. Te sientas un momento en el umbral. Piensas que este valle, antes de la luz verde, debió de ser un lugar donde la luna se reflejaba en el río.

#### `cirithungol_4_1`

El derrumbe.
Más arriba, un tramo de la senda se ha desplomado, y entre los dos lados queda un vacío demasiado ancho para saltarlo. Por encima, en la pared, sobresale un cuerno de roca. Lanzas la cuerda gris de los elfos, la enganchas en él a la segunda y pasas colgado, con los pies contra la roca y el abismo debajo. Al otro lado das un tirón seco, y la cuerda se suelta y cae en tus manos. La enrollas con cuidado.

#### `cirithungol_4_2`

La hoguera del nicho.
Sigues trepando hasta un rellano y ves una luz rojiza: en un nicho de la pared, cuatro orcos de la luna blanca se calientan junto a una hoguera pequeña, discutiendo a gritos sobre algo que ha pasado arriba. Te pegas a la roca para pasar de largo. Pero el viento cambia, les lleva tu olor y las voces se callan de golpe. Cuatro cabezas se vuelven hacia la oscuridad donde estás. Uno coge una tea encendida y se acerca, olfateando.

#### `cirithungol_4_3`

El orco que huye.
Oyes pasos que bajan a la carrera y te pegas a la pared, dentro de una grieta. Un orco pasa a tu lado sin verte, tropezando, mirando hacia atrás. Lleva el casco torcido y sangre en el brazo, y murmura algo una y otra vez, como un loco. No huye de ti: huye de algo que hay arriba. Se le cae un cuchillo curvo en los peldaños y ni siquiera se para a recogerlo. Esperas a que sus pasos se pierdan abajo antes de seguir.

#### `cirithungol_4_4`

Las piedras que caen.
Sigues subiendo, peldaño a peldaño. De pronto, un ruido seco arriba, y luego un estruendo que crece: piedras que caen rebotando por la escalera, algunas grandes como cabezas. Te aplastas contra la pared mientras pasan silbando a un palmo de ti y se pierden en el vacío. Una te roza el hombro y te deja el brazo dormido un buen rato. Cuando cesa el ruido, te quedas quieto, esperando la siguiente. No llega. Nunca sabrás si se soltaron solas.

#### `cirithungol_4_5`

La garganta de las telas.
Para ahorrarte un tramo de escalera, atajas por una garganta estrecha entre dos paredes. Es un error. La garganta está tapada de lado a lado por telarañas, una detrás de otra, como velas de un barco. Cortas la primera con la espada; es dura y pegajosa como cuero mojado. Al cortar la segunda, toda la garganta tiembla. Cuatro arañas bajan por los hilos, rápidas, desde las sombras de arriba, chasqueando las mandíbulas. Detrás de ti, las telas ya cierran el camino.

#### `cirithungol_4_6`

Las tortas contadas.
Te sientas al abrigo de una roca y abres el zurrón. Cuentas lo que te queda del pan de los elfos: tres tortas, envueltas todavía en sus hojas. Partes una, comes la mitad despacio y guardas el resto. El sabor te devuelve por un momento la luz dorada de los árboles, el agua del río plateado, la voz serena de la Dama. Te sientes más fuerte de lo que deberías. Bebes un sorbo del odre y vuelves a ponerte en camino.

#### `cirithungol_4_7`

La luz que busca.
Te asomas al borde para mirar el valle. En ese momento la luz de la torre se intensifica, y un haz verde se despliega y recorre despacio las paredes, como una mano que tantea en la oscuridad. Se acerca a ti. Te tiras detrás de una roca y te encoges, sin respirar. El haz pasa por encima, ilumina la piedra a tu lado con un brillo de enfermedad y sigue de largo. Esperas a que se apague del todo para volver a moverte.

#### `cirithungol_5_0`

La cresta.
Llegas por fin a lo alto de la subida. Delante, la montaña se alza en una cresta dentada, negra contra el cielo pardo, y en ella se abre una hendidura: el paso. Junto a él, encaramada sobre las rocas, hay una torre oscura con una sola ventana encendida de rojo. Desde aquí no ves guardias en sus muros, pero tampoco sabes si te vigilan. Decides que, sea como sea, no pasarás por debajo de esa torre si puedes evitarlo.

#### `cirithungol_5_1`

El hedor.
El aire cambia. Empiezas a notar un olor dulzón y podrido que no se parece a nada que conozcas, y que se hace más espeso a cada paso. Entre las rocas aparecen huesos de cabras, de lobos y de orcos, vacíos y limpios, como si algo les hubiera sorbido la vida y hubiera tirado lo demás. Hay hilos grises enredados en ellos. Te tapas la nariz con la capa. No quieres saber qué come así, pero sospechas que pronto lo sabrás.

#### `cirithungol_5_2`

La cornisa.
Agotado, buscas un sitio para dormir y solo encuentras una cornisa estrecha, poco más ancha que tus hombros, sobre el vacío. Pasas la cuerda de los elfos por un pico de roca y por tu cintura, y te tumbas de lado contra la pared. Duermes a ratos, despertándote cada vez que cae una piedra. No llega el alba, solo un gris pardo un poco más claro. Desatas la cuerda, la enrollas y la guardas, y sigues subiendo.

#### `cirithungol_5_3`

La humareda.
Una humareda espesa baja del paso y se arrastra por la ladera, empujada por un viento del este. Huele a azufre y a hierro quemado, y te hace llorar los ojos. Te atas un pañuelo sobre la boca y avanzas casi a ciegas, palpando la pared con una mano. Toses hasta que te duele el pecho. Cuando por fin sales del humo, tienes la cara negra de hollín y la boca amarga. Es el primer aliento de la tierra que hay al otro lado.

#### `cirithungol_6_0`

La boca negra.
La senda se pierde entre rocas y la sigues hasta el final. Allí, en la pared, se abre una boca negra, más alta que dos hombres, de la que sale un aire frío y fétido. Por encima, una torre oscura encaramada en la cresta, con una ventana roja, vigila el paso; no quieres pasar bajo ella, y no hay otro camino hacia el este. En el umbral cuelgan telas desgarradas, como si alguien las hubiera atravesado hace poco, y en la piedra hay unas manchas verdosas, secas. Respiras hondo, empuñas la espada y entras.

#### `cirithungol_6_1`

Las crías.
Te desvías de la senda por una hondonada entre rocas, para que nadie te vea desde arriba. El suelo está cubierto de cáscaras secas, como de huevos enormes, y de hilos que se te pegan a las botas. Demasiado tarde comprendes que es un criadero. Tres arañas jóvenes, del tamaño de un ternero, salen de debajo de una losa y te rodean despacio, levantando las patas delanteras. Su olor dulzón y podrido llena la hondonada.

#### `cirithungol_7_0`

Ella-Laraña.
Al final solo queda un camino: el túnel que cruza la montaña. Dentro, la oscuridad es completa y el hedor, insoportable. Avanzas tanteando la pared, entre telas que se te pegan a la cara. Entonces oyes un roce enorme, un chasquido, y delante se encienden dos racimos de ojos, uno de ellos apagado y costroso, herido hace poco. Una araña gigantesca llena el túnel de lado a lado. Es Ella-Laraña, la vieja señora de este túnel, y tiene hambre.

## gorgoroth

#### `gorgoroth_1_0`

La tierra de ceniza.
Has dejado a la gran araña chillando en la oscuridad, herida, de vuelta a sus agujeros, y no la has seguido. Al salir del túnel has pasado de noche bajo una torre que guardaba silencio, sin guardias en los muros y con humo en las ventanas, y has bajado por un barranco de la otra vertiente, resbalando entre piedras sueltas. Ahora, delante, se extiende una llanura de ceniza y escoria, rota por grietas rojas. En medio, sola, una montaña escupe fuego. El agua del odre tendrá que durar.

#### `gorgoroth_2_0`

El rastreador negro.
Al pie del barranco, agachado sobre tu rastro en la ceniza, hay un orco enorme, de piel negra y brazos largos, con una cota de malla hasta las rodillas y un ojo rojo pintado en el escudo. Olfatea tus huellas como un sabueso. Cuando levanta la vista y te ve, no grita ni llama a nadie: sonríe, enseñando los colmillos, y se levanta despacio, desenvainando una espada ancha. Quiere la presa para él solo.

#### `gorgoroth_2_1`

La calzada de la ceniza.
Encuentras una calzada elevada que cruza la llanura, de piedras negras apisonadas, y la sigues un trecho porque a los lados la ceniza te llega a los tobillos. Al rodear un montón de escoria, te das de bruces con dos orcos que vienen en sentido contrario, cargados con fardos. Se quedan un instante pasmados, tan sorprendidos como tú. Luego tiran los fardos al suelo, sacan las cimitarras y se lanzan contra ti, gritando para darse valor.

#### `gorgoroth_3_0`

El agua de la roca.
El odre pesa ya muy poco, y buscas agua entre unas rocas negras. En una oquedad encuentras un charco de lluvia vieja, de un palmo de hondo, cubierto por una película de ceniza. La apartas con la mano. El agua sabe a hierro y a humo, amarga, pero es agua. Bebes despacio, a sorbos pequeños, y llenas el odre hasta arriba. Luego te quedas un rato mirando el charco vacío, como quien mira un tesoro gastado.

#### `gorgoroth_3_1`

La grieta roja.
Oyes pies que marchan y te descuelgas en una grieta que corta el suelo. Abajo, muy hondo, brilla una luz roja, y sube un calor que te seca los labios y te hace sudar bajo la capa. Te quedas colgado de un saliente, a dos palmos del borde, mientras arriba pasa una tropa de orcos. La ceniza que levantan te cae encima. Cuando se alejan, sales arrastrándote, con las manos doloridas de tanto apretar la roca.

#### `gorgoroth_3_2`

Los campamentos.
Desde lo alto de un montón de escoria miras la llanura. Está salpicada de campamentos: hogueras, tiendas negras, corrales y largas filas de orcos que se ponen en marcha hacia el norte, una detrás de otra, como hormigas. Toda esta tierra parece vaciarse hacia allí, hacia algo que la reclama. Al nordeste, entre las nubes, adivinas una mole oscura, inmensa, más alta que cualquier montaña, y apartas enseguida la vista. Mejor no mirarla mucho.

#### `gorgoroth_3_3`

El foso de ceniza.
Para alejarte de la calzada cruzas una hondonada llena de ceniza fina como harina. Te hundes hasta las rodillas, y luego hasta los muslos. Cada paso levanta una nube gris que se te mete en la boca, en los ojos y en la nariz. Te tapas la cara con el borde de la capa y respiras a través de ella. Tardas una eternidad en cruzar lo que habrías recorrido en un momento por la calzada. Al otro lado, escupes gris durante un buen rato.

#### `gorgoroth_4_0`

Las hogueras.
Al caer la noche tienes que pasar cerca de un campamento para no meterte en una grieta. Avanzas a gatas, aprovechando las sombras de la escoria. Estás a punto de dejarlo atrás cuando un orco que sale a orinar tropieza contigo. Su grito despierta a todo el campamento. Dos uruks negros y dos orcos se te echan encima, y a lo lejos, por toda la llanura, empiezan a sonar cuernos, uno tras otro. De pronto, todos te buscan a ti.

#### `gorgoroth_4_1`

El día sin sol.
No hay día aquí, solo una penumbra parda un poco más clara que la noche. Te tumbas entre dos rocas y te cubres por entero con la capa gris de los elfos. Duermes a ratos. Una vez te despiertan unos pasos: una patrulla de orcos pasa a tres zancadas de ti, discutiendo, y uno llega a sentarse en tu roca a sacarse una piedra de la bota. No te ve. La capa te hace parecer una piedra más. Cuando se van, tardas mucho en volver a respirar.

#### `gorgoroth_4_2`

El río de fuego.
Sigues el borde de la grieta durante horas. Más adelante se ensancha, y por el fondo corre despacio un río de roca fundida, rojo y amarillo, que hace crujir el aire. Su luz te alumbra el camino en la oscuridad, pero el calor es tan fuerte que tienes que alejarte del borde cada poco para respirar. Una piedra que pisas se suelta y desaparece en el fuego sin un ruido. Dejas la grieta en cuanto ves tierra firme a un lado.

#### `gorgoroth_4_3`

El olog del puente.
La grieta solo se puede cruzar por un puente de piedra estrecho. En el otro extremo, apoyado en el pretil, hay un trol. No es como el de las colinas: es más esbelto, de escamas negras, con escudo y martillo, y sus ojos se fijan en ti con una inteligencia fría. Ninguna criatura de esta tierra te ha parecido nunca tan peligrosa. Es un olog-hai. Se endereza, golpea el escudo con el martillo y avanza hasta el centro del puente. No piensa dejarte cruzar.

#### `gorgoroth_4_4`

Las fraguas.
Para alejarte de las filas de orcos bajas por el otro lado del montón y te encuentras con un barrio de fraguas al aire libre: martillos que golpean sin descanso, chorros de chispas, hornos que rugen y orcos sudorosos que sacan del fuego hojas de espada al rojo. Nadie mira hacia fuera; todos trabajan como si les fuera la vida en ello. Te arrastras por detrás de las pilas de escoria, entre el estruendo, hasta que el ruido de los martillos queda atrás.

#### `gorgoroth_4_5`

El campamento vacío.
Encuentras un campamento abandonado: tiendas caídas, hogueras frías y basura por todas partes. Su tropa ya ha marchado hacia el norte. Lo atraviesas buscando algo de agua, y no estás solo. Cuatro orcos rezagados revuelven los restos, peleándose por un barril. Al verte se olvidan del barril. Uno silba, otro se ríe, y los cuatro se despliegan a tu alrededor entre las tiendas caídas, cerrándote el paso por todos lados.

#### `gorgoroth_4_6`

La patrulla.
Sales de la hondonada por la otra ladera y te sacudes la ceniza. Pero tu rastro en la ceniza blanda es tan claro como un camino, y una patrulla lo encuentra enseguida: un uruk negro al mando y dos orcos que corren delante con la nariz pegada al suelo. Te alcanzan en un llano sin rocas donde esconderse. El uruk ladra una orden y los orcos se separan para atacarte por los flancos, mientras él viene de frente.

#### `gorgoroth_4_7`

La última torta.
Te detienes al abrigo de una roca negra, agotado. En el fondo del zurrón encuentras la última torta de pan élfico, envuelta en su hoja. La partes en dos y te comes las dos mitades, despacio, con los ojos cerrados. Por un momento la ceniza desaparece y vuelves a estar bajo los árboles de oro, con agua clara cerca. Doblas la hoja vacía y la guardas en el pecho, como un recuerdo. Te levantas con fuerzas que no sabías que tenías.

#### `gorgoroth_5_0`

La persecución.
Alguien ha encontrado tu rastro: empiezan a sonar cuernos por toda la llanura, y las patrullas convergen hacia donde estás. Corres hacia el oeste, alejándote de la montaña, saltando grietas y escondiéndote tras la escoria, mientras ellos te siguen con antorchas. Durante horas los llevas detrás de ti, cada vez más lejos. Al fin los pierdes en un campo de rocas. Exhausto, te das la vuelta y miras atrás. La llanura que rodea la montaña se ha quedado vacía de patrullas.

#### `gorgoroth_5_1`

Bajo el ojo.
Cruzas la llanura de noche. Al nordeste, en lo alto de una mole oscura e inmensa, arde una luz roja, y de pronto sientes que te busca, que barre la tierra como el haz de un faro. No te tiras al suelo: sigues andando, con la cabeza gacha y la capa ceñida, como una sombra más de la ceniza. Durante un instante insoportable el peso de esa mirada parece rozarte, y te tiemblan las rodillas, pero no te detienes. Luego se aparta y se vuelve hacia el norte, hacia otra cosa que le importa más. Respiras.

#### `gorgoroth_5_2`

La cueva de lava.
Encuentras la boca de una cueva larga y negra que dejó la lava al enfriarse, y te metes dentro para descansar. El aire está quieto y huele a azufre, pero aquí no te verán. Agitas el odre: queda poco. Bebes un sorbo, solo uno, y te lo pasas de un lado a otro de la boca antes de tragarlo. Tienes los labios agrietados y la garganta en carne viva. Duermes sentado contra la pared, con la espada en la mano.

#### `gorgoroth_5_3`

La sombra alada.
Cruzas un llano abierto, sin una roca, y a mitad de camino el aire se enfría de golpe. Un grito agudo baja del cielo. Una sombra con alas enormes vuela bajo, en círculos, buscando. Echas a correr hacia una grieta lejana, la única cobertura, y la sombra te corta el paso una y otra vez, obligándote a desviarte hacia el sur, lejos de donde querías ir. Llegas a la grieta sin aliento y te metes dentro. Cuando sales, el cielo está vacío y no sabes dónde estás. Te buscaba a ti, y no sabes por qué.

#### `gorgoroth_6_0`

Las faldas del fuego.
Subes por el flanco más apartado de la montaña de fuego, lejos de la calzada que asciende en espiral, por una ladera de escoria suelta que se desliza bajo tus pies. El suelo quema a través de las botas y la montaña retumba como un tambor. Desde arriba ves antorchas de patrullas que barren la llanura, buscando. Entonces haces algo que no te explicas del todo: empujas una piedra grande ladera abajo, con estrépito, y te dejas ver contra el resplandor. Las antorchas giran hacia ti. Te las llevas detrás por este flanco, lejos de cualquier otro camino.

#### `gorgoroth_6_1`

Los últimos guardias.
Al pie de la montaña hay un último puesto de guardia: una torre baja de piedra negra junto a la calzada. Solo quedan en él dos uruks negros, afilando sus espadas junto a la puerta. Intentas rodearlos, pero la escoria cruje bajo tus pies. Uno se lleva un cuerno a la boca y lo hace sonar. Por toda la llanura le responden otros cuernos, y las antorchas de las patrullas empiezan a converger hacia ti. Los dos uruks se acercan con las espadas desnudas, uno por cada lado.

#### `gorgoroth_7_0`

La bestia alada.
Te alejas de la calzada por el flanco más apartado de la montaña, con las antorchas siempre detrás. Pero hay algo que lleva tiempo buscándote por la llanura, y por fin te encuentra. Un chillido rasga el cielo y el aire se hiela, aunque el suelo arde. Una bestia sin plumas, de cuello largo y piel como cuero viejo, se posa en una roca delante de ti, y sobre ella una figura envuelta en negro, sin rostro, con una espada pálida. Un nazgûl. Retrocedes cuesta abajo, arrastrándolos contigo, cada vez más lejos del camino.

## orodruin

#### `orodruin_final`

El fin de la sombra.
El nazgûl ha huido hacia el norte, chillando, a lomos de su bestia herida, y tú te has quedado en la ladera sin entender cómo sigues vivo. Al día siguiente, la tierra se estremece. La montaña revienta: fuego, rocas y un trueno que no termina. A lo lejos, la torre oscura del norte se resquebraja y se desploma entre nubes de polvo, y un viento enorme barre la llanura. Corres cuesta abajo entre ríos de lava hasta una loma de roca firme. Cuando el humo se abre, ves pasar grandes águilas hacia la montaña. Nunca sabrás del todo qué ocurrió allí arriba, ni si las patrullas que arrastraste tras de ti dejaron libre otro camino. Piensas en los dueños de aquellas huellas pequeñas y deseas, sin saber por qué, que estén vivos. La sombra se ha ido. Con el zurrón y el odre casi vacíos tras tantos días, miras al oeste y emprendes el largo regreso a Bree.

<!-- end -->

# English

<!-- lang:en -->

## bree

#### `bree_1_0`

A Door Closing.
You close the door of your house in Bree, and the key weighs more than you expected. It is autumn in the year 3018, and the village seethes with rumour: riders clad in black asking questions at midnight, a silent Ranger haunting the inn, hobbits from away who came in with the rain. For years you have watched the East Road from your window. Today you have resolved to follow it. You settle the sword you inherited from your father, swing your pack onto your shoulder and pause in the muddy street. Before you leave, you can still bid Bree farewell.

#### `bree_2_0`

The Prancing Pony.
The warmth of the common room greets you with the smell of ale and damp firewood. Men and hobbits talk in low voices, glancing sidelong at the door. They say that last night someone forced their way into the strangers' rooms, and that the horses fled the stable in terror. In a corner, a tall, weathered man smokes without lowering his hood. The innkeeper, sweating and distracted, pours you a mug and wishes you a good journey without listening. You understand that fear has reached Bree before you have even set foot on the Road.

#### `bree_2_1`

Bree-hill.
You climb the slope as the village wakes beneath your feet. From the top of Bree-hill you watch the East Road wind away between hedges and fields until it vanishes into a dark smudge of trees: the Chetwood. Beyond, they say, lie only marshes and empty hills. The wind carries the smell of rain and smoke. Beside a stone you find fresh boot prints and the ash of a badly smothered fire. Someone else watches the Road from here, and does not seem to want to be seen.

#### `bree_3_0`

The Back Alley.
You slip out by the inn's back door and take a narrow alley between stone walls. You have barely gone twenty paces when a shadow peels away from a doorway. A sallow-faced man bars your path, a knife in his hand. He wants your purse and your sword, and he smiles like someone who has done this before. Nobody will look out to help you. Your first fight does not await you on the Road, but around the corner in your own village.

#### `bree_3_1`

The Stable Yard.
Disorder reigns in the stable yard: doors burst open, hay trampled, and a stable lad mourning the lost ponies. While you help him shut the gates, two men appear behind the trough a sack slung over one shoulder. They have used the chaos of the night to steal harness and bridles. Seeing you, they drop their loot and draw their blades. They are no Black Riders, only common thieves, but their steel cuts all the same, and they have no wish to leave a witness.

#### `bree_3_2`

The Orchards of Staddle.
You descend the hill to the south-east, towards the orchards that lie between Bree and the village of Staddle. The hens have fallen silent and a dog barks in the distance without daring to come out. Among the apple trees, a lean grey wolf sniffs at the remains of a henhouse. Hunger has driven it far from the forest, too close to the houses. It sees you, bares its teeth and does not give ground. If you do not stop it now, tonight it will be the children who find it.

#### `bree_3_3`

The Western Dike.
You follow the great dike that encircles Bree, its thick hedge running along the top, seeking a way to the Road without crossing the village. In the hollow you come upon a ragged man crouched beside a lean wolf, tossing it scraps of meat as one feeds a fighting dog. At the sight of you, the wolf bolts up the hedge with its scrap and the bandit, alone, grips a cudgel. There are folk in Bree who have learned to live with worse things than wolves.

#### `bree_4_0`

The Smithy.
Your hand still trembling, you reach the forge of the smith who has known you since you were a child. He examines your sword, clicks his tongue at the notch from the fight and lays it on the anvil. As he hammers, he tells you that strangers from the south have spent weeks buying knives and asking about the eastern roads. He hands back the blade well honed and refuses your coin. He asks only that, if one day you return, you bring him news of the world out there.

#### `bree_4_1`

The Rogues' Den.
The cutpurse wore a chalk mark on his sleeve, and the same mark appears on the door of a shed by the south wall. You push the door open and find stolen purses, blankets and a crude map of the Road with crosses on its loneliest stretches. Two men rise from a straw pallet, startled and furious. These are no mere cutpurses: they belong to a gang that waits for travellers beyond the fields of Bree.

#### `bree_4_2`

The Stable Lad.
Grateful, the stable lad offers you bread and cheese in the hayloft while he gathers what the thieves left behind. He whispers what he felt before dawn: shadows by the fence, a cold that froze the blood, and then the animals bolting through the streets. He says the Ranger left this morning with the hobbits along the Road, and that someone sold them a scrawny pony for its weight in gold. You thank him and shoulder your pack once more.

#### `bree_4_3`

The Fernys' House.
Before fleeing in a sorry state, the thieves shouted a name: Ferny. His house stands by the south road, a dwelling of shuttered windows and murky repute. You creep to the hedge and hear voices: a black-haired stranger from the south arguing about riders and coin with someone you cannot see. A twig snaps under your boot. The door flies open and out comes a burly thug with a club. Whatever is being plotted inside is none of your business, but you can no longer leave without a fight.

#### `bree_4_4`

The Hunter's Cabin.
On the edge of Staddle lives an old hunter who watched the wolf fall from his porch. He invites you in, stirs the fire and teaches you to read the tracks pressed into the mud by his door. He says that this autumn the wolves come down leaner and bolder than ever, and that in the Chetwood he has seen a huge one, grey as ash, leading the others. He gives you a hunting horn and a piece of advice: never make camp where you cannot see the sky.

#### `bree_4_5`

After the Pack.
The wolf did not come alone. In the orchard mud there are more tracks, and you follow them across the fields to a thicket of thorns that reeks of carrion. There, among sheep bones, two wolves raise their heads at once. One slinks away through the thorns; the other, the leanest, is skin and bone and will not let slip a quarry that walks straight towards it. The sun hides behind a cloud. You feel your pulse in your temples, plant your feet in the earth and raise your sword.

#### `bree_4_6`

The Keeper of the South-gate.
You reach the South-gate, where the Road leaves Bree. The gatekeeper, a nervous man who never lets go of the bar, looks you up and down before opening. He tells you that last night something passed by his lodge, something he will not describe, and that he has not slept since. He also warns you of a band of highwaymen watching the Road beyond the fields, led by a fellow they call One-Eye. He wishes you luck like someone who never expects to see you again.

#### `bree_4_7`

Beyond the Hedge.
To avoid the gate and its questions, you climb the hedge and drop into the fields on the other side. The tall grass reaches your waist and soaks your cloak. Halfway across, a movement among the thistles stops you short: a big wolf with a filthy coat is pacing alongside you without a sound. By the time you turn, it is already too close. Outside the shelter of Bree, the world does not wait for you to be ready.

#### `bree_5_0`

The East Road.
At last you tread the East Road, broad and worn by centuries of carts. On either side stretch farms, stone walls and birch copses turning yellow. You pass a pedlar hurrying back towards Bree who does not stop to greet you. The autumn sun gives little warmth, but every step takes you further from the life you know. For the first time since you closed your door, you smile: the journey has truly begun.

#### `bree_5_1`

The Houses of Staddle.
You pass through Staddle, the village of houses dug into the hillside, where as many hobbits live as Men. An old woman offers you water from the well and a handful of nuts for the road. Children play at chasing Black Riders with sticks for swords, and their mothers shout for them to come inside. In the square, a carter remarks that the Road is emptier than ever. You fill your water bottle, give thanks for the kindness and carry on eastward.

#### `bree_5_2`

The Roofs of Archet.
You take a path that climbs towards Archet, the village nearest the forest. Its houses huddle among oaks and the air smells of resin. In the tavern, woodcutters speak of a Ranger who went into the Chetwood with several hobbits, far from the Road, as if fleeing something. Others swear that at night hoofbeats can be heard beside the trees. You eat quickly, listen much, say little and set out again.

#### `bree_5_3`

A Night in the Open.
Evening falls and you decide to camp in a hollow a few paces from the Road. You light a small fire, as you were taught, and sup on stale bread watching the first stars. Far off, a fox barks. Later, half asleep, you hear hoofbeats pass slowly along the Road and halt. You hold your breath until the sound fades. At dawn no trace remains, save a strange cold in the frost-rimed grass.

#### `bree_6_0`

The Empty Fields.
You press on along the Road, where the last farms of the Bree-land give way to scrub and heather. At a bend you find an overturned cart, with neither oxen nor owner, one wheel still turning in the wind. There is dried blood on the driver's seat and tracks that vanish towards the forest. In Bree it was already whispered that highwaymen watch this stretch. You quicken your pace, hand on the hilt, alert to every shadow among the nearby trees.

#### `bree_6_1`

A Shortcut Through the Brambles.
You leave the Road to save time and plunge into a maze of brambles and heather. The shortcut proves worse than you hoped: thorns tear your cloak and the ground gives way into hidden pits. In a clearing you stumble upon a highwaymen's lookout, who raises the alarm with a whistle. Before you can flee, a hungry wolf, drawn by the noise, joins the fray. This time there are no walls of Bree at your back.

#### `bree_7_0`

The Bandit Chief.
Where the Road brushes the first trees of the Chetwood, a fallen trunk blocks the way. It is no accident. From among the ferns steps a broad-shouldered man with a leather patch over one eye and a notched axe in his hand: One-Eye, chief of the highwaymen. He comes alone: his men have stayed at the camp, sure he will need no help. One-Eye offers you the chance to leave your pack and return to Bree alive. Behind him, the shadow of the forest waits. There is only one way forward.

## chet

#### `chet_1_0`

Beneath the Chetwood.
With One-Eye defeated, you heave the trunk aside and look into the thicket. The Road runs on eastward, but rumour tells of a Ranger who led some hobbits through the forest, far from prying eyes, and you decide to follow his example. The Chetwood swallows you within a few paces: twisted oaks, ferns waist-high and a green gloom that deadens every sound. Here and there, blazed trunks and narrow paths betray the hunters. From afar comes a long howl, and another answers it.

#### `chet_2_0`

The Hunters' Path.
You choose the best-trodden path, the one opened by generations of Archet hunters. It is narrow but firm, and someone has cut notches into the trunks so as not to lose the way. You walk for hours beneath the canopy, listening to the creak of branches and the flutter of birds. At a bend you find a knotted rope tied to a tree, a sign that the path forks: one branch leads down towards the smoke of a chimney; the other, towards the remains of an old fire.

#### `chet_2_1`

The Thicket.
You would rather not follow paths that others may be watching, so you force your way through the thicket, hacking at branches and brambles with your sword. Soon you lose the sun. The forest closes around you and an acrid smell puts you on guard: a wolf has marked its territory here. You hear it before you see it, a low growl to your left. A brown-furred beast bursts from the ferns, bigger than any dog in Bree and far surer of itself.

#### `chet_3_0`

The Abandoned Camp.
The branch brings you to a clearing where someone camped not long ago, a day or two perhaps. The ashes are cold, but the ground tells a story: prints of small, bare, broad feet beside those of long boots and a pony's hooves. Hobbits, without a doubt, and the Ranger they spoke of in Bree. They covered their trail with care, though not enough for a watchful eye. By a log you see other marks too: heavy boots that came afterwards.

#### `chet_3_1`

The Trapper's Cabin.
The smoke rises from a log cabin half buried in the hillside. A bearded, wary trapper opens the door, a bow half drawn. When you tell him you come from Bree, he lowers the weapon and lets you warm yourself by the hearth. He speaks of wolves that no longer fear fire and of a huge grey male who leads them. Two nights ago they killed his dog. He looks at you with weary eyes and asks whether you truly know how to use that sword.

#### `chet_3_2`

The Hidden Stream.
Following the murmur of water, you come to a stream running between moss-covered stones. You wash off blood and sweat, drink your fill and, for a moment, the forest seems kindly. Blue kingfishers dart like arrows over the current. Downstream, the water stills in dark pools; upstream, flat stones offer a crossing to the far bank. You allow yourself a breather before deciding which way to go.

#### `chet_3_3`

The Fallen Oak.
A gigantic oak lies felled by some ancient storm, its roots in the air like a rampart. You climb along the trunk to a point where you can see over the ferns. To the north rises a column of grey smoke; to the east, the trees seem to thin over lower, misty ground. A squirrel scolds you from a branch, offended by the visit. You breathe deeply: at least now you know which way the forest opens.

#### `chet_4_0`

On the Trail.
You follow the small footprints for a good league. Whoever leads the group knows the forest: they choose ground carpeted with leaves, cross streams to throw off pursuit and shun the clearings. You lose the trail twice and find it as often again. In the end you understand that you must not catch them; whatever their errand, they would rather not be found. You respect their silence, but you note their heading: north-east, towards low, wet lands.

#### `chet_4_1`

The Heavy Boots.
The heavy boots lead you to a hollow where two men are roasting a rabbit. They are of the same ilk as One-Eye's band: long knives, stolen clothes and a list of names scrawled on a scrap of leather. They talk of claiming a reward for news of the hobbits. You have no intention of helping them earn it. When you step on a dry twig, both turn towards you and rise, grinning like wolves.

#### `chet_4_2`

The Broken Trap.
You offer the trapper your help, and he asks you to check his traps while he mends nets. In the third you find a wolf caught by a hind leg, thrashing in fury. Before you can decide what to do, the chain gives way with a snap. The beast, wounded and mad with pain, hurls itself at you without a thought. A cornered animal is the most dangerous thing in the forest, the old man had warned you, and now you understand why.

#### `chet_4_3`

By the Hearth.
You accept a night under the trapper's roof. You share a stew of mushrooms and dried meat while the wind shakes the treetops. The old man tells you of the marsh beyond the forest: still water, mists and midges that eat you alive. He also shows you how to make an ointment of bitter herbs to keep them off. You sleep as you have not slept since Bree and, at dawn, he sends you off with a loaf under your arm.

#### `chet_4_4`

The Stepping Stones.
You leap from stone to stone to the far bank. One shifts under your foot and you nearly fall into the icy water, but you catch your balance in time. On the opposite bank you find an old wooden post bearing simple signs you cannot read and a carved arrow pointing east. Someone, long ago, marked a route here. Following its direction, the forest suddenly seems a little less hostile.

#### `chet_4_5`

Eyes on the Bank.
You follow the current down to the dark pools, where the forest leans over the water. The silence is complete; not a single bird sings. Then you see them: two pairs of yellow eyes among the reeds of the bank, waiting for prey to come down and drink. You have walked straight into their ambush. The wolves burst from the water and surround you, one on each side, hackles raised and fangs bared.

#### `chet_4_6`

The Highwaymen's Fire.
The smoke leads you to a makeshift camp beneath an overhang of rock. A single man guards it, surrounded by stolen bundles: a straggler from One-Eye's band. By his look, he already knows his chief has fallen, and who brought him down. His face turns from fright to rage in an instant. He seizes a short spear propped against the rock and charges at you howling for vengeance, without stopping to think.

#### `chet_4_7`

Towards the Lowlands.
You climb down from the oak and walk towards where you saw the mist. The ground slopes gently and the oaks give way to alders and willows with damp roots. The air grows heavy and the first mosquitoes appear. Beneath a willow you find a small cairn of piled stones, mantled in moss, with offerings centuries old. You pause before it a moment, not quite knowing why, and then walk on.

#### `chet_5_0`

The Clearing of Howls.
You come out into a wide clearing, deep in tall grass and ringed by white birches. Halfway across you realise your mistake: there is nowhere to hide here. A howl rises behind you, another to your right, a third ahead. Three wolves leave the treeline and begin to close the circle, unhurried, like those who know their trade well. You plant your feet and wait for the first.

#### `chet_5_1`

The Toll Hollow.
The path drops into a hollow barred by a log laid across it. Beside it, a man with a rusty crossbow and a crooked hat demands a toll from all who pass: two coins or your life. He claims the forest has been his since the Archet hunters stopped coming. You have no intention of paying anyone to walk among trees, and you tell him so. He spits on the ground and lunges at you.

#### `chet_5_2`

The Wall of Brambles.
A wall of brambles closes off the forest and there is no way round it. You hack your way through, thorns raking your face and hands. When at last you emerge on the other side, bleeding and breathless, an old wolf covered in scars sits waiting for you, as if it knew you would come this way. It rises slowly, without a growl. It is a patient hunter, and you are tired.

#### `chet_5_3`

The Log Bridge.
A deep torrent splits the forest, and the only crossing is a bridge of logs lashed with ropes. When you are halfway over, two bandits step out of the undergrowth, one at each end. The nearer holds an axe beside the ropes and smiles: one more step and he will cut. They order you to lay your weapons on the logs. You look at the foaming water far below, then at your enemy, and decide to move first.

#### `chet_6_0`

A Rangers' Refuge.
Night falls and you find a refuge dug beneath the roots of a beech, hidden behind a curtain of ivy. Inside there is dry firewood, a clay bowl and a simple star carved into the stone. The Rangers of the North use such hiding places to roam the wild lands unseen. You sleep in shelter, listening to howls that draw near and fade away. Before dawn, one sounds very close.

#### `chet_6_1`

Marks in the Bark.
On the trunks in this part of the forest appear deep claw marks, at a height that chills your blood. Some enormous wolf has been sharpening them here, marking a territory that stretches eastward. Gnawed bones and tufts of grey fur litter the ground. The howls that have dogged you for days now seem to come from that same direction, as if the whole forest were driving you towards its lair.

#### `chet_7_0`

The Grey Alpha Wolf.
At the edge of the forest, where the trees grow sparse and the ground already smells of bog, the pack awaits you. Atop a rock, the grey alpha wolf watches you: taller than a pony, with ash-coloured fur and a scar splitting its muzzle. It is the one that leads the others, the terror of the Archet hunters. With a single growl it drives the young wolves back: this prey is its own. It leaps down from the rock and begins to circle you slowly. Beyond, in the mist, the marshes begin.

## moscagua

#### `moscagua_1_0`

The Midgewater Marshes.
With the alpha slain, the forest lets you go. Before you stretch the Midgewater Marshes: a sea of reeds, brown waters and banks of fog that drift like flocks. Every step sinks into the mire and the air quivers with the drone of countless insects seeking your skin. Even so, the mud holds fresh tracks: small feet, long boots, a pony. And others, heavier, that you like not at all, following the first.

#### `moscagua_2_0`

Among the Reeds.
You wade in among reeds taller than you, seeking the most direct way. The water reaches your knees, warm, thick and foul-smelling. Suddenly the drone becomes a roar: a cloud of midges rises from the surface like black smoke and descends upon you. They bite, they get into your eyes and mouth, and there is no fleeing with water to your knees. You have to face them.

#### `moscagua_2_1`

Tracks in the Mud.
You decide to follow the tracks, which always seek the firmest banks of earth. Soon you discover you are not alone: a man with black hair and weathered skin walks stooped ahead of you, studying the same marks. He wears clothes of the southern lands and carries a curved knife at his belt. When he notices you, he asks nothing. His hand flies to his weapon, and you understand that a witness is the last thing he wants.

#### `moscagua_3_0`

The Willow Islet.
You escape the water onto an islet where three twisted willows grow. The ground is firm and dry, a small miracle in the midst of the marsh. You shake off the insects, smear your face with mud for protection and eat a little of what remains to you. From here you hear a deep, unbroken drone coming from the north, as though something enormous were breathing beneath the water. You would rather not think too hard about it.

#### `moscagua_3_1`

The Black Water.
You risk crossing a lagoon of black water that shortens the way. Halfway across, the bottom vanishes beneath your feet and the mire seizes you to the waist. The more you move, the deeper you sink. With what calm remains, you lean back, reach a root with the tip of your sword and pull yourself along, little by little. You emerge caked in mud, shaking and alive, with a lesson well learned.

#### `moscagua_3_2`

The Spy's Camp.
Following the spy's tracks backwards, you find his camp: a blanket, a waterskin and a tightly tied bundle. Inside are black bread, a handful of silver coins and a scrap of cloth painted with a white hand. You do not know what it means, but it has the feel of something important. There is also a map with the Road drawn on it and several crosses beside a tall hill to the east. You keep the scrap and press on.

#### `moscagua_3_3`

The Marsh Light.
As evening falls you see a pale light floating over the water, not far off. You think it may be another traveller with a lantern and follow it, splashing. But the light draws away each time you come closer: it dances, goes out and kindles again further on. When at last you stop, you no longer know where you are. Around you there is only fog, still water and the croaking of frogs, which suddenly falls silent.

#### `moscagua_4_0`

Night on the Islet.
You decide to spend the night on the islet. You dare not light a fire, so you wrap yourself in your damp cloak and listen to the marsh: splashes, the moan of the wind among the reeds and, far off to the east, flashes lighting the clouds above some hills, like lightning without thunder. You wonder whether they are storms or something worse. At dawn the fog has erased everything, and the way still awaits you.

#### `moscagua_4_1`

The Nest of the Drone.
You follow the deep drone to a hollow stump rising from the middle of the water. Around it the air is a living mass of insects, and inside it something like a comb of mud throbs. As you draw near, four swarms break from the main cloud and fling themselves at you from different sides. You realise this nest is only one of many, and that somewhere in the marsh there must be a far greater one.

#### `moscagua_4_2`

The Sunken Walkway.
Recovered from your fright, you explore the shore and find the remains of a wooden walkway, half sunk but still serviceable. Someone built it centuries ago, when perhaps these lands were inhabited. You make your way along it carefully, testing every plank. On a post you discover an ancient carving, worn by the water: a crown ringed with stars. You remember that the tales of Bree spoke of a lost kingdom of the North.

#### `moscagua_4_3`

Eyes in the Fog.
As you scrape off the mud, you hear voices in a harsh tongue you do not understand. You crouch in the fog and see two swarthy men rummaging in the mud and pointing east. They are spies, no doubt, folk from the south seeking nothing good so far north. One of them sniffs the air like a dog and turns exactly towards your hiding place. There is no time to flee on legs numbed by the cold of the mire.

#### `moscagua_4_4`

The Comrade Returns.
You hide beside the camp and wait. At nightfall another man arrives, the first one's comrade, who curses at the sight of the rifled bundle. He searches about with a torch, and the light rouses two swarms of midges sleeping in the reeds. The spy discovers you at the very moment the droning cloud engulfs you both. In the chaos he chooses to attack, and the insects take no sides.

#### `moscagua_4_5`

The Hill of Crosses.
You study the spy's map in the evening light. The crosses mark a tall hill to the east, beside the Road, together with some words in a tongue you do not know. Whatever they were seeking, they were seeking it there. You turn your steps that way, choosing the firmest banks of reeds. When the fog parts for a moment, you see far off a bare summit, crowned by a ring of stones.

#### `moscagua_4_6`

The Drowned Man of the Marsh.
The light kindles again just ahead and, this time, it stays still. You approach and find it glowing over a rotten log where a skeleton lies in the remains of armour, trapped in the mire for many years. When you brush it, the log splits and releases a furious swarm that slept inside, larger and fiercer than any you have seen. The light goes out and only the drone remains.

#### `moscagua_4_7`

Flashes in the Hills.
Through the fog you make out a mound of earth and stones rising from the water. You climb it to get your bearings. From there, at last, you see more than reeds: to the east, a line of dark hills cuts the sky, and above the tallest flicker white flashes, like sparks from a distant storm. Something is happening on those heights. You wait for them to cease and, with the stars as your guide, set your course towards them.

#### `moscagua_5_0`

Firmer Ground.
Little by little, the ground hardens beneath your boots. Tufts of heather appear and the odd grey rock peering out of the moss. It is not the end of the marsh, but it is a respite: you can walk without sinking and dry your clothes in the wind. You sit on a stone and count your provisions, scantier than you would like. In the distance, the hills look somewhat nearer than yesterday. There is still much mud ahead.

#### `moscagua_5_1`

The Hidden Spring.
After days of drinking murky water, you find it almost by chance: a spring welling between white stones, clean and icy. You drink until your teeth ache and fill your water bottle. Watercress and wild mint grow around it, and the midges, strangely, keep away. You bathe your bites, which burn less, and rest a while listening to the water. Seldom has something so small seemed so great a gift.

#### `moscagua_5_2`

A Recent Camp.
You find a place where someone camped very recently: grass flattened by four small bodies and one large, branches cut with care and not an ember in sight. The hobbits and their guide, surely. One of them must have complained bitterly about the midges, for in a corner lies a heap of scorched fragrant herbs. You smile in spite of yourself. At least, for now, no one has caught up with them here.

#### `moscagua_5_3`

The Canebrake.
You enter a canebrake so tall and dense that you can barely see the sky. The canes creak and whisper with every breath of wind, and at moments you think you hear footsteps beside you. You press on, notching the canes with your sword so as not to walk in circles. When at last you come out on the other side, you find your heading is true: the ground rises gently and the fog begins to fall behind you.

#### `moscagua_6_0`

The Eastern Shore.
You choose the long way and skirt the bog along its eastern shore, where the reeds thin out and the first thorn bushes appear. It costs you a day more, but you arrive whole. From a small rise you behold the last stretch of marsh: a broad lagoon wrapped in fog, from which rises a drone so powerful that the air trembles. Every swarm seems to return there, like bees to their hive.

#### `moscagua_6_1`

The Heart of the Bog.
You cut straight through the middle of the bog, where the water is deepest and the fog thickest. It is a mistake. A Dunlending spy has followed you for hours, waiting for his chance, and he finds it when a swarm surrounds you in a narrow passage. The man bursts from the reeds with his knife raised while the insects blind you. You fight with mud to your chest and the certainty that no one will come looking for you.

#### `moscagua_7_0`

The Swarm Queen.
The lagoon at the heart of the marsh seethes with insects. On an islet of mud and bones stirs the queen of the swarm: a winged creature the size of a dog, red-eyed, its sting dripping, born of stagnant water and centuries of oblivion. Even the midges shy away from her. She has sensed your coming, and she spreads her wings with a drone that sets the reeds along the shore trembling. Beyond the lagoon, the land climbs towards bare hills and a summit crowned with stones. To reach it, you must first cross her realm.

## cima

#### `cima_1_0`

Beneath Weathertop.
You leave the marsh behind with the queen's drone still in your ears. The ground turns firm and stony, and a chain of bare hills rises before you. The tallest, to the south, ends in a ring of broken stones: Weathertop, the Amon Sûl of the songs of Bree, where they say a great watchtower once stood. The wind blows icy and carries the smell of ash. To the north, a half-erased path runs along the ridges; to the south, the East Road skirts the hills. You settle your pack and choose.

#### `cima_2_0`

The Ridge Path.
You climb to the path that winds along the spine of the hills. It is ancient: in places it is lined with dressed stones and the remains of walls that someone raised centuries ago to defend these heights. From here you see everything and no one sees you. In the mud of a bend you discover fresh tracks, several small ones and a long boot print, all heading towards the summit. The travellers whose trail you follow passed this way a few days ago. You are late, but you are on the right road.

#### `cima_2_1`

An Over-Friendly Traveller.
You go down to the Road, broad and open, which skirts the hills to the south. You make good speed, but here anyone can see you. Before long, a dark-haired man steps out from some thorn bushes and greets you like any other traveller. He talks too much and looks too much at your sword. When you turn your back to walk on, you hear the whisper of a knife leaving its sheath. He is a Dunlending, a man of Dunland, and he is not here by chance.

#### `cima_3_0`

The Fallen Walls.
The path runs through the remains of a fort: crumbling towers, lichen-covered walls, a courtyard where nettles grow. As you pass beneath an arch you hear a growl. Two lean wolves, ribs showing, emerge from a crack in the wall where they have made their den. Hunger has stripped them of their fear of men. They split apart to attack from both sides, and you look for a wall at your back before they spring.

#### `cima_3_1`

The Scout by the Stream.
You go down into a hollow where a stream runs among brambles, seeking water and shelter from the wind. Someone has had the same idea: on the bank, a man of Dunland is cleaning a bow and going over some marks on a strip of bark. He is a seasoned scout, with scars on his arms. He sees you before you can withdraw and asks no questions. He sets down the bark, draws a short axe and comes at you.

#### `cima_3_2`

The Trail of Hooves.
On the Road you find hoofprints, large and deep, of heavy horses that galloped eastward. You are not the only one following them: a pack of wolves noses along the same trail, as if those riders left behind them a promise of carrion. Four of them raise their heads at your scent. One growls; the others fan out along the verge. You understand that today the prey will be you.

#### `cima_3_3`

The Old Watchtower.
Beside the Road stands a ruined watchtower, little more than a stump of stone. From afar you see a thread of smoke: someone is using it as a shelter. You creep closer and hear three men arguing in a harsh tongue as they watch the Road. They speak of riders, of hills and of a payment that does not come. One of them steps out to stretch his legs and stumbles into you. His shout warns the others, and all three reach for their weapons.

#### `cima_4_0`

The Star Flagstone.
When the last wolf falls, you walk the fort in silence. In the courtyard, beneath the nettles, a flagstone peers out, carved with a many-pointed star and worn by centuries of rain. This place belonged to a kingdom no one in Bree names any more, and yet someone remembers it: beside the stone lie dry branches stacked with care, as if left for whoever comes after. You take only what you need for one night and give thanks under your breath.

#### `cima_4_1`

A Lone Crow.
You would rather not linger among the ruins and follow the path southward. The wind rises and forces you to walk hunched over. In the afternoon, from a ridge, you see Weathertop much closer: a round, bare hill with its crown of stones. Above it flies a crow, only one, in slow circles. There is something about that hill that wrings your heart, as if the air there weighed more. Even so, you press on.

#### `cima_4_2`

The Marked Bark.
You pick up the bark the scout left behind. It bears crude marks: a hill with a circle, some strokes that might be days and five figures, four small and one tall. Someone has been watching the travellers you follow and counting their steps. You tuck the bark into your jerkin. If the spies know so much, other, worse enemies may know it too, and the hill no longer looks like a refuge to you, but a trap.

#### `cima_4_3`

Water and Silence.
You wash your hands in the stream and sit among the brambles to catch your breath. The water runs cold and clear, and the wind does not reach the bottom of the hollow. For the first time in days you eat something hot: a little bacon toasted over a tiny flame you put out at once. As you chew, you see in the mud of the bank the prints of a laden pony. The travellers you follow drank here too.

#### `cima_4_4`

Where the Tracks End.
You follow the hoofprints until they leave the Road and fade into a field of scree at the foot of the hills. There the grass is burned in circles, and the air holds a cold that is not autumn's. The riders split up in several directions, as though they meant to surround something. A shiver rises from the nape of your neck. You do not know what they were seeking, but you are sure they were all looking towards the summit.

#### `cima_4_5`

The Road Marker.
Wounded by the bites, you seek a spring that wells beside an old stone marker at the edge of the Road. You wash your wounds and bind them with strips of your shirt. As you do, you see on the marker some letters almost worn away and, beneath them, a fresh sign scratched with the point of a knife: an arrow pointing to the hills. A Ranger's mark, you think. It is not meant for you, but it tells you which way to go.

#### `cima_4_6`

What They Were Watching.
You search the watchtower. The spies had provisions for many days, a blanket of good wool that is not theirs and a bag of silver coins minted far from here. Someone pays them well to keep watch. On the wall they have drawn in charcoal the outline of Weathertop and, beside it, a white hand. You do not know what that sign means, but you etch it into your memory. You keep the blanket: the nights here are cruel.

#### `cima_4_7`

A Shadow on the Crown.
You move away from the watchtower and seek a rise that overlooks the hill. You lie down in the heather and wait for evening. Nothing moves on the summit, but now and then the wind carries a smell of burnt wood and something else, sour, that you cannot name. When the light fails, you seem to see a shadow blacker than the night slipping between the stones of the crown. You blink, and it is gone.

#### `cima_5_0`

In the Lee of the Rocks.
The wind blows so hard you can scarcely move forward. You take refuge in a hollow between great rocks at the foot of the hills and wait for it to drop. From there you study the slope: there are several ways up and none is easy. You warm your hands with your breath, run a thumb along your sword's edge and eat the last of your dried fruit. When at last the wind relents, Weathertop is closer than ever, dark against a sky of torn clouds.

#### `cima_5_1`

The Open Heath.
You cut across an open heath that climbs towards the hill. There is nowhere to hide, and you know it. Halfway up, a figure rises from the heather where it had lain for hours: a tracker from Dunland, his face smeared with mud and a short spear in his hand. He was waiting for you. He neither shouts nor calls for help; he means to deal with you alone, and by the way he moves, he knows how.

#### `cima_5_2`

The Line of Stones.
You find a line of standing stones climbing the slope, the remains of a wall or of some very ancient causeway. You follow them, and they lead you along a hidden pass no one would guess from below. On some, the moss is scraped by recent boots. It comforts you to think that people who know these hills better than you walk this way. You climb slowly, without a sound, and when you look back, the whole world seems small and grey.

#### `cima_5_3`

The Western Slope.
You climb the western slope, the steepest, clutching at shrubs and loose rocks. Halfway up, a thick stench warns you too late: you have reached the mouth of a den. Three wolves come out of it, lean and furious, and block your way above and on either side. Below lies only the drop. You cannot flee without falling, so you set your feet against the rock and raise your sword.

#### `cima_6_0`

The Deserted Camp.
You peer into a sheltered dell on the western face of the hill, below the summit, and climb down into it warily. There is a spring, stacked firewood and the remains of a large fire. The grass is trampled and burned in patches, there are broken branches everywhere and, on the ground, signs of a struggle you cannot read. Small footprints and a pony's trail leave the dell towards the south, hurried. Whoever camped here left in haste, and something forced them to.

#### `cima_6_1`

The Crown of Stones.
You climb the last stretch to the summit. Of the great tower of Amon Sûl only a ring of broken stones remains, covered in rough grass. The ground is blackened, and some rocks seem melted by a terrible heat, though the air is freezing. Something burned here only a few days ago. On a heap of stones you find fresh scratches, like letters, that you do not understand. Below stretch empty lands beneath a leaden sky.

#### `cima_7_0`

The Wraith of the Summit.
Night falls over the hill and the wind stops dead. In the silence, the cold bites into your bones and a nameless dread tightens your chest. From where you stand you see a tall figure appear among the stones of the crown wrapped in a black mantle, faceless beneath its hood; only a pale gleam where the eyes should be. It comes towards you without haste. It is one of the Black Riders they spoke of in Bree, returned to the hill like one seeking a lost trail. This is no foe you can kill. All that remains is to endure, with steel and fire, until the shadow withdraws.

## puente

#### `puente_1_0`

The Lone-lands.
When dawn breaks, the shadow is gone and you are still standing, trembling, your sword notched and your soul frozen. You leave Weathertop behind without looking back. Eastward stretch the Lone-lands: brown ridges, withered heather, stunted copses and not a house for leagues. The Road crosses them towards the river Hoarwell and the Last Bridge. At night, from very far away, come long howls that belong to no common wolf. They are Wargs, and they are hunting.

#### `puente_2_0`

Leagues of Road.
You keep to the Road, which is at least firm and straight. You make good speed, sleeping little and badly, always with one eye on the horizon. The land does not change: ridges, stones, wind. At times the Road passes beside crumbled walls or beneath hills crowned with ruins, memories of a time when this land had kings. Now only the crows dwell here. On the second day the sky closes in and the air grows heavy with rain.

#### `puente_2_1`

The Grey Scout.
You leave the Road and strike into the wild lands to the south, where it is easier to pass unnoticed. The ground is rough, full of ravines and scrub. On the second evening, a lone Warg comes out at you from a copse: huge, with a dirty grey coat and yellow eyes. This is no ordinary hungry wolf; it is a scout of its pack, and if it escapes, it will bring the others. You cannot let it go.

#### `puente_3_0`

The Fallen Tower.
The storm catches you in open country and you run towards the only shelter you see: a ruined tower on a knoll beside the Road. Inside, the floor is dry, and long ago someone left a pile of firewood beneath a collapsed stair. You light a small fire, the first in days. Outside, the rain lashes the stones. From the arrow slit you see the Road gleaming with water and hear, between peals of thunder, a distant howl.

#### `puente_3_1`

The Dwarf from the West.
You meet a traveller walking west with an enormous bundle: a Dwarf with a braided beard and an axe at his belt, returning to the Blue Mountains. He mistrusts you until you share your pipe-weed. Then he talks: of the Wargs prowling the river, more numerous than ever, and of a pack leader as big as a pony that guides them. He advises you to cross the bridge by day and to trust no one who asks too many questions.

#### `puente_3_2`

Small Footprints.
In a sandy ravine you come upon a trail you recognise: small bare feet, a man's boots and a pony's hooves. The travellers passed this way, slowly, as though one of them could not walk fast. You follow them a good stretch among brambles and stones. The trail bends little by little to the north, back towards the Road, and you understand that they too are making for the bridge. Perhaps you are not as far behind as you thought.

#### `puente_3_3`

The Bramble Ravine.
To avoid a bare ridge where you could be seen for leagues, you go down into a narrow ravine choked with brambles and thorns. Its floor is a maze of fallen rocks and icy rills. You make slow progress, your cloak in tatters, and in the afternoon you find something that chills your blood: the remains of a deer devoured very recently and, all around, paw prints the size of your open hand. You are in Warg country.

#### `puente_4_0`

Siege at the Tower.
You decide to spend the night in the tower. Towards midnight the fire has burned down to embers and you wake with a start: something is scratching at the stone outside. Three Wargs have smelled the smoke and prowl the entrance, growling softly, as if talking among themselves. One slips through the fallen doorway while the others seek the arrow slit. You seize a brand in your left hand and your sword in your right. The night will be long.

#### `puente_4_1`

The Man at the Bend.
You do not want to lose time and push on through the rain. At a bend in the Road, a hooded man sits waiting on a stone, as though the downpour meant nothing to him. He asks whether you come from the hills and whether you have seen some halflings. He speaks with the accent of Dunland, and his hand never strays from his dagger. When you answer no, he smiles with crooked teeth and rises. One way or another, he means to get what you know.

#### `puente_4_2`

A Night with the Dwarf.
You accept a place at the Dwarf's camp that night, in the shelter of some rocks. He snores like a bellows, but he sleeps with his axe in his hand, and rightly so: before dawn, the Wargs attack. He plants himself against two, cursing in his own tongue, and shouts at you to deal with the third, the biggest, which comes straight for you with gaping jaws. There is no time to think, nor to be afraid.

#### `puente_4_3`

Those Who Ask.
You take leave of the Dwarf and go on alone eastward at a good pace. At midday you hear hooves behind you: two riders on scrawny horses overtake you and hem you in. They are men of Dunland, and they ask after a Dwarf with a great deal of baggage and some halflings travelling with a Ranger. You tell them you have seen no one. They do not believe you. They dismount unhurriedly and draw their swords.

#### `puente_4_4`

On the Same Scent.
You follow the travellers' trail without losing it, though at times it fades among the stones. You are not the only one: other prints, enormous, cross it again and again. At dusk, in a birch clearing, four Wargs lift their muzzles from the ground they were sniffing. They were after the same scent as you, and they look at you as a nuisance that can be eaten. They spread out unhurriedly to hem you in. At your back there is only a fallen trunk.

#### `puente_4_5`

The Lookout's Horn.
You leave the trail and turn back towards the Road, seeking flatter ground. On top of a ridge you see a man crouched watching the highway: a Dunlending spy with a horn hung about his neck. If he sounds it, anyone nearby will know you are coming. You creep up behind him, slowly, but a stone shifts under your boot. The man spins round, lifts the horn to his lips and, seeing your sword, draws his own.

#### `puente_4_6`

The Lair.
You follow the floor of the ravine, determined to get out of it as soon as you can. But the ravine narrows and ends in a wall of rock with a cave at its foot. The smell is unmistakable. Out of the darkness comes an old, enormous Warg, grey-muzzled and with one ear torn away, ready to defend its lair. It growls so deep you feel it in your chest. The passage is too narrow to turn your back on it.

#### `puente_4_7`

Ambush on the Crest.
You climb the ravine wall, hand over hand, until you come out onto a bare crest. There a surprise awaits you: a spy from Dunland, keeping watch from above, greets you with a kick that nearly sends you back to the bottom. You catch hold of a root and manage to haul yourself up. As you grapple, a howl answers very near, and a Warg appears over the crest, drawn by the noise. Now you have two enemies and a precipice at your back.

#### `puente_5_0`

The Moorland Well.
You find a stone well in the middle of the moor, beside the foundations of a house that no longer exists. The water at the bottom is dark, but fresh. You tend your scratches, drink and sit with your back against the well's rim. For a while you hear only the wind in the heather. You think of Bree, of your shuttered house, of how far you have come. Then you rise: the river cannot be far now.

#### `puente_5_1`

The Bridge in the Distance.
You climb a ridge to get your bearings. From the top, at last, you see the end of the Lone-lands: a band of dark trees marks the course of the Hoarwell, and over it, small as a toy, a stone bridge of three arches. Beyond, the land rises into wooded hills. But you see something else too: grey shadows moving among the trees on the bank, too many to count.

#### `puente_5_2`

The Water That Erases.
A broad, icy stream bars your way towards the Hoarwell. You wade across with the water to your knees, which go numb at once. On the far bank you dry yourself under a pale sun, then step back into the current for a good stretch to wash away your scent, as the hunters of Bree do to throw off the dogs. If the Wargs are looking for you, at least they will have to work for it. The air already smells of a great river.

#### `puente_5_3`

The Field of Bones.
You cross a plain where bones lie bleaching in the grass: horses, wolves and the odd man, fallen in a skirmish no one will ever sing of. The weapons are rusted, though some arrowheads look recent. You pick up a small shield, still serviceable, and sling it on your back. As you walk, you feel the eyes of the crows upon you. They know this field always has room for one more.

#### `puente_6_0`

Among the Reeds.
You go down towards the river by a wide detour, always against the wind, so that the Wargs will not catch your scent. The Hoarwell runs grey and swift, swollen by the rains, between banks of stone. You crawl through the reeds until the Last Bridge is in sight. On the causeway, between the arches, several grey beasts lie stretched out. A shadow far larger and darker than the rest rises and sniffs the air in your direction.

#### `puente_6_1`

A Race to the River.
You decide not to wait and run down towards the river across open country, hoping to reach the bridge before you are discovered. You do not make it. Two Wargs that were watching from a copse come after you, one on each side, and force you to a halt on a slope of loose stones. Further down, by the bridge, a deep howl answers them. You have to finish these two before the others arrive.

#### `puente_7_0`

The Pack Leader.
The Last Bridge spans the Hoarwell in three arches of ancient stone. The travellers you follow crossed here days ago; now, on the causeway, the leader of the pack awaits you: a gigantic Warg, black as coal, scarred by a thousand fights, with eyes that seem to understand too much. It plants itself in the middle of the bridge and bars your way. On the other side wait the wooded hills and the road east. To reach them, you must go through it.

## quebradas

#### `quebradas_1_0`

Hills of Ill Repute.
Beyond the Last Bridge, the Road enters a different land: steep hills clad in forest, gloomy ravines and, atop some of the heights, ruined castles of sinister aspect. In Bree they call this country the Trollshaws, and they do not say it in jest. It rains often, the trees drip and moss covers everything. Among the ferns you see broad paths, flattened by feet far too large. To the east, very far away, snow-capped peaks at last appear.

#### `quebradas_2_0`

The Forgotten Highway.
You keep to the Road, which winds between the hills, hemmed in by slopes of tall trees. Here the highway is neglected: roots heaving up the paving stones, deep puddles, fallen branches no one clears. Even so, it is the only thing that seems safe in this land. At midday you find a stone bearing a fresh notch, the sign of someone who stopped here not long ago. In the distance, a column of smoke rises among the trees.

#### `quebradas_2_1`

Beneath the Old Trees.
You leave the Road and climb a wooded slope, where the trees are so old that their crowns quench the daylight. The ground is covered with rotting leaves that muffle your steps. You make your way among gigantic trunks, following a game trail that rises and falls without rest. Now and then, a crack of branches in the distance forces you to stop and listen. The forest is not empty, and what moves in it is not small.

#### `quebradas_3_0`

The Castle on the Hill.
You climb a rocky hill crowned by the ruins of a castle. Its black walls still stand, cracked, and the wind whistles through the empty windows. Whoever built it was no one's friend: on the stones of the gateway are carvings of chained figures, blurred by time. From the battlements you command the whole valley. The Road fades eastward among the woods, and in a nearby hollow you see small, dark figures moving.

#### `quebradas_3_1`

The Fires in the Ravine.
You follow the smoke to a ravine beside the Road. You crawl through the ferns and see a camp of orcs from the Misty Mountains: shelters of branches, gnawed bones and a smoking fire. They have come down from the mountains with the autumn, seeking prey on the roads. Before you can withdraw, three of them, returning from the hunt, find you from behind. They let out a screech and charge at you with jagged scimitars.

#### `quebradas_3_2`

The Warg of the Pool.
You go down to a stream running between moss-covered rocks and follow it upstream, thinking it will lead you to open ground. But water draws others too. In a still pool, a Warg drinks with ears pricked. Around its neck hang the remains of a leather strap: someone has used it as a mount. Seeing you, it bares its fangs and leaps from stone to stone along the stream, straight towards you.

#### `quebradas_3_3`

The Bare Knoll.
You climb a knoll where the forest opens and only ferns and rocks grow. From there you see the whole country: hills upon hills, dark with trees, and between them the valley along which the Road runs. On the far side of the knoll, in a clearing, you make out three large grey shapes, still as rocks. They look like giants crouched around a dead fire. They do not move. Even so, your mouth goes dry.

#### `quebradas_4_0`

The Cellars.
You descend a broken stair to the castle's cellars. Down there it smells of mould and of something worse. Bones lie heaped, large and small, and on the walls are claw marks at a height impossible for a man. In a corner you find a burst chest, empty but for a few ancient coins stuck to the bottom with mud. You pocket them and get back to the light as fast as you can. Something big used this place as a lair, and not so long ago.

#### `quebradas_4_1`

What the Orcs Fear.
You stay on the battlements watching the hollow. The figures are orcs, a dozen of them, dragging something heavy through the trees towards the northern hills. They are not coming your way, and you give thanks. You wait for them to disappear and, meanwhile, you study the ground: the orcs carefully avoid certain ravines, as though something in them frightened even them. You take good note of those places.

#### `quebradas_4_2`

The Orcs' Plunder.
When the second orc falls, no one else appears: the camp was almost empty. You search it quickly. Among filthy hides and pots you find things stolen from travellers: a wineskin, a Dwarf's boot, a torn green cloak, a handful of horseshoe nails. The orcs hunt on the Road, and not all their encounters end well for wayfarers. You fill your bag with whatever may serve you and slip away before the rest return.

#### `quebradas_4_3`

Lost in the Forest.
You do not stay to find out if there are more. You plunge into the forest at a run, leaping roots and thrusting branches aside, until the smoke of the camp is far behind. When you stop, panting, you are lost among identical trees. You calm yourself, look for the moss on the trunks and the light between the crowns, and little by little recover your heading east. You have lost time, but you are alive, and no one follows you.

#### `quebradas_4_4`

Giant Footprints.
After the fight, you kneel by the pool to wash, and then you see them: footprints in the mud of the bank, so large that a child could lie down in each one. They have thick toes and sink very deep. They are fresh, from last night at most, and lead away upstream towards a dark ravine. You no longer have any doubt about the name of this land. You rise slowly, sword in hand, and listen.

#### `quebradas_4_5`

The Hollow Fir.
To get away from the Warg's carcass, which will soon draw others, you cross the stream on some fallen trunks, slick with moss. On the far bank the forest is lighter and smells of resin. You find an enormous fir with a hollow trunk and crawl inside to rest. From your refuge you watch a fox go by, a family of deer and, high among the branches, a squirrel scolding you. For a moment, the forest seems almost kindly.

#### `quebradas_4_6`

The Three of Stone.
You go down into the clearing with your heart in your mouth. The three shapes are trolls indeed, but of stone: enormous, hunched, one with an arm raised as if about to strike. Time has clothed them in lichen and moss. All around are fresh prints of boots and of bare feet, and the grass is flattened where someone sat down to rest. The travellers you follow were here. You smile, picturing them before these giants, as frightened as you.

#### `quebradas_4_7`

The Cave of the Slab.
You circle the clearing and, among bramble-covered rocks, find the mouth of a cave. A great slab of stone, which must once have served as a door, lies broken to one side. Inside, the floor is strewn with old bones, broken pots and coins green with rust. No one has lived here for a long time, or so it seems. But beside the entrance lies a heap of freshly torn branches and a stench that is anything but ancient.

#### `quebradas_5_0`

The Wolf Rider.
You advance along the crest of a hill, where the trees thin and the going is easier. It is a mistake: from afar you stand out against the sky. An orc mounted on a Warg has seen you and comes charging up the slope, brandishing a spear above its head. A few paces off, the orc leaps from its mount to attack you from one side while the beast rams you from the other. You must divide your attention between the two.

#### `quebradas_5_1`

The Hollow of Mist.
You cross a dark, damp hollow where the mist clings among the trunks. A howl breaks the silence, very close, and freezes you where you stand. Out of the mist comes a huge Warg, hackles raised and muzzle stained with fresh blood. It has been hunting, and it has not had enough. It circles you, measuring your strength, and then it lunges. You plant your feet in the wet earth and await the blow.

#### `quebradas_5_2`

The Broad Path.
You find a broad, trampled path that cuts through the forest like a wound. Large creatures use it, but this time those who walk it are small, noisy and many: four orcs coming down laden with a deer, quarrelling at the tops of their voices. They see you at the same moment you see them. They drop their prey, draw their blades and spread out to surround you, shouting something in their tongue that sounds like mockery. You wait for the first to close in.

#### `quebradas_5_3`

The Orc on the Bank.
You go down towards the river that runs along the valley floor, looking for a place to cross. On the bank, seated on a rock, a big, brawny orc is whetting a stolen sword. It wears a dented helmet and a plain black shield. It is taller than you and seems in no hurry. It rises slowly, slings the shield onto its arm and beckons you closer with a mocking gesture.

#### `quebradas_6_0`

The Voice from the Cave.
You decide to risk no more and make a long detour along the hillside, among crags and fallen trees. It takes you almost the whole day, but you meet no one. As evening falls, the forest suddenly opens before a deep ravine at the foot of a rock face. It smells of stale smoke and rotting meat. In the rock face gapes a great cave, and from it comes a sound you first take for a rockfall: an enormous voice, humming.

#### `quebradas_6_1`

The Valley in Darkness.
You want to be out of these hills as soon as possible and cross the valley by night, guided by the stars that peer between the treetops. In the darkness, two Wargs bar your way in a clearing, drawn by your scent. You fight almost blind, guessing their leaps by the glint of their eyes. Beyond the clearing, a wall of rock rises against the sky, and at its base the mouth of a great cave lets out the red glow of a fire.

#### `quebradas_7_0`

The Stone-troll.
Out of the cave comes a Stone-troll, alive and wide awake: almost twice the height of a man, with skin grey and rough as rock and a club made from a whole tree trunk. It has smelled you, and it likes what it smells. Behind it, the fire lights a heap of bones and stolen treasure. You know from the tales that sunlight turns its kind to stone, but dawn is a long way off. Until then, you have only your sword. Beyond these hills, the Road descends towards a swift river and the ford that leads to the land of the Elves: the Ford of Bruinen.

## bruinen

#### `bruinen_1_0`

The Vale of the Bruinen.
Dawn breaks as you leave the troll-hills behind, your body aching and your sword notched. The Road now descends through a long gorge between dark pines, and the air fills with a growing murmur: the Bruinen, the river that comes down from the mountains and marks the border of the land of the Elves. In the mud of the highway you see deep prints of shod hooves, many and close together, all running east. No one rides like that for pleasure. Something, or someone, was hunted along here only a few days ago.

#### `bruinen_2_0`

The Gorge of the Road.
You follow the highway as it drops in tight bends between walls of rock. With every step the voice of the river grows louder, though you cannot yet see it. The hoofprints never pause: not a fire, not a halt to water the horses. Those who left them were in a hurry, or hungry for something. At midday, at a bend, you think you see a figure crouch behind a crag above the Road and vanish. You stand still for a long while, your hand on the hilt.

#### `bruinen_2_1`

The Wooded Slope.
You would rather not walk where those riders have passed, so you strike into the forest that clothes the slope. You descend among birches and pines, skirting slippery stones and brooks that run down to the valley. It is slower, but here no one will see you from the Road. By mid-morning you come upon the carcass of a deer, half devoured, marked by bites from jaws far too large for any common wolf. The beast's tracks lead away through the ferns. Higher up, a crag of grey stone commands the whole valley.

#### `bruinen_3_0`

Small Feet in the Mud.
You kneel beside the highway and study the mud with care. Beneath the hoofprints, almost erased by them, lie older tracks: those of a laden pony, the boots of a tall man and small bare feet with broad toes. Hobbit feet. You remember the rumours in Bree about the Ranger who was guiding some halflings eastward. They passed this way before the riders, though not long before. What you see nowhere are tracks coming back, neither theirs nor the others'.

#### `bruinen_3_1`

The Watcher at the Bend.
You leave the highway and climb towards the crag where you saw the shadow move. Behind it crouches a man with matted hair and a brown cloak, a long knife at his belt: a spy from Dunland. He has been watching the Road, and judging by the bed of branches and the breadcrumbs, he has been at it for days. Seeing you, he hurls himself at you without a word. Further down, among the pines, you hear branches snapping: someone else is fleeing downhill, never looking back.

#### `bruinen_3_2`

The Wargs in the Bracken.
You follow the tracks through the ferns with your sword bare. Soon you hear the growling: in a clearing, three Wargs are fighting over the remains of another kill. They are gaunt, their coats caked with mud, but no less savage for it, and the moment the wind carries your scent to them they leave the meat and turn towards you. One circles to your left, another to your right, and the third faces you, baring its teeth. There is no tree to climb and nowhere to run. All that remains is to fight.

#### `bruinen_3_3`

The Grey Crag.
You climb the crag, clinging to roots and cracks, up to a ledge that overlooks the entire valley. Below, the Bruinen runs wide and turbid, a muddy brown that bears no likeness to clear mountain water. Whole trunks float in the current, and along the banks lie bands of mud and stones carried far higher than they should be. The river has risen suddenly, and not long ago, as if something had burst it from its bed. To the east, on the far bank, the forest climbs towards hidden valleys.

#### `bruinen_4_0`

The Hollow of the Camp.
The small tracks turn aside from the Road for a stretch and lead you to a hollow sheltered by rocks. Someone camped here: there is a ring of cold ash, pine boughs laid out as a bed and, among them, a scrap of bandage with dark stains. It still smells, very faintly, of crushed leaves with a sweet, clean scent you do not recognise. One of the travellers was wounded, and someone tended him with a Ranger's skill. You keep the bandage without quite knowing why and return to the highway.

#### `bruinen_4_1`

The Gallop.
You follow the trail of the black horses. Further on, the hoofprints lengthen, sink deeper and spatter mud to the sides: here they broke into a gallop. For a stretch, among them, you see different prints, those of a lighter horse that barely marks the ground, as if it ran ahead of all the rest. At the end of the stretch, a rag of black cloth flutters on a low branch. When you touch it your hand turns icy, as though you had plunged it into snow. You know that cold: you felt it on Weathertop.

#### `bruinen_4_2`

The Spy's Pack.
When the spy falls, you go through his belongings. He carries little: hard bread, a threadbare blanket, a flint and a purse of silver coins minted far from here. But in the lining of his cloak you find something more: a scrap of leather with a crude drawing of the Road, the river and the ford, and marks beside every bend. Someone wants to know who crosses into the land of the Elves, and pays well for it. You keep the leather. Perhaps the lords beyond the river will wish to see it.

#### `bruinen_4_3`

The One Who Fled.
You go down the slope after the other man, guided by snapped branches and disturbed stones. He runs like someone who knows the ground well, and at times you lose sight of him. At last you reach the bank of a swollen stream and see him on the far side, scrambling west in haste, back the way you came. It is not worth following him: whatever he has to tell, he will tell. You stand a moment watching the water, which runs murky, laden with branches and mud.

#### `bruinen_4_4`

The Cold Water.
The Wargs have left scratches on your arm and a shallow bite on your leg. You go down to a brook leaping between the stones and wash your wounds in the icy water, gritting your teeth. Then you bind them with strips torn from your own shirt. As you work, you notice that the brook sweeps leaves and branches along at a strange speed, and that on the bank stones there is a line of dried mud far above the water. A few days ago, this whole valley lay beneath a flood.

#### `bruinen_4_5`

The Den.
You follow the Wargs' trail up the slope to a cleft between pine roots, where the earth is churned and reeks of carrion. It is their den. Inside lie bones of deer, of sheep, and others you would rather not look at closely. By the entrance you find a severed leather strap, the kind used by the wolf-riders of the mountains. These beasts were not here by chance: someone brought them, or drove them towards the ford. You step out into the clean air with an unease that will not leave you.

#### `bruinen_4_6`

The Fallen Trees.
You climb down from the crag to the bank of the Bruinen. Up close, the flood is more astonishing still: whole trees torn up by the roots and heaped like straw, boulders the size of a cart shifted from their places, and the sand covered with branches and dried foam. The water has fallen somewhat, but it still roars, swift and cold. It has not rained enough for this. A woodcutter from Bree would say it was a storm in the mountains. You, after all you have seen this autumn, are no longer so sure.

#### `bruinen_4_7`

The Cleft in the Crag.
You find a cleft in the rock, deep enough to shelter you from the wind, and decide to rest a while before going on. You fall asleep without meaning to. You are woken, late in the afternoon, by a sound from far away downstream: a long, shrill cry that belongs to no animal. Your skin crawls, for you know that cry. You wait, motionless, your sword across your knees, but it does not come again. Only the murmur of the water remains, and the beating of your own heart.

#### `bruinen_5_0`

The Old Warg.
A deer trail runs down towards the ford through the birches, and you take it because it seems the shortest way. It is narrow and covered with wet leaves. Halfway down, the forest falls suddenly silent. A huge old Warg steps out from behind a trunk and plants itself in the middle of the trail. Its muzzle is grey and a scar runs across its back. It does not attack at once: it studies you, as if it knew you have been days on the road and are weary.

#### `bruinen_5_1`

Watchers by the River.
You let the noise of the river guide you as it draws ever closer. In a clearing beside some rocks, two men of Dunland sit before a small, smokeless fire, their eyes fixed on the far bank. They speak low in their harsh tongue. One of them turns, sees you and gives a shout. In a moment both are on their feet, knives in hand. They have no intention of letting anyone tell that they were keeping watch here.

#### `bruinen_5_2`

The Pack on the Scree.
To save time, you cut across a scree: a slope of loose stones, without a single tree, running straight down to the river valley. The stones roll beneath your boots and the noise betrays every step. Halfway across you hear howling above. Four Wargs come bounding down from rock to rock, far nimbler than you on this ground. There is nowhere to hide. You find a large boulder, set your back against it so they cannot surround you, and await the first leap with your sword raised.

#### `bruinen_5_3`

The Scarred Man.
You would rather not go down to the ford yet, so you climb to some high crags from which you can see without being seen. Or so you believe. Between two rocks, a man is waiting for you: a spy from Dunland, hardened and with the look of one who has killed more than once. He carries a short sword of good steel, and a scar splits his cheek. He has watched you coming from afar. He smiles without mirth, spits into his palm and advances towards you.

#### `bruinen_6_0`

The Ford.
At dusk you reach at last the Ford of Bruinen, where the Road enters the river and climbs out on the far bank, steep and dark with trees. The water still runs high and murky. This side of the bank is a chaos of mud, stones and branches, and in it you see things that tighten your stomach: black hoofprints sunk in the mire, rags of dark cloth snagged on the brambles and, half buried in the sand, the body of a great black horse, drowned. You decide to wait for the moon to rise before crossing.

#### `bruinen_6_1`

The Vigil in the Grove.
You do not trust yourself to approach the water in the open, so you post yourself in a grove from which you can see the whole ford. Night falls. The hours pass and nothing moves but the river. Then, behind you, you hear panting: two Wargs have followed your trail here and split apart to attack you from both sides. You fight among the trunks by the light of a pale moon. And, above the clamour, you sense that something on the bank seems to be listening.

#### `bruinen_7_0`

The Black Rider.
Beneath the moon, you approach the water's edge to cross. Then the mud of the bank stirs. A tall figure rises from the mire, wrapped in a tattered, sodden black cloak: a horseless Black Rider, left behind on the western bank by the flood. You feel the same cold as on Weathertop, but more bitter, as if the creature, humiliated, were seeking someone on whom to take its revenge. It draws a pale blade. Behind you the river roars louder, and on the far bank a white light moves among the trees.

## rivendel

#### `rivendel_1_0`

Waking in Rivendell.
You wake in a soft bed, in a room full of light, with the murmur of a waterfall drifting in through the window. It takes you a while to remember: the rider at the ford, the cold, and then the river rising again in white crests that swept the rider downstream amid shrieks. Afterwards, as you will later be told, an Elf-lord with golden hair lifted you from the bank and set you upon his steed. An Elf-woman changing the water in the basin smiles at you: you are in Rivendell, in the house of the lord of the valley, and here you are safe. Outside, the valley awaits you.

#### `rivendel_2_0`

The Great House.
When you can stand, you wander through the house. It is large and labyrinthine, made of carved wood and pale stone, with galleries open to the valley and stairs that never seem to lead to the same place twice. The Elves greet you with a quiet courtesy, as if wounded travellers were an everyday matter. From a window you watch people arriving from very far away: Dwarves with braided beards, Elves clad in green and grey, a tall man with a great horn at his side. Something important is being prepared in this house.

#### `rivendel_2_1`

The Hidden Valley.
You step out into the valley with the morning. Rivendell lies tucked in a narrow cleft between pinewoods, with the river running along its floor and waterfalls tumbling everywhere, white and loud. The air smells of resin and damp leaves. Along the paths pass Elves who sing as they work, and far off, beyond the trees, you hear the rhythmic ring of a hammer on an anvil. A path climbs the slope beside the highest waterfall. You breathe deeply. For the first time in weeks, you are in no hurry.

#### `rivendel_3_0`

The Hall of Fire.
At nightfall they lead you to the Hall of Fire, a long, pillared chamber where a fire always burns and where the Elves come to sing and to listen. You sit in a corner, near the shadows. The songs are in a tongue you do not understand, but the images come on their own: grey seas, ancient stars, white ships. For the first time since you left Bree, the weariness in your chest dissolves. No one asks who you are or what you seek. For tonight, it is enough to be here.

#### `rivendel_3_1`

The Ancient Books.
A soft-spoken Elf leads you to a room full of shelves, rolled maps and books older than any kingdom of Men. He teaches you to read a map of the lands stretching south of the valley, along the Misty Mountains. There, he tells you, lay Eregion, which Men call Hollin: a land of Elven-smiths, famed in another age, that fell to ruin after an ancient war. Today only stones, holly trees and silence remain. Something in those words calls to you.

#### `rivendel_3_2`

The Forge of the Elves.
You follow the sound to an open forge beside the river, where a strong-armed Elven-smith is working a red-hot blade. He notices your sword, hanging notched from your belt, and asks you to leave it with him. He holds it a long while in his hands, like one who listens. It is honest steel, he says, forged by Men, and it has seen things it should not have seen. He promises to return it better than it came. Meanwhile, he invites you to stay and watch.

#### `rivendel_3_3`

Beside the Waterfalls.
You climb the path that clambers beside the waterfall, among ferns drenched by the spray. At the top is a stone lookout from which you can see the whole valley and, to the north and east, the peaks of the mountains. There you find two tall Elves, dark-haired and with the bearing of warriors, preparing to ride out scouting. They are so alike they could be twins. They greet you, surprised to see a man of Bree so far from home, and ask you about the Road.

#### `rivendel_4_0`

The Old Hobbit.
In the middle of the hall, a very old hobbit with white hair and bright eyes rises and recites verses he has composed himself, amid the laughter and applause of the Elves. Then he sits beside you, curious to learn that you come from Bree. He asks about the inn, about the beer, about the weather on the Road. When you speak to him of the Ranger and the hobbits, he smiles mischievously and tells you there are stories best left for their owners to tell.

#### `rivendel_4_1`

The Dwarf of the Mountain.
You step out onto the terrace for some air and find a Dwarf with a white, braided beard, smoking a pipe beneath the stars. He comes from the Lonely Mountain, he tells you, far to the east, and has made a long journey to seek counsel. He speaks proudly of his people, but when he mentions kinsmen who set out years ago for the ancient mines of the Misty Mountains, his voice darkens. They have had no word of them for a long time. You fall silent, gazing at the dark mountains.

#### `rivendel_4_2`

The Southern Roads.
You spend hours bent over the maps. You learn the names: the Hoarwell, which you have already crossed; the Bruinen; the Misty Mountains, running south like a spine; and on their western slopes, Eregion. Beyond rises a peak with reddish flanks that the Elf calls Caradhras, the Redhorn, with a high pass leading to the far side of the mountains. You copy the way as best you can onto a scrap of parchment. For the first time, your journey has a shape and a direction.

#### `rivendel_4_3`

The Smiths of Old.
The Elf tells you the story of Eregion. Long ago the finest Elven-smiths lived there, in a city called Ost-in-Edhil, friends of the Dwarves who dwelt beyond the mountains. They made marvellous works, but someone of fair words and foul intent deceived them, and war destroyed them. He tells you no more, and you do not ask. He says only that in that land the holly trees they planted still grow, and that the stones remember.

#### `rivendel_4_4`

The Tempering.
You spend your days at the forge, working the bellows and hauling charcoal. The smith takes your sword apart, straightens the blade, smooths away the notches and tempers it in river water, murmuring words you do not understand. When he gives it back, it weighs the same yet seems another thing: more alive, firmer in the hand. At the base of the blade he has engraved a small Elvish sign. It means, he tells you, something like 'the one who watches'. It seems a good name for what you are.

#### `rivendel_4_5`

The Light Mail.
While your sword rests at the forge, the smith rummages in a chest and draws out a coat of mail of fine, dark rings. It was made here many years ago, he tells you, for a Ranger who never returned to claim it. He fits it on you himself with leather straps. It weighs less than a woollen shirt, and yet the edge of a knife slides over it without biting. At the end of the day he returns your sword as well, clean and sharp. You feel a different man.

#### `rivendel_4_6`

Lessons in Swordplay.
Before riding out scouting, the two Elves invite you to practise with them in a meadow by the river. They correct your guard, your footwork, the way you look your enemy in the eye rather than at his sword. They are patient, but not gentle: you end up on the ground more times than you can count. While you rest, they tell you they often ride with the Rangers of the North, and that they know well the one who brought the hobbits here. They do not tell you his name. Only that he is among the best.

#### `rivendel_4_7`

The White Horse.
You go down to the stables, where the Elves care for their steeds as if they were friends. A groom shows you a white horse, slender and proud, and tells you it was this horse that crossed the ford a few days ago bearing a wounded halfling, with the Nine hard on its heels. It reached the far bank just before the river rose. The horse sniffs your hand calmly. You are glad that the small traveller, whoever he was, came through safely.

#### `rivendel_5_0`

The Closed Council.
One morning, the house wakes different. The Elves speak in low voices and, on a porch beside the river, many gather: the lord of the house, an old man clad in grey, Elves, Dwarves, Men and a small figure or two. A guard gently stops you at some distance. It is not a council for you, he says. You watch from afar for hours. When at last they part, no one is smiling, and each walks away in silence. Something has been decided there that weighs upon them all.

#### `rivendel_5_1`

The Healer's Hands.
The wounds of the journey have closed, but the cold of the ford lingers in your bones: at times you wake frozen, with the feeling of a shadow at your side. You go to see the healers of the house. An Elf-woman with cool hands listens to you, prepares hot water and casts into it some crushed leaves with a sweet, clean scent. The steam fills your head with light. When you leave, the cold has gone. She tells you that you were fortunate: others bear that hurt far longer.

#### `rivendel_5_2`

The Night of Stars.
One night you climb to a high terrace open to the sky. Elves sit there in silence, gazing at the stars, and one of them sings softly a song they call the Queen of the Stars. You lie down on the stone, still warm, and think of Bree: of your empty house, of the inn, of the muddy street. It surprises you not to miss it as much as you expected. This land is not yours, you know, and sooner or later you will have to go on your way.

#### `rivendel_5_3`

News from the Scouts.
In the courtyard, some Elven scouts and a pair of Rangers of the North are preparing to ride out in search of the Black Riders. You approach to ask. They tell you the river swept away the Nine and their horses drowned, but no one has found the riders, for such creatures do not die as others do. They will go out in every direction: north, west and south as well, following the mountains. You tell them of the rider on the bank. They listen closely and thank you.

#### `rivendel_6_0`

The Word of the Lord of the House.
Before leaving, you ask to see the lord of the house, and he receives you in a small room, without ceremony. His face has no age, but his eyes seem to have seen the whole world. He hears your tale without interrupting. When you tell him you mean to go south along the skirts of the mountains, he warns you that the way is not safe: black birds keep watch from the sky, and men of Dunland serve a master not their own. Trust no one who asks too many questions, he tells you, and he gives you a grey cloak of Elven cloth.

#### `rivendel_6_1`

The Preparations.
Winter comes to the valley, and you know it is time to leave. You pack with care: a grey cloak of Elven cloth they give you, warmer than its weight would suggest, waybread that keeps for weeks, a water flask, rope and a pouch of herbs from the healers. You check your sword, your boots and your straps. As you work, you hear that another company is being prepared in the house too, more important than yours, but no one says where it will go or when. It is not your concern, and you know it.

#### `rivendel_7_0`

The Farewell.
On a cold winter morning you cross for the last time the narrow bridge over the river. Some Elves have come out to see you off and sing a brief song for your road. You turn once to look at the house, the waterfalls, the light among the pines, and it is hard to tear your eyes away. Then you leave the valley and set your course south, following the western skirts of the Misty Mountains. Beyond many leagues of empty land, Eregion awaits you, the land of holly. Your body is rested, and the valley lives in your memory.

## eregion

#### `eregion_1_0`

The Land of Holly.
After many days marching south along the skirts of the mountains, the landscape changes. You enter a country of gentle downs and scrub, dotted with holly trees of dark leaves and red berries, and here and there carved stones peer out of the grass: the remains of walls, fallen columns, steps that lead nowhere. It is Hollin, Eregion of old, the ancient land of the Elves. The air is clean and cold, but something is strange: not a single bird can be heard. To the east, three great snowy peaks command the horizon.

#### `eregion_2_0`

The Elven Highway.
You come upon the remains of an ancient highway of broad, well-fitted flagstones, running east between the downs. You follow it. The walking is easy, though you feel exposed: there are hardly any trees on either side and the sky is enormous. Off to one side, not far away, the ruins of an ivy-clad building rise into view. Then you see, high above, a dark smudge moving like smoke against the wind. It is not smoke. It is a flock of black birds flying in wide circles, as if searching for something.

#### `eregion_2_1`

Among the Holly.
You would rather advance under cover, so you slip in among the holly trees, which grow here tall and thick as a forest. Their leaves prick and snag on your cloak, but no one above could see you. You move slowly, following the hollows. In mid-afternoon you smell wood smoke and, soon after, hear a long howl coming from a nearby stream. You stop dead. No one should live here: this land has lain empty for centuries.

#### `eregion_3_0`

The Flock.
You keep to the highway in spite of everything, and the flock sees you. The birds come spiralling down, cawing in hoarse voices, and then you recognise them: crebain, great crows of Dunland that, so the tales say, serve as eyes for whoever feeds them. Two of them dive at you, beaks and claws first, seeking your face, while the rest watch from above. If any escapes, someone will soon know that an armed man is walking through Hollin.

#### `eregion_3_1`

The Wolf in the Ruins.
You leave the highway and run to hide among the ivy-covered ruins. The birds pass over. But you are not alone: among the fallen stones lies an enormous wolf, its coat a dirty grey and its eyes yellow, watching you with an intelligence you do not like at all. It is no wild wolf. It moves as though someone had taught it to hunt men. It growls without haste and places itself between you and the only way out.

#### `eregion_3_2`

The Fire among the Holly.
You creep towards the smoke on your belly among the holly. In a clearing lies a small camp: two men of Dunland, with dark, matted hair, roasting something over a fire beside some blankets and a bundle of provisions. From what you can catch, they have spent weeks watching this land, and they grumble about the cold and the pay. A branch cracks beneath your knee. Both spring to their feet, seize their weapons and come towards you with narrowed eyes.

#### `eregion_3_3`

Wolves at the Stream.
You go carefully down towards the stream the howl came from. On the bank, two large wolves are drinking the icy water, lean and grey-coated. But these are no common wolves: they are too tall, too silent, and one bears on its neck the mark of an old rope, as if it had been kept tied. They raise their heads together, look at you, and part without a sound, one on each side of the stream. They know exactly what they are doing.

#### `eregion_4_0`

The Stones of the City.
The highway brings you to the remains of a city: collapsed walls, squares overgrown with grass and broken arches that still bear carvings of leaves and stars. Perhaps it was here that the Elven-smiths of the tales once lived. You find a stone shaped like an anvil, split in two, and beside it an enormous holly tree, older than anything you have ever seen. All is silent. You feel like an intruder in a house where someone died long ago.

#### `eregion_4_1`

Beneath the Old Holly.
The flock passes again, lower this time, and you throw yourself beneath an enormous holly whose branches reach the ground. From inside, through the leaves, you watch it wheel over the highway for a long while. Then it moves off to the south-west, towards Dunland and, beyond it, to where they say a tower of black stone stands beside an old forest. When you crawl out, you are certain that someone, somewhere, already knows you are here.

#### `eregion_4_2`

The Fallen Tower.
Among the ruins, half a tower still stands, and you climb its stones to the top. From there the view is vast: the holly-clad downs, the highway running east and, in the distance, the mountains, with one peak of reddish flanks standing out above the rest. In a far-off hollow you see the smoke of a fire rising and, beside it, small figures moving. There are more people in Hollin than there ought to be.

#### `eregion_4_3`

The Spring.
Behind the ruins, the murmur of water leads you to a spring welling from a basin of carved stone, adorned with holly leaves. The water is icy and so clear that you can see every grain of sand on the bottom. You drink, wash your face and sit a while. For the first time since entering this land you feel something like peace, as if those who made this fountain had left in it a little of their care. You regain your strength and go on.

#### `eregion_4_4`

The Prisoner.
One of the men is still alive, wounded in the leg. You bind him and ask what they are doing here. At first he spits at you; then, fearful, he begins to talk. His people live in Dunland, and a powerful lord pays them to watch the roads: who goes, who comes and in which direction. He speaks of a captain who commands them from some ruins at the foot of the red mountain. When you ask him about that lord, he says only that he lives in a tower and knows everything.

#### `eregion_4_5`

The Camp's Belongings.
You rummage through the camp. Among the blankets you find food for weeks, a purse of silver and a scrap of parchment with crude markings: the highway, the ruins, the pass of the red mountain and, beside each place, strokes that might be counting travellers. At the foot of the drawing, someone has traced an open hand in white ink. You do not know what it means, but you do not like it. You kick earth over the fire, pocket the parchment and slip away before anyone returns.

#### `eregion_4_6`

The Pack's Trail.
You examine the ground beside the stream. The two wolves were not alone: there are prints of many more, all fresh, coming from the south-west and heading on towards the mountains. Among them, in the mud, you find the tread of a man's boots. Someone walks with the pack, or behind it, like a shepherd with his dogs. The thought chills you more than the water of the stream. You follow the trail a good way, until you lose it on a stony slope.

#### `eregion_4_7`

The Stone Shelter.
You find a small Elven building almost whole, its roof of slabs still standing and stars carved on the lintel. Inside it is dry. You light a small fire and spend the night there, your sword within reach. Outside it begins to snow. Before you fall asleep, by the light of the embers, you make out on the wall some Elvish letters, nearly worn away. You cannot read them, but you have the strange feeling that someone, long ago, wrote a farewell there.

#### `eregion_5_0`

The Elven Waymark.
You climb a high down crowned by a stone waymark, so ancient that moss covers it almost entirely. Clearing it, you discover a carved arrow pointing east, towards the mountains, and beneath it runes that look Dwarvish. The road that joined Eregion to the Dwarves beyond the range must once have passed this way. From the summit you see the mountain with reddish flanks, nearer now, with grey clouds snagged on its peak. That is where you must go.

#### `eregion_5_1`

The Ambush in the Ravine.
You take a shortcut through a narrow ravine walled with rock and covered in fresh snow. It is a mistake. Halfway along, a short whistle breaks the silence and your enemies appear at both ends: a man of Dunland with a short spear and, beside him, a grey wolf that obeys his gestures like a hunting hound. They had seen you coming from above. You cannot climb the walls or turn back. You tighten your grip on the hilt and choose your first foe.

#### `eregion_5_2`

Shadows in the Sky.
You cross an open plain, without a tree, to gain time. The sky is low and grey, and it seems empty until it no longer is: three crebain drop from the clouds in a steep dive, without a caw, straight at you. They are large, with glossy black feathers, and they attack with a cunning you do not expect from birds, one from the front and two from behind. Higher up, others circle, waiting. You must finish these before they return to tell what they have seen.

#### `eregion_5_3`

The Riverbank.
You go down to a narrow river of dark, swift water and follow its bank east, towards the mountains, sheltered by bare willows. There is no one here. In the sand you find the tracks of a band of men, more than a dozen, who marched in your direction a couple of days ago, in heavy boots, dragging something. You follow them from afar, with care. Whoever commands them is watching the way into the mountains.

#### `eregion_6_0`

The Patrol on the Saddle.
You climb towards a saddle between the downs, from which you hope to see the road into the mountains. At the top you run straight into a patrol coming down: a man of Dunland and three grey wolves on leashes, which he slips the moment he sees you. For an instant no one moves. Then the man shouts something in his tongue and the wolves hurl themselves at you. Beyond the saddle, at the foot of the red mountain, glows the light of a great fire.

#### `eregion_6_1`

The Highway to the Mountain.
You find the old Elven highway again and follow it east, as the ground begins to rise towards the mountains. The downs grow steeper, the holly thins and snow already covers the stones. As evening falls, the road passes beside some great ruins at the foot of the red mountain. There is light within their walls: a fire, the voices of men, the growling of wolves. To reach the pass, you will have to go right past them.

#### `eregion_7_0`

The Dunlending Captain.
At the foot of the red mountain, within the walls of an ancient Elven hall, the men of Dunland have made their camp. From there they watch the road to the pass. Their chief comes out to meet you in the firelight: a huge man, black-bearded, his arms covered in scars, an axe in each hand. With a gesture he orders his men to stand back: this fight he wants for himself. The captain laughs. His lord wishes to know who crosses the mountains, he says, and you are not going to cross them. Behind him, Caradhras vanishes into the clouds.

## caradhras

#### `caradhras_1_0`

At the Foot of the Redhorn.
You leave behind the ruins of the Dunlending camp, where the embers still smoke, and begin to climb. The road to the pass clambers up the flank of Caradhras, whose slopes beneath the snow have a reddish hue, like old iron. The holly trees fall away below and, with them, the last shelter. The wind comes down from the summit with a long whistle, and the northern sky has turned the colour of lead. You draw the grey cloak of Rivendell tight about you and look up. Beyond those mountains runs the trail you have followed since Bree.

#### `caradhras_2_0`

The Road to the Pass.
You follow the old road as it zigzags up the slope, half buried by snow. Rounding a bend, you stop: someone passed this way only a few days ago. There are deep prints of tall men's boots, others far smaller, almost like children's, and the hoofmarks of a laden pony. They climb towards the pass in single file, breaking a furrow. Whoever they are, they overtook you while you were fighting in Hollin, and you recall the rumours that have followed you since Bree. You tread where they trod and keep climbing, the wind in your face.

#### `caradhras_2_1`

The Frozen Gully.
You leave the road and climb a narrow gully that cuts across the slope, where the wind barely reaches. It is steeper, but the rock walls shield you and you make good speed. The silence is almost complete: only the snow creaking beneath your boots and, now and then, a shard of ice breaking loose far above. Then you see in the snow great wolf tracks, many of them, and black feathers snagged on the rocks. This gully is not as empty as it seems. You tighten your grip on your sword.

#### `caradhras_3_0`

Eyes on the Ridge.
So as not to lose the trail, you climb a bare ridge from which half the mountain can be seen. You can be seen too. Two crebain break from the clouds and dive at you, cawing hoarsely, while others keep watch from on high. They attack by turns, seeking your eyes with their beaks. If one of them returns to its master, someone will soon know that a lone traveller is climbing towards the pass. You cannot let them escape.

#### `caradhras_3_1`

The Wolf on the Trail.
You follow the company's tracks step by step, never straying from the furrow they broke. By mid-morning you realise you are not the only one: over the travellers' footprints lie fresher ones, those of a large wolf that has also followed the trail. You see it soon after, standing in the middle of the path, grey and lean, its yellow eyes fixed on you. It neither flees nor howls. It lowers its head and advances slowly, with the calm of one that has hunted men before.

#### `caradhras_3_2`

The Pack in the Gully.
You follow the wolf tracks up the gully, your sword bare. The trail narrows between two rocks and, on the other side, you find them: two enormous wolves with dirty grey coats, gnawing the bones of a mountain goat. They raise their heads together. These are no wild beasts; they move as one, and while one stays in front, the other slinks along the slope to cut off your retreat. Someone has taught them to hunt like this. You will have to fight in the snow, with no room to manoeuvre.

#### `caradhras_3_3`

Black Feathers.
You would rather not cross paths with whatever left those tracks, so you climb the wall of the gully, clinging to icy holds. At the top, on a ledge, you discover where the feathers came from: a nest of crebain, large and filthy, full of small bones. Four black birds take flight at once and fall upon you, shrieking, while you hold on to the rock with one hand. You must find your footing, draw your sword and fight on a ledge no wider than a table.

#### `caradhras_4_0`

The Shelter beneath the Crag.
Beneath an overhanging crag you find a hollow sheltered from the wind, and you are not the first to use it. The snow is flattened where several people lay down, there are ashes of a fire and a forgotten branch of firewood, and in a corner, pony dung. The fire burned fiercely: the rock above is blackened, as if it had endured a flame brighter than any wood could give. You sit where they sat and eat something. For a moment you feel less alone on the mountain.

#### `caradhras_4_1`

The Ledge.
The road narrows to a ledge skirting a precipice, scarcely wider than your boots. On the left, the rock; on the right, a white void where the wind is lost. You edge sideways, your back pressed to the wall and your cloak flapping like a sail. Halfway along, a block of snow breaks loose above your head and brushes past you into the abyss. You stand motionless, not breathing, until the sound dies away. Then you go on, step by step, until you reach firm ground.

#### `caradhras_4_2`

Voices on the Wind.
You take refuge behind a cluster of rocks to catch your breath. The wind has changed: it no longer whistles but moans, and between its gusts you seem to hear distant voices, cruel laughter, words in a tongue you do not know. You tell yourself it is weariness. You close your eyes and the voices are still there, closer, as if someone were speaking just on the other side of the rock. You look out: there is no one, only whirling snow. It is as though the mountain itself had a voice and did not want you here.

#### `caradhras_4_3`

The Slope.
To gain time you cut straight across a smooth slope of hard snow instead of following the road's long detour. At first all goes well, digging in your heels. Then the crust breaks beneath your weight and you slide down the slope, faster and faster, until you manage to stop yourself by driving your sword into the ice. You lie sprawled, gasping, your heart pounding against your ribs. You have lost height and nearly an hour, but you are still whole. You rise, shake the snow from your cloak and climb again.

#### `caradhras_4_4`

The Lair.
You follow the pack's trail upward to a low cave that reeks of rotting meat. You enter with care. It is empty, but not abandoned: there are gnawed bones, grey fur on the rocks and, in a corner, a leather collar painted with a white hand. Those wolves have a master, and their master wants them to guard this pass. Before leaving you hear, very far off, a howl answered by others. The pack is hunting. You leave the cave and keep climbing, faster now.

#### `caradhras_4_5`

The Snow Bridge.
A deep crevasse cuts the gully from side to side, and there is only one way across: a bridge of frozen snow spanning it like a fragile arch. You test it with your sword. It holds. You cross on hands and knees, spreading your weight, never looking at the dark blue that opens beneath. Just as you are about to reach the far side, the arch creaks and collapses behind you with a muffled roar. You leap to the other edge and stand staring at the gap. There is no turning back this way now.

#### `caradhras_4_6`

The Ice Cave.
You find a cave opened in the ice, with smooth blue walls that shine with a strange light. Inside, the wind does not blow, and the silence is so deep that you hear your own breathing. You sit down to rest and, by the light that filters in, discover on the rock at the back some angular runes, carved long ago. They are Dwarvish marks: someone opened this pass, or used it, when the mountains were still roads of trade. It comforts you to think that others survived here before you.

#### `caradhras_4_7`

The Spur.
You climb to the top of a rocky spur that commands the pass. The view takes your breath away: to the east, the summit of Caradhras is lost in a whirl of black clouds; to the west, far below, the downs of Hollin look like a grey carpet. And from the north the storm advances, a white wall swallowing the mountains one by one. You reckon you have a few hours. You scramble down from the spur in haste, determined to get as high as you can before it catches you.

#### `caradhras_5_0`

The Deep Snow.
You keep climbing towards the pass. The snow is already up to your knees, and every step costs as much as ten. It begins to snow in earnest: thick, heavy flakes that fall without a sound and erase the world around you. Soon you cannot see more than a few yards. You steer by the slope, always upward, and by the wind in your face. The cold seeps into your bones despite the cloak. You tell yourself the pass cannot be far, and you do not know whether you believe it.

#### `caradhras_5_1`

Wolves in the Blizzard.
You skirt the mountain along a lower slope, where the wind strikes less fiercely. It is a short respite. Out of the swirling snow appear two grey shapes trotting towards you without a sound, one on each side. They are large, lean wolves, their yellow eyes fixed on your throat. They have waited for the cold to wear you down. With snow up to your knees you can neither flee nor dodge. You plant your feet, raise your sword and await the first leap.

#### `caradhras_5_2`

Stones from Above.
You advance hugging the rock wall, seeking its shelter. That is when it begins to rain stone. First a pebble falls, then another, then a block the size of a barrel that bounces beside you and rolls on down the slope. You look up and see no one, only rock and cloud. You flatten yourself against the wall until it stops. When at last you move, you are certain that this mountain wants no one on its slopes, and you least of all.

#### `caradhras_5_3`

Cawing in the Snow.
You cross an open snowfield, without a rock to hide behind. Halfway over, a caw breaks the silence: a crebain, a huge black bird, circles above you, calling. At once a grey wolf that had been trailing you at a distance rises out of the snow, and the crebain swoops to attack with it, one from above and one from below. They work together, like the hound and the hawk of a single hunter. You turn with your sword raised; there is no way out but to fight.

#### `caradhras_6_0`

The Retreat.
You understand that the mountain will not let you pass, and you turn back. Going down is almost worse than going up: the new snow covers the hollows and every step may be a slip. Soon you hear howling behind you, ever closer. Four grey wolves come down the slope after you, bounding through the snow with more ease than you will ever have. They catch you on a level shelf. There is no sense in running further. You turn to face them, sword in hand.

#### `caradhras_6_1`

The Closed Pass.
You climb to the mouth of the pass, between two walls of rock where the wind roars like a river. There the snow forms a wall taller than you. At its foot you find a furrow hacked open, as if someone had tried to dig a way through, and then the prints of many feet turning around and going down. Others came this far and could go no further. Nor will you. You look east one last time and begin your descent.

#### `caradhras_7_0`

The Wrath of Caradhras.
Then the mountain unleashes all its fury. The sky closes in, the wind becomes an unbroken howl and the snow drives sideways, so thick you cannot see your own hands. High above, something rumbles: an avalanche breaks loose and comes down at you like a white sea. This is no ordinary storm; there is malice in it, an old will that wants to bury you. You cannot defeat a mountain. You can only endure, stay on your feet and descend westward, towards the skirts that curve south, where the ancient realms of the Dwarves lie hidden.

## durin

#### `durin_1_0`

The Dry Riverbed.
When at last you leave the snow behind, your fingers are numb and your cloak stiff with ice, but you are alive. You descend to the downs and go on south, skirting the range. On the second day you come upon the trail of a company of travellers: men's boots, small bare feet, the shoes of a pony. They too came down from the mountains and turned south. The tracks lead you to a valley through which runs the bed of a dry river, full of round stones. At its head rise sheer walls of grey rock.

#### `durin_2_0`

The Stair by the Falls.
You follow the dry riverbed, as the travellers did. It climbs among rocks to a drop where a waterfall must once have fallen; now only a thread of water trickles down the stone. Beside it, steps carved in the rock, worn and slippery, climb the cliff. You climb them. At the top awaits something you never imagined: a dark lake, still as a slab, filling the valley to the foot of the walls. The old road sinks into it. No wind blows, and the water smells of rot.

#### `durin_2_1`

The Heights of the Valley.
You dislike how hemmed in the valley is, so you climb the slope to see it from above. From the top of a crag you understand everything: the river has stopped because something has blocked its course, and its waters form a black lake beneath enormous cliffs. At the foot of the wall, on the shore, lie two huge holly trees torn up by the roots and rocks heaped together, as if a giant had flown into a rage. Behind you, in the hills, a howl rings out. They have caught your scent.

#### `durin_3_0`

The Lone Pony.
You skirt the lake along a narrow strip of ground between water and rock. Halfway along, something stirs among the stones and your hand flies to your sword, but it is a pony: a lean beast, with neither load nor saddle, its eyes wild with terror. By its shoes you know it belongs to the company you are following. You call to it softly, but it will not come near. It looks at the water, neighs and trots off northward, away from the lake. Something has frightened it badly.

#### `durin_3_1`

Goblins on the Stair.
You linger a while at the top of the steps, hidden, watching the lake. Before long you see movement: two hunched figures coming down the rocks of the shore, in ill-fitting black armour and with rusty scimitars. They are goblins, creatures of the mines, come out of some hole in the mountain. One hangs back and the other sniffs the air and climbs straight towards you. They have seen you. On the stair there is no room to flee; you will have to fight upon the steps.

#### `durin_3_2`

The Wolves of the Slope.
You keep to the heights, leaping from rock to rock, but the howls draw nearer. Three grey wolves appear on the ridge, outlined against the sky, and come down at you in a straight line. They are large and silent, and they move as though someone had taught them to hunt men. There is no tree to climb and no cave to crawl into. You find a rock to guard your back and make ready. Below, the black lake lies still, indifferent to everything.

#### `durin_3_3`

The Edge of the Cliff.
You make your way carefully down the slope to the top of the cliffs, right above the spot where you saw the uprooted trees. From here you hear the water lapping at the stone far below. You peer over: the wall drops sheer, smooth, without a single mark, and at its foot the heaped rocks form a barrier along the shore. Something has struck here with terrible force; there are fresh cracks in the rock, white as wounds. You search for a way down and find one: a narrow chimney between two crags.

#### `durin_4_0`

The Pony's Hunters.
You run after the pony along the shore, fearing it will break a leg among the stones. You are not the only one pursuing it. A grey wolf bursts from among the rocks and lunges at the animal, which rears and flees up the slope. The wolf then turns on you, its hackles raised: it prefers prey that does not run. You place yourself between it and the way the pony went and raise your sword. That beast will get home, if you have any say in it.

#### `durin_4_1`

The Scavenger.
You follow the shore towards the cliffs. Near the wall, the ground is churned: freshly fallen rocks, split trunks and a smell of slime that turns your stomach. Among the stones, a lone goblin is grubbing in the mud for anything of value. Seeing you, it lets out a screech and hurls itself at you with a curved knife in each hand, more rabid than brave. If it escapes, it will warn its kin. You cannot let it go.

#### `durin_4_2`

The Burrow.
The other goblin, the one that hung back, has fled. You follow it among the rocks of the shore to a crack in the slope, half hidden with branches, into which it slips. You peer in. It is a burrow, not an entrance: a narrow hole reeking of smoke and burnt flesh, where three more goblins leap to their feet beside a small fire. Now there are four, and the firelight gleams on their blades. You can no longer retreat without turning your back on them.

#### `durin_4_3`

Quarrel on the Shore.
At last you go down to the lake shore. The stones are coated with green, slippery slime, and the water, so close, seems blacker still. Beside a fallen trunk a strange scene surprises you: a goblin and a grey wolf fighting over the remains of something dead, snarling at each other. Seeing you, they forget their quarrel. The wolf crouches to spring and the goblin, cleverer, moves aside to attack you from the flank. The lake is at your back, and you do not like it at all.

#### `durin_4_4`

The Pack.
The first wolves were only the vanguard. Four more come down from the ridge, two on each flank, noses pressed to your trail. You could hide, but they would find you. You seek a narrow gap between two rocks, where they cannot all attack at once, and wait with your sword raised. The first one leaps; the others follow, snarling, seeking an opening to reach you. The fight will be long, and no one is coming to help you.

#### `durin_4_5`

Flight among the Crags.
You hear more howls on the ridge and do not stay to count them. You bound down among the crags towards the lake, seeking a place where the wolves cannot surround you. Most of them lose your trail among the rocks, but one, the largest, is not deceived. It catches you at the foot of the slope, where the stones give way to the shore, and stands before you with its fangs bared. This one will not give up. You will have to finish it.

#### `durin_4_6`

The Blocked Doors.
You climb down the chimney to the foot of the wall. Up close, the destruction is greater still: enormous rocks piled against the cliff, split holly trees, churned mud. Behind that heap, you suspect, there is a door, though you cannot see so much as a crack. A goblin is clambering over the rocks trying to shift them, without success, cursing aloud in its tongue. Seeing you, it draws its weapon. It seems it too is seeking a way in, or a way out.

#### `durin_4_7`

The Crack at the Summit.
You walk along the top of the cliff looking for another way down. You find a fresh crack splitting the rock from top to bottom, as if the mountain had been cleft by a blow. It smells of damp and of something rancid. You are not alone: up through it climbs a goblin leading a wolf on a rope. Seeing you, the goblin slips the beast with a shrill laugh and screams an order. The wolf leaps at you and its master follows with a spear. Up here, one false step would send you into the lake.

#### `durin_5_0`

The Last Tracks.
You search for the trail of the travellers who came down from the mountains. You find it near the wall, on a strip of ground the water has not covered: the boots, the small feet, the pony. The tracks stop at the foot of the cliff and there mingle with other marks, broad and slimy, coming out of the lake as if something had dragged itself through the mud. After that, nothing: no blood, no bodies, only rocks heaped against the smooth stone. They went in, you understand, and something closed the way behind them.

#### `durin_5_1`

The Moving Water.
You sit on a rock and watch the lake. Its surface looks like black glass, without a ripple. But after a while you see it is not entirely still: near the shore slow rings appear, as if something were breathing beneath, and a line of bubbles moves forward and stops. Dead fish float belly up. In the mud of the shore are broad, fresh furrows, made by no animal you know. You move away from the water without a sound and never turn your back on it again.

#### `durin_5_2`

The Ancient Channel.
You pause to think how this lake was formed. It is not natural: downstream, a barrier of stones and earth blocks the riverbed and holds back the water, which barely seeps over the top. But something else catches your eye. Following the line of the old riverbed towards the cliffs, you understand that the river did not rise from any ordinary spring: it flowed from the mountain itself, through a channel now beneath the water, beside the wall. If water could come out that way, perhaps a man could go in.

#### `durin_5_3`

Lines in the Moonlight.
You find a dry corner far from the water and rest until nightfall, your back against a rock. When the moon rises and the clouds part, you see something that leaves you breathless: on the smooth face of the cliff, silver lines kindle, fine as threads, tracing an arch, some trees and a star. It is an enormous door that shows itself only by moonlight. But it is shut and blocked by fallen rocks, and you do not know its secret. After a while the clouds return and the drawing fades.

#### `durin_6_0`

The Shore beneath the Wall.
You advance along the shore towards the base of the cliff, stepping carefully among the slime-coated stones. The water is a pace away, black and still, and you try not to look at it. Behind you, however, you hear now and then a soft splash, as though something were following you beneath the surface, matching your pace. You turn and there is nothing, only widening rings. You quicken your step. The wall draws near, enormous, and at its foot the water swirls against fallen rocks.

#### `durin_6_1`

The Last Wolves.
You cut towards the wall through a chaos of fallen crags, leaping from one to the next. Halfway there, two grey wolves that had been trailing you for some time without a howl catch up with you. The first leaps from a higher rock and nearly knocks you down; the second goes for your legs. You fight balanced on slippery stones, with the lake gleaming below like a black eye. You know that if you fall into the water, you may never come out.

#### `durin_7_0`

The Watcher in the Water.
At the foot of the wall, level with the water, the rock has split: the blows that felled trees and heaped up rocks have also cracked an old Dwarvish channel, and through the fissure comes a breath of cold air from inside the mountain. It is your way. You wade in up to your knees to reach it and, at that moment, the lake boils. Immensely long arms, pale and slimy, rise from the surface and coil about, seeking you. You cannot kill what lives down there; you must strike it until it sinks and lets you pass.

## moria

#### `moria_1_0`

Beneath the Mountain.
The channel is narrow, cold and half flooded. You crawl on for what seems like hours, until the water falls behind and the gallery widens. You light a torch with trembling hands. The light reveals smooth walls, carved with a precision no man could equal, and steps climbing into the dark. The channel has left you on a low level; the long roads of the city pass above you. You are inside Khazad-dûm, the ancient realm of the Dwarves, which Men call Moria. Nothing can be heard. Somewhere to the east, there must be a way out.

#### `moria_2_0`

The Sentry on the Stair.
You climb a broad stair, its steps worn by thousands of Dwarven feet, rising through the rock for an endless time. At the top it opens onto a passage, and there a reddish light glows: the torch of a goblin on guard, warming its hands, bored, until it sees your own flame. It shrieks and hurls its spear at you. You cannot let it raise the alarm in these halls. You race up the last steps, sword first.

#### `moria_2_1`

The Troll of the Cistern.
You would rather not climb, so you follow a low tunnel that runs east at the level of the rock. After a while it opens into a chamber with a cistern of still water, carved in the stone. Something large is drinking from it: a cave-troll, with grey, scaly hide and arms that nearly brush the floor. It smells you before it sees you. It turns, water streaming from its mouth, and raises an iron club. The chamber is small and there is nowhere to hide.

#### `moria_3_0`

The Hall of Pillars.
Beyond the passage opens a hall so vast that your torch reaches neither ceiling nor walls. You see only pillars, rows of pillars carved like tree trunks, vanishing into the dark like a forest of stone. You walk among them in silence. Suddenly, very far away, you see a pale white light, unlike any fire, moving slowly along another gallery and disappearing. Someone else walks these halls. Perhaps it is the travellers who entered by the western door.

#### `moria_3_1`

The Cold Forges.
An open door leads you into the forges. They are enormous, with furnaces carved in the rock and anvils the size of a cart, and everything has been cold for many years. Tools lie scattered across the floor, hammers, tongs, a Dwarvish axe split in half. Some walls are blackened by a fire that did not come from the furnaces, and in a corner you see black arrows lodged in the wood. There was fighting here. You run your hand over an anvil and think of those who worked at it.

#### `moria_3_2`

The Great Shaft.
The tunnel opens onto the edge of an immense shaft, so wide your torch cannot light the far side. A stone walkway without a railing runs around it. You lean out carefully. Below there is nothing, only blackness, but from the depths rises a hot, dry air that smells of iron and burning, like the breath of a distant forge. For an instant you seem to feel a tremor in the rock. You draw back from the edge with your hair on end, not quite knowing what you fear.

#### `moria_3_3`

The Mines.
You go down into the mines by a stone ramp. Here the tunnels are rougher and branch in every direction, and veins in the walls glitter in the torchlight like frost. There are overturned carts, rusty picks and bones you would rather not look at. The Dwarves have not worked here for a long time. But the silence is not complete: very far off, in some tunnel, rings the blow of a pick against rock, rhythmic and patient. Someone is still digging in the dark.

#### `moria_4_0`

Footprints in the Dust.
You head towards where you saw the light, but when you reach the gallery no one is there. In the dust of the floor, however, footprints come down a stair and cross the gallery: large boots, small feet and a round mark, like that of a staff planted at every step. They vanish northward, up a rising stair. You understand that the channel has saved you a long road: they must have made a great detour through the upper levels. For a moment you wonder whether to wait for them. Then you turn away: whatever their road, it is not yours to cross. You seek your own way out.

#### `moria_4_1`

The Patrol.
You take a wide passage running east. You move fast, too fast. Rounding a corner you run headlong into a patrol: three hunched goblins with torches and, behind them, a far larger orc in a mail shirt and a horned helm, growling orders. He is a captain. He looks at you, grins to show his fangs and draws a broad, notched sword. The goblins slink off to cut your retreat. They are used to hunting in these passages.

#### `moria_4_2`

The Looters.
You search the workshops surrounding the forges, in case anything useful remains. You find a Dwarvish lamp without oil and little else. Leaving the last one, you discover you were not the only one searching: four goblins laden with scrap come in through the other door, squabbling over a dented helm. They freeze at the sight of you. Then they drop their loot and draw their knives, grinning. Four against one seems a good bargain to them.

#### `moria_4_3`

The Channel of the Forges.
A stone channel that once carried water to the forges still runs along a wall. You follow it. The water is cold and clean, and you drink your fill. The channel leads you down a narrow passage, far from the great halls, to a small chamber where some Dwarf must once have lived: a stone bench, an empty niche, runes on the lintel. You sit down to rest. For the first time since entering the mountain, you close your eyes for a moment.

#### `moria_4_4`

The Guardroom.
You skirt the shaft along the walkway, hugging the wall, to a door that opens onto a guardroom. Inside you find the remains of an ancient battle: bones of Dwarves still in their mail, shattered shields, black arrows. The last defenders died here, facing the door. On the wall someone carved hasty runes, crooked, which you cannot read. There is no need: you understand it is a farewell. You bow your head a moment before going on.

#### `moria_4_5`

The Bridge over the Shaft.
A narrow stone bridge crosses the shaft through its centre, without a railing, and you decide to risk it. Halfway across you hear heavy footsteps that make the stone tremble. At the far end appears a cave-troll with a goblin perched on its back, prodding it with a spear to steer it. The troll sees you and roars. There is no room to step aside and no time to turn back. You plant your feet on the stone and wait, with the abyss on either side.

#### `moria_4_6`

The Diggers.
You follow the tapping of the pick through the tunnels to a torchlit gallery. There several goblins are at work, hewing the rock under the eye of a large orc with a whip. Seeing you, the miners flee shrieking down the side tunnels, but the orc does not. He is a captain, broad-shouldered and covered in scars, and he seems almost pleased that something has broken the monotony. He cracks his whip, draws an axe and comes at you without haste.

#### `moria_4_7`

The Silver Vein.
You slip into a narrow vein that branches away from the main tunnels, so low you must go stooping. At its end it widens into a small chamber. In its walls gleams a metal that is neither silver nor iron: bright, cold, with a radiance of its own that grows as you bring the torch near. You have heard legends of that metal and of the greed it awakens. You do not touch it. You rest a while in its light, in silence, and then retrace your steps.

#### `moria_5_0`

The Tomb in the Light.
You climb through passages and stairs until you see true light: a shaft of grey daylight falling through a high opening at the far end of a chamber. Beneath it, in the centre, stands a pale slab carved with runes: a tomb. All around, dust covers the bones of Dwarves and orcs, notched axes, burst chests and a book with pages slashed through by a blade. No one has passed this way in years; there is not a single footprint in the dust. Here the last of someone's people held out. You cannot read what the stone says, and you touch nothing. After a moment of silence, you go on.

#### `moria_5_1`

Drums in the Deep.
Then the drums begin. They sound far below, in the depths of the mountain, slow and deep, and set the rock trembling beneath your feet. You freeze. At once, on every side, you hear running feet, shrieks and harsh orders: the mines are waking. You break into a run eastward, but as you burst into a hall, two goblins and an orc captain bellowing at his troops bar your way. They were not expecting you, but they do not hesitate. You will have to hack your way through.

#### `moria_5_2`

The Hidden Chamber.
You slip into a side chamber, cover your torch with your cloak and wait in the dark. Soon you hear footsteps, many and heavy, and harsh voices in a hideous tongue. Through the crack of the door you watch a column of orcs and goblins with torches marching past, hundreds perhaps, heading west in haste, as if on a hunt. One stops to sniff beside your door. You hold your breath. Then it follows the others. When the last echo dies away, it is long before you dare to come out.

#### `moria_5_3`

The Troll in the Passage.
You choose the most direct passage east, though it is the most exposed. It is wide and straight, lined with statues of Dwarf-kings, and you advance quickly. Too quickly. One of the statues moves: it is no statue, but a cave-troll that was sleeping upright in a niche. It wakes with a grunt, shakes the dust from its grey hide and blocks your way with its whole body. There is no turning back: the passage belongs to the troll, and you will have to win it.

#### `moria_6_0`

The Hall of the Chasm.
You descend a broad flight of steps towards the deepest levels of the city. The air grows hotter with every step. Below awaits an enormous hall with a soaring roof, its floor split by a chasm that gives off a red glow, like embers. At the far end you glimpse a still greater drop and, beyond it, an arch that might lead to the way out. You hear drums in the distance, ever faster. Something is coming, and it is not orcs. You hurry towards the far end of the hall.

#### `moria_6_1`

The Burning Passage.
You slip into a narrow passage that slopes downward, twisting like a worm. It is hot, ever hotter, and the walls sweat. Through cracks in the rock a red gleam shows now and then, and the smoke makes you cough. You hear drums in the distance, ever faster, and another, deeper sound, like a vast bellows breathing. At last the passage opens all at once onto an immense black space, and before you, over an abyss, stretches a bridge of stone.

#### `moria_7_0`

Durin's Bane.
The bridge is a narrow arch of stone, without a railing, flung across a fiery abyss. On the far side, stairs climb towards the eastern gate. Behind you, the drums fall suddenly silent and the goblins flee, shrieking. Halfway across, the heat strikes you: from the chasm rises an immense shadow with a heart of flame. You cannot defeat it; you can only endure its blows of fire and press on. At the height of the struggle, the shadow halts and turns its head towards the western halls, towards a white light and approaching footsteps, and lets you escape. You climb the stairs towards the eastern gate and the light of day, never looking back.

## sombrio

#### `sombrio_1_0`

The East Gate.
You climb the last steps blindly and, all at once, the air changes: cold, clean, smelling of snow. You pass through a vast, broken gate and step out into the light of day. It is a grey light, beneath an overcast sky, but after so much darkness it makes your eyes stream. At your feet opens a long valley between mountains, the Dimrill Dale, which the Dwarves called Azanulbizar. At its far end gleams a still, dark lake. Behind you, very deep, the drums still rumble. You cannot stay here.

#### `sombrio_2_0`

The Stair by the Torrent.
Beside the gate begins a path of carved steps that descends the slope alongside a white torrent. You take it without a thought: it is the shortest way to the valley floor. The steps are worn and slippery with frost, and the water leaps beside you with a roar that drowns every other sound. That troubles you. Every few paces you turn to look at the gate, high above, black as an open mouth.

#### `sombrio_2_1`

Among the Crags.
You do not trust the stepped path: anyone coming out of the gate will see it. You strike off along the slope, leaping from rock to rock among dry heather and fallen crags. It is slower and more tiring, but from up here you command the whole valley: the lake, the torrent, the river running south-east. You stop to catch your breath behind a large stone. Then you hear harsh voices coming down from the gate.

#### `sombrio_3_0`

Goblins on the Steps.
You descend as fast as you dare. Halfway down the stair, stones roll from above and strike your heels: four goblins have come out of the gate after you and are bounding down the steps, eyes narrowed against the light. They do not like the day, but they hunger for the chase and know you are alone. They catch you on a landing where the torrent spatters the stone. You turn to face them, sword in hand.

#### `sombrio_3_1`

The Archer on the Ledge.
You leave the stair and go down along the bank of the torrent, leaping between wet rocks to gain time. A black arrow buries itself in the moss a hand's breadth from your fingers. You look up: on a ledge beside the stair, an orc archer is drawing his bow again, wrapped in rags against the light. He is alone, but from up there he can riddle you with arrows. There is nowhere to take cover. You climb towards him, rock by rock, before he finds his mark.

#### `sombrio_3_2`

Orcs on the Slope.
You go on along the slope towards the lake, crouching among the crags. The voices draw nearer and, rounding a rock, you run straight into them: a large orc of the mountains, long-armed and bearing a short scimitar, and two goblins, one of them burdened with his spear. They had come out to scour the slope. The orc laughs to see you alone and wrenches the spear from the goblin's hands. Then all three hurl themselves at you.

#### `sombrio_3_3`

The Watchers on the Cliff.
You climb towards the cliffs, thinking no one will look for you so high. You are wrong. On a ledge, two figures are watching the valley: an orc of the mountains and an archer with his bow on his back. They are scouts, sent to see who has come out of the gate. The orc sees you first and growls a warning. The archer unslings his bow and the orc draws his blade. You are too close to flee.

#### `sombrio_4_0`

The Mirror Lake.
At the foot of the stair, the path runs beside the lake. It is long and narrow, of a blue so dark it seems black, and no wind ruffles its surface. The Dwarves called it Kheled-zâram, the Mirrormere. You lean over the shore without quite knowing why. In the water you see the mountains upside down, sharp as if carved, and among their peaks pale glints, like gems, that the grey sky cannot explain. You gaze longer than you would wish. At last you draw away, feeling you have seen something not meant for you.

#### `sombrio_4_1`

Durin's Stone.
Near the shore of the lake stands a solitary column, broken at the top, with runes worn by centuries of rain. You cannot read them, but you remember what you heard told in Rivendell: that here, in the first days, the first of the Dwarf-kings looked into the lake and resolved to found his realm beneath these mountains. That realm is the darkness from which you have just escaped. You rest your hand on the cold stone a moment, as one who greets the dead, and go on your way.

#### `sombrio_4_2`

The Springs of the River.
Following the torrent you come to a hollow where several springs well up, their water so cold it makes your teeth ache to drink. From them rises a clear river that runs south-east between white stones: the Silverlode, they told you in Rivendell, the river that flows down towards the woods. You wash your face and hands, black with soot and blood, and for a moment the water seems to carry away something of what you saw down below. You fill your waterskin and go on.

#### `sombrio_4_3`

Remnants of an Old Battle.
The stony slope you cross is not only stone. Among the heather peer rusted helms, spearheads and bleached orc bones, so old they crumble at a touch. A great battle was fought here long ago, Dwarves against orcs, at the very gates of the lost realm. You walk slowly, with respect, among what little remains. On a rock you find a Dwarvish axe still whole, without a trace of rust, but you leave it where it lies. It is not yours, and here no one has forgotten whose it is.

#### `sombrio_4_4`

Those Who Leave the Gate.
From high on the slope you watch the gate through which you came out. After a while someone else emerges: a small company of figures coming slowly down the stair, some tall and others the height of a child. They do not run like those fleeing a battle, but walk like those who bear a burden. Reaching the bottom, one sinks to his knees and the others turn towards the gate, in silence, for a long time. You understand they have left someone in there. You do not approach. It is not your grief.

#### `sombrio_4_5`

The Fallen Watchtower.
On the slope you find the remains of a Dwarven watchtower: three walls of dressed stone and a half-collapsed roof. Inside there is old ash and a dry corner, sheltered from the wind. You sit for the first time since the bridge. Your hands are shaking. You eat a little hard bread from the provisions of Rivendell and close your eyes. When you open them, the sky has darkened a little more. The shadows of the valley lengthen and, with them, fear returns.

#### `sombrio_4_6`

The Highest Cliff.
You climb to the top of a cliff from which the whole valley can be seen. To the west, the black mouth of the gate; below, the lake, still as an eye; to the south-east, a river running down between ever gentler hills until it is lost in a distant smudge of trees, grey and golden at once, though it is the dead of winter. You have heard tales of such a forest, and not all of them are reassuring. But it is the only road that does not lead you back into the mountain.

#### `sombrio_4_7`

The Goat Path.
You come down from the cliffs by a path so narrow that only goats can have made it. At a bend you find fresh prints in the mud: hobnailed boots, many of them, heading down into the valley. Orcs. They have come out of the mountain by some side door and now roam the valley, somewhere between you and the south-east. You follow the tracks carefully, from afar, until they split into two groups. One goes towards the river; the other, towards the lake.

#### `sombrio_5_0`

The Riverbank.
You go down to the river that rises in the valley and begin to follow it south-east. It runs swift and clear over white stones, and its voice is the only sound. The path along the bank is ancient, paved in stretches by the Dwarves, and you make good progress. Little by little the mountains fall behind and the valley widens. You pass a deer drinking, which watches you without fear. For a while you almost forget that someone may be coming after you.

#### `sombrio_5_1`

The Guarded Ford.
You want to cross to the far bank of the river, where the ground seems more sheltered, and you look for a ford. You find one where the water spreads over a bed of pebbles. But you are not the first to arrive: on the opposite bank, an orc of the mountains is filling a waterskin while another, with a black bow, keeps watch. They see you as you see them. The archer looses; the orc splashes across the water, scimitar raised. You plant yourself on the pebbles and wait.

#### `sombrio_5_2`

A Halt in the Hollow.
You take refuge in a hollow among great rocks, where the wind cannot reach, and allow yourself to rest. Evening falls quickly in the valley. When the sun sets behind the mountains, you hear the drums again, distant, and then a clamour of voices and horns coming from the gate. You know what it means: with the dark, the orcs come out to hunt. You have no intention of waiting for them. You rise, settle your cloak and set off south-east, faster than before.

#### `sombrio_5_3`

The Forward Scout.
You quicken your pace to leave the valley behind before nightfall. The path winds down between crags and, coming out of one bend, you find an orc of the mountains sitting on a stone, eating something you would rather not look at. He is big, in a coat of rings and with a leather shield, and from the way he rises he does not seem afraid. Perhaps he went ahead of the rest of his band. He spits, takes up his spear and comes towards you.

#### `sombrio_6_0`

The Pursuers.
When night falls, you decide not to stop. You walk beside the river, guided by its sound, with the moon peering now and then between the clouds. After some hours you hear something behind you: quick footsteps, breathing, a jingle of metal. They catch you on an open stretch of the bank: two orcs of the mountains and two archers running ahead on your trail. Someone larger, who has stayed behind, commands them, but these four do not wait. You turn to meet them.

#### `sombrio_6_1`

The Fir Copse.
When night falls, you slip into a copse of firs beside the river and hide among the roots, wrapped in your grey cloak. Soon you hear footsteps on the bank, many of them, and voices arguing in their hideous tongue: a band of orcs is following the river south-east, hunting someone. One of them, enormous, bellows orders. They pass a few paces from you. You wait until the noise fades and emerge with caution. You know they are ahead of you, and that your road is the same as theirs.

#### `sombrio_7_0`

The Captain of the Mountain.
The river widens and the hills grow gentle. In the distance you can already make out, dark against the sky, the line of a forest. Then, on the bank, an orc captain of the mountains comes out to meet you, taller than you, with a dented helm and an enormous scimitar. He comes alone, having left his band behind to claim the prey with his own hands. The captain grins at the sight of you: he has been hunting you all night. You fight with your feet in the mud and the river at your back. Beyond him, to the south-east, the trees await you.

## nimrodel

#### `nimrodel_1_0`

The Eaves of the Wood.
With the captain sunk in the river, the path along the bank brings you at last to the first trees. They resemble no trees you know: smooth grey trunks, immensely tall, and branches laden with golden leaves in the dead of winter, gleaming faintly beneath the moon. Under them the air is different, stiller and cleaner. From the west another river comes down to join the one you have followed, singing among the stones. In Rivendell you heard its name: the Nimrodel. Behind you lie the orcs; ahead, a forest few men speak of without lowering their voices.

#### `nimrodel_2_0`

Beneath the Golden Trees.
You venture beneath the trees. Golden leaf-fall covers the ground and muffles your steps, and the moonlight filters through the branches like gold dust. No animal can be heard. You have a feeling, ever clearer, that you are not alone: that something, or someone, is watching you from high above. You look up and see nothing but leaves. Even so, you take your hand from your sword without noticing, like one entering another's house.

#### `nimrodel_2_1`

Orcs on the Bank.
You would rather not enter an unknown forest by night, so you follow the bank, between the eaves and the water. Soon you see you are not the only one to think of it: four orcs of the mountains, filthy from the road, are squatting by the river, arguing. One lifts his nose, sniffs and gets to his feet. They do not want to go in among the trees, but they very much want to catch you. They advance along the bank, two on each side.

#### `nimrodel_3_0`

An Archer among the Trunks.
A barely visible path winds between the trunks and you follow it. In a small clearing, a whistle cuts the air and a black arrow buries itself in the tree beside your head. It does not come from above, but from behind a fallen trunk: an orc archer who has strayed into the forest, hunched and jittery, his eyes glinting in the dark. He looses again, misses, and fumbles for a third arrow with clumsy fingers. This place frightens him more than you do. You rush at him.

#### `nimrodel_3_1`

The Disputed Prey.
You follow the murmur of water through dry ferns until you hear something else: a growl and a harsh laugh. Beside a stream, an orc of the mountains and two archers have cornered a wounded deer and are arguing over who will keep it. You tread on a branch. All three turn at once. The deer seizes its chance and flees among the trees; the orcs, for their part, seem to think you are better prey.

#### `nimrodel_3_2`

The Fire in the Clearing.
To get away from the bank, you cut across an open clearing between the eaves and the first great trees. It is a mistake: in the middle burns a small, badly hidden fire, and around it two orcs, one of the mountains and an archer, are warming their hands. One is binding his arm, where an arrow with grey feathers is still lodged. They see you. They rise without haste, and one kicks the embers towards you.

#### `nimrodel_3_3`

The Archer by the Pool.
You go down to a pebbled bank where a stream forms a still pool. The moon shines on the water. On the far side, among some rocks, something moves: a crouching orc archer, watching the water, who has seen you. His first arrow skips off the stones at your feet. You could retreat, but then he would keep shooting at your back. You cross the pool with icy water up to your knees, while he reaches for another arrow.

#### `nimrodel_4_0`

Whistles on High.
You stop in the middle of the path and listen. At first there is nothing. Then, far above, a soft whistle sounds, like a bird's, and another answers it from the other side. They are not birds. You stand still, your hands far from your sword, and let them look at you. After a while the whistles fall silent, and a golden leaf comes spinning down onto your shoulder. You do not know whether it is a warning or a permission. You choose to take it as a permission and go on, slowly.

#### `nimrodel_4_1`

The Great Tree.
You sit at the foot of the largest tree you have ever seen: its smooth grey trunk is so broad that five men could not encircle it. Among the roots lies a dry hollow, soft with leaves. You lean your back against the bark and feel something strange, like a slow, calm pulse, very deep. For the first time since you entered the mountain, fear lets go of you entirely. You sleep a little, without dreams. When you wake, the moon has moved and you feel lighter.

#### `nimrodel_4_2`

The Warning Arrow.
You cross the stream, leaping from stone to stone. The moment you set foot on the far bank, an arrow with grey feathers buries itself in the earth just before your boot, quivering. It is no orc arrow. You freeze. A clear voice says something from on high, in a tongue that sounds like water; you do not understand it, but the tone is firm. You raise your hands slowly, open, and take a step back. No one shoots again. Whatever you do from now on will have witnesses.

#### `nimrodel_4_3`

The Fallen Orc.
You follow the wounded deer's drops of blood through the ferns, but what you find is something else: a dead orc, lying face down beside a trunk, with two grey-feathered arrows in his back. He has not been dead long, and no one has taken his weapons. Whoever killed him did so from afar and in silence, and then left without a trace. This forest has guardians, and they do not like orcs. You hope they dislike Men a little less.

#### `nimrodel_4_4`

The Waystone.
You move away from the eaves into the heart of the forest. Here the trees are larger and the leaf-fall thicker. You find a ring of stones with cold ash: the fire of travellers who passed years ago. On one of the stones someone carved a rune you recognise from Rivendell, an Elvish sign of safe passage. You sit beside it a moment. It has been a long time since anyone left you a kindly sign upon the road.

#### `nimrodel_4_5`

The Column without Torches.
You hide among the tall ferns when you hear footsteps. Soon a column of orcs passes along the eaves, a few yards away, in haste and without torches. They do not go in among the great trees: they skirt them, glancing upward in fear. One lags behind and sniffs the air in your direction. You hold your breath. From on high comes a short whistle; the orc shudders, curses and runs after the others. You stay still a long while before moving.

#### `nimrodel_4_6`

The Waterfall.
You follow the stream upward along its bank to a small waterfall, where the water tumbles down steps of pale stone with a sound like a song. You sit on a rock to listen. You do not know why, but a lump rises in your throat: you think of Bree, of the road, of everything you have seen die since then. You let the waterfall sing for you a while. When you rise, the weight you carried out of the mountain is a little lighter.

#### `nimrodel_4_7`

The Band's Trail.
You follow the bank southward and come upon a trail that hides nothing: broken branches, churned mud, gnawed bones and an abandoned shield. A large band of orcs passed this way not long ago, skirting the forest without daring to enter it, as if seeking a door. By the tracks, they are not few. You decide to keep out of their path and move away from the trail towards the trees, sword to hand and ears alert.

#### `nimrodel_5_0`

Archers in the Mist.
A thick mist rises from the river and tangles among the trunks, and you advance through it almost blind, guided by the murmur of the water. Suddenly, four hunched shadows loom before you: orc archers, as lost as you in the mist, arrows already on the string. For an instant no one moves. Then one of them shrieks and all four loose at once. An arrow grazes your cloak. You hurl yourself at them before they can nock again.

#### `nimrodel_5_1`

A Platform on High.
You find a tree with low branches and climb, higher than you have ever climbed. Among the golden leaves you find something unexpected: a platform of pale wood, firmly lashed to the branches, empty. You lie down on it, wrapped in your cloak. From here the forest is a sea of gold beneath the moon. You fall asleep. When you open your eyes, you think you see grey shadows sitting in the branches above, watching you. You blink, and no one is there. You climb down carefully, wondering whether you dreamed it.

#### `nimrodel_5_2`

The Stragglers.
You cut along the eaves of the forest, where the trees are lower and you can move faster. There, among some bushes, two orcs are arguing in low voices: a big one from the mountains and a scrawny archer. They have fallen behind their band and dare not enter the forest alone. Seeing you, the argument ends: at last they have something to do with their fear. They circle you slowly, keeping you from reaching the great trees.

#### `nimrodel_5_3`

A Distant Song.
You stop, lean against a trunk and wait in silence, not quite knowing for what. After a while there comes, from very far away, a song. Clear voices, many of them, singing high in the trees in a tongue you do not understand. It is not a joyful song. It speaks of water, you think, and of something lost long ago. You listen to the end. When it falls silent, you find your cheeks wet and your heart, somehow, at peace.

#### `nimrodel_6_0`

The Waters of Nimrodel.
You reach the bank of the Nimrodel, which runs clear and swift over pale stones. You take off your boots and wade across. The water is icy and comes up to your knees, but it does not hurt: on the contrary, with every step you feel it carrying something away, the weariness, the dust of the mountain, the memory of the fire beneath the bridge. Reaching the far bank, you stand a moment, still, the water dripping from you. For weeks you have not breathed like this.

#### `nimrodel_6_1`

The Hidden Path.
Between two enormous roots you discover a narrow path, so clear of leaves that someone must tend it. You follow it. Every time you hesitate at a fork, you find a sign: a bent branch, a white stone, a leaf pinned to a piece of bark. You never see anyone, but you know you are being guided, slowly, eastward, towards the place where the rivers meet. You wonder whether it is an invitation or a trap. You decide to trust.

#### `nimrodel_7_0`

The Chief of the Archers.
You reach the place where the Nimrodel joins the river you have followed from the mountains, wider and colder. On the far side, the forest grows thicker and more golden. But before you can find a way across, an enormous orc bars your path, a black bow as tall as himself in his hand: the chief of the archers, who has left his band behind to hunt you down in person. You fight with your back to the river. High above, among the golden branches, figures clad in grey watch the struggle in silence, bows drawn and a rope coiled over one shoulder.

## lothlorien

#### `lothlorien_1_0`

Caras Galadhon.
For a couple of days the Elves lead you through the forest in silence, until you reach a green hill ringed by a wall and a fosse. Upon it rise the tallest trees you have ever seen, and among their boughs shine hundreds of lights, silver, green and gold: this is Caras Galadhon, the city of the trees. They give you food, clean clothes and a high bed among the leaves. That night you do not dream of fire. You think of Bree, of the wind on Weathertop, of the Ford, of the darkness beneath the mountain. Atop the hill a serene white light seems to watch over the whole wood, and you know that someone has seen you arrive.

#### `lothlorien_2_0`

The Houses in the Trees.
In the morning you climb a white stair that winds around a vast trunk. Above, among the branches, are platforms of pale wood, the talan, with walls of grey cloth that the wind stirs like sails. Here the Galadhrim live: they sleep, sing and work high above the ground, and not a single branch seems forced. An Elf child watches you with curiosity and laughs when you clutch at the rail. From on high, the forest is a sea of gold that stretches away eastward, as far as a distant glint of water.

#### `lothlorien_2_1`

The Ways of the City.
You walk the paths that cross the hill. They are paved with white stone and lined with lamps that no one seems to light or put out. The Elves greet you with a brief bow, asking nothing, and go on about their business. Somewhere high above you hear music, a harp and a voice, and the murmur of water beneath the moss. The green wall encircles the city like an embrace. For the first time since Rivendell your hand is not resting on your sword. You notice, and slowly draw it away.

#### `lothlorien_3_0`

The Hall of the Lady.
They lead you to the top of the greatest tree, to an oval hall walled in green and silver. At its far end, seated side by side, are the Lord and the Lady of the wood. They receive you only for a moment, courteously, amid weightier matters: they ask where you come from and by what roads, and thank you for your news. It is no long audience; messengers are waiting and voices murmur beside the pillars. You leave the hall feeling that you have stood very close to something ancient and immense, and have not wholly understood it.

#### `lothlorien_3_1`

The Songs of Mourning.
As evening falls, the whole forest sings. The songs are slow, in a tongue you do not understand, but sorrow needs no translation. An Elf guarding the stair tells you that they are lamenting someone who fell in the depths of the mountain, a grey wanderer many had known for centuries. You remember the fire beneath the bridge of Moria and the shuddering of the stone. You sit down on a root and listen until the last voice fades among the leaves.

#### `lothlorien_3_2`

The Guide of the Borders.
You seek out the Elf who guided you from the Nimrodel and find him fletching arrows at the foot of a mallorn. He greets you more warmly than he did in the forest, now that you are no longer an armed stranger. He tells you that he and his brothers watch the northern border and that for months the orcs have prowled the banks more than ever. He teaches you a few words of his tongue, to give thanks and to ask for passage. You repeat them until they no longer sound clumsy in your mouth.

#### `lothlorien_3_3`

The Fountain in the Garden.
You follow the sound of water to a spring that wells up near the top of the hill and runs down in a silver thread through a channel of stone. There is a silent garden there, with neither wall nor guard, and yet no one enters it without leave. You sit on the grass, at a distance. The water is so clear that you see the bottom as though there were no water at all. You drink a mouthful from the channel, and your head seems to clear, as after a long sleep.

#### `lothlorien_4_0`

The Mirror.
At nightfall an Elf maiden leads you to the top of some steps and points to a hollow in the garden where a silver basin stands upon a pedestal. She tells you it is the Mirror of the Lady, and that no one looks into it without her leave. You stay at the edge and do not go down. From there you see only still water, and the stars within it. Yet for an instant they seem to tremble and draw something else: a great river, two enormous figures of stone and, far to the east, a red glow. You blink, and they are stars again.

#### `lothlorien_4_1`

The Lord of the Wood.
The Lord of the wood receives you alone on a high terrace. He wants to know about your road: where you saw orcs, where the wolves are prowling, what is said in Bree of the lands to the south. He listens patiently and then speaks to you of the river that rises in the north and runs down to the sea, and of the lands that each bank guards. As you take your leave you understand that he did not question you out of curiosity. There are many borders to watch, and every traveller brings news.

#### `lothlorien_4_2`

The Dwarf and the Elf.
Beside the guest pavilion you see two figures pass whom you never expected to see together: a long-bearded Dwarf with an axe at his belt, and a tall, fair-haired Elf who laughs at something the Dwarf has said. They talk of trees and caves like old friends. You stare longer than you should. The Dwarf returns your gaze with a frown, then nods to you. You do not approach: they belong to another road.

#### `lothlorien_4_3`

The Weavers.
On a low talan, Elven women weave cloaks on looms of pale wood. The cloth is light as air, and its colour changes as you look at it: grey in the sun, green among the leaves, brown as wet earth. They let you run your hand over a half-finished length. It is warm and weighs nothing. One of them tells you that such cloaks make no one invisible, but they help enemy eyes slide past. You wish you had had one on Weathertop.

#### `lothlorien_4_4`

The Archers.
The guardians of the borders practise in a glade at the foot of the hill, and they let you try one of their bows. It is taller than you and hard to draw. Your first arrow is lost among the ferns; the third, at least, strikes the trunk. They do not laugh. One corrects the set of your feet and another teaches you to breathe before you loose. In the end you have not learned to shoot, but you have learned to look more calmly at what lies before you.

#### `lothlorien_4_5`

The Rope-makers.
In a workshop beneath the roots, two Elves braid grey rope from a fibre that gleams like silk. The rope is slender, scarcely thicker than your finger, and yet they assure you it would bear the weight of three men. They let you braid a span of it; yours comes out crooked and slack. One of them patiently unpicks it and begins again before your eyes, more slowly. You think that on the road ahead, some day, a rope like this might be worth more than a sword.

#### `lothlorien_4_6`

The Ovens.
The smell of bread leads you to some stone ovens hidden among the roots. The Elves who work there are making thin golden cakes, which they wrap in leaves. They give you a piece to taste. It is little more than a mouthful, yet after a while you feel as though you had eaten a whole meal. They tell you that this bread keeps for weeks on the road and is not given to just anyone. They say it without looking at you, as if it had nothing to do with you.

#### `lothlorien_4_7`

The Hill of Flowers.
Half a morning's walk to the west lies a small hill covered in evergreen grass and strewn with golden and white flowers. On its summit grow two rings of trees, and at their centre a mallorn holds up an ancient talan. The Elves seldom visit it and speak of it softly, as of a place where time does not pass. You lie down in the grass. For a moment you hear nothing, neither the wind nor your own blood.

#### `lothlorien_5_0`

A Night on the Talan.
That night you cannot sleep and go out to the edge of the talan. The forest is dark, but above, the stars seem nearer than anywhere else on your journey. Below, someone is singing very softly. You think of the road still ahead of you, whose end you no longer know, and for the first time not knowing does not frighten you. You wrap yourself in your blanket and sit there until the sky begins to turn grey.

#### `lothlorien_5_1`

The Lore of the Galadhrim.
A grey-haired Elf, one of the oldest in the city, agrees to speak with you. He tells you the history of the wood from before there were men in Bree, and speaks of the Enemy, who is rising again in his black land in the east, beyond the river and the mountains of ash. He says nothing to frighten you; he speaks as one describing winter. By the time you have finished listening, the sun has set and you have much to think about.

#### `lothlorien_5_2`

The Sentinels of the River.
A company of guardians returns from the bank of the great river and you sit with them by the fire. They bring ill news: on the eastern shore there are orcs who shoot at anything that moves on the water, and Wargs that come down to drink at dusk. Further south, they say, rapids close the river and can only be passed overland, with the boats on your shoulders. You listen in silence and ask a question now and then. Soon, you think, that will be your road.

#### `lothlorien_5_3`

The Maps of the River.
In a low chamber an Elf spreads before you a map of the great river drawn in silver ink. He shows you the woods along the banks, the islands, the rapids where no boat passes unless it is carried, and further down two great figures of stone on either side of the stream, and after them a lake and a roaring waterfall. Beyond that, the map turns dark and empty. You ask him what lies there. He shrugs and rolls up the parchment.

#### `lothlorien_6_0`

The Farewell Feast.
The Elves prepare a simple supper beneath the trees to bid you farewell, as though you were an old friend and not a lost traveller from Bree. There is bread, honey and a pale wine that tastes of summer. At the end, the Lady passes among the tables and pauses beside you for a moment. She tells you very little: that the southern road is full of shadow, and that the light you carry within you counts as well. Then she goes on her way among the lamps.

#### `lothlorien_6_1`

Rumours Downriver.
You ask after the guests of the grey pavilion, and a guardian tells you they have already gone. They left a few days ago, downriver, in three grey boats, wearing cloaks of the Galadhrim and bearing gifts from the Lady. They were heading south, though no one knows exactly where, and some say not even they knew. You look at the empty pavilion, its curtains drawn back. Once again you arrive one step behind, as in Bree, as in Rivendell.

#### `lothlorien_7_0`

The Departure by Boat.
At dawn they lead you down to the shore, where the silver stream joins the great river. There a grey boat awaits you, small and light. The Elves give you a cloak like the ones you saw being woven, bread wrapped in leaves and a coil of grey rope, and they teach you to paddle and to patch the hull. A guardian tells you that a few days ago three boats like yours set out from this very shore, downriver, to the south. Then they push yours off and the current takes you. You turn once: through the mist, the golden wood shines and recedes.

## anduin

#### `anduin_1_0`

The Great River.
The great river is broad and slow, a grey-green that mirrors the winter sky. The Elven boat answers the lightest stroke of the paddle, and soon you learn to let the current do almost all the work. On either side dark woods, reed-beds and muddy banks go by in turn. You see no one. You eat a mouthful of Elven bread and look south, where the river bends and disappears. You know that somewhere ahead of you, three grey boats are holding the same course.

#### `anduin_2_0`

The Islands.
The river opens into several arms between long, low islands covered in bare willows and rushes. You choose a narrow channel and paddle on between branches that brush the water. Here the current is gentler and the silence weighs heavily. A heron takes flight before you, and its harsh cry startles you more than any orc. On the shore of an islet you see old footprints in the mud, almost washed away by the rain.

#### `anduin_2_1`

Arrows from the East.
You draw close to the eastern bank to escape the strong current in midstream. It is a mistake. A black arrow whistles over your head and plunges into the water, then another, which sinks into the gunwale. Among the thickets of the bank two orcs with short bows show themselves, laughing. You cannot paddle faster than their arrows, so you drive the boat onto the shore, leap into the mud and climb towards them, sword in hand.

#### `anduin_3_0`

The Willow Bank.
You land on a bank of willows to stretch your legs and look for dry firewood. The ground is soft and smells of rot. Among the trunks you find the bones of a deer, picked clean and recent, and around them the prints of enormous paws. You turn slowly. Between the branches, yellow eyes are watching you, and then a Warg slinks out of the undergrowth, low and silent, its hackles raised. Between it and the boat there is only you.

#### `anduin_3_1`

The Shallows.
The river widens and loses depth, and you must climb out and drag the boat across a stony shallow. You are knee-deep in water when you hear the howling. Two Wargs come racing down the slope towards the bank, and behind them an orc with a bow shouts orders. You tie the boat's rope to a rock and climb out of the water to meet them before they can surround you in the shallows.

#### `anduin_3_2`

The Reed-bed.
You slip into a dead arm of the river, among reeds taller than you, looking for a quiet place to spend the night. The water is still and black. Suddenly an arrow slices through the stems beside your face. An orc archer has followed you along the bank, crouching among the reeds, and is drawing his bow again. You leap onto firm ground and run at him before he has time to take aim once more.

#### `anduin_3_3`

The Pack on the Shore.
You make camp on a shingle beach beneath a bank, without a fire, wrapped in your grey cloak. At midnight you are woken by the sound of rolling stones. On top of the bank, shadows are moving, four of them, one after another, and the wind brings you a smell of wet dog and carrion. A pack of Wargs has caught your scent. There is no time to push the boat into the water. You get to your feet with your back to the river.

#### `anduin_4_0`

A Camp by the Water.
In an inlet on the western bank you discover the remains of a camp. There is cold ash beneath a layer of leaves, three furrows in the sand where someone beached three boats, and the prints of small, bare feet beside others made by boots. Among the stones you find a folded leaf, green and silver, like those that wrapped the Elves' bread. There is no blood and no sign of a struggle. Whoever slept here left in peace, downriver.

#### `anduin_4_1`

The Brown Lands.
The eastern bank becomes flat and bare, a brown land of dry grass and burnt scrub stretching away out of sight. No tree grows, no bird sings. Here and there you see old ashes and heaps of bones. You paddle as close as you can to the opposite shore. At dusk a column of smoke rises very far off, to the east, and then dissolves. Someone lives in that wasteland, and you do not wish to know who.

#### `anduin_4_2`

Fires on the Far Shore.
That night, moored to a branch, you watch fires kindle on the eastern bank one after another, until they form a row of red eyes along the water. You hear hoarse singing and the clash of metal. You cannot stay so close. You cover yourself with the grey cloak, slip the mooring and let yourself drift down the middle of the river without touching the paddle. You pass the fires so slowly that your chest aches from holding your breath. No one cries out. At dawn the river is empty again.

#### `anduin_4_3`

The Dawn Mist.
Dawn comes with a mist so thick that you cannot see the prow of your own boat. The river is only a murmur and the dip of the paddle. You go on blindly, guided by the sound of the bank, and at one point you hear harsh voices very close, in a tongue you know from Moria. You stop paddling and hold your breath. The voices fade away. When the mist lifts, the river is empty and a pale sun shines on the water.

#### `anduin_4_4`

The Islet of Rushes.
You find a small islet in the middle of the river, ringed with rushes, and decide to rest there a whole day. No one can approach without your hearing it. You dry your clothes in the pale sun, mend your old cloak and hone your sword's edge on your whetstone. You eat little, for the Elven bread goes a long way. In the afternoon a kingfisher alights on the prow of the boat. You watch it without moving until it flies away.

#### `anduin_4_5`

Orc Tracks.
On the eastern bank the mud holds a fresh trail: hobnailed boots, many of them, and among them the marks of great paws. You follow it on foot for a stretch. The trail runs parallel to the river, southward, as if those who left it were following something that travels by water. You find a black rag caught on a bramble and a broken arrow. You hurry back to the boat. Whatever they are hunting is ahead of you.

#### `anduin_4_6`

The Silent Wood.
On the western bank the forest comes down to the water, old and dark, its trunks clad in moss. You land to fetch water from a stream and go a few paces in. Everything is silent, but it is not a hostile silence: it is that of a place no one has entered for a long time. You drink, fill your waterskin and stay a while, listening. For a moment you remember the Golden Wood, and it seems very far away.

#### `anduin_4_7`

The Rain.
A cold rain begins to fall at midday and does not stop all day. The river turns grey and swollen, and the boat fills with water faster than you can bail it. You take shelter beneath an overhanging rock on the bank, with the boat upturned above you. The Elven cloak keeps you drier than you expected. You listen to the rain drumming on the hull, and fall asleep thinking of the inn at Bree.

#### `anduin_5_0`

Ambush at the Bend.
The river turns in a sharp bend beneath a cliff on the eastern bank, and the current drives you towards it with no escape. By the time you see it, it is too late: two orcs with bows wait above, and on the beach at the foot of the cliff two Wargs strain at their chains. The arrows force you ashore. You leap onto the sand shielding your head with your arm. The orcs slip the chains.

#### `anduin_5_1`

Stars upon the Water.
You decide to travel by night, when the eyes on the shore see less. The sky is clear and the stars are mirrored in the river, so that you seem to float between two skies. You barely touch the paddle. The current carries you slowly and silently, and only now and then do you hear a splash or the cry of a night bird. By dawn you have covered many leagues, and no one has seen you pass.

#### `anduin_5_2`

Wargs in the Water.
The river narrows between an islet and the bank, and the current carries you closer to land than you would like. On the shore two Wargs were waiting for you. They plunge into the water without hesitation and swim towards the boat, ears flattened and eyes fixed on you. One manages to set its paws on the gunwale and almost overturns you. You beat it off with the paddle, reach a gravel bar and leap onto it, sword in hand, as the two of them climb out of the water, dripping.

#### `anduin_5_3`

Signs on the Shore.
On a smooth rock on the western bank someone has left a sign: three small stones, one atop another, and an arrow drawn in charcoal pointing south. It is not the work of orcs. Further on, on a tree, you see the faint mark of a rope someone tied to moor a boat. You do not know whether the signs were meant for you or for others coming behind, but they give you heart. Someone has passed this way, and gone on.

#### `anduin_6_0`

The Stone Hills.
After many days on the river, the land begins to rise on either side. The woods thin out and grey hills of bare rock appear, ever higher and steeper. The river narrows and the current gathers strength; the boat now runs on its own, and you must use the paddle to keep it clear of the rocks. The air is colder. Ahead, between the hills, the water turns white in places.

#### `anduin_6_1`

A Distant Roar.
One morning, as you wake, you hear a new sound: a dull, unceasing roar that comes from the south and is nothing like the wind. You press your ear to the rock and feel it there too, like a heartbeat. Rapids, many and strong, and still far off. On the eastern bank there are more fires than ever. You understand that the river is about to close before you, and that whoever wishes to pass that way will have to go between enemies.

#### `anduin_7_0`

The Chieftain of the Raiders.
In a narrow pass just above the rapids, the river runs between two crags. Suddenly the boat strikes something: a chain stretched beneath the water. On the eastern crag a huge orc with a longbow and a scimitar at his belt rises, laughing. He is the chieftain of the river raiders, who lives on whatever the water brings him. He is alone on the crag and needs no one else: an arrow is already on the string. You leap onto the nearest rock and climb towards him, the boat caught fast.

## argonath

#### `argonath_1_0`

The Roar of Sarn Gebir.
The murmur has become a roar. Ahead, the river breaks into a chaos of rocks and white foam that no boat could pass through whole: these are the rapids of Sarn Gebir. You paddle with all your strength towards the western bank and beach the boat on a stony strand, your arms trembling. The hills rise on either side, grey and bare. You will have to carry the boat on your shoulders until the river grows calm again.

#### `argonath_2_0`

Wargs on the Portage.
You look for the old portage-way the Elves described to you and you find it: a path that climbs along the western bank, away from the water. You are not the only one who knows it. Halfway along, two Wargs are waiting for you, sitting in the middle of the path like hounds guarding a gate. They rise when they see you, unhurried. You set the boat down on the ground, carefully, and draw your sword.

#### `argonath_2_1`

The Stony Shore.
You cannot find the portage-way, so you skirt the rapids along the bank itself, carrying the boat and setting it down at every leap between the rocks. The spray drenches your face. On a flat stone you see an arrow lodged in a crack, black and dark-feathered, shot from the far shore. Beyond it there are others, dozens, scattered among the rocks and buried in the sand. You wonder whom so many arrows were seeking, and whether any found its mark.

#### `argonath_3_0`

The Bearers' Path.
You hoist the boat onto your shoulders. It is light, but the way is long and steep, and soon your legs are burning. The path is ancient: there are steps hewn in the rock, worn by centuries of feet. In some stretches you see fresh marks, scrapes on the stone such as the keel of a boat would leave when dragged, and prints of boots and of small feet. You reach a level place and lie down, the boat beside you.

#### `argonath_3_1`

Remains of a Fight.
From the top of the path you look towards the eastern bank. Among the rocks on the far side you see dead fires and broken bows, abandoned in haste. On your bank, by contrast, there are black arrows driven into the sand and the tree trunks, all shot from over there. Someone crossed the river by night under that rain of arrows and reached this side. You see no blood on the stones and no wreckage of boats. Whoever passed here did so swiftly and well, and was gone before dawn.

#### `argonath_3_2`

The Uruk Scout.
You leave the boat for a moment at the foot of the slope and climb the high rocks to see the way from above, and you come face to face with an orc unlike those of Moria. He is big, dark-skinned and long-legged, and the daylight does not trouble him. On his shield is painted a white hand. He looks at you with contempt, as at an insect, and draws a broad, short sword. There is nowhere to run.

#### `argonath_3_3`

The White Hand.
You follow a trail of boots that crosses the portage from west to east and run into three Uruk-hai coming down towards the river. They wear helms with a white rune and shields with a painted hand. They do not come from Mordor, you think: they come from the west, from someone who is not the Eye but who is also seeking something. One points at you and growls a word. The other two spread out to surround you.

#### `argonath_4_0`

The Wounded Boat.
As you set the boat down you see that a rock has opened a crack in its side. You sit down to mend it with what you have: resin from a pine, a strip of your old cloak and a span of Elven rope. You work slowly, as you were taught on the shore of the Golden Wood. When you finish, the crack can barely be seen. The boat is not yours, you think, it has only been lent to you, and you want to return it to the river whole.

#### `argonath_4_1`

The Ancient Road.
For a stretch the path becomes a road of flagstones, broken and crusted with lichen. Along its sides are the remains of walls and a fallen pillar bearing a worn emblem: a tree, perhaps, and some stars. Men of an ancient kingdom built this road when the river was theirs. Now only orcs and lost travellers pass. You run your hand over the cold stone and go on, the boat on your back.

#### `argonath_4_2`

The Winged Shadow.
A sudden chill stops you dead. The sun goes dark as if a cloud had covered it, but there are no clouds. Very high above the eastern bank, a shadow with enormous wings crosses the sky towards the south. You throw yourself beneath a rock and lie still, your heart pounding. You know that cold: you felt it on Weathertop, and at the Ford. The shadow passes away. A long while goes by before you dare come out.

#### `argonath_4_3`

The Pool.
You go down to a quiet pool beside the rapids to fill your waterskin. As you bend over, you see in the water the reflection of something behind you. A lone Warg, bigger than the others, with a scar across its muzzle, has come down without a sound. It does not attack at once. It takes your measure, as if it had hunted men before. You straighten slowly, water dripping from your hand.

#### `argonath_4_4`

The Uruk Camp.
You follow a thread of smoke to a hollow among the rocks. There is a camp: a badly smothered fire, gnawed bones and piled weapons, and four Uruk-hai arguing loudly in a harsh tongue. One hurls a shield bearing the white hand against a stone. You crouch down, but the wind shifts, and the nearest one lifts his head, sniffs and looks straight at you.

#### `argonath_4_5`

The Abandoned Shield.
On the hillside you find a black shield abandoned among the stones, with a white hand painted at its centre. Beside it lies a helm bearing a rune shaped like a lightning bolt, also white. You leave them where they are. No orc of the mountains bears such tokens. You remember what was said in Hollin about the crows and the wizard's tower in the south. Someone else besides the Eye has sent his servants to this river.

#### `argonath_4_6`

The Gorge.
The path enters a narrow gorge, its walls of rock closing above your head. The thunder of the river comes multiplied by the echoes, and you must shout to hear yourself. You carry the boat sideways so that it fits. At one point the light almost vanishes. You advance inch by inch, your elbows grazing the stone, until at last you see daylight on the other side.

#### `argonath_4_7`

The Backwater.
At the foot of the last rapids the river forms a wide, dark backwater, where the foam turns slowly before moving on. You lower the boat carefully into the water and let it float. It holds. You test it, paddling in circles near the bank, until you trust it again. Your arms ache from carrying it, but feeling the current beneath the hull once more is like breathing again.

#### `argonath_5_0`

The End of the Rapids.
The rapids lie behind you. The river is one again, deep and swift, hemmed in between ever higher hills. You return to the boat and let yourself be carried, watchful for rocks. The roar of Sarn Gebir dies away at your back, and in its place you hear the wind whistling among the stones. You eat a mouthful of Elven bread and count the days. You have already lost count, and that troubles you more than the orcs.

#### `argonath_5_1`

The Cliffs.
The hills become sheer cliffs that box in the river. The water runs dark and deep between walls that hide the sky. Above, far above, you see black birds pass that make no sound. There are hardly any shores to land on: if something attacks you here, almost all you can do is paddle. You press on, close to one of the walls, and the evening light paints the rock red.

#### `argonath_5_2`

Horns on the Hillside.
From high on a hillside you hear, far off, the sound of a hoarse and grating horn. Another answers further south, and then a third, to the west. These are not hunting calls: they are patrols calling to one another, many of them, spread across the hills. The servants of the White Hand are combing this land in search of something, and you are in the middle. You stay motionless, trying to guess where they come from. The wind carries the sound away and it does not return.

#### `argonath_5_3`

Night in the Cleft.
You find a cleft in the rock, deep enough to squeeze into with the boat, and decide to spend the night there. You light no fire. From inside you hear patrols pass along the hillside: heavy footsteps, growls, the scrape of a spear against stone. You wrap yourself in the grey cloak and keep still. No one looks in. At dawn you come out stiff, but whole, and go on southward.

#### `argonath_6_0`

Figures in the Mist.
The river widens a little and a low mist covers the water. Far off, downstream, two enormous shapes rise on either side of the channel, taller than any tower you have ever seen. At first you take them for mountains, but they have shoulders, and crowned heads. You remember the Elves' map. You feel fear, and something close to reverence. You paddle towards them slowly.

#### `argonath_6_1`

The Patrol on the Shore.
Where the western bank opens into a strip of gravel, you go ashore and continue on foot, towing the boat by its rope. A patrol bars your way: two Uruk-hai with the white hand on their shields and two Wargs straining at their leashes. The Uruks grin when they see you are alone. One lets his Warg loose and shouts something at it. There is no time to get into the boat. You tie the rope to a root and plant yourself in the middle of the path.

#### `argonath_7_0`

The Kings of Stone.
The two figures leave no doubt now: they are kings of stone, carved from the living rock, their left hands raised towards the north as if commanding a halt. The current sweeps you towards the gate they form. But at their feet, on a ledge of rock above the water, an Uruk bigger than the rest awaits you, his helm painted white: the captain of the White Hand, alone, his blade bare. You beach the boat as best you can and climb onto the ledge to face him.

## amonhen

#### `amonhen_1_0`

The Lake of the Three Hills.
Beyond the kings of stone, the river opens into a long, still lake shut in between hills. In its midst rises a tall, sheer island, and at the far end thunders a roar that never ceases: a great waterfall you cannot yet see. On the western shore, at the foot of a wooded hill, a green lawn runs down to the water. You beach the boat there and leave it on the grass. The Elves called that hill Amon Hen, the Hill of the Eye. No bird sings. From the west, far away, come the hoarse calls of horns, ever fainter, moving off.

#### `amonhen_2_0`

The Crows of the Lawn.
You cross the lawn towards the trees. The grass is trampled in broad swathes, as though a crowd had run over it very recently, and there are dark stains on the green. At the edge of the wood a flock of black crows bursts up from the ferns. Most fly off westward, cawing, but two circle over your head and dive at you, talons first. They are crebain, spying birds that do not behave like crows.

#### `amonhen_2_1`

The Lakeshore.
You would rather not enter the wood yet, and follow the shore southward along a strip of pebbles. The water of the lake is so still that it mirrors the hills upside down. You find the remains of an old wall, covered in moss, and a little further on some stone steps climbing among the trees towards the summit. To the south the thunder of the falls grows, and a column of white spray rises above the water. You sit for a moment on a stone. Everything is far too quiet.

#### `amonhen_3_0`

Beneath the Trees.
Beneath the trees the ground is churned up, littered with broken branches and black arrows lodged in the trunks. Among the ferns lie dead orcs, many of them, of two different kinds. Some are large, with a white hand painted on their shields. Others, smaller, bear a red eye on their helms. Two masters, you think, and neither of them a friend. You do not know who fought them here, or whether anyone survived. You walk slowly, touching nothing, your hand on the hilt.

#### `amonhen_3_1`

The Muddied Stream.
A stream runs down from the hill to the lake, and you want to fill your waterskin. But the water comes down cloudy, stirred up by hundreds of feet that crossed it higher up. In the mud of the bank there are prints of iron-shod boots, so many that they tread on one another, and all go the same way: westward, away from the lake. Some are very deep, as of someone bearing a load. You wait for the water to clear and drink, gazing to where the trail disappears.

#### `amonhen_3_2`

The Ancient Stair.
You climb the ancient stair. The steps are broad and worn, hewn centuries ago so that kings might ascend to the summit without weariness. Roots and moss grow between the stones. As you rise, the wood thins and the lake appears below, ever smaller, with the island in its midst like the prow of a ship of stone. You stop to catch your breath on a landing. Above, against the sky, you see something that looks like a carved seat.

#### `amonhen_3_3`

The Path to the Falls.
You follow the shore towards the thunder. The path narrows between the water and a wall of rock, and the air fills with a fine drizzle. Rounding a crag you meet two Uruk-hai returning from the shore with wet boots. On the shoulder of one perches a black crow, which caws and points at you with its beak. Without a word the Uruk-hai part to close the path on both sides, and the crow takes wing.

#### `amonhen_4_0`

The Spring in the Wood.
Far from the dead, in a hollow of the wood, you find a spring that wells from a stone and falls into a carved basin strewn with leaves. The water is cold and clean. You wash your face and hands, then sit with your back against the stone to eat a piece of Elven bread. For the first time all day you hear a bird. You close your eyes for a moment. When you open them, the light has changed and the afternoon is wearing on.

#### `amonhen_4_1`

Dust in the West.
You climb to a clearing from which the west can be seen. Beyond the hill the land falls away to vast grasslands, green and brown, vanishing into the horizon. Very far off, a dark smudge moves across them, raising a cloud of dust. It is a large band, running without rest. You cannot make out who they are, but you know where they come from: from this wood, from among these dead. You follow it with your eyes until the smudge melts into the land.

#### `amonhen_4_2`

Three More Trails.
You follow the orcs' trail for a stretch, as far as the end of the hill. There you see something you had not seen before: on top of the iron-shod prints, fresher, are those of only three walkers. Two were made by long, light boots; the third, shorter and heavier, sinks its heel into the mud. They are running after the orcs, and by the length of their strides, running hard. You would never catch them. You stand looking at the trail, and then turn back towards the lake.

#### `amonhen_4_3`

The Straggler.
You skirt a thicket of holly and almost stumble over him. He is an Uruk-hai sitting against a trunk, one leg bound with filthy rags. He has been left behind, alone, and he knows it. He is whetting a short sword on a stone, and when he sees you he is not afraid: he grins, sets down the stone and rises, leaning on the tree. He limps, but the sword does not waver in his hand. He has no intention of letting you go and tell anyone you saw him.

#### `amonhen_4_4`

The Seat of Stone.
On the summit there is a platform of flagstones and, upon four pillars, a seat of stone reached by a stair. You dare not sit in it; it is not for you. You stay beside it, your hand on the cold stone. Even so, from here you can see very far. To the east, beyond the river, lie grey and broken hills; behind them, a plain that gleams like dirty water; and beyond that, a line of dark mountains beneath a cloud that does not move.

#### `amonhen_4_5`

The Watchers on the Slope.
You leave the stair to explore the upper slope, among rocks and twisted pines. You reach an outcrop and come upon four Uruk-hai who have climbed up there to watch the lake. They point at the shores, argue, and one spits towards the water. They were searching for something from on high and have not found it. Your arrival strikes them as a consolation prize. They spread out around you on the rock, and at your back there is only the drop.

#### `amonhen_4_6`

The Brink of the Falls.
You reach the brink of the falls. Here the lake ends abruptly and the whole river plunges over a step of rock into an abyss of foam so deep you cannot see the bottom. The noise fills your chest and drives out all thought. Above the spray a bow of colours forms. Further down, the river runs on southward, wide and slow, between low lands. No boat could pass here whole. You stand gazing a long while, drenched and unable to look away.

#### `amonhen_4_7`

The Ancient Landing.
Among the rushes you discover a landing-stage of stone, so old that the waves have rounded its edges. It has iron rings eaten by rust and steps that go down into the water. Someone moored their boats here when these hills had masters. You sit on the lowest step, your feet close to the water, and gaze at the island in the middle of the lake, unreachable, crowned with birds that circle without ever settling.

#### `amonhen_5_0`

A Night without Fire.
Night overtakes you far from the lawn, and you spend it beneath an overhanging rock, wrapped in the grey cloak. You light no fire. The cold creeps down from the hill and the thunder of the falls is never silent. At midnight you wake and see, far to the east, a red glow beneath the clouds, waxing and waning like an ember someone is blowing on. You do not sleep again. When dawn at last comes, the glow has faded and the sky is grey.

#### `amonhen_5_1`

Mist in the Wood.
At dawn a white mist rises from the lake and creeps into the wood. The trunks appear and vanish around you like figures drawing near and moving away. You walk slowly, counting your steps, guided by the murmur of the falls. At one point you hear a flutter of wings very close, and a caw, and then nothing. You stand still until the silence returns. When the mist tears open, you are on the hillside, nearer the water than you thought.

#### `amonhen_5_2`

The Ruined Watchtower.
Among the trees of the slope you find the ruins of a watchtower: a ring of dressed stones and a pillar still standing, with a winged helm carved at its top. The men who built it watched the river and the eastern border from here. Now nettles grow where their sentinels slept. You lean against the pillar and look east, as they must have looked. The land on the far side is still grey and hostile. No one watches it any more.

#### `amonhen_5_3`

The Deer Path.
You find a narrow path, worn by deer, that zigzags down the slope among ferns and stones. You follow it because it is the only one no iron-shod boots have trodden. After a while you see the deer: three hinds and a young stag, motionless among the trees, watching you. They do not flee. You pass beside them slowly, hardly breathing, and they follow you with their eyes until the path turns. It is the only living, peaceful thing you have seen on this hill.

#### `amonhen_6_0`

The Great Crow.
You go down through the wood towards the lawn. For some time a crow has been following you, larger than the rest, hopping from branch to branch without a sound. When you stop, it stops. When you walk on, it flies ahead and waits for you. At last, in a clearing, it alights on a low branch, so close you can see its eyes. You know that if you let it fly, it will carry your face to someone. Before you can raise your sword, it hurls itself at your eyes.

#### `amonhen_6_1`

Looters among the Stones.
You go down a scree slope that opens among the trees, without cover, in plain sight. At its foot two Uruk-hai are rummaging among the things their dead left behind: belts, knives, a waterskin. Above them, two black crows wait for their share. The birds see you first. They caw, and the Uruk-hai lift their heads together. One flings away what he holds and draws his blade; the other whistles, and the crows swoop down on you.

#### `amonhen_7_0`

The Warlord of the Shore.
You return to the lawn as evening falls. Your boat is where you left it. But a little further on there is a furrow in the sand where someone pushed another boat into the water with its prow towards the far shore, and small, bare footprints that walk into the lake. Bent over them is the largest Uruk you have ever seen, with a horned helm and a broad sword, sniffing the trail like a hound. His band is already marching west with other prey; he alone has stayed behind. The warlord looks at you and laughs.

## emynmuil

#### `emynmuil_1_0`

The Far Shore.
That night you cross the lake in your boat, towards the far shore, the way the owners of those small footprints went. You paddle slowly, the falls roaring on your right. At dawn you beach on a stony strand beneath a bare hill and hide the boat among the rocks. You will not come back for it: where you are going there is no river. Before you rise the hills of the Emyn Muil, a maze of grey rock cut by ravines and clefts, where the wind whistles and almost nothing grows.

#### `emynmuil_2_0`

The Blind Ravine.
You enter the first ravine that opens to the east. The walls are of grey rock, smooth and cold, and the floor is full of loose stones that roll beneath your boots. After an hour the ravine ends in a wall. You turn back, choose another, and that one ends too. By mid-afternoon you understand that these hills cannot be crossed by walking straight: they must be read like a riddle. You sit down, take a drink and look at the clefts with more patience.

#### `emynmuil_2_1`

The Windy Ridge.
You decide to climb before you lose yourself, and scramble up a ridge of rock to the top. There the wind strikes you sideways, cutting, and you must crouch to keep your balance. From above you see the whole maze: parallel ridges, crossing ravines, wells of shadow where the sun never reaches. Everything is grey. With your eyes you choose a line that seems to run eastward, and you commit it to memory stone by stone before climbing down.

#### `emynmuil_3_0`

Eyes in the Wall.
You follow a deep cleft that seems to run east. It is so narrow that the sky is only a white ribbon above your head. Suddenly something moves in the wall, at the height of your eyes: there is a ledge, and on it a wolf with grey fur, the same colour as the stone, so still that you had passed beside it without seeing it. Now it bares its teeth. It springs from the ledge without a howl, straight for your throat.

#### `emynmuil_3_1`

The Orc Path.
Among the stones you find a beaten path, the first you have seen in these hills. There are prints of heavy boots and, on a rock, someone has painted in something red a lidless eye. You follow it warily. At a bend you come face to face with two orcs climbing up. They are different from the Uruk-hai: shorter, long-armed, with black shields and that same red eye. For an instant they stand still, surprised. Then they shriek and charge.

#### `emynmuil_3_2`

The Horse-head Rock.
You walk along the heights following the line you memorised. For hours all goes well. But at dusk you recognise a split rock shaped like a horse's head, the very one you passed at midday. You have made an enormous circle and come back to the start. You stand staring at it, furious. The wind drives the cold into your bones. This time you mark the rock with the point of your sword, and promise yourself you will never see it again.

#### `emynmuil_3_3`

The Hollow of Bones.
You go down into a hollow sheltered from the wind. The ground is strewn with goat bones, picked clean, and there are the remains of a fire. Beside it a Mordor orc gnaws a bone, and at his feet two rock wolves sleep, curled up like dogs. One of them opens its eyes. It makes no sound: it rises, wakes the other with its muzzle, and both stare at you. The orc sees where they are looking, spits out the bone and grabs his spear.

#### `emynmuil_4_0`

The Leap.
The cleft you are following suddenly opens into another, at right angles, that cuts across the way from side to side. It is too wide to cross in one stride and too deep to see the bottom: you hear only, far below, the dripping of water. You take off your pack, throw it to the other side and step back for a run-up. You leap. Your boots reach the edge, slip, and you cling to the stone with your nails. You haul yourself up. You lie there trembling, laughing without knowing why.

#### `emynmuil_4_1`

A Shadow beneath the Moon.
You sleep beneath a slanting slab that shields you from the wind. You are woken by a sound of falling pebbles. The moon has risen, and for an instant, atop a rock, you see outlined against it a thin, hunched shadow gazing eastward. You blink and it is gone. You do not know whether it was a beast, an orc or a dream. You do not close your eyes again until dawn, your sword across your knees and your ears straining at every stone.

#### `emynmuil_4_2`

Red Signs.
The orc path runs on eastward, and every so often, on a rock, the red eye appears again, painted in haste. Beside one of them is a cairn of stones with a spear thrust into it and a black rag tied on. Further on, the path splits in three, and each branch bears its sign. You understand that these hills are watched by folk who know the way. You decide to keep off the paths, though it costs you twice the effort.

#### `emynmuil_4_3`

Tracks in the Frost.
You hide among the rocks to let a patrol go by, and it does: four orcs going down at a brisk pace, without speaking. You breathe again. But one stops, turns back and crouches over the frost that covers the path. Your footprints are there, marked in white, clear as a signature. The orc sniffs, growls, and the other three turn round. Before they find you, you go out to meet them in the narrow pass, where they can only come two at a time.

#### `emynmuil_4_4`

The Frozen Night.
The night on the heights is the coldest you can remember. There is no firewood in these hills, not so much as a scrubby bush to burn. You wrap yourself in the grey cloak and huddle between two stones, your knees against your chest. The cloak is warmer than it looks, but the cold rises from the rock. You rub your hands, count the stars, softly repeat the songs of the inn. At dawn there is frost on your eyebrows, but you are still alive.

#### `emynmuil_4_5`

The Sheer Drop.
The way you chose ends at a cliff of rock, as high as ten men, with no other way down for a long distance. You take out the grey rope of the Elves. You loop it double around a spur of stone, so that you can recover it from below, and climb down backwards, bracing your boots against the wall. The rope is as thin as a cord and yet it does not give a span. At the bottom you pull one end and the whole rope falls at your feet, coiled like a grey serpent.

#### `emynmuil_4_6`

The Water in the Stone.
You have gone a whole day without finding water and your waterskin is nearly empty. Your tongue sticks to the roof of your mouth. At last, in a shaded crevice, you come upon a natural basin in the rock, full of rainwater. It is cold and tastes of stone, but it seems better to you than the ale of the inn. You drink slowly, fill your waterskin to the brim and stay there a while, watching the water grow still again.

#### `emynmuil_4_7`

The Rockslide.
You cross a scree of loose rocks at the foot of a crag. Halfway over you hear a crack above, then a swelling roar: the hillside has given way. You run diagonally, leaping from stone to stone, while behind you boulders the size of houses come rolling down. One passes so close you feel its wind. When silence falls, you are on the other side, covered in grey dust, and the way you came no longer exists.

#### `emynmuil_5_0`

Haze among the Rocks.
Dawn comes with a low haze that fills the ravines like milk. Only the ridges show, like islands in a white sea. You walk along them, from one to the next, while below the mist moves slowly. It is beautiful and treacherous: one false step and you would fall into the whiteness without knowing how far. At midday the sun burns it away, and you find you have gone further than in the whole of the day before. For the first time, to the east, the hills seem to fall away.

#### `emynmuil_5_1`

The Stone Arch.
The wind has carved in the rock an enormous arch, so high that a tower would fit beneath it. You pass under it and stop in the middle, neck craned, gazing upward. The stone is grained like wood, in grey and ochre layers. In the shadow of the arch lies a smooth stone with ancient markings: lines, circles, perhaps a map. Someone passed this way long ago and wished to leave a record. Beside it you carve a small B, for Bree.

#### `emynmuil_5_2`

The Pack of the Plateau.
You come out onto a bare plateau without a rock to take cover behind. Halfway across you hear the first howl, short, on your left. Another answers on your right, and another behind. Four rock wolves emerge from the clefts at the edge and fan out, unhurried, never taking their eyes off you. They hunt as wolves hunt: in silence, closing the circle step by step. You take your stand in the centre, turn slowly and wait for the first.

#### `emynmuil_5_3`

The Watcher of the Eye.
You skirt a tall crag and see, at its very top, a heap of dry firewood and an orc sitting beside it. He is a lookout, posted there to light a signal if he sees anything. He has not seen you yet. You climb up from behind, clutching at every crack, holding your breath. You have one stretch left when a stone comes loose beneath your boot. The orc turns and seizes a torch; then, seeing you so close, he trades the torch for his scimitar.

#### `emynmuil_6_0`

The Edge of the Hills.
At last you reach the eastern edge. The hills end in a long, broken cliff, and at its feet stretches a grey and brown plain that gleams here and there like a dirty mirror: stagnant water, reeds, mist. Beyond it, on the horizon, rises a wall of black mountains. The wind brings a smell of rot. You search for hours for a way down, and at last you find a broad ravine that descends, stone by stone, towards the plain.

#### `emynmuil_6_1`

Enormous Footprints.
You go down a dry watercourse that descends eastward. In the sand of its bed there are footprints: enormous, of bare feet longer than your arm, with thick toes and nails that scratch the rock. There are bones too, smashed by blows, not by teeth, and a stone with dark stains. Whatever left those prints passed here not long ago and is far bigger than you. You go on, because there is no other way, but now you watch every shadow.

#### `emynmuil_7_0`

The Hill-troll.
The descent opens onto a level stretch at the foot of the hills, from which the marshes can already be seen. There, sitting on a rock, waits something that freezes your blood: a Hill-troll, grey as stone, with a club made from a whole tree trunk. There is no one else on the flat: only the creature, turning its head towards you and sniffing the air. The troll rises slowly, taller than two men, and the whole level ground seems to shrink.

## cienagas

#### `cienagas_1_0`

A Land of Dead Water.
You leave the last rocks behind and the ground turns soft beneath your boots. Before you stretches the marsh: pools of brown water, clumps of rotten reeds, mounds of mud that quiver when you tread on them. A sweet, rancid smell fills everything. Mist crawls low over the water, and it is cold, a damp cold that does not leave with the sun. No bird flies here. To the east, behind the haze, you can make out the dark wall of the mountains.

#### `cienagas_2_0`

The First Light.
You follow a shore of reeds as evening falls. Ahead, over the water, a pale light kindles, greenish, like the flame of a candle that no one holds. It stays still for a moment. Then it draws near, floating, and when it reaches you it swells and stretches, and you feel on your face an icy breath instead of warmth. It is a flame that burns with cold. When your sword passes through it, it sputters, shrinks and grows again.

#### `cienagas_2_1`

The Black Pools.
You go forward leaping from one tussock of grass to the next, for between them the water is black and no bottom can be seen. As you land on a large tussock, the water of the pools around it begins to tremble. Two shapes rise slowly, made of mist and foul water, with something like arms. They have no faces, only two dark hollows where the eyes should be. They smell of the bottom of a well. They lean towards you as if they wished to embrace you.

#### `cienagas_3_0`

Faces beneath the Water.
You stop beside a pool of still water, so clear that it is startling in this place. At first you see only mud. Then, beneath the surface, you see a face: pale, beneath an ancient helm, the eyes open as if dreaming. And another behind it, and another, men of other ages and creatures you do not recognise, lying on the bottom as though they had been left there after some very ancient battle. You lean no further. You draw away slowly, without turning your back on the water.

#### `cienagas_3_1`

The Sounding Staff.
The mist grows so thick that you can no longer see your feet. You cut a staff from a dry bush and sound the ground before each step. Sometimes the staff sinks without finding bottom, and you must seek another way. You go on like this for hours, in a white and mute world, not knowing whether you are heading east or walking in circles. Only the cold tells you that you are awake. When the mist thins a little, you see the staff is caked with mud halfway up.

#### `cienagas_3_2`

The Dry Islet.
You find an islet of firm ground, scarcely bigger than a room, with a twisted bush and a few stones. It is the first dry ground all day. You take off your boots, wring out your stockings and eat a piece of Elven bread, slowly, watching the water all around. The leaves that wrap it still smell of the Golden Wood. For a moment the stench of the marsh vanishes, and you remember the silver trees and their lamps.

#### `cienagas_3_3`

The Sunken Road.
Among the tussocks you discover a line of flat stones, half sunk, that cross the marsh in a straight line. It is what remains of a road, made by people who knew how to build, so long ago that the marsh has been swallowing it ever since. You follow it, leaping from flagstone to flagstone. Some shift beneath your weight. On one you see carved a circle with rays, like a sun. You wonder where this road led when there was no water here.

#### `cienagas_4_0`

What Juts from the Mud.
From a mound of mud something metallic juts out. You dig it up with the point of your sword: it is a rusted helm, tall and narrow, with the remains of a plume that has lost all colour. Beneath it lie a spearhead and a piece of shield with a worn device. Someone died here a very long time ago, in a war no one has told you of. You put everything back where it was, cover it with mud and go on in silence.

#### `cienagas_4_1`

The Deceiving Lights.
By night you see lights ahead, in a row, like the lanterns of a camp. You think of men, of fire, of a little warmth, and you go towards them. The lights draw back as you advance, always at the same distance. Before you realise it, you are waist-deep in water. Then you see that the lights do not float alone: two are held aloft by grey, still figures you took for dead trunks. The other two come down towards you, and the figures begin to wade through the water. You have fallen into their trap like a child.

#### `cienagas_4_2`

Voices in the Mist.
In the mist you begin to hear voices. They are faint, distant, and you cannot understand what they say; they seem to speak in a very ancient tongue, slowly, like someone calling to one who has gone. They come from the water. You stop to listen, and for a moment it seems to you that one of them speaks your name. You shake your head hard. You cover one ear with your hand and go on sounding with your staff, faster, until the voices are left behind.

#### `cienagas_4_3`

The Quagmire.
To save time you cut across a quagmire that looks firm. It is not. At the third step you sink to your knees, then to your waist, and the mud grips your legs like hands. The more you move, the deeper you sink. You keep still, breathe, and cast the grey Elven rope towards a dead trunk jutting up to one side. At the third try it wraps around a branch. You pull yourself out span by span, covered in black mud and trembling, and unwind the rope from the branch before going on.

#### `cienagas_4_4`

Those Who Rise from the Water.
You decide to spend the night on the islet. You fall asleep with your sword beside you, and the cold wakes you: icy hands have seized your ankles from the water at the edge. They drag you towards the mud, slowly, without a sound. You kick yourself free and roll to the twisted bush. At the edge of the islet, half in the water, three wraiths are crawling towards you on their elbows, with tatters of mist for hair. You set your back to the bush and raise your sword.

#### `cienagas_4_5`

The Sunless Dawn.
You leave the islet before daybreak, to make use of what little light there is. But dawn here brings no sun: only a grey that lightens a little, like dirty water in a glass. To the east the mountains stand out sharper than ever, black, with smoke on their peaks. Ahead the marsh goes on and on. You walk without speaking, not even to yourself. You catch yourself missing even the icy wind of the hills.

#### `cienagas_4_6`

Sparks from the Road.
The road ends in a level space of broken flagstones, perhaps a square or a sunken guard-post. As you step onto the first, pale sparks spring from the cracks between the stones, as from a dead forge that someone had stirred. They rise, gather and form four flames, which begin to circle around you. They do not burn: they freeze. Where they pass, the water of the puddles is coated with frost. The ring tightens.

#### `cienagas_4_7`

The Mounds.
The road passes beside some long mounds covered in coarse grass, rising from the water in rows. They are too regular to be natural. At the head of one stands a tilted standing stone with letters that time has worn away. They are graves, you understand, or what is left of them. You skirt the rows with respect, treading on none. You do not know to whom they belong, but someone wished them to be remembered.

#### `cienagas_5_0`

The Cry on High.
You cross a plain of mud without a single reed. Halfway across, the air turns to ice and the light goes out. Very high among the clouds a shadow with enormous wings passes, and from it falls a cry that pierces you. You throw yourself face down in the mud, covered by the grey cloak, not moving, not breathing. When the cold at last withdraws, the sky is empty again, but it is a long while before you get to your feet.

#### `cienagas_5_1`

Thirst.
You are surrounded by water and cannot drink it. The water of the pools tastes of iron and rot, and the mere smell of it turns your stomach. Your waterskin is nearly empty. At last you find some broad leaves, folded like bowls, that have gathered the night's dew. You empty them one by one into your mouth, careful not to lose a single drop. It is little, but enough to keep you going until it rains again.

#### `cienagas_5_2`

The Dry Reeds.
You plunge into a sea of dry reeds, taller than you, that creak and rub together in the wind like bones. You can see nothing beyond a single step. The noise is constant, and beneath it you seem to hear others: a splash, a rustle, something dragging itself along. Each time you stop, everything stops. You go forward parting the stems with your arm, always towards where you believe the east to be. When you come out, your face is covered in fine cuts.

#### `cienagas_5_3`

The Night of Candles.
As night falls you take refuge in a heap of stones, and from there you watch lights kindle all over the marsh: hundreds of them, pale, green and white, rising and falling over the water like candles in an endless procession. None comes near. You stay motionless, your cloak over your head, watching them through a gap. It is beautiful and terrible at once. At dawn, one by one, they go out, and the marsh is grey and empty again.

#### `cienagas_6_0`

The Wraith of the Channel.
A wide channel of slow water bars your way, and there is no way around it. You wade it with your sword held high and the water up to your chest, feeling the bottom with your feet. The current carries towards you a bundle of dark rags, floating, like an old cloak someone lost. You move aside to let it pass. But the bundle stops beside you, unfolds and rises up: a wraith larger than any you have seen, wrapped in the tatters of an ancient cloak. And it hurls itself upon you.

#### `cienagas_6_1`

The End of the Marsh.
You skirt a broad lagoon for hours, and little by little the marsh changes. The reeds grow scarce, the mud dries and cracks, and heaps of grey slag and dust begin to appear. Beyond, the land is a desert of ash reaching to the feet of the black mountains. Firm ground at last, though dead. You stop on the last shore. Between you and that land there remains only a great pool, dark and still as an eye.

#### `cienagas_7_0`

The Lord of the Drowned.
You reach the last pool, the one that divides the marsh from the dry land. When you set foot in the water, the whole pool shudders. From its centre rises an enormous figure, made of mud and black water, with a rusted crown sunk into its brow and the remnants of ancient armour. Its eyes are two pale lights. Black water streams from its arms as it raises them towards you. It is the lord of those who sleep beneath the marsh, and it will not let you pass.

## ithilien

#### `ithilien_1_0`

South, beneath the Mountains.
You leave behind the pool of the lord of the drowned and come out into the desert of ash. There you lose the small footprints among the slag, but you know where they were going: east, beyond the black wall of the mountains. There is no pass on this side, and to the north you glimpse dark towers closing a valley. You turn south and follow the western skirts of the mountains along an ancient road. For two days all is dust. On the third the land changes: grass, bushes, trees, and a scent of thyme and resin. At the first clean spring you drink your fill and refill your waterskin.

#### `ithilien_2_0`

The Scouts of the South.
The road, paved centuries ago by the men of Gondor, runs south between ever taller trees. At a bend you run into two men coming the other way. They are tall and swarthy, with red cloaks, gold rings in their ears and a red serpent painted on their shields. They are the first men you have seen in weeks, but they mean no good: they shout something at you in a tongue you do not understand and draw their scimitars as one.

#### `ithilien_2_1`

The Forsaken Garden.
You leave the road and go down slopes covered with herbs: thyme, sage, marjoram, bushes that crackle beneath your boots and release their scent as you tread on them. Among them low stone walls appear, terraces that someone tended long ago and that no one waters now. This was a garden, you understand, the garden of a whole realm. In a brook you wash the mud of the marshes from your face and hands. For the first time in many days, you find yourself smiling.

#### `ithilien_3_0`

The Watchtower.
On a hill stands a watchtower, roofless, its inner stair half collapsed. On the lintel of the door is carved a tree with seven stars above it, almost hidden by ivy. You climb carefully to the top. From there you see woods rolling southward, the distant gleam of the great river to the west and, to the east, always, the black mountains. To the south, among the trees, several columns of smoke rise. Someone is camping in these lands, and they are not the masters of the tower.

#### `ithilien_3_1`

Whistles in the Wood.
You go deep into a dense wood of oaks and ferns. Soon you hear a whistle, like a bird's, and another answering it from the opposite side. Then a third, closer. They are not birds. You stand still, your hand on your sword, and for a long while nothing happens. Only once, between two trunks, do you seem to see a shadow clad in green and brown move and vanish without a sound. Someone has seen you, and has decided to let you pass.

#### `ithilien_3_2`

Among the Cypresses.
You cross a grove of dark cypresses, tall and slender as spears, smelling of warm resin. An arrow buries itself in the trunk beside you, a hand's breadth from your head, and stays there quivering. You throw yourself to the ground. Among the branches of a cypress, higher up, you see a red cloak and the gleam of a bow being drawn again. The archer is alone and has chosen you as his mark. You run from trunk to trunk towards him, while another arrow whistles past you.

#### `ithilien_3_3`

The Stepping-stone Ford.
A wide, swift stream comes down from the mountains, and you cross it by a ford of flat stones. On the far bank, three men of the south are crouched by the water, filling their waterskins. They see you at the same moment you see them. The one with the spear leaps up and wades into the stream to bar your way, while the other two fall back along the bank and unsling their bows. You are in the middle of the water with nowhere to hide. You advance on the spearman.

#### `ithilien_4_0`

The Empty Village.
At the foot of the hill lies a ruined village: roofless stone houses, a choked well and a dry fountain with a basin carved in the shape of a shell. In one of the houses the chimney still holds soot. You decide to spend the night there without lighting a fire, for the smoke would be seen from afar. You sup on Elven bread with some sorrel leaves you find in an overgrown garden. Before you sleep, you wonder who lived here and where they went.

#### `ithilien_4_1`

The Camp of Smoke.
You approach one of the columns of smoke at dusk, crawling among the ferns. It is a camp of men of the south: red tents, spears planted in the ground and four guards by the fire, roasting something. You mean to circle round it unseen, but a dry branch snaps beneath your knee. The four heads turn at once. Two of them reach for their scimitars; the other two, for their bows. There is no point in hiding any longer.

#### `ithilien_4_2`

The Green Arrow.
You follow the direction of the whistles and come to a clearing. At its edge, beside a fallen trunk, lies a man of the south, his red cloak spread on the grass. In his chest is lodged a long arrow with green feathers, unlike any orc arrow. Someone still defends these woods, unseen, against those who come from the south. You touch nothing. You leave the clearing slowly, feeling on the back of your neck eyes that you cannot find.

#### `ithilien_4_3`

The Red Column.
From the hillside, hidden among the bushes, you watch a column of men of the south pass along the road in the valley. They are marching north, hundreds of them, with red and black banners, spears, serpent shields and drums that throb in your chest. In the midst of the column moves something you at first mistake for a grey hill: an enormous beast, with curved tusks and a wooden tower on its back. You hold your breath until the last soldier passes out of sight.

#### `ithilien_4_4`

The Pool of Water-lilies.
Among some cedars you find a round pool of clear water, with white water-lilies and a rim of dressed stone that time has been covering with moss. Blue dragonflies skim the surface. You take off your boots and dip your feet in the cool water, and for a while you forget the arrows, the ash and the mountains. A frog leaps from a leaf. When at last you rise, the sun has sunk and the shadow of the cedars covers the water.

#### `ithilien_4_5`

Horns in the West.
To the west horns sound, then shouts, the clash of steel and a bellow so enormous it makes the leaves tremble. You run towards it, but the wood is dense and it takes you hours to arrive. When you do, it is all over. You find only flattened ferns, broken spears, shreds of red cloaks hanging from the branches and a swathe of snapped trees running off towards the river. There is no one. Whoever fought here has gone, and you arrive late, once again.

#### `ithilien_4_6`

Archers in the Ravine.
You climb a dry ravine, among white rocks and thorn bushes, looking for a way south. Halfway up a stone rolls down from above, then another. You look up: on the rim of the ravine two archers of the south await you with arrows on the string, one on each side, as though they knew you would pass this way. The arrows glance off the rocks at your feet. You climb towards the nearer one, crouching, from rock to rock.

#### `ithilien_4_7`

The Mild Night.
You make camp beneath a huge cedar, sheltered by its low branches. It is the first mild night since you left the Golden Wood. The sky is clear and full of stars, and among the bushes a nightingale sings, again and again, as if there were no war in the world. You lie on your back on your cloak. You think of Bree, of the inn and its clatter of mugs, and fall asleep before you know it. No one troubles you all night.

#### `ithilien_5_0`

The Broken Bridge.
A deep-cut torrent splits the wood from north to south, and the only crossing is a single-arched stone bridge, broken in the middle. Between its two ends there is a gap of several paces above the foaming water. First you throw your pack to the far side. Then you step back, take a run-up and leap. You land on your knees at the edge, half your body in the air, and drag yourself to safety. Below, the water roars on, indifferent.

#### `ithilien_5_1`

The Arrow You Did Not See.
You pass through an ancient oakwood, its trunks so broad that three men could not encircle them. You keep your eyes on the ground, on the creaking branches, and do not look up. A short whistle behind you makes you spin round: from a high branch an archer of the south falls through the leaves with a green-feathered arrow in his shoulder. He was aiming at you. He picks himself up as best he can and limps away southward. You look for whoever shot. There are only trees, and a long silence.

#### `ithilien_5_2`

The Storm.
The sky clouds over all at once and a spring storm breaks, with thunder rolling among the hills. You find shelter in a stone hall half buried in the hillside, with ivy-clad pillars. On the far wall is carved a king with a tall crown and a sword across his knees, his face worn away by the centuries. You sit beside him, drenched, and listen to the rain beating on the leaves. It smells of wet earth and rosemary.

#### `ithilien_5_3`

Enormous Tracks.
In a clearing you find round tracks, so large you could sit inside one, sunk into the soft earth. Around them the young trees are snapped at the height of your chest and branches have been torn off far higher than your sword could reach. Something enormous passed here not long ago, at a slow pace, and stopped to eat bark. The tracks lead on southward. You follow them warily, taking care to make no noise.

#### `ithilien_6_0`

The Sentinel of the South.
The wood thins and you reach a saddle crossed by a path running south. In the middle, leaning on a long spear, waits a warrior of the south taller than the rest, with a round bronze shield and a helm with a red plume. He neither hides nor calls to anyone. He watches you approach, plants his spear in the ground, draws a broad scimitar and nods to you. He wants to fight you alone.

#### `ithilien_6_1`

Beneath the Smoke of the East.
You climb a bare knoll to see the way. As you watch, the eastern sky begins to darken: above the mountains an enormous brown cloud advances, spreading slowly over the woods like a stain of oil on water. The sun becomes a pale disc and then disappears. The birdsong dies away. At mid-morning it seems that night is falling. You do not know what it is, but you know where it comes from, and you feel that something great has begun.

#### `ithilien_7_0`

The War Oliphaunt.
The path leads you to a clearing beside the road, full of snapped trees. There it is: a grey beast, as big as a house, with curved tusks bound in bronze rings and a wooden tower on its back from which red tatters hang. It must have fallen behind its column; the tower is empty, its riders fallen or fled. With no one to guide it, the beast raises its trunk, lets out a bellow that shakes the ground and comes towards you.

## cirithungol

#### `cirithungol_1_0`

The Green Light.
Beneath a brown sky that no longer lets the sun be seen, you go on southward to a crossroads in the middle of a ring of trees. There you take the road that climbs east, towards the mountains. It leads you into a cold valley where everything glows with a sickly green light: the river, the meadows and, at the far end, a city of pale walls with a tall tower crowned with light. You have no intention of going near. Up the northern wall of the valley climbs a path of hewn steps, and in its dust you see small footprints once again.

#### `cirithungol_2_0`

The Pale Flowers.
You skirt the valley through the meadows that run down to the river, seeking the foot of the path without passing near the city. The meadows are covered in white flowers that glimmer faintly in the gloom, like little moons, and smell of rot, of an open grave. The river runs black and soundless. You dare not drink from it, thirsty though you are. You wrap yourself in your cloak and walk quickly, eyes lowered, while the green light follows you over the grass.

#### `cirithungol_2_1`

The Valley Patrol.
You begin to climb the path and at the first bend come face to face with two orcs coming down. They are not like those of the mountains: they wear black mail and on their breasts a painted device, a white moon with a skull for a face. They stop dead and sniff the air. One says something in a hoarse voice and laughs; the other has already drawn a long, curved knife. They stand between you and the climb, and they have no thought of moving aside.

#### `cirithungol_3_0`

The Orc at the Stream.
A stream comes down from the heights and crosses the meadows to join the river. Its water is so cold that your ankles ache as you wade it, and it has the same greenish sheen as everything else. Halfway across you hear a splash upstream: an orc with the white moon on his breast has crouched to fill a bucket. He raises his head, sees you and drops the bucket, which floats away. He runs at you through the water, sword already out.

#### `cirithungol_3_1`

Webbed Clefts.
To keep off the path, where you might meet someone, you climb the clefts in the wall, clutching at roots and ledges. The clefts are full of grey cobwebs, old and dusty, so thick they look like rags. As you brush one aside with your hand, something moves within. Two spiders the size of dogs come out of the cleft, black and hairy, with eyes that gleam like beads. One drops down above your head. The other cuts off your retreat.

#### `cirithungol_3_2`

The Straight Stair.
The path becomes a straight stair, hewn in the rock, climbing on and on without rest into the darkness. The steps are high and narrow, worn in the middle. There is no rail, nothing to hold on to, only the wall on one side and the drop on the other. You climb counting steps until you lose count. Your legs are burning. When you turn to look, the city of the green light lies far below, small as a toy.

#### `cirithungol_3_3`

The Silence of the Valley.
You hide among some rocks to wait for the danger to pass. After a while you realise that you can hear nothing: no wind, no water, no bird, not even your own breathing. You clap your hands and the sound dies in the air as if it had never been. The green light of the city rises and falls slowly, like the pulse of something asleep. You do not know how long you stay like that. When at last you rise, your hands are numb.

#### `cirithungol_4_0`

The Post of the Moon.
Beside the path stands an ancient guard-post of white stone, built by folk far better than orcs. Above the door a crescent moon was once carved, beautiful, but someone has hacked at it and painted a skull over it. Inside there are animal bones, ashes and filth. You sit for a moment on the threshold. You think that this valley, before the green light, must have been a place where the moon was mirrored in the river.

#### `cirithungol_4_1`

The Collapse.
Higher up, a stretch of the path has fallen away, and between the two sides there is a gap too wide to leap. Above it, in the wall, a horn of rock juts out. You cast the grey Elven rope, catch it on the horn at the second try and swing across, your feet against the rock and the abyss beneath. On the other side you give a sharp tug, and the rope comes loose and falls into your hands. You coil it carefully.

#### `cirithungol_4_2`

The Fire in the Niche.
You go on climbing to a landing and see a reddish light: in a niche in the wall, four orcs of the white moon are warming themselves at a small fire, arguing loudly about something that has happened above. You press yourself against the rock to slip past. But the wind shifts, carries your scent to them, and the voices fall suddenly silent. Four heads turn towards the darkness where you stand. One takes up a burning brand and comes closer, sniffing.

#### `cirithungol_4_3`

The Fleeing Orc.
You hear footsteps running down and press yourself into a crack in the wall. An orc passes right by you without seeing you, stumbling, looking back over his shoulder. His helm is askew and there is blood on his arm, and he mutters something over and over, like a madman. He is not fleeing from you: he is fleeing from something above. He drops a curved knife on the steps and does not even stop to pick it up. You wait until his footsteps fade below before going on.

#### `cirithungol_4_4`

The Falling Stones.
You keep climbing, step by step. Suddenly there is a sharp crack above, and then a growing din: stones bouncing down the stair, some as big as heads. You flatten yourself against the wall while they whistle past a hand's breadth from you and vanish into the void. One grazes your shoulder and leaves your arm numb for a good while. When the noise stops, you stay still, waiting for the next. It does not come. You will never know whether they fell of their own accord.

#### `cirithungol_4_5`

The Gorge of Webs.
To spare yourself a stretch of stair, you cut through a narrow gorge between two walls. It is a mistake. The gorge is blocked from side to side by cobwebs, one after another, like the sails of a ship. You cut the first with your sword; it is tough and sticky as wet leather. As you cut the second, the whole gorge trembles. Four spiders come swiftly down the threads from the shadows above, clacking their jaws. Behind you, the webs already close the way.

#### `cirithungol_4_6`

The Counted Cakes.
You sit down in the shelter of a rock and open your pack. You count what is left of the Elves' bread: three cakes, still wrapped in their leaves. You break one, eat half of it slowly and put the rest away. The taste brings back to you for a moment the golden light of the trees, the water of the silver stream, the serene voice of the Lady. You feel stronger than you ought to. You take a sip from your waterskin and set off again.

#### `cirithungol_4_7`

The Searching Light.
You lean over the edge to look at the valley. At that moment the light of the tower grows stronger, and a green beam unfolds and sweeps slowly across the walls, like a hand groping in the dark. It draws near you. You throw yourself behind a rock and curl up, not breathing. The beam passes overhead, lights the stone beside you with a sickly gleam and moves on. You wait for it to die out completely before you move again.

#### `cirithungol_5_0`

The Ridge.
At last you reach the top of the climb. Before you the mountain rises in a jagged ridge, black against the brown sky, and in it opens a cleft: the pass. Beside it, perched upon the rocks, stands a dark tower with a single window lit red. From here you see no guards on its walls, but neither do you know whether they are watching you. You resolve that, come what may, you will not pass beneath that tower if you can help it.

#### `cirithungol_5_1`

The Stench.
The air changes. You begin to notice a sweet, rotten smell unlike anything you know, growing thicker at every step. Among the rocks lie the bones of goats, of wolves and of orcs, empty and clean, as if something had sucked the life from them and cast away the rest. Grey threads are tangled in them. You cover your nose with your cloak. You do not wish to know what feeds like this, but you suspect you soon will.

#### `cirithungol_5_2`

The Ledge.
Exhausted, you look for a place to sleep and find only a narrow ledge, little wider than your shoulders, above the void. You pass the Elven rope around a spike of rock and around your waist, and lie on your side against the wall. You sleep fitfully, waking every time a stone falls. No dawn comes, only a brownish grey a little paler than before. You untie the rope, coil it and put it away, and go on climbing.

#### `cirithungol_5_3`

The Fumes.
A thick pall of fumes comes down from the pass and crawls over the hillside, driven by an east wind. It smells of sulphur and burnt iron, and it makes your eyes stream. You tie a cloth over your mouth and go forward almost blindly, feeling the wall with one hand. You cough until your chest aches. When at last you come out of the smoke, your face is black with soot and your mouth bitter. It is the first breath of the land that lies beyond.

#### `cirithungol_6_0`

The Black Mouth.
The path is lost among rocks and you follow it to the end. There, in the wall, opens a black mouth, taller than two men, from which comes a cold and fetid air. Above, a dark tower perched on the ridge, with a red window, watches the pass; you do not wish to go beneath it, and there is no other way east. Torn webs hang across the threshold, as if someone had broken through them not long ago, and on the stone there are greenish stains, dried. You draw a deep breath, grip your sword and go in.

#### `cirithungol_6_1`

The Brood.
You turn off the path into a hollow among the rocks, so that no one can see you from above. The ground is covered in dry husks, like those of enormous eggs, and threads that stick to your boots. Too late you understand that it is a nest. Three young spiders, the size of calves, come out from beneath a slab and slowly surround you, raising their forelegs. Their sweet, rotten smell fills the hollow.

#### `cirithungol_7_0`

Shelob.
In the end only one way remains: the tunnel that runs through the mountain. Inside, the darkness is complete and the stench unbearable. You go forward feeling along the wall, among webs that cling to your face. Then you hear an enormous scraping, a click, and ahead of you two clusters of eyes kindle, one of them dull and crusted, wounded not long ago. A gigantic spider fills the tunnel from side to side. It is Shelob, the ancient mistress of this tunnel, and she is hungry.

## gorgoroth

#### `gorgoroth_1_0`

The Land of Ash.
You have left the great spider shrieking in the darkness, wounded, crawling back to her holes, and you did not follow her. Coming out of the tunnel, you passed by night beneath a tower that kept silent, with no guards on its walls and smoke in its windows, and you went down a ravine on the far side, slithering among loose stones. Now, before you, stretches a plain of ash and slag, broken by red fissures. In its midst, alone, a mountain spews fire. The water in your waterskin will have to last.

#### `gorgoroth_2_0`

The Black Tracker.
At the foot of the ravine, crouched over your trail in the ash, is a huge orc, black-skinned and long-armed, with a mail shirt down to his knees and a red eye painted on his shield. He sniffs your footprints like a hound. When he looks up and sees you, he neither shouts nor calls to anyone: he grins, baring his fangs, and rises slowly, drawing a broad sword. He wants the prey for himself alone.

#### `gorgoroth_2_1`

The Road of Ash.
You find a raised road crossing the plain, of black stones beaten flat, and you follow it for a stretch because on either side the ash comes up to your ankles. Rounding a heap of slag, you run headlong into two orcs coming the other way, laden with bundles. For an instant they stand dumbfounded, as surprised as you. Then they throw their bundles to the ground, draw their scimitars and hurl themselves at you, shouting to give themselves courage.

#### `gorgoroth_3_0`

The Water in the Rock.
Your waterskin weighs very little now, and you search for water among some black rocks. In a hollow you find a pool of old rain, a hand's depth, covered by a film of ash. You brush it aside with your hand. The water tastes of iron and smoke, bitter, but it is water. You drink slowly, in small sips, and fill your waterskin to the brim. Then you stay a while gazing at the empty pool, as one gazes at a spent treasure.

#### `gorgoroth_3_1`

The Red Fissure.
You hear marching feet and lower yourself into a fissure that cuts the ground. Below, very deep, a red light glows, and a heat rises that dries your lips and makes you sweat beneath your cloak. You hang from a ledge, two spans below the rim, while above you a troop of orcs goes by. The ash they raise falls on you. When they have gone, you crawl out, your hands aching from gripping the rock so hard.

#### `gorgoroth_3_2`

The Camps.
From the top of a heap of slag you look out over the plain. It is dotted with camps: fires, black tents, pens and long lines of orcs setting off northward one after another, like ants. This whole land seems to be emptying itself that way, towards something that summons it. To the north-east, among the clouds, you make out a dark mass, immense, taller than any mountain, and at once you look away. Better not to gaze at it long.

#### `gorgoroth_3_3`

The Pit of Ash.
To get away from the road you cross a hollow full of ash as fine as flour. You sink to your knees, then to your thighs. Each step raises a grey cloud that gets into your mouth, your eyes and your nose. You cover your face with the hem of your cloak and breathe through it. It takes you an eternity to cross what you would have covered in a moment on the road. On the other side you spit grey for a good while.

#### `gorgoroth_4_0`

The Fires.
As night falls you must pass close to a camp to avoid a fissure. You creep forward on all fours, using the shadows of the slag. You are about to leave it behind when an orc coming out to relieve himself stumbles over you. His shout wakes the whole camp. Two black Uruks and two orcs fall upon you, and far off, all across the plain, horns begin to sound, one after another. Suddenly, everyone is looking for you.

#### `gorgoroth_4_1`

The Sunless Day.
There is no day here, only a brown gloom a little lighter than the night. You lie down between two rocks and cover yourself entirely with the grey Elven cloak. You sleep fitfully. Once you are woken by footsteps: an orc patrol passes three strides from you, arguing, and one even sits on your rock to shake a stone out of his boot. He does not see you. The cloak makes you look like just another stone. When they have gone, it is a long time before you breathe again.

#### `gorgoroth_4_2`

The River of Fire.
You follow the rim of the fissure for hours. Further on it widens, and along its floor runs, slowly, a river of molten rock, red and yellow, that makes the air crackle. Its light shows you the way in the darkness, but the heat is so fierce that every little while you must step back from the edge to breathe. A stone you tread on breaks loose and vanishes into the fire without a sound. You leave the fissure as soon as you see firm ground to one side.

#### `gorgoroth_4_3`

The Olog of the Bridge.
The fissure can only be crossed by a narrow bridge of stone. At the far end, leaning on the parapet, is a troll. It is not like the one in the hills: it is leaner, black-scaled, with shield and hammer, and its eyes fix on you with a cold intelligence. No creature of this land has ever seemed to you so dangerous. It is an Olog-hai. It straightens, strikes its shield with its hammer and advances to the middle of the bridge. It has no intention of letting you cross.

#### `gorgoroth_4_4`

The Forges.
To keep away from the lines of orcs you go down the far side of the heap and come upon a quarter of open-air forges: hammers pounding without rest, fountains of sparks, roaring furnaces and sweating orcs drawing red-hot sword blades from the fire. No one looks outward; they all work as though their lives depended on it. You crawl behind the piles of slag, amid the din, until the noise of the hammers is left behind.

#### `gorgoroth_4_5`

The Empty Camp.
You find an abandoned camp: fallen tents, cold fires and refuse everywhere. Its troop has already marched north. You go through it looking for some water, and you are not alone. Four straggling orcs are rummaging through the remains, fighting over a barrel. When they see you they forget the barrel. One whistles, another laughs, and the four spread out around you among the fallen tents, barring your way on every side.

#### `gorgoroth_4_6`

The Patrol.
You climb out of the hollow by the far slope and shake off the ash. But your trail in the soft ash is as plain as a road, and a patrol finds it at once: a black Uruk in command and two orcs running ahead with their noses to the ground. They catch you on a flat stretch with no rocks to hide behind. The Uruk barks an order and the orcs split up to attack you on the flanks, while he comes straight at you.

#### `gorgoroth_4_7`

The Last Cake.
You stop in the shelter of a black rock, exhausted. At the bottom of your pack you find the last cake of Elven bread, wrapped in its leaf. You break it in two and eat both halves, slowly, with your eyes closed. For a moment the ash disappears and you are once more beneath the golden trees, with clear water near. You fold the empty leaf and keep it against your breast, as a keepsake. You rise with a strength you did not know you had.

#### `gorgoroth_5_0`

The Pursuit.
Someone has found your trail: horns begin to sound all across the plain, and the patrols converge on where you are. You run westward, away from the mountain, leaping fissures and hiding behind the slag, while they follow you with torches. For hours you draw them along behind you, ever further. At last you lose them in a field of rocks. Exhausted, you turn and look back. The plain around the mountain has been emptied of patrols.

#### `gorgoroth_5_1`

Beneath the Eye.
You cross the plain by night. To the north-east, atop a dark and immense mass, a red light burns, and suddenly you feel it searching for you, sweeping the land like the beam of a beacon. You do not throw yourself down: you keep walking, head bowed and cloak drawn tight, like one more shadow of the ash. For one unbearable instant the weight of that gaze seems to brush you, and your knees tremble, but you do not stop. Then it turns aside, towards the north, to something that matters to it more. You breathe.

#### `gorgoroth_5_2`

The Lava Cave.
You find the mouth of a long, black cave that the lava left as it cooled, and you creep inside to rest. The air is still and smells of sulphur, but here they will not see you. You shake your waterskin: there is little left. You take a sip, only one, and swill it round your mouth before swallowing. Your lips are cracked and your throat is raw. You sleep sitting against the wall, sword in hand.

#### `gorgoroth_5_3`

The Winged Shadow.
You cross an open flat without a single rock, and halfway over the air turns suddenly cold. A piercing cry comes down from the sky. A shadow with enormous wings flies low, circling, searching. You run for a distant fissure, the only cover, and the shadow cuts across your path again and again, forcing you to swerve southward, away from where you meant to go. You reach the fissure breathless and crawl inside. When you come out, the sky is empty and you do not know where you are. It was hunting you, and you do not know why.

#### `gorgoroth_6_0`

The Skirts of Fire.
You climb the most remote flank of the fire-mountain, far from the road that winds up it, over a slope of loose slag that slides beneath your feet. The ground burns through your boots and the mountain booms like a drum. From above you see the torches of patrols sweeping the plain, searching. Then you do something you cannot wholly explain: you push a great stone down the slope, with a crash, and show yourself against the glow. The torches turn towards you. You draw them after you along this flank, far from any other path.

#### `gorgoroth_6_1`

The Last Guards.
At the foot of the mountain stands one last guard-post: a low tower of black stone beside the road. Only two black Uruks remain there, whetting their swords by the door. You try to skirt round them, but the slag crunches beneath your feet. One raises a horn to his lips and sounds it. All across the plain other horns answer, and the torches of the patrols begin to converge on you. The two Uruks close in with naked swords, one on each side.

#### `gorgoroth_7_0`

The Winged Beast.
You move away from the road along the most remote flank of the mountain, the torches always behind you. But something has long been searching for you across the plain, and at last it finds you. A shriek rends the sky and the air freezes, though the ground burns. A featherless beast, long-necked, its hide like old leather, alights on a rock before you, and upon it a figure shrouded in black, faceless, with a pale sword. A Nazgûl. You fall back downhill, drawing them with you, ever further from the path.

## orodruin

#### `orodruin_final`

The End of the Shadow.
The Nazgûl has fled northward, shrieking, astride its wounded beast, and you are left on the slope without understanding how you are still alive. The next day the earth shudders. The mountain bursts open: fire, rocks and a thunder that does not end. Far off, the dark tower of the north cracks and crumbles amid clouds of dust, and a mighty wind sweeps the plain. You run downhill between rivers of lava to a knoll of solid rock. When the smoke parts, you see great Eagles flying towards the mountain. You will never wholly know what happened up there, nor whether the patrols you drew after you left another path free. You think of the owners of those small footprints and find yourself hoping, without knowing why, that they are alive. The shadow has gone. With your pack and waterskin all but empty after so many days, you look west and begin the long road home to Bree.

<!-- end -->
