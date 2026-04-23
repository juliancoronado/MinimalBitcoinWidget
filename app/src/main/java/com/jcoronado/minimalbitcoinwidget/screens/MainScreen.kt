package com.jcoronado.minimalbitcoinwidget.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.PriceUiState
import com.jcoronado.minimalbitcoinwidget.ui.theme.googleSansCodeFontFamily
import com.jcoronado.minimalbitcoinwidget.utils.FormatUtils
import java.text.SimpleDateFormat
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.font.FontWeight
import com.jcoronado.minimalbitcoinwidget.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(uiState: PriceUiState, onRefresh: () -> Unit, onAddWidgetClick: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }, colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            PriceCard(uiState, onRefresh = onRefresh)
            // TODO - shortcuts section - put this into a separate component later
            Text(
                // TODO - convert to stringResource and provide translations in strings.xml files
                "Shortcuts",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            AddWidgetShortcut(onClick = onAddWidgetClick)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddWidgetShortcut(onClick: () -> Unit) {
    val view = LocalView.current
    // TODO - convert to stringResource and provide translations in strings.xml files
    SegmentedListItem(
        // since there's only 1 item in this section, round the corner manually
        shapes = ListItemDefaults.shapes(shape = RoundedCornerShape(16.dp)),
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        },
        content = {
            Text(
                "Add to Home Screen", fontWeight = FontWeight.Bold
            )
        },
        supportingContent = {
            Text(
                "Tap here to pin a widget to your home screen",
            )
        })
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PriceCard(uiState: PriceUiState, onRefresh: () -> Unit) {
    val view = LocalView.current
    val colors =
        ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surface)
    val formattedTime = SimpleDateFormat(
        "hh:mm:ss a", LocalLocale.current.platformLocale
    ).format(uiState.lastUpdated)
    SegmentedListItem(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onRefresh()
        }, colors = colors, shapes = ListItemDefaults.segmentedShapes(
            index = 0, count = 2
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val priceData =
                    FormatUtils.formatPriceSeparated(uiState.price, uiState.selectedCurrency)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(all = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_currency_bitcoin_24),
                        contentDescription = stringResource(R.string.bitcoin_icon_description),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "/",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = googleSansCodeFontFamily),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = uiState.selectedCurrency.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = googleSansCodeFontFamily),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "・",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = googleSansCodeFontFamily),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = stringResource(uiState.changeIntervalLabelResId),
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = googleSansCodeFontFamily),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val priceStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = googleSansCodeFontFamily
                    )
                    val symbolStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = googleSansCodeFontFamily,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    if (priceData.symbolAtStart) {
                        // SYMBOL LEFT (e.g. $ 95,000)
                        Text(
                            text = priceData.symbol,
                            style = symbolStyle,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = priceData.price,
                            style = priceStyle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        // SYMBOL RIGHT (e.g. 95.000 €)
                        Text(
                            text = priceData.price,
                            style = priceStyle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = priceData.symbol,
                            style = symbolStyle,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(all = 12.dp)
                ) {
                    if (uiState.percentageChange > 0) Icon(
                        painter = painterResource(id = R.drawable.rounded_trending_up_24),
                        contentDescription = stringResource(R.string.trending_up_icon_description),
                        tint = MaterialTheme.colorScheme.primary
                    ) else if (uiState.percentageChange < 0) Icon(
                        painter = painterResource(id = R.drawable.rounded_trending_down_24),
                        contentDescription = stringResource(R.string.trending_down_icon_description),
                        tint = MaterialTheme.colorScheme.error
                    ) else Icon(
                        painter = painterResource(id = R.drawable.rounded_trending_flat_24),
                        contentDescription = stringResource(R.string.trending_flat_icon_description),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = FormatUtils.formatChange(uiState.percentageChange),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = googleSansCodeFontFamily),
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // TODO - revisit this and see if we still want to keep this animation
            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(durationMillis = 100)),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                LoadingIndicator(
                    modifier = Modifier.size(32.dp),
                    polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons.shuffled()
                )
            }

            if (uiState.errorMessage != null) {
                Text(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        SegmentedListItem(
            onClick = {}, colors = colors, shapes = ListItemDefaults.segmentedShapes(
                index = 1, count = 2
            )
        ) {
            Text(stringResource(R.string.last_updated, formattedTime))
        }
    }

}

@Preview(showSystemUi = true)
@Composable
fun MainScreenPreview() {
    AppTheme(darkTheme = false, dynamicColors = true) {
        MainScreen(
            uiState = PriceUiState(
                price = 52849.10, percentageChange = 2.03, isLoading = false
            ), onRefresh = { }, onAddWidgetClick = { })
    }
}
