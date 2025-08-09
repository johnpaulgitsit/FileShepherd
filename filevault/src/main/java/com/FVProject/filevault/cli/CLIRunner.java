package com.FVProject.filevault.cli;


import com.FVProject.filevault.DTO.UserLoginRequest;
import com.FVProject.filevault.DTO.UserRegisterRequest;
import com.FVProject.filevault.model.User;
import com.FVProject.filevault.services.UserService;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class CLIRunner {

    private final UserService userService;

    public CLIRunner(UserService userService) {
        this.userService = userService;
    }


    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Type 'register' to create an account or 'login' to sign in:");
        String action = scanner.nextLine().trim().toLowerCase();

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        switch (action) {
            case "register":
                UserRegisterRequest regRequest = new UserRegisterRequest();
                regRequest.setUsername(username);
                regRequest.setPassword(password);
                regRequest.setEmail(email);

                Long userId = userService.createUser(regRequest);

                if (userId == null) {
                    System.out.println("message: Username or email already exists");
                    break;
                }

                // Assign IAM role for Google Cloud Storage
                boolean roleAssigned = userService.assignUserStorageRole(regRequest.getEmail(), userId);

                if (!roleAssigned) {
                    System.out.println("⚠️ User registered, but failed to assign IAM role.");
                } else {
                    System.out.println("✅ IAM role assigned successfully!");
                }

                System.out.println("🙂 Registration successful!");
                break;


            case "login":
                UserLoginRequest loginRequest = new UserLoginRequest();
                loginRequest.setUsername(username);
                loginRequest.setPassword(password);

                try {
                    boolean loggedIn = userService.authenticate(loginRequest);
                    if (loggedIn) {
                        System.out.println(" Login successful!");

                        Optional<User> loggedInUser = userService.getUserByUsername(username);
                        if (loggedInUser != null && "ADMIN".equalsIgnoreCase(loggedInUser.get().getRole())) {
                            System.out.println("🔐 Welcome Admin!");
                            System.out.println("Options: [view-all] [sayhi]");
                            String option = scanner.nextLine().trim().toLowerCase();

                            if ("view-all".equals(option)) {
                                try {
                                    List<User> allUsers = userService.getAllUsers(username);
                                    System.out.println(" All Users:");
                                    for (User user : allUsers) {
                                        System.out.println("- " + user.getUsername() + " (" + user.getRole() + ")");
                                    }
                                } catch (Exception e) {
                                    System.out.println(" Could not fetch users: " + e.getMessage());
                                }
                            } else if ("sayhi".equals(option)) {
                                System.out.println(" Hello Admin, how’s it going?");
                            } else {
                                System.out.println("?  Unknown command.");
                            }

                        } else {
                            System.out.println(" Welcome User!");
                            System.out.println("Options: [sayhi]");
                            String option = scanner.nextLine().trim().toLowerCase();

                            if ("sayhi".equals(option)) {
                                System.out.println(" Hello there! Hope you're doing well.");
                            } else {
                                System.out.println(" ? Unknown command.");
                            }
                        }
                    } else {
                        System.out.println(" Login failed: Invalid credentials.");
                    }
                } catch (Exception e) {
                    System.out.println(" Login failed: " + e.getMessage());
                }
                break;

            default:
                System.out.println(" ? Unknown action.");
        }

        scanner.close();
    }

}
