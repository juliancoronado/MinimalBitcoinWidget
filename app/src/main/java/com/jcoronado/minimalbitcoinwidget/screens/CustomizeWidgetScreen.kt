package com.jcoronado.minimalbitcoinwidget.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.HapticFeedbackConstants
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.glance.appwidget.compose
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.WidgetBackgroundOpacity
import com.jcoronado.minimalbitcoinwidget.classes.WidgetFont
import com.jcoronado.minimalbitcoinwidget.classes.WidgetPriceSize
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomizeWidgetScreen(
    currentFont: WidgetFont,
    currentPriceSize: WidgetPriceSize = WidgetPriceSize.DEFAULT,
    currentAmoledBlack: Boolean = false,
    currentOpacity: WidgetBackgroundOpacity = WidgetBackgroundOpacity.SOLID,
    currentShowHeader: Boolean = true,
    price: Double,
    percentageChange: Double,
    currency: String,
    @StringRes intervalLabelResId: Int,
    onSave: (WidgetFont, WidgetPriceSize, Boolean, WidgetBackgroundOpacity, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val view = LocalView.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var selectedFont by remember(currentFont) { mutableStateOf(currentFont) }
    var selectedPriceSize by remember(currentPriceSize) { mutableStateOf(currentPriceSize) }
    var selectedAmoledBlack by remember(currentAmoledBlack) { mutableStateOf(currentAmoledBlack) }
    var selectedOpacity by remember(currentOpacity) { mutableStateOf(currentOpacity) }
    var selectedShowHeader by remember(currentShowHeader) { mutableStateOf(currentShowHeader) }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.customize_widget),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // PINNED WIDGET PREVIEW STAGE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                SectionHeader(
                    title = stringResource(R.string.widget_font_preview_header),
                    top = true
                )

                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    SegmentedListItem(
                        onClick = {},
                        colors = ListItemDefaults.segmentedColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shapes = ListItemDefaults.segmentedShapes(
                            index = 0,
                            count = 1
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GlanceWidgetPreviewCard(
                                selectedFont = selectedFont,
                                selectedPriceSize = selectedPriceSize,
                                selectedAmoledBlack = selectedAmoledBlack,
                                selectedOpacity = selectedOpacity,
                                selectedShowHeader = selectedShowHeader,
                                price = if (price > 0.0) price else 62884.21,
                                percentageChange = if (price > 0.0) percentageChange else 2.03,
                                currency = if (currency.isNotBlank()) currency else "USD",
                                intervalLabelResId = if (intervalLabelResId != 0) intervalLabelResId else R.string.interval_24h
                            )
                        }
                    }
                }
            }

            // SCROLLABLE OPTIONS AND SAVE BUTTON
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val fontEntries = WidgetFont.entries

                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    SectionHeader(
                        title = stringResource(R.string.widget_font),
                        top = true
                    )

                    fontEntries.forEachIndexed { index, font ->
                        val isSelected = (font == selectedFont)
                        SegmentedListItem(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedFont = font
                            },
                            colors = ListItemDefaults.segmentedColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shapes = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = fontEntries.size
                            ),
                            content = {
                                Text(
                                    text = stringResource(font.labelResId),
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            trailingContent = {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null
                                )
                            }
                        )
                    }
                }

                val priceSizeEntries = WidgetPriceSize.entries

                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    SectionHeader(
                        title = stringResource(R.string.widget_price_size)
                    )

                    priceSizeEntries.forEachIndexed { index, size ->
                        val isSelected = (size == selectedPriceSize)
                        SegmentedListItem(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedPriceSize = size
                            },
                            colors = ListItemDefaults.segmentedColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shapes = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = priceSizeEntries.size
                            ),
                            content = {
                                Text(
                                    text = stringResource(size.labelResId),
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            trailingContent = {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null
                                )
                            }
                        )
                    }
                }

                val opacityEntries = WidgetBackgroundOpacity.entries

                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    SectionHeader(
                        title = stringResource(R.string.widget_background)
                    )

                    opacityEntries.forEachIndexed { index, opacity ->
                        val isSelected = (opacity == selectedOpacity)
                        SegmentedListItem(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedOpacity = opacity
                            },
                            colors = ListItemDefaults.segmentedColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shapes = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = opacityEntries.size
                            ),
                            content = {
                                Text(
                                    text = stringResource(opacity.labelResId),
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            trailingContent = {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    CompositionLocalProvider(LocalRippleConfiguration provides null) {
                        SegmentedListItem(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedAmoledBlack = !selectedAmoledBlack
                            },
                            colors = ListItemDefaults.segmentedColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shapes = ListItemDefaults.segmentedShapes(
                                index = 0,
                                count = 1
                            ),
                            content = {
                                Text(
                                    text = stringResource(R.string.widget_amoled_black_title),
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.widget_amoled_black_subtitle),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = selectedAmoledBlack,
                                    onCheckedChange = {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        selectedAmoledBlack = it
                                    }
                                )
                            }
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    SectionHeader(
                        title = stringResource(R.string.widget_display_options)
                    )

                    CompositionLocalProvider(LocalRippleConfiguration provides null) {
                        SegmentedListItem(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedShowHeader = !selectedShowHeader
                            },
                            colors = ListItemDefaults.segmentedColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shapes = ListItemDefaults.segmentedShapes(
                                index = 0,
                                count = 1
                            ),
                            content = {
                                Text(
                                    text = stringResource(R.string.widget_show_header_title),
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.widget_show_header_subtitle),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = selectedShowHeader,
                                    onCheckedChange = {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        selectedShowHeader = it
                                    }
                                )
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onSave(
                            selectedFont,
                            selectedPriceSize,
                            selectedAmoledBlack,
                            selectedOpacity,
                            selectedShowHeader
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.navigationBarsPadding())
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private val PREVIEW_WIDTH = 208.dp
private val PREVIEW_HEIGHT = 108.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("DefaultLocale")
@Composable
private fun GlanceWidgetPreviewCard(
    selectedFont: WidgetFont,
    selectedPriceSize: WidgetPriceSize,
    selectedAmoledBlack: Boolean,
    selectedOpacity: WidgetBackgroundOpacity,
    selectedShowHeader: Boolean,
    price: Double,
    percentageChange: Double,
    currency: String,
    @StringRes intervalLabelResId: Int
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val themedContext = remember(context, isDark) {
        val uiModeNight = if (isDark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        val config = Configuration(context.resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or uiModeNight
        }
        val configContext = context.createConfigurationContext(config)
        val themeResId = if (isDark) android.R.style.Theme_DeviceDefault else android.R.style.Theme_DeviceDefault_Light
        ContextThemeWrapper(configContext, themeResId)
    }

    val widgetCornerRadius = remember(context, density) {
        val systemRadiusId = context.resources.getIdentifier(
            "system_app_widget_background_radius", "dimen", "android"
        )
        if (Build.VERSION.SDK_INT >= 31 && systemRadiusId != 0) {
            val px = context.resources.getDimension(systemRadiusId)
            with(density) { px.toDp() }
        } else {
            16.dp
        }
    }
    val widgetShape = remember(widgetCornerRadius) {
        RoundedCornerShape(widgetCornerRadius)
    }

    val widgetState = remember(
        selectedFont,
        selectedPriceSize,
        selectedAmoledBlack,
        selectedOpacity,
        selectedShowHeader,
        price,
        percentageChange,
        currency,
        intervalLabelResId
    ) {
        PriceWidgetState.Available(
            price = price,
            changePercentage = percentageChange,
            intervalLabelResId = intervalLabelResId,
            currency = currency,
            fontKey = selectedFont.key,
            priceSizeKey = selectedPriceSize.key,
            amoledBlack = selectedAmoledBlack,
            backgroundOpacityKey = selectedOpacity.key,
            showHeader = selectedShowHeader
        )
    }

    val remoteViews by produceState<RemoteViews?>(initialValue = null, widgetState, themedContext) {
        value = try {
            PriceWidget().compose(
                context = themedContext,
                size = DpSize(PREVIEW_WIDTH, PREVIEW_HEIGHT),
                state = widgetState
            )
        } catch (e: Exception) {
            null
        }
    }

    Box(
        contentAlignment = Alignment.Center
    ) {
        val currentViews = remoteViews
        if (currentViews != null) {
            Box(
                modifier = Modifier
                    .width(PREVIEW_WIDTH)
                    .height(PREVIEW_HEIGHT)
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(widgetShape),
                    factory = { _ ->
                        val frameLayout = FrameLayout(themedContext)
                        val inflated = currentViews.apply(themedContext, frameLayout)
                        if (inflated != null) {
                            frameLayout.addView(inflated)
                        }
                        frameLayout
                    },
                    update = { frameLayout ->
                        frameLayout.removeAllViews()
                        val inflated = currentViews.apply(themedContext, frameLayout)
                        if (inflated != null) {
                            frameLayout.addView(inflated)
                        }
                    }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            shape = widgetShape
                        )
                        .pointerInput(Unit) {}
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .width(PREVIEW_WIDTH)
                    .height(PREVIEW_HEIGHT),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(
                    modifier = Modifier.size(32.dp),
                    polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
                )
            }
        }
    }
}
