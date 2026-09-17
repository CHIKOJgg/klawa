package org.example.aiassistantklawa.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(1)
@Slf4j
public class RequestIdFilter  extends OncePerRequestFilter {
    private static final String TRACE_HEADER = "X-Trace-Id";
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String traceId = Optional.ofNullable(request.getHeader(TRACE_HEADER)).orElse(UUID.randomUUID().toString().substring(0,8));
       MDC.put(TRACE_HEADER, traceId);
    //    log.info("Request Id: {}", traceId);
        response.setHeader(TRACE_HEADER, traceId);
        try{
            filterChain.doFilter(request, response);
        }
        finally{
            MDC.clear();
        }
    }
}
