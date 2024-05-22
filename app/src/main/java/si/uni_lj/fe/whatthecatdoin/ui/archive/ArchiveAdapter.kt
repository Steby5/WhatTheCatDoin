package si.uni_lj.fe.whatthecatdoin.ui.archive

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R

class ArchiveAdapter(private val onClick: (Post) -> Unit) :
	RecyclerView.Adapter<ArchiveAdapter.ArchiveViewHolder>() {

	private var posts = listOf<Post>()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_archive, parent, false)
		return ArchiveViewHolder(view, onClick)
	}

	override fun onBindViewHolder(holder: ArchiveViewHolder, position: Int) {
		holder.bind(posts[position])
	}

	override fun getItemCount() = posts.size

	fun submitList(newPosts: List<Post>) {
		posts = newPosts
		notifyDataSetChanged()
	}

	class ArchiveViewHolder(itemView: View, val onClick: (Post) -> Unit) :
		RecyclerView.ViewHolder(itemView) {
		private val imageView: ImageView = itemView.findViewById(R.id.archiveImageView)
		private var currentPost: Post? = null

		init {
			itemView.setOnClickListener {
				currentPost?.let {
					onClick(it)
				}
			}
		}

		fun bind(post: Post) {
			currentPost = post
			Glide.with(itemView.context).load(post.imageUrl).into(imageView)
		}
	}
}
