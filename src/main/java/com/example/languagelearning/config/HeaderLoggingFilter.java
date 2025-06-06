package com.example.languagelearning.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
@Slf4j
public class HeaderLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        log.info("Incoming request URL: {}", httpRequest.getRequestURL());
        log.info("Request method: {}", httpRequest.getMethod());
        log.info("Origin header: {}", httpRequest.getHeader("Origin"));

        chain.doFilter(request, response);

        Collection<String> responseHeaderNames = httpResponse.getHeaderNames();
        log.info("Response headers for URL: {}", httpRequest.getRequestURL());
        for (String headerName : responseHeaderNames) {
            log.info("{}: {}", headerName, httpResponse.getHeader(headerName));
        }
    }
} 