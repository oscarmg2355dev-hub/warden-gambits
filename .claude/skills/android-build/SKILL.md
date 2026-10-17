---
name: android-build
description: Use when building, testing, installing or running the Android app (Gradle, gradlew, adb, emulator, lint, JDK/SDK setup) on this Windows machine, or when a Gradle build fails for environment reasons.
---

# Android build en este entorno (Windows)

## Entorno detectado

| Pieza | Ubicación / valor |
|---|---|
| Android SDK | `C:\Users\pakmg\AppData\Local\Android\Sdk` (`ANDROID_HOME` NO está definida) |
| Plataformas | android-35, android-36, android-37.0 |
| JDK para Gradle | JBR 21 de Android Studio: `C:\Program Files\Android\Android Studio\jbr` |
| JDK del sistema | Java 25 (NO usarlo para Gradle/AGP: provoca errores de toolchain) |
| Gradle | No hay instalación global → usar siempre el wrapper `./gradlew` |

## Reglas

1. **Siempre el wrapper**: `./gradlew` (Bash) o `.\gradlew.bat` (PowerShell). Nunca `gradle` global.
2. **JDK**: fija `JAVA_HOME` al JBR antes de invocar Gradle:
   - Bash: `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`
   - PowerShell: `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"`
   - En código: `kotlin { jvmToolchain(21) }`.
3. **SDK**: `local.properties` (no versionado) con
   `sdk.dir=C\:\\Users\\pakmg\\AppData\\Local\\Android\\Sdk`.
4. **adb** está en `$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe` (no en PATH).
5. Versiones de dependencias SOLO en `gradle/libs.versions.toml` (version catalog).

## Comandos habituales

```bash
./gradlew :engine:test                 # tests del motor (JVM puro, rápido) — el bucle principal
./gradlew :engine:test --tests "*ComboMatcherTest*"
./gradlew :app:testDebugUnitTest       # tests unitarios de la app (ViewModels)
./gradlew :app:assembleDebug           # APK debug
./gradlew :app:installDebug            # instalar en dispositivo/emulador conectado
./gradlew lint                         # Android lint
./gradlew build                        # todo (compila + tests + lint)
```

Emulador / dispositivo:

```bash
ADB="$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe"
"$ADB" devices
"$LOCALAPPDATA/Android/Sdk/emulator/emulator.exe" -list-avds
"$LOCALAPPDATA/Android/Sdk/emulator/emulator.exe" -avd <nombre> &   # en background
"$ADB" shell am start -n <applicationId>/.MainActivity
"$ADB" logcat -s Warden3:V AndroidRuntime:E
"$ADB" exec-out screencap -p > screen.png     # captura para verificar UI
```

## Diagnóstico de fallos de build

- `Unsupported class file major version 69` / errores de toolchain → se está usando Java 25; fija `JAVA_HOME` al JBR.
- `SDK location not found` → falta `local.properties`.
- Primer build lento (descarga de dependencias): usa timeout amplio (≥ 10 min) o `run_in_background`.
- Fallo real de código → aplica la skill `systematic-debugging` antes de tocar nada.

## Verificación

Antes de declarar algo terminado, ejecuta como mínimo `./gradlew :engine:test` y, si se tocó `:app`, `./gradlew :app:assembleDebug :app:testDebugUnitTest`, y lee la salida (skill `verification-before-completion`).
