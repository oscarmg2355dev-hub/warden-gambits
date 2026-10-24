# Música ambiente y narración — plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:**
- Música ambiente en todas las pantallas salvo el combate, que baja de volumen mientras se narra.
- Narración automática de cada nodo con audios que genera el usuario en Gemini, convertidos por una herramienta del proyecto.
- Tres interruptores de sonido.

**Architecture:**
- **Python (`tools/narration`):** prompts por nodo y conversión a AAC en `assets/narration`.
- **App:** `MusicPlayer` y `NarrationPlayer` detrás de interfaces, para poder probarlos sin `MediaPlayer`. El control lo gobiernan la ruta actual y los ajustes.
- **`:engine`:** no cambia.

**Tech Stack:** Python 3 + `imageio-ffmpeg`; Kotlin, Compose, `MediaPlayer`, DataStore, Robolectric.

**Spec:** `docs/superpowers/specs/2026-10-21-music-narration-design.md`

## Global Constraints

- **Solo `:app` y `tools/`.** `:engine` sin cambios.
- **Narración:**
  - audios del usuario en `docs/content/narration/<es|en>/<clave>.<wav|mp3|m4a|ogg|flac>`; se aceptan sufijos « (1)», con preferencia de la versión sin sufijo;
  - salida en `app/src/main/assets/narration/<es|en>/<clave>.m4a`: AAC-LC mono, 32 kbps (`--bitrate 24` opcional), 24 kHz;
  - silencios recortados (unos -45 dB, dejando unos 150 ms) y volumen normalizado (loudnorm, unos -18 LUFS);
  - claves: `<zona>_<nivel>_<índice>` y `orodruin_final`, 507 por idioma.
- **Música:**
  - licencia libre para uso comercial (Pixabay Content License, CC0 o CC-BY con crédito);
  - originales y `LICENCIAS.md` en `docs/content/music/`;
  - en la app, `res/raw/music_*.ogg`, 5–8 MB en total;
  - suena en todas las rutas salvo `combat` y `practice_combat`;
  - fundidos de unos 800 ms al salir, unos 1,5 s al entrar y unos 300 ms para bajar al 25 % durante la narración;
  - se pausa en segundo plano.
- **Narración en la app:**
  - automática al entrar en un nodo y en la pantalla de fin si existe el audio en el idioma del juego (también con escritura tengwar);
  - botón «■ Parar» / «▶ Leer»;
  - se para al elegir, al luchar, al salir, en segundo plano y al cambiar de idioma;
  - sin audio, ni botón ni reproducción.
- **Ajustes:** `music_enabled` (por defecto sí), `sfx_enabled` (migra el valor de `sound_enabled`) y `narration_enabled` (por defecto sí).

  | clave | es | en |
  |---|---|---|
  | `options_music` | Música | Music |
  | `options_sfx` | Efectos | Effects |
  | `options_narration` | Narración | Narration |
  | `node_read` | ▶ Leer | ▶ Read |
  | `node_stop_reading` | ■ Parar | ■ Stop |

  Los botones de ajuste usan el patrón actual «X: sí/no».
- **Sin commits.** Se conservan los finales de línea; `strings.xml` es CRLF, así que nada de `sed -i` sobre él.
- **Entorno:** `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`.

## Review Focus

1. **Cambiar de nodo rápido** (elegir opción → nuevo nodo): la narración anterior se para antes de que empiece la nueva; nunca suenan dos a la vez. → Task 4, `newNodeStopsPreviousNarration`.
2. **Ir al combate con la narración sonando:** se para y la música se funde a cero; al volver, la música entra y la narración no se reanuda sola en el nodo anterior. → Task 4, `fightStopsNarration`, y Task 3, `musicPausesInCombatRoutes`.
3. **Interruptores apagados en caliente:** apagar «Música» para la música al instante y apagar «Narración» para la que suena. → Task 2 y Task 4.
4. **Audio ausente o corrupto:** si `MediaPlayer` falla, se registra y no hay botón ni bloqueo. → Task 4, `brokenAudioIsIgnored`.
5. **Segundo plano a mitad de narración:** se para (no se reanuda sola) y la música se pausa y vuelve. → Task 3 y Task 4, verificación manual.

---

### Task 1: Herramientas de narración (prompts y conversión)

**Files:**
- Create:
  - `tools/narration/build_prompts.py`
  - `tools/narration/convert.py`
  - `tools/narration/test_narration.py` (unittest)
  - `tools/narration/requirements.txt` (`imageio-ffmpeg`)
- Generated:
  - `docs/content/narration-prompts.md`
  - `docs/content/narracion-pendientes.md`
  - las carpetas `docs/content/narration/es/` y `en/` (con un `.gitkeep`)

**Interfaces:**
- Produces:
  - **`build_prompts.py`:** sin argumentos; lee `engine/src/main/resources/story/*.json` y `story/en/*.json` en orden de `Route` (las zonas de `Route.kt`) y escribe `narration-prompts.md`, con:
    - una cabecera con el procedimiento en AI Studio («Generate speech», una sola voz, WAV, cómo nombrar y dónde guardar);
    - una voz recomendada (una femenina madura de Gemini, por ejemplo «Gacrux» o «Kore»);
    - el bloque de estilo en español y en inglés;
    - por idioma y zona, una entrada por nodo con la clave y el texto (`título.` + salto + texto).
  - **`convert.py [--bitrate 32|24] [--force] [--lang es|en]`:** convierte lo que falta o ha cambiado, regenera `narracion-pendientes.md` (claves sin audio por idioma, contadores y MB en `assets/narration`) e imprime `Convertidos N, al día M; narración en la app: X MB; bundle estimado: ~Y MB` (Y = tamaño del último `app-release.aab`, si existe, más el delta).

- [ ] **Step 1: Write the failing tests** `test_narration.py`:
  - `test_prompts_cover_every_node`: el markdown generado tiene 507 claves por idioma, `orodruin_final` incluida, y cada texto coincide con el contenido.
  - `test_convert_wav_to_m4a`: un WAV sintético de 2 s de tono con 1 s de silencio a cada lado, en una carpeta temporal → `.m4a` de duración unos 2,3 s, mono y 24 kHz (con `ffprobe` de imageio-ffmpeg o leyendo la salida de `ffmpeg -i`); la clave desaparece de pendientes.
  - `test_convert_skips_unchanged`: una segunda pasada no reconvierte.
  - `test_duplicate_suffix_preference`: con `x.wav` y `x (1).wav` gana `x.wav`; con solo `x (1).wav` se usa ese.
  - Los tests usan carpetas temporales mediante parámetros de las funciones o variables de entorno, sin tocar `docs/` ni `assets/`.
- [ ] **Step 2: Run** `python -m unittest tools/narration/test_narration.py`. Expected: FAIL.
- [ ] **Step 3: Implement.** `pip install --user imageio-ffmpeg` si falta. ffmpeg con `silenceremove`, `loudnorm`, `-ac 1 -ar 24000 -c:a aac -b:a 32k`.
- [ ] **Step 4: Run** los tests (PASS), `python tools/narration/build_prompts.py` y `python tools/narration/convert.py` (sin audios: pendientes con 507 + 507).

### Task 2: Tres interruptores de sonido

**Files:**
- Modify:
  - `data/SettingsRepository.kt`: `music_enabled`, `sfx_enabled` (por defecto, el valor de `sound_enabled` si existe; si no, `true`) y `narration_enabled`, con flows y `toggle*`;
  - `AppContainer.kt`: `musicEnabled`, `sfxEnabled` y `narrationEnabled` como `StateFlow`; el `GatedSoundPlayer` pasa a seguir `sfxEnabled`;
  - `ui/options/OptionsScreen.kt`: tres botones en lugar de «Sonido»;
  - `navigation/WardenNavHost.kt`;
  - `strings.xml` es/en.
- Test: actualiza `SettingsRepositoryTest`, los tests de Options y `StringResourcesParityTest`.

**Interfaces:**
- Produces:
  - `SettingsRepository.musicEnabled / sfxEnabled / narrationEnabled: Flow<Boolean>` y `toggleMusic()`, `toggleSfx()`, `toggleNarration()`;
  - `AppContainer.musicEnabled / sfxEnabled / narrationEnabled: StateFlow<Boolean>` y `toggleMusic()`, `toggleSfx()`, `toggleNarration()`;
  - `OptionsScreen(musicEnabled, sfxEnabled, narrationEnabled, onToggleMusic, onToggleSfx, onToggleNarration, language, script, …)`.

- [ ] **Step 1: Write the failing tests:**
  - **Valores por defecto:** sí/sí/sí.
  - **Migración:** con `sound_enabled = false` guardado, `sfxEnabled` es `false` y los otros dos `true`.
  - **Persistencia:** cada interruptor alterna y se guarda.
  - **Opciones:** muestra «Música: sí», «Efectos: sí» y «Narración: sí», y al pulsar cada uno llama a su toggle; en inglés y en tengwar.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*Settings*" --tests "*Options*"`. Expected: FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.

### Task 3: Música ambiente

**Files:**
- Create:
  - `docs/content/music/` (originales y `LICENCIAS.md`)
  - `app/src/main/res/raw/music_*.ogg`
  - `audio/MusicPlayer.kt` (interfaz y `AndroidMusicPlayer`)
  - `audio/MusicController.kt` (lógica pura: ruta, ajustes y narración → reproducir, pausar o bajar)
- Modify:
  - `AppContainer.kt`;
  - `MainActivity.kt` o `WardenNavHost.kt`, que observan la ruta actual con `navController.currentBackStackEntryAsState()` y el ciclo de vida;
  - `ui/about/AboutScreen.kt` y strings, solo si alguna pista exige atribución.
- Test: `app/src/test/java/com/wardengambits/app/audio/MusicControllerTest.kt`

**Interfaces:**
- Consumes: `AppContainer.musicEnabled` (Task 2).
- Produces:
  - `interface MusicPlayer { fun play(); fun pause(); fun setVolume(fraction: Float, fadeMillis: Long); fun release() }`;
  - `class MusicController(player: MusicPlayer)` con `fun update(route: String?, musicEnabled: Boolean, narrating: Boolean, foreground: Boolean)`:
    - reproduce si hay música activa, la app está en primer plano y la ruta no es de combate;
    - si no, pausa con fundido;
    - el volumen es 0,25 si `narrating` y 1 si no.
  - `const val MUSIC_DUCK = 0.25f`;
  - `val COMBAT_ROUTES = setOf(Routes.COMBAT, Routes.PRACTICE_COMBAT)`, comparando el patrón de ruta del back stack.

- [ ] **Step 1: Busca y elige las pistas.**
  - Busca en la web 2–3 pistas de «medieval fantasy ambient / tavern / calm adventure» con licencia verificada en su página: Pixabay Music (Pixabay Content License), OpenGameArt filtrando CC0, o CC-BY con nombre de autor.
  - Descárgalas a `docs/content/music/` y apunta en `LICENCIAS.md` el título, el autor, la URL, la licencia y si exige atribución.
  - Conviértelas a OGG Vorbis de unos 112 kbps en `res/raw/music_<n>.ogg`, con el ffmpeg de imageio-ffmpeg.
  - Si alguna pista exige crédito, añádelo en «Acerca de» (es/en).
- [ ] **Step 2: Write the failing tests** `MusicControllerTest`, con un `MusicPlayer` falso que registra llamadas:
  - `musicPlaysOutsideCombat()`: rutas `menu`, `map`, `node` y `practice` → play.
  - `musicPausesInCombatRoutes()`: `combat` y `practice_combat/{zone}/{level}/{index}` → pause.
  - `musicOffPauses()`.
  - `backgroundPauses()`.
  - `narrationDucksAndRestores()`: volumen 0,25 y luego 1.
  - `noRepeatedCalls()`: el mismo estado dos veces no repite `play`.
- [ ] **Step 3: Run** `./gradlew :app:testDebugUnitTest --tests "*MusicController*"`. Expected: FAIL.
- [ ] **Step 4: Implement.**
  - **`AndroidMusicPlayer`:** `MediaPlayer` por pista, encadenadas con `setOnCompletionListener` en bucle de la lista; fundidos con una corrutina o `ValueAnimator`.
  - **Cableado:** la ruta, el ajuste, `narrating` (de `NarrationPlayer.state`, Task 4; hasta entonces `false`) y el primer plano entran en `MusicController.update`.
- [ ] **Step 5: Run** `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.

### Task 4: Narración en los nodos y en la pantalla de fin

**Files:**
- Create: `audio/NarrationPlayer.kt` (interfaz, `AndroidNarrationPlayer` sobre `assets/narration/<lang>/<key>.m4a` y `NarrationState`)
- Modify:
  - `AppContainer.kt`: `narration: NarrationPlayer`; `narrating` alimenta `MusicController`;
  - `ui/node/NodeScreen.kt` y `NodeViewModel.kt` (o una ruta-envoltorio), y `ui/ending/EndingScreen.kt`, con el botón y los disparos;
  - `navigation/WardenNavHost.kt`;
  - `strings.xml` es/en: `node_read` y `node_stop_reading`.
- Test:
  - `app/src/test/java/com/wardengambits/app/audio/NarrationControllerTest.kt`
  - tests Compose de nodo y fin

**Interfaces:**
- Consumes: `AppContainer.narrationEnabled` (Task 2), `MusicController` (Task 3).
- Produces:
  - `enum class NarrationState { IDLE, PLAYING }`;
  - `interface NarrationPlayer { val state: StateFlow<NarrationState>; fun hasAudio(key: String, language: AppLanguage): Boolean; fun play(key: String, language: AppLanguage); fun stop(); fun release() }`;
  - `class NarrationController(player: NarrationPlayer)` con:
    - `fun onNodeShown(key, language, enabled)`: para lo anterior y, si está activa y hay audio, reproduce;
    - `fun onLeave()`: para;
    - `fun toggle(key, language)`: «Parar» o «Leer».

- [ ] **Step 1: Write the failing tests:**
  - **`NarrationControllerTest`**, con un player falso:
    - `autoPlaysWhenAudioExists()`;
    - `noAudioNoPlay()`;
    - `disabledNoPlay()`;
    - `newNodeStopsPreviousNarration()`: `stop` antes del segundo `play`;
    - `fightStopsNarration()` (vía `onLeave`);
    - `languageChangeStops()`;
    - `brokenAudioIsIgnored()`: si `play` lanza, se queda en IDLE y no propaga la excepción;
    - `englishUsesEnFolder()`.
  - **Compose:**
    - el nodo con audio muestra «■ Parar» mientras `PLAYING` y «▶ Leer» al terminar;
    - sin audio, ningún botón;
    - pulsar una opción o «¡Luchar!» para la narración;
    - la pantalla de fin narra `orodruin_final`.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*Narration*" --tests "*NodeScreen*" --tests "*Ending*"`. Expected: FAIL.
- [ ] **Step 3: Implement.**
  - **Reproductor:** `AndroidNarrationPlayer` abre con `assets.openFd`; `hasAudio` comprueba `assets.list("narration/<lang>")` una vez, con caché por idioma; el estado pasa a `IDLE` al completar o si hay error.
  - **Cableado:** la narración se dispara en el `LaunchedEffect(nodeKey, language)` del nodo y de la pantalla de fin, y `onLeave` se llama en `DisposableEffect`, al elegir, al luchar, en ON_STOP y al cambiar de idioma.
  - **Música:** `narrating` va a `MusicController`.
- [ ] **Step 4: Run** `./gradlew :engine:test :app:testDebugUnitTest :app:assembleDebug :app:bundleRelease`. Expected: PASS.
- [ ] **Step 5: Manual en el emulador:**
  - con un audio de prueba **temporal**, convertido con `convert.py` desde un WAV generado para la prueba y colocado en `assets/narration/es/bree_1_0.m4a`:
    - la narración arranca sola en `bree_1_0`;
    - la música baja y vuelve;
    - «Parar» funciona;
    - al elegir, se para;
  - la música suena en el menú y el mapa y se calla en combate;
  - quita el audio de prueba al terminar;
  - apunta el tamaño del AAB;
  - CLAUDE.md (Estado): una línea sobre música y narración (spec, herramientas, `narration-prompts.md`, `narracion-pendientes.md` y tamaño).
