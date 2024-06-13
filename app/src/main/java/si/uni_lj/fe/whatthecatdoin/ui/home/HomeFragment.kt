package si.uni_lj.fe.whatthecatdoin.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R
import si.uni_lj.fe.whatthecatdoin.RecyclerViewAdapter

class HomeFragment : Fragment() {

	private lateinit var db: FirebaseFirestore
	private lateinit var auth: FirebaseAuth
	private lateinit var recyclerView: RecyclerView
	private lateinit var adapter: RecyclerViewAdapter
	private lateinit var postList: MutableList<Post>
	private lateinit var swipeRefreshLayout: SwipeRefreshLayout
	private lateinit var filterButton: Button
	private lateinit var fabScrollToTop: FloatingActionButton
	private var showAllPosts: Boolean = true

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_home, container, false)

		db = FirebaseFirestore.getInstance()
		auth = FirebaseAuth.getInstance()
		recyclerView = view.findViewById(R.id.recyclerView)
		swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
		filterButton = view.findViewById(R.id.filterButton)
		fabScrollToTop = view.findViewById(R.id.fabScrollToTop)
		postList = mutableListOf()
		adapter = RecyclerViewAdapter(postList)
		recyclerView.layoutManager = LinearLayoutManager(context)
		recyclerView.adapter = adapter

		swipeRefreshLayout.setOnRefreshListener {
			loadPosts {
				swipeRefreshLayout.isRefreshing = false
			}
		}

		filterButton.setOnClickListener {
			showAllPosts = !showAllPosts
			filterButton.text = if (showAllPosts) "All Cats" else "Followed Cats"
			loadPosts()
		}

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

		loadPosts()

		return view
	}

	private fun loadPosts(onComplete: (() -> Unit)? = null) {
		val userId = auth.currentUser?.uid ?: return

		if (showAllPosts) {
			db.collection("posts").get()
				.addOnSuccessListener { result ->
					postList.clear()
					for (document in result) {
						val post = document.toObject(Post::class.java).copy(id = document.id)
						postList.add(post)
					}
					postList.sortByDescending { it.timestamp }
					adapter.notifyDataSetChanged()
					onComplete?.invoke()
				}
				.addOnFailureListener {
					onComplete?.invoke()
				}
		} else {
			db.collection("users").document(userId).collection("following").get()
				.addOnSuccessListener { result ->
					val followedUsers = result.documents.map { it.id }
					if (followedUsers.isNotEmpty()) {
						db.collection("posts").whereIn("userId", followedUsers).get()
							.addOnSuccessListener { result ->
								postList.clear()
								for (document in result) {
									val post = document.toObject(Post::class.java).copy(id = document.id)
									postList.add(post)
								}
								postList.sortByDescending { it.timestamp }
								adapter.notifyDataSetChanged()
								onComplete?.invoke()
							}
							.addOnFailureListener {
								onComplete?.invoke()
							}
					} else {
						postList.clear()
						adapter.notifyDataSetChanged()
						onComplete?.invoke()
					}
				}
				.addOnFailureListener {
					onComplete?.invoke()
				}
		}
	}

	override fun onResume() {
		super.onResume()
		loadPosts()
	}
}
