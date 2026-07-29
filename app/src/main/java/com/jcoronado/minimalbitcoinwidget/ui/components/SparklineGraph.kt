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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
            val totalPoints = 80
            val startPrice = 35000.0
            val endPrice = 65000.0
            List(totalPoints) { i ->
                val progress = i / (totalPoints - 1.0)
                val linearTrend = startPrice + (endPrice - startPrice) * progress
                val macroWave = kotlin.math.sin(i * 0.26) * 12000.0
                val microWave = kotlin.math.cos(i * 0.55) * 6000.0
                val detailWave = kotlin.math.sin(i * 1.10) * 1800.0
                linearTrend + macroWave + microWave + detailWave
            }
        } else prices
    }

    val normalLineColor = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val lineColor = if (isUnavailable) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f) else normalLineColor
    val guidelineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val indicatorOuterColor = normalLineColor.copy(alpha = 0.3f)

    // Smooth raw prices using Simple Moving Average (SMA)
    val smoothPrices = remember(effectivePrices) {
        if (effectivePrices.size < 10) effectivePrices
        else {
            val windowSize = (effectivePrices.size * 0.10).coerceIn(3.0, 7.0).toInt()
            effectivePrices.indices.map { index ->
                val start = (index - windowSize / 2).coerceAtLeast(0)
                val end = (index + windowSize / 2).coerceAtMost(effectivePrices.lastIndex)
                effectivePrices.subList(start, end + 1).average()
            }
        }
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
                val yFraction = ((smoothPrices[i] - minPrice) / priceRange).toFloat()
                val y = height - verticalPadding - (yFraction * usableHeight)
                Offset(x, y)
            }

            // Construct smooth Bezier Path
            val path = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points[0].x, points[0].y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 3f, p0.y)
                        val controlPoint2 = Offset(p0.x + 2 * (p1.x - p0.x) / 3f, p1.y)
                        cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                    }
                }
            }

            // Draw sparkline stroke
            drawPath(
                path = path,
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
