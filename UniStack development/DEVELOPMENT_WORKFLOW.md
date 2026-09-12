# DEVELOPMENT_WORKFLOW — de la tarea al APK

Comandos verificados en el repo (Windows, PowerShell; Git Bash también funciona con `./gradlew`).
Los detalles de compilación y errores están en [`BUILD.md`](BUILD.md); el porqué de la escalera
de versiones, en `PUBLICAR.md` del repo.

## Ramas

| Rama | Uso |
|---|---|
| `TDL` | **Trabajo diario** (sep 2026). Todos los commits de las tandas van aquí. |
| `main` | Línea publicable. Se pone al día por **fast-forward** desde `TDL` al cerrar cada tanda. |
| `test` | Espejo antiguo; ya no se usa como rama de trabajo. |
| `release/X.Y.Z` | Sólo desde una `rc`; no ha existido todavía. |
| `respaldo/antes-de-conventional-2026-09-10` | Copia de seguridad previa a una reescritura. |

Remoto: `UniStack` → `github.com/Kmlozmz/UniStack` (privado).

## 1. Iniciar una tarea

- Leer el punto de la TDL tal como él lo escribió (no interpretar de más).
- `git status` limpio; `git log --oneline -10` para ver la tanda anterior.
- Si es rediseño o cambio de flujo → artifact primero (ver `AI_WORKFLOW.md`).

## 2. Analizar el estado actual

- Leer pantalla, ViewModel, dominio, repositorio y tests de la feature.
- Revisar `DESIGN_DECISIONS.md`, `FAILED_APPROACHES.md`, `KNOWN_ISSUES.md`.
- Si toca datos: `UniStackDatabase.kt` (versión, migraciones), `LocalJsonBackupRepository.kt`,
  `FirebaseCloudBackupRepository.kt`, `RoomMigrationTest.kt`.

## 3. Planificar

- Bloques que compilan por separado (dominio+datos → ViewModel → pantalla → textos → tests).
- Si hay migración masiva de código (como i18n), escribir scripts Python **a fichero** en el
  scratchpad con `assert` y verificación por resultado; nunca por heredoc.

## 4. Implementar

- Compilar a menudo: `./gradlew -q compileDebugKotlin`.
- Textos en los dos XML desde el principio.
- Copiar piezas existentes; no inventar componentes.

## 5. Probar

```bash
./gradlew -q testDebugUnitTest     # 534 tests, ~40 s
./gradlew check                     # + verifyDesignTokens (+ lint; puede fallar la 1ª vez)
```

Resultados en `app/build/test-results/testDebugUnitTest/*.xml`.

## 6. Compilar y enviar la alpha (sin preguntar)

```bash
./gradlew -q sendAlpha "-PversionName=1.6.0-alpha.96"
```

- Sube el `.N` en cada envío; nunca repetir uno enviado. El contador del pie (`#N`) vive en
  `.gradle/telegram-apk-counters.properties` (de esta máquina).
- Una sola APK por tanda. Si él pide la beta, `-PversionName=1.6.0-beta.1` (misma serie que las alphas).
- Un `dev` (`assembleDebug`/`installDebug`) no pasa por el bot y **no se instala encima de una
  alpha** (versionCode 1): sólo para emulador.

## 7. Commit

Mensaje a fichero y `git commit -F` (evita el trailer automático y las comillas de PowerShell):

```bash
git add -A
git -c core.safecrlf=false commit -q -F "<scratchpad>/commit.txt"
```

Formato:

```
feat(tasks): open a task in a sheet and reach its subject

Tapping a task did nothing and the subject was only reachable from
Académico. The sheet copies HojaDeClase from attendance ...

Subtasks get their own table (Room 21→22) ...
```

- Inglés, Conventional Commits (`feat`, `fix`, `refactor`, `docs`, `build`, `test`), asunto
  ≤72 en imperativo y minúscula, cuerpo con el porqué. **Sin `Co-Authored-By`.**
- Los avisos `LF will be replaced by CRLF` son normales en este repo.

## 8. Empujar

```bash
git push -q UniStack TDL
git checkout -q main && git merge -q --ff-only TDL && git push -q UniStack main && git checkout -q TDL
```

## 9. Reportar

Ver `AI_WORKFLOW.md` § Cómo presentar resultados. Incluir el número de alpha enviado.

## 10. Publicar (sólo cuando él lo pida)

1. Sección en `CHANGELOG.md` con el número exacto (`## [1.6.0-beta.1] — 2026-09-15`), escrita
   para el usuario («qué nota», no «qué fichero»). Sin ella la tarea falla a propósito.
2. Desde `main`:
   ```bash
   ./gradlew publishReleaseToGitHub "-PversionName=1.6.0-beta.1"
   ```
   Crea la release `v1.6.0-beta.1` en `Kmlozmz/UniStack-releases`, sube el APK (sin buildType en
   el nombre) y lo manda por el bot. Necesita `GITHUB_TOKEN` en `.env` y firma real.
3. Reglas: alphas `X.Y.Z-alpha.N` → beta `X.Y.Z-beta.1`; un número publicado no se reutiliza;
   preestrenos antes de su definitiva; después de una definitiva, sólo números mayores.
4. Peldaños y cuándo sube cada uno: `PUBLICAR.md` (tres días de uso en alpha, sin pérdida de
   datos, migraciones probadas a mano, nada a medias visible).

## 11. Al cerrar un punto de la TDL

- Actualizar la memoria del agente / `CURRENT_STATE.md`.
- Enseñarle la lista con lo hecho tachado.
- Decir qué queda pendiente de él (probar, elegir).

## Herramientas auxiliares

- `scripts/send_apk.sh` — envío manual por el bot (`VERIFY` uso actual; la tarea Gradle lo cubre).
- Scratchpad de sesión (`%TEMP%\claude\…\scratchpad`) para scripts, artifacts y mensajes de
  commit; `.claude/launch.json` para servir el scratchpad por HTTP y previsualizar artifacts.
- Artifacts de diseño: publicados en claude.ai (privados); las URL de los decididos están en la
  memoria y en `DESIGN_DECISIONS.md`.
