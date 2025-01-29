package com.gvs.wakeupandunlock

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
        Handler(Looper.getMainLooper()).postDelayed({
            openWhatsApp()
        }, 2000)

        Handler(Looper.getMainLooper()).postDelayed({
            returnToApp()
        }, 7000)

        Handler(Looper.getMainLooper()).postDelayed({
            screenManager.lockScreen()
        }, 9000)
    }

    private fun openWhatsApp() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("whatsapp://send?phone=+1234567890&text=" + Uri.encode("Hola, esto es una prueba."))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun returnToApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun startForegroundService() {
        val serviceIntent = Intent(this, ScreenUnlockService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
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
