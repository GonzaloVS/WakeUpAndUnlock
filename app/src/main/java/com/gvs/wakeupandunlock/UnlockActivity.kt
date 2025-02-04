package com.gvs.wakeupandunlock

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity

class UnlockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mostrar la pantalla encima del bloqueo
        setTurnScreenOn(true)
        setShowWhenLocked(true)
//        window.addFlags(
//            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
//                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
//                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//        )

        // Esperar 1 segundo y abrir WhatsApp
        Handler(Looper.getMainLooper()).postDelayed({
            openWhatsApp()
        }, 1000)
    }

    private fun openWhatsApp() {
        try {
            val phoneNumber = "+34638397366"
            val message = "test"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("whatsapp://send?phone=$phoneNumber&text=${Uri.encode(message)}")
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }

            startActivity(intent)
            Log.d("UnlockActivity", "WhatsApp abierto con mensaje de prueba")

            finish() // Cerrar UnlockActivity después de abrir WhatsApp
        } catch (e: Exception) {
            Log.e("UnlockActivity", "Error al abrir WhatsApp", e)
        }
    }
}
