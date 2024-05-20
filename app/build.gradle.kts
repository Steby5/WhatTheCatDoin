plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlin.kapt")
	id("com.google.gms.google-services")
}

android {
	namespace = "si.uni_lj.fe.whatthecatdoin"  // Add this line
	compileSdk = 31

	defaultConfig {
		applicationId = "si.uni_lj.fe.whatthecatdoin"
		minSdk = 26
		targetSdk = 31
		versionCode = 1
		versionName = "1.0"

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
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
	implementation("androidx.core:core-ktx:1.7.0")
	implementation("androidx.appcompat:appcompat:1.3.1")
	implementation("com.google.android.material:material:1.4.0")
	implementation("androidx.constraintlayout:constraintlayout:2.1.1")
	implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
	implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

	// Import the BoM for Firebase
	implementation(platform("com.google.firebase:firebase-bom:28.4.1"))

	// Declare the dependencies without version numbers
	implementation("com.google.firebase:firebase-auth-ktx")
	implementation("com.google.firebase:firebase-firestore-ktx")
	implementation("com.google.firebase:firebase-storage-ktx")

	// Optional dependencies for image loading
	implementation("com.github.bumptech.glide:glide:4.12.0")
	kapt("com.github.bumptech.glide:compiler:4.12.0")

	testImplementation("junit:junit:4.13.2")
	androidTestImplementation("androidx.test.ext:junit:1.1.3")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
