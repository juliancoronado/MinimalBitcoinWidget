package com.jcoronado.minimalbitcoinwidget.data

/**
 * A generic class that holds a value with its loading status.
 *
 * @param T The type of data contained within the resource.
 */
sealed class Resource<out T> {
    /**
     * Represents a successful state containing data.
     */
    data class Success<out T>(val data: T) : Resource<T>()

    /**
     * Represents an error state containing a user-readable error message and optional cause.
     */
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()

    /**
     * Represents an ongoing loading operation.
     */
    data object Loading : Resource<Nothing>()
}
