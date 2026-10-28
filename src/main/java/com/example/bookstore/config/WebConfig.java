package com.example.bookstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class WebConfig {
    @org.springframework.context.annotation.Bean
    PageableHandlerMethodArgumentResolverCustomizer paginationCustomizer() {
        return resolver -> resolver.setMaxPageSize(100);
    }
}
