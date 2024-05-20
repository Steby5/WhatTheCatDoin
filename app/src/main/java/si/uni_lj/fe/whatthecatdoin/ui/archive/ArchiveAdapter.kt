package si.uni_lj.fe.whatthecatdoin.ui.archive

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R

class ArchiveAdapter(private val postList: List<Post>, private val fragment: ArchiveFragment) :
	RecyclerView.Adapter<ArchiveAdapter.ViewHolder>() {

	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val postImage: ImageView = itemView.findViewById(R.id.postImage)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_archive, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val post = postList[position]
		Glide.with(holder.itemView.context).load(post.imageUrl).into(holder.postImage)

		holder.itemView.setOnClickListener {
			fragment.showPostDetails(post)
		}
	}

	override fun getItemCount(): Int = postList.size
}
