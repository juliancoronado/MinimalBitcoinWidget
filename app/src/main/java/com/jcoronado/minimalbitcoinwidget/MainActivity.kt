package com.jcoronado.minimalbitcoinwidget

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.jcoronado.minimalbitcoinwidget.ui.theme.BTCPriceWidgetTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

// val TAG for Log information
private const val TAG = "Main Activity"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stored_price")
val PRICE_KEY = doublePreferencesKey("btc_price")
val CHANGE_KEY = floatPreferencesKey("btc_change")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RunApp()
        }
    }

    /**
     * HTTP GET request using the OkHttp library
     */
    private fun fetchData() {

        // set up shared preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        // store current currency selection
        val currency = prefs.getString("currency", "usd")

        var symbol = ""
        var isoCode = ""

        // set symbol and isoCode depending on selected currency
        when (currency) {
            "usd" -> {
                symbol = "$"
                isoCode = "USD"
            }

            "gbp" -> {
                symbol = "£"
                isoCode = "GBP"
            }

            "eur" -> {
                symbol = "€"
                isoCode = "EUR"
            }

            "cad" -> {
                symbol = "$"
                isoCode = "CAD"
            }

            "mxn" -> {
                symbol = "$"
                isoCode = "MXN"
            }

            "aud" -> {
                symbol = "$"
                isoCode = "AUD"
            }

            "brl" -> {
                symbol = "R$"
                isoCode = "BRL"
            }
        }

        // build API url string with selected currency
        val url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=$currency&ids=bitcoin"
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.i(TAG, "GET request successful.")

                // converts response into string
                val body = response.body?.string()

                // extracts object from JSON
                val tempList: Array<Data> = Gson().fromJson(body, Array<Data>::class.java)
                data = tempList[0]
                // update the activity_main layout with the new price data
                // updateLayout(data, symbol, isoCode)
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.i(TAG, "Failed to execute GET request.")
            }
        })
    }
}

@Composable
fun RunApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = {
            EnterTransition.None
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
    val btcPriceViewModel = BTCPriceViewModel(DataStoreManager(LocalContext.current))

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
                                contentDescription = "Open Settings Page"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    PriceCard(btcPriceViewModel)
                    // display snackbar in case of an error
                    if (btcPriceViewModel.error.value.isNotEmpty()) {
                        SnackbarWidget(
                            message = btcPriceViewModel.error.value,
                            actionLabel = "Dismiss"
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
    // Fetch price data when Composable is created
    LaunchedEffect(Unit) {
        btcPriceViewModel.startHere()
        // this code takes too long,
        // the value remains zero for a few seconds
    }

    Card(
        onClick = {
            btcPriceViewModel.fetchBitcoinPrice()
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
            .height(150.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 8.dp)
        ) {
            CardHeader(selectedCurrency = "USD")
            CardPrice(symbol = "$", price = btcPriceViewModel.priceValue)
            CardDetails(
                change = btcPriceViewModel.changeValue,
                btcPriceViewModel = btcPriceViewModel
            )
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
        Text("Bitcoin / $selectedCurrency")
    }
}

// ViewModel to fetch the Bitcoin Price from CoinGecko API
class BTCPriceViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
    var priceValue by mutableDoubleStateOf(0.00) // price state
    var changeValue by mutableFloatStateOf(0.00f) // change state
    var loading = mutableStateOf(false) // loading state
    var error = mutableStateOf("")

    // init here is causing issues I think? trying another method
    suspend fun startHere() {
        viewModelScope.launch(Dispatchers.IO) {
            fetchBitcoinPrice()
        }
        loading.value = true
        val price = dataStoreManager.getPrice() // Fetch from DataStore
        val change = dataStoreManager.getChange()
        priceValue = price // Update state
        changeValue = change
        loading.value = false
    }

    fun fetchBitcoinPrice() {

        val existingPriceValue = priceValue
        val existingChangeValue = changeValue

        viewModelScope.launch(Dispatchers.IO) {
            error.value = "" // reset error value before new api call
            val url =
                "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd&include_24hr_change=true&precision=2"
            val client = OkHttpClient()

            val request = Request.Builder().url(url).get().build()

            try {
                loading.value = true
                val response = client.newCall(request).execute()

                if (response.code == 200) {
                    val responseBody = response.body?.string()
                    Log.d("API Response", responseBody!!)
                    val data = Gson().fromJson(responseBody, PriceData::class.java)
                    delay(1000)
                    priceValue = data.bitcoin.usd
                    changeValue = data.bitcoin.usd_24h_change
                    loading.value = false
                    dataStoreManager.savePrice(data.bitcoin.usd)
                    dataStoreManager.saveChange(data.bitcoin.usd_24h_change)
                } else if (response.code == 429) {
                    error.value = "Too many requests, try again later"
                    priceValue = existingPriceValue
                    changeValue = existingChangeValue
                    loading.value = false
                }
                response.close()
            } catch (e: Exception) {
                Log.e("CoinGecko Error", e.toString())
                loading.value = false
            }
        }
    }
}

class DataStoreManager(context: Context) {
    private val dataStore: DataStore<Preferences> = context.dataStore

    suspend fun getPrice(): Double {
        val preferences = dataStore.data.first()
        val price = preferences[PRICE_KEY] ?: 0.0
        Log.d("Stored Price", price.toString())
        return price
    }

    suspend fun savePrice(newPrice: Double) {
        Log.d("Price To Be Saved", newPrice.toString())
        dataStore.edit { preferences ->
            preferences[PRICE_KEY] = newPrice
        }
    }

    suspend fun getChange(): Float {
        val preferences = dataStore.data.first()
        val change = preferences[CHANGE_KEY] ?: 0.0f
        Log.d("Stored Change", change.toString())
        return change
    }

    suspend fun saveChange(newChange: Float) {
        Log.d("Change To Be Saved", newChange.toString())
        dataStore.edit { preferences ->
            preferences[CHANGE_KEY] = newChange
        }
    }
}

@Composable
fun SnackbarWidget(
    message: String, actionLabel: String, duration: SnackbarDuration = SnackbarDuration.Short
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarHostState) {
        snackbarHostState.showSnackbar(message, actionLabel, duration = duration)
    }
    Box(
        Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
    ) {
        SnackbarHost(
            hostState = snackbarHostState
        )
    }
}

@Composable
fun CardPrice(symbol: String, price: Double) {

    val myStyle =
        MaterialTheme.typography.bodyLarge.copy(fontSize = 36.sp, fontWeight = FontWeight.Bold)

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = symbol, style = myStyle)
        Text(text = String.format("%,.2f", price), style = myStyle)
    }
}

@Composable
fun CardDetails(change: Float, btcPriceViewModel: BTCPriceViewModel) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val placeholderModifier = Modifier.size(20.dp) // Size of progress indicator

        Text(text = "24h: ${String.format("%.2f", change)}%")
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
@Preview(showBackground = true, showSystemUi = true)
fun AppPreview() {
    RunApp()
}