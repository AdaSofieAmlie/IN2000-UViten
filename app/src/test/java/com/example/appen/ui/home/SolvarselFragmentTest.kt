package com.example.appen.ui.home
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SolvarselFragmentTest {


    @Test
    fun kalkulerTest() {
        val home = HomeFragment()

        val simple = SolvarselFragment(null)
        home.setUvTimeTest(5.0F)
        assertEquals(simple.anbefaling(2, home.uvTime, true), 30)

        home.setUvTimeTest(0.0F)
        assertEquals(simple.anbefaling(0, home.uvTime, true), 0)
    }
}