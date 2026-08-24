package com.example.weatherapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.ui.theme.WeatherAppTheme
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val preferences by lazy {
        getSharedPreferences(
            "WeatherApp",
            Context.MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            WeatherAppTheme {
                MainApp()
            }
        }
    }

    // ============================================================
    // MAIN APP
    // ============================================================

    @Composable
    private fun MainApp() {

        var selectedScreen by remember {
            mutableStateOf(0)
        }

        var selectedCity by remember {
            mutableStateOf(getSavedCity())
        }

        var savedCities by remember {
            mutableStateOf(getSavedCities())
        }

        var searchError by remember {
            mutableStateOf("")
        }

        Scaffold(
            containerColor = Color.Transparent,

            bottomBar = {

                NavigationBar {

                    NavigationBarItem(
                        selected = selectedScreen == 0,

                        onClick = {
                            selectedScreen = 0
                        },

                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        },

                        label = {
                            Text("Home")
                        }
                    )

                    NavigationBarItem(
                        selected = selectedScreen == 1,

                        onClick = {
                            selectedScreen = 1
                        },

                        icon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Locations"
                            )
                        },

                        label = {
                            Text("Locations")
                        }
                    )
                }
            }
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                if (selectedScreen == 0) {

                    WeatherScreen(
                        selectedCity = selectedCity
                    )

                } else {

                    LocationsScreen(
                        savedCities = savedCities,

                        searchError = searchError,

                        onDismissError = {
                            searchError = ""
                        },

                        onCitySelected = { city ->

                            selectedCity = city

                            selectedScreen = 0
                        },

                        onCitySearched = { city ->

                            val cleanCity = city.trim()

                            if (cleanCity.isNotBlank()) {

                                // Check the API BEFORE saving.
                                getWeather(

                                    city = cleanCity,

                                    onSuccess = {

                                        // Weather exists.
                                        // Now save the city.
                                        saveCity(cleanCity)

                                        savedCities =
                                            getSavedCities()

                                        selectedCity =
                                            cleanCity

                                        selectedScreen = 0
                                    },

                                    onError = {

                                        // Weather does not exist.
                                        // Do NOT save the city.
                                        searchError =
                                            "No weather data is available for \"$cleanCity\"."
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    // ============================================================
    // SAVE CITY
    // ============================================================

    private fun saveCity(city: String) {

        val cleanCity = city.trim()

        if (cleanCity.isBlank()) {
            return
        }

        val currentCities =
            preferences
                .getStringSet(
                    "saved_cities",
                    emptySet()
                )
                ?.toMutableSet()
                ?: mutableSetOf()

        currentCities.add(cleanCity)

        preferences.edit()
            .putString(
                "saved_city",
                cleanCity
            )
            .putStringSet(
                "saved_cities",
                currentCities
            )
            .apply()
    }

    // ============================================================
    // GET SAVED CITY
    // ============================================================

    private fun getSavedCity(): String {

        return preferences.getString(
            "saved_city",
            ""
        ) ?: ""
    }

    // ============================================================
    // GET SAVED CITIES
    // ============================================================

    private fun getSavedCities(): List<String> {

        return preferences
            .getStringSet(
                "saved_cities",
                emptySet()
            )
            ?.toList()
            ?.filter {
                it.isNotBlank()
            }
            ?.distinctBy {
                it.trim().lowercase()
            }
            ?.sorted()
            ?: emptyList()
    }

    // ============================================================
    // WEATHER SCREEN
    // ============================================================

    @Composable
    private fun WeatherScreen(
        selectedCity: String
    ) {

        var weather by remember {
            mutableStateOf<WeatherData?>(null)
        }

        var errorMessage by remember {
            mutableStateOf("")
        }

        var loading by remember {
            mutableStateOf(false)
        }

        LaunchedEffect(selectedCity) {

            if (selectedCity.isNotBlank()) {

                loading = true
                errorMessage = ""

                getWeather(

                    city = selectedCity,

                    onSuccess = { data ->

                        weather = data
                        loading = false
                    },

                    onError = { error ->

                        errorMessage = error
                        loading = false
                    }
                )
            }
        }

        val background =
            getWeatherBackground(
                weather?.currentCondition
            )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.navigationBars
                    )
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 20.dp
                    )
            ) {

                Spacer(
                    modifier = Modifier.height(25.dp)
                )

                // =================================================
                // LOADING
                // =================================================

                if (loading) {

                    Column(
                        modifier = Modifier.fillMaxWidth(),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator(
                            color = Color.White
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text =
                                stringResource(
                                    R.string.getting_weather
                                ),

                            color = Color.White,

                            fontSize = 16.sp
                        )
                    }
                }

                // =================================================
                // ERROR
                // =================================================

                if (errorMessage.isNotEmpty()) {

                    GlassCard {

                        Text(
                            text = errorMessage,

                            color = Color.White,

                            textAlign = TextAlign.Center,

                            fontSize = 16.sp,

                            modifier =
                                Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(15.dp)
                    )
                }

                // =================================================
                // WEATHER
                // =================================================

                weather?.let { data ->

                    // =================================================
                    // CITY
                    // =================================================

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.Center,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.LocationOn,

                            contentDescription =
                                stringResource(
                                    R.string.location
                                ),

                            tint = Color.White,

                            modifier =
                                Modifier.size(22.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.width(5.dp)
                        )

                        Text(
                            text = data.city,

                            color = Color.White,

                            fontSize = 34.sp,

                            fontWeight =
                                FontWeight.Medium
                        )
                    }

                    // =================================================
                    // DATE
                    // =================================================

                    Text(
                        text = getCurrentDate(),

                        color =
                            Color.White.copy(
                                alpha = 0.80f
                            ),

                        fontSize = 16.sp,

                        textAlign =
                            TextAlign.Center,

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier =
                            Modifier.height(30.dp)
                    )

                    // =================================================
                    // MAIN WEATHER
                    // =================================================

                    Column(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        WeatherIcon(
                            condition =
                                data.currentCondition,

                            modifier =
                                Modifier.size(110.dp),

                            tint =
                                Color.White
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                getConditionText(
                                    data.currentCondition
                                ),

                            color = Color.White,

                            fontSize = 27.sp
                        )

                        Text(
                            text =
                                "${data.maxTemperature}°",

                            color = Color.White,

                            fontSize = 86.sp,

                            fontWeight =
                                FontWeight.ExtraLight
                        )

                        Text(
                            text =
                                stringResource(
                                    R.string.todays_high
                                ),

                            color =
                                Color.White.copy(
                                    alpha = 0.75f
                                ),

                            fontSize = 15.sp
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(25.dp)
                    )

                    // =================================================
                    // TEMPERATURE RANGE
                    // =================================================

                    GlassCard {

                        Text(
                            text =
                                stringResource(
                                    R.string.temperature_range
                                ),

                            color =
                                Color.White.copy(
                                    alpha = 0.65f
                                ),

                            fontSize = 11.sp,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Text(
                                text =
                                    "${data.minTemperature}°",

                                color = Color.White,

                                fontSize = 16.sp
                            )

                            Text(
                                text =
                                    "${data.maxTemperature}°",

                                color = Color.White,

                                fontSize = 16.sp
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(9.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            50.dp
                                        )
                                    )
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF54AFFF),
                                                Color(0xFFFFC857)
                                            )
                                        )
                                    )
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )

                    // =================================================
                    // WIND
                    // =================================================

                    GlassCard {

                        Text(
                            text =
                                stringResource(
                                    R.string.wind
                                ),

                            color =
                                Color.White.copy(
                                    alpha = 0.65f
                                ),

                            fontSize = 12.sp,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Air,

                                contentDescription =
                                    stringResource(
                                        R.string.wind
                                    ),

                                tint = Color.White,

                                modifier =
                                    Modifier.size(45.dp)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(18.dp)
                            )

                            Column {

                                Text(
                                    text =
                                        getWindDirectionText(
                                            data.windDirection
                                        ),

                                    color = Color.White,

                                    fontSize = 24.sp,

                                    fontWeight =
                                        FontWeight.Medium
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(3.dp)
                                )

                                Text(
                                    text =
                                        "${data.windSpeed} ${
                                            stringResource(
                                                R.string.km_per_hour
                                            )
                                        }",

                                    color =
                                        Color.White.copy(
                                            alpha = 0.75f
                                        ),

                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )

                    // =================================================
                    // TOMORROW
                    // =================================================

                    GlassCard {

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                Text(
                                    text =
                                        stringResource(
                                            R.string.tomorrow
                                        ),

                                    color =
                                        Color.White.copy(
                                            alpha = 0.65f
                                        ),

                                    fontSize = 12.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(8.dp)
                                )

                                Text(
                                    text =
                                        getConditionText(
                                            data.nextDayOutlook
                                        ),

                                    color = Color.White,

                                    fontSize = 25.sp,

                                    fontWeight =
                                        FontWeight.Medium
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(3.dp)
                                )

                                Text(
                                    text =
                                        stringResource(
                                            R.string.next_day_outlook
                                        ),

                                    color =
                                        Color.White.copy(
                                            alpha = 0.70f
                                        ),

                                    fontSize = 14.sp
                                )
                            }

                            WeatherIcon(
                                condition =
                                    data.nextDayOutlook,

                                modifier =
                                    Modifier.size(65.dp),

                                tint =
                                    Color.White
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(30.dp)
                    )
                }
            }
        }
    }

    // ============================================================
    // GLASS CARD
    // ============================================================

    @Composable
    private fun GlassCard(
        content: @Composable () -> Unit
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            26.dp
                        )
                    )
                    .background(
                        Color.White.copy(
                            alpha = 0.18f
                        )
                    )
                    .padding(20.dp)
        ) {

            content()
        }
    }

    // ============================================================
    // GET WEATHER
    // ============================================================

    private fun getWeather(
        city: String,
        onSuccess: (WeatherData) -> Unit,
        onError: (String) -> Unit
    ) {

        Thread {

            var connection: HttpURLConnection? = null

            try {

                val encodedCity =
                    URLEncoder.encode(
                        city.trim(),
                        "UTF-8"
                    )

                val url = URL(
                    "https://weatherapi-production-c8b1.up.railway.app/api/Weather/$encodedCity"
                )

                connection =
                    url.openConnection()
                            as HttpURLConnection

                connection.requestMethod = "GET"

                connection.connectTimeout = 10000

                connection.readTimeout = 10000

                val responseCode =
                    connection.responseCode

                if (
                    responseCode ==
                    HttpURLConnection.HTTP_OK
                ) {

                    val response =
                        connection.inputStream
                            .bufferedReader()
                            .use {
                                it.readText()
                            }

                    val json =
                        JSONObject(response)

                    val data =
                        WeatherData(

                            city =
                                json.getString(
                                    "city"
                                ),

                            currentCondition =
                                json.getString(
                                    "currentCondition"
                                ),

                            maxTemperature =
                                json.getInt(
                                    "maxTemperature"
                                ),

                            minTemperature =
                                json.getInt(
                                    "minTemperature"
                                ),

                            windDirection =
                                json.getString(
                                    "windDirection"
                                ),

                            windSpeed =
                                json.getInt(
                                    "windSpeed"
                                ),

                            nextDayOutlook =
                                json.getString(
                                    "nextDayOutlook"
                                )
                        )

                    runOnUiThread {
                        onSuccess(data)
                    }

                } else {

                    runOnUiThread {

                        onError(
                            "No weather data is available for \"$city\"."
                        )
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {

                    onError(
                        getString(
                            R.string.connection_error
                        )
                    )
                }

            } finally {

                connection?.disconnect()
            }

        }.start()
    }

    // ============================================================
    // CONDITION TEXT
    // ============================================================

    @Composable
    private fun getConditionText(
        condition: String
    ): String {

        return when (
            condition.lowercase()
        ) {

            "sunny" ->
                stringResource(R.string.sunny)

            "cloudy" ->
                stringResource(R.string.cloudy)

            "overcast" ->
                stringResource(R.string.overcast)

            "rain" ->
                stringResource(R.string.rain)

            "drizzle" ->
                stringResource(R.string.drizzle)

            "fog" ->
                stringResource(R.string.fog)

            "snow" ->
                stringResource(R.string.snow)

            else ->
                condition.replaceFirstChar {
                    it.uppercase()
                }
        }
    }

    // ============================================================
    // WIND TEXT
    // ============================================================

    @Composable
    private fun getWindDirectionText(
        direction: String
    ): String {

        return when (
            direction.lowercase()
        ) {

            "north" ->
                stringResource(R.string.north)

            "south" ->
                stringResource(R.string.south)

            "east" ->
                stringResource(R.string.east)

            "west" ->
                stringResource(R.string.west)

            "northeast" ->
                stringResource(R.string.northeast)

            "northwest" ->
                stringResource(R.string.northwest)

            "southeast" ->
                stringResource(R.string.southeast)

            "southwest" ->
                stringResource(R.string.southwest)

            else ->
                direction.replaceFirstChar {
                    it.uppercase()
                }
        }
    }

    // ============================================================
    // WEATHER ICON
    // ============================================================

    @Composable
    private fun WeatherIcon(
        condition: String,
        modifier: Modifier,
        tint: Color
    ) {

        Icon(
            imageVector =
                getWeatherIcon(condition),

            contentDescription =
                condition,

            tint = tint,

            modifier = modifier
        )
    }

    private fun getWeatherIcon(
        condition: String
    ): ImageVector {

        return when (
            condition.lowercase()
        ) {

            "sunny" ->
                Icons.Default.WbSunny

            "cloudy" ->
                Icons.Default.Cloud

            "overcast" ->
                Icons.Default.CloudQueue

            "rain" ->
                Icons.Default.WaterDrop

            "drizzle" ->
                Icons.Default.WaterDrop

            "fog" ->
                Icons.Default.Cloud

            "snow" ->
                Icons.Default.Cloud

            else ->
                Icons.Default.Cloud
        }
    }

    // ============================================================
    // WEATHER BACKGROUND
    // ============================================================

    private fun getWeatherBackground(
        condition: String?
    ): Brush {

        return when (
            condition?.lowercase()
        ) {

            "sunny" ->
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF248FEA),
                        Color(0xFF62C4F5),
                        Color(0xFFBDEBFF)
                    )
                )

            "cloudy" ->
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF527895),
                        Color(0xFF789EB5),
                        Color(0xFFB6CBD7)
                    )
                )

            "overcast" ->
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF445B6B),
                        Color(0xFF718999),
                        Color(0xFFB7C7CF)
                    )
                )

            "rain" ->
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF203A56),
                        Color(0xFF3E6485),
                        Color(0xFF7899B0)
                    )
                )

            "drizzle" ->
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF31516C),
                        Color(0xFF62849C),
                        Color(0xFFA5BBC7)
                    )
                )

            "fog" ->
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF65747B),
                        Color(0xFF929FA4),
                        Color(0xFFC7D0D2)
                    )
                )

            "snow" ->
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF6794B4),
                        Color(0xFF9FC2D8),
                        Color(0xFFDDEBF2)
                    )
                )

            else ->
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF4AA8E8),
                        Color(0xFF82C8F2),
                        Color(0xFFBDE5FA)
                    )
                )
        }
    }

    // ============================================================
    // DATE
    // ============================================================

    private fun getCurrentDate(): String {

        val formatter =
            SimpleDateFormat(
                "EEEE, d MMMM",
                Locale.getDefault()
            )

        return formatter.format(Date())
    }
}

// ================================================================
// WEATHER DATA
// ================================================================

data class WeatherData(

    val city: String,

    val currentCondition: String,

    val maxTemperature: Int,

    val minTemperature: Int,

    val windDirection: String,

    val windSpeed: Int,

    val nextDayOutlook: String
)