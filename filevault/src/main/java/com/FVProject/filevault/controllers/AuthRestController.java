package com.FVProject.filevault.controllers;

import com.FVProject.filevault.DTO.UserLoginRequest;
import com.FVProject.filevault.DTO.UserRegisterRequest;
import com.FVProject.filevault.model.User;
import com.FVProject.filevault.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private UserService userService;

    private static String currentLoggedInUser = null;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest userDTO) {
        Long userId = userService.createUser(userDTO);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Username or email already exists"));
        }
        return ResponseEntity.ok(Map.of("message", "Demo registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest loginDTO) {
        User user;
        try {
            user = userService.findByUsername(loginDTO.getUsername());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }

        boolean authenticated = userService.authenticate(loginDTO);
        if (!authenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }

        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Account is disabled"));
        }

        if (user.isLocked()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Account is locked"));
        }

        currentLoggedInUser = user.getUsername();

        System.out.println("Login successful - User: " + user.getUsername() + ", Role: " + user.getRole());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "username", user.getUsername(),
                "role", user.getRole()
        ));
    }

    @GetMapping("/session")
    public ResponseEntity<?> checkSession() {
        String username = currentLoggedInUser != null ? currentLoggedInUser : "demoUser";

        User currentUser;
        try {
            currentUser = userService.findByUsername(username);
        } catch (RuntimeException e) {
            currentUser = userService.getDemoUser();
        }

        boolean isAdminCheck = "ADMIN".equals(currentUser.getRole());

        System.out.println("Session check - Current user: " + currentUser.getUsername() + ", Role: " + currentUser.getRole() + ", IsAdmin: " + isAdminCheck);

        if (!currentUser.isEnabled() || currentUser.isLocked()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "User access denied"));
        }

        return ResponseEntity.ok(Map.of(
                "username", currentUser.getUsername(),
                "enabled", currentUser.isEnabled(),
                "locked", currentUser.isLocked(),
                "isAdmin", isAdminCheck
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        currentLoggedInUser = null;
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}