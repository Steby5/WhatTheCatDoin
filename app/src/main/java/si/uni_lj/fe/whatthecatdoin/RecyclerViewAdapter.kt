package si.uni_lj.fe.whatthecatdoin

import android.app.AlertDialog
import android.content.Context
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
import com.google.firebase.storage.FirebaseStorage
import si.uni_lj.fe.whatthecatdoin.ui.comments.CommentsActivity

class RecyclerViewAdapter(private var postList: List<Post>, private var isAdmin: Boolean = false) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

	private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
	private val auth: FirebaseAuth = FirebaseAuth.getInstance()
	private val storage: FirebaseStorage = FirebaseStorage.getInstance()
	private val currentUserId = auth.currentUser?.uid

	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val profileName: TextView = itemView.findViewById(R.id.profileName)
		val postImage: ImageView = itemView.findViewById(R.id.postImage)
		val description: TextView = itemView.findViewById(R.id.description)
		val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
		val likeCount: TextView = itemView.findViewById(R.id.likeCount)
		val commentButton: ImageButton = itemView.findViewById(R.id.commentButton)
		val commentCount: TextView = itemView.findViewById(R.id.commentCount)
		val followButton: ImageButton = itemView.findViewById(R.id.followButton)
		val followCount: TextView = itemView.findViewById(R.id.followCount)
		val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
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
						holder.followButton.setImageResource(R.drawable.ic_unfollow)
					} else {
						holder.followButton.setImageResource(R.drawable.ic_follow)
					}
				}

			holder.followButton.setOnClickListener {
				toggleFollow(post, holder)
			}

			// Show delete button for admin users
			if (isAdmin) {
				holder.deleteButton.visibility = View.VISIBLE
				holder.deleteButton.setOnClickListener {
					showDeleteConfirmationDialog(holder.itemView.context, post, holder)
				}
			}
		}

		// Fetch and display comment count
		db.collection("posts").document(post.id).collection("comments")
			.get()
			.addOnSuccessListener { result ->
				holder.commentCount.text = result.size().toString()
			}

		// Fetch and display follower count
		db.collection("users").document(post.userId).collection("followers")
			.get()
			.addOnSuccessListener { result ->
				holder.followCount.text = result.size().toString()
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
					post.likes = maxOf(0, post.likes - 1)
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
			val followingRef = db.collection("users").document(currentUserId).collection("following").document(post.userId)
			val followerRef = db.collection("users").document(post.userId).collection("followers").document(currentUserId)

			db.runTransaction { transaction ->
				val followingDoc = transaction.get(followingRef)
				if (followingDoc.exists()) {
					// Unfollow the user
					transaction.delete(followingRef)
					transaction.delete(followerRef)
					holder.itemView.post {
						holder.followButton.setImageResource(R.drawable.ic_follow)
						Toast.makeText(holder.itemView.context, "Unfollowed", Toast.LENGTH_SHORT).show()
					}
				} else {
					// Follow the user
					transaction.set(followingRef, hashMapOf("following" to true))
					transaction.set(followerRef, hashMapOf("follower" to true))
					holder.itemView.post {
						holder.followButton.setImageResource(R.drawable.ic_unfollow)
						Toast.makeText(holder.itemView.context, "Followed", Toast.LENGTH_SHORT).show()
					}
				}
			}.addOnSuccessListener {
				updateFollowCount(post.userId, holder)
			}.addOnFailureListener { e ->
				holder.itemView.post {
					Toast.makeText(holder.itemView.context, "Failed to update follow status: ${e.message}", Toast.LENGTH_SHORT).show()
				}
			}
		}
	}

	private fun updateFollowCount(userId: String, holder: ViewHolder) {
		db.collection("users").document(userId).collection("followers").get()
			.addOnSuccessListener { documents ->
				holder.itemView.post {
					holder.followCount.text = documents.size().toString()
				}
			}
			.addOnFailureListener { e ->
				holder.itemView.post {
					holder.followCount.text = "0"
				}
			}
	}


	private fun showDeleteConfirmationDialog(context: Context, post: Post, holder: ViewHolder) {
		AlertDialog.Builder(context)
			.setMessage("Are you sure you want to delete this post?")
			.setPositiveButton("Delete") { _, _ -> deletePost(post, holder) }
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun deletePost(post: Post, holder: ViewHolder) {
		// Delete the image from Firebase Storage
		val imageRef = storage.getReferenceFromUrl(post.imageUrl)
		imageRef.delete()
			.addOnSuccessListener {
				// Delete the post document from Firestore
				db.collection("posts").document(post.id)
					.delete()
					.addOnSuccessListener {
						Toast.makeText(holder.itemView.context, "Post deleted", Toast.LENGTH_SHORT).show()
						(postList as MutableList).remove(post)
						notifyDataSetChanged()
					}
					.addOnFailureListener { e ->
						Toast.makeText(holder.itemView.context, "Failed to delete post: ${e.message}", Toast.LENGTH_LONG).show()
					}
			}
			.addOnFailureListener { e ->
				Toast.makeText(holder.itemView.context, "Failed to delete image: ${e.message}", Toast.LENGTH_LONG).show()
			}
	}
}
