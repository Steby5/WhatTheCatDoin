package si.uni_lj.fe.whatthecatdoin

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import si.uni_lj.fe.whatthecatdoin.ui.comments.CommentsActivity

class RecyclerViewAdapter(private var postList: List<Post>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

	private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
	private val auth: FirebaseAuth = FirebaseAuth.getInstance()
	private val currentUserId = auth.currentUser?.uid

	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val profileName: TextView = itemView.findViewById(R.id.profileName)
		val postImage: ImageView = itemView.findViewById(R.id.postImage)
		val description: TextView = itemView.findViewById(R.id.description)
		val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
		val likeCount: TextView = itemView.findViewById(R.id.likeCount)
		val commentButton: ImageButton = itemView.findViewById(R.id.commentButton)
		val followButton: ImageButton = itemView.findViewById(R.id.followButton)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val post = postList[position]
		holder.profileName.text = post.profileName
		holder.likeCount.text = post.likes.toString()
		holder.description.text = post.description

		if (post.imageUrl.isNotEmpty()) {
			Glide.with(holder.itemView.context).load(post.imageUrl).into(holder.postImage)
		}

		if (currentUserId != null) {
			val postRef = db.collection("posts").document(post.id)
			postRef.collection("likes").document(currentUserId)
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

			holder.commentButton.setOnClickListener {
				val context = holder.itemView.context
				val intent = Intent(context, CommentsActivity::class.java).apply {
					putExtra("postId", post.id)
				}
				context.startActivity(intent)
			}

			db.collection("users").document(currentUserId).collection("following")
				.document(post.userId)
				.get()
				.addOnSuccessListener { document ->
					if (document.exists()) {
						holder.followButton.setImageResource(R.drawable.ic_following)
					} else {
						holder.followButton.setImageResource(R.drawable.ic_follow)
					}
				}

			holder.followButton.setOnClickListener {
				toggleFollow(post, holder)
			}
		}
	}

	override fun getItemCount(): Int = postList.size

	fun updatePosts(newPostList: List<Post>) {
		postList = newPostList
		notifyDataSetChanged()
	}

	private fun toggleLike(post: Post, holder: ViewHolder) {
		if (currentUserId != null) {
			val postRef = db.collection("posts").document(post.id)
			val likeRef = postRef.collection("likes").document(currentUserId)
			likeRef.get().addOnSuccessListener { document ->
				if (document.exists()) {
					// Unlike the post
					likeRef.delete()
					post.likes -= 1
					holder.likeButton.setImageResource(R.drawable.heart)
					Toast.makeText(holder.itemView.context, "Unliked", Toast.LENGTH_SHORT).show()
				} else {
					// Like the post
					likeRef.set(hashMapOf("liked" to true))
					post.likes += 1
					holder.likeButton.setImageResource(R.drawable.heart_green)
					Toast.makeText(holder.itemView.context, "Liked", Toast.LENGTH_SHORT).show()
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

	private fun toggleFollow(post: Post, holder: ViewHolder) {
		if (currentUserId != null) {
			val followRef = db.collection("users").document(currentUserId).collection("following").document(post.userId)
			followRef.get().addOnSuccessListener { document ->
				if (document.exists()) {
					// Unfollow the user
					followRef.delete()
					holder.followButton.setImageResource(R.drawable.ic_follow)
					Toast.makeText(holder.itemView.context, "Unfollowed", Toast.LENGTH_SHORT).show()
				} else {
					// Follow the user
					followRef.set(hashMapOf("following" to true))
					holder.followButton.setImageResource(R.drawable.ic_following)
					Toast.makeText(holder.itemView.context, "Followed", Toast.LENGTH_SHORT).show()
				}
			}
		}
	}
}
