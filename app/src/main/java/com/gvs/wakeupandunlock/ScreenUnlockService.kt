package com.gvs.wakeupandunlock

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat

class ScreenUnlockService : Service() {

    private lateinit var screenManager: ScreenManager

    override fun onCreate() {
        super.onCreate()
        screenManager = ScreenManager(this)
        startForegroundServiceWithNotification()
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "WakeUpServiceChannel"
        val channelName = "Wake Up Service"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        // 🔥 Intent para forzar que la actividad aparezca sobre la pantalla de bloqueo
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Wake Up Service")
            .setContentText("Desbloqueando pantalla y enviando mensaje...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true) // 🔥 Esto forzará la visibilidad de la app
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }

        Log.d("ScreenUnlockService", "Foreground Service iniciado correctamente con FullScreenIntent")
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTestSequence()
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "WakeUpServiceChannel"
        val channelName = "Wake Up Service"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Wake Up Service")
            .setContentText("Desbloqueando pantalla y enviando mensaje...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun startTestSequence() {
        screenManager.wakeUpScreen()

        Handler(Looper.getMainLooper()).postDelayed({
            openWhatsApp()
        }, 2000)

        Handler(Looper.getMainLooper()).postDelayed({
            returnToApp()
        }, 7000)

        Handler(Looper.getMainLooper()).postDelayed({
            screenManager.lockScreen()
            stopSelf()
        }, 9000)
    }

    private fun openWhatsApp() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("whatsapp://send?phone=+1234567890&text=" + Uri.encode("Hola, esto es una prueba."))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun returnToApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) // 🔥 Asegura que la app esté en primer plano
        }

        startActivity(intent)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
