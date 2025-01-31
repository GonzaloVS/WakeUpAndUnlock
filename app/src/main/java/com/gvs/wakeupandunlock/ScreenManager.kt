package com.gvs.wakeupandunlock

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import android.util.Log

class ScreenManager(private val context: Context) {

    private var wakeLock: PowerManager.WakeLock? = null
    private var keyguardLock: KeyguardManager.KeyguardLock? = null

    fun wakeUpScreen() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        // 🔥 1️⃣ **Despertar pantalla**
        if (!powerManager.isInteractive) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "WakeUpAndUnlock:WakeLock"
            )
            wakeLock?.acquire(10 * 1000L)
        }

        // 🔥 2️⃣ **Desbloquear Keyguard (si no tiene PIN o Patrón)**
        if (keyguardManager.isKeyguardLocked) {
            Log.d("ScreenManager", "Desbloqueando pantalla...")
            keyguardLock = keyguardManager.newKeyguardLock("WakeUpAndUnlock:KeyguardLock")
            keyguardLock?.disableKeyguard()
        }

        // 🔥 3️⃣ **Esperar 2 segundos antes de abrir WhatsApp (para asegurar que el Keyguard se haya desactivado)**
        Thread.sleep(2000)
    }

    fun lockScreen() {
        wakeLock?.release()
    }
}
