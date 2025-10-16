package com.lms.controller;

import com.lms.entity.Course;
import com.lms.entity.User;
import com.lms.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {
    
    @Autowired
    private CourseService courseService;
    
    @GetMapping("/")
    public String home(Model model, @RequestParam(required = false) String search, @RequestParam(required = false) String logout) {
        List<Course> courses;
        
        if (search != null && !search.trim().isEmpty()) {
            courses = courseService.searchCourses(search);
            model.addAttribute("searchTerm", search);
        } else {
            courses = courseService.getPublishedAndApprovedCourses();
        }
        
        model.addAttribute("courses", courses);
        
        // Get current user info
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User currentUser = (User) auth.getPrincipal();
            model.addAttribute("currentUser", currentUser);
        }
        
        // Handle logout message
        if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully.");
        }
        
        return "index";
    }
    
}
