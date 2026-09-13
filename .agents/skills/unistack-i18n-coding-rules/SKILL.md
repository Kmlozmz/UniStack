---
name: unistack-i18n-coding-rules
description: >-
  Bilingual strings, Textos helper, and Compose/architecture coding rules for UniStack.
  Use when adding UI text (es + en parity), resolving strings in composables/callbacks/ViewModels,
  writing screens, ViewModels, Room/DataStore code, navigation routes, tests, or reviewing code
  against prohibited patterns. Source: UniStack development/CODING_RULES.md.
---

# UniStack i18n & Coding Rules Skill

## 1. i18n — hard rule

**Every user-facing text lives in BOTH files with identical keys and format args:**
`app/src/main/res/values/strings.xml` (Spanish) and `values-en/strings.xml` (English).
Keys in `snake_case` with area prefix (`tasks_`, `attendance_`, `motion_g_…`, `theme_…`, `tpl_…`).
Date formats that change per language are resources too (`notes_day_pattern_same_year`).

- Compose: `stringResource(R.string.x, args)`. **Never** inside `onClick`, result
  `LaunchedEffect`, or coroutines — there (and in domain, ViewModels, notifications, repos):
  `Textos.get(R.string.x, args)` / `Textos.lista(arrayId)` (resolve earlier with `val` if handy).
- Enums with label: `enum class X(@StringRes val labelRes: Int) { val label get() = Textos.get(labelRes) }`.
- **Forbidden**: `if (isEn) "…" else "…"`, `Locale.getDefault().language == "en"` for text choice,
  Spanish literals in code, concatenating translated chunks (use `%1$s`).
- `Textos` resolves with the **app-chosen** language (`AppLanguage`), not the system's;
  installed in `UniStackApplication.onCreate()` **before** `super.onCreate()`.
- Tests: `TextosDePrueba.instalar()` in `@Before` whenever code paths touch `Textos`
  (it reads the real XMLs — a missing key fails). Test names in descriptive Spanish.
- Code identifiers in English (recent UI pieces use Spanish — follow the file you touch).
  Comments/KDoc **in Spanish, explaining the WHY** ("Aquí estaba el fallo de los modos…").

## 2. Compose

- Screen = `@Composable fun XScreen(onBack, …, viewModel: XViewModel = hiltViewModel())`;
  state via `collectAsStateWithLifecycle()`; local state `remember`/`rememberSaveable`.
- **Use own pieces** (design-system skill): `UniCard` (+ inner `Column`), `UniStackButton`/
  `SquishyButton` (never raw `Button` except with `UniStackButtonDefaults.shapes`),
  `UniSegmentedControl` vs `UniChoiceRow` per their KDoc, `MetricCard`, `UniDropdownMenu`,
  `UniSwitch`, `UniDatePicker`/`UniTimePicker`, `UniSearchField`. `LeaveGuard` in every form.
- Colors only from theme (`MaterialTheme.colorScheme.*`, `LocalSectionColors.current.*`,
  `AttendanceColors`). Shapes from `MaterialTheme.shapes.*`; hand radii only for pills
  (`CircleShape`) and documented details.
- Lists: `LazyColumn` with `key`; bottom `contentPadding` = `scrollBottomRoom`
  (+ `anchoredButtonRoom` with anchored button). Anchored actions with `bottomActionInsets()`.
- Animation: `duracion(ms)` + `tweenDeMovimiento`/`muelleDeMovimiento` from `MotionRuntime`,
  never fixed numbers; `Animatable` for entrances; respect `hayMovimiento()`;
  `performSafely(HapticFeedbackType)` for haptics; app gesture modifiers
  (`entradaDeLista(i)`, `latidoDeVencido(activo)`, `tachadoDe(…)`, `celebracionDelDia(…)`).
- `contentDescription` on every icon button (`null` only decorative).
- `LaunchedEffect` with explicit keys; **zero business logic in composables**.
- File layout: `XScreen.kt`, `XViewModel.kt` apart, screen pieces in `XPieces.kt`, sheets in
  `XSheet.kt`. Split screens past ~1,000 lines into `*Pieces.kt`.

## 3. ViewModels, data, navigation, errors

- `@HiltViewModel … @Inject constructor(repos…) : ViewModel()`. Expose `StateFlow`
  (`stateIn(viewModelScope, WhileSubscribed(5_000), …)`) or the repo flow; `combine` flows.
  Return simple results (`Boolean`, `Prompt`, `ValidationResult`) so the screen decides;
  feedback messages via `Textos.get`. No `Context` except `@ApplicationContext`.
- Room: `XEntity` in `data/local`, DAO with `Flow` reads, `toDomain()`/`toEntity()` next to
  the entity; interface in `domain`, `RoomXRepository` in `data`, `InMemoryXRepository` for
  tests (same contract). Domain models: immutable `data class` + `copy`.
- New route: constant in `AppRoutes` (+ `xRoute(id)` if args), destination in `AppNavGraph`
  with `screen()` (+ `rememberLeaveGuard` if form), check `moduleForRoute()`; respect the
  depth map for `zIndex`. Never delete `grades`/`tasks` redirect routes (old alarms store literals).
- Validation via `TextValidators` → `ValidationResult(isValid, errorRes, errorArgs)`.
  External intents in `runCatching`. No noisy `Log`. **Never `throw` from an `Application`
  coroutine** (a network `IOException` once killed the process — swallow it there).

## 4. Prohibited (each broke something once)

`if (isEn)` anywhere · `stringResource` in non-composable lambdas · multiple direct children
in `UniCard` · `LinearProgressIndicator` for grade progress · recomputing averages outside
`GradeCalculator` · sniffing state from text (don't extend `heroLabel()`) · eyeballed list
bottom paddings · `fallbackToDestructiveMigration` / renaming persisted columns · side color
rails, colored borders, shadow hierarchy · repeating a shipped alpha number · bulk-migration
scripts via Bash heredoc with backslashes.
