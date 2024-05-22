package si.uni_lj.fe.whatthecatdoin.ui.archive

import android.app.Dialog
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R
import java.text.SimpleDateFormat
import java.util.*

class PostDetailDialogFragment : DialogFragment() {

	private lateinit var detailImageView: ImageView
	private lateinit var detailTimestamp: TextView
	private lateinit var detailLikes: TextView
	private lateinit var detailTags: TextView

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_post_detail_dialog, container, false)

		detailImageView = view.findViewById(R.id.detailImageView)
		detailTimestamp = view.findViewById(R.id.detailTimestamp)
		detailLikes = view.findViewById(R.id.detailLikes)
		detailTags = view.findViewById(R.id.detailTags)

		return view
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val post = arguments?.getParcelable<Post>("post")

		post?.let {
			// Load the image with Glide and adjust its size
			Glide.with(this)
				.load(it.imageUrl)
				.listener(object : RequestListener<Drawable> {
					override fun onLoadFailed(
						e: GlideException?,
						model: Any?,
						target: Target<Drawable>?,
						isFirstResource: Boolean
					): Boolean {
						return false
					}

					override fun onResourceReady(
						resource: Drawable?,
						model: Any?,
						target: Target<Drawable>?,
						dataSource: DataSource?,
						isFirstResource: Boolean
					): Boolean {
						resource?.let { drawable ->
							val aspectRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
							val display = requireActivity().windowManager.defaultDisplay
							val size = Point()
							display.getSize(size)
							val width = size.x
							val height = (width / aspectRatio).toInt()

							detailImageView.layoutParams.width = width
							detailImageView.layoutParams.height = height
							detailImageView.requestLayout()
						}
						return false
					}
				})
				.into(detailImageView)

			// Set the timestamp
			val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
			val date = Date(it.timestamp)
			detailTimestamp.text = dateFormat.format(date)

			// Set the likes
			detailLikes.text = getString(R.string.likes_count, it.likes)

			// Set the tags with "#" prefix
			detailTags.text = if (it.tags.isNotEmpty()) it.tags.joinToString(", ") { tag -> "#$tag" } else getString(R.string.no_tags)
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialog = super.onCreateDialog(savedInstanceState)
		dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
		return dialog
	}

	companion object {
		fun newInstance(post: Post): PostDetailDialogFragment {
			val args = Bundle().apply {
				putParcelable("post", post)
			}
			val fragment = PostDetailDialogFragment()
			fragment.arguments = args
			return fragment
		}
	}
}
