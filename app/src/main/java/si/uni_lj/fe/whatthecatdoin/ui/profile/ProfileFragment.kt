package si.uni_lj.fe.whatthecatdoin.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import si.uni_lj.fe.whatthecatdoin.LoginActivity
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R
import si.uni_lj.fe.whatthecatdoin.RegisterActivity
import si.uni_lj.fe.whatthecatdoin.ui.archive.ArchiveAdapter
import si.uni_lj.fe.whatthecatdoin.ui.archive.PostDetailDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

	private lateinit var recyclerView: RecyclerView
	private lateinit var adapter: ArchiveAdapter
	private lateinit var postList: MutableList<Post>
	private lateinit var db: FirebaseFirestore
	private lateinit var auth: FirebaseAuth
	private lateinit var usernameTextView: TextView
	private lateinit var logoutButton: Button

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_profile, container, false)

		recyclerView = view.findViewById(R.id.archiveRecyclerView)
		recyclerView.layoutManager = GridLayoutManager(context, 2)
		postList = mutableListOf()
		adapter = ArchiveAdapter { post -> showPostDetailDialog(post) }
		recyclerView.adapter = adapter

		db = FirebaseFirestore.getInstance()
		auth = FirebaseAuth.getInstance()

		usernameTextView = view.findViewById(R.id.usernameTextView)
		logoutButton = view.findViewById(R.id.logoutButton)

		loadProfile()
		loadPosts()

		logoutButton.setOnClickListener {
			auth.signOut()
			startActivity(Intent(requireContext(), LoginActivity::class.java))
			requireActivity().finish()
		}

		usernameTextView.setOnClickListener {
			showProfileOptionsMenu()
		}

		return view
	}

	private fun loadProfile() {
		val user = auth.currentUser
		user?.let {
			usernameTextView.text = it.displayName ?: "No Username"
		}
	}

	private fun loadPosts() {
		val userId = auth.currentUser?.uid
		db.collection("posts").whereEqualTo("userId", userId).get()
			.addOnSuccessListener { result ->
				postList.clear()
				for (document in result) {
					val post = document.toObject(Post::class.java).copy(id = document.id)
					postList.add(post)
				}
				adapter.submitList(postList)
			}
	}

	private fun showPostDetailDialog(post: Post) {
		val dialog = PostDetailDialogFragment().apply {
			arguments = Bundle().apply {
				putString("imageUrl", post.imageUrl)
				putInt("likesCount", post.likes)
				putStringArray("tags", post.tags.toTypedArray())
				putString("timestamp", SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(post.timestamp)))
			}
		}
		dialog.show(childFragmentManager, "PostDetailDialogFragment")
	}

	private fun showProfileOptionsMenu() {
		val popupMenu = PopupMenu(requireContext(), usernameTextView)
		popupMenu.menuInflater.inflate(R.menu.profile_options_menu, popupMenu.menu)
		popupMenu.setOnMenuItemClickListener { menuItem ->
			when (menuItem.itemId) {
				R.id.changeUsername -> changeUsername()
				R.id.changeEmail -> changeEmail()
				R.id.changePassword -> changePassword()
				R.id.deleteAccount -> confirmDeleteAccount()
			}
			true
		}
		popupMenu.show()
	}

	private fun changeUsername() {
		// Implement change username logic
		Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
	}

	private fun changeEmail() {
		// Implement change email logic
		Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
	}

	private fun changePassword() {
		// Implement change password logic
		Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
	}

	private fun confirmDeleteAccount() {
		// Implement delete account confirmation logic
		Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
	}
}
