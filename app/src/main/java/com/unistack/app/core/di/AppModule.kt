package com.unistack.app.core.di

import android.content.Context
import com.unistack.app.core.datastore.UserPreferencesDataSource
import com.unistack.app.feature_expenses.data.local.ExpenseDao
import com.unistack.app.feature_grades.data.local.GradeDao
import com.unistack.app.feature_grades.data.local.SubjectDao
import com.unistack.app.feature_grades.data.local.UniStackDatabase
import com.unistack.app.feature_schedule.data.local.AgendaEventDao
import com.unistack.app.feature_terms.data.local.AcademicBreakDao
import com.unistack.app.feature_terms.data.local.AcademicTermDao
import com.unistack.app.feature_schedule.data.local.ClassOccurrenceDao
import com.unistack.app.feature_schedule.data.local.ClassSessionDao
import com.unistack.app.feature_notes.data.local.NoteAttachmentDao
import com.unistack.app.feature_notes.data.local.NoteDao
import com.unistack.app.feature_tasks.data.local.TaskDao
import com.unistack.app.feature_templates.data.local.AcademicWorkDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserPreferencesDataSource(@ApplicationContext context: Context): UserPreferencesDataSource =
        UserPreferencesDataSource(context)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): UniStackDatabase =
        UniStackDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideSubjectDao(db: UniStackDatabase): SubjectDao = db.subjectDao()

    @Provides
    @Singleton
    fun provideGradeDao(db: UniStackDatabase): GradeDao = db.gradeDao()

    @Provides
    @Singleton
    fun provideTaskDao(db: UniStackDatabase): TaskDao = db.taskDao()

    @Provides
    @Singleton
    fun provideExpenseDao(db: UniStackDatabase): ExpenseDao = db.expenseDao()

    @Provides
    @Singleton
    fun provideAcademicWorkDao(db: UniStackDatabase): AcademicWorkDao = db.academicWorkDao()

    @Provides
    @Singleton
    fun provideClassSessionDao(db: UniStackDatabase): ClassSessionDao = db.classSessionDao()

    @Provides
    @Singleton
    fun provideClassOccurrenceDao(db: UniStackDatabase): ClassOccurrenceDao = db.classOccurrenceDao()

    @Provides
    @Singleton
    fun provideAgendaEventDao(db: UniStackDatabase): AgendaEventDao = db.agendaEventDao()

    @Provides
    @Singleton
    fun provideAcademicTermDao(db: UniStackDatabase): AcademicTermDao = db.academicTermDao()

    @Provides
    fun provideAcademicBreakDao(db: UniStackDatabase): AcademicBreakDao = db.academicBreakDao()

    @Provides
    @Singleton
    fun provideNoteDao(db: UniStackDatabase): NoteDao = db.noteDao()

    @Provides
    @Singleton
    fun provideNoteAttachmentDao(db: UniStackDatabase): NoteAttachmentDao = db.noteAttachmentDao()
}
