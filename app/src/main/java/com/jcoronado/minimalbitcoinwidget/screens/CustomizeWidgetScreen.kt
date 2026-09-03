package com.jcoronado.minimalbitcoinwidget.screens

import android.annotation.SuppressLint
import android.view.ContextThemeWrapper
import android.view.HapticFeedbackConstants
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.glance.appwidget.compose
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.WidgetFont
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomizeWidgetScreen(
    currentFont: WidgetFont,
    price: Double,
    percentageChange: Double,
    currency: String,
    @StringRes intervalLabelResId: Int,
    onSave: (WidgetFont) -> Unit,
    onBack: () -> Unit
) {
    val view = LocalView.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var selectedFont by remember(currentFont) { mutableStateOf(currentFont) }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.customize_widget),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onBack()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_arrow_back_24),
                            contentDescription = stringResource(R.string.back_icon_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onSave(selectedFont)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                SectionHeader(
                    title = stringResource(R.string.widget_font_preview_header),
                    top = true
                )

                // Live Widget Preview
                GlanceWidgetPreviewCard(
                    selectedFont = selectedFont,
                    price = if (price > 0.0) price else 62884.21,
                    percentageChange = if (price > 0.0) percentageChange else 2.03,
                    currency = if (currency.isNotBlank()) currency else "USD",
                    intervalLabelResId = if (intervalLabelResId != 0) intervalLabelResId else R.string.interval_24h
                )
            }

            val fontEntries = WidgetFont.entries

            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                SectionHeader(
                    title = stringResource(R.string.widget_font)
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    fontEntries.forEachIndexed { index, font ->
                        val isSelected = (font == selectedFont)
                        SegmentedButton(
                            selected = isSelected,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedFont = font
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = fontEntries.size
                            ),
                            icon = {
                                SegmentedButtonDefaults.Icon(active = isSelected)
                            }
                        ) {
                            Text(
                                text = stringResource(font.labelResId),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun GlanceWidgetPreviewCard(
    selectedFont: WidgetFont,
    price: Double,
    percentageChange: Double,
    currency: String,
    @StringRes intervalLabelResId: Int
) {
    val context = LocalContext.current
    val widgetState = remember(selectedFont, price, percentageChange, currency, intervalLabelResId) {
        PriceWidgetState.Available(
            price = price,
            changePercentage = percentageChange,
            intervalLabelResId = intervalLabelResId,
            currency = currency,
            fontKey = selectedFont.key
        )
    }

    val remoteViews by produceState<RemoteViews?>(initialValue = null, widgetState) {
        value = try {
            PriceWidget().compose(
                context = context,
                size = DpSize(208.dp, 108.dp),
                state = widgetState
            )
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (remoteViews != null) {
            Box(
                modifier = Modifier
                    .width(208.dp)
                    .height(108.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val themedContext = android.view.ContextThemeWrapper(ctx, android.R.style.Theme_DeviceDefault)
                        val frameLayout = FrameLayout(themedContext)
                        val inflated = remoteViews?.apply(themedContext, frameLayout)
                        if (inflated != null) {
                            frameLayout.addView(inflated)
                        }
                        frameLayout
                    },
                    update = { frameLayout ->
                        frameLayout.removeAllViews()
                        val themedContext = android.view.ContextThemeWrapper(frameLayout.context, android.R.style.Theme_DeviceDefault)
                        val inflated = remoteViews?.apply(themedContext, frameLayout)
                        if (inflated != null) {
                            frameLayout.addView(inflated)
                        }
                    }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {}
                )
            }
        } else {
            // Fallback while loading
            Box(
                modifier = Modifier
                    .width(208.dp)
                    .height(108.dp)
            )
        }
    }
}
