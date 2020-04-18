package oupson.phototaskerplugin.helper

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.palette.graphics.Palette
import com.topjohnwu.superuser.Shell
import oupson.phototaskerplugin.BuildConfig
import kotlin.math.abs


// TODO LINEAGEOS PIE
class OverlayHelper {
    companion object {
        private const val TAG = "OverlayHelper"

        private const val ACCENT_COLOR_LIGHT_NAME = "accent_device_default_light"
        private const val ACCENT_COLOR_DARK_NAME = "accent_device_default_dark"

        @RequiresApi(Build.VERSION_CODES.Q)
        fun getColorList(context: Context, isDark: Boolean): HashMap<Int, String> {
            val colorList = hashMapOf<Int, String>()
            val overlayDump = Shell.su("cmd overlay dump").exec()
            if (overlayDump.isSuccess) {
                overlayDump.out.joinToString("").split('}').forEach {
                    if (it.contains("android.theme.customization.accent_color")) {
                        val packageName = it.split(":0")[0]
                        if (packageName.contains("black") && isDark)
                            return@forEach
                        val r = context.packageManager.getResourcesForApplication(packageName)
                        val lightColor = r.getColor(
                            r.getIdentifier(ACCENT_COLOR_LIGHT_NAME, "color", packageName),
                            null
                        )
                        val darkColor = r.getColor(
                            r.getIdentifier(ACCENT_COLOR_DARK_NAME, "color", packageName),
                            null
                        )

                        colorList[if (isDark)
                            darkColor
                        else
                            lightColor] = packageName
                        if (BuildConfig.DEBUG)
                            Log.v(
                                TAG,
                                "$packageName : Light Color : ${String.format(
                                    "#%08X",
                                    lightColor
                                )}, Dark Color : ${String.format(
                                    "#%08X",
                                    darkColor
                                )}"
                            )
                    }
                }
            }
            return colorList
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        fun setDarkMode(context: Context, isDark: Boolean) {
            if (ActivityCompat.checkSelfPermission(context, "android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_DENIED) {
                Shell.su("pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS").exec()
            }
            if (isDark && !isDarkModeEnabled(context)) {
                Settings.Secure.putInt(context.contentResolver, "ui_night_mode", 2)
            } else if (!isLightModeEnabled(context) && !isDark) {
                Settings.Secure.putInt(context.contentResolver, "ui_night_mode", 1)
            }

            val uiModeManager =
                context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            uiModeManager.enableCarMode(UiModeManager.MODE_NIGHT_AUTO)
            uiModeManager.disableCarMode(UiModeManager.MODE_NIGHT_AUTO)
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        fun isDarkModeEnabled(context: Context) : Boolean =
            Settings.Secure.getInt(context.contentResolver, "ui_night_mode") == 2

        @RequiresApi(Build.VERSION_CODES.Q)
        fun isLightModeEnabled(context: Context) : Boolean
            = Settings.Secure.getInt(context.contentResolver, "ui_night_mode") == 1

        @RequiresApi(Build.VERSION_CODES.Q)
        fun getAccentEnabled(context: Context, accentList : HashMap<Int, String> = getColorList(context, isDarkModeEnabled(context))) : String? {
            val list = Shell.su("cmd overlay list | grep [x]").exec()
            val aList = accentList.values
            if (list.isSuccess) {
                list.out.forEach {
                    if (it.replace("[x] ", "") in aList)
                        return it.replace("[x] ", "")
                }
            }
            return null
        }

        fun setAccentPackage(pack: String) {
            Shell.su("cmd overlay enable-exclusive --category $pack").exec()
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        fun getSuggestion(context: Context, bitmap: Bitmap) : Pair<Boolean, String?> {
            val isDark = isDark(bitmap)
            if (BuildConfig.DEBUG)
                Log.i(TAG, "Is Dark : $isDark")

            val colorList = getColorList(context, isDark)

            val accentColors = colorList.keys.toIntArray()
            val palette = Palette.from(bitmap).generate()
            val vibrant = palette.vibrantSwatch?.rgb ?: palette.lightVibrantSwatch?.rgb ?: palette.darkVibrantSwatch?.rgb ?: palette.mutedSwatch?.rgb ?: Palette.from(bitmap).generate().swatches.sortedBy { it.population }[0].rgb

            if (BuildConfig.DEBUG)
                Log.i(TAG, "Selected color : ${String.format("#%06X", vibrant)}")

            var nearest = accentColors[0]
            accentColors.forEach {
                if (distance(vibrant, nearest) > distance(vibrant, it)) {
                    nearest = it
                    if (BuildConfig.DEBUG)
                        Log.v(TAG, "Selected ${colorList[it]}(${distance(vibrant, nearest)}) : ${String.format("#%06X", nearest)}")
                } else {
                    if (BuildConfig.DEBUG)
                        Log.v(TAG, "${colorList[nearest]}(${distance(vibrant, nearest)}; ${String.format("#%06X", nearest)}) vs ${colorList[it]}(${distance(vibrant, it)}; ${String.format("#%06X", it)})")
                }
            }

            if (BuildConfig.DEBUG)
                Log.i(TAG, "Suggestion is ${colorList[nearest]} ${String.format("#%06X", nearest)}")

            return Pair(isDark, colorList[nearest])
        }

        private fun isDark(bitmap: Bitmap): Boolean {
            var dark = false
            val darkThreshold = bitmap.width * bitmap.height * 0.45f
            var darkPixels = 0
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            for (pixel in pixels) {
                val r: Int = Color.red(pixel)
                val g: Int = Color.green(pixel)
                val b: Int = Color.blue(pixel)
                val luminance =
                    0.299 * r + 0.0f + 0.587 * g + 0.0f + 0.114 * b + 0.0f
                if (luminance < 150) {
                    darkPixels++
                }
            }
            if (darkPixels >= darkThreshold) {
                dark = true
            }
            return dark
        }

        private fun distance(vibrant : Int, it : Int) : Float {
            val vHsv = FloatArray(3)
            Color.colorToHSV(vibrant, vHsv)
            val iHsv = FloatArray(3)
            Color.colorToHSV(it, iHsv)

            //return (iHsv[2] - vHsv[2]).pow(2) + (iHsv[1] * cos(iHsv[0] * PI.toFloat() / 180f) - vHsv[1]*cos(iHsv[0] * PI.toFloat() / 180f)).pow(2) + (iHsv[1] * sin(iHsv[0] * PI.toFloat() / 180f) - vHsv[1]*sin(vHsv[0] * PI.toFloat() / 180f)).pow(2)
            return abs(iHsv[0] - vHsv[0]) + abs(iHsv[1] - vHsv[1]) + 2 * abs(iHsv[2] - iHsv[2])
            //((Color.red(vibrant) - Color.red(it))).pow(2) * 0.2989F  + ((Color.green(vibrant) - Color.green(it))).pow(2) * 0.5870F + ((Color.blue(vibrant) - Color.blue(it))).pow(2) * 0.1140F
        }
    }
}