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
            List<Task> dueTasks = new ArrayList<>();
            for (Task task : entry.getValue()) {
                if (today.equals(task.getDeadline())) {
                    dueTasks.add(task);
                }
            }
            if (!dueTasks.isEmpty()) {
                res.put(entry.getKey(), dueTasks);
            }
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
