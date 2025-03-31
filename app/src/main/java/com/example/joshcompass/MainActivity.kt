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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.joshcompass.ui.theme.JoshCompassTheme
import kotlin.math.floor

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        enableEdgeToEdge()
        setContent {
            JoshCompassTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
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

            println("Azimuth: $iAzimuth")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        println("accuracy: $accuracy")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JoshCompassTheme {
        Greeting("Android")
    }
}