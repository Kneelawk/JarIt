import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    alias(libs.plugins.quilt.loom)
    kotlin("jvm").version(libs.versions.kotlin.jvm.get())
}

val maven_group: String by project
group = maven_group

// System to get the release version if this project is being built as part of a release
val modVersion: String = if (System.getenv("RELEASE_TAG") != null) {
    val releaseTag = System.getenv("RELEASE_TAG")
    val modVersion = releaseTag.substring(1)
    println("Detected Release Version: $modVersion")
    modVersion
} else {
    val mod_version: String by project
    println("Detected Local Version: $mod_version")
    mod_version
}
version = modVersion

val archives_base_name: String by project
base {
    archivesName.set(archives_base_name)
}

repositories {
    mavenCentral()

    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
    maven("https://maven.alexiil.uk/") { name = "AlexIIL" }
    maven("https://kneelawk.com/maven/") { name = "Kneelawk" }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        addLayer(quiltMappings.mappings("org.quiltmc:quilt-mappings:${libs.versions.quilt.mappings.get()}:v2"))
    })

    // Using modCompileOnly & modLocalRuntime so that these dependencies don't get brought into any projects that depend
    // on this one.

    // Quilt Loader
    modCompileOnly(libs.quilt.loader)
    modLocalRuntime(libs.quilt.loader)

    // Quilted Fabric Api
    modCompileOnly(libs.quilted.fabric.api)
    modLocalRuntime(libs.quilted.fabric.api)

    // Fabric Language Kotlin
    modImplementation(libs.fabric.kotlin)

    // LNS
    modImplementation(libs.lns)

    // QOL runtime mods (e.g. mod menu)
    modRuntimeOnly(libs.bundles.qol.mods)

    // We use JUnit 4 because many Minecraft classes require heavy mocking or complete gutting, meaning a custom
    // classloader is required. JUnit 5 does not yet support using custom classloaders.
    testImplementation("junit:junit:4.13.2")
}

tasks {
    val javaVersion = JavaVersion.VERSION_17

    processResources {
        inputs.property("version", modVersion)

        exclude("**/*.xcf")

        filesMatching("quilt.mod.json") {
            expand(mapOf("version" to modVersion))
        }
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to modVersion))
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    withType<KotlinCompile> {
        kotlinOptions { jvmTarget = javaVersion.toString() }
    }

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }

        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion

        withSourcesJar()
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${archives_base_name}" }
        }
    }

    javadoc {
        options {
            optionFiles(file("javadoc-options.txt"))
        }
    }

    test {
        useJUnit()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = maven_group
            artifactId = project.name
            version = modVersion

            from(components["java"])
        }
    }

    repositories {
        if (System.getenv("PUBLISH_REPO") != null) {
            maven {
                name = "publishRepo"
                url = uri(System.getenv("PUBLISH_REPO"))
            }
        }
    }
}
