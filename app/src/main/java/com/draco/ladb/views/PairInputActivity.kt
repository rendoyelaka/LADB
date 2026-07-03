package com.draco.ladb.views

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.draco.ladb.receivers.PairNotificationReceiver
import com.draco.ladb.services.PairNotificationManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.LinearLayout
import android.content.Context

class PairInputActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STEP = "step"
        const val STEP_PORT  = "port"
        const val STEP_CODE  = "code"
        const val STEP_SKIP  = "skip"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val step = intent.getStringExtra(EXTRA_STEP) ?: STEP_PORT

        if (step == STEP_SKIP) {
            PairNotificationManager.dismiss(this)
            PairNotificationReceiver.pendingPort = null
            sendBroadcast(Intent(PairNotificationReceiver.ACTION_PAIR_RESULT).apply {
                putExtra(PairNotificationReceiver.EXTRA_SKIPPED, true)
                setPackage(packageName)
            })
            finish()
            return
        }

        val hint = if (step == STEP_PORT) "Port number" else "6-digit Pairing code"
        val title = if (step == STEP_PORT) "Enter Port" else "Enter Pairing code"

        val input = TextInputEditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val layout = TextInputLayout(this).apply {
            this.hint = hint
            addView(input)
            setPadding(48, 16, 48, 0)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setView(layout)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                val value = input.text.toString().trim()
                if (value.isEmpty()) { finish(); return@setPositiveButton }

                if (step == STEP_PORT) {
                    PairNotificationReceiver.pendingPort = value
                    PairNotificationManager.updatePortReceived(this, value)
                } else {
                    val port = PairNotificationReceiver.pendingPort ?: run { finish(); return@setPositiveButton }
                    PairNotificationReceiver.pendingPort = null
                    PairNotificationManager.dismiss(this)
                    sendBroadcast(Intent(PairNotificationReceiver.ACTION_PAIR_RESULT).apply {
                        putExtra(PairNotificationReceiver.EXTRA_PORT, port)
                        putExtra(PairNotificationReceiver.EXTRA_CODE, value)
                        setPackage(packageName)
                    })
                }
                finish()
            }
            .setNegativeButton("Cancel") { _, _ -> finish() }
            .show()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }
}
