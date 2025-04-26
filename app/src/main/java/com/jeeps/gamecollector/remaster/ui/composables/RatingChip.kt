package com.jeeps.gamecollector.remaster.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.AppTheme
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.deprecated.utils.ColorsUtils
import kotlin.math.roundToInt


@Composable
fun RatingChip(
    title: String,
    score: Double,
    reviewCount: Int,
    modifier: Modifier = Modifier
) {
    val chipColorRes by rememberSaveable { mutableIntStateOf(ColorsUtils.getColorByRatingRange(score)) }
    val chipColor = colorResource(id = chipColorRes)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
         modifier = modifier
    ) {
        Title(text = title)
        Chip(score = score.roundToInt(), chipColor, modifier = Modifier.padding(top = 5.dp))
        ReviewCounter(reviewCount = reviewCount)
    }
}

@Composable
private fun Title(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
private fun Chip(
    score: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(70.dp)
            .background(backgroundColor, shape = RoundedCornerShape(percent = 100))
    ) {
        Text(text = score.toString(), textAlign = TextAlign.Center, color = Color.White, fontSize = 29.sp)
    }
}

@Composable
private fun ReviewCounter(
    reviewCount: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$reviewCount reviews",
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        color = colorResource(id = R.color.textSecondaryColor),
        modifier = modifier
            .padding(top = 5.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun RatingChipPreview() {
    AppTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            RatingChip("Critics", 0.0, 10)
            RatingChip("Users", 50.0, 213)
            RatingChip("All", 90.0, 43)
        }
    }
}