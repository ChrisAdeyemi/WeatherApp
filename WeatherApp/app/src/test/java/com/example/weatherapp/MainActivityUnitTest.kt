package com.example.weatherapp

import org.junit.Assert
import org.junit.Test

class MainActivityUnitTest {

    @Test
    fun mainActivityCanBeInstantiated() {
        val activity = MainActivity()
        Assert.assertNotNull(activity)
    }
}