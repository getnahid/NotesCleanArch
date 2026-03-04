
plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.kapt")
  id("org.jetbrains.kotlin.plugin.serialization")
  id("com.google.dagger.hilt.android")
  id("com.google.devtools.ksp")
}

android {
  namespace = "com.example.notes"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.example.notes"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"
  }

  buildFeatures {
    viewBinding = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.appcompat:appcompat:1.7.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
  implementation("androidx.recyclerview:recyclerview:1.3.2")
  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
  implementation("androidx.activity:activity-ktx:1.9.3")
  implementation("androidx.fragment:fragment-ktx:1.8.5")

  // Coroutines / Flow are part of Kotlin stdlib + kotlinx-coroutines
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

  // Hilt
  implementation("com.google.dagger:hilt-android:2.52")
  kapt("com.google.dagger:hilt-compiler:2.52")

  // Room (KSP)
  implementation("androidx.room:room-runtime:2.6.1")
  implementation("androidx.room:room-ktx:2.6.1")
  ksp("androidx.room:room-compiler:2.6.1")

  // Retrofit + OkHttp + Kotlinx Serialization
  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}