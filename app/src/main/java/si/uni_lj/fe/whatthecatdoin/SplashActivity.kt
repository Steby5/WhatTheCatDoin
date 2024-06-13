package si.uni_lj.fe.whatthecatdoin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {

	private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		arrayOf(
			Manifest.permission.CAMERA,
			Manifest.permission.READ_MEDIA_IMAGES
		)
	} else {
		arrayOf(
			Manifest.permission.CAMERA,
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
		)
	}
	private val REQUEST_CODE_PERMISSIONS = 10

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_splash)

		val imageView: ImageView = findViewById(R.id.splash_image)
		imageView.setImageResource(R.drawable.gato)
		val drawable = imageView.drawable
		if (drawable is Animatable) {
			drawable.start()
		}

		val versionTextView: TextView = findViewById(R.id.app_version)
		val versionName = BuildConfig.VERSION_NAME
		versionTextView.text = "Version: $versionName"

		if (allPermissionsGranted()) {
			proceedToLogin()
		} else {
			Log.d("SplashActivity", "Requesting permissions")
			ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
		}
	}

	private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
		ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
	}

	private fun proceedToLogin() {
		Log.d("SplashActivity", "Permissions granted, proceeding to login")
		Handler(Looper.getMainLooper()).postDelayed({
			startActivity(Intent(this, LoginActivity::class.java))
			finish()
		}, SPLASH_DISPLAY_LENGTH)
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == REQUEST_CODE_PERMISSIONS) {
			if (allPermissionsGranted()) {
				Log.d("SplashActivity", "Permissions granted after request")
				proceedToLogin()
			} else {
				Log.d("SplashActivity", "Permissions not granted. Closing the app.")
				Toast.makeText(this, "Permissions not granted. Closing the app.", Toast.LENGTH_SHORT).show()
				finish()
			}
		}
	}

	companion object {
		private const val SPLASH_DISPLAY_LENGTH = 2500L // Duration of the splash screen in milliseconds
	}
}
