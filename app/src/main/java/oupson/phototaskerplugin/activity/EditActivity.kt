package oupson.phototaskerplugin.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE
import com.twofortyfouram.spackle.bundle.BundleScrubber
import kotlinx.android.synthetic.main.activity_edit.*
import oupson.phototaskerplugin.BuildConfig
import oupson.phototaskerplugin.R
import oupson.phototaskerplugin.bundle.PluginBundleValues
import oupson.phototaskerplugin.fragment.EditFragment
import oupson.phototaskerplugin.fragment.edit.InfoFragment
import oupson.phototaskerplugin.fragment.edit.ThemeFragment
import oupson.phototaskerplugin.tasker.TaskerPlugin

class EditActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "EditActivity"
    }

    private var mIsCancelled = false

    val previousBundle : Bundle? by lazy {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "getIntent() : $intent")
        BundleScrubber.scrub(intent).also {
            if (BuildConfig.DEBUG)
                Log.v(TAG, "Successfully scrubbed intent : $it")
        }

        val previousBundle =intent.getBundleExtra(EXTRA_BUNDLE)
        BundleScrubber.scrub(previousBundle).also {
            if (BuildConfig.DEBUG)
                Log.v(TAG, "Successfully scrubbed intent : $it")
        }

        if (BuildConfig.DEBUG)
            Log.i(TAG, "Previous bundle : $previousBundle")
        previousBundle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        ArrayAdapter.createFromResource(
            this,
            R.array.edit_spinner_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            action_selector_spinner.adapter = adapter
        }

        action_selector_spinner.onItemSelectedListener =
            (object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val transaction = supportFragmentManager.beginTransaction()
                    when (position) {
                        0 -> if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) !is InfoFragment) {
                            transaction
                                .replace(
                                    R.id.fragmentContainer,
                                    InfoFragment.newInstance(previousBundle)
                                )
                                .commit()
                        }
                        1 -> if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) !is ThemeFragment) {
                            transaction
                                .replace(
                                    R.id.fragmentContainer,
                                    ThemeFragment.newInstance(previousBundle)
                                )
                                .commit()
                        }
                    }
                }
            })

        /*
         * To help the user keep context, the title shows the host's name and the subtitle
         * shows the plug-in's name.
         */
        var callingApplicationLabel: CharSequence? = null
        try {
            callingApplicationLabel = packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(
                    callingPackage!!,
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

        action_selector_spinner.setSelection(previousBundle?.getInt(PluginBundleValues.BUNDLE_EXTRA_INT_ACTION) ?: 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun finish() {
        if (!mIsCancelled) {
            mIsCancelled =
                (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? EditFragment)?.isCancelled()
                    ?: false
        }
        val resIntent = Intent()
        if (TaskerPlugin.Setting.hostSupportsSynchronousExecution(intent.extras)) {
            TaskerPlugin.Setting.requestTimeoutMS(
                resIntent,
                TaskerPlugin.Setting.REQUESTED_TIMEOUT_MS_NEVER
            )
        }

        (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? EditFragment)?.finish(
            intent,
            resIntent
        )

        setResult(if (mIsCancelled) Activity.RESULT_CANCELED else Activity.RESULT_OK, resIntent)
        super.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_discard_changes -> {
                mIsCancelled = true
                finish()
                true
            }
            else -> if (item != null) super.onOptionsItemSelected(item) else false
        }
    }
}