package com.draco.ladb.views

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.draco.ladb.R
import com.draco.ladb.databinding.ActivityPairBinding
import com.draco.ladb.services.OverlayService
import com.google.android.material.snackbar.Snackbar

class PairActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPairBinding

    companion object {
        const val EXTRA_PORT    = "port"
        const val EXTRA_CODE    = "code"
        const val EXTRA_SKIPPED = "skipped"
    }

    private val pairReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val skipped = intent.getBooleanExtra(OverlayService.EXTRA_SKIPPED, false)
            val result = Intent().apply {
                if (skipped) {
                    putExtra(EXTRA_SKIPPED, true)
                } else {
                    putExtra(EXTRA_PORT, intent.getStringExtra(OverlayService.EXTRA_PORT))
                    putExtra(EXTRA_CODE, intent.getStringExtra(OverlayService.EXTRA_CODE))
                }
            }
            setResult(RESULT_OK, result)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPairBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        title = getString(R.string.pair_title)

        // Register receiver for overlay result
        val filter = IntentFilter(OverlayService.ACTION_PAIR_RESULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pairReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(pairReceiver, filter)
        }

        binding.openWirelessDebugging.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
                startActivity(intent)
                Toast.makeText(this,
                    "Grant 'Display over other apps' permission, then tap again",
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            // Start overlay service
            val serviceIntent = Intent(this, OverlayService::class.java).apply {
                action = OverlayService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            // Open Wireless Debugging behind the overlay
            openWirelessDebugging()
        }

        binding.btnPair.setOnClickListener { submitPair() }

        binding.btnSkip.setOnClickListener {
            val result = Intent().apply { putExtra(EXTRA_SKIPPED, true) }
            setResult(RESULT_OK, result)
            finish()
        }

        binding.btnHelp.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse(getString(R.string.tutorial_url))))
            } catch (e: Exception) { }
        }
    }

    private fun openWirelessDebugging() {
        val intents = listOf(
            Intent("android.settings.WIRELESS_DEBUGGING_SETTINGS"),
            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return
            } catch (e: Exception) { continue }
        }
    }

    private fun submitPair() {
        val port = binding.port.text.toString().trim()
        val code = binding.code.text.toString().trim()
        if (port.isEmpty() || code.isEmpty()) {
            Snackbar.make(binding.root, "Enter both Port and Pairing code", Snackbar.LENGTH_SHORT).show()
            return
        }
        val result = Intent().apply {
            putExtra(EXTRA_PORT, port)
            putExtra(EXTRA_CODE, code)
        }
        setResult(RESULT_OK, result)
        finish()
    }

    override fun onDestroy() {
        try { unregisterReceiver(pairReceiver) } catch (e: Exception) { }
        super.onDestroy()
    }
}
