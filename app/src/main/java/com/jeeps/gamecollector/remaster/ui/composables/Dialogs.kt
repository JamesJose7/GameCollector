package com.jeeps.gamecollector.remaster.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun Dialog(
    title: String,
    description: String,
    icon: ImageVector?,
    confirmButtonText: String = "Confirm",
    dismissButtonText: String = "Dismiss",
    onDismissRequest: (() -> Unit)? = null,
    onConfirmation: (() -> Unit)? = null,
) {
    AlertDialog(
        icon = {
            icon?.let {
                Icon(it, contentDescription = null)
            }
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = description)
        },
        onDismissRequest = {
            if (onDismissRequest != null) {
                onDismissRequest()
            }
        },
        confirmButton = {
            if (onConfirmation != null) {
                TextButton(
                    onClick = {
                        onConfirmation()
                    }
                ) {
                    Text(confirmButtonText)
                }
            }
        },
        dismissButton = {
            if (onDismissRequest != null) {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(dismissButtonText)
                }
            }
        }
    )
}