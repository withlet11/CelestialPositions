/**
 * DetailsFragment.kt
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import io.github.withlet11.astronomical.AstronomicalTimes
import io.github.withlet11.astronomical.SphericalCoordinate
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.round
import kotlinx.coroutines.delay

class DetailsFragment : Fragment() {
    private lateinit var hourAngleField: String
    private lateinit var altitudeField: String
    private lateinit var azimuthField: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val args = arguments
        val name = args?.getString("NAME") ?: ""
        val milliSecRa = args?.getLong("RIGHT_ASCENSION") ?: 0
        val declination = args?.getDouble("DECLINATION") ?: 0.0
        val position = SphericalCoordinate(Duration.ofMillis(milliSecRa), declination)
        val details = args?.getString("DETAILS") ?: ""

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(colorScheme = darkColorScheme()) {
                    var time by remember {
                        mutableStateOf(AstronomicalTimes(ZonedDateTime.now()))
                    }
                    updateValues(position, time)
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(1000)
                            val now = AstronomicalTimes(ZonedDateTime.now())
                            updateValues(position, now)
                            time = now
                        }
                    }

                    Surface {
                        Column(modifier = Modifier.requiredWidthIn()) {
                            Text(
                                style = MaterialTheme.typography.titleLarge,
                                text = name
                            )
                            Spacer(modifier = Modifier.height(12.dp))
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
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                style = MaterialTheme.typography.bodyLarge,
                                text = details
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateValues(position: SphericalCoordinate, time: AstronomicalTimes) {
        if (isVisible) {
            view?.apply {
                hourAngleField = position.hourAngleStr(time.gmst + difference).substring(0, 7)
                val (altitude, azimuth) = position.horizontal(time.gmst + difference, latitude)
                altitudeField = SIGNED_THREE_DIGIT.format(round(altitude).toInt())
                azimuthField = UNSIGNED_THREE_DIGIT.format(round(azimuth).toInt())
            }
        }
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