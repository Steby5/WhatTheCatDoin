package si.uni_lj.fe.whatthecatdoin

import android.text.InputFilter
import android.text.Spanned

class CharacterLimitFilter(private val max: Int) : InputFilter {

	override fun filter(
		source: CharSequence?,
		start: Int,
		end: Int,
		dest: Spanned?,
		dstart: Int,
		dend: Int
	): CharSequence? {
		val keep = max - (dest?.length ?: 0) + (dend - dstart)
		return if (keep <= 0) {
			""  // No characters can be added
		} else if (keep >= end - start) {
			null  // Keep original
		} else {
			source?.subSequence(start, start + keep)
		}
	}
}
