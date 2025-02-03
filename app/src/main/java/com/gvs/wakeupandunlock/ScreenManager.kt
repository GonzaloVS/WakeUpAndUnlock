package com.gvs.wakeupandunlock

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import android.util.Log

class ScreenManager(private val context: Context) {

    private var wakeLock: PowerManager.WakeLock? = null
    private var keyguardLock: KeyguardManager.KeyguardLock? = null

    fun wakeUpAndUnlockScreen() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        // 🔥 1️⃣ Encender la pantalla si está apagada
        if (!powerManager.isInteractive) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "WakeUpAndUnlock:WakeLock"
            )
            wakeLock?.acquire(10 * 1000L) // Mantener pantalla encendida por 10 segundos
        }

        // 🔥 2️⃣ Desbloquear pantalla si está bloqueada
        if (keyguardManager.isKeyguardLocked) {
            keyguardLock = keyguardManager.newKeyguardLock("WakeUpAndUnlock:KeyguardLock")
            keyguardLock?.disableKeyguard()
            Log.d("ScreenManager", "Pantalla desbloqueada con éxito")
        }
    }
}
