enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "hazzard"

hazzardProject("core")
hazzardProject("standard")
hazzardProject("internal")
hazzardProject("bom")

fun hazzardProject(path: String, name: String = "hazzard-$path"): ProjectDescriptor {
    include(path)
    val project = project(":$path")
    project.name = name
    return project
}
