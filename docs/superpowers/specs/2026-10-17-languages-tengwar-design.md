# Idiomas (español / inglés) y escrituras (latina / tengwar) — diseño

Fecha: 2026-10-17. Estado: aprobado en conversación.

Elecciones del usuario:
- números y teclas siempre en cifras normales;
- transcripción **ortográfica**, letra a letra, no fonética;
- vocales con **tehtar**.

## 1. Objetivo

El juego puede verse en dos idiomas (español, inglés) y dos escrituras (latina, tengwar), en cualquier combinación.

**Abarca todos los textos:**
- interfaz;
- ayuda;
- historia: 507 nodos con su título, texto y opciones;
- nombres de zonas, rivales y combos.

## 2. Menú y preferencias

- **Botones del menú:** dos nuevos, «Idioma: Español / English» y «Escritura: Latina / Tengwar». Cada uno alterna su valor y la interfaz se recompone al instante, sin reiniciar la Activity.
- **Guardado:** `SettingsRepository` (DataStore), claves `language` (`es` / `en`) y `script` (`latin` / `tengwar`).
- **Valores iniciales:** idioma del dispositivo (`es` si empieza por «es», si no `en`) y escritura `latin`.

## 3. Interfaz

- **Recursos:** `values/strings.xml` (es) y `values-en/strings.xml` (en), con las mismas claves. Un test comprueba que no falta ninguna clave ni sobra ninguna, y que los `%d` / `%s` coinciden.
- **Cambio de idioma dentro de la app:**
  - la raíz Compose envuelve el contenido con un `Context` configurado con el `Locale` elegido (`createConfigurationContext`) y lo provee en `LocalContext`;
  - `stringResource` lee así el idioma elegido;
  - el idioma va además en `LocalAppLanguage` para la lógica que lo necesite (contenido de la historia).
- **Textos que hoy genera `:engine`** (por ejemplo `ComboDescriber`: descripciones de la ayuda):
  - el motor expone datos (familia, tier, magnitudes, ticks);
  - la redacción se hace en la app con plantillas de recursos traducibles;
  - el motor no contiene textos de presentación nuevos;
  - los nombres de combos que hoy viven en `Combos.kt` pasan a recursos indexados por secuencia (`combo_name_1212` o un mapa por secuencia), en los dos idiomas.

## 4. Historia en inglés

- **Ficheros de traducción:** `engine/src/main/resources/story/en/<zona>.json`, uno por zona, con solo textos:
  - nombre de la zona;
  - por nodo (clave `<zona>_<nivel>_<índice>` o `orodruin_final`): `title`, `text`, `choiceLabel`;
  - por nombre de rival en español: su nombre en inglés.
- **Carga:** `StoryContentLoader.loadFromResources(language)` mezcla la traducción sobre el contenido español. Las mecánicas salen siempre del español, y el motor sigue sin saber de UI; el idioma es un dato de entrada.
- **Test:** `TranslationCoverageTest` exige cada nodo, cada opción (si el nodo la tiene), cada nombre de rival y cada zona, sin claves sobrantes y sin textos vacíos.
- **Traducción:**
  - inglés natural de calidad literaria, segunda persona del presente, tono épico-sobrio y longitud parecida (60–120 palabras; epílogo 100–160);
  - mismas reglas de canon;
  - nombres canónicos de Tolkien en inglés: Bree, Chetwood, Midgewater Marshes, Weathertop, the Last Bridge, the Trollshaws, Ford of Bruinen, Rivendell, Hollin (Eregion), Caradhras, Doors of Durin, Moria, Dimrill Dale, Nimrodel, Lothlórien, the Anduin, Sarn Gebir and the Argonath, Amon Hen, Emyn Muil, the Dead Marshes, Ithilien, Morgul Vale and Cirith Ungol, Gorgoroth, Mount Doom;
  - la hacen subagentes por lotes de zonas, con revisión.

## 5. Tengwar

- **Fuente:** **Alcarin Tengwar** (Toshi Omagari, OFL), con mapeo CSUR (U+E000–U+E07F) y colocación de tehtar por OpenType. Va en `res/font`, con su licencia en `assets/licenses/`.
- **Prueba previa (Tarea 1):** renderizar en el emulador palabras de prueba con tehtar sobre consonantes altas y bajas, portador corto, raya de doble consonante y gancho de «s».
  - Si los tehtar no se colocan bien en Android, se para y se propone una alternativa: otra fuente o tehtar como glifos precompuestos.
- **Transcriptor** (`:app`, paquete `i18n/tengwar`, Kotlin puro con tests): `fun transcribe(text: String, mode: TengwarMode): String`, con `TengwarMode { SPANISH, ENGLISH }`.
  - **Ortográfico, letra a letra:**
    - cada consonante tiene su tengwa según la tabla del modo (en el plan);
    - **vocales como tehtar sobre la consonante anterior**:
      - a: tres puntos;
      - e: acento agudo;
      - i: punto;
      - o: rizo derecho;
      - u: rizo izquierdo;
    - una vocal sin consonante delante (inicio de palabra, o tras otra vocal) va sobre el **portador corto**, y una vocal larga o acentuada (á, é, í, ó, ú) sobre el **portador largo**;
    - **dobles** (ll en inglés, rr…): raya inferior bajo la consonante, cuando la tabla del modo no le asigna un tengwa propio;
    - **«s» final:** gancho;
    - **ñ**, **ch**, **ll** (español) y **th**, **sh**, **ng**, **wh** (inglés) tienen tengwar propios.
  - **Qué no se toca:** dígitos, signos matemáticos, espacios y los tokens protegidos (los códigos de combo como `SP2`, `CU1`, `GD`, y las teclas `1`, `2`, `3`) quedan tal cual, en latina.
  - **Puntuación:** coma y punto pasan a puntuación tengwar (punto simple y doble); el resto queda igual.
  - **Mayúsculas:** se ignoran, porque los tengwar no las tienen.
- **Aplicación:**
  - un `LocalScript` (LATIN / TENGWAR) y un helper `@Composable fun tr(text: String): String` que transcribe cuando el modo es TENGWAR;
  - un estilo de texto que cambia la fuente a Alcarin;
  - en tengwar, todas las fuentes de la UI (Cinzel, Uncial, Almendra, la del sistema) se sustituyen por Alcarin, excepto en los textos que solo contienen números o tokens protegidos;
  - la transcripción se cachea por (texto, modo) para no recalcular en cada recomposición;
  - las cifras se dibujan con una fuente latina, aunque compartan línea con tengwar.

## 6. Pruebas

- **Transcriptor:** casos esperados por modo: palabras con todas las vocales, vocal inicial, vocales seguidas, dobles, «s» final, dígrafos, puntuación, tokens protegidos y números.
- **Cobertura de la traducción:** `TranslationCoverageTest` en `:engine` y el test de claves de `strings.xml` en `:app`.
- **Pantallas:** tests Compose en las 4 combinaciones de menú, nodo, ficha y combate: se muestra el texto traducido o transcrito y los números siguen en cifras.
- **Preferencias:** el cambio de idioma o escritura se guarda y se aplica.
- **Verificación:**
  - `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`;
  - capturas en el emulador en las 4 combinaciones (`docs/screenshots/v3/lang-*.png`);
  - instalación en el móvil y la tablet si están conectados.
