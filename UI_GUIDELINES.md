# UniStack UI Guidelines

Guía base para mantener una UI coherente en toda la app. La intención es que cada pantalla se sienta parte del mismo sistema: sobria, académica, premium y clara.

## Principios

- La app prioriza lectura rápida, jerarquía clara y acciones obvias.
- El dark mode usa un fondo profundo casi negro, no morados dominantes.
- El color de cada módulo funciona como acento, no como bloque gigante.
- Los CTA deben ser consistentes entre secciones: mismo tamaño visual, mismo radio y misma posición relativa.
- Evitar duplicar acciones dentro de empty states si ya existe un CTA principal.

## Paleta

### Base Dark

- Background principal: `#080B13`.
- Surface/card: `#10131B`.
- Surface elevada o sheet: `#121620`.
- Bordes: blanco con alpha bajo (`0.06f` a `0.12f`).
- Bottom bar: mismo tono del background, con elevación visual mínima.

### Base Light

- Background principal: `#FCFBFF`.
- Cards: blanco o `surfaceContainerLowest`.
- Bordes: `outlineVariant` o `SoftOutline` con alpha suave.

### Acentos

- Primario UniStack: morado para navegación, selección global y acciones académicas.
- Gastos: coral para monto, categorías de gasto, CTA de gastos y selección de fechas en gastos.
- Éxito/meta alcanzada: verde/teal.
- Advertencia/cerca de meta: ámbar.
- Riesgo/lejos de meta: coral/rojo suave.

## Tipografía

- Títulos de pantalla: Bold o ExtraBold, pero solo en encabezados principales.
- Subtítulos: Medium o Normal, color secundario.
- Títulos de cards: SemiBold.
- Texto común: Normal o Medium, nunca Bold por defecto.
- Métricas principales: Bold si son el foco visual.
- Labels: Medium, tamaño menor, color secundario.
- Evitar usar mayúsculas completas salvo en etiquetas/separadores de sección muy cortos.

## Shapes

- Cards principales: `12.dp` a `18.dp`, según densidad.
- Cards de lista compacta: `8.dp` a `12.dp`.
- Inputs: `12.dp` a `16.dp`.
- Chips/segmentos: `12.dp` a `16.dp`.
- CTAs principales: mismo lenguaje que `Nueva tarea`, radio moderado, no cápsulas exageradas.
- Bottom sheets: esquinas superiores `18.dp` a `22.dp`.
- Bottom navigation: integrada al borde inferior, esquinas superiores suaves.

## Espaciado

- Padding horizontal en pantallas raíz: `24.dp`.
- Padding horizontal en pantallas internas: `24.dp`.
- Separación entre header y primer bloque: `20.dp` a `28.dp`.
- Separación entre secciones: `20.dp` a `28.dp`.
- Padding interno de cards: `16.dp` a `22.dp`.
- Pantallas raíz edge-to-edge deben reservar un arranque visual consistente cerca de `56.dp` a `60.dp` de padding superior de contenido para que los títulos respiren debajo del status bar.

## Responsive

- La adaptación responsive se decide por ancho disponible en `dp`, no por DPI ni density manual.
- Breakpoints base: Compact `< 380.dp`, Medium `380.dp..429.dp`, Expanded `>= 430.dp`.
- No usar un `scale` global ciego para toda la UI. En pantallas compactas se ajustan métricas puntuales: padding, radios, spacing, tamaños de ilustración y layout.
- Si un texto se corta en compact, primero se cambia la composición o se usan labels más cortos antes de recurrir a ellipsis.
- En Home no se optimiza para meter todo en una sola vista: se permite scroll vertical cuando eso mejora legibilidad, jerarquía y aire visual.
- En Home, las prioridades se apilan en Compact y pueden ir lado a lado en Medium/Expanded si el ancho real lo permite.
- En Home, las acciones rápidas se vuelven cards full-width en Compact antes que recortar textos; en anchos mayores pueden ir en fila.

## Componentes

### CTAs

- Deben aparecer una sola vez por pantalla si son acciones primarias.
- En pantallas raíz, colocarlos cerca de la parte inferior y alineados como el CTA de tareas.
- No usar botones duplicados dentro de empty states.
- Los CTA principales de raíz usan el mismo lenguaje visual: altura `56.dp`, radio `22.dp`, padding horizontal `16.dp`, texto oscuro `#15131D`, peso `SemiBold` y un icono `+` de `18.dp` dentro de un círculo translúcido de `24.dp`.
- Cada módulo conserva su color de botón: Tareas y Materias usan primario; Gastos usa coral.
- Home usa un FAB morado de `+` abajo a la derecha para abrir acciones rápidas; no debe mostrar una sección fija de acciones rápidas como cards dentro del scroll.
- El FAB de acciones del Home debe usar el componente compartido `UniStackFabMenu`; no crear variantes locales de FAB/sheet para esta pantalla.

### Home

- Estructura visual de Inicio: header de marca, saludo, hero `Resumen de hoy`, métricas, prioridades, semana, materia destacada, gastos semanales, FAB de acciones y bottom navigation.
- El hero de Home puede usar halo morado/azulado suave para profundidad, pero las superficies siguen siendo oscuras neutras con borde sutil.
- El hero de Home es la pieza protagonista: borde morado visible pero sutil, ilustración integrada con halo, chips cómodos y CTA outline proporcionado.
- Home debe adaptar todos sus fondos, superficies, bordes y textos al modo claro/oscuro; no se permiten fondos dark hardcodeados cuando el tema está en claro.
- El Home debe priorizar legibilidad premium sobre densidad: cards con aire, textos completos y scroll cómodo antes que miniaturas comprimidas.
- En Home se permite reducir el padding horizontal a `16.dp` para ganar ancho útil y evitar truncamientos, manteniendo el resto de pantallas raíz en `24.dp` salvo necesidad explícita.
- En Home, el hero, las métricas, las cards semanales, la materia destacada, gastos y acciones rápidas pueden crecer en altura si con eso evitan cortes y mantienen composición clara.
- En Home, la ilustración del hero debe ser proporcional y secundaria al contenido: no puede robar ancho hasta cortar chips, texto o el CTA `Ver pendientes`.
- En Home, al compactar cards se reduce altura solo si los textos siguen completos y las acciones permanecen visibles; nunca se acepta un botón o chip aplastado.
- Home no debe inventar prioridades: `Materia crítica`, `Gastos altos` o cards de prioridad solo aparecen cuando hay datos reales que lo justifiquen.
- En Home, si no hay datos reales todavía, el hero usa un mensaje de construcción del resumen y oculta CTA/chips de urgencia que sugieran pendientes inexistentes.
- En Home, las cards informativas no deben sentirse infladas: se prefieren alturas compactas con texto legible antes que bloques altos con exceso de aire vertical.
- Las métricas de Home se agrupan en una card horizontal con divisores internos finos.
- Las prioridades del Home se muestran apiladas full-width en móvil para evitar truncamientos y mantener lectura cómoda.
- Las acciones rápidas del Home viven detrás del FAB morado, no como cards permanentes ni fila de pills.
- En Home, las cards de materia y gastos deben priorizar composición horizontal cuando haya ancho suficiente: métrica, progreso/contexto y acción o gráfico separados por divisores sutiles.

### Tareas

- La creación/edición de tareas usa `Título`, `Descripción` opcional sin límite de caracteres, fecha límite, materia, tipo y prioridad.
- La selección de materia en tareas se abre en bottom sheet, no en dropdown flotante; debe incluir `Sin materia asignada`, materias existentes y acción `Crear nueva materia`.
- Los tipos de tarea visibles deben cubrir casos académicos frecuentes: taller, parcial, ensayo, exposición, investigación, examen, práctica, proyecto, lectura y otro.

### Formularios Internos

- Los formularios internos con flecha atrás deben usar `statusBarsPadding()` y un offset superior adicional de `16.dp` para que la flecha no invada la status bar y el título respire a una altura coherente con las pantallas raíz.
- La separación entre flecha y título puede mantenerse compacta, pero el bloque completo nunca debe arrancar debajo de los iconos del sistema.

### Date Pickers

- Todos los selectores de fecha deben usar el mismo sistema de color.
- Superficie oscura: `#080B13`.
- Celdas: `#10131B`.
- Texto principal claro.
- Acento contextual: coral para gastos, primario para tareas si el contexto académico lo requiere.

### Sheets y Dialogs

- Fondo alineado al background global, no morado dominante.
- Contenido en cards/superficies discretas.
- Botón principal con contraste alto.
- Evitar listas enormes visibles si puede usarse un selector compacto.

### Empty States

- Un mensaje claro, un subtítulo y, si hace falta, icono.
- No duplicar CTA si ya hay botón flotante o principal en la pantalla.

## Navegación

- Root screens mantienen bottom nav: Inicio, Materias, Tareas, Gastos, Perfil.
- Pantallas internas deben ocultar bottom nav cuando el flujo lo permita.
- Bottom nav siempre usa labels en una línea, sin letter spacing agresivo.
- Las system bars se mantienen edge-to-edge sin scrims/contraste forzado para evitar franjas o sombras sobre pantallas de fondo oscuro.

## Interacción

- Evitar overlays grises de press/long press en superficies personalizadas.
- Usar `cleanClickable` para cards/chips donde el ripple nativo rompa el look.
- Haptic feedback solo cuando aporte sensación de acción real, no en cada tap decorativo.
