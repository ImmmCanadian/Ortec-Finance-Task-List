package com.ortecfinance.tasklist.service;
import java.util.*;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import com.ortecfinance.tasklist.Task;
import com.ortecfinance.tasklist.exception.ProjectNotFoundException;
import com.ortecfinance.tasklist.exception.TaskNotFoundException;

@Service
public class TaskService {

    private final Map<String, List<Task>> projects = new LinkedHashMap<>();
    private final Map<Long, Task> tasksById = new HashMap<>();
    private long lastId = 0;

    public void addProject(String name) {
        projects.put(name, new ArrayList<Task>());
    }

    public Task addTask(String project, String description) {
        List<Task> projectTasks = projects.get(project);
        if (projectTasks == null) {
            throw new ProjectNotFoundException(project);
            
        }
        Task task = new Task(nextId(), description, false);
        projectTasks.add(task);
        tasksById.put(task.getId(), task);
        return task;
        
    }

    public void setDone(long id, boolean done){
        Task task = tasksById.get(id);
        if (task == null){
            throw new TaskNotFoundException(id);
        }
        task.setDone(done);
    }

    public void setDeadline(long taskId, LocalDate deadline){
        Task task = tasksById.get(taskId);
        if (task == null){
            throw new TaskNotFoundException(taskId);
        }
        task.setDeadline(deadline);
    }

    public Map<String, List<Task>> getTasksDueToday() {
        LocalDate today = LocalDate.now();
        Map<String, List<Task>> res = new LinkedHashMap<>();
        for (Map.Entry<String, List<Task>> entry : projects.entrySet()) {
            List<Task> tasksToday = new ArrayList<>();
            for (Task task : entry.getValue()) {
                if (today.equals(task.getDeadline())) {
                    tasksToday.add(task);
                }
            }
            if (!tasksToday.isEmpty()) {
                res.put(entry.getKey(), tasksToday);
            }
        }
        return res;
    }

    public Map<LocalDate, Map<String, List<Task>>> getTasksGroupedByDeadline() {
    // TreeMap doesnt allow null keys, so we need to handle "no deadline" separately
    Map<LocalDate, Map<String, List<Task>>> dated = new TreeMap<>();
    Map<String, List<Task>> noDeadline = new LinkedHashMap<>();

    for (Map.Entry<String, List<Task>> entry : projects.entrySet()) {
        String projectName = entry.getKey();
        for (Task task : entry.getValue()) {
            if (task.getDeadline() == null) {
                noDeadline.computeIfAbsent(projectName, k -> new ArrayList<>()).add(task);
            } else {
                dated.computeIfAbsent(task.getDeadline(), k -> new LinkedHashMap<>())
                     .computeIfAbsent(projectName, k -> new ArrayList<>())
                     .add(task);
            }
        }
    }

    // Combine them back at end
    Map<LocalDate, Map<String, List<Task>>> res = new LinkedHashMap<>(dated);
    if (!noDeadline.isEmpty()) {
        res.put(null, noDeadline);
    }
    return res;
}

    public Map<String, List<Task>> getAllProjects() {
        return Collections.unmodifiableMap(projects);
    }

    private long nextId() {
        return ++lastId;
    }
}
