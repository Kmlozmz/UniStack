# BUILD — compilar UniStack

Resumen operativo de `COMPILAR.md` (más largo, con recetas) más lo aprendido después. Todos los
comandos están verificados en este repo. **No hay comandos inventados.**

## Requisitos (ya instalados en la máquina del desarrollador)

| Pieza | Dónde | Comprobar |
|---|---|---|
| JDK 21 (Temurin) | `C:\Program Files\Eclipse Adoptium\jdk-21…`, en `PATH` y `JAVA_HOME` | `java -version` |
| Android SDK | **dentro del repo**: `.toolchains/android-sdk-windows` (ignorado por git) | `sdk.dir=` en `local.properties` |
| Firma de release | `.signing/unistack-release.jks` + cuatro claves `release*` en `local.properties` | — |
| Bot de Telegram | `.env` → `TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHAT_ID` | `Get-Content .env` |
| Token de GitHub | `.env` → `GITHUB_TOKEN` (sólo para publicar) | — |
| Google (opcional) | `googleWebClientId` en `local.properties`; **no hay `google-services.json`** | — |
| curl | viene con Windows | — |

No hace falta Android Studio. **Un clon nuevo no compila release** hasta copiar `.jks`, `.env`
y `local.properties`; `assembleDebug` sí compila (firma de debug de reserva `VERIFY`).

Versiones: AGP 8.13.2 · Gradle 8.14.5 (wrapper) · Kotlin 2.2.21 · compileSdk 36 · targetSdk 35
· minSdk 26 · JVM target 17.

## Dónde

Siempre desde la raíz (donde está `gradlew`): `C:\Users\Lenovo\.claude\scratch\UniStack`.
En PowerShell, `./gradlew` (no `gradlew` a secas). **Entrecomillar siempre `-PversionName`**:

```bash
./gradlew sendAlpha "-PversionName=1.6.0-alpha.96"
```

## Comandos

| Tarea | Qué hace | Bot | Publica |
|---|---|:--:|:--:|
| `./gradlew -q compileDebugKotlin` | compila Kotlin (rápido, para iterar) | no | no |
| `./gradlew assembleDebug` / `installDebug` | APK `dev` (versionCode 1), instala en emulador/móvil | no | no |
| `./gradlew assembleRelease "-PversionName=X"` | APK firmado | **sí** | no |
| `./gradlew sendAlpha "-PversionName=X.Y.Z-alpha.N"` | **el del día a día**: compila release y lo manda | **sí** | no |
| `./gradlew publishReleaseToGitHub "-PversionName=X.Y.Z[-beta.N]"` | release en `UniStack-releases` + APK | sí¹ | **sí** |
| `./gradlew -q testDebugUnitTest` | 534 tests JVM | — | — |
| `./gradlew verifyDesignTokens` | falla con colores a pelo | — | — |
| `./gradlew check` | tests + tokens + lint | — | — |
| `./gradlew --stop` / `--status` | parar / ver daemons | — | — |

¹ vía `assembleRelease`.

Banderas: `-PskipTelegramApk=true` (no enviar), `-PapkType=x` (pie del mensaje),
`-Pprerelease=false`, `-PversionCode=N` (no usar). Útiles: `--console=plain`, `--stacktrace`, `-q`, `--offline`.

## Números de versión

`versionCode = mayor×10.000.000 + menor×100.000 + parche×1.000 + peldaño`
(dev = 0 → código 1; alpha = 100+N; beta = 300+N; rc = 600+N; estable = 999). Ejemplos:
`1.6.0-alpha.96 → 10.600.196`, `1.6.0-beta.1 → 10.600.301`, `1.6.0 → 10.600.999`.
Publicada hoy: `1.5.10 → 10.510.999`. Todo lo que él instale debe ir por encima: serie actual
**`1.6.0-alpha.N`**, siguiente **`.96`**. Tope 99 por peldaño (pasarse rompe la compilación).

## Dónde queda

- APK: `app/build/outputs/apk/release/UniStack-<versión>.apk` (debug en `.../debug/` con sello).
- Contador del pie del bot: `.gradle/telegram-apk-counters.properties` (alpha=99 al 11 sep).
- Informe de fallos: `build/reports/problems/problems-report.html`.
- Resultados de tests: `app/build/test-results/testDebugUnitTest/`.

## Errores conocidos y solución

| Error | Causa | Solución | No hacer |
|---|---|---|---|
| `Task '.6.0-alpha.1' not found` | PowerShell partió el `-P` | comillas en `"-PversionName=…"` | — |
| `Couldn't delete …\R.jar` | dos compilaciones a la vez | esperar o `./gradlew --stop` y repetir | lanzar otra en paralelo |
| `lintAnalyzeDebugUnitTest` falla en `check` la 1ª vez | entorno | repetir `check` sin tocar nada | «arreglar» código |
| `sendAlpha solo manda alphas, y X no lo es` | nombre sin `-alpha.N` | usar `assembleRelease` para beta | — |
| `CHANGELOG.md no tiene seccion para X` | falta `## [X] — fecha` | escribirla | publicar sin notas |
| `TELEGRAM_BOT_TOKEN and TELEGRAM_CHAT_ID must be set` / `GITHUB_TOKEN no configurado` | `.env` vacío | reponerlo | commitear `.env` |
| `El release esta firmado con credenciales de desarrollo` | falta `.jks` o claves | reponer `local.properties` | — |
| `SDK location not found` | `sdk.dir` mal | apuntar a `.toolchains/android-sdk-windows` | — |
| `la iteración pasa de 99` | alpha.100 | subir parche (`1.6.1-alpha.1`) | — |
| `Textos sin proveedor` (tests) | Robolectric sin `Textos` | `TextosDePrueba.instalar()` en `@Before` | — |
| `Textos sin proveedor` (app, al abrir) | `Textos.desde` después de `super.onCreate()` | debe ir **antes** (Hilt construye el canal de avisos en un constructor) | — |
| «aplicación no instalada» en el móvil | versionCode baja (dev sobre alpha, alpha sobre estable) | desinstalar (pierde datos) o usar número mayor | repetir números |
| Unused `isEn` / imports | restos de migraciones | limpiar cuando se toque el fichero | — |
| `AssertionError: ya existe <clave>` en scripts i18n | la clave ya estaba en el XML | usar `i18n_recuperar_cadenas.py` (añade sólo las que faltan) | relanzar el script entero |

## Intentos que fallaron (build)

- Quitar el peldaño `dev` (13 ago): cada prueba exigía una alpha pública; revertido el mismo día.
- Peldaño de dos dígitos en el `versionCode` (hasta 20 ago): 14 alphas con el mismo número.
- `versionCode` desde el reloj: ordenaba por hora de compilación; se pasó a la fórmula.
- Filtrar el bot sólo a alphas (hasta 21 ago): él tenía que buscar el APK a mano.
- Mensajes «cooking…/completed!» del bot (hasta 28 ago): tapaban el APK.

## Tiempo de compilación orientativo

`compileDebugKotlin` ~1–2 min incremental; `testDebugUnitTest` ~40 s; `sendAlpha` ~7 min
(minificado + firma + envío). El timeout de herramienta debe ser ≥ 10 min para `sendAlpha`.
