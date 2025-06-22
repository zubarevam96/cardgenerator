package com.kaleidoscope.cardgenerator.service;

import com.kaleidoscope.cardgenerator.exception.UserAlreadyExistAuthenticationException;
import com.kaleidoscope.cardgenerator.model.User;
import com.kaleidoscope.cardgenerator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    public User register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            //TODO: looks like this error cannot be read by unauthorized user
            throw new UserAlreadyExistAuthenticationException(
                    "User with name '" + user.getUsername() + "' already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public String verify(User user) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        user.getUsername(), user.getPassword()));

        if (authentication.isAuthenticated())
            return jwtService.generateToken(user.getUsername());

        return "fail";
    }

    public User getCurrentUser() {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return getByUsername(principal.getUsername());
    }

    public User getByUsername(String username) {
       return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User with name '" + username + "' not found"));
    }
}
