package com.example.promptdb.config;

import com.example.promptdb.security.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/",
                        "/login",
                        "/logout",
                        "/organizations/bootstrap",
                        "/organizations/bootstrap/**",
                        "/register",
                        "/register/**",
                        "/css/**",
                        "/js/**",
                        "/webjars/**",
                        "/error",
                        "/error/**"
                );
    }
}
