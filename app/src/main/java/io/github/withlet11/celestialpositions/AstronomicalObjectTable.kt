/**
 * AstronomicalObjectTable.kt
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import io.github.withlet11.astronomical.*
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.round


data class TableValues(val name: String, val position: SphericalCoordinate, val details: String)

@Composable
fun AstronomicalObjectTable(
    navController: NavHostController,
    list: ArrayList<AstronomicalObject>,
    latitude: Double,
    longitude: Double,
    openDetails: (String, ArrayList<TableValues>) -> Unit
) {
    val tableValueList = arrayListOf<TableValues>()

    val scrollState = rememberScrollState()
    Column(modifier = Modifier.verticalScroll(scrollState)) {
        list.forEach { item ->
            val position = SphericalCoordinate.makePosition(item.ra, item.dec)
            tableValueList.add(TableValues(item.name, position, item.details))
            ObjectDataRow(
                navController,
                name = item.name,
                declinationFieldText = item.dec.substring(0, 7),
                tableValueList = tableValueList,
                position = position,
                latitude = latitude,
                longitude = longitude,
                openDetails = openDetails
            )
        }
    }
}

@Composable
fun ObjectDataRow(
    navController: NavHostController,
    name: String,
    declinationFieldText: String,
    position: SphericalCoordinate,
    tableValueList: ArrayList<TableValues>,
    latitude: Double,
    longitude: Double,
    openDetails: (String, ArrayList<TableValues>) -> Unit
) {
    val context = LocalContext.current

    val unsignedThreeDigit = context.getString(R.string.unsignedThreeDigit)
    val signedThreeDigit = context.getString(R.string.signedThreeDigit)

    var time by remember {
        mutableStateOf(AstronomicalTimes(ZonedDateTime.now()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            time = AstronomicalTimes(ZonedDateTime.now())
        }
    }

    val difference = Duration.ofMillis((longitude / 360 * 24 * 60 * 60 * 1000).toLong())
    val (altitude, azimuth) = position.horizontal(time.gmst + difference, latitude)
    val altitudeFieldText = signedThreeDigit.format(round(altitude).toInt())
    val azimuthFieldText = unsignedThreeDigit.format(round(azimuth).toInt())
    val hourAngleFieldText = position.hourAngleStr(time.gmst + difference).substring(0, 7)

    val style = TextStyle(
        fontSize = 14.sp,
        color = when {
            altitude > 45 -> Color(0xffffc0c0) // R.style.forHighAltitude
            altitude > 20 -> Color(0xffffc0c0) // R.style.forMiddleAltitude
            altitude > 0 -> Color(0xffc0a0a0) // R.style.forLowAltitude
            else -> Color(0xffa0a0a0) // R.style.forUnderHorizon
        },
        fontWeight = if (altitude > 45) FontWeight.Bold else FontWeight.Normal
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clickable { openDetails(name, tableValueList) },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(modifier = Modifier.weight(1.2f)) {
            Text(
                text = name,
                style = style,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(modifier = Modifier.weight(0.8f)) {
            Text(
                text = altitudeFieldText,
                style = style
            )
        }
        Box(modifier = Modifier.weight(0.8f)) {
            Text(
                text = azimuthFieldText,
                style = style
            )
        }
        Box(modifier = Modifier.weight(1.0f)) {
            Text(
                text = hourAngleFieldText,
                style = style
            )
        }
        Box(modifier = Modifier.weight(1.0f)) {
            Text(
                text = declinationFieldText,
                style = style
            )
        }
    }
}

/*
private fun openDetails(
    navController: NavHostController,
    name: String,
    tableValueList: ArrayList<TableValues>,
) {

    val index = tableValueList.indexOfFirst { it.name == name }
    navController.navigate(MainNavigation.MessierObjectDetails(index = index))

 */
/*
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
context.startActivity(intent)

 */
// }
// }

/*
override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View? = inflater.inflate(layout, container, false)


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

 */