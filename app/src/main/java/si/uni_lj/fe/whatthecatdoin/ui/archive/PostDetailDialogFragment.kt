package si.uni_lj.fe.whatthecatdoin.ui.archive

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import si.uni_lj.fe.whatthecatdoin.R

class PostDetailDialogFragment : DialogFragment() {

	private lateinit var postImageView: ImageView
	private lateinit var likesCountTextView: TextView
	private lateinit var tagsTextView: TextView
	private lateinit var timestampTextView: TextView
	private lateinit var toolbar: Toolbar

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_post_detail_dialog, container, false)

		postImageView = view.findViewById(R.id.postImageView)
		likesCountTextView = view.findViewById(R.id.likes_count)
		tagsTextView = view.findViewById(R.id.tags)
		timestampTextView = view.findViewById(R.id.timestamp)
		toolbar = view.findViewById(R.id.toolbar)

		toolbar.setNavigationOnClickListener {
			dismiss()
		}

		val imageUrl = arguments?.getString("imageUrl")
		val likesCount = arguments?.getInt("likesCount")
		val tags = arguments?.getStringArray("tags")
		val timestamp = arguments?.getString("timestamp")

		likesCountTextView.text = likesCount.toString()
		tagsTextView.text = tags?.joinToString(" ") { tag -> "#$tag" }
		timestampTextView.text = timestamp

		imageUrl?.let {
			Glide.with(this)
				.load(it)
				.listener(object : RequestListener<Drawable> {
					override fun onLoadFailed(
						e: GlideException?,
						model: Any?,
						target: Target<Drawable>,
						isFirstResource: Boolean
					): Boolean {
						// Handle the error here
						return false
					}

					override fun onResourceReady(
						resource: Drawable,
						model: Any,
						target: Target<Drawable>?,
						dataSource: DataSource,
						isFirstResource: Boolean
					): Boolean {
						// Handle the success here
						return false
					}
				})
				.into(postImageView)
		}

		return view
	}
}
