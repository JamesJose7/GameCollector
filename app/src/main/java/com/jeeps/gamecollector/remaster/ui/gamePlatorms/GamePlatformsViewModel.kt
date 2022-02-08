package com.jeeps.gamecollector.remaster.ui.gamePlatorms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeps.gamecollector.model.Platform
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.repository.PlatformsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.Comparator

@ExperimentalCoroutinesApi
@HiltViewModel
class GamePlatformsViewModel @Inject constructor(
    private val platformsRepository: PlatformsRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _platforms = MutableLiveData<List<Platform>>()
    val platforms: LiveData<List<Platform>>
        get() = _platforms.also {
            loadPlatforms()
        }

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private fun loadPlatforms() {
        viewModelScope.launch(Dispatchers.IO) {
            platformsRepository.getPlatforms().collect {
                when (it) {
                    is State.Loading -> {
                        _isLoading.postValue(true)
                    }
                    is State.Success -> {
                        _isLoading.postValue(false)
                        it.data.let { result ->
                            val sortedPlatforms = result.sortedWith(Comparator.comparing { p: Platform ->
                                p.name.lowercase()
                            })
                            _platforms.postValue(sortedPlatforms)
                        }
                    }
                    is State.Failed -> {
                        _isLoading.postValue(false)
                    }
                }
            }
        }
    }
}