package com.schoolproject.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        if (!absolutePath.endsWith(java.io.File.separator)) {
            absolutePath += java.io.File.separator;
        }
        String materialsPath = absolutePath + "materials" + java.io.File.separator;

        registry.addResourceHandler("/materials/**")
                .addResourceLocations("file:" + materialsPath);
    }
}
