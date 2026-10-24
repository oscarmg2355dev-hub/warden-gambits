# Música ambiente y narración de los nodos — diseño

Fecha: 2026-10-21. Estado: aprobado en conversación.

Elecciones del usuario:
- **Narración siempre automática:** empieza sola al entrar en un nodo, con un botón para pararla y una opción para desactivarla.
- **La música baja de volumen** mientras habla la narradora.
- **Audios generados por el usuario** en Gemini (AI Studio), como con las imágenes, a partir de prompts por nodo. Los convierte y comprime la herramienta del proyecto.
- **Todo dentro del bundle,** midiendo hasta dónde llega el tamaño.

## 1. Música ambiente

- **Pistas:** de 2 a 3, de fantasía medieval tranquila, todas con licencia libre para uso comercial:
  - Pixabay Content License;
  - CC0 (OpenGameArt o Freesound);
  - o CC-BY, con crédito en «Acerca de».

  Los originales y sus licencias quedan en `docs/content/music/` con un `LICENCIAS.md` (título, autor, URL, licencia y si exige atribución). En la app van en `app/src/main/res/raw/music_*.ogg` (OGG Vorbis, estéreo, unos 96–128 kbps, 5–8 MB en total).
- **Dónde suena:** en todas las pantallas excepto el combate (`Routes.COMBAT` y `Routes.PRACTICE_COMBAT`).
  - Al entrar en combate se funde a cero en unos 800 ms y se pausa.
  - Al salir, se reanuda con una entrada de unos 1,5 s.
  - Las pistas se encadenan en bucle.
  - Con la app en segundo plano (fuera de STARTED), la música se pausa.
- **Volumen:** la música suena a su volumen normal y **baja al 25 %** mientras suena una narración (fundido de unos 300 ms); vuelve al terminar o al parar la narración.
- **Componente:** `audio/MusicPlayer`, con `MediaPlayer` sobre `res/raw`. Lo controla `MainActivity` o `WardenNavHost` según la ruta actual y los ajustes. El ambiente de combate actual (`amb_combat`) no cambia.

## 2. Narración

### 2.1 Prompts para el usuario

- **`docs/content/narration-prompts.md`:**
  - **Cabecera:** el procedimiento en AI Studio («Generate speech»): voz recomendada, cómo guardar y formato WAV.
  - **Instrucciones de estilo:** un bloque fijo en español y otro en inglés, para pegarlos una sola vez: narradora femenina de fantasía épica, voz grave y cálida, ritmo pausado, suspense y pausas en los momentos de tensión, sin efectos ni música.
  - **Una sección por zona,** en orden de ruta, con una entrada por nodo: la clave (`bree_1_0`, …, `orodruin_final`) y el texto a leer, con el título y el texto del nodo tal cual, en español y en inglés.
  - Lo genera `tools/narration/build_prompts.py` a partir del contenido (`story/*.json` y `story/en/*.json`), así que se rehace si cambian los textos.
- **Dónde guarda el usuario los audios:** `docs/content/narration/es/<clave>.wav` y `docs/content/narration/en/<clave>.wav`. También se aceptan `.mp3`, `.m4a`, `.ogg` y `.flac`, y los sufijos « (1)» de descargas repetidas, con la misma regla que las imágenes: la versión sin sufijo tiene preferencia.

### 2.2 Conversión

- **`tools/narration/convert.py`**, que usa el ffmpeg de `imageio-ffmpeg` (paquete de Python; no se instala nada en el sistema):
  - recorta los silencios de inicio y final (umbral de unos -45 dB, dejando unos 150 ms);
  - normaliza el volumen (loudnorm, unos -18 LUFS);
  - lo pasa a **AAC-LC mono a 32 kbps y 24 kHz** (`.m4a`);
  - lo escribe en `app/src/main/assets/narration/<es|en>/<clave>.m4a`;
  - solo convierte lo que falta o ha cambiado (por fecha), como `generate_images.py convert`.
- **Tras convertir:**
  - regenera `docs/content/narracion-pendientes.md`, con las claves sin audio por idioma;
  - imprime el tamaño total de la narración en la app y una estimación del bundle.
- **Opción `--bitrate 24`** para reducir tamaño si hace falta.

### 2.3 Reproducción en la app

- **`audio/NarrationPlayer`:** `MediaPlayer` sobre `assets/narration/<idioma>/<clave>.m4a`, con `play(key, language)`, `stop()` y un estado `StateFlow<NarrationState>` (`IDLE`, `PLAYING`).
- **Pantalla del nodo:**
  - Al entrar en un nodo (y al cambiar de nodo), si la narración está activa y existe el audio del nodo en el idioma del juego, empieza sola. También en tengwar, porque la voz es del idioma, no de la escritura.
  - **Botón** en la cabecera del texto: «■ Parar» mientras lee; «▶ Leer» cuando está parada o ha terminado.
  - Se para al elegir una opción, al pulsar «¡Luchar!», al salir del nodo (atrás o mapa) y al pasar la app a segundo plano.
  - **Nodos sin audio:** no hay botón ni narración.
- **Pantalla de fin:** el epílogo (`orodruin_final`) se narra igual.
- **Cambio de idioma:** si cambia el idioma con un nodo abierto, la narración en curso se para.

## 3. Opciones

- **Tres interruptores** sustituyen a «Sonido: sí/no», con claves de DataStore:
  - `music_enabled` (por defecto, sí);
  - `sfx_enabled` (por defecto, el valor actual de `sound_enabled`, que se migra);
  - `narration_enabled` (por defecto, sí).

  Textos: «Música», «Efectos», «Narración» / «Music», «Effects», «Narration».
- **Efectos:** gobiernan lo que hoy gobierna «Sonido», es decir, los efectos y el ambiente de combate.

## 4. Tamaño

- **Estimación:** unos 73 MB por idioma a 32 kbps (5 h 15 min de voz), así que el bundle pasa de unos 71 a **unos 220 MB**, por encima del límite de 200 MB del módulo base de Play.
- **El informe de `convert.py` lo mide de verdad** a medida que llegan audios. Medidas, por este orden:
  1. `--bitrate 24` (unos -37 MB en total);
  2. si sigue sin caber, mover `assets/narration` a un paquete de Play Asset Delivery (`install-time`). Esto no se hace en esta spec.

## 5. Fuera de alcance

- Narración en combate y en el resto de pantallas.
- Elegir la voz en el juego.
- Voz del sistema (`TextToSpeech`).
- Asset packs.

## 6. Pruebas

- **Robolectric y unitarias:**
  - **Música:** suena en las rutas de fuera del combate y se pausa en `combat` y `practice_combat` (control sin `MediaPlayer` real, detrás de una interfaz); baja al 25 % con la narración y vuelve.
  - **Ajustes:** migración de `sound_enabled` a `sfx_enabled`; los tres interruptores se guardan, se respetan y salen en es, en y tengwar.
  - **Narración:**
    - un nodo con audio arranca solo y muestra «Parar»;
    - sin audio, ni botón ni reproducción;
    - elegir una opción o ir al combate la para;
    - con la narración desactivada no arranca;
    - con el idioma en inglés usa `en/`.
- **Herramientas (Python):** `convert.py` convierte un WAV sintético de prueba a `.m4a`, regenera `narracion-pendientes.md` sin esa clave y no reconvierte si no ha cambiado. `build_prompts.py` genera 507 entradas por idioma.
- **Verificación:**
  - `./gradlew :engine:test :app:testDebugUnitTest :app:assembleDebug :app:bundleRelease`;
  - en el emulador: la música en el menú y el mapa, el silencio en combate y la narración con un audio de prueba;
  - tamaño del AAB anotado.
