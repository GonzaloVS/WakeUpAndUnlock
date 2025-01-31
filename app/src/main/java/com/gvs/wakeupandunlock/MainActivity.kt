package com.gvs.wakeupandunlock

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gvs.wakeupandunlock.ui.theme.WakeUpAndUnlockTheme

class MainActivity : ComponentActivity() {
    private lateinit var screenManager: ScreenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("device_pin", "1234") // 🔥 Reemplazar con el PIN real del usuario
        editor.apply()


        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Activar permiso de superposición para mejor funcionamiento", Toast.LENGTH_LONG).show()
        }

        screenManager = ScreenManager(this)

        setContent {
            WakeUpAndUnlockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }

        // 🔥 **AHORA SE INICIA EL SERVICIO AL ABRIR LA APP**
        startForegroundService()
    }

    private fun startForegroundService() {
        try {
            val serviceIntent = Intent(this, ScreenUnlockService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent) // ✅ Para Android 8+ (API 26+)
            } else {
                startService(serviceIntent) // ✅ Para versiones anteriores
            }
            Log.d("MainActivity", "Foreground Service iniciado correctamente")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al iniciar el Foreground Service", e)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WakeUpAndUnlockTheme {
        Greeting("Android")
    }
}
