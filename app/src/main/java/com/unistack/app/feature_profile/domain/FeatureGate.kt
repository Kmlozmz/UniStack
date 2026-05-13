package com.unistack.app.feature_profile.domain

object FeatureGate {
    val freePlan = UserPlan(
        isPro = false,
        maxSubjects = 5
    )

    val proPreviewPlan = UserPlan(
        isPro = true,
        maxSubjects = Int.MAX_VALUE
    )

    fun planFor(isPro: Boolean): UserPlan {
        return if (isPro) proPreviewPlan else freePlan
    }

    val proBenefits = listOf(
        ProBenefit(
            title = "Materias ilimitadas",
            description = "Gestiona semestres completos sin el límite del plan gratis."
        )
    )

    fun canCreateSubject(plan: UserPlan, currentSubjectCount: Int): Boolean {
        return plan.isPro || currentSubjectCount < plan.maxSubjects
    }

    fun remainingSubjects(plan: UserPlan, currentSubjectCount: Int): Int? {
        if (!plan.hasSubjectLimit) return null
        return (plan.maxSubjects - currentSubjectCount).coerceAtLeast(0)
    }
}
