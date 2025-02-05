package com.gvs.wakeupandunlock

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat

class ScreenUnlockService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d("ScreenUnlockService", "Servicio iniciado")
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Aquí puedes ejecutar la lógica para desbloquear la pantalla
        return START_STICKY
    }

    private fun startForegroundService() {
        val notificationChannelId = "unlock_service_channel"

        // Crear el canal de notificación si es Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Desbloqueo de pantalla",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Desbloqueo de pantalla activo")
            .setContentText("La app está funcionando en segundo plano")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
