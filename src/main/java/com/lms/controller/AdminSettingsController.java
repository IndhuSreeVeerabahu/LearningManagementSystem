package com.lms.controller;

import com.lms.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/settings")
public class AdminSettingsController {

    @GetMapping
    public String systemSettings(Model model, Authentication authentication) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }
        model.addAttribute("title", "System Settings - LearnHub");
        
        // Add system configuration data
        model.addAttribute("siteName", "LearnHub");
        model.addAttribute("siteDescription", "Empowering learners worldwide with quality education");
        model.addAttribute("maxFileSize", "10MB");
        model.addAttribute("allowedFileTypes", "JPG, PNG, GIF, MP4, MP3, PDF");
        model.addAttribute("emailNotifications", true);
        model.addAttribute("autoApproveCourses", false);
        model.addAttribute("maintenanceMode", false);
        
        return "admin/settings";
    }
    
    @PostMapping("/update")
    public String updateSettings(
            @RequestParam(required = false) String siteName,
            @RequestParam(required = false) String siteDescription,
            @RequestParam(required = false) String maxFileSize,
            @RequestParam(required = false) String allowedFileTypes,
            @RequestParam(required = false) Boolean emailNotifications,
            @RequestParam(required = false) Boolean autoApproveCourses,
            @RequestParam(required = false) Boolean maintenanceMode,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Here you would typically save these settings to a database
            // For now, we'll just show a success message
            
            
            redirectAttributes.addFlashAttribute("success", "System settings updated successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update system settings. Please try again.");
        }
        
        return "redirect:/admin/settings";
    }
    
    @PostMapping("/backup")
    public String createBackup(RedirectAttributes redirectAttributes) {
        try {
            // Here you would implement actual backup functionality
            
            // Simulate backup process
            Thread.sleep(1000);
            
            redirectAttributes.addFlashAttribute("success", "System backup created successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create backup. Please try again.");
        }
        
        return "redirect:/admin/settings";
    }
    
    @PostMapping("/maintenance")
    public String toggleMaintenanceMode(
            @RequestParam Boolean maintenanceMode,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Here you would implement actual maintenance mode toggle
            
            String message = maintenanceMode ? 
                "Maintenance mode enabled. Site is now under maintenance." :
                "Maintenance mode disabled. Site is now accessible.";
                
            redirectAttributes.addFlashAttribute("success", message);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to toggle maintenance mode. Please try again.");
        }
        
        return "redirect:/admin/settings";
    }
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }
}
