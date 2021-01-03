package com.bobbyprabowo.dumbflow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launchWhenCreated {

            viewModel = ViewModelProvider(this@MainActivity, MainViewModelFactory()).get(MainViewModel::class.java)
            viewModel.uiState.asLiveData().observe(this@MainActivity, {state ->
                println(">>>> ${state.data}")
                Toast.makeText(this@MainActivity, state.data, Toast.LENGTH_LONG).show()
            })

            viewModel.doInitialDataLoad()
            viewModel.doInitialDataFetch()
        }
    }
}
