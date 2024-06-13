package si.uni_lj.fe.whatthecatdoin.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R
import si.uni_lj.fe.whatthecatdoin.RegisterActivity
import si.uni_lj.fe.whatthecatdoin.ui.archive.ArchiveAdapter
import si.uni_lj.fe.whatthecatdoin.ui.archive.PostDetailFragment

class ProfileFragment : Fragment(), PostDetailFragment.PostDeleteListener {

	private lateinit var recyclerView: RecyclerView
	private lateinit var adapter: ArchiveAdapter
	private lateinit var postList: MutableList<Post>
	private lateinit var db: FirebaseFirestore
	private lateinit var auth: FirebaseAuth
	private lateinit var usernameTextView: TextView
	private lateinit var logoutButton: TextView
	private lateinit var scrollToTopButton: FloatingActionButton

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_profile, container, false)

		db = FirebaseFirestore.getInstance()
		auth = FirebaseAuth.getInstance()

		usernameTextView = view.findViewById(R.id.usernameTextView)
		logoutButton = view.findViewById(R.id.logoutButton)
		recyclerView = view.findViewById(R.id.archiveRecyclerView)
		recyclerView.layoutManager = GridLayoutManager(context, 2)
		postList = mutableListOf()
		adapter = ArchiveAdapter { post -> showPostDetail(post) }
		recyclerView.adapter = adapter
		scrollToTopButton = view.findViewById(R.id.scrollToTopButton)

		loadPosts()

		auth.currentUser?.let { user ->
			usernameTextView.text = user.displayName
		}

		usernameTextView.setOnClickListener { showPopupMenu() }
		logoutButton.setOnClickListener { logout() }
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
					adapter.submitList(postList)
				}
		}
	}

	private fun showPostDetail(post: Post) {
		val dialogFragment = PostDetailFragment.newInstance(post)
		dialogFragment.setTargetFragment(this, 0)
		dialogFragment.show(parentFragmentManager, "post_detail")
	}

	private fun showPopupMenu() {
		val popupMenu = PopupMenu(context, usernameTextView)
		popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)
		popupMenu.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.changeUsername -> changeUsername()
				R.id.changeEmail -> changeEmail()
				R.id.changePassword -> changePassword()
				R.id.deleteAccount -> deleteAccount()
				R.id.banUser -> showBanUserDialog()
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
		// Implement change username functionality
	}

	private fun changeEmail() {
		// Implement change email functionality
	}

	private fun changePassword() {
		// Implement change password functionality
	}

	private fun deleteAccount() {
		// Implement delete account functionality
	}

	private fun showBanUserDialog() {
		val usersRef = db.collection("users")
		val usersList = mutableListOf<String>()
		val usersIdList = mutableListOf<String>()

		usersRef.get().addOnSuccessListener { result ->
			for (document in result) {
				val username = document.getString("username") ?: "Unknown"
				usersList.add(username)
				usersIdList.add(document.id)
			}

			val builder = AlertDialog.Builder(requireContext())
			builder.setTitle("Select a user to ban")
			builder.setItems(usersList.toTypedArray()) { _, which ->
				val userIdToBan = usersIdList[which]
				showBanConfirmationDialog(userIdToBan)
			}
			builder.show()
		}
	}

	private fun showBanConfirmationDialog(userIdToBan: String) {
		AlertDialog.Builder(requireContext())
			.setMessage("Are you sure you want to ban this user?")
			.setPositiveButton("Ban") { _, _ ->
				banUser(userIdToBan)
			}
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun banUser(userIdToBan: String) {
		val userRef = db.collection("users").document(userIdToBan)
		val postsRef = db.collection("posts").whereEqualTo("userId", userIdToBan)

		// Delete all posts by the user
		postsRef.get().addOnSuccessListener { result ->
			for (document in result) {
				document.reference.delete()
			}

			// Delete the user document
			userRef.delete().addOnSuccessListener {
				Toast.makeText(context, "User banned successfully", Toast.LENGTH_SHORT).show()
			}.addOnFailureListener { e ->
				Toast.makeText(context, "Failed to ban user: ${e.message}", Toast.LENGTH_SHORT).show()
			}
		}
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
