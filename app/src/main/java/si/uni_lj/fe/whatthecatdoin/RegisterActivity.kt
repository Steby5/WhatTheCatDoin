package si.uni_lj.fe.whatthecatdoin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

	private lateinit var auth: FirebaseAuth

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_register)

		auth = FirebaseAuth.getInstance()

		val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
		val emailEditText = findViewById<EditText>(R.id.emailEditText)
		val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
		val repeatPasswordEditText = findViewById<EditText>(R.id.repeatPasswordEditText)
		val createAccountButton = findViewById<Button>(R.id.createAccountButton)

		createAccountButton.setOnClickListener {
			val username = usernameEditText.text.toString()
			val email = emailEditText.text.toString()
			val password = passwordEditText.text.toString()
			val repeatPassword = repeatPasswordEditText.text.toString()

			if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()) {
				if (password == repeatPassword) {
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
												startActivity(Intent(this, MainActivity::class.java))
												finish()
											}
										}
								}
							} else {
								Toast.makeText(this, "Registration Failed.", Toast.LENGTH_SHORT).show()
							}
						}
				} else {
					Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
				}
			} else {
				Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
			}
		}
	}
}
