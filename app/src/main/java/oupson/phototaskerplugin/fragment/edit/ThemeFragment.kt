package oupson.phototaskerplugin.fragment.edit

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textfield.TextInputLayout
import oupson.phototaskerplugin.R
import oupson.phototaskerplugin.bundle.PluginBundleValues
import oupson.phototaskerplugin.fragment.EditFragment
import oupson.phototaskerplugin.tasker.TaskerPlugin

class ThemeFragment : EditFragment() {
    companion object {
        private const val TAG = "ThemeFragment"
        @JvmStatic
        fun newInstance(previousBundle: Bundle?) = ThemeFragment().apply {
            arguments = previousBundle
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_theme, container, false)
        if (arguments != null) {
            val path = PluginBundleValues.getPath(arguments!!)
            view.findViewById<TextInputLayout?>(R.id.theme_image_path)?.editText?.setText(path)
        }
        return view
    }

    override fun generateBundle(): Bundle? {
        var result: Bundle? = Bundle()
        val path = view?.findViewById<TextInputLayout?>(R.id.theme_image_path)?.editText?.text
        Log.v(TAG, "Path is $path")
        if (!TextUtils.isEmpty(path) && context?.applicationContext != null) {
            result = PluginBundleValues.generateBundle(
                context?.applicationContext!!,
                PluginBundleValues.ACTION_SET_THEME,
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
}
