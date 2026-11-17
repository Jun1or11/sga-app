# Reglas del proyecto — compatibilidad obligatoria

Este proyecto debe abrir y compilar **también** en la computadora de la universidad,
que tiene **Android Studio Otter 2 Feature Drop (2025.2.2 Patch 1)** con **JDK 21**.

> Si una instrucción del usuario choca con estas reglas, pregunta antes de romperlas.

## Versiones BLOQUEADAS (no cambiar sin autorización explícita)

| Componente | Versión permitida |
|---|---|
| Gradle (wrapper) | **8.13** |
| Android Gradle Plugin (AGP) | **8.13.2** |
| JDK para Gradle / Project SDK | **21** (`jbr-21`) |
| compileSdk | **36** |
| targetSdk | **36** |
| minSdk | **24** |
| Java source/target | **11** |
| Dependencias | las definidas en `gradle/libs.versions.toml` |

## Prohibido

- Subir Gradle a **9.x** o AGP a **9.x**.
- Subir `compileSdk` / `targetSdk` a **37** o más.
- Cambiar el `distributionUrl` del wrapper o volver a agregar `distributionSha256Sum`
  de otra versión.
- Crear/restaurar `gradle/gradle-daemon-jvm.properties` con `toolchainVersion` distinto de 21.
- Volver a agregar el plugin `org.gradle.toolchains.foojay-resolver-convention`.
- Activar `org.gradle.configuration-cache` (puede romper tareas de AGP 8.13).
- Usar DSL exclusivo de AGP 9:
  - `compileSdk { version = release(...) }` → usar `compileSdk = 36`
  - `buildTypes.release.optimization { ... }` → usar `isMinifyEnabled = false`
  - `android.experimentalProperties[...]` en `gradle.properties`
- Actualizar versiones de CameraX, ML Kit, AndroidX, Material, etc. sin permiso.
- Cambiar el JDK del proyecto a 25 (Android Studio 2026 lo intenta por defecto).

## Permitido sin pedir permiso

- Cambios de código Java, layouts, recursos, colores, textos y lógica de la app.
- Correcciones de bugs que no toquen la configuración de build.
- Agregar tests.

## Antes de tocar la configuración de build

1. Verifica que el cambio sea compatible con **Gradle 8.13 + AGP 8.13.2 + JDK 21 + SDK 36**.
2. Si de verdad necesitas subir una versión, **pregunta primero** y explica el motivo.
3. Después de cualquier cambio de build, compila con:
   ```powershell
   $env:JAVA_HOME="$env:USERPROFILE\.jdks\jbr-21.0.11"
   .\gradlew.bat :app:assembleDebug
   ```

## Notas

- `gradle/wrapper/gradle-wrapper.jar` ya corresponde a Gradle 8.13; no reemplazarlo.
- `.gradle/config.properties` es local (no se versiona) y apunta al JDK 21 de cada PC.
- `.idea/gradle.xml` usa `#GRADLE_LOCAL_JAVA_HOME`, igual que el proyecto T2 que sí abre en la universidad.
