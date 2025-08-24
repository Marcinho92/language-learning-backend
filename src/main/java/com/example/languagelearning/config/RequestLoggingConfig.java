package com.example.languagelearning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false);
        filter.setIncludeHeaders(false);
        filter.setMaxPayloadLength(10000);
        filter.setBeforeMessagePrefix("REQUEST DATA : ");
        filter.setAfterMessagePrefix("RESPONSE DATA : ");
        filter.setAfterMessageSuffix(" - Response Time: ");
        return filter;
    }
}
