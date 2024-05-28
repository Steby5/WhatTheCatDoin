package si.uni_lj.fe.whatthecatdoin.ui.archive

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R
import java.text.SimpleDateFormat
import java.util.*

class PostDetailFragment : DialogFragment() {

	interface PostDeleteListener {
		fun onPostDeleted()
	}

	companion object {
		private const val ARG_POST = "post"

		fun newInstance(post: Post): PostDetailFragment {
			val args = Bundle()
			args.putParcelable(ARG_POST, post)
			val fragment = PostDetailFragment()
			fragment.arguments = args
			return fragment
		}
	}

	private lateinit var postImageView: ImageView
	private lateinit var likesCountTextView: TextView
	private lateinit var descriptionTextView: TextView
	private lateinit var timestampTextView: TextView
	private lateinit var toolbar: Toolbar
	private lateinit var db: FirebaseFirestore
	private lateinit var storage: FirebaseStorage
	private var post: Post? = null
	private var listener: PostDeleteListener? = null

	override fun onAttach(context: Context) {
		super.onAttach(context)
		if (context is PostDeleteListener) {
			listener = context
		} else if (targetFragment is PostDeleteListener) {
			listener = targetFragment as PostDeleteListener
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_post_detail, container, false)

		db = FirebaseFirestore.getInstance()
		storage = FirebaseStorage.getInstance()
		toolbar = view.findViewById(R.id.toolbar)
		postImageView = view.findViewById(R.id.postImageView)
		likesCountTextView = view.findViewById(R.id.likes_count)
		descriptionTextView = view.findViewById(R.id.description)
		timestampTextView = view.findViewById(R.id.timestamp)

		toolbar.setNavigationOnClickListener {
			dismiss()
		}

		toolbar.inflateMenu(R.menu.menu_post_detail)
		toolbar.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.action_delete -> {
					showDeleteConfirmationDialog()
					true
				}
				else -> false
			}
		}

		post = arguments?.getParcelable(ARG_POST)
		post?.let {
			Glide.with(this).load(it.imageUrl).into(postImageView)
			likesCountTextView.text = it.likes.toString()
			descriptionTextView.text = it.description
			timestampTextView.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it.timestamp))
		}

		return view
	}

	private fun showDeleteConfirmationDialog() {
		AlertDialog.Builder(requireContext())
			.setMessage("Are you sure you want to delete this post?")
			.setPositiveButton("Delete") { _, _ -> deletePost() }
			.setNegativeButton("Cancel", null)
			.show()
	}

	private fun deletePost() {
		post?.let { post ->
			// Delete the image from Firebase Storage
			val imageRef = storage.getReferenceFromUrl(post.imageUrl)
			imageRef.delete()
				.addOnSuccessListener {
					// Delete the post document from Firestore
					db.collection("posts").document(post.id)
						.delete()
						.addOnSuccessListener {
							Toast.makeText(requireContext(), "Image deleted", Toast.LENGTH_SHORT).show()
							listener?.onPostDeleted()
							dismiss()  // Close the dialog after deletion
						}
						.addOnFailureListener { e ->
							Toast.makeText(requireContext(), "Failed to delete image: ${e.message}", Toast.LENGTH_SHORT).show()
						}
				}
				.addOnFailureListener { e ->
					if (e.message?.contains("Object does not exist") == true) {
						// If the image does not exist, proceed to delete the post data
						db.collection("posts").document(post.id)
							.delete()
							.addOnSuccessListener {
								Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
								listener?.onPostDeleted()
								dismiss()  // Close the dialog after deletion
							}
							.addOnFailureListener { ex ->
								Toast.makeText(requireContext(), "Failed to delete post: ${ex.message}", Toast.LENGTH_SHORT).show()
							}
					} else {
						Toast.makeText(requireContext(), "Failed to delete image: ${e.message}", Toast.LENGTH_SHORT).show()
					}
				}
		}
	}
}
