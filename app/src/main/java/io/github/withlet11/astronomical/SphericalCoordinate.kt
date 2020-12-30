/**
 * SphericalCoordinate.kt
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
import kotlin.math.*

class SphericalCoordinate(private val ra: Duration, val dec: Double) {
    val getMilliSecRa: Long
        get() = ra.toMillis()

    private fun hourAngle(siderealTime: Duration): Duration {
        val seconds = ((siderealTime - ra).seconds + 86400) % 86400
        return Duration.ofSeconds(seconds)
    }

    fun hourAngleStr(siderealTime: Duration): String = formatHMS(hourAngle(siderealTime))

    fun horizontal(siderealTime: Duration, latitude: Double): Pair<Double, Double> {
        val hourAngle = Math.toRadians(hourAngle(siderealTime).seconds.toDouble() / 86400 * 360)
        val dec = Math.toRadians(dec)
        val lat = Math.toRadians(latitude)

        val altitude = asin(
            sin(dec) * sin(lat) +
                    cos(dec) * cos(lat) * cos(hourAngle)
        )

        var azimuth = if (cos(altitude) == 0.0) 0.0 else Math.toDegrees(
            acos(
                (cos(lat) * sin(dec) - sin(lat) * cos(dec) * cos(hourAngle)) / cos(altitude)
            )
        )

        if (-cos(dec) * sin(hourAngle) / cos(altitude) < 0) azimuth = -azimuth
        azimuth = (azimuth + 360) % 360

        return Pair(Math.toDegrees(altitude), azimuth)
    }

    companion object {
        private fun toDoubleMultipliedBy1000(x: String): Double =
            if (x == "") 0.0 else x.toDouble() * 1000

        private fun separateFractionAndMultiplyBy60(xStr: String, y: Double): Pair<Long, Long> {
            val x: Double = xStr.toDouble()
            val yy: Long = (y + if (x % 1 != 0.0) (x % 1) * 60 * 1000 else 0.0).toLong()
            return Pair(floor(x / 1).toLong(), yy)
        }

        fun formatHMS(time: Duration): String {
            val h = time.toHours() % 24
            val m = time.toMinutes() % 60
            val s = (time.toNanos() / 1000000000) % 60
            return "%02dh %02dm %02ds".format(h, m, s)
        }

        fun formatDMS(degree: Double): String {
            val absSeconds = abs(degree * 3600.0).toLong()
            val s = absSeconds % 60
            val absMinutes = absSeconds / 60
            val m = absMinutes % 60
            val d = absMinutes / 60
            return (if (degree < 0) "−" else "+") + "%02d° %02d′ %02d″".format(d, m, s)
        }

        private fun raStringToDuration(raStr: String): Duration {
            val modifiedRA: String = Regex("[hms] *").replace(raStr, " ")
            val (raHourStr: String, raMinStr: String, raSecStr: String) = modifiedRA.split(" ")
            val raTotalMillisec = toDoubleMultipliedBy1000(raSecStr)
            val (raMin, raMillisec) = separateFractionAndMultiplyBy60(raMinStr, raTotalMillisec)
            return Duration.ofHours(raHourStr.toLong()) +
                    Duration.ofMinutes(raMin) +
                    Duration.ofMillis(raMillisec)
        }

        private fun decStringToDouble(decStr: String): Double {
            val modifiedDec: String = Regex("[°′″] *").replace(decStr, " ")
            val (decDegStr, decMinStr, decSecStr) = modifiedDec.split(" ")
            var dec: Double = if (decSecStr == "") 0.0 else decSecStr.toDouble() / 3600
            dec += decMinStr.toDouble() / 60
            return (decDegStr.substring(1)
                .toLong() + dec) * (if (decDegStr[0] == '−') -1 else 1)
        }

        fun makePosition(raStr: String, decStr: String): SphericalCoordinate = try {
            SphericalCoordinate(raStringToDuration(raStr), decStringToDouble(decStr))
        } catch (e: ClassCastException) {
            SphericalCoordinate(Duration.ZERO, 0.0)
        }
    }
}
