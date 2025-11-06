architectury {
    fabric()
}

configurations.getByName("developmentFabric") {
    extendsFrom(configurations.getByName("common"))
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject.ext["fabric_loader_version"]}")
    add("common", project(path=":", configuration="namedElements")) {
        isTransitive = false
    }
    add("shadowCommon", project(path=":", configuration="transformProductionFabric")) {
        isTransitive = false
    }
}