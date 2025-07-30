package com.FVProject.filevault.security;

import com.FVProject.filevault.model.User;
import com.FVProject.filevault.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsServiceImpl {
    @Autowired
    private UserRepository userRepository;


    public CustomUserDetails loadUserByUsername(String username) {

        User user = userRepository.findByUsername(username);
        return new CustomUserDetails(user);
    }

}
