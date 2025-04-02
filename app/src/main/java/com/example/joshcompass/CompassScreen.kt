package com.example.joshcompass

import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import kotlin.math.floor


@Composable
fun CompassScreen(navController: NavHostController, sharedPreferences: SharedPreferences, outerOnAzimuthChange: ((Float) -> Unit) -> Unit, modifier: Modifier = Modifier) {
    var azimuth by remember { mutableFloatStateOf(0f) }
    // Note the inclusion of "-" to flip the sign for setting the direction of the offset properly.
    val offset by remember { mutableIntStateOf(-sharedPreferences.getInt("offset", 0)) }

    LaunchedEffect(Unit) {
        outerOnAzimuthChange { newAzimuth ->
            azimuth = newAzimuth
        }
    }

    Compass(navController = navController, azimuth = azimuth, offset = offset, modifier = modifier)
}

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
fun Compass(navController: NavHostController, azimuth: Float, offset: Int, modifier: Modifier = Modifier) {
    val finalAzimuth = getFinalAzimuth(azimuth, offset)
    val iAzimuth: Int = floor(finalAzimuth).toInt()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text(
                text = getDirection(iAzimuth),
                fontSize = 16.em,
                modifier = modifier.align(Alignment.CenterHorizontally)
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
                        rotationZ = 360f - finalAzimuth
                    )
                )
            }
            Text(
                text = "$iAzimuth°",
                fontSize = 12.em,
                modifier = modifier.align(Alignment.CenterHorizontally)
            )
            Button(
                onClick = { navController.navigate("preferences") },
                modifier = modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Open Preferences")
            }
        }
    }
}
