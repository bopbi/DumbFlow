package com.bobbyprabowo.dumbflow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.bobbyprabowo.dumbflow.databinding.ActivityMainBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this@MainActivity, MainViewModelFactory()).get(MainViewModel::class.java)
        viewModel.uiState.asLiveData().observe(this@MainActivity) { state ->
            println(">>>> ${state.data}")
            binding.mainText.text = state.data
        }

        lifecycleScope.launchWhenResumed {
            viewModel.doInitialDataLoad()
            viewModel.doInitialDataFetch()
        }
    }
}
