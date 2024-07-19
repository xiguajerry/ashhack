import java.net.URI

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }

    val fkotlin_version: String by settings

    plugins {
        id("fabric-loom") version "1.6.3"
        id("org.jetbrains.kotlin.jvm") version fkotlin_version.split("+kotlin.")[1]
    }
}

rootProject.name = "ashhack"