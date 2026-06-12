package org.example.aiassistantklawa.memory.api;

import org.example.aiassistantklawa.memory.domain.MemoryEntry;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MemoryController {
    @PostMapping("api/v1/memories")
    public MemoryEntry storeMemories(){
        return null;
    }
    @GetMapping("api/v1/memories")
    public List<MemoryEntry> getMemories(){
        return null;
    }
    @GetMapping("api/v1/memories/{id}")
    public MemoryEntry getMemory(@PathVariable int id){
        return null;
    }
    @DeleteMapping("api/v1/memories/{id}")
    public void deleteMemory(@PathVariable int id){

    }
    @GetMapping("api/v1/memories/search")
    public List<MemoryEntry> searchMemories(@RequestParam String search){
        return null;
    }
}
