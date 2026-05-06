plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.unistack.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.unistack.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.9")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
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
