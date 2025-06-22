package com.kaleidoscope.cardgenerator.service;

import com.kaleidoscope.cardgenerator.model.UserPrincipal;
import com.kaleidoscope.cardgenerator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    private final Map<String, UserPrincipal> userPrincipalCache = new HashMap<>();


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userPrincipalCache.computeIfAbsent(username, k ->
                new UserPrincipal(userRepository.findByUsername(username).orElseThrow(() ->
                        new UsernameNotFoundException("User with name '" + username + "' not found"))));
    }
}
