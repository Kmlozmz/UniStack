# CURRENT_STATE — fotografía al 12 sep 2026

Segundo archivo que se lee. Se actualiza al cerrar cada tanda; si la fecha de arriba es vieja,
el `git log` de la rama `TDL` manda.

## Números

| | |
|---|---|
| Rama de trabajo | `TDL` (= `main`, fast-forward al cerrar cada tanda) |
| Último commit | `f06d392` — `feat(i18n): support, templates, previews, theme names and the last loose texts read resources` |
| Última alpha enviada | `1.6.0-alpha.95` (envío #99 por el bot, 11 sep 2026) |
| Siguiente alpha | `1.6.1-alpha.5` (serie 1.6.1 desde el 13 sep 2026) |
| Última versión **publicada** en GitHub | `1.5.10` (26 ago 2026) |
| Base de datos | Room v21, 12 tablas |
| Código | 307 ficheros Kotlin, ~84.500 líneas; 2.733 cadenas por idioma (es, en) |
| Tests | 55 ficheros, **534 tests, todos en verde** (`./gradlew testDebugUnitTest`) |
| Historial | 441 commits desde el 1 may 2026 |

## Qué funciona (existente y usado a diario)

Todo lo listado como *existente* en [`PRODUCT.md`](PRODUCT.md): Inicio con hero de prioridad,
Académico (materias, notas por corte, suelo/techo, estadísticas, cierre de corte con sello),
Tareas (con la lógica antigua: ver abajo), Horario (semana, agenda, asistencia con historial
rediseñado, aviso 20 min tras la clase), periodos académicos con histórico y cierre comprobado,
Notas rápidas (Keep-like, Markdown, adjuntos, recordatorios, archivo y papelera), Gastos (con
lecturas Comparado/Ritmo/Calendario), Ajustes (Apariencia con 28 temas y seis puertas,
Movimiento con 7 gestos/39 variantes, Accesibilidad con 17 ajustes, Notificaciones, Datos y
respaldos locales, Configuración académica editable, Idioma), Soporte (recursos, FAQ, ticket
a Telegram, calculadora de promedio), actualizador desde GitHub Releases, plantillas de trabajos
(sólo dev/alpha/beta).

## Qué no funciona o está a medias

- **Conectar con Google / copia en la nube**: el código existe (`FirebaseGoogleAuthService`,
  `FirebaseCloudBackupRepository`) pero **no hay `google-services.json`**; el botón está al 45 %
  con insignia «Pronto». Aplazado a petición suya hasta que exista el proyecto de Firebase.
- **Trabajos (plantillas académicas)**, **UniStack AI** y **Labs**: rutas con pantalla «Pronto»
  fuera de dev/alpha/beta. Trabajos tiene contenido real pero está por «fundir dentro de Tareas»
  (`PENDING`, decidido el 19 ago).
- **Enlace de descarga de la landing** sigue apuntando al repo privado (`PENDING`, bloquea la
  1.0.0 pública desde agosto).
- **Rediseño M3E de Notas y de Configuración académica/histórico**: funcional, pero él lo
  calificó de «muy virgen / funky» y «no coincide mucho con M3E». En el saco de «rediseñar».
- **La app en inglés no la ha probado él todavía** (i18n cerrado el 11 sep; 534 tests pasan,
  falta su ojo).
- **La hoja de nota que sólo cierra con el asa**: sin veredicto suyo.

## Qué se está haciendo ahora mismo

**Rediseño de Tareas — implementación de la propuesta D.** Decidido el 12 sep 2026 en el artifact
`https://claude.ai/code/artifact/9e646565-dfd7-4a7b-a22c-7ce3bf18c1a1` (v3):

- Lista como agenda (de B): hero tonal «Esta semana» con **anillo ondulado de hechas** (de C),
  tira de siete días con puntos, riel de materias con cuenta, rótulos por día
  («Ayer · Hoy · Mié 16 · Más adelante · Hechas»), filas de lista (no tarjetas), «Entregadas sin
  nota» arriba en tarjeta tonal.
- Detalle (de A): **hoja** al tocar, con botón **«abrir entera»** que empuja la pantalla completa
  (con línea de vida y relacionado). Materia como fila tocable que abre `SubjectDetailScreen`.
- **Subtareas en esta ronda** → tabla nueva en Room (v22) y sección en el formulario.
- Lógica nueva común: **hecha ≠ entregada** (`gradingStatus` se decide por el tipo de tarea:
  taller/examen/quiz/ensayo/proyecto/exposición se califican; lectura/práctica/otro no);
  snackbar en vez de diálogo al marcar; posponer (mañana / próximo lunes / calendario) desde el
  detalle; registrar la nota en su hoja; menú ⋮ fuera de la tarjeta; buscador tras la lupa;
  hoja de filtros como la actual; sección «Tareas de esta materia» en el detalle de materia.
- Alcance estimado en el artifact: ~1 semana + la tabla.

Lo que **no** entra (a propósito): cronómetro/«empezar por algo», «ponerla en el horario».

## Último objetivo cerrado

**Accesibilidad e idiomas** (11 sep 2026): i18n real con `Textos` como proveedor global, 2.733
cadenas en dos idiomas, tests leyendo los XML reales (`TextosDePrueba`). Fuera de recursos
sólo quedan textos de desarrollo (`BancoDePruebas`, `NoteSamples`) y nombres propios.

En la misma tanda: Movimiento recortado a **7 gestos / 39 variantes** (Base: velocidad, carga ·
Transiciones: transición, listas · Momentos: sello, celebración, clase ahora); el aviso de
20 min abre el historial con la clase lista para marcar; check de asistencia verde/rojo fijo.

## Siguiente objetivo (después de Tareas)

Fase de **definir/planear**, en este orden que él dio: recursos · UniStack AI · trabajos · labs ·
reporte de errores. Cada uno arranca con un artifact de propuestas, no con código.

Después, según su TDL: rediseñar Apariencia con más opciones (carrusel M3E), M3E de Notas y de
Configuración académica/histórico. Aplazados por él: términos y privacidad (con la landing),
conectar con Google, quitar el sistema de módulos.

## Bloqueadores

| Bloqueador | Bloquea | Quién lo desbloquea |
|---|---|---|
| Sin proyecto de Firebase (`google-services.json`) | Conectar con Google, copia en la nube | El desarrollador |
| Landing apunta al repo privado | Publicar 1.0.0 pública | El desarrollador |
| Sin GitHub Actions | Publicación reproducible en CI (decidido para v1.0.0) | Agente, cuando toque |

## Decisiones recientes (últimos 10 días)

- **12 sep**: Tareas → propuesta D + hoja con «abrir entera» + subtareas ya (DECISIÓN).
- **11 sep**: Movimiento se recorta a 7 gestos; lo quitado no vuelve (DECISIÓN). Aviso post-clase
  abre asistencias. Check de asistencia verde/rojo fijo, no del tema.
- **11 sep**: i18n: todo a resources; `Textos` se instala antes de `super.onCreate()`.
- **10-11 sep**: historial de asistencias rediseñado con ruedas de visto/equis (no siluetas M3E);
  celebración a pantalla completa con mensaje; hero de Inicio que sabe si estás en clase.
- **3 sep**: Gastos cerrado (dos estilos de gráfico elegibles en la propia pantalla, no en
  Apariencia).
- **2 sep**: Pro/planes borrados enteros; Notas rápidas cerradas; Configuración académica
  editable cerrada.

## Archivos calientes (los que más se tocan en la tanda actual)

`feature_tasks/presentation/TasksScreen.kt` (2.223 líneas, se va a partir),
`feature_tasks/presentation/AddTaskScreen.kt` (1.759), `feature_tasks/presentation/TasksViewModel.kt`,
`feature_tasks/domain/StudentTask.kt`, `feature_grades/data/local/UniStackDatabase.kt` (migración 21→22),
`feature_grades/presentation/SubjectDetailScreen.kt` (sección de tareas), `core/navigation/AppNavGraph.kt`
(ruta del detalle a pantalla), `res/values/strings.xml` y `values-en/strings.xml`.
