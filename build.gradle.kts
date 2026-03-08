import java.io.File
import java.util.Properties

// SDK yolunu ayarla: önce local.properties'teki sdk.dir, geçerli değilse ANDROID_HOME kullan
val localPropertiesFile = rootProject.file("local.properties")
val sdkDirFromEnv = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
var sdkDirValid = false
if (localPropertiesFile.exists()) {
    val localProps = Properties().apply { localPropertiesFile.inputStream().use { load(it) } }
    val sdkDir = localProps.getProperty("sdk.dir")?.replace("\\\\", "\\")
    sdkDirValid = !sdkDir.isNullOrBlank() && File(sdkDir).isDirectory
}
if (!sdkDirValid && !sdkDirFromEnv.isNullOrBlank() && File(sdkDirFromEnv).isDirectory) {
    val escapedSdkDir = sdkDirFromEnv.replace("\\", "\\\\")
    val header = """
        ## This file must *NOT* be checked into Version Control Systems,
        # as it contains information specific to your local configuration.
        #
        # Location of the SDK. This is only used by Gradle.
        """.trimIndent()
    val newContent = if (localPropertiesFile.exists()) {
        localPropertiesFile.readText().replace(Regex("sdk\\.dir=.*"), "sdk.dir=$escapedSdkDir")
            .let { if (it.contains("sdk.dir=")) it else it.trimEnd() + "\nsdk.dir=$escapedSdkDir\n" }
    } else {
        "$header\nsdk.dir=$escapedSdkDir\n"
    }
    localPropertiesFile.writeText(newContent)
}

plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}

val sharedBuildRoot = File(System.getProperty("java.io.tmpdir"), "fitness-rep-counter-gradle")
layout.buildDirectory.set(sharedBuildRoot.resolve("root"))

subprojects {
    layout.buildDirectory.set(sharedBuildRoot.resolve(name))
}
