/**
 * AstronomicalTimes.kt
 *
 * Copyright 2020 Yasuhiro Yamakawa <withlet11@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.withlet11.astronomical

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.ZoneOffset
import kotlin.math.floor

class AstronomicalTimes(time: ZonedDateTime?) {
    private val dut1: Duration = Duration.ofNanos(-243 * 1000000)  // on 2020-06-25
    private val alternative = LocalDateTime.ofInstant(
        ZonedDateTime.parse("2000-01-01T12:00:00+00:00").toInstant(),
        ZoneOffset.UTC
    ) - this.dut1
    val localTime: LocalDateTime = time?.toLocalDateTime() ?: alternative
    val utc: LocalDateTime =
        time?.let { LocalDateTime.ofInstant(it.toInstant(), ZoneOffset.UTC) } ?: alternative
    private val ut1: LocalDateTime = utc + dut1

    private val elapsedSeconds =
        Duration.ofHours(ut1.hour.toLong()) +
        Duration.ofMinutes(ut1.minute.toLong()) +
        Duration.ofSeconds(ut1.second.toLong()) +
        Duration.ofNanos(ut1.nano.toLong())

    private val jd =
        floor(365.25 * (ut1.year - (12 - ut1.monthValue) / 10)) +
                (ut1.year - (12 - ut1.monthValue) / 10) / 400 -
                (ut1.year - (12 - ut1.monthValue) / 10) / 100 +
                floor(30.59 * (ut1.monthValue + (12 - ut1.monthValue) / 10 * 12 - 2)) +
                ut1.dayOfMonth +
                1721088.5 +
                elapsedSeconds.seconds / 86400.0

//    val tjd
//        get() = jd - 2439999.5

    // Julian centuries at UT1=0
    private val jcAt0: Double = let {
            val jdAt0: Double = jd - elapsedSeconds.seconds / 86400.0
            (jdAt0 - 2451545.0) / 36525.0
        }

    // Greenwich mean sidereal time (GMST)
    val gmst: Duration = let {
            val c0 = 24110.54841  // https://www.cfa.harvard.edu/~jzhao/times.html
            val c1 = 8640184.812866
            val c2 = 0.093104
            val c3 = 0.0000062
            val t = jcAt0
            val solarTimeIntervals = 1.0 + c1 / 36525.0 / 86400.0
            val second: Double =
                ((c0 + c1 * t + c2 * t * t - c3 * t * t * t) % 86400.0 +
                solarTimeIntervals * elapsedSeconds.seconds) % 86400.0

            Duration.ofMillis(floor(second * 1000.0).toLong())
        }
}