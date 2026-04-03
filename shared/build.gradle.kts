plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization") version "2.1.20"
    id("app.cash.sqldelight") version "2.0.2"
    id("com.google.devtools.ksp") version "2.1.20-1.0.32"
}

kotlin {
    androidTarget()

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                
                // Ktor for HTTP client
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
                
                // SQLDelight for multiplatform database
                implementation("app.cash.sqldelight:runtime:2.0.2")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.7.2")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.1")
                
                // Room for Android database
                implementation("androidx.room:room-runtime:2.6.1")
                implementation("androidx.room:room-ktx:2.6.1")
                
                // DataStore for settings
                implementation("androidx.datastore:datastore-preferences:1.0.0")
                
                // Keep SQLDelight runtime for common code compatibility
                implementation("app.cash.sqldelight:runtime:2.0.2")
            }
        }
        val iosX64Main by getting {
            dependencies {
                implementation("app.cash.sqldelight:native-driver:2.0.2")
            }
        }
        val iosArm64Main by getting {
            dependencies {
                implementation("app.cash.sqldelight:native-driver:2.0.2")
            }
        }
        val iosSimulatorArm64Main by getting {
            dependencies {
                implementation("app.cash.sqldelight:native-driver:2.0.2")
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation("io.ktor:ktor-client-cio:2.3.7")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
                
                // SQLDelight JDBC driver for desktop
                implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
            }
        }
    }
    
    // KSP configuration for Room
    dependencies {
        ksp("androidx.room:room-compiler:2.6.1")
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.myapplication.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

sqldelight {
    databases {
        create("StudyDatabaseSqlDelight") {
            packageName.set("data.db")
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
    }
}
