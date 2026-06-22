package com.unistack.app.core.navigation

import com.unistack.app.feature_user.domain.AppModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationRulesTest {

    @Test
    fun bottomRouteForMapsNestedRoutesToTheirTab() {
        assertEquals(AppRoutes.Academic, bottomRouteFor("${AppRoutes.SubjectDetail}/subject-1"))
        assertEquals(AppRoutes.Academic, bottomRouteFor("${AppRoutes.SubjectPeriodDetail}/subject-1/period-1"))
        assertEquals(AppRoutes.Academic, bottomRouteFor("${AppRoutes.EditGrade}/subject-1/grade-1"))
        assertEquals(AppRoutes.Academic, bottomRouteFor("${AppRoutes.EditTask}/task-1"))
        assertEquals(AppRoutes.Calendar, bottomRouteFor(AppRoutes.Calendar))
        assertEquals(AppRoutes.Expenses, bottomRouteFor(AppRoutes.AddExpense))
        assertEquals(AppRoutes.Expenses, bottomRouteFor("${AppRoutes.EditExpense}/expense-1"))
        assertEquals(AppRoutes.Profile, bottomRouteFor(AppRoutes.Profile))
        assertEquals(AppRoutes.Profile, bottomRouteFor(AppRoutes.Pro))
        assertEquals(AppRoutes.Home, bottomRouteFor(AppRoutes.AcademicTemplates))
    }

    @Test
    fun moduleForRouteMapsProtectedRoutes() {
        assertEquals(AppModule.GRADES, moduleForRoute("${AppRoutes.AddGrade}/subject-1"))
        assertEquals(AppModule.GRADES, moduleForRoute("${AppRoutes.SubjectPeriodDetail}/subject-1/period-1"))
        assertEquals(AppModule.TASKS, moduleForRoute(AppRoutes.AddTask))
        assertEquals(AppModule.EXPENSES, moduleForRoute("${AppRoutes.EditExpense}/expense-1"))
        assertEquals(AppModule.ACADEMIC_TEMPLATES, moduleForRoute(AppRoutes.AcademicTemplates))
        assertNull(moduleForRoute(AppRoutes.Profile))
    }

    @Test
    fun bottomNavigationDoesNotRestoreHomeChildRoutes() {
        assertFalse(shouldRestoreBottomRouteState(AppRoutes.AddTask, AppRoutes.Home))
        assertFalse(shouldRestoreBottomRouteState(AppRoutes.AcademicTemplates, AppRoutes.Home))
        assertTrue(shouldRestoreBottomRouteState(AppRoutes.Home, AppRoutes.Academic))
    }

    @Test
    fun selectedBottomRoutePopsToItsRoot() {
        assertTrue(shouldPopSelectedBottomRoute(AppRoutes.AddTask, AppRoutes.Academic))
        assertTrue(shouldPopSelectedBottomRoute(AppRoutes.AcademicTemplates, AppRoutes.Home))
        assertFalse(shouldPopSelectedBottomRoute(AppRoutes.Home, AppRoutes.Home))
    }

    @Test
    fun slideDirectionFollowsTabOrderAndChildDepth() {
        assertTrue(isForwardNavigation(AppRoutes.Home, AppRoutes.Academic))
        assertTrue(isForwardNavigation(AppRoutes.Academic, AppRoutes.Calendar))
        assertFalse(isForwardNavigation(AppRoutes.Expenses, AppRoutes.Academic))
        assertTrue(isForwardNavigation(AppRoutes.Home, AppRoutes.Profile))
        assertFalse(isForwardNavigation(AppRoutes.Profile, AppRoutes.Home))
    }
}
