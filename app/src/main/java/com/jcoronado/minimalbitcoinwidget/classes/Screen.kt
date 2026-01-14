package com.jcoronado.minimalbitcoinwidget.classes

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class Screen : NavKey {
    @Serializable
    object Dashboard : Screen()

    @Serializable
    sealed class Settings : Screen() {
        @Serializable
        object Main : Settings()
        @Serializable
        object Data : Settings()
        @Serializable
        object Appearance : Settings()
        @Serializable
        object About : Settings()
    }
}

data class NavItem(
    val route: Screen,
    @param:DrawableRes val icon: Int,
    @param:StringRes val label: Int
)

 data class SettingsNavItem(
     val route: Screen.Settings,
     @param:DrawableRes val icon: Int,
     @param:StringRes val label: Int,
     val subtitle: String,
 )