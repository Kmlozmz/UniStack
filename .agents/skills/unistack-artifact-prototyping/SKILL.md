---
name: unistack-artifact-prototyping
description: >-
  Interactive HTML/CSS/JS prototyping methodology for UniStack UI and UX features.
  Use before implementing non-trivial UI screens in Compose, when exploring multiple UX proposals (A/B/C/D),
  or when building high-fidelity 390x844 mobile mockups with UniStack's dark design system (#0A0C11).
---

# UniStack Artifact Prototyping Skill

This skill provides the mandatory guidelines and templates for creating interactive HTML artifacts before writing Jetpack Compose code in UniStack.

---

## 1. The "Artifact-First" Directive

In UniStack, **no major screen redesign or new visual feature is written in Kotlin without prior visual approval via an interactive HTML artifact**.

### Why Artifacts First?
1. Eliminates tedious trial-and-error compile/run cycles on Android.
2. Allows instant comparison of competing UX approaches (e.g. Proposals A, B, C, D).
3. Validates touch targets, state transitions, visual hierarchy, and edge cases with zero build overhead.

---

## 2. Technical Prototype Standards

All UniStack UI artifacts must adhere to these specifications:

### Viewport & Shell
- **Mobile Container**: Centered container sized at `390px` width by `844px` height (standard modern smartphone viewport).
- **Device Chrome**: Render realistic Android status bar (time, battery, wifi icons) and system gesture bar at bottom.
- **Scroll Behavior**: Inner scrollable body with hidden or styled scrollbars; header and bottom bars stay anchored.

### Color Tokens & Styling (Dark Theme First)
Use exact UniStack CSS variables:
```css
:root {
  --bg-color: #0A0C11;
  --surface-lowest: #0F1218;
  --surface-low: #151820;
  --surface-mid: #1B1E28;
  --surface-high: #222632;
  --surface-highest: #2A2F3E;
  
  --primary: #8AB4F8;
  --on-primary: #042B59;
  --primary-container: #1C3B6F;
  --on-primary-container: #D2E3FC;

  --text-primary: #E2E2E6;
  --text-secondary: #C4C6D0;
  --text-muted: #8E9099;
  
  --color-gastos: #E53935;
  --color-horario: #3F51B5;
  --color-presente: #4CAF50;
  --color-ausente: #F44336;
}
```

### Component Fidelity
- **UniCard**: Rounded corners (`16px`), background `--surface-mid`, no colored borders.
- **MetricCard**: Compact (`58px` height), distinct large number and label.
- **OutcomeRangeBar**: Visual bar with floor, ceiling, and target indicator needle.
- **State Wheels**: 24px round badge with SVG icons (check, cross, dash, arrow).

### Proposal Switcher
When presenting alternative solutions:
- Provide an interactive switcher bar at the top (e.g., `[ Opción A ] [ Opción B ] [ Opción C ] [ Opción D ]`).
- Toggling options dynamically swaps screens or states using pure vanilla JavaScript.

---

## 3. Workflow Protocol

1. **Synthesize Requirements**: Extract domain rules and user needs.
2. **Author HTML Artifact**: Write complete standalone HTML file into `<appDataDir>/brain/<conversation-id>/...` or render inline.
3. **Request Feedback**: Ask the user to interact with the mockup, test the proposals, and pick their preferred design.
4. **Implementation Handoff**: Once approved, translate the winning proposal 1:1 into Jetpack Compose using `UniCard`, `MaterialTheme`, and bilingual strings.
