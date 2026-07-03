package com.draco.ladb.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.draco.ladb.services.PairNotificationManager

class PairNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PAIR_RESULT = "com.draco.ladb.PAIR_RESULT"
        const val EXTRA_PORT         = "port"
        const val EXTRA_CODE         = "code"
        const val EXTRA_SKIPPED      = "skipped"

        // In-memory state between port and code steps
        var pendingPort: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            PairNotificationManager.ACTION_PORT -> {
                val remoteInput = RemoteInput.getResultsFromIntent(intent)
                val port = remoteInput?.getCharSequence(PairNotificationManager.KEY_PORT)
                    ?.toString()?.trim() ?: return
                pendingPort = port
                PairNotificationManager.updatePortReceived(context, port)
            }

            PairNotificationManager.ACTION_CODE -> {
                val remoteInput = RemoteInput.getResultsFromIntent(intent)
                val code = remoteInput?.getCharSequence(PairNotificationManager.KEY_CODE)
                    ?.toString()?.trim() ?: return
                val port = pendingPort ?: return
                pendingPort = null
                PairNotificationManager.dismiss(context)

                val result = Intent(ACTION_PAIR_RESULT).apply {
                    putExtra(EXTRA_PORT, port)
                    putExtra(EXTRA_CODE, code)
                    setPackage(context.packageName)
                }
                context.sendBroadcast(result)
            }

            PairNotificationManager.ACTION_SKIP -> {
                pendingPort = null
                PairNotificationManager.dismiss(context)
                val result = Intent(ACTION_PAIR_RESULT).apply {
                    putExtra(EXTRA_SKIPPED, true)
                    setPackage(context.packageName)
                }
                context.sendBroadcast(result)
            }
        }
    }
}
