package com.cofeecode.application.powerhauscore.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    @Value("${upload.dir}")
    private String uploadDirectory;

    public String getUploadDirectory() {
        return uploadDirectory;
    }

    public void setUploadDirectory(String path) {
        this.uploadDirectory = path;
    }

    @PostConstruct
    public void logUploadPath(){
        System.out.println("Upload directory from properties: " + uploadDirectory);
    }
}
