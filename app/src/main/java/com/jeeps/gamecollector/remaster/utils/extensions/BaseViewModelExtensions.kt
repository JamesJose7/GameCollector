package com.jeeps.gamecollector.remaster.utils.extensions

import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.model.ErrorResponse
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel

suspend fun <S> BaseViewModel.handleNetworkResponse(
    response: NetworkResponse<S, ErrorResponse>,
    onSuccess: suspend (S) -> Unit = {}
): S? {
    return when (response) {
        is NetworkResponse.Success -> {
            onSuccess(response.body)
            response.body
        }
        is NetworkResponse.Error -> {
            handleError(response)
            stopLoading()
            null
        }
    }
}

suspend fun <S> BaseViewModel.handleNetworkResponse(
    response: NetworkResponse<S, ErrorResponse>,
    onSuccess: suspend (S) -> Unit,
    onFailure: suspend (ErrorResponse?) -> Unit
): S? {
    return when (response) {
        is NetworkResponse.Success -> {
            onSuccess(response.body)
            response.body
        }
        is NetworkResponse.Error -> {
            onFailure(response.body)
            null
        }
    }
}