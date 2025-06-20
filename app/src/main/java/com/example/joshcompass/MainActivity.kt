package com.example.joshcompass

import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.joshcompass.ui.theme.JoshCompassTheme

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private var pressureSensor: Sensor? = null
    private var innerOnAzimuthChange: ((Float) -> Unit)? = null // Callback to update UI
    private var innerOnAltitudeChange: ((Float) -> Unit)? = null // Callback to update UI
    private var pressureASL: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(FLAG_KEEP_SCREEN_ON)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        val outerOnAzimuthChange = { inner: (Float) -> Unit -> innerOnAzimuthChange = inner }
        val outerOnAltitudeChange = { inner: (Float) -> Unit -> innerOnAltitudeChange = inner }

        val sharedPreferences = getPreferences(MODE_PRIVATE)

        val pressureASLTxt = Utils.getPressureASL(sharedPreferences)
        pressureASL = pressureASLTxt.toFloat() * 33.863888f // Convert from inHg to hPa

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            JoshCompassTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        sharedPreferences = sharedPreferences,
                        outerOnAzimuthChange = outerOnAzimuthChange,
                        outerOnAltitudeChange = outerOnAltitudeChange,
                        onPressureASLChange = { pressureASL = it },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        rotationVectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        pressureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)

            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            if (azimuth < 0) azimuth += 360 // Convert to 0-360 range

            innerOnAzimuthChange?.invoke(azimuth)
        } else if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
            val pressure = event.values[0]

            pressureASL?.let { pASL ->
                val altitudeFeet = SensorManager.getAltitude(pASL, pressure) * 3.28084f
                innerOnAltitudeChange?.invoke(altitudeFeet)
            } ?: run {
                // This block should almost never run.
                val altitudeFeet = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure) * 3.28084f
                innerOnAltitudeChange?.invoke(altitudeFeet)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    sharedPreferences: SharedPreferences,
    outerOnAzimuthChange: ((Float) -> Unit) -> Unit,
    outerOnAltitudeChange: ((Float) -> Unit) -> Unit,
    onPressureASLChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(navController, startDestination = "main") {
        composable("main") {
            CompassScreen(
                navController,
                sharedPreferences = sharedPreferences,
                outerOnAzimuthChange = outerOnAzimuthChange,
                outerOnAltitudeChange = outerOnAltitudeChange,
                modifier = modifier)
        }
        composable("preferences") {
            PreferencesScreen(navController, sharedPreferences, onPressureASLChange)
        }
    }
}
