package com.artemchep.bindin

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

data class InBinding<T>(
    val lifecycleOwner: LifecycleOwner,
    val minimumLifecycleState: Lifecycle.State,
    val data: InBindingData<T>,
    /**
     * Invoke this function from the UI thread to cancel
     * the binding.
     */
    val unbind: () -> Unit,
)

class InBindingData<T> {
    companion object {
        val nothing = Any()
    }

    /**
     * Latest emitted by the [bindOut] value, or
     * [nothing] as an initial value.
     */
    var outValue: Any? = nothing

    /**
     * Returns `true` if the [value] is equal to
     * the [outValue], `false` otherwise.
     */
    fun eq(value: T) = this.outValue == value
}
