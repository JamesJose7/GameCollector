package com.jeeps.gamecollector.remaster.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.AppTheme
import com.jeeps.gamecollector.R
import kotlin.math.abs

data class CarouselItem(
    val id: Int,
    @DrawableRes val imageResId: Int,
    val contentDescriptionResId: String
)

private const val NON_FOCUSED_ITEMS_SCALE_REDUCTION = 0.4f
private const val NON_FOCUSED_ITEMS_ALPHA_REDUCTION = 0.6f

@Composable
fun CompletionTimeline(modifier: Modifier = Modifier) {
    val items =
        listOf(
            CarouselItem(0, R.drawable.default_cover, "Something"),
            CarouselItem(1, R.drawable.wii_cover, "Something"),
            CarouselItem(2, R.drawable.ps4_cover, "Something"),
            CarouselItem(3, R.drawable.ds_cover, "Something"),
            CarouselItem(4, R.drawable.switch_cover, "Something"),
            CarouselItem(5, R.drawable.xbox1_cover, "Something"),
            CarouselItem(6, R.drawable.n3ds_cover, "Something"),
            CarouselItem(7, R.drawable.login_cover_art, "Something"),
            CarouselItem(8, R.drawable.wiiu_cover, "Something"),
            CarouselItem(9, R.drawable.switch_cover, "Something"),
        )

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

    LazyRow(
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { layoutCoordinates ->
                containerWidthPx = layoutCoordinates.size.width.toFloat()
                // Subtract the item width to center the item in the middle of the container
                horizontalPaddingPx = (containerWidthPx - itemWidthPx) / 2
            }
    ) {
        itemsIndexed(items) { index, item ->
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
                modifier = Modifier
                    .width(itemWidth)
                    .height(200.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .clip(RoundedCornerShape(12.dp))
            ) {
                GameCard(
                    game = item,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    game: CarouselItem
) {
    Image(
        painter = painterResource(game.imageResId),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@Preview
@Composable
private fun CompletionTimelinePreview() {
    AppTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 20.dp)
            ) {
                CompletionTimeline()
            }
        }
    }
}