package com.jeeps.gamecollector.remaster.data.model.data.igdb

data class GenreIg(
    var name: String = ""
)

fun List<GenreIg>.toNames() = map { it.name }