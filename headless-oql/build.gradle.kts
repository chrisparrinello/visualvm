plugins {
    application
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("org.graalvm.visualvm.headless.oql.Main")
    applicationDefaultJvmArgs = listOf("-Djava.awt.headless=true")
}

dependencies {
    val useCentral = providers.gradleProperty("useMavenCentralLibs")
        .getOrElse("true")
        .toBoolean()

    if (useCentral) {
        implementation(libs.profiler.oql)
        implementation(libs.nashorn.core)
    } else {
        val suiteBuild = rootProject.layout.projectDirectory
            .dir("visualvm/libs.profiler")
        implementation(files(
            suiteBuild.file("lib.profiler.heap/build/cluster/modules/org-graalvm-visualvm-lib-jfluid-heap.jar"),
            suiteBuild.file("profiler.oql/build/cluster/modules/org-graalvm-visualvm-lib-profiler-oql.jar"),
            suiteBuild.file("profiler.api/build/cluster/modules/org-graalvm-visualvm-lib-profiler-api.jar"),
            suiteBuild.file("lib.profiler/build/cluster/modules/org-graalvm-visualvm-lib-jfluid.jar"),
            suiteBuild.file("lib.profiler.common/build/cluster/modules/org-graalvm-visualvm-lib-common.jar"),
        ))
        implementation(libs.nashorn.core)
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
}

tasks.test {
    useJUnitPlatform()
}
