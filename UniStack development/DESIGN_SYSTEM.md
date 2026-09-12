# DESIGN_SYSTEM — cómo se ve y se siente UniStack

El archivo más importante después de `AI_CONTEXT.md`. Dos partes: **lo que la app es hoy**
(tokens y componentes reales, verificados en `core/design/`) y **lo que él aprueba y rechaza**
(reglas extraídas de sus vetos, con la fecha). Si algo de aquí choca con `design-system/unistack/MASTER.md`
del repo, ese fichero está obsoleto (paleta teal generada el 6 ago 2026 que nunca se usó).

## 1. Filosofía visual

- **Material 3 Expressive, de verdad, no «Material genérico».** `MaterialExpressiveTheme` con
  esquema de movimiento por muelles; los componentes M3E de `material3:1.5.0-alpha15` (barra
  corta, grupos conectados, indicadores ondulados, `LoadingIndicator`, `MaterialShapes`,
  `SplitButton`, `FloatingToolbar`). Su crítica más repetida a una pantalla nueva es «no coincide
  mucho con M3E» o «se ve muy funky / muy virgen».
- **El color rellena, no bordea.** Los bloques importantes son contenedores tonales enteros
  (hero verde/ámbar/rojo/violeta según estado). **Nunca una franja de color al costado de una
  tarjeta**: lo llamó «AI slop» (11 sep 2026). Tampoco tarjetas con borde de color.
- **La forma dice más que el color**: estados con ruedas (visto/equis/guion/flecha), no sólo
  tintes. Hay un ajuste «formas además de color» para daltonismo.
- **Una app, no un acento sobre una base ajena**: los neutros del esquema tiran a violeta
  (semilla `#5B46E0`), no a gris puro.
- **Cada sección tiene identidad cromática** (`SectionColors`, fuera del `ColorScheme` a
  propósito): Horario azul, Gastos **rojo** (identidad, no error), «al día» verde, «en riesgo»
  ámbar. Los estados de rendimiento y las identidades no se mezclan con el token `error`.
- **La longitud de una barra mide avance; el color, identidad; nunca rendimiento** (`EvaluationBar`).
- **Copiar la pieza que existe** antes que diseñar una nueva (ver § 6).

## 2. Tokens reales

### Color
- **Esquema claro** generado desde la semilla violeta `#5B46E0`: `primary #6C4DFF`,
  `primaryContainer #E6DFFF`, `onPrimaryContainer #1C0090`; `secondary` es familia del primario
  (`#605A70`, container `#E7DEF8`); terciario `#7C5264`.
- **Tema oscuro por defecto («UniStack»)**: fondo **`#0A0C11`** (decidido; «ya no el morado»),
  superficies `#12161D / #171C24 / #1C222D / #232B37`, acento `#7F77DD`, tinta `#E8EBF3`,
  secundaria `#98A2B7`, línea `#262E3B`, borde `#6C7689`. Estos son los que se usan también en
  los artifacts (variables `--suelo`, `--s-low`… en el HTML).
- **28 temas** (`AppThemes.catalog`): 14 propios (UniStack, Medianoche, Bosque, Atardecer,
  Carbón, Cereza, Océano, Ámbar, Lavanda, Vino; y de nacer claros Papel, Nieve, Menta, Arena)
  + paletas publicadas con sus valores oficiales (Catppuccin Mocha/Macchiato/Frappé/Latte,
  Tokyo Night/Storm, Dracula, Nord, Gruvbox, Solarized, Rosé Pine, One Dark, Everforest,
  Monokai). Cada tema tiene cara clara y oscura; el modo (sistema/claro/oscuro/OLED) elige la cara.
- **Monet (color dinámico) apagado por defecto** — DECISIÓN.
- **Colores de sección** (`SectionColors.Light/Dark`), dark: `schedule #4797FF`,
  `expenses #FF5340`, `onTrack #11C045`, `atRisk #E0A400`, cada uno con su `container`/`on`.
  Light oscurecidos para contraste (`schedule #0A63D6`, `onTrack #0A8A31`…).
- **Colores fijos de asistencia** (`AttendanceColors.kt`, **no siguen al tema** — DECISIÓN):
  asistió `#58D68D`, falta `#F1706F`, cancelada `#F0B429`, reprogramada `#8AA6F2`; pares tonales
  profundo/pálido para heros (`#0A5C23/#B4F2C4`, `#6B4E00/#FFE29E`, `#8C1F0A/#FFDCD5`).
- **`verifyDesignTokens`** (tarea Gradle en `check`) falla si aparece `Color(0x…)` o
  `Color.White/Black` a pelo fuera del sistema de diseño.
- **El logo es el PNG** (`unistack_option_a_symbol.png`, índigo); no se dibuja por código —
  DECISIÓN 21 ago. `UniStackBrandMark.Pills` existe sólo para animar el arranque.

### Tipografía
- Sans-serif del sistema (`FontFamily.SansSerif`), sin fuente propia. Elegible en Apariencia:
  Sans / Sistema / Serif / Mono, y «fuente de lectura» en Accesibilidad.
- Escala Material 3 con **variantes `…Emphasized`** (`expressiveTypography`): el énfasis es una
  elección de estilo con nombre, no un `fontWeight` suelto. Cabeceras en ExtraBold, títulos
  SemiBold; `titleLarge` ExtraBold en las hojas.
- Rótulos de sección: `labelSmall`/10.5 px en mayúsculas con `letter-spacing .13–.14em`,
  color secundario («VENCIDAS · 1», «ESTA SEMANA»).
- Cifras grandes: peso 900, `letter-spacing` negativo, `tabular-nums`.

### Forma
- Escala atada al ajuste **Esquinas** (`escalaDeFormas`): *Compact* 4/6/10/14/16 dp,
  **Balanced (defecto) 8/12/20/28/32 dp**, *Soft* 12/18/28/36/42 dp. Los cinco tamaños se
  mueven juntos.
- Tarjetas `shapes.medium` (20) o `extraLarge` (32) para las grandes; hojas
  `shapes.extraLarge` arriba; heros ~30 dp; píldoras 999.
- Grupos conectados: extremos redondeados hacia fuera, interiores casi rectos.
- `BadgeShapes` con `MaterialShapes` (galleta, círculo, aleatorio) para distintivos.

### Elevación y superficies
- **Sin sombras y sin bordes**: jerarquía por **elevación tonal** (`surfaceContainerLow/…/Highest`).
  Ajustable en Apariencia (superficie plana / con filete / elevada; intensidad de sombra).
- Las tarjetas neutras se apilan por tono; el hero es el único bloque con color pleno.

### Espaciado y layout
- Sangrado horizontal de listas **20 dp**; dentro de hojas **18 dp**; gap entre tarjetas 6–8 dp;
  entre secciones 16–26 dp.
- Márgenes inferiores compartidos (`AppearanceTheme.kt`): `scrollBottomRoom` 28 dp y
  `anchoredButtonRoom` 76 dp; una lista con botón anclado reserva **los dos**.
- Acciones ancladas con `Modifier.bottomActionInsets()` **dentro** de la superficie.
- Edge-to-edge obligatorio (targetSdk 35): cada pantalla se aparta del teclado sola.
- Densidad elegible (compacta / equilibrada / amplia); tamaño de toque elegible.

## 3. Componentes propios (`core/design/components/`)

| Pieza | Para qué | Regla |
|---|---|---|
| `UniCard` | tarjeta base | mete el contenido en un `Box`: **siempre una `Column` dentro** |
| `UniSegmentedControl` | cambiar de **vista** (Materias/Tareas) | relleno con el acento; «estás viendo esto» |
| `UniChoiceRow` | **acotar/filtrar/elegir** | contorno + visto; «has marcado esto». Usar relleno para filtros hizo que parecieran pestañas |
| `MetricCard` | cifra con icono, **58 dp** | la misma en Inicio, Horario y Tareas; al tocar abre la hoja con lo que hay detrás |
| `UniIconButton` / `UniStackButton` / `SquishyButton` | botones | traen el gesto de compresión; el `Button` crudo no. `UniStackButtonDefaults.shapes` para M3E |
| `UniStackFabMenu` | FAB con menú | — |
| `UniSearchField` | buscador con cruz | 56 dp en Notas |
| `UniDatePicker` / `UniTimePicker` | fecha y hora | los de Material; la hora se escribe (`TimeInput`) con el reloj en el botón |
| `UniDropdownMenu` | menús | Eliminar separado por línea y en rojo |
| `UniSwitch` | interruptor | icono dentro (visto/aspa), elegible |
| `SwipeRow` / `FilaDeslizable` | deslizar para borrar | el fondo rojo aparece con el arrastre |
| `UniSelectionToolbar` | barra flotante de selección múltiple | — |
| `EvaluationBar`, `OutcomeRangeBar` | avance de notas; franja suelo–techo con la meta | no usar `LinearProgressIndicator` aquí |
| `AnimatedCheckmark`, `RuedaDeEstado`, `RuedaDeAsistencia` | ruedas de estado | verde visto / rojo equis / ámbar guion / azul flecha; **sólo rebote** al marcar |
| `LargeTitleScaffold`, `ScreenScaffoldPieces`, `SectionHeader`, `SettingsPieces` | andamiaje | los ajustes son filas de `UniCard` |
| `MotionPieces`, `MotionMoments` | gestos de Movimiento | ver § 5 |
| `LeaveGuard` | aviso al descartar un formulario | siempre en formularios |
| `CleanClickable` | clic sin ripple con compresión | lo usan las tarjetas |

**Hojas (bottom sheets)**: `containerColor = background`, `shapes.extraLarge`, tirador por
defecto, título `titleLarge` ExtraBold, sangrado 18 dp, filas de `UniCard`. Referencias para
copiar: `feature_home/presentation/SubjectPickerSheet.kt` (elegir de una lista),
`feature_schedule/presentation/CatchUpSheet.kt` (resolver pendientes),
`feature_schedule/presentation/AttendanceHistoryPieces.kt › HojaDeClase` (editar un elemento con
estado tonal + grupo conectado).

**Navegación**: barra inferior `ShortNavigationBar` acoplada al borde (no flotante), indicador
`secondaryContainer` (el del spec), cuatro pestañas; se esconde en cajón y formularios.
Transición **empuje iOS** sin fundido. Diálogos sólo para confirmar irreversibles; el resto son
hojas. Los formularios son pantallas completas con secciones en tarjetas de filas.

## 4. Estados, feedback, vacío
- Snackbar con acción para lo reversible (deshacer, «no lleva nota»); diálogo doble para lo
  irreversible (borrar notas por cambio de escala, cerrar periodo escribiendo CERRAR).
- «Sin dato» es `—` (`NO_DATA`), nunca `0`; `0` es un dato.
- Estados vacíos en `UniCard` grande con título y una línea; sin ilustraciones.
- Feedback de error en texto con color `error` bajo el campo; validaciones en `TextValidators`.
- Píldoras de estado (hoy ámbar, vencida roja con latido, espera nota, con nota verde).

## 5. Movimiento
- Esquema de movimiento M3E (muelles). Empuje de navegación 300 ms `(0.32,0.72,0,1)`.
- **Ajustes › Movimiento**: 7 gestos con variantes (velocidad, carga, transición, listas,
  sello de corte, celebración, clase ahora), cada uno **animado en vivo en su tarjeta**. Lo que
  no está ahí «ya se mueve solo» (rebote al marcar asistencia, latido de lo vencido, tachado en
  línea, punto de guardado, despegue al fijar, aviso de presupuesto).
- Tiempos por `duracion(ms)` escalados por el ajuste y por «reducir movimiento» del sistema.
- Momentos propios: sello de corte (golpe/onda/tinta/cinta), celebración a pantalla completa
  (2.200 ms, en dp, con mensaje), entrada de lista, latido de lo vencido (escala ±2,8 %,
  periodo 1,9 s), borde cónico que respira en la sugerencia de nota.
- Haptics vía `performSafely`.

## 6. Lo que le gusta y lo que rechaza (reglas extraídas)

**Le gusta / aprueba**
- Contenedores tonales enteros como hero, cambiando de color con el estado (verde → ámbar → rojo).
- Grupos conectados M3E donde el elegido se ensancha (`ButtonGroup` con `animateWidth`).
- Indicador circular **ondulado** para «cuánto llevas» (faltas, hechas), que se ondula más cerca
  del final.
- Ruedas de estado con el mismo símbolo que al marcar (visto/equis).
- Textos concretos e intuitivos, no genéricos («Quedan 3 de 4 faltas», no «Estado: OK»).
- Que cada ajuste tenga muestra en vivo (Apariencia, Movimiento).
- Replicar patrones conocidos cuando encajan: **Google Keep** para notas (lo pidió con capturas).
- El empuje iOS de navegación («lo dijo dos veces»).
- Artifacts «full M3E, colores, organizados, armónicos visualmente».

**Rechaza (con fecha)**
- Franjas/rails de color al costado de tarjetas — «AI slop» (11 sep).
- Siluetas M3E (trébol, ráfaga) para estados de asistencia — «se ve extraño con esas shapes» (10 sep).
- Cabeceras grandes con subtítulo y tarjetas inventadas en una hoja de filtro — «sólo hazlo como
  el sheet de la app» (2 sep).
- Logo dibrado por código — «eso sí que no lo apruebo» (21 ago).
- Rebote elástico al desplazar, encabezados que encogen, gesto atrás predictivo, barra flotante
  (17–19 ago).
- Fundido en el cambio de pestaña (se veían tres pantallas a medio camino).
- Animaciones sin texto («a Onda le falta texto, esos círculos solos no van», 11 sep) — todo
  momento lleva su mensaje.
- Confeti pequeño y rápido — la celebración ocupa toda la pantalla.
- Exceso de opciones que no aplican nada — «ningún ajuste entra sin su sitio de aplicación».
- Que el check de asistencia tome el color del tema — verde/rojo fijos (11 sep).
- Que un aviso te lleve a una pantalla donde no puedes hacer nada (aviso post-clase → historial).
- UI «virgen / funky» que no coincide con el artifact aprobado (Notas, 28 ago).
- Dos APK por envío; mensajes del bot que no sean el APK.

## 7. Accesibilidad
17 ajustes en Accesibilidad e idiomas (contraste, paleta para daltonismo, formas además de
color, negrita, fuente de lectura, tamaño de toque, transparencia, una mano, duración de
deshacer, descripciones habladas, confirmar irreversibles, pantalla encendida, escala de texto,
movimiento, 24 h, fecha, moneda) + idioma. `contentDescription` en todos los icon buttons
(«los botones de icono dicen qué hacen»: pulsación larga muestra el nombre). Contrastes light
oscurecidos a propósito (`#4797FF` sobre blanco daba 3,1).

## 8. Dark / light
La app nace oscura (fondo `#0A0C11`) y así se juzgan los diseños; el claro se deriva del mismo
tema con contraste comprobado. Los artifacts se hacen en oscuro con los tokens del tema
UniStack. Cada tema tiene su cara clara real (Latte para Catppuccin, Dawn para Rosé Pine, Day
para Tokyo Night), nunca una inventada.

## 9. Cómo se diseña una pantalla nueva aquí
1. Artifact HTML interactivo (teléfono 390×844 con tokens de arriba + panel de mandos y razones).
   Varias propuestas cuando el cambio es grande; él elige y combina.
2. Implementar **1:1** con las piezas de § 3. Él compara con capturas y manda correcciones
   numeradas; se iteran alphas hasta «ya está perfecto».
3. Anotar lo decidido (memoria/`DESIGN_DECISIONS.md`) para no reabrirlo.
