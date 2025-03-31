package com.example.joshcompass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.joshcompass.ui.theme.JoshCompassTheme
import kotlin.math.floor

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private var innerOnAzimuthChange: ((Int) -> Unit)? = null // Callback to update UI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val outerOnAzimuthChange = { inner: (Int) -> Unit -> innerOnAzimuthChange = inner }

        enableEdgeToEdge()
        setContent {
            JoshCompassTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CompassScreen(
                        outerOnAzimuthChange = outerOnAzimuthChange,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        rotationVectorSensor?.let {
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

            var azimuth = Math.toDegrees(orientationAngles[0].toDouble())
            if (azimuth < 0) azimuth += 360 // Convert to 0-360 range
            val iAzimuth = floor(azimuth).toInt()

            innerOnAzimuthChange?.invoke(iAzimuth)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        println("accuracy: $accuracy")
    }
}

@Composable
fun CompassScreen(outerOnAzimuthChange: ((Int) -> Unit) -> Unit, modifier: Modifier = Modifier) {
    var azimuth by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        outerOnAzimuthChange { newAzimuth ->
            azimuth = newAzimuth
        }
    }

    Compass(azimuth = azimuth, modifier = modifier)
}

@Composable
fun Compass(azimuth: Int, modifier: Modifier = Modifier) {
    Text(
        text = "Azimuth: $azimuth",
        modifier = modifier
    )
}
