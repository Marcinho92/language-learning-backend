package com.example.languagelearning.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ResponseTimeInterceptor responseTimeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(responseTimeInterceptor)
                .addPathPatterns("/api/**")  // Tylko dla endpointów API
                .excludePathPatterns("/actuator/**"); // Wyłącz dla Actuator
    }
}
