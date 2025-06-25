package com.cofeecode.application.powerhauscore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${upload.dir}")
    private String uploadDir; // This might still be used by other parts, or planned for future use.

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // The FileController is now responsible for serving files from uploadDir
        // for the transaction photo feature. This static mapping might be redundant
        // or could conflict. Commenting out for now.
        // If other features rely on this /uploads/** mapping, it can be reinstated.

        // Path uploadPath = Paths.get(uploadDir);
        // String resourceLocation = uploadPath.toUri().toString();
        // if (!resourceLocation.endsWith("/")) {
        //     resourceLocation += "/";
        // }
        // registry.addResourceHandler("/uploads/**")
        //         .addResourceLocations(resourceLocation);
    }
}
