package pl.bachorski.composewithflowapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModelSharedFlow(
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _sharedFlow =
        MutableSharedFlow<Int>(replay = 0) // hot flow, replay caches last events
    val sharedFlow = _sharedFlow.asSharedFlow()

    init {
        Log.v(TAG_SHARED_FLOW, "MainViewModelSharedFlow init")
//        squaredNumber(3) // this works before subscription because we use replay above

        viewModelScope.launch(dispatchers.main) {
            sharedFlow.collect { // not collectLatest because we want to have all one time events delivered
                delay(2000L)
                Log.v(TAG_SHARED_FLOW, "SharedFlow _1_ collect: $it")
            }
        }
        viewModelScope.launch(dispatchers.main) {
            sharedFlow.collect { // not collectLatest because we want to have all one time events delivered
                delay(3000L)
                Log.v(TAG_SHARED_FLOW, "SharedFlow _2_ collect: $it")
            }
        }
        squaredNumber(3)
    }

    fun squaredNumber(number: Int) {
        Log.v(TAG_SHARED_FLOW, "squared number before operation: $number")
        viewModelScope.launch(dispatchers.main) {
            _sharedFlow.emit(number * number)
        }
    }
}

class MainViewModelSharedFlowFactory(private val dispatchers: DispatcherProvider) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModelSharedFlow::class.java)) {
            MainViewModelSharedFlow(this.dispatchers) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}