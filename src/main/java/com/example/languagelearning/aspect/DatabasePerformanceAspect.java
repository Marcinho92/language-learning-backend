package com.example.languagelearning.aspect;

import com.example.languagelearning.config.DatabasePerformanceInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DatabasePerformanceAspect {

    private final DatabasePerformanceInterceptor dbInterceptor;

    @Around("execution(* com.example.languagelearning.repository.*.*(..))")
    public Object measureRepositoryMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed(); // Brak kontekstu HTTP (np. testy)
        }

        HttpServletRequest request = attributes.getRequest();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String operation = className + "." + methodName;

        // Rozpocznij pomiar operacji DB
        dbInterceptor.startDbOperation(request);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // Zaloguj czas operacji DB
            dbInterceptor.endDbOperation(request, operation, duration);
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("DB OPERATION FAILED: {} - Operation: {} - Time: {}ms - Error: {}", 
                    getEndpointKey(request), operation, duration, e.getMessage());
            throw e;
        }
    }

    private String getEndpointKey(HttpServletRequest request) {
        return request.getMethod() + " " + request.getRequestURI() + 
                (request.getQueryString() != null ? "?" + request.getQueryString() : "");
    }
}
