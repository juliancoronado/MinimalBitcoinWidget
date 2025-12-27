package com.jcoronado.minimalbitcoinwidget.widgets

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.jcoronado.minimalbitcoinwidget.R

class TestWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // TODO - load data here before drawing the widget (?)

        provideContent {
            WidgetContent()
        }
    }

    @Composable
    private fun WidgetContent(changePercentage : Double = 0.023) {

        // widgetBackground is more in line with dynamic colors (deeper color)
        // surface and background are the same values (whiter color)

        GlanceTheme(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) GlanceTheme.colors
            else GlanceColorScheme.colors
        ) {
            Scaffold(
                backgroundColor = GlanceTheme.colors.surface
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TODO - replace hard coded values with actual data
                    TitleUI()
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    PriceUi()
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    ChangeUI(changePercentage = changePercentage)
                }
            }
        }
    }

    @Composable
    private fun TitleUI() {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.rounded_currency_bitcoin_24),
                contentDescription = "Bitcoin Icon",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                modifier = GlanceModifier.size(14.dp).padding(top = 1.dp)
            )
            Text(
                "/ USD", style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp
                )
            )
        }
    }

    @Composable
    private fun PriceUi() {
        Text(
            "$123,456.78",
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontFamily = FontFamily.SansSerif,
                fontSize = 24.sp
            )
        )
    }

    @Composable
    private fun ChangeUI(changePercentage: Double) {
        // determine the icon and color based on the changePercentage value
        val (iconRes, color) = if (changePercentage > 0) {
            R.drawable.rounded_trending_up_24 to GlanceTheme.colors.tertiary
        } else {
            R.drawable.rounded_trending_down_24 to GlanceTheme.colors.error
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color),
                modifier = GlanceModifier.size(16.dp).padding(top = 2.dp)
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = String.format("%.2f%%", changePercentage * 100),
                style = TextStyle(
                    color = GlanceTheme.colors.secondary,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp
                )
            )
        }
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataPos() {
        // use default changePercentage value for this preview
        WidgetContent()
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataNeg() {
        WidgetContent(changePercentage = -0.0167)
    }
}