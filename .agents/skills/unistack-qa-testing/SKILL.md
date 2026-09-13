---
name: unistack-qa-testing
description: >-
  Verification, unit testing, regression watchlist, and debugging playbook for UniStack.
  Use when writing JUnit/Robolectric tests, fixing RoomMigrationTest or backup tests,
  triaging a bug, validating a batch before sendAlpha, or deciding whether a task counts
  as done. Source: UniStack development/TESTING.md, KNOWN_ISSUES.md, FAILED_APPROACHES.md.
---

# UniStack QA & Testing Skill

No Compose UI tests per screen — **the final validator is his phone**. "Implementado" ≠ "funciona"
(learned with grade deletion: dialog right, write wrong). Done = green tests + his verdict.

## 1. Commands

```bash
./gradlew -q testDebugUnitTest                        # all (534, ~40 s)
./gradlew testDebugUnitTest --tests "*TasksViewModelTest*"   # one
./gradlew check                                        # tests + verifyDesignTokens + lint
```

Report: `app/build/test-results/testDebugUnitTest/TEST-*.xml` (search `<failure>`).
Instrumented (`connectedDebugAndroidTest`): 3 files, rarely run, `VERIFY` they pass.
`check` failing 1st time on `lintAnalyzeDebugUnitTest` = environment flake: repeat untouched.

## 2. What must be tested (project rule)

- **Every new/changed domain rule**: pure test (GradeCalculator, CutDateRules, TermCloseCheck,
  SubjectAttendanceHistory, ReminderTiming, ExpenseInsights, NoteMarkdown, ReleaseVersion…).
- **Every Room migration**: case in `RoomMigrationTest` (build old version, migrate, read).
- **Every repo write that failed once**: through real Room (`RoomRepositoriesTest`), not the
  in-memory double. **Every new model field**: backup round-trip
  (`LocalJsonBackupRepositoryTest`). **Every duplicated formula** (Gradle ↔ Kotlin): compare test.
- Infra: `TextosDePrueba` (reads the real XMLs — install in `@Before`), `MainDispatcherRule`.
  Room tests: `@RunWith(RobolectricTestRunner::class)`, `@Config(manifest = NONE, sdk = [34])`.
- New text ⇒ exists in both XMLs (the harness fails if missing from `values`).

## 3. Manual validation (the one that counts)

1. Alpha installs **over** the previous one (same signature, higher number).
2. Walk the report's exact path with real or test-bench/sample data ("le da pereza crearlas
   a mano" — seed it for him).
3. If data was touched: create data on the old version → upgrade → verify → export → restore
   (the `PUBLICAR.md` bar for climbing to beta).
4. Mismatches come back as numbered screenshots.

## 4. Regression watchlist (all happened)

"Fixed" that wasn't (dialog ok, write missing) · writes silently dropping fields
(`absenceLimit`, `termId`) and backups not storing them · invisible animations (born at
destination, scale inside `clip`, px vs dp) · labels changing by keyword-sniffing ·
alarms pointing at deleted routes · app dying on open (`Textos` with no provider, network
exception in `Application`) · red test after a text change (missing `TextosDePrueba` or XML key).

## 5. Debugging playbook

`grep`/read before assuming — KDocs usually hold the why of the current state. When
something "doesn't show", suspect the known traps first: multi-child `UniCard`,
`animateFloatAsState` born at destination, parent-coerced `size`, scale inside `clip`,
`stringResource` in a lambda, `Textos` with no provider. Report failures as-is with output;
never "debería funcionar". Fixing a real bug found in passing is welcome (report it as such).

## 6. Done criteria

Compiles · `testDebugUnitTest` green · `check` green (2nd run if env lint) · texts in es+en,
no new literals · migration + test + backup if schema · alpha sent, committed, `TDL`+`main`
pushed · report with interno/palpable + paths · **his verdict on the phone**.

Open issues live in `KNOWN_ISSUES.md` (priority Alta/Media/Baja) — check before promising dates.
