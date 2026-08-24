package com.jcoronado.minimalbitcoinwidget.screens

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.WidgetFont
import com.jcoronado.minimalbitcoinwidget.utils.FormatUtils
import com.jcoronado.minimalbitcoinwidget.widgets.glance.WidgetBitmapUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WidgetFontScreen(
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
    val fontFamily = selectedFont.getFontFamily()
    val letterSpacing = if (selectedFont.letterSpacingEm > 0f) selectedFont.letterSpacingEm.em else TextUnit.Unspecified
    val priceData = FormatUtils.formatPriceSeparated(price, currency)

    val (trendIcon, trendColor) = if (percentageChange > 0) {
        Pair(R.drawable.rounded_trending_up_24, MaterialTheme.colorScheme.primary)
    } else if (percentageChange < 0) {
        Pair(R.drawable.rounded_trending_down_24, MaterialTheme.colorScheme.error)
    } else {
        Pair(R.drawable.rounded_trending_flat_24, MaterialTheme.colorScheme.secondary)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(208.dp)
                .height(108.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val (priceFontSize, symbolFontSize) = WidgetBitmapUtils.getWidgetPriceFontSize(price, false)
                val secondaryFontSize = WidgetBitmapUtils.getWidgetSecondaryFontSize(false)

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_currency_bitcoin_24),
                        contentDescription = stringResource(R.string.bitcoin_icon_description),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "/ ${currency.uppercase()} ・ ${stringResource(intervalLabelResId)}",
                        fontSize = secondaryFontSize.sp,
                        letterSpacing = letterSpacing,
                        color = MaterialTheme.colorScheme.secondary,
                        fontFamily = fontFamily
                    )
                }

                // Price Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (priceData.symbolAtStart) {
                        Text(
                            text = priceData.symbol,
                            fontSize = symbolFontSize.sp,
                            letterSpacing = letterSpacing,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = fontFamily
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = priceData.price,
                            fontSize = priceFontSize.sp,
                            letterSpacing = letterSpacing,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = fontFamily
                        )
                    } else {
                        Text(
                            text = priceData.price,
                            fontSize = priceFontSize.sp,
                            letterSpacing = letterSpacing,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = fontFamily
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = priceData.symbol,
                            fontSize = symbolFontSize.sp,
                            letterSpacing = letterSpacing,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = fontFamily
                        )
                    }
                }

                // Percentage Change Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(trendIcon),
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.2f%%", percentageChange),
                        fontSize = secondaryFontSize.sp,
                        letterSpacing = letterSpacing,
                        color = MaterialTheme.colorScheme.secondary,
                        fontFamily = fontFamily
                    )
                }
            }
        }
    }
}
