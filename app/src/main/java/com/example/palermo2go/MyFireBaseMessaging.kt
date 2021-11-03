package com.example.palermo2go

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class MyFireBaseMessaging: FirebaseMessagingService() {

    private var broadcaster: LocalBroadcastManager? = null


    override fun onCreate() {

        broadcaster = LocalBroadcastManager.getInstance(this);

        super.onCreate()
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.e("NOTIFICA", Gson().toJson(p0.data))
        val intent = Intent("MyData")
        intent.putExtra("data", p0.data["corsa"] as String)
        broadcaster!!.sendBroadcast(intent)
        super.onMessageReceived(p0)
    }


}