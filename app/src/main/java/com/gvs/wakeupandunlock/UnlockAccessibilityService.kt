package com.gvs.wakeupandunlock

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class UnlockAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No se necesita manejar eventos específicos
    }

    override fun onInterrupt() {
        // No se necesita implementar
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        performSwipeUpGesture()
    }

    // Simula un gesto de deslizamiento hacia arriba
    private fun performSwipeUpGesture() {
        val path = Path()
        path.moveTo(500f, 1800f) // Punto de inicio del gesto (centro de la pantalla)
        path.lineTo(500f, 300f) // Punto final del gesto (deslizar hacia arriba)

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 500))
            .build()

        dispatchGesture(gesture, null, null)
    }
}
