package si.uni_lj.fe.whatthecatdoin.ui.upload

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
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
	private lateinit var uploadButton: Button
	private lateinit var cameraButton: Button
	private lateinit var galleryButton: Button

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
		cameraButton = view.findViewById(R.id.cameraButton)
		galleryButton = view.findViewById(R.id.galleryButton)

		auth = FirebaseAuth.getInstance()
		db = FirebaseFirestore.getInstance()
		storage = FirebaseStorage.getInstance()

		// Apply character limit filters
		val filters = arrayOf<InputFilter>(CharacterLimitFilter(TAG_CHAR_LIMIT))
		tag1EditText.filters = filters
		tag2EditText.filters = filters

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
		val readStoragePermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
		val writeStoragePermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
		return cameraPermission == PackageManager.PERMISSION_GRANTED &&
				readStoragePermission == PackageManager.PERMISSION_GRANTED &&
				writeStoragePermission == PackageManager.PERMISSION_GRANTED
	}

	private fun requestPermissions() {
		requestPermissions(arrayOf(
			Manifest.permission.CAMERA,
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
		), PERMISSION_REQUEST_CODE)
	}

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
			val storageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
			val baos = ByteArrayOutputStream()
			selectedImage?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
			val data = baos.toByteArray()

			storageRef.putBytes(data)
				.addOnSuccessListener { taskSnapshot ->
					storageRef.downloadUrl.addOnSuccessListener { uri ->
						val docRef = db.collection("posts").document()
						val post = Post(
							id = docRef.id,
							profileName = userName,
							imageUrl = uri.toString(),
							tags = tags,
							likes = 0,
							timestamp = System.currentTimeMillis()
						)

						docRef.set(post)
							.addOnSuccessListener {
								Toast.makeText(context, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
								findNavController().navigate(R.id.navigation_home)
							}
							.addOnFailureListener { e ->
								Toast.makeText(context, "Failed to upload post: ${e.message}", Toast.LENGTH_SHORT).show()
							}
					}
				}
				.addOnFailureListener { e ->
					Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
				}
		} else {
			Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == Activity.RESULT_OK) {
			when (requestCode) {
				CAMERA_REQUEST_CODE -> {
					selectedImage = data?.extras?.get("data") as Bitmap
					imageView.setImageBitmap(selectedImage)
				}
				GALLERY_REQUEST_CODE -> {
					val imageUri = data?.data
					imageView.setImageURI(imageUri)
					selectedImage = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
				}
			}
		}
	}
}
