package com.kaleidoscope.cardgenerator.controller;

import com.kaleidoscope.cardgenerator.service.UserService;
import com.kaleidoscope.cardgenerator.service.KeycloakService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
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

    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            Model model,
                            HttpSession session) {
        try {
            // Obtain access token from Keycloak using Password Grant
            String token = keycloakService.getToken(username, password);

            // Fetch user details using the token
            UserDetails userDetails = keycloakService.getUserDetails(token);

            // Create an Authentication object
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            // Set the authentication in Spring Security's context
            SecurityContextHolder.getContext().setAuthentication(auth);

            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // Redirect to home page on success
            return "redirect:/home";
        } catch (AuthorizationDeniedException e) {
            model.addAttribute("error", e.getLocalizedMessage());
            return "login";
        } catch (Exception ex) {
            log.error("Error occurred while authenticating a user {}", username, ex);
            model.addAttribute("error", ex.getLocalizedMessage());
            return "login";
        }
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
            userService.createUser(username, password);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getLocalizedMessage());
            return "register";
        }

        return "redirect:/login";
    }
}
