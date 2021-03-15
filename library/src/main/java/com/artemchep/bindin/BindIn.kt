package com.artemchep.bindin

import androidx.annotation.UiThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStateAtLeast
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private val DEFAULT_MIN_LIFECYCLE_STATE = Lifecycle.State.STARTED

/**
 * Collects the flow each time when and only when the lifecycle
 * is in the [minimumLifecycleState]. To undo the binding,
 * invoke the [InBinding.unbind] lambda from the UI thread.
 *
 * @see bindIn
 * @see bindOut
 */
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

/**
 * The suspending [pipe] is run on the `PausingDispatcher` and hence guarantees
 * that the lifecycle is in the required state. To undo the binding,
 * invoke the [InBinding.unbind] lambda from the UI thread.
 *
 * Make sure you know the limitations:
 * ```
 * viewLifecycleOwner.bindInSuspending(flow) {
 *   try {
 *     delay(100L)
 *     // state >= Lifecycle.State.STARTED is guaranteed!
 *     println("Hello world")
 *   } catch (e: IOException) {
 *     // state >= Lifecycle.State.STARTED is guaranteed!
 *   } catch (e: Throwable) {
 *     // state >= Lifecycle.State.STARTED is not guaranteed!
 *     // Check kotlin.coroutines.cancellation / CancellationException
 *   } finally {
 *     // state >= Lifecycle.State.STARTED is not guaranteed!
 *   }
 * }
 * ```
 *
 * @see bindIn
 * @see bindOut
 * @see bindBlock
 */
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
 * The block will be executed on the `PausingDispatcher` when the state changes from
 * unsupported to [minimumLifecycleState]. To undo the binding,
 * invoke the result of this function from the UI thread.
 *
 * Make sure you know the limitations:
 * ```
 * viewLifecycleOwner.bindBlock {
 *   try {
 *     delay(100L)
 *     // state >= Lifecycle.State.STARTED is guaranteed!
 *     println("Hello world")
 *   } catch (e: IOException) {
 *     // state >= Lifecycle.State.STARTED is guaranteed!
 *   } catch (e: Throwable) {
 *     // state >= Lifecycle.State.STARTED is not guaranteed!
 *     // Check kotlin.coroutines.cancellation / CancellationException
 *   } finally {
 *     // state >= Lifecycle.State.STARTED is not guaranteed!
 *   }
 * }
 * ```
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
