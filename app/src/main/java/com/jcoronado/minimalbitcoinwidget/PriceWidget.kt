package com.jcoronado.minimalbitcoinwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text

class PriceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            WidgetContent()
        }
    }

    @Composable
    @Preview
    private fun WidgetContent() {
        GlanceTheme{
            Box(
                modifier = GlanceModifier.clickable {
                    actionRunCallback<RefreshPriceAction>()
                }
            ) {
                Column(
                    modifier = GlanceModifier.height(90.dp).fillMaxWidth().background(Color.White),
                    verticalAlignment = Alignment.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "BTC Price", modifier = GlanceModifier.padding(12.dp))
                    Row(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Hello, world!",
                        )
                    }
                }
            }
        }
    }
}

class RefreshPriceAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        PriceWidget().updateAll(context)
    }
}