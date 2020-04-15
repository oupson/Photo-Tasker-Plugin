package oupson.phototaskerplugin.bundle

import android.content.Context
import android.os.Bundle
import com.twofortyfouram.assertion.Assertions
import com.twofortyfouram.assertion.BundleAssertions
import com.twofortyfouram.log.Lumberjack
import com.twofortyfouram.spackle.AppBuildInfo
import net.jcip.annotations.ThreadSafe
import oupson.phototaskerplugin.BuildConfig


/**
 * Manages the [EXTRA_BUNDLE][com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE] for this
 * plug-in.
 */
@ThreadSafe
class PluginBundleValues private constructor() {
    companion object {
        /**
         * Type: `String`.
         *
         *
         * Path of image
         */
        const val BUNDLE_EXTRA_STRING_PATH =
            "${BuildConfig.APPLICATION_ID}.extra.STRING_PATH" //$NON-NLS-1$
        /**
         * Type: `int`.
         *
         *
         * versionCode of the plug-in that saved the Bundle.
         */
        /*
         * This extra is not strictly required, however it makes backward and forward compatibility
         * significantly easier. For example, suppose a bug is found in how some version of the plug-in
         * stored its Bundle. By having the version, the plug-in can better detect when such bugs occur.
         */
        @Suppress("MemberVisibilityCanBePrivate")
        const val BUNDLE_EXTRA_INT_VERSION_CODE =
            "${BuildConfig.APPLICATION_ID}.extra.INT_VERSION_CODE" //$NON-NLS-1$

        const val BUNDLE_EXTRA_INT_ACTION = "${BuildConfig.APPLICATION_ID}.extra.ACTION"

        const val ACTION_GET_INFO = 0
        const val ACTION_SET_THEME = 1

        /**
         * Method to verify the content of the bundle are correct.
         *
         *
         * This method will not mutate `bundle`.
         *
         * @param bundle bundle to verify. May be null, which will always return false.
         * @return true if the Bundle is valid, false if the bundle is invalid.
         */
        fun isBundleValid(bundle: Bundle?): Boolean {
            if (null == bundle) {
                return false
            }
            try {
                BundleAssertions.assertHasInt(
                    bundle,
                    BUNDLE_EXTRA_INT_ACTION
                )
                BundleAssertions.assertHasInt(
                    bundle,
                    BUNDLE_EXTRA_INT_VERSION_CODE
                )
                //BundleAssertions.assertKeyCount(bundle, 2)
            } catch (e: AssertionError) {
                Lumberjack.e("Bundle failed verification%s", e) //$NON-NLS-1$
                return false
            }
            return true
        }

        /**
         * @param context Application context.
         * @param path Path of image
         * @return A plug-in bundle.
         */
        fun generateBundle(
            context: Context,
            action : Int,
            path: String? = null
        ): Bundle {
            Assertions.assertNotNull(context, "context") //$NON-NLS-1$
            if (path != null)
                Assertions.assertNotEmpty(path, "path") //$NON-NLS-1$
            val result = Bundle()
            result.putInt(
                BUNDLE_EXTRA_INT_VERSION_CODE,
                AppBuildInfo.getVersionCode(context)
            )
            result.putInt(BUNDLE_EXTRA_INT_ACTION, action)
            if (path != null)
                result.putString(BUNDLE_EXTRA_STRING_PATH, path)
            return result
        }

        /**
         * @param bundle A valid plug-in bundle.
         * @return The message inside the plug-in bundle.
         */
        fun getPath(bundle: Bundle): String? {
            return bundle.getString(BUNDLE_EXTRA_STRING_PATH)
        }

        fun getAction(bundle: Bundle) = bundle.getInt(BUNDLE_EXTRA_INT_ACTION)
    }

    /**
     * Private constructor prevents instantiation
     *
     * @throws java.lang.UnsupportedOperationException because this class cannot be instantiated.
     */
    init {
        throw UnsupportedOperationException("This class is non-instantiable") //$NON-NLS-1$
    }
}