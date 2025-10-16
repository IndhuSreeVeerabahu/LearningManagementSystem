package com.lms.controller;

import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.service.CourseService;
import com.lms.service.LessonService;
import com.lms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private LessonService lessonService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> allUsers = userService.getAllUsers();
        List<Course> allCourses = courseService.getAllCourses();
        List<Course> pendingCourses = courseService.getPendingApprovalCourses();
        
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalCourses", allCourses.size());
        model.addAttribute("pendingCourses", pendingCourses.size());
        model.addAttribute("pendingCoursesList", pendingCourses);
        model.addAttribute("currentUser", getCurrentUser());
        
        return "admin/dashboard";
    }
    
    @GetMapping("/users")
    public String users(Model model, @RequestParam(required = false) String search) {
        List<User> users;
        
        if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search);
            model.addAttribute("searchTerm", search);
        } else {
            users = userService.getAllUsers();
        }
        
        model.addAttribute("users", users);
        model.addAttribute("currentUser", getCurrentUser());
        return "admin/users";
    }
    
    @GetMapping("/courses")
    public String courses(Model model, @RequestParam(required = false) String search) {
        List<Course> courses;
        
        if (search != null && !search.trim().isEmpty()) {
            courses = courseService.searchCourses(search);
            model.addAttribute("searchTerm", search);
        } else {
            courses = courseService.getAllCourses();
        }
        
        model.addAttribute("courses", courses);
        model.addAttribute("currentUser", getCurrentUser());
        return "admin/courses";
    }
    
    @GetMapping("/courses/{courseId}/view")
    public String viewCourse(@PathVariable Long courseId, Model model) {
        Course course = courseService.getCourseById(courseId).orElse(null);
        if (course == null) {
            return "redirect:/admin/courses";
        }
        
        List<Lesson> lessons = lessonService.getLessonsByCourse(course);
        
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("currentUser", getCurrentUser());
        return "admin/course-view";
    }
    
    private User getCurrentUser() {
        return (User) org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
    
    @PostMapping("/users/{userId}/toggle-status")
    public String toggleUserStatus(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            if (user.getIsActive()) {
                userService.deactivateUser(userId);
            } else {
                userService.activateUser(userId);
            }
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{userId}/change-role")
    public String changeUserRole(@PathVariable Long userId, @RequestParam Role newRole) {
        User user = userService.getUserById(userId);
        if (user != null) {
            user.setRole(newRole);
            userService.updateUser(user);
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/courses/{courseId}/approve")
    public String approveCourse(@PathVariable Long courseId) {
        courseService.approveCourse(courseId);
        return "redirect:/admin/courses";
    }
    
    @PostMapping("/courses/{courseId}/reject")
    public String rejectCourse(@PathVariable Long courseId) {
        courseService.rejectCourse(courseId);
        return "redirect:/admin/courses";
    }
    
    @PostMapping("/courses/{courseId}/delete")
    public String deleteCourse(@PathVariable Long courseId, RedirectAttributes redirectAttributes) {
        try {
            Optional<Course> courseOpt = courseService.getCourseById(courseId);
            if (courseOpt.isPresent()) {
                Course course = courseOpt.get();
                String courseTitle = course.getTitle();
                courseService.deleteCourse(courseId);
                redirectAttributes.addFlashAttribute("success", "Course '" + courseTitle + "' has been deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Course not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete course: " + e.getMessage());
        }
        return "redirect:/admin/courses";
    }
}
