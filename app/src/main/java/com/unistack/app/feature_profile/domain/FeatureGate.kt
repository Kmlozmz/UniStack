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

    val proBenefits = listOf(
        ProBenefit(
            title = "Materias ilimitadas",
            description = "Gestiona semestres completos sin el límite del plan gratis."
        ),
        ProBenefit(
            title = "Reportes y exportación",
            description = "Estructura preparada para PDF, resúmenes y evidencias académicas."
        ),
        ProBenefit(
            title = "Simulación avanzada",
            description = "Base lista para escenarios de notas, metas y recuperación."
        ),
        ProBenefit(
            title = "Plantillas académicas",
            description = "Más formatos y estructuras para ensayos, entregas y normas académicas."
        ),
        ProBenefit(
            title = "Recordatorios inteligentes",
            description = "Preparado para notificaciones y alertas prioritarias."
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
