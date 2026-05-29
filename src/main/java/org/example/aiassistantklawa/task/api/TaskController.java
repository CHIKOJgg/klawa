package org.example.aiassistantklawa.task.api;

import org.springframework.scheduling.config.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TaskController {
    @PostMapping("/api/v1/tasks")
    public Task createTask(@RequestBody Task task) {
        return task;
    }
    @GetMapping("/api/v1/tasks")
    public List<Task> getTasks() {
        return null;
    }
    @GetMapping("/api/v1/tasks/{id}")
    public List<Task> getTasks(@PathVariable String id) {
        return null;
    }
    @PutMapping("/api/v1/tasks/{id}")
    public Task updateTask(@PathVariable String id, @RequestBody Task task) {
        return task;
    }
    @PatchMapping("/api/v1/tasks/{id}")
    public Task patchTask(@PathVariable String id, @RequestBody Task task)
    {
        return task;
    }
    @DeleteMapping("/api/v1/tasks/{id}")
    public void deleteTask(@PathVariable String id)
    {

    }
    @PostMapping("/api/v1/tasks/{id}/complete")
    public void completeTask(@PathVariable String id) {

    }
    @PostMapping("/api/v1/tasks/{id}/reopen")
    public void reopenTask(@PathVariable String id) {

    }
    @PostMapping("/api/v1/tasks/{id}/archive")
    public void archiveTask(@PathVariable String id) {

    }
    @GetMapping("/api/v1/tasks/{id}/history")
    public List<Task> getTaskHistory(@PathVariable String id) {
        return null;
    }
}
