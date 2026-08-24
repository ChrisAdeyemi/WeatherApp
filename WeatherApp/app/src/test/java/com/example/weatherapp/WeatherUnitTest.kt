package com.example.weatherapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherUnitTest {

    @Test
    fun weatherDataStoresCorrectValues() {

        val weather = WeatherData(
            city = "Dublin",
            currentCondition = "sunny",
            maxTemperature = 20,
            minTemperature = 12,
            windDirection = "west",
            windSpeed = 15,
            nextDayOutlook = "cloudy"
        )

        assertEquals("Dublin", weather.city)
        assertEquals("sunny", weather.currentCondition)
        assertEquals(20, weather.maxTemperature)
        assertEquals(12, weather.minTemperature)
        assertEquals("west", weather.windDirection)
        assertEquals(15, weather.windSpeed)
        assertEquals("cloudy", weather.nextDayOutlook)
    }

    @Test
    fun maximumTemperatureIsGreaterThanMinimum() {

        val weather = WeatherData(
            city = "Dublin",
            currentCondition = "cloudy",
            maxTemperature = 20,
            minTemperature = 10,
            windDirection = "south",
            windSpeed = 20,
            nextDayOutlook = "rain"
        )

        assertTrue(
            weather.maxTemperature > weather.minTemperature
        )
    }

    @Test
    fun windSpeedIsNotNegative() {

        val weather = WeatherData(
            city = "Dublin",
            currentCondition = "rain",
            maxTemperature = 15,
            minTemperature = 8,
            windDirection = "west",
            windSpeed = 25,
            nextDayOutlook = "cloudy"
        )

        assertTrue(
            weather.windSpeed >= 0
        )
    }

    @Test
    fun temperaturesAreWithinExpectedRange() {

        val weather = WeatherData(
            city = "Dublin",
            currentCondition = "sunny",
            maxTemperature = 30,
            minTemperature = 15,
            windDirection = "east",
            windSpeed = 10,
            nextDayOutlook = "sunny"
        )

        assertTrue(weather.maxTemperature in -40..40)
        assertTrue(weather.minTemperature in -40..40)
    }

    @Test
    fun windSpeedIsWithinExpectedRange() {

        val weather = WeatherData(
            city = "Dublin",
            currentCondition = "cloudy",
            maxTemperature = 18,
            minTemperature = 11,
            windDirection = "north",
            windSpeed = 100,
            nextDayOutlook = "fog"
        )

        assertTrue(
            weather.windSpeed in 0..200
        )
    }
}