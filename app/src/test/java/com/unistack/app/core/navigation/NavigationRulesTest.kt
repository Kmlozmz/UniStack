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
        assertEquals(AppRoutes.Grades, bottomRouteFor("${AppRoutes.SubjectDetail}/subject-1"))
        assertEquals(AppRoutes.Grades, bottomRouteFor("${AppRoutes.EditGrade}/subject-1/grade-1"))
        assertEquals(AppRoutes.Tasks, bottomRouteFor("${AppRoutes.EditTask}/task-1"))
        assertEquals(AppRoutes.Expenses, bottomRouteFor(AppRoutes.AddExpense))
        assertEquals(AppRoutes.Expenses, bottomRouteFor("${AppRoutes.EditExpense}/expense-1"))
        assertEquals(AppRoutes.Home, bottomRouteFor(AppRoutes.Profile))
        assertEquals(AppRoutes.Home, bottomRouteFor(AppRoutes.Pro))
        assertEquals(AppRoutes.Home, bottomRouteFor(AppRoutes.AcademicTemplates))
    }

    @Test
    fun moduleForRouteMapsProtectedRoutes() {
        assertEquals(AppModule.GRADES, moduleForRoute("${AppRoutes.AddGrade}/subject-1"))
        assertEquals(AppModule.TASKS, moduleForRoute(AppRoutes.AddTask))
        assertEquals(AppModule.EXPENSES, moduleForRoute("${AppRoutes.EditExpense}/expense-1"))
        assertEquals(AppModule.ACADEMIC_TEMPLATES, moduleForRoute(AppRoutes.AcademicTemplates))
        assertNull(moduleForRoute(AppRoutes.Profile))
    }

    @Test
    fun bottomNavigationDoesNotRestoreHomeChildRoutes() {
        assertFalse(shouldRestoreBottomRouteState(AppRoutes.AddTask, AppRoutes.Home))
        assertFalse(shouldRestoreBottomRouteState(AppRoutes.AcademicTemplates, AppRoutes.Home))
        assertTrue(shouldRestoreBottomRouteState(AppRoutes.Home, AppRoutes.Tasks))
    }

    @Test
    fun selectedBottomRoutePopsToItsRoot() {
        assertTrue(shouldPopSelectedBottomRoute(AppRoutes.AddTask, AppRoutes.Tasks))
        assertTrue(shouldPopSelectedBottomRoute(AppRoutes.AcademicTemplates, AppRoutes.Home))
        assertFalse(shouldPopSelectedBottomRoute(AppRoutes.Home, AppRoutes.Home))
    }
}
