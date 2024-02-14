package com.jcoronado.minimalbitcoinwidget.minimalbitcoinwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.text.Text

class MyAppWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // in this method, load data needed to render the appwidget

        provideContent { MyContent() }
    }

    @Composable
    private fun MyContent() {
        GlanceTheme {
            Column {
                Text("Hello, world!")
                Text("I'm a widget!")
            }
        }
    }
}