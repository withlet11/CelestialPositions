/**
 * MainActivity.kt
 *
 * Copyright 2020-2021 Yasuhiro Yamakawa <withlet11@gmail.com>
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
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.withlet11.astronomical.AstronomicalTimes
import java.time.ZonedDateTime
import java.util.*


class MainActivity : AppCompatActivity() {
    companion object {
        const val UPDATE_INTERVAL = 1000L
    }

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private val tabAdapter = AstronomicalListTabAdapter(this)
    private lateinit var localTimeField: TextView
    private lateinit var utcField: TextView
    private lateinit var latitudeField: TextView
    private lateinit var longitudeField: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 2 && data != null) {
            try {
                latitude = data.getDoubleExtra("LATITUDE", 0.0)
                longitude = data.getDoubleExtra("LONGITUDE", 0.0)
            } catch (e: ClassCastException) {
                latitude = 0.0
                longitude = 0.0
            } finally {
                tabAdapter.latitude = latitude
                tabAdapter.longitude = longitude
            }

            with(getSharedPreferences("observation_position", Context.MODE_PRIVATE).edit()) {
                putFloat("latitude", latitude.toFloat())
                putFloat("longitude", longitude.toFloat())
                commit()
            }

            updateObservationPosition()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_settings -> {
                val intent = Intent(application, LocationSettingActivity::class.java)
                intent.putExtra("LATITUDE", latitude)
                intent.putExtra("LONGITUDE", longitude)
                startActivityForResult(intent, 2)
            }
            R.id.item_licenses -> {
                startActivity(Intent(application, LicenseActivity::class.java))
            }
            R.id.item_credits -> {
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            }
            android.R.id.home -> finish()
        }
        return true
    }

    private fun loadPreviousPosition() {
        val previous = getSharedPreferences("observation_position", Context.MODE_PRIVATE)

        try {
            latitude = previous.getFloat("latitude", 0.0F).toDouble()
            longitude = previous.getFloat("longitude", 0.0F).toDouble()
        } catch (e: ClassCastException) {
            latitude = 0.0
            longitude = 0.0
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
