package com.ortecfinance.tasklist;
import com.ortecfinance.tasklist.exception.ProjectNotFoundException;
import com.ortecfinance.tasklist.exception.TaskNotFoundException;
import com.ortecfinance.tasklist.service.TaskService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.*;
import java.util.*;

public final class TaskList implements Runnable {
    private static final String QUIT = "quit";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String ADD_PROJECT_USAGE = "Usage: add project <project name>";
    private static final String ADD_TASK_USAGE = "Usage: add task <project name> <task description>";
    private static final String CHECK_USAGE = "Usage: check <task ID>";
    private static final String UNCHECK_USAGE = "Usage: uncheck <task ID>";
    private static final String DEADLINE_USAGE = "Usage: deadline <task ID> <date>";
    private final BufferedReader in;
    private final PrintWriter out;
    private final TaskService service;

    public static void startConsole() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        TaskService service = new TaskService();
        new TaskList(in, out, service).run();
    }

    public TaskList(BufferedReader reader, PrintWriter writer, TaskService service) {
        this.in = reader;
        this.out = writer;
        this.service = service;
    }

    public void run() {
        out.println("Welcome to TaskList! Type 'help' for available commands.");
        while (true) {
            out.print("> ");
            out.flush();
            String command;
            try {
                command = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (command.equals(QUIT)) {
                break;
            }
            execute(command);
        }
    }

    private void execute(String commandLine) {
        String[] commandRest = commandLine.split(" ", 2);
        String command = commandRest[0];
        switch (command) {
            case "show":
                show();
                break;
            case "add":
                add(commandRest.length > 1 ? commandRest[1] : "");
                break;
            case "check":
                check(commandRest.length > 1 ? commandRest[1] : "");
                break;
            case "uncheck":
                uncheck(commandRest.length > 1 ? commandRest[1] : "");
                break;
            case "deadline":             
                deadline(commandRest.length > 1 ? commandRest[1] : ""); 
                break;
            case "view_by_deadline":     
                viewByDeadline();        
                break; 
            case "today":     
                today();      
                break;     
            case "help":
                help();
                break;
            default:
                error(command);
                break;
        }
    }

    private void show() {
        Map<String, List<Task>> projects = service.getAllProjects();
        boolean hasAnyTask = false;

        for (Map.Entry<String, List<Task>> project : projects.entrySet()) {
            if (project.getValue().isEmpty()) {
                continue;
            }
            hasAnyTask = true;
            out.println(project.getKey());
            for (Task task : project.getValue()) {
                out.printf("    [%c] %d: %s%n", (task.isDone() ? 'x' : ' '), task.getId(), task.getDescription());
            }
            out.println();
        }

        if (!hasAnyTask) {
            out.println("No tasks found.");
        }
    }

    private void today() {
        Map<String, List<Task>> todayTasks = service.getTasksDueToday();
        if (todayTasks.isEmpty()) {
            out.println("No tasks due today.");
            return;
        }
        for (Map.Entry<String, List<Task>> project : todayTasks.entrySet()) {
            out.println(project.getKey());
            for (Task task : project.getValue()) {
                out.printf("    [%c] %d: %s%n", (task.isDone() ? 'x' : ' '), task.getId(), task.getDescription());
            }
            out.println();
        }
    }

    private void viewByDeadline() {
        Map<LocalDate, Map<String, List<Task>>> grouped = service.getTasksGroupedByDeadline();
        if (grouped.isEmpty()) {
            out.println("No tasks found.");
            return;
        }

        for (Map.Entry<LocalDate, Map<String, List<Task>>> dateEntry : grouped.entrySet()) {
            LocalDate date = dateEntry.getKey();
            out.println(date == null ? "No deadline:" : date.format(DATE_FORMAT) + ":");
            for (Map.Entry<String, List<Task>> projectEntry : dateEntry.getValue().entrySet()) {
                out.println("    " + projectEntry.getKey());
                for (Task task : projectEntry.getValue()) {
                    out.printf("        [%c] %d: %s%n", (task.isDone() ? 'x' : ' '), task.getId(), task.getDescription());
                }
            }
            out.println();
        }
    }


    private void add(String commandLine) {
        if (commandLine == null || commandLine.isBlank()) {
            out.println(ADD_PROJECT_USAGE);
            out.println(ADD_TASK_USAGE);
            return;
        }

        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];

        if (subcommand.equals("project")) {
            if (subcommandRest.length < 2 || subcommandRest[1].isBlank()) {
                out.println(ADD_PROJECT_USAGE);
                return;
            }
            String projectName = subcommandRest[1];
            service.addProject(projectName);
            out.printf("Added project \"%s\".%n", projectName);
        } else if (subcommand.equals("task")) {
            if (subcommandRest.length < 2 || subcommandRest[1].isBlank()) {
                out.println(ADD_TASK_USAGE);
                return;
            }
            String[] projectTask = subcommandRest[1].split(" ", 2);
            if (projectTask.length < 2 || projectTask[0].isBlank() || projectTask[1].isBlank()) {
                out.println(ADD_TASK_USAGE);
                return;
            }
            try{
                Task task = service.addTask(projectTask[0], projectTask[1]);
                out.printf("Added task %d to \"%s\".%n", task.getId(), projectTask[0]);
            }
            catch(ProjectNotFoundException e){
                out.println(e.getMessage());
            }
        } else {
            out.println(ADD_PROJECT_USAGE);
            out.println(ADD_TASK_USAGE);
        }
    }

    private void check(String idString) {
        setDone(idString, true, CHECK_USAGE);
    }

    private void uncheck(String idString) {
        setDone(idString, false, UNCHECK_USAGE);
    }

    private void setDone(String idString, boolean done, String usageMessage){
        if (idString == null || idString.isBlank()) {
            out.println(usageMessage);
            return;
        }

        try {
            long id = Long.parseLong(idString);
            service.setDone(id, done);
        }
        catch (NumberFormatException e) {
            out.println(usageMessage);
        } 
        catch (TaskNotFoundException e) {
            out.println(e.getMessage());
        }
    }

    private void deadline(String commandLine) {
        if (commandLine == null || commandLine.isBlank()) {
            out.println(DEADLINE_USAGE);
            return;
        }

        String[] parts = commandLine.split(" ", 2);
        if (parts.length < 2 || parts[1].isBlank()) {
            out.println(DEADLINE_USAGE);
            return;
        }

        try {
            long id = Long.parseLong(parts[0]);
            LocalDate date = LocalDate.parse(parts[1], DATE_FORMAT);
            service.setDeadline(id, date);
            out.printf("Set deadline for task %d to %s.%n", id, date.format(DATE_FORMAT));
        }
        catch (NumberFormatException e) {
            out.println(DEADLINE_USAGE);
        } 
        catch (DateTimeParseException e) {
            out.println("Invalid date format. Use dd-MM-yyyy.");
        } 
        catch (TaskNotFoundException e) {
            out.println(e.getMessage());
        }
    }

    private void help() {
        out.println("Commands:");
        out.println("  show");
        out.println("  add project <project name>");
        out.println("  add task <project name> <task description>");
        out.println("  check <task ID>");
        out.println("  uncheck <task ID>");
        out.println("  deadline <task ID> <date>");
        out.println("  view_by_deadline");
        out.println("  today");
        out.println("  quit");
        out.println();
    }

    private void error(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }
}
