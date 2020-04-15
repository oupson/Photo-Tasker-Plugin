package oupson.phototaskerplugin.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.topjohnwu.superuser.Shell
import kotlinx.android.synthetic.main.activity_test.*
import lineageos.style.StyleInterface
import oupson.phototaskerplugin.BuildConfig
import oupson.phototaskerplugin.R
import oupson.phototaskerplugin.activity.test.PaletteTestActivity
import oupson.phototaskerplugin.helper.OverlayHelper
import kotlin.system.measureTimeMillis

class TestActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "TestActivity"
    }

    init {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
        Shell.Config.verboseLogging(BuildConfig.DEBUG)
        Shell.Config.setTimeout(10)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.i(TAG, OverlayHelper.getColorList(this, false).toString())
            Log.i(TAG, "Is dark theme enabled : ${OverlayHelper.isDarkModeEnabled(this)}, Is light theme enabled : ${OverlayHelper.isLightModeEnabled(this)}")
            Log.i(TAG, "Enabled Accent : ${OverlayHelper.getAccentEnabled(this) ?: "null"}")

            var list : HashMap<Int, String>? = null
            val colorListTime = measureTimeMillis {
                list = OverlayHelper.getColorList(this, false)
            }
            Log.i(TAG, "getColorList take ${colorListTime}ms")


            var getAccentTime = measureTimeMillis {
                OverlayHelper.getAccentEnabled(this, list!!)
            }
            Log.i(TAG, "getAccent with list take ${getAccentTime}ms")

            getAccentTime = measureTimeMillis {
                OverlayHelper.getAccentEnabled(this)
            }
            Log.i(TAG, "getAccent without list take ${getAccentTime}ms")
        }

        set_darkTheme_button.setOnClickListener {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P && lineageos.os.Build.LINEAGE_VERSION.SDK_INT == lineageos.os.Build.LINEAGE_VERSION_CODES.ILAMA) {
                val s = StyleInterface.getInstance(this)
                s.setGlobalStyle(
                    StyleInterface.STYLE_GLOBAL_DARK,
                    s.accent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                OverlayHelper.setDarkMode(this, true)
            } else {
                Toast.makeText(this, R.string.unsupported_device, Toast.LENGTH_SHORT).show()
            }
        }

        set_lightTheme_button.setOnClickListener {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P && lineageos.os.Build.LINEAGE_VERSION.SDK_INT == lineageos.os.Build.LINEAGE_VERSION_CODES.ILAMA) {
                val s = StyleInterface.getInstance(this)
                s.setGlobalStyle(
                    StyleInterface.STYLE_GLOBAL_LIGHT,
                    s.accent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                OverlayHelper.setDarkMode(this, false)
            } else {
                Toast.makeText(this, R.string.unsupported_device, Toast.LENGTH_SHORT).show()
            }
        }

        open_paletteActivity_button.setOnClickListener {
            val myIntent = Intent(this, PaletteTestActivity::class.java)
            startActivity(myIntent)
        }
    }
}
