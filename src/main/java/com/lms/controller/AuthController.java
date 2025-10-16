package com.lms.controller;

import com.lms.dto.AuthResponse;
import com.lms.dto.LoginRequest;
import com.lms.dto.RegisterRequest;
import com.lms.entity.User;
import com.lms.security.JwtUtil;
import com.lms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal());
            
            User user = userService.findByUsername(loginRequest.getUsername()).orElse(null);
            if (user != null) {
                AuthResponse response = new AuthResponse(
                    jwt,
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole()
                );
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.badRequest().body(new AuthResponse("User not found"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse("Invalid username or password"));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Check if username already exists
            if (userService.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity.badRequest().body(new AuthResponse("Username is already taken"));
            }
            
            // Check if email already exists
            if (userService.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest().body(new AuthResponse("Email is already registered"));
            }
            
            // Create new user
            User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getRole()
            );
            
            user = userService.saveUser(user);
            
            // Generate JWT token
            String jwt = jwtUtil.generateToken(user);
            
            AuthResponse response = new AuthResponse(
                jwt,
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse("Registration failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new AuthResponse("Logged out successfully"));
    }
}
