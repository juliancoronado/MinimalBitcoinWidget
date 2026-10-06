package com.jcoronado.minimalbitcoinwidget.widgets.glance

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.jcoronado.minimalbitcoinwidget.classes.WidgetFont
import com.jcoronado.minimalbitcoinwidget.utils.FormatUtils
import com.jcoronado.minimalbitcoinwidget.utils.FormattedPrice
import kotlin.math.ceil

object WidgetBitmapUtils {

    const val WIDGET_LETTER_SPACING_EM = 0.03f

    fun getTypeface(context: Context, widgetFont: WidgetFont): Typeface? {
        if (widgetFont.fontResId == null) {
            return null
        }
        val baseTypeface = try {
            ResourcesCompat.getFont(context, widgetFont.fontResId) ?: return null
        } catch (_: Exception) {
            return null
        }

        return Typeface.create(baseTypeface, 400, false)
    }

    /**
     * Calculates the optimal font size (in SP) for the price and currency symbol
     * based on the total visual character length of the rendered price and symbol,
     * scaled by an optional scale factor.
     */
    fun getWidgetPriceFontSize(formattedPrice: FormattedPrice, scaleFactor: Float = 1.0f): Pair<Float, Float> {
        return getWidgetPriceFontSize(formattedPrice.price, formattedPrice.symbol, scaleFactor)
    }

    /**
     * Calculates the optimal font size (in SP) for the price and currency symbol
     * based on the total character length of the price text and symbol,
     * scaled by an optional scale factor.
     */
    fun getWidgetPriceFontSize(
        priceText: String,
        symbolText: String,
        scaleFactor: Float = 1.0f
    ): Pair<Float, Float> {
        val totalLength = priceText.length + symbolText.length
        val (basePriceSp, baseSymbolSp) = when {
            totalLength <= 11 -> Pair(24f, 16f)
            totalLength <= 13 -> Pair(21f, 15f)
            totalLength <= 15 -> Pair(18f, 14f)
            else -> Pair(15f, 12f)
        }
        return Pair(basePriceSp * scaleFactor, baseSymbolSp * scaleFactor)
    }

    /**
     * Overload calculating optimal font size (in SP) given a raw price and currency code.
     */
    fun getWidgetPriceFontSize(
        price: Double,
        currencyCode: String = "USD",
        scaleFactor: Float = 1.0f
    ): Pair<Float, Float> {
        val formatted = FormatUtils.formatPriceSeparated(price, currencyCode)
        return getWidgetPriceFontSize(formatted, scaleFactor)
    }

    /**
     * Returns the optimal secondary font size (in SP) for headers and percentage changes.
     */
    fun getWidgetSecondaryFontSize(): Float {
        return 12f
    }

    fun createTextBitmap(
        context: Context,
        text: String,
        fontSizeSp: Float,
        typeface: Typeface?,
        letterSpacingEm: Float = WIDGET_LETTER_SPACING_EM
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
            if (letterSpacingEm > 0f) {
                this.letterSpacing = letterSpacingEm
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
