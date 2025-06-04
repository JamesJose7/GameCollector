package com.jeeps.gamecollector.remaster.ui.composables

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class MenuItem(
    val text: String,
    val onClick: () -> Unit,
    val showBottomDivider: Boolean = false
)

@Composable
fun PopUpMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    menuItems: List<MenuItem>,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        menuItems.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.text
                    )
                },
                onClick = item.onClick
            )
            if (item.showBottomDivider) {
                HorizontalDivider()
            }
        }
    }
}