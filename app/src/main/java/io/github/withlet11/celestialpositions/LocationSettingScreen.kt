/**
 * LocationSettingScreen.kt
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

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch

const val MAXIMUM_UPDATE_INTERVAL = 10000L
const val MINIMUM_UPDATE_INTERVAL = 5000L

@Composable
fun LocationSettingScreen(
    navController: NavHostController,
    latitude: MutableDoubleState,
    longitude: MutableDoubleState
) {
    var latitudeString by remember { mutableStateOf("%+f".format(latitude.doubleValue)) }
    var longitudeString by remember { mutableStateOf("%+f".format(longitude.doubleValue)) }
    var statusMessage by remember { mutableStateOf("") }
    var isLocationFieldEnabled by remember { mutableStateOf(true) }
    var isModifyButtonEnabled by remember { mutableStateOf(true) }
    var isLatitudeFieldValid by remember { mutableStateOf(true) }
    var isLongitudeFieldValid by remember { mutableStateOf(true) }

    val validValueColor = TextFieldDefaults.colors()
    val invalidValueColor = TextFieldDefaults.colors(
        focusedTextColor = Color(0xffff8080),
        unfocusedTextColor = Color(0xffff8080)
    )

    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var areLocationPermissionsGranted by remember {
        mutableStateOf(areLocationPermissionsAlreadyGranted(context))
    }
    var shouldShowPermissionRationale by remember {
        mutableStateOf(
            context.getActivity()
                ?.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                ?: false
        )
    }

    var shouldDirectUserToApplicationSettings by remember { mutableStateOf(false) }

    var currentPermissionsStatus by remember {
        mutableStateOf(
            decideCurrentPermissionStatus(
                areLocationPermissionsGranted,
                shouldShowPermissionRationale
            )
        )
    }

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            areLocationPermissionsGranted = permissions.values.reduce { acc, isPermissionGranted ->
                acc && isPermissionGranted
            }

            if (!areLocationPermissionsGranted) {
                shouldShowPermissionRationale =
                    context.getActivity()
                        ?.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                        ?: false
            }
            shouldDirectUserToApplicationSettings =
                !shouldShowPermissionRationale && !areLocationPermissionsGranted
            currentPermissionsStatus = decideCurrentPermissionStatus(
                areLocationPermissionsGranted,
                shouldShowPermissionRationale
            )
        })

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START &&
                !areLocationPermissionsGranted &&
                !shouldShowPermissionRationale
            ) {
                locationPermissionLauncher.launch(locationPermissions)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    )

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val locationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, MAXIMUM_UPDATE_INTERVAL)
            .setMinUpdateIntervalMillis(MINIMUM_UPDATE_INTERVAL)
            .setWaitForAccurateLocation(true)
            .build()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.padding(12.dp)
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            TextField(
                value = latitudeString,
                label = { Text(stringResource(R.string.latitude)) },
                enabled = isLocationFieldEnabled,
                colors = if (isLatitudeFieldValid) validValueColor else invalidValueColor,
                onValueChange = { text ->
                    var value = text.replace(',', '.').toDoubleOrNull()
                    value?.let { if (it > 90.0 || it < -90.0) value = null }
                    isLatitudeFieldValid = value != null
                    statusMessage =
                        if (!isLatitudeFieldValid || !isLongitudeFieldValid) context.getString(R.string.invalidValue) else ""
                    isModifyButtonEnabled = isLatitudeFieldValid && isLongitudeFieldValid
                    latitudeString = text
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = {
                    Text(text = "%+.4f".format(23.4567), color = Color.Gray)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            TextField(
                value = longitudeString,
                label = { Text(stringResource(R.string.longitude)) },
                enabled = isLocationFieldEnabled,
                colors = if (isLongitudeFieldValid) validValueColor else invalidValueColor,
                onValueChange = { text ->
                    var value = text.replace(',', '.').toDoubleOrNull()
                    value?.let { if (it > 90.0 || it < -90.0) value = null }
                    isLongitudeFieldValid = value != null
                    statusMessage =
                        if (!isLatitudeFieldValid || !isLongitudeFieldValid) context.getString(R.string.invalidValue) else ""
                    isModifyButtonEnabled = isLatitudeFieldValid && isLongitudeFieldValid
                    longitudeString = text
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = {
                    Text(text = "%+.3f".format(123.456), color = Color.Gray)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    statusMessage = context.getString(R.string.inGettingLocation)
                    startGPS(
                        context = context,
                        lockViewItems = { isLocationFieldEnabled = false },
                        unlockViewItems = { isLocationFieldEnabled = true },
                        fusedLocationClient = fusedLocationClient,
                        locationRequest = locationRequest,
                        locationCallback = object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                val location = locationResult.lastLocation

                                latitudeString = "%+f".format(location?.latitude ?: 0.0)
                                longitudeString = "%+f".format(location?.longitude ?: 0.0)
                                statusMessage = context.getString(R.string.locationDataRetrieved)
                                isLatitudeFieldValid = true
                                isLongitudeFieldValid = true

                                fusedLocationClient.removeLocationUpdates(this)
                            }

                        }
                    )
                },
                enabled = areLocationPermissionsGranted,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.gps))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    latitude.doubleValue =
                        latitudeString.replace(',', '.').toDoubleOrNull() ?: latitude.doubleValue
                    longitude.doubleValue =
                        longitudeString.replace(',', '.').toDoubleOrNull() ?: longitude.doubleValue

                    context.getSharedPreferences("observation_position", Context.MODE_PRIVATE)
                        ?.edit()
                        ?.run {
                            putFloat("latitude", latitude.doubleValue.toFloat())
                            putFloat("longitude", longitude.doubleValue.toFloat())
                            commit()
                        }
                    navController.popBackStack()
                },
                enabled = isModifyButtonEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.modify))
            }
        }
        if (shouldShowPermissionRationale) {
            LaunchedEffect(Unit) {
                scope.launch {
                    val userAction = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.pleaseAuthorizeLocationPermissions),
                        actionLabel = context.getString(R.string.approve),
                        duration = SnackbarDuration.Indefinite,
                        withDismissAction = true
                    )
                    when (userAction) {
                        SnackbarResult.ActionPerformed -> {
                            shouldShowPermissionRationale = false
                            locationPermissionLauncher.launch(locationPermissions)
                        }

                        SnackbarResult.Dismissed -> {
                            shouldShowPermissionRationale = false
                        }
                    }
                }
            }
        }
        if (shouldDirectUserToApplicationSettings) {
            openApplicationSettings(context)
        }
    }
}

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

private fun areLocationPermissionsAlreadyGranted(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun openApplicationSettings(context: Context) {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).also {
        context.startActivity(it)
    }
}

private fun decideCurrentPermissionStatus(
    locationPermissionsGranted: Boolean,
    shouldShowPermissionRationale: Boolean
): String {
    return if (locationPermissionsGranted) "Granted"
    else if (shouldShowPermissionRationale) "Rejected"
    else "Denied"
}

private fun startGPS(
    context: Context,
    lockViewItems: () -> Unit,
    unlockViewItems: () -> Unit,
    fusedLocationClient: FusedLocationProviderClient,
    locationRequest: LocationRequest,
    locationCallback: LocationCallback
) {
    lockViewItems()

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    unlockViewItems()
}