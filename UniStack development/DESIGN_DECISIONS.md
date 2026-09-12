# DESIGN_DECISIONS — registro de decisiones cerradas

Cada entrada: qué se decidió, contexto, alternativas, por qué, y qué **no** hacer. Ordenadas por
área; la fecha es la de la decisión. Las marcadas **DECISIÓN** las tomó el desarrollador; las
marcadas *(agente, sin objeción)* las tomó el agente y él no las cambió.

## Producto

### Uso libre, sin planes — DECISIÓN 20 ago, cerrado 2 sep 2026
- Contexto: existía un módulo `feature_billing`, `FeatureGate.PRO_FEATURES_ENABLED = false`,
  pantalla Pro, tope de materias.
- Alternativas: dejarlo apagado «por si acaso» / borrarlo.
- Elegido: **borrar todo** (commit `804863b`). «No habrá límites para nada y nada se esconderá
  detrás de un paywall. Quizá en el futuro… pero debidamente planeado.»
- No hacer: gates a medias, permisos de billing, ni reintroducir «Pro» sin planificarlo entero.

### Priorizar universidad — DECISIÓN 27 ago 2026
- Se eliminaron `EducationLevel`, `gradeLevel` y `AcademicPeriodLabel`; «Corte» es constante
  traducible y «periodo» queda libre para el semestre.
- No hacer: adaptar la app a primaria/secundaria (estaba en la lista antigua; ya no).

### Histórico por semestre antes que asistencias — DECISIÓN 27 ago 2026
- Contexto: la asistencia inventaba 120 días hacia atrás desde la regla de repetición.
- Elegido: primero el periodo académico (con fechas), luego el resto. Hecho entero el 28 ago.

### Cambiar la escala borra las notas — DECISIÓN 12 ago 2026
- Alternativa: convertir (85/100 → 4,3/5). Rechazada: inventa un número que nadie dio y no es
  reversible con un decimal.
- Implementado: doble confirmación, conteo real, sin aviso si no hay notas, reajuste de
  `targetAverage`. No hay respaldo automático previo.

### Tope de faltas único, en el perfil — DECISIÓN 28 ago 2026
- «No es como que haya un tope de faltas por materia.» Se puede cambiar desde el historial de
  una materia, pero guarda el mismo valor para todas.

### Notas rápidas: replicar Google Keep — DECISIÓN 29 ago 2026 (manda sobre el artifact original)
- Cabecera = barra de buscar; todas las notas iguales por dentro; casillas recogidas «+ N
  marcadas»; filtro en hoja; recordatorios «clave»; cuaderno por defecto; normal (no Markdown)
  por defecto; `@` para vincular materia (no `#`); sugerencia «Estás en Cálculo II» sólo en la
  píldora; escribir es pantalla completa.
- Descartado: ascender casilla a tarea; subrayado; tipos de nota; fondos ilustrados (no
  objetado, no hecho).

### Asistencias — DECISIÓN 10–11 sep 2026
- Estados con **ruedas** (visto/equis/guion/flecha), no siluetas M3E. Hero tonal sin franja.
  Marcar = sólo rebote. Sólo lo pasado se edita; cancelada y reprogramada no gastan falta
  *(agente, sin objeción)*. Aviso a los 20 min (no 10) y su cuerpo abre el historial con esa
  clase.

### Movimiento: 7 gestos — DECISIÓN 11 sep 2026 (segundo recorte)
- De 25 gestos/130 variantes (3 sep) a 12/68 (alpha.94) a **7/39**. Quitados con su valor por
  defecto fijado: registrar nota (Cae y empuja), tachar (Línea), latido (Pulso), guardado
  (Punto), fijar (Despega), marcar asistencia (rebote), presupuesto (aviso arriba).
- No hacer: volver a ofrecer variantes de lo quitado.

### Gastos — DECISIÓN 2–3 sep 2026
- Rojo como identidad. Filtro por defecto «Todo»; chips Todo/Semana/Mes; un día = una tarjeta.
- El **selector de estilo de gráfico va en la propia pantalla**, no en Apariencia: «cambia una
  tarjeta, no la app».

### Actualizaciones sin canales — DECISIÓN 19 ago 2026
- Se borraron `UpdateChannel`, `ChannelAccess`, códigos, periodo de gracia. La app toma siempre
  la última publicación de GitHub. Los APK son públicos a propósito (13 ago). Alphas ya no se
  publican (17 ago): sólo por el bot.

### Tareas: propuesta D — DECISIÓN 12 sep 2026
- Combinación elegida por él: lista por materia y por día (B) + hoja de detalle (A) + anillo de
  hechas (C); **hoja con «abrir entera»**; **subtareas en esta ronda**; por días. Lógica
  hecha≠entregada por tipo; snackbar en vez de diálogo. Ver `CURRENT_STATE.md`.

## Interfaz

### Nada de franjas de color al costado — DECISIÓN 11 sep 2026 («AI slop»)
### El logo es el PNG — DECISIÓN 21 ago 2026
### Barra inferior acoplada, indicador `secondaryContainer` — DECISIÓN 18/22 ago 2026
- Conflicto documental: `CONTEXTO.md` dice que el indicador usa el contenedor del acento;
  **la versión más reciente** (esquema desde semilla, `UniStackExpressive.kt`) usa
  `secondaryContainer`, el del spec, y no se fuerza vía `NavigationItemColors`.
### Empuje iOS sin fundido; fuera rebote, encabezados que encogen, gesto predictivo — DECISIÓN 17–19 ago
### `targetSdk 35` a propósito — DECISIÓN
### Monet apagado por defecto — DECISIÓN
### El hero de Inicio con color propio (no el acento pleno) — DECISIÓN 12 ago
### El fondo oscuro es `#0A0C11` — DECISIÓN (28 ago)
### Rediseñar = copiar la pieza de la app — DECISIÓN 2 sep 2026 («sólo hazlo como el sheet de la app»)
### Las tres cifras de Tareas son la `MetricCard` de 58 dp de Horario/Inicio, con hoja al tocar
### Todo momento animado lleva texto — DECISIÓN 11 sep («esos círculos solos no van»)
### Celebración a pantalla completa, sólo al cumplir la meta / cerrar la última del día — DECISIÓN 11 sep

## Ingeniería

### Textos en resources, `Textos` global — DECISIÓN de arquitectura, sep 2026 (agente; él lo pidió como «i18n de verdad»)
### `versionCode` desde el nombre, tres dígitos de peldaño — DECISIÓN 13/20 ago 2026
### Alphas por delante de la beta que preparan — regla 19 ago 2026
### `CHANGELOG.md` obligatorio para publicar — DECISIÓN
### Repo público aparte para releases — DECISIÓN 12–13 ago 2026
### Commits en inglés, Conventional Commits, sin trailer — DECISIÓN 8 ago 2026
### El bot recibe todo APK de release, uno por tanda, sin avisos — DECISIÓN 21/28 ago 2026
### No reescribir historia publicada sin consultar — regla (94 commits antiguos en español siguen)
### Todo cálculo de notas sale de `GradeCalculator`; suelo y techo, no proyecciones — DECISIÓN de dominio
### El corte activo lo elige el usuario; un corte con 100 % se cierra a mano con sello (Room v21)

## Conflictos entre documentos (y cuál manda)

| Tema | `CONTEXTO.md` (19 ago) / `PUBLICAR.md` (13 ago) dicen | Estado real (más reciente) |
|---|---|---|
| Rama de trabajo | `test` (PUBLICAR) / `main` y espejo `test` (CONTEXTO) | **`TDL`**, con `main` en fast-forward al cerrar tanda (sep 2026) |
| Canales alpha/beta con código | Descritos como vigentes | **Eliminados** el 19 ago (misma sesión, sección posterior lo anula) |
| Indicador de la barra | «contenedor del acento, no `secondaryContainer`» | **`secondaryContainer`** (22 ago) |
| «Nada a medio hacer fuera de dev y alpha» | BuildStage sólo dev/alpha | **dev/alpha/beta** (`allowsUnfinished` incluye beta hoy) |
| Beta «llega solo por la app con código» | PUBLICAR | Sin código; la beta se manda también por el bot |
| Transición elegible (empuje/fundido/ninguna) | CONTEXTO | Existe como gesto «transición» en Movimiento (7 gestos) — `VERIFY` variantes exactas |
| Trabajos «fundir en Tareas en 1.2.0» | CONTEXTO | Sigue `PENDING`; ahora dentro de «definir/planear» |
| Paleta teal del `MASTER.md` | design-system | **Nunca usada**; obsoleto |

Regla: ante duda, `git log -S` sobre el símbolo y la fecha del último cambio.

## OPEN QUESTIONS
- El promedio general de Materias es la media simple; decidir si se pondera (créditos) — `TO DEFINE`.
- Separar el token de Gastos del de error — `PENDING` técnico, sin decisión de color.
- Respaldo con bytes de adjuntos (zip) — pendiente de que él lo confirme.
- Tachado en vez de subrayado en notas — pendiente de su confirmación explícita.
