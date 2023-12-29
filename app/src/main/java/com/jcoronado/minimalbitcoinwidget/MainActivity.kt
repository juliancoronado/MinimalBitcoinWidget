@file:OptIn(ExperimentalMaterial3Api::class)

package com.jcoronado.minimalbitcoinwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

// val TAG for Log information
private const val TAG = "Main Activity"

class MainActivity : ComponentActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    // var data will hold the information received from the HTTP Request
    // var data = Data()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            RunApp()
        }

//        // follow system theme
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//        setContentView(R.layout.activity_main)
//
//        // inflate toolbar
//        val mToolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.m_toolbar)
//        setSupportActionBar(mToolbar)
//
//        // create TextView objects that contain reference to layout objects
//        val priceTv: TextView = findViewById(R.id.main_price_text)
//        val changeTv: TextView = findViewById(R.id.main_day_change)
//        val symbolTv: TextView = findViewById(R.id.main_symbol)
//
//        // set TextViews text to loading string
//        priceTv.text = getString(R.string.loading_text)
//        changeTv.text = getString(R.string.loading_text)
//        symbolTv.text = ""
//
//        // initial HTTP GET request
//        fetchData()
//
//        // when update button gets pressed or activity restarted
//        val updateButton: ImageButton = findViewById(R.id.main_refresh_button)
//        updateButton.setOnClickListener {
//            Log.i(TAG, "Refreshing price layout.")
//
//            // temp changes to show price is loading
//            runOnUiThread {
//                changeTv.setTextColor(priceTv.currentTextColor)
//                priceTv.text = getString(R.string.loading_text)
//                changeTv.text = getString(R.string.loading_text)
//                symbolTv.text = ""
//            }
//            // make HTTP GET request
//            fetchData()
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.version_info -> {
                val intent = Intent(this, InfoActivity::class.java)
                startActivity(intent)
            }

//            R.id.settings -> {
//                val intent = Intent(this, SettingsActivity::class.java)
//                startActivity(intent)
//            }
        }

        return true
    }

    /**
     * Updates the price information layout
     */
    private fun updateLayout(values: Data, symbol: String, isoCode: String) {

        // create TextView objects that contain reference to layout objects
        val priceTv: TextView = findViewById(R.id.main_price_text)
        val changeTv: TextView = findViewById(R.id.main_day_change)
        val isoCodeTv: TextView = findViewById(R.id.main_iso_code)
        val symbolTv: TextView = findViewById(R.id.main_symbol)

        runOnUiThread {

            // update the layout with new data
            priceTv.text = values.priceString()
            changeTv.text = values.dayChangeString()
            isoCodeTv.text = isoCode
            symbolTv.text = symbol

            // check for positive or negative change to set color accordingly
            if (values.dayChangeString() == "0.0%") {
                // no change
                changeTv.setTextColor(priceTv.currentTextColor)
            } else if (values.dayChangeString().contains('+')) {
                // green color
                changeTv.setTextColor(ContextCompat.getColor(this, R.color.positive_green))
            } else {
                // red color
                changeTv.setTextColor(ContextCompat.getColor(this, R.color.negative_red))
            }

        }
    }

    /**
     * HTTP GET request using the OkHttp library
     */
    private fun fetchData() {

        // set up shared preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)

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
                updateLayout(data, symbol, isoCode)
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.i(TAG, "Failed to execute GET request.")
            }
        })
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // HTTP GET the new data using new currency selection
        Log.i(TAG, "Currency preference changed.")
        fetchData()

        // update home screen widget
        // build intent to call opUpdate()
        val intent = Intent(this, PriceWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        val ids: IntArray = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, PriceWidget::class.java)
        )

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }
}

@Composable
fun RunApp() {
    val navController = rememberNavController()

    // TODO is there a simpler way to open a new page? Launch a new activity?
    NavHost(
        navController = navController,
        startDestination = "start",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(
            "start",
        ) {
            MainPage(
                onSettingsButtonPressed = {
                    navController.navigate("settings")
                },
                onVersionInfoButtonPressed = {
                    // TODO navigate to version info page
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
            }
        ) {
            SettingsPage(
                onBackButtonPressed = {
                    navController.popBackStack()
                },
            )
        }
    }
}

@Composable
fun MainPage(onSettingsButtonPressed: () -> Unit, onVersionInfoButtonPressed: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val btcPriceViewModel = BTCPriceViewModel()

    BTCPriceWidgetTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Minimal Bitcoin Widget", // TODO move to strings.xml
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    },
                    actions = {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "Settings") },
                                onClick = {
                                    showMenu = false
                                    onSettingsButtonPressed()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(text = "Version Info") },
                                onClick = {
                                    showMenu = false
                                    onVersionInfoButtonPressed()
                                },
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
                        SnackbarWidget(message = btcPriceViewModel.error.value, actionLabel = "Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
fun PriceCard(btcPriceViewModel: BTCPriceViewModel) {
    // Fetch price data when Composable is created
    LaunchedEffect(Unit) {
        btcPriceViewModel.fetchBitcoinPrice()
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
    Row(verticalAlignment = CenterVertically
    ) {
        Box(modifier = Modifier.height(16.dp)) {
            Image(modifier = Modifier.padding(1.dp), painter = painterResource(id = R.drawable.bitcoin_icon), contentDescription = "")
        }
        Box(modifier = Modifier.size(2.dp))
        Text("Bitcoin / $selectedCurrency")
    }
}

// ViewModel to fetch the Bitcoin Price from CoinGecko API
class BTCPriceViewModel : ViewModel() {
    var priceValue by mutableStateOf(0.00) // price state
    var changeValue by mutableStateOf(0.00f) // change state
    var loading = mutableStateOf(false) // loading state
    var error = mutableStateOf("")

    fun fetchBitcoinPrice() {

        val existingPriceValue = priceValue
        val existingChangeValue = changeValue

        viewModelScope.launch(Dispatchers.IO) {
            error.value = "" // reset error value before new api call
            val url = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd&include_24hr_change=true&precision=2"
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
        verticalAlignment = Alignment.CenterVertically,
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