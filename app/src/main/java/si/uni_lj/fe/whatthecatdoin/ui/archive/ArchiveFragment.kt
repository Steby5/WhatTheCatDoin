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

class ArchiveFragment : Fragment() {

	private lateinit var recyclerView: RecyclerView
	private lateinit var adapter: ArchiveAdapter
	private lateinit var auth: FirebaseAuth
	private lateinit var db: FirebaseFirestore

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_archive, container, false)
		recyclerView = view.findViewById(R.id.archiveRecyclerView)
		recyclerView.layoutManager = GridLayoutManager(context, 3)
		auth = FirebaseAuth.getInstance()
		db = FirebaseFirestore.getInstance()
		adapter = ArchiveAdapter { post -> onPostClicked(post) }
		recyclerView.adapter = adapter

		loadPosts()

		return view
	}

	private fun loadPosts() {
		val currentUser = auth.currentUser ?: return
		db.collection("posts")
			.whereEqualTo("userId", currentUser.uid)
			.get()
			.addOnSuccessListener { result ->
				val posts = result.map { document ->
					document.toObject(Post::class.java)
				}
				adapter.submitList(posts)
			}
	}

	private fun onPostClicked(post: Post) {
		val dialog = PostDetailDialogFragment.newInstance(post)
		dialog.show(parentFragmentManager, "PostDetailDialog")
	}
}
