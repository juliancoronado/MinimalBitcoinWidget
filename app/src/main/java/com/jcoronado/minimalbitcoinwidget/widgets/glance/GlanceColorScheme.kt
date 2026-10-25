package com.jcoronado.minimalbitcoinwidget.widgets.glance

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.color.ColorProviders as GlanceColorProviders
import androidx.glance.color.colorProviders
import androidx.glance.material3.ColorProviders
import androidx.glance.unit.ResourceColorProvider
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

    @SuppressLint("RestrictedApi")
    val dynamicLightColors = colorProviders(
        primary = ResourceColorProvider(R.color.widget_dynamic_light_primary),
        onPrimary = ResourceColorProvider(R.color.widget_dynamic_light_on_primary),
        primaryContainer = ResourceColorProvider(R.color.widget_dynamic_light_primary_container),
        onPrimaryContainer = ResourceColorProvider(R.color.widget_dynamic_light_on_primary_container),
        secondary = ResourceColorProvider(R.color.widget_dynamic_light_secondary),
        onSecondary = ResourceColorProvider(R.color.widget_dynamic_light_on_secondary),
        secondaryContainer = ResourceColorProvider(R.color.widget_dynamic_light_secondary_container),
        onSecondaryContainer = ResourceColorProvider(R.color.widget_dynamic_light_on_secondary_container),
        tertiary = ResourceColorProvider(R.color.widget_dynamic_light_tertiary),
        onTertiary = ResourceColorProvider(R.color.widget_dynamic_light_on_tertiary),
        tertiaryContainer = ResourceColorProvider(R.color.widget_dynamic_light_tertiary_container),
        onTertiaryContainer = ResourceColorProvider(R.color.widget_dynamic_light_on_tertiary_container),
        error = ResourceColorProvider(R.color.widget_dynamic_light_error),
        errorContainer = ResourceColorProvider(R.color.widget_dynamic_light_error_container),
        onError = ResourceColorProvider(R.color.widget_dynamic_light_on_error),
        onErrorContainer = ResourceColorProvider(R.color.widget_dynamic_light_on_error_container),
        background = ResourceColorProvider(R.color.widget_dynamic_light_background),
        onBackground = ResourceColorProvider(R.color.widget_dynamic_light_on_background),
        surface = ResourceColorProvider(R.color.widget_dynamic_light_surface),
        onSurface = ResourceColorProvider(R.color.widget_dynamic_light_on_surface),
        surfaceVariant = ResourceColorProvider(R.color.widget_dynamic_light_surface_variant),
        onSurfaceVariant = ResourceColorProvider(R.color.widget_dynamic_light_on_surface_variant),
        outline = ResourceColorProvider(R.color.widget_dynamic_light_outline),
        inverseOnSurface = ResourceColorProvider(R.color.widget_dynamic_light_inverse_on_surface),
        inverseSurface = ResourceColorProvider(R.color.widget_dynamic_light_inverse_surface),
        inversePrimary = ResourceColorProvider(R.color.widget_dynamic_light_inverse_primary),
        widgetBackground = ResourceColorProvider(R.color.widget_dynamic_light_widget_background),
    )

    @SuppressLint("RestrictedApi")
    val dynamicDarkColors = colorProviders(
        primary = ResourceColorProvider(R.color.widget_dynamic_dark_primary),
        onPrimary = ResourceColorProvider(R.color.widget_dynamic_dark_on_primary),
        primaryContainer = ResourceColorProvider(R.color.widget_dynamic_dark_primary_container),
        onPrimaryContainer = ResourceColorProvider(R.color.widget_dynamic_dark_on_primary_container),
        secondary = ResourceColorProvider(R.color.widget_dynamic_dark_secondary),
        onSecondary = ResourceColorProvider(R.color.widget_dynamic_dark_on_secondary),
        secondaryContainer = ResourceColorProvider(R.color.widget_dynamic_dark_secondary_container),
        onSecondaryContainer = ResourceColorProvider(R.color.widget_dynamic_dark_on_secondary_container),
        tertiary = ResourceColorProvider(R.color.widget_dynamic_dark_tertiary),
        onTertiary = ResourceColorProvider(R.color.widget_dynamic_dark_on_tertiary),
        tertiaryContainer = ResourceColorProvider(R.color.widget_dynamic_dark_tertiary_container),
        onTertiaryContainer = ResourceColorProvider(R.color.widget_dynamic_dark_on_tertiary_container),
        error = ResourceColorProvider(R.color.widget_dynamic_dark_error),
        errorContainer = ResourceColorProvider(R.color.widget_dynamic_dark_error_container),
        onError = ResourceColorProvider(R.color.widget_dynamic_dark_on_error),
        onErrorContainer = ResourceColorProvider(R.color.widget_dynamic_dark_on_error_container),
        background = ResourceColorProvider(R.color.widget_dynamic_dark_background),
        onBackground = ResourceColorProvider(R.color.widget_dynamic_dark_on_background),
        surface = ResourceColorProvider(R.color.widget_dynamic_dark_surface),
        onSurface = ResourceColorProvider(R.color.widget_dynamic_dark_on_surface),
        surfaceVariant = ResourceColorProvider(R.color.widget_dynamic_dark_surface_variant),
        onSurfaceVariant = ResourceColorProvider(R.color.widget_dynamic_dark_on_surface_variant),
        outline = ResourceColorProvider(R.color.widget_dynamic_dark_outline),
        inverseOnSurface = ResourceColorProvider(R.color.widget_dynamic_dark_inverse_on_surface),
        inverseSurface = ResourceColorProvider(R.color.widget_dynamic_dark_inverse_surface),
        inversePrimary = ResourceColorProvider(R.color.widget_dynamic_dark_inverse_primary),
        widgetBackground = ResourceColorProvider(R.color.widget_dynamic_dark_widget_background),
    )

    /**
     * Resolves the appropriate [ColorProviders] based on [WidgetColorStyle] and [WidgetTheme].
     */
    @Composable
    fun colorsFor(context: Context, colorStyle: WidgetColorStyle, widgetTheme: WidgetTheme): GlanceColorProviders {
        return when (colorStyle) {
            WidgetColorStyle.SOLID -> when (widgetTheme) {
                WidgetTheme.LIGHT -> solidLightColors
                WidgetTheme.DARK -> solidDarkColors
                WidgetTheme.SYSTEM -> {
                    if (Build.VERSION.SDK_INT >= 31) {
                        solidSystemColors
                    } else {
                        val sysUiMode = Resources.getSystem().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                        if (sysUiMode == Configuration.UI_MODE_NIGHT_YES) solidDarkColors else solidLightColors
                    }
                }
            }
            WidgetColorStyle.DYNAMIC -> {
                if (Build.VERSION.SDK_INT >= 31) {
                    when (widgetTheme) {
                        WidgetTheme.LIGHT -> dynamicLightColors
                        WidgetTheme.DARK -> dynamicDarkColors
                        WidgetTheme.SYSTEM -> GlanceTheme.colors
                    }
                } else {
                    when (widgetTheme) {
                        WidgetTheme.LIGHT -> defaultLightColors
                        WidgetTheme.DARK -> defaultDarkColors
                        WidgetTheme.SYSTEM -> {
                            val sysUiMode = Resources.getSystem().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                            if (sysUiMode == Configuration.UI_MODE_NIGHT_YES) defaultDarkColors else defaultLightColors
                        }
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
        val context = LocalContext.current

        // On Android 12+ (API 31+) with system corner radius, use native dynamic theme backgrounds
        if (Build.VERSION.SDK_INT >= 31 && systemCornerRadiusDefined) {
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
                    when (widgetTheme) {
                        WidgetTheme.LIGHT -> modifier.background(fallbackLightBackground)
                        WidgetTheme.DARK -> modifier.background(fallbackDarkBackground)
                        WidgetTheme.SYSTEM -> modifier.background(GlanceTheme.colors.widgetBackground)
                    }
                }
            }
        }

        // On API < 31, determine night mode at composition time so background, text, and sparkline
        // stay in lockstep without contrast clashes when the system theme changes.
        val isNight = when (widgetTheme) {
            WidgetTheme.DARK -> true
            WidgetTheme.LIGHT -> false
            WidgetTheme.SYSTEM -> {
                val sysUiMode = Resources.getSystem().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                sysUiMode == Configuration.UI_MODE_NIGHT_YES
            }
        }

        val bgDrawable = when (colorStyle) {
            WidgetColorStyle.SOLID -> {
                if (isNight) R.drawable.glance_widget_bg_solid_dark else R.drawable.glance_widget_bg_solid_light
            }
            WidgetColorStyle.DYNAMIC -> {
                if (isNight) R.drawable.glance_widget_bg_dynamic_dark else R.drawable.glance_widget_bg_dynamic_light
            }
        }

        return modifier.background(ImageProvider(bgDrawable))
    }
}