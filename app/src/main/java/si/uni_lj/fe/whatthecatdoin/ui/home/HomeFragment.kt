package si.uni_lj.fe.whatthecatdoin.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R
import si.uni_lj.fe.whatthecatdoin.RecyclerViewAdapter

class HomeFragment : Fragment() {

	private lateinit var db: FirebaseFirestore
	private lateinit var recyclerView: RecyclerView
	private lateinit var adapter: RecyclerViewAdapter
	private lateinit var postList: MutableList<Post>
	private lateinit var swipeRefreshLayout: SwipeRefreshLayout

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_home, container, false)

		db = FirebaseFirestore.getInstance()
		recyclerView = view.findViewById(R.id.recyclerView)
		swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
		postList = mutableListOf()
		adapter = RecyclerViewAdapter(postList)
		recyclerView.layoutManager = LinearLayoutManager(context)
		recyclerView.adapter = adapter

		swipeRefreshLayout.setOnRefreshListener {
			loadPosts {
				swipeRefreshLayout.isRefreshing = false
			}
		}

		loadPosts()

		return view
	}

	private fun loadPosts(onComplete: (() -> Unit)? = null) {
		db.collection("posts").get()
			.addOnSuccessListener { result ->
				val uniquePosts = mutableSetOf<Post>()
				for (document in result) {
					val post = document.toObject(Post::class.java).copy(id = document.id)
					uniquePosts.add(post)
				}
				postList.clear()
				postList.addAll(uniquePosts)
				postList.sortByDescending { it.timestamp }  // Ensure posts are sorted by timestamp
				adapter.updatePosts(postList)
				onComplete?.invoke()
			}
			.addOnFailureListener {
				onComplete?.invoke()
			}
	}

	override fun onResume() {
		super.onResume()
		loadPosts()
	}
}
