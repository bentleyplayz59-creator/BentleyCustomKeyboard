package com.bentleyplayz.customkeyboard

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

class BentleyKeyboardService : InputMethodService() {
    private var ctrl = false
    private var alt = false
    private var shift = false
    private var fn = false
    private var caps = false
    private lateinit var root: LinearLayout
    private val prefs by lazy { getSharedPreferences("keyboard_settings", MODE_PRIVATE) }

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(3), dp(5), dp(3), dp(5)) }
        rebuild()
        return root
    }

    private fun rebuild() {
        if (!::root.isInitialized) return
        root.removeAllViews()
        val dark = prefs.getBoolean("dark", false)
        root.setBackgroundColor(if (dark) 0xFF171D26.toInt() else 0xFFE0E5EC.toInt())
        val h = prefs.getInt("key_height", 48)
        addScrollableRow(listOf("Esc","F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","F11","F12")) { key ->
            if (key == "Esc") sendCode(KeyEvent.KEYCODE_ESCAPE) else sendFunction(key)
        }
        addRow(listOf("\u0060","1","2","3","4","5","6","7","8","9","0","-","=","⌫"), h) { pressSymbol(it) }
        addRow(listOf("Tab","Q","W","E","R","T","Y","U","I","O","P","[","]","\\"), h) { pressSpecialOrText(it) }
        addRow(listOf("Caps","A","S","D","F","G","H","J","K","L",";","'","Enter"), h) { pressSpecialOrText(it) }
        addRow(listOf("Shift","Z","X","C","V","B","N","M",",",".","/","Shift"), h) { pressSpecialOrText(it) }
        addBottomRow(h)
        addRow(listOf("Ins","Home","PgUp","Del","End","PgDn"), h) { pressSpecialOrText(it) }
        addRow(listOf("Ctrl","Alt","Fn","Settings"), h) { pressSpecialOrText(it) }
    }

    private fun addScrollableRow(keys: List<String>, action: (String) -> Unit) {
        val scroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        keys.forEach { key -> row.addView(keyView(key, true) { action(key) },
            LinearLayout.LayoutParams(dp(43), dp(38)).apply { setMargins(dp(2), dp(2), dp(2), dp(3)) }) }
        scroll.addView(row); root.addView(scroll)
    }

    private fun addRow(keys: List<String>, height: Int, action: (String) -> Unit) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        keys.forEach { key ->
            val weight = if (key in listOf("Tab", "Caps", "Shift", "Enter", "⌫")) 1.45f else 1f
            row.addView(keyView(key, false) { action(key) },
                LinearLayout.LayoutParams(0, dp(height), weight).apply { setMargins(dp(1), dp(2), dp(1), dp(2)) })
        }
        root.addView(row)
    }

    private fun addBottomRow(height: Int) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        val items = listOf("Ctrl" to 1.3f, "Alt" to 1.1f, "Space" to 4.2f, "Alt" to 1.1f, "←" to 1f, "↓" to 1f, "↑" to 1f, "→" to 1f)
        items.forEach { (key, weight) -> row.addView(keyView(key, false) { pressSpecialOrText(key) },
            LinearLayout.LayoutParams(0, dp(height), weight).apply { setMargins(dp(1), dp(2), dp(1), dp(2)) }) }
        root.addView(row)
    }

    private fun keyView(label: String, small: Boolean, action: () -> Unit): TextView {
        val dark = prefs.getBoolean("dark", false)
        val active = (label == "Ctrl" && ctrl) || (label == "Alt" && alt) ||
            (label == "Shift" && shift) || (label == "Fn" && fn) || (label == "Caps" && caps)
        return TextView(this).apply {
            text = label; gravity = Gravity.CENTER
            textSize = if (small) 10f else if (label.length > 4) 10f else 13f
            setTextColor(if (dark) Color.WHITE else 0xFF17202B.toInt())
            background = GradientDrawable().apply {
                cornerRadius = dp(5).toFloat()
                setColor(if (active) 0xFF477FC1.toInt() else if (dark) 0xFF303B4B.toInt() else Color.WHITE)
                setStroke(dp(1), if (dark) 0xFF465366.toInt() else 0xFFC4CCD6.toInt())
            }
            isFocusable = false
            setOnClickListener {
                if (prefs.getBoolean("vibration", false)) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                action()
            }
        }
    }

    private fun pressSymbol(key: String) {
        if (fn && key in listOf("1","2","3","4","5","6","7","8","9","0")) {
            val number = if (key == "0") 10 else key.toInt()
            sendFunction("F$number")
            return
        }
        if (key == "⌫") {
            sendCode(if (fn) KeyEvent.KEYCODE_FORWARD_DEL else KeyEvent.KEYCODE_DEL)
            return
        }
        val shifted = shift
        val value = when (key) {
            "\u0060" -> if (shifted) "~" else "\u0060"
            "1" -> if (shifted) "!" else "1"
            "2" -> if (shifted) "@" else "2"
            "3" -> if (shifted) "#" else "3"
            "4" -> if (shifted) "$" else "4"
            "5" -> if (shifted) "%" else "5"
            "6" -> if (shifted) "^" else "6"
            "7" -> if (shifted) "&" else "7"
            "8" -> if (shifted) "*" else "8"
            "9" -> if (shifted) "(" else "9"
            "0" -> if (shifted) ")" else "0"
            "-" -> if (shifted) "_" else "-"
            "=" -> if (shifted) "+" else "="
            else -> key
        }
        commit(value)
    }

    private fun pressSpecialOrText(key: String) {
        when (key) {
            "Ctrl" -> ctrl = !ctrl
            "Alt" -> alt = !alt
            "Shift" -> shift = !shift
            "Fn" -> fn = !fn
            "Caps" -> caps = !caps
            "Settings" -> startActivity(android.content.Intent(this, SettingsActivity::class.java).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            "Space" -> commit(" ")
            "Tab" -> sendCode(KeyEvent.KEYCODE_TAB)
            "Enter" -> sendCode(KeyEvent.KEYCODE_ENTER)
            "⌫" -> sendCode(KeyEvent.KEYCODE_DEL)
            "←" -> sendCode(if (fn) KeyEvent.KEYCODE_MOVE_HOME else KeyEvent.KEYCODE_DPAD_LEFT)
            "→" -> sendCode(if (fn) KeyEvent.KEYCODE_MOVE_END else KeyEvent.KEYCODE_DPAD_RIGHT)
            "↑" -> sendCode(if (fn) KeyEvent.KEYCODE_PAGE_UP else KeyEvent.KEYCODE_DPAD_UP)
            "↓" -> sendCode(if (fn) KeyEvent.KEYCODE_PAGE_DOWN else KeyEvent.KEYCODE_DPAD_DOWN)
            "Ins" -> sendCode(KeyEvent.KEYCODE_INSERT)
            "Home" -> sendCode(KeyEvent.KEYCODE_MOVE_HOME)
            "End" -> sendCode(KeyEvent.KEYCODE_MOVE_END)
            "PgUp" -> sendCode(KeyEvent.KEYCODE_PAGE_UP)
            "PgDn" -> sendCode(KeyEvent.KEYCODE_PAGE_DOWN)
            "Del" -> sendCode(KeyEvent.KEYCODE_FORWARD_DEL)
            else -> {
                val upper = shift || caps
                val value = if (upper) key.uppercase() else key.lowercase()
                if (ctrl || alt) {
                    val code = letterKeyCode(value.firstOrNull())
                    if (code != null) sendModifiedCode(code) else commit(value)
                } else commit(value)
                if (shift) shift = false
            }
        }
        rebuild()
    }

    private fun commit(value: String) {
        currentInputConnection?.commitText(value, 1)
        if (shift) shift = false
        rebuild()
    }

    private fun sendFunction(label: String) {
        val number = label.removePrefix("F").toIntOrNull() ?: return
        sendCode(KeyEvent.KEYCODE_F1 + number - 1)
    }

    private fun sendCode(code: Int) {
        val ic: InputConnection = currentInputConnection ?: return
        val meta = modifierMeta()
        val now = android.os.SystemClock.uptimeMillis()
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, code, 0, meta))
        ic.sendKeyEvent(KeyEvent(now, android.os.SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, code, 0, meta))
        if (shift) shift = false
        if (fn) fn = false
        rebuild()
    }

    private fun sendModifiedCode(code: Int) {
        sendCode(code)
        ctrl = false; alt = false
    }

    private fun modifierMeta(): Int {
        var meta = 0
        if (ctrl) meta = meta or KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
        if (alt) meta = meta or KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON
        if (shift) meta = meta or KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON
        return meta
    }

    private fun letterKeyCode(char: Char?): Int? = when (char?.uppercaseChar()) {
        'A' -> KeyEvent.KEYCODE_A; 'B' -> KeyEvent.KEYCODE_B; 'C' -> KeyEvent.KEYCODE_C
        'D' -> KeyEvent.KEYCODE_D; 'E' -> KeyEvent.KEYCODE_E; 'F' -> KeyEvent.KEYCODE_F
        'G' -> KeyEvent.KEYCODE_G; 'H' -> KeyEvent.KEYCODE_H; 'I' -> KeyEvent.KEYCODE_I
        'J' -> KeyEvent.KEYCODE_J; 'K' -> KeyEvent.KEYCODE_K; 'L' -> KeyEvent.KEYCODE_L
        'M' -> KeyEvent.KEYCODE_M; 'N' -> KeyEvent.KEYCODE_N; 'O' -> KeyEvent.KEYCODE_O
        'P' -> KeyEvent.KEYCODE_P; 'Q' -> KeyEvent.KEYCODE_Q; 'R' -> KeyEvent.KEYCODE_R
        'S' -> KeyEvent.KEYCODE_S; 'T' -> KeyEvent.KEYCODE_T; 'U' -> KeyEvent.KEYCODE_U
        'V' -> KeyEvent.KEYCODE_V; 'W' -> KeyEvent.KEYCODE_W; 'X' -> KeyEvent.KEYCODE_X
        'Y' -> KeyEvent.KEYCODE_Y; 'Z' -> KeyEvent.KEYCODE_Z
        else -> null
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
