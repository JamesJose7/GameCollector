package com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ToggleButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jeeps.gamecollector.databinding.AdvancedFiltersDialogBinding

class AdvancedFiltersDialog(
    context: Context,
    private val listener: AdvancedFiltersDialogListener,
    private val sortControls: SortControls
) : BottomSheetDialog(context) {

    private lateinit var binding: AdvancedFiltersDialogBinding

    init {
        inflateViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindSortControls()
        activateEnabledToggles(sortControls)
    }

    private fun inflateViews() {
        binding = AdvancedFiltersDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
    }

    private fun bindSortControls() {
        binding.sortOrderToggle.setOnCheckedChangeListener { _, isChecked ->
            sortControls.isAscending = !isChecked
            listener.updateSortControls(sortControls)
        }

        bindToggleControls(binding.sortFormatPhysicalToggle, sortControls) {
            sortControls.isPhysical = it
        }
        bindToggleControls(binding.sortFormatDigitalToggle, sortControls) {
            sortControls.isDigital = it
        }
        bindToggleControls(binding.sortAlphabeticalToggle, sortControls) {
            sortControls.isAlphabetical = it
        }
        bindToggleControls(binding.sortTimesCompletedToggle, sortControls) {
            sortControls.isCompletion = it
        }
        bindToggleControls(binding.sortHoursMainToggle, sortControls) {
            sortControls.isHoursMain = it
        }
        bindToggleControls(binding.sortHoursExtraToggle, sortControls) {
            sortControls.isHoursExtra = it
        }
        bindToggleControls(binding.sortHoursCompletionistToggle, sortControls) {
            sortControls.isHoursCompletionist = it
        }
    }

    private fun bindToggleControls(
        toggleButton: ToggleButton,
        sortControls: SortControls,
        updateSortControl: (isChecked: Boolean) -> Unit
    ) {
        toggleButton.setOnCheckedChangeListener { view, isChecked ->
            if (view.isPressed) {
                clearMiscellaneousToggles(sortControls)
                updateSortControl(isChecked)
                activateEnabledToggles(sortControls)
                listener.updateSortControls(sortControls)
            }
        }
    }

    private fun activateEnabledToggles(sortControls: SortControls) {
        with(sortControls) {
            with(binding) {
                sortOrderToggle.isChecked = !isAscending
                sortFormatPhysicalToggle.isChecked = isPhysical
                sortFormatDigitalToggle.isChecked = isDigital
                sortAlphabeticalToggle.isChecked = isAlphabetical
                sortTimesCompletedToggle.isChecked = isCompletion
                sortHoursMainToggle.isChecked = isHoursMain
                sortHoursExtraToggle.isChecked = isHoursExtra
                sortHoursCompletionistToggle.isChecked = isHoursCompletionist
            }
        }
    }

    private fun clearMiscellaneousToggles(sortControls: SortControls) {
        with(sortControls) {
            isPhysical = false
            isDigital = false
            isAlphabetical = false
            isCompletion = false
            isHoursMain = false
            isHoursExtra = false
            isHoursCompletionist = false
        }
    }
}

interface AdvancedFiltersDialogListener {
    fun updateSortControls(sortControls: SortControls)
}