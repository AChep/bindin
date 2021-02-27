package com.artemchep.bindin

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

data class InBinding<T>(
    val lifecycleOwner: LifecycleOwner,
    val minimumLifecycleState: Lifecycle.State,
    val data: InBindingData<T>,
    val unbind: () -> Unit,
)

class InBindingData<T> {
    var outValue: Any? = Any()

    fun eq(value: T) = this.outValue == value
}
