package com.gvs.wakeupandunlock

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
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

    private lateinit var keyguardManager: KeyguardManager
    private var keyguardLock: KeyguardManager.KeyguardLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wakeUpScreen()
        // Para versiones antiguas, se usan flags en la ventana
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        disableKeyguard()

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

        // Verificar y solicitar servicio de accesibilidad
        //startAccessibilityUnlockProcess()
    }

//    override fun onResume() {
//        super.onResume()
//        Log.d("MainActivity", "Volviendo a la app desde ajustes de accesibilidad")
//        //startAccessibilityUnlockProcess() // Reintentar si el usuario ya activó el servicio
//    }

    private fun wakeUpScreen() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "WakeUpAndUnlock:WakeLock"
        )
        wakeLock.acquire(3000) // Mantiene la pantalla encendida 3 segundos
        wakeLock.release()
    }

    private fun disableKeyguard() {
        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguardManager.isKeyguardLocked) {
            keyguardLock = keyguardManager.newKeyguardLock("WakeUpAndUnlock:KeyguardLock")
            keyguardLock?.disableKeyguard()
        }
    }

    private fun startAccessibilityUnlockProcess() {
        if (!isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
            Log.e("MainActivity", "El servicio de accesibilidad no está habilitado. Pidiendo activación.")

            // Abrir la configuración de accesibilidad
            val settingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(settingsIntent)

            // Reintentar en 3 segundos
            Handler(Looper.getMainLooper()).postDelayed({ startAccessibilityUnlockProcess() }, 3000)
        } else {
            Log.d("MainActivity", "El servicio de accesibilidad ya está activado. Iniciando UnlockActivity...")
            val unlockIntent = Intent(this, UnlockActivity::class.java)
            unlockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(unlockIntent)
        }
    }


    private fun isAccessibilityServiceEnabled(
        context: Context,
        service: Class<out AccessibilityService>
    ): Boolean {
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
