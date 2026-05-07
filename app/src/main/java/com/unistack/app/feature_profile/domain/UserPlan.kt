package com.unistack.app.feature_profile.domain

data class UserPlan(
    val isPro: Boolean,
    val maxSubjects: Int
) {
    val name: String
        get() = if (isPro) "Pro" else "Gratis"

    val hasSubjectLimit: Boolean
        get() = !isPro
}

data class ProBenefit(
    val title: String,
    val description: String
)
