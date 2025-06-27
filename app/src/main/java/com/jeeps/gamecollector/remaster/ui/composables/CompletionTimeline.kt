package com.jeeps.gamecollector.remaster.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jeeps.gamecollector.remaster.ui.theme.AppTheme
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.abs

data class CarouselItem(
    val id: Int,
    @DrawableRes val imageResId: Int,
    val contentDescriptionResId: String
)

private const val NON_FOCUSED_ITEMS_SCALE_REDUCTION = 0.4f
private const val NON_FOCUSED_ITEMS_ALPHA_REDUCTION = 0.6f

sealed class GameTimelineItem {
    data class GameItem(val game: Game): GameTimelineItem()
    data class MonthSeparator(val yearMonth: YearMonth): GameTimelineItem()
}

@Composable
fun CompletionTimeline(
    modifier: Modifier = Modifier,
    games: List<Game>,
    selectedGame: Game? = null
) {
    var gameItems: List<GameTimelineItem> by remember { mutableStateOf(emptyList()) }

    val listState = rememberLazyListState()
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
    val density = LocalDensity.current
    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    val itemWidth = 140.dp
    val itemWidthPx = with(density) { itemWidth.toPx() }
    var horizontalPaddingPx by remember { mutableFloatStateOf(0f) }
    val horizontalPadding = with(density) { horizontalPaddingPx.toDp() }

    // Snap to center
    val flingBehavior = rememberSnapFlingBehavior(
        lazyListState = listState
    )

    LaunchedEffect(games) {
        gameItems = games
            .filter { it.completionDateParsed != null }
            .sortedBy { it.completionDateParsed }
            .groupBy { YearMonth.from(it.completionDateParsed) }
            .flatMap { (month, games) ->
                listOf(GameTimelineItem.MonthSeparator(month)) +
                        games.map { GameTimelineItem.GameItem(it) }
            }
    }

    LaunchedEffect(selectedGame, gameItems) {
        if (gameItems.isNotEmpty() && selectedGame != null) {
            val index = gameItems.indexOfFirst { it is GameTimelineItem.GameItem && it.game.id == selectedGame.id }
            if (index != -1) {
                delay(100)
                listState.animateScrollToItem(index)
            }
        }
    }

    LazyRow(
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { layoutCoordinates ->
                containerWidthPx = layoutCoordinates.size.width.toFloat()
                // Subtract the item width to center the item in the middle of the container
                horizontalPaddingPx = (containerWidthPx - itemWidthPx) / 2
            }
    ) {
        itemsIndexed(
            items = gameItems,
            key = { _, item ->
                when (item) {
                    is GameTimelineItem.GameItem -> "game_${item.game.id}"
                    is GameTimelineItem.MonthSeparator -> "month_${item.yearMonth}"
                }
            },
            contentType = { _, item ->
                when (item) {
                    is GameTimelineItem.GameItem -> GameTimelineItem.GameItem::class
                    is GameTimelineItem.MonthSeparator -> GameTimelineItem.MonthSeparator::class
                }
            }
        ) { index, item ->
            val visibleItemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }

            // Default scale/alpha values
            var scale = 0.6f
            var alpha = 0.6f
            visibleItemInfo?.let { itemInfo ->
                // The offset does not include content padding, so it needs to be added in order
                // for the center item to be highlighted
                val itemCenter = (horizontalPaddingPx + itemInfo.offset) + itemInfo.size / 2
                val viewportCenter = containerWidthPx / 2
                val distanceFromCenter = abs(itemCenter - viewportCenter)
                val normDistance = (distanceFromCenter / viewportCenter).coerceIn(0f, 1f)

                scale = 1f - (NON_FOCUSED_ITEMS_SCALE_REDUCTION * normDistance)
                alpha = 1f - (NON_FOCUSED_ITEMS_ALPHA_REDUCTION * normDistance)
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(itemWidth)
                    .height(250.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
            ) {
                when (item) {
                    is GameTimelineItem.GameItem -> {
                        GameCard(
                            game = item.game,
                            modifier = Modifier
                        )
                    }

                    is GameTimelineItem.MonthSeparator -> {
                        MonthCard(
                            yearMonth = item.yearMonth,
                            modifier = Modifier
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    game: Game
) {
    Column(
        modifier = modifier
    ) {
        AsyncImage(
            model = game.imageUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            fallback = painterResource(R.drawable.game_controller),
            placeholder = painterResource(R.drawable.game_controller),
            modifier = Modifier
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
        )
        BasicText(
            text = game.completionDateParsed?.let { formatDateDifference(it) }.orEmpty(),
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium.copy(
                textAlign = TextAlign.Center,
            ),
            autoSize = TextAutoSize.StepBased(
                minFontSize = 12.sp,
                maxFontSize = 17.sp,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        Text(
            text = game.completionDateFormatted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MonthCard(
    modifier: Modifier = Modifier,
    yearMonth: YearMonth
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .size(140.dp)
                .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp))
        ) {
            Text(
                text = yearMonth.month.toString(),
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
            )
            Text(
                text = yearMonth.year.toString(),
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
            )
        }
    }
}

private fun formatDateDifference(date: LocalDate): String {
    val today = LocalDate.now()

    if (date.isAfter(today)) return "in the future"

    val daysBetween = ChronoUnit.DAYS.between(date, today)
    return when {
        daysBetween < 1 -> "today"
        daysBetween < 365 -> "$daysBetween days ago"
        else -> {
            val period = Period.between(date, today)
            val years = period.years
            val months = period.months

            when {
                months == 0 -> "$years year${if (years > 1) "s" else ""} ago"
                else -> "$years year${if (years > 1) "s" else ""}, $months month${if (months > 1) "s" else ""} ago"
            }
        }
    }
}

@Preview
@Composable
private fun CompletionTimelinePreview() {
    val games = listOf(
        Game(
            id = "1",
            imageUri = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1qv5.jpg",
            completionDate = "2024-11-22T02:32:04.808Z"
        ),
        Game(
            id = "2",
            imageUri = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1qv5.jpg",
            completionDate = "2024-10-22T02:32:04.808Z"
        ),
        Game(
            id = "3",
            imageUri = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1qv5.jpg",
            completionDate = "2024-12-22T02:32:04.808Z"
        ),
        Game(
            id = "4",
            imageUri = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1qv5.jpg",
            completionDate = "2024-10-22T02:32:04.808Z"
        ),
        Game(
            id = "5",
            imageUri = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1qv5.jpg",
            completionDate = "2024-10-22T02:32:04.808Z"
        ),
        Game(
            id = "6",
            imageUri = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1qv5.jpg",
            completionDate = "2023-10-22T02:32:04.808Z"
        ),
        Game(
            id = "7",
            imageUri = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1qv5.jpg",
            completionDate = "2022-10-22T02:32:04.808Z"
        ),
        Game(
            id = "8",
            imageUri = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1qv5.jpg",
            completionDate = "2024-09-22T02:32:04.808Z"
        ),
        Game(
            id = "9",
            imageUri = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1qv5.jpg",
            completionDate = "2024-09-22T02:32:04.808Z"
        ),
        Game(
            id = "10",
            imageUri = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1qv5.jpg",
            completionDate = "2024-10-22T02:32:04.808Z"
        )
    )


    AppTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 20.dp)
            ) {
                CompletionTimeline(
                    games = games
                )
            }
        }
    }
}