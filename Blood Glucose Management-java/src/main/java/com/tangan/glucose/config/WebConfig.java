package com.tangan.glucose.config;

import com.tangan.glucose.auth.JwtAuthInterceptor;
import com.tangan.glucose.common.RequestIdFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TanganProperties.class)
public class WebConfig implements WebMvcConfigurer {
    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final TanganProperties properties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor).addPathPatterns("/api/v1/**").excludePathPatterns(
                "/api/v1/users", "/api/v1/sessions", "/api/v1/sessions/refresh", "/api/v1/public/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = properties.getAllowedOrigins().isEmpty()
                ? new String[]{"*"}
                : properties.getAllowedOrigins().toArray(new String[0]);
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Bean
    public RequestIdFilter requestIdFilter() { return new RequestIdFilter(); }
}
