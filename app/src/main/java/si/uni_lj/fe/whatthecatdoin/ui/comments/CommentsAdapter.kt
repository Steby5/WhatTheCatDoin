package si.uni_lj.fe.whatthecatdoin.ui.comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import si.uni_lj.fe.whatthecatdoin.R

data class Comment(
	val userId: String = "",
	val userName: String = "",
	val text: String = "",
	val timestamp: Long = 0L
)

class CommentsAdapter(private val comments: List<Comment>, private val currentUserId: String) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

	private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
	private val auth: FirebaseAuth = FirebaseAuth.getInstance()

	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val userName: TextView = itemView.findViewById(R.id.commentUserName)
		val text: TextView = itemView.findViewById(R.id.commentText)
		val timestamp: TextView = itemView.findViewById(R.id.commentTimestamp)
		val deleteButton: ImageButton = itemView.findViewById(R.id.deleteCommentButton)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val comment = comments[position]
		holder.userName.text = comment.userName
		holder.text.text = comment.text
		holder.timestamp.text = java.text.DateFormat.getDateTimeInstance().format(comment.timestamp)

		// Show delete button if the comment belongs to the current user or if the current user is an admin
		if (comment.userId == currentUserId || auth.currentUser?.getIdToken(false)?.result?.claims?.get("admin") == true) {
			holder.deleteButton.visibility = View.VISIBLE
			holder.deleteButton.setOnClickListener {
				deleteComment(comment, holder)
			}
		} else {
			holder.deleteButton.visibility = View.GONE
		}
	}

	override fun getItemCount() = comments.size

	private fun deleteComment(comment: Comment, holder: ViewHolder) {
		val postId = (holder.itemView.context as CommentsActivity).postId
		db.collection("posts").document(postId).collection("comments")
			.whereEqualTo("userId", comment.userId)
			.whereEqualTo("timestamp", comment.timestamp)
			.get()
			.addOnSuccessListener { result ->
				for (document in result) {
					db.collection("posts").document(postId).collection("comments").document(document.id).delete()
						.addOnSuccessListener {
							(comments as MutableList).remove(comment)
							notifyDataSetChanged()
							holder.itemView.post {
								Toast.makeText(holder.itemView.context, "Comment deleted", Toast.LENGTH_SHORT).show()
							}
						}
						.addOnFailureListener { e ->
							holder.itemView.post {
								Toast.makeText(holder.itemView.context, "Failed to delete comment: ${e.message}", Toast.LENGTH_SHORT).show()
							}
						}
				}
			}
	}
}
