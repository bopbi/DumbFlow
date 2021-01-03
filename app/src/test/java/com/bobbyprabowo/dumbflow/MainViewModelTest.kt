package com.bobbyprabowo.dumbflow

import app.cash.turbine.test
import com.bobbyprabowo.dumbflow.domain.GetData
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
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
            fun thenCondition() = runBlocking {
                mainViewModel.uiState.test {
                    assertEquals(MainState(data = "IDLE"), expectItem())
                }
            }
        }
    }

    @DisplayName("Given getData emit error")
    @Nested
    inner class FailedGetData {

        private val expectedError = Exception("error")

        @BeforeEach
        fun setup() {
            every { mockedGetData.execute() } returns flow { throw expectedError }
        }

        @Test
        @DisplayName("doInitialDataFetch then should emit Default State")
        fun thenCondition() = runBlocking { // if you are using turbine, do not use runblocking see https://github.com/cashapp/turbine/blob/39740b4dad4977de427e1bf43121570ad0f2a618/src/jvmTest/kotlin/app/cash/turbine/jvmTestUtil.kt#L31
            mainViewModel.uiState.test {
                assertEquals(MainState(data = "IDLE"), expectItem())
                mainViewModel.doInitialDataLoad()
                assertEquals(MainState(data = "Load Loading"), expectItem())
                assertEquals(MainState(data = "Load Error"), expectItem())
            }

        }
    }
}
