package com.ortecfinance.tasklist;
import com.ortecfinance.tasklist.dto.*;
import com.ortecfinance.tasklist.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final TaskService service;

    public ProjectController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> createProject(@RequestBody CreateProjectRequest request) {
        service.addProject(request.getName());
        return Map.of("name", request.getName());
    }

    @GetMapping
    public List<ProjectResponse> getAllProjects() {
        List<ProjectResponse> result = new ArrayList<>();
        for (Map.Entry<String, List<Task>> entry : service.getAllProjects().entrySet()) {
            result.add(new ProjectResponse(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    @PostMapping("/{projectName}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@PathVariable String projectName, @RequestBody CreateTaskRequest request) {
        Task task = service.addTask(projectName, request.getDescription());
        return new TaskResponse(task);
    }

    @PutMapping("/{projectName}/tasks/{taskId}")
    public TaskResponse updateTaskDeadline(@PathVariable String projectName, @PathVariable long taskId, @RequestParam String deadline) {
        LocalDate date = LocalDate.parse(deadline, DATE_FORMAT);
        service.setDeadline(taskId, date);
        return new TaskResponse(service.getTaskById(taskId));
    }

    @GetMapping("/view_by_deadline")
    public Map<String, List<ProjectResponse>> getTasksByDeadline() {
        Map<LocalDate, Map<String, List<Task>>> grouped = service.getTasksGroupedByDeadline();
        Map<String, List<ProjectResponse>> result = new LinkedHashMap<>();

        for (Map.Entry<LocalDate, Map<String, List<Task>>> dateEntry : grouped.entrySet()) {
            String dateKey = dateEntry.getKey() == null ? "No deadline" : dateEntry.getKey().format(DATE_FORMAT);
            List<ProjectResponse> projects = new ArrayList<>();
            for (Map.Entry<String, List<Task>> projectEntry : dateEntry.getValue().entrySet()) {
                projects.add(new ProjectResponse(projectEntry.getKey(), projectEntry.getValue()));
            }
            result.put(dateKey, projects);
        }
        return result;
    }

    @GetMapping("/today")
    public List<ProjectResponse> getTasksDueToday() {
        Map<String, List<Task>> todayTasks = service.getTasksDueToday();
        List<ProjectResponse> result = new ArrayList<>();
        for (Map.Entry<String, List<Task>> entry : todayTasks.entrySet()) {
            result.add(new ProjectResponse(entry.getKey(), entry.getValue()));
        }
        return result;
    }

}