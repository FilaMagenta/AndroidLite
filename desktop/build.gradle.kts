import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    kotlin("jvm") version "1.8.10"
    id("org.jetbrains.compose") version "1.3.1"
    id("com.google.devtools.ksp")
}

version = "0.0.1"

val compileKotlin: KotlinCompilationTask<*> by tasks
compileKotlin.compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")

// Use KSP Generated sources
sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

dependencies {
    // Jetpack Compose dependencies
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    implementation("org.json:json:20230227")

    // Some utility annotations
    implementation("androidx.annotation:annotation:1.6.0")

    // Include core
    implementation(project(":core"))

    // For using MS SQL databases
    api("net.sourceforge.jtds:jtds:1.3.1")

    // Dependency injection
    implementation("io.insert-koin:koin-annotations:1.2.0")
    ksp("io.insert-koin:koin-ksp-compiler:1.2.0")
    implementation("io.insert-koin:koin-core:3.4.0")

    // Issue reporting
    implementation("io.sentry:sentry:6.17.0")

    testImplementation("junit:junit:4.13.2")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb)
            packageName = "filamagenta-desktop"

            packageVersion = "0.0.1"

            val iconsRoot = project.file("src/main/resources/icons")

            linux {
                iconFile.set(iconsRoot.resolve("icon.png"))
            }

            windows {
                iconFile.set(iconsRoot.resolve("icon.ico"))
            }
        }
    }
}
