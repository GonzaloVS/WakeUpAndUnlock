package com.gvs.wakeupandunlock

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mostrar "Hola Mundo" en pantalla
        val textView = TextView(this).apply {
            text = "Hola, Mundo!"
            textSize = 24f
        }
        setContentView(textView)

        // Mantener la pantalla encendida y saltarse el bloqueo
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )

        // Registrar broadcast para detectar apagado y encendido de pantalla
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
    }

    // BroadcastReceiver para detectar apagado y encendido de pantalla
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    wakeUpScreen(context)
                }
                Intent.ACTION_SCREEN_ON -> {
                    // Al encender la pantalla, enviar una intent al servicio de accesibilidad
                    val accessibilityIntent = Intent(context, UnlockAccessibilityService::class.java)
                    context?.startService(accessibilityIntent)
                }
            }
        }
    }

    // Método para encender la pantalla
    private fun wakeUpScreen(context: Context?) {
        val powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "MyApp::WakeLock"
        )
        wakeLock.acquire(3000) // Mantiene la pantalla encendida 3 segundos
        wakeLock.release()
    }
}
