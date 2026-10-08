package com.jcoronado.minimalbitcoinwidget.widgets.glance

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.color.ColorProviders as GlanceColorProviders
import androidx.glance.material3.ColorProviders
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.WidgetColorStyle
import com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme
import com.jcoronado.minimalbitcoinwidget.ui.theme.darkScheme
import com.jcoronado.minimalbitcoinwidget.ui.theme.lightScheme

object GlanceColorScheme {

    val solidLightScheme: ColorScheme = lightColorScheme(
        surface = Color.White,
        onSurface = Color.Black,
        surfaceContainer = Color.White,
        secondary = Color(0xFF44474E),
        primary = Color(0xFF00875A),
        error = Color(0xFFDE350B)
    )

    val solidDarkScheme: ColorScheme = darkColorScheme(
        surface = Color.Black,
        onSurface = Color.White,
        surfaceContainer = Color.Black,
        secondary = Color(0xFFC4C6D0),
        primary = Color(0xFF36B37E),
        error = Color(0xFFFF5630)
    )

    val solidLightColors = ColorProviders(light = solidLightScheme, dark = solidLightScheme)
    val solidDarkColors = ColorProviders(light = solidDarkScheme, dark = solidDarkScheme)
    val solidSystemColors = ColorProviders(light = solidLightScheme, dark = solidDarkScheme)

    val defaultLightColors = ColorProviders(light = lightScheme, dark = lightScheme)
    val defaultDarkColors = ColorProviders(light = darkScheme, dark = darkScheme)
    val colors = ColorProviders(light = lightScheme, dark = darkScheme)

    val solidLightBackground = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFFFFFFFF))
    val solidDarkBackground = ColorProvider(day = Color(0xFF000000), night = Color(0xFF000000))
    val solidSystemBackground = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF000000))

    val fallbackLightBackground = ColorProvider(day = Color(0xFFF8F9FF), night = Color(0xFFF8F9FF))
    val fallbackDarkBackground = ColorProvider(day = Color(0xFF1D2024), night = Color(0xFF1D2024))

    /**
     * Resolves the appropriate [ColorProviders] based on [WidgetColorStyle] and [WidgetTheme].
     */
    fun colorsFor(context: Context, colorStyle: WidgetColorStyle, widgetTheme: WidgetTheme): GlanceColorProviders {
        return when (colorStyle) {
            WidgetColorStyle.SOLID -> when (widgetTheme) {
                WidgetTheme.LIGHT -> solidLightColors
                WidgetTheme.DARK -> solidDarkColors
                WidgetTheme.SYSTEM -> solidSystemColors
            }
            WidgetColorStyle.DYNAMIC -> {
                if (Build.VERSION.SDK_INT >= 31) {
                    val dynLight = dynamicLightColorScheme(context)
                    val dynDark = dynamicDarkColorScheme(context)
                    when (widgetTheme) {
                        WidgetTheme.LIGHT -> ColorProviders(light = dynLight, dark = dynLight)
                        WidgetTheme.DARK -> ColorProviders(light = dynDark, dark = dynDark)
                        WidgetTheme.SYSTEM -> ColorProviders(light = dynLight, dark = dynDark)
                    }
                } else {
                    when (widgetTheme) {
                        WidgetTheme.LIGHT -> defaultLightColors
                        WidgetTheme.DARK -> defaultDarkColors
                        WidgetTheme.SYSTEM -> colors
                    }
                }
            }
        }
    }

    /**
     * Applies the appropriate background modifier based on [WidgetColorStyle], [WidgetTheme],
     * and system corner radius support.
     */
    @Composable
    fun applyBackground(
        modifier: GlanceModifier,
        colorStyle: WidgetColorStyle,
        widgetTheme: WidgetTheme,
        systemCornerRadiusDefined: Boolean
    ): GlanceModifier {
        return when (colorStyle) {
            WidgetColorStyle.SOLID -> {
                val solidBg = when (widgetTheme) {
                    WidgetTheme.LIGHT -> solidLightBackground
                    WidgetTheme.DARK -> solidDarkBackground
                    WidgetTheme.SYSTEM -> solidSystemBackground
                }
                modifier.background(solidBg)
            }
            WidgetColorStyle.DYNAMIC -> {
                if (Build.VERSION.SDK_INT >= 31 && systemCornerRadiusDefined) {
                    modifier.background(GlanceTheme.colors.widgetBackground)
                } else {
                    when (widgetTheme) {
                        WidgetTheme.LIGHT -> modifier.background(fallbackLightBackground)
                        WidgetTheme.DARK -> modifier.background(fallbackDarkBackground)
                        WidgetTheme.SYSTEM -> modifier.background(ImageProvider(R.drawable.glance_widget_bg))
                    }
                }
            }
        }
    }
}