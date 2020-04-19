package oupson.phototaskerplugin.fragment.edit

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textfield.TextInputLayout
import oupson.phototaskerplugin.BuildConfig

import oupson.phototaskerplugin.R
import oupson.phototaskerplugin.bundle.PluginBundleValues
import oupson.phototaskerplugin.fragment.EditFragment
import oupson.phototaskerplugin.tasker.TaskerPlugin

class InfoFragment : EditFragment() {
    companion object {
        private const val TAG = "InfoFragment"

        @JvmStatic
        fun newInstance(previousBundle: Bundle?) = InfoFragment().apply {
            arguments = previousBundle
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info, container, false)
        if (arguments != null) {
            val path = PluginBundleValues.getPath(arguments!!)
            view.findViewById<TextInputLayout?>(R.id.path_input_layout)?.editText?.setText(path)
        }
        return view
    }

    override fun generateBundle(): Bundle? {
        var result: Bundle? = Bundle()
        val path = view?.findViewById<TextInputLayout?>(R.id.path_input_layout)?.editText?.text
        if (BuildConfig.DEBUG)
            Log.v(TAG, "Path is $path")
        if (!TextUtils.isEmpty(path) && context?.applicationContext != null) {
            result = PluginBundleValues.generateBundle(
                context?.applicationContext!!,
                PluginBundleValues.ACTION_GET_INFO,
                path?.toString()
            )

            if (TaskerPlugin.Setting.hostSupportsOnFireVariableReplacement(activity)) {
                TaskerPlugin.Setting.setVariableReplaceKeys(
                    result,
                    arrayOf(PluginBundleValues.BUNDLE_EXTRA_STRING_PATH)
                )
            }
        }
        return result
    }

    override fun onBackPressed(intent: Intent, resIntent: Intent) {
        if (TaskerPlugin.hostSupportsRelevantVariables(intent.extras)) {
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
    }
}
