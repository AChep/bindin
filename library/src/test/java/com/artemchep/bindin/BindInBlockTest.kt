package com.artemchep.bindin

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test

@ExperimentalCoroutinesApi
class BindInBlockTest : BindInTest() {
    @Test
    fun `block should run immediately after each state change`() = runBlockingTest {
        val count = 20
        var total = 0
        bindBlock {
            total += 1
        }

        repeat(count) {
            // this should trigger the block immediately
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }
        Assert.assertEquals(count, total)
    }

    @Test
    fun `block should cancel after state changes to incompatible`() = runBlockingTest {
        var runBefore = false
        var runAfter = false
        bindBlock {
            runBefore = true
            delay(1L) // must be more then 0
            runAfter = true
        }

        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        Assert.assertTrue(runBefore)
        Assert.assertFalse(runAfter)
    }

    @Test
    fun `block binding can be canceled`() = runBlockingTest {
        val unregister = bindBlock {
            throw IllegalStateException()
        }
        unregister()

        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }
}