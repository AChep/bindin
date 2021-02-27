package com.artemchep.bindin

import androidx.annotation.UiThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStateAtLeast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private val DEFAULT_MIN_LIFECYCLE_STATE = Lifecycle.State.STARTED

@UiThread
fun <T> LifecycleOwner.bindIn(
    flow: Flow<T>,
    minimumLifecycleState: Lifecycle.State = DEFAULT_MIN_LIFECYCLE_STATE,
    pipe: (T) -> Unit,
): InBinding<T> = _bindIn(
    flow = flow,
    minimumLifecycleState = minimumLifecycleState,
    pipe = { value ->
        pipe(value)
    },
)

@UiThread
fun <T> LifecycleOwner.bindInSuspending(
    flow: Flow<T>,
    minimumLifecycleState: Lifecycle.State = DEFAULT_MIN_LIFECYCLE_STATE,
    pipe: suspend (T) -> Unit,
): InBinding<T> = _bindIn(
    flow = flow,
    minimumLifecycleState = minimumLifecycleState,
    pipe = { value ->
        lifecycle.whenStateAtLeast(minimumLifecycleState) {
            pipe(value)
        }
    },
)

@Suppress("FunctionName")
@UiThread
private inline fun <T> LifecycleOwner._bindIn(
    flow: Flow<T>,
    minimumLifecycleState: Lifecycle.State,
    crossinline pipe: suspend (T) -> Unit,
): InBinding<T> {
    val inBindingData = InBindingData<T>()
    val registration = bindBlock(
        minimumLifecycleState = minimumLifecycleState,
        block = {
            flow.collect {
                val isActive = lifecycle.currentState >= minimumLifecycleState
                if (isActive) {
                    if (!inBindingData.eq(it))
                        pipe(it)
                }
            }
        },
    )
    return InBinding(
        lifecycleOwner = this,
        minimumLifecycleState = minimumLifecycleState,
        data = inBindingData,
        unbind = registration,
    )
}

/**
 * The block will bee executed on the [Dispatchers.Main.immediate] when the state changes from
 * unsupported to [minimumLifecycleState].
 */
@UiThread
fun LifecycleOwner.bindBlock(
    minimumLifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend () -> Unit,
): () -> Unit {
    var wasActive = false
    var job: Job? = null

    val observer = LifecycleEventObserver { source, _ ->
        val isActive = source.lifecycle.currentState >= minimumLifecycleState
        if (isActive) {
            // Check if previous job has been completed (or canceled) and
            // start a new one.
            if (!wasActive) {
                job?.cancel()
                job = lifecycleScope.launch {
                    block()
                }
            }
        } else {
            job?.cancel()
            job = null
        }
        wasActive = isActive
    }

    val registration = {
        lifecycle.removeObserver(observer)
        job?.cancel()
        job = null
    }

    lifecycle.addObserver(observer)
    // Just like LiveData you should bind only once when the lifecycle is
    // created!

    return registration
}
