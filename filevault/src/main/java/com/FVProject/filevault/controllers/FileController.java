package com.FVProject.filevault.controllers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/files")
public class FileController {

    private final Path uploadDir = Paths.get("uploads");

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("username") String username) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Uploaded file is empty"));
        }

        Path rootDir = Paths.get("uploads");
        Path userDir = rootDir.resolve(username);

        try {
            Files.createDirectories(userDir);

            Path destination = userDir.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(Map.of(
                    "message", "File uploaded successfully",
                    "filename", file.getOriginalFilename(),
                    "path", destination.toString()
            ));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error saving file: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listUserFiles(@RequestParam String username) {
        Path userDir = uploadDir.resolve(username);

        if (!Files.exists(userDir) || !Files.isDirectory(userDir)) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        try {
            List<String> fileUrls = Files.list(userDir)
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        String fileName = path.getFileName().toString();
                        return ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/files/download/")
                                .path(username + "/")
                                .path(fileName)
                                .toUriString();
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(fileUrls);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/download/{username}/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String username,
            @PathVariable String filename) {

        Path filePath = uploadDir.resolve(username).resolve(filename);

        try {
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Missing required parameter: " + name));
    }
}











