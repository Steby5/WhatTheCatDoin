package si.uni_lj.fe.whatthecatdoin

import android.os.Parcel
import android.os.Parcelable

data class Post(
	var id: String = "",
	val profileName: String = "",
	val imageResId: Int? = null,
	val imageUrl: String? = null,
	val tags: List<String> = listOf(),
	var likes: Int = 0,
	val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
	constructor(parcel: Parcel) : this(
		parcel.readString() ?: "",
		parcel.readString() ?: "",
		parcel.readValue(Int::class.java.classLoader) as? Int,
		parcel.readString(),
		parcel.createStringArrayList() ?: listOf(),
		parcel.readInt(),
		parcel.readLong()
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(id)
		parcel.writeString(profileName)
		parcel.writeValue(imageResId)
		parcel.writeString(imageUrl)
		parcel.writeStringList(tags)
		parcel.writeInt(likes)
		parcel.writeLong(timestamp)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<Post> {
		override fun createFromParcel(parcel: Parcel): Post {
			return Post(parcel)
		}

		override fun newArray(size: Int): Array<Post?> {
			return arrayOfNulls(size)
		}
	}
}
