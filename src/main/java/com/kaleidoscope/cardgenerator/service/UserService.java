package com.kaleidoscope.cardgenerator.service;

import com.kaleidoscope.cardgenerator.model.User;
import com.kaleidoscope.cardgenerator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private KeycloakService keycloakService;

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

    @SuppressWarnings("UnusedReturnValue")
    public User createUser(String username, String password) throws HttpClientErrorException {
        User user = new User(username);
        userRepository.save(user);
        try {
            keycloakService.createUser(username, password);
        } catch (Exception e) {
            // HttpClientErrorException with 409 means that the user already exists in keycloak
            if (!(e instanceof HttpClientErrorException) ||
                    ((HttpClientErrorException) e).getStatusCode() != HttpStatus.CONFLICT) {
                userRepository.delete(user);
            }
            throw e;
        }
         return user;
    }
}
