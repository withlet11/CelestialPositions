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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
    var latitudeString by remember { mutableStateOf(latitude.doubleValue.toString()) }
    var longitudeString by remember { mutableStateOf(longitude.doubleValue.toString()) }
    var statusMessage by remember { mutableStateOf("") }
    var isLocationFieldEnabled by remember { mutableStateOf(true) }
    var isModifyButtonEnabled by remember { mutableStateOf(true) }

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

    Surface(modifier = Modifier.padding(12.dp)) {
        Scaffold(snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }) { contentPadding ->
            Column(modifier = Modifier.padding(contentPadding)) {
                TextField(
                    value = latitudeString,
                    label = { Text(stringResource(R.string.latitude)) },
                    enabled = isLocationFieldEnabled,
                    onValueChange = { latitudeString = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = longitudeString,
                    label = { Text(stringResource(R.string.longitude)) },
                    enabled = isLocationFieldEnabled,
                    onValueChange = { longitudeString = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        latitude.doubleValue = latitudeString.toDoubleOrNull() ?: 0.0
                        longitude.doubleValue = longitudeString.toDoubleOrNull() ?: 0.0

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
                            message = context.getString(R.string.pleaseAuthorizeLocationPermistions),
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

/*


private fun requestLocationPermission(context: Context, statusMessage: MutableState<String>) {
    if (ActivityCompat.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    ) {
        statusMessage.value =
            context.getString(R.string.checkAppLvlPermission)
                .format(context.getString(R.string.app_name))
    } else {
        ActivityCompat.requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMISSION
        )
    }
}
}
 */
/*
class LocationSettingFragment : DialogFragment() {
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
    private lateinit var latitudeField: TextView
    private lateinit var longitudeField: TextView

    private lateinit var getLocationButton: Button
    private lateinit var statusField: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var dialog: AlertDialog

    companion object {
        private const val MAXIMUM_UPDATE_INTERVAL = 10000L
        private const val MINIMUM_UPDATE_INTERVAL = 5000L
        private const val REQUEST_PERMISSION = 1000
    }

    interface LocationSettingDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    private var listener: LocationSettingDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as LocationSettingDialogListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val locationSettingView = inflater.inflate(R.layout.fragment_location_setting, null)
        builder.setView(locationSettingView)
            .setTitle(R.string.locationSettings)
            .setPositiveButton("Modified") { _, _ ->
                context?.getSharedPreferences("observation_position", Context.MODE_PRIVATE)?.edit()
                    ?.run {
                        putFloat("latitude", latitude?.toFloat() ?: 0f)
                        putFloat("longitude", longitude?.toFloat() ?: 0f)
                        commit()
                    }
                listener?.onDialogPositiveClick(this)
            }
            .setNegativeButton("Cancel") { _, _ -> listener?.onDialogNegativeClick(this) }

        getPreviousValues()
        prepareGUIComponents(locationSettingView)

        return builder.create().also { dialog = it }
    }

    override fun onStart() {
        super.onStart()
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, MAXIMUM_UPDATE_INTERVAL)
            .setMinUpdateIntervalMillis(MINIMUM_UPDATE_INTERVAL)
            .setWaitForAccurateLocation(true)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation

                latitude = location?.latitude
                longitude = location?.longitude
                latitudeField.text = "%+f".format(latitude)
                longitudeField.text = "%+f".format(longitude)
                unlockViewItems()
                statusField.text = ""

                fusedLocationClient.removeLocationUpdates(this)
            }
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    private fun getPreviousValues() {
        context?.getSharedPreferences("observation_position", Context.MODE_PRIVATE)?.run {
            latitude = getFloat("latitude", 0f).toDouble()
            longitude = getFloat("longitude", 0f).toDouble()
        }
    }

    private fun prepareGUIComponents(locationSettingView: View) {
        latitudeField = locationSettingView.findViewById<TextView>(R.id.latitudeField).apply {
            keyListener = DigitsKeyListener.getInstance("0123456789.,+-")
            setAutofillHints("%+.4f".format(23.4567))
            hint = "%+.4f".format(23.4567)
            text = "%+f".format(latitude)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    latitude = latitudeField.text.toString().replace(',', '.').toDoubleOrNull()
                    latitude?.let { if (it > 90.0 || it < -90.0) latitude = null }
                    latitudeField.setTextColor(if (latitude == null) Color.RED else Color.WHITE)
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                        latitude != null && longitude != null
                }
            })
        }

        longitudeField = locationSettingView.findViewById<TextView>(R.id.longitudeField).apply {
            keyListener = DigitsKeyListener.getInstance("0123456789.,+-")
            setAutofillHints("%+.3f".format(123.456))
            hint = "%+.3f".format(123.456)
            text = "%+f".format(longitude)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    longitude = longitudeField.text.toString().replace(',', '.').toDoubleOrNull()
                    longitude?.let { if (it > 180.0 || it < -180.0) longitude = null }
                    longitudeField.setTextColor(if (longitude == null) Color.RED else Color.WHITE)
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                        latitude != null && longitude != null
                }
            })
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        getLocationButton = locationSettingView.findViewById<Button>(R.id.getLocationButton).apply {
            setOnClickListener { startGPS() }
        }

        statusField = locationSettingView.findViewById(R.id.statusField)

    }

    private fun lockViewItems() {
        latitudeField.isEnabled = false
        longitudeField.isEnabled = false
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        getLocationButton.isEnabled = false
    }

    private fun unlockViewItems() {
        latitudeField.isEnabled = true
        longitudeField.isEnabled = true
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
            latitude != null && longitude != null
        getLocationButton.isEnabled = true
    }

    private fun startGPS() {
        context?.let { context ->
            lockViewItems()
            statusField.text = getString(R.string.inGettingLocation)
            val isPermissionFineLocation = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            )
            val isPermissionCoarseLocation = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            )

            if (isPermissionFineLocation != PackageManager.PERMISSION_GRANTED &&
                isPermissionCoarseLocation != PackageManager.PERMISSION_GRANTED
            ) {
                unlockViewItems()
                requestLocationPermission()
            } else {
                val locationManager: LocationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                } else {
                    unlockViewItems()
                    statusField.text = getString(R.string.pleaseCheckIfGPSIsOn)
                }
            }
        }
    }

    private fun requestLocationPermission() {
        activity?.let { activity ->
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                statusField.text =
                    getString(R.string.checkAppLvlPermission).format(getString(R.string.app_name))
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION
                )
            }
        }
    }
}

 */