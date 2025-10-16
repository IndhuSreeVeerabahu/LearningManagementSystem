package com.lms.controller;

import com.lms.entity.User;
import com.lms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/profile")
    public String profile(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", currentUser);
        return "profile";
    }
    
    @PostMapping("/profile")
    public String updateProfile(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String email,
                               RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            // Check if email is being changed and if it's already taken
            if (!currentUser.getEmail().equals(email) && userService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "Email is already taken by another user");
                return "redirect:/profile";
            }
            
            // Update user information
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            currentUser.setEmail(email);
            
            userService.updateUser(currentUser);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }
}
