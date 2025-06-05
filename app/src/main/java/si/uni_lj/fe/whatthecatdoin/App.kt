package si.uni_lj.fe.whatthecatdoin

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.android.material.color.DynamicColors

class App : Application() {
        override fun onCreate() {
                super.onCreate()
                FirebaseApp.initializeApp(this)
                DynamicColors.applyToActivitiesIfAvailable(this)
        }
}
