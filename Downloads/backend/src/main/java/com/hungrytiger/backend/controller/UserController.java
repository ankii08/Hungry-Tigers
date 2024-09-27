package com.hungrytiger.backend.controller;

import com.hungrytiger.backend.model.User;
import com.hungrytiger.backend.security.JwtUtils;
import com.hungrytiger.backend.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * User registration endpoint.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body("Email is already in use.");
        }
        User savedUser = userService.registerUser(user);
        // Remove sensitive information before returning
        savedUser.setPassword(null);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * User login endpoint.
     * Returns a JWT token if authentication is successful.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginData.get("email"),
                            loginData.get("password")
                    )
            );

            // Generate JWT token
            String token = jwtUtils.generateJwtToken(authentication);

            // Return the token in the response
            return ResponseEntity.ok(Map.of("token", token));
        } catch (AuthenticationException e) {
            // Return unauthorized status if authentication fails
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    /**
     * Get the authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getUserProfile() {
        // Get the authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Remove sensitive information before returning
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    /**
     * Update the authenticated user's profile.
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody User updatedUser) {
        // Get the authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            // Update user details
            existingUser.setName(updatedUser.getName());
            // You can add more fields to update as needed

            userService.saveUser(existingUser);
            // Remove sensitive information before returning
            existingUser.setPassword(null);
            return ResponseEntity.ok(existingUser);
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    /**
     * Logout endpoint (optional, for client-side token invalidation).
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // Since JWT is stateless, logout can be handled on the client side by deleting the token
        return ResponseEntity.ok("Logout successful");
    }
}
