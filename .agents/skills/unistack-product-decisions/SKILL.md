---
name: unistack-product-decisions
description: >-
  Product vision, decision criteria, closed decisions, and vetoes for UniStack.
  Use when scoping a feature, proposing a design, judging between implementations, checking
  whether something was already decided or vetoed, or writing reports and TDL updates.
  Source: UniStack development/PROJECT.md, PRODUCT.md, DECISION_MAKING.md, DESIGN_DECISIONS.md,
  FAILED_APPROACHES.md, PREFERENCES.md, AI_WORKFLOW.md.
---

# UniStack Product & Decisions Skill

UniStack: Android app for **university students** holding the whole semester — grades with
floor/ceiling vs a target, tasks, schedule/attendance with absence cap, term history, Keep-like
notes, expenses, average calculator, local reminders. **No account, offline, free, no paywall**
(DECISION 2 sep 2026). One dev (`Kmlozmz`), daily user of his own app, Colombia (COP, "corte",
`America/Bogota` in tests). Quality bars: **no data loss** (the only hard rule for climbing
rungs), real M3E not generic Material, every text in two languages.

## 1. How a solution is judged (in this order)

1. **Does it lose or invent data?** If yes, no (convert grades on scale change → wipe with
   confirm; projections → floor & ceiling).
2. **Is it honest?** "Borrar es honesto; convertir parece amable pero falsifica."
3. **Does it look like what the app already does?** Existing piece wins; else explain why not.
4. **Does it do anything visible?** A setting/screen/option that changes nothing doesn't enter.
5. **Is it M3E for real?** Tonal container, connected group, wavy indicator, MaterialShapes,
   spring motion — not generic Material.
6. **Does it fit this round?** New table/screen gets flagged and decided separately.

Clash resolutions: UX beats architecture if data is safe · simplicity beats options (Motion
25→7: "lo demás ya se mueve solo") · identity beats spec unless spec already respects it ·
precision in data, speed in UI · what he asked beats what's "better" (code-drawn logo was
"better" and got reverted).

## 2. Closed decisions (never reopen without him asking)

Free, no plans (billing deleted whole, 2 sep) · university-only · term history before
attendance · scale change wipes grades · single global absence cap · Keep-clone notes
(`@materia`, no `#`; no underline, no note-types, no checkbox→task) · attendance wheels
(not M3E silhouettes), only past editable, 20-min prompt opening history · Motion: 7 gestures,
removed ones don't return · Gastos red + in-screen chart picker · no update channels, public
APK repo, alphas by bot only · Tasks proposal D (sheet + "open full", subtasks now, by-days;
snackbar not dialog) · no side color rails ("AI slop") · PNG logo · docked bottom bar with
`secondaryContainer` · iOS push, no fade; no bounce/shrinking-headers/predictive-back ·
targetSdk 35 on purpose · Monet off · dark `#0A0C11` · redesign = copy the app's piece ·
every animated moment carries text · full-screen celebration · i18n via `Textos` ·
versionCode from the name · CHANGELOG mandatory to publish · English Conventional Commits,
no trailers · bot gets every release APK, one per batch · no rewriting published git history
unconsulted · grade math only from `GradeCalculator` · user picks the active cut, 100% cuts
close by hand with seal. Full log: `DESIGN_DECISIONS.md`. Failures museum: `FAILED_APPROACHES.md`
— read both before proposing.

## 3. Scope

**In:** grades, tasks, schedule/attendance, terms+history, quick notes, expenses, reminders,
deep appearance+a11y, own updater, support. **Out (today):** sync (postponed), student
collaboration, university-platform integrations, web/iOS, monetization.
**Planned, in his order:** define/plan (recursos → UniStack AI → trabajos into Tasks → labs →
**error reporting**, the one agent-side blocker for the public) → Appearance carousel →
M3E for Notes + Academic config/history → migrate old alarms, audit navigation → split
Gastos/error token. **Parked by him:** terms&privacy (with landing), Google connect (needs
his Firebase project), removing modules ("no tocar hasta que lo pida").
**Discarded (never re-propose):** paywall/limits, channels+codes, public alphas, download
locking, grade conversion, checkbox→task, underline, note types, floating bar, bounce,
shrinking headers, predictive back, code logo, color rails, removed Motion variants.

## 4. Working with him

TDL is HIS: never invent or reorder points; closing one → show the struck-through list.
"Procede / continúa / no pares" = do it whole, ask only if two readings mean materially
different work. Decide routine alone and report it; ask with options when ambiguous,
irreversible/outward-facing, or contradicting a past decision. Never ask: send the APK,
create what he asked, continue on "procede". Short Spanish, no preamble, no apologies.
Vetoes are short and final ("eso sí que no lo apruebo", "se va") — undo, extract the rule,
annotate. He likes: delivered whole, told what's missing and why it matters, touchable
artifacts, exact check paths, rules remembered without repeating, real bugs fixed in passing.
Report style (workflow skill): Interno / Palpable + Ruta en el cel, his pendings, what follows.

Priorities: 1) no data loss 2) real M3E coherence 3) every setting does something
4) close the TDL point by point, in his order. Tolerance for abstraction: low (one module,
no use-case layer, no new libs without real pain — but centralize rules: one calculator,
one catalog, one text provider).
