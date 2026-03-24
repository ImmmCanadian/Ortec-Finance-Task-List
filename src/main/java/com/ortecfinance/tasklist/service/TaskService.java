package com.ortecfinance.tasklist.service;
import java.util.*;
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

    public Map<String, List<Task>> getAllProjects() {
        return Collections.unmodifiableMap(projects);
    }

    private long nextId() {
        return ++lastId;
    }
}
