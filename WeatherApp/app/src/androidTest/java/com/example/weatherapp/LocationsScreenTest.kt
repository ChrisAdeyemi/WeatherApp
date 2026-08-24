package com.example.weatherapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LocationsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyLocationsAreDisplayed() {

        composeTestRule.setContent {

            LocationsScreen(
                savedCities = emptyList(),
                searchError = "",
                onDismissError = {},
                onCitySelected = {},
                onCitySearched = {}
            )
        }

        composeTestRule
            .onNodeWithText("Locations")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("My Locations")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("No saved locations")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Search for a city above")
            .assertIsDisplayed()
    }

    @Test
    fun savedLocationIsDisplayed() {

        composeTestRule.setContent {

            LocationsScreen(
                savedCities = listOf("Dublin"),
                searchError = "",
                onDismissError = {},
                onCitySelected = {},
                onCitySearched = {}
            )
        }

        composeTestRule
            .onNodeWithText("Locations")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Dublin")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("View weather")
            .assertIsDisplayed()
    }

    @Test
    fun searchBoxAcceptsCity() {

        composeTestRule.setContent {

            LocationsScreen(
                savedCities = emptyList(),
                searchError = "",
                onDismissError = {},
                onCitySelected = {},
                onCitySearched = {}
            )
        }

        composeTestRule
            .onNode(hasSetTextAction())
            .performTextInput("Dublin")

        composeTestRule
            .onNodeWithText("Dublin")
            .assertIsDisplayed()
    }

    @Test
    fun searchButtonCallsSearch() {

        var searchedCity = ""

        composeTestRule.setContent {

            LocationsScreen(
                savedCities = emptyList(),
                searchError = "",
                onDismissError = {},
                onCitySelected = {},
                onCitySearched = { city ->
                    searchedCity = city
                }
            )
        }

        composeTestRule
            .onNode(hasSetTextAction())
            .performTextInput("Dublin")

        composeTestRule
            .onNodeWithContentDescription("Search button")
            .performClick()

        assertEquals(
            "Dublin",
            searchedCity
        )
    }

    @Test
    fun locationCanBeSelected() {

        var selectedCity = ""

        composeTestRule.setContent {

            LocationsScreen(
                savedCities = listOf("Dublin"),
                searchError = "",
                onDismissError = {},
                onCitySelected = { city ->
                    selectedCity = city
                },
                onCitySearched = {}
            )
        }

        composeTestRule
            .onNodeWithText("Dublin")
            .performClick()

        assertEquals(
            "Dublin",
            selectedCity
        )
    }

    @Test
    fun locationIconIsDisplayed() {

        composeTestRule.setContent {

            LocationsScreen(
                savedCities = listOf("Dublin"),
                searchError = "",
                onDismissError = {},
                onCitySelected = {},
                onCitySearched = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Location")
            .assertIsDisplayed()
    }

    @Test
    fun weatherIconIsDisplayed() {

        composeTestRule.setContent {

            LocationsScreen(
                savedCities = listOf("Dublin"),
                searchError = "",
                onDismissError = {},
                onCitySelected = {},
                onCitySearched = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Weather")
            .assertIsDisplayed()
    }

    @Test
    fun searchErrorIsDisplayed() {

        composeTestRule.setContent {

            LocationsScreen(
                savedCities = emptyList(),
                searchError =
                    "No weather data is available for \"London\".",
                onDismissError = {},
                onCitySelected = {},
                onCitySearched = {}
            )
        }

        composeTestRule
            .onNodeWithText("Location not found")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                "No weather data is available for \"London\"."
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("OK")
            .assertIsDisplayed()
    }

    @Test
    fun errorCanBeDismissed() {

        var dismissed = false

        composeTestRule.setContent {

            LocationsScreen(
                savedCities = emptyList(),
                searchError =
                    "No weather data is available for \"London\".",
                onDismissError = {
                    dismissed = true
                },
                onCitySelected = {},
                onCitySearched = {}
            )
        }

        composeTestRule
            .onNodeWithText("OK")
            .performClick()

        assertEquals(
            true,
            dismissed
        )
    }
}