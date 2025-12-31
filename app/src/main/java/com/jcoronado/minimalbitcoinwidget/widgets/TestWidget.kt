package com.jcoronado.minimalbitcoinwidget.widgets

import android.content.Context
import android.content.Intent
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
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
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
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.jcoronado.minimalbitcoinwidget.MainActivity
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.AppConstants
import java.text.NumberFormat

class TestWidget : GlanceAppWidget() {

    override val stateDefinition = PriceWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<PriceWidgetState>()
            WidgetContent(state)
        }
    }

    @Composable
    private fun WidgetContent(state: PriceWidgetState) {
        val openAppIntent = Intent(LocalContext.current, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AppConstants.EXTRA_RESET_NAV, true)
        }
        GlanceTheme(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                GlanceTheme.colors
            } else {
                GlanceColorScheme.colors
            }
        ) {
            Scaffold {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(vertical = 10.dp)
                        .clickable(actionStartActivity(openAppIntent)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (state) {
                        is PriceWidgetState.Available -> {
                            AvailableUI(state, GlanceModifier.defaultWeight())
                        }

                        is PriceWidgetState.Error -> {
                            ErrorUI(state)
                        }

                        is PriceWidgetState.Loading -> {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AvailableUI(state: PriceWidgetState.Available, modifier: GlanceModifier) {
        Header(state.currency)
        Spacer(modifier)
        PriceValue(state)
        Spacer(modifier)
        PriceChange(state.changePercentage / 100)
    }

    @Composable
    private fun ErrorUI(state: PriceWidgetState.Error) {
        Text("Error: ${state.message ?: "Unknown"}")
    }

    @Composable
    private fun Header(currency: String) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.rounded_currency_bitcoin_24),
                contentDescription = "Bitcoin Icon",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary),
                modifier = GlanceModifier.size(14.dp)
            )
            Text(
                "/ ${currency.uppercase()}", style = TextStyle(
                    color = GlanceTheme.colors.secondary,
                    fontSize = 12.sp
                )
            )
        }
    }

    @Composable
    private fun PriceValue(state: PriceWidgetState.Available) {
        val numberFormatter = NumberFormat.getNumberInstance().apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        val fontSize = when {
            state.price >= 1000000 -> 20.sp
            state.price >= 100000 -> 22.sp
            else -> 24.sp
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = state.symbol, style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontSize = 16.sp
                )
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = numberFormatter.format(state.price), style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = fontSize
                )
            )
        }
    }

    @Composable
    private fun PriceChange(changePercentage: Double) {
        val (iconRes, color) = if (changePercentage >= 0) {
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
                text = String.format("%.2f%%", changePercentage * 100), style = TextStyle(
                    color = GlanceTheme.colors.secondary,
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
        val state = PriceWidgetState.Available(
            price = 123456.78,
            changePercentage = 0.49,
            currency = "USD",
            symbol = "$"
        )
        WidgetContent(state)
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataNeg() {
        val state = PriceWidgetState.Available(
            price = 123456.78,
            changePercentage = -2.45,
            currency = "USD",
            symbol = "$"
        )
        WidgetContent(state)
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataLargePos() {
        val state = PriceWidgetState.Available(
            price = 1234560.78,
            changePercentage = 3.58,
            currency = "USD",
            symbol = "$"
        )
        WidgetContent(state)
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataLargeNeg() {
        val state = PriceWidgetState.Available(
            price = 1234560.78,
            changePercentage = -3.29,
            currency = "USD",
            symbol = "$"
        )
        WidgetContent(state)
    }
}
