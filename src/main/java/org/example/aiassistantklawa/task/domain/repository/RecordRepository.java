package org.example.aiassistantklawa.task.domain.repository;

import org.example.aiassistantklawa.task.domain.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordRepository extends JpaRepository<Request, Integer> {
    public List<Request> findByTitleContaining(String title);
}
