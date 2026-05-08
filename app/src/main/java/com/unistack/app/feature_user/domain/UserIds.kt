package com.unistack.app.feature_user.domain

object UserIds {
    const val LOCAL = "local_user"
    private const val LEGACY_LOCAL = "local-user"

    fun normalize(userId: String): String {
        return when (userId) {
            "", LEGACY_LOCAL -> LOCAL
            else -> userId
        }
    }

    fun storageIdsFor(userId: String): List<String> {
        val normalized = normalize(userId)
        return if (normalized == LOCAL) {
            listOf(LOCAL, LEGACY_LOCAL)
        } else {
            listOf(normalized)
        }
    }
}
