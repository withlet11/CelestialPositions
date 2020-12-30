package io.github.withlet11.celestialpositions

import io.github.withlet11.astronomical.AstronomicalTimes
import org.junit.Test

import org.junit.Assert.*
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    private fun getNano(h: Int, m: Int, ms: Int): Long = ((h * 60 + m) * 60 * 1000 + ms) * 1000000L
    @Test
    fun gmst_isCorrect1() {
        val time = AstronomicalTimes(ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")))
        println(time.gmst.toString())
        assert(abs(time.gmst.toNanos() - getNano(6, 40, 29234)) < 1500000000)
    }

    @Test
    fun gmst_isCorrect2() {
        val time = AstronomicalTimes(ZonedDateTime.of(2020, 2, 6, 14, 10, 9, 0, ZoneId.of("UTC")))
        println(time.gmst.toString())
        assert(abs(time.gmst.toNanos() - getNano(23, 14, 53886)) < 1500000000)
    }

    @Test
    fun gmst_isCorrect3() {
        val time = AstronomicalTimes(ZonedDateTime.of(2020, 6, 12, 6, 59, 59, 0, ZoneId.of("UTC")))
        println(time.gmst.toString())
        assert(abs(time.gmst.toNanos() - getNano(0, 24, 15572)) < 1500000000)
    }

    @Test
    fun gmst_isCorrect4() {
        val time = AstronomicalTimes(ZonedDateTime.of(2020, 9, 2, 0, 18, 59, 0, ZoneId.of("UTC")))
        println(time.gmst.toString())
        assert(abs(time.gmst.toNanos() - getNano(23, 5, 27418)) < 1500000000)
    }
}