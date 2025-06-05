package si.uni_lj.fe.whatthecatdoin

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
        @Test
        fun addition_isCorrect() {
                assertEquals(4, 2 + 2)
        }

        @Test
        fun postParceling_roundTripPreservesData() {
                val original = Post(
                        id = "1",
                        userId = "user",
                        profileName = "Profile",
                        imageUrl = "http://example.com/image.jpg",
                        description = "desc",
                        likes = 5,
                        timestamp = 1000L
                )

                val parcel = android.os.Parcel.obtain()
                original.writeToParcel(parcel, 0)
                parcel.setDataPosition(0)
                val recreated = Post.CREATOR.createFromParcel(parcel)
                parcel.recycle()

                assertEquals(original, recreated)
        }
}