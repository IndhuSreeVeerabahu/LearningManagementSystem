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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    @GetMapping("/courses")
    public String browseCourses(Model model, @RequestParam(required = false) String search) {
        try {
            // Get current user info
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User) {
                User currentUser = (User) auth.getPrincipal();
                model.addAttribute("currentUser", currentUser);
            }
            
            // Get courses
            if (search != null && !search.trim().isEmpty()) {
                var courses = courseService.searchCourses(search);
                model.addAttribute("courses", courses);
                model.addAttribute("searchTerm", search);
            } else {
                var courses = courseService.getPublishedAndApprovedCourses();
                model.addAttribute("courses", courses);
                
                // If no published courses, show all courses for debugging
                if (courses.isEmpty()) {
                    var allCourses = courseService.getAllCourses();
                    for (var course : allCourses) {
                    }
                }
            }
            
            return "courses/browse";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("courses", java.util.Collections.emptyList());
            return "courses/browse";
        }
    }
    
    @GetMapping("/courses/{courseId}")
    public String courseDetails(@PathVariable Long courseId, Model model) {
        Course course = courseService.getCourseById(courseId).orElse(null);
        if (course == null) {
            return "redirect:/courses";
        }
        
        // Get current user info
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User currentUser = (User) auth.getPrincipal();
            model.addAttribute("currentUser", currentUser);
        }
        
        model.addAttribute("course", course);
        return "courses/details";
    }
}
