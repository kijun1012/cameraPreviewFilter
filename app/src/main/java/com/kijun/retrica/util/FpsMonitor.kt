package com.kijun.retrica.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import com.kijun.retrica.R

class FpsMonitor(
    var textView: TextView? = null
) {
    private var frames = 0
    private var lastMs = android.os.SystemClock.uptimeMillis()

    private val mainHandler = Handler(Looper.getMainLooper())

    fun tickFps() {
        frames++
        val now = android.os.SystemClock.uptimeMillis()
        val dt = now - lastMs
        if (dt >= 1000) {
            val fps = (frames * 1000 / dt)   // 정수 fps
            Log.d("Fps Monitor", "${fps}fps")

            frames = 0
            lastMs = now

            val tv = textView
            if (tv != null) {
                mainHandler.post { tv.text = tv.context.getString(R.string.fps, fps) }
            }
        }
    }
}