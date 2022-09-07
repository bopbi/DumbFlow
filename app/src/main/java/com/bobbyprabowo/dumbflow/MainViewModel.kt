package com.bobbyprabowo.dumbflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobbyprabowo.dumbflow.domain.GetData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.seconds

@FlowPreview
@ExperimentalCoroutinesApi
class MainViewModel(
    private val getData: GetData
) : ViewModel() {

    private val intentFlow = MutableSharedFlow<MainIntent>()
    private val _uiState = MutableStateFlow(MainState(data = "IDLE"))

    // The UI collects from this StateFlow to get its state updates
    val uiState: StateFlow<MainState> = _uiState

    private val intentFilter = { incomingFlow: Flow<MainIntent> ->
        val sharedFlow = incomingFlow.shareIn(viewModelScope, SharingStarted.Eagerly)

        merge(
            sharedFlow.filter { it is MainIntent.InitialLoadIntent }.take(1),
            sharedFlow.filter { it is MainIntent.InitialRefreshIntent }.take(1)
        )
    }

    private val intentToAction = { incomingFlow: Flow<MainIntent> ->
        incomingFlow.map { intent ->
            when (intent) {
                is MainIntent.InitialLoadIntent -> MainAction.InitialLoadAction
                is MainIntent.InitialRefreshIntent -> MainAction.InitialRefreshAction
            }
        }
    }

    private val actionProcessor = { incomingFlow: Flow<MainAction> ->
        val sharedFlow = incomingFlow.shareIn(viewModelScope, SharingStarted.Eagerly)

        val actionInitialLoad = { actionFlow: Flow<MainAction.InitialLoadAction> ->
            actionFlow.flatMapConcat {
                getData.execute()
                    .map { MainResult.InitialLoadResult.Success(it) as MainResult }
                    .catch {
                        emit(MainResult.InitialLoadResult.Error)
                    }
                    .onStart {
                        delay(1.seconds)
                        emit(MainResult.InitialLoadResult.Loading)
                    }
                    .flowOn(Dispatchers.IO)

            }
        }

        val actionInitialRefresh = { actionFlow: Flow<MainAction.InitialRefreshAction> ->
            actionFlow.flatMapConcat {
                flowOf(MainResult.InitialRefreshResult.Success("Refresh Success") as MainResult)
                    .onStart {
                        delay(1.seconds)
                        emit(MainResult.InitialRefreshResult.Loading as MainResult)
                    }
                    .flowOn(Dispatchers.IO)
            }
        }

        merge(
            sharedFlow.filter { it is MainAction.InitialLoadAction }
                .map { it as MainAction.InitialLoadAction }
                .let(actionInitialLoad),
            sharedFlow.filter { it is MainAction.InitialRefreshAction }
                .map { it as MainAction.InitialRefreshAction }
                .let(actionInitialRefresh),
        )
    }

    private val reducer = { resultFlow: Flow<MainResult> ->
        resultFlow.scan(MainState(data = "IDLE")) { previousState, result ->
            when (result) {
                MainResult.InitialLoadResult.Loading -> {
                    previousState.copy(data = "Load Loading")
                }
                is MainResult.InitialLoadResult.Success -> {
                    previousState.copy(data = result.result)
                }
                MainResult.InitialLoadResult.Error -> {
                    previousState.copy(data = "Load Error")
                }
                MainResult.InitialRefreshResult.Loading -> {
                    previousState.copy(data = "Refresh Loading")
                }
                is MainResult.InitialRefreshResult.Success -> {
                    previousState.copy(data = result.result)
                }
                MainResult.InitialRefreshResult.Error -> {
                    previousState.copy(data = "Refresh Error")
                }
            }
        }
    }

    init {
        intentFlow
            .let(intentFilter) // intent limiter, like debounce, take
            .let(intentToAction) //  convert intent to action
            .let(actionProcessor) // where use case / repository called based on action type
            .let(reducer) // reducer function
            .onEach { newState ->
                _uiState.value = newState
            }
            .launchIn(viewModelScope)
    }

    suspend fun doInitialDataFetch() {
        intentFlow.emit(MainIntent.InitialRefreshIntent)
    }

    suspend fun doInitialDataLoad() {
        intentFlow.emit(MainIntent.InitialLoadIntent)
    }
}

sealed class MainIntent {

    object InitialLoadIntent : MainIntent()
    object InitialRefreshIntent : MainIntent()
}

sealed class MainAction {
    object InitialLoadAction : MainAction()
    object InitialRefreshAction : MainAction()
}

sealed class MainResult {
    sealed class InitialLoadResult : MainResult() {
        object Loading : InitialLoadResult()
        data class Success(val result: String) : InitialLoadResult()
        object Error : InitialLoadResult()
    }

    sealed class InitialRefreshResult : MainResult() {
        object Loading : InitialRefreshResult()
        data class Success(val result: String) : InitialRefreshResult()
        object Error : InitialRefreshResult()
    }
}

data class MainState(
    val data: String
)
