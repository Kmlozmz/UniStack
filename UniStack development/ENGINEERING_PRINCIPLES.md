# ENGINEERING_PRINCIPLES — cómo se construye aquí

**Explícito** = lo dijo el desarrollador o está escrito en el repo. **Inferido** = patrón
observado en cómo se ha trabajado; puede corregirse.

## Arquitectura

- **Explícito — Un sitio para cada cosa.** Toda cuenta de notas sale de `GradeCalculator`; toda
  regla de recordatorios, de `ReminderTiming`; la escalera de versiones, de `versionCodeFor()`;
  el catálogo de Movimiento, de `MotionCatalog` («añadir un gesto es una entrada allí y nada
  más»). Cuando dos pantallas calculaban lo mismo distinto, se centralizó y se dejó test.
- **Explícito — Dominio puro y testeable.** Las reglas viven en objetos Kotlin sin Android
  (`TermCloseCheck`, `SubjectAttendanceHistory`, `DailyPriorityEngine`, `NoteMarkdown`,
  `ExpenseInsights`) con sus tests. Los ViewModels orquestan, no calculan.
- **Explícito — Un solo fichero nombra Material** (`UniStackExpressive.kt`): absorber renombres
  de la alpha en un sitio.
- **Inferido — Organización por features, un módulo.** No se ha partido en módulos Gradle y no
  duele; no proponerlo sin motivo.
- **Explícito — Estados comprobables, no deducidos.** `TargetOutlook` sale de comparar meta con
  suelo/techo; antes se deducía de un `null` ambiguo. `heroLabel()` aún adivina por palabras
  clave y está marcado como trampa.

## Simplicidad vs abstracción

- **Explícito — Ningún ajuste sin su sitio de aplicación.** Un ajuste que se guarda y no pinta
  nada cuesta más que tres variantes que sí. Se arreglaron `surfaceStyle`, `accentIntensity`,
  `cornerStyle`, alto contraste.
- **Explícito — Nada a medias «por si acaso».** El paywall apagado costaba un permiso y
  «invitaba a escribir gates a medias en código nuevo». Se borra o se hace entero.
- **Inferido — Preferir la constante o el enum con recurso al `when` repetido**; preferir
  componentes propios (`Uni*`) con KDoc que explique cuándo se usa cada uno.
- **Inferido — No introducir librerías nuevas sin necesidad.** El motor de Markdown, la
  grabadora, el instalador OTA, el Telegram bot, todo se escribió a mano.

## Reutilización y modularidad

- **Explícito — Rediseñar es copiar la pieza que ya existe.** Ver `DESIGN_SYSTEM.md` § 6.
- **Explícito — Formularios únicos con modos** (`SubjectFormScreen` ACADEMIC/SCHEDULE) en vez de
  dos formularios que divergen.
- **Inferido — Las piezas de una pantalla grande se sacan a `*Pieces.kt`** cuando la pantalla
  pasa de ~1.000 líneas (`AttendanceHistoryPieces`, `TermPieces`, `NotePieces`). `TasksScreen`
  (2.223) se partirá en el rediseño.

## Naming y legibilidad

- **Explícito — Código en inglés, comentarios y KDoc en español, textos de usuario en resources.**
  Los identificadores nuevos de la tanda de sep 2026 mezclan español en piezas de UI
  (`HojaDeClase`, `RuedaDeEstado`, `celebracionDelDia`, `tachadoDe`) — patrón aceptado por él
  (`VERIFY` si quiere volver a inglés puro). Los del dominio siguen en inglés.
- **Explícito — El comentario explica el porqué, no el qué.** Los KDoc del repo cuentan la
  decisión y el fallo que la motivó («Aquí estaba el fallo de los modos…»). Es el estilo a imitar.
- **Explícito — Sin dato es `—`**, `NO_DATA`; `DayLabels` para días (miércoles = `X`).

## Mantenibilidad y deuda

- **Explícito — Trampas documentadas** (`CONTEXTO.md` § Trampas, `KNOWN_ISSUES.md`), no
  arregladas en silencio si tocan datos de usuario.
- **Explícito — Migraciones de Room a mano** con test; columnas «muertas» se conservan
  (`notes.format`) en vez de migrar destructivamente.
- **Inferido — Cuándo refactorizar:** cuando dos sitios discrepan (cálculo, formulario) o cuando
  un cambio de producto obliga a tocarlo. **Cuándo no:** «arreglar» decisiones deliberadas
  (Gastos rojo, escala que borra) o reescribir historia publicada de git.

## Rendimiento

- **Inferido — Suficiente con no hacerlo mal**: `LazyColumn` con `key`, `remember` para listas
  filtradas, flujos combinados una vez en el ViewModel, ticker por minuto sólo donde hace falta.
  Nunca ha sido tema de discusión; no optimizar sin medida.

## Seguridad y privacidad

- **Explícito — Nada de secretos en el APK** (ni token de GitHub ni credenciales del bot). Los
  APK son públicos, así que cualquier secreto embebido es público.
- **Explícito — Los datos son del estudiante**: locales, exportables, sin analítica, sin cuenta.
- **Explícito — No perder datos** es la única regla dura para subir de peldaño.

## Accesibilidad

- **Explícito** — 17 ajustes, descripciones en todos los icon buttons, contraste comprobado en
  claro, «reducir movimiento» respetado en todas las animaciones, formas además de color.

## Testing

- **Explícito — Se prueba la regla, no la pantalla.** Dominio y ViewModels con JUnit; Room con
  Robolectric y `RoomMigrationTest`; fórmulas duplicadas fijadas por test
  (`VersionCodeOrderTest`); cadenas leídas del XML real (`TextosDePrueba`).
- **Explícito — La validación final es el teléfono**: él instala cada alpha y manda capturas.
  «Está implementado» no significa «funciona» (aprendido con el borrado de notas).

## Manejo de errores

- **Explícito** — Los mensajes de error son de usuario y traducidos; `errorMessage` en
  `ValidationResult` con `errorRes`. Las corrutinas del `Application` no propagan excepciones
  (un `IOException` de red cerró el proceso una vez).
- **Inferido** — `runCatching` alrededor de intents externos (Telegram, instalador, enlaces).

## Dependencias

- **Explícito** — Fijadas a versiones exactas; material3 clavada. Subir versiones no es tarea
  de una tanda de producto.

## Cómo saber si algo está «bien hecho» aquí

1. Copia una pieza existente y se ve como el resto.
2. El texto está en los dos XML y el código lo pide por recurso.
3. Hay test si es regla; hay migración si es esquema.
4. `./gradlew testDebugUnitTest` en verde; `check` en verde (segunda ejecución si lint falla).
5. El reporte dice qué es palpable y cómo comprobarlo.
6. No reabre nada de `DESIGN_DECISIONS.md`.
