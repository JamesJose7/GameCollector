package com.jeeps.gamecollector.remaster.utils.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
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