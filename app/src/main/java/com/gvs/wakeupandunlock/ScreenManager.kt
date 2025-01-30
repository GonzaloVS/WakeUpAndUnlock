package com.gvs.wakeupandunlock

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager

class ScreenManager(private val context: Context) {

    private var wakeLock: PowerManager.WakeLock? = null

//    fun wakeUpScreen() {
//        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
//        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//
//        // 🔥 Encender la pantalla si está apagada
//        if (!powerManager.isInteractive) {
//            wakeLock = powerManager.newWakeLock(
//                PowerManager.PARTIAL_WAKE_LOCK,
//                "WakeUpAndUnlock:WakeLock"
//            )
//            wakeLock?.acquire(10 * 1000L) // Mantener la pantalla encendida durante 10 segundos
//        }
//    }

    fun wakeUpScreen() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        // 🔥 Encender la pantalla si está apagada
        if (!powerManager.isInteractive) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "WakeUpAndUnlock:WakeLock"
            )
            wakeLock?.acquire(10 * 1000L) // Mantener la pantalla encendida durante 10 segundos
        }

        // 🔥 Desbloquear la pantalla completamente
        val keyguardLock = keyguardManager.newKeyguardLock("WakeUpAndUnlock:KeyguardLock")
        keyguardLock.disableKeyguard()
    }


    fun lockScreen() {
        releaseWakeLock() // ✅ Llamar a releaseWakeLock para apagar la pantalla
    }

    fun releaseWakeLock() { // ✅ Nueva función corregida
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    companion object {
        fun allowUnlockOnActivity(activity: Activity) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                activity.setShowWhenLocked(true)
                activity.setTurnScreenOn(true)
            }
        }
    }
}
