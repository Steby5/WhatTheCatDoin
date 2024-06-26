package si.uni_lj.fe.whatthecatdoin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

	private lateinit var auth: FirebaseAuth

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)

		auth = FirebaseAuth.getInstance()

		// Check if user is already logged in
		val currentUser = auth.currentUser
		if (currentUser != null && currentUser.isEmailVerified) {
			startActivity(Intent(this, MainActivity::class.java))
			finish()
		}

		val emailEditText = findViewById<EditText>(R.id.emailEditText)
		val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
		val loginButton = findViewById<Button>(R.id.loginButton)
		val registerButton = findViewById<Button>(R.id.registerButton)
		val resendVerificationButton = findViewById<Button>(R.id.resendVerificationButton)

		setupLoginButton(emailEditText, passwordEditText, loginButton)
		setupRegisterButton(registerButton)
		setupResendVerificationButton(resendVerificationButton, emailEditText, passwordEditText)
	}

	private fun setupLoginButton(emailEditText: EditText, passwordEditText: EditText, loginButton: Button) {
		loginButton.setOnClickListener {
			val email = emailEditText.text.toString().trim()
			val password = passwordEditText.text.toString().trim()

			if (email.isNotEmpty() && password.isNotEmpty()) {
				auth.signInWithEmailAndPassword(email, password)
					.addOnCompleteListener(this) { task ->
						if (task.isSuccessful) {
							val user = auth.currentUser
							if (user?.isEmailVerified == true) {
								startActivity(Intent(this, MainActivity::class.java))
								finish()
							} else {
								findViewById<Button>(R.id.resendVerificationButton).visibility = View.VISIBLE
								Toast.makeText(this, "Please verify your email address.", Toast.LENGTH_SHORT).show()
								auth.signOut() // Sign out the user
							}
						} else {
							Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
						}
					}
			} else {
				Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun setupRegisterButton(registerButton: Button) {
		registerButton.setOnClickListener {
			startActivity(Intent(this, RegisterActivity::class.java))
		}
	}

	private fun setupResendVerificationButton(resendVerificationButton: Button, emailEditText: EditText, passwordEditText: EditText) {
		resendVerificationButton.setOnClickListener {
			val email = emailEditText.text.toString().trim()
			val password = passwordEditText.text.toString().trim()

			if (email.isNotEmpty() && password.isNotEmpty()) {
				auth.signInWithEmailAndPassword(email, password)
					.addOnCompleteListener(this) { task ->
						if (task.isSuccessful) {
							val user = auth.currentUser
							user?.let {
								it.sendEmailVerification()
									.addOnCompleteListener { sendTask ->
										if (sendTask.isSuccessful) {
											Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show()
										} else {
											Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
										}
									}
							}
						} else {
							Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
						}
					}
			} else {
				Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
			}
		}
	}
}
