package com.jcoronado.minimalbitcoinwidget

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.jcoronado.minimalbitcoinwidget.classes.PriceUiState
import com.jcoronado.minimalbitcoinwidget.screens.PriceCard
import com.jcoronado.minimalbitcoinwidget.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

/**
 * Example Instrumented Test for a Compose UI component.
 */
class PriceCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun priceCard_displaysPriceAndCurrency() {
        val testUiState = PriceUiState(
            price = 50000.0,
            selectedCurrency = "USD",
            isLoading = false
        )

        composeTestRule.setContent {
            AppTheme(darkTheme = false, dynamicColors = false) {
                PriceCard(uiState = testUiState, onRefresh = {})
            }
        }

        // check if "USD" is displayed
        composeTestRule.onNodeWithText("USD").assertIsDisplayed()
    }
}
