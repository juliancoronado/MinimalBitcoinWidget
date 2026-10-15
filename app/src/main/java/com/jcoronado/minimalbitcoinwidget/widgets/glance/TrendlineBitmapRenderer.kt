package com.jcoronado.minimalbitcoinwidget.widgets.glance

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Shader
import android.util.TypedValue
import com.jcoronado.minimalbitcoinwidget.ui.components.PLACEHOLDER_SPARKLINE_PRICES
import com.jcoronado.minimalbitcoinwidget.ui.components.SparklineScaleCalculator

/**
 * Gradient fill style for the trendline chart.
 */
enum class TrendlineFillStyle {
    SUBTLE, // Soft linear gradient fade
    NONE
}

/**
 * High-performance off-screen Bitmap renderer for sparkline trendlines in Glance widgets.
 * Supports smooth Catmull-Rom splines, dashed baselines, live glowing dots, and volumetric gradients.
 */
object TrendlineBitmapRenderer {

    const val DEFAULT_STROKE_WIDTH_DP = 2.5f

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
     * Resolves the trendline line color adhering to MBW theme settings and dynamic colors.
     */
    fun resolveTrendColor(
        context: Context,
        isPositive: Boolean,
        colorStyle: com.jcoronado.minimalbitcoinwidget.classes.WidgetColorStyle,
        widgetTheme: com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme
    ): Int {
        val isDark = when (widgetTheme) {
            com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme.DARK -> true
            com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme.LIGHT -> false
            com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme.SYSTEM -> {
                val nightModeFlags = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }

        return if (colorStyle == com.jcoronado.minimalbitcoinwidget.classes.WidgetColorStyle.SOLID) {
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
     * Backwards-compatible overload for existing calls.
     */
    fun createTrendlineBitmap(
        context: Context,
        prices: List<Double>,
        strokeColor: Int,
        widthPx: Int,
        heightPx: Int,
        includeGradient: Boolean = true,
        strokeWidthDp: Float = DEFAULT_STROKE_WIDTH_DP
    ): Bitmap {
        return createTrendlineBitmap(
            context = context,
            prices = prices,
            strokeColor = strokeColor,
            widthPx = widthPx,
            heightPx = heightPx,
            fillStyle = if (includeGradient) TrendlineFillStyle.SUBTLE else TrendlineFillStyle.NONE,
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
    fun createTrendlineBitmap(
        context: Context,
        prices: List<Double>,
        strokeColor: Int,
        widthPx: Int,
        heightPx: Int,
        fillStyle: TrendlineFillStyle = TrendlineFillStyle.SUBTLE,
        strokeWidthDp: Float = DEFAULT_STROKE_WIDTH_DP,
        drawDashedBaseline: Boolean = false,
        drawLiveDot: Boolean = false,
        neonGlow: Boolean = false,
        smoothingRounds: Int = 2
    ): Bitmap {
        val w = widthPx.coerceAtLeast(10)
        val h = heightPx.coerceAtLeast(10)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rawPrices = if (prices.size < 2) PLACEHOLDER_SPARKLINE_PRICES else prices

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
            val y = h.toFloat() - bottomPaddingPx - (yFraction * usableHeight)
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

        // 3. Draw gradient fill under trendline
        if (fillStyle != TrendlineFillStyle.NONE && points.size >= 2) {
            val fillPath = Path(linePath).apply {
                lineTo(points.last().x, h.toFloat())
                lineTo(points.first().x, h.toFloat())
                close()
            }

            when (fillStyle) {
                TrendlineFillStyle.SUBTLE -> {
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
                TrendlineFillStyle.NONE -> Unit
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
