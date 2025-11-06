import java.time.Instant

plugins {
    id("java")
    id("dev.architectury.loom") version "1.11-SNAPSHOT"
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("com.gradleup.shadow") version "9.2.2"
}

architectury {
    minecraft = rootProject.ext["minecraft_version"].toString()
    common("fabric", "neoforge", "forge")
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")

    base {
        archivesName = "${rootProject.name}-${project.name}"
    }
}

allprojects {
    group = "xland.mcmod"
    version = project.ext["app_version"]!!

    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net")
        maven("https://maven.neoforged.net/releases")
        maven("https://mvn.7c7.icu")
        maven("https://maven.architectury.dev")
    }

    dependencies {
        minecraft("com.mojang:minecraft:${project.ext["minecraft_version"]}")
        mappings(loom.officialMojangMappings())

//    implementation("net.fabricmc:sponge-mixin:0.16.5+mixin.0.8.7")
//    implementation("org.ow2.asm:asm-tree:9.9")
        compileOnly("org.jetbrains:annotations:26.0.2")
    }

    tasks.processResources {
        inputs.properties("version" to project.version)

        filesMatching(listOf("fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
            expand("version" to project.version)
        }
    }

    tasks.withType(JavaCompile::class).configureEach {
        options.encoding = "utf8"
        options.release.set(21)
    }

    java {
        toolchain.languageVersion = JavaLanguageVersion.of(21)
        withSourcesJar()
    }
}

tasks.withType(Jar::class).configureEach {
    manifest.attributes("MixinConfigs" to "mob_effect_display_fix.mixins.json")
    from("LICENSE.txt") {
        rename { "META-INF/LICENSE_${project.name}.txt" }
    }
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${project.ext["fabric_loader_version"]}")
}

loom {
    mixin {
        defaultRefmapName = "MobEffectDisplayFix.refmap.json"
    }
}

subprojects {
    apply(plugin = "com.gradleup.shadow")

    architectury {
        platformSetupLoomIde()
    }

    val common by configurations.registering
    val shadowCommon by configurations.registering
    configurations.compileClasspath.configure { extendsFrom(common.get()) }
    configurations.runtimeClasspath.configure { extendsFrom(common.get()) }

    tasks.shadowJar {
        exclude("architectury.common.json")
        configurations.add(shadowCommon)
        archiveClassifier.set("dev-shadow")
    }

    tasks.remapJar {
        inputFile.set(provider { tasks.shadowJar.get().archiveFile.get() })
        dependsOn(tasks.shadowJar)
        archiveClassifier.set(null as String?)
    }

    tasks.jar {
        archiveClassifier.set("dev")
    }

    tasks.getByName("sourcesJar", Jar::class) {
        val commonSources = rootProject.tasks.getByName<Jar>("sourcesJar")
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })
    }

    components.getByName("java") {
//        withVariantsFromConfiguration(project.configurations.getByName("shadowRuntimeElements")) {
//            skip()
//        }
    }

    tasks.jar {
        manifest {
            attributes(
                "Specification-Title" to "MobEffectDisplayFix",
                "Specification-Vendor" to "teddyxlandlee",
                "Specification-Version" to 1,
                "Implementation-Title" to "${rootProject.name}-${project.name}",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "teddyxlandlee",
                "Implementation-Timestamp" to Instant.now()
            )
        }
    }
}
