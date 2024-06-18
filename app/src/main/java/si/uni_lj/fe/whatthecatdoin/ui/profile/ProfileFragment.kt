package si.uni_lj.fe.whatthecatdoin.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R
import si.uni_lj.fe.whatthecatdoin.RegisterActivity
import si.uni_lj.fe.whatthecatdoin.ui.archive.ArchiveAdapter
import si.uni_lj.fe.whatthecatdoin.ui.archive.PostDetailFragment
import java.io.File

class ProfileFragment : Fragment(), PostDetailFragment.PostDeleteListener {

	private lateinit var recyclerView: RecyclerView
	private lateinit var adapter: ArchiveAdapter
	private lateinit var postList: MutableList<Post>
	private lateinit var db: FirebaseFirestore
	private lateinit var auth: FirebaseAuth
	private lateinit var usernameTextView: TextView
	private lateinit var logoutButton: TextView
	private lateinit var profileImageView: ImageView
	private lateinit var storage: FirebaseStorage
	private lateinit var scrollToTopButton: FloatingActionButton
	private val PICK_IMAGE_REQUEST = 1

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_profile, container, false)

		db = FirebaseFirestore.getInstance()
		auth = FirebaseAuth.getInstance()
		storage = FirebaseStorage.getInstance()

		usernameTextView = view.findViewById(R.id.usernameTextView)
		logoutButton = view.findViewById(R.id.logoutButton)
		profileImageView = view.findViewById(R.id.profileImageView)
		recyclerView = view.findViewById(R.id.archiveRecyclerView)
		recyclerView.layoutManager = GridLayoutManager(context, 2)
		postList = mutableListOf()
		adapter = ArchiveAdapter { post -> showPostDetail(post) }
		recyclerView.adapter = adapter
		scrollToTopButton = view.findViewById(R.id.scrollToTopButton)

		loadPosts()
		loadProfileImage()

		auth.currentUser?.let { user ->
			usernameTextView.text = user.displayName
		}

		usernameTextView.setOnClickListener { showPopupMenu() }
		logoutButton.setOnClickListener { logout() }
		profileImageView.setOnClickListener { openImageSelector() }
		scrollToTopButton.setOnClickListener {
			recyclerView.smoothScrollToPosition(0)
		}

		recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				if (dy > 0) {
					scrollToTopButton.show()
				} else {
					scrollToTopButton.hide()
				}
			}
		})

		return view
	}

	private fun loadPosts() {
		val userId = auth.currentUser?.uid
		if (userId != null) {
			db.collection("posts")
				.whereEqualTo("userId", userId)
				.get()
				.addOnSuccessListener { result ->
					postList.clear()
					for (document in result) {
						val post = document.toObject(Post::class.java).apply {
							id = document.id
						}
						postList.add(post)
					}
					postList.sortByDescending { it.timestamp }
					adapter.submitList(postList)
				}
		}
	}

	private fun loadProfileImage() {
		val userId = auth.currentUser?.uid
		if (userId != null) {
			db.collection("users").document(userId).get()
				.addOnSuccessListener { document ->
					if (document.exists()) {
						val profileImageUrl = document.getString("profileImageUrl")
						profileImageUrl?.let {
							Glide.with(this).load(it).into(profileImageView)
						}
					}
				}
		}
	}

	private fun openImageSelector() {
		val intent = Intent(Intent.ACTION_GET_CONTENT)
		intent.type = "image/*"
		startActivityForResult(intent, PICK_IMAGE_REQUEST)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
			val imageUri = data?.data
			imageUri?.let { startCrop(it) }
		} else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
			val resultUri = UCrop.getOutput(data!!)
			resultUri?.let { uploadImageToFirebase(it) }
		}
	}

	private fun startCrop(uri: Uri) {
		val destinationUri = Uri.fromFile(File(context?.cacheDir, "profile_image_crop.jpg"))
		val options = UCrop.Options()
		options.setCircleDimmedLayer(true)
		options.setShowCropGrid(false)
		options.setShowCropFrame(false)
		UCrop.of(uri, destinationUri)
			.withAspectRatio(1f, 1f)
			.withMaxResultSize(500, 500)
			.withOptions(options)
			.start(requireContext(), this)
	}

	private fun uploadImageToFirebase(uri: Uri) {
		val userId = auth.currentUser?.uid
		val storageRef = storage.reference.child("profile_images/$userId.jpg")
		storageRef.putFile(uri)
			.addOnSuccessListener {
				storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
					saveProfileImageUrl(downloadUri.toString())
				}
			}
			.addOnFailureListener { e ->
				Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
			}
	}

	private fun saveProfileImageUrl(url: String) {
		val userId = auth.currentUser?.uid
		if (userId != null) {
			db.collection("users").document(userId)
				.update("profileImageUrl", url)
				.addOnSuccessListener {
					Toast.makeText(context, "Profile image updated", Toast.LENGTH_SHORT).show()
					loadProfileImage()
					updateProfileImageInPosts(url)
				}
				.addOnFailureListener { e ->
					Toast.makeText(context, "Failed to update profile image URL: ${e.message}", Toast.LENGTH_SHORT).show()
				}
		}
	}

	private fun updateProfileImageInPosts(url: String) {
		val userId = auth.currentUser?.uid
		if (userId != null) {
			db.collection("posts").whereEqualTo("userId", userId).get()
				.addOnSuccessListener { result ->
					for (document in result) {
						db.collection("posts").document(document.id)
							.update("profileImageUrl", url)
					}
				}
		}
	}

	private fun showPostDetail(post: Post) {
		val dialogFragment = PostDetailFragment.newInstance(post)
		dialogFragment.setTargetFragment(this, 0)
		dialogFragment.show(parentFragmentManager, "post_detail")
	}

	private fun showPopupMenu() {
		val popupMenu = PopupMenu(context as Context, usernameTextView)
		popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)
		popupMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.changeUsername -> changeUsername()
				R.id.changeEmail -> changeEmail()
				R.id.changePassword -> changePassword()
				R.id.deleteAccount -> showDeleteAccountConfirmationDialog()
				R.id.banUser -> banUser()
			}
			true
		}
		popupMenu.show()

		// Check if the user is an admin
		auth.currentUser?.getIdToken(false)?.addOnSuccessListener { result ->
			val isAdmin = result.claims["admin"] as? Boolean ?: false
			if (isAdmin) {
				popupMenu.menu.findItem(R.id.banUser).isVisible = true
			}
		}
	}

	private fun changeUsername() {
		val builder = AlertDialog.Builder(context)
		val inflater = layoutInflater
		val dialogLayout = inflater.inflate(R.layout.dialog_change_username, null)
		val editText = dialogLayout.findViewById<EditText>(R.id.editTextUsername)
		builder.setView(dialogLayout)
		builder.setTitle("Change Username")
		builder.setPositiveButton("Change") { _, _ ->
			val newUsername = editText.text.toString().trim()
			if (newUsername.isNotEmpty()) {
				checkUsernameAvailability(newUsername)
			} else {
				Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
			}
		}
		builder.setNegativeButton("Cancel", null)
		builder.show()
	}

	private fun checkUsernameAvailability(newUsername: String) {
		val currentUserId = auth.currentUser?.uid ?: return
		db.collection("users").whereEqualTo("username", newUsername).get()
			.addOnSuccessListener { documents ->
				if (documents.isEmpty) {
					checkUsernameChangeEligibility(currentUserId, newUsername)
				} else {
					Toast.makeText(context, "Username already exists", Toast.LENGTH_SHORT).show()
				}
			}
			.addOnFailureListener { e ->
				Toast.makeText(context, "Failed to check username availability: ${e.message}", Toast.LENGTH_SHORT).show()
			}
	}

	private fun checkUsernameChangeEligibility(currentUserId: String, newUsername: String) {
		db.collection("users").document(currentUserId).get()
			.addOnSuccessListener { document ->
				val lastChanged = document.getLong("lastUsernameChange")
				val currentTime = System.currentTimeMillis()
				if (lastChanged == null || currentTime - lastChanged > 30L * 24 * 60 * 60 * 1000) { // 30 days
					showUsernameChangeConfirmationDialog(currentUserId, newUsername, currentTime)
				} else {
					Toast.makeText(context, "You can only change your username once every 30 days", Toast.LENGTH_SHORT).show()
				}
			}
			.addOnFailureListener { e ->
				Toast.makeText(context, "Failed to check username change eligibility: ${e.message}", Toast.LENGTH_SHORT).show()
			}
	}

	private fun showUsernameChangeConfirmationDialog(currentUserId: String, newUsername: String, currentTime: Long) {
		AlertDialog.Builder(context)
			.setMessage("Are you sure you want to change your username to $newUsername?\nYou can only change your username once every 30 days.")
			.setPositiveButton("Yes") { _, _ -> changeUsernameInDatabase(currentUserId, newUsername, currentTime) }
			.setNegativeButton("No", null)
			.show()
	}

	private fun changeUsernameInDatabase(currentUserId: String, newUsername: String, currentTime: Long) {
		db.collection("users").document(currentUserId).update(
			"username", newUsername,
			"lastUsernameChange", currentTime
		)
			.addOnSuccessListener {
				updateUsernameInPosts(currentUserId, newUsername)
				updateProfileUsername(newUsername)
			}
			.addOnFailureListener { e ->
				Toast.makeText(context, "Failed to change username: ${e.message}", Toast.LENGTH_SHORT).show()
			}
	}

	private fun updateUsernameInPosts(currentUserId: String, newUsername: String) {
		db.collection("posts").whereEqualTo("userId", currentUserId).get()
			.addOnSuccessListener { result ->
				for (document in result) {
					db.collection("posts").document(document.id)
						.update("profileName", newUsername)
				}
				Toast.makeText(context, "Username changed successfully", Toast.LENGTH_SHORT).show()
			}
			.addOnFailureListener { e ->
				Toast.makeText(context, "Failed to update username in posts: ${e.message}", Toast.LENGTH_SHORT).show()
			}
	}

	private fun updateProfileUsername(newUsername: String) {
		auth.currentUser?.let { user ->
			val profileUpdates = UserProfileChangeRequest.Builder()
				.setDisplayName(newUsername)
				.build()
			user.updateProfile(profileUpdates)
				.addOnCompleteListener { task ->
					if (task.isSuccessful) {
						usernameTextView.text = newUsername
					} else {
						Toast.makeText(context, "Failed to update username in profile", Toast.LENGTH_SHORT).show()
					}
				}
		}
	}


	private fun changeEmail() {
		// Implement change email functionality
	}

	private fun changePassword() {
		AlertDialog.Builder(context)
			.setMessage("Are you sure you want to reset your password? Link to reset password will be sent to your email (" + auth.currentUser?.email + ").")
			.setPositiveButton("Yes") { _, _ -> sendPasswordResetEmail() }
			.setNegativeButton("No", null)
			.show()
	}

	private fun sendPasswordResetEmail() {
		val user = auth.currentUser
		user?.let {
			auth.sendPasswordResetEmail(it.email!!)
				.addOnCompleteListener { task ->
					if (task.isSuccessful) {
						Toast.makeText(context, "Password reset email sent.", Toast.LENGTH_SHORT).show()
						logout() // Log out the user after sending the reset email
					} else {
						Toast.makeText(context, "Failed to send password reset email.", Toast.LENGTH_SHORT).show()
					}
				}
		}
	}

	private fun showDeleteAccountConfirmationDialog() {
		AlertDialog.Builder(context)
			.setMessage("Are you sure you want to delete your account? \n\nTHIS CANNOT BE UNDONE!")
			.setPositiveButton("Delete") { _, _ -> deleteUser(auth.currentUser?.uid) }
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun deleteUser(userId: String?) {
		if (userId == null) return
		db.collection("users").document(userId).delete()
			.addOnSuccessListener {
				db.collection("posts").whereEqualTo("userId", userId).get()
					.addOnSuccessListener { result ->
						for (document in result) {
							db.collection("posts").document(document.id).delete()
						}
					}
			}
			.addOnFailureListener { e ->
				Toast.makeText(context, "Failed to delete user: ${e.message}", Toast.LENGTH_SHORT).show()
			}
	}

	private fun banUser() {
		val userId = auth.currentUser?.uid ?: return
		db.collection("users").get()
			.addOnSuccessListener { result ->
				val users = result.documents.filter { it.id != userId }
				val userNames = users.map { it.getString("username") ?: "Unknown" }.toTypedArray()
				val userIds = users.map { it.id }.toTypedArray()

				AlertDialog.Builder(context)
					.setTitle("Select a user to ban")
					.setItems(userNames) { _, which ->
						val selectedUserId = userIds[which]
						showBanConfirmationDialog(selectedUserId, userNames[which])
					}
					.setNegativeButton("Cancel", null)
					.show()
			}
	}

	private fun showBanConfirmationDialog(userId: String, username: String = "") {
		AlertDialog.Builder(context)
			.setMessage("Are you sure you want to ban this user (" + username + ")?")
			.setPositiveButton("Ban") { _, _ -> deleteUser(userId) }
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun logout() {
		auth.signOut()
		startActivity(Intent(context, RegisterActivity::class.java))
		activity?.finish()
	}

	override fun onPostDeleted() {
		loadPosts()
	}
}
