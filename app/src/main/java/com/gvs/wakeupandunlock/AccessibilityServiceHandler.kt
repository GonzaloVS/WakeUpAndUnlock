package com.gvs.wakeupandunlock

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    private var isUnlocking = false // 🔥 Evita loops infinitos

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
        if (event?.packageName == "com.android.systemui") { // 📌 **Pantalla de bloqueo detectada**
            if (!isUnlocking) {
                isUnlocking = true
                Log.d("AccessibilityService", "Pantalla de bloqueo detectada, intentando desbloquear...")
                desbloquearPantalla()
            }
        }
    }

    private fun desbloquearPantalla() {
        val rootNode = rootInActiveWindow ?: return

        val pin = obtenerPinUsuario() // 📌 **Recuperamos el PIN guardado en la app**
        if (pin.isEmpty()) {
            Log.e("AccessibilityService", "No hay PIN configurado en la app")
            return
        }

        // 🔥 **Introduce el PIN número por número**
        for (digit in pin) {
            val button = buscarNodoPorTexto(rootNode, digit.toString())
            button?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        // 📌 **Pulsa "OK" o "Enter" para confirmar**
        val enterButton = buscarNodoPorTexto(rootNode, "OK") ?: buscarNodoPorTexto(rootNode, "Enter")
        enterButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)

        // 🔥 **Espera un momento y vuelve a lanzar WhatsApp**
        Handler(Looper.getMainLooper()).postDelayed({
            abrirWhatsApp()
            isUnlocking = false
        }, 2000) // ✅ Espera 2 segundos tras desbloquear
    }

    private fun buscarNodoPorTexto(rootNode: AccessibilityNodeInfo, texto: String): AccessibilityNodeInfo? {
        val nodes = rootNode.findAccessibilityNodeInfosByText(texto)
        return nodes.firstOrNull()
    }

    private fun obtenerPinUsuario(): String {
        val prefs: SharedPreferences = getSharedPreferences("config", MODE_PRIVATE)
        return prefs.getString("device_pin", "") ?: ""
    }

    private fun abrirWhatsApp() {
        Log.d("AccessibilityService", "Reintentando abrir WhatsApp...")

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("whatsapp://send?phone=+34638397366&text=" + Uri.encode("Hola, esto es una prueba."))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        Log.w("AccessibilityService", "Interrumpido")
    }
}
