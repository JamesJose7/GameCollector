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
    private val filterControls: FilterControls,
    private val sortControls: SortControls,
) : BottomSheetDialog(context) {

    private lateinit var binding: AdvancedFiltersDialogBinding

    init {
        inflateViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindFilterControls()
        bindSortControls()
        activateEnabledFilterToggles()
        activateEnabledSortToggles()
    }

    private fun inflateViews() {
        binding = AdvancedFiltersDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
    }

    private fun bindFilterControls() {
        bindFilterButton(binding.filterCompletedToggle) { filterControls.completed = it }
        bindFilterButton(binding.filterNotCompletedToggle) { filterControls.notCompleted = it }
        bindFilterButton(binding.filterDigitalToggle) { filterControls.isDigital = it }
        bindFilterButton(binding.filterPhysicalToggle) { filterControls.isPhysical = it }

        binding.clearFiltersButton.setOnClickListener {
            listener.clearFilters()
            clearFilters()
        }
    }

    private fun bindFilterButton(
        filterToggleButton: ToggleButton,
        updateFilterControl: (Boolean) -> Unit
    ) {
        filterToggleButton.setOnCheckedChangeListener { toggleView, isChecked ->
            if (toggleView.isPressed) {
                updateFilterControl(isChecked)
                listener.updateFilterControls(filterControls)
            }
        }
    }

    private fun bindSortControls() {
        binding.sortOrderToggle.setOnCheckedChangeListener { _, isChecked ->
            sortControls.isAscending = !isChecked
            listener.updateSortControls(sortControls)
        }

        bindSortButton(binding.sortFormatPhysicalToggle) {
            sortControls.isPhysical = it
        }
        bindSortButton(binding.sortFormatDigitalToggle) {
            sortControls.isDigital = it
        }
        bindSortButton(binding.sortAlphabeticalToggle) {
            sortControls.isAlphabetical = it
        }
        bindSortButton(binding.sortTimesCompletedToggle) {
            sortControls.isCompletion = it
        }
        bindSortButton(binding.sortHoursMainToggle) {
            sortControls.isHoursMain = it
        }
        bindSortButton(binding.sortHoursExtraToggle) {
            sortControls.isHoursExtra = it
        }
        bindSortButton(binding.sortHoursCompletionistToggle) {
            sortControls.isHoursCompletionist = it
        }
    }

    private fun bindSortButton(
        toggleButton: ToggleButton,
        updateSortControl: (isChecked: Boolean) -> Unit
    ) {
        toggleButton.setOnCheckedChangeListener { view, isChecked ->
            if (view.isPressed) {
                clearMiscellaneousToggles()
                updateSortControl(isChecked)
                activateEnabledSortToggles()
                listener.updateSortControls(sortControls)
            }
        }
    }

    private fun activateEnabledSortToggles() {
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

    private fun activateEnabledFilterToggles() {
        with(filterControls) {
            with(binding) {
                filterCompletedToggle.isChecked = completed
                filterNotCompletedToggle.isChecked = notCompleted
                filterDigitalToggle.isChecked = isDigital
                filterPhysicalToggle.isChecked = isPhysical
            }
        }
    }

    private fun clearMiscellaneousToggles() {
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

    private fun clearFilters() {
        with(filterControls) {
            completed = false
            notCompleted = false
            isPhysical = false
            isDigital = false
        }

        with(binding) {
            filterCompletedToggle.isChecked = false
            filterNotCompletedToggle.isChecked = false
            filterDigitalToggle.isChecked = false
            filterPhysicalToggle.isChecked = false
        }
    }
}

interface AdvancedFiltersDialogListener {
    fun updateFilterControls(filterControls: FilterControls)
    fun clearFilters()
    fun updateSortControls(sortControls: SortControls)
}