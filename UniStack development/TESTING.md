# TESTING — qué se prueba y qué significa «terminado»

## Cómo ejecutar

```bash
./gradlew -q testDebugUnitTest          # todos (534 tests, ~40 s)
./gradlew testDebugUnitTest --tests "*TasksViewModelTest*"   # uno
./gradlew check                         # tests + verifyDesignTokens + lint
```

Informe: `app/build/test-results/testDebugUnitTest/TEST-*.xml` (buscar `<failure`).
Instrumentados (`connectedDebugAndroidTest`): 3 ficheros, apenas usados, `VERIFY` que pasan.

## Qué existe (55 ficheros, por área)

| Área | Ficheros | Qué fijan |
|---|---|---|
| Notas académicas | `GradeCalculatorTest`, `GradesRepositoryContractTest`, `GradesViewModelTest`, `SubjectMarkShapeTest`, `CutDateRulesTest`, `GradingCutSchemeDatesTest` | suelo/techo, umbrales en fracción, contrato `updateSubject` no toca notas, fechas de corte sin solapes |
| Periodos | `AcademicTermTest`, `TermCloseCheckTest` (19), `RoomAcademicTermRepositoryTest`, `SetupTermDatesTest` | tramo del año, comprobación de cierre, repositorio |
| Horario / asistencia | `ClassSessionTest`, `AgendaEventTest`, `AttendanceSummaryTest`, `AttendanceWeekTest`, `SubjectAttendanceHistoryTest` (12), `TimelineRowsTest` | ventana acotada al periodo, cuenta de faltas, filas del día |
| Tareas | `TaskDateUtilsTest`, `TasksViewModelTest` | fechas relativas, completar/nota |
| Inicio | `HomeSummaryFactoryTest` | prioridades, plan tranquilo, clase en curso |
| Notas rápidas | 11 tests de dominio (`NoteMarkdown`, `NoteChecklist`, `NoteMention`, `NoteReminders`, `NoteGrouping`, `NoteSearch`…) + `RoomNotesRepositoryTest`, `NoteSamplesTest` | motor Markdown, `@` sin tildes, agrupación por día, recordatorios |
| Gastos | `ExpenseDateUtilsTest`, `ExpenseInsightsTest` (13), `ExpensesViewModelTest` | lecturas Comparado/Ritmo/Calendario |
| Datos | `RoomMigrationTest`, `RoomRepositoriesTest`, `LocalJsonBackupRepositoryTest` | cada migración 1→21, escrituras reales (`absenceLimit`, `termId`), respaldo ida y vuelta con ids estables |
| Notificaciones | `ReminderTimingTest` (13) | ventana 48 h, rearmado, vencidos no se tiran |
| Actualizaciones | `ReleaseVersionTest`, `VersionCodeOrderTest`, `UpdateOutcomeTest`, `ReleaseNotesTest`, `ChangelogTest` | orden de sufijos, escalera (fórmula duplicada), estados «al día» |
| Navegación / build | `NavigationRulesTest`, `BuildStageTest` | rutas de Horario no dependen del módulo de notas; peldaños |
| Apariencia / Movimiento | `AppearancePreferencesTest`, `MotionCatalogTest` | 7 gestos / 39 variantes, grupos |
| i18n | `LocalizationEnglishTest`, `TextValidatorsTest` | textos en inglés; validaciones con `errorRes` |
| Soporte | `SupportTicketTest`, `CalculatorMathTest`, `AcademicTemplateExportTest` | temas de Telegram, cálculos, exportación |

Infraestructura: `TextosDePrueba` (proveedor de textos que lee los XML reales; instalar en
`@Before`), `MainDispatcherRule`.

## Qué debe probarse (regla del proyecto)

- **Toda regla de dominio** nueva o cambiada: test puro.
- **Toda migración de Room**: caso en `RoomMigrationTest` (crea la versión anterior, migra, lee).
- **Toda escritura de repositorio** que ya falló una vez: test que pase por Room de verdad
  (`RoomRepositoriesTest`), no por el doble en memoria.
- **Todo campo nuevo del modelo**: respaldo ida y vuelta (`LocalJsonBackupRepositoryTest`).
- **Toda fórmula duplicada** (Gradle ↔ Kotlin): test que compare.
- **Texto nuevo**: existe en los dos XML (`TextosDePrueba` falla si falta en `values`).

No se hacen tests de UI Compose por pantalla; la UI la valida él en el teléfono.

## Validación manual (la que cuenta)

1. Enviar la alpha; él la instala **encima** de la anterior (misma firma, número mayor).
2. Recorrer la ruta indicada en el reporte con datos reales o del banco de pruebas / notas de ejemplo.
3. Si tocó datos: crear datos en la versión anterior → actualizar → comprobar → exportar copia →
   restaurar (regla de `PUBLICAR.md` para subir a beta).
4. Capturas numeradas de lo que no cuadre.

## Regresiones a vigilar (han pasado)

- Un arreglo «implementado» que no funcionaba (borrado de notas: el diálogo bien, la escritura no).
- Escrituras que ignoraban campos (`absenceLimit`, `termId`) y respaldos que no los guardaban.
- Animaciones invisibles (nacidas en el destino, escala dentro de `clip`, px vs dp).
- Textos que cambian de rótulo por «olfateo» de palabras clave.
- Alarmas apuntando a rutas borradas.
- La app cerrándose al abrir por `Textos` sin proveedor o por excepción de red en el `Application`.

## Criterios para dar una tarea por terminada

- Compila; `testDebugUnitTest` en verde; `check` en verde (segunda ejecución si lint del entorno).
- Textos en es y en; sin literales nuevos en código.
- Migración + test + respaldo si hubo esquema.
- Alpha enviada, commit hecho, `TDL` y `main` empujados.
- Reporte con interno/palpable y rutas.
- **Y su veredicto en el teléfono.** Hasta entonces está «implementado», no «terminado».
