import java.io.File

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
