package pl.bachorski.composewithflowapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "FLOW_APP"

class MainViewModel(
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    val countdownFlow = flow<Int> {
        val startingValue = 5
        var currentValue = startingValue

        emit(startingValue)
        while (currentValue > 0) {
            delay(1000L)
            currentValue--
            emit(currentValue)
        }
    }.flowOn(dispatchers.main)

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
//        collectionCollectLatest()
    }

    private fun collectFlow() {
        viewModelScope.launch(dispatchers.main) {
            countdownFlow.collect { time -> // collectLatest prints only 0
                delay(1500L)
                Log.v(TAG, time.toString())
            }
        }
    }

    private fun collectFlowAndFilter() {
        viewModelScope.launch(dispatchers.main) {
            countdownFlow
                .filter { time ->
                    time % 2 == 0
                }
                .map {
                    it.toString()
                }
                .onEach {
                    println("$TAG onEach: $it")
                }
                .collect { time ->
                    Log.v(TAG, time)
                }
        }
    }

    private fun countFlow() {
        viewModelScope.launch(dispatchers.main) {
            val count = countdownFlow
                .count { time ->
                    time % 2 == 0
                }
            println("$TAG count result is: $count")
        }
    }

    private fun reduceFlow() {
        viewModelScope.launch(dispatchers.main) {
            val result = countdownFlow
                .reduce { accumulator: Int, value: Int ->
                    accumulator + value
                }
            println("$TAG reduce result is: $result")
        }
    }

    private fun foldFlow() {
        viewModelScope.launch(dispatchers.main) {
            val result = countdownFlow
                .fold(100) { accumulator: Int, value: Int ->
                    accumulator + value
                }
            println("$TAG reduce result is: $result")
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
        }.flowOn(dispatchers.main)
        viewModelScope.launch(dispatchers.main) {
            flow1.flatMapConcat { value ->
                flow {
                    emit(value + 1)
                    delay(1000L)
                    emit(value + 2)
                }.flowOn(dispatchers.main)
            }.collect { value ->
                println("$TAG Result of FlatMapConcat: $value")
            }
        }
    }

    private fun flatMapConcatExampleFlows() { // waits until each flow to finishes and then starts next one
        val flow1 = (1..5).asFlow().flowOn(dispatchers.main)

        viewModelScope.launch(dispatchers.main) {
            flow1.flatMapConcat { id ->
                getRecipeById(id)
            }.collect { value ->
                println("$TAG Result of getting recipe by id: $value")
            }
        }
    }

    private fun getRecipeById(id: Int): Flow<String> {
        return flow {
            delay(1000L)
            emit("Recipe $id")
        }.flowOn(dispatchers.main)
    }

    private fun flatMapMergeExampleFlows() { // Like concat but parallel executin - all results in 1 second
        val flow1 = (1..5).asFlow().flowOn(dispatchers.main)

        viewModelScope.launch(dispatchers.main) {
            flow1.flatMapMerge { id ->
                getRecipeById(id)
            }.collect { value ->
                println("$TAG Result of getting recipe by id: $value")
            }
        }
    }

    private fun flatMapLatestExampleFlows() { // prints only result of last flow1 -> "Result of getting recipe by id: Recipe 5"
        val flow1 = (1..5).asFlow().flowOn(dispatchers.main)

        viewModelScope.launch(dispatchers.main) {
            flow1.flatMapLatest { id -> // getRecipeById is delayed so only the result of the last flow1 emission is shown
                getRecipeById(id)
            }.collect { value ->
                println("$TAG Result of getting recipe by id: $value")
            }
        }
    }

    private fun collectionDelayed() { // linear order of execution, collect slows down emissions
        val flow = dishesFlow()
        viewModelScope.launch(dispatchers.main) {
            flow.onEach {
                println("$TAG Current dish: $it")
            }
                .collect {
                    println("$TAG Eating: $it")
                    delay(1500L)
                    println("$TAG Finished: $it")
                }
        }
    }

    private fun collectionDelayedWithBuffer() { // linear order of execution, collect slows down emissions
        val flow = dishesFlow()
        viewModelScope.launch(dispatchers.main) {
            flow.onEach {
                println("$TAG Current dish: $it")
            }
                .buffer() // makes collect() run in different coroutine and does not wait for collect to finish
                .collect {
                    println("$TAG Eating: $it")
                    delay(1500L)
                    println("$TAG Finished: $it")
                }
        }
    }

    private fun collectionDelayedWithConflate() { // skips emissions if there are new ones
        val flow = dishesFlow()
        viewModelScope.launch(dispatchers.main) {
            flow.onEach {
                println("$TAG Current dish: $it")
            }
                .conflate() // skips original flow older emissions if there are new ones (in this case misses only eating dinner)
                .collect {
                    println("$TAG Eating: $it")
                    delay(1500L)
                    println("$TAG Finished: $it")
                }
        }
    }

    private fun collectionCollectLatest() { // skips emissions if there are new ones - only supper is finished
        val flow = dishesFlow()
        viewModelScope.launch(dispatchers.main) {
            flow.onEach {
                println("$TAG Current dish: $it")
            }
                .collectLatest {
                    println("$TAG Eating: $it")
                    delay(1500L)
                    println("$TAG Finished: $it")
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
    }.flowOn(dispatchers.main)
}

class MainViewModelFactory(private val dispatchers: DispatcherProvider) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(this.dispatchers) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}