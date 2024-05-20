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

	private lateinit var db: FirebaseFirestore
	private lateinit var auth: FirebaseAuth
	private lateinit var recyclerView: RecyclerView
	private lateinit var adapter: ArchiveAdapter
	private lateinit var postList: MutableList<Post>

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_archive, container, false)

		db = FirebaseFirestore.getInstance()
		auth = FirebaseAuth.getInstance()
		recyclerView = view.findViewById(R.id.ArchiverRecyclerView)
		postList = mutableListOf()
		adapter = ArchiveAdapter(postList, this)
		recyclerView.layoutManager = GridLayoutManager(context, 3)
		recyclerView.adapter = adapter

		loadUserPosts()

		return view
	}

	private fun loadUserPosts() {
		val currentUser = auth.currentUser?.uid
		if (currentUser != null) {
			db.collection("posts").whereEqualTo("profileName", currentUser).get()
				.addOnSuccessListener { result ->
					postList.clear()
					for (document in result) {
						val post = document.toObject(Post::class.java)
						postList.add(post)
					}
					adapter.notifyDataSetChanged()
				}
				.addOnFailureListener { exception ->
					// Handle error
					exception.printStackTrace()
				}
		} else {
			// Handle the case where currentUser is null
			println("User is not authenticated.")
		}
	}

	fun showPostDetails(post: Post) {
		val fragment = PostDetailFragment.newInstance(post)
		parentFragmentManager.beginTransaction()
			.replace(R.id.nav_host_fragment_activity_main, fragment)
			.addToBackStack(null)
			.commit()
	}
}
