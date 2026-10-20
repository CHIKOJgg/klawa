package org.example.klawa.core.repository;

import org.example.klawa.core.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<Project> findByIdAndUserIdAndDeletedAtIsNull(Long id, UUID userId);
}
