package com.FVProject.filevault.wizard;

import com.FVProject.filevault.WizardApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/wizard")
@CrossOrigin(origins = "http://localhost:3000")
public class WizardController {

    private static final Path GCP_KEY_PATH = Paths.get("temp-gcp-key.json");

    @GetMapping("/welcome")
    public ResponseEntity<Map<String, Object>> getWelcome() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "title", "Welcome to FileVault Setup Wizard",
                "description", "This wizard will guide you through setting up your FileVault application with database and Google Cloud Storage configuration.",
                "steps", Arrays.asList(
                        "Upload Google Cloud Service Account Key",
                        "Configure Database Connection",
                        "Test Configuration",
                        "Save and Start Application"
                ),
                "version", "1.0.0"
        ));
    }

    // Wizard progress flags
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean gcpKeyExists = Files.exists(GCP_KEY_PATH);
        List<String> missingVars = getMissingVarsFromEnvFile();
        boolean wizardComplete = isWizardCompleteStatic();

        Map<String, Boolean> varStatuses = new HashMap<>();
        String[] requiredVars = {"DB", "GCPKEYPATH", "MYBUCKET", "MYSQL_PORT", "MYSQLPASS", "MYSQLUSER"};

        for (String var : requiredVars) {
            varStatuses.put(var, !missingVars.contains(var));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "wizardComplete", wizardComplete && missingVars.isEmpty(),
                "gcpKeyExists", gcpKeyExists,
                "variables", varStatuses,
                "missingVars", missingVars,
                "currentStep", getCurrentStep(gcpKeyExists, missingVars.isEmpty())
        ));
    }

    // Final config overview
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        try {
            if (Files.exists(Paths.get(".env"))) {
                String envContent = Files.readString(Paths.get(".env"));
                Map<String, String> config = parseEnvFile(envContent);

                summary.put("success", true);
                summary.put("database", Map.of(
                        "host", "localhost",
                        "port", config.getOrDefault("MYSQL_PORT", "3306"),
                        "database", config.getOrDefault("DB", "Not configured"),
                        "username", config.getOrDefault("MYSQLUSER", "Not configured")
                ));
                summary.put("googleCloud", Map.of(
                        "bucket", config.getOrDefault("MYBUCKET", "Not configured"),
                        "keyPath", config.getOrDefault("GCPKEYPATH", "Not configured")
                ));
                summary.put("wizardComplete", "true".equals(config.get("WIZARD_COMPLETE")));
            } else {
                summary.put("success", false);
                summary.put("error", "Configuration not found");
            }
        } catch (IOException e) {
            summary.put("success", false);
            summary.put("error", "Failed to read configuration");
        }

        return ResponseEntity.ok(summary);
    }

    // POST /upload-key - Accept multipart file upload
    @PostMapping("/upload-key")
    public ResponseEntity<Map<String, Object>> uploadKey(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "error", "No file provided"));
            }

            // Validate file name
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.endsWith(".json")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "error", "File must be a JSON file"));
            }

            // Read and validate JSON content
            String keyContent = new String(file.getBytes(), StandardCharsets.UTF_8);

            try {
                new ObjectMapper().readTree(keyContent); // Validate JSON structure
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "error", "Invalid JSON format"));
            }

            // Save to temporary location
            Files.write(GCP_KEY_PATH, keyContent.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "GCP key uploaded successfully",
                    "filename", filename
            ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Failed to save key file"));
        }
    }

    // POST /db-test - Test database connection
    @PostMapping("/db-test")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection(@RequestBody Map<String, String> dbConfig) {
        String host = dbConfig.getOrDefault("host", "localhost");
        String port = dbConfig.getOrDefault("port", "3306");
        String database = dbConfig.get("dbName");
        String username = dbConfig.get("username");
        String password = dbConfig.get("password");

        // Validate required fields
        if (database == null || username == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Missing required database credentials"));
        }

        try {
            // Validate port is numeric
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Port must be a valid number"));
        }

        // Test database connection
        String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s", host, port, database);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Database connection successful",
                        "connectionUrl", jdbcUrl
                ));
            }
        } catch (ClassNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "MySQL driver not found"));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Database connection failed: " + e.getMessage()));
        }
    }

    // POST /save-config - Save full configuration
    @PostMapping("/save-config")
    public ResponseEntity<Map<String, Object>> saveConfig(@RequestBody Map<String, String> config) {
        try {
            String[] requiredFields = {"port", "dbName", "username", "password", "bucket"};
            for (String field : requiredFields) {
                if (config.get(field) == null || config.get(field).trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("success", false, "error", "Missing required field: " + field));
                }
            }

            if (!Files.exists(GCP_KEY_PATH)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "error", "GCP key not uploaded yet"));
            }

            // Validate and move GCP key to resources
            Path resourcesDir = Paths.get("src/main/resources");
            Files.createDirectories(resourcesDir);
            Path resourceKeyPath = resourcesDir.resolve("gcp-service-account.json");

            Files.copy(GCP_KEY_PATH, resourceKeyPath, StandardCopyOption.REPLACE_EXISTING);

            // Set secure permissions (Unix-like systems only)
            try {
                Files.setPosixFilePermissions(resourceKeyPath, PosixFilePermissions.fromString("rw-------"));
            } catch (UnsupportedOperationException e) {
                System.out.println("Warning: Unable to set POSIX permissions on Windows system");
            }

            // Sanitize and validate input
            String port = config.get("port").trim();
            String dbName = config.get("dbName").trim();
            String username = config.get("username").trim();
            String password = config.get("password");
            String bucket = config.get("bucket").trim();

            try {
                Integer.parseInt(port);
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "error", "Port must be a valid number"));
            }

            // Create .env file content
            StringBuilder envContent = new StringBuilder();
            envContent.append("# Database Configuration\n");
            envContent.append("MYSQLPORT=").append(port).append("\n");
            envContent.append("DB=").append(dbName).append("\n");
            envContent.append("MYSQLUSER=").append(username).append("\n");
            envContent.append("MYSQLPASS=").append(escapeEnvValue(password)).append("\n");
            envContent.append("\n");
            envContent.append("# Google Cloud Configuration\n");
            envContent.append("MYBUCKET=").append(bucket).append("\n");
            envContent.append("GCPKEYPATH=classpath:gcp-service-account.json\n");
            envContent.append("\n");
            envContent.append("# Wizard Status\n");
            envContent.append("WIZARD_COMPLETE=true\n");

            // Write .env file
            Path envFile = Paths.get(".env");
            Files.write(envFile, envContent.toString().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            try {
                Files.setPosixFilePermissions(envFile, PosixFilePermissions.fromString("rw-------"));
            } catch (UnsupportedOperationException e) {
                System.out.println("Warning: Unable to set POSIX permissions on .env file");
            }

            // Clean up temporary key file
            Files.deleteIfExists(GCP_KEY_PATH);

            System.out.println(" Configuration complete. Environment variables loaded into backend.");

            // Trigger application restart asynchronously
            CompletableFuture.runAsync(() -> {
                WizardApplication.restartWithMainApplication();
            });

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Configuration saved successfully! Restarting application...",
                    "restart", true
            ));

        } catch (IOException e) {
            System.err.println("IO Error saving configuration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Failed to save configuration files"));
        } catch (Exception e) {
            System.err.println("Unexpected error saving configuration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "An unexpected error occurred"));
        }
    }

    // Helper methods
    private List<String> getMissingVarsFromEnvFile() {
        List<String> missing = new ArrayList<>();
        String[] requiredVars = {"DB", "GCPKEYPATH", "MYBUCKET", "MYSQLPORT", "MYSQLPASS", "MYSQLUSER"};

        if (!Files.exists(Paths.get(".env"))) {
            return Arrays.asList(requiredVars);
        }

        try {
            String envContent = Files.readString(Paths.get(".env"));
            for (String var : requiredVars) {
                if (!envContent.contains(var + "=")) {
                    missing.add(var);
                }
            }
        } catch (IOException e) {
            return Arrays.asList(requiredVars);
        }

        return missing;
    }

    private boolean isWizardCompleteStatic() {
        if (!Files.exists(Paths.get(".env"))) return false;

        try {
            String content = Files.readString(Paths.get(".env"));
            return content.contains("WIZARD_COMPLETE=true");
        } catch (IOException e) {
            return false;
        }
    }

    private int getCurrentStep(boolean gcpKeyExists, boolean varsComplete) {
        if (!gcpKeyExists) return 1; // Upload key step
        if (!varsComplete) return 2; // Configure database step
        return 3; // Final step
    }

    private Map<String, String> parseEnvFile(String envContent) {
        Map<String, String> config = new HashMap<>();
        String[] lines = envContent.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int equalIndex = line.indexOf('=');
            if (equalIndex > 0) {
                String key = line.substring(0, equalIndex).trim();
                String value = line.substring(equalIndex + 1).trim();

                // Remove quotes if present
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                    value = value.replace("\\\"", "\"").replace("\\\\", "\\");
                }

                config.put(key, value);
            }
        }

        return config;
    }

    private String escapeEnvValue(String value) {
        if (value == null) return "";

        if (value.contains(" ") || value.contains("\"") || value.contains("'") ||
                value.contains("$") || value.contains("`") || value.contains("\\")) {
            return "\"" + value.replace("\"", "\\\"").replace("\\", "\\\\") + "\"";
        }
        return value;
    }
}


