package com.cofeecode.application.powerhauscore.controllers;

import com.cofeecode.application.powerhauscore.services.FileStorageService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files") // Changed base path
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    @RolesAllowed({"USER", "HR", "ADMIN"}) // Allow authenticated users to upload
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File cannot be empty."));
        }
        // Add file size validation if needed: if (file.getSize() > MAX_FILE_SIZE) ...
        // Add file type validation if needed, beyond what client might send
        try {
            String fileName = fileStorageService.storeFile(file);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/files/view/")
                    .path(fileName)
                    .toUriString();

            return ResponseEntity.ok(Map.of(
                "fileName", fileName,
                "fileDownloadUri", fileDownloadUri,
                "message", "File uploaded successfully: " + file.getOriginalFilename()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                           .body(Map.of("error", "Could not upload the file: " + file.getOriginalFilename() + "! " + e.getMessage()));
        }
    }

    @GetMapping("/view/{filename:.+}")
    // No specific role needed if files are meant to be publicly viewable by their direct link
    // once associated with a transaction that the user has access to.
    // If direct file access needs protection beyond transaction access, add @RolesAllowed.
    public ResponseEntity<Resource> viewFile(@PathVariable String filename) {
        try {
            Path filePath = fileStorageService.loadFileAsPath(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = null;
                try {
                    contentType = java.nio.file.Files.probeContentType(filePath);
                } catch (IOException e) {
                    // Log error but continue, default content type will be used
                }
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\\"" + resource.getFilename() + "\\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build(); // Or internal server error if path construction is wrong
        }
    }

    @DeleteMapping("/{filename:.+}")
    @RolesAllowed({"ADMIN"})
    public ResponseEntity<Map<String, String>> deleteFileGeneric(@PathVariable String filename) {
        boolean deleted = fileStorageService.deleteFile(filename);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "File deleted successfully: " + filename));
        } else {
            // This could be because file not found or actual deletion error
            // FileStorageService.deleteFile logs IOExceptions.
            // For a REST API, giving a bit more context might be useful, but depends on desired verbosity.
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(Map.of("error", "Failed to delete file or file not found: " + filename));
        }
    }
}