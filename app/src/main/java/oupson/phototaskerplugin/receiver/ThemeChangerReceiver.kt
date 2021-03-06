package oupson.phototaskerplugin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import oupson.phototaskerplugin.BuildConfig
import oupson.phototaskerplugin.helper.OverlayHelper
import oupson.phototaskerplugin.util.getBitmap

class ThemeChangerReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "ThemeChangerReceiver"
        const val ACTION_SET_THEME = "${BuildConfig.APPLICATION_ID}.service.action.SET_THEME"
        const val ACTION_SET_THEME_FROM_IMAGE =
            "${BuildConfig.APPLICATION_ID}.service.action.SET_THEME_FROM_IMAGE"

        const val EXTRA_PARAM_IS_DARK = "${BuildConfig.APPLICATION_ID}.service.param.IS_DARK"
        const val EXTRA_PARAM_ACCENT_PACKAGE =
            "${BuildConfig.APPLICATION_ID}.service.param.ACCENT_PACKAGE"

        const val EXTRA_PARAM_PATH = "${BuildConfig.APPLICATION_ID}.service.param.IMAGE_PATH"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if (context == null) {
                Log.e(TAG, "Context is null, returning.")
                return
            }
            when (intent?.getStringExtra("ACTION")) {
                ACTION_SET_THEME -> {
                    val isDark =
                        intent.getBooleanExtra(EXTRA_PARAM_IS_DARK, false)
                    val accentPackage =
                        intent.getStringExtra(EXTRA_PARAM_ACCENT_PACKAGE)
                            ?: "default"
                    setTheme(context, isDark, accentPackage)
                }
                ACTION_SET_THEME_FROM_IMAGE -> {
                    val path = intent.getStringExtra(EXTRA_PARAM_PATH)
                    if (path == null) {
                        Log.e(TAG, "Path is null, returning")
                        return
                    }
                    setThemeFromImage(context, path)
                }
            }
        } catch (e : Exception) {
            Log.e(TAG, "Error onReceive : $e")
        }
    }

    private fun setTheme(context: Context, isDark: Boolean, accentPackage: String) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "setTheme(isDark : $isDark, accentPackage : $accentPackage)")
        try {
            OverlayHelper.setDarkMode(context, isDark)
            OverlayHelper.setAccentPackage(context, accentPackage)
        } catch (e: Exception) {
            Log.e(TAG, "Exception on setTheme() : $e")
        }
    }

    private fun setThemeFromImage(context: Context, path: String) {
        try {
            if (BuildConfig.DEBUG)
                Log.i(TAG, "setTheme(path : $path)")
            val bitmap = getBitmap(context, path)!!

            val suggestion =
                OverlayHelper.getSuggestion(context, bitmap)
            OverlayHelper.setDarkMode(context, suggestion.first)

            if (suggestion.second != null)
                OverlayHelper.setAccentPackage(
                    context,
                    suggestion.second!!
                )
        } catch (e : Exception) {
            Log.e(TAG, "Exception on setThemeWithImage() : $e")
        }
    }
}
