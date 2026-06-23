    package org.example.aiassistantklawa.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

    @RestControllerAdvice
    @Slf4j
    public class GlobalExceptionHandler {
        @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
        public ResponseEntity<ApiError> handleNotFoundException(ChangeSetPersister.NotFoundException e, HttpServletRequest request) {
            log.warn("Not found: {}", e.getMessage());
            return ResponseEntity.status(404).body(
                    ApiError.of(404,"NOT_FOUND", "not found error",request.getRequestURI()));
        }
        @ExceptionHandler(HttpClientErrorException.Conflict.class)
        public ResponseEntity<ApiError> apiErrorResponseEntity(Exception e, HttpServletRequest request) {
            log.warn("conflict: {}", e.getMessage());
            return ResponseEntity
                    .status(409)
                    .body(ApiError.of(409,"CONFLICT","conflict error",request.getRequestURI() ));

        }
        @ExceptionHandler(HttpClientErrorException.Forbidden.class)
        public ResponseEntity<ApiError> apiErrorResponseEntityForbidden(Exception e, HttpServletRequest request) {
            log.warn("Forbidden: {}", e.getMessage());
            return ResponseEntity
                    .status(403)
                    .body(ApiError
                    .of(403,"FORBIDDEN","forbidden request",request.getRequestURI()));
        }
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> apiErrorResponseEntity(MethodArgumentNotValidException e, HttpServletRequest request) {
            String fields = e.getFieldErrors().stream().map(
                    ex->ex.getField()+":"+ex.getDefaultMessage()
            ).collect(Collectors.joining(";"));
            return ResponseEntity
                    .status(400)
                    .body(ApiError.of(400,"VALIDATION_ERROR",fields,request.getRequestURI()));
        }
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ApiError> handleResponseStatusException(ResponseStatusException e, HttpServletRequest request) {
            log.warn("conflict: {}", e.getMessage());
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(ApiError.of(e.getStatusCode().value(), "CONFLICT", e.getReason(), request.getRequestURI()));
        }
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleAllException(Exception e, HttpServletRequest request) {
            log.error("unexpected error: {}", e.getMessage());
            return ResponseEntity
                    .status(500)
                    .body(ApiError.of(500, "INTERNAL_SERVER_ERROR", "Internal server error", request.getRequestURI()));

        }
    }
