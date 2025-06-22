package com.kaleidoscope.cardgenerator.controller;

import com.kaleidoscope.cardgenerator.model.User;
import com.kaleidoscope.cardgenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @GetMapping("/users")
    public String getAll(Model model) {
        List<User> users = userService.findAll();

        model.addAttribute("users", users);

        return "users";
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return userService.verify(user);
    }
}
