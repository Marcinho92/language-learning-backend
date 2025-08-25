package com.example.languagelearning.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class DatabasePerformanceInterceptor implements HandlerInterceptor {

    private static final String DB_START_TIME_ATTRIBUTE = "dbStartTime";
    private static final String DB_OPERATIONS_ATTRIBUTE = "dbOperations";
    private static final ConcurrentHashMap<String, AtomicLong> endpointDbTimeMap = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Inicjalizuj licznik operacji DB dla tego requestu
        request.setAttribute(DB_OPERATIONS_ATTRIBUTE, new AtomicLong(0));
        return true;
    }

    public void startDbOperation(HttpServletRequest request) {
        if (request.getAttribute(DB_START_TIME_ATTRIBUTE) == null) {
            request.setAttribute(DB_START_TIME_ATTRIBUTE, System.currentTimeMillis());
        }
        AtomicLong operations = (AtomicLong) request.getAttribute(DB_OPERATIONS_ATTRIBUTE);
        if (operations != null) {
            operations.incrementAndGet();
        }
    }

    public void endDbOperation(HttpServletRequest request, String operation, long duration) {
        String endpoint = getEndpointKey(request);
        
        // Logowanie czasu operacji DB
        if (duration > 1000) { // > 1 sekunda
            log.warn("SLOW DB OPERATION: {} - Operation: {} - Time: {}ms ⚠️", endpoint, operation, duration);
        } else if (duration > 500) { // > 500ms
            log.info("DB OPERATION: {} - Operation: {} - Time: {}ms 🐌", endpoint, operation, duration);
        } else {
            log.debug("DB OPERATION: {} - Operation: {} - Time: {}ms ✅", endpoint, operation, duration);
        }
    }

    public void logTotalDbTime(HttpServletRequest request) {
        Long startTime = (Long) request.getAttribute(DB_START_TIME_ATTRIBUTE);
        AtomicLong operations = (AtomicLong) request.getAttribute(DB_OPERATIONS_ATTRIBUTE);
        
        if (startTime != null && operations != null && operations.get() > 0) {
            long totalTime = System.currentTimeMillis() - startTime;
            String endpoint = getEndpointKey(request);
            
            if (totalTime > 2000) { // > 2 sekundy
                log.warn("HIGH DB TIME: {} - Total DB time: {}ms for {} operations ⚠️", endpoint, totalTime, operations.get());
            } else if (totalTime > 1000) { // > 1 sekunda
                log.info("DB SUMMARY: {} - Total DB time: {}ms for {} operations 🐌", endpoint, totalTime, operations.get());
            }
        }
    }

    private String getEndpointKey(HttpServletRequest request) {
        return request.getMethod() + " " + request.getRequestURI() + 
                (request.getQueryString() != null ? "?" + request.getQueryString() : "");
    }
}
