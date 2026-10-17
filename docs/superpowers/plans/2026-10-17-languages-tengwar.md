# Idiomas y tengwar — plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:** dos idiomas (es, en) y dos escrituras (latina, tengwar) para todos los textos, con dos botones en el menú.

**Architecture:**
- **Interfaz:** recursos Android por idioma, con un `Context` local en Compose.
- **Historia:** traducciones de la historia en JSON superpuestos, cargados por idioma.
- **Tengwar:** un transcriptor ortográfico en Kotlin y la fuente Alcarin Tengwar aplicada con un `CompositionLocal`.

**Spec:** `docs/superpowers/specs/2026-10-17-languages-tengwar-design.md`

## Global Constraints

- **Sin git:** se omiten los pasos de commit.
- **Entorno:** `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`.
- **Idiomas:** código en inglés.
- **Finales de línea:** se conservan (`strings.xml` es CRLF).
- **JSON:** UTF-8 sin BOM.
- **`:engine`:** sin dependencias nuevas ni textos de presentación nuevos; el idioma entra como dato.
- **Qué no se transcribe a tengwar:** números, teclas `1`, `2`, `3` y códigos de combo (`SP1`…`GD`, `PO`).
- **Tengwar:** transcripción **ortográfica**, letra a letra; vocales como **tehtar** sobre la consonante anterior, o sobre el portador corto si no hay consonante delante; vocal acentuada sobre el portador largo; gancho para la «s» final; raya inferior para las dobles sin tengwa propio; dígrafos con tengwa propio.
- **Fuente tengwar:** Alcarin Tengwar (OFL, mapeo CSUR), con su licencia en `app/src/main/assets/licenses/`.
- **Inglés:** nombres canónicos de Tolkien (spec §4); calidad literaria; mismas longitudes y reglas de canon.

## Review Focus

1. **Cambiar de idioma en mitad de un combate o de un nodo:** la pantalla se recompone en el idioma nuevo sin perder el estado. Test en la Tarea 3.
2. **Texto con números y códigos mezclados** («−1840 SP2», «Nv 34 · ATQ 3400»): en tengwar, solo las palabras cambian. Test en las tareas 2 y 6.
3. **Cadenas de recursos con formato** (`%d`, `%1$s`): las dos traducciones tienen los mismos argumentos. Test en la Tarea 4.
4. **Rival con nombre sin traducción:** el test de cobertura lo impide; en ejecución se usaría el nombre en español. Test en la Tarea 5.
5. **Rendimiento:** con la transcripción cacheada, no hay tirones en el combate. Comprobación en la Tarea 6.

---

### Task 1: Prueba de la fuente Alcarin Tengwar (go/no-go)

**Files:**
- Create: `app/src/main/res/font/alcarin_tengwar.ttf` y su licencia (desde el repositorio oficial del autor, OFL).
- Create: una pantalla o un test de depuración temporal que pinte muestras.

- [ ] **Step 1:** descarga la fuente (TTF, release oficial con OFL). Verifica la licencia y el mapeo CSUR: tinco U+E000 … y tehtar en U+E040…, según la documentación de la fuente.
- [ ] **Step 2:** en el emulador, con un composable temporal, pinta:
  - palabras con tehtar sobre tengwar altos (tinco, parma) y bajos (anto, ampa) y sobre el portador corto y el largo;
  - la raya inferior;
  - el gancho de «s».

  Saca una captura en `.superpowers/sdd/languages/tengwar-spike.png`.
- [ ] **Step 3: decisión.** Si los tehtar se colocan bien gracias al GPOS de la fuente, GO. Si no, NO-GO: informa con la captura y propón alternativas. No sigas.
- [ ] **Step 4:** quita la pantalla temporal y deja la fuente y la licencia.

### Task 2: Transcriptor tengwar (Kotlin puro)

**Files:**
- Create: `app/src/main/java/com/wardengambits/app/i18n/tengwar/{TengwarMode.kt, TengwarTables.kt, Tengwar.kt}`
- Create: `app/src/test/.../i18n/tengwar/TengwarTest.kt`

**Interfaces:**
- Produces:
  - `enum class TengwarMode { SPANISH, ENGLISH }`;
  - `object Tengwar { fun transcribe(text: String, mode: TengwarMode): String }`, con caché LRU interna (sincronizada) por (texto, modo).
- **Tablas:** las define el implementador con la documentación de Alcarin y los modos ortográficos habituales. En español: c/qu/k, g/gu, j, ch, ll, ñ, rr, y, h muda (ortográfico: tiene tengwa propio, hyarmen), v/b, z. En inglés: th, sh, ch, ng, wh, ph, gh, y, w, c, q, x. Documentadas en KDoc con una tabla.

- [ ] **Step 1: Write the failing tests**, con resultados esperados escritos en códigos CSUR o con constantes con nombre:
  - una vocal sobre la consonante anterior;
  - una vocal inicial sobre el portador;
  - vocales seguidas;
  - vocal acentuada sobre el portador largo;
  - dobles;
  - «s» final;
  - dígrafos (ch, ll, ñ, rr / th, sh, ng);
  - puntuación;
  - mayúsculas ignoradas;
  - dígitos, `SP2`, `CU1`, `GD`, «1», «2», «3» intactos;
  - una frase mixta («−1840 SP2», «Nivel 34»).
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests "*TengwarTest*"`. Expected: FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run.** Expected: PASS.

### Task 3: Infraestructura de idioma y escritura, y botones del menú

**Files:**
- Modify:
  - `data/SettingsRepository.kt` (claves `language` y `script`)
  - `ui/menu/MenuScreen.kt`: el botón «Sonido» sale del menú principal y entra un botón «Opciones», que abre una pantalla nueva (ruta `options`) con «Sonido: sí / no», «Idioma: Español / English» y «Escritura: Latina / Tengwar». Debajo va una **tabla de equivalencias latina → tengwar** del modo del idioma elegido: letras, dígrafos y vocales (tehtar sobre un portador y sobre una consonante de ejemplo), con la letra en latina y su tengwa al lado. Las filas salen de las tablas del transcriptor (`TengwarTables`), no se escriben a mano.
  - la raíz Compose (`MainActivity` / `WardenNavHost` / `WardenTheme`: `LocalContext` con el locale, `LocalAppLanguage`, `LocalScript`)
  - `ui/theme/Type.kt` (familia Alcarin y estilos que cambian según `LocalScript`)
- Create: `app/.../i18n/{AppLanguage.kt, AppScript.kt, Tr.kt}`, con `@Composable fun tr(text: String): String` y `@Composable fun trStringResource(id, args)`.
- Tests: preferencias guardadas y valores iniciales; en Opciones, pulsar los botones cambia los textos (es ↔ en) y la escritura (latina ↔ tengwar); el sonido sigue funcionando desde Opciones; la tabla de equivalencias muestra todas las letras del modo y cambia al cambiar de idioma; cambiar de idioma en mitad de un nodo no pierde el estado.

- [ ] **Step 1: Write the failing tests.**
- [ ] **Step 2: Run.** Expected: FAIL.
- [ ] **Step 3: Implement.** Todas las llamadas de texto visibles de la app (`Text(stringResource(...))` y los textos que vienen de UiState) pasan por `tr(...)`, y los estilos se resuelven según `LocalScript`. Los textos solo numéricos o con tokens protegidos no cambian, porque el transcriptor ya los respeta.
- [ ] **Step 4: Run** `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.

### Task 4: Interfaz en inglés y textos del motor en la app

**Files:**
- Create: `app/src/main/res/values-en/strings.xml`
- Modify:
  - el `ComboDescriber` (y cualquier texto de presentación de `:engine`) para que exponga datos
  - plantillas en `strings.xml` (es) y `values-en` (en)
  - los nombres de combos como recursos por secuencia en los dos idiomas
- Tests: `StringResourcesParityTest` (mismas claves y mismos marcadores de formato en es y en); la ayuda de combos se muestra en inglés con los números correctos.

- [ ] **Step 1: Write the failing tests.**
- [ ] **Step 2: Run.** Expected: FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** `./gradlew :engine:test :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.

### Task 5: Historia en inglés (carga, cobertura y traducción por lotes)

**Files:**
- Modify: `engine/.../story/content/StoryContentLoader.kt` (`loadFromResources(language: String = "es")`, mezcla la traducción) y el uso de la app (`GameSession` o donde se cargue, recargando el contenido al cambiar de idioma).
- Create:
  - `engine/src/test/.../story/TranslationCoverageTest.kt`
  - `engine/src/main/resources/story/en/<zona>.json` (24 ficheros)

**Lotes de traducción:** cada uno es un subagente con revisión de calidad y canon.
- 5a: bree … quebradas;
- 5b: bruinen … nimrodel;
- 5c: lothlorien … orodruin.

- [ ] **Step 1: Write the failing tests.** Cobertura (cada nodo, opción, rival y zona; nada sobrante ni vacío); la carga en inglés devuelve los textos traducidos y las mismas mecánicas que en español.
- [ ] **Step 2:** implementa la carga y crea los 24 ficheros, lote a lote. Cada lote valida con `TranslationCoverageTest` sus zonas.
- [ ] **Step 3: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest`. Expected: PASS.

### Task 6: Tengwar en toda la app, verificación e instalación

- [ ] **Step 1:** tests Compose en las 4 combinaciones de menú, nodo, ficha, crónica, ayuda y combate:
  - en tengwar, el texto visible es el transcrito y los números y códigos siguen en latina;
  - en inglés se ve el texto traducido.
- [ ] **Step 2: Run** `./gradlew :engine:test :sim:test :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.
- [ ] **Step 3:** saca capturas en el emulador de las 4 combinaciones (`docs/screenshots/v3/lang-{es,en}-{latin,tengwar}.png`).
- [ ] **Step 4:** instala en el móvil y la tablet si están conectados (`install -r`) y actualiza `CLAUDE.md`.

### Task 7: Pantalla «Acerca de»

**Files:**
- Modify: `ui/menu/MenuScreen.kt` (botón «Acerca de»), `navigation/WardenNavHost.kt` (ruta `about`)
- Create: `ui/about/AboutScreen.kt`
- Modify: textos en `strings.xml` (es) y `values-en` (en)

**Contenido** (provisional, en los dos idiomas, en pergamino como la ficha):
- **Cómo se ha hecho:** juego nativo para Android en Kotlin y Jetpack Compose, sin motor gráfico. Un motor de combate por combos puro y determinista, un simulador para el balance, la historia escrita como datos (24 zonas, 507 nodos), imágenes pixel-art generadas con Gemini a partir de prompts propios, el mapa generado por código y los sonidos sintetizados. Desarrollo asistido por IA (Claude Code) con un flujo de specs, planes, tests y revisiones.
- **El autor:** Oscar Martínez Gómez, programador, aficionado al mundo de Tolkien y jugador retirado de El Señor de los Anillos Online (LOTRO), donde nació la idea del Warden (el Guardián) y sus combos (gambits).
- **Créditos:** fuentes (Cinzel, Uncial Antiqua, Almendra y Alcarin Tengwar, licencias OFL) y un aviso de que es un proyecto de aficionado sin relación con los titulares de los derechos de Tolkien.

- [ ] **Step 1: Write the failing tests:** el menú tiene «Acerca de» y abre la pantalla, que muestra el nombre del autor y los créditos; se ve en inglés con el idioma inglés y en tengwar con la escritura tengwar.
- [ ] **Step 2: Implement.**
- [ ] **Step 3: Run** `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Expected: PASS.
