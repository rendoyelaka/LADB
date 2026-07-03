package com.draco.ladb.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.draco.ladb.R
import com.draco.ladb.receivers.PairNotificationReceiver

object PairNotificationManager {

    const val CHANNEL_ID   = "pair_channel"
    const val NOTIF_ID     = 99
    const val KEY_PORT     = "key_port"
    const val KEY_CODE     = "key_code"
    const val ACTION_PORT  = "com.draco.ladb.ACTION_PORT"
    const val ACTION_CODE  = "com.draco.ladb.ACTION_CODE"
    const val ACTION_SKIP  = "com.draco.ladb.ACTION_SKIP"

    fun show(context: Context) {
        createChannel(context)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Port input action
        val portInput = RemoteInput.Builder(KEY_PORT)
            .setLabel("Enter Port number")
            .build()
        val portIntent = PendingIntent.getBroadcast(
            context, 1,
            Intent(ACTION_PORT).setPackage(context.packageName),
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val portAction = NotificationCompat.Action.Builder(
            R.drawable.ic_baseline_adb_24, "Enter Port", portIntent)
            .addRemoteInput(portInput)
            .build()

        // Code input action
        val codeInput = RemoteInput.Builder(KEY_CODE)
            .setLabel("Enter Pairing code")
            .build()
        val codeIntent = PendingIntent.getBroadcast(
            context, 2,
            Intent(ACTION_CODE).setPackage(context.packageName),
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val codeAction = NotificationCompat.Action.Builder(
            R.drawable.ic_baseline_adb_24, "Enter Code", codeIntent)
            .addRemoteInput(codeInput)
            .build()

        // Skip action
        val skipIntent = PendingIntent.getBroadcast(
            context, 3,
            Intent(ACTION_SKIP).setPackage(context.packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val skipAction = NotificationCompat.Action.Builder(
            R.drawable.ic_baseline_adb_24, "Skip", skipIntent)
            .build()

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_adb_24)
            .setContentTitle("LADB — Pairing required")
            .setContentText("Step 1: Tap 'Enter Port' → Step 2: Tap 'Enter Code'")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Open Wireless Debugging. Then:\n1️⃣ Tap 'Enter Port' and type the port number\n2️⃣ Tap 'Enter Code' and type the 6-digit pairing code"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(portAction)
            .addAction(codeAction)
            .addAction(skipAction)
            .build()

        nm.notify(NOTIF_ID, notif)
    }

    fun updatePortReceived(context: Context, port: String) {
        createChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val codeInput = RemoteInput.Builder(KEY_CODE)
            .setLabel("Enter Pairing code")
            .build()
        val codeIntent = PendingIntent.getBroadcast(
            context, 2,
            Intent(ACTION_CODE).setPackage(context.packageName),
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val codeAction = NotificationCompat.Action.Builder(
            R.drawable.ic_baseline_adb_24, "Enter Code", codeIntent)
            .addRemoteInput(codeInput)
            .build()

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_adb_24)
            .setContentTitle("LADB — Port ✅ received: $port")
            .setContentText("Now tap 'Enter Code' and type the 6-digit pairing code")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(codeAction)
            .build()

        nm.notify(NOTIF_ID, notif)
    }

    fun dismiss(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIF_ID)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "LADB Pairing",
                NotificationManager.IMPORTANCE_HIGH
            )
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }
}
