package com.ortecfinance.tasklist.service;

import com.ortecfinance.tasklist.Task;
import com.ortecfinance.tasklist.exception.ProjectNotFoundException;
import com.ortecfinance.tasklist.exception.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    private TaskService service;

    @BeforeEach
    void start_with_a_fresh_service() {
        service = new TaskService();
    }

    @Test
    void it_creates_an_empty_project_when_a_project_is_added() {
        service.addProject("secrets");
        Map<String, List<Task>> projects = service.getAllProjects();
        assertTrue(projects.containsKey("secrets"));
        assertTrue(projects.get("secrets").isEmpty());
    }

    @Test
    void it_adds_a_task_to_an_existing_project() {
        service.addProject("training");
        Task task = service.addTask("training", "Practice Outside-In TDD");
        assertEquals(1, task.getId());
        assertEquals("Practice Outside-In TDD", task.getDescription());
        assertFalse(task.isDone());
        List<Task> tasks = service.getAllProjects().get("training");
        assertEquals(1, tasks.size());
        assertEquals("Practice Outside-In TDD", tasks.get(0).getDescription());
    }

    @Test
    void it_throws_when_adding_a_task_to_a_missing_project() {
        assertThrows(ProjectNotFoundException.class, () ->
            service.addTask("ghost-project", "Finish report")
        );
    }

    @Test
    void it_assigns_incrementing_task_ids() {
        service.addProject("secrets");
        Task t1 = service.addTask("secrets", "Eat more donuts.");
        Task t2 = service.addTask("secrets", "Destroy all humans.");
        assertEquals(1, t1.getId());
        assertEquals(2, t2.getId());
    }

    @Test
    void it_marks_a_task_as_done_when_checked() {
        service.addProject("training");
        service.addTask("training", "SOLID");
        service.setDone(1, true);
        assertTrue(service.getAllProjects().get("training").get(0).isDone());
    }

    @Test
    void it_marks_a_task_as_not_done_when_unchecked() {
        service.addProject("training");
        service.addTask("training", "Primitive Obsession");
        service.setDone(1, true);
        service.setDone(1, false);
        assertFalse(service.getAllProjects().get("training").get(0).isDone());
    }

    @Test
    void it_throws_when_marking_a_missing_task() {
        assertThrows(TaskNotFoundException.class, () ->
            service.setDone(999, true)
        );
    }

    @Test
    void it_sets_a_deadline_for_an_existing_task() {
        service.addProject("training");
        service.addTask("training", "Outside-In TDD kata");
        LocalDate deadline = LocalDate.of(2025, 1, 15);
        service.setDeadline(1, deadline);
        assertEquals(deadline, service.getAllProjects().get("training").get(0).getDeadline());
    }

    @Test
    void it_throws_when_setting_a_deadline_for_a_missing_task() {
        service.addProject("training");
        assertThrows(TaskNotFoundException.class, () ->
            service.setDeadline(999, LocalDate.of(2025, 1, 15))
        );
    }

    @Test
    void it_groups_tasks_by_deadline_then_project() {
        service.addProject("secrets");
        service.addProject("training");
        service.addTask("secrets", "Eat more donuts.");
        service.addTask("training", "Learn TDD");
        service.addTask("training", "SOLID");

        service.setDeadline(1, LocalDate.of(2025, 12, 20));
        service.setDeadline(2, LocalDate.of(2025, 11, 15));

        Map<LocalDate, Map<String, List<Task>>> grouped = service.getTasksGroupedByDeadline();
        assertEquals(3, grouped.size());

        var iterator = grouped.entrySet().iterator();
        var first = iterator.next();
        assertEquals(LocalDate.of(2025, 11, 15), first.getKey());
        assertTrue(first.getValue().containsKey("training"));
        assertEquals(1, first.getValue().get("training").size());

        var second = iterator.next();
        assertEquals(LocalDate.of(2025, 12, 20), second.getKey());
        assertTrue(second.getValue().containsKey("secrets"));

        var third = iterator.next();
        assertNull(third.getKey());
        assertTrue(third.getValue().containsKey("training"));
        assertEquals("SOLID", third.getValue().get("training").get(0).getDescription());
    }

    @Test
    void it_returns_no_deadline_groups_when_there_are_no_tasks() {
        Map<LocalDate, Map<String, List<Task>>> grouped = service.getTasksGroupedByDeadline();
        assertTrue(grouped.isEmpty());
    }


}
