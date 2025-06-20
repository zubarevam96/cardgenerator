package com.kaleidoscope.cardgenerator.service;

import com.kaleidoscope.cardgenerator.exception.UserAlreadyExistAuthenticationException;
import com.kaleidoscope.cardgenerator.model.User;
import com.kaleidoscope.cardgenerator.model.UserPrincipal;
import com.kaleidoscope.cardgenerator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private final Map<String, UserPrincipal> userPrincipalCache = new HashMap<>();


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getByUsername(username);

        return userPrincipalCache.computeIfAbsent(user.getUsername(), k -> new UserPrincipal(user));
    }

    public User getByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User '" + username + "' not found in database"));
    }
}
