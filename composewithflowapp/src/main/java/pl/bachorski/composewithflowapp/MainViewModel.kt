package pl.bachorski.composewithflowapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "FLOW_APP"

class MainViewModel : ViewModel() {

    val countdownFlow = flow<Int> {
        val startingValue = 10
        var currentValue = startingValue

        emit(startingValue)
        while (currentValue > 0) {
            delay(1000L)
            currentValue--
            emit(currentValue)
        }
    }

    init {
//        collectFlow()
//        collectFlowAndFilter()
//        countFlow()
//        reduceFlow()
//        foldFlow()
//        flattenFlows()
//        flatMapConcatExampleFlows()
//        flatMapMergeExampleFlows()
//        flatMapLatestExampleFlows()
//        collectionDelayed()
//        collectionDelayedWithBuffer()
//        collectionDelayedWithConflate()
        collectionCollectLatest()
    }

    private fun collectFlow() {
        viewModelScope.launch {
            countdownFlow.collect { time -> // collectLatest prints only 0
                delay(1500L)
                Log.v(TAG, time.toString())
            }
        }
    }

    private fun collectFlowAndFilter() {
        viewModelScope.launch {
            countdownFlow
                .filter { time ->
                    time % 2 == 0
                }
                .map {
                    it.toString()
                }
                .onEach {
                    Log.v(TAG, "onEach: $it")
                }
                .collect { time ->
                    Log.v(TAG, time)
                }
        }
    }

    private fun countFlow() {
        viewModelScope.launch {
            val count = countdownFlow
                .count { time ->
                    time % 2 == 0
                }
            Log.v(TAG, "count result is: $count")
        }
    }

    private fun reduceFlow() {
        viewModelScope.launch {
            val result = countdownFlow
                .reduce { accumulator: Int, value: Int ->
                    accumulator + value
                }
            Log.v(TAG, "reduce result is: $result")
        }
    }

    private fun foldFlow() {
        viewModelScope.launch {
            val result = countdownFlow
                .fold(100) { accumulator: Int, value: Int ->
                    accumulator + value
                }
            Log.v(TAG, "reduce result is: $result")
        }
    }

    private fun flattenFlows() {
        val flow1 = flow<Int> {
            emit(1)
            delay(500L)
            emit(2)
            delay(500L)
            emit(3)
            delay(500L)
            emit(4)
        }
        viewModelScope.launch {
            flow1.flatMapConcat { value ->
                flow {
                    emit(value + 1)
                    delay(1000L)
                    emit(value + 2)
                }
            }.collect { value ->
                Log.v(TAG, "Result of FlatMapConcat: $value")
            }
        }
    }

    private fun flatMapConcatExampleFlows() { // waits until each flow to finishes and then starts next one
        val flow1 = (1..5).asFlow()

        viewModelScope.launch {
            flow1.flatMapConcat { id ->
                getRecipeById(id)
            }.collect { value ->
                Log.v(TAG, "Result of getting recipe by id: $value")
            }
        }
    }

    private fun getRecipeById(id: Int): Flow<String> {
        return flow {
            delay(1000L)
            emit("Recipe $id")
        }
    }

    private fun flatMapMergeExampleFlows() { // Like concat but parallel executin - all results in 1 second
        val flow1 = (1..5).asFlow()

        viewModelScope.launch {
            flow1.flatMapMerge { id ->
                getRecipeById(id)
            }.collect { value ->
                Log.v(TAG, "Result of getting recipe by id: $value")
            }
        }
    }

    private fun flatMapLatestExampleFlows() { // prints only result of last flow1 -> "Result of getting recipe by id: Recipe 5"
        val flow1 = (1..5).asFlow()

        viewModelScope.launch {
            flow1.flatMapLatest { id -> // getRecipeById is delayed so only the result of the last flow1 emission is shown
                getRecipeById(id)
            }.collect { value ->
                Log.v(TAG, "Result of getting recipe by id: $value")
            }
        }
    }

    private fun collectionDelayed() { // linear order of execution, collect slows down emissions
        val flow = dishesFlow()
        viewModelScope.launch {
            flow.onEach {
                Log.v(TAG, "Current dish: $it")
            }
                .collect {
                    Log.v(TAG, "Eating: $it")
                    delay(1500L)
                    Log.v(TAG, "Finished: $it")
                }
        }
    }

    private fun collectionDelayedWithBuffer() { // linear order of execution, collect slows down emissions
        val flow = dishesFlow()
        viewModelScope.launch {
            flow.onEach {
                Log.v(TAG, "Current dish: $it")
            }
                .buffer() // makes collect() run in different coroutine and does not wait for collect to finish
                .collect {
                    Log.v(TAG, "Eating: $it")
                    delay(1500L)
                    Log.v(TAG, "Finished: $it")
                }
        }
    }

    private fun collectionDelayedWithConflate() { // skips emissions if there are new ones
        val flow = dishesFlow()
        viewModelScope.launch {
            flow.onEach {
                Log.v(TAG, "Current dish: $it")
            }
                .conflate() // skips original flow older emissions if there are new ones (in this case misses only eating dinner)
                .collect {
                    Log.v(TAG, "Eating: $it")
                    delay(1500L)
                    Log.v(TAG, "Finished: $it")
                }
        }
    }

    private fun collectionCollectLatest() { // skips emissions if there are new ones - only supper is finished
        val flow = dishesFlow()
        viewModelScope.launch {
            flow.onEach {
                Log.v(TAG, "Current dish: $it")
            }
                .collectLatest {
                    Log.v(TAG, "Eating: $it")
                    delay(1500L)
                    Log.v(TAG, "Finished: $it")
                }
        }
    }

    private fun dishesFlow() = flow {
        delay(250L)
        emit("Breakfast")
        delay(1000L)
        emit("Dinner")
        delay(200L)
        emit("Supper")
    }
}