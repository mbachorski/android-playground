package pl.bachorski.composewithflowapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModelSharedFlow : ViewModel() {

    private val _sharedFlow =
        MutableSharedFlow<Int>(replay = 0) // hot flow, replay caches last events
    val sharedFlow = _sharedFlow.asSharedFlow()

    init {
        Log.v(TAG_SHARED_FLOW, "MainViewModelSharedFlow init")
//        squaredNumber(3) // this works before subscription because we use replay above

        viewModelScope.launch {
            sharedFlow.collect { // not collectLatest because we want to have all one time events delivered
                delay(2000L)
                Log.v(TAG_SHARED_FLOW, "SharedFlow _1_ collect: $it")
            }
        }
        viewModelScope.launch {
            sharedFlow.collect { // not collectLatest because we want to have all one time events delivered
                delay(3000L)
                Log.v(TAG_SHARED_FLOW, "SharedFlow _2_ collect: $it")
            }
        }
        squaredNumber(3)
    }

    fun squaredNumber(number: Int) {
        Log.v(TAG_SHARED_FLOW, "squared number before operation: $number")
        viewModelScope.launch {
            _sharedFlow.emit(number * number)
        }
    }
}