/**
 * AstronomicalListFragment.kt
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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.fragment.app.Fragment
import io.github.withlet11.astronomical.*
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.round


data class TableValues(val name: String, val position: SphericalCoordinate, val details: String)

abstract class AstronomicalListFragment : Fragment() {
    var longitude = 0.0
    var latitude = 0.0
    abstract val layout: Int
    abstract val tableView: Int
    private lateinit var unsignedThreeDigit: String
    private lateinit var signedThreeDigit: String
    private var tableValueList = arrayListOf<TableValues>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        unsignedThreeDigit = getString(R.string.unsignedThreeDigit)
        signedThreeDigit = getString(R.string.signedThreeDigit)
        val table = view.findViewById<TableLayout>(tableView)
        activity?.let { getList().load(it.applicationContext) }
            ?.forEachIndexed { i, item -> addRow(table, item, i) }
    }

    abstract fun getList(): AstronomicalObjectList

    private fun addRow(table: TableLayout, item: AstronomicalObject, i: Int) {
        layoutInflater.inflate(R.layout.row, table)
        val row = table.getChildAt(i) as LinearLayout

        row.setOnClickListener { view ->
            if (view is LinearLayout) {
                val child = view.getChildAt(0)
                if (child is TextView) {
                    val name = child.text.toString()
                    val index = tableValueList.indexOfFirst { it.name == name }
                    val intent = Intent(context, DetailsActivity::class.java)
                    intent.putExtra("INDEX", index)
                    intent.putExtra("LATITUDE", latitude)
                    intent.putExtra("LONGITUDE", longitude)
                    intent.putExtra(
                        "DECLINATION",
                        ArrayList<Double>().apply { tableValueList.forEach { add(it.position.dec) } }
                            .toDoubleArray()
                    )
                    intent.putExtra(
                        "RIGHT_ASCENSION",
                        ArrayList<Long>().apply { tableValueList.forEach { add(it.position.getMilliSecRa) } }
                            .toLongArray()
                    )
                    intent.putStringArrayListExtra(
                        "NAME",
                        ArrayList<String>().apply { tableValueList.forEach { add(it.name) } })
                    intent.putStringArrayListExtra(
                        "DETAILS",
                        ArrayList<String>().apply { tableValueList.forEach { add(it.details) } })
                    startActivityForResult(intent, 2)
                }
            }
        }

        setValuesToCells(item, row)
    }

    private fun setValuesToCells(item: AstronomicalObject, row: LinearLayout) {
        val nameField = row.findViewById(R.id.textview_name) as TextView
        val declinationField = row.findViewById(R.id.textview_declination) as TextView

        nameField.text = item.name
        declinationField.text = item.dec.substring(0, 7)

        val position = SphericalCoordinate.makePosition(item.ra, item.dec)
        tableValueList.add(TableValues(item.name, position, item.details))

        val time = AstronomicalTimes(ZonedDateTime.now())
        updateValues(time, row, position)
    }

    private fun setStyle(row: LinearLayout, altitude: Double) {
        val style = when {
            altitude > 45 -> R.style.forHighAltitude
            altitude > 20 -> R.style.forMiddleAltitude
            altitude > 0 -> R.style.forLowAltitude
            else -> R.style.forUnderHorizon
        }

        row.children.forEach { (it as? TextView)?.run { setTextAppearance(style) } }
    }

    fun updateTable(time: AstronomicalTimes) {
        if (isVisible) {
            val table = view?.findViewById<TableLayout>(tableView)
            val children = table?.children
            children?.forEachIndexed { index, row ->
                (row as? LinearLayout)?.let {
                    updateValues(
                        time,
                        it,
                        tableValueList.elementAt(index).position
                    )
                }
            }
        }
    }

    private fun updateValues(
        time: AstronomicalTimes,
        row: LinearLayout,
        position: SphericalCoordinate
    ) {
        val difference = Duration.ofMillis((longitude / 360 * 24 * 60 * 60 * 1000).toLong())
        val (altitude, azimuth) = position.horizontal(time.gmst + difference, latitude)
        val altitudeField = row.findViewById(R.id.textview_altitude) as TextView
        val azimuthField = row.findViewById(R.id.textview_azimuth) as TextView
        val hourAngleField = row.findViewById(R.id.textview_hourangle) as TextView
        altitudeField.text = signedThreeDigit.format(round(altitude).toInt())
        azimuthField.text = unsignedThreeDigit.format(round(azimuth).toInt())
        hourAngleField.text = position.hourAngleStr(time.gmst + difference).substring(0, 7)

        setStyle(row, altitude)
    }
}
