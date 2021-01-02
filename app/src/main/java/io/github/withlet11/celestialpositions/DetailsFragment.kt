/**
 * DetailsFragment.kt
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

package io.github.withlet11.celestialpositions

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.github.withlet11.astronomical.AstronomicalTimes
import io.github.withlet11.astronomical.SphericalCoordinate
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.round

class DetailsFragment : Fragment() {
    private var position: SphericalCoordinate = SphericalCoordinate(Duration.ZERO, 0.0)
    private var timer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get arguments
        val args = arguments
        val name = args?.getString("NAME") ?: ""
        val milliSecRa = args?.getLong("RIGHT_ASCENSION") ?: 0
        val declination = args?.getDouble("DECLINATION") ?: 0.0
        position = SphericalCoordinate(Duration.ofMillis(milliSecRa), declination)
        val details = args?.getString("DETAILS") ?: ""

        // get gui part IDs
        val nameField = view.findViewById<TextView>(R.id.textview_name)
        val declinationField = view.findViewById<TextView>(R.id.textview_declination)
        val detailsField = view.findViewById<TextView>(R.id.textview_details)

        // set values
        nameField.text = name
        declinationField.text = SphericalCoordinate.formatDMS(declination).subSequence(0, 7)
        detailsField.text = details
        updateValues()
    }

    override fun onStart() {
        super.onStart()
        super.onResume()

        updateValues()

        val guiUpdater = Handler(Looper.getMainLooper())

        timer = timer ?: Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                guiUpdater.post { updateValues() }
            }
        }, 1000, 1000)
    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
        timer = null
    }

    fun updateValues() {
        if (isVisible) {
            view?.apply {
                val hourAngleField = findViewById<TextView>(R.id.textview_hourangle)
                val altitudeField = findViewById<TextView>(R.id.textview_altitude)
                val azimuthField = findViewById<TextView>(R.id.textview_azimuth)

                val time = AstronomicalTimes(ZonedDateTime.now())
                hourAngleField.text = position.hourAngleStr(time.gmst + difference).substring(0, 7)
                val (altitude, azimuth) = position.horizontal(time.gmst + difference, latitude)
                altitudeField.text = SIGNED_THREE_DIGIT.format(round(altitude).toInt())
                azimuthField.text = UNSIGNED_THREE_DIGIT.format(round(azimuth).toInt())
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