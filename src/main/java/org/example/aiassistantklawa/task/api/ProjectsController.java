package org.example.aiassistantklawa.task.api;

import org.example.aiassistantklawa.task.domain.Project;
import org.springframework.scheduling.config.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class ProjectsController {
    @PostMapping("/api/v1/projects")
    public Project createProject(){
        return  new Project();
    }

    @GetMapping("/api/v1/projects")
    public List<Project> getProjects(){
        return List.of(new Project());
    }
    @GetMapping("/api/v1/projects/{id}")
    public Project getProjects(@PathVariable String id){
        return null;
    }
    @PutMapping("/api/v1/projects/{id}")
    public Project updateProject(){
        return null;
    }
    @DeleteMapping("/api/v1/projects/{id}")
    public void deleteProject(@PathVariable String id){}

    @GetMapping("/api/v1/projects/{id}/tasks")
    public List<Task> getTasks(@PathVariable String id){
        return null;
    }

}
