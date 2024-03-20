package com.jeeps.gamecollector.remaster.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jeeps.gamecollector.R

@Composable
fun FireworksAnimation(
    modifier: Modifier = Modifier,
    animationReps: Int = LottieConstants.IterateForever
) {
    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.fireworks_animation
        )
    )

    LottieAnimation(
        composition = preloaderLottieComposition,
        iterations = animationReps,
        modifier = modifier
    )
}

@Preview(widthDp = 200, heightDp = 200, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun FireworksAnimationPreview() {
    Column(
        modifier = Modifier.padding(all = 20.dp)
    ) {
        FireworksAnimation()
    }
}

