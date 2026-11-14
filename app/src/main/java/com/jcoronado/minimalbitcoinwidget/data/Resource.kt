package com.jcoronado.minimalbitcoinwidget.data

import android.content.Context
import com.jcoronado.minimalbitcoinwidget.R
import java.io.IOException

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
     * Represents an error state containing a diagnostic error message and optional cause.
     */
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>() {
        /**
         * Returns a localized, user-facing error message suitable for dashboard and widget UI.
         */
        fun getUserFacingMessage(context: Context): String {
            return if (cause is IOException) {
                context.getString(R.string.error_network)
            } else {
                context.getString(R.string.error_update_price)
            }
        }
    }

    /**
     * Represents an ongoing loading operation.
     */
    data object Loading : Resource<Nothing>()
}
