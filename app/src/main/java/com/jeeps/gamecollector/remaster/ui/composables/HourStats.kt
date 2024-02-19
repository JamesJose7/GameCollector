package com.jeeps.gamecollector.remaster.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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
import kotlin.math.roundToInt


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
                .padding(vertical = 20.dp, horizontal = 10.dp)
        ) {
            HourStat(label = stringResource(id = R.string.main_story_label), hours = storyHours, modifier = Modifier.padding(bottom = 5.dp))
            HourStat(label = stringResource(id = R.string.main_extra_label), hours = mainExtraHours, modifier = Modifier.padding(bottom = 5.dp))
            HourStat(label = stringResource(id = R.string.completionist_label), hours = completionistHours)

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
private fun HourStat(
    label: String,
    hours: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Text(
            text = label,
            color = colorResource(id = R.color.textColorPrimary),
            fontSize = 17.sp,
            modifier = Modifier.weight(40f)
        )
        Text(
            text = stringResource(id = R.string.hours_template, hours.roundToInt()),
            color = colorResource(id = ColorsUtils.getColorByHoursRange(hours)),
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(60f)
        )
    }
}

@Composable
private fun ErrorMessage(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = R.string.no_hours_stats_error),
        color = colorResource(id = R.color.textSecondaryColor),
        modifier = modifier.fillMaxWidth()
    )
}

@Preview()
@Composable
fun HourStatsPreview() {
    AppTheme {
        HourStats(
            storyHours = 50.0,
            mainExtraHours = 97.0,
            completionistHours = 188.0,
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