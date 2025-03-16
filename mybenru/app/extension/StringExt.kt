package com.mybenru.app.extension

import android.util.Patterns
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import timber.log.Timber
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.Normalizer
import java.util.*

/**
 * Check if string is empty
 */
fun String?.isNotNullOrEmpty(): Boolean {
    return !this.isNullOrEmpty()
}

/**
 * Check if string is blank
 */
fun String?.isNotNullOrBlank(): Boolean {
    return !this.isNullOrBlank()
}

/**
 * Capitalize first letter of string
 */
fun String.capitalizeFirstLetter(): String {
    if (this.isEmpty()) return this
    return this[0].uppercase() + this.substring(1)
}

/**
 * Convert HTML to plain text
 */
fun String.htmlToPlainText(): String {
    return try {
        Jsoup.clean(this, Safelist.none())
    } catch (e: Exception) {
        Timber.e(e, "Error converting HTML to plain text")
        this
    }
}

/**
 * Remove HTML tags
 */
fun String.removeHtmlTags(): String {
    return try {
        this.replace(Regex("<[^>]*>"), "")
    } catch (e: Exception) {
        Timber.e(e, "Error removing HTML tags")
        this
    }
}

/**
 * Check if string is valid URL
 */
fun String.isValidUrl(): Boolean {
    return Patterns.WEB_URL.matcher(this).matches()
}

/**
 * Check if string is valid email
 */
fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * URL encode string
 */
fun String.urlEncode(): String {
    return URLEncoder.encode(this, "UTF-8")
}

/**
 * Truncate string to a specific length
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    if (this.length <= maxLength) return this

    return this.substring(0, maxLength - ellipsis.length) + ellipsis
}

/**
 * Generate MD5 hash
 */
fun String.md5(): String {
    val md5 = MessageDigest.getInstance("MD5")
    val digest = md5.digest(this.toByteArray())
    return digest.joinToString("") { String.format("%02x", it) }
}

/**
 * Generate SHA-256 hash
 */
fun String.sha256(): String {
    val sha256 = MessageDigest.getInstance("SHA-256")
    val digest = sha256.digest(this.toByteArray())
    return digest.joinToString("") { String.format("%02x", it) }
}

/**
 * Normalize string (remove accents, special characters, etc.)
 */
fun String.normalize(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return normalized.replace(Regex("[^\\p{ASCII}]"), "")
}

/**
 * Split string by delimiter with a maximum limit of substrings
 */
fun String.smartSplit(delimiter: String, limit: Int = 0): List<String> {
    val result = mutableListOf<String>()
    val parts = this.split(delimiter, limit = if (limit > 0) limit else Int.MAX_VALUE)

    for (part in parts) {
        if (part.isNotBlank()) {
            result.add(part.trim())
        }
    }

    return result
}

/**
 * Extract numbers from string
 */
fun String.extractNumbers(): String {
    return this.replace(Regex("[^0-9]"), "")
}

/**
 * Extract text between two delimiters
 */
fun String.extractBetween(start: String, end: String): String? {
    val startIndex = this.indexOf(start)
    if (startIndex < 0) return null

    val endIndex = this.indexOf(end, startIndex + start.length)
    if (endIndex < 0) return null

    return this.substring(startIndex + start.length, endIndex)
}

/**
 * Check if string contains any of the words
 */
fun String.containsAnyOf(vararg words: String, ignoreCase: Boolean = true): Boolean {
    for (word in words) {
        if (this.contains(word, ignoreCase = ignoreCase)) {
            return true
        }
    }
    return false
}

/**
 * Check if string contains all of the words
 */
fun String.containsAllOf(vararg words: String, ignoreCase: Boolean = true): Boolean {
    for (word in words) {
        if (!this.contains(word, ignoreCase = ignoreCase)) {
            return false
        }
    }
    return true
}

/**
 * Convert string to slug (URL-friendly string)
 */
fun String.toSlug(): String {
    return this.lowercase()
        .replace(Regex("[^a-z0-9\\s-]"), "")  // Remove non-alphanumeric chars
        .replace(Regex("\\s+"), "-")          // Replace spaces with hyphens
        .replace(Regex("-+"), "-")            // Replace multiple hyphens with single hyphen
        .trim('-')                            // Trim hyphens from start and end
}

/**
 * Convert string to title case
 */
fun String.toTitleCase(): String {
    if (this.isEmpty()) return this

    val words = this.split("\\s+".toRegex())
    val result = StringBuilder()

    for (word in words) {
        if (word.isEmpty()) continue

        result.append(word[0].uppercase())
        if (word.length > 1) {
            result.append(word.substring(1).lowercase())
        }
        result.append(" ")
    }

    return result.toString().trim()
}

/**
 * Reverse string
 */
fun String.reverse(): String {
    return this.reversed()
}

/**
 * Count occurrences of substring
 */
fun String.countOccurrences(substring: String, ignoreCase: Boolean = false): Int {
    if (substring.isEmpty()) return 0

    var count = 0
    var index = 0

    while (index != -1) {
        index = this.indexOf(substring, index, ignoreCase = ignoreCase)
        if (index != -1) {
            count++
            index += substring.length
        }
    }

    return count
}