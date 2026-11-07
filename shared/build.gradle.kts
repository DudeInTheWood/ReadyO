plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        namespace = "app.readyo.shared"
        compileSdk { version = release(37) { minorApiLevel = 2 } }
        minSdk = 26
        androidResources.enable = true
        withHostTestBuilder {}
    }
    iosArm64 {
        binaries.framework {
            baseName = "ReadyOShared"
            isStatic = true
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "ReadyOShared"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation("io.github.alexzhirkevich:compottie-lite:2.3.3")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            implementation("io.ktor:ktor-client-core:3.3.3")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
        androidMain.dependencies { implementation("io.ktor:ktor-client-okhttp:3.3.3") }
        iosMain.dependencies { implementation("io.ktor:ktor-client-darwin:3.3.3") }
    }
}

compose.resources {
    packageOfResClass = "app.readyo.resources"
}
