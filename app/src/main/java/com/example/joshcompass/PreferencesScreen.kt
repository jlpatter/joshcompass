import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@Composable
fun PreferencesScreen(navController: NavHostController) {

    var sliderValue by remember { mutableFloatStateOf(0f) }
    var textValue by remember { mutableStateOf(sliderValue.toString()) }

    val valueRangeMin = -90f
    val valueRangeMax = 90f

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Offset (in +/- degrees):")
            OutlinedTextField(
                value = textValue,
                onValueChange = { newValue ->
                    newValue.toFloatOrNull()?.let { floatValue ->
                        if (floatValue in valueRangeMin..valueRangeMax) {
                            textValue = floatValue.toString()
                            sliderValue = floatValue
                        }
                    }
                },
                label = { Text("Enter a number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue
                    textValue = newValue.toInt().toString()
                },
                valueRange = valueRangeMin..valueRangeMax,
                steps = 0
            )

            Text(text = "Selected Value: ${sliderValue.toInt()}")

            Button(
                onClick = { navController.navigate("main") },
            ) {
                Text("Close")
            }
        }
    }
}
