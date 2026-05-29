package org.example.aiassistantklawa.reminder.api;

import org.example.aiassistantklawa.reminder.domain.Reminder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReminderController {
    @PostMapping("api/v1/reminders")
    public Reminder createReminder(@RequestBody Reminder reminder) {
        return null;
    }
    @GetMapping("api/v1/remiders")
    public List<Reminder> getReminders(){
        return null;
    }
    @GetMapping("api/v1/remiders/{id}")
    public Reminder getReminder(@PathVariable int id){
        return  null;
    }
    @PutMapping("api/v1/remiders/{id}")
    public Reminder updateReminder(@PathVariable int id){
        return null;
    }
    @DeleteMapping("api/v1/remiders/{id}")
    public void deleteReminder(@PathVariable int id){

    }
    @PostMapping("api/v1/remiders/{id}/snooze")
    public void snoozeReminder(@PathVariable int id){

    }


}
