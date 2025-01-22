/**
 * MainActivity.kt
 *
 * Copyright 2020-2025 Yasuhiro Yamakawa <withlet11@gmail.com>
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

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import io.github.withlet11.astronomical.MessierObjectList
import io.github.withlet11.astronomical.StarList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val _licensesStateFlow = MutableStateFlow(OssLicenseList(arrayListOf()))
    private val licensesStateFlow = _licensesStateFlow.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val location = loadPreviousPosition()
        val messierList = MessierObjectList().load(this)
        val starList = StarList().load(this)

        createLicenses(this)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xffd0d0e0),
                    secondary = Color(0xff8080c0),
                    primaryContainer = Color(0xff000080),
                    secondaryContainer = Color(0xff000040),
                ),
            ) {
                MainScreen(
                    messierList = messierList,
                    starList = starList,
                    initialLatitude = location.first,
                    initLongitude = location.second,
                    licensesStateFlow = licensesStateFlow
                )
            }
        }

    }

    private fun loadPreviousPosition(): Pair<Double, Double> {
        val previous = getSharedPreferences("observation_position", Context.MODE_PRIVATE)
        var latitude: Double
        var longitude: Double

        try {
            latitude = previous.getFloat("latitude", 0f).toDouble()
            longitude = previous.getFloat("longitude", 0f).toDouble()
        } catch (e: ClassCastException) {
            latitude = 0.0
            longitude = 0.0
        } finally {
        }
        return Pair(latitude, longitude)
    }

    private fun createLicenses(context: Context) {
        lifecycleScope.launch {
            _licensesStateFlow.update {
                OssLicenseList.create(context)
            }
        }
    }
}