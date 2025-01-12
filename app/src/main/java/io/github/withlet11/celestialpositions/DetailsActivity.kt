/**
 * DetailsActivity.kt
 *
 * Copyright 2025 Yasuhiro Yamakawa <withlet11@gmail.com>
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

package io.github.withlet11.celestialpositions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.withlet11.astronomical.AstronomicalTimes
import io.github.withlet11.astronomical.SphericalCoordinate
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.round


class DetailsActivity : ComponentActivity() {
    private lateinit var nameList: ArrayList<String>
    private lateinit var declinationList: DoubleArray
    private lateinit var rightAscensionList: LongArray
    private lateinit var detailsList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val index = intent.run {
            nameList = getStringArrayListExtra("NAME") ?: arrayListOf()
            declinationList = getDoubleArrayExtra("DECLINATION") ?: doubleArrayOf()
            rightAscensionList = getLongArrayExtra("RIGHT_ASCENSION") ?: longArrayOf()
            detailsList = getStringArrayListExtra("DETAILS") ?: arrayListOf()
            latitude = getDoubleExtra("LATITUDE", 0.0)
            longitude = getDoubleExtra("LONGITUDE", 0.0)
            getIntExtra("INDEX", 0)
        }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                val pagerState =
                    rememberPagerState(pageCount = { nameList.size }, initialPage = index)

                Surface {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page: Int ->
                        Column(
                            modifier = Modifier
                                .requiredWidthIn()
                                .fillMaxHeight()
                        ) {
                            ObjectNameTitle(nameList[page])
                            Spacer(modifier = Modifier.height(12.dp))
                            PositionInfo(rightAscensionList[page], declinationList[page])
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailsTextView(detailsList[page])
                        }
                    }
                }
            }

        }
    }

    @Composable
    private fun ObjectNameTitle(name: String) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge
        )
    }

    @Composable
    private fun PositionInfo(milliSecRa: Long, declination: Double) {
        var time by remember {
            mutableStateOf(AstronomicalTimes(ZonedDateTime.now()))
        }

        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                time = AstronomicalTimes(ZonedDateTime.now())
            }
        }

        val position = SphericalCoordinate(Duration.ofMillis(milliSecRa), declination)
        val hourAngleField = position.hourAngleStr(time.gmst + difference).substring(0, 7)
        val (altitude, azimuth) = position.horizontal(time.gmst + difference, latitude)
        val altitudeField = SIGNED_THREE_DIGIT.format(round(altitude).toInt())
        val azimuthField = UNSIGNED_THREE_DIGIT.format(round(azimuth).toInt())

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = getString(R.string.declination)
            )
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = SphericalCoordinate.formatDMS(declination)
                    .subSequence(0, 7)
                    .toString()
            )
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = getString(R.string.hourAngle)
            )
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = hourAngleField
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = getString(R.string.altitude)
            )
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = altitudeField
            )
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = getString(R.string.azimuth)
            )
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = azimuthField
            )
        }
    }

    @Composable
    private fun DetailsTextView(details: String) {
        Text(
            style = MaterialTheme.typography.bodyLarge,
            text = details
        )
    }

    companion object {
        var latitude: Double = 0.0

        var longitude: Double = 0.0
            set(value) {
                field = value
                difference = Duration.ofMillis((longitude / 360 * 24 * 60 * 60 * 1000).toLong())
            }

        private var difference = Duration.ofMillis((longitude / 360 * 24 * 60 * 60 * 1000).toLong())
        const val UNSIGNED_THREE_DIGIT: String = "%3d°"
        const val SIGNED_THREE_DIGIT: String = "%+3d°"
    }
}
