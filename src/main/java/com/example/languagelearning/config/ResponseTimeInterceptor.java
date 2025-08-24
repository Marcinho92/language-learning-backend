package com.example.languagelearning.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseTimeInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "startTime";
    private final DatabasePerformanceInterceptor dbInterceptor;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        
        String endpoint = request.getMethod() + " " + request.getRequestURI() + 
                (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        
        log.info("🚀 REQUEST STARTED: {}", endpoint);
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        String status = response.getStatus() >= 400 ? "ERROR" : "SUCCESS";
        String endpoint = request.getMethod() + " " + request.getRequestURI() + 
                (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        
        // Logowanie z ostrzeżeniami dla wolnych zapytań
        if (responseTime > 3000) { // > 3 sekundy
            log.warn("SLOW ENDPOINT: {} - Status: {} - Time: {}ms ⚠️", endpoint, status, responseTime);
        } else if (responseTime > 1000) { // > 1 sekunda
            log.info("RESPONSE: {} - Status: {} - Time: {}ms 🐌", endpoint, status, responseTime);
        } else {
            log.info("RESPONSE: {} - Status: {} - Time: {}ms ✅", endpoint, status, responseTime);
        }
        
        // Zaloguj podsumowanie czasu DB dla tego endpointu
        dbInterceptor.logTotalDbTime(request);
    }
}
