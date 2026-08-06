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

val fallbackVersionCode = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHH"))
val generatedVersionCode = providers.gradleProperty("versionCode")
    .orElse(providers.environmentVariable("VERSION_CODE"))
    .orElse(fallbackVersionCode)
    .get()
    .filter(Char::isDigit)
    .take(9)
    .toIntOrNull()
    ?: fallbackVersionCode.toInt()
val generatedVersionName = providers.gradleProperty("versionName")
    .orElse(providers.environmentVariable("VERSION_NAME"))
    .orElse("1.0.$generatedVersionCode")
    .get()

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

}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.05.01"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.05.01"))

    //noinspection GradleDependency
    implementation("androidx.core:core-ktx:1.18.0")
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
}



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

        val currentSnapshot = currentProjectSnapshot()
        val sizeMb = apkPath.length().toDouble() / 1024.0 / 1024.0
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
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

afterEvaluate {
    tasks.findByName("assembleRelease")?.mustRunAfter(validateReleaseReady)

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
