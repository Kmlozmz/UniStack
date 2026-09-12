# CODING_RULES — cómo se escribe código en UniStack

Reglas concretas, verificadas en el repo. Cuando dudes, abre un fichero parecido de la misma
feature y copia su forma.

## Lenguaje y estructura de ficheros

- Kotlin, un paquete por feature: `feature_x/domain`, `feature_x/data` (+ `data/local`),
  `feature_x/presentation`. Lo transversal en `core/{design,navigation,notifications,datastore,di,utils}`.
- Un fichero por pantalla (`XScreen.kt`), su ViewModel aparte (`XViewModel.kt`), piezas
  reutilizables de esa pantalla en `XPieces.kt`, hojas en `XSheet.kt`.
- Los modelos de dominio son `data class` inmutables con `copy`; los enums llevan su `labelRes`.
- Ids de entidades: `String` con prefijo (`subject-<uuid>`, `task-<uuid>`, `period-N`,
  `sample-…` para datos de muestra).

## Textos (i18n) — regla dura

- **Todo texto de usuario en `res/values/strings.xml` y `res/values-en/strings.xml`**, mismas
  claves en los dos. Claves en `snake_case` con prefijo de área (`tasks_`, `attendance_`,
  `motion_g_…`, `theme_…`, `tpl_…`).
- En composables: `stringResource(R.string.x, args)`. **Nunca dentro de `onClick`, `LaunchedEffect`
  de resultado o corrutinas**: ahí `Textos.get(R.string.x, args)` (o resolver antes con `val`).
- Fuera de Compose (dominio, ViewModel, notificaciones, repositorios): `Textos.get` / `Textos.lista`.
- Enums con etiqueta: `enum class X(@StringRes val labelRes: Int) { val label get() = Textos.get(labelRes) }`.
- **Prohibido**: `Locale.getDefault().language == "en"` para elegir texto; literales en español
  en el código; construir frases concatenando trozos traducidos (usar `%1$s`).
- Formatos de fecha también son recursos cuando cambian por idioma (`notes_day_pattern_same_year`).
- Los tests instalan `TextosDePrueba.instalar()` en `@Before` si pasan por `Textos`.
- Comentarios y KDoc **en español**, explicando el porqué. Los identificadores, en inglés (las
  piezas de UI recientes usan español; seguir el estilo del fichero que se toca).

## Compose

- Pantalla = `@Composable fun XScreen(onBack, …, viewModel: XViewModel = hiltViewModel())`;
  estado con `collectAsStateWithLifecycle()`; estado local con `remember`/`rememberSaveable`.
- **Usar las piezas propias**: `UniCard` (con `Column` dentro), `UniIconButton`, `UniStackButton`
  / `SquishyButton` (nunca `Button` crudo salvo con `UniStackButtonDefaults.shapes`),
  `UniSegmentedControl` vs `UniChoiceRow` según la regla de su KDoc, `MetricCard`,
  `UniDropdownMenu`, `UniSwitch`, `UniDatePicker`/`UniTimePicker`, `UniSearchField`.
- Hojas: `ModalBottomSheet` con `containerColor = MaterialTheme.colorScheme.background`,
  `shape = MaterialTheme.shapes.extraLarge`, contenido con sangrado 18 dp.
- Colores: **sólo del tema** (`MaterialTheme.colorScheme.*`, `LocalSectionColors.current.*`,
  `AttendanceColors`). `Color(0x…)` a pelo fuera de `core/design` rompe `verifyDesignTokens`.
- Formas: `MaterialTheme.shapes.*`; radios a mano sólo para píldoras (`CircleShape`) y detalles
  pequeños documentados.
- Listas: `LazyColumn` con `key`; `contentPadding` inferior = `scrollBottomRoom` (+
  `anchoredButtonRoom` si hay botón anclado). Acciones ancladas con `bottomActionInsets()`.
- Animación: `duracion(ms)` y `tweenDeMovimiento`/`muelleDeMovimiento` de `MotionRuntime` en vez
  de números fijos; `Animatable` para entradas (no `animateFloatAsState` nacido en el destino);
  respetar `hayMovimiento()`; `performSafely(HapticFeedbackType)` para haptics.
- `Modifier` de gestos de la app: `entradaDeLista(i)`, `latidoDeVencido(activo)`,
  `tachadoDe(completado, color)`, `reacomodoDeLista()`, `celebracionDelDia(...)`.
- `contentDescription` en todos los botones de icono; `null` sólo en decorativos.
- `LaunchedEffect` con claves explícitas; nada de lógica de negocio en composables.

## ViewModels

- `@HiltViewModel class XViewModel @Inject constructor(repos…) : ViewModel()`.
- Exponen `StateFlow` (`stateIn(viewModelScope, WhileSubscribed(5_000), …)`) o el flujo del
  repositorio directamente; combinan con `combine`.
- Devuelven resultados simples (`Boolean`, un `Prompt`, `ValidationResult`) para que la pantalla
  decida qué enseñar; los mensajes de feedback son `Textos.get`.
- Nada de `Context` en ViewModels salvo `@ApplicationContext` para recursos.

## Datos

- Entidad Room `XEntity` en `data/local`, DAO con `Flow` para lecturas, mapeos `toDomain()` /
  `toEntity()` junto a la entidad. Repositorio interfaz en `domain`, `RoomXRepository` en `data`,
  y `InMemoryXRepository` para tests cuando exista (mantener su contrato igual al de Room).
- **Cambio de esquema = `version + 1` + `MIGRATION_N_M` escrita a mano + añadirla a
  `ALL_MIGRATIONS` + test en `RoomMigrationTest` + esquema exportado**. Nunca destructiva.
- Los nombres persistidos no se renombran (columnas, claves JSON, ids `period-N`) aunque el
  código se renombre: el respaldo y las alarmas los llevan.
- Preferencias: clave nueva en `UserPreferencesDataSource.Keys` + lectura + escritura + campo en
  `UserProfile` + incluirla en el respaldo JSON (y Firebase).

## Navegación

- Ruta nueva: constante en `AppRoutes` (+ función `xRoute(id)` si lleva argumento), destino en
  `AppNavGraph` con `screen()`, y si es formulario `rememberLeaveGuard`. Comprobar
  `moduleForRoute()` si pertenece a un módulo. Respetar el mapa de profundidad para el `zIndex`.

## Errores y logs

- Validación con `TextValidators` → `ValidationResult(isValid, errorRes, errorArgs)`.
- Intents externos con `runCatching`. Sin `Log` ruidoso; los logs existentes son puntuales.
- Nunca `throw` desde una corrutina del `Application` (cerró el proceso una vez).

## Tests

- JUnit 4. Dominio: tests puros. Room: `@RunWith(RobolectricTestRunner::class)`,
  `@Config(manifest = Config.NONE, sdk = [34])`, base en memoria, `TextosDePrueba.instalar()`.
- Fijar con test cualquier regla que se haya roto una vez (el contrato de `updateSubject`, la
  fórmula del `versionCode`, los umbrales en fracción de escala, las rutas que no dependen del
  módulo de notas).
- Nombres de test en español descriptivo (`losDosDiasQueSeMiranTienenNombrePropio`).

## Commits

- Inglés, Conventional Commits: `feat(scope): subject` en imperativo, minúscula, ≤72; cuerpo con
  el porqué, agrupado por secciones si toca varias áreas. **Sin trailer de coautoría.**
- Un commit por bloque coherente; los cambios de datos (migración) en su propio commit si es
  posible.

## Patrones prohibidos / anti-patterns vistos

- `if (isEn) "…" else "…"` en cualquier capa.
- `stringResource` dentro de lambdas no composables.
- Varios hijos directos en `UniCard`.
- `LinearProgressIndicator` para avance de notas.
- Recalcular promedios/proyecciones fuera de `GradeCalculator`.
- Deducir estado «olfateando» texto (`heroLabel()` aún lo hace; no extenderlo).
- Números mágicos de `padding` inferior en listas (118 dp «a ojo» ya falló).
- `fallbackToDestructiveMigration`; renombrar columnas persistidas.
- Franjas de color al costado; bordes de color; sombras para jerarquía.
- Repetir un número de alpha ya enviado.
- Scripts de migración masiva por heredoc de Bash con barras invertidas.
