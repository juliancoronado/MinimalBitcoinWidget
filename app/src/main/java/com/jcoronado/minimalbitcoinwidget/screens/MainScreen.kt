package com.jcoronado.minimalbitcoinwidget.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(uiState: PriceUiState, onRefresh: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = {
                Text(stringResource(R.string.app_name))
            })
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            PriceCard(uiState, onRefresh = onRefresh)
            HorizontalDivider(thickness = DividerDefaults.Thickness / 2)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PriceCard(uiState: PriceUiState, onRefresh: () -> Unit) {
    Surface(
        onClick = onRefresh,
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(all = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_currency_bitcoin_24),
                        contentDescription = "Bitcoin Icon",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "/",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = uiState.selectedCurrency.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = FormatUtils.formatPrice(uiState.price),
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = googleSansCodeFontFamily),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(all = 12.dp)
                ) {
                    if (uiState.percentageChange > 0) Icon(
                        painter = painterResource(id = R.drawable.rounded_trending_up_24),
                        contentDescription = "Trending up icon",
                        tint = MaterialTheme.colorScheme.primary
                    ) else if (uiState.percentageChange < 0) Icon(
                        painter = painterResource(id = R.drawable.rounded_trending_down_24),
                        contentDescription = "Trending down icon",
                        tint = MaterialTheme.colorScheme.error
                    ) else Icon(
                        painter = painterResource(id = R.drawable.rounded_trending_flat_24),
                        contentDescription = "Trending flat icon",
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

            if (uiState.isLoading) {
                LoadingIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(32.dp),
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
}

// @Preview(showBackground = true, showSystemUi = true)
// @Composable
// fun MainScreenPreview() {
//     MainScreen(
//         uiState = PriceUiState(
//             price = 95234.12, percentageChange = 0.45, isLoading = false
//         )) {}
// }


@Preview(showBackground = true)
@Composable
fun PriceCardPreviewLoadedPos() {
    MaterialTheme {
        PriceCard(
            uiState = PriceUiState(
                price = 95234.12, percentageChange = 0.45, isLoading = false
            ), onRefresh = { })
    }
}

@Preview(showBackground = true)
@Composable
fun PriceCardPreviewLoadedNeg() {
    MaterialTheme {
        PriceCard(
            uiState = PriceUiState(
                price = 95234.12, percentageChange = -0.32, isLoading = false
            ), onRefresh = { })
    }
}

@Preview(showBackground = true)
@Composable
fun PriceCardPreviewLoading() {
    MaterialTheme {
        PriceCard(
            uiState = PriceUiState(
                price = 95234.12, percentageChange = 2.45, isLoading = true
            ), onRefresh = { })
    }
}

@Preview(showBackground = true)
@Composable
fun PriceCardPreviewError() {
    MaterialTheme {
        PriceCard(
            uiState = PriceUiState(
                price = 95234.12,
                percentageChange = 1.95,
                isLoading = false,
                errorMessage = "Error: HTTP 400"
            ), onRefresh = { })
    }
}