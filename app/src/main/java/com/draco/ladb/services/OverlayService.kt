package com.draco.ladb.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.draco.ladb.R
import com.draco.ladb.databinding.OverlayPairBinding
import com.draco.ladb.views.PairActivity

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: android.view.View? = null

    companion object {
        const val ACTION_START = "START_OVERLAY"
        const val ACTION_STOP  = "STOP_OVERLAY"
        const val CHANNEL_ID   = "overlay_channel"
        const val NOTIF_ID     = 42

        // Broadcast action to send result back to PairActivity
        const val ACTION_PAIR_RESULT  = "com.draco.ladb.PAIR_RESULT"
        const val EXTRA_PORT          = "port"
        const val EXTRA_CODE          = "code"
        const val EXTRA_SKIPPED       = "skipped"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> showOverlay()
            ACTION_STOP  -> { removeOverlay(); stopSelf() }
        }
        return START_NOT_STICKY
    }

    private fun showOverlay() {
        if (overlayView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        val overlayBinding = OverlayPairBinding.inflate(inflater)
        overlayView = overlayBinding.root

        val dm = resources.displayMetrics
        val overlayHeight = (dm.heightPixels * 0.46).toInt()

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayHeight,
            type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.START
        }

        windowManager?.addView(overlayView, params)

        overlayBinding.btnPair.setOnClickListener {
            val port = overlayBinding.port.text.toString().trim()
            val code = overlayBinding.code.text.toString().trim()
            if (port.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "Enter both Port and Pairing code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Send result back via broadcast
            val result = Intent(ACTION_PAIR_RESULT).apply {
                putExtra(EXTRA_PORT, port)
                putExtra(EXTRA_CODE, code)
                setPackage(packageName)
            }
            sendBroadcast(result)
            removeOverlay()
            stopSelf()
        }

        overlayBinding.btnSkip.setOnClickListener {
            val result = Intent(ACTION_PAIR_RESULT).apply {
                putExtra(EXTRA_SKIPPED, true)
                setPackage(packageName)
            }
            sendBroadcast(result)
            removeOverlay()
            stopSelf()
        }

        overlayBinding.btnHelp.setOnClickListener {
            try {
                val i = Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse(getString(R.string.tutorial_url)))
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            } catch (e: Exception) { }
        }

        overlayBinding.btnClose.setOnClickListener {
            removeOverlay()
            stopSelf()
            // Bring PairActivity back to front
            val i = Intent(this, PairActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(i)
        }
    }

    private fun removeOverlay() {
        try {
            overlayView?.let { windowManager?.removeView(it) }
            overlayView = null
        } catch (e: Exception) { }
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LADB Pairing")
            .setContentText("Pairing overlay is active")
            .setSmallIcon(R.drawable.ic_baseline_adb_24)
            .addAction(0, "Dismiss", stopPending)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "LADB Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}
