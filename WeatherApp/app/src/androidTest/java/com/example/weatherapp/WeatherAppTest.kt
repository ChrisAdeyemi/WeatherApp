package com.example.weatherapp

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WeatherAppTest {

    @get:Rule
    val composeTestRule =
        createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {

        composeTestRule.activity
            .getSharedPreferences(
                "WeatherApp",
                Context.MODE_PRIVATE
            )
            .edit()
            .clear()
            .apply()
    }

    @Test
    fun appOpensSuccessfully() {

        composeTestRule
            .onNodeWithText("Home")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Locations")
            .assertIsDisplayed()
    }

    @Test
    fun locationsScreenOpens() {

        composeTestRule
            .onNodeWithText("Locations")
            .performClick()

        composeTestRule
            .onNodeWithText("Search city")
            .assertIsDisplayed()
    }

    @Test
    fun canNavigateBackToHome() {

        composeTestRule
            .onNodeWithText("Locations")
            .performClick()

        composeTestRule
            .onNodeWithText("Home")
            .performClick()

        composeTestRule
            .onNodeWithText("Home")
            .assertIsDisplayed()
    }

    @Test
    fun canNavigateBetweenHomeAndLocations() {

        // Home → Locations

        composeTestRule
            .onNodeWithText("Locations")
            .performClick()

        composeTestRule
            .onNodeWithText("Search city")
            .assertIsDisplayed()

        // Locations → Home

        composeTestRule
            .onNodeWithText("Home")
            .performClick()

        composeTestRule
            .onNodeWithText("Home")
            .assertIsDisplayed()

        // Home → Locations again

        composeTestRule
            .onNodeWithText("Locations")
            .performClick()

        composeTestRule
            .onNodeWithText("Search city")
            .assertIsDisplayed()
    }

}