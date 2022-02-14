package com.jeeps.gamecollector.remaster.ui.gamePlatorms

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.databinding.ActivityAddPlatformBinding
import com.jeeps.gamecollector.databinding.ContentAddPlatformBinding
import com.jeeps.gamecollector.model.Platform
import com.jeeps.gamecollector.remaster.ui.base.BaseActivity
import com.jeeps.gamecollector.remaster.utils.extensions.compressImage
import com.jeeps.gamecollector.remaster.utils.extensions.showToast
import com.jeeps.gamecollector.remaster.utils.extensions.viewBinding
import com.jeeps.gamecollector.utils.PlatformColors
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddPlatformActivity : BaseActivity() {

    private val binding by viewBinding(ActivityAddPlatformBinding::inflate)
    private lateinit var content: ContentAddPlatformBinding

    private val viewModel: AddPlatformViewModel by viewModels()

    private val registerForOpenDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        handleImageSelected(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        content = binding.content

        // Get platform if being edited
        val platform = intent?.getSerializableExtra(EDITED_PLATFORM) as? Platform
        platform?.let {
            viewModel.setPlatform(it)
            viewModel.isEdit = true
        }

        bindFab()
        bindPlatform()
        bindFormElements()
        bindLoading()

        handleErrorMessages()
        handleServiceEvents()
    }

    private fun handleErrorMessages() {
        viewModel.errorMessage.observe(this) {
            showToast(it)
        }
    }

    private fun bindFab() {
        binding.fab.setOnClickListener {
            viewModel.savePlatform()
        }
    }

    private fun bindPlatform() {
        viewModel.platform.observe(this) { platform ->
            platform.imageUri?.let { uri ->
                if (uri.isNotEmpty()) {
                    Picasso.get()
                        .load(uri)
                        .into(content.platformCover)
                }
            }
            content.platformCover.setBackgroundColor(Color.parseColor("#99cccccc"))
            content.platformNameEdit.setText(platform.name)
            // TODO: Is there a better way to do this?
            PlatformColors.values()
                .find { it.color == platform.color }
                ?.let { content.platformColorRadioGroup.check(it.colorId) }
        }
    }

    private fun bindFormElements() {
        content.platformCover.setOnClickListener {
            registerForOpenDocument.launch(arrayOf("image/*"))
        }

        content.platformNameEdit.doOnTextChanged { text, _, _, _ ->
            viewModel.setPlatformName(text.toString())
        }
    }

    private fun bindLoading() {
        viewModel.isLoading.observe(this) { isLoading ->
            toggleProgressbar(isLoading)
        }
    }

    fun onColorPickerClicked(view: View) {
        if (view !is RadioButton) return
        val checked = view.isChecked

        when (view.id) {
            R.id.color_switch_normiewhite ->
                if (checked) viewModel.setPlatformColor(PlatformColors.NORMIE_WHITE.color)
            R.id.color_switchred ->
                if (checked) viewModel.setPlatformColor(PlatformColors.SWITCH_RED.color)
            R.id.color_xboxgreen ->
                if (checked) viewModel.setPlatformColor(PlatformColors.XBOX_GREEN.color)
            R.id.color_playstationblue ->
                if (checked) viewModel.setPlatformColor(PlatformColors.PLAYSTATION_BLUE.color)
        }
    }

    private fun handleImageSelected(uri: Uri?) {
        if (uri != null) {
            Picasso.get().load(uri).into(content.platformCover)
            content.platformCover.setBackgroundColor(Color.parseColor("#99cccccc"))
            viewModel.imageHasBeenEdited()
            viewModel.currentImageUri = uri
        } else {
            showToast("There was a problem with the image you selected")
        }
    }

    private fun toggleProgressbar(isLoading: Boolean) {
        if (isLoading) {
            binding.addPlatformProgressbar.visibility = View.VISIBLE
            binding.fab.visibility = View.INVISIBLE
        } else {
            binding.addPlatformProgressbar.visibility = View.INVISIBLE
            binding.fab.visibility = View.VISIBLE
        }
    }

    private fun handleServiceEvents() {
        viewModel.isPlatformSaved.observe(this) { event ->
            event.getContentIfNotHandled()?.let { isPlatformSaved ->
                if (isPlatformSaved && viewModel.isImageEdited) {
                    val compressedImage =
                        viewModel.currentImageUri?.let { compressImage("temp.png", it) }
                    viewModel.uploadImageCover(compressedImage)
                } else {
                    viewModel.skipImageUpload()
                }
            }
        }

        viewModel.isImageUploaded.observe(this) { event ->
            event.getContentIfNotHandled()?.let { isImageUploaded ->
                if (isImageUploaded) {
                    finish()
                }
            }
        }
    }

    companion object {
        const val EDITED_PLATFORM = "EDITED PLATFORM"
        const val EDITED_PLATFORM_POSITION = "EDITED PLATFORM POSITION"
        const val PLATFORM = "PLATFORM"
    }
}