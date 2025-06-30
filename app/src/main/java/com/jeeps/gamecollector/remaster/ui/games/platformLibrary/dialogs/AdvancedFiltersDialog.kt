package com.jeeps.gamecollector.remaster.ui.games.platformLibrary.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Filters",
            fontSize = 25.sp,
            color = colorResource(R.color.textColorPrimary),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
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
                selected = filterControls.completed,
                leadingIcon = {
                    if (filterControls.completed) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                        )
                    }
                }
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
                selected = filterControls.notCompleted,
                leadingIcon = {
                    if (filterControls.notCompleted) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                        )
                    }
                }
            )
            FilterChip(
                onClick = {
                    // Turn off opposite filter if it's on
                    val isPhysical = if (!filterControls.isDigital && filterControls.isPhysical) {
                        false
                    } else {
                        filterControls.notCompleted
                    }

                    onFilterControlsUpdated(
                        filterControls.copy(
                            isDigital = !filterControls.isDigital,
                            isPhysical = isPhysical
                        )
                    )
                },
                label = { Text(text = "Digital") },
                selected = filterControls.isDigital,
                leadingIcon = {
                    if (filterControls.isDigital) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                        )
                    }
                }
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
                selected = filterControls.isPhysical,
                leadingIcon = {
                    if (filterControls.isPhysical) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                        )
                    }
                },
                modifier = Modifier
            )
        }
        AssistChip(
            onClick = onClearFilters,
            label = { Text("Clear filters") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Localized description",
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        )
        Text(
            text = "Sorting",
            fontSize = 25.sp,
            color = colorResource(R.color.textColorPrimary),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )
        Text(
            text = "Order",
            fontSize = 18.sp,
            color = colorResource(R.color.textColorPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
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
        Text(
            text = "Format",
            fontSize = 18.sp,
            color = colorResource(R.color.textColorPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                onClick = {
                    onSortControlsUpdated(
                        SortControls(isDigital = !sortControls.isDigital, isAscending = sortControls.isAscending),
                        false
                    )
                },
                label = { Text("Digital") },
                selected = sortControls.isDigital,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_download_cloud),
                        contentDescription = "Localized description",
                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                    )
                }
            )
            FilterChip(
                onClick = {
                    onSortControlsUpdated(
                        SortControls(isPhysical = !sortControls.isPhysical, isAscending = sortControls.isAscending),
                        false
                    )
                },
                label = { Text("Physical") },
                selected = sortControls.isPhysical,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_physical),
                        contentDescription = "Localized description",
                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                    )
                }
            )
        }
        Text(
            text = "Miscellaneous",
            fontSize = 18.sp,
            color = colorResource(R.color.textColorPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            FilterChip(
                onClick = {
                    onSortControlsUpdated(
                        SortControls(isAlphabetical = !sortControls.isAlphabetical, isAscending = sortControls.isAscending),
                        false
                    )
                },
                label = { Text(text = "Alphabetical") },
                selected = sortControls.isAlphabetical,
                leadingIcon = {
                    if (sortControls.isAlphabetical) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                        )
                    }
                }
            )
            FilterChip(
                onClick = {
                    onSortControlsUpdated(
                        SortControls(isCompletion = !sortControls.isCompletion, isAscending = sortControls.isAscending),
                        false
                    )
                },
                label = { Text(text = "Completion") },
                selected = sortControls.isCompletion,
                leadingIcon = {
                    if (sortControls.isCompletion) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                        )
                    }
                }
            )
            FilterChip(
                onClick = {
                    onSortControlsUpdated(
                        SortControls(isHoursMain = !sortControls.isHoursMain, isAscending = sortControls.isAscending),
                        false
                    )
                },
                label = { Text(text = "Hours (Main)") },
                selected = sortControls.isHoursMain,
                leadingIcon = {
                    if (sortControls.isHoursMain) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                        )
                    }
                }
            )
            FilterChip(
                onClick = {
                    onSortControlsUpdated(
                        SortControls(isHoursExtra = !sortControls.isHoursExtra, isAscending = sortControls.isAscending),
                        false
                    )
                },
                label = { Text(text = "Hours (Main + Extra)") },
                selected = sortControls.isHoursExtra,
                leadingIcon = {
                    if (sortControls.isHoursExtra) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                        )
                    }
                },
                modifier = Modifier
            )
            FilterChip(
                onClick = {
                    onSortControlsUpdated(
                        SortControls(isHoursCompletionist = !sortControls.isHoursCompletionist, isAscending = sortControls.isAscending),
                        false
                    )
                },
                label = { Text(text = "Hours (Completionist)") },
                selected = sortControls.isHoursCompletionist,
                leadingIcon = {
                    if (sortControls.isHoursCompletionist) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                        )
                    }
                },
                modifier = Modifier
            )
        }
        Text(
            text = "Show info",
            fontSize = 25.sp,
            color = colorResource(R.color.textColorPrimary),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            FilterChip(
                onClick = {
                    onShowInfoControlsUpdated(
                        ShowInfoControls(isHoursMain = !showInfoControls.isHoursMain)
                    )
                },
                label = { Text(text = "Hours (Main)") },
                selected = showInfoControls.isHoursMain,
                leadingIcon = {
                    if (showInfoControls.isHoursMain) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                        )
                    }
                }
            )
            FilterChip(
                onClick = {
                    onShowInfoControlsUpdated(
                        ShowInfoControls(isHoursExtra = !showInfoControls.isHoursExtra)
                    )
                },
                label = { Text(text = "Hours (Main + Extra)") },
                selected = showInfoControls.isHoursExtra,
                leadingIcon = {
                    if (showInfoControls.isHoursExtra) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                        )
                    }
                },
                modifier = Modifier
            )
            FilterChip(
                onClick = {
                    onShowInfoControlsUpdated(
                        ShowInfoControls(isHoursCompletionist = !showInfoControls.isHoursCompletionist)
                    )
                },
                label = { Text(text = "Hours (Completionist)") },
                selected = showInfoControls.isHoursCompletionist,
                leadingIcon = {
                    if (showInfoControls.isHoursCompletionist) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize)
                        )
                    }
                },
                modifier = Modifier
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