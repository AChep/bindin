package com.artemchep.bindin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

fun <T> InBinding<T>.bindOut(
    observe: (callback: (T) -> Unit) -> () -> Unit,
    /** A pipe to pass data to... */
    pipe: (T) -> Unit,
): InBinding<T> {
    val unbindObserve = observe { value ->
        if (data.eq(value)) {
            return@observe
        }

        data.outValue = value
        pipe(value)
    }
    return copy(
        unbind = {
            unbind()
            unbindObserve()
        }
    )
}

fun <T> InBinding<T>.bindOut(
    flow: Flow<T>,
    /** A pipe to pass data to... */
    pipe: (T) -> Unit,
): InBinding<T> {
    val unbindBlock = lifecycleOwner.bindBlock(minimumLifecycleState = minimumLifecycleState) {
        flow.collect {
            _pipe(it, pipe)
        }
    }
    return copy(
        unbind = {
            unbind()
            unbindBlock()
        }
    )
}

@Suppress("FunctionName")
private fun <T> InBinding<T>._pipe(
    value: T,
    /** A pipe to pass data to... */
    pipe: (T) -> Unit,
) {
    if (data.eq(value)) {
        return
    }

    data.outValue = value
    pipe(value)
}
