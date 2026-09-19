package com.unistack.app.feature_home.presentation

import com.unistack.app.feature_home.domain.HomeContent as DomainHomeContent
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.HomeSummaryFactory as DomainHomeSummaryFactory
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.UserProfile

typealias HomeContent = DomainHomeContent

/**
 * Delegado de compatibilidad hacia [DomainHomeSummaryFactory].
 * La lógica de negocio y agregación de inicio reside ahora en la capa domain.
 */
object HomeSummaryFactory {
    fun create(
        content: DomainHomeContent,
        profile: UserProfile?,
        user: AppUser
    ): HomeSummary = DomainHomeSummaryFactory.create(content, profile, user)
}
