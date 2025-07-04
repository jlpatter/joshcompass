package com.example.joshcompass

import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.navigation.NavHostController
import kotlin.math.round


private fun getDirection(azimuth: Int): String {
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

private fun getFinalAzimuth(azimuth: Float, offset: Int): Float {
    var finalAzimuth = azimuth + offset.toFloat()
    if (finalAzimuth < 0f) {
        finalAzimuth += 360f
    } else if (finalAzimuth > 360f) {
        finalAzimuth -= 360f
    }
    return finalAzimuth
}

@Composable
fun CompassScreen(
    navController: NavHostController,
    sharedPreferences: SharedPreferences,
    outerOnAzimuthChange: ((Float) -> Unit) -> Unit,
    outerOnAltitudeChange: ((Float) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var azimuth by remember { mutableFloatStateOf(0f) }
    // Note the inclusion of "-" to flip the sign for setting the direction of the offset properly.
    val offset = remember { -sharedPreferences.getInt("offset", 0) }
    var altitude by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        outerOnAzimuthChange { newAzimuth ->
            azimuth = newAzimuth
        }
    }

    LaunchedEffect(Unit) {
        outerOnAltitudeChange { newAltitude ->
            altitude = newAltitude
        }
    }

    val finalAzimuth by remember { derivedStateOf { getFinalAzimuth(azimuth, offset) } }
    val invertedFinalAzimuth by remember { derivedStateOf { 360f - finalAzimuth } }
    val iAzimuth by remember { derivedStateOf { round(finalAzimuth).toInt() } }
    val iAltitude by remember { derivedStateOf { round(altitude).toInt() } }
    val direction by remember { derivedStateOf { getDirection(iAzimuth) } }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Compass(
                direction,
                invertedFinalAzimuth,
                iAzimuth,
            )
            Text(
                text = "$iAltitude ft ASL",
                fontSize = 12.em,
                modifier = Modifier.padding(32.dp),
            )
            Button(
                onClick = { navController.navigate("preferences") },
                modifier = Modifier.padding(32.dp),
            ) {
                Text("Open Preferences")
            }
        }
    }
}

@Composable
fun Compass(
    direction: String,
    invertedFinalAzimuth: Float,
    iAzimuth: Int,
) {
    Text(
        text = direction,
        fontSize = 16.em,
    )
    Box(contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Compass Background",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(350.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.gps_arrow_2),
            contentDescription = "Compass Rose",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(200.dp).graphicsLayer(
                rotationZ = invertedFinalAzimuth
            )
        )
    }
    Text(
        text = "$iAzimuth°",
        fontSize = 12.em,
    )
}
