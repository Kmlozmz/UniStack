# API — integraciones externas

UniStack **no tiene backend ni API propia**. Sólo consume dos servicios y produce uno (el bot).
Todo lo de aquí está en el código; nada más existe.

## 1. GitHub Releases (actualizador de la app) — activo

- **Repo:** `Kmlozmz/UniStack-releases` (público, sólo etiquetas y APK). `BuildConfig.GITHUB_REPO`
  sale de `githubReleasesSlug` en `app/build.gradle.kts`.
- **Endpoint:** `GET https://api.github.com/repos/{repo}/releases?per_page=10`
  (no `/releases/latest`: excluye preestrenos, y la app debe ver betas).
- **Cabeceras:** `Accept: application/vnd.github+json`. Sin autenticación (repo público).
- **Lectura:** por cada release, `tag_name` (sin `v`) → nombre de versión; `prerelease`;
  `body` → notas; `assets[].name.endsWith(".apk")` → `browser_download_url`.
- **Comparación:** `ReleaseVersion.parse` ordena `mayor.menor.parche-peldaño.N` trozo a trozo,
  números como números (`beta.10 > beta.2`). Nunca ofrece una versión menor que la instalada.
- **Descarga e instalación:** `GitHubReleaseUpdateRepository` descarga a `unistack-update.apk`
  en almacenamiento interno, comprueba **SHA-256** si la release lo publica (`VERIFY` formato
  exacto del hash en el body/asset) y que la firma coincida con la instalada, y abre el
  instalador (`application/vnd.android.package-archive` vía `FileProvider`). Android pide una
  vez el permiso de origen desconocido.
- **Cadencia:** `UpdateCheckWorker` (WorkManager) cada 2 h; al abrir la app si pasaron 45 min.
  Falla con `IOException` sin red a propósito (para que WorkManager reintente); en el
  `Application` esa excepción se traga.
- **Errores:** `No se pudieron comprobar las actualizaciones.` (i18n `update_check_failed`);
  estados en `UpdateOutcomeTest`.

## 2. GitHub Releases (publicar) — desde Gradle, no desde la app

- Tarea `publishReleaseToGitHub` en `app/build.gradle.kts`: crea la release (`POST /repos/{repo}/releases`
  con `tag_name = v<versión>`, `prerelease` según sufijo o `-Pprerelease`), sube el APK como
  asset. Requiere `GITHUB_TOKEN` en `.env` con permiso de escritura sobre el repo de releases.
- El cuerpo de la release sale de la sección de `CHANGELOG.md` de esa versión (`VERIFY` si se
  copia literal o resumido).

## 3. Telegram Bot API — desde Gradle

- Tarea `sendReleaseApkToTelegram` (cuelga de `assembleRelease`): `curl` a
  `https://api.telegram.org/bot<TOKEN>/sendDocument` con `chat_id` y el APK; pie
  `(alpha) 1.6.0-alpha.95 · envio #99`. Credenciales en `.env`. Sin mensajes previos ni
  posteriores (DECISIÓN 28 ago).

## 4. Firebase (Auth + Firestore) — código presente, **no conectado**

- `FirebaseGoogleAuthService`: inicio de sesión con Google vía Credential Manager
  (`GOOGLE_WEB_CLIENT_ID` de `local.properties`) → `FirebaseAuth`. Sin `google-services.json`
  lanza «Falta app/google-services.json para inicializar Firebase.».
- `FirebaseCloudBackupRepository`: documento `users/{userId}/backups/current` con el mismo JSON
  del respaldo local (tope 1 MB por documento → los adjuntos van como ficha).
- Decisión de producto: **identidad + respaldo, cuenta opcional**. Aplazado hasta que exista el
  proyecto de Firebase. Permisos/reglas de Firestore: `TO DEFINE`.

## 5. Telegram (soporte) — enlaces, no API

- Grupo `@unistacksoporte`; temas `2` (fallos) y `3` (sugerencias) en `SupportChannel`.
- La app copia el ticket al portapapeles y abre `tg://resolve?domain=unistacksoporte&thread=N`
  (respaldo `https://t.me/...`). Requiere `<queries>` en el manifiesto.

## Convenciones

- Toda llamada de red va en `Dispatchers.IO`, con `runCatching`/`try` y mensaje de usuario traducido.
- No se guardan tokens en el APK; los APK son públicos.
- No hay analítica, crash reporting ni telemetría (`reporte de errores` está en «definir/planear»).
