package com.jeeps.gamecollector.remaster.ui.composables

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.AppTheme
import kotlin.math.roundToInt

@Composable
fun CircularGraph(
    modifier: Modifier = Modifier,
    percentage: Float,
    strokeWidth: Dp = 12.dp,
    strokeColor: Color = MaterialTheme.colorScheme.tertiary,
    size: Dp = 120.dp,
    animationSpec: AnimationSpec<Float> = tween(
        durationMillis = 600,
        easing = FastOutSlowInEasing
    )
) {
    val barProgress: Float by animateFloatAsState(
        targetValue = percentage,
        label = "Bar animation",
        animationSpec = animationSpec
    )

    val percentageText = (percentage * 100).roundToInt()

    Box(
        modifier = modifier
    ) {
        CircularProgressIndicator(
            progress = { barProgress },
            strokeCap = StrokeCap.Round,
            strokeWidth = strokeWidth,
            color = strokeColor,
            modifier = Modifier.size(size)
        )
        Text(
            text = "${percentageText}%",
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier
                .align(Alignment.Center)
        )
        Text(
            text = "Completed",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 40.dp)
        )

    }

}

@Preview
@Composable
private fun CircularGraphPreview() {
    AppTheme {
        Surface {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularGraph(percentage = 0.1f)
                CircularGraph(percentage = 0.5f)
                CircularGraph(percentage = 0.8f)
                CircularGraph(percentage = 1f)
            }
        }
    }
}