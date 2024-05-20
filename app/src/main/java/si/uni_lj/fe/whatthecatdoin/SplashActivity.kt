package si.uni_lj.fe.whatthecatdoin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget

class SplashActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_splash)

		val gifView = findViewById<ImageView>(R.id.gifView)
		Glide.with(this).load(R.drawable.gato).into(DrawableImageViewTarget(gifView))

		Handler().postDelayed({
			val mainIntent = Intent(
				this@SplashActivity,
				MainActivity::class.java
			)
			this@SplashActivity.startActivity(mainIntent)
			this@SplashActivity.finish()
		}, SPLASH_DISPLAY_LENGTH.toLong())
	}

	companion object {
		private const val SPLASH_DISPLAY_LENGTH = 3600 // Duration in milliseconds (1.8 seconds)
	}
}