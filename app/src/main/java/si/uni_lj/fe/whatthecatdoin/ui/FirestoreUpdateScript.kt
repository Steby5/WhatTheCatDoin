package si.uni_lj.fe.whatthecatdoin.ui

import com.google.firebase.firestore.FirebaseFirestore
import si.uni_lj.fe.whatthecatdoin.Post
import java.util.*

fun updateExistingPosts() {
	val db = FirebaseFirestore.getInstance()
	val postsRef = db.collection("posts")

	postsRef.get()
		.addOnSuccessListener { result ->
			for (document in result) {
				val post = document.toObject(Post::class.java)
				val postId = document.id
				// Check if the post already has an ID
				if (post.id.isEmpty()) {
					val newId = UUID.randomUUID().toString()
					document.reference.update("id", newId)
				}
				// Check if the post already has a profileImageUrl
				if (post.profileImageUrl.isNullOrEmpty()) {
					val userId = post.userId
					db.collection("users").document(userId).get()
						.addOnSuccessListener { userDocument ->
							val profileImageUrl = userDocument.getString("profileImageUrl")
							if (!profileImageUrl.isNullOrEmpty()) {
								document.reference.update("profileImageUrl", profileImageUrl)
									.addOnSuccessListener {
										println("Post $postId updated with profile image.")
									}
									.addOnFailureListener { e ->
										println("Failed to update post $postId: ${e.message}")
									}
							}
						}
						.addOnFailureListener { e ->
							println("Failed to fetch user $userId: ${e.message}")
						}
				}
			}
		}
		.addOnFailureListener { e ->
			// Handle any errors
			println("Error updating posts: ${e.message}")
		}
}
