package com.FVProject.filevault.services;

import com.FVProject.filevault.DTO.UserLoginRequest;
import com.FVProject.filevault.DTO.UserRegisterRequest;
import com.FVProject.filevault.model.User;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

// !!!! DEMO USER SERVICE !!!!

@Service
public class UserService {

    private final List<User> mockUsers = new ArrayList<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public UserService() {
        mockUsers.add(createUser("admin1", "ADMIN"));
        mockUsers.add(createUser("demoUser", "USER"));
    }

    private User createUser(String username, String role) {
        User user = new User();
        user.setId(idGen.getAndIncrement());
        user.setUsername(username);
        user.setRole(role);
        user.setEmail(username + "@example.com");
        user.setEnabled(true);
        user.setLocked(false);
        return user;
    }

    public User getDemoUser() {
        return findByUsername("demoUser");
    }

    public User getDemoStandardUser() {
        return findByUsername("demoUser");
    }

    public boolean authenticate(UserLoginRequest request) {
        System.out.println("Demo mode: skipping authentication");
        return true;
    }

    public Long createUser(UserRegisterRequest request) {
        User user = createUser(request.getUsername(), "USER");
        mockUsers.add(user);
        System.out.println("Demo mode: created user " + user.getUsername());
        return user.getId();
    }

    public User findByUsername(String username) {
        return mockUsers.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers(String requestingUsername) {
        return mockUsers;
    }

    public User getUserByUsername(String username) {
        return findByUsername(username);
    }

    public void updateUser(User updatedUser) {
        int index = mockUsers.indexOf(findByUsername(updatedUser.getUsername()));
        mockUsers.set(index, updatedUser);
    }

    public User getUserById(Long id) {
        return mockUsers.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User with ID " + id + " not found"));
    }
}







