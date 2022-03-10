package com.jeeps.gamecollector.remaster.data.model.data.user

data class User(
    var username: String = "",
    var createdAt: String = "",
    var email: String = "",
    var userId: String = ""
) {
    constructor(userId: String, username: String, email: String) : this() {
        this.userId = userId
        this.username = username
        this.email = email
    }
}