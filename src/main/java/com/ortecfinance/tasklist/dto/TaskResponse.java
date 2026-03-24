package com.ortecfinance.tasklist.dto;

import com.ortecfinance.tasklist.Task;
import java.time.LocalDate;

public class TaskResponse {
    private long id;
    private String description;
    private boolean done;
    private LocalDate deadline;

    public TaskResponse() {}

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.description = task.getDescription();
        this.done = task.isDone();
        this.deadline = task.getDeadline();
    }

    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
