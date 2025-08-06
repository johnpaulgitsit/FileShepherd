package com.FVProject.filevault.services;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// F I L E _ S E R V I C E ----> DEMO!!!! ONLY FOR TESTING!!!

@Service
public class FileService {

    private final Path uploadRoot = Paths.get("demo-uploads");

    public FileService() throws IOException {
        Files.createDirectories(uploadRoot);
    }

    public void saveDemoFile(String username, String filename, byte[] fileData) throws IOException {
        Path userDir = uploadRoot.resolve(username);
        Files.createDirectories(userDir);
        Path filePath = userDir.resolve(filename);
        Files.write(filePath, fileData);
    }

    public byte[] loadDemoFile(String username, String filename) throws IOException {
        Path filePath = uploadRoot.resolve(username).resolve(filename);
        return Files.readAllBytes(filePath);
    }

    public List<String> listDemoFiles(String username) throws IOException {
        Path userDir = uploadRoot.resolve(username);
        if (!Files.exists(userDir)) return List.of();

        try (Stream<Path> paths = Files.list(userDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }
}


