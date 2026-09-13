---
name: unistack-domain-database
description: >-
  Academic domain rules, Room schema, hand-written migrations, DataStore, and backup for UniStack.
  Use when touching GradeCalculator (floor/ceiling), grading cuts, attendance, tasks and gradingStatus,
  Room entities/DAOs, MIGRATION_N_M, UserPreferences keys, or LocalJsonBackupRepository/Firebase backup.
  Source: UniStack development/DATABASE.md, ARCHITECTURE.md, PRODUCT.md.
---

# UniStack Domain & Database Skill

No backend: Room (`unistack.db`, **v22** with `task_subtasks`, 13 tables; docs snapshot said v21/12
at 12 sep — check `UniStackDatabase.kt` for the live number) + DataStore Preferences (~137 keys).
Single Gradle module, packages per `feature_*` + `core`, each feature `domain / data / presentation`.

## 1. Domain invariants (never break)

- **No projections.** `GradeCalculator` yields exact Floor (`guaranteedMinimum`: final scoring 0
  on what's left) and Ceiling (`bestPossible`: scoring everything). Never compute averages or
  projections anywhere else. Thresholds in scale fraction (pinned by tests).
- **Scale change wipes `grades`** (whole table) + readjusts `targetAverage`. Double confirm with
  real count; silent if no grades. Never map between scales (round-trip doesn't close).
- **Cuts (`GradingCut`)**: weight (%) + optional `endEpochDay`; no overlaps/gaps by construction
  (`CutDateRules`); registering a grade picks the cut by date. A 100%-complete cut closes
  **by hand with seal** (`closedPeriodIdsJson`); new grades go to the next open cut.
  The word `Corte` is a translatable constant. Persisted as `period-N` — never rename.
- **`activePeriodId` empty = undecided**: no history claimed, grade button off.
- **Attendance**: bounded to the active period (`SubjectAttendanceHistory`) — never invent past
  classes. ATTENDED / ABSENT / CANCELLED / RESCHEDULED per occurrence; cancelled/rescheduled
  **don't spend absences**. Max 1 record per subject per day. Room + professor live in
  `ClassSession.location` as `"aula•profesor"`, NOT in `Subject`.
- **Global absence cap** (`UserProfile.absenceLimit`); `subjects.absenceLimit` is legacy.
  Post-class prompt fires **20 min** after class ends → opens history with that class ready.
- **Tasks**: done ≠ graded. `gradingStatus`: `UNDECIDED` (initial) · `NOT_GRADED`
  (reading/practice) · `AWAITING_GRADE` (submitted, waiting) · `GRADED` (linked grade).
  Completing a gradable task creates a `GradeItem` with `taskId`, `source = TASK`.
  Subtasks live in `task_subtasks` (id, taskId, title, done, position, createdAt), drag-drop by position.
- **Term close**: `TermCloseCheck` lists the half-done; double confirm typing CERRAR;
  `stampTerm` seals subjects; the new term inherits scale/pass/cuts; repeats = empty copies
  with `repeatedFromSubjectId`.
- All tables carry `userId` (always the local user today). Entity ids are prefixed Strings
  (`subject-<uuid>`, `task-<uuid>`, `period-N`, `sample-…`).

## 2. Tables (live in `feature_grades/data/local/UniStackDatabase.kt`)

`subjects` · `grades` (`subjectId` FK CASCADE; `taskId` logical) · `tasks` · `task_subtasks`
· `class_sessions` · `class_occurrences` (one row per MARKED class; unmarked don't exist)
· `agenda_events` · `academic_terms` (one active; closed = history) · `academic_breaks`
· `notes` (+`title`, `reminderAt`, `colorArgb`, `archived`, `deletedAt` trash) ·
`note_attachments` (bytes in app-internal storage) · `expenses` (integer amount) ·
`academic_works` (templates; to be merged into Tasks — PENDING).

Relations without FK (orphans possible — each repository defines behavior; Tasks shows
"Sin materia"): `tasks/class_sessions/notes/academic_works.subjectId`, `subjects.termId`,
`class_occurrences.sessionId`, `note_attachments.noteId` (files deleted by code).
`updateSubject()` does NOT touch grades (separate table): `clearGrades(subjectId)`.
One subject holds **at most one** `class_sessions` (code-kept invariant).

## 3. Migration protocol (hard)

Schema change = `version + 1` + hand-written `MIGRATION_N_M` + add to `ALL_MIGRATIONS` +
case in `RoomMigrationTest` (build old, migrate, read) + exported schema (`app/schemas/`).
**Never `fallbackToDestructiveMigration`.** Dead columns are kept (`notes.format`).
Persisted names (columns, JSON keys, `period-N`, alarm route literals) are never renamed.

Repos expose `StateFlow` of domain models (`stateIn(scope, Eagerly)`); writes are
non-suspending fire-and-forget (`scope.launch { dao.insert(…) }`) — tests observe via flow
or `runTest`/`advanceUntilIdle`. Keep `InMemory*Repository` contract equal to Room
(a laxer double once passed tests and failed in production — now pinned by contract test).

## 4. DataStore (not Room)

`UserPreferencesDataSource` (~137 keys) → `UserProfile`: identity, academic (scale, custom
max, pass mark, target, absence cap, **`gradingCutScheme`** with weights+dates), modules,
appearance, accessibility (+language), notifications, expenses, notes prefs, calculator
scenarios, `setupCompleted`. New preference = key + read + write + `UserProfile` field +
JSON backup (+ Firebase). Never change a preference default without migrating the key
(`screenTransitionStyle` was born new for that reason).

## 5. Backup

`LocalJsonBackupRepository` serializes everything above (Room + profile) to JSON with stable
ids; restore replaces; preview before restoring. **New model field ⇒ add it here AND in
`FirebaseCloudBackupRepository`** (same JSON, 1 MB doc cap → attachments back up the
*record*, not the bytes; on another phone the note says "not on this phone").
No automatic backup before destructive ops. Base unencrypted in app-private storage.
