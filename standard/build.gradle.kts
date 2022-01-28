plugins {
    id("hazzard.api")
}

description = "Standard implementation of core functionality for hazzard"

dependencies {
    api(project(":hazzard-core"))
    implementation(project(":hazzard-internal"))
}
