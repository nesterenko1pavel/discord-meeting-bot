import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    application
}

group = BotProjectConfigs.GROUP
version = BotProjectConfigs.VERSION

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.5")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}