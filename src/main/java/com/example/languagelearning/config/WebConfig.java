package com.example.languagelearning.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ResponseTimeInterceptor responseTimeInterceptor;
    private final DatabasePerformanceInterceptor databasePerformanceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Interceptor do mierzenia czasu odpowiedzi endpointów
        registry.addInterceptor(responseTimeInterceptor)
                .addPathPatterns("/", "/api/**")  // Dodaj root endpoint i API
                .excludePathPatterns("/actuator/**"); // Wyłącz dla Actuator
        
        // Interceptor do inicjalizacji liczników DB
        registry.addInterceptor(databasePerformanceInterceptor)
                .addPathPatterns("/", "/api/**")  // Dodaj root endpoint i API
                .excludePathPatterns("/actuator/**"); // Wyłącz dla Actuator
        
        System.out.println("🔧 Interceptors registered successfully!");
        System.out.println("   - ResponseTimeInterceptor: " + responseTimeInterceptor.getClass().getSimpleName());
        System.out.println("   - DatabasePerformanceInterceptor: " + databasePerformanceInterceptor.getClass().getSimpleName());
    }
}
