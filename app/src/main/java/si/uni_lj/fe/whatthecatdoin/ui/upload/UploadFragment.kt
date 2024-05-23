package si.uni_lj.fe.whatthecatdoin.ui.upload

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import si.uni_lj.fe.whatthecatdoin.CharacterLimitFilter
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R
import java.io.ByteArrayOutputStream
import java.util.*

class UploadFragment : Fragment() {

	private lateinit var imageView: ImageView
	private lateinit var tag1EditText: EditText
	private lateinit var tag2EditText: EditText
	private lateinit var uploadButton: LinearLayout
	private lateinit var uploadIcon: ImageView
	private lateinit var uploadText: TextView
	private lateinit var uploadProgressBar: ProgressBar
	private lateinit var cameraButton: LinearLayout
	private lateinit var galleryButton: LinearLayout
	private lateinit var progressBar: ProgressBar

	private lateinit var auth: FirebaseAuth
	private lateinit var db: FirebaseFirestore
	private lateinit var storage: FirebaseStorage

	private var selectedImage: Bitmap? = null

	companion object {
		private const val CAMERA_REQUEST_CODE = 100
		private const val GALLERY_REQUEST_CODE = 200
		private const val PERMISSION_REQUEST_CODE = 300
		private const val TAG_CHAR_LIMIT = 12
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_upload, container, false)

		imageView = view.findViewById(R.id.imageView)
		tag1EditText = view.findViewById(R.id.tag1EditText)
		tag2EditText = view.findViewById(R.id.tag2EditText)
		uploadButton = view.findViewById(R.id.uploadButton)
		uploadIcon = view.findViewById(R.id.uploadIcon)
		uploadText = view.findViewById(R.id.uploadText)
		uploadProgressBar = view.findViewById(R.id.uploadProgressBar)
		cameraButton = view.findViewById(R.id.cameraButton)
		galleryButton = view.findViewById(R.id.galleryButton)
		progressBar = view.findViewById(R.id.progressBar)

		auth = FirebaseAuth.getInstance()
		db = FirebaseFirestore.getInstance()
		storage = FirebaseStorage.getInstance()

		// Apply character limit filters
		val filters = arrayOf<InputFilter>(CharacterLimitFilter(TAG_CHAR_LIMIT))
		tag1EditText.filters = filters
		tag2EditText.filters = filters

		checkPermissions()

		cameraButton.setOnClickListener {
			if (checkPermissions()) {
				openCamera()
			} else {
				requestPermissions()
			}
		}

		galleryButton.setOnClickListener {
			if (checkPermissions()) {
				openGallery()
			} else {
				requestPermissions()
			}
		}

		uploadButton.setOnClickListener {
			uploadPost()
		}

		return view
	}

	private fun checkPermissions(): Boolean {
		val cameraPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
		val readStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
		} else {
			ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
		}
		val writeStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			PackageManager.PERMISSION_GRANTED // WRITE_EXTERNAL_STORAGE is not required for Android 13+
		} else {
			ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
		}
		return cameraPermission == PackageManager.PERMISSION_GRANTED &&
				readStoragePermission == PackageManager.PERMISSION_GRANTED &&
				writeStoragePermission == PackageManager.PERMISSION_GRANTED
	}

	private fun requestPermissions() {
		val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			arrayOf(
				Manifest.permission.CAMERA,
				Manifest.permission.READ_MEDIA_IMAGES,
				Manifest.permission.READ_MEDIA_VIDEO,
				Manifest.permission.READ_MEDIA_AUDIO
			)
		} else {
			arrayOf(
				Manifest.permission.CAMERA,
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
			)
		}
		requestPermissions(permissions, PERMISSION_REQUEST_CODE)
	}

	@Deprecated("Deprecated in Java")
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == PERMISSION_REQUEST_CODE) {
			if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
				Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show()
			} else {
				Toast.makeText(context, "Permissions Denied", Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun openCamera() {
		val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
		startActivityForResult(intent, CAMERA_REQUEST_CODE)
	}

	private fun openGallery() {
		val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
		startActivityForResult(intent, GALLERY_REQUEST_CODE)
	}

	private fun uploadPost() {
		val tag1 = tag1EditText.text.toString()
		val tag2 = tag2EditText.text.toString()

		val tags = mutableListOf<String>()
		if (tag1.isNotEmpty()) tags.add(tag1)
		if (tag2.isNotEmpty()) tags.add(tag2)

		if (selectedImage != null) {
			val user = auth.currentUser ?: return
			val userName = user.displayName ?: user.email ?: "Unknown"
			val userId = user.uid
			val storageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
			val baos = ByteArrayOutputStream()
			selectedImage?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
			val data = baos.toByteArray()

			// Show loading indicator and hide button content
			uploadIcon.visibility = View.GONE
			uploadText.visibility = View.GONE
			uploadProgressBar.visibility = View.VISIBLE
			uploadButton.isEnabled = false

			storageRef.putBytes(data)
				.addOnSuccessListener { taskSnapshot ->
					storageRef.downloadUrl.addOnSuccessListener { uri ->
						val docRef = db.collection("posts").document()
						val post = Post(
							id = docRef.id,
							userId = userId,
							profileName = userName,
							imageUrl = uri.toString(),
							tags = tags,
							likes = 0,
							timestamp = System.currentTimeMillis()
						)

						docRef.set(post)
							.addOnSuccessListener {
								Toast.makeText(context, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
								progressBar.visibility = View.GONE
								uploadButton.isEnabled = true
								findNavController().navigate(R.id.navigation_home)
							}
							.addOnFailureListener { e ->
								Toast.makeText(context, "Failed to upload post: ${e.message}", Toast.LENGTH_SHORT).show()
								progressBar.visibility = View.GONE
								uploadButton.isEnabled = true

								// Restore button content
								uploadIcon.visibility = View.VISIBLE
								uploadText.visibility = View.VISIBLE
								uploadProgressBar.visibility = View.GONE
							}
					}
				}
				.addOnFailureListener { e ->
					Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
					progressBar.visibility = View.GONE
					uploadButton.isEnabled = true

					// Restore button content
					uploadIcon.visibility = View.VISIBLE
					uploadText.visibility = View.VISIBLE
					uploadProgressBar.visibility = View.GONE
				}
		} else {
			Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
		}
	}

	@Deprecated("Deprecated in Java")
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == Activity.RESULT_OK) {
			when (requestCode) {
				CAMERA_REQUEST_CODE -> {
					selectedImage = data?.extras?.get("data") as Bitmap
					scaleImage()
					imageView.setImageBitmap(selectedImage)
				}
				GALLERY_REQUEST_CODE -> {
					val imageUri = data?.data
					imageView.setImageURI(imageUri)
					selectedImage = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
					scaleImage()
				}
			}
		}
	}

	private fun scaleImage() {
		val display = requireActivity().windowManager.defaultDisplay
		val size = Point()
		display.getSize(size)
		val width = size.x
		val height = (size.y * 0.5).toInt()

		val aspectRatio = selectedImage!!.width.toFloat() / selectedImage!!.height.toFloat()
		val scaledHeight = (width / aspectRatio).toInt()

		if (scaledHeight > height) {
			imageView.layoutParams.height = height
		} else {
			imageView.layoutParams.height = scaledHeight
		}
		imageView.requestLayout()
	}
}
