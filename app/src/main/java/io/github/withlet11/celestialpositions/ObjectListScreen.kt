/**
 * ObjectListScreen.kt
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import io.github.withlet11.astronomical.AstronomicalObject
import io.github.withlet11.astronomical.AstronomicalTimes
import kotlinx.coroutines.delay
import java.time.ZonedDateTime

@Composable
fun ObjectListScreen(
    navController: NavHostController,
    messierList: ArrayList<AstronomicalObject>,
    starList: ArrayList<AstronomicalObject>,
    latitude: Double,
    longitude: Double
) {
    var time by remember {
        mutableStateOf(AstronomicalTimes(ZonedDateTime.now()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            time = AstronomicalTimes(ZonedDateTime.now())
        }
    }

    Column(modifier = Modifier.padding(4.dp)) {
        TableHeader(
            time = time,
            latitude = latitude,
            longitude = longitude
        )

        val pagerState = rememberPagerState(pageCount = { 2 })
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> AstronomicalObjectTable(
                    navController = navController,
                    list = messierList,
                    latitude = latitude,
                    longitude = longitude,
                    openDetails = fun(name: String, tableValueList: ArrayList<TableValues>) {
                        val index = tableValueList.indexOfFirst { it.name == name }
                        navController.navigate(MainNavigation.MessierObjectDetails(index = index))
                    }
                )

                else -> AstronomicalObjectTable(
                    navController = navController,
                    list = starList,
                    latitude = latitude,
                    longitude = longitude,
                    openDetails = fun(name: String, tableValueList: ArrayList<TableValues>) {
                        val index = tableValueList.indexOfFirst { it.name == name }
                        navController.navigate(MainNavigation.StarDetails(index = index))
                    }
                )
            }
        }
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val activeStyle = TextStyle(
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            val inactiveStyle = TextStyle(
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .weight(1f)
                    .background(
                        color = if (pagerState.currentPage == 0)
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        else MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                Text(
                    text = stringResource(R.string.messier_objects),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    style = if (pagerState.currentPage == 0) activeStyle else inactiveStyle
                )
            }
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .weight(1f)
                    .background(
                        color = if (pagerState.currentPage == 1)
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        else MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                Text(
                    text = stringResource(R.string.stars),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    style = if (pagerState.currentPage != 0) activeStyle else inactiveStyle
                )
            }
        }
    }
}

@Composable
private fun TableHeader(
    time: AstronomicalTimes,
    latitude: Double,
    longitude: Double
) {
    val latitudeFieldText = "%+f".format(latitude)
    val longitudeFieldText = "%+f".format(longitude)
    val localTimeFieldText = time.localTime.toString().substring(11, 19)
    val utcFieldText = time.utc.toString().substring(11, 19)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = stringResource(R.string.localTimeLabel),
            fontSize = 14.sp,
            textAlign = TextAlign.End
        )
        Text(
            text = localTimeFieldText,
            fontSize = 14.sp
        )
        Text(
            text = stringResource(R.string.latitude),
            fontSize = 14.sp,
            textAlign = TextAlign.End
        )
        Text(
            text = latitudeFieldText,
            fontSize = 14.sp
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = stringResource(R.string.utcLabel),
            fontSize = 14.sp,
            textAlign = TextAlign.End
        )
        Text(
            text = utcFieldText,
            fontSize = 14.sp
        )
        Text(
            text = stringResource(R.string.longitude),
            fontSize = 14.sp,
            textAlign = TextAlign.End
        )
        Text(
            text = longitudeFieldText,
            fontSize = 14.sp
        )
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1.2f)) {
            Text(
                text = stringResource(R.string.commonName),
                fontSize = 14.sp
            )
        }
        Box(modifier = Modifier.weight(0.8f)) {
            Text(
                text = stringResource(R.string.altitude),
                fontSize = 14.sp
            )
        }
        Box(modifier = Modifier.weight(0.8f)) {
            Text(
                text = stringResource(R.string.azimuth),
                fontSize = 14.sp
            )
        }
        Box(modifier = Modifier.weight(1.0f)) {
            Text(
                text = stringResource(R.string.hourAngle),
                fontSize = 14.sp
            )
        }
        Box(modifier = Modifier.weight(1.0f)) {
            Text(
                text = stringResource(R.string.declination),
                fontSize = 14.sp
            )
        }
    }
}

