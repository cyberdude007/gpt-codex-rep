pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PaisaSplit"

// Include :app only if we're on CI or an Android SDK is present locally.
// This keeps the Codex terminal from failing on startup when no SDK exists.
fun hasAndroidSdk(): Boolean =
    System.getenv("ANDROID_HOME") != null ||
    System.getenv("ANDROID_SDK_ROOT") != null ||
    file("local.properties").exists()

val isCI = System.getenv("CI")?.isNotBlank() == true

if (isCI || hasAndroidSdk()) {
    include(":app")
} else {
    println("Android SDK not found; skipping :app (Codex env).")
}
