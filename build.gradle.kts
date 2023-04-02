import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    application
}

group = BotProjectConfigs.GROUP
version = BotProjectConfigs.VERSION

repositories {
    mavenCentral()
}

dependencies {
    val jda = "5.0.0-beta.5"
    implementation("net.dv8tion:JDA:$jda")

    implementation("com.squareup.retrofit2:converter-gson:2.7.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
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