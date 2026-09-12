# DATABASE — datos de UniStack

Room, base `unistack.db`, **versión 21** (12 sep 2026), `exportSchema = true` (esquemas en
`app/schemas/`). Migraciones escritas a mano en `feature_grades/data/local/UniStackDatabase.kt`
(`MIGRATION_1_2` … `MIGRATION_20_21`, todas en `ALL_MIGRATIONS`), probadas en `RoomMigrationTest`.
**Nunca migración destructiva.** El perfil y las preferencias **no** están en Room: DataStore.

Todas las tablas llevan `userId` (hoy siempre el usuario local; pensado para la cuenta opcional).
Los ids son `String` con prefijo. Los nombres persistidos (columnas, `period-N`, claves JSON)
**no se renombran** aunque el dominio cambie (`period` → «corte»).

## Tablas

| Tabla | Entidad | Campos clave | Notas |
|---|---|---|---|
| `subjects` | `SubjectEntity` | `id`, `name`, `targetAverage`, `visualType`, `customColor`, `periodSchemeJson`, `activePeriodId` (vacío = sin elegir), `historyPromptStatus`, `unknownPeriodIdsJson`, `closedPeriodIdsJson` (v21), `termId` (índice), `absenceLimit` (legado; el tope es global), `repeatedFromSubjectId` (v16) | Una materia tiene **como mucho un** `class_sessions` (invariante mantenido por código) |
| `grades` | `GradeEntity` | `id`, `subjectId` (FK CASCADE), `name`, `value`, `percentage`, `type`, `periodId`, `source` (ACTIVITY/TASK…), `weightStatus`, `taskId` (índice), `recordedAt` | Las notas viven aquí, no dentro de `subjects`: `updateSubject()` no las toca |
| `tasks` | `TaskEntity` | `id`, `title`, `description`, `subjectId?`, `type`, `dueDateMillis`, `difficulty`, `estimatedMinutes`, `completed`, `periodId?`, `gradingStatus` (UNDECIDED/NOT_GRADED/AWAITING_GRADE/GRADED), `linkedGradeId?`, `completedAt?` | **Con el rediseño D se añade `task_subtasks` (v22)** — `PENDING` |
| `class_sessions` | `ClassSessionEntity` | `subjectId`, `daysOfWeekCsv`, `startMinute`, `endMinute`, `location` (`"aula•profesor"`), `reminderMinutes`, `repeatEveryWeeks`, `recurrenceStartEpochDay` | El profesor vive aquí, no en la materia |
| `class_occurrences` | `ClassOccurrenceEntity` | `sessionId`, `dateEpochDay`, `status` (ATTENDED/ABSENT/CANCELLED/RESCHEDULED), `modality`, `absenceReason?`, `note`, `override*` | Una fila por clase marcada; las no marcadas no existen |
| `agenda_events` | `AgendaEventEntity` | `title`, `kind`, `startMillis`, `endMillis?`, `allDay`, `recurrence`, `reminderMinutes`, `colorArgb?` | Eventos propios del horario |
| `academic_terms` | `AcademicTermEntity` | `name`, `type` (SEMESTER/TRIMESTER/QUARTER/ANNUAL/BLOCKS), `startEpochDay`, `plannedEndEpochDay?`, `closedEpochDay?`, `status` | Uno activo; los cerrados forman el histórico (v14) |
| `academic_breaks` | `AcademicBreakEntity` | `name`, `startEpochDay`, `endEpochDay` | Días sin clase |
| `notes` | `NoteEntity` | `title` (v19), `body` (Markdown), `subjectId?`, `format` (columna muerta), `pinned`, `reminderAt?` (v19), `colorArgb?`, `archived` (v20), `deletedAt?` (v20, papelera) | v17 |
| `note_attachments` | `NoteAttachmentEntity` | `noteId`, `kind`, `displayName`, `storedName`, `mimeType`, `sizeBytes`, `durationMillis?` | Bytes en almacenamiento interno de la app; v18 |
| `expenses` | `ExpenseEntity` | `category`, `amount` (entero, moneda del perfil), `dateMillis` | — |
| `academic_works` | `AcademicWorkEntity` | `templateId`, `title`, `subjectId?`, `dueDateMillis?`, `status`, `priority`, `completedChecklistIdsJson`, `thesis`, `outline`, `sources`, `notes` | Plantillas de trabajos; por fundir en Tareas |

## Relaciones

- `grades.subjectId → subjects.id` (FK, CASCADE). `grades.taskId → tasks.id` lógica (sin FK).
- `tasks.subjectId`, `class_sessions.subjectId`, `notes.subjectId`, `academic_works.subjectId`
  → `subjects.id` **sin FK** (borrar materia deja huérfanos: `VERIFY` cómo lo trata cada
  repositorio; Tareas muestra «Sin materia»).
- `subjects.termId → academic_terms.id` lógica. `class_occurrences.sessionId → class_sessions.id` lógica.
- `note_attachments.noteId → notes.id` lógica; borrar nota borra archivos por código.

## Migraciones relevantes (qué introdujo cada una)

| Versión | Qué |
|---|---|
| 13→14 | `academic_terms`, `subjects.termId` (histórico por semestre) |
| 14→15 | fechas de cierre de cortes (`endEpochDay` en el esquema JSON) `VERIFY` |
| 15→16 | `subjects.repeatedFromSubjectId` |
| 16→17 | `notes` |
| 17→18 | `note_attachments` |
| 18→19 | `notes.title`, `reminderAt`, `colorArgb` |
| 19→20 | `notes.archived`, `deletedAt` (+ índice) |
| 20→21 | `subjects.closedPeriodIdsJson` (cierre de corte a mano con sello) |
| **21→22** | **`task_subtasks`** (rediseño D) — `PENDING` |

## Preferencias (DataStore, no Room)

`UserPreferencesDataSource` con ~137 claves → `UserProfile`: identidad (nombre, foto local,
programa, área, institución, semestre actual/total), académico (escala, máximo custom,
aprobado, meta, tope de faltas, **esquema de cortes** `gradingCutScheme` con pesos y fechas),
módulos, apariencia (`AppearancePreferences`), accesibilidad (`AccessibilityPreferences`,
incluye idioma), notificaciones (por tipo, antelación, resumen diario, silencio), gastos
(presupuestos, umbral, categorías, estilo de gráfico), notas (vista, orden, formato por
defecto), escenarios de la calculadora, `setupCompleted`.

## Reglas de negocio que viven en los datos

- **Cambiar la escala borra `grades` enteras** y reajusta `targetAverage` (DECISIÓN).
- **Un corte se cierra a mano** (`closedPeriodIdsJson`); las notas nuevas van al siguiente con hueco.
- **`activePeriodId` vacío = sin elegir**: no se reclama historial, botón de nota apagado.
- **Fechas de corte sin solapes ni huecos por construcción** (`CutDateRules`); registrar una nota
  elige el corte por fecha.
- **Asistencia acotada al periodo activo** (`SubjectAttendanceHistory`): no se inventan clases
  anteriores al inicio del periodo. Canceladas/reprogramadas no gastan falta.
- **Tope de faltas**: valor único en el perfil (`UserProfile.absenceLimit`); `subjects.absenceLimit` legado.
- **Cerrar un periodo**: `TermCloseCheck` lista lo a medias; se sellan las materias
  (`gradesRepository.stampTerm`); el nuevo hereda escala/aprobado/cortes; materias repetidas =
  copia vacía con `repeatedFromSubjectId`.
- **Tareas → notas**: completar una tarea calificable crea un `GradeItem` con `taskId` y
  `source = TASK`; desvincular borra la nota o la deja según acción (`VERIFY` en `TasksViewModel`).

## Respaldo

- `LocalJsonBackupRepository` serializa **todo lo anterior** (Room + perfil) a JSON con ids
  estables; restaurar sustituye. Campo nuevo ⇒ añadirlo aquí y en `FirebaseCloudBackupRepository`.
- Adjuntos de notas: se respalda la **ficha**, no los bytes (tope 1 MB en Firestore). En otro
  teléfono la nota dice «No está en este teléfono».
- No hay respaldo automático antes de operaciones destructivas.

## Seguridad

Base sin cifrar en almacenamiento privado de la app (`UNKNOWN` si se ha considerado cifrar;
nunca se discutió). Sin datos en servidores propios.

## Queries de interés

DAOs con `Flow<List<Entity>>` para lecturas reactivas; consultas por `userId`, por
`subjectId`, por rango de `dateEpochDay`/`dueDateMillis`; `notes` filtra `deletedAt IS NULL`.
No hay consultas complejas: la agregación se hace en dominio.

## OPEN QUESTIONS
- Qué pasa con tareas/notas/clases al borrar una materia (huérfanos vs cascada): `VERIFY`.
- Cifrado local: `UNKNOWN`.
- Esquema exacto de `task_subtasks` (id, taskId, title, done, position, createdAt): `TO DEFINE`
  en la implementación de D — propuesta del agente.
