package com.gvs.wakeupandunlock

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log

class ScreenUnlockService : Service() {

    private lateinit var screenManager: ScreenManager
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        screenManager = ScreenManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startProcessLoop()
        return START_STICKY
    }

    private fun startProcessLoop() {
        handler.postDelayed({
            Log.d("ScreenUnlockService", "Ejecutando ciclo de cambio de apps")

            // 🔥 1️⃣ Desbloquear la pantalla
            screenManager.wakeUpAndUnlockScreen()

            // 🔥 2️⃣ Traer MainActivity al frente
            bringMainActivityToFront()

            // 🔥 3️⃣ Esperar 2 segundos y abrir UnlockActivity
            handler.postDelayed({
                openUnlockActivity()
            }, 2000)

            // 🔥 4️⃣ Esperar 3 segundos después de WhatsApp y volver a nuestra app
            handler.postDelayed({
                returnToMainActivity()
            }, 8000)

            // 🔥 5️⃣ Repetir el ciclo cada 10 segundos
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
