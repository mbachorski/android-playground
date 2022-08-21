package pl.bachorski.composewithflowapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.bachorski.composewithflowapp.ui.theme.AndroidplaygroundTheme

private const val TAG = "FLOW_APP_ACTIVITY"
const val TAG_SHARED_FLOW = "FLOW_APP_SHARED"

fun <T> ComponentActivity.collectLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

class MainComposeWithFlowActivity : ComponentActivity() {

    // for xml example, this is not how its used with compose
    private val viewModelWithStateFlow: MainViewModelStateFlow by viewModels()
    private val viewModelWithSharedFlow: MainViewModelSharedFlow by lazy {
        ViewModelProvider(
            this,
            MainViewModelSharedFlowFactory(DefaultDispatchers())
        )[MainViewModelSharedFlow::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // stateflow examples for xml
        stateFlowXmlFullExample()
        stateFlowXmlWithExtensionExample()

        // compose
        setContent {
            AndroidplaygroundTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Log.v(TAG, "Surface composing..")
                    val viewModel =
                        viewModel<MainViewModel>(factory = MainViewModelFactory(DefaultDispatchers()))
                    val time = viewModel.countdownFlow.collectAsState(initial = 10)
                    FlowResult(text = time.value.toString())

                    StateFlowResult()

                    // shared flow - simulate one time event in compose
                    LaunchedEffect(key1 = true) {
                        viewModelWithSharedFlow.sharedFlow.collect { number ->
                            Log.v(
                                TAG_SHARED_FLOW,
                                "Consuming shared flow event in compose $number"
                            )
                        }
                    }
                }
            }
        }
        viewModelWithSharedFlow.squaredNumber(10)
    }

    private fun stateFlowXmlWithExtensionExample() {
        collectLatestLifecycleFlow(viewModelWithStateFlow.stateFlow) { number ->
            Log.v(TAG, "Consuming StateFlow in Activity with extension helper $number")
            // use number on your xml binding
        }
    }

    // compare to fun StateFlowResult in this file -> in compose it is much cleaner
    // compare to stateFlowXmlWithExtensionExample()
    private fun stateFlowXmlFullExample() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelWithStateFlow.stateFlow.collectLatest { number ->
                    Log.v(TAG, "Consuming StateFlow in Activity $number")
                    // use number on your xml binding
                }
            }
        }
    }
}

@Composable
fun FlowResult(text: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = text,
            fontSize = 30.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun StateFlowResult() {
    val viewModelStateFlow = viewModel<MainViewModelStateFlow>()
    val state =
        viewModelStateFlow.stateFlow.collectAsState(initial = 0) // compose has state so state flow is better for xml

    Box(modifier = Modifier.fillMaxSize()) {
        Button(onClick = { viewModelStateFlow.incrementCounter() }) {
            Text(text = "Counter: ${state.value}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AndroidplaygroundTheme {
        FlowResult(text = "10")
    }
}