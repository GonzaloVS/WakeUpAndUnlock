package com.gvs.wakeupandunlock

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    private var isUnlocking = false // Evita loops infinitos

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AccessibilityService", "Servicio de accesibilidad iniciado")

        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName == "com.android.systemui" || event?.packageName == "com.android.keyguard") {
            // **Pantalla de bloqueo detectada**
            if (!isUnlocking) {
                isUnlocking = true
                Log.d("AccessibilityService", "Pantalla de bloqueo detectada, intentando desbloquear...")
                desbloquearPantalla()
            }
        }
    }

    private fun desbloquearPantalla() {
        val rootNode = rootInActiveWindow ?: return

        // **Paso 1: Verificar si hay un slider para desbloquear**
        val deslizarNodo = buscarNodoPorTexto(rootNode, "Deslizar para desbloquear")
        if (deslizarNodo != null) {
            deslizarNodo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("AccessibilityService", "Deslizando para desbloquear...")
            Handler(Looper.getMainLooper()).postDelayed({ desbloquearPantalla() }, 1000) // Esperar y reintentar
            return
        }

        // **Paso 2: Ingresar el PIN**
        val pin = "1234" // 📌 **Configura el PIN**
        for (digit in pin) {
            val button = buscarNodoPorTexto(rootNode, digit.toString())
            if (button != null) {
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d("AccessibilityService", "Ingresando número: $digit")
                Thread.sleep(500) // Espera breve entre cada número
            }
        }

        // **Paso 3: Confirmar con "OK" o "Enter"**
        val enterButton = buscarNodoPorTexto(rootNode, "OK") ?: buscarNodoPorTexto(rootNode, "Enter")
        if (enterButton != null) {
            enterButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("AccessibilityService", "PIN ingresado, desbloqueando...")
        }

        // **Paso 4: Abrir WhatsApp**
        Handler(Looper.getMainLooper()).postDelayed({
            abrirWhatsApp()
        }, 2000) // Espera 2 segundos tras desbloquear
    }

    private fun buscarNodoPorTexto(rootNode: AccessibilityNodeInfo, texto: String): AccessibilityNodeInfo? {
        val nodes = rootNode.findAccessibilityNodeInfosByText(texto)
        return nodes.firstOrNull()
    }

    private fun abrirWhatsApp() {
        Log.d("AccessibilityService", "Reintentando abrir WhatsApp...")

        val intent = packageManager.getLaunchIntentForPackage("com.whatsapp")
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Log.d("AccessibilityService", "WhatsApp abierto exitosamente")
        } else {
            Log.e("AccessibilityService", "WhatsApp no está instalado")
        }
    }

    override fun onInterrupt() {
        Log.w("AccessibilityService", "Interrumpido")
    }
}
