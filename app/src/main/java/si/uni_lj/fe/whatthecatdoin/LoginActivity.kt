package si.uni_lj.fe.whatthecatdoin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

	private lateinit var auth: FirebaseAuth

	private val REQUIRED_PERMISSIONS = arrayOf(
		Manifest.permission.CAMERA,
		Manifest.permission.READ_EXTERNAL_STORAGE,
		Manifest.permission.WRITE_EXTERNAL_STORAGE
	)
	private val REQUEST_CODE_PERMISSIONS = 10

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)

		auth = FirebaseAuth.getInstance()

		// Check if user is already logged in
		val currentUser = auth.currentUser
		if (currentUser != null) {
			startActivity(Intent(this, MainActivity::class.java))
			finish()
		}

		val emailEditText = findViewById<EditText>(R.id.emailEditText)
		val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
		val loginButton = findViewById<Button>(R.id.loginButton)
		val registerButton = findViewById<Button>(R.id.registerButton)

		if (allPermissionsGranted()) {
			setupLoginButton(emailEditText, passwordEditText, loginButton)
			setupRegisterButton(registerButton)
		} else {
			ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
		}
	}

	private fun setupLoginButton(emailEditText: EditText, passwordEditText: EditText, loginButton: Button) {
		loginButton.setOnClickListener {
			val email = emailEditText.text.toString()
			val password = passwordEditText.text.toString()

			if (email.isNotEmpty() && password.isNotEmpty()) {
				auth.signInWithEmailAndPassword(email, password)
					.addOnCompleteListener(this) { task ->
						if (task.isSuccessful) {
							startActivity(Intent(this, MainActivity::class.java))
							finish()
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

	private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
		ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == REQUEST_CODE_PERMISSIONS) {
			if (allPermissionsGranted()) {
				val emailEditText = findViewById<EditText>(R.id.emailEditText)
				val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
				val loginButton = findViewById<Button>(R.id.loginButton)
				val registerButton = findViewById<Button>(R.id.registerButton)
				setupLoginButton(emailEditText, passwordEditText, loginButton)
				setupRegisterButton(registerButton)
			} else {
				Toast.makeText(this, "Permissions not granted. Closing the app.", Toast.LENGTH_SHORT).show()
				finish()
			}
		}
	}
}
