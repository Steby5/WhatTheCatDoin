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
import android.widget.ImageButton
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import si.uni_lj.fe.whatthecatdoin.LoginActivity
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R
import si.uni_lj.fe.whatthecatdoin.RegisterActivity
import si.uni_lj.fe.whatthecatdoin.ui.archive.ArchiveAdapter
import si.uni_lj.fe.whatthecatdoin.ui.archive.PostDetailFragment
import java.io.File
import java.io.Serializable

class ProfileFragment : Fragment(), PostDetailFragment.PostDeleteListener {

	private lateinit var recyclerView: RecyclerView
	private lateinit var adapter: ArchiveAdapter
	private lateinit var postList: MutableList<Post>
	private lateinit var db: FirebaseFirestore
	private lateinit var auth: FirebaseAuth
	private lateinit var usernameTextView: TextView
	private lateinit var settingsButton: ImageButton
	private lateinit var followersCountTextView: TextView
	private lateinit var followingCountTextView: TextView
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
		settingsButton = view.findViewById(R.id.settingsButton)
		followersCountTextView = view.findViewById(R.id.followersCountTextView)
		followingCountTextView = view.findViewById(R.id.followingCountTextView)
		profileImageView = view.findViewById(R.id.profileImageView)
		recyclerView = view.findViewById(R.id.archiveRecyclerView)
		recyclerView.layoutManager = GridLayoutManager(context, 2)
		postList = mutableListOf()
		adapter = ArchiveAdapter { post -> showPostDetail(post) }
		recyclerView.adapter = adapter
		scrollToTopButton = view.findViewById(R.id.scrollToTopButton)

		loadPosts()
		loadProfileImage()
		loadFollowersAndFollowing()

		auth.currentUser?.let { user ->
			usernameTextView.text = user.displayName
		}

		settingsButton.setOnClickListener { showSettingsMenu() }
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

		followersCountTextView.setOnClickListener { showFollowers() }
		followingCountTextView.setOnClickListener { showFollowing() }

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

	private fun loadFollowersAndFollowing() {
		val userId = auth.currentUser?.uid
		if (userId != null) {
			db.collection("users").document(userId).collection("followers").get()
				.addOnSuccessListener { result ->
					followersCountTextView.text = "${result.size()} Followers"
				}

			db.collection("users").document(userId).collection("following").get()
				.addOnSuccessListener { result ->
					followingCountTextView.text = "${result.size()} Following"
				}
		}
	}

	private fun showFollowers() {
		val userId = auth.currentUser?.uid
		if (userId != null) {
			db.collection("users").document(userId).collection("followers").get()
				.addOnSuccessListener { result ->
					val followerIds = result.documents.map { it.id }
					fetchUsernames(followerIds) { followerNames ->
						showUserListDialog("Followers", followerNames)
					}
				}
		}
	}

	private fun showFollowing() {
		val userId = auth.currentUser?.uid
		if (userId != null) {
			db.collection("users").document(userId).collection("following").get()
				.addOnSuccessListener { result ->
					val followingIds = result.documents.map { it.id }
					fetchUsernames(followingIds) { followingNames ->
						showUserListDialog("Following", followingNames)
					}
				}
		}
	}

	private fun fetchUsernames(userIds: List<String>, callback: (List<Pair<String, String>>) -> Unit) {
		val usernames = mutableListOf<Pair<String, String>>()
		val db = FirebaseFirestore.getInstance()

		for (userId in userIds) {
			db.collection("users").document(userId).get()
				.addOnSuccessListener { document ->
					val username = document.getString("username") ?: "Unknown"
					usernames.add(userId to username)
					if (usernames.size == userIds.size) {
						callback(usernames)
					}
				}
				.addOnFailureListener {
					usernames.add(userId to "Unknown")
					if (usernames.size == userIds.size) {
						callback(usernames)
					}
				}
		}
	}

	private fun showUserListDialog(title: String, userList: List<Pair<String, String>>) {
		val userNames = userList.map { it.second }.toTypedArray()
		AlertDialog.Builder(context)
			.setTitle(title)
			.setItems(userNames, null)
			.setNegativeButton("Close", null)
			.show()
	}

	private fun showPostDetail(post: Post) {
		val dialogFragment = PostDetailFragment.newInstance(post)
		dialogFragment.setTargetFragment(this, 0)
		dialogFragment.show(parentFragmentManager, "post_detail")
	}

	private fun showSettingsMenu() {
		val popupMenu = PopupMenu(context as Context, settingsButton)
		popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)
		popupMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.changeProfileImage -> openImageSelector()
				R.id.changeUsername -> changeUsername()
				R.id.changeEmail -> changeEmail()
				R.id.changePassword -> changePassword()
				R.id.deleteAccount -> showDeleteAccountConfirmationDialog()
				R.id.logout -> logout()
				R.id.manageUsers -> manageUsers()
			}
			true
		}
		popupMenu.show()

		// Check if the user is an admin
		auth.currentUser?.getIdToken(false)?.addOnSuccessListener { result ->
			val isAdmin = result.claims["admin"] as? Boolean ?: false
			if (isAdmin) {
				popupMenu.menu.findItem(R.id.manageUsers).isVisible = true
			}
		}
	}

	private fun openImageSelector() {
		val intent = Intent(Intent.ACTION_GET_CONTENT)
		intent.type = "image/*"
		startActivityForResult(intent, PICK_IMAGE_REQUEST)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, requestCode, data)
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

	private fun changeUsername() {
		val context = requireContext()
		val userId = auth.currentUser?.uid ?: return

		// Create an input dialog to get the new username from the user
		val input = EditText(context)
		AlertDialog.Builder(context)
			.setTitle("Change Username")
			.setMessage("Enter your new username:")
			.setView(input)
			.setPositiveButton("Change") { dialog, which ->
				val newUsername = input.text.toString().trim()
				if (newUsername.isNotEmpty()) {
					// Check if the new username is already taken
					db.collection("users").whereEqualTo("username", newUsername).get()
						.addOnSuccessListener { result ->
							if (result.isEmpty) {
								// Update the username in the user's document
								db.collection("users").document(userId).update("username", newUsername)
									.addOnSuccessListener {
										Toast.makeText(context, "Username changed successfully", Toast.LENGTH_SHORT).show()
										updateUsernameInPosts(newUsername)
										usernameTextView.text = newUsername
									}
									.addOnFailureListener { e ->
										Toast.makeText(context, "Failed to change username: ${e.message}", Toast.LENGTH_SHORT).show()
									}
							} else {
								Toast.makeText(context, "Username already taken", Toast.LENGTH_SHORT).show()
							}
						}
						.addOnFailureListener { e ->
							Toast.makeText(context, "Failed to check username: ${e.message}", Toast.LENGTH_SHORT).show()
						}
				} else {
					Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
				}
			}
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun updateUsernameInPosts(username: String) {
		val userId = auth.currentUser?.uid
		if (userId != null) {
			db.collection("posts").whereEqualTo("userId", userId).get()
				.addOnSuccessListener { result ->
					for (document in result) {
						db.collection("posts").document(document.id)
							.update("profileName", username)
					}
				}
		}
	}

	private fun changeEmail() {
		val context = requireContext()
		val userId = auth.currentUser?.uid ?: return

		// Create an input dialog to get the new email from the user
		val input = EditText(context)
		AlertDialog.Builder(context)
			.setTitle("Change Email")
			.setMessage("Enter your new email:")
			.setView(input)
			.setPositiveButton("Change") { dialog, which ->
				val newEmail = input.text.toString().trim()
				if (newEmail.isNotEmpty()) {
					// Update the email in the user's document
					db.collection("users").document(userId).update("email", newEmail)
						.addOnSuccessListener {
							Toast.makeText(context, "Email changed successfully", Toast.LENGTH_SHORT).show()
						}
						.addOnFailureListener { e ->
							Toast.makeText(context, "Failed to change email: ${e.message}", Toast.LENGTH_SHORT).show()
						}
				} else {
					Toast.makeText(context, "Email cannot be empty", Toast.LENGTH_SHORT).show()
				}
			}
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun changePassword() {
		val context = requireContext()
		val user = auth.currentUser ?: return

		AlertDialog.Builder(context)
			.setTitle("Change Password")
			.setMessage("Are you sure you want to reset your password?")
			.setPositiveButton("Reset") { _, _ ->
				auth.sendPasswordResetEmail(user.email!!)
					.addOnSuccessListener {
						Toast.makeText(context, "Password reset email sent", Toast.LENGTH_SHORT).show()
					}
					.addOnFailureListener { e ->
						Toast.makeText(context, "Failed to send password reset email: ${e.message}", Toast.LENGTH_SHORT).show()
					}
			}
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun manageUsers() {
		// Fetch all users and display in a dialog
		db.collection("users").get()
			.addOnSuccessListener { result ->
				val userList = result.documents.map { (it.getString("username") ?: "Unknown") to it.id }
				showManageUsersDialog(userList)
			}
	}

	private fun showManageUsersDialog(userList: List<Pair<String?, String>>) {
		val userNames = userList.map { it.first }.toTypedArray()
		val userIds = userList.map { it.second }.toTypedArray()

		AlertDialog.Builder(context)
			.setTitle("Manage Users")
			.setItems(userNames) { dialog, which ->
				val selectedUserId = userIds[which]
				showUserOptionsDialog(selectedUserId)
			}
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun showUserOptionsDialog(userId: String) {
		AlertDialog.Builder(context)
			.setTitle("User Options")
			.setItems(arrayOf("Show Profile", "Change Profile Image", "Change Username", "Change Email", "Reset Password", "Ban User", "Timeout User", "Verify User")) { dialog, which ->
				when (which) {
					0 -> showUserProfile(userId)
					1 -> changeUserProfileImage(userId)
					2 -> changeUserUsername(userId)
					3 -> changeUserEmail(userId)
					4 -> resetUserPassword(userId)
					5 -> banUser(userId)
					6 -> timeoutUser(userId)
					7 -> verifyUser(userId)
				}
			}
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun showUserProfile(userId: String) {
		// Implement show user profile functionality
		Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
	}

	private fun changeUserProfileImage(userId: String) {
		// Implement change user profile image functionality
		Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
	}

	private fun changeUserUsername(userId: String) {
		// Implement change user username functionality
		Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
	}

	private fun changeUserEmail(userId: String) {
		// Implement change user email functionality
		Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
	}

	private fun resetUserPassword(userId: String) {
		// Implement reset user password functionality
		Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
	}

	private fun banUser(userId: String) {
		// Implement ban user functionality
		Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
	}

	private fun timeoutUser(userId: String) {
		// Implement timeout user functionality
		Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
	}

	private fun verifyUser(userId: String) {
		AlertDialog.Builder(context)
			.setMessage("Are you sure you want to verify this user?")
			.setPositiveButton("Verify") { _, _ ->
				db.collection("users").document(userId).update("isVerified", true)
					.addOnSuccessListener {
						Toast.makeText(context, "User verified successfully", Toast.LENGTH_SHORT).show()
					}
					.addOnFailureListener { e ->
						Toast.makeText(context, "Failed to verify user: ${e.message}", Toast.LENGTH_SHORT).show()
					}
			}
			.setNegativeButton("Cancel", null)
			.show()
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
				auth.currentUser?.delete()
					?.addOnCompleteListener { task ->
						if (task.isSuccessful) {
							Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
							startActivity(Intent(context, RegisterActivity::class.java))
							activity?.finish()
						} else {
							Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show()
						}
					}
			}
			.addOnFailureListener { e ->
				Toast.makeText(context, "Failed to delete user: ${e.message}", Toast.LENGTH_SHORT).show()
			}
	}

	private fun logout() {
		auth.signOut()
		startActivity(Intent(context, LoginActivity::class.java))
		activity?.finish()
	}

	override fun onPostDeleted() {
		loadPosts()
	}
}
