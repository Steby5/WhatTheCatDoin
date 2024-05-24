package si.uni_lj.fe.whatthecatdoin.ui.comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import si.uni_lj.fe.whatthecatdoin.R

data class Comment(
	val userId: String = "",
	val userName: String = "",
	val text: String = "",
	val timestamp: Long = 0L
)

class CommentsAdapter(private val comments: List<Comment>) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val userName: TextView = itemView.findViewById(R.id.commentUserName)
		val text: TextView = itemView.findViewById(R.id.commentText)
		val timestamp: TextView = itemView.findViewById(R.id.commentTimestamp)
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
	}

	override fun getItemCount() = comments.size
}
