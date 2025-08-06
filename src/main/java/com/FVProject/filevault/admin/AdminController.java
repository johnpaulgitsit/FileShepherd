package com.FVProject.filevault.admin;

import com.FVProject.filevault.model.User;
import com.FVProject.filevault.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// ----> DEMO MODE ADMIN PRIVILEGES !!!!!!!
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/admin/users")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @GetMapping("/getUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        List<User> users = adminService.getAllUsers();
        users = users.stream()
                .filter(user -> !user.getUsername().equals(currentUsername)) // Exclude self
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<?> lockUser(@PathVariable Long id) {
        adminService.lockUserAccount(id);
        return ResponseEntity.ok("User account locked.");
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable Long id) {
        adminService.unlockUserAccount(id);
        return ResponseEntity.ok("User account unlocked.");
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        adminService.disableUserAccount(id);
        return ResponseEntity.ok("User account disabled.");
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        adminService.enableUserAccount(id);
        return ResponseEntity.ok("User account enabled.");
    }
}

