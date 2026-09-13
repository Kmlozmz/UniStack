---
name: unistack-artifact-prototyping
description: >-
  Artifact-first interactive HTML prototyping for UniStack screens.
  Use before implementing any non-trivial UI screen or redesign in Compose, when comparing
  UX proposals (A/B/C/D), or when building 390x844 dark-theme mockups (#0A0C11) with live
  controls for the developer to pick or combine. Source: UniStack development/AI_WORKFLOW.md,
  DECISION_MAKING.md, DESIGN_SYSTEM.md.
---

# UniStack Artifact Prototyping Skill

**No large redesign ships in Kotlin without prior approval via an interactive HTML artifact.**
He approves by artifact, then compares the APK against it with screenshots —
deviation is a bug, not an improvement ("no se parece mucho al artifact que aprobé" killed Notas).
When a known pattern fits (Google Keep for notes — requested with screenshots), replicate it;
it beats inventing, and it overrides the original artifact.

## 1. Layout: phone + control panel

- **Phone left**: centered `390×844` container, real Android chrome (status bar: time, battery,
  wifi; bottom gesture bar), inner scrollable body with hidden/styled scrollbars, anchored
  header and bottom bars. Dark UniStack tokens (CSS vars mirror the app):
  bg `#0A0C11`, surfaces `#12161D / #171C24 / #1C222D / #232B37`, accent `#7F77DD`,
  ink `#E8EBF3`, secondary `#98A2B7`, line `#262E3B`; sections schedule `#4797FF`,
  expenses `#FF5340`, onTrack `#11C045`, atRisk `#E0A400`; attendance fixed
  `#58D68D / #F1706F / #F0B429 / #8AA6F2`.
- **Panel right**: controls that toggle every option live + the reasons ("mandos y razones").
  He never picks blind ("no elige a ciegas"): each toggle needs a live preview, and for
  decisions offer explicit toggles so he picks knowing the cost (Tasks D was closed as three
  toggles: sheet+"open full", subtasks now, by-days).
- **Proposal switcher** on top for competing approaches (`[ A ] [ B ] [ C ] [ D ]`), pure
  vanilla JS swapping. Expect combinations ("A+B with C's ring") — design proposals to mix.
- Detail level: "full M3E, con colores, organizados, armónicos visualmente". Artifacts with
  too little detail get bounced ("le falta detalle, aunque no mucho").

## 2. Component fidelity (must look like the app)

`UniCard` (rounded ~16–20px, tonal surface, NO colored borders, inner padding) ·
`MetricCard` (58px, big figure + label) · `OutcomeRangeBar` (floor–ceiling band + target
needle, never a single projected point) · status wheels (24px round badge, SVG
check/cross/dash/arrow) · connected groups (chosen one widens) · wavy ring for "cuánto
llevas" (waves more near the end) · sheets (bg = page bg, top radius XL, handle, ExtraBold
title, 18px inset, card rows) · docked bottom bar, never floating.
Concrete, intuitive copy ("Quedan 3 de 4 faltas", not "Estado: OK"). Every animated moment
carries its message ("esos círculos solos no van").

## 3. Preview & publish protocol

- Serve locally and click every flow for real (or via JS) before publishing:
  `python -m http.server` in the scratchpad.
- Always `<meta charset="utf-8">` for the local preview.
- Published artifacts cache in his local browser: **cache-bust on review** (`?v=`).
- Mark scope honestly: days estimate + what needs a new table/screen; what is NOT in
  this round, and `PENDING`/`VERIFY` where open. No smoke ("humo").

## 4. Handoff to Compose (1:1)

The approved artifact (or his stated combination) is the spec: implement with the
design-system pieces, bilingual strings from the start, per-area commits. Numbered
corrections come back as screenshots — iterate alphas until "ya está perfecto",
then record decisions in memory / `DESIGN_DECISIONS.md` so they never reopen.
