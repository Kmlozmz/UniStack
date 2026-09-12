# ARCHITECTURE — cómo está construido

Todo lo de aquí está verificado en el código al 12 sep 2026. No hay backend propio: la
«arquitectura» es la de una app Android local con dos integraciones externas (GitHub Releases
para actualizarse; Firebase, presente en código pero desconectado).

## Forma general

- **Un solo módulo Gradle** (`:app`), un solo `Application` (`UniStackApplication`, Hilt),
  una sola `Activity` (`MainActivity`) que aloja **toda la navegación con Navigation Compose**
  (`core/navigation/AppNavGraph.kt`, ~1.500 líneas).
- **Organización por features** (paquetes, no módulos): `feature_home`, `feature_grades`,
  `feature_tasks`, `feature_schedule`, `feature_terms`, `feature_notes`, `feature_expenses`,
  `feature_profile` (ajustes), `feature_setup` (onboarding), `feature_support`,
  `feature_templates`, `feature_updates`, `feature_user`, `feature_sync`, `feature_notifications`.
- Cada feature sigue **domain / data / presentation** cuando tiene datos:
  - `domain`: modelos Kotlin puros (`Subject`, `StudentTask`, `AcademicTerm`…), interfaz del
    repositorio, reglas (`GradeCalculator`, `TermCloseCheck`, `SubjectAttendanceHistory`,
    `NoteMarkdown`, `ExpenseInsights`, `ReminderTiming`).
  - `data`: `Room*Repository` (implementación real) y, en varias, `InMemory*Repository` para
    tests; `data/local` con `*Entity`, `*Dao` y mapeos.
  - `presentation`: pantallas Compose + `*ViewModel` (Hilt) + piezas (`*Pieces.kt`, `*Sheet.kt`).
- `core/`: `design` (tema, componentes propios, motion), `navigation`, `notifications`,
  `datastore`, `di`, `utils`.

## Capas y comunicación

```
Compose screen ──collectAsStateWithLifecycle──▶ ViewModel (Hilt) ──▶ Repository (interfaz, domain)
                                                    │                       │
                                                    │                 Room*Repository (data)
                                                    │                       │
                                                    ▼                  Room DAO / DataStore
                                          StateFlow<UiState> ◀── Flow<List<Entity>> mapeados a domain
```

- Los repositorios exponen `StateFlow` de modelos de dominio (`stateIn(scope, Eagerly)`); las
  escrituras son funciones **no suspendidas** que lanzan la operación en un `scope` propio del
  repositorio (`scope.launch { dao.insert(...) }`): «fire and forget». Consecuencia: quien
  escribe no espera al disco; los tests de Room usan `runTest`/`advanceUntilIdle` o el flujo
  para observar el resultado.
- Los ViewModels combinan flujos (`combine`) en un `uiState`; el Home añade un ticker por
  minuto para «clase en curso».
- **Sin capa de casos de uso**: la lógica de negocio vive en objetos de dominio puros y
  testeables (`GradeCalculator`, `DailyPriorityEngine`, `HomeSummaryFactory`, `TermCloseCheck`).
- **Inyección**: `core/di/AppModule` (DataStore, base Room y todos los DAO), `RepositoriesModule`
  (interfaz → `Room*Repository`), `ServicesModule` (auth de Google, repositorio de
  actualizaciones), `UniStackEntryPoint` para receptores y workers fuera de Hilt.

## Estado y preferencias

- **Room** (`unistack.db`, v21): todo lo que es dato del estudiante. Ver [`DATABASE.md`](DATABASE.md).
- **DataStore Preferences** (`UserPreferencesDataSource`, ~137 claves): el `UserProfile` entero
  (nombre, programa, escala, meta, módulos, apariencia, accesibilidad, notificaciones,
  presupuesto, preferencias de notas, esquema de cortes, etc.). `UserRepository` lo expone como
  `Flow<UserProfile?>`.
- **SharedPreferences** puntuales: idioma persistido (`LocaleHelper`), estado del actualizador,
  contadores locales. `QuickNotesStore` (la hoja vieja de notas) ya se migró a Room.
- **Estado de UI** en `remember`/`rememberSaveable` dentro de la pantalla; sin librería de
  estado adicional.

## Navegación

- `AppRoutes` (`core/navigation/BottomNavItem.kt`) declara todas las rutas como constantes con
  argumentos en la ruta (`subject_detail/{subjectId}`, `academic?tab=…`, `new_note?start=…`).
- `AppNavGraph` registra cada destino con `screen()` (capa `zIndex` por profundidad para que el
  empuje se lea bien) y aplica el **empuje estilo iOS** (`NavigationMotion.kt`: entra entera
  desde el borde, la anterior se aparta un tercio, curva `(0.32, 0.72, 0, 1)`, 300 ms).
- Barra inferior M3E (`ShortNavigationBar`) con cuatro pestañas; se esconde en rutas
  «inmersivas» (cajón) y en formularios (`rememberLeaveGuard` avisa antes de descartar).
- `navigateIfModuleEnabled` respeta los módulos apagados. `grades` y `tasks` existen sólo como
  redirección para alarmas antiguas.
- Deep links internos desde notificaciones: `AttendanceDeepLink` (extras en `MainActivity`).

## Notificaciones

- `LocalReminderScheduler` programa **alarmas exactas** (`USE_EXACT_ALARM`) para entregas,
  clases, resumen diario, asistencia post-clase (20 min), notas rápidas; ventana de 48 h con
  rearmado a las 3:00; `BootReceiver` reprograma tras reinicio.
- `ReminderCoordinator` observa los repositorios y reprograma; `ReminderTiming` contiene las
  reglas (13 tests). Canales: `alerts` (IMPORTANCE_HIGH) y `digest`; el actualizador tiene el suyo.
- `AttendanceActionReceiver` procesa los botones Asistí/Falta de la notificación.
- `NotificationHistoryStore` guarda lo enviado para la pantalla Notificaciones.

## Actualizaciones (OTA propia)

- `GitHubReleaseUpdateRepository`: consulta `https://api.github.com/repos/Kmlozmz/UniStack-releases/releases`
  (no `/latest`, para incluir preestrenos), compara nombres con `ReleaseVersion.parse` (trozo a
  trozo, números como números), descarga el APK, verifica SHA-256 si está publicado, lanza el
  instalador vía `FileProvider`. `UpdateCheckWorker` (WorkManager, cada 2 h) + comprobación al
  abrir si pasaron 45 min. Ver [`API.md`](API.md).

## Respaldos

- `LocalJsonBackupRepository`: exporta/importa un JSON con perfil, materias, notas, tareas,
  gastos, periodos, notas rápidas (ficha de adjuntos, no bytes). Vista previa antes de restaurar.
- `FirebaseCloudBackupRepository`: documento de Firestore por usuario (tope 1 MB). **No
  operativo** sin `google-services.json`. `FirebaseGoogleAuthService` idem (Credential Manager).

## Internacionalización

- `strings.xml` (es) y `values-en/strings.xml`, 2.733 claves cada uno.
- `core/utils/Textos`: proveedor global para código no-Compose (`Textos.get(id, args)`,
  `Textos.lista(arrayId)`), resuelve con el idioma **elegido en la app** (`AppLanguage`), no el
  del sistema, creando un contexto de configuración. Se instala en `UniStackApplication.onCreate()`
  antes de `super.onCreate()`. Los tests instalan `TextosDePrueba` que lee los XML reales.
- `LocaleHelper` aplica el idioma a la Activity.

## Build, firma y distribución

- `app/build.gradle.kts` (~1.040 líneas) contiene, además de lo normal: `versionCodeFor()`
  (fórmula de la escalera), firma de release desde `local.properties` (`.signing/*.jks`, debug
  comparte firma), tareas `sendAlpha`, `sendReleaseApkToTelegram`, `publishReleaseToGitHub`,
  `verifyDesignTokens` (falla con `Color(0x…)` a pelo fuera del sistema de diseño), inyección de
  `BuildConfig.GITHUB_REPO`, `GOOGLE_WEB_CLIENT_ID`, lectura de `.env`.
- Ver [`BUILD.md`](BUILD.md) y [`DEVELOPMENT_WORKFLOW.md`](DEVELOPMENT_WORKFLOW.md).

## Estructura de carpetas (raíz)

```
UniStack/
├── app/                      # el único módulo
│   ├── build.gradle.kts
│   ├── schemas/              # esquemas exportados de Room (exportSchema = true)
│   └── src/main|test|androidTest/
├── design-system/unistack/MASTER.md   # OBSOLETO (teal, 6 ago)
├── docs/changelog-historico.md
├── scripts/send_apk.sh
├── CHANGELOG.md · COMPILAR.md · PUBLICAR.md · CONTEXTO.md
├── UniStack development/     # esta carpeta
├── .toolchains/android-sdk-windows   # SDK dentro del repo (ignorado por git)
├── .signing/ · .env · local.properties  # secretos, ignorados
└── .claude/                  # ignorado
```

## Infraestructura y despliegue

- Sin servidores. Distribución: APK firmado por el bot de Telegram (alphas) y por GitHub
  Releases del repo público (beta/estable). Landing en `Kmlozmz/UniStack-landing_page` (privado,
  `VERIFY` si está desplegada).
- Sin CI. Decidido GitHub Actions para v1.0.0 (ver `ROADMAP.md`).

## OPEN QUESTIONS
- ¿Se mantendrá un solo módulo Gradle al crecer? Nunca se ha discutido (`UNKNOWN`); hoy no
  duele y partir en módulos no está en la lista.
- Si las escrituras «fire and forget» deberían pasar a `suspend` para poder encadenar y
  reportar fallos: nunca se ha discutido (`UNKNOWN`); hoy funciona y no está en la lista.
