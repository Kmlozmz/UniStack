# PRODUCT — UniStack como producto

Estados: **Existente** (en la app, usado) · **En desarrollo** (código en curso) · **Planeado**
(decidido, sin código) · **Idea** (mencionado, sin decidir) · **Descartado** (no volver a proponer).

## Módulos y pantallas

La barra inferior tiene **cuatro pestañas fijas**: Inicio · Académico · Horario · Gastos.
Académico contiene las pestañas Materias y Tareas. El resto se abre desde el cajón/panel de
Inicio o desde Ajustes. Existe un **sistema de módulos** (`AppModule`: GRADES, TASKS, EXPENSES,
ACADEMIC_TEMPLATES) que apaga secciones; él quiere quitarlo y dejar sólo «ocultar» sin preguntar
en el onboarding — **no tocar hasta que lo pida** (28 ago 2026).

### Inicio — Existente
- Saludo, **hero de prioridad** (`DailyPriorityEngine` + `HomeSummaryFactory`): compite entre
  entrega urgente, clase en curso/próxima (sabe si estás en clase, ticker por minuto), plan del
  día con tiempo estimado; acción contextual (abrir tarea, horario…).
- Agenda del día, «snapshot» con cifras, panel de navegación (cajón) con las herramientas.
- FAB con menú: nota (calificación), tarea, apunte, gasto.
- Configurable en Ajustes → Tu inicio (secciones, orden).

### Académico › Materias — Existente
- Lista de materias con promedio, barra de avance (`EvaluationBar`: longitud = avance, color =
  identidad, nunca rendimiento), selección múltiple con pulsación larga.
- Detalle de materia: cortes, notas por corte, **`OutcomeRangeBar`** (suelo–techo con la meta),
  veredicto al cerrar corte (`TarjetaDeVeredicto`), **sello de cierre** animado, celebración si
  se cumple la meta, historial previo, estadísticas, «Nueva nota» (apunte) desde el ⋮.
- Formulario único crear/editar (`SubjectFormScreen`, modos ACADEMIC/SCHEDULE) con bloques
  Identidad / Cuándo / Académico.
- Registrar nota (`AddGradeScreen`) con validación contra máximo y suma de porcentajes; el
  corte se elige por fecha si hay fechas de corte.
- **Cambiar la escala borra todas las notas** (doble confirmación, conteo real) — DECISIÓN.

### Académico › Tareas — Existente (lógica antigua) → **En desarrollo (rediseño D)**
- Hoy: buscador, tres `MetricCard` (Hoy/Semana/Vencidas) con hoja, filtro en hoja
  (estado/materia/prioridad/orden), secciones Vencidas/Hoy/Próximas/Más adelante/Completadas,
  tarjeta con casilla + ⋮, banner «Entregadas sin nota», diálogo «¿lleva nota?» al marcar
  cualquier tarea con materia, registro de nota enlazado (crea `GradeItem`), deslizar para
  borrar, latido en lo vencido, celebración al cerrar la última del día.
- Lo que cambia con D: ver [`CURRENT_STATE.md`](CURRENT_STATE.md) § Qué se está haciendo.

### Horario — Existente
- Vista de semana/día con bloques de clase, agenda (eventos propios), clase en curso destacada,
  «ponerse al día» (`CatchUpSheet`) para marcar asistencia pendiente agrupada por día.
- **Historial de asistencias** rediseñado (11 sep): hero tonal (verde/ámbar/rojo según faltas
  restantes), anillo ondulado, mapa del periodo por semanas, filas por clase con **rueda de
  estado** (visto/equis/guion/flecha), hoja para editar una clase pasada (estado, modalidad,
  motivo, nota). Lo por venir no se edita — DECISIÓN.
- Aviso **20 min después de cada clase** con botones Asistí/Falta; el cuerpo abre el historial
  con esa clase lista para marcar (11 sep).
- Tope de faltas **global** (vive en el perfil) — DECISIÓN 28 ago.

### Periodos académicos — Existente
- Onboarding pregunta tipo, nombre y fechas; cortes con fecha de cierre (`endEpochDay`).
- Ajustes › Histórico académico: lista de periodos cerrados, detalle, **comprobación previa al
  cierre** (`TermCloseCheck`, 19 tests) con doble confirmación escribiendo CERRAR si queda algo
  a medias; sin periodo activo Inicio ofrece crear uno; el nuevo hereda escala/aprobado/cortes y
  ofrece repetir materias como **copia vacía** (`repeatedFromSubjectId`).
- Días sin clase (`AcademicBreak`), «Tu periodo» editable.

### Notas rápidas — Existente (diseño cerrado; UI «por acercar al artifact»)
- Lista cuaderno/mosaico, Fijadas/Otras, buscador tras la lupa, filtro por materia en hoja,
  orden por modificación/creación, archivo y papelera con deshacer, selección múltiple.
- Editor a pantalla completa Keep-like: título, cuerpo con **Markdown propio** (`NoteMarkdown`)
  formateado al vuelo, barra de formato, casillas marcables, tabla, color, recordatorio,
  adjuntos (galería, cámara, archivo, grabadora propia) **copiados dentro de la app**, mención
  `@materia` sin tildes, sugerencia «Estás en Cálculo II» con borde cónico que gira.
- Notas de ejemplo (sólo dev/alpha/beta) desde el ⋮.

### Gastos — Existente (cerrado 3 sep)
- Registro por categoría (categorías administrables), filtros Todo/Semana/Mes, un día = una
  tarjeta, presupuesto semanal/mensual con aviso arriba al pasarse, **dos estilos de gráfico
  elegibles en la propia pantalla** (barras / anillo con galleta) — no en Apariencia, DECISIÓN.
- «Ver más» → tres lecturas: Comparado, Ritmo, Calendario (`ExpenseInsights`, 13 tests).
- Rojo como identidad de la sección — DECISIÓN.

### Ajustes — Existente
- **Apariencia** (hub de seis puertas con maqueta arriba): tema y color (28 temas: 14 propios +
  Catppuccin, Tokyo Night, Dracula, Nord, Gruvbox, Solarized, Rosé Pine, One Dark, Everforest,
  Monokai; Monet apagado por defecto; OLED), forma y superficie, tipografía, componentes
  (barra con/sin texto, progreso recto/ondulado, icono en interruptor, chips, campos, iconos,
  distintivos), **Movimiento** (7 gestos con variantes, cada una animada en vivo), tu inicio.
- **Accesibilidad e idiomas**: 17 ajustes (contraste, paleta para daltonismo, formas además de
  color, texto en negrita, fuente de lectura, tamaño de toque, transparencia, una mano, duración
  de deshacer, descripciones habladas, confirmar irreversibles, pantalla encendida, escala de
  texto, movimiento, 24 h, formato de fecha, moneda) e **idioma** (sistema/es/en).
- Notificaciones (por tipo, antelación, resumen diario con hora, horas de silencio),
  Configuración académica (escala, cortes y fechas, tope de faltas, días sin clase, tu periodo),
  Módulos, Datos y respaldos (exportar/importar JSON; nube «Pronto»), Cuenta y perfil (foto
  local, programa, institución), Actualizaciones (comprueba GitHub, instala APK), Novedades.

### Soporte y herramientas — Existente
- Recursos (enlaces), Ayuda/FAQ, **Escríbenos** (ticket con versión y dispositivo copiado al
  portapapeles + abre el tema de Telegram por `tg://`), Acerca de.
- **Calculadora de promedio** (Materia / Semestre / Me falta, escenarios guardados).
- **Banco de pruebas** (dev/alpha): datos de muestra y palancas.

### Actualizaciones — Existente
- `UpdateCheckWorker` cada 2 h y al abrir si pasaron 45 min; consulta `/releases` del repo
  público `Kmlozmz/UniStack-releases`; banner + hoja con notas; descarga e instala el APK (pide
  permiso de origen desconocido una vez). Sin canales ni códigos — DECISIÓN 19 ago.

## En desarrollo
- **Tareas, propuesta D** (ver `CURRENT_STATE.md`).

## Planeado (decidido, sin código)
- **Definir/planear** (en este orden): recursos · UniStack AI · trabajos (fundir en Tareas) ·
  labs · **reporte de errores** (lo único que el agente considera bloqueante para público
  general; él lo aceptó en la lista).
- **Rediseñar**: Apariencia con más opciones (carrusel M3E aparcado a propósito), M3E de Notas
  y de Configuración académica/histórico.
- **CI/CD con GitHub Actions** al preparar v1.0.0.
- **Migrar alarmas antiguas** para borrar las rutas `grades`/`tasks` de redirección; auditoría
  completa de navegación.
- **Separar el token de Gastos del de error** (`Coral` sirve para los dos).
- **Aplazados por él**: términos y privacidad (con la landing), conectar con Google (cuando
  exista Firebase), quitar módulos.

## Ideas (mencionadas, sin decidir)
- Adjuntar media al crear tareas (TDL antigua; con D se cubre parcialmente vía notas relacionadas: `VERIFY`).
- Respaldo en zip que se lleve los archivos de las notas (hoy la copia lleva la ficha, no los bytes).
- Créditos por materia para ponderar el promedio general (hoy media simple).
- Rediseñar el flujo de «Añadir materia» (TDL antigua).
- Pro/planes «quizá en el futuro, debidamente planeado» — no es un plan.

## Descartado (no proponer)
- Planes de pago, límites, paywall (borrado entero el 2 sep 2026).
- Canales de actualización y códigos de acceso (19 ago).
- Publicar alphas en GitHub (17 ago): las alphas van sólo por el bot.
- Firebase App Distribution / proxy propio para cerrar descargas.
- Convertir notas al cambiar de escala.
- Ascender una casilla de nota rápida a tarea.
- Subrayado en notas (Markdown no lo tiene; se usa tachado).
- Variantes de animación quitadas de Movimiento (ver `FAILED_APPROACHES.md`).
- Barra de navegación flotante; rebote al desplazar; encabezados que encogen; gesto atrás predictivo.
- Tipos de nota distintos al crear (todas iguales por dentro, Keep).

## Flujos principales (resumen)

1. **Onboarding** (`feature_setup`): permisos → ¿qué estudias? (área, programa, institución) →
   tu periodo (tipo, nombre, fechas) → ¿ya empezó? → cortes y sus fechas → módulos → listo.
2. **Nota**: FAB o detalle → elige materia → corte (por fecha) → valor y % → si completa el
   100 % del corte, el sello cierra y el veredicto aparece; celebración si se cumple la meta.
3. **Tarea** (hoy): FAB → formulario → lista; marcar → diálogo nota → registrar → enlazada.
   Con D: marcar → snackbar; hoja → materia / posponer / nota.
4. **Asistencia**: notificación 20 min después → Asistí/Falta, o abrir historial → clase → estado.
5. **Cierre de semestre**: Ajustes › Histórico → Cerrar → comprobación → confirmar → nuevo periodo
   con herencia y materias repetidas vacías.

## Prioridades de producto (cómo las ordena él)

1. Que no se pierdan datos.
2. Que se sienta M3 Expressive y coherente con el resto de la app.
3. Que cada ajuste **haga algo** («ningún ajuste entra sin su sitio de aplicación»).
4. Cerrar la TDL punto por punto, en su orden.

## Trade-offs asumidos
- Sin backend → sin sincronización real; a cambio, sin cuenta y sin coste.
- `targetSdk 35` → sin gesto atrás predictivo forzado; a cambio, no se aprovecha lo nuevo de 36.
- `material3` alpha clavada → componentes M3E hoy; a cambio, un solo fichero nombra Material
  (`UniStackExpressive.kt`) para absorber renombres.
- Todo texto en resources → dos idiomas de verdad; a cambio, `Textos` global para código no-Compose.
