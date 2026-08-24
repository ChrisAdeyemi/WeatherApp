package com.example.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LocationsScreen(
    savedCities: List<String>,
    searchError: String,
    onDismissError: () -> Unit,
    onCitySelected: (String) -> Unit,
    onCitySearched: (String) -> Unit
) {

    var searchText by remember {
        mutableStateOf("")
    }

    // =========================================================
    // ERROR POPUP
    // =========================================================

    if (searchError.isNotBlank()) {

        AlertDialog(
            onDismissRequest = {
                onDismissError()
            },

            title = {
                Text(
                    text = stringResource(
                        R.string.location_not_found
                    )
                )
            },

            text = {
                Text(
                    text = searchError
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        onDismissError()
                    }
                ) {
                    Text(
                        text = stringResource(
                            R.string.ok
                        )
                    )
                }
            }
        )
    }

    // =========================================================
    // SAVED CITIES
    // =========================================================

    val uniqueCities = savedCities
        .map {
            it.trim()
        }
        .filter {
            it.isNotBlank()
        }
        .distinctBy {
            it.lowercase()
        }

    // =========================================================
    // FILTER WHILE TYPING
    // =========================================================

    val filteredCities = uniqueCities.filter { city ->

        city.contains(
            searchText.trim(),
            ignoreCase = true
        )
    }

    // =========================================================
    // MAIN LOCATION SCREEN
    // =========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFF4AA8E8)
            )
            .padding(
                horizontal = 20.dp
            )
    ) {

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        // =========================================================
        // TITLE
        // =========================================================

        Text(
            text = stringResource(
                R.string.locations
            ),
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // =========================================================
        // SEARCH
        // =========================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = searchText,

                onValueChange = {
                    searchText = it
                },

                placeholder = {
                    Text(
                        text = stringResource(
                            R.string.search_city
                        ),
                        color = Color.White.copy(
                            alpha = 0.70f
                        )
                    )
                },

                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(
                            R.string.search
                        ),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                },

                singleLine = true,

                modifier = Modifier.weight(1f)
            )

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            IconButton(
                onClick = {

                    val city = searchText.trim()

                    if (city.isNotBlank()) {

                        onCitySearched(city)

                        searchText = ""
                    }
                }
            ) {

                Icon(
                    imageVector = Icons.Default.Search,

                    contentDescription = stringResource(
                        R.string.search_button
                    ),

                    tint = Color.White,

                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        // =========================================================
        // MY LOCATIONS
        // =========================================================

        Text(
            text = stringResource(
                R.string.my_locations
            ),

            color = Color.White.copy(
                alpha = 0.75f
            ),

            fontSize = 14.sp,

            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // =========================================================
        // NO LOCATIONS
        // =========================================================

        if (filteredCities.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 40.dp
                    ),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Icon(
                    imageVector =
                        Icons.Default.LocationOn,

                    contentDescription =
                        stringResource(
                            R.string.no_locations
                        ),

                    tint =
                        Color.White.copy(
                            alpha = 0.70f
                        ),

                    modifier =
                        Modifier.size(55.dp)
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Text(
                    text =
                        if (searchText.isBlank()) {

                            stringResource(
                                R.string.no_saved_locations
                            )

                        } else {

                            stringResource(
                                R.string.no_matching_locations
                            )
                        },

                    color =
                        Color.White,

                    fontSize =
                        18.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )

                Text(
                    text =
                        stringResource(
                            R.string.search_for_city_above
                        ),

                    color =
                        Color.White.copy(
                            alpha = 0.70f
                        ),

                    fontSize =
                        14.sp
                )
            }

        } else {

            // =====================================================
            // LOCATION LIST
            // =====================================================

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = filteredCities,

                    key = {
                        it.lowercase()
                    }
                ) { city ->

                    LocationItem(
                        city = city,

                        onClick = {
                            onCitySelected(city)
                        }
                    )
                }
            }
        }
    }
}

// =================================================================
// LOCATION ITEM
// =================================================================

@Composable
private fun LocationItem(
    city: String,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(
                Color.White.copy(
                    alpha = 0.18f
                )
            )
            .clickable {
                onClick()
            }
            .padding(20.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        // =========================================================
        // LOCATION ICON
        // =========================================================

        Icon(
            imageVector =
                Icons.Default.LocationOn,

            contentDescription =
                stringResource(
                    R.string.location
                ),

            tint =
                Color.White,

            modifier =
                Modifier.size(35.dp)
        )

        Spacer(
            modifier =
                Modifier.width(16.dp)
        )

        // =========================================================
        // CITY NAME
        // =========================================================

        Column {

            Text(
                text =
                    city,

                color =
                    Color.White,

                fontSize =
                    21.sp,

                fontWeight =
                    FontWeight.Medium
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    stringResource(
                        R.string.view_weather
                    ),

                color =
                    Color.White.copy(
                        alpha = 0.70f
                    ),

                fontSize =
                    14.sp
            )
        }

        Spacer(
            modifier =
                Modifier.weight(1f)
        )

        // =========================================================
        // WEATHER ICON
        // =========================================================

        Icon(
            imageVector =
                Icons.Default.Cloud,

            contentDescription =
                stringResource(
                    R.string.weather
                ),

            tint =
                Color.White.copy(
                    alpha = 0.80f
                ),

            modifier =
                Modifier.size(30.dp)
        )
    }
}