package org.example.klawa.core.repository;

import org.example.klawa.contracts.enums.TaskStatus;
import org.example.klawa.core.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Page<Task> findAllByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Page<Task> findAllByUserIdAndProjectIdAndDeletedAtIsNull(UUID userId, Long projectId, Pageable pageable);

    Page<Task> findAllByUserIdAndStatusAndDeletedAtIsNull(UUID userId, TaskStatus status, Pageable pageable);

    Optional<Task> findByIdAndUserIdAndDeletedAtIsNull(Long id, UUID userId);

    long countByUserIdAndStatusAndDeletedAtIsNull(UUID userId, TaskStatus status);
}
