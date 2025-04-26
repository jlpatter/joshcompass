package com.example.joshcompass

import android.content.SharedPreferences

class Utils {
    companion object {
        fun getPressureASL(sharedPreferences: SharedPreferences): String {
            val pressureASLTxt = sharedPreferences.getString("pressureASL", "29.92") ?: "29.92"

            return pressureASLTxt
        }
    }
}
