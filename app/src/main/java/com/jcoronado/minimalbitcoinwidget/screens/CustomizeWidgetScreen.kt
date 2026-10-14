package com.jcoronado.minimalbitcoinwidget.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.HapticFeedbackConstants
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
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
import com.jcoronado.minimalbitcoinwidget.classes.WidgetColorStyle
import com.jcoronado.minimalbitcoinwidget.classes.WidgetFont
import com.jcoronado.minimalbitcoinwidget.classes.WidgetPriceSize
import com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme
import com.jcoronado.minimalbitcoinwidget.ui.components.PLACEHOLDER_SPARKLINE_PRICES
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetState
import com.jcoronado.minimalbitcoinwidget.widgets.glance.TrendlinePriceWidget

enum class PreviewWidgetType(@StringRes val labelResId: Int) {
    MINIMAL(R.string.widget_preview_minimal),
    TRENDLINE(R.string.widget_preview_trendline)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomizeWidgetScreen(
    currentFont: WidgetFont,
    currentPriceSize: WidgetPriceSize = WidgetPriceSize.DEFAULT,
    currentColorStyle: WidgetColorStyle = WidgetColorStyle.DYNAMIC,
    currentTheme: WidgetTheme = WidgetTheme.SYSTEM,
    price: Double,
    percentageChange: Double,
    currency: String,
    @StringRes intervalLabelResId: Int,
    sparklinePrices: List<Double> = emptyList(),
    onSave: (WidgetFont, WidgetPriceSize, WidgetColorStyle, WidgetTheme) -> Unit,
    onBack: () -> Unit
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val pagerState = rememberPagerState(pageCount = { PreviewWidgetType.entries.size })
    val currentPreviewHeight by animateDpAsState(
        targetValue = if (pagerState.targetPage == 0) MINIMAL_PREVIEW_HEIGHT else TRENDLINE_PREVIEW_HEIGHT,
        animationSpec = tween(durationMillis = 250),
        label = "previewHeight"
    )
    var selectedFont by remember(currentFont) { mutableStateOf(currentFont) }
    var selectedPriceSize by remember(currentPriceSize) { mutableStateOf(currentPriceSize) }
    var selectedColorStyle by remember(currentColorStyle) { mutableStateOf(currentColorStyle) }
    var selectedTheme by remember(currentTheme) { mutableStateOf(currentTheme) }

    val isDefault = selectedFont == WidgetFont.DEFAULT &&
        selectedPriceSize == WidgetPriceSize.DEFAULT &&
        selectedColorStyle == WidgetColorStyle.DEFAULT &&
        selectedTheme == WidgetTheme.DEFAULT

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
                actions = {
                    IconButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            selectedFont = WidgetFont.DEFAULT
                            selectedPriceSize = WidgetPriceSize.DEFAULT
                            selectedColorStyle = WidgetColorStyle.DEFAULT
                            selectedTheme = WidgetTheme.DEFAULT
                        },
                        enabled = !isDefault
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_restart_alt_24),
                            contentDescription = stringResource(R.string.reset_to_default)
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(currentPreviewHeight)
                            ) { page ->
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val widgetType = PreviewWidgetType.entries[page]
                                    GlanceWidgetPreviewCard(
                                        selectedWidgetType = widgetType,
                                        selectedFont = selectedFont,
                                        selectedPriceSize = selectedPriceSize,
                                        selectedColorStyle = selectedColorStyle,
                                        selectedTheme = selectedTheme,
                                        price = if (price > 0.0) price else 62884.21,
                                        percentageChange = if (price > 0.0) percentageChange else 2.03,
                                        currency = if (currency.isNotBlank()) currency else "USD",
                                        intervalLabelResId = if (intervalLabelResId != 0) intervalLabelResId else R.string.interval_7d,
                                        sparklinePrices = sparklinePrices
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Indicator Dots
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(PreviewWidgetType.entries.size) { pageIndex ->
                                    val isSelected = pagerState.currentPage == pageIndex
                                    val dotColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                    }
                                    val dotWidth = if (isSelected) 12.dp else 6.dp
                                    val dotHeight = 6.dp
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .size(width = dotWidth, height = dotHeight)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                            .clickable {
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(pageIndex)
                                                }
                                            }
                                    )
                                }
                            }
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

                val colors =
                    ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surface)

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
                            colors = colors,
                            shapes = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = fontEntries.size
                            ),
                            verticalAlignment = Alignment.CenterVertically,
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
                            colors = colors,
                            shapes = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = priceSizeEntries.size
                            ),
                            verticalAlignment = Alignment.CenterVertically,
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

                val colorStyleEntries = WidgetColorStyle.entries

                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    SectionHeader(
                        title = stringResource(R.string.widget_colors)
                    )

                    colorStyleEntries.forEachIndexed { index, style ->
                        val isSelected = (style == selectedColorStyle)
                        SegmentedListItem(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedColorStyle = style
                            },
                            colors = colors,
                            shapes = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = colorStyleEntries.size
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            content = {
                                Text(
                                    text = stringResource(style.labelResId),
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

                val themeEntries = WidgetTheme.entries

                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    SectionHeader(
                        title = stringResource(R.string.widget_theme)
                    )

                    themeEntries.forEachIndexed { index, theme ->
                        val isSelected = (theme == selectedTheme)
                        SegmentedListItem(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedTheme = theme
                            },
                            colors = colors,
                            shapes = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = themeEntries.size
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            content = {
                                Text(
                                    text = stringResource(theme.labelResId),
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

                Button(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onSave(
                            selectedFont,
                            selectedPriceSize,
                            selectedColorStyle,
                            selectedTheme
                        )
                    },
                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MediumContainerHeight),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .height(ButtonDefaults.MediumContainerHeight)
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)
                    )
                }
                Spacer(modifier = Modifier.navigationBarsPadding())
                // Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private val MINIMAL_PREVIEW_WIDTH = 208.dp
private val MINIMAL_PREVIEW_HEIGHT = 108.dp

private val TRENDLINE_PREVIEW_WIDTH = 220.dp
private val TRENDLINE_PREVIEW_HEIGHT = 136.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("DiscouragedApi", "LocalContextResourcesRead")
@Composable
private fun GlanceWidgetPreviewCard(
    selectedWidgetType: PreviewWidgetType,
    selectedFont: WidgetFont,
    selectedPriceSize: WidgetPriceSize,
    selectedColorStyle: WidgetColorStyle,
    selectedTheme: WidgetTheme,
    price: Double,
    percentageChange: Double,
    currency: String,
    @StringRes intervalLabelResId: Int,
    sparklinePrices: List<Double>
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isSystemDark = isSystemInDarkTheme()

    val isPreviewDark = when (selectedTheme) {
        WidgetTheme.LIGHT -> false
        WidgetTheme.DARK -> true
        WidgetTheme.SYSTEM -> isSystemDark
    }

    val themedContext = remember(context, configuration, isPreviewDark) {
        val uiModeNight = if (isPreviewDark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        val config = Configuration(configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or uiModeNight
        }
        val configContext = context.createConfigurationContext(config)
        val themeResId = if (isPreviewDark) android.R.style.Theme_DeviceDefault else android.R.style.Theme_DeviceDefault_Light
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
        selectedColorStyle,
        selectedTheme,
        price,
        percentageChange,
        currency,
        intervalLabelResId,
        sparklinePrices
    ) {
        PriceWidgetState.Available(
            price = price,
            changePercentage = percentageChange,
            intervalLabelResId = intervalLabelResId,
            currency = currency,
            fontKey = selectedFont.key,
            priceSizeKey = selectedPriceSize.key,
            colorStyleKey = selectedColorStyle.key,
            themeKey = selectedTheme.key,
            sparklinePrices = if (sparklinePrices.isNotEmpty()) sparklinePrices else PLACEHOLDER_SPARKLINE_PRICES
        )
    }

    val (previewWidth, previewHeight) = when (selectedWidgetType) {
        PreviewWidgetType.MINIMAL -> Pair(MINIMAL_PREVIEW_WIDTH, MINIMAL_PREVIEW_HEIGHT)
        PreviewWidgetType.TRENDLINE -> Pair(TRENDLINE_PREVIEW_WIDTH, TRENDLINE_PREVIEW_HEIGHT)
    }

    val remoteViews by produceState<RemoteViews?>(initialValue = null, widgetState, themedContext, selectedWidgetType) {
        value = try {
            when (selectedWidgetType) {
                PreviewWidgetType.MINIMAL -> {
                    PriceWidget().compose(
                        context = themedContext,
                        size = DpSize(previewWidth, previewHeight),
                        state = widgetState
                    )
                }
                PreviewWidgetType.TRENDLINE -> {
                    TrendlinePriceWidget().compose(
                        context = themedContext,
                        size = DpSize(previewWidth, previewHeight),
                        state = widgetState
                    )
                }
            }
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
                    .width(previewWidth)
                    .height(previewHeight)
            ) {
                key(isPreviewDark, selectedWidgetType) {
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
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            shape = widgetShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .width(previewWidth)
                    .height(previewHeight),
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
