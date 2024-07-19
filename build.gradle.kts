import org.jetbrains.kotlin.gradle.utils.extendsFrom
import java.net.URI

plugins {
    kotlin("jvm")
    id("fabric-loom")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.ashhack"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://impactdevelopment.github.io/maven/")
    maven("https://jitpack.io")
}

val modInclude by configurations.creating
val modInclude0 = configurations.named("modInclude")

val library by configurations.creating
val library0 = configurations.named("library")

val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fapi_version: String by project
val fkotlin_version: String by project
val reflections_version: String by project

configurations {
    modImplementation.extendsFrom(modInclude0)
    include.extendsFrom(modInclude0)

    implementation.extendsFrom(library0)
    shadow.extendsFrom(library0)
}

dependencies {
    "minecraft"("com.mojang:minecraft:${minecraft_version}")
    "mappings"("net.fabricmc:yarn:${yarn_mappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loader_version}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fapi_version}")
//    modImplementation("baritone-api-fabric:baritone-api-fabric:1.6.3")
    implementation(files("src/main/resources/baritone-api-fabric-1.10.2.jar"))
//    modImplementation("org.ladysnake:satin:1.15.0")
    // Include Satin as a Jar-in-Jar dependency (optional)
//    include("org.ladysnake:satin:1.15.0")
//    implementation(files("src/main/resources/META-INF/jars/satin-1.15.0.jar"))
}

loom {
    accessWidenerPath.set(file("src/main/resources/ash.accesswidener"))
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property ("minecraft_version", minecraft_version)
    inputs.property ("fabric_version", fapi_version)
    inputs.property ("loader_version", loader_version)
    inputs.property ("fkotlin_version", fkotlin_version)

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to minecraft_version,
            "fabric_version" to fapi_version,
            "loader_version" to loader_version,
            "fkotlin_version" to fkotlin_version
        )
    }
}

// ensure that the encoding is set to UTF-8, no matter what the system default is
// this fixes some edge cases with special characters not displaying correctly
// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
tasks.compileJava {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    options.release.set(17)
}

kotlin {
    jvmToolchain(17)
}