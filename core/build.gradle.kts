plugins {
    id("hazzard.api")
}

description = "Core functionality of hazzard"

dependencies {
    api(libs.geantyref)
    implementation(project(":hazzard-internal"))
    testImplementation(project(":hazzard-standard"))
    testImplementation(libs.examination.api)
    testImplementation(libs.examination.string)
}
