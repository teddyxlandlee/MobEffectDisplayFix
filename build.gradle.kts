plugins {
    id("java")
    id("xland.gradle.forge-init-injector") version "3.1.0"
}

group = "xland.mcmod"
version = project.ext["app_version"]!!

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net") {
        name = "Fabric"
    }
}

dependencies {
    implementation("net.fabricmc:sponge-mixin:0.16.5+mixin.0.8.7")
    implementation("net.fabricmc:fabric-loader:${project.ext["fabric_loader_version"]}")
    implementation("org.ow2.asm:asm-tree:9.9")
    implementation("org.slf4j:slf4j-api:2.0.16")
    compileOnly("org.jetbrains:annotations:26.1.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    inputs.properties("version" to project.version)

    filesMatching(listOf("fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
        expand("version" to project.version)
    }
}

tasks.withType(Jar::class).configureEach {
    manifest.attributes("MixinConfigs" to "mob_effect_display_fix.mixins.json")
    from("LICENSE.txt") {
        rename { "META-INF/LICENSE_${project.name}.txt" }
    }
}

forgeInitInjector {
    stubPackage = "X8UrlZ3laULt41ERJ1GvY"
    modId = "mob_effect_display_fix"
    neoFlag("post_20_5")    // This branch is for 1.21.5+
}

java.withSourcesJar()
