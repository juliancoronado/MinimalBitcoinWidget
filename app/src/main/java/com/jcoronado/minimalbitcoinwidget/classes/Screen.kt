package com.jcoronado.minimalbitcoinwidget.classes

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class Screen : NavKey {
    @Serializable
    object Dashboard : Screen()

    @Serializable
    object Settings : Screen()

    @Serializable
    object DeveloperOptions : Screen()
}

data class NavItem(
    val route: Screen,
    @param:DrawableRes val icon: Int,
    @param:StringRes val label: Int
)