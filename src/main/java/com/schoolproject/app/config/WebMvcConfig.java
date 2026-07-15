package com.schoolproject.app.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Path materialsDir = Paths.get(uploadDir, "materials");
            Files.createDirectories(materialsDir);
            log.info("Materials upload directory: {}", materialsDir.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create upload directory", e);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path materialsPath = Paths.get(uploadDir, "materials").toAbsolutePath().normalize();
        String location = materialsPath.toUri().toString();

        registry.addResourceHandler("/materials/**")
                .addResourceLocations(location);
    }
}
