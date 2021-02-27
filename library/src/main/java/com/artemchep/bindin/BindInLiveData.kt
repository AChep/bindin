package com.artemchep.bindin

import androidx.annotation.UiThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

@UiThread
fun <T> LifecycleOwner.bindIn(
    liveData: LiveData<T>,
    pipe: (T) -> Unit,
): InBinding<T> {
    val inBindingData = InBindingData<T>()
    val observer = Observer<T> { value ->
        if (!inBindingData.eq(value))
            pipe(value)
    }

    val inBinding = InBinding(
        lifecycleOwner = this,
        minimumLifecycleState = Lifecycle.State.STARTED,
        data = inBindingData,
    ) {
        liveData.removeObserver(observer)
    }

    liveData.observe(this, observer)

    return inBinding
}
