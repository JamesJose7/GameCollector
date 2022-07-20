package com.jeeps.gamecollector.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.jeeps.gamecollector.databinding.ViewRatingChipBinding
import com.jeeps.gamecollector.utils.ColorsUtils
import kotlin.math.roundToInt

class RatingChip : ConstraintLayout {

    private lateinit var binding: ViewRatingChipBinding

    constructor(context: Context) : super(context) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setupView()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        setupView()
    }

    private fun setupView() {
        inflateView()
    }

    private fun inflateView() {
        binding = ViewRatingChipBinding.inflate(
            LayoutInflater.from(context), this, true)
    }

    companion object {

        @JvmStatic
        @BindingAdapter("ratingTitle")
        fun RatingChip.setTitle(title: String?) {
            title?.let { binding.title.text = it }
        }

        @JvmStatic
        @BindingAdapter("ratingSubtitle")
        fun RatingChip.setSubtitle(subtitle: String?) {
            subtitle?.let { binding.subtitle.text = it }
        }

        @JvmStatic
        @BindingAdapter("rating")
        fun RatingChip.setRating(rating: Double?) {
            val roundRating = rating?.roundToInt() ?: 0
            val chipColor = ColorsUtils.getColorByRatingRange(context, rating ?: 0.0)
            binding.rating.text = roundRating.toString()
            binding.rating.setBackgroundColor(chipColor)
        }
    }
}
