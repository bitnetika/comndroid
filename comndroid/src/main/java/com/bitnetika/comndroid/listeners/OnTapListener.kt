package com.bitnetika.comndroid.listeners

import android.os.SystemClock
import android.view.View

class OnTapListener(
    private val interval: Long,
    private val onTap: (view: View?) -> Unit
) : View.OnClickListener {

    private var tappedAt: Long = 0

    override fun onClick(view: View?) {
        if (SystemClock.elapsedRealtime() - tappedAt < interval) {
            return
        }
        tappedAt = SystemClock.elapsedRealtime()
        onTap.invoke(view)
    }
}
