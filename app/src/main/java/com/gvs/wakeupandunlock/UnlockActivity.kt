package com.gvs.wakeupandunlock

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity

class UnlockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("UnlockActivity", "UnlockActivity creado")

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        // Esperar un segundo y luego abrir WhatsApp
        Handler(Looper.getMainLooper()).postDelayed({
            enableSplitScreenWithWhatsApp()
        }, 1000)
    }

    private fun enableSplitScreenWithWhatsApp() {
        try {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (keyguardManager.isKeyguardLocked) {
                Log.d("UnlockActivity", "El teléfono sigue bloqueado. Reintentando en 1 segundo...")
                Handler(Looper.getMainLooper()).postDelayed(
                    { enableSplitScreenWithWhatsApp() },
                    1000
                )
                return
            }

            val phoneNumber = "+34638397366"
            val message = "test"
            val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("whatsapp://send?phone=$phoneNumber&text=${Uri.encode(message)}")
                setPackage("com.whatsapp")
                addFlags(
                    Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
            }

            val thisAppIntent = Intent(this, MainActivity::class.java)

            // Intent para abrir los Ajustes de Accesibilidad en una tarea flotante/múltiple
            val accessibilitySettingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                )
            }

            startActivity(whatsappIntent)

            // Espera 1.5 segundos y luego abre esta app en la otra mitad
            Handler(Looper.getMainLooper()).postDelayed({
                thisAppIntent.addFlags(
                    Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                )
                startActivity(thisAppIntent)

            }, 500)

        } catch (e: Exception) {
            Log.e("UnlockActivity", "Error al habilitar pantalla dividida", e)
            openWhatsApp() // Si falla, abrir WhatsApp normalmente
        }
    }

    private fun openWhatsApp(reintentos: Int = 0) {
        try {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (keyguardManager.isKeyguardLocked) {
                Log.d("UnlockActivity", "El teléfono sigue bloqueado. Reintentando en 1 segundo...")
                if (reintentos < 10) {
                    Handler(Looper.getMainLooper()).postDelayed({ openWhatsApp(reintentos + 1) }, 1000)
                } else {
                    Log.e("UnlockActivity", "Demasiados intentos. No se pudo abrir WhatsApp.")
                }
                return
            }

            val phoneNumber = "+34638397366"
            val message = "test"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("whatsapp://send?phone=$phoneNumber&text=${Uri.encode(message)}")
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }

            startActivity(intent)
            Log.d("UnlockActivity", "WhatsApp abierto con mensaje de prueba")

            finish()
        } catch (e: Exception) {
            Log.e("UnlockActivity", "Error al abrir WhatsApp", e)
        }
    }


}
