package org.example.klawa.contracts.enums;

/**
 * Статусы жизненного цикла задачи в соответствии с V0__init_tables.sql и openapi.yaml.
 */
public enum TaskStatus {
    OPEN,
    IN_PROGRESS,
    COMPLETED,
    ARCHIVED
}
