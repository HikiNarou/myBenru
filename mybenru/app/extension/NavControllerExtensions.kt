package com.mybenru.app.extension

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import timber.log.Timber

/**
 * Safely navigate with NavController, catching any exceptions
 */
fun NavController.safeNavigate(directions: NavDirections) {
    try {
        navigate(directions)
    } catch (e: Exception) {
        Timber.e(e, "Navigation failed: ${e.message}")
    }
}

/**
 * Safely pop back stack, catching any exceptions
 */
fun NavController.safePopBackStack(): Boolean {
    return try {
        popBackStack()
    } catch (e: Exception) {
        Timber.e(e, "Pop back stack failed: ${e.message}")
        false
    }
}

/**
 * Safely navigate up, catching any exceptions
 */
fun NavController.safeNavigateUp(): Boolean {
    return try {
        navigateUp()
    } catch (e: Exception) {
        Timber.e(e, "Navigate up failed: ${e.message}")
        false
    }
}