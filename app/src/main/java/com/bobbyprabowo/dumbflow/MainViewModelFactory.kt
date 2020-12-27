package com.bobbyprabowo.dumbflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bobbyprabowo.dumbflow.domain.GetData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.lang.RuntimeException

class MainViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(object : GetData {
                override fun execute(): Flow<String> {
                    return flowOf("Load Success")
                }
            }) as T
        } else {
            throw RuntimeException("")
        }
    }
}
