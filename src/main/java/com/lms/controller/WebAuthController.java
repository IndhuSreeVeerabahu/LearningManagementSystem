package com.lms.controller;

import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebAuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String firstName,
                          @RequestParam String lastName,
                          @RequestParam(defaultValue = "STUDENT") String role,
                          RedirectAttributes redirectAttributes) {
        try {
            // Check if username already exists
            if (userService.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("error", "Username is already taken");
                return "redirect:/register";
            }
            
            // Check if email already exists
            if (userService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "Email is already registered");
                return "redirect:/register";
            }
            
            // Create new user
            User user = new User(
                username,
                email,
                password,
                firstName,
                lastName,
                Role.valueOf(role)
            );
            
            user = userService.saveUser(user);
            
            // Auto-login after registration
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            redirectAttributes.addFlashAttribute("success", "Registration successful! Welcome to LearnHub!");
            
            // Redirect based on role
            switch (user.getRole()) {
                case ADMIN:
                    return "redirect:/admin/dashboard";
                case INSTRUCTOR:
                    return "redirect:/instructor/dashboard";
                case STUDENT:
                default:
                    return "redirect:/student/dashboard";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }
    
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }
}
