package com.gvs.wakeupandunlock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            Log.d("ScreenReceiver", "Pantalla bloqueada, encendiéndola nuevamente...")

            // Volver a encender la pantalla y desbloquearla
            val screenManager = ScreenManager(context)
            screenManager.wakeUpAndUnlockScreen()
        }
    }
}
