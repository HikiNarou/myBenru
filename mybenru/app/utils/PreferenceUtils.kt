package com.mybenru.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Utility class for handling SharedPreferences with delegated properties
 */
object PreferenceUtils {

    /**
     * Get default SharedPreferences
     */
    fun getDefaultPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    /**
     * Get SharedPreferences with specific name
     */
    fun getPreferences(context: Context, name: String): SharedPreferences {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    /**
     * Get string from SharedPreferences
     */
    fun SharedPreferences.getString(key: String, defaultValue: String = ""): String {
        return getString(key, defaultValue) ?: defaultValue
    }

    /**
     * Put string in SharedPreferences
     */
    fun SharedPreferences.putString(key: String, value: String) {
        edit { putString(key, value) }
    }

    /**
     * Get int from SharedPreferences
     */
    fun SharedPreferences.getInt(key: String, defaultValue: Int = 0): Int {
        return getInt(key, defaultValue)
    }

    /**
     * Put int in SharedPreferences
     */
    fun SharedPreferences.putInt(key: String, value: Int) {
        edit { putInt(key, value) }
    }

    /**
     * Get boolean from SharedPreferences
     */
    fun SharedPreferences.getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getBoolean(key, defaultValue)
    }

    /**
     * Put boolean in SharedPreferences
     */
    fun SharedPreferences.putBoolean(key: String, value: Boolean) {
        edit { putBoolean(key, value) }
    }

    /**
     * Get long from SharedPreferences
     */
    fun SharedPreferences.getLong(key: String, defaultValue: Long = 0L): Long {
        return getLong(key, defaultValue)
    }

    /**
     * Put long in SharedPreferences
     */
    fun SharedPreferences.putLong(key: String, value: Long) {
        edit { putLong(key, value) }
    }

    /**
     * Get float from SharedPreferences
     */
    fun SharedPreferences.getFloat(key: String, defaultValue: Float = 0f): Float {
        return getFloat(key, defaultValue)
    }

    /**
     * Put float in SharedPreferences
     */
    fun SharedPreferences.putFloat(key: String, value: Float) {
        edit { putFloat(key, value) }
    }

    /**
     * Remove key from SharedPreferences
     */
    fun SharedPreferences.remove(key: String) {
        edit { remove(key) }
    }

    /**
     * Clear all SharedPreferences
     */
    fun SharedPreferences.clear() {
        edit { clear() }
    }

    /**
     * Get object from SharedPreferences using Gson
     */
    inline fun <reified T> SharedPreferences.getObject(key: String, defaultValue: T? = null): T? {
        val json = getString(key, "")
        return if (json.isEmpty()) {
            defaultValue
        } else {
            try {
                Gson().fromJson(json, object : TypeToken<T>() {}.type)
            } catch (e: Exception) {
                Timber.e(e, "Error parsing JSON from preferences")
                defaultValue
            }
        }
    }

    /**
     * Put object in SharedPreferences using Gson
     */
    inline fun <reified T> SharedPreferences.putObject(key: String, value: T?) {
        if (value == null) {
            remove(key)
        } else {
            try {
                val json = Gson().toJson(value)
                putString(key, json)
            } catch (e: Exception) {
                Timber.e(e, "Error serializing object to JSON for preferences")
            }
        }
    }

    /**
     * SharedPreferences delegates for property delegation
     */
    class StringPreference(
        private val preferences: SharedPreferences,
        private val key: String,
        private val defaultValue: String = ""
    ) : ReadWriteProperty<Any, String> {
        override fun getValue(thisRef: Any, property: KProperty<*>): String {
            return preferences.getString(key, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
            preferences.edit { putString(key, value) }
        }
    }

    class IntPreference(
        private val preferences: SharedPreferences,
        private val key: String,
        private val defaultValue: Int = 0
    ) : ReadWriteProperty<Any, Int> {
        override fun getValue(thisRef: Any, property: KProperty<*>): Int {
            return preferences.getInt(key, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
            preferences.edit { putInt(key, value) }
        }
    }

    class BooleanPreference(
        private val preferences: SharedPreferences,
        private val key: String,
        private val defaultValue: Boolean = false
    ) : ReadWriteProperty<Any, Boolean> {
        override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
            return preferences.getBoolean(key, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
            preferences.edit { putBoolean(key, value) }
        }
    }

    class LongPreference(
        private val preferences: SharedPreferences,
        private val key: String,
        private val defaultValue: Long = 0L
    ) : ReadWriteProperty<Any, Long> {
        override fun getValue(thisRef: Any, property: KProperty<*>): Long {
            return preferences.getLong(key, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
            preferences.edit { putLong(key, value) }
        }
    }

    class FloatPreference(
        private val preferences: SharedPreferences,
        private val key: String,
        private val defaultValue: Float = 0f
    ) : ReadWriteProperty<Any, Float> {
        override fun getValue(thisRef: Any, property: KProperty<*>): Float {
            return preferences.getFloat(key, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Float) {
            preferences.edit { putFloat(key, value) }
        }
    }

    class NullableStringPreference(
        private val preferences: SharedPreferences,
        private val key: String
    ) : ReadWriteProperty<Any, String?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): String? {
            return preferences.getString(key, null)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
            preferences.edit {
                if (value == null) {
                    remove(key)
                } else {
                    putString(key, value)
                }
            }
        }
    }

    class ObjectPreference<T>(
        private val preferences: SharedPreferences,
        private val key: String,
        private val defaultValue: T? = null,
        private val clazz: Class<T>
    ) : ReadWriteProperty<Any, T?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T? {
            val json = preferences.getString(key, null)
            return if (json == null) {
                defaultValue
            } else {
                try {
                    Gson().fromJson(json, clazz)
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing JSON from preferences")
                    defaultValue
                }
            }
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
            preferences.edit {
                if (value == null) {
                    remove(key)
                } else {
                    putString(key, Gson().toJson(value))
                }
            }
        }
    }
}