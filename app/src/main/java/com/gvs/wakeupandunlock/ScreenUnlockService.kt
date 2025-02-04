package com.gvs.wakeupandunlock

import android.app.Service
import android.content.*
import android.os.*
import android.util.Log

class ScreenUnlockService : Service() {

    private lateinit var screenManager: ScreenManager
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var screenReceiver: ScreenReceiver

    override fun onCreate() {
        super.onCreate()
        screenManager = ScreenManager(this)

        // Registrar BroadcastReceiver para detectar bloqueo de pantalla
        screenReceiver = ScreenReceiver()
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startProcessLoop()
        return START_STICKY
    }

    private fun startProcessLoop() {
        handler.postDelayed({
            Log.d("ScreenUnlockService", "Ejecutando ciclo de cambio de apps")

            // Desbloquear la pantalla y encenderla
            screenManager.wakeUpAndUnlockScreen()

            // Traer MainActivity al frente
            bringMainActivityToFront()

            // Esperar 2 segundos y abrir UnlockActivity
            handler.postDelayed({
                openUnlockActivity()
            }, 2000)

            // Esperar 3 segundos después de WhatsApp y volver a nuestra app
            handler.postDelayed({
                returnToMainActivity()
            }, 8000)

            // Repetir el ciclo cada 10 segundos
            startProcessLoop()

        }, 10000)
    }

    private fun bringMainActivityToFront() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)
    }

    private fun openUnlockActivity() {
        val intent = Intent(this, UnlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
    }

    private fun returnToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
