import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    id("com.google.devtools.ksp") version "1.8.0-1.0.8"
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

    val moshi = "1.14.0"
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshi")

    val moshix = "1.7.20-Beta-0.18.3"
    implementation("dev.zacsweers.moshix:moshi-sealed-runtime:$moshix")
    ksp("dev.zacsweers.moshix:moshi-sealed-codegen:$moshix")
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