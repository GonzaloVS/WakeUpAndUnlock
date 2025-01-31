package com.gvs.wakeupandunlock

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

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
        if (event?.packageName == "com.android.systemui") {
            Log.d("AccessibilityService", "Pantalla de bloqueo detectada, esperando para desbloquear...")

            Handler(Looper.getMainLooper()).postDelayed({
                desbloquearPantalla()
            }, 3000) // 🔥 **Esperar antes de desbloquear**
        }
    }


    private fun desbloquearPantalla() {
        val rootNode = rootInActiveWindow ?: return

        val pin = obtenerPinUsuario() // Obtener el PIN guardado en SharedPreferences
        if (pin.isEmpty()) {
            Log.e("AccessibilityService", "No hay PIN configurado en la app")
            return
        }

        // Buscar los botones numéricos y pulsarlos
        for (digit in pin) {
            val button = buscarNodoPorTexto(rootNode, digit.toString())
            button?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        // Buscar el botón "OK" o "Enter" y pulsarlo
        val enterButton = buscarNodoPorTexto(rootNode, "OK") ?: buscarNodoPorTexto(rootNode, "Enter")
        enterButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun buscarNodoPorTexto(rootNode: AccessibilityNodeInfo, texto: String): AccessibilityNodeInfo? {
        val nodes = rootNode.findAccessibilityNodeInfosByText(texto)
        return nodes.firstOrNull()
    }

    private fun obtenerPinUsuario(): String {
        val prefs: SharedPreferences = getSharedPreferences("config", MODE_PRIVATE)
        return prefs.getString("device_pin", "") ?: ""
    }

    override fun onInterrupt() {
        Log.w("AccessibilityService", "Interrumpido")
    }
}
