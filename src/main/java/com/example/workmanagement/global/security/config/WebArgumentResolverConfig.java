package com.example.workmanagement.global.security.config;

import com.example.workmanagement.global.security.resolver.AuthenticatedUserIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebArgumentResolverConfig implements WebMvcConfigurer {

    private final AuthenticatedUserIdArgumentResolver authenticatedUserIdArgumentResolver;

    public WebArgumentResolverConfig(AuthenticatedUserIdArgumentResolver authenticatedUserIdArgumentResolver) {
        this.authenticatedUserIdArgumentResolver = authenticatedUserIdArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedUserIdArgumentResolver);
    }
}
