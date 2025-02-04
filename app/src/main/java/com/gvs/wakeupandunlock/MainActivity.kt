package com.gvs.wakeupandunlock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mostrar la app sobre la pantalla de bloqueo y encender la pantalla
        //setTurnScreenOn(true)
        //setShowWhenLocked(true)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        // (Opcional) Forzar redibujado en algunos dispositivos
        window?.decorView?.post {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

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

        // Verificar y activar servicio de accesibilidad
        startAccessibilityUnlockProcess()
    }

    private fun startAccessibilityUnlockProcess() {
        // Intentar iniciar el servicio de accesibilidad
        val intent = Intent(this, MyAccessibilityService::class.java)
        startService(intent)

        // Verificar si el servicio está activado
        if (!isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
            Log.e("MainActivity", "El servicio de accesibilidad no está habilitado. Pidiendo activación.")

            // Abrir la configuración de accesibilidad para que el usuario lo active manualmente
            val settingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(settingsIntent)
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals("${context.packageName}/${service.name}", ignoreCase = true)) {
                return true
            }
        }
        return false
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
