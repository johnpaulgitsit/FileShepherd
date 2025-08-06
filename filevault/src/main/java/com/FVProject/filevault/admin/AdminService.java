package com.FVProject.filevault.admin;
import com.FVProject.filevault.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.FVProject.filevault.model.User;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

// ________!!!! DEMO ADMIN SERVICES !!!!_______________


@Service
public class AdminService {

    private final UserService userService;

    @Autowired
    public AdminService(UserService userService) {
        this.userService = userService;
    }

    public List<User> getAllUsers() {
        return userService.getAllUsers("admin");
    }

    public void lockUserAccount(Long id) {
        User user = userService.getUserById(id);
        user.setLocked(true);
        user.setUpdatedAt(LocalDateTime.now());
        userService.updateUser(user);
    }

    public void unlockUserAccount(Long id) {
        User user = userService.getUserById(id);
        user.setLocked(false);
        user.setUpdatedAt(LocalDateTime.now());
        userService.updateUser(user);
    }

    public void disableUserAccount(Long id) {
        User user = userService.getUserById(id);
        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        userService.updateUser(user);
    }

    public void enableUserAccount(Long id) {
        User user = userService.getUserById(id);
        user.setEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        userService.updateUser(user);
    }
}

