package com.jeeps.gamecollector.remaster.ui.userStats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeps.gamecollector.model.UserStats
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class UserStatsViewModel @Inject constructor(
    private val userStatsRepository: UserStatsRepository,
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _userStats = MutableLiveData<UserStats>()
    val userStats: LiveData<UserStats>
        get() = _userStats.also {
            loadStats()
        }

    private fun loadStats() {
        val user = authenticationRepository.getUser() ?: return

        viewModelScope.launch {
            userStatsRepository.getUserStats(user.username).collect {
                when (it) {
                    is State.Loading -> _isLoading.postValue(true)
                    is State.Success -> {
                        _isLoading.postValue(false)
                        it.data?.let { result ->
                            _userStats.postValue(result)
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