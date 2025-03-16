package com.mybenru.app.extension

import android.content.Context
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

/**
 * Extension to safely navigate with NavController
 */
fun NavController.safeNavigate(direction: NavDirections) {
    try {
        navigate(direction)
    } catch (e: Exception) {
        Timber.e(e, "Navigation error")
    }
}

/**
 * Extension to safely navigate with NavController to destination ID
 */
fun NavController.safeNavigate(
    @IdRes resId: Int,
    args: Bundle? = null,
    navOptions: androidx.navigation.NavOptions? = null
) {
    try {
        navigate(resId, args, navOptions)
    } catch (e: Exception) {
        Timber.e(e, "Navigation error")
    }
}

/**
 * Extension to safely navigate up with NavController
 */
fun NavController.safeNavigateUp(): Boolean {
    return try {
        navigateUp()
    } catch (e: Exception) {
        Timber.e(e, "Navigation up error")
        false
    }
}

/**
 * Extension to safely find NavController
 */
fun Fragment.findNavControllerSafely(): NavController? {
    return try {
        findNavController()
    } catch (e: Exception) {
        Timber.e(e, "Find NavController error")
        null
    }
}

/**
 * Extension function to observe LiveData only when the lifecycle is at least in STARTED state.
 * Untuk menghindari konflik dengan definisi observeWithLifecycle di file lain, fungsi ini diganti namanya.
 */
fun <T> LiveData<T>.observeWithCustomLifecycle(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    observer: (T) -> Unit
) {
    observe(lifecycleOwner, LifecycleAwareObserver(lifecycleOwner, minActiveState, observer))
}

/**
 * Extension function to collect Flow only when the lifecycle is at least in STARTED state
 */
suspend fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit
) {
    flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
        .collectLatest { collector(it) }
}

/**
 * Extension function to get a view model from a parent Fragment
 */
inline fun <reified T : androidx.lifecycle.ViewModel> Fragment.getParentViewModel(): T {
    return androidx.lifecycle.ViewModelProvider(requireParentFragment())[T::class.java]
}

/**
 * Extension function to get a view model from the activity
 */
inline fun <reified T : androidx.lifecycle.ViewModel> Fragment.getActivityViewModel(): T {
    return androidx.lifecycle.ViewModelProvider(requireActivity())[T::class.java]
}

/**
 * Extension function to get a shared view model from the navigation graph
 */
inline fun <reified T : androidx.lifecycle.ViewModel> Fragment.getNavGraphViewModel(@IdRes navGraphId: Int): T {
    return androidx.navigation.fragment.NavHostFragment.findNavController(this)
        .getBackStackEntry(navGraphId).let {
            androidx.lifecycle.ViewModelProvider(it)[T::class.java]
        }
}

/**
 * Extension to get a string from resources without nullability concerns
 */
fun Fragment.requireString(resId: Int): String = requireContext().getString(resId)

/**
 * Extension to get a string from resources with formatting arguments
 */
fun Fragment.requireString(resId: Int, vararg formatArgs: Any): String {
    return requireContext().getString(resId, *formatArgs)
}

/**
 * LifecycleAwareObserver that only calls the observer when the lifecycle is in the appropriate state
 */
private class LifecycleAwareObserver<T>(
    private val lifecycleOwner: LifecycleOwner,
    private val minActiveState: Lifecycle.State,
    private val observer: (T) -> Unit
) : Observer<T> {
    override fun onChanged(value: T) {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(minActiveState)) {
            observer(value)
        }
    }
}
