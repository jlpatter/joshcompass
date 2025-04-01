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
import com.example.joshcompass.R
import kotlin.math.floor


@Composable
fun CompassScreen(navController: NavHostController, outerOnAzimuthChange: ((Float) -> Unit) -> Unit, modifier: Modifier = Modifier) {
    var azimuth by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        outerOnAzimuthChange { newAzimuth ->
            azimuth = newAzimuth
        }
    }

    Compass(navController = navController, azimuth = azimuth, modifier = modifier)
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
fun Compass(navController: NavHostController, azimuth: Float, modifier: Modifier = Modifier) {
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
                painter = painterResource(id = R.drawable.gps_arrow_2),
                contentDescription = "Compass Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(300.dp).graphicsLayer(
                    rotationZ = 360f - azimuth
                )
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
