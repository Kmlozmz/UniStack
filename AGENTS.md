# UniStack Agent Guidelines & Project Rules

This document establishes the authoritative rules, domain invariants, technical standards, and workflows for all AI agents operating within the UniStack codebase. It synthesizes the 26 core architectural documents located in `UniStack development/`.

---

## 1. Non-Negotiable Operating Principles

1. **Branch Discipline**:
   - All active development occurs on branch `TDL`.
   - Never commit directly to `main`. `main` is updated via fast-forward merge only when finalizing batches:
     ```powershell
     git checkout main; git merge --ff-only TDL; git push origin main; git checkout TDL
     ```

2. **Autonomous Alpha Delivery (`sendAlpha`)**:
   - After completing and verifying a unit of work, compile and distribute an alpha build immediately:
     ```powershell
     ./gradlew sendAlpha "-PversionName=1.6.0-alpha.N"
     ```
   - **PowerShell Quoting Rule**: Always wrap `-PversionName=...` in double quotes.
   - The build is delivered automatically to the developer's Telegram bot (`UniStackBot`).

3. **Commit Integrity (No AI Footers)**:
   - Use Conventional Commits in **English** explaining the architectural *why*.
   - **STRICT PROHIBITION**: Do NOT include `Co-Authored-By: Claude...`, `Co-Authored-By: Antigravity...`, or any AI attribution trailers.

4. **Spanish Reporting Format**:
   - Reports must be concise, bulleted, and in Spanish, broken into:
     - **Interno**: Refactorings, migrations, database queries, tests, architectural updates.
     - **Palpable**: Visual changes, buttons, dialogs, user-visible state updates.
     - **Ruta en el cel**: Exact navigation path to test the change on a physical device.

5. **Artifacts Before Code**:
   - For all non-trivial UI features or screen redesigns, create an interactive HTML artifact (390×844px mobile viewport, `#0A0C11` dark surface) and obtain user approval before modifying Kotlin Compose code.

---

## 2. Core Domain & Architecture Invariants

1. **Grade Calculation (`GradeCalculator`)**:
   - **No Projections**: Strictly compute Guaranteed Minimum (Floor) and Best Possible (Ceiling).
   - **Scale Change Wiping**: Changing the grading scale (e.g. from 0–5 to 0–100) permanently wipes existing grade records. Never attempt mathematical mapping between incompatible scales.

2. **Attendance Rules**:
   - Maximum 1 attendance record per subject per calendar day.
   - Schedule room and professor string stored as `"aula•profesor"`.
   - Post-class prompt triggers 20 minutes after class ends, deep-linking into attendance history with the class pre-selected.

3. **Tasks & Evaluations**:
   - Done $\ne$ Graded. Tasks have explicit `gradingStatus`: `UNDECIDED`, `NOT_GRADED`, `AWAITING_GRADE`, `GRADED`.
   - Subtasks checklist stored in Room v22 table `task_subtasks` with drag-and-drop position sorting.

4. **Database & Migrations**:
   - Room schema currently at v21 (12 tables). Handcrafted migrations in `DatabaseMigrations.kt`.
   - Destructive migrations are strictly forbidden.
   - All migrations must be verified via `RoomMigrationTest.kt`.
   - DataStore preferences (~137 keys) accessed only via typed repositories.

5. **Internationalization (i18n)**:
   - 100% bilingual parity between `res/values/strings.xml` and `res/values-en/strings.xml`.
   - In Compose: `stringResource(R.string.id)`.
   - In non-Composable / callbacks: `Textos.get(R.string.id)`.
   - In unit tests: call `TextosDePrueba.instalar()` in `@Before`.

---

## 3. Design System & Anti-Patterns

- **Base Surface**: `#0A0C11` (dark theme default).
- **Semantics**: Gastos is strictly Red (`#E53935`); Horario is Indigo (`#3F51B5`).
- **Core Components**:
  - `UniCard`: Must wrap an inner `Column(modifier = Modifier.padding(...))`.
  - `MetricCard`: Fixed height `58.dp`.
  - `OutcomeRangeBar`: Honest floor/ceiling visualization.
  - Spacing constants: `scrollBottomRoom` (28dp), `anchoredButtonRoom` (76dp).
- **Strictly Forbidden Patterns**:
  - ❌ Colored card borders (use tonal surfaces).
  - ❌ Side-rail accent stripes ("AI slop").
  - ❌ Canvas-drawn UniStack logo (use `R.drawable.logo` PNG asset).
  - ❌ Floating bottom navigation bars (must dock to bottom).
  - ❌ Bounce scroll / overscroll physics.
  - ❌ Predictive back gesture overrides.

---

## 4. Specialized Skills & Subagents

### Discovered Workspace Skills (`.agents/skills/`)
- `unistack-workflow-release`: Compilation, versioning math, branch management, Telegram deployment.
- `unistack-design-system`: M3 Expressive, tokens, components, motion, and anti-patterns.
- `unistack-domain-database`: Room migrations, GradeCalculator, attendance, and DataStore.
- `unistack-i18n-coding-rules`: Bilingual string synchronization, Textos helper, test harness.
- `unistack-artifact-prototyping`: Interactive HTML prototypes and multi-proposal design reviews.
- `unistack-qa-testing`: Test suite, regression watchlist, debugging playbook, done criteria.
- `unistack-product-decisions`: Vision, decision criteria, closed decisions, vetoes, TDL discipline.

### Registered Subagents (briefs in `.agents/agents/`, dispatch via subagent with the brief as context)
- `unistack_architect`: Specialist in Room migrations, domain models, DataStore, and repositories.
- `unistack_ui_engineer`: Specialist in Jetpack Compose, M3 Expressive, gestures, and UI components.
- `unistack_release_manager`: Specialist in Gradle tasks, APK delivery via Telegram, and Git synchronization.
- `unistack_qa_i18n_specialist`: Specialist in JUnit4 test suites, bilingual string parity, and regression testing.
