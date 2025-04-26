package com.example.joshcompass

import android.content.SharedPreferences
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
import androidx.core.content.edit


@Composable
fun PreferencesScreen(navController: NavHostController, sharedPreferences: SharedPreferences, onPressureASLChange: (Float) -> Unit) {
    var sliderValue by remember { mutableFloatStateOf(sharedPreferences.getInt("offset", 0).toFloat()) }
    var textValue by remember { mutableStateOf(sliderValue.toString()) }
    val valueRangeMin = remember { -90f }
    val valueRangeMax = remember { 90f }

    var pressureTextValue by remember { mutableStateOf(Utils.getPressureASL(sharedPreferences)) }

    Preferences(
        sliderValue,
        onSliderValueChange = { sliderValue = it },
        textValue,
        onTextValueChange = { textValue = it },
        valueRangeMin,
        valueRangeMax,
        pressureTextValue,
        onPressureTextValueChange = { pressureTextValue = it },
        onPressureASLChange,
        navController,
        sharedPreferences
    )
}

@Composable
fun Preferences(
    sliderValue: Float,
    onSliderValueChange: (Float) -> Unit,
    textValue: String,
    onTextValueChange: (String) -> Unit,
    valueRangeMin: Float,
    valueRangeMax: Float,
    pressureTextValue: String,
    onPressureTextValueChange: (String) -> Unit,
    onPressureASLChange: (Float) -> Unit,
    navController: NavHostController,
    sharedPreferences: SharedPreferences
) {
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
                            onTextValueChange.invoke(floatValue.toString())
                            onSliderValueChange.invoke(floatValue)
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
                    onSliderValueChange.invoke(newValue)
                    onTextValueChange.invoke(newValue.toInt().toString())
                },
                valueRange = valueRangeMin..valueRangeMax,
                steps = 0
            )

            Text(text = "Selected Value: ${sliderValue.toInt()}")

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = pressureTextValue,
                onValueChange = { newValue ->
                    onPressureTextValueChange.invoke(newValue)
                },
                label = { Text("Enter pressure at sea level (in inHg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    sharedPreferences.edit {
                        putInt("offset", sliderValue.toInt())
                        putString("pressureASL", pressureTextValue)
                        onPressureASLChange.invoke(pressureTextValue.toFloat() * 33.863888f) // Convert from inHg to hPa
                    }
                    navController.navigate("main")
                },
            ) {
                Text("Save & Close")
            }
        }
    }
}
