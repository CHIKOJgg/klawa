package org.example.aiassistantklawa.task.infrastructure;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ProcessingRequestService {

    @Transactional
    public void process() {
    }
}
