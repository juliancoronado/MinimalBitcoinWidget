package com.jcoronado.minimalbitcoinwidget.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jcoronado.minimalbitcoinwidget.R
import kotlin.math.roundToInt

/**
 * Self-contained interactive Sparkline trend graph.
 *
 * @param prices List of historical price points.
 * @param isPositive True if the net change over the interval is non-negative.
 * @param modifier Layout modifier.
 * @param strokeWidth Width of the trend line stroke.
 * @param lastUpdatedTimestamp Timestamp of the latest price point (used for calculating historical timestamps during scrub).
 * @param isUnavailable True if chart data is unavailable for the current interval (e.g. 30D).
 * @param unavailableMessage Message to display in the overlay when chart data is unavailable.
 * @param onScrub Callback emitted during drag gestures with the scrubbed price and calculated timestamp, or (null, null) when touch ends.
 */
@Composable
fun SparklineGraph(
    prices: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 3.dp,
    verticalPaddingFraction: Double = SparklineScaleCalculator.DEFAULT_VERTICAL_PADDING_FRACTION,
    lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    isUnavailable: Boolean = false,
    showOverlay: Boolean = isUnavailable,
    showDottedBaseline: Boolean = true,
    showLiveDot: Boolean = true,
    animationKey: Any? = null,
    unavailableMessage: String = stringResource(R.string.chart_unavailable_30d),
    onScrub: (scrubbedPrice: Double?, scrubbedTimestamp: Long?) -> Unit = { _, _ -> }
) {
    val view = LocalView.current
    val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }

    // Use synthetic placeholder curve when graph data is unavailable
    val effectivePrices = remember(prices, isUnavailable) {
        if (isUnavailable || prices.size < 2) {
            PLACEHOLDER_SPARKLINE_PRICES
        } else prices
    }

    val normalLineColor = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val lineColor = if (isUnavailable) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f) else normalLineColor
    val guidelineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val indicatorOuterColor = normalLineColor.copy(alpha = 0.3f)

    // Smooth raw prices using endpoint-preserving weighted Gaussian smoothing
    val smoothPrices = remember(effectivePrices, isUnavailable) {
        if (isUnavailable || effectivePrices.size < 5) effectivePrices
        else {
            val n = effectivePrices.size
            val smoothed = effectivePrices.toDoubleArray()
            repeat(2) {
                val temp = smoothed.clone()
                for (i in 1 until n - 1) {
                    val prev = temp[i - 1]
                    val curr = temp[i]
                    val next = temp[i + 1]
                    smoothed[i] = 0.25 * prev + 0.50 * curr + 0.25 * next
                }
                // Strictly preserve exact start and end values
                smoothed[0] = effectivePrices.first()
                smoothed[n - 1] = effectivePrices.last()
            }
            smoothed.toList()
        }
    }

    // Trend line entrance animation state (draws left-to-right on display / data updates)
    val animationProgress = remember { Animatable(0f) }
    val dotEntranceScale = remember { Animatable(0f) }
    val dotEntranceAlpha = remember { Animatable(0f) }
    val dotGlowFlare = remember { Animatable(0f) }

    LaunchedEffect(effectivePrices, lastUpdatedTimestamp, animationKey) {
        dotEntranceScale.snapTo(0f)
        dotEntranceAlpha.snapTo(0f)
        dotGlowFlare.snapTo(0f)
        animationProgress.snapTo(0f)
        launch {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        // As the line reaches the endpoint (~480ms), bloom the dot into existence with a one-time glow flare
        delay(480)
        launch {
            dotEntranceAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 240, easing = LinearEasing)
            )
        }
        launch {
            dotGlowFlare.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 650
                    0f at 0 using FastOutSlowInEasing
                    1.35f at 220 using FastOutSlowInEasing // radiant entrance glow peak
                    1.0f at 650 using FastOutSlowInEasing  // settles to calm steady resting state
                }
            )
        }
        dotEntranceScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    var isScrubbing by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(isScrubbing) {
        if (!isScrubbing && animationProgress.value >= 1f) {
            dotEntranceScale.snapTo(0.7f)
            dotEntranceAlpha.snapTo(0.3f)
            dotGlowFlare.snapTo(1f) // Keep halo calm at normal 1.0f, no flare on scrub release
            launch {
                dotEntranceAlpha.animateTo(1f, tween(160, easing = LinearEasing))
            }
            dotEntranceScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val handleTouch: (Offset, Float) -> Unit = { offset, width ->
        if (width > 0f && !isUnavailable) {
            val clampedX = offset.x.coerceIn(0f, width)
            val fraction = clampedX / width
            val newIndex = (fraction * (effectivePrices.lastIndex)).roundToInt().coerceIn(0, effectivePrices.lastIndex)

            if (newIndex != selectedIndex) {
                selectedIndex = newIndex
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                val priceAtPoint = effectivePrices[newIndex]
                val pointsAgo = effectivePrices.lastIndex - newIndex
                // Hourly data points: each step is 1 hour
                val timestampAtPoint = lastUpdatedTimestamp - (pointsAgo * 3600_000L)
                onScrub(priceAtPoint, timestampAtPoint)
            }
        }
    }

    val clearTouch: () -> Unit = {
        isScrubbing = false
        selectedIndex = -1
        onScrub(null, null)
    }

    val pointerModifier = if (!isUnavailable) {
        Modifier.pointerInput(effectivePrices, lastUpdatedTimestamp) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val width = size.width.toFloat()
                try {
                    isScrubbing = true
                    handleTouch(down.position, width)

                    val pointerId = down.id
                    while (true) {
                        val event = awaitPointerEvent()
                        val pointer = event.changes.find { it.id == pointerId } ?: event.changes.firstOrNull()

                        if (pointer == null || !pointer.pressed) {
                            break
                        }

                        pointer.consume()
                        handleTouch(pointer.position, width)
                    }
                } finally {
                    clearTouch()
                }
            }
        }
    } else Modifier

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(pointerModifier)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f) return@Canvas

            val minPrice = smoothPrices.minOrNull() ?: 0.0
            val maxPrice = smoothPrices.maxOrNull() ?: 1.0

            // Vertical padding so stroke line caps don't clip canvas boundaries
            val verticalPadding = strokeWidthPx * 2f
            val usableHeight = (height - (verticalPadding * 2f)).coerceAtLeast(1f)

            // Convert prices to coordinates using dynamic auto-scaling
            val points = smoothPrices.indices.map { i ->
                val x = (i.toFloat() / (smoothPrices.size - 1)) * width
                val yFraction = SparklineScaleCalculator.calculateYFraction(
                    price = smoothPrices[i],
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    verticalPaddingFraction = verticalPaddingFraction
                )
                val y = height - verticalPadding - (yFraction * usableHeight)
                Offset(x, y)
            }

            // 1. Draw dotted opening reference baseline (matching trendline widget)
            if (showDottedBaseline && !isUnavailable && points.isNotEmpty()) {
                val baselineY = points.first().y
                val baselineDotColor = normalLineColor.copy(alpha = 0.40f)
                val dotRadius = 1.1.dp.toPx()
                val dotSpacing = 7.5.dp.toPx()
                var currentX = 0f
                while (currentX <= width) {
                    drawCircle(
                        color = baselineDotColor,
                        radius = dotRadius,
                        center = Offset(currentX, baselineY)
                    )
                    currentX += dotSpacing
                }
            }

            // 2. Construct smooth Catmull-Rom Spline Path
            val path = Path().apply {
                if (points.size >= 2) {
                    moveTo(points[0].x, points[0].y)
                    val n = points.size
                    val alpha = 0.20f // Smooth spline curvature multiplier

                    for (i in 0 until n - 1) {
                        val p0 = points[if (i == 0) 0 else i - 1]
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val p3 = points[if (i + 2 >= n) n - 1 else i + 2]

                        val cp1X = p1.x + (p2.x - p0.x) * alpha
                        val cp1Y = p1.y + (p2.y - p0.y) * alpha

                        val cp2X = p2.x - (p3.x - p1.x) * alpha
                        val cp2Y = p2.y - (p3.y - p1.y) * alpha

                        cubicTo(cp1X, cp1Y, cp2X, cp2Y, p2.x, p2.y)
                    }
                }
            }

            // Calculate animated path segment according to drawing progress
            val currentProgress = animationProgress.value
            val animatedPath = if (currentProgress < 1f) {
                val pathMeasure = PathMeasure().apply { setPath(path, false) }
                Path().apply {
                    if (pathMeasure.length > 0f) {
                        pathMeasure.getSegment(0f, pathMeasure.length * currentProgress, this, true)
                    }
                }
            } else {
                path
            }

            // Draw sparkline stroke
            drawPath(
                path = animatedPath,
                color = lineColor,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Render touch scrubbing guideline & glowing point indicator when available
            if (!isUnavailable && isScrubbing && selectedIndex in points.indices) {
                val point = points[selectedIndex]

                // Draw vertical dashed guideline
                drawLine(
                    color = guidelineColor,
                    start = Offset(point.x, 0f),
                    end = Offset(point.x, height),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Glowing outer circle dot
                drawCircle(
                    color = indicatorOuterColor,
                    radius = 12.dp.toPx(),
                    center = point
                )

                // Solid center dot
                drawCircle(
                    color = normalLineColor,
                    radius = 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = point
                )
            }

            // 3. Draw real-time live price dot at the endpoint when resting (not scrubbing)
            if (showLiveDot && !isUnavailable && !isScrubbing && points.isNotEmpty() && dotEntranceAlpha.value > 0f) {
                val endpoint = points.last()
                val entranceScale = dotEntranceScale.value
                val entranceAlpha = dotEntranceAlpha.value
                val flare = dotGlowFlare.value

                // Outer halo: flares on entrance (up to ~16.2dp radius, ~0.42 alpha) and relaxes to steady 12dp, 0.30 alpha
                val baseHaloRadius = 12.dp.toPx()
                val haloRadius = baseHaloRadius * flare * entranceScale
                val haloAlpha = (0.30f * flare).coerceIn(0f, 0.45f) * entranceAlpha

                drawCircle(
                    color = normalLineColor.copy(alpha = haloAlpha),
                    radius = haloRadius,
                    center = endpoint
                )

                // Inner solid dot
                drawCircle(
                    color = normalLineColor.copy(alpha = entranceAlpha),
                    radius = 5.dp.toPx() * entranceScale,
                    center = endpoint
                )

                // Center white pupil
                drawCircle(
                    color = Color.White.copy(alpha = entranceAlpha),
                    radius = 2.5.dp.toPx() * entranceScale,
                    center = endpoint
                )
            }
        }

        // Display unavailable overlay pill when requested (e.g. 30D interval)
        if (showOverlay) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = unavailableMessage,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * Calculates normalized Y height fractions (0.0 at bottom, 1.0 at top) for sparkline graph rendering.
 *
 * Automatically scales the price data dynamically to the min and max prices of the current dataset,
 * adding a balanced vertical padding fraction so peaks and valleys are expressive and do not clip.
 */
object SparklineScaleCalculator {
    const val DEFAULT_VERTICAL_PADDING_FRACTION = 0.10

    fun calculateYFraction(
        price: Double,
        minPrice: Double,
        maxPrice: Double,
        verticalPaddingFraction: Double = DEFAULT_VERTICAL_PADDING_FRACTION
    ): Float {
        val actualRange = maxPrice - minPrice
        if (actualRange <= 0.0) {
            return 0.5f
        }

        val padding = actualRange * verticalPaddingFraction
        val effectiveMin = minPrice - padding
        val effectiveRange = actualRange + (padding * 2.0)

        val fraction = (price - effectiveMin) / effectiveRange
        return fraction.toFloat().coerceIn(0f, 1f)
    }
}

/**
 * Synthetic default sparkline curve points used when live data is unavailable (e.g. 30D interval or initial load).
 * Modeled after a smooth waveform with drastic amplitude swings: a lower starting baseline, tall primary crest, deep plunge trough, sharp secondary peak, and an elevated ending curve.
 */
val PLACEHOLDER_SPARKLINE_PRICES: List<Double> = listOf(
    15.0, 31.0, 36.0, 42.0, 65.0,
    90.0, 100.0, 99.0, 88.0, 68.0,
    38.0, 14.0, 3.0, 0.0, 8.0,
    38.0, 72.0, 88.0, 75.0, 64.0,
    58.0, 55.0, 58.0, 69.0, 92.0
)
