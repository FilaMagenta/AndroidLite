plugins {
    kotlin("jvm") version "1.8.10"
    id("org.jetbrains.compose") version "1.3.1"
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.json:json:20230227")

    testImplementation("junit:junit:4.13.2")
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
