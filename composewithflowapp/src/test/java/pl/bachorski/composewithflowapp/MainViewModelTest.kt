package pl.bachorski.composewithflowapp


import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var testDispatchers: TestDispatchers

    @Before
    fun setUp() {
        testDispatchers = TestDispatchers()
        viewModel = MainViewModel(testDispatchers)
    }

    @Test
    fun `countDownFlow, properly counts donw from 5 to 0`() = runBlocking {
        viewModel.countdownFlow.test {
            for (i in 5 downTo 0) {
                testDispatchers.testDispatcher.advanceTimeBy(1000L)
                val emission = awaitItem()
                println("Awaited item is: $emission")
                assertThat(emission).isEqualTo(i)
            }
            cancelAndConsumeRemainingEvents()
        }
    }
}