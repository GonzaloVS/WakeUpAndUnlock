package com.gvs.wakeupandunlock

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import androidx.core.app.NotificationCompat

class ScreenUnlockService : Service() {

    private lateinit var screenManager: ScreenManager

    override fun onCreate() {
        super.onCreate()
        screenManager = ScreenManager(this)
        startForegroundService()
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
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
