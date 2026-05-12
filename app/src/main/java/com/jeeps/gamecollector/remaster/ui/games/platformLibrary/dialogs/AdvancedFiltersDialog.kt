package com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.remaster.ui.theme.AppTheme

@Composable
fun AdvancedFiltersDialog(
    modifier: Modifier = Modifier,
    filterControls: FilterControls,
    sortControls: SortControls,
    showInfoControls: ShowInfoControls,
    onFilterControlsUpdated: (FilterControls) -> Unit = {},
    onSortControlsUpdated: (SortControls, isOrderSort: Boolean) -> Unit = { _, _ -> },
    onShowInfoControlsUpdated: (ShowInfoControls) -> Unit = {},
    onClearFilters: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Filters & Sorting",
                fontSize = 21.sp,
                color = colorResource(R.color.textColorPrimary),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(2f)
            )
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = "Clear All",
                    textAlign = TextAlign.Center
                )
            }
        }
        Text(
            text = "Filter By",
            fontSize = 18.sp,
            color = colorResource(R.color.textColorPrimary),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            FilterChip(
                onClick = {
                    // Turn off opposite filter if it's on
                    val notCompleted = if (!filterControls.completed && filterControls.notCompleted) {
                        false
                    } else {
                        filterControls.notCompleted
                    }

                    onFilterControlsUpdated(
                        filterControls.copy(
                            completed = !filterControls.completed,
                            notCompleted = notCompleted
                        )
                    )
                },
                label = { Text(text = "Completed") },
                selected = filterControls.completed
            )
            FilterChip(
                onClick = {
                    // Turn off opposite filter if it's on
                    val completed = if (!filterControls.notCompleted && filterControls.completed) {
                        false
                    } else {
                        filterControls.completed
                    }

                    onFilterControlsUpdated(
                        filterControls.copy(
                            notCompleted = !filterControls.notCompleted,
                            completed = completed
                        )
                    )
                },
                label = { Text(text = "Not completed") },
                selected = filterControls.notCompleted
            )
        }
        Text(
            text = "Format",
            fontSize = 14.sp,
            color = colorResource(R.color.textSecondaryColor),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
        )
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            FilterChip(
                onClick = {
                    // Turn off opposite filter if it's on
                    val isPhysical = if (!filterControls.isDigital && filterControls.isPhysical) {
                        false
                    } else {
                        filterControls.isPhysical
                    }

                    onFilterControlsUpdated(
                        filterControls.copy(
                            isDigital = !filterControls.isDigital,
                            isPhysical = isPhysical
                        )
                    )
                },
                label = { Text(text = "Digital") },
                selected = filterControls.isDigital
            )
            FilterChip(
                onClick = {
                    // Turn off opposite filter if it's on
                    val isDigital = if (!filterControls.isPhysical && filterControls.isDigital) {
                        false
                    } else {
                        filterControls.isDigital
                    }

                    onFilterControlsUpdated(
                        filterControls.copy(
                            isPhysical = !filterControls.isPhysical,
                            isDigital = isDigital
                        )
                    )
                },
                label = { Text(text = "Physical") },
                selected = filterControls.isPhysical
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .padding(vertical = 10.dp)
        )
        Text(
            text = "Sort By",
            fontSize = 18.sp,
            color = colorResource(R.color.textColorPrimary),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
        )
        AssistChip(
            onClick = {
                onSortControlsUpdated(
                    sortControls.copy(isAscending = !sortControls.isAscending), true
                )
            },
            label = {
                Text(
                    text = if (sortControls.isAscending) "Ascending" else "Descending"
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = if (sortControls.isAscending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = "Localized description",
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        )
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            FilterChip(
                onClick = {
                    onSortControlsUpdated(
                        SortControls(isAlphabetical = true, isAscending = sortControls.isAscending),
                        false
                    )
                },
                label = { Text(text = "Alphabetical") },
                selected = sortControls.isAlphabetical
            )
            FilterChip(
                onClick = {
                    onSortControlsUpdated(
                        SortControls(isCompletion = !sortControls.isCompletion, isAscending = sortControls.isAscending),
                        false
                    )
                },
                label = { Text(text = "Completion") },
                selected = sortControls.isCompletion
            )
            FilterChip(
                onClick = {
                    onSortControlsUpdated(
                        SortControls(isHoursMain = !sortControls.isHoursMain, isAscending = sortControls.isAscending),
                        false
                    )
                },
                label = { Text(text = "Hours (Main)") },
                selected = sortControls.isHoursMain
            )
            FilterChip(
                onClick = {
                    onSortControlsUpdated(
                        SortControls(isHoursExtra = !sortControls.isHoursExtra, isAscending = sortControls.isAscending),
                        false
                    )
                },
                label = { Text(text = "Hours (Main + Extra)") },
                selected = sortControls.isHoursExtra
            )
            FilterChip(
                onClick = {
                    onSortControlsUpdated(
                        SortControls(isHoursCompletionist = !sortControls.isHoursCompletionist, isAscending = sortControls.isAscending),
                        false
                    )
                },
                label = { Text(text = "Hours (Completionist)") },
                selected = sortControls.isHoursCompletionist
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .padding(vertical = 10.dp)
        )
        Text(
            text = "Show info",
            fontSize = 18.sp,
            color = colorResource(R.color.textColorPrimary),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
        )
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            FilterChip(
                onClick = {
                    onShowInfoControlsUpdated(
                        ShowInfoControls(isHoursMain = !showInfoControls.isHoursMain)
                    )
                },
                label = { Text(text = "Hours (Main)") },
                selected = showInfoControls.isHoursMain
            )
            FilterChip(
                onClick = {
                    onShowInfoControlsUpdated(
                        ShowInfoControls(isHoursExtra = !showInfoControls.isHoursExtra)
                    )
                },
                label = { Text(text = "Hours (Main + Extra)") },
                selected = showInfoControls.isHoursExtra
            )
            FilterChip(
                onClick = {
                    onShowInfoControlsUpdated(
                        ShowInfoControls(isHoursCompletionist = !showInfoControls.isHoursCompletionist)
                    )
                },
                label = { Text(text = "Hours (Completionist)") },
                selected = showInfoControls.isHoursCompletionist
            )
        }
    }
}

@Preview
@Composable
private fun AdvancedFiltersDialogPreview() {
    AppTheme {
        AdvancedFiltersDialog(
            filterControls = FilterControls(),
            sortControls = SortControls(),
            showInfoControls = ShowInfoControls()
        )
    }
}