package com.example.appen.ui.Home

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class SimpleDisplayFragmentTest {


    @Test
    fun kalkulerTest() {
        val home = HomeFragment()

        val simple = SimpleDisplayFragment(null)
        home.setUvTimeTest(5.0F)
        assertEquals(simple.anbefaling(2, home.uvTime, true), 30)

        home.setUvTimeTest(0.0F)
        assertEquals(simple.anbefaling(0, home.uvTime, true), 0)
    }
}