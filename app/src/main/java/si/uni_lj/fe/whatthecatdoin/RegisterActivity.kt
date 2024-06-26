package si.uni_lj.fe.whatthecatdoin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

	private lateinit var auth: FirebaseAuth
	private lateinit var db: FirebaseFirestore

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_register)

		auth = FirebaseAuth.getInstance()
		db = FirebaseFirestore.getInstance()

		val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
		val emailEditText = findViewById<EditText>(R.id.emailEditText)
		val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
		val repeatPasswordEditText = findViewById<EditText>(R.id.repeatPasswordEditText)
		val createAccountButton = findViewById<Button>(R.id.createAccountButton)
		val loginButton = findViewById<Button>(R.id.loginButton)

		createAccountButton.setOnClickListener {
			val username = usernameEditText.text.toString().trim()
			val email = emailEditText.text.toString().trim()
			val password = passwordEditText.text.toString().trim()
			val repeatPassword = repeatPasswordEditText.text.toString().trim()

			if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()) {
				if (password == repeatPassword) {
					registerUser(username, email, password)
				} else {
					Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
				}
			} else {
				Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
			}
		}

		loginButton.setOnClickListener {
			startActivity(Intent(this, LoginActivity::class.java))
			finish()
		}
	}

	private fun registerUser(username: String, email: String, password: String) {
		auth.createUserWithEmailAndPassword(email, password)
			.addOnCompleteListener(this) { task ->
				if (task.isSuccessful) {
					val user = auth.currentUser
					user?.let {
						val profileUpdates = UserProfileChangeRequest.Builder()
							.setDisplayName(username)
							.build()
						it.updateProfile(profileUpdates)
							.addOnCompleteListener { task2 ->
								if (task2.isSuccessful) {
									sendEmailVerification(it)
								} else {
									Toast.makeText(
										this,
										"Failed to update profile.",
										Toast.LENGTH_SHORT
									).show()
								}
							}
					}
				} else {
					val errorMessage = when (task.exception) {
						is FirebaseAuthWeakPasswordException -> "Password is too weak."
						is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
						is FirebaseAuthUserCollisionException -> "Email is already in use."
						else -> "Registration failed: ${task.exception?.message}"
					}
					Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
				}
			}
	}

	private fun sendEmailVerification(user: FirebaseUser) {
		user.sendEmailVerification()
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show()
					saveUserData(user)
				} else {
					Toast.makeText(
						this,
						"Failed to send verification email.",
						Toast.LENGTH_SHORT
					).show()
				}
			}
	}

	private fun saveUserData(user: FirebaseUser) {
		val userId = user.uid
		val userData = hashMapOf(
			"username" to user.displayName,
			"email" to user.email
		)

		db.collection("users").document(userId).set(userData)
			.addOnSuccessListener {
				startActivity(Intent(this, EmailVerificationActivity::class.java))
				finish()
			}
			.addOnFailureListener { e ->
				Toast.makeText(
					this,
					"Failed to save user data: ${e.message}",
					Toast.LENGTH_SHORT
				).show()
			}
	}
}
