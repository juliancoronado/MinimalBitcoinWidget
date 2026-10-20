package com.jcoronado.minimalbitcoinwidget.widgets.glance

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import android.util.TypedValue
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.WidgetColorStyle
import com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme
import com.jcoronado.minimalbitcoinwidget.ui.components.PLACEHOLDER_SPARKLINE_PRICES
import com.jcoronado.minimalbitcoinwidget.ui.components.SparklineScaleCalculator

/**
 * Gradient fill style for the chart.
 */
enum class ChartFillStyle {
    SUBTLE, // Soft linear gradient fade
    NONE
}

/**
 * High-performance off-screen Bitmap renderer for sparkline charts in Glance widgets.
 * Supports smooth Catmull-Rom splines, dashed baselines, live glowing dots, and volumetric gradients.
 */
object ChartBitmapRenderer {

    const val DEFAULT_STROKE_WIDTH_DP = 2.5f

    /**
     * Downsamples price series with high data density (such as 168-point 7D hourly series)
     * to a clean, balanced point count (~35 points) to eliminate high-frequency micro-jitter
     * and optimize off-screen rendering performance, strictly preserving opening and ending prices.
     */
    fun downsamplePrices(prices: List<Double>, maxPoints: Int = 42, targetPoints: Int = 35): List<Double> {
        if (prices.size <= maxPoints) return prices
        val targetCount = targetPoints.coerceAtLeast(2)
        return List(targetCount) { i ->
            val index = (i * (prices.size - 1) + (targetCount - 1) / 2) / (targetCount - 1)
            prices[index]
        }
    }

    /**
     * Converts DP to physical pixels for the current display metrics.
     */
    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

    /**
     * Converts SP to physical pixels for the current display metrics.
     */
    fun spToPx(context: Context, sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }

    /**
     * Determines whether the widget should render in dark mode based on theme settings and system configuration.
     */
    fun isDarkTheme(context: Context?, widgetTheme: WidgetTheme): Boolean {
        return when (widgetTheme) {
            WidgetTheme.DARK -> true
            WidgetTheme.LIGHT -> false
            WidgetTheme.SYSTEM -> {
                val configuration = context?.resources?.configuration
                if (configuration != null) {
                    val nightModeFlags = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    nightModeFlags == Configuration.UI_MODE_NIGHT_YES
                } else {
                    false
                }
            }
        }
    }

    /**
     * Resolves the trendline line color adhering to MBW theme settings and dynamic colors.
     */
    fun resolveTrendColor(
        context: Context,
        isPositive: Boolean,
        colorStyle: WidgetColorStyle,
        widgetTheme: WidgetTheme
    ): Int {
        val isDark = isDarkTheme(context, widgetTheme)

        return if (colorStyle == WidgetColorStyle.SOLID) {
            if (isDark) {
                if (isPositive) 0xFF36B37E.toInt() else 0xFFFF5630.toInt()
            } else {
                if (isPositive) 0xFF00875A.toInt() else 0xFFDE350B.toInt()
            }
        } else {
            val colorRes = if (isDark) {
                if (isPositive) com.jcoronado.minimalbitcoinwidget.R.color.widget_dynamic_dark_primary
                else com.jcoronado.minimalbitcoinwidget.R.color.widget_dynamic_dark_error
            } else {
                if (isPositive) com.jcoronado.minimalbitcoinwidget.R.color.widget_dynamic_light_primary
                else com.jcoronado.minimalbitcoinwidget.R.color.widget_dynamic_light_error
            }
            androidx.core.content.ContextCompat.getColor(context, colorRes)
        }
    }

    /**
     * Renders a neutral, muted unavailable state with a centered dotted reference baseline
     * and an informative pill overlay (e.g. "Chart unavailable for 30D interval").
     */
    fun createUnavailableChartBitmap(
        context: Context,
        widthPx: Int,
        heightPx: Int,
        widgetTheme: WidgetTheme = WidgetTheme.SYSTEM,
        message: String = context.getString(R.string.chart_unavailable_30d),
        typeface: Typeface? = null
    ): Bitmap {
        val w = widthPx.coerceAtLeast(10)
        val h = heightPx.coerceAtLeast(10)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val isDark = isDarkTheme(context, widgetTheme)
        val dotColor = if (isDark) {
            Color.argb((255 * 0.30f).toInt(), 196, 198, 208)
        } else {
            Color.argb((255 * 0.30f).toInt(), 68, 71, 78)
        }
        val pillBgColor = if (isDark) {
            Color.rgb(39, 42, 47)
        } else {
            Color.rgb(230, 232, 238)
        }
        val pillBorderColor = if (isDark) {
            Color.argb((255 * 0.20f).toInt(), 196, 198, 208)
        } else {
            Color.argb((255 * 0.20f).toInt(), 68, 71, 78)
        }
        val textColor = if (isDark) 0xFFC4C6D0.toInt() else 0xFF44474E.toInt()

        val centerY = h / 2f

        // 1. Draw horizontal dotted reference line across the full width
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = dotColor
        }
        val dotRadius = dpToPx(context, 1.1f)
        val dotSpacing = dpToPx(context, 7.5f)
        var currentX = 0f
        while (currentX <= w.toFloat()) {
            canvas.drawCircle(currentX, centerY, dotRadius, dotPaint)
            currentX += dotSpacing
        }

        // 2. Draw centered unavailable pill if vertical space permits
        val minHeightForPill = dpToPx(context, 18f)
        if (h >= minHeightForPill && message.isNotBlank()) {
            val isCompactHeight = h < dpToPx(context, 32f)
            var fontSizeSp = if (isCompactHeight) 8.0f else 9.5f
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = textColor
                this.textSize = spToPx(context, fontSizeSp)
                this.typeface = typeface ?: Typeface.DEFAULT
                this.textAlign = Paint.Align.CENTER
            }

            val pillPaddingH = if (isCompactHeight) dpToPx(context, 8f) else dpToPx(context, 12f)
            val pillPaddingV = if (isCompactHeight) dpToPx(context, 2.5f) else dpToPx(context, 4.5f)
            val maxAvailableTextWidth = (w - dpToPx(context, 16f) - (pillPaddingH * 2)).coerceAtLeast(10f)

            if (textPaint.measureText(message) > maxAvailableTextWidth) {
                fontSizeSp = if (isCompactHeight) 7.5f else 8.5f
                textPaint.textSize = spToPx(context, fontSizeSp)
            }

            val ellipsizeText = TextUtils.ellipsize(
                message,
                TextPaint(textPaint),
                maxAvailableTextWidth,
                TextUtils.TruncateAt.END
            ).toString()

            val textBounds = Rect()
            textPaint.getTextBounds(ellipsizeText, 0, ellipsizeText.length, textBounds)
            val measuredTextWidth = textPaint.measureText(ellipsizeText)

            val pillWidth = (measuredTextWidth + (pillPaddingH * 2)).coerceAtMost(w.toFloat() - dpToPx(context, 8f))
            val pillHeight = (textBounds.height() + (pillPaddingV * 2)).coerceAtMost(h.toFloat() - dpToPx(context, 2f))
            val pillRadius = pillHeight / 2f

            val pillLeft = (w - pillWidth) / 2f
            val pillTop = centerY - (pillHeight / 2f)
            val pillRight = pillLeft + pillWidth
            val pillBottom = pillTop + pillHeight
            val pillRect = RectF(pillLeft, pillTop, pillRight, pillBottom)

            // Pill background
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = pillBgColor
            }
            canvas.drawRoundRect(pillRect, pillRadius, pillRadius, bgPaint)

            // Pill border
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = dpToPx(context, 0.75f)
                color = pillBorderColor
            }
            canvas.drawRoundRect(pillRect, pillRadius, pillRadius, borderPaint)

            // Centered text
            val textY = centerY - textBounds.exactCenterY()
            canvas.drawText(ellipsizeText, w / 2f, textY, textPaint)
        }

        return bitmap
    }

    /**
     * Backwards-compatible overload for existing calls.
     */
    fun createChartBitmap(
        context: Context,
        prices: List<Double>,
        strokeColor: Int,
        widthPx: Int,
        heightPx: Int,
        includeGradient: Boolean = false,
        strokeWidthDp: Float = DEFAULT_STROKE_WIDTH_DP
    ): Bitmap {
        return createChartBitmap(
            context = context,
            prices = prices,
            strokeColor = strokeColor,
            widthPx = widthPx,
            heightPx = heightPx,
            fillStyle = if (includeGradient) ChartFillStyle.SUBTLE else ChartFillStyle.NONE,
            strokeWidthDp = strokeWidthDp,
            drawDashedBaseline = false,
            drawLiveDot = false,
            neonGlow = false
        )
    }

    /**
     * Generates an off-screen [Bitmap] with advanced styling:
     * - Smooth Catmull-Rom cubic spline
     * - Optional dashed opening baseline
     * - Optional glowing real-time endpoint dot
     * - Volumetric / subtle / ambient gradient fills
     * - Neon outer stroke glow
     */
    fun createChartBitmap(
        context: Context,
        prices: List<Double>,
        strokeColor: Int,
        widthPx: Int,
        heightPx: Int,
        fillStyle: ChartFillStyle = ChartFillStyle.NONE,
        strokeWidthDp: Float = DEFAULT_STROKE_WIDTH_DP,
        drawDashedBaseline: Boolean = false,
        drawLiveDot: Boolean = false,
        neonGlow: Boolean = false,
        smoothingRounds: Int = 2
    ): Bitmap {
        val w = widthPx.coerceAtLeast(10)
        val h = heightPx.coerceAtLeast(10)

        if (prices.size < 2) {
            return createUnavailableChartBitmap(
                context = context,
                widthPx = w,
                heightPx = h,
                widgetTheme = WidgetTheme.SYSTEM,
                message = context.getString(R.string.chart_unavailable_30d)
            )
        }

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rawPrices = downsamplePrices(prices)

        // Gaussian 3-point smoothing preserving endpoints
        val smoothPrices = if (rawPrices.size < 5) {
            rawPrices
        } else {
            val n = rawPrices.size
            val smoothed = rawPrices.toDoubleArray()
            repeat(smoothingRounds.coerceIn(1, 4)) {
                val temp = smoothed.clone()
                for (i in 1 until n - 1) {
                    smoothed[i] = 0.25 * temp[i - 1] + 0.50 * temp[i] + 0.25 * temp[i + 1]
                }
                smoothed[0] = rawPrices.first()
                smoothed[n - 1] = rawPrices.last()
            }
            smoothed.toList()
        }

        val strokeWidthPx = dpToPx(context, strokeWidthDp)
        val haloRadius = if (neonGlow) dpToPx(context, 8.0f) else dpToPx(context, 6.0f)

        // When drawLiveDot is enabled (as in widgets), provide generous right and bottom insets
        // so the glowing endpoint dot and halo never clip into the widget's rounded corner curves (16-24dp)
        // during steep negative price trends.
        val leftPaddingPx = strokeWidthPx * 1.5f
        val rightPaddingPx = if (drawLiveDot) dpToPx(context, 18f) else strokeWidthPx
        val topPaddingPx = strokeWidthPx * 1.6f
        val bottomPaddingPx = if (drawLiveDot) dpToPx(context, 18f) else (strokeWidthPx * 1.6f)

        val usableWidth = (w.toFloat() - leftPaddingPx - rightPaddingPx).coerceAtLeast(1f)
        val usableHeight = (h.toFloat() - topPaddingPx - bottomPaddingPx).coerceAtLeast(1f)

        // Scale vertical space occupied by the trendline to 80% with symmetric 10% top & bottom padding
        val chartVerticalScale = 0.80f
        val verticalInsetPx = usableHeight * ((1f - chartVerticalScale) / 2f)
        val chartUsableHeight = usableHeight * chartVerticalScale

        val minPrice = smoothPrices.minOrNull() ?: 0.0
        val maxPrice = smoothPrices.maxOrNull() ?: 1.0

        val points = smoothPrices.indices.map { i ->
            val x = leftPaddingPx + ((i.toFloat() / (smoothPrices.size - 1)) * usableWidth)
            val yFraction = SparklineScaleCalculator.calculateYFraction(
                price = smoothPrices[i],
                minPrice = minPrice,
                maxPrice = maxPrice,
                verticalPaddingFraction = SparklineScaleCalculator.DEFAULT_VERTICAL_PADDING_FRACTION
            )
            val y = h.toFloat() - bottomPaddingPx - verticalInsetPx - (yFraction * chartUsableHeight)
            PointF(x, y)
        }

        // 1. Draw dotted opening reference baseline if requested (all the way to the edges)
        if (drawDashedBaseline && points.isNotEmpty()) {
            val baselineY = points.first().y
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.argb(
                    (Color.alpha(strokeColor) * 0.40f).toInt(),
                    Color.red(strokeColor),
                    Color.green(strokeColor),
                    Color.blue(strokeColor)
                )
            }
            val dotRadius = dpToPx(context, 1.1f)
            val dotSpacing = dpToPx(context, 7.5f)
            var currentX = 0f
            while (currentX <= w.toFloat()) {
                canvas.drawCircle(currentX, baselineY, dotRadius, dotPaint)
                currentX += dotSpacing
            }
        }

        // 2. Build Catmull-Rom cubic spline path
        val linePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, points[0].y)
                val n = points.size
                val alpha = 0.20f

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

        // 3. Draw gradient fill under chart curve
        if (fillStyle != ChartFillStyle.NONE && points.size >= 2) {
            val fillPath = Path(linePath).apply {
                lineTo(points.last().x, h.toFloat())
                lineTo(points.first().x, h.toFloat())
                close()
            }

            when (fillStyle) {
                ChartFillStyle.SUBTLE -> {
                    val topAlpha = (Color.alpha(strokeColor) * 0.25f).toInt().coerceIn(0, 255)
                    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        shader = LinearGradient(
                            0f, 0f, 0f, h.toFloat(),
                            Color.argb(topAlpha, Color.red(strokeColor), Color.green(strokeColor), Color.blue(strokeColor)),
                            Color.TRANSPARENT,
                            Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawPath(fillPath, fillPaint)
                }
                ChartFillStyle.NONE -> Unit
            }
        }

        // 4. Draw outer neon glow stroke if enabled
        if (neonGlow) {
            val glowStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = strokeWidthPx * 2.4f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                color = Color.argb(
                    (Color.alpha(strokeColor) * 0.30f).toInt(),
                    Color.red(strokeColor),
                    Color.green(strokeColor),
                    Color.blue(strokeColor)
                )
            }
            canvas.drawPath(linePath, glowStrokePaint)
        }

        // 5. Anti-aliased main stroke line
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            this.color = strokeColor
        }
        canvas.drawPath(linePath, strokePaint)

        // 6. Draw glowing live endpoint dot if requested
        if (drawLiveDot && points.isNotEmpty()) {
            val lastPoint = points.last()
            val dotX = lastPoint.x
            val dotY = lastPoint.y

            // Outer halo
            val haloRadius = if (neonGlow) dpToPx(context, 8.0f) else dpToPx(context, 6.0f)
            val haloAlpha = if (neonGlow) 0.50f else 0.35f
            val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.argb(
                    (Color.alpha(strokeColor) * haloAlpha).toInt(),
                    Color.red(strokeColor),
                    Color.green(strokeColor),
                    Color.blue(strokeColor)
                )
            }
            canvas.drawCircle(dotX, dotY, haloRadius, haloPaint)

            // Solid core
            val coreRadius = dpToPx(context, 3.2f)
            val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = strokeColor
            }
            canvas.drawCircle(dotX, dotY, coreRadius, corePaint)

            // Pinpoint center highlight
            val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.WHITE
            }
            canvas.drawCircle(dotX, dotY, dpToPx(context, 1.2f), pinPaint)
        }

        return bitmap
    }
}
