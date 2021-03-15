package com.artemchep.bindin

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test

@ExperimentalCoroutinesApi
class BindInFlowTest : BindInTest() {
    @Test
    fun `flow should collect immediately after each state change`() = runBlockingTest {
        val count = 20
        var total = 0
        bindIn(
            flow = repeatFlow(count),
        ) {
            total += 1
        }

        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        Assert.assertEquals(count, total)
    }

    @Test
    fun `flow should cancel after state changes to incompatible`() = runBlockingTest {
        val flow = MutableStateFlow(0)
        var total = 0
        bindIn(flow) {
            total += 1
        }

        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        flow.emit(1)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        flow.emit(2)
        Assert.assertEquals(2, total)
    }

    @Test
    fun `flow binding can be canceled`() = runBlockingTest {
        val flow = MutableStateFlow(0)
        val unregister = bindIn(flow) {
            throw IllegalStateException()
        }.unbind
        unregister()

        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    private fun repeatFlow(n: Int) = flowOf(*(0 until n).toList().toTypedArray())
}