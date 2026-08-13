import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.security.MessageDigest
import java.util.Properties
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    //noinspection NewerVersionAvailable
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21"
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
}

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun localProperty(name: String): String =
    localProperties.getProperty(name).orEmpty()

fun releaseProperty(localName: String, envName: String): String =
    localProperty(localName).ifBlank { providers.environmentVariable(envName).orNull.orEmpty() }

fun releaseStoreFileValue(): String =
    releaseProperty("releaseStoreFile", "RELEASE_STORE_FILE").ifBlank { ".signing/unistack-release.jks" }

fun releaseStorePasswordValue(): String {
    val value = releaseProperty("releaseStorePassword", "RELEASE_STORE_PASSWORD")
    if (value.isBlank()) {
        logger.warn("⚠️  RELEASE_STORE_PASSWORD no configurada. Usando valor local de desarrollo. NO usar en producción.")
        return "unistack-dev-release"
    }
    return value
}

fun releaseKeyAliasValue(): String =
    releaseProperty("releaseKeyAlias", "RELEASE_KEY_ALIAS").ifBlank { "unistack" }

fun releaseKeyPasswordValue(): String {
    val value = releaseProperty("releaseKeyPassword", "RELEASE_KEY_PASSWORD")
    if (value.isBlank()) {
        logger.warn("⚠️  RELEASE_KEY_PASSWORD no configurada. Usando valor local de desarrollo. NO usar en producción.")
        return "unistack-dev-release"
    }
    return value
}

fun hasConfiguredReleaseSigning(): Boolean {
    val storeFile = rootProject.file(releaseStoreFileValue())
    return listOf(
        releaseStoreFileValue(),
        releaseStorePasswordValue(),
        releaseKeyAliasValue(),
        releaseKeyPasswordValue()
    ).all { it.isNotBlank() } && storeFile.exists()
}

val devFallbackSigningValue = "unistack-dev-release"

fun isUsingRealReleaseSigning(): Boolean =
    hasConfiguredReleaseSigning() &&
        releaseStorePasswordValue() != devFallbackSigningValue &&
        releaseKeyPasswordValue() != devFallbackSigningValue

val fallbackVersionCode = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHH"))
val debugBuildStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmm"))

/**
 * El `versionCode` que corresponde a un nombre de versión.
 *
 * Android solo deja instalar encima si el `versionCode` no baja, así que ese número es el que
 * decide de verdad qué se puede poner sobre qué. Si sale del reloj —como salía, `yyMMddHH`—
 * ordena por hora de compilación y no por versión: una alpha compilada por la mañana no entra
 * sobre un debug compilado por la tarde, y basta con recompilar una etiqueta antigua para que
 * adelante a la nueva. Derivándolo del nombre, el orden es el que se ve:
 *
 * ```
 * 0.0.0-dev.…            1
 * 1.0.0-alpha.1  1_000_011
 * 1.0.0-beta.1   1_000_031
 * 1.0.0-rc.1     1_000_061
 * 1.0.0          1_000_099
 * 1.0.1          1_000_199
 * 1.1.0          1_010_099
 * ```
 *
 * Cada tramo deja sitio para 19 iteraciones, y la versión sin sufijo va siempre por encima de
 * sus preestrenos. Que una alpha no se instale sobre la definitiva es lo correcto: es un paso
 * atrás, y para eso se desinstala a conciencia.
 */
fun versionCodeFor(versionName: String): Int {
    val cleaned = versionName.trim().removePrefix("v").removePrefix("V")
    val separator = cleaned.indexOfFirst { it == '-' || it == '+' }
    val numeric = if (separator >= 0) cleaned.take(separator) else cleaned
    val suffix = if (separator >= 0) cleaned.substring(separator + 1).lowercase() else null
    val parts = numeric.split('.').map { part -> part.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
    val iteration = suffix
        ?.dropWhile { !it.isDigit() }
        ?.takeWhile(Char::isDigit)
        ?.toIntOrNull()
        ?.coerceIn(0, 19)
        ?: 0
    val stage = when {
        suffix == null -> 99
        suffix.startsWith("alpha") -> 10 + iteration
        suffix.startsWith("beta") -> 30 + iteration
        suffix.startsWith("rc") -> 60 + iteration
        // «dev» y cualquier sufijo desconocido: por debajo de todo lo publicable. Todas las
        // compilaciones locales comparten número, y reinstalar el mismo sí está permitido.
        else -> 0
    }.coerceIn(0, 99)
    return (
        parts.getOrElse(0) { 0 } * 1_000_000 +
            parts.getOrElse(1) { 0 } * 10_000 +
            parts.getOrElse(2) { 0 } * 100 +
            stage
        ).coerceAtLeast(1)
}

val explicitVersionNameProvider = providers.gradleProperty("versionName")
    .orElse(providers.environmentVariable("VERSION_NAME"))
val hasExplicitVersionName = explicitVersionNameProvider.isPresent
/*
 * Las compilaciones locales se numeran por debajo de cualquier versión publicada.
 *
 * Antes salían como `1.0.<yyMMddHH>`, es decir, un parche altísimo de la 1.0. Eso dejaba sin
 * sitio a la primera versión pública: etiquetar `1.0.0` habría quedado por detrás de lo que
 * tiene instalado quien prueba, y el actualizador diría «Al día». Con `0.0.0-dev.<sello>` la
 * numeración pública empieza donde tiene que empezar, en la 1.0.0.
 */
val generatedVersionName = explicitVersionNameProvider
    .orElse("0.0.0-dev.$fallbackVersionCode")
    .get()
val generatedVersionCode = (
    providers.gradleProperty("versionCode").orNull
        ?: providers.environmentVariable("VERSION_CODE").orNull
    )
    ?.filter(Char::isDigit)
    ?.take(9)
    ?.toIntOrNull()
    ?: versionCodeFor(generatedVersionName)

/*
 * El código vive en un repositorio privado y las publicaciones en uno público aparte.
 *
 * Tienen que estar separados: las publicaciones de un repositorio privado devuelven 404 a
 * quien no ha iniciado sesión, así que ni el actualizador de la app ni el botón de descarga
 * de la web podrían llegar a ellas. La alternativa —incrustar un token en el APK— no sirve:
 * se extrae del paquete en un momento y daría acceso de escritura al código.
 *
 * Este repositorio solo aloja etiquetas y APK; el código fuente no se publica.
 */
val githubReleasesSlug = "Kmlozmz/UniStack-releases"
val roomVersion = "2.8.4"

android {
    namespace = "com.unistack.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.unistack.app"
        minSdk = 26
        targetSdk = 36
        versionCode = generatedVersionCode
        versionName = generatedVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProperty("googleWebClientId")}\"")
        buildConfigField("String", "PRO_MONTHLY_PRODUCT_ID", "\"${localProperty("proMonthlyProductId").ifBlank { "unistack_pro_monthly" }}\"")
        buildConfigField("String", "GITHUB_REPO", "\"$githubReleasesSlug\"")

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("localRelease") {
            storeFile = rootProject.file(releaseStoreFileValue())
            storePassword = releaseStorePasswordValue()
            keyAlias = releaseKeyAliasValue()
            keyPassword = releaseKeyPasswordValue()
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("localRelease")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("localRelease")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }



    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    applicationVariants.configureEach {
        outputs.configureEach {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                // El versionCode se genera por hora, así que dos builds de debug seguidos
                // salían con el mismo nombre de archivo y no había manera de distinguirlos
                // una vez enviados. Al de debug se le añade la hora y el minuto reales del
                // build. El de release conserva el nombre limpio: ahí el nombre es el de la
                // versión publicada y no debe llevar ruido.
                // El de publicación no lleva el nombre del buildType: se llamaba
                // «UniStack-1.0.0-alpha.1-release.apk», que se contradice consigo mismo. Si
                // es alpha no es la versión definitiva, y quien la publica ya lo dice en el
                // número. El de debug sí lo lleva, porque ahí sí distingue de qué es.
                this.outputFileName = if (buildType.name == "debug") {
                    "UniStack-${versionName}-debug-$debugBuildStamp.apk"
                } else {
                    "UniStack-${versionName}.apk"
                }
            }
        }
    }

}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.05.01"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.05.01"))

    //noinspection GradleDependency
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.core:core:1.18.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.navigation:navigation-compose:2.9.8")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    // Polígonos redondeados y morphing entre formas (motor sobre el que Material construye
    // MaterialShapes). Estable, e independiente de material3: nos da las formas expresivas
    // sin arrastrar el canal alpha de material3 1.5.
    implementation("androidx.graphics:graphics-shapes:1.1.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.room:room-testing:$roomVersion")
    testImplementation("androidx.test:core:1.7.0")
    testImplementation("androidx.test.ext:junit:1.3.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation("org.robolectric:robolectric:4.16.1")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")

    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.11.0")

    implementation(platform("com.google.firebase:firebase-bom:34.14.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("androidx.credentials:credentials:1.6.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")
    implementation("com.android.billingclient:billing-ktx:9.0.0")

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("com.google.dagger:hilt-android:2.56.2")
    kapt("com.google.dagger:hilt-compiler:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    testImplementation("com.google.dagger:hilt-android-testing:2.56.2")
    kaptTest("com.google.dagger:hilt-compiler:2.56.2")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.56.2")
    kaptAndroidTest("com.google.dagger:hilt-compiler:2.56.2")
}



/**
 * Impide que vuelvan a colarse colores fijos en la capa de UI.
 *
 * El lenguaje de diseño se define en `core/design/theme`; cualquier otro sitio debe usar
 * los tokens de `UniStackColors`. Sin esta verificación la limpieza se deshace sola: basta
 * con que alguien pegue un `Color(0xFF...)` en una pantalla.
 *
 * Dos vías de exención, ambas explícitas y auto-documentadas:
 *  - Archivo entero: poner `design-tokens-exempt: <motivo>` en las primeras líneas.
 *    Reservado a definiciones de paleta y a paletas categóricas (colores que el usuario
 *    elige, no tokens de marca).
 *  - Línea suelta: comentario `design-tokens-ok: <motivo>` en la línea o en la anterior.
 *  - Bloque: `design-tokens-ok-begin: <motivo>` ... `design-tokens-ok-end`, para literales
 *    que ocupan varias líneas (listas de muestras de color, lienzos de selector).
 */
val verifyDesignTokens = tasks.register("verifyDesignTokens") {
    group = "verification"
    description = "Falla si hay colores hardcodeados fuera del sistema de diseño."

    val sourceRoot = file("src/main/java")
    inputs.dir(sourceRoot)
    // Sin salidas reales; marcamos un archivo sello para que Gradle pueda cachear la tarea.
    val stamp = layout.buildDirectory.file("reports/design-tokens/ok.txt")
    outputs.file(stamp)

    doLast {
        val hexColor = Regex("""Color\(0x[0-9A-Fa-f]{8}\)""")
        val namedColor = Regex("""Color\.(White|Black)\b""")
        val violations = mutableListOf<String>()

        sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { source ->
                val lines = source.readLines()
                val fileExempt = lines.take(20).any { "design-tokens-exempt:" in it }
                if (fileExempt) return@forEach

                var insideExemptBlock = false
                lines.forEachIndexed { index, line ->
                    if ("design-tokens-ok-begin:" in line) insideExemptBlock = true
                    if ("design-tokens-ok-end" in line) insideExemptBlock = false
                    if (insideExemptBlock) return@forEachIndexed
                    if (!hexColor.containsMatchIn(line) && !namedColor.containsMatchIn(line)) return@forEachIndexed
                    val previous = lines.getOrNull(index - 1).orEmpty()
                    if ("design-tokens-ok:" in line || "design-tokens-ok:" in previous) return@forEachIndexed
                    val relative = source.relativeTo(sourceRoot).invariantSeparatorsPath
                    violations += "  $relative:${index + 1}  ${line.trim()}"
                }
            }

        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Colores hardcodeados fuera del sistema de diseño (${violations.size}):")
                    violations.forEach { appendLine(it) }
                    appendLine()
                    appendLine("Usa los tokens de UniStackColors, o UniStackColors.contentColorOn(fondo)")
                    appendLine("para contenido sobre un color arbitrario.")
                    appendLine("Si el color es legítimo (paleta, matemática de contraste, sombra), añade")
                    appendLine("un comentario 'design-tokens-ok: <motivo>' en esa línea.")
                }
            )
        }

        stamp.get().asFile.apply {
            parentFile.mkdirs()
            writeText("sin colores hardcodeados\n")
        }
    }
}

tasks.named("check") { dependsOn(verifyDesignTokens) }

fun readTelegramEnv(): Map<String, String> {
    val envFile = rootProject.file(".env")
    if (!envFile.exists()) return emptyMap()

    return envFile.readLines()
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.startsWith("#") && "=" in it }
        .associate { line ->
            val key = line.substringBefore("=").trim()
            val value = line.substringAfter("=").trim().trim('"', '\'')
            key to value
        }
}

fun findApkForVariant(variant: String): File {
    val outputDir = layout.buildDirectory.dir("outputs/apk/$variant").get().asFile
    return outputDir
        .walkTopDown()
        .filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }
        .maxByOrNull { it.lastModified() }
        ?: throw GradleException("$variant APK not found under ${outputDir.absolutePath}")
}

fun summarizeChangeFiles(files: List<String>): List<String> {
    val normalized = files.map { it.replace('\\', '/') }.distinct()
    val hasSetupChanges = normalized.any { it.contains("feature_setup/") || it.contains("welcome_unistack_hero") }

    return buildList {
        if (hasSetupChanges) {
            add("Setup: pasos de modulos, resumen y cierre del onboarding redisenados.")
        }
        if (normalized.any { it.contains("core/design/theme/Type.kt") }) {
            add("Diseno: pesos tipograficos normalizados para textos y labels.")
        }
        if (normalized.any { it.contains("feature_grades/presentation/GradesScreen") }) {
            add("Materias: lista con progreso, estado y siguiente accion por materia.")
        } else if (normalized.any { it.contains("feature_grades/") || it.contains("core/utils/GradeCalculator") }) {
            add("Materias: detalle de cortes y editor de apariencia/corte actual redisenados.")
        }
        if (normalized.any { it.contains("feature_home/") }) {
            add("Home: hero inteligente actualizado con resultados, historial y porcentajes pendientes.")
        }
        if (normalized.any { it.contains("feature_tasks/") }) {
            add("Tareas: resultados pendientes agrupados y editor compacto con nota vinculada.")
        }
        if (normalized.any { it.contains("feature_profile/") }) {
            add("Configuracion: Perfil simplificado y ajustes separados por categoria.")
            add("Apariencia: tema, fondos, colores, densidad, Home y navegacion personalizables.")
        }
        if (normalized.any { it.contains("feature_schedule/") }) {
            add("Agenda: calendario y horario semanal conectados con materias y tareas.")
        }
        if (normalized.any { it.contains("AccessibilityPreferences") || it.contains("AccessibilitySettings") }) {
            add("Accesibilidad: idioma, contraste, texto, formato horario y movimiento centralizados.")
        }
        if (normalized.any { it.contains("feature_sync/") || it.contains("FirebaseGoogleAuthService") }) {
            add("Cuenta: Google y respaldos local/nube preparados para datos academicos y horario.")
        }
        if (normalized.any { it.contains("core/notifications/") }) {
            add("Recordatorios: clases, tareas y seguimiento academico integrados al historial real.")
        }
        val hasProductChanges = normalized.any { it.startsWith("app/src/") }
        if (!hasSetupChanges && !hasProductChanges && normalized.any { it == "app/build.gradle.kts" || it.startsWith("scripts/") || it.endsWith("send_apk.sh") }) {
            add("Build/release: changelog Telegram y validacion release afinados.")
        }
        if (normalized.any { it.contains("androidTest/") || it.contains("src/test/") }) {
            add("QA: pruebas conectadas/unitarias actualizadas.")
        }
        if (normalized.any { it == ".editorconfig" }) {
            add("Texto: configuracion UTF-8 fijada para evitar mojibake.")
        }
        if (normalized.any { it.endsWith(".md") }) {
            add("Limpieza: documentacion obsoleta retirada o actualizada.")
        }
        if (normalized.any { it.contains("core/navigation/") }) {
            add("Navegacion/setup: flujo principal ajustado.")
        }
        if (normalized.isNotEmpty() && isEmpty()) {
            add("Cambios locales: archivos del proyecto actualizados.")
        }
    }
}

fun projectSnapshotFiles(): List<File> {
    val excludedDirectories = setOf(
        ".git",
        ".gradle",
        ".gradle-user-home",
        ".toolchains",
        ".idea",
        ".signing",
        ".antigravitycli",
        "build"
    )
    val allowedExtensions = setOf(
        "bat",
        "gradle",
        "java",
        "jpeg",
        "jpg",
        "json",
        "kt",
        "kts",
        "md",
        "png",
        "properties",
        "sh",
        "toml",
        "webp",
        "xml",
        "yaml",
        "yml"
    )
    val allowedNames = setOf(".editorconfig", ".gitignore", "gradlew")
    val excludedNames = setOf(".env", "local.properties")
    val excludedExtensions = setOf("apk", "class", "jar", "jks", "keystore")

    return rootProject.rootDir
        .walkTopDown()
        .onEnter { it.name !in excludedDirectories }
        .filter { it.isFile }
        .filterNot { it.name in excludedNames }
        .filterNot { it.extension.lowercase(Locale.US) in excludedExtensions }
        .filter {
            it.name in allowedNames || it.extension.lowercase(Locale.US) in allowedExtensions
        }
        .sortedBy { rootProject.rootDir.toPath().relativize(it.toPath()).toString() }
        .toList()
}

fun sha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
}

fun currentProjectSnapshot(): Map<String, String> {
    val rootPath = rootProject.rootDir.toPath()
    return projectSnapshotFiles().associate { file ->
        val relativePath = rootPath.relativize(file.toPath()).toString().replace('\\', '/')
        relativePath to sha256(file)
    }
}

fun telegramSnapshotFile(variant: String): File =
    rootProject.file(".gradle/telegram-apk-$variant.snapshot.properties")

fun readTelegramSnapshot(variant: String): Map<String, String> {
    val file = telegramSnapshotFile(variant)
    if (!file.exists()) return emptyMap()

    return Properties().apply {
        file.inputStream().use(::load)
    }.entries.associate { (key, value) -> key.toString() to value.toString() }
}

fun writeTelegramSnapshot(variant: String, snapshot: Map<String, String>) {
    val file = telegramSnapshotFile(variant)
    file.parentFile.mkdirs()
    Properties().apply {
        snapshot.forEach { (path, hash) -> setProperty(path, hash) }
        file.outputStream().use { store(it, "Last UniStack Telegram APK snapshot for $variant") }
    }
}

fun changedFilesSinceSnapshot(previous: Map<String, String>, current: Map<String, String>): List<String> {
    return (previous.keys + current.keys)
        .filter { previous[it] != current[it] }
        .sorted()
}

fun telegramChangelogLinesForVariant(
    variant: String,
    currentSnapshot: Map<String, String> = currentProjectSnapshot()
): List<String> {
    val previousSnapshot = readTelegramSnapshot(variant)
    if (previousSnapshot.isEmpty()) {
        return listOf("Build: historial de cambios por APK activado desde este envio.")
    }

    val changedFiles = changedFilesSinceSnapshot(previousSnapshot, currentSnapshot)
    return summarizeChangeFiles(changedFiles)
        .ifEmpty { listOf("Sin cambios de codigo desde el APK anterior.") }
}

fun String.htmlEscape(): String {
    return replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}

fun telegramChangelogBlock(lines: List<String>, maxChars: Int = 480): String {
    val selected = mutableListOf<String>()
    var usedChars = 0
    for (line in lines) {
        val next = "- ${line.htmlEscape()}"
        if (usedChars + next.length + 1 > maxChars) {
            selected += "- ..."
            break
        }
        selected += next
        usedChars += next.length + 1
    }
    return selected.ifEmpty { listOf("- Build local generado.") }.joinToString("\n")
}

fun registerTelegramApkTask(variant: String) = tasks.register("send${variant.replaceFirstChar { it.uppercase() }}ApkToTelegram") {
    group = "distribution"
    description = "Sends the $variant APK to Telegram"
    onlyIf {
        val assembleTask = tasks.findByName("assemble${variant.replaceFirstChar { it.uppercase() }}")
        assembleTask?.state?.failure == null
    }

    doLast {
        val apkPath = findApkForVariant(variant)
        val telegramEnv = readTelegramEnv()
        val botToken = providers.environmentVariable("TELEGRAM_BOT_TOKEN")
            .orElse(telegramEnv["TELEGRAM_BOT_TOKEN"] ?: "")
            .get()
        val chatId = providers.environmentVariable("TELEGRAM_CHAT_ID")
            .orElse(telegramEnv["TELEGRAM_CHAT_ID"] ?: "")
            .get()
        val uploadMaxTime = providers.environmentVariable("TELEGRAM_UPLOAD_MAX_TIME")
            .orElse(telegramEnv["TELEGRAM_UPLOAD_MAX_TIME"] ?: "600")
            .get()

        if (botToken.isBlank() || chatId.isBlank()) {
            throw GradleException("TELEGRAM_BOT_TOKEN and TELEGRAM_CHAT_ID must be set in environment variables or .env")
        }

        val sizeMb = apkPath.length().toDouble() / 1024.0 / 1024.0
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val currentSnapshot = currentProjectSnapshot()
        val changelog = telegramChangelogBlock(telegramChangelogLinesForVariant(variant, currentSnapshot))
        val variantTitle = variant.replaceFirstChar { it.uppercase() }
        val sizeText = String.format(Locale.US, "%.2f", sizeMb)
        val caption = """
            <b>Nuevo APK de ${rootProject.name.htmlEscape()}</b>
            <blockquote>$variantTitle - ${apkPath.name.htmlEscape()} - $sizeText MB
            v${generatedVersionName.htmlEscape()} ($generatedVersionCode)
            $timestamp</blockquote>
            <b>Cambios</b>
            <blockquote>$changelog</blockquote>
        """.trimIndent()

        println("Sending ${apkPath.name} to Telegram...")
        providers.exec {
            commandLine(
                "curl",
                "--silent",
                "--show-error",
                "--fail-with-body",
                "--http1.1",
                "--retry",
                "4",
                "--retry-delay",
                "8",
                "--retry-all-errors",
                "--connect-timeout",
                "20",
                "--max-time",
                uploadMaxTime,
                "--form-string",
                "chat_id=$chatId",
                "-F",
                "document=@${apkPath.absolutePath}",
                "--form-string",
                "caption=$caption",
                "--form-string",
                "parse_mode=HTML",
                "https://api.telegram.org/bot$botToken/sendDocument"
            )
        }.result.get().assertNormalExitValue()
        writeTelegramSnapshot(variant, currentSnapshot)
        println("Telegram upload completed.")
    }
}

val sendDebugApkToTelegram = registerTelegramApkTask("debug")
val sendReleaseApkToTelegram = registerTelegramApkTask("release")
val skipTelegramApk = providers.gradleProperty("skipTelegramApk")
    .orElse(providers.environmentVariable("SKIP_TELEGRAM_APK"))
    .map { it.toBoolean() }
    .orElse(false)

val validateReleaseReady = tasks.register("validateReleaseReady") {
    group = "verification"
    description = "Validates signing settings before creating a release APK for distribution."

    doLast {
        if (!hasConfiguredReleaseSigning()) {
            throw GradleException(
                "Release APK requires releaseStoreFile, releaseStorePassword, releaseKeyAlias and releaseKeyPassword " +
                    "in local.properties, or RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS and RELEASE_KEY_PASSWORD."
            )
        }
        val store = rootProject.file(releaseStoreFileValue())
        if (!store.exists()) {
            throw GradleException("Release keystore not found: ${store.absolutePath}")
        }
    }
}

sendReleaseApkToTelegram.configure {
    dependsOn(validateReleaseReady)
}

val assembleReleaseAndSendToTelegram = tasks.register("assembleReleaseAndSendToTelegram") {
    group = "distribution"
    description = "Builds the release APK and sends it to Telegram on explicit request."
    dependsOn(validateReleaseReady)
    dependsOn("assembleRelease")
    finalizedBy(sendReleaseApkToTelegram)
}

fun githubReleaseSnapshotFile(): File =
    rootProject.file(".gradle/github-release.snapshot.properties")

/**
 * Las notas de la publicación salen de `CHANGELOG.md`, de la sección de esa versión exacta.
 *
 * Antes se generaban comparando huellas de archivos contra una instantánea de la publicación
 * anterior, así que decían qué archivos se tocaron —«cambios en la capa de datos»— y no qué
 * nota quien usa la app. Y si no había con qué comparar, salía «Primera version publicada».
 *
 * Falta la sección: falla. Es a propósito. Una versión sin notas escritas no debería llegar a
 * publicarse, y un texto de relleno generado automáticamente es peor que no publicar.
 */
fun changelogBodyFor(versionName: String): String {
    val file = rootProject.file("CHANGELOG.md")
    if (!file.exists()) {
        throw GradleException("Falta CHANGELOG.md en la raiz del proyecto.")
    }
    val lines = file.readLines()
    val heading = "## [$versionName]"
    val start = lines.indexOfFirst { it.trimStart().startsWith(heading) }
    if (start < 0) {
        throw GradleException(
            "CHANGELOG.md no tiene seccion para $versionName. Anade '$heading - <fecha>' con lo que " +
                "cambia para quien usa la app, y vuelve a publicar."
        )
    }
    val rest = lines.drop(start + 1)
    val end = rest.indexOfFirst { it.trimStart().startsWith("## [") }
    val body = (if (end < 0) rest else rest.take(end))
        .joinToString(System.lineSeparator())
        .trim()
        .removeSuffix("---")
        .trim()
    if (body.isBlank()) {
        throw GradleException("La seccion de $versionName en CHANGELOG.md esta vacia.")
    }
    return body
}

val validateGitHubPublishReady = tasks.register("validateGitHubPublishReady") {
    group = "verification"
    description = "Validates that a build is intentional and properly signed before publishing a public GitHub Release."

    doLast {
        if (!isUsingRealReleaseSigning()) {
            throw GradleException(
                "El release esta firmado con credenciales de desarrollo por defecto (o falta configurar la firma). " +
                    "Configura RELEASE_STORE_PASSWORD y RELEASE_KEY_PASSWORD reales en local.properties o variables de " +
                    "entorno antes de publicar en GitHub Releases."
            )
        }
        if (!hasExplicitVersionName) {
            throw GradleException(
                "Falta indicar la version publica. Vuelve a ejecutar con -PversionName=X.Y.Z (ej: -PversionName=1.1.0)."
            )
        }
    }
}

val publishReleaseToGitHub = tasks.register("publishReleaseToGitHub") {
    group = "distribution"
    description = "Creates a GitHub Release with the signed release APK and changelog. " +
        "Run: ./gradlew publishReleaseToGitHub -PversionName=X.Y.Z (requires real release signing + GITHUB_TOKEN)"
    dependsOn(validateGitHubPublishReady)
    dependsOn("assembleRelease")

    doLast {
        val apkPath = findApkForVariant("release")
        val telegramEnv = readTelegramEnv()
        val githubToken = providers.environmentVariable("GITHUB_TOKEN")
            .orElse(telegramEnv["GITHUB_TOKEN"] ?: "")
            .get()
        if (githubToken.isBlank()) {
            throw GradleException("GITHUB_TOKEN no configurado en .env o variable de entorno.")
        }

        val versionName = generatedVersionName
        val tagName = "v$versionName"
        val body = changelogBodyFor(versionName)

        /*
         * Una versión con sufijo (1.1.0-alpha.1) se publica como preestreno salvo que se diga
         * lo contrario con -Pprerelease=false. Va por el nombre y no por una bandera suelta
         * para que no puedan contradecirse: lo que dice la etiqueta es lo que se publica.
         */
        val isPreRelease = providers.gradleProperty("prerelease")
            .map { it.toBoolean() }
            .getOrElse(versionName.contains('-'))

        val createPayload = mapOf(
            "tag_name" to tagName,
            "name" to versionName,
            "body" to body,
            "draft" to false,
            "prerelease" to isPreRelease
        )

        val tempDir = File(buildDir, "github-release").apply { mkdirs() }
        val payloadFile = File(tempDir, "payload.json")
        val responseFile = File(tempDir, "response.json")

        payloadFile.writeText(JsonOutput.toJson(createPayload))

        println("Creating GitHub Release $tagName${if (isPreRelease) " (preestreno)" else ""}...")
        val createResult = project.exec {
            commandLine = listOf(
                "curl", "-s", "-X", "POST",
                "-H", "Authorization: Bearer $githubToken",
                "-H", "Accept: application/vnd.github+json",
                "-H", "Content-Type: application/json",
                "-d", "@${payloadFile.absolutePath}",
                "https://api.github.com/repos/$githubReleasesSlug/releases"
            )
            standardOutput = responseFile.outputStream()
            isIgnoreExitValue = true
        }

        if (createResult.exitValue != 0) {
            throw GradleException("curl exited with ${createResult.exitValue}. Response: ${responseFile.readText()}")
        }

        if (!responseFile.exists() || responseFile.length() == 0L) {
            throw GradleException("No response from GitHub API. Check GITHUB_TOKEN and connectivity.")
        }

        val responseText = responseFile.readText()
        @Suppress("UNCHECKED_CAST")
        val parsed = JsonSlurper().parseText(responseText) as Map<String, Any?>

        if (parsed.containsKey("errors") || parsed.containsKey("message")) {
            throw GradleException("GitHub API error: ${parsed["message"] ?: parsed["errors"]}")
        }

        val releaseId = (parsed["id"] as? Number)?.toLong()
            ?: throw GradleException("Could not extract release ID. Response: $responseText")

        println("Uploading ${apkPath.name}...")
        val uploadUrl = "https://uploads.github.com/repos/$githubReleasesSlug/releases/$releaseId/assets?name=${apkPath.name}"

        val uploadResult = project.exec {
            commandLine = listOf(
                "curl", "-s", "-X", "POST",
                "-H", "Authorization: Bearer $githubToken",
                "-H", "Content-Type: application/vnd.android.package-archive",
                "--data-binary", "@${apkPath.absolutePath}",
                uploadUrl
            )
            isIgnoreExitValue = true
        }

        if (uploadResult.exitValue != 0) {
            throw GradleException("APK upload failed (curl exit ${uploadResult.exitValue})")
        }

        println("✓ Published $tagName to https://github.com/$githubReleasesSlug/releases/tag/$tagName")
    }
}

afterEvaluate {
    tasks.findByName("assembleRelease")?.mustRunAfter(validateReleaseReady)
    tasks.findByName("assembleRelease")?.mustRunAfter(validateGitHubPublishReady)

    if (!skipTelegramApk.get()) {
        tasks.named("assembleDebug") {
            finalizedBy(sendDebugApkToTelegram)
        }

        tasks.named("assembleRelease") {
            finalizedBy(sendReleaseApkToTelegram)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
