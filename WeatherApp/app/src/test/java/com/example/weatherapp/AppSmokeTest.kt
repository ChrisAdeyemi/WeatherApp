package com.example.weatherapp

import org.junit.Assert
import org.junit.Test

class AppSmokeTest {

    @Test
    fun weatherDataModelIsUsable() {
        val data = WeatherData(
            city = "TestCity",
            currentCondition = "sunny",
            maxTemperature = 25,
            minTemperature = 10,
            windDirection = "north",
            windSpeed = 50,
            nextDayOutlook = "cloudy"
        )

        Assert.assertEquals("TestCity", data.city)
    }
}