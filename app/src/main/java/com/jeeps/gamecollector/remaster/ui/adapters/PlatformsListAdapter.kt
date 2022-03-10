package com.jeeps.gamecollector.remaster.ui.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jeeps.gamecollector.MainLibraryActivity
import com.jeeps.gamecollector.PlatformLibraryActivity
import com.jeeps.gamecollector.R
import com.jeeps.gamecollector.remaster.ui.adapters.PlatformsListAdapter.PlatformsViewHolder
import com.jeeps.gamecollector.databinding.PlatformCardLayoutBinding
import com.jeeps.gamecollector.remaster.data.model.data.platforms.Platform
import com.jeeps.gamecollector.remaster.ui.gamePlatforms.AddPlatformActivity
import com.jeeps.gamecollector.remaster.ui.games.platformLibrary.GamesFromPlatformActivity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by jeeps on 12/23/2017.
 */
@ExperimentalCoroutinesApi
class PlatformsListAdapter(
    private val parentActivity: Activity,
    private val platforms: List<Platform>
) : RecyclerView.Adapter<PlatformsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatformsViewHolder {
        val binding = PlatformCardLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return PlatformsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlatformsViewHolder, position: Int) {
        val platform = platforms[position]
        holder.bind(platform)

        holder.binding.platformCard.setOnClickListener {
            val intent = Intent(parentActivity, GamesFromPlatformActivity::class.java).apply {
                putExtra(PlatformLibraryActivity.CURRENT_PLATFORM, platform.id)
                putExtra(PlatformLibraryActivity.CURRENT_PLATFORM_NAME, platform.name)
            }
            parentActivity.startActivity(intent)
        }
        holder.binding.platformCard.setOnLongClickListener {
            val intent = Intent(parentActivity, AddPlatformActivity::class.java).apply {
                putExtra(AddPlatformActivity.EDITED_PLATFORM, platform)
                putExtra(AddPlatformActivity.EDITED_PLATFORM_POSITION, position)
            }
            parentActivity.startActivityForResult(
                intent,
                MainLibraryActivity.EDIT_PLATFORM_RESULT
            )
            true
        }
    }

    override fun getItemCount(): Int {
        return platforms.size
    }

    inner class PlatformsViewHolder(val binding: PlatformCardLayoutBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(platform: Platform) {
            with(binding) {
                if (platform.color.isNotEmpty()) {
                    platformCardBorder.setBackgroundColor(Color.parseColor(platform.color))
                }

                if (platform.imageUri.isNotEmpty()) {
                    Picasso.get().load(platform.imageUri).into(platformCardImage)
                } else {
                    Picasso.get().load(R.drawable.game_controller).into(platformCardImage)
                }
                platformCardName.text = platform.name
            }
        }
    }
}