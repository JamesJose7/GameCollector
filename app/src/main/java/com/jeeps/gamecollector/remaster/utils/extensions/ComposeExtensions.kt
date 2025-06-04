package com.jeeps.gamecollector.remaster.utils.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.graphics.toColorInt
import com.example.compose.AppTheme

fun ComposeView.setComposable(
    content: @Composable() () -> Unit
) {
    apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            AppTheme {
                content()
            }
        }
    }
}

@Composable
internal fun String?.colorFromHexString(fallbackColor: Color = Color.White): Color = when (this?.trim()) {
    null -> fallbackColor
    "" -> fallbackColor
    else -> Color(this.toColorInt())
}