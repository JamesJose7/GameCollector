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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val uiEventsChannel = Channel<UiEvent>()
    val uiEventsChannelFlow = uiEventsChannel.receiveAsFlow()

    var isEdit: Boolean = false
    private var isImageEdited: Boolean = false
    private var currentImageUri: Uri? = null

    private val _platform = MutableStateFlow(Platform())
    val platform: StateFlow<Platform> = _platform.asStateFlow()

    fun setPlatform(platform: Platform) {
        _platform.value = platform

        // Invalidate cached image URI
        Picasso.get().invalidate(platform.imageUri)
    }

    fun setPlatformImageUri(uri: Uri?) {
        uri?.let {
            isImageEdited = true
            currentImageUri = uri
            _platform.value = _platform.value.copy(imageUri = uri.toString())
        }
    }

    fun setPlatformColor(color: String) {
        _platform.value = _platform.value.copy(color = color)
    }

    fun setPlatformName(name: String) {
        _platform.value = _platform.value.copy(name = name)
    }

    fun savePlatform() {
        if (validateFields()) {
            startLoading()
            viewModelScope.launch {
                if (isEdit) editPlatform()
                else saveNewPlatform()
            }
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
                    token, _platform.value.id, body)) {
                    if (file.exists()) file.delete()
                }
            }

            sendUiEvent(UiEvent.ImageFinishedUploading)
            stopLoading()
        }
    }

    private fun skipImageUpload() {
        stopLoading()
        sendUiEvent(UiEvent.ImageFinishedUploading)
    }

    private fun validateFields(): Boolean {
        return when {
            currentImageUri == null && !isEdit -> {
                sendUiEvent((UiEvent.ShowFieldError("Please select an image")))
                false
            }
            platform.value.name.isEmpty() -> {
                sendUiEvent((UiEvent.ShowFieldError("Please input a name")))
                false
            }
            platform.value.color.isEmpty() -> {
                sendUiEvent((UiEvent.ShowFieldError("Please select a color")))
                false
            }
            else -> true
        }
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            uiEventsChannel.send(event)
        }
    }

    sealed class UiEvent {
        data class ShowFieldError(val message: String) : UiEvent()
        data object ImageFinishedUploading : UiEvent()
    }
}