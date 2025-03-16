package com.mybenru.domain.exception

/**
 * Base exception class for domain layer exceptions
 */
abstract class DomainException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when a requested resource is not found
 */
class ResourceNotFoundException(
    message: String = "The requested resource was not found",
    val resourceId: String? = null,
    val resourceType: String? = null,
    cause: Throwable? = null
) : DomainException(message, cause)

/**
 * Exception thrown when a network-related error occurs
 */
class NetworkException(
    message: String = "A network error occurred",
    val code: Int? = null,
    val url: String? = null,
    cause: Throwable? = null
) : DomainException(message, cause)

/**
 * Exception thrown when there's a parsing error
 */
class ParsingException(
    message: String = "Failed to parse content",
    val source: String? = null,
    val content: String? = null,
    cause: Throwable? = null
) : DomainException(message, cause)

/**
 * Exception thrown when a source is not supported
 */
class UnsupportedSourceException(
    val sourceId: String,
    message: String = "Source with ID $sourceId is not supported",
    cause: Throwable? = null
) : DomainException(message, cause)

/**
 * Exception thrown when there's an issue with content download
 */
class DownloadException(
    message: String = "Failed to download content",
    val novelId: String? = null,
    val chapterId: String? = null,
    cause: Throwable? = null
) : DomainException(message, cause)

/**
 * Exception thrown when there's an authentication error
 */
class AuthenticationException(
    message: String = "Authentication failed",
    val source: String? = null,
    cause: Throwable? = null
) : DomainException(message, cause)

/**
 * Exception thrown when there's a rate limit issue
 */
class RateLimitException(
    message: String = "Rate limit exceeded",
    val source: String? = null,
    val retryAfterSeconds: Int? = null,
    cause: Throwable? = null
) : DomainException(message, cause)

/**
 * Exception thrown when content is unavailable
 */
class ContentUnavailableException(
    message: String = "Content is unavailable",
    val novelId: String? = null,
    val chapterId: String? = null,
    cause: Throwable? = null
) : DomainException(message, cause)

/**
 * Exception thrown when a source extension is missing
 */
class MissingExtensionException(
    val sourceId: String,
    message: String = "Extension for source $sourceId is not installed",
    cause: Throwable? = null
) : DomainException(message, cause)