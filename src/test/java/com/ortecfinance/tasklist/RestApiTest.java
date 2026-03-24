package com.ortecfinance.tasklist;

import com.ortecfinance.tasklist.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@Import(TaskService.class)
class RestApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService service;

    @Test
    void it_creates_a_project_and_returns_201() throws Exception {
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"secrets\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("secrets")));
    }

    @Test
    void it_returns_projects_from_get_all_projects() throws Exception {
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"training\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void it_creates_a_task_and_returns_201() throws Exception {
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"training\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/projects/training/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\": \"Practice Outside-In TDD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is("Practice Outside-In TDD")))
                .andExpect(jsonPath("$.done", is(false)));
    }

    @Test
    void it_updates_a_task_deadline_and_returns_the_updated_task() throws Exception {
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"training\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/projects/training/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\": \"Finish report\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/projects/training/tasks/" + getLatestTaskId())
                .param("deadline", "15-01-2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deadline", is("2025-01-15")));
    }

    @Test
    void it_returns_grouped_tasks_for_view_by_deadline() throws Exception {
                mockMvc.perform(get("/projects/view_by_deadline"))
                .andExpect(status().isOk());
    }

    @Test
    void it_returns_ok_for_tasks_due_today_endpoint() throws Exception {
        mockMvc.perform(get("/projects/today"))
                .andExpect(status().isOk());
    }


    private long getLatestTaskId() {
        return service.getAllProjects().values().stream()
                .flatMap(List::stream)
                .mapToLong(Task::getId)
                .max()
                .orElse(1);
    }
}
