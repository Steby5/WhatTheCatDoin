package si.uni_lj.fe.whatthecatdoin.ui.archive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R
import java.text.SimpleDateFormat
import java.util.*

class ArchiveFragment : Fragment() {

	private lateinit var recyclerView: RecyclerView
	private lateinit var adapter: ArchiveAdapter
	private lateinit var postList: MutableList<Post>
	private lateinit var db: FirebaseFirestore
	private lateinit var auth: FirebaseAuth

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_archive, container, false)

		recyclerView = view.findViewById(R.id.archiveRecyclerView)
		recyclerView.layoutManager = GridLayoutManager(context, 2)
		postList = mutableListOf()
		adapter = ArchiveAdapter { post -> showPostDetailDialog(post) }
		recyclerView.adapter = adapter

		db = FirebaseFirestore.getInstance()
		auth = FirebaseAuth.getInstance()

		loadPosts()

		return view
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
}
