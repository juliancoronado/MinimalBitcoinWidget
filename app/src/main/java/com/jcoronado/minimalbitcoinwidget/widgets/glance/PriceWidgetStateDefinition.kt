package com.jcoronado.minimalbitcoinwidget.widgets.glance

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

private const val FILE_NAME = "price_widget_state.json"

private val Context.dataStore by dataStore(
    fileName = FILE_NAME,
    serializer = PriceWidgetStateDefinition.PriceWidgetStateSerializer
)

object PriceWidgetStateDefinition : GlanceStateDefinition<PriceWidgetState> {

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<PriceWidgetState> {
        return context.dataStore
    }

    override fun getLocation(
        context: Context,
        fileKey: String
    ): File {
        return context.dataStoreFile(FILE_NAME)
    }

    /**
     * Custom serializer for PriceWidgetState using Kotlin Serialization.
     */
    object PriceWidgetStateSerializer : Serializer<PriceWidgetState> {
        override val defaultValue: PriceWidgetState = PriceWidgetState.Loading

        override suspend fun readFrom(input: InputStream): PriceWidgetState {
            return try {
                Json.decodeFromString(
                    PriceWidgetState.serializer(),
                    input.readBytes().decodeToString()
                )
            } catch (_: SerializationException) {
                defaultValue
            }
        }

        override suspend fun writeTo(t: PriceWidgetState, output: OutputStream) {
            output.use {
                it.write(
                    Json.encodeToString(PriceWidgetState.serializer(), t).toByteArray()
                )
            }
        }
    }
}
