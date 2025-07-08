package com.kaleidoscope.cardgenerator.controller;

import com.kaleidoscope.cardgenerator.service.UserService;
import com.kaleidoscope.cardgenerator.service.KeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@Controller
public class AuthController {

    @Autowired
    private KeycloakService keycloakService;

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
        if (username.length() < 4) {
            model.addAttribute("error", "Username length should be at least 4 symbols");
            return "register";
        }

        if (password.length() < 8) {
            model.addAttribute("error", "Password length should be at least 8 symbols");
        }

        // Check if passwords match
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "Passwords do not match");
            return "register";
        }

        if (userService.usernameExists(username)) {
            model.addAttribute("error", "Username already taken");
            return "register";
        }

        try {
            keycloakService.createUser(username, password);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getLocalizedMessage());
            return "register";
        }

        return "redirect:/login";
    }
}
