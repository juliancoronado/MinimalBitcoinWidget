package com.jcoronado.minimalbitcoinwidget.widgets

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text

class TestWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) GlanceTheme.colors
                else GlanceColorScheme.colors
            ) {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        Scaffold {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Sample Widget")
            }
        }
    }
}