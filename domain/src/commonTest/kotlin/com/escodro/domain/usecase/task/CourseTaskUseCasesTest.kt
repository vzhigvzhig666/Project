package com.escodro.domain.usecase.task

import com.escodro.domain.model.Task
import com.escodro.domain.usecase.alarm.UpdateAlarm
import com.escodro.domain.usecase.fake.GlanceInteractorFake
import com.escodro.domain.usecase.fake.TaskRepositoryFake
import com.escodro.domain.usecase.task.implementation.AddTaskImpl
import com.escodro.domain.usecase.task.implementation.LoadTaskImpl
import com.escodro.domain.usecase.task.implementation.UpdateTaskDescriptionImpl
import com.escodro.domain.usecase.task.implementation.UpdateTaskImpl
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CourseTaskUseCasesTest {
    private val repository = TaskRepositoryFake()
    private val widget = GlanceInteractorFake()
    private val alarms = mutableListOf<Task>()
    private val alarm = object : UpdateAlarm {
        override suspend fun invoke(task: Task) {
            alarms.add(task)
        }
    }
    private val addTask = AddTaskImpl(repository, alarm, widget)
    private val loadTask = LoadTaskImpl(repository)
    private val updateDescription = UpdateTaskDescriptionImpl(loadTask, UpdateTaskImpl(repository, widget))

    @Test
    fun savingTwoNewTasksKeepsBothWithDistinctIds() = runTest {
        addTask(Task(title = "Prepare tests", categoryId = 2))
        addTask(Task(title = "Read report", description = "Check failures"))

        val saved = repository.findAllTasks()
        assertEquals(2, saved.size)
        val first = saved.single { it.title == "Prepare tests" }
        val second = saved.single { it.title == "Read report" }
        assertNotEquals(first.id, second.id)
        assertEquals(2L, loadTask(first.id)?.categoryId)
        assertEquals("Check failures", loadTask(second.id)?.description)
    }

    @Test
    fun alarmReceivesPersistedTaskWithGeneratedId() = runTest {
        val task = Task(title = "Review coverage", dueDate = LocalDateTime(2030, 1, 2, 12, 0))
        addTask(task)

        val persisted = repository.findAllTasks().single()
        assertTrue(persisted.id > 0)
        assertEquals(listOf(persisted), alarms)
        assertTrue(widget.isNotified)
    }

    @Test
    fun savingTaskWithoutDueDateDoesNotScheduleAlarm() = runTest {
        addTask(Task(title = "Prepare notes"))

        assertEquals("Prepare notes", repository.findAllTasks().single().title)
        assertTrue(alarms.isEmpty())
    }

    @Test
    fun whitespaceTitleDoesNotChangeRepositoryOrSendNotifications() = runTest {
        addTask(Task(title = " \t\n", dueDate = LocalDateTime(2030, 1, 2, 12, 0)))

        assertTrue(repository.findAllTasks().isEmpty())
        assertTrue(alarms.isEmpty())
        assertFalse(widget.isNotified)
    }

    @Test
    fun changingDescriptionPreservesOtherTaskFields() = runTest {
        val original = Task(id = 42, title = "Prepare notes", categoryId = 2, isCompleted = true)
        repository.insertTask(original)

        updateDescription(original.id, "Use screenshots and test results")

        assertEquals(original.copy(description = "Use screenshots and test results"), loadTask(original.id))
    }

    @Test
    fun changingDescriptionOfMissingTaskDoesNotCreateOne() = runTest {
        val existing = Task(id = 42, title = "Prepare notes")
        repository.insertTask(existing)

        updateDescription(999, "This task does not exist")

        assertNull(loadTask(999))
        assertEquals(listOf(existing), repository.findAllTasks())
    }
}
