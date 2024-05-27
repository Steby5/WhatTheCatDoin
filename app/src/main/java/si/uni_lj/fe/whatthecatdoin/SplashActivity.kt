package si.uni_lj.fe.whatthecatdoin

import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

	private lateinit var db: FirebaseFirestore
	private val handler = Handler(Looper.getMainLooper())

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

		db = FirebaseFirestore.getInstance()

		// Start time for splash screen display
		val startTime = System.currentTimeMillis()

		lifecycleScope.launch {
			val postList = loadPosts()

			// Calculate the remaining time to display splash screen
			val elapsedTime = System.currentTimeMillis() - startTime
			val remainingTime = SPLASH_DISPLAY_LENGTH - elapsedTime

			// Ensure the splash screen is displayed for at least SPLASH_DISPLAY_LENGTH
			if (remainingTime > 0) {
				handler.postDelayed({
					startNextActivity(postList)
				}, remainingTime)
			} else {
				startNextActivity(postList)
			}
		}
	}

	private suspend fun loadPosts(): List<Post> {
		return withContext(Dispatchers.IO) {
			val postList = mutableListOf<Post>()
			val result = db.collection("posts").get().await()
			for (document in result) {
				val post = document.toObject(Post::class.java).copy(id = document.id)
				postList.add(post)
			}
			postList
		}
	}

	private fun startNextActivity(postList: List<Post>) {
		val intent = Intent(this, LoginActivity::class.java).apply {
			putParcelableArrayListExtra("postList", ArrayList(postList))
		}
		startActivity(intent)
		finish()
	}

	companion object {
		private const val SPLASH_DISPLAY_LENGTH = 1800L // Duration of the splash screen in milliseconds
	}
}
