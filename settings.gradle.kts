// settings.gradle.kts

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

// --- Robust Android SDK detection & toggles ---

fun envDir(name: String): java.io.File? =
    System.getenv(name)?.let { java.io.File(it) }?.takeIf { it.isDirectory }

val isCI = System.getenv("CI")?.isNotBlank() == true
val forceInclude = System.getenv("FORCE_INCLUDE_ANDROID") == "true"
val skipAndroid = System.getenv("SKIP_ANDROID") == "true"

when {
    skipAndroid -> {
        println("SKIP_ANDROID=true → Skipping ':app' for this environment.")
    }
    forceInclude || isCI -> {
        include(":app")
        println("Including ':app' (forceInclude=$forceInclude, isCI=$isCI)")
    }
    else -> {
        println("Android SDK not found; skipping ':app' (Codex env).")
    }
}
