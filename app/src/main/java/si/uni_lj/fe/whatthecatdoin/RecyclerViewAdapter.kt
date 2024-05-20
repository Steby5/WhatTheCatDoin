package si.uni_lj.fe.whatthecatdoin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecyclerViewAdapter(private val postList: List<Post>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

	private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
	private val auth: FirebaseAuth = FirebaseAuth.getInstance()
	private val currentUserId = auth.currentUser?.uid

	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val profileName: TextView = itemView.findViewById(R.id.profileName)
		val postImage: ImageView = itemView.findViewById(R.id.postImage)
		val tags: TextView = itemView.findViewById(R.id.tags)
		val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
		val likeCount: TextView = itemView.findViewById(R.id.likeCount)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val post = postList[position]
		holder.profileName.text = post.profileName
		holder.likeCount.text = post.likes.toString()

		if (post.tags.isNotEmpty()) {
			holder.tags.text = "Tags: ${post.tags.joinToString(" ")}"
			holder.tags.visibility = View.VISIBLE
		} else {
			holder.tags.visibility = View.GONE
		}

		if (post.imageResId != null) {
			holder.postImage.setImageResource(post.imageResId)
		} else if (post.imageUrl != null) {
			Glide.with(holder.itemView.context).load(post.imageUrl).into(holder.postImage)
		}

		if (currentUserId != null) {
			db.collection("posts").document(post.id).collection("likes").document(currentUserId)
				.get()
				.addOnSuccessListener { document ->
					if (document.exists()) {
						holder.likeButton.setImageResource(R.drawable.heart_green)
					} else {
						holder.likeButton.setImageResource(R.drawable.heart)
					}
				}

			holder.likeButton.setOnClickListener {
				toggleLike(post, holder)
			}
		}
	}

	override fun getItemCount(): Int = postList.size

	private fun toggleLike(post: Post, holder: ViewHolder) {
		if (currentUserId != null) {
			val likeRef = db.collection("posts").document(post.id).collection("likes").document(currentUserId)
			likeRef.get().addOnSuccessListener { document ->
				if (document.exists()) {
					// Unlike the post
					likeRef.delete()
					post.likes -= 1
					holder.likeButton.setImageResource(R.drawable.heart)
				} else {
					// Like the post
					likeRef.set(hashMapOf("liked" to true))
					post.likes += 1
					holder.likeButton.setImageResource(R.drawable.heart_green)
				}
				holder.likeCount.text = post.likes.toString()
				updatePostLikes(post)
			}
		}
	}

	private fun updatePostLikes(post: Post) {
		db.collection("posts").document(post.id)
			.update("likes", post.likes)
			.addOnSuccessListener {
				// Successfully updated likes
			}
			.addOnFailureListener { e ->
				// Handle failure
			}
	}
}
