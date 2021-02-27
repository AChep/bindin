package com.artemchep.bindin

class InBinding<T>(
    val data: InBindingData<T>,
    val unbind: () -> Unit,
)

class InBindingData<T> {
    var outValue: Any? = Any()

    fun eq(value: T) = this.outValue == value
}
