import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.*

val appVersionName = "2.1.3"
val appVersionCode = 5

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "1.9.0"
    id("com.google.gms.google-services")
    id("com.codingfeline.buildkonfig") version "0.17.1"
}

buildkonfig {
    packageName = "sasipca"
    objectName = "AppConfig"

    defaultConfigs {
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "VERSION", appVersionName)
    }
}

kotlin {
    jvmToolchain(21)

    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.mlkit.barcode)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.firebase.messaging)
            implementation(libs.androidx.preference)
            implementation("androidx.glance:glance-material3:1.1.1")
            implementation("androidx.glance:glance-appwidget:1.1.1")
            implementation("com.microsoft.identity.client:msal:5.5.0") {
                exclude(group = "com.microsoft.device.display")
            }
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.multiplatform.settings)
            implementation(libs.kotlinx.datetime)

            val voyagerVersion = "1.1.0-beta03"
            implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")

            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.java)
            implementation(libs.ktor.client.cio)
            implementation(libs.multiplatform.settings.jvm)
            implementation("com.microsoft.azure:msal4j:1.23.1")
            implementation("org.slf4j:slf4j-simple:2.0.17")
            implementation("com.microsoft.signalr:signalr:10.0.1")
            implementation("io.reactivex.rxjava3:rxjava:3.1.12")
            implementation("io.github.kdroidfilter:composenativetray:1.0.4")
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "app.sasipca"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "app.sasipca"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.desktop {
    application {
        mainClass = "sasipca.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe, TargetFormat.Rpm)
            packageName = "sasipca"
            packageVersion = appVersionName
            description = "SASIPCA"
            vendor = "G8 IPCA"

            includeAllModules = true
            javaHome = System.getProperty("java.home")

            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icons/icon512x512.icns"))
                bundleID = "app.sasipca"
            }

            windows {
                iconFile.set(project.file("src/jvmMain/resources/icons/icon512x512.ico"))
                shortcut = true
                upgradeUuid = "6f272782-747d-4c3e-9080-877f1687f340"
            }

            linux {
                // No Linux, o iconFile deve estar acessível durante o packaging
                iconFile.set(project.file("src/jvmMain/resources/icons/icon512x512.png"))
                shortcut = true
                menuGroup = "Utility"
                // Importante para o KDE associar o processo ao ícone da tray
                appRelease = "1"
                appCategory = "Office"
            }
        }
    }
}

// --- Tasks de Organização de Ficheiros Finais ---

tasks.register("renameDesktopDistributables") {
    val version = appVersionName
    val packageDir = layout.buildDirectory.dir("compose/binaries/main")

    doLast {
        val dir = packageDir.get().asFile
        val extensions = listOf("msi", "exe", "deb", "dmg", "pkg", "rpm")
        if (dir.exists()) {
            dir.walkTopDown().forEach { file ->
                if (file.extension in extensions && !file.name.contains(version)) {
                    val newName = "sasipca-$version.${file.extension}"
                    file.renameTo(File(file.parent, newName))
                }
            }
        }
    }
}

tasks.register<Copy>("copyAndRenameDebugApk") {
    val debugDir = layout.buildDirectory.dir("outputs/apk/debug")
    val version = appVersionName

    from(debugDir)
    into(layout.buildDirectory.dir("outputs/final-apk"))
    include("*.apk")

    rename { fileName ->
        if (fileName.endsWith(".apk")) "sasipca-$version.apk" else fileName
    }
}

afterEvaluate {
    tasks.matching {
        it.name.contains("package") || it.name.contains("createDistributable")
    }.configureEach {
        if (!this.name.contains("rename")) {
            finalizedBy("renameDesktopDistributables")
        }
    }

    tasks.named("assembleDebug") {
        finalizedBy("copyAndRenameDebugApk")
    }
}