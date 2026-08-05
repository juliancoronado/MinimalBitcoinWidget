package com.jcoronado.minimalbitcoinwidget.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.TextStyle

data class DigitChar(
    val char: Char,
    val isDigit: Boolean,
    val placeIndex: Int
) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is DigitChar -> char == other.char && isDigit == other.isDigit && placeIndex == other.placeIndex
            else -> super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = char.hashCode()
        result = 31 * result + isDigit.hashCode()
        result = 31 * result + placeIndex
        return result
    }
}

/**
 * A Jetpack Compose component that renders a price string with per-digit slide animations.
 *
 * When rawPrice increases, changing digits slide up (entering from bottom, exiting to top).
 * When rawPrice decreases, changing digits slide down (entering from top, exiting to bottom).
 * Non-digit characters (delimiters like commas and decimal points) remain stationary.
 *
 * @param priceText Formatted price string (e.g. "96,432.50").
 * @param rawPrice Numeric price value used to calculate animation direction (increase vs decrease).
 * @param style Typography style for the price digits.
 * @param color Text color for the price digits.
 * @param modifier Modifier for the layout container.
 * @param animate Whether animations are enabled (set to false during scrubbing for instant updates).
 */
@Composable
fun AnimatedPriceText(
    priceText: String,
    rawPrice: Double,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    var prevPrice by remember { mutableDoubleStateOf(rawPrice) }
    val isIncrease = rawPrice >= prevPrice

    SideEffect {
        prevPrice = rawPrice
    }

    val platformLocale = LocalLocale.current.platformLocale
    val charList = remember(priceText, platformLocale) {
        val localeDecimalSep = java.text.DecimalFormatSymbols.getInstance(platformLocale).decimalSeparator
        val lastNonDigitIndex = priceText.indexOfLast { !it.isDigit() }
        val decimalPos = if (priceText.contains(localeDecimalSep)) {
            priceText.indexOf(localeDecimalSep)
        } else if (lastNonDigitIndex >= 0 && priceText.length - lastNonDigitIndex <= 3) {
            lastNonDigitIndex
        } else {
            priceText.length
        }

        priceText.mapIndexed { index, char ->
            val place = if (char.isDigit()) {
                if (index < decimalPos) (decimalPos - 1 - index) else (decimalPos - index)
            } else 0
            DigitChar(
                char = char,
                isDigit = char.isDigit(),
                placeIndex = place
            )
        }
    }

    Row(
        modifier = modifier.animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        charList.forEach { digit ->
            if (!animate || !digit.isDigit) {
                Text(
                    text = digit.char.toString(),
                    style = style,
                    color = color
                )
            } else {
                AnimatedContent(
                    targetState = digit,
                    transitionSpec = {
                        if (isIncrease) {
                            // Price increase: enter from bottom (+height), exit to top (-height)
                            slideInVertically { height -> height } togetherWith slideOutVertically { height -> -height }
                        } else {
                            // Price decrease: enter from top (-height), exit to bottom (+height)
                            slideInVertically { height -> -height } togetherWith slideOutVertically { height -> height }
                        }
                    },
                    label = "DigitAnimation"
                ) { targetDigit ->
                    Text(
                        text = targetDigit.char.toString(),
                        style = style,
                        color = color
                    )
                }
            }
        }
    }
}
