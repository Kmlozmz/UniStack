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

## Componentes

### CTAs

- Deben aparecer una sola vez por pantalla si son acciones primarias.
- En pantallas raíz, colocarlos cerca de la parte inferior y alineados como el CTA de tareas.
- No usar botones duplicados dentro de empty states.
- Los CTA principales de raíz usan el mismo lenguaje visual: altura `56.dp`, radio `22.dp`, padding horizontal `16.dp`, texto oscuro `#15131D`, peso `SemiBold` y un icono `+` de `18.dp` dentro de un círculo translúcido de `24.dp`.
- Cada módulo conserva su color de botón: Tareas y Materias usan primario; Gastos usa coral.

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

## Interacción

- Evitar overlays grises de press/long press en superficies personalizadas.
- Usar `cleanClickable` para cards/chips donde el ripple nativo rompa el look.
- Haptic feedback solo cuando aporte sensación de acción real, no en cada tap decorativo.
