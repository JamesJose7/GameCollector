package com.jeeps.gamecollector.deprecated.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.deprecated.adapters.GameCardAdapter.MyViewHolder
import com.jeeps.gamecollector.databinding.GameCardLayoutBinding
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.games.SortStat
import com.jeeps.gamecollector.deprecated.utils.ColorsUtils
import com.jeeps.gamecollector.deprecated.utils.FormatUtils
import com.jeeps.gamecollector.remaster.utils.extensions.setComposable
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

/**
 * Created by jeeps on 12/25/2017.
 */
class GameCardAdapter(
    val games: MutableList<Game>,
    val listener: GameCardAdapterListener
) : RecyclerView.Adapter<MyViewHolder>() {

    private var sortStat = SortStat.NONE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = GameCardLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val game = games[position]
        holder.bind(game)

        holder.itemView.setOnLongClickListener {
            listener.deleteSelectedGame(position)
            true
        }

        holder.itemView.setOnClickListener {
            listener.editGame(
                position,
                holder.binding.cardGameCover,
                holder.binding.cardGameTitle
            )
        }
    }

    override fun getItemCount(): Int {
        return games.size
    }

    fun setGames(games: List<Game>) {
        this.games.clear()
        this.games.addAll(games)
        notifyDataSetChanged()
    }

    fun setSortStat(sortStat: SortStat) {
        this.sortStat = sortStat
    }

    fun removeGameAtPosition(position: Int) {
        games.removeAt(position)
        notifyItemRemoved(position)
    }

    fun addGameAtPosition(position: Int, selectedGame: Game) {
        games.add(position, selectedGame)
        notifyItemInserted(position)
    }

    interface GameCardAdapterListener {
        fun deleteSelectedGame(position: Int)
        fun editGame(position: Int, imageView: View, titleView: TextView)
    }

    inner class MyViewHolder(val binding: GameCardLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: Game) {
            with(binding) {
                //Set progressbar animation
                coverProgressbar.setComposable {
                    LoadingAnimation()
                }

                //Display progressbar
                coverProgressbar.visibility = View.VISIBLE
                //load image cover
                if (game.imageUri.isNotEmpty()) {
                    Picasso.get().load(game.imageUri).into(cardGameCover, object : Callback {
                        override fun onSuccess() {
                            //Hide progress bar when image is finished loading
                            coverProgressbar.visibility = View.INVISIBLE
                        }

                        override fun onError(e: Exception) {
                            //Hide progress bar when image is finished loading
                            coverProgressbar.visibility = View.INVISIBLE
                            Picasso.get().load(R.drawable.game_controller).into(cardGameCover)
                        }
                    })
                } else {
                    coverProgressbar.visibility = View.INVISIBLE
                    Picasso.get().load(R.drawable.game_controller).into(cardGameCover)
                }
                //Display logo if it's digital
                if (game.isPhysical)
                    cardIsDigital.visibility = View.GONE
                else
                    cardIsDigital.visibility = View.VISIBLE

                //Name
                cardGameTitle.text = game.shortName.ifEmpty { game.name }

                //turn green check if game is completed
                if (game.timesCompleted > 0)
                    cardGameCompleted.setColorFilter(Color.parseColor("#7FFF00"))
                else
                    cardGameCompleted.setColorFilter(Color.parseColor("#cccccc"))

                // Sort stats
                when (sortStat) {
                    SortStat.HOURS_MAIN -> {
                        sortStatCard.visibility = View.VISIBLE
                        val gameplayMainExtra: Double = game.gameHoursStats.gameplayMain
                        formatHourStat(sortStatTv, gameplayMainExtra)
                    }
                    SortStat.HOURS_MAIN_EXTRA -> {
                        sortStatCard.visibility = View.VISIBLE
                        val gameplayMainExtra: Double = game.gameHoursStats.gameplayMainExtra
                        formatHourStat(sortStatTv, gameplayMainExtra)
                    }
                    SortStat.HOURS_COMPLETIONIST -> {
                        sortStatCard.visibility = View.VISIBLE
                        val gameplayMainExtra: Double = game.gameHoursStats.gameplayCompletionist
                        formatHourStat(sortStatTv, gameplayMainExtra)
                    }
                    SortStat.NONE -> {
                        sortStatCard.visibility = View.GONE
                    }
                }
            }
        }

        private fun formatHourStat(sortStatTv: TextView, gameplayMainExtra: Double) {
            sortStatTv.text = sortStatTv.context.getString(
                R.string.hours_template,
                FormatUtils.formatDecimal(gameplayMainExtra)
            )
            sortStatTv.setTextColor(
                ColorsUtils.getColorByHoursRange(
                    sortStatTv.context, gameplayMainExtra
                )
            )
        }
    }
}

@Composable
private fun LoadingAnimation(
    modifier: Modifier = Modifier,
    animationReps: Int = LottieConstants.IterateForever
) {
    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.three_dot_loading
        )
    )

    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                MaterialTheme.colorScheme.tertiary.hashCode(),
                BlendModeCompat.SRC_ATOP
            ),
            keyPath = arrayOf(
                "**"
            )
        )
    )

    LottieAnimation(
        composition = preloaderLottieComposition,
        iterations = animationReps,
        dynamicProperties = dynamicProperties,
        modifier = modifier
    )
}