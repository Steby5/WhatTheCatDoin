package si.uni_lj.fe.whatthecatdoin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import si.uni_lj.fe.whatthecatdoin.ui.updateExistingPosts

class MainActivity : AppCompatActivity() {

	private lateinit var navController: NavController

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val navHostFragment = supportFragmentManager
			.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
		navController = navHostFragment.navController

		val bottomNavigationView: BottomNavigationView = findViewById(R.id.nav_view)
		NavigationUI.setupWithNavController(bottomNavigationView, navController)

		updateExistingPosts()
	}
}
