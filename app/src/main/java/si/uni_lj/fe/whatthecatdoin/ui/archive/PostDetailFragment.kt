package si.uni_lj.fe.whatthecatdoin.ui.archive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.databinding.FragmentPostDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class PostDetailFragment : Fragment() {

	private lateinit var post: Post
	private var _binding: FragmentPostDetailBinding? = null
	private val binding get() = _binding!!

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		post = arguments?.getParcelable(POST_KEY)!!
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = FragmentPostDetailBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
		val date = sdf.format(Date(post.timestamp))

		binding.postDetailDate.text = date
		binding.postDetailLikes.text = "Likes: ${post.likes}"
		binding.postDetailTags.text = if (post.tags.isNotEmpty()) "Tags: ${post.tags.joinToString(" ")}" else ""
		Glide.with(this).load(post.imageUrl).into(binding.postDetailImage)

		view.setOnClickListener {
			parentFragmentManager.popBackStack()
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	companion object {
		private const val POST_KEY = "post"

		fun newInstance(post: Post): PostDetailFragment {
			val fragment = PostDetailFragment()
			val args = Bundle()
			args.putParcelable(POST_KEY, post)
			fragment.arguments = args
			return fragment
		}
	}
}
