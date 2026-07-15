package com.homehub.app.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class HomeHubMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        // TODO: send token to backend, e.g. POST /devices/register-token
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // TODO: handle incoming push (device alerts, rule triggers, etc.)
    }
}