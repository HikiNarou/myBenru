package com.mybenru.app.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mybenru.app.R
import com.mybenru.app.databinding.ActivityMainBinding
import com.mybenru.app.extension.observeWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Main activity hosting navigation and fragments
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle splash screen transition
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Timber.d("MainActivity created")

        setupNavigation()
        setupBottomNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up bottom navigation with nav controller
        binding.bottomNavigation.setupWithNavController(navController)

        // Listen for navigation destination changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Show bottom navigation only for main tabs
            val shouldShowBottomNav = when (destination.id) {
                R.id.homeFragment,
                R.id.libraryFragment,
                R.id.exploreFragment -> true
                else -> false
            }

            if (shouldShowBottomNav) {
                binding.bottomNavigation.visibility = View.VISIBLE
            } else {
                binding.bottomNavigation.visibility = View.GONE
            }
        }
    }

    private fun setupBottomNavigation() {
        // Handle bottom navigation item selection
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    navigateToTabIfDifferent(R.id.homeFragment)
                    true
                }
                R.id.libraryFragment -> {
                    navigateToTabIfDifferent(R.id.libraryFragment)
                    true
                }
                R.id.exploreFragment -> {
                    navigateToTabIfDifferent(R.id.exploreFragment)
                    true
                }
                else -> false
            }
        }

        // Handle reselection to navigate to the top of the fragment stack
        binding.bottomNavigation.setOnItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment,
                R.id.libraryFragment,
                R.id.exploreFragment -> {
                    // Pop back stack to the start destination of the current navigation graph
                    val navController = navController
                    val navGraph = navController.graph
                    navController.popBackStack(navGraph.startDestinationId, false)
                }
            }
        }
    }

    /**
     * Navigate to the tab only if it's different from the current one
     */
    private fun navigateToTabIfDifferent(destinationId: Int) {
        val currentDestinationId = navController.currentDestination?.id
        if (currentDestinationId != destinationId) {
            navController.navigate(destinationId)
        }
    }

    /**
     * Handle back button press
     */
    override fun onBackPressed() {
        if (!handleBackPressed()) {
            super.onBackPressed()
        }
    }

    /**
     * Custom back handling behavior
     * @return true if back is handled, false otherwise
     */
    private fun handleBackPressed(): Boolean {
        val currentDestinationId = navController.currentDestination?.id

        // If we're in one of the main tabs and not at home tab, navigate to home tab
        if (currentDestinationId == R.id.libraryFragment || currentDestinationId == R.id.exploreFragment) {
            navController.navigate(R.id.homeFragment)
            return true
        }

        return false
    }

    /**
     * Handle deep link intents
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }
}