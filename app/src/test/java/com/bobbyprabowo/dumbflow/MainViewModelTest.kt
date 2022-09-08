package com.bobbyprabowo.dumbflow

import app.cash.turbine.test
import com.bobbyprabowo.dumbflow.domain.GetData
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime


@FlowPreview
@ExperimentalTime
@ExperimentalCoroutinesApi
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var mockedGetData: GetData

    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        mainViewModel = MainViewModel(mockedGetData, testDispatcher)
    }

    @After
    fun after() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDefault() = runTest {
        mainViewModel.uiState.test {
            assertEquals(MainState(data = "IDLE"), awaitItem())
        }
    }

    @Test
    fun thenCondition() = runTest {
        val expectedError = Exception("error")
        every { mockedGetData.execute() } returns flow { throw expectedError }
        mainViewModel.uiState.test {
            assertEquals(MainState(data = "IDLE"), awaitItem())
            mainViewModel.doInitialDataLoad()
            assertEquals(MainState(data = "Load Loading"), awaitItem())
            assertEquals(MainState(data = "Load Error"), awaitItem())
        }

    }
}
