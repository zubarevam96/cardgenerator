package com.kaleidoscope.cardgenerator.service;

import com.kaleidoscope.cardgenerator.model.User;
import com.kaleidoscope.cardgenerator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return getByUsername(username);
    }

    public User getByUsername(String username) {
       return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User with name '" + username + "' not found"));
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
