package com.unistack.app.feature_updates.domain

data class UpdateInfo(
    val versionName: String,
    val releaseNotes: String,
    val releaseDate: String,
    val downloadUrl: String,
    val sizeMb: Double
)
