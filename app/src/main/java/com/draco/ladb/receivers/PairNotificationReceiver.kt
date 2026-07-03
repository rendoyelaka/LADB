package com.draco.ladb.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PairNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PAIR_RESULT = "com.draco.ladb.PAIR_RESULT"
        const val EXTRA_PORT         = "port"
        const val EXTRA_CODE         = "code"
        const val EXTRA_SKIPPED      = "skipped"

        // In-memory state shared with PairInputActivity
        var pendingPort: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Handled by PairInputActivity — nothing to do here
    }
}
