package si.uni_lj.fe.whatthecatdoin.ui.comments

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import si.uni_lj.fe.whatthecatdoin.R

class CommentsActivity : AppCompatActivity() {

	private lateinit var db: FirebaseFirestore
	private lateinit var auth: FirebaseAuth
	private lateinit var recyclerView: RecyclerView
	private lateinit var adapter: CommentsAdapter
	private lateinit var commentList: MutableList<Comment>
	lateinit var postId: String

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_comments)

		db = FirebaseFirestore.getInstance()
		auth = FirebaseAuth.getInstance()
		recyclerView = findViewById(R.id.commentsRecyclerView)
		recyclerView.layoutManager = LinearLayoutManager(this)
		commentList = mutableListOf()
		adapter = CommentsAdapter(commentList, auth.currentUser?.uid ?: "")
		recyclerView.adapter = adapter

		postId = intent.getStringExtra("postId") ?: return

		val addCommentButton: Button = findViewById(R.id.addCommentButton)
		val commentEditText: EditText = findViewById(R.id.commentEditText)

		addCommentButton.setOnClickListener {
			val commentText = commentEditText.text.toString()
			if (commentText.isNotEmpty()) {
				addComment(commentText)
			} else {
				Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
			}
		}

		loadComments()
	}

	private fun loadComments() {
		db.collection("posts").document(postId).collection("comments")
			.get()
			.addOnSuccessListener { result ->
				commentList.clear()
				for (document in result) {
					val comment = document.toObject(Comment::class.java)
					commentList.add(comment)
				}
				adapter.notifyDataSetChanged()
			}
	}

	private fun addComment(text: String) {
		val comment = Comment(
			userId = auth.currentUser?.uid ?: "",
			userName = auth.currentUser?.displayName ?: "",
			text = text,
			timestamp = System.currentTimeMillis()
		)
		db.collection("posts").document(postId).collection("comments")
			.add(comment)
			.addOnSuccessListener {
				commentList.add(comment)
				adapter.notifyDataSetChanged()
				findViewById<EditText>(R.id.commentEditText).text.clear()
				Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show()
			}
			.addOnFailureListener {
				Toast.makeText(this, "Failed to add comment", Toast.LENGTH_SHORT).show()
			}
	}
}
