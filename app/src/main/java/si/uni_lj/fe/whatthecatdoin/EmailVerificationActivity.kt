// EmailVerificationActivity.kt
package si.uni_lj.fe.whatthecatdoin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class EmailVerificationActivity : AppCompatActivity() {

	private lateinit var auth: FirebaseAuth
	private lateinit var user: FirebaseUser

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_email_verification)

		auth = FirebaseAuth.getInstance()
		user = auth.currentUser ?: run {
			startActivity(Intent(this, LoginActivity::class.java))
			finish()
			return
		}

		val emailTextView = findViewById<TextView>(R.id.emailTextView)
		val resendEmailButton = findViewById<Button>(R.id.resendEmailButton)
		val checkVerificationButton = findViewById<Button>(R.id.checkVerificationButton)

		emailTextView.text = user.email

		resendEmailButton.setOnClickListener {
			resendVerificationEmail()
		}

		checkVerificationButton.setOnClickListener {
			checkEmailVerification()
		}

		checkEmailVerification() // Check verification status on create
	}

	override fun onResume() {
		super.onResume()
		checkEmailVerification() // Check verification status on resume
	}

	private fun resendVerificationEmail() {
		user.sendEmailVerification()
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show()
				} else {
					Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
				}
			}
	}

	private fun checkEmailVerification() {
		user.reload().addOnCompleteListener { task ->
			if (task.isSuccessful) {
				if (user.isEmailVerified) {
					Toast.makeText(this, "Email verified successfully.", Toast.LENGTH_SHORT).show()
					startActivity(Intent(this, MainActivity::class.java))
					finish()
				} else {
					Toast.makeText(this, "Please verify your email address.", Toast.LENGTH_SHORT).show()
				}
			} else {
				Toast.makeText(this, "Failed to check email verification.", Toast.LENGTH_SHORT).show()
			}
		}
	}
}
