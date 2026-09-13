---
name: unistack-workflow-release
description: >-
  Build, test, version, branch, and release workflow for UniStack on Windows.
  Use when compiling (compileDebugKotlin, sendAlpha), computing versionCode from the version name,
  committing on TDL, fast-forwarding main, publishing to GitHub, handling Gradle errors,
  or writing the delivery report (Interno / Palpable / Ruta en el cel).
  Source: UniStack development/BUILD.md and DEVELOPMENT_WORKFLOW.md.
---

# UniStack Workflow & Release Skill

Machine: Windows. Shell: PowerShell (`./gradlew`, never bare `gradlew`) or Git Bash.
Work from repo root (where `gradlew` lives). JDK 21, SDK inside repo
(`.toolchains/android-sdk-windows`, git-ignored). Secrets (`.signing/`, `.env`,
`local.properties`) are git-ignored — never commit them; a fresh clone only builds debug.

## 1. Golden rules

1. **Alpha without asking.** After finishing + verifying a unit of work, compile and send:
   `./gradlew sendAlpha "-PversionName=1.6.0-alpha.N"` (next: **`.96`**).
   The bot delivers the APK to his Telegram. One APK per batch, no "cooking/completed" chatter.
2. **Quote `-PversionName` always**: `Task '.6.0-alpha.1' not found` = PowerShell split the `-P`.
3. **No AI trailers.** Conventional Commits in English, body explains the WHY.
   **Never** `Co-Authored-By` / "Generated with". Write the message to a file, `git commit -F`.
4. **Report in Spanish**: bulleted, per change **Interno** (refactors, migrations, tests…)
   or **Palpable + exact check path** ("Horario → … debería pasar X"). End: what's pending
   from him + what follows. When a TDL point closes: show the list with done struck through.

## 2. Commands (verified, nothing invented)

| Task | Command | Bot |
|---|---|:--:|
| iterate compile | `./gradlew -q compileDebugKotlin` (~1–2 min incremental) | no |
| all tests | `./gradlew -q testDebugUnitTest` (534 tests, ~40 s) | — |
| tokens + lint + tests | `./gradlew check` (+ `verifyDesignTokens`) | — |
| **day-to-day alpha** | `./gradlew sendAlpha "-PversionName=1.6.0-alpha.N"` (~7 min; tool timeout ≥ 10 min) | **yes** |
| debug APK | `./gradlew assembleDebug` / `installDebug` (`dev`, versionCode 1 — does NOT install over an alpha) | no |
| publish beta/stable | `./gradlew publishReleaseToGitHub "-PversionName=1.6.0-beta.1"` (from `main`, needs `GITHUB_TOKEN` + real signing + `CHANGELOG.md` section) | yes |

Flags: `-PskipTelegramApk=true`, `-PapkType=x`, `-Pprerelease=false`. Never `-PversionCode`.
APK lands in `app/build/outputs/apk/release/`. Bot counter:
`.gradle/telegram-apk-counters.properties` (alpha = 99 @ 11 sep).

## 3. Version math (the ladder)

`versionCode = mayor×10.000.000 + menor×100.000 + parche×1.000 + peldaño`
(dev = 0 → code 1; alpha = 100+N; beta = 300+N; rc = 600+N; stable = 999).
- `1.6.0-alpha.96 → 10.600.196` · `1.6.0-beta.1 → 10.600.301` · `1.6.0 → 10.600.999`.
- Published today: `1.5.10 → 10.510.999`. Current series: **`1.6.0-alpha.N`**.
- **Cap 99 per rung** (alpha.100 breaks the build → bump patch instead).
- Never repeat a shipped alpha number ("aplicación no instalada" = lower versionCode over
  installed; fix = higher number, never reinstall advice that loses data).
- Betas inherit their alphas' number (`VersionCodeOrderTest` pins the formula — update both).

## 4. Branches & commits

- `TDL` = daily work. `main` = publishable, updated ONLY by fast-forward at batch close.
  Remote is `UniStack` (private `Kmlozmz/UniStack`), not `origin`.
  ```bash
  git push -q UniStack TDL
  git checkout -q main && git merge -q --ff-only TDL && git push -q UniStack main && git checkout -q TDL
  ```
- Commit: `git add -A` + `git -c core.safecrlf=false commit -q -F "<scratchpad>/commit.txt"`.
  `feat(scope): imperative subject ≤72, lowercase` + body with the why, grouped if multi-area.
  Data changes (migration) in their own commit when possible. `LF→CRLF` warnings are normal.
- TDL discipline: the list is HIS. Never invent/reorder points. Start a task reading his point
  literally + `git status` clean + `git log --oneline -10`.

## 5. Task pipeline (small and large)

Small (one screenshot): change → compile → tests → alpha → commit → 3-line report.
Large (redesign, i18n, history): artifact → plan in separately-compiling blocks
(domain+data → ViewModel → screen → strings → tests, one commit each) → ONE alpha at batch
end → grouped report. Mass code migrations: Python scripts **to a file** in the scratchpad,
with asserts and result verification — never Bash heredoc (backslashes arrive half-eaten),
and `cambiar()` scripts are not idempotent (`git checkout -- <files>`, fix, relaunch).

## 6. Publish (only when he asks)

1. `CHANGELOG.md` section with the exact number (`## [1.6.0-beta.1] — 2026-09-15`),
   written for the user ("qué nota", not "qué fichero"). The task fails on purpose without it.
2. From `main`: `publishReleaseToGitHub` → release `v1.6.0-beta.1` in public
   `Kmlozmz/UniStack-releases`, APK asset (no buildType in name) + bot.
3. Rules: alphas `X.Y.Z-alpha.N` → beta `X.Y.Z-beta.1`; a published number is never reused;
   prereleases before their final. Rung promotion (`PUBLICAR.md`): 3 days of alpha use,
   no data loss, hand-tested migrations, nothing half-done visible.

## 7. Known errors → fix

Two Gradle daemons (`Couldn't delete R.jar`) → `./gradlew --stop`, repeat, never parallel.
`check` fails 1st time on `lintAnalyzeDebugUnitTest` → environment flake, repeat untouched.
`sendAlpha solo manda alphas` → name lacks `-alpha.N`, use `assembleRelease` for beta.
`CHANGELOG.md no tiene sección` → write it. Missing `.env`/`.jks` keys → restore, never commit.
`Textos sin proveedor` (tests) → `TextosDePrueba.instalar()` in `@Before`; (app at open) →
`Textos.desde` must run **before** `super.onCreate()`.
