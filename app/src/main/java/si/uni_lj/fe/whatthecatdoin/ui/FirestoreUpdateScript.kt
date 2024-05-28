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
				// Check if the post already has an ID
				if (post.id.isEmpty()) {
					val newId = UUID.randomUUID().toString()
					document.reference.update("id", newId)
				}
			}
		}
		.addOnFailureListener { e ->
			// Handle any errors
			println("Error updating posts: ${e.message}")
		}
}
