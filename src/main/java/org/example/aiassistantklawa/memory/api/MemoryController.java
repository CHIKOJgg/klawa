package org.example.aiassistantklawa.memory.api;

import org.example.aiassistantklawa.memory.domain.MemoryObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MemoryController {
    @PostMapping("api/v1/memories")
    public MemoryObject storeMemories(){
        return null;
    }
    @GetMapping("api/v1/memories")
    public List<MemoryObject> getMemories(){
        return null;
    }
    @GetMapping("api/v1/memories/{id}")
    public MemoryObject getMemory(@PathVariable int id){
        return null;
    }
    @DeleteMapping("api/v1/memories/{id}")
    public void deleteMemory(@PathVariable int id){

    }
    @GetMapping("api/v1/memories/search")
    public List<MemoryObject> searchMemories(@RequestParam String search){
        return null;
    }
}
