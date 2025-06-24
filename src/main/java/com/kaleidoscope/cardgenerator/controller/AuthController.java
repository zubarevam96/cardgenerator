package com.kaleidoscope.cardgenerator.controller;

import com.kaleidoscope.cardgenerator.model.User;
import com.kaleidoscope.cardgenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam(name = "password-confirm") String passwordConfirm,
                               Model model) {
        // Check if passwords match
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "Passwords do not match");
            return "register";
        }

        if (userService.usernameExists(username)) {
            model.addAttribute("error", "Username already taken");
            return "register";
        }
        User user = new User(username, password);
        userService.save(user);

        return "redirect:/login";
    }
}
