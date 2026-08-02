package com.jcoronado.minimalbitcoinwidget.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    isUnavailable: Boolean = false,
    showOverlay: Boolean = isUnavailable,
    unavailableMessage: String = stringResource(R.string.chart_unavailable_30d),
    onScrub: (scrubbedPrice: Double?, scrubbedTimestamp: Long?) -> Unit = { _, _ -> }
) {
    val view = LocalView.current
    val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }

    // Use synthetic placeholder curve when graph data is unavailable
    val effectivePrices = remember(prices, isUnavailable) {
        if (isUnavailable || prices.size < 2) {
            val totalPoints = 120
            val startPrice = 35000.0
            val endPrice = 65000.0
            val amplitude = (endPrice * 0.05).coerceAtLeast(kotlin.math.abs(endPrice - startPrice) * 0.45)
            List(totalPoints) { i ->
                val progress = i / (totalPoints - 1.0)
                val baseLinear = startPrice + (endPrice - startPrice) * progress
                val window = kotlin.math.sin(progress * Math.PI)
                val sineWave = kotlin.math.sin(progress * Math.PI * 5.0) * amplitude * window
                val secondaryHarmonic = kotlin.math.cos(progress * Math.PI * 9.0) * (amplitude * 0.3) * window
                baseLinear + sineWave + secondaryHarmonic
            }
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

    // Trendline entrance animation state (draws left-to-right on display / data updates)
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(effectivePrices) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    var isScrubbing by remember { mutableStateOf(false) }
    var scrubX by remember { mutableFloatStateOf(0f) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val handleTouch: (Offset, Float) -> Unit = { offset, width ->
        if (width > 0f && !isUnavailable) {
            val clampedX = offset.x.coerceIn(0f, width)
            val fraction = clampedX / width
            val newIndex = (fraction * (effectivePrices.lastIndex)).roundToInt().coerceIn(0, effectivePrices.lastIndex)
            scrubX = clampedX

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
            val priceRange = (maxPrice - minPrice).let { if (it == 0.0) 1.0 else it }

            // Vertical padding so stroke line caps don't clip canvas boundaries
            val verticalPadding = strokeWidthPx * 2f
            val usableHeight = (height - (verticalPadding * 2f)).coerceAtLeast(1f)

            // Convert prices to coordinates
            val points = smoothPrices.indices.map { i ->
                val x = (i.toFloat() / (smoothPrices.size - 1)) * width
                val yFraction = if (maxPrice == minPrice) 0.5f else ((smoothPrices[i] - minPrice) / priceRange).toFloat()
                val y = height - verticalPadding - (yFraction * usableHeight)
                Offset(x, y)
            }

            // Construct smooth Catmull-Rom Spline Path
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

