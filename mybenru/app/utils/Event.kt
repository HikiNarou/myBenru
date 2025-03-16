package com.mybenru.app.utils

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 *
 * This is used for one-time events that should not be triggered again if the observer
 * is reattached or configuration changes.
 */
class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}