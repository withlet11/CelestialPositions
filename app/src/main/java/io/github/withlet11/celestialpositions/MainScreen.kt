/**
 * MainScreen.kt
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

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.withlet11.astronomical.AstronomicalObject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable


sealed interface MainNavigation {
    @Serializable
    object ObjectList

    @Serializable
    data class MessierObjectDetails(val index: Int)

    @Serializable
    data class StarDetails(val index: Int)

    @Serializable
    object LocationSettings

    @Serializable
    object License

    @Serializable
    object OssLicense

    @Serializable
    data class OssLicenseDetails(val name: String, val terms: String)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenAppBar(
    currentScreen: NavDestination?,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    navController: NavHostController,
) {
    var expanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = stringResource(
                    when {
                        currentScreen == null -> R.string.app_name
                        currentScreen.hasRoute(MainNavigation.MessierObjectDetails::class) -> R.string.messier_objects
                        currentScreen.hasRoute(MainNavigation.StarDetails::class) -> R.string.stars
                        currentScreen.hasRoute(MainNavigation.LocationSettings::class) -> R.string.locationSettings
                        currentScreen.hasRoute(MainNavigation.License::class) -> R.string.license
                        currentScreen.hasRoute(MainNavigation.OssLicense::class) -> R.string.opensource_licenses
                        currentScreen.hasRoute(MainNavigation.OssLicenseDetails::class) -> R.string.opensource_licenses
                        else -> R.string.app_name
                    }
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Localized description"
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.locationSettings)) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Place,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        navController.navigate(MainNavigation.LocationSettings)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.license)) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        navController.navigate(MainNavigation.License)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.opensource_licenses)) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        navController.navigate(MainNavigation.OssLicense)
                        expanded = false
                    }
                )
            }
        },
    )
}

@Composable
fun MainScreen(
    messierList: ArrayList<AstronomicalObject>,
    starList: ArrayList<AstronomicalObject>,
    latitude: Double,
    longitude: Double,
    licensesStateFlow: StateFlow<OssLicenseList>,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination
    var _latitude = remember { mutableDoubleStateOf(latitude) }
    var _longitude = remember { mutableDoubleStateOf(longitude) }

    Scaffold(
        topBar = {
            MainScreenAppBar(
                currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                navController = navController,
            )
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = MainNavigation.ObjectList,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<MainNavigation.ObjectList> {
                ObjectListScreen(
                    navController = navController,
                    messierList = messierList,
                    starList = starList,
                    latitude = _latitude.doubleValue,
                    longitude = _longitude.doubleValue
                )
            }
            composable<MainNavigation.MessierObjectDetails> { backStackEntry ->
                val index = backStackEntry.toRoute<MainNavigation.MessierObjectDetails>().index

                ObjectDetailScreen(
                    objectList = messierList,
                    latitude = _latitude.doubleValue,
                    longitude = _longitude.doubleValue,
                    index = index
                )
            }
            composable<MainNavigation.StarDetails> { backStackEntry ->
                val index = backStackEntry.toRoute<MainNavigation.StarDetails>().index

                ObjectDetailScreen(
                    objectList = starList,
                    latitude = _latitude.doubleValue,
                    longitude = _longitude.doubleValue,
                    index = index
                )
            }
            composable<MainNavigation.LocationSettings> {
                LocationSettingScreen(
                    navController = navController,
                    latitude = _latitude,
                    longitude = _longitude
                )
            }
            composable<MainNavigation.License> {
                LicenceScreen()
            }
            composable<MainNavigation.OssLicense> {
                OSSLicenseListScreen(
                    navController = navController,
                    licensesStateFlow = licensesStateFlow
                )
            }
            composable<MainNavigation.OssLicenseDetails> { backStackEntry ->
                val name = backStackEntry.toRoute<MainNavigation.OssLicenseDetails>().name
                val terms = backStackEntry.toRoute<MainNavigation.OssLicenseDetails>().terms

                OssLicenseDetailScreen(name = name, terms = terms)
            }
        }
    }
}
