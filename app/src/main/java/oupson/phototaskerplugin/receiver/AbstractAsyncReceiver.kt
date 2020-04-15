package oupson.phototaskerplugin.receiver

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.os.Build.VERSION_CODES
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Pair
import com.twofortyfouram.assertion.Assertions
import com.twofortyfouram.spackle.AndroidSdkVersion
import com.twofortyfouram.spackle.ThreadUtil
import com.twofortyfouram.spackle.ThreadUtil.ThreadPriority
import net.jcip.annotations.ThreadSafe


/**
 * Simplifies asynchronous broadcast handling. Subclasses call
 * [.goAsyncWithCallback], and the abstract class takes
 * care of executing the callback on a background thread.
 */
@ThreadSafe
/* package */
abstract class AbstractAsyncReceiver : BroadcastReceiver() {
    /*
     * This method is package visible rather than protected so that it will be
     * obfuscated by ProGuard.
     *
     * @param callback Callback to execute on a background thread.
     * @param isOrdered Indicates whether an ordered broadcast is being processed.
     */
    @TargetApi(VERSION_CODES.HONEYCOMB) /* package */    fun goAsyncWithCallback(
        callback: AsyncCallback,
        isOrdered: Boolean
    ) {
        Assertions.assertNotNull(callback, "callback") //$NON-NLS-1$
        val pendingResult = goAsync()
            ?: throw AssertionError(
                "PendingResult was null.  Was goAsync() called previously?"
            ) //$NON-NLS-1$
        val handlerCallback: Handler.Callback =
            AsyncHandlerCallback()
        val thread = ThreadUtil.newHandlerThread(
            javaClass.name,
            ThreadPriority.BACKGROUND
        )
        val handler = Handler(thread.looper, handlerCallback)
        val obj: Any = Pair(
            pendingResult,
            callback
        )
        val isOrderedInt = if (isOrdered) 1 else 0
        val msg = handler
            .obtainMessage(
                AsyncHandlerCallback.MESSAGE_HANDLE_CALLBACK,
                isOrderedInt,
                0,
                obj
            )
        val isMessageSent = handler.sendMessage(msg)
        if (!isMessageSent) {
            throw AssertionError()
        }
    }

    @TargetApi(VERSION_CODES.HONEYCOMB)
    private class AsyncHandlerCallback : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            Assertions.assertNotNull(msg, "msg") //$NON-NLS-1$
            when (msg.what) {
                MESSAGE_HANDLE_CALLBACK -> {
                    Assertions.assertNotNull(msg.obj, "msg.obj") //$NON-NLS-1$
                    Assertions.assertInRangeInclusive(msg.arg1, 0, 1, "msg.arg1") //$NON-NLS-1$
                    val pair =
                        castObj(msg.obj)
                    val isOrdered = 0 != msg.arg1
                    val pendingResult = pair.first
                    val asyncCallback = pair.second
                    try {
                        val resultCode = asyncCallback.runAsync()
                        if (isOrdered) {
                            pendingResult.resultCode = resultCode
                        }
                    } finally {
                        pendingResult.finish()
                    }
                    quit()
                }
            }
            return true
        }

        companion object {
            /**
             * Message MUST contain a `Pair<PendingResult, AsyncCallback>` as the `msg.obj`
             * and a boolean encoded in the `msg.arg1` to indicate whether the broadcast was
             * ordered.
             */
            const val MESSAGE_HANDLE_CALLBACK = 0

            private fun castObj(o: Any): Pair<PendingResult, AsyncCallback> {
                return o as Pair<PendingResult, AsyncCallback>
            }

            private fun quit() {
                if (AndroidSdkVersion.isAtLeastSdk(VERSION_CODES.JELLY_BEAN_MR2)) {
                    quitJellybeanMr2()
                } else {
                    Looper.myLooper()?.quit()
                }
            }

            @TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
            private fun quitJellybeanMr2() {
                Looper.myLooper()?.quitSafely()
            }
        }
    }

    /* package */
    interface AsyncCallback {
        /**
         * @return The result code to be set if this is an ordered broadcast.
         */
        fun runAsync(): Int
    }
}