pluginManagement {
    repositories {
        maven("https://mvn.7c7.icu") {
            name = "7c7Maven"
        }
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev")
        maven("https://maven.neoforged.net/releases")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "MobEffectDisplayFix"

include("fabric", "neo")
