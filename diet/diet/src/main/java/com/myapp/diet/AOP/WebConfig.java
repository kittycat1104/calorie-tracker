package com.myapp.diet.AOP;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtUserInterceptor jwtUserInterceptor;

    public WebConfig(JwtUserInterceptor jwtUserInterceptor) {
        this.jwtUserInterceptor = jwtUserInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtUserInterceptor)
                .addPathPatterns("/api/**");  // 你後端 API 的路徑
    }
}
