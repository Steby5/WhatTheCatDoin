package si.uni_lj.fe.whatthecatdoin.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

	private val _text = MutableLiveData<String>().apply {
		value = "What The Cat Doin'"
	}
	val text: LiveData<String> = _text
}