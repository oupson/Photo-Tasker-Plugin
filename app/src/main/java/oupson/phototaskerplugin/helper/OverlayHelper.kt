package oupson.phototaskerplugin.helper

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.palette.graphics.Palette
import com.topjohnwu.superuser.Shell
import lineageos.providers.LineageSettings
import oupson.phototaskerplugin.BuildConfig
import kotlin.math.abs

internal class OverlayHelper {
    class UnsupportedDeviceException :
        Exception("VERSION >= Q : ${Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q}; Lineage os Pie : ${isLineageOsPie()}")

    companion object {
        private const val TAG = "OverlayHelper"

        private const val ACCENT_COLOR_LIGHT_NAME = "accent_device_default_light"
        private const val ACCENT_COLOR_DARK_NAME = "accent_device_default_dark"
        private const val ANDROID_PACKAGE = "android"

        private val lineageOsAccentPackageList = listOf(
            "org.lineageos.overlay.accent.blue",
            "org.lineageos.overlay.accent.brown",
            "org.lineageos.overlay.accent.cyan",
            "org.lineageos.overlay.accent.green",
            "org.lineageos.overlay.accent.orange",
            "org.lineageos.overlay.accent.pink",
            "org.lineageos.overlay.accent.purple",
            "org.lineageos.overlay.accent.red",
            "org.lineageos.overlay.accent.yellow"
        )

        fun getColorList(context: Context, isDark: Boolean): HashMap<Int, String> {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    val colorList = hashMapOf<Int, String>()
                    colorList[getSystemDefault(
                        isDark
                    )] = "default"
                    val overlayDump = Shell.su("cmd overlay dump").exec()
                    if (overlayDump.isSuccess) {
                        overlayDump.out.joinToString("").split('}').forEach {
                            if (it.contains("android.theme.customization.accent_color")) {
                                val packageName = it.split(":0")[0]
                                if (packageName.contains("black") && isDark)
                                    return@forEach
                                val r =
                                    context.packageManager.getResourcesForApplication(packageName)
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
                isLineageOsPie() -> {
                    val colorList = hashMapOf<Int, String>()
                    colorList[getSystemDefault(
                        isDark
                    )] = "default"
                    lineageOsAccentPackageList.forEach { packageName ->
                        val r = context.packageManager.getResourcesForApplication(packageName)
                        val lightColor = r.getColor(
                            r.getIdentifier(ACCENT_COLOR_LIGHT_NAME, "color", packageName),
                            null
                        )
                        colorList[lightColor] = packageName
                    }
                    return colorList
                }
                else -> {
                    throw UnsupportedDeviceException()
                }
            }
        }

        private fun getSystemDefault(isDark: Boolean): Int {
            val system: Resources = Resources.getSystem()
            return ResourcesCompat.getColor(
                system,
                system.getIdentifier(
                    if (isDark) ACCENT_COLOR_DARK_NAME else ACCENT_COLOR_LIGHT_NAME,
                    "color",
                    ANDROID_PACKAGE
                ), null
            )
        }

        fun setDarkMode(context: Context, isDark: Boolean) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    "android.permission.WRITE_SECURE_SETTINGS"
                ) == PackageManager.PERMISSION_DENIED
            ) {
                Shell.su("pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS")
                    .exec()
            }

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    if (isDark && !isDarkModeEnabled(
                            context
                        )
                    ) {
                        Settings.Secure.putInt(context.contentResolver, "ui_night_mode", 2)
                    } else if (!isLightModeEnabled(
                            context
                        ) && !isDark) {
                        Settings.Secure.putInt(context.contentResolver, "ui_night_mode", 1)
                    }

                    val uiModeManager =
                        context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
                    uiModeManager.enableCarMode(UiModeManager.MODE_NIGHT_AUTO)
                    uiModeManager.disableCarMode(UiModeManager.MODE_NIGHT_AUTO)
                }
                isLineageOsPie() -> {
                    LineageSettings.System.putInt(
                        context.contentResolver,
                        LineageSettings.System.BERRY_GLOBAL_STYLE, if (isDark) 3 else 2
                    )
                    LineageSettings.System.putString(
                        context.contentResolver,
                        LineageSettings.System.BERRY_MANAGED_BY_APP, context.packageName
                    )
                }
                else -> {
                    throw UnsupportedDeviceException()
                }
            }
        }

        fun isDarkModeEnabled(context: Context): Boolean =
            if (isLineageOsPie() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                Settings.Secure.getInt(context.contentResolver, "ui_night_mode") == 2
            else
                throw UnsupportedDeviceException()

        fun isLightModeEnabled(context: Context): Boolean =
            if (isLineageOsPie() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                Settings.Secure.getInt(context.contentResolver, "ui_night_mode") == 1
            else
                throw UnsupportedDeviceException()

        fun getAccentEnabled(
            context: Context,
            accentList: HashMap<Int, String> = getColorList(
                context,
                isDarkModeEnabled(
                    context
                )
            )
        ): String? {
            if (isLineageOsPie() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val list = Shell.su("cmd overlay list | grep [x]").exec()
                val aList = accentList.values
                if (list.isSuccess) {
                    list.out.forEach {
                        if (it.replace("[x] ", "") in aList)
                            return it.replace("[x] ", "")
                    }
                }
                return null
            } else {
                throw UnsupportedDeviceException()
            }
        }

        fun setAccentPackage(context: Context, pack: String) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    if (pack != "default")
                        Shell.su("cmd overlay enable-exclusive --category $pack").exec()
                    else {
                        val enabled =
                            getAccentEnabled(
                                context
                            )
                        if (enabled != null)
                            Shell.su("cmd overlay disable $enabled").exec()
                    }

                }
                isLineageOsPie() -> {
                    val enabled =
                        getAccentEnabled(
                            context
                        )
                    val success = if (enabled != null)
                        Shell.su("cmd overlay disable $enabled").exec().isSuccess
                    else
                        true
                    if (success) {
                        if (pack != "default")
                            Shell.su("cmd overlay enable $pack").exec()
                        LineageSettings.System.putString(
                            context.contentResolver,
                            LineageSettings.System.BERRY_CURRENT_ACCENT,
                            if (pack != "default") pack else ""
                        )
                    }
                }
                else -> throw UnsupportedDeviceException()
            }
        }

        fun getSuggestion(context: Context, bitmap: Bitmap): Pair<Boolean, String?> {
            val isDark =
                isDark(
                    bitmap
                )
            if (BuildConfig.DEBUG)
                Log.i(TAG, "Is Dark : $isDark")

            val colorList =
                getColorList(
                    context,
                    isDark
                )

            val accentColors = colorList.keys.toIntArray()
            val palette = Palette.from(bitmap).generate()
            val vibrant = palette.vibrantSwatch?.rgb ?: palette.lightVibrantSwatch?.rgb
            ?: palette.darkVibrantSwatch?.rgb ?: palette.mutedSwatch?.rgb ?: Palette.from(bitmap)
                .generate().swatches.sortedBy { it.population }[0].rgb

            if (BuildConfig.DEBUG)
                Log.i(TAG, "Selected color : ${String.format("#%06X", vibrant)}")

            var nearest = accentColors[0]
            accentColors.forEach {
                if (distance(
                        vibrant,
                        nearest
                    ) > distance(
                        vibrant,
                        it
                    )
                ) {
                    nearest = it
                    if (BuildConfig.DEBUG)
                        Log.v(
                            TAG,
                            "Selected ${colorList[it]}(${distance(
                                vibrant,
                                nearest
                            )}) : ${String.format("#%06X", nearest)}"
                        )
                } else {
                    if (BuildConfig.DEBUG)
                        Log.v(
                            TAG,
                            "${colorList[nearest]}(${distance(
                                vibrant,
                                nearest
                            )}; ${String.format("#%06X", nearest)}) vs ${colorList[it]}(${distance(
                                vibrant,
                                it
                            )}; ${String.format("#%06X", it)})"
                        )
                }
            }

            if (BuildConfig.DEBUG)
                Log.i(TAG, "Suggestion is ${colorList[nearest]} ${String.format("#%06X", nearest)}")

            return Pair(isDark, colorList[nearest])
        }

        fun isLineageOsPie() =
            Build.VERSION.SDK_INT == Build.VERSION_CODES.P && lineageos.os.Build.LINEAGE_VERSION.SDK_INT == lineageos.os.Build.LINEAGE_VERSION_CODES.ILAMA

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

        private fun distance(vibrant: Int, it: Int): Float {
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