# GLOSSARY — términos propios de UniStack

| Término | Significado aquí |
|---|---|
| **Académico** | Pestaña con Materias y Tareas (`AppRoutes.Academic?tab=`). |
| **Alpha** | Compilación de trabajo firmada (`X.Y.Z-alpha.N`) que va por el bot y no se publica. |
| **Anillo (ondulado)** | `CircularWavyProgressIndicator` de M3E usado para «cuánto llevas» (faltas, hechas). |
| **Apunte** | Nota rápida (así se llama en el FAB de Inicio, porque «Nota» ya es una calificación). |
| **Artifact** | Página HTML interactiva publicada en claude.ai con la que se deciden los diseños. |
| **Banco de pruebas** | Pantalla oculta (dev/alpha) con datos de muestra y palancas (`BancoDePruebas`). |
| **Bot** | Bot de Telegram del desarrollador que recibe cada APK de release (`(alpha) #N`). |
| **BuildStage / peldaño** | dev · alpha · beta · rc · estable; sale del nombre de versión. `allowsUnfinished` = dev/alpha/beta. |
| **Cajón / panel** | `HomeNavigationPanel`: accesos a herramientas y ajustes desde Inicio. |
| **Celebración** | Momento a pantalla completa (confeti/onda/destello/…) al cumplir la meta o cerrar la última tarea del día. |
| **Clase** / **ocurrencia** | `ClassSession` (bloque semanal) / `ClassOccurrence` (fecha concreta con asistencia). |
| **Corte** | `GradingCut`: tramo de evaluación con peso y fecha de cierre. Constante `Corte.Singular/Plural` (traducible). Persistido como `period-N`. |
| **Cubetas** | Agrupación antigua de Tareas: Vencidas / Hoy / Próximas / Más adelante. |
| **Días sin clase** | `AcademicBreak`. |
| **Empuje (iOS)** | Transición de navegación: la nueva entra entera y la anterior se aparta un tercio. |
| **Entregada** | Estado de tarea calificable: hecha y esperando nota (`AWAITING_GRADE`). |
| **Escalera** | Orden de versiones y `versionCode` (`PUBLICAR.md`). |
| **Gesto** | Cada animación elegible en Ajustes → Movimiento, con variantes (`MotionCatalog`). |
| **Hecha** | Tarea completada sin nota (`completed = true`, `NOT_GRADED`/no calificable). |
| **Hero** | Tarjeta tonal grande: la de prioridad en Inicio, la de faltas en asistencias, la de semana en Tareas D. |
| **Histórico** | Periodos cerrados con sus materias selladas (Ajustes › Histórico académico). |
| **Hoja** | Bottom sheet con la forma de la app (`background`, `extraLarge`, tirador, 18 dp). |
| **Latido** | Pulso de escala en lo vencido (`latidoDeVencido`). |
| **Materia** | `Subject`. |
| **Meta** | `targetAverage`, la nota final deseada; se dibuja dentro de la franja suelo–techo. |
| **MetricCard** | Tarjeta de cifra de 58 dp con icono; al tocar abre una hoja con lo que hay detrás. |
| **Módulo** | `AppModule` (GRADES, TASKS, EXPENSES, ACADEMIC_TEMPLATES) encendible/apagable; sistema por quitar. |
| **Movimiento** | Sección de Ajustes con los gestos; también el runtime (`MotionRuntime`, `duracion`). |
| **Palpable / interno** | Clasificación de cada cambio en el reporte: se ve en la app / no se ve. |
| **Periodo (académico)** | `AcademicTerm`: semestre, trimestre, cuatrimestre, anual, bloques. |
| **Ponerse al día** | `CatchUpSheet`: marcar asistencia de clases pasadas sin marcar, por día. |
| **Pronto** | Etiqueta de lo que existe pero no se abre fuera de dev/alpha/beta. |
| **Riel** | Fila horizontal de chips de materias con cuenta (Tareas D). |
| **Rueda (de estado)** | Círculo lleno con visto/equis/guion/flecha para asistencia; aro vacío = sin marcar, punteado = por venir. |
| **Sello** | Animación al cerrar un corte a mano (golpe/onda/tinta/cinta). |
| **Suelo / techo** | `guaranteedMinimum` / `bestPossible`: nota final con 0 en lo que falta / con todo. |
| **TDL** | La lista de pendientes que escribe el desarrollador («to-do list»); también la rama de trabajo `TDL`. |
| **Textos** | `core/utils/Textos`: proveedor global de cadenas para código no-Compose; `TextosDePrueba` en tests. |
| **Tira (de la semana)** | Siete días L–D con puntos por tarea, filtra por día (Tareas D). |
| **Tope de faltas** | `absenceLimit` global del perfil. |
| **Veredicto** | Tarjeta al cerrar un corte: meta cumplida / aprobado / en riesgo… |
| **Vencida** | Tarea pendiente cuya fecha pasó; no es un estado, es una fecha. |
