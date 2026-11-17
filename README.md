# Bentley Custom Keyboard

A native Android input method with a PC-style layout, including Ctrl, Shift, Alt, Fn, function keys, navigation keys, and configurable appearance.

The Android IME service is registered in system keyboard settings. Install the APK, enable **Bentley Custom Keyboard** under Settings → System → Keyboard → On-screen keyboard, then select it as the current keyboard.

## Build

GitHub Actions builds the Android APK on pushes to `main`. The debug APK is always produced. A signed release APK is produced when the four Android signing secrets are configured.
