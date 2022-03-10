package com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jeeps.gamecollector.databinding.AdvancedFiltersDialogBinding

class AdvancedFiltersDialog(
    context: Context
) : BottomSheetDialog(context) {

    private lateinit var binding: AdvancedFiltersDialogBinding

    init {
        inflateViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindViews()
    }

    private fun inflateViews() {
        binding = AdvancedFiltersDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
    }

    private fun bindViews() {


    }
}

