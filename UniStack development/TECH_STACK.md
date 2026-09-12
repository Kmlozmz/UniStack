# TECH_STACK — tecnologías, por qué y con qué cuidado

Versiones tomadas de `app/build.gradle.kts`, `build.gradle.kts` y `gradle-wrapper.properties`
el 12 sep 2026.

| Tecnología | Versión | Dónde | Por qué | Cuidado |
|---|---|---|---|---|
| **Kotlin** | 2.2.21 (JVM target 17) | todo | Lenguaje nativo de Android moderno | — |
| **Jetpack Compose** | BOM 2026.05.01 | toda la UI; no hay XML de vistas | UI declarativa, única forma de usar M3 Expressive | `stringResource` sólo en composables |
| **Material 3** | `material3:1.5.0-alpha15` **clavado** | `core/design` y pantallas | Es la versión que trae **todos** los componentes M3 Expressive necesarios (barra corta, wavy progress, ButtonGroup conectado, LoadingIndicator, SplitButton, FloatingToolbar, MaterialShapes…), verificado por `javap` el 22 ago | Subir de alpha exige AGP 9.1 + compileSdk 37; **no hace falta** para ningún componente. `UniStackExpressive.kt` es el único fichero que nombra Material para absorber renombres |
| `material-icons-extended` | 1.7.8 | iconos | Catálogo completo de Material Icons | — |
| `graphics-shapes` (vía material3) | — | `BadgeShapes`, galleta de Gastos, sello | Polígonos redondeados y morphing | — |
| **Hilt** | 2.56.2 (+ `hilt-navigation-compose` 1.2.0) | DI: Application, ViewModels, módulos en `core/di` | Estándar de Android; `hiltViewModel()` en pantallas | Usa **kapt** (no KSP) para Hilt; KSP para Room |
| **Room** | 2.8.4 | `feature_*/data/local`, `UniStackDatabase` | Base local con migraciones explícitas; `exportSchema = true` a `app/schemas` | Cada cambio de esquema = migración escrita a mano + `RoomMigrationTest`. Nunca `fallbackToDestructiveMigration` |
| **DataStore Preferences** | 1.2.1 | `UserPreferencesDataSource` | Perfil y preferencias como flujo | ~137 claves; cada preferencia nueva es clave + mapeo + respaldo |
| **WorkManager** | 2.10.0 | `UpdateCheckWorker` | Comprobar actualizaciones cada 2 h | Falla a propósito sin red para que reintente; **no** propagar esa excepción al `launch` de la app (ya cerró el proceso una vez) |
| **Navigation Compose** | 2.9.8 | `AppNavGraph` | Un grafo, rutas string con argumentos | Empuje propio en `NavigationMotion.kt`; `zIndex` por destino |
| **Lifecycle** | 2.10.0 | ViewModels, `collectAsStateWithLifecycle` | — | — |
| **Activity Compose** | 1.13.0 | `MainActivity` | edge-to-edge | Desde targetSdk 35 la ventana no se redimensiona con el teclado: cada pantalla se aparta sola |
| **Coroutines** | 1.11.0 (+ play-services) | todo | — | — |
| **Coil** | 2.7.0 | fotos de perfil, adjuntos de notas | Carga de imágenes en Compose | — |
| **Firebase** | BOM 34.14.1 (auth, firestore) | `FirebaseGoogleAuthService`, `FirebaseCloudBackupRepository` | Identidad + respaldo en la nube, cuenta opcional (decidido) | **Desconectado**: no hay `google-services.json`; `GOOGLE_WEB_CLIENT_ID` sale de `local.properties` |
| **Credential Manager** | 1.6.0 (+ play-services-auth) | inicio de sesión con Google | API moderna | idem |
| **AGP** | 8.13.2 · **Gradle** 8.14.5 · **JDK** 21 (Temurin) | build | — | `compileSdk 36`, **`targetSdk 35` a propósito**, `minSdk 26` |
| **KSP** | 2.2.21-2.0.5 | Room | Procesador de anotaciones rápido | — |
| **JUnit 4 + Robolectric 4.16.1 + room-testing + coroutines-test** | — | `app/src/test` (55 ficheros, 534 tests) | Tests JVM; Robolectric para Room y contexto | Robolectric con `manifest = NONE` no instala `Textos`: `TextosDePrueba.instalar()` en `@Before` |
| Compose UI test (androidTest) | — | 3 ficheros | Instrumentados, apenas usados | `VERIFY` si pasan |
| **Telegram Bot API** (curl) | — | tareas Gradle `sendReleaseApkToTelegram` | Entregar APK al desarrollador | Token y chat en `.env` |
| **GitHub REST API** | — | publicar releases (Gradle) y actualizador (app) | Distribución pública | Repo público aparte para releases |

## Alternativas descartadas (discutidas)

- **Dibujar el logo por código** en vez del PNG: rechazado (el asset es la marca).
- **Firebase App Distribution / proxy propio** para cerrar descargas: descartado, los APK son públicos.
- **Token de GitHub embebido en el APK** para leer releases de un repo privado: descartado
  (se extrae trivialmente; se creó el repo público de releases).
- **`fallbackToDestructiveMigration`**: nunca; los datos son del estudiante.
- **`LinearProgressIndicator` para barras de avance de notas**: descartado (pinta un punto al
  final de la pista que flota con la barra a cero); se usa `EvaluationBar` propia.
- **Google Play**: `UNKNOWN`; nunca se ha discutido publicar en la tienda. Hoy el permiso de
  «origen desconocido» forma parte del flujo (FAQ lo explica).

## Restricciones importantes

- **No subir `targetSdk` a 36** sin aceptar el gesto atrás predictivo (Android 16 lo fuerza).
- **No subir material3 de alpha** sin la cadena completa (AGP 9.1, compileSdk 37); y no hace falta.
- **Firma**: debug y release comparten clave (`debug { signingConfig = localRelease }`), así los
  APK se instalan uno sobre otro sin perder datos.
- **`versionCode` sale del nombre** (`mayor×10⁷ + menor×10⁵ + parche×10³ + peldaño`); ninguna
  versión nueva puede bajar de número. `VersionCodeOrderTest` duplica la fórmula a propósito.
- **`CHANGELOG.md` obligatorio** para publicar (la tarea falla sin la sección exacta).
- **Secretos fuera del repo**: `.env`, `.signing/`, `local.properties`, `.toolchains/`.
