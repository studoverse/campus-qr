package com.studo.campusqr.campusqr

import android.os.Handler
import android.os.Looper

fun runAsync(action: () -> Unit) = Thread(Runnable(action)).start()

fun runOnUiThread(action: () -> Unit) = Handler(Looper.getMainLooper()).post(action)