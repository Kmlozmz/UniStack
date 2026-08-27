package com.unistack.app.core.navigation

import com.unistack.app.feature_user.domain.AppModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationRulesTest {

    @Test
    fun bottomRouteForMapsNestedRoutesToTheirTab() {
        assertEquals(AppRoutes.Academic, bottomRouteFor("${AppRoutes.SubjectDetail}/subject-1"))
        assertEquals(AppRoutes.Academic, bottomRouteFor("${AppRoutes.SubjectCutDetail}/subject-1/period-1"))
        assertEquals(AppRoutes.Academic, bottomRouteFor("${AppRoutes.EditGrade}/subject-1/grade-1"))
        assertEquals(AppRoutes.Academic, bottomRouteFor("${AppRoutes.EditTask}/task-1"))
        assertEquals(AppRoutes.Calendar, bottomRouteFor(AppRoutes.Calendar))
        assertEquals(AppRoutes.Expenses, bottomRouteFor(AppRoutes.AddExpense))
        assertEquals(AppRoutes.Expenses, bottomRouteFor("${AppRoutes.EditExpense}/expense-1"))
        // La quinta pestaña es Configuración, y el perfil pasó a ser una pantalla dentro
        // de ella: las dos, y todo lo que cuelga de ajustes, pertenecen a esa pestaña.
        assertEquals(AppRoutes.Settings, bottomRouteFor(AppRoutes.Settings))
        assertEquals(AppRoutes.Settings, bottomRouteFor(AppRoutes.Profile))
        assertEquals(AppRoutes.Settings, bottomRouteFor(AppRoutes.AppearanceSettings))
        assertEquals(AppRoutes.Settings, bottomRouteFor(AppRoutes.Pro))
        assertEquals(AppRoutes.Home, bottomRouteFor(AppRoutes.AcademicTemplates))
    }

    @Test
    fun academicWithItsTabArgumentStillBelongsToItsTab() {
        // Navigation informa el patrón, no la URL rellena: lo que llega es
        // «academic?tab={tab}». Si el reconocimiento de rutas no contempla el argumento
        // opcional, Académico deja de pertenecer a su pestaña y se queda sin barra.
        assertEquals(AppRoutes.Academic, bottomRouteFor(AppRoutes.AcademicWithTab))
        assertTrue(routeShowsBottomBar(AppRoutes.AcademicWithTab))
        assertEquals(AppRoutes.Academic, bottomRouteFor(AppRoutes.academic(AppRoutes.AcademicTabTasks)))
    }

    @Test
    fun theOldStandaloneRoutesStillResolve() {
        // Siguen existiendo como redirección: hay recordatorios ya programados que llevan
        // la cadena guardada dentro y apuntarían a la nada si se borraran.
        assertEquals(AppRoutes.Academic, bottomRouteFor(AppRoutes.Grades))
        assertEquals(AppRoutes.Academic, bottomRouteFor(AppRoutes.Tasks))
    }

    @Test
    fun browsingScreensKeepTheBottomBar() {
        // Las pantallas de consulta la conservan, estén al nivel que estén. El detalle de
        // una materia es el caso que motivó el cambio: se quedaba sin barra mientras su
        // propia lista sí la tenía.
        assertTrue(routeShowsBottomBar(AppRoutes.Home))
        assertTrue(routeShowsBottomBar(AppRoutes.Academic))
        assertTrue(routeShowsBottomBar("${AppRoutes.SubjectDetail}/subject-1"))
        assertTrue(routeShowsBottomBar("${AppRoutes.SubjectCutDetail}/subject-1/period-1"))
        assertTrue(routeShowsBottomBar(AppRoutes.PriorHistory))
        assertTrue(routeShowsBottomBar(AppRoutes.Notifications))
        assertTrue(routeShowsBottomBar(AppRoutes.AcademicTemplates))
        assertTrue(routeShowsBottomBar(AppRoutes.Settings))
    }

    @Test
    fun formsHideTheBottomBar() {
        // Crear y editar son tareas, no destinos: con la barra puesta se abandona un
        // formulario a medio llenar de un solo toque.
        assertFalse(routeShowsBottomBar(AppRoutes.AddSubject))
        assertFalse(routeShowsBottomBar("${AppRoutes.EditSubject}/subject-1"))
        assertFalse(routeShowsBottomBar(AppRoutes.AddSubjectFromSchedule))
        assertFalse(routeShowsBottomBar("${AppRoutes.EditSubjectFromSchedule}/subject-1"))
        assertFalse(routeShowsBottomBar("${AppRoutes.AddGrade}/subject-1"))
        assertFalse(routeShowsBottomBar("${AppRoutes.AddGradeFromHistory}/subject-1"))
        assertFalse(routeShowsBottomBar("${AppRoutes.EditGrade}/subject-1/grade-1"))
        assertFalse(routeShowsBottomBar(AppRoutes.AddTask))
        assertFalse(routeShowsBottomBar("${AppRoutes.EditTask}/task-1"))
        assertFalse(routeShowsBottomBar(AppRoutes.AddExpense))
        assertFalse(routeShowsBottomBar("${AppRoutes.EditExpense}/expense-1"))
    }

    @Test
    fun everyRouteThatShowsTheBarKnowsItsTab() {
        // La regla se deriva del mapa, así que no puede haber una pantalla con barra sin
        // pestaña que encender. Si alguien añade una ruta y olvida mapearla, aquí se ve.
        val routes = listOf(
            AppRoutes.Home,
            AppRoutes.Academic,
            AppRoutes.Grades,
            AppRoutes.Tasks,
            AppRoutes.Calendar,
            AppRoutes.Expenses,
            AppRoutes.Profile,
            AppRoutes.PriorHistory,
            AppRoutes.Notifications,
            AppRoutes.AcademicTemplates
        )
        routes.forEach { route ->
            assertTrue("$route debería mostrar barra", routeShowsBottomBar(route))
            assertNotNull("$route debería tener pestaña", bottomRouteFor(route))
        }
    }

    @Test
    fun theSubjectFormOpenedFromScheduleBelongsToSchedule() {
        // Es el mismo formulario que el de Académico, pero se entra y se sale por Horario.
        assertEquals(AppRoutes.Calendar, bottomRouteFor(AppRoutes.AddSubjectFromSchedule))
        assertEquals(AppRoutes.Calendar, bottomRouteFor("${AppRoutes.EditSubjectFromSchedule}/subject-1"))
        // Y las rutas de Académico no se lo quedan por parecido de nombre: «add_subject» es
        // prefijo literal de «add_subject_from_schedule».
        assertEquals(AppRoutes.Academic, bottomRouteFor(AppRoutes.AddSubject))
    }

    @Test
    fun creatingAClassDoesNotDependOnTheGradesModule() {
        // Crear una clase crea una materia por debajo, pero Horario sigue siendo pestaña con
        // Académico apagado. Atarlas a GRADES convertiría «Añadir clase» en un botón que
        // lleva a Inicio.
        assertNull(moduleForRoute(AppRoutes.AddSubjectFromSchedule))
        assertNull(moduleForRoute("${AppRoutes.EditSubjectFromSchedule}/subject-1"))
        assertEquals(AppModule.GRADES, moduleForRoute(AppRoutes.AddSubject))
    }

    @Test
    fun moduleForRouteMapsProtectedRoutes() {
        assertEquals(AppModule.GRADES, moduleForRoute("${AppRoutes.AddGrade}/subject-1"))
        assertEquals(AppModule.GRADES, moduleForRoute("${AppRoutes.SubjectCutDetail}/subject-1/period-1"))
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
