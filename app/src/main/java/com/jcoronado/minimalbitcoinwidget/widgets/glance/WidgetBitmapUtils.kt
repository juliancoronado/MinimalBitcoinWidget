package com.jcoronado.minimalbitcoinwidget.widgets.glance

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.jcoronado.minimalbitcoinwidget.classes.WidgetFont
import kotlin.math.ceil

object WidgetBitmapUtils {

    fun getTypeface(context: Context, widgetFont: WidgetFont, isBold: Boolean = false): Typeface? {
        if (widgetFont.fontResId == null) {
            return null
        }
        val baseTypeface = try {
            ResourcesCompat.getFont(context, widgetFont.fontResId) ?: return null
        } catch (_: Exception) {
            return null
        }

        return if (isBold) {
            Typeface.create(baseTypeface, 700, false)
        } else {
            Typeface.create(baseTypeface, 400, false)
        }
    }

    /**
     * Calculates the optimal font size (in SP) for the price and currency symbol
     * based on price magnitude and whether the font is monospaced (e.g. Google Sans Code).
     */
    fun getWidgetPriceFontSize(price: Double, isMonospaced: Boolean): Pair<Float, Float> {
        val (priceSp, symbolSp) = if (isMonospaced) {
            when {
                price >= 1_000_000 -> Pair(16f, 13f)
                price >= 100_000 -> Pair(18f, 14f)
                else -> Pair(20f, 15f)
            }
        } else {
            when {
                price >= 1_000_000 -> Pair(19f, 15f)
                price >= 100_000 -> Pair(21f, 15f)
                else -> Pair(24f, 16f)
            }
        }
        return Pair(priceSp, symbolSp)
    }

    /**
     * Returns the optimal secondary font size (in SP) for headers and percentage changes.
     */
    fun getWidgetSecondaryFontSize(isMonospaced: Boolean): Float {
        return if (isMonospaced) 11f else 12f
    }

    fun createTextBitmap(
        context: Context,
        text: String,
        fontSizeSp: Float,
        typeface: Typeface?,
        isBold: Boolean = false
    ): Bitmap {
        val textSizePx = android.util.TypedValue.applyDimension(
            android.util.TypedValue.COMPLEX_UNIT_SP,
            fontSizeSp,
            context.resources.displayMetrics
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.textSize = textSizePx
            this.color = Color.WHITE
            if (isBold) {
                if (typeface == null || typeface.weight < 600) {
                    this.isFakeBoldText = true
                }
            } else {
                this.isFakeBoldText = false
            }
        }

        val width = ceil(paint.measureText(text)).toInt().coerceAtLeast(1)
        val fontMetrics = paint.fontMetrics
        val height = ceil(fontMetrics.descent - fontMetrics.ascent).toInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, 0f, -fontMetrics.ascent, paint)
        return bitmap
    }
}
