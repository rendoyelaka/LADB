package com.draco.ladb.views

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.draco.ladb.receivers.PairNotificationReceiver
import com.draco.ladb.services.PairNotificationManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class PairInputActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STEP = "step"
        const val STEP_BOTH = "both"
        const val STEP_SKIP = "skip"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val step = intent.getStringExtra(EXTRA_STEP) ?: STEP_BOTH

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

        // Build single dialog with Port + Code fields
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }

        val portInput = TextInputEditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val portLayout = TextInputLayout(this).apply {
            hint = "Port number"
            addView(portInput)
        }

        val codeInput = TextInputEditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val codeLayout = TextInputLayout(this).apply {
            hint = "6-digit Pairing code"
            addView(codeInput)
            setPadding(0, 16, 0, 0)
        }

        container.addView(portLayout)
        container.addView(codeLayout)

        MaterialAlertDialogBuilder(this)
            .setTitle("Enter Pairing Details")
            .setView(container)
            .setCancelable(false)
            .setPositiveButton("PAIR NOW") { _, _ ->
                val port = portInput.text.toString().trim()
                val code = codeInput.text.toString().trim()
                if (port.isEmpty() || code.isEmpty()) {
                    finish()
                    return@setPositiveButton
                }
                PairNotificationManager.dismiss(this)
                sendBroadcast(Intent(PairNotificationReceiver.ACTION_PAIR_RESULT).apply {
                    putExtra(PairNotificationReceiver.EXTRA_PORT, port)
                    putExtra(PairNotificationReceiver.EXTRA_CODE, code)
                    setPackage(packageName)
                })
                finish()
            }
            .setNegativeButton("SKIP") { _, _ ->
                PairNotificationManager.dismiss(this)
                sendBroadcast(Intent(PairNotificationReceiver.ACTION_PAIR_RESULT).apply {
                    putExtra(PairNotificationReceiver.EXTRA_SKIPPED, true)
                    setPackage(packageName)
                })
                finish()
            }
            .show()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }
}
