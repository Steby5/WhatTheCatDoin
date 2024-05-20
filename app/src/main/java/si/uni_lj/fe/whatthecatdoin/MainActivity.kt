package si.uni_lj.fe.whatthecatdoin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val navView: BottomNavigationView = findViewById(R.id.nav_view)
		val navHostFragment = supportFragmentManager
			.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
		val navController = navHostFragment.navController

		NavigationUI.setupWithNavController(navView, navController)
	}
}
