---
name: unistack-i18n-coding-rules
description: >-
  Strict internationalization (i18n), bilingual string synchronization, Textos helper usage, and Compose coding rules for UniStack.
  Use when adding or modifying strings in strings.xml and values-en/strings.xml, resolving strings in Compose or non-Composable scopes,
  setting up unit tests with TextosDePrueba, or enforcing Jetpack Compose architecture standards.
---

# UniStack i18n & Coding Rules Skill

This skill enforces bilingual localization, string resolution patterns, Compose performance guidelines, and coding discipline in UniStack.

---

## 1. Dual-Language String Synchronization (i18n)

UniStack is fully bilingual (Spanish primary, English secondary).

### The Dual-File Rule
Every single UI text must exist in BOTH files with identical keys and format arguments:
1. `app/src/main/res/values/strings.xml` (Spanish, default)
2. `app/src/main/res/values-en/strings.xml` (English)

### Placeholder Parity
- Format specifiers must match in count, type, and ordering:
  - Spanish: `<string name="task_due_in">%1$s vence en %2$d días</string>`
  - English: `<string name="task_due_in">%1$s due in %2$d days</string>`
- Hardcoded user-facing strings in Kotlin files are **STRICTLY PROHIBITED**.

---

## 2. String Resolution Patterns

### In Composable Scope
Always use the standard Compose API:
```kotlin
Text(text = stringResource(R.string.home_pending_tasks, count))
```

### In Non-Composable / Callback / ViewModel Scope
Compose APIs like `stringResource` cannot be called inside `onClick` lambdas, ViewModels, BroadcastReceivers, or background Coroutines. Use the global `Textos` helper:
```kotlin
// In a callback or ViewModel event handler:
val message = Textos.get(R.string.task_created_success)
```

### Application Initialization
`Textos.install(this)` is initialized inside `UniStackApplication.onCreate()` **before** calling `super.onCreate()`:
```kotlin
class UniStackApplication : Application() {
    override fun onCreate() {
        Textos.install(this)
        super.onCreate()
    }
}
```

### Unit Test Rule (`TextosDePrueba`)
Because `Textos` relies on an Android `Application` context, calling domain code or ViewModels in JUnit4 / Robolectric unit tests will throw a `NullPointerException` if uninitialized.
- **Rule**: Every test class that triggers code calling `Textos.get()` must install test strings in `@Before`:
  ```kotlin
  @Before
  fun setUp() {
      TextosDePrueba.instalar()
  }
  ```

---

## 3. Jetpack Compose Architecture & Performance Rules

1. **State Hoisting**:
   - Screen-level Composables receive `uiState: StateFlow<ScreenUiState>` and pass explicit lambda callbacks: `onAction: (ScreenAction) -> Unit`.
   - Low-level UI components must be stateless and testable.

2. **Recomposition Optimization**:
   - Wrap complex derived computations in `remember(keys) { ... }` or `derivedStateOf { ... }`.
   - Use stable types or immutable collections (`kotlinx.collections.immutable.ImmutableList`) to prevent unnecessary recompositions in lists.

3. **No Business Logic in Composables**:
   - Never perform math calculations (like `GradeCalculator` calculations), database queries, or DataStore writes inside a Composable function. Delegate everything to the `ViewModel`.

4. **Comments & Documentation**:
   - KDocs and code comments are written in **Spanish**, detailing the architectural *why* rather than just restating the syntax.
   - Commit messages are written in **English** using Conventional Commits.
