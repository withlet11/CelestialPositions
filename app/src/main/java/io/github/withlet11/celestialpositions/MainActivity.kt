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
import io.github.withlet11.astronomical.AstronomicalObject
import io.github.withlet11.astronomical.MessierObjectList
import io.github.withlet11.astronomical.StarList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private lateinit var messierList: ArrayList<AstronomicalObject>
    private lateinit var starList: ArrayList<AstronomicalObject>

    private val _licensesStateFlow = MutableStateFlow(OssLicenseList(arrayListOf()))
    private val licensesStateFlow = _licensesStateFlow.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        loadPreviousPosition()

        messierList = MessierObjectList().load(this)
        starList = StarList().load(this)

        createLicenses(this)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xffc0c0ff),
                    secondary = Color(0xff8080c0),
                    primaryContainer = Color(0xff000080),
                    secondaryContainer = Color(0xff000040),
                ),
            ) {
                MainScreen(
                    messierList = messierList,
                    starList = starList,
                    latitude = latitude,
                    longitude = longitude,
                    licensesStateFlow = licensesStateFlow
                )
            }
        }

    }

    private fun loadPreviousPosition() {
        val previous = getSharedPreferences("observation_position", Context.MODE_PRIVATE)

        try {
            latitude = previous.getFloat("latitude", 0f).toDouble()
            longitude = previous.getFloat("longitude", 0f).toDouble()
        } catch (e: ClassCastException) {
            latitude = 0.0
            longitude = 0.0
        } finally {
        }
    }

    private fun createLicenses(context: Context) {
        lifecycleScope.launch {
            _licensesStateFlow.update {
                OssLicenseList.create(context)
            }
        }
    }
}

/*
override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)
    val toolbar: Toolbar = findViewById(R.id.my_toolbar)
    toolbar.setTitle(R.string.app_name)
    toolbar.setLogo(R.drawable.ic_launcher_foreground)
    toolbar.inflateMenu(R.menu.menu_main)

    toolbar.setOnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.item_settings -> {
                val dialog = LocationSettingFragment()
                dialog.show(supportFragmentManager, "locationSetting")
            }
            R.id.item_licenses -> {
                startActivity(Intent(application, LicenseActivity::class.java))
            }
            R.id.item_credits -> {
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            }
        }
        true
    }

    localTimeField = findViewById(R.id.textview_localtime)
    utcField = findViewById(R.id.textview_utc)
    latitudeField = findViewById(R.id.textview_latitude)
    longitudeField = findViewById(R.id.textview_longitude)

    val pager = findViewById<ViewPager2>(R.id.pager)
    pager.adapter = tabAdapter

    val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
    TabLayoutMediator(tabLayout, pager) { tab, position ->
        tab.setText(arrayOf(R.string.messier_objects, R.string.stars)[position])
    }.attach()

    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    loadPreviousPosition()
}

override fun onDialogPositiveClick(dialog: DialogFragment) {
    loadPreviousPosition()
}

override fun onDialogNegativeClick(dialog: DialogFragment) {
    // Do nothing
}

private fun loadPreviousPosition() {
    val previous = getSharedPreferences("observation_position", Context.MODE_PRIVATE)

    try {
        latitude = previous.getFloat("latitude", 0f).toDouble()
        longitude = previous.getFloat("longitude", 0f).toDouble()
    } catch (e: ClassCastException) {
        latitude = 0.0
    } finally {
        tabAdapter.latitude = latitude
        tabAdapter.longitude = longitude
    }

    updateObservationPosition()
}

private fun updateObservationPosition() {
    latitudeField.text = "%+f".format(latitude)
    longitudeField.text = "%+f".format(longitude)
}

override fun onStart() {
    super.onStart()

    val timer = Timer()
    // val guiUpdater = Handler()

    timer.schedule(object : TimerTask() {
        override fun run() {
            val time = AstronomicalTimes(ZonedDateTime.now())
            Handler(Looper.getMainLooper()).post {
                updateTimeValues(time)
                tabAdapter.updateTable(time)
            }
        }
    }, UPDATE_INTERVAL, UPDATE_INTERVAL)
}

private fun updateTimeValues(time: AstronomicalTimes) {
    localTimeField.text = time.localTime.toString().substring(11, 19)
    utcField.text = time.utc.toString().substring(11, 19)
}
}


 */