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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingFlat
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jcoronado.minimalbitcoinwidget.ui.theme.BTCPriceWidgetTheme

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

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
    val dataStoreManager = DataStoreManager(LocalContext.current)
    val priceViewModel = PriceViewModel(dataStoreManager = dataStoreManager)

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
                priceViewModel = priceViewModel,
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
                priceViewModel = priceViewModel,
                onBackButtonPressed = {
                    navController.popBackStack()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(priceViewModel: PriceViewModel, onSettingsButtonPressed: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }

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
                        priceViewModel.fetchData()
                    }
                    PriceCard(priceViewModel)
                }
            }
        }
    }
}

@Composable
fun PriceCard(priceViewModel: PriceViewModel) {
    Card(
        onClick = {
            priceViewModel.fetchData()
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
            .height(200.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 8.dp)
        ) {
            CardHeader(priceViewModel = priceViewModel)
            CardPrice(priceViewModel = priceViewModel)
            CardDetails(priceViewModel = priceViewModel)
        }
    }
}

@Composable
fun CardHeader(priceViewModel: PriceViewModel) {
    val currencyCode by priceViewModel.currencyCode.collectAsState(initial = "")

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
        Text(" Bitcoin / ${currencyCode?.uppercase()}")
    }
}

@Composable
fun CardPrice(priceViewModel: PriceViewModel) {

    val symbol by priceViewModel.symbol.collectAsState(initial = "")
    val price by priceViewModel.priceValue.collectAsState(initial = 0.00)

    val myStyle =
        MaterialTheme.typography.bodyLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold)

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = symbol ?: "", style = myStyle)
        Text(text = String.format("%,.2f", price), style = myStyle)
    }
}

@Composable
fun CardDetails(priceViewModel: PriceViewModel) {

    val change by priceViewModel.changeValue.collectAsState(initial = 0.00)

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Text(text = "24h: ${String.format("%.2f", change)}% ")

        if (change != null && change!! == 0.00) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.TrendingFlat,
                modifier = Modifier.size(20.dp),
                contentDescription = "Trending Flat"
            )
        }

        if (change != null && change!! > 0) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                tint = Color.Green,
                modifier = Modifier.size(20.dp),
                contentDescription = "Trending Up"
            )
        }

        if (change != null && change!! < 0) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.TrendingDown,
                tint = Color.Red,
                modifier = Modifier.size(20.dp),
                contentDescription = "Trending Down"
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (priceViewModel.loading.value) {
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
            Box(modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
@Preview
fun AppPreview() {
    MinimalBitcoinWidget()
}