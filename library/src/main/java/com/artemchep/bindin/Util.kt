package com.artemchep.bindin

import android.os.Looper

internal val isMainThread get() = Looper.myLooper() == Looper.getMainLooper()
