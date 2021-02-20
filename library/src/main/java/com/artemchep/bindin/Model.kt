package com.artemchep.bindin

private typealias Registration = () -> Unit

class InBinding<T>(
    val data: InBindingData<T>,
    private val registration: Registration,
) : Registration by registration

class InBindingData<T> {
    var outValue: Any? = Any()

    fun eq(value: T) = this.outValue == value
}
