plugins {
  id("com.android.application") version "8.5.0"
  kotlin("android") version "1.9.23"
  kotlin("plugin.serialization") version "1.9.23"
  kotlin("kapt") version "1.9.23"
}

android {
  namespace = "com.splitpaisa"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.splitpaisa"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "0.1"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.11"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {
  val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
  implementation(composeBom)
  androidTestImplementation(composeBom)

  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.activity:activity-compose:1.9.0")
  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  implementation("androidx.compose.material:material-icons-extended")

  implementation("androidx.navigation:navigation-compose:2.7.7")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

  val roomVersion = "2.6.1"
  implementation("androidx.room:room-runtime:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")
  kapt("androidx.room:room-compiler:$roomVersion")

  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

  testImplementation("junit:junit:4.13.2")
  testImplementation("androidx.test:core:1.5.0")
  testImplementation("androidx.room:room-testing:$roomVersion")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
