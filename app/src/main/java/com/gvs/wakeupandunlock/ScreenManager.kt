package com.gvs.wakeupandunlock

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager

class ScreenManager(private val context: Context) {

    private var wakeLock: PowerManager.WakeLock? = null
    //private var keyguardLock: KeyguardManager.KeyguardLock? = null

    fun wakeUpAndUnlockScreen() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!powerManager.isInteractive) {
            wakeLock = powerManager.newWakeLock(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                 //or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "WakeUpAndUnlock:WakeLock"
            )
            wakeLock?.acquire(20 * 1000L) // Mantener pantalla encendida por 20 segundos
        }

        if (keyguardManager.isKeyguardLocked) {
            //keyguardLock = keyguardManager.newKeyguardLock("WakeUpAndUnlock:KeyguardLock")
            //keyguardLock?.disableKeyguard()
            Log.d("ScreenManager", "Pantalla desbloqueada con éxito")
        }
    }
}
