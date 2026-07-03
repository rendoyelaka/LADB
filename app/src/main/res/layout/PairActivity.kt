package com.draco.ladb.views

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.draco.ladb.R
import com.draco.ladb.databinding.ActivityPairBinding
import com.google.android.material.snackbar.Snackbar

class PairActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPairBinding

    companion object {
        const val EXTRA_PORT = "port"
        const val EXTRA_CODE = "code"
        const val EXTRA_SKIPPED = "skipped"
        const val REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPairBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        title = getString(R.string.pair_title)

        binding.openWirelessDebugging.setOnClickListener {
            val intents = listOf(
                Intent("android.settings.WIRELESS_DEBUGGING_SETTINGS"),
                Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
                Intent(Settings.ACTION_SETTINGS)
            )
            var launched = false
            for (intent in intents) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    launched = true
                    break
                } catch (e: Exception) {
                    continue
                }
            }
            if (!launched) {
                Snackbar.make(
                    binding.root,
                    "Go to Settings → Developer Options → Wireless Debugging",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        binding.btnPair.setOnClickListener {
            val port = binding.port.text.toString().trim()
            val code = binding.code.text.toString().trim()
            if (port.isEmpty() || code.isEmpty()) {
                Snackbar.make(binding.root, "Enter both Port and Pairing code", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val result = Intent().apply {
                putExtra(EXTRA_PORT, port)
                putExtra(EXTRA_CODE, code)
            }
            setResult(RESULT_OK, result)
            finish()
        }

        binding.btnSkip.setOnClickListener {
            val result = Intent().apply {
                putExtra(EXTRA_SKIPPED, true)
            }
            setResult(RESULT_OK, result)
            finish()
        }

        binding.btnHelp.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                android.net.Uri.parse(getString(R.string.tutorial_url)))
            try { startActivity(intent) } catch (e: Exception) { }
        }
    }
}
