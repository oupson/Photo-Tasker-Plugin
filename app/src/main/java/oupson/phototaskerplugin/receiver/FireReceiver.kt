package oupson.phototaskerplugin.receiver

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.exifinterface.media.ExifInterface
import androidx.palette.graphics.Palette
import com.topjohnwu.superuser.Shell
import oupson.phototaskerplugin.BuildConfig
import oupson.phototaskerplugin.bundle.PluginBundleValues
import oupson.phototaskerplugin.helper.OverlayHelper
import oupson.phototaskerplugin.tasker.TaskerPlugin
import oupson.phototaskerplugin.util.getBitmap
import oupson.phototaskerplugin.util.openInputStream

class FireReceiver : AbstractPluginSettingReceiver() {
    companion object {
        private const val TAG = "FireReceiver"
    }

    init {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
        Shell.Config.verboseLogging(BuildConfig.DEBUG)
        Shell.Config.setTimeout(10)
    }

    override fun isBundleValid(bundle: Bundle): Boolean {
        return PluginBundleValues.isBundleValid(bundle)
    }

    override val isAsync: Boolean
        get() = false

    override fun firePluginSetting(context: Context, intent: Intent, bundle: Bundle) {
        if (BuildConfig.DEBUG)
            Log.v(TAG, "Path is ${PluginBundleValues.getPath(bundle)}")

        if (isOrderedBroadcast)
            resultCode = TaskerPlugin.Setting.RESULT_CODE_PENDING

        try {
            when (PluginBundleValues.getAction(bundle)) {
                PluginBundleValues.ACTION_GET_INFO -> {
                    getInfo(context, bundle, intent)
                    return
                }
                PluginBundleValues.ACTION_SET_THEME -> {
                    // Set style
                    val bitmap = getBitmap(context, PluginBundleValues.getPath(bundle)!!)!!
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || OverlayHelper.isLineageOsPie()) {
                        if (ActivityCompat.checkSelfPermission(context, "android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_DENIED) {
                            Shell.su("pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS")
                                .exec()
                        }

                        val suggestion = OverlayHelper.getSuggestion(context, bitmap)
                        OverlayHelper.setDarkMode(context, suggestion.first)

                        if (suggestion.second != null)
                            OverlayHelper.setAccentPackage(context, suggestion.second!!)
                    } else {
                        throw OverlayHelper.UnsupportedDeviceException()
                    }
                }
            }
            TaskerPlugin.Setting.signalFinish(
                context,
                intent,
                TaskerPlugin.Setting.RESULT_CODE_OK,
                null
            )
        } catch (e : Exception) {
            Log.e(TAG, "Error when generating palette", e)
            val vars = Bundle()
            vars.putString(TaskerPlugin.Setting.VARNAME_ERROR_MESSAGE, e.message)
            TaskerPlugin.Setting.signalFinish(context, intent, TaskerPlugin.Setting.RESULT_CODE_FAILED, vars)
        }
    }

    private fun getInfo(context: Context, bundle: Bundle, intent: Intent) {
        val bitmap = getBitmap(context, PluginBundleValues.getPath(bundle)!!)!!
        val vars = Bundle()

        //region Palette
        val palette = Palette.from(bitmap).generate()
        if (palette.vibrantSwatch != null) {
            vars.putString(
                "%vibrant",
                String.format("#%06X", (0xFFFFFF and palette.vibrantSwatch!!.rgb))
            )
            vars.putString(
                "%vibranttext",
                String.format(
                    "#%06X",
                    (0xFFFFFF and palette.vibrantSwatch!!.bodyTextColor)
                )
            )
        }

        if (palette.darkVibrantSwatch != null) {
            vars.putString(
                "%darkvibrant",
                String.format("#%06X", (0xFFFFFF and palette.darkVibrantSwatch!!.rgb))
            )
            vars.putString(
                "%darkvibranttext",
                String.format(
                    "#%06X",
                    (0xFFFFFF and palette.darkVibrantSwatch!!.bodyTextColor)
                )
            )
        }

        if (palette.lightVibrantSwatch != null) {
            vars.putString(
                "%lightvibrant",
                String.format("#%06X", (0xFFFFFF and palette.lightVibrantSwatch!!.rgb))
            )
            vars.putString(
                "%lightvibranttext",
                String.format(
                    "#%06X",
                    (0xFFFFFF and palette.lightVibrantSwatch!!.bodyTextColor)
                )
            )
        }

        if (palette.mutedSwatch != null) {
            vars.putString(
                "%muted",
                String.format("#%06X", (0xFFFFFF and palette.mutedSwatch!!.rgb))
            )
            vars.putString(
                "%mutedtext",
                String.format(
                    "#%06X",
                    (0xFFFFFF and palette.mutedSwatch!!.bodyTextColor)
                )
            )
        }

        if (palette.darkMutedSwatch != null) {
            vars.putString(
                "%darkmuted",
                String.format("#%06X", (0xFFFFFF and palette.darkMutedSwatch!!.rgb))
            )
            vars.putString(
                "%darkmutedtext",
                String.format(
                    "#%06X",
                    (0xFFFFFF and palette.darkMutedSwatch!!.bodyTextColor)
                )
            )
        }

        if (palette.lightMutedSwatch != null) {
            vars.putString(
                "%lightmuted",
                String.format("#%06X", (0xFFFFFF and palette.lightMutedSwatch!!.rgb))
            )
            vars.putString(
                "%lightmutedtext",
                String.format(
                    "#%06X",
                    (0xFFFFFF and palette.lightMutedSwatch!!.bodyTextColor)
                )
            )
        }
        // endregion

        try {
            val inputStream = openInputStream(context, PluginBundleValues.getPath(bundle)!!)!!
            val exif = ExifInterface(inputStream)
            inputStream.close()

            val latLong = exif.latLong
            if (latLong != null) {
                if (BuildConfig.DEBUG)
                    Log.v(TAG, "LatLong is ${latLong.joinToString(";")}")
                vars.putString("%metadata_lat", latLong[0].toString())
                vars.putString("%metadata_long", latLong[1].toString())
            } else if (BuildConfig.DEBUG)
                Log.v(TAG, "LatLong is null")

            val model = exif.getAttribute(ExifInterface.TAG_MODEL)
            if (model != null) {
                if (BuildConfig.DEBUG)
                    Log.v(TAG, "Model is $model")
                vars.putString("%metadata_model", model)
            } else if (BuildConfig.DEBUG)
                Log.v(TAG, "Model is null")

            val artist = exif.getAttribute(ExifInterface.TAG_ARTIST)
            if (artist != null) {
                if (BuildConfig.DEBUG)
                    Log.v(TAG, "Artist is $artist")
                vars.putString("%metadata_artist", artist)
            } else if (BuildConfig.DEBUG)
                Log.v(TAG, "Artist is null")

            val date = exif.getAttribute(ExifInterface.TAG_DATETIME)
            if (date != null) {
                if (BuildConfig.DEBUG)
                    Log.v(TAG, "Date is $date")
                vars.putString("%metadata_date", date)
            } else if (BuildConfig.DEBUG)
                Log.v(TAG, "Date is null")
        } catch (e: Exception) {
            Log.e(TAG, "Error", e)
            vars.putString(TaskerPlugin.Setting.VARNAME_ERROR_MESSAGE, e.message)
            TaskerPlugin.Setting.signalFinish(
                context,
                intent,
                TaskerPlugin.Setting.RESULT_CODE_FAILED,
                vars
            )
            return
        }

        TaskerPlugin.Setting.signalFinish(
            context,
            intent,
            TaskerPlugin.Setting.RESULT_CODE_OK,
            vars
        )
    }
}