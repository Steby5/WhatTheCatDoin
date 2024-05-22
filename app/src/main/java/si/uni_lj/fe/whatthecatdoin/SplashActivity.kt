package si.uni_lj.fe.whatthecatdoin

import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView

class SplashActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_splash)

		val imageView: ImageView = findViewById(R.id.splash_image)
		imageView.setImageResource(R.drawable.gato)
		val drawable = imageView.drawable
		if (drawable is Animatable) {
			drawable.start()
		}

		Handler(Looper.getMainLooper()).postDelayed({
			startActivity(Intent(this, LoginActivity::class.java))
			finish()
		}, SPLASH_DISPLAY_LENGTH)
	}

	companion object {
		private const val SPLASH_DISPLAY_LENGTH = 1800L // Duration of the splash screen in milliseconds
	}
}
