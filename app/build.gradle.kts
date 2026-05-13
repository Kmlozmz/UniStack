import java.util.Properties

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

android {
    namespace = "com.unistack.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.unistack.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProperty("googleWebClientId")}\"")
        buildConfigField("String", "PRO_MONTHLY_PRODUCT_ID", "\"${localProperty("proMonthlyProductId").ifBlank { "unistack_pro_monthly" }}\"")

        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProperty("googleWebClientId")}\"")
        buildConfigField("String", "PRO_MONTHLY_PRODUCT_ID", "\"${localProperty("proMonthlyProductId").ifBlank { "unistack_pro_monthly" }}\"")

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

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

    // Room
    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
}



fun registerTelegramApkTask(variant: String) = tasks.register("send${variant.replaceFirstChar { it.uppercase() }}ApkToTelegram") {
    group = "distribution"
    description = "Sends the $variant APK to Telegram"

    doLast {
        val apkPath = layout.buildDirectory.file("outputs/apk/$variant/app-$variant.apk").get().asFile
        val scriptPath = rootProject.file("scripts/send_apk.sh")

        if (!scriptPath.exists()) {
            throw GradleException("Telegram script not found at ${scriptPath.absolutePath}")
        }
        if (!apkPath.exists()) {
            throw GradleException("$variant APK not found at ${apkPath.absolutePath}")
        }

        println("Sending ${rootProject.name} $variant APK to Telegram...")
        providers.exec {
            commandLine(scriptPath.absolutePath, apkPath.absolutePath, rootProject.name, variant)
        }.result.get().assertNormalExitValue()
    }
}

val sendDebugApkToTelegram = registerTelegramApkTask("debug")
val sendReleaseApkToTelegram = registerTelegramApkTask("release")

afterEvaluate {
    tasks.named("assembleDebug") {
        finalizedBy(sendDebugApkToTelegram)
    }

    tasks.named("assembleRelease") {
        finalizedBy(sendReleaseApkToTelegram)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
