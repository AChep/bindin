package com.artemchep.bindin

fun <T> InBinding<T>.bindOut(
    observe: (callback: (T) -> Unit) -> Unit,
    /** A pipe to pass data to... */
    pipe: (T) -> Unit,
) {
    observe { value ->
        if (data.eq(value)) {
            return@observe
        }

        data.outValue = value
        pipe(value)
    }
}
