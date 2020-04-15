package oupson.phototaskerplugin.activity

import android.app.Activity
import android.content.pm.PackageManager
import android.opengl.Visibility
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.Nullable
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractAppCompatPluginActivity
import kotlinx.android.synthetic.main.activity_edit.*
import oupson.phototaskerplugin.R
import oupson.phototaskerplugin.bundle.PluginBundleValues
import oupson.phototaskerplugin.tasker.TaskerPlugin


class EditActivity : AbstractAppCompatPluginActivity() {
    companion object {
        private const val TAG = "EditActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        ArrayAdapter.createFromResource(
            this,
            R.array.edit_spinner_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            action_selector_spinner.adapter = adapter
        }



        action_selector_spinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        path_input_layout.visibility = View.VISIBLE
                        theme_image_path.visibility = View.GONE
                    }
                    1 -> {
                        path_input_layout.visibility = View.GONE
                        theme_image_path.visibility = View.VISIBLE
                    }
                }
            }
        })

        /*
         * To help the user keep context, the title shows the host's name and the subtitle
         * shows the plug-in's name.
         */
        /*
         * To help the user keep context, the title shows the host's name and the subtitle
         * shows the plug-in's name.
         */
        var callingApplicationLabel: CharSequence? = null
        try {
            callingApplicationLabel = packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(
                    callingPackage,
                    0
                )
            )
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Calling package couldn't be found%s", e) //$NON-NLS-1$
        }
        if (null != callingApplicationLabel) {
            title = callingApplicationLabel
        }

        supportActionBar?.setSubtitle(R.string.plugin_name)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onPostCreateWithPreviousResult(
        previousBundle: Bundle,
        previousBlurb: String
    ) {
        val message= PluginBundleValues.getPath(previousBundle)
        val a = PluginBundleValues.getAction(previousBundle)
        action_selector_spinner.setSelection(a)
        when (a) {
            0 ->  path_input_layout?.editText?.setText(message)
            1 -> theme_image_path?.editText?.setText(message)
        }

    }

    override fun isBundleValid(bundle: Bundle): Boolean {
        return PluginBundleValues.isBundleValid(bundle)
    }

    @Nullable
    override fun getResultBundle(): Bundle? {
        var result: Bundle? = null
        val message = when (action_selector_spinner.selectedItemPosition) {
            0 -> path_input_layout.editText?.text?.toString() ?: ""
            1 -> theme_image_path.editText?.text?.toString() ?: ""
            else -> ""
        }
        Log.v(TAG, "Path is $message")
        if (!TextUtils.isEmpty(message)) {
            result = PluginBundleValues.generateBundle(
                applicationContext,
                action_selector_spinner.selectedItemPosition,
                message
            )

            if (TaskerPlugin.Setting.hostSupportsOnFireVariableReplacement(this)) {
                TaskerPlugin.Setting.setVariableReplaceKeys(
                    result,
                    arrayOf(PluginBundleValues.BUNDLE_EXTRA_STRING_PATH)
                )
            }
        }
        return result
    }

    override fun getResultBlurb(bundle: Bundle): String {
        val message = PluginBundleValues.getPath(bundle) ?: ""
        val maxBlurbLength = resources.getInteger(
            R.integer.com_twofortyfouram_locale_sdk_client_maximum_blurb_length
        )
        return if (message.length > maxBlurbLength) {
            message.substring(0, maxBlurbLength)
        } else message
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            android.R.id.home -> {
                mIsCancelled = false
                val resIntent = intent

                if (TaskerPlugin.Setting.hostSupportsSynchronousExecution(intent.extras)) {
                    TaskerPlugin.Setting.requestTimeoutMS(resIntent, TaskerPlugin.Setting.REQUESTED_TIMEOUT_MS_NEVER)
                }

                if (TaskerPlugin.hostSupportsRelevantVariables(intent.extras) && action_selector_spinner.selectedItemPosition == 0) {
                    println("TRUE")
                    TaskerPlugin.addRelevantVariableList(resIntent, arrayOf(
                        "%vibrant\n Vibrant color\n",
                        "%vibranttext\n Vibrant text color\n",
                        "%darkvibrant\n Dark Vibrant color\n",
                        "%darkvibranttext\n Dark Vibrant text color\n",
                        "%lightvibrant\n Light Vibrant color\n",
                        "%lightvibranttext\n Light Vibrant text color\n",
                        "%muted\n Muted color\n",
                        "%mutedtext\n Muted Text color\n",
                        "%darkmuted\n Dark Muted color\n",
                        "%darkmutedtext\n Dark Muted text color\n",
                        "%lightmuted\n Light Muted text color\n",
                        "%lightmutedtext\n Light Muted text color\n",
                        "%metadata_lat\nLatitude\n",
                        "%metadata_long\nLongitude\n",
                        "%metadata_model\nModel\n",
                        "%metadata_artist\nArtist\n",
                        "%metadata_date\nDate"
                    ))
                }

                setResult(Activity.RESULT_OK, resIntent)
                finish()
                true
            }
            R.id.menu_discard_changes -> {
                mIsCancelled = true
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
