package com.FVProject.filevault.controllers;

import com.FVProject.filevault.DTO.UserLoginRequest;
import com.FVProject.filevault.DTO.UserRegisterRequest;
import com.FVProject.filevault.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthRestController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest userDTO) {
        Long userId = userService.createUser(userDTO); //Returns null in createUser if user already exists

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Username or email already exists"));
        }

        // Assign IAM role for Google Cloud Storage
        boolean roleAssigned = userService.assignUserStorageRole(userDTO.getEmail(), userId);

        if (!roleAssigned) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "User registered, but failed to assign storage permissions"));
        }

        return ResponseEntity.ok(Map.of("message", "Registration successful, storage access granted"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest loginDTO, HttpSession session) {
        boolean authenticated = userService.authenticate(loginDTO);

        if (!authenticated) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }

        // Store username in session
        session.setAttribute("username", loginDTO.getUsername());

        // Tell Spring Security the user is authenticated + persist in session
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);

        //  Persist security context in session
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "username", loginDTO.getUsername()
        ));
    }

    // check session (React can use this to stay logged in)
    @GetMapping("/session")
    public ResponseEntity<?> checkSession(HttpSession session) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        }

        return ResponseEntity.ok(Map.of("username", username));
    }

    //logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));

    }
}