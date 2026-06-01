package com.example.foodndeliv.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name = "app.readonly", havingValue = "true")
public class ReadOnlyConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object handler) throws Exception {
                if (!"GET".equalsIgnoreCase(request.getMethod())) {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                            "This is a read-only API. Only GET requests are allowed.");
                    return false;
                }
                return true;
            }
        });
    }
}