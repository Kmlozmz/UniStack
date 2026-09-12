---
name: unistack-workflow-release
description: >-
  Workflows, compilation, testing, versioning, Git conventions, and Telegram release automation for UniStack.
  Use when building the app, running Gradle tasks, packaging alpha/production APKs, computing versionCode,
  committing code on the TDL branch, syncing with main, or reporting delivery to the user.
---

# UniStack Workflow & Release Skill

This skill provides authoritative procedures for building, testing, versioning, releasing, and reporting in the UniStack Android project.

---

## 1. Golden Rules of Delivery

1. **Autonomous Alpha Delivery**:
   - Whenever a significant set of features or fixes is completed and tested, compile and send an alpha APK **immediately** without waiting for explicit user permission:
     ```powershell
     ./gradlew sendAlpha "-PversionName=1.6.0-alpha.N"
     ```
   - The bot will deliver the APK directly to the developer's Telegram bot (`UniStackBot`).

2. **PowerShell Parameter Quoting Rule**:
   - In Windows PowerShell, `-PversionName=...` contains symbols that PowerShell may misinterpret.
   - **ALWAYS** pass Gradle properties enclosed in double quotes:
     ```powershell
     # CORRECT:
     ./gradlew sendAlpha "-PversionName=1.6.0-alpha.96"

     # WRONG (causes build error):
     ./gradlew sendAlpha -PversionName=1.6.0-alpha.96
     ```

3. **No Co-Authored-By Trailers**:
   - Under NO circumstances include `Co-Authored-By: Claude...` or `Co-Authored-By: Antigravity...` or any AI assistant attribution lines in commit messages. Commits must look 100% human-authored.

4. **Reporting Structure**:
   - All reports must be in concise Spanish.
   - Clearly divide deliverables into:
     - **Interno**: Refactors, migrations, performance, database updates, tests.
     - **Palpable**: Visual changes, new buttons, behavior tweaks visible to the user.
   - Always include the exact mobile navigation test path:
     - *Ruta en el cel:* `Pantalla Principal > Ajustes > ...`

---

## 2. Versioning & VersionCode Calculation

### Semantic Versioning Format
`MAJOR.MINOR.PATCH-alpha.STEP` (e.g. `1.6.0-alpha.96`)

### VersionCode Formula
UniStack uses an integer math formula implemented in `app/build.gradle.kts`:
$$\text{versionCode} = (\text{major} \times 10^7) + (\text{minor} \times 10^5) + (\text{patch} \times 10^3) + \text{alpha\_step}$$

- For `1.6.0-alpha.95`:
  $$1 \times 10000000 + 6 \times 100000 + 0 \times 1000 + 95 = 10600095$$
- For `1.6.0-alpha.96`:
  $$10600096$$
- For stable release `1.6.0` (peldaño = 0):
  $$10600000$$

Before bumping `versionName`, check the latest tag with `git tag --sort=-v:refname | Select-Object -First 5` and inspect `app/build.gradle.kts`.

---

## 3. Git Branching & Commit Conventions

### Branch Strategy
- **`TDL`**: The active trunk branch for all day-to-day development. Work is done directly here.
- **`main`**: The stable branch. Fast-forward merged from `TDL` when a batch is finalized.
  ```powershell
  git checkout main
  git merge --ff-only TDL
  git push origin main
  git checkout TDL
  ```

### Commit Messages
- Use **Conventional Commits** in **English**.
- Focus on the **WHY** behind the change, not just a literal restatement of what was changed.
- Structure:
  ```text
  feat(tasks): implement subtask reordering and swipe actions

  Allows students to reorder task checklist items via drag-and-drop
  and dismiss completed items with haptic feedback.
  ```
- Allowed types: `feat`, `fix`, `refactor`, `test`, `chore`, `docs`, `perf`, `style`.

---

## 4. Gradle Task Catalog

| Command | Purpose |
| :--- | :--- |
| `./gradlew testDebugUnitTest` | Run all 534+ unit tests (domain, migrations, ViewModels). |
| `./gradlew assembleDebug` | Compile debug APK without sending. |
| `./gradlew sendAlpha "-PversionName=1.6.0-alpha.N"` | Build alpha and push to Telegram channel. |
| `./gradlew releaseToGithub "-PversionName=1.6.0"` | Compile release APK and draft GitHub release. |
| `./gradlew ktlintCheck` | Validate code style formatting. |
| `./gradlew lintDebug` | Android lint analysis. |

---

## 5. Typical Alpha Delivery Checklist

1. [ ] Run tests: `./gradlew testDebugUnitTest`
2. [ ] Verify string keys match in `values/strings.xml` and `values-en/strings.xml`.
3. [ ] Commit changes to `TDL` with clean Conventional Commit (no trailers).
4. [ ] Determine next alpha version (e.g., `1.6.0-alpha.96`).
5. [ ] Execute `./gradlew sendAlpha "-PversionName=1.6.0-alpha.96"`.
6. [ ] Confirm Telegram upload response (HTTP 200).
7. [ ] Present concise Spanish report with *Interno*, *Palpable*, and *Ruta en el cel*.
