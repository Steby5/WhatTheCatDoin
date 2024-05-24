package si.uni_lj.fe.whatthecatdoin.ui.upload

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import si.uni_lj.fe.whatthecatdoin.Post
import si.uni_lj.fe.whatthecatdoin.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UploadFragment : Fragment() {

	private lateinit var imageView: ImageView
	private lateinit var tag1EditText: EditText
	private lateinit var tag2EditText: EditText
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
		tag1EditText = view.findViewById(R.id.tag1EditText)
		tag2EditText = view.findViewById(R.id.tag2EditText)
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

		val tag1 = tag1EditText.text.toString().trim()
		val tag2 = tag2EditText.text.toString().trim()

		progressBar.visibility = View.VISIBLE
		uploadButton.isEnabled = false

		val userId = auth.currentUser?.uid ?: return
		val storageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")

		storageRef.putFile(imageUri!!)
			.addOnSuccessListener {
				storageRef.downloadUrl.addOnSuccessListener { uri ->
					val post = Post(
						userId = userId,
						profileName = auth.currentUser?.displayName ?: "Anonymous",
						imageUrl = uri.toString(),
						tags = listOfNotNull(tag1.takeIf { it.isNotEmpty() }, tag2.takeIf { it.isNotEmpty() }),
						likes = 0,
						timestamp = System.currentTimeMillis()
					)
					db.collection("posts").add(post)
						.addOnSuccessListener {
							progressBar.visibility = View.GONE
							uploadButton.isEnabled = true
							Toast.makeText(context, "Post uploaded", Toast.LENGTH_SHORT).show()
							imageView.setImageResource(R.drawable.image_placeholder)
							tag1EditText.text.clear()
							tag2EditText.text.clear()
						}
						.addOnFailureListener {
							progressBar.visibility = View.GONE
							uploadButton.isEnabled = true
							Toast.makeText(context, "Failed to upload post", Toast.LENGTH_SHORT).show()
						}
				}
			}
			.addOnFailureListener {
				progressBar.visibility = View.GONE
				uploadButton.isEnabled = true
				Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
			}
	}
}
