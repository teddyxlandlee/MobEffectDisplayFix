architectury {
    neoForge()
}

configurations.getByName("developmentNeoForge") {
    extendsFrom(configurations.getByName("common"))
}

dependencies {
    neoForge("net.neoforged:neoforge:${rootProject.ext["neo_version"]}")

    add("common", project(path=":", configuration="namedElements")) {
        isTransitive = false
    }
    add("shadowCommon", project(path=":", configuration="transformProductionNeoForge")) {
        isTransitive = false
    }
}

tasks.withType(Jar::class).configureEach {
    manifest {
        // For Forge
        attributes("MixinConfigs" to "mob_effect_display_fix.mixins.json")
    }
}
