# PROJECT — qué es UniStack

## En una frase

UniStack es una app Android para estudiantes universitarios que junta en un solo sitio lo que
un semestre exige llevar en la cabeza: qué nota llevas y cuánta te falta en cada materia, qué
tienes que entregar y cuándo, a qué clases puedes faltar todavía, y lo que apuntas por el
camino. Sin cuenta, sin conexión, sin pagar.

## Propósito y problema

**El problema** (INFERENCIA a partir de cómo se ha construido y de lo que él prioriza): el
estudiante lleva las notas en la cabeza o en una hoja de cálculo, no sabe con certeza qué
necesita sacar en lo que falta para pasar, pierde la cuenta de las faltas hasta que es tarde, y
las entregas viven en el grupo de WhatsApp. Las apps que existen son agendas genéricas o
calculadoras sueltas; ninguna sabe qué es un *corte*, una *meta* o un *tope de faltas*.

**Lo que UniStack hace distinto** (hechos del producto):

- **No proyecta, acota.** En vez de «vas a terminar con 4,1» —que exige suponer lo que aún no
  has hecho— enseña el **suelo** (nota final sacando 0 en lo que falta) y el **techo** (sacándolo
  todo), y dibuja la meta dentro de esa franja. Si la meta cae fuera, se ve sin leer nada.
  (DECISIÓN: «no hay proyecciones, hay suelo y techo».)
- **Entiende el semestre**: periodos académicos con fecha de inicio y fin, cortes con su peso y
  su fecha de cierre, materias que se cierran y pasan a un histórico, y una comprobación previa
  al cierre que dice qué queda a medias.
- **Asistencia honesta**: tope de faltas, las clases pasadas se marcan (o se preguntan 20 min
  después de acabar), y el historial sólo cuenta clases dentro del periodo.
- **Todo local y sin cuenta.** Los datos son del estudiante y viven en su teléfono; la copia es
  un archivo que él exporta. (La copia en la nube con Google está aplazada; el código existe.)
- **Uso libre.** Sin planes, cupos ni paywall (DECISIÓN, 2 sep 2026).

## Usuarios

- **Usuario objetivo:** estudiante universitario. Se eliminaron primaria y secundaria del
  onboarding y del modelo el 27 ago 2026 («priorizar universidad»).
- **Usuario real hoy:** el propio desarrollador, a diario en su semestre, y un círculo pequeño
  que recibe betas (mencionado como «tu amigo» en `PUBLICAR.md`). `UNKNOWN`: cuántas personas.
- **Contexto:** Colombia (INFERENCIA: COP por defecto, vocabulario «corte», «parcial»,
  «semestre 2026-1», escala 0–5 por defecto, zona horaria Bogotá en tests). Soporte por un grupo
  público de Telegram (`@unistacksoporte`).

## Casos de uso (los que la app resuelve hoy)

1. Registrar una nota y ver al instante cuánto falta para la meta de esa materia.
2. Saber qué toca hoy: clases, entregas, lo vencido — en el hero de Inicio y en la agenda.
3. Marcar asistencia (desde la notificación o desde el historial) y ver cuántas faltas quedan.
4. Cerrar un corte con sello, y un semestre con comprobación previa, sin perder el histórico.
5. Apuntar algo en clase (foto de la pizarra, audio, lista) y dejarlo vinculado a la materia.
6. Llevar los gastos del mes con presupuesto y aviso.
7. Calcular «me falta X» con la calculadora, con escenarios guardables.
8. Recibir recordatorios locales exactos (entregas, clases, resumen diario) sin servidor.

## Visión y objetivos

- **Visión declarada** (`TO DEFINE` en palabras suyas; INFERENCIA por sus prioridades): que un
  estudiante universitario lleve todo el semestre desde la app y no vuelva a la hoja de cálculo.
- **Objetivo inmediato:** cerrar la TDL que él escribe y publicar la **1.0.0 pública** (hoy
  publica 1.5.10 en un repo de releases público, pero la landing y el reporte de errores
  bloquean abrirla a todo el mundo).
- **Objetivos de calidad que sí ha fijado:** que **no se pierdan datos** (única regla dura para
  subir de alpha a beta), que la UI sea Material 3 Expressive de verdad y no «genérica de
  Material», que todo texto tenga dos idiomas.

## Alcance

**Dentro:** notas, tareas, horario/asistencia, periodos e histórico, notas rápidas, gastos,
recordatorios, apariencia y accesibilidad extensas, actualizador propio, soporte.

**Fuera (hoy):** sincronización entre dispositivos (aplazada), colaboración entre estudiantes,
integración con plataformas de la universidad (`UNKNOWN`: nunca se ha discutido), web/iOS
(sólo existe una landing, repo privado), monetización (descartada por ahora).

## Qué NO es UniStack

- No es una agenda genérica ni un «todo list» con etiquetas: cada entidad sabe de universidad.
- No es una app de notas con IA: «UniStack AI» es una ruta con pantalla «Pronto» sin definir.
- No es un servicio: no hay backend propio, no hay cuenta obligatoria, no hay analítica.
- No es una app de pago ni freemium.

## Conceptos fundamentales

| Concepto | Qué es aquí |
|---|---|
| **Materia** (`Subject`) | Asignatura del periodo activo: nombre, color, meta, cortes, notas, un bloque de clase como mucho, tope de faltas (global). |
| **Corte** (`GradingCut`) | Tramo de evaluación del periodo con peso (%) y fecha de cierre. Se cierra a mano con «sello». La palabra `Corte` es constante (`Corte.Singular/Plural`) y se traduce. |
| **Nota** (`GradeItem`) | Lo que puso un profesor: valor en la escala, porcentaje dentro del corte. Registro, no medida: por eso cambiar la escala las borra. |
| **Suelo / techo** | `guaranteedMinimum` / `bestPossible` de `GradeCalculator`. Sustituyen a cualquier proyección. |
| **Meta** (`targetAverage`) | Nota final que el estudiante quiere; dibujada dentro de la franja suelo–techo. `TargetOutlook`: SECURED, ON_TRACK, AT_RISK, UNREACHABLE, NO_DATA. |
| **Periodo académico** (`AcademicTerm`) | Semestre/trimestre/cuatrimestre/anual/bloques, con fechas; uno activo, los demás cerrados en el histórico. |
| **Tarea** (`StudentTask`) | Entrega o trabajo con materia opcional, tipo, fecha/hora, prioridad («dificultad» en el modelo), tiempo estimado, estado de calificación (`UNDECIDED/NOT_GRADED/AWAITING_GRADE/GRADED`) y nota enlazada. |
| **Clase** (`ClassSession`) + **ocurrencia** | Bloque semanal de una materia (días, horas, aula•profesor) y cada fecha concreta con su asistencia. |
| **Asistencia** | ATTENDED / ABSENT / CANCELLED / RESCHEDULED por ocurrencia; canceladas y reprogramadas no gastan falta. |
| **Nota rápida** (`QuickNote`) | Apunte Keep-like: título, cuerpo Markdown, color, materia opcional, adjuntos copiados dentro, recordatorio, fijada, archivada, papelera. |
| **Hero** | La tarjeta grande de Inicio con la prioridad del día; también el bloque tonal superior de otras pantallas. |
| **Peldaño** | dev / alpha / beta / rc / estable. Sale del nombre de versión. |
| **Gesto** (Movimiento) | Cada animación elegible en Ajustes → Movimiento, con variantes. |
| **Banco de pruebas** | Pantalla oculta (dev/alpha) con datos de muestra y palancas. |

Glosario completo: [`GLOSSARY.md`](GLOSSARY.md).

## OPEN QUESTIONS

- Visión y posicionamiento en palabras del desarrollador: `TO DEFINE` (el pitch kit propone una
  formulación; él debe validarla).
- Tamaño y perfil del círculo de beta: `UNKNOWN`.
- Si habrá integración con plataformas universitarias o multi-dispositivo real más allá de la
  copia en la nube: `UNKNOWN`.
