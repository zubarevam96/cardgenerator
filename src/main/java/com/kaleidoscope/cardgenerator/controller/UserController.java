package com.kaleidoscope.cardgenerator.controller;

import com.kaleidoscope.cardgenerator.model.User;
import com.kaleidoscope.cardgenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @GetMapping("/users/all")
    public List<User> getAll() {
        return userService.findAll();
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return userService.verify(user);
    }
}
