package com.bobbyprabowo.dumbflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
class MainViewModel : ViewModel() {

    private val intentFlow = MutableSharedFlow<MainIntent>()
    private val _uiState = MutableStateFlow(MainState(data = "IDLE"))
    // The UI collects from this StateFlow to get its state updates
    val uiState: StateFlow<MainState> = _uiState

    private val intentFilter = { incomingFlow: Flow<MainIntent> ->
        val sharedFlow = incomingFlow.shareIn(viewModelScope, SharingStarted.Eagerly)

        merge(
            sharedFlow.filter { it is MainIntent.InitialLoadIntent }.take(1),
            sharedFlow.filter { it is MainIntent.InitialRefreshIntent }.take(1),
        )
    }

    private val intentToAction = { incomingFlow: Flow<MainIntent> ->
        incomingFlow.map { intent ->
            when(intent) {
                is MainIntent.InitialLoadIntent -> MainAction.InitialLoadAction
                is MainIntent.InitialRefreshIntent -> MainAction.InitialRefreshAction
            }
        }
    }

    private val actionProcessor = { incomingFlow: Flow<MainAction> ->
        val sharedFlow = incomingFlow.shareIn(viewModelScope, SharingStarted.Eagerly)

        val actionInitialLoad = {actionFlow: Flow<MainAction.InitialLoadAction> ->
            flow<MainResult> {
                try {
                    emit(MainResult.InitialLoadResult.Success("Load Success"))
                } catch (error: Throwable) {
                    emit(MainResult.InitialLoadResult.Error)
                }
            }
                .onStart {
                    emit(MainResult.InitialLoadResult.Loading as MainResult)
                }
        }

        val actionInitialRefresh = {actionFlow: Flow<MainAction.InitialRefreshAction> ->
            flow<MainResult> {
                try {
                    emit(MainResult.InitialRefreshResult.Success("Refresh Success"))
                } catch (error: Throwable) {
                    emit(MainResult.InitialRefreshResult.Error)
                }
            }
                .onStart {
                    emit(MainResult.InitialRefreshResult.Loading as MainResult)
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
        resultFlow.onEach { result ->
            val previousState = _uiState.value
            _uiState.value = when (result) {
                MainResult.InitialLoadResult.Loading -> {
                    previousState
                }
                is MainResult.InitialLoadResult.Success -> {
                    previousState.copy(data = result.result)
                }
                MainResult.InitialLoadResult.Error -> {
                    previousState
                }
                MainResult.InitialRefreshResult.Loading -> {
                    previousState
                }
                is MainResult.InitialRefreshResult.Success -> {
                    previousState.copy(data = result.result)
                }
                MainResult.InitialRefreshResult.Error -> {
                    previousState
                }
            }
        }
    }

    init {

        intentFlow.asSharedFlow()
            .let(intentFilter)
            .let(intentToAction)
            .let(actionProcessor)
            .let(reducer)
            .launchIn(viewModelScope)

        intentFlow.tryEmit(MainIntent.InitialLoadIntent)
        intentFlow.tryEmit(MainIntent.InitialRefreshIntent)
    }

    fun doSomething() {

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
    val data : String
)
