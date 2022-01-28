plugins {
    id("hazzard.publishing")
    `java-platform`
}

description = "Bill of materials for hazzard"

indra {
    configurePublications {
        from(components["javaPlatform"])
    }
}

dependencies {
    constraints {
        sequenceOf(
            "core",
            "standard",
        ).forEach {
            api(project(":hazzard-$it"))
        }
    }
}
