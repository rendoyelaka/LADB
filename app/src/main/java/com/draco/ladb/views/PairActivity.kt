package com.draco.ladb.views

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.draco.ladb.R
import com.draco.ladb.databinding.ActivityPairBinding
import com.draco.ladb.receivers.PairNotificationReceiver
import com.draco.ladb.services.PairNotificationManager
import com.google.android.material.snackbar.Snackbar

class PairActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPairBinding

    companion object {
        const val EXTRA_PORT    = "port"
        const val EXTRA_CODE    = "code"
        const val EXTRA_SKIPPED = "skipped"
    }

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            PairNotificationManager.show(this)
            openWirelessDebugging()
        } else {
            Snackbar.make(binding.root,
                "Notification permission required. Enable in App Settings.",
                Snackbar.LENGTH_LONG)
                .setAction("Settings") {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:$packageName")))
                }.show()
        }
    }

    private val pairReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val skipped = intent.getBooleanExtra(PairNotificationReceiver.EXTRA_SKIPPED, false)
            val result = Intent().apply {
                if (skipped) {
                    putExtra(EXTRA_SKIPPED, true)
                } else {
                    putExtra(EXTRA_PORT, intent.getStringExtra(PairNotificationReceiver.EXTRA_PORT))
                    putExtra(EXTRA_CODE, intent.getStringExtra(PairNotificationReceiver.EXTRA_CODE))
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

        val filter = IntentFilter(PairNotificationReceiver.ACTION_PAIR_RESULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pairReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(pairReceiver, filter)
        }

        binding.openWirelessDebugging.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                PairNotificationManager.show(this)
                openWirelessDebugging()
            }
        }

        binding.btnPair.setOnClickListener { submitPair() }

        binding.btnSkip.setOnClickListener {
            PairNotificationManager.dismiss(this)
            val result = Intent().apply { putExtra(EXTRA_SKIPPED, true) }
            setResult(RESULT_OK, result)
            finish()
        }

        binding.btnHelp.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.tutorial_url))))
            } catch (e: Exception) { }
        }
    }

    private fun openWirelessDebugging() {
        // Try multiple intents — Vivo/AOSP variations
        val intents = listOf(
            Intent("android.settings.WIRELESS_DEBUGGING_SETTINGS"),
            Intent("com.android.settings.WIRELESS_DEBUGGING"),
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
        Snackbar.make(binding.root,
            "Go to Settings → Developer Options → Wireless Debugging",
            Snackbar.LENGTH_LONG).show()
    }

    private fun submitPair() {
        val port = binding.port.text.toString().trim()
        val code = binding.code.text.toString().trim()
        if (port.isEmpty() || code.isEmpty()) {
            Snackbar.make(binding.root, "Enter both Port and Pairing code", Snackbar.LENGTH_SHORT).show()
            return
        }
        PairNotificationManager.dismiss(this)
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
