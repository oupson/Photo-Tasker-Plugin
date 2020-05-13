package oupson.phototaskerplugin.fragment

import android.content.Intent
import androidx.fragment.app.Fragment

abstract class EditFragment : Fragment() {
    open fun isCancelled() : Boolean = false

    open fun finish(intent: Intent, resIntent: Intent) {}
}