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
                add(commandRest[1]);
                break;
            case "check":
                check(commandRest[1]);
                break;
            case "uncheck":
                uncheck(commandRest[1]);
                break;
            case "deadline":             
                deadline(commandRest[1]); 
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
        for (Map.Entry<String, List<Task>> project : service.getAllProjects().entrySet()) {
            out.println(project.getKey());
            for (Task task : project.getValue()) {
                out.printf("    [%c] %d: %s%n", (task.isDone() ? 'x' : ' '), task.getId(), task.getDescription());
            }
            out.println();
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

    private void add(String commandLine) {
        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];
        if (subcommand.equals("project")) {
            service.addProject(subcommandRest[1]);
        } else if (subcommand.equals("task")) {
            String[] projectTask = subcommandRest[1].split(" ", 2);
            try{
                service.addTask(projectTask[0], projectTask[1]);
            }
            catch(ProjectNotFoundException e){
                out.println(e.getMessage());
            }
            
        }
    }

    private void check(String idString) {
        setDone(idString, true);
    }

    private void uncheck(String idString) {
        setDone(idString, false);
    }

    private void setDone(String idString, boolean done){
        int id = Integer.parseInt(idString);
        try {
            service.setDone(id, done);
        } 
        catch (TaskNotFoundException e) {
            out.println(e.getMessage());
        }
    }

    private void deadline(String commandLine) {
        String[] parts = commandLine.split(" ", 2);
        long id = Long.parseLong(parts[0]);
        try {
            LocalDate date = LocalDate.parse(parts[1], DATE_FORMAT);
            service.setDeadline(id, date);
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
        out.println("  today");
        out.println();
    }

    private void error(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }
}
