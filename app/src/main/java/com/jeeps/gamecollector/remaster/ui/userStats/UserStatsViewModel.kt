package com.jeeps.gamecollector.remaster.ui.userStats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jeeps.gamecollector.model.UserStats
import com.jeeps.gamecollector.remaster.data.State
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.UserStatsRepository
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
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
) : BaseViewModel() {

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
                    is State.Loading -> startLoading()
                    is State.Success -> {
                        stopLoading()
                        it.data?.let { result ->
                            _userStats.postValue(result)
                        }
                    }
                    is State.Failed -> {
                        stopLoading()
                    }
                }
            }
        }
    }
}