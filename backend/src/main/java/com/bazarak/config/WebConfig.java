package com.bazarak.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve profile photos from the profile-photos directory
        registry.addResourceHandler("/uploads/profile/**")
                .addResourceLocations("file:profile-photos/");

        // If you have other upload directories, add them here too
        // registry.addResourceHandler("/uploads/ads/**")
        //         .addResourceLocations("file:ad-photos/");
    }
}