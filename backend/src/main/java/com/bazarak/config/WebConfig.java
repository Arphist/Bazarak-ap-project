package com.bazarak.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get absolute path to the profile-photos directory
        String uploadPath = Paths.get("profile-photos").toAbsolutePath().toString();

        registry.addResourceHandler("/uploads/profile/**")
                .addResourceLocations("file:" + uploadPath + "/");

        System.out.println("📁 Serving profile photos from: " + uploadPath);
    }
}