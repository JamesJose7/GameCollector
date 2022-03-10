package com.jeeps.gamecollector.remaster.ui.gamePlatforms

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.haroldadmin.cnradapter.NetworkResponse
import com.jeeps.gamecollector.remaster.data.model.data.platforms.Platform
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.PlatformsRepository
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.ui.base.ErrorType
import com.jeeps.gamecollector.remaster.utils.Event
import com.squareup.picasso.Picasso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class AddPlatformViewModel @Inject constructor(
    private val platformsRepository: PlatformsRepository,
    private val authenticationRepository: AuthenticationRepository
) : BaseViewModel() {

    var isEdit: Boolean = false
    var isImageEdited: Boolean = false
    var currentImageUri: Uri? = null

    private val _platform = MutableLiveData(Platform())
    val platform: LiveData<Platform>
        get() = _platform

    private val _isPlatformSaved = MutableLiveData<Event<Boolean>>()
    val isPlatformSaved: LiveData<Event<Boolean>>
        get() = _isPlatformSaved

    private val _isImageUploaded = MutableLiveData<Event<Boolean>>()
    val isImageUploaded: LiveData<Event<Boolean>>
        get() = _isImageUploaded

    private val _fieldErrorMessage = MutableLiveData<String>()
    val fieldErrorMessage: LiveData<String>
        get() = _fieldErrorMessage

    fun setPlatform(platform: Platform) {
        _platform.value = platform

        // Invalidate cached image URI
        Picasso.get().invalidate(platform.imageUri)
    }

    fun imageHasBeenEdited() {
//        if (isEdit)
        isImageEdited = true
    }

    fun setPlatformColor(color: String?) {
        color?.let {
            _platform.value?.let { platform ->
                platform.color = color
            }
        }
    }

    fun setPlatformName(name: String) {
        _platform.value?.name = name
    }

    fun savePlatform() {
        startLoading()
        viewModelScope.launch {
            if (isEdit) editPlatform()
            else saveNewPlatform()
        }
    }

    private suspend fun saveNewPlatform() {
        val token = authenticationRepository.getUserToken()
        _platform.value?.let {
            it.imageUri = currentImageUri.toString()
            when (val response = platformsRepository.savePlatform(token, it)) {
                is NetworkResponse.Success -> {
                    it.id = response.body.id
                    _isPlatformSaved.postValue(Event(true))
                }
                is NetworkResponse.ServerError -> {
                    handleError(ErrorType.SERVER_ERROR, response.error)
                    stopLoading()
                }
                is NetworkResponse.NetworkError -> {
                    handleError(ErrorType.NETWORK_ERROR, response.error)
                    stopLoading()
                }
                is NetworkResponse.UnknownError -> {
                    handleError(ErrorType.UNKNOWN_ERROR, response.error)
                    stopLoading()
                }
            }
        }
    }

    private suspend fun editPlatform() {
        val token = authenticationRepository.getUserToken()
        _platform.value?.let {
            when (val response = platformsRepository.editPlatform(token, it)) {
                is NetworkResponse.Success -> {
                    _isPlatformSaved.postValue(Event(true))
                }
                is NetworkResponse.ServerError -> {
                    handleError(ErrorType.SERVER_ERROR, response.error)
                    stopLoading()
                }
                is NetworkResponse.NetworkError -> {
                    handleError(ErrorType.NETWORK_ERROR, response.error)
                    stopLoading()
                }
                is NetworkResponse.UnknownError -> {
                    handleError(ErrorType.UNKNOWN_ERROR, response.error)
                    stopLoading()
                }
            }
        }
    }

    fun uploadImageCover(compressedImage: File?) {
        viewModelScope.launch {
            compressedImage?.let { file ->
                val token = authenticationRepository.getUserToken()
                val requestFile = file
                    .asRequestBody("image/png".toMediaTypeOrNull())
                val body: MultipartBody.Part =
                    MultipartBody.Part.createFormData("image", file.name, requestFile)
                when (val response = platformsRepository.uploadPlatformCover(
                    token, _platform.value?.id ?: "", body)) {
                    is NetworkResponse.Success -> {
                        if (file.exists()) file.delete()
                    }
                    is NetworkResponse.ServerError -> {
                        handleError(ErrorType.SERVER_ERROR, response.error)
                    }
                    is NetworkResponse.NetworkError -> {
                        handleError(ErrorType.NETWORK_ERROR, response.error)
                    }
                    is NetworkResponse.UnknownError -> {
                        handleError(ErrorType.UNKNOWN_ERROR, response.error)
                    }
                }
            }

            _isImageUploaded.postValue(Event(true))
            stopLoading()
        }
    }

    fun skipImageUpload() {
        stopLoading()
        _isImageUploaded.postValue(Event(true))
    }

    fun validateFields(): Boolean {
        return when {
            currentImageUri == null && !isEdit -> {
                _fieldErrorMessage.value = "Please select an image"
                false
            }
            platform.value?.name.isNullOrEmpty() -> {
                _fieldErrorMessage.value = "Please input a name"
                false
            }
            platform.value?.color.isNullOrEmpty() -> {
                _fieldErrorMessage.value = "Please select a color"
                false
            }
            else -> true
        }
    }
}