package com.FVProject.filevault.cli;

import com.FVProject.filevault.DTO.UserLoginRequest;
import com.FVProject.filevault.DTO.UserRegisterRequest;
import com.FVProject.filevault.model.User;
import com.FVProject.filevault.services.UserService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class CLIRunner {

    private final UserService userService;

    public CLIRunner(UserService userService) {
        this.userService = userService;
    }

    public String registerUser(String username, String password, String email) {
        UserRegisterRequest regRequest = new UserRegisterRequest();
        regRequest.setUsername(username);
        regRequest.setPassword(password);
        regRequest.setEmail(email);

        Long userId = userService.createUser(regRequest);
        return (userId == null) ? "Username or email already exists." : "Registration successful!";
    }

    public String loginUser(String username, String password) {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        return userService.authenticate(loginRequest) ? "Login successful!" : "Invalid username or password.";
    }

    public List<String> listAllUsers() {
        return userService.getAllUsers("admin").stream()
                .map(u -> u.getUsername() + " (" + u.getRole() + ")")
                .toList();
    }

    public void handleAdminFlow(Scanner scanner) {
        System.out.println("Welcome Admin! Type 'view-all', 'sayhi', or 'exit':");
        while (true) {
            String command = scanner.nextLine().trim().toLowerCase();
            switch (command) {
                case "view-all" -> listAllUsers().forEach(System.out::println);
                case "sayhi" -> System.out.println("Hello Admin!");
                case "exit" -> { System.out.println("Goodbye."); return; }
                default -> System.out.println("Unknown command.");
            }
        }
    }

    public void handleUserFlow(Scanner scanner) {
        System.out.println("Welcome User! Type 'sayhi' or 'exit':");
        while (true) {
            String command = scanner.nextLine().trim().toLowerCase();
            switch (command) {
                case "sayhi" -> System.out.println("Hello there!");
                case "exit" -> { System.out.println("Goodbye."); return; }
                default -> System.out.println("Unknown command.");
            }
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Type 'register' or 'login':");
        String action = scanner.nextLine().trim().toLowerCase();

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        String result;
        if ("register".equals(action)) {
            System.out.print("Email: ");
            String email = scanner.nextLine();
            result = registerUser(username, password, email);
            System.out.println(result);
        } else if ("login".equals(action)) {
            result = loginUser(username, password);
            System.out.println(result);

            if ("Login successful!".equals(result)) {
                User user = userService.getUserByUsername(username);
                if (user != null) {
                    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                        handleAdminFlow(scanner);
                    } else {
                        handleUserFlow(scanner);
                    }
                }
            }
        } else {
            System.out.println("Unknown action.");
        }
    }
}


