import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "1.9.0"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)

        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
            implementation("io.ktor:ktor-client-okhttp:3.3.1")
            implementation("androidx.preference:preference-ktx:1.2.1")

            implementation("com.google.mlkit:barcode-scanning:17.2.0")
            implementation("androidx.camera:camera-camera2:1.3.4")
            implementation("androidx.camera:camera-lifecycle:1.3.4")
            implementation("androidx.camera:camera-view:1.3.4")


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
            implementation("io.ktor:ktor-client-core:3.3.2")
            implementation("io.ktor:ktor-client-content-negotiation:3.3.2")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.2")
            implementation("io.ktor:ktor-client-auth:3.3.2")
            implementation("io.ktor:ktor-client-logging:3.3.2")
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            implementation("com.russhwolf:multiplatform-settings:1.3.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1-0.6.x-compat")

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.java)
            implementation("io.ktor:ktor-client-cio:3.3.1")
            implementation("com.russhwolf:multiplatform-settings-jvm:1.3.0")
            implementation("org.slf4j:slf4j-simple:2.0.17")


        }
    }
}

android {
    namespace = "app.sasipca"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "app.sasipca"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "sasipca.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            description = "SASIPCA"
            vendor = "G8 IPCA"
            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icons/icon512x512.icns"))
            }
            windows {
                iconFile.set(project.file("src/jvmMain/resources/icons/icon512x512.ico"))
                shortcut = true
            }
            linux {
                iconFile.set(project.file("src/jvmMain/resources/icons/icon512x512.png"))
            }
            packageName = "sasipca"
            packageVersion = "1.0.0"
        }
    }
}