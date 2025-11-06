architectury {
    forge()
}

configurations.getByName("developmentForge") {
    extendsFrom(configurations.getByName("common"))
}

dependencies {
    forge("net.minecraftforge:forge:${rootProject.ext["forge_version"]}")

    add("common", project(path=":", configuration="namedElements")) {
        isTransitive = false
    }
    add("shadowCommon", project(path=":", configuration="transformProductionForge")) {
        isTransitive = false
    }
}

loom.forge {
    mixinConfig("mob_effect_display_fix.mixins.json")
}
