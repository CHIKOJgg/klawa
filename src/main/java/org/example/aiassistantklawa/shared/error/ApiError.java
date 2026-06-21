package org.example.aiassistantklawa.shared.error;

import org.slf4j.MDC;

import java.time.Instant;

public record ApiError(Instant timestamp, int status, String code,
                       String message, String path, String traceId) {
    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(Instant.now(), status, code, message, path, MDC.get("traceId"));
    }
}
