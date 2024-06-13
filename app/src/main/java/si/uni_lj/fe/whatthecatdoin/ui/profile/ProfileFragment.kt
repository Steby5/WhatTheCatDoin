package si.uni_lj.fe.whatthecatdoin.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
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
	private lateinit var fabScrollToTop: FloatingActionButton

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
		fabScrollToTop = view.findViewById(R.id.fabScrollToTop)
		recyclerView.layoutManager = GridLayoutManager(context, 2)
		postList = mutableListOf()
		adapter = ArchiveAdapter { post -> showPostDetail(post) }
		recyclerView.adapter = adapter

		loadPosts()
		usernameTextView.text = auth.currentUser?.displayName
		usernameTextView.setOnClickListener { showPopupMenu() }
		logoutButton.setOnClickListener { logout() }

		recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)
				if (dy > 20) {
					fabScrollToTop.visibility = View.VISIBLE
				} else if (dy < -20 && recyclerView.computeVerticalScrollOffset() == 0) {
					fabScrollToTop.visibility = View.GONE
				}
			}
		})

		fabScrollToTop.setOnClickListener {
			recyclerView.smoothScrollToPosition(0)
			fabScrollToTop.visibility = View.GONE
		}

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
			}
			true
		}
		popupMenu.show()
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

	private fun logout() {
		auth.signOut()
		startActivity(Intent(context, RegisterActivity::class.java))
		activity?.finish()
	}

	override fun onPostDeleted() {
		loadPosts()
	}
}
