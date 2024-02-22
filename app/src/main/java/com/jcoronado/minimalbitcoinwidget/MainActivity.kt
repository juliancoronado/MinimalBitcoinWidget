package com.jcoronado.minimalbitcoinwidget

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jcoronado.minimalbitcoinwidget.ui.theme.BTCPriceWidgetTheme

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")
val PRICE_KEY = doublePreferencesKey("price")
val CHANGE_KEY = doublePreferencesKey("change")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MinimalBitcoinWidget()
        }
    }
}

@Composable
fun MinimalBitcoinWidget() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    200
                )
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.End,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.End,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        },
    ) {
        composable(
            "main",
        ) {
            MainPage(
                onSettingsButtonPressed = {
                    navController.navigate("settings")
                }
            )
        }
        composable(
            "settings",
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Companion.Start,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            },
        ) {
            SettingsPage(
                onBackButtonPressed = {
                    navController.popBackStack()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(onSettingsButtonPressed: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val apiHelper = ApiHelper()
    val btcDataStoreManager = BtcDataStoreManager(LocalContext.current)
    val btcPriceViewModel = BTCPriceViewModel(apiHelper = apiHelper, btcDataStoreManager = btcDataStoreManager)

    BTCPriceWidgetTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    },
                    actions = {
                        IconButton(onClick = onSettingsButtonPressed) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = stringResource(id = R.string.content_description_settings)
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                ) {
                    LaunchedEffect(Unit) {
                        btcPriceViewModel.fetchDataAsync()
                    }
                    PriceCard(btcPriceViewModel)
                    Button(
                        onClick = {
                            btcPriceViewModel.printStoredValues()
                        },
                        content = {Text("Press Me")}
                    )
                    // display snackbar in case of an error
                    if (btcPriceViewModel.error.value.isNotEmpty()) {
                        SnackbarWidget(
                            message = btcPriceViewModel.error.value,
                            actionLabel = stringResource(id = R.string.dismiss)
                        )
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceCard(btcPriceViewModel: BTCPriceViewModel) {

    Card(
        onClick = {
            btcPriceViewModel.fetchDataAsync()
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
        ),
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp)
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 8.dp)
        ) {
            CardHeader(selectedCurrency = "USD")
            CardPrice(symbol = "$", btcPriceViewModel = btcPriceViewModel)
            CardDetails(btcPriceViewModel = btcPriceViewModel)
        }
    }
}

@Composable
fun CardHeader(selectedCurrency: String) {
    Row(
        verticalAlignment = CenterVertically
    ) {
        Box(modifier = Modifier.height(16.dp)) {
            Image(
                modifier = Modifier.padding(1.dp),
                painter = painterResource(id = R.drawable.bitcoin_icon),
                contentDescription = ""
            )
        }
        Box(modifier = Modifier.size(2.dp))
        Text(" Bitcoin / $selectedCurrency")
    }
}

@Composable
fun CardPrice(symbol: String, btcPriceViewModel: BTCPriceViewModel) {

    val myStyle =
        MaterialTheme.typography.bodyLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold)

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = symbol, style = myStyle)
        Text(text = String.format("%,.2f", btcPriceViewModel.priceValue), style = myStyle)
    }
}

@Composable
fun CardDetails(btcPriceViewModel: BTCPriceViewModel) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val placeholderModifier = Modifier.size(20.dp) // Size of progress indicator

        Text(text = "24h: ${String.format("%.2f", btcPriceViewModel.changeValue)}%")
        if (btcPriceViewModel.loading.value) {
            Box(
                modifier = Modifier.size(20.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.wrapContentSize(),
                    color = MaterialTheme.colorScheme.secondary,
                    strokeWidth = 2.dp
                )
            }
        } else {
            Box(placeholderModifier)
        }
    }
}

@Composable
@Preview
fun AppPreview() {
    MinimalBitcoinWidget()
}