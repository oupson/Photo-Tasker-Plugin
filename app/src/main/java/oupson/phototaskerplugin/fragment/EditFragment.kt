package oupson.phototaskerplugin.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

abstract class EditFragment : Fragment() {
    abstract fun generateBundle() : Bundle?

    open fun isCancelled() : Boolean = false

    open fun onBackPressed(intent: Intent, resIntent: Intent) {}
}