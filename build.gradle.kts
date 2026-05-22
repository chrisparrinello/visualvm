import java.net.URI
import java.util.zip.ZipFile

plugins {
    base
}

val nbPlatformDir = layout.projectDirectory.dir(
    providers.gradleProperty("nbPlatformDir").getOrElse("visualvm/visualvm/netbeans")
)
val nbPlatformZipUrl = providers.gradleProperty("nbPlatformZipUrl").get()

tasks.register("downloadNetbeansPlatform") {
    group = "visualvm"
    description = "Download and extract the NetBeans Platform 22 zip for the legacy Ant VisualVM build."

    val markerFile = nbPlatformDir.file("platform").asFile
    val downloadDir = layout.buildDirectory.dir("platform-download")
    val zipFile = downloadDir.map { it.file("nb-platform.zip") }

    outputs.dir(nbPlatformDir)
    onlyIf { !markerFile.exists() }

    doLast {
        val dest = nbPlatformDir.asFile
        val zip = zipFile.get().asFile
        zip.parentFile.mkdirs()

        logger.lifecycle("Downloading NetBeans platform from {}", nbPlatformZipUrl)
        URI(nbPlatformZipUrl).toURL().openStream().use { input ->
            zip.outputStream().use { output -> input.copyTo(output) }
        }

        val extractRoot = downloadDir.get().asFile.resolve("extract")
        extractRoot.mkdirs()
        ZipFile(zip).use { zf ->
            zf.entries().asSequence().forEach { entry ->
                val out = extractRoot.resolve(entry.name)
                if (entry.isDirectory) {
                    out.mkdirs()
                } else {
                    out.parentFile.mkdirs()
                    zf.getInputStream(entry).use { input ->
                        out.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
        }

        val platformSource = findPlatformRoot(extractRoot)
            ?: error("Downloaded zip does not contain a NetBeans platform (harness/ and platform/) under $extractRoot")

        dest.parentFile.mkdirs()
        if (dest.exists()) {
            dest.deleteRecursively()
        }
        platformSource.copyRecursively(dest, overwrite = true)
        logger.lifecycle("NetBeans platform installed at {}", dest.absolutePath)
    }
}

tasks.register<Exec>("buildVisualvm") {
    group = "visualvm"
    description = "Build VisualVM distribution zip using Ant (requires NetBeans platform)."
    dependsOn("downloadNetbeansPlatform")
    workingDir = layout.projectDirectory.dir("visualvm/visualvm").asFile
    commandLine("ant", "build-zip")
}

fun findPlatformRoot(dir: File): File? {
    if (dir.resolve("harness").isDirectory && dir.resolve("platform").isDirectory) {
        return dir
    }
    dir.listFiles()?.forEach { child ->
        if (child.isDirectory) {
            findPlatformRoot(child)?.let { return it }
        }
    }
    return null
}

tasks.register<Exec>("runVisualvm") {
    group = "visualvm"
    description = "Run VisualVM GUI using Ant (requires NetBeans platform)."
    dependsOn("downloadNetbeansPlatform")
    workingDir = layout.projectDirectory.dir("visualvm/visualvm").asFile
    commandLine("ant", "run")
}
