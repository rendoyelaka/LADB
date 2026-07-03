package com.draco.ladb.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.draco.ladb.R
import com.draco.ladb.views.PairInputActivity

object PairNotificationManager {

    const val CHANNEL_ID = "pair_channel"
    const val NOTIF_ID   = 99

    fun show(context: Context) {
        createChannel(context)

        val pairIntent = PendingIntent.getActivity(
            context, 1,
            Intent(context, PairInputActivity::class.java).apply {
                putExtra(PairInputActivity.EXTRA_STEP, PairInputActivity.STEP_BOTH)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val skipIntent = PendingIntent.getActivity(
            context, 2,
            Intent(context, PairInputActivity::class.java).apply {
                putExtra(PairInputActivity.EXTRA_STEP, PairInputActivity.STEP_SKIP)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_adb_24)
            .setContentTitle("LADB — Pairing required")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Open Wireless Debugging → tap 'Pair device with pairing code' → pull down this notification → tap ENTER DETAILS"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .addAction(R.drawable.ic_baseline_adb_24, "ENTER DETAILS", pairIntent)
            .addAction(R.drawable.ic_baseline_adb_24, "SKIP", skipIntent)
            .build()

        nm(context).notify(NOTIF_ID, notif)
    }

    fun dismiss(context: Context) = nm(context).cancel(NOTIF_ID)

    // Keep for compatibility
    fun updatePortReceived(context: Context, port: String) {}

    private fun nm(context: Context) =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "LADB Pairing",
                NotificationManager.IMPORTANCE_HIGH
            )
            nm(context).createNotificationChannel(channel)
        }
    }
}
