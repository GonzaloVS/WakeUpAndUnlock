package com.gvs.wakeupandunlock

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog

class UnlockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("UnlockActivity", "UnlockActivity creado")

        // Encender pantalla y descartar el lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as android.app.KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // Esperar un segundo y luego abrir WhatsApp
        Handler(Looper.getMainLooper()).postDelayed({
            openWhatsApp()
        }, 1000)
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
