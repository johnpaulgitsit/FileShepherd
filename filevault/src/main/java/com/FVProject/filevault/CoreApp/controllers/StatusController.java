package com.FVProject.filevault.CoreApp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // allow cookies
public class StatusController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean wizardComplete = isWizardComplete();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("setupComplete", wizardComplete); // frontend expects this key
        response.put("backendPort", System.getProperty("server.port", "8080"));

        return ResponseEntity.ok(response);
    }

    private boolean isWizardComplete() {
        Path envPath = Paths.get(".env");
        if (!Files.exists(envPath)) return false;

        try {
            String content = Files.readString(envPath);
            return content.contains("WIZARD_COMPLETE=true");
        } catch (IOException e) {
            return false;
        }
    }
}

