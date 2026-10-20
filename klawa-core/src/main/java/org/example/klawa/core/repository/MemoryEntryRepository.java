package org.example.klawa.core.repository;

import org.example.klawa.core.domain.MemoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemoryEntryRepository extends JpaRepository<MemoryEntry, Long> {

    List<MemoryEntry> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<MemoryEntry> findByUserIdAndKeyAndDeletedAtIsNull(UUID userId, String key);
}
