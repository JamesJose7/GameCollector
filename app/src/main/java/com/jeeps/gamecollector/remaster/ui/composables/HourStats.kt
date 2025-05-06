package com.jeeps.gamecollector.remaster.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.AppTheme
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.deprecated.utils.ColorsUtils
import com.jeeps.gamecollector.remaster.ui.games.details.SectionTitle
import kotlin.math.max
import kotlin.math.roundToInt

private const val RANGE_LOW = 16.0
private const val RANGE_MED = 30.0
private const val RANGE_HIGH = 60.0

@Composable
fun HourStats(
    storyHours: Double,
    mainExtraHours: Double,
    completionistHours: Double,
    isLoadingStats: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
    onRefreshClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
        // TODO: set up light and dark mode colors
        //    .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            SectionTitle(
                text = stringResource(id = R.string.hours_stats),
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )
            HoursBarBreakdown(
                storyHours = storyHours,
                mainExtraHours = mainExtraHours,
                completionistHours = completionistHours,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            HourStat(
                label = stringResource(id = R.string.main_story_label),
                hours = storyHours,
                legendColor = colorResource(R.color.rating_range_80),
                modifier = Modifier.padding(bottom = 5.dp)
            )
            HourStat(
                label = stringResource(id = R.string.main_extra_label),
                hours = mainExtraHours,
                legendColor = colorResource(R.color.rating_range_40),
                modifier = Modifier.padding(bottom = 5.dp)
            )
            HourStat(
                label = stringResource(id = R.string.completionist_label),
                hours = completionistHours,
                legendColor = colorResource(R.color.rating_range_0)
            )

            if (isError) {
                ErrorMessage(modifier = Modifier.padding(top = 10.dp))
            }
        }
        if (isLoadingStats) {
            CircularProgressIndicator(
                color = colorResource(id = R.color.colorAccent),
                strokeWidth = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(30.dp)
                    .padding(5.dp)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_refresh),
                colorFilter = ColorFilter.tint(color = colorResource(id = R.color.textSecondaryColor)),
                contentDescription = stringResource(id = R.string.content_description_refresh_icon),
                modifier = Modifier
                    .clickable { onRefreshClick() }
                    .size(30.dp)
                    .padding(5.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun HoursBarBreakdown(
    modifier: Modifier = Modifier,
    storyHours: Double,
    mainExtraHours: Double,
    completionistHours: Double
) {
    val hoursTotal = max(max(storyHours, mainExtraHours), completionistHours)
    val storyPercentage = (storyHours / hoursTotal).coerceIn(0.0, 1.0)
    val mainExtraPercentage = (mainExtraHours / hoursTotal).coerceIn(0.0, 1.0)
    val completionistPercentage = (completionistHours / hoursTotal).coerceIn(0.0, 1.0)

    Box(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), shape = CircleShape)
            .height(8.dp)
            .fillMaxWidth()
    ) {
        if (hoursTotal > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(color = colorResource(R.color.rating_range_0), shape = CircleShape)
                    .fillMaxWidth(completionistPercentage.toFloat())
            )
        }
        if (mainExtraHours > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(color = colorResource(R.color.rating_range_40), shape = CircleShape)
                    .fillMaxWidth(mainExtraPercentage.toFloat())
            )
        }
        if (storyPercentage > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(color = colorResource(R.color.rating_range_80), shape = CircleShape)
                    .fillMaxWidth(storyPercentage.toFloat())
            )
        }
    }
}

@Composable
private fun HourStat(
    label: String,
    hours: Double,
    legendColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = legendColor, shape = CircleShape)
        )
        Text(
            text = label,
            color = colorResource(id = R.color.textColorPrimary),
            fontSize = 15.sp,
            modifier = Modifier
                .weight(40f)
                .padding(start = 4.dp)
        )
        Text(
            text = stringResource(id = R.string.hours_template, hours.roundToInt()),
//            color = colorResource(id = ColorsUtils.getColorByHoursRange(hours)),
            color = colorResource(id = R.color.textColorPrimary),
            fontSize = 15.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier
                .weight(60f)
                .padding(end = 4.dp)
        )
        when {
            hours < RANGE_LOW -> {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null,
                    tint = colorResource(id = ColorsUtils.getColorByHoursRange(hours)),
                    modifier = Modifier.size(20.dp)
                )
            }
            hours in RANGE_LOW..<RANGE_MED -> {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    tint = colorResource(id = ColorsUtils.getColorByHoursRange(hours)),
                    modifier = Modifier.size(20.dp)
                )
            }
            hours in RANGE_MED..<RANGE_HIGH -> {
                Icon(
                    imageVector = Icons.Filled.KeyboardDoubleArrowUp,
                    contentDescription = null,
                    tint = colorResource(id = ColorsUtils.getColorByHoursRange(hours)),
                    modifier = Modifier.size(20.dp)
                )
            }
            hours >= RANGE_HIGH -> {
                Icon(
                    painter = painterResource(R.drawable.arrow_triple),
                    contentDescription = null,
                    tint = colorResource(id = ColorsUtils.getColorByHoursRange(hours)),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = R.string.no_hours_stats_error),
        color = colorResource(id = R.color.textSecondaryColor),
        fontSize = 15.sp,
        modifier = modifier.fillMaxWidth()
    )
}

@Preview()
@Composable
fun HourStatsPreview() {
    AppTheme {
        HourStats(
            storyHours = 20.0,
            mainExtraHours = 50.0,
            completionistHours = 188.0,
            isLoadingStats = false,
            isError = false
        )
    }
}

@Preview()
@Composable
fun HourStatsNoDataPreview() {
    AppTheme {
        HourStats(
            storyHours = 0.0,
            mainExtraHours = 0.0,
            completionistHours = 0.0,
            isLoadingStats = false,
            isError = false
        )
    }
}

@Preview()
@Composable
fun HourStatsLoadingPreview() {
    AppTheme {
        HourStats(
            storyHours = 50.0,
            mainExtraHours = 97.0,
            completionistHours = 188.0,
            isLoadingStats = true,
            isError = false
        )
    }
}

@Preview()
@Composable
fun HourStatsErrorPreview() {
    AppTheme {
        HourStats(
            storyHours = 50.0,
            mainExtraHours = 97.0,
            completionistHours = 188.0,
            isLoadingStats = false,
            isError = true
        )
    }
}