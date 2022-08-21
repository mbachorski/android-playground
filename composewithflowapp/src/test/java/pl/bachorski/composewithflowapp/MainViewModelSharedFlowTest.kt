package pl.bachorski.composewithflowapp

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

import org.junit.Before
import org.junit.Test

class MainViewModelSharedFlowTest {

    private lateinit var viewModel: MainViewModelSharedFlow
    private lateinit var testDispatchers: TestDispatchers

    @Before
    fun setUp() {
        testDispatchers = TestDispatchers()
        viewModel = MainViewModelSharedFlow(testDispatchers)
    }

    @Test
    fun `squaredNumber, number properly squared`() = runBlocking {
        val job = launch {
            viewModel.sharedFlow.test {
                val emission = awaitItem()
                assertThat(emission).isEqualTo(9)
                cancelAndConsumeRemainingEvents()
            }
        }
        viewModel.squaredNumber(3)
        job.join()
        job.cancel()
    }
}