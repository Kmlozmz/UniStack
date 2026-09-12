---
name: unistack-domain-database
description: >-
  Domain calculations, academic invariants, Room database migrations, DataStore schemas, and data persistence for UniStack.
  Use when modifying GradeCalculator logic, Room entities and DAOs, Room migration scripts (MIGRATION_X_Y),
  attendance rules, task grading statuses, or JSON backup/restore repositories.
---

# UniStack Domain & Database Skill

This skill documents the core domain logic, academic invariants, Room database architecture, migration rules, and DataStore preferences for UniStack.

---

## 1. Academic Domain Invariants

### Honest Grading: Floor vs. Ceiling
The `GradeCalculator` rejects artificial linear projections.
- **Guaranteed Minimum (Floor)**: Grade the student achieves if they score 0 on all remaining evaluations:
  $$\text{Floor} = \sum (\text{grade}_i \times \text{weight}_i)$$
- **Best Possible (Ceiling)**: Grade the student achieves if they score 100% on all remaining evaluations:
  $$\text{Ceiling} = \text{Floor} + \sum (\text{maxScore} \times \text{weight}_{\text{remaining}})$$
- **Needed Score (`scoreNeeded`)**: The score required on the remaining percentage to achieve passing grade or custom target.
- **Scale Change Policy**: Changing the grading scale (e.g. from 0.0–5.0 to 0–100) **wipes all existing grades**. Automatic numeric scaling produces irreversible decimal distortion and fabricates historical academic records.

### Academic Cuts ("Cortes") & Terms
- A semester is divided into terms and cuts (`GradingCut`).
- Each cut has an assigned weight ($\sum \text{weights} = 100\%$) and an optional `endEpochDay`.
- **Closed Cut Stamp**: When a cut ends, the student stamps it as closed. The closed state is tracked via `closedPeriodIdsJson` (introduced in Room v21).

### Attendance System Rules
- **One Session Per Subject Per Day**: The model allows at most one registered attendance status per subject on any given calendar day.
- **Schedule Location Delimiter**: Room stores classroom and professor in a single string delimited by a bullet: `"aula•profesor"`.
- **Absence Threshold**: Defined globally in `UserProfile.absenceLimit` (default 20%). Warning indicators appear when absence percentage exceeds this value.
- **Post-Class Reminder**: Fires 20 minutes after class ends. Clicking the notification routes directly into the Attendance History with the target subject pre-selected.

### Tasks & Subtasks
- **Done $\ne$ Graded**: Completing a task does not imply it has been graded.
- **`gradingStatus` Lifecycle**:
  - `UNDECIDED`: Initial state for custom tasks.
  - `NOT_GRADED`: Reading, practice, non-evaluative work.
  - `AWAITING_GRADE`: Submitted assignments waiting for the professor's review.
  - `GRADED`: Attached to a formal grade entry in the academic record.
- **Subtasks (Room v22)**: Checklist items stored in `task_subtasks` table with foreign key to `tasks(id)` and explicit integer `position` for drag-and-drop sorting.

---

## 2. Room Database Architecture

### Entity Catalog (Room v21)
1. `subjects`: Core academic course definitions.
2. `grades`: Evaluations with cut assignments and weights.
3. `tasks`: Academic tasks, exams, homework, deadlines.
4. `attendance_records`: Daily presence/absence per subject.
5. `class_schedules`: Weekly recurring class hours.
6. `grading_cuts`: Definition of cuts/periods for subjects.
7. `terms`: Academic semesters/quarters.
8. `notes`: Markdown-supported notes with tagging.
9. `expense_records`: Personal student finances (Gastos).
10. `flashcard_decks`: Study decks.
11. `flashcards`: Spaced repetition flashcard items.
12. `study_sessions`: Pomodoro and study tracking sessions.

*(Upcoming v22 adds `task_subtasks`).*

### Migration Protocol
1. **Never use `fallbackToDestructiveMigration()` in production**: All database changes must have explicit, tested `Migration(from, to)` scripts in `DatabaseMigrations.kt`.
2. **Schema Export**: `room.schemaLocation` is configured to export schemas to `schemas/`.
3. **Automated Migration Tests**:
   - Every new migration must have a test case in `RoomMigrationTest.kt` verifying:
     - Old schema creation via `MigrationTestHelper`.
     - Applying `MIGRATION_X_Y`.
     - Validating table schemas, default values, and foreign keys.
4. **SQLite Type Integrity**: Use SQLite `INTEGER` (0/1) for Booleans, `TEXT` for ISO dates / JSON arrays, and `REAL` for floating-point calculations.

---

## 3. DataStore Preferences

- Stored in Jetpack DataStore (~137 configuration keys).
- Access is strictly encapsulated through typed repositories (e.g. `UserPreferencesRepository`, `ThemePreferencesRepository`).
- Default values are guarded with null-safe fallbacks.
- Never write raw blocking reads (`runBlocking`) on DataStore from UI threads or Composables; observe via `Flow<T>`.

---

## 4. Backup & Export Architecture

- **`LocalJsonBackupRepository`**:
  - Serializes all Room tables and critical DataStore keys to a single JSON archive.
  - Includes schema version, export timestamp, and payload integrity checksum.
  - Backward compatibility: Import logic must tolerate missing fields or legacy schemas from earlier versions.
