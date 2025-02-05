package com.gvs.wakeupandunlock

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    private var isUnlocking = false
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AccessibilityService", "Servicio de accesibilidad iniciado")

        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }

        // Intentar encender la pantalla si aún no lo está
        wakeUpAndUnlockScreen()

        if (isDeviceLocked()) {
            Log.d("AccessibilityService", "El dispositivo está bloqueado. Iniciando proceso de desbloqueo...")
        } else {
            Log.d("AccessibilityService", "El dispositivo ya está desbloqueado. Abriendo UnlockActivity directamente.")
            abrirUnlockActivity()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Se detectan eventos de la pantalla de bloqueo del sistema
        if (event?.packageName == "com.android.systemui" || event?.packageName == "com.android.keyguard") {
            if (!isUnlocking && isDeviceLocked()) {
                isUnlocking = true
                Log.d("AccessibilityService", "Pantalla de bloqueo detectada, intentando desbloquear...")
                desbloquearPantalla()
            } else {
                Log.d("AccessibilityService", "El dispositivo ya está desbloqueado o se está procesando el desbloqueo.")
            }
        }
    }

    private fun isDeviceLocked(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isKeyguardLocked
    }

    private fun wakeUpAndUnlockScreen() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isInteractive) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                "WakeUpAndUnlock:WakeLock"
            )
            wakeLock?.acquire(10 * 1000L) // Mantiene la pantalla encendida 10 segundos
        }
        // Nota: La manipulación del keyguard con newKeyguardLock está obsoleta.
        // Se recomienda gestionar el keyguard en la Activity usando requestDismissKeyguard().
    }

    private fun desbloquearPantalla(reintentos: Int = 0) {
        val rootNode = rootInActiveWindow

        if (isDeviceLocked()) {
            Log.d("AccessibilityService", "El teléfono sigue bloqueado. Reintento: $reintentos")

            if (reintentos >= 10) {
                Log.e("AccessibilityService", "Demasiados intentos de desbloqueo. Abortando.")
                return
            }

            Handler(Looper.getMainLooper()).postDelayed({ desbloquearPantalla(reintentos + 1) }, 1000)
            return
        }

        if (rootNode == null) {
            Log.d("AccessibilityService", "No se pudo obtener rootInActiveWindow. Reintentando en 1s...")
            Handler(Looper.getMainLooper()).postDelayed({ desbloquearPantalla(reintentos + 1) }, 1000)
            return
        }

        // Verificar si hay un slider para desbloquear
        val deslizarNodo = buscarNodoPorTexto(rootNode, "Deslizar para desbloquear")
        if (deslizarNodo != null) {
            deslizarNodo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("AccessibilityService", "Deslizando para desbloquear...")
            Handler(Looper.getMainLooper()).postDelayed({ desbloquearPantalla(reintentos + 1) }, 1000)
            return
        }

        // Ingresar el PIN (1,2,3,4)
        val pin = "1234"
        for (digit in pin) {
            val button = buscarNodoPorTexto(rootNode, digit.toString())
            if (button != null) {
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d("AccessibilityService", "Ingresando número: $digit")
                Thread.sleep(500)
            } else {
                Log.d("AccessibilityService", "No se encontró el botón para el número $digit. Reintentando...")
                Handler(Looper.getMainLooper()).postDelayed({ desbloquearPantalla(reintentos + 1) }, 1000)
                return
            }
        }

        // Confirmar con "OK" o "Enter"
        val enterButton = buscarNodoPorTexto(rootNode, "OK") ?: buscarNodoPorTexto(rootNode, "Enter")
        if (enterButton != null) {
            enterButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("AccessibilityService", "PIN ingresado, desbloqueando...")
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isDeviceLocked()) {
                Log.d("AccessibilityService", "Desbloqueo exitoso. Abriendo WhatsApp...")
                abrirUnlockActivity()
            } else {
                Log.d("AccessibilityService", "El teléfono sigue bloqueado. Reintentando en 1 segundo...")
                desbloquearPantalla(reintentos + 1)
            }
        }, 2000)
    }


    private fun buscarNodoPorTexto(rootNode: AccessibilityNodeInfo, texto: String): AccessibilityNodeInfo? {
        return rootNode.findAccessibilityNodeInfosByText(texto).firstOrNull()
    }

    private fun abrirUnlockActivity() {
        Log.d("AccessibilityService", "Iniciando UnlockActivity para abrir WhatsApp...")
        val intent = Intent(this, UnlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
            wakeLock = null
        }
    }

    override fun onInterrupt() {
        Log.w("AccessibilityService", "Servicio de accesibilidad interrumpido")
    }
}
