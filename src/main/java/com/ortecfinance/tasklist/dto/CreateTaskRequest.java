package com.ortecfinance.tasklist.dto;

public class CreateTaskRequest {

    private String description;

    public CreateTaskRequest() {}

    public CreateTaskRequest(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
