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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf() // 🔥 Evita ejecutar el servicio si no tiene intent válido
            return START_NOT_STICKY
        }

        startTestSequence() // ✅ **Ahora se llama correctamente cuando el servicio inicia**
        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "WakeUpServiceChannel"
        val channelName = "Wake Up Service"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

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
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }

    private fun startTestSequence() {
        screenManager.wakeUpScreen() // 🔥 **1️⃣ Desbloquear la pantalla completamente**

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("ScreenUnlockService", "Pantalla desbloqueada. Abriendo WhatsApp...")
            openWhatsApp() // 🔥 **2️⃣ Esperar antes de abrir WhatsApp**
        }, 3000) // ✅ **Asegurar que la pantalla está completamente desbloqueada antes de abrir WhatsApp**

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("ScreenUnlockService", "Regresando a la app principal...")
            returnToApp()
        }, 8000)

        Handler(Looper.getMainLooper()).postDelayed({
            screenManager.lockScreen()
            stopSelf()
        }, 10000)
    }


    private fun openWhatsApp() {

        try {
            val whatsappPackage = "com.whatsapp"

//        val intent = Intent(Intent.ACTION_VIEW).apply {
//            data = Uri.parse("whatsapp://send?phone=+1234567890&text=" + Uri.encode("Hola, esto es una prueba."))
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) // 🔥 Evita animaciones que puedan minimizar WhatsApp
//            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) // 🔥 Trae WhatsApp al frente si ya está abierto

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("whatsapp://send?phone=+34638397366&text=Hola")
            ).apply {
                setPackage(whatsappPackage)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
            }
//            setPackage(whatsappPackage)
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) // 🔥 Evita animaciones que puedan minimizar WhatsApp
//            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) // 🔥 Trae WhatsApp al frente si ya está abierto
//            addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
//        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT) // 🔥 En algunos dispositivos ayuda a mantener WhatsApp en pantalla
//            }
//        }

            //if (intent.resolveActivity(packageManager) != null)
            startActivity(intent)
            //else
            //    stopSelf() // 🔥 Si no se puede abrir WhatsApp, terminamos el servicio

        } catch (ex: Exception) {
            Log.e("WhatsAppSender", "Error abriendo WhatsApp", ex)
        }
    }

    private fun returnToApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
