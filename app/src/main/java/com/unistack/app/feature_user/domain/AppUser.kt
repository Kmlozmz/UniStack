package com.unistack.app.feature_user.domain

data class AppUser(
    val userId: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?
)
