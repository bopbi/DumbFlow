package com.bobbyprabowo.dumbflow

import app.cash.turbine.test
import com.bobbyprabowo.dumbflow.domain.GetData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.time.ExperimentalTime


@ExperimentalTime
@ExperimentalCoroutinesApi
internal class MainViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @Mock
    private lateinit var mockedGetData: GetData

    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        mainViewModel = MainViewModel(mockedGetData)
    }

    @Test
    fun shouldEmitIdleFlow() {
        runBlocking {
            mainViewModel.uiState.test {
                assertEquals(MainState(data="IDLE"), expectItem())
            }
        }

    }
}
