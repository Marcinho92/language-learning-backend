package com.example.languagelearning.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class ResponseTimeInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        
        log.info("REQUEST: {} {}", request.getMethod(), request.getRequestURI() + 
                (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        String status = response.getStatus() >= 400 ? "ERROR" : "SUCCESS";
        log.info("RESPONSE: {} {} - Status: {} - Time: {}ms", 
                request.getMethod(), 
                request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""),
                status,
                responseTime);
    }
}
