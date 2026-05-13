package com.unistack.app.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextValidatorsTest {

    @Test
    fun `normalize text trims and collapses whitespace`() {
        assertEquals("Ana Maria", TextValidators.normalizeText("  Ana   Maria  "))
    }

    @Test
    fun `display name requires letters and minimum length`() {
        assertTrue(TextValidators.validateDisplayName("Lu").isValid)
        assertFalse(TextValidators.validateDisplayName("L").isValid)
        assertFalse(TextValidators.validateDisplayName("1234").isValid)
    }

    @Test
    fun `subject and activity names reject repetitive text`() {
        assertTrue(TextValidators.validateSubjectName("Calculo integral").isValid)
        assertTrue(TextValidators.validateActivityName("Parcial final").isValid)
        assertFalse(TextValidators.validateSubjectName("aaaa").isValid)
        assertFalse(TextValidators.validateActivityName("!!!!").isValid)
    }

    @Test
    fun `custom career enforces longer meaningful values`() {
        assertTrue(TextValidators.validateCustomCareer("Ingenieria de datos").isValid)
        assertFalse(TextValidators.validateCustomCareer("Ing").isValid)
    }
}
