package com.unistack.app.feature_profile.domain

object FeatureGate {
    const val PRO_FEATURES_ENABLED = false

    val freePlan = UserPlan(
        isPro = false,
        maxSubjects = 5
    )

    val proPreviewPlan = UserPlan(
        isPro = true,
        maxSubjects = Int.MAX_VALUE
    )

    fun planFor(isPro: Boolean): UserPlan {
        if (!PRO_FEATURES_ENABLED) return proPreviewPlan
        return if (isPro) proPreviewPlan else freePlan
    }

    val proBenefits = listOf(
        ProBenefit(
            title = "Materias ilimitadas",
            description = "Gestiona semestres completos sin el límite del plan gratis."
        )
    )

    fun canCreateSubject(plan: UserPlan, currentSubjectCount: Int): Boolean {
        if (!PRO_FEATURES_ENABLED) return true
        return plan.isPro || currentSubjectCount < plan.maxSubjects
    }

    fun remainingSubjects(plan: UserPlan, currentSubjectCount: Int): Int? {
        if (!PRO_FEATURES_ENABLED) return null
        if (!plan.hasSubjectLimit) return null
        return (plan.maxSubjects - currentSubjectCount).coerceAtLeast(0)
    }
}
