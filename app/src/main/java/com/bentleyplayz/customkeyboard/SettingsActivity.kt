package com.bentleyplayz.customkeyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class SettingsActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("keyboard_settings", MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
            setBackgroundColor(0xFFF3F5F8.toInt())
        }
        root.addView(TextView(this).apply { text = getString(R.string.settings_title); textSize = 25f; setTextColor(0xFF17202B.toInt()) })
        root.addView(TextView(this).apply {
            text = getString(R.string.settings_description); textSize = 14f
            setTextColor(0xFF526171.toInt()); setPadding(0, dp(8), 0, dp(18))
        })
        root.addView(CheckBox(this).apply {
            text = getString(R.string.dark_theme); isChecked = prefs.getBoolean("dark", false)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("dark", checked).apply() }
        })
        root.addView(CheckBox(this).apply {
            text = getString(R.string.vibration); isChecked = prefs.getBoolean("vibration", false)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("vibration", checked).apply() }
        })
        root.addView(TextView(this).apply {
            text = getString(R.string.key_height); textSize = 16f; setTextColor(0xFF17202B.toInt())
            setPadding(0, dp(18), 0, dp(4))
        })
        root.addView(SeekBar(this).apply {
            max = 28; progress = prefs.getInt("key_height", 48) - 40
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) prefs.edit().putInt("key_height", progress + 40).apply()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        })
        root.addView(Button(this).apply {
            text = getString(R.string.enable_keyboard)
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        })
        root.addView(Button(this).apply {
            text = getString(R.string.choose_keyboard)
            setOnClickListener { (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker() }
        })
        root.addView(TextView(this).apply {
            text = "Bentley Custom Keyboard 1.0.0"; textSize = 12f; gravity = Gravity.CENTER
            setTextColor(0xFF687789.toInt()); setPadding(0, dp(24), 0, 0)
        })
        setContentView(root)
    }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
