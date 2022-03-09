package com.jeeps.gamecollector.remaster.data.model.data.platforms

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by jeeps on 12/23/2017.
 */

data class Platform(
    @SerializedName("platformId")
    var id: String = "",
    var user: String = "",
    var name: String = "",
    var imageUri: String = "",
    var color: String = ""
) : Serializable