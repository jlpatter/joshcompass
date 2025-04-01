package com.example.joshcompass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.joshcompass.ui.theme.JoshCompassTheme
import kotlin.math.floor

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private var innerOnAzimuthChange: ((Float) -> Unit)? = null // Callback to update UI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val outerOnAzimuthChange = { inner: (Float) -> Unit -> innerOnAzimuthChange = inner }

        enableEdgeToEdge()
        setContent {
            JoshCompassTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
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

            var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            if (azimuth < 0) azimuth += 360 // Convert to 0-360 range

            innerOnAzimuthChange?.invoke(azimuth)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        println("accuracy: $accuracy")
    }
}

fun getDirection(azimuth: Int): String {
    return when (azimuth) {
        in 23..67 -> "NE"
        in 68..112 -> "E"
        in 113..157 -> "SE"
        in 158..202 -> "S"
        in 203..247 -> "SW"
        in 248..292 -> "W"
        in 293..337 -> "NW"
        else -> "N"
    }
}

@Composable
fun CompassScreen(outerOnAzimuthChange: ((Float) -> Unit) -> Unit, modifier: Modifier = Modifier) {
    var azimuth by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        outerOnAzimuthChange { newAzimuth ->
            azimuth = newAzimuth
        }
    }

    Compass(azimuth = azimuth, modifier = modifier)
}

@Composable
fun Compass(azimuth: Float, modifier: Modifier = Modifier) {
    val iAzimuth: Int = floor(azimuth).toInt()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text(
                text = "$iAzimuth° ${getDirection(iAzimuth)}",
                fontSize = 12.em,
                modifier = modifier.align(Alignment.CenterHorizontally)
            )
            Image(
                painter = painterResource(id = R.drawable.gps_arrow),
                contentDescription = "Compass Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(300.dp).graphicsLayer(
                    rotationZ = 360f - azimuth
                )
            )
        }
    }
}
