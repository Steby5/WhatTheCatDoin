package si.uni_lj.fe.whatthecatdoin

import android.os.Parcel
import android.os.Parcelable

data class Post(
	var id: String = "",
	val userId: String = "",
	val profileName: String = "",
	val imageUrl: String = "",
	val description: String = "",
	var likes: Int = 0,
	val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
	constructor(parcel: Parcel) : this(
		parcel.readString() ?: "",
		parcel.readString() ?: "",
		parcel.readString() ?: "",
		parcel.readString() ?: "",
		parcel.readString() ?: "",
		parcel.readInt(),
		parcel.readLong()
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(id)
		parcel.writeString(userId)
		parcel.writeString(profileName)
		parcel.writeString(imageUrl)
		parcel.writeString(description)
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
