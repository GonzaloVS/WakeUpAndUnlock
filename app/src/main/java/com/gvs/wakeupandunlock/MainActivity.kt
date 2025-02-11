package com.gvs.wakeupandunlock

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.WindowManager
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mostrar "Hola Mundo" en pantalla
        val textView = TextView(this).apply {
            text = "Hola Mundo"
            textSize = 24f
        }
        setContentView(textView)

        // 🔹 Configurar la ventana para ignorar el bloqueo de pantalla si es de tipo "deslizar"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // 🔹 Intentar desbloquear
        wakeUpAndUnlockScreen()

        // 🔹 Abrir WhatsApp en pantalla dividida
        launchWhatsAppSplitScreen()
    }

    // 🔹 Método para encender la pantalla y desbloquear
    private fun wakeUpAndUnlockScreen() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "MyApp::WakeLock"
        )
        wakeLock.acquire(3000)
        wakeLock.release()

        // 🔹 Si hay un bloqueo fuerte (PIN, patrón, huella), pedir al usuario que lo desbloquee
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguardManager.isKeyguardLocked) {
            val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                "Desbloqueo requerido",
                "Para continuar, desbloquea tu dispositivo."
            )
            if (intent != null) {
                startActivity(intent)
            }
        }
    }

    // 🔹 Método para abrir WhatsApp en pantalla dividida
    private fun launchWhatsAppSplitScreen() {
        val phoneNumber = "+34638397366"
        val message = "test"

        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("whatsapp://send?phone=$phoneNumber&text=${Uri.encode(message)}")
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Se remueve FLAG_ACTIVITY_MULTIPLE_TASK para evitar instancias repetidas
        }

        val thisAppIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
        }

        try {
            startActivity(whatsappIntent)
            Handler(Looper.getMainLooper()).postDelayed({
                // ⚠ Verificar que la app NO esté ya en foreground antes de lanzarla
                if (!isAppInForeground(this)) {
                    startActivity(thisAppIntent)
                }
            }, 1000) // Esperar 1 segundo en lugar de 500ms para evitar conflictos
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 🔹 Método para verificar si la app ya está en primer plano
    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val runningProcesses = activityManager.runningAppProcesses
        if (runningProcesses != null) {
            for (processInfo in runningProcesses) {
                if (processInfo.processName == context.packageName &&
                    processInfo.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                ) {
                    return true
                }
            }
        }
        return false
    }
}
