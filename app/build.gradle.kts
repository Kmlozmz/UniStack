import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.Properties
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
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
    compileSdk = 35

    defaultConfig {
        applicationId = "com.unistack.app"
        minSdk = 26
        targetSdk = 35
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
            storeFile = rootProject.file(localProperty("releaseStoreFile").ifBlank { ".signing/unistack-release.jks" })
            storePassword = localProperty("releaseStorePassword").ifBlank { "unistack-dev-release" }
            keyAlias = localProperty("releaseKeyAlias").ifBlank { "unistack" }
            keyPassword = localProperty("releaseKeyPassword").ifBlank { "unistack-dev-release" }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("localRelease")
        }

        release {
            isMinifyEnabled = false
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
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.9")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    implementation("io.coil-kt:coil-compose:2.6.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.room:room-testing:$roomVersion")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.robolectric:robolectric:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("androidx.credentials:credentials:1.6.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.android.billingclient:billing-ktx:8.3.0")

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

fun gitExecutable(): String {
    val configured = localProperty("gitExecutable")
        .ifBlank { System.getenv("GIT_EXECUTABLE").orEmpty() }
    if (configured.isNotBlank()) return configured

    return listOf(
        "C:/Program Files/Git/cmd/git.exe",
        "C:/Program Files/Git/bin/git.exe",
        "C:/Program Files (x86)/Git/cmd/git.exe"
    ).firstOrNull { rootProject.file(it).exists() } ?: "git"
}

fun runGit(vararg args: String): String? {
    return try {
        val output = ByteArrayOutputStream()
        val gitErrorOutput = ByteArrayOutputStream()
        val result = providers.exec {
            commandLine(listOf(gitExecutable()) + args)
            workingDir(rootProject.rootDir)
            standardOutput = output
            errorOutput = gitErrorOutput
            isIgnoreExitValue = true
        }.result.get()
        output.toString().trim().takeIf { result.exitValue == 0 && it.isNotBlank() }
    } catch (_: Exception) {
        null
    }
}

fun gitChangedFiles(): List<String> {
    return runGit("status", "--short", "--untracked-files=all")
        ?.lineSequence()
        ?.map { it.drop(3).trim().substringAfter(" -> ") }
        ?.filter { it.isNotBlank() }
        ?.toList()
        .orEmpty()
}

fun recentProjectFiles(hours: Long = 72): List<String> {
    val cutoff = System.currentTimeMillis() - hours * 60L * 60L * 1000L
    val excludedDirectories = setOf(".git", ".gradle", ".toolchains", "build")
    val rootPath = rootProject.rootDir.toPath()

    return rootProject.rootDir
        .walkTopDown()
        .onEnter { it.name !in excludedDirectories }
        .filter { it.isFile && it.lastModified() >= cutoff }
        .map { rootPath.relativize(it.toPath()).toString().replace('\\', '/') }
        .filterNot { it.endsWith(".apk") || it.endsWith(".class") || it.endsWith(".jar") }
        .distinct()
        .toList()
}

fun summarizeChangeFiles(files: List<String>): List<String> {
    val normalized = files.map { it.replace('\\', '/') }.distinct()

    return buildList {
        if (normalized.any { it.contains("feature_grades/") || it.contains("core/utils/GradeCalculator") }) {
            add("Notas: rediseño de cortes/notas y ajustes académicos aplicados.")
        }
        if (normalized.any { it.contains("feature_home/") }) {
            add("Home: plan diario y motor de prioridades actualizado.")
        }
        if (normalized.any { it.contains("feature_profile/") }) {
            add("Perfil: recordatorios contextuales y exportación de datos ajustados.")
        }
        if (normalized.any { it == "app/build.gradle.kts" || it.startsWith("scripts/") || it.endsWith("send_apk.sh") }) {
            add("Build: versión automática y envío de APK por Telegram afinados.")
        }
        if (normalized.any { it.contains("androidTest/") || it.contains("src/test/") }) {
            add("QA: pruebas conectadas/unitarias actualizadas.")
        }
        if (normalized.any { it == ".editorconfig" }) {
            add("Texto: configuración UTF-8 fijada para evitar mojibake.")
        }
        if (normalized.any { it.endsWith(".md") }) {
            add("Limpieza: documentación obsoleta retirada o actualizada.")
        }
        if (normalized.any { it.contains("core/navigation/") || it.contains("feature_setup/") }) {
            add("Navegación/setup: flujo principal ajustado.")
        }
        if (normalized.isNotEmpty() && isEmpty()) {
            add("Cambios locales: archivos del proyecto actualizados.")
        }
    }
}

fun gitChangelogLines(): List<String> {
    val reliableLocalSummaries = summarizeChangeFiles(gitChangedFiles())
    if (reliableLocalSummaries.isNotEmpty()) return reliableLocalSummaries

    val reliableRecentSummaries = summarizeChangeFiles(recentProjectFiles())
    if (reliableRecentSummaries.isNotEmpty()) return reliableRecentSummaries

    val files = gitChangedFiles()
    val localSummaries = buildList {
        if (files.any { it.contains("feature_home/") }) {
            add("Home: plan diario y motor de prioridades actualizado.")
        }
        if (files.any { it.contains("feature_profile/") }) {
            add("Perfil: recordatorios contextuales y datos/exportación ajustados.")
        }
        if (files.any { it == "app/build.gradle.kts" || it.startsWith("scripts/") }) {
            add("Build: versión automática y envío de APK por Telegram afinados.")
        }
        if (files.any { it.contains("androidTest/") || it.contains("src/test/") }) {
            add("QA: pruebas conectadas/unitarias actualizadas.")
        }
        if (files.any { it == ".editorconfig" }) {
            add("Texto: configuración UTF-8 fijada para evitar mojibake.")
        }
        if (files.any { it.endsWith(".md") }) {
            add("Limpieza: documentación obsoleta retirada o actualizada.")
        }
        if (files.any { it.contains("core/navigation/") || it.contains("feature_setup/") }) {
            add("Navegación/setup: flujo principal ajustado.")
        }
        if (files.any { it.contains("feature_grades/") || it.contains("core/utils/GradeCalculator") }) {
            add("Notas: cálculo y pantallas académicas ajustadas.")
        }
    }

    if (localSummaries.isNotEmpty()) return localSummaries

    return runGit("log", "-5", "--pretty=format:%h %s")
        ?.lineSequence()
        ?.map { "Commit: ${it.trim()}" }
        ?.filter { it.isNotBlank() }
        ?.toList()
        ?.takeIf { it.isNotEmpty() }
        ?: listOf("Build local generado; no fue posible leer cambios locales.")
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
        return listOf("Build: historial de cambios por APK activado desde este envío.")
    }

    val changedFiles = changedFilesSinceSnapshot(previousSnapshot, currentSnapshot)
    return summarizeChangeFiles(changedFiles)
        .ifEmpty { listOf("Sin cambios de código desde el APK anterior.") }
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
            <blockquote>$variantTitle · ${apkPath.name.htmlEscape()} · $sizeText MB
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
val sendReleaseApk = providers.gradleProperty("sendReleaseApk")
    .orElse(providers.environmentVariable("SEND_RELEASE_TELEGRAM_APK"))
    .map { it.toBoolean() }
    .orElse(false)

val assembleReleaseAndSendToTelegram = tasks.register("assembleReleaseAndSendToTelegram") {
    group = "distribution"
    description = "Builds the release APK and sends it to Telegram on explicit request."
    dependsOn("assembleRelease")
    finalizedBy(sendReleaseApkToTelegram)
}

afterEvaluate {
    if (!skipTelegramApk.get()) {
        tasks.named("assembleDebug") {
            finalizedBy(sendDebugApkToTelegram)
        }

        if (sendReleaseApk.get()) {
            tasks.named("assembleRelease") {
                finalizedBy(sendReleaseApkToTelegram)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
