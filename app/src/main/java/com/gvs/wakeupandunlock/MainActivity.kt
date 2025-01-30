package com.gvs.wakeupandunlock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.startForeground
import androidx.lifecycle.lifecycleScope
import com.gvs.wakeupandunlock.ui.theme.WakeUpAndUnlockTheme
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    private lateinit var screenManager: ScreenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Activar permiso de superposición para mejor funcionamiento", Toast.LENGTH_LONG).show()
        }

        // 🔥 Permitir que la Activity se muestre en pantalla bloqueada
        ScreenManager.allowUnlockOnActivity(this)

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

        // 🔥 Iniciar servicio en segundo plano
        startForegroundService()

        // 🔥 Iniciar la secuencia de prueba
        startTestSequence()
    }

    override fun onDestroy() {
        super.onDestroy()
        screenManager.releaseWakeLock()
    }

    private fun startTestSequence() {
        lifecycleScope.launch {
            Log.d("MainActivity", "Esperando 2 segundos antes de abrir WhatsApp...")
            delay(2000)
            openWhatsApp()

            Log.d("MainActivity", "Esperando 5 segundos antes de volver a la app...")
            delay(5000)

            Log.d("MainActivity", "Esperando 300ms extra antes de volver a la app...")
            delay(300) // 🔥 Pequeño delay para asegurar que WhatsApp no interfiere
            returnToApp()

            Log.d("MainActivity", "Esperando 2 segundos antes de bloquear pantalla...")
            delay(2000)
            screenManager.lockScreen()

            Log.d("MainActivity", "Secuencia finalizada, deteniendo el servicio...")
            stopForegroundService() // ✅ Ahora se detiene correctamente
        }
    }


    private fun openWhatsApp() {
        Log.d("MainActivity", "Intentando abrir WhatsApp...")

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("whatsapp://send?phone=+1234567890&text=" + Uri.encode("Hola, esto es una prueba."))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY)
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
                Log.d("MainActivity", "WhatsApp abierto con éxito")
            } else {
                Log.e("MainActivity", "WhatsApp no está instalado o no se puede abrir")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al abrir WhatsApp", e)
        }
    }


    private fun returnToApp() {
        Log.d("MainActivity", "Intentando volver a la app...")

        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }

            startActivity(intent)
            Log.d("MainActivity", "MainActivity abierta correctamente")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al volver a la app", e)
        }
    }


//    private fun startForegroundService() {
//        try {
//            val serviceIntent = Intent(this, ScreenUnlockService::class.java)
//            startForegroundService(serviceIntent)
//            Log.d("MainActivity", "Foreground Service iniciado correctamente")
//        } catch (e: Exception) {
//            Log.e("MainActivity", "Error al iniciar el Foreground Service", e)
//        }
//    }

    private fun startForegroundService() {
        try {
            val serviceIntent = Intent(this, ScreenUnlockService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent) // ✅ Iniciar el servicio correctamente
            } else {
                startService(serviceIntent)
            }
            Log.d("MainActivity", "Foreground Service iniciado correctamente")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al iniciar el Foreground Service", e)
        }
    }



    private fun stopForegroundService() {
        try {
            val serviceIntent = Intent(this, ScreenUnlockService::class.java)
            stopService(serviceIntent)
            Log.d("MainActivity", "Foreground Service detenido correctamente")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al detener el Foreground Service", e)
        }
    }
}




@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WakeUpAndUnlockTheme {
        Greeting("Android")
    }
}
