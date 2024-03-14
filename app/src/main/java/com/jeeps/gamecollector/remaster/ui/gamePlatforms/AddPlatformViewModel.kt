package com.jeeps.gamecollector.remaster.ui.gamePlatforms

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jeeps.gamecollector.remaster.data.model.data.platforms.Platform
import com.jeeps.gamecollector.remaster.data.repository.AuthenticationRepository
import com.jeeps.gamecollector.remaster.data.repository.PlatformsRepository
import com.jeeps.gamecollector.remaster.ui.base.BaseViewModel
import com.jeeps.gamecollector.remaster.utils.Event
import com.jeeps.gamecollector.remaster.utils.ImageCompressor
import com.jeeps.gamecollector.remaster.utils.extensions.handleNetworkResponse
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
    private val authenticationRepository: AuthenticationRepository,
    private val imageCompressor: ImageCompressor
) : BaseViewModel() {

    var isEdit: Boolean = false
    var isImageEdited: Boolean = false
    var currentImageUri: Uri? = null

    private val _platform = MutableLiveData(Platform())
    val platform: LiveData<Platform>
        get() = _platform

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
        _platform.value?.let { currentPlatform ->
            currentPlatform.imageUri = currentImageUri.toString()
            handleNetworkResponse(platformsRepository.savePlatform(token, currentPlatform)) { newPlatform ->
                currentPlatform.id = newPlatform.id
                if (isImageEdited) {
                    currentImageUri?.let { uri ->
                        uploadImageCover(imageCompressor.compressImage(uri))
                    }
                } else {
                    skipImageUpload()
                }
            }
        }
    }

    private suspend fun editPlatform() {
        val token = authenticationRepository.getUserToken()
        _platform.value?.let {
            handleNetworkResponse(platformsRepository.editPlatform(token, it)) {
                if (isImageEdited) {
                    currentImageUri?.let { uri ->
                        uploadImageCover(imageCompressor.compressImage(uri))
                    }
                } else {
                    skipImageUpload()
                }
            }
        }
    }

    private fun uploadImageCover(compressedImage: File?) {
        viewModelScope.launch {
            compressedImage?.let { file ->
                val token = authenticationRepository.getUserToken()
                val requestFile = file
                    .asRequestBody("image/png".toMediaTypeOrNull())
                val body: MultipartBody.Part =
                    MultipartBody.Part.createFormData("image", file.name, requestFile)

                handleNetworkResponse(platformsRepository.uploadPlatformCover(
                    token, _platform.value?.id ?: "", body)) {
                    if (file.exists()) file.delete()
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