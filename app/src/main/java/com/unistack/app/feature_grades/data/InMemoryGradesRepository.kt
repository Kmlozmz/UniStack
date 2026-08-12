package com.unistack.app.feature_grades.data

import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryGradesRepository : GradesRepository {
    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    override val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    override fun addSubject(subject: Subject) {
        _subjects.update { current ->
            if (current.any { it.id == subject.id }) current else current + subject
        }
    }

    override fun updateSubject(subject: Subject) {
        _subjects.update { current ->
            current.map { existing ->
                // Se conservan las notas que ya había. Esta implementación reemplazaba el
                // objeto entero, mientras que la de Room solo escribe los campos de la
                // materia: cualquier código probado contra este doble daba por hecho que
                // podía cambiar las notas por aquí y luego no lo hacía en producción.
                if (existing.id == subject.id) subject.copy(grades = existing.grades) else existing
            }
        }
    }

    override fun deleteSubject(subjectId: String) {
        _subjects.update { current -> current.filterNot { it.id == subjectId } }
    }

    override fun addGrade(subjectId: String, grade: GradeItem) {
        _subjects.update { current ->
            current.map { subject ->
                if (subject.id == subjectId) {
                    subject.copy(grades = subject.grades + grade)
                } else {
                    subject
                }
            }
        }
    }

    override fun updateGrade(subjectId: String, grade: GradeItem) {
        _subjects.update { current ->
            current.map { subject ->
                if (subject.id == subjectId) {
                    subject.copy(
                        grades = subject.grades.map { existing ->
                            if (existing.id == grade.id) grade else existing
                        }
                    )
                } else {
                    subject
                }
            }
        }
    }

    override fun deleteGrade(subjectId: String, gradeId: String) {
        _subjects.update { current ->
            current.map { subject ->
                if (subject.id == subjectId) {
                    subject.copy(grades = subject.grades.filterNot { it.id == gradeId })
                } else {
                    subject
                }
            }
        }
    }

    override fun clearGrades(subjectId: String) {
        _subjects.update { current ->
            current.map { subject ->
                if (subject.id == subjectId) subject.copy(grades = emptyList()) else subject
            }
        }
    }
}
