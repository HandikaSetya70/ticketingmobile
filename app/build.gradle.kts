plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.dicoding.ticketingsystem"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dicoding.ticketingsystem"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildTypes {
        debug {
            buildConfigField("String", "SUPABASE_URL", "\"https://wecwdyfrafbdowxadyyj.supabase.co\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"your-anon-key-here\"")
        }
        release {
            buildConfigField("String", "SUPABASE_URL", "\"https://wecwdyfrafbdowxadyyj.supabase.co\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"your-anon-key-here\"")

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.media3.common.ktx)
    dependencies {
        // Core Android dependencies
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)

        // ViewModel and LiveData
        implementation(libs.androidx.lifecycle.viewmodel.ktx)
        implementation(libs.androidx.lifecycle.livedata.ktx)

        // DataStore
        implementation(libs.androidx.datastore.preferences)

        // Coroutines
        implementation(libs.kotlinx.coroutines.android)

        // Activity KTX for viewModels()
        implementation(libs.androidx.activity.ktx)

        // Testing
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)

        // Serialization
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
        implementation("io.ktor:ktor-client-serialization:2.3.5")

        // Hilt for Dependency Injection
        implementation("com.google.dagger:hilt-android:2.44")
        kapt("com.google.dagger:hilt-compiler:2.44")

        // Retrofit for API calls
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")

        // OkHttp for networking
        implementation("com.squareup.okhttp3:okhttp:4.11.0")
        implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

        // Gson for JSON parsing
        implementation("com.google.code.gson:gson:2.10.1")

        // Glide for image loading
        implementation("com.github.bumptech.glide:glide:4.16.0")
        kapt("com.github.bumptech.glide:compiler:4.16.0")

        // Glide transformations (optional, for image transformations like rounded corners)
        implementation("jp.wasabeef:glide-transformations:4.3.0")
    }
}