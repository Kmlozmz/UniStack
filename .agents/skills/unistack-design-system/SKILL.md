---
name: unistack-design-system
description: >-
  Material 3 Expressive system for UniStack: tokens, components, sheets, motion, accessibility, and vetoes.
  Use when writing Jetpack Compose UI, styling screens, picking components (UniCard, MetricCard, OutcomeRangeBar,
  UniSegmentedControl), applying dark theme tokens (#0A0C11), building bottom sheets or heroes, animating,
  or auditing UI against forbidden patterns. Source: UniStack development/DESIGN_SYSTEM.md.
---

# UniStack Design System Skill (M3E)

UniStack is **Material 3 Expressive for real, not generic Material**. The dev's most repeated
critique of a new screen is "no coincide mucho con M3E" / "se ve muy funky / muy virgen".
`material3:1.5.0-alpha15` is pinned on purpose and already contains every M3E component needed
(ShortNavigationBar, connected ButtonGroup, wavy indicators, LoadingIndicator, MaterialShapes,
SplitButton, FloatingToolbar). Never touch the toolchain to "unlock" a component.
Only one file names Material (`UniStackExpressive.kt`) to absorb alpha renames.

## 1. Visual philosophy (decides ties)

- **Color fills, never borders.** Important blocks are full tonal containers (hero changes color
  with state: green → amber → red). Never a color stripe on a card's side ("AI slop", vetoed
  11 sep 2026). No colored card borders either.
- **Shape says more than color.** States use wheels (seen/cross/dash/arrow), not tints alone.
  There is a "shapes besides color" setting for color blindness.
- **One app, not an accent on чужой base**: neutrals lean violet (seed `#5B46E0`), never pure gray.
- **Each section has chromatic identity** (`SectionColors`, outside `ColorScheme` on purpose):
  Horario blue, Gastos **red** (identity, not error), on-track green, at-risk amber.
  Performance states and identities never mix with the `error` token.
- **Bar length = progress; color = identity; never performance** (`EvaluationBar`).
- **Redesign = copy the existing piece.** Before writing a sheet, row, or selector, search
  `core/design/components/` and a sibling screen. The KDoc rule of each component wins over taste.

## 2. Tokens

### Color
- **Dark default ("UniStack")**: background **`#0A0C11`**, surfaces
  `#12161D / #171C24 / #1C222D / #232B37`, accent `#7F77DD`, ink `#E8EBF3`,
  secondary `#98A2B7`, line `#262E3B`, border `#6C7689`. Artifacts use these same tokens.
- **Light scheme** generated from violet seed `#5B46E0`: `primary #6C4DFF`,
  `primaryContainer #E6DFFF`, `onPrimaryContainer #1C0090`.
- **28 themes** (`AppThemes.catalog`): 14 own + published palettes with official values
  (Catppuccin, Tokyo Night/Storm, Dracula, Nord, Gruvbox, Solarized, Rosé Pine, One Dark,
  Everforest, Monokai). Each theme has a real light AND dark face; mode picks the face.
  **Monet (dynamic color) off by default** — DECISION.
- **Section colors** (`SectionColors`, dark): schedule `#4797FF`, expenses `#FF5340`,
  onTrack `#11C045`, atRisk `#E0A400`, each with container/on. Light faces darkened for
  contrast (`#4797FF` on white scored 3.1 — never reuse dark values on light).
- **Fixed attendance colors** (`AttendanceColors.kt`, **do NOT follow theme** — DECISION):
  attended `#58D68D`, absent `#F1706F`, cancelled `#F0B429`, rescheduled `#8AA6F2`;
  deep/pale tonal pairs for heroes (`#0A5C23/#B4F2C4`, `#6B4E00/#FFE29E`, `#8C1F0A/#FFDCD5`).
- `verifyDesignTokens` (Gradle, runs in `check`) **fails on bare `Color(0x…)` or
  `Color.White/Black`** outside the design system.
- **The logo is the PNG** (`unistack_option_a_symbol.png`, indigo). Never drawn in code —
  DECISION 21 ago ("eso sí que no lo apruebo"). `UniStackBrandMark.Pills` only animates boot.

### Typography
- System sans (`FontFamily.SansSerif`), no custom font. Selectable in Appearance:
  Sans / System / Serif / Mono, plus a "reading font" in Accessibility.
- Material 3 scale with **`…Emphasized` variants** (`expressiveTypography`): emphasis is a named
  style choice, never a loose `fontWeight`. Headers ExtraBold, titles SemiBold;
  `titleLarge` ExtraBold in sheets.
- Section labels: `labelSmall`/10.5px UPPERCASE, letter-spacing .13–.14em, secondary color.
- Big figures: weight 900, negative letter-spacing, `tabular-nums`.

### Shape
- Scale tied to the **Corners** setting (`escalaDeFormas`): Compact 4/6/10/14/16,
  **Balanced (default) 8/12/20/28/32**, Soft 12/18/28/36/42 dp. All five move together.
- Cards `shapes.medium` (20) or `extraLarge` (32) for large; sheets `shapes.extraLarge` on top;
  heroes ~30 dp; pills 999. Connected groups: outer ends rounded outward, inner nearly straight.
- `BadgeShapes` with `MaterialShapes` (cookie, circle, random) for badges only — never M3E
  silhouette shapes (clover, burst) for status ("se ve extraño con esas shapes", 10 sep).

### Elevation, spacing, layout
- **No shadows, no borders**: hierarchy via **tonal elevation**
  (`surfaceContainerLow/…/Highest`). The hero is the only full-color block.
- List horizontal inset **20 dp**; inside sheets **18 dp**; card gaps 6–8 dp; sections 16–26 dp.
- Shared bottom margins (`AppearanceTheme.kt`): `scrollBottomRoom` 28 dp,
  `anchoredButtonRoom` 76 dp; a list with an anchored button reserves **both**.
- Anchored actions use `Modifier.bottomActionInsets()` **inside** the surface.
- Edge-to-edge mandatory (targetSdk 35): every screen moves away from the keyboard itself.

## 3. Component catalog (`core/design/components/`)

| Piece | Use | Rule |
|---|---|---|
| `UniCard` | base card | content goes in a `Box`: **always a `Column` inside** (direct children paint over each other) |
| `UniSegmentedControl` | switch **view** (Materias/Tareas) | filled with accent; "you are seeing this" |
| `UniChoiceRow` | **bound/filter/pick** | outline + check; "you marked this". Filled filters looked like tabs — never fill |
| `MetricCard` | figure + icon, **58 dp** | same in Home, Horario, Tareas; tap opens the sheet with what's behind |
| `UniIconButton` / `UniStackButton` / `SquishyButton` | buttons | bring the squish gesture; never raw `Button` except with `UniStackButtonDefaults.shapes` |
| `UniStackFabMenu` | FAB with menu | — |
| `UniSearchField` | search with cross | 56 dp in Notas |
| `UniDatePicker` / `UniTimePicker` | date & time | Material ones; time is typed (`TimeInput`) with clock on the button |
| `UniDropdownMenu` | menus | Delete separated by a line, in red |
| `UniSwitch` | switch | icon inside (check/cross), selectable |
| `SwipeRow` / `FilaDeslizable` | swipe to delete | red background appears with the drag |
| `EvaluationBar`, `OutcomeRangeBar` | grade progress; floor–ceiling band with target | never `LinearProgressIndicator` here |
| `AnimatedCheckmark`, `RuedaDeEstado`, `RuedaDeAsistencia` | status wheels | green check / red cross / amber dash / blue arrow; **only bounce** when marking |
| `LargeTitleScaffold`, `ScreenScaffoldPieces`, `SectionHeader`, `SettingsPieces` | scaffolding | settings are `UniCard` rows |
| `LeaveGuard` | discard warning | always in forms |
| `CleanClickable` | click without ripple, with squish | what cards use |

**Sheets** (`ModalBottomSheet`): `containerColor = background`, `shape = extraLarge`,
default handle, title `titleLarge` ExtraBold, 18 dp inset, `UniCard` rows.
Copy these references: `SubjectPickerSheet.kt` (pick from list), `CatchUpSheet.kt`
(resolve pendings), `AttendanceHistoryPieces.kt › HojaDeClase` (edit one item, tonal
state + connected group).

**Navigation**: bottom `ShortNavigationBar` docked to the edge (never floating),
`secondaryContainer` indicator (the spec one), four tabs; hidden in drawer and forms.
**iOS-push transition**, no fade. Dialogs only for irreversible confirms; everything else
is sheets. Forms are full screens with sections in row-cards.

## 4. States, feedback, empty

- Snackbar with action for reversible (undo, "no lleva nota"); double dialog for irreversible
  (scale-change wipe with real count, close-period typing CERRAR).
- "No data" is `—` (`NO_DATA`), never `0`; `0` is data.
- Empty states: large `UniCard` with title + one line; no illustrations.
- Field error as translated text under the field (`TextValidators`).
- Status pills (today amber, overdue red with heartbeat, awaiting-grade, graded green).

## 5. Motion (7 gestures, M3E spring scheme)

- M3E motion scheme (springs). Nav push 300 ms `(0.32,0.72,0,1)`.
- **Settings › Motion**: 7 gestures with variants, each **animated live on its own card**.
  What is not there "already moves on its own" (attendance-mark bounce, overdue heartbeat,
  strike-through, save dot, pin lift-off, budget warning).
- Times via `duracion(ms)`, scaled by the setting and the system "reduce motion".
- Own moments: cut seal (thump/wave/ink/ribbon), full-screen celebration (2,200 ms, in dp,
  **with message** — every animated moment carries text), list entry, overdue heartbeat
  (±2.8% scale, 1.9 s period), breathing conic border on the grade suggestion.
- Haptics via `performSafely`. `Animatable` for entrances (never `animateFloatAsState`
  born at destination); respect `hayMovimiento()`.

## 6. Vetoes (never propose again)

Side color rails/stripes · colored card borders · code-drawn logo · floating nav bar ·
bounce scroll/overscroll · shrinking headers · predictive-back overrides · tab-change fade ·
text-less animations · small fast confetti · settings with no effect · attendance check taking
theme color (fixed green/red) · notifications leading where you cannot act · screens that don't
copy the app ("sólo hazlo como el sheet de la app") · deviations from the approved artifact
(deviation = bug, not improvement).

## 7. Designing a new screen here

1. Interactive HTML artifact first (390×844 phone + controls panel + reasons; several
   proposals when the change is big). He picks / combines.
2. Implement **1:1** with the pieces above. He compares with screenshots and sends numbered
   corrections; iterate alphas until "ya está perfecto".
3. Write down what was decided (memory / `DESIGN_DECISIONS.md`) so it never reopens.
