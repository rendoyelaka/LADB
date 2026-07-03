package com.draco.ladb.views

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.draco.ladb.R
import com.draco.ladb.databinding.ActivityPairBinding
import com.draco.ladb.databinding.OverlayPairBinding
import com.google.android.material.snackbar.Snackbar

class PairActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPairBinding
    private var overlayView: android.view.View? = null
    private var windowManager: WindowManager? = null

    companion object {
        const val EXTRA_PORT = "port"
        const val EXTRA_CODE = "code"
        const val EXTRA_SKIPPED = "skipped"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPairBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        title = getString(R.string.pair_title)

        setupButtons()
    }

    private fun setupButtons() {
        binding.openWirelessDebugging.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                // Request overlay permission first
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
                startActivity(intent)
                Toast.makeText(this, "Grant 'Display over other apps' permission, then tap the button again", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            // Show overlay and open Wireless Debugging behind it
            showOverlay()
            openWirelessDebugging()
        }

        binding.btnPair.setOnClickListener { submitPair() }
        binding.btnSkip.setOnClickListener { submitSkip() }
        binding.btnHelp.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(getString(R.string.tutorial_url))))
            } catch (e: Exception) { }
        }
    }

    private fun showOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        val overlayBinding = OverlayPairBinding.inflate(inflater)
        overlayView = overlayBinding.root

        val dm = resources.displayMetrics
        val screenHeight = dm.heightPixels
        val overlayHeight = (screenHeight * 0.48).toInt()

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

        // Wire overlay buttons
        overlayBinding.btnPair.setOnClickListener {
            val port = overlayBinding.port.text.toString().trim()
            val code = overlayBinding.code.text.toString().trim()
            if (port.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "Enter both Port and Pairing code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            removeOverlay()
            val result = Intent().apply {
                putExtra(EXTRA_PORT, port)
                putExtra(EXTRA_CODE, code)
            }
            setResult(RESULT_OK, result)
            finish()
        }

        overlayBinding.btnSkip.setOnClickListener {
            removeOverlay()
            submitSkip()
        }

        overlayBinding.btnHelp.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(getString(R.string.tutorial_url))))
            } catch (e: Exception) { }
        }

        overlayBinding.btnClose.setOnClickListener {
            removeOverlay()
            // Return to PairActivity
            startActivity(Intent(this, PairActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
        }
    }

    private fun removeOverlay() {
        try {
            overlayView?.let { windowManager?.removeView(it) }
            overlayView = null
        } catch (e: Exception) { }
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

    private fun submitSkip() {
        val result = Intent().apply { putExtra(EXTRA_SKIPPED, true) }
        setResult(RESULT_OK, result)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }
}
