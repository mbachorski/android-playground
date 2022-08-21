package pl.bachorski.composewithflowapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "FLOW_APP"

class MainViewModelStateFlow : ViewModel() {

    private val _stateFlow = MutableStateFlow(0) // hot flow
    val stateFlow = _stateFlow.asStateFlow()

    fun incrementCounter() {
        _stateFlow.value += 1
    }
}