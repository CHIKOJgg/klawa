package org.example.aiassistantklawa.reminder.api;

import org.example.aiassistantklawa.reminder.domain.Event;
import org.springframework.scheduling.config.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EventsController {
    @PostMapping("api/v1/events")
    public void createEvent(@RequestBody Event event){
    }
    @GetMapping("api/v1/events")
    public List<Event> getEvents(){
        return null;
    }
    @GetMapping("api/v1/events/{id}")
    public Event getEvent(@PathVariable int id){
        return null;
    }
    @PutMapping("api/v1/events/{id}")
    public void updateEvent(@PathVariable int id, @RequestBody Event event){
    }
    @DeleteMapping("api/v1/events/{id}")
    public void deleteEvent(@PathVariable int id){

    }
    @GetMapping("api/v1/events/upcoming")
    public List<Event> getUpcomingEvents(){
        return null;
    }
}
