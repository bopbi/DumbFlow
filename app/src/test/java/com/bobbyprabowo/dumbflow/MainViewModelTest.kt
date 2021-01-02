package com.bobbyprabowo.dumbflow

import app.cash.turbine.test
import com.bobbyprabowo.dumbflow.domain.GetData
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime


@FlowPreview
@ExperimentalTime
@ExperimentalCoroutinesApi
internal class MainViewModelTest : CoroutineTest {

    override lateinit var testScope: TestCoroutineScope
    override lateinit var dispatcher: TestCoroutineDispatcher

    @MockK
    private lateinit var mockedGetData: GetData

    private lateinit var mainViewModel: MainViewModel

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mainViewModel = MainViewModel(mockedGetData)
    }


    @DisplayName("Given Nothing")
    @Nested
    inner class DefaultStateTest {

        @DisplayName("When not receive any intent")
        @Nested
        inner class NoState {

            @Test
            @DisplayName("then should emit Default State")
            fun thenCondition() {
                runBlocking {
                    mainViewModel.uiState.test {
                        assertEquals(MainState(data="IDLE"), expectItem())
                    }
                }
            }
        }
    }
}
