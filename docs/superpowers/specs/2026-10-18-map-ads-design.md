# Publicidad en el mapa (AdMob) — diseño

> **Aparcado (2026-10-21):** por decisión del usuario, los anuncios se quitan de `main` mientras la app esté en la prueba cerrada. La implementación completa está en la rama local `feature/map-ads` (commit `1f70e85`); se recupera con `git merge feature/map-ads` al pasar a producción.

Fecha: 2026-10-18. Estado: aprobado en conversación.

Elecciones del usuario:
- objetivo «prueba ahora, real después»: anuncios de prueba ya; la versión de la tienda, preparada para activar anuncios reales cuando haya cuenta de AdMob;
- un **banner fijo abajo**, solo en la pantalla del mapa;
- enfoque B: la **librería de anuncios va siempre incluida**, también en la versión de la tienda, aunque no muestre anuncios.

## 1. Objetivo

Ver cómo funciona la publicidad de Google sin estropear el juego.
- **Solo un banner discreto en el mapa:** sin vídeos, sin intersticiales y sin anuncios en el combate, los nodos o los menús.
- **Ahora:** la versión de depuración muestra anuncios de prueba de Google.
- **Más adelante:** la versión de la tienda muestra anuncios reales en cuanto se rellene `admob.properties`.

Fuera de alcance:
- otros formatos (intersticial, recompensado, nativo);
- compra para quitar anuncios;
- mediación;
- `app-ads.txt`;
- la política de privacidad publicada y los cambios en Play Console. Se documentan en §6, pero no se hacen ahora.

## 2. Configuración y librerías

- **Librerías** (solo `:app`; `:engine` no cambia):
  - `com.google.android.gms:play-services-ads`;
  - `com.google.android.ump:user-messaging-platform`;
  - última estable de cada una, en `gradle/libs.versions.toml`.
- **`admob.properties`** en la raíz del proyecto, fuera de git (se añade a `.gitignore`):
  ```properties
  appId=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
  mapBannerId=ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ
  ```
- **Resolución en `app/build.gradle.kts`**, por tipo de compilación:

  | compilación | ID de app (manifest) | ID del banner | `ADS_ENABLED` |
  |---|---|---|---|
  | debug | prueba de Google `ca-app-pub-3940256099942544~3347511713` | prueba de banner adaptativo `ca-app-pub-3940256099942544/9214589741` | `true` |
  | release con `admob.properties` | `appId` | `mapBannerId` | `true` |
  | release sin `admob.properties` | el de prueba (la librería lo exige para arrancar) | vacío | `false` |

- **Cómo llega a la app:**
  - el ID de app entra en el manifest por `manifestPlaceholders` (`com.google.android.gms.ads.APPLICATION_ID`);
  - el ID del banner y el interruptor entran como `BuildConfig.MAP_BANNER_AD_UNIT_ID` y `BuildConfig.ADS_ENABLED`;
  - se activa `buildFeatures.buildConfig`.
- **Con `ADS_ENABLED = false`:**
  - no se pide consentimiento;
  - no se inicializa `MobileAds`;
  - no se carga ni se reserva el banner.

  La app se comporta como hoy, salvo por lo que la librería añade al manifest fusionado de release: `INTERNET`, `ACCESS_NETWORK_STATE`, `AD_ID`, `ACCESS_ADSERVICES_AD_ID`, `ACCESS_ADSERVICES_ATTRIBUTION`, `ACCESS_ADSERVICES_TOPICS` (Privacy Sandbox), `WAKE_LOCK` y `FOREGROUND_SERVICE` (WorkManager). Además, `MobileAdsInitProvider` se ejecuta al arrancar el proceso aunque los anuncios estén desactivados (sin peticiones de anuncios ni AppMeasurement).

## 3. Consentimiento (RGPD)

- **`ads/UmpAdsController.kt`** (interfaz en `ads/AdsController.kt`, estado en `ads/AdsState.kt`):
  - al arrancar la Activity (y solo si `ADS_ENABLED`), llama a `ConsentInformation.requestConsentInfoUpdate` y luego a `UserMessagingPlatform.loadAndShowConsentFormIfRequired`;
  - cuando `canRequestAds()` es cierto, inicializa `MobileAds` una sola vez, fuera del hilo principal como recomienda Google;
  - expone un `StateFlow<AdsState>` con dos datos: si se pueden pedir anuncios y si hacen falta opciones de privacidad.
- **Si el usuario rechaza la personalización,** Google sirve anuncios no personalizados. La app no decide nada sobre eso.
- **Errores** (sin red, formulario no disponible): se registran con `Log.w` y no se muestran anuncios salvo que UMP ya permita pedirlos (`canRequestAds()`). El juego nunca se bloquea por esto.
- **Arranque:** `container.ads.start(activity)` se ejecuta después de `setContent` (en el primer arranque UMP necesita la vista de contenido; llamarlo antes provocaba un cierre).
- **Depuración:**
  - en compilaciones debug, `ConsentDebugSettings` puede forzar la geografía UE (`DebugGeography.DEBUG_GEOGRAPHY_EEA`) para probar el formulario;
  - lo activa una constante interna del código de depuración, apagada por defecto;
  - el dispositivo de pruebas se identifica con su hash, que la librería imprime en el log.
- **Opciones de privacidad:** en la pantalla Opciones aparece el botón «Privacidad de los anuncios» («Ad privacy» en inglés), que abre `UserMessagingPlatform.showPrivacyOptionsForm`. Solo se muestra cuando:
  - `ADS_ENABLED` está activo;
  - y `privacyOptionsRequirementStatus == REQUIRED`.

  En tengwar, el texto se transcribe como el resto de botones.

## 4. El banner en el mapa

- **`MapScreen`** gana un parámetro `bottomBar: @Composable () -> Unit = {}`, colocado debajo del mapa en la `Column`. Los tests y llamadas actuales no cambian.
- **La navegación** (`WardenNavHost`) le pasa `MapAdBanner()` cuando `ADS_ENABLED` y el consentimiento lo permite. Con anuncios activados en la compilación (`ADS_ENABLED`), `MapAdsSlot` reserva siempre la altura del banner adaptativo (la misma `adaptiveBannerHeight(anchoDp)` que usa el banner) y compone el `AdView` solo cuando `canRequestAds`; con los anuncios desactivados en la compilación no reserva nada. Motivo: el mapa no debe cambiar de altura si el consentimiento se resuelve con él abierto.
- **`ads/MapAdBanner.kt`:**
  - es un `AndroidView` con un `AdView` de banner adaptativo anclado;
  - el tamaño se calcula con `AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, anchoDp)` a partir del ancho disponible;
  - la altura se reserva desde la primera composición, aunque el anuncio aún no haya cargado. El mapa calcula sus filas de baldosas con la altura disponible y se regeneraría si cambiara, lo que tarda varios segundos.
  - Si el anuncio no carga, la franja queda con el color de fondo de la app.
  - El `AdView` se pausa, se reanuda y se destruye con el ciclo de vida de la composición (`DisposableEffect` y `LocalLifecycleOwner`).
- **Ninguna otra pantalla** muestra anuncios.

## 5. Pruebas

- **Unitarias / Robolectric:**
  - `BuildConfig` en debug: `ADS_ENABLED` es cierto y el ID del banner es el de prueba de Google;
  - `MapScreen` con `bottomBar` dibuja la ranura bajo el mapa, y sin ella el mapa ocupa lo mismo que hoy;
  - con `AdsState(canRequestAds = false)` la ranura de la navegación reserva la altura del banner sin componer el `AdView` (y con `ADS_ENABLED` desactivado no reserva nada);
  - Opciones muestra «Privacidad de los anuncios» solo con `privacyOptionsRequired = true`;
  - la paridad de `strings.xml` es/en sigue en verde.

  `AdsConsent` y `MapAdBanner` hablan con la librería de Google, así que se aíslan detrás del estado (`AdsState`) para poder probar la UI con estados falsos, sin la librería.
- **Comprobación manual:**
  - en el emulador y el Moto G85, el banner «Test Ad» aparece en el mapa y en ninguna otra pantalla;
  - forzando la geografía UE aparece el formulario de consentimiento;
  - la versión release sin `admob.properties` compila y no muestra banner.
- **Verificación:** `./gradlew :engine:test :app:testDebugUnitTest :app:assembleDebug :app:bundleRelease`.

## 6. Para activar los anuncios reales (documentado, no se hace ahora)

1. Crear la app en AdMob y un bloque de anuncio de **banner**, y copiar sus IDs a `admob.properties`.
2. Publicar una **política de privacidad**, por ejemplo `PRIVACY.md` en el repositorio de GitHub o en GitHub Pages, y poner su URL en Play Console.
3. Configurar el **mensaje de RGPD** en AdMob (Privacidad y mensajes).
4. En **Play Console:**
   - «Contiene anuncios: sí»;
   - declaración del **ID de publicidad**;
   - **seguridad de datos** con lo que recoge el SDK de anuncios (identificadores del dispositivo, datos de uso y diagnóstico).
5. Cambiar en `docs/store/descripcion.md` la sección «Sin anuncios ni conexión».
6. Subir el `versionCode` y generar el AAB.

**Aviso del enfoque B:** desde la próxima versión subida a Play, aunque aún no haya anuncios, el manifest fusionado de release incluye `INTERNET`, `ACCESS_NETWORK_STATE`, `AD_ID`, `ACCESS_ADSERVICES_AD_ID`, `ACCESS_ADSERVICES_ATTRIBUTION`, `ACCESS_ADSERVICES_TOPICS` (Privacy Sandbox), `WAKE_LOCK` y `FOREGROUND_SERVICE` (WorkManager), y `MobileAdsInitProvider` se ejecuta al arrancar el proceso aunque los anuncios estén desactivados (sin peticiones de anuncios ni AppMeasurement). Play Console pedirá la declaración del ID de publicidad y revisar la seguridad de datos en esa subida; hay que tenerlo en cuenta en ambas declaraciones.

**Recomendación para anuncios reales:** mantener un único `AdView` por sesión en lugar de una petición nueva en cada entrada al mapa.
