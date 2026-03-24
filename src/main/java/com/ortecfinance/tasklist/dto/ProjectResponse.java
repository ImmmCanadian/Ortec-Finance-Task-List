package com.ortecfinance.tasklist.dto;

import com.ortecfinance.tasklist.Task;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectResponse {
    private String name;
    private List<TaskResponse> tasks;

    public ProjectResponse() {}

    public ProjectResponse(String name, List<Task> tasks) {
        this.name = name;
        this.tasks = tasks.stream().map(TaskResponse::new).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public List<TaskResponse> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskResponse> tasks) {
        this.tasks = tasks;
    }
}
