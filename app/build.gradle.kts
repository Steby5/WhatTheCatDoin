plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlin.kapt")
	id("com.google.gms.google-services")
	id("com.google.devtools.ksp")

}

android {
	namespace = "si.uni_lj.fe.whatthecatdoin"  // Add this line
	compileSdk = 34

	defaultConfig {
		applicationId = "si.uni_lj.fe.whatthecatdoin"
		minSdk = 26
		targetSdk = 33
		versionCode = 2
		versionName = "2.4"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}

	kotlinOptions {
		jvmTarget = "1.8"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	viewBinding {
		enable = true
	}
}

dependencies {
	implementation(libs.kotlin.stdlib)
	implementation(libs.androidx.core.ktx.v170)
	implementation(libs.androidx.appcompat.v140)
	implementation(libs.material.v140)
	implementation(libs.androidx.constraintlayout.v212)
	implementation(libs.androidx.navigation.fragment.ktx.v235)
	implementation(libs.androidx.navigation.ui.ktx.v235)
	implementation(libs.firebase.auth.ktx)
	implementation(libs.firebase.firestore.ktx)
	implementation(libs.firebase.storage.ktx)
	implementation(libs.glide)
	implementation(libs.androidx.recyclerview)
	implementation(libs.androidx.swiperefreshlayout)
	kapt(libs.compiler)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit.v113)
	androidTestImplementation(libs.androidx.espresso.core.v340)

	implementation("com.github.yalantis:ucrop:2.2.9")

}
