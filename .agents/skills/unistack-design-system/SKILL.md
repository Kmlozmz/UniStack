---
name: unistack-design-system
description: >-
  Material 3 Expressive design system, color tokens, typography, component library, motion, and anti-patterns for UniStack.
  Use when writing Jetpack Compose UI, styling screens, selecting components (UniCard, MetricCard, OutcomeRangeBar),
  applying dark theme tokens (#0A0C11), setting up layout padding, or auditing UI for forbidden patterns.
---

# UniStack Design System Skill

This skill defines the Material 3 Expressive design architecture, color palette, custom components, motion specs, and strict styling rules for UniStack.

---

## 1. Visual Foundation & Color Tokens

### Dark Theme Priority
UniStack is designed dark-first. The standard dark surface is:
- **Base Surface / Background**: `#0A0C11`
- **Surface Container Lowest**: `#0F1218`
- **Surface Container Low**: `#151820`
- **Surface Container**: `#1B1E28`
- **Surface Container High**: `#222632`
- **Surface Container Highest**: `#2A2F3E`

### Section & Semantic Colors
- **Gastos**: Strictly Red (`#E53935` / `Color(0xFFE53935)`). Non-negotiable.
- **Horario**: Indigo / Deep Blue (`#3F51B5` / `#5C6BC0`).
- **Notas / Académico**: Emerald Green / Amber depending on passing threshold.
- **Asistencia**:
  - Presente: Green (`#4CAF50`)
  - Ausente: Red (`#F44336`)
  - Justificada: Amber / Yellow (`#FFC107`)
  - Cancelada: Grey (`#9E9E9E`)

---

## 2. Component Catalog & Proper Usage

### `UniCard`
- **Rule**: `UniCard` does NOT take inner padding or column semantics by default.
- **Pattern**: ALWAYS place a `Column` with standard padding inside `UniCard`:
  ```kotlin
  UniCard(modifier = modifier) {
      Column(modifier = Modifier.padding(16.dp)) {
          // Card contents
      }
  }
  ```

### `MetricCard`
- **Fixed Height**: Standardized to `58.dp` height.
- Used in dashboards and section headers to present concise figures (e.g. Current Average, Attendance %, Pending Tasks).
- Large number: `MaterialTheme.typography.titleLarge` or `headlineSmall`.
- Small subtitle: `MaterialTheme.typography.labelSmall`.

### `OutcomeRangeBar`
- Visualizes academic possibilities honestly.
- Left edge: **Floor** (`guaranteedMinimum`, score if remaining assessments are 0).
- Right edge: **Ceiling** (`bestPossible`, score if remaining assessments are 100%).
- Center marker / needle: **Target** (`targetAverage`).
- **DO NOT** replace with a single projected average point or curve.

### `UniSegmentedControl` vs `UniChoiceRow`
- Use `UniSegmentedControl` for binary or ternary mutually exclusive states (2 to 3 items, e.g., "Todos | Pendientes | Listas").
- Use `UniChoiceRow` (horizontal scrollable chips) when choices exceed 3 or items have variable text lengths.

### State Wheels
UniStack uses expressive round icon badges for states:
- `Checkmark` (completed / passed)
- `Cross` (failed / absent)
- `Dash` (neutral / not graded)
- `Arrow` (in progress / awaiting)

### Spacing & Room Constants
- Standard list bottom padding: `scrollBottomRoom` = `28.dp`.
- Bottom padding when an anchored action button / FAB exists: `anchoredButtonRoom` = `76.dp`.
- Ensure list contents are never obscured by floating or bottom-anchored bars.

---

## 3. Strict Anti-Patterns (Forbidden Patterns)

| Forbidden Pattern | Rationale & Alternative |
| :--- | :--- |
| ❌ **Colored card borders** | "AI slop" appearance. Use tonal surface elevation (`surfaceContainerHigh`) or subtle hairline opacity instead. |
| ❌ **Side-rail accent stripes** | Generic template look. Express identity through typography, state wheels, and tonal fills. |
| ❌ **Canvas-drawn logo** | Inconsistent rendering across densities. Use the official vector/PNG asset `R.drawable.logo`. |
| ❌ **Floating bottom navigation** | Wastes screen real estate and clashes with system gesture pills. Navigation bar must dock to bottom edge. |
| ❌ **Bounce scroll / Overscroll** | Distracting on Android 12+ standard stretch physics. Use platform default scroll behaviors. |
| ❌ **Predictive back overrides** | Fragile custom gesture handlers break Compose navigation. Rely on standard back handling. |
| ❌ **Grade projections** | Misleads students into false confidence. Show Floor vs. Ceiling only. |

---

## 4. Motion & Animation Specs

- **Spring Dynamics**: Use snappy springs for UI state toggles:
  `spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)`
- **Duration**: Micro-interactions $\le 200\text{ms}$; screen crossfades $\approx 250\text{ms}$.
- **Haptic Feedback**:
  - Light click on segment/tab changes.
  - Medium impact on marking task completed or stamping an academic cut.
  - Warning rumble on dangerous actions (e.g., deleting subject, wiping scale).
