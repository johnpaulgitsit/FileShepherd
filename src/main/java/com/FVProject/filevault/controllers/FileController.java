package com.FVProject.filevault.controllers;

import com.FVProject.filevault.model.FileMetadata;
import com.FVProject.filevault.repositories.FileMetadataRepository;
import com.FVProject.filevault.services.FileService;
import com.google.auth.oauth2.ServiceAccountCredentials;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/files")
public class FileController {

    @Value("${spring.cloud.gcp.credentials.location}")
    private String KEY_PATH;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String BUCKET;

    @Autowired
    private final FileService fileService;
    private final FileMetadataRepository fileMetadataRepository;

    public FileController(FileService fileService, FileMetadataRepository fileMetadataRepository) {
        this.fileService = fileService;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        try {
            Long userId = fileService.getUserIdByUsername(username);
            String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // Load Google Cloud credentials explicitly
            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(
                            new FileInputStream(KEY_PATH)
                    ))
                    .build()
                    .getService();

            BlobId blobId = BlobId.of(BUCKET, "user_" + userId + "/" + uniqueFilename);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
            storage.create(blobInfo, file.getBytes());

            // Save metadata in database
            FileMetadata metadata = new FileMetadata(userId, uniqueFilename, blobId.getName());
            fileMetadataRepository.save(metadata);

            return ResponseEntity.ok("File uploaded successfully: " + uniqueFilename);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/my-files")
    public ResponseEntity<?> getUserFiles(HttpServletRequest request) throws IOException {
        // Get session manually - this is more reliable
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No session found"));
        }

        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        }

        try {
            Long userId = fileService.getUserIdByUsername(username);
            List<FileMetadata> files = fileMetadataRepository.findByUserId(userId);

            // Use the same Storage initialization with credentials
            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(
                            new FileInputStream(KEY_PATH)
                    ))
                    .build()
                    .getService();

            List<Map<String, String>> fileList = files.stream().map(file -> {
                Blob blob = storage.get(BUCKET, file.getFilePath());
                String fileUrl = blob.signUrl(7, TimeUnit.DAYS).toString();
                return Map.of("filename", file.getFilename(), "url", fileUrl);
            }).collect(Collectors.toList());

            return ResponseEntity.ok(fileList);

        } catch (Exception e) {

            System.err.println("Error fetching user files: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching files: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, HttpSession session) throws IOException {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        Long userId = fileService.getUserIdByUsername(username);
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File metadata not found"));

        if (!metadata.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this file.");
        }

        // Use explicit credentials for Storage client
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(
                        new FileInputStream(KEY_PATH)
                ))
                .build()
                .getService();

        Blob blob = storage.get(BUCKET, metadata.getFilePath());

        if (blob == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in cloud storage.");
        }

        byte[] fileBytes = blob.getContent();
        ByteArrayResource resource = new ByteArrayResource(fileBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFilename() + "\"")
                .body(resource);
    }
}







