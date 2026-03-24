# Task List Application

A task management application with both a command-line interface and a REST API.

## Project Structure

```text
src/main/java/com/ortecfinance/tasklist/
├── Task.java                     # Data model
├── TaskList.java                 # Console interface (REPL, parsing, output)
├── TaskListApplication.java      # Entry point
├── ProjectController.java        # REST controller
├── GlobalExceptionHandler.java
├── dto/                          # JSON input/output classes
│   ├── CreateProjectRequest.java
│   ├── CreateTaskRequest.java
│   ├── ProjectResponse.java
│   └── TaskResponse.java
├── exception/                    # Custom exceptions
│   ├── ProjectNotFoundException.java
│   └── TaskNotFoundException.java
└── service/
	└── TaskService.java          # Core logic
```

## Running the Application

Console mode:

```bash
mvn compile exec:java -Dexec.mainClass="com.ortecfinance.tasklist.TaskListApplication"
```

Web mode (REST API):

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="web"
```

Run tests:

```bash
mvn test
```

## REST API Endpoints

| Method | Path                        | Body / Params            | Response                        |
| ------ | --------------------------- | ------------------------ | ------------------------------- |
| POST   | /projects                   | `{"name": "..."}`        | 201 — created project           |
| GET    | /projects                   | —                        | 200 — all projects with tasks   |
| POST   | /projects/{name}/tasks      | `{"description": "..."}` | 201 — created task              |
| PUT    | /projects/{name}/tasks/{id} | `?deadline=dd-MM-yyyy`   | 200 — updated task              |
| GET    | /projects/today             | —                        | 200 — tasks due today           |
| GET    | /projects/view-by-deadline  | —                        | 200 — tasks grouped by deadline |

## Console Commands

| Command                           | Description                    |
| --------------------------------- | ------------------------------ |
| `show`                            | list all projects and tasks    |
| `add project <name>`              | create a project               |
| `add task <project name> <desc>`  | add a task to a project        |
| `check <task ID>`                 | mark task as done              |
| `uncheck <task ID>`               | mark task as not done          |
| `deadline <task ID> <dd-MM-yyyy>` | set a deadline on a task       |
| `today`                           | show tasks due today           |
| `view-by-deadline`                | show tasks grouped by deadline |
| `help`                            | show available commands        |
| `quit`                            | exit                           |

Non-required improvements I did:

Added extra HashMap to allow for O(1) look up times for service logic.
Added DTOs to make the JSON parsing and serialization easier and more safe since avoid exposing internal objects
Added some intactive feedback on terminal app to make it feel more responsive
Added some safer parsing to avoid out of bounds errors on command parse

Potential future improvements/fixes:

Add more input validation into DTO's to prevent bad tasks/projects being submitted
Could add more testing, tried to cover edge cases but sure there are some I missed
Switched the routing to /projects based off the task requirements, but original controller/TaskListApplication used
/tasks. Unsure what was wanted, so stuck with /projects for the routes.

---
