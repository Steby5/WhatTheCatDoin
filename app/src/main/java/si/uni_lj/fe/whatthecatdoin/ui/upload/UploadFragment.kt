package si.uni_lj.fe.whatthecatdoin.ui.upload

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import si.uni_lj.fe.whatthecatdoin.MainActivity
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R
import java.io.File
import java.util.*

class UploadFragment : Fragment() {

	private lateinit var imageView: ImageView
	private lateinit var descriptionEditText: EditText
	private lateinit var progressBar: ProgressBar
	private lateinit var uploadButton: LinearLayout
	private lateinit var cameraButton: LinearLayout
	private lateinit var galleryButton: LinearLayout
	private var imageUri: Uri? = null

	private val storage = FirebaseStorage.getInstance()
	private val db = FirebaseFirestore.getInstance()
	private val auth = FirebaseAuth.getInstance()
	private val REQUEST_CAMERA = 1
	private val REQUEST_GALLERY = 2
	private val REQUEST_CROP = 3

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_upload, container, false)

		imageView = view.findViewById(R.id.imageView)
		descriptionEditText = view.findViewById(R.id.descriptionEditText)
		progressBar = view.findViewById(R.id.progressBar)
		uploadButton = view.findViewById(R.id.uploadButton)
		cameraButton = view.findViewById(R.id.cameraButton)
		galleryButton = view.findViewById(R.id.galleryButton)

		cameraButton.setOnClickListener { openCamera() }
		galleryButton.setOnClickListener { openGallery() }
		uploadButton.setOnClickListener { uploadImage() }

		return view
	}

	private fun openCamera() {
		val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
		if (intent.resolveActivity(requireActivity().packageManager) != null) {
			startActivityForResult(intent, REQUEST_CAMERA)
		}
	}

	private fun openGallery() {
		val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
		intent.type = "image/*"
		startActivityForResult(intent, REQUEST_GALLERY)
	}

	private fun startCrop(uri: Uri) {
		val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "croppedImage.jpg"))
		val options = UCrop.Options()
		options.setFreeStyleCropEnabled(true)
		UCrop.of(uri, destinationUri)
			.withOptions(options)
			.withAspectRatio(1f, 1f)
			.withMaxResultSize(1000, 1000)
			.start(requireContext(), this, REQUEST_CROP)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == Activity.RESULT_OK) {
			when (requestCode) {
				REQUEST_CAMERA -> {
					val imageBitmap = data?.extras?.get("data") as Bitmap
					val tempUri = getImageUri(imageBitmap)
					startCrop(tempUri)
				}
				REQUEST_GALLERY -> {
					val selectedImageUri = data?.data
					if (selectedImageUri != null) {
						startCrop(selectedImageUri)
					}
				}
				REQUEST_CROP -> {
					val resultUri = UCrop.getOutput(data!!)
					if (resultUri != null) {
						imageUri = resultUri
						imageView.setImageURI(resultUri)
					}
				}
			}
		}
	}

	private fun getImageUri(bitmap: Bitmap): Uri {
		val path = MediaStore.Images.Media.insertImage(requireContext().contentResolver, bitmap, "TempImage", null)
		return Uri.parse(path)
	}

	private fun uploadImage() {
		if (imageUri == null) {
			Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
			return
		}

		val description = descriptionEditText.text.toString().trim()

		progressBar.visibility = View.VISIBLE
		uploadButton.isEnabled = false

		val userId = auth.currentUser?.uid ?: return
		val postId = UUID.randomUUID().toString()
		val storageRef = storage.reference.child("images/$postId.jpg")

		storageRef.putFile(imageUri!!)
			.addOnSuccessListener {
				storageRef.downloadUrl.addOnSuccessListener { uri ->
					val post = Post(
						id = postId,
						userId = userId,
						profileImageUrl = auth.currentUser?.photoUrl.toString(),
						profileName = auth.currentUser?.displayName ?: "Anonymous",
						imageUrl = uri.toString(),
						description = description,
						likes = 0,
						timestamp = System.currentTimeMillis()
					)
					db.collection("posts").document(postId).set(post)
						.addOnSuccessListener {
							progressBar.visibility = View.GONE
							uploadButton.isEnabled = true
							Toast.makeText(context, "Post uploaded", Toast.LENGTH_SHORT).show()
							descriptionEditText.text.clear()
							// Redirect to home screen
							val intent = Intent(context, MainActivity::class.java)
							intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
							startActivity(intent)
							requireActivity().finish()
						}
						.addOnFailureListener { e ->
							progressBar.visibility = View.GONE
							uploadButton.isEnabled = true
							Toast.makeText(context, "Failed to upload post ${e.message}", Toast.LENGTH_SHORT).show()
						}
				}
			}
			.addOnFailureListener { e ->
				progressBar.visibility = View.GONE
				uploadButton.isEnabled = true
				Toast.makeText(context, "Failed to upload image ${e.message}", Toast.LENGTH_SHORT).show()
			}
	}


	companion object {
		private const val REQUEST_CODE_PERMISSIONS = 10
		private val REQUIRED_PERMISSIONS = arrayOf(
			android.Manifest.permission.CAMERA,
			android.Manifest.permission.READ_EXTERNAL_STORAGE,
			android.Manifest.permission.WRITE_EXTERNAL_STORAGE
		)
	}

	private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
		ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
	}

	override fun onRequestPermissionsResult(
		requestCode: Int, permissions: Array<out String>, grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == REQUEST_CODE_PERMISSIONS) {
			if (allPermissionsGranted()) {
				// Permissions granted, open camera or gallery based on the button click
			} else {
				// If permissions are not granted, you can show a message
			}
		}
	}
}
