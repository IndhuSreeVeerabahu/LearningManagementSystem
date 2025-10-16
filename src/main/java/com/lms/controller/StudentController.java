package com.lms.controller;

import com.lms.entity.*;
import com.lms.service.CourseService;
import com.lms.service.EnrollmentService;
import com.lms.service.LessonService;
import com.lms.service.ProgressService;
import com.lms.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
public class StudentController {
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private LessonService lessonService;
    
    @Autowired
    private ProgressService progressService;
    
    @Autowired
    private CertificateService certificateService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = getCurrentUser();
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(currentUser);
        
        // Calculate statistics
        long totalEnrolled = enrollments.size();
        long completedCourses = enrollments.stream()
            .mapToLong(enrollment -> enrollment.getIsCompleted() ? 1 : 0)
            .sum();
        
        // Calculate total completed lessons across all courses
        long totalCompletedLessons = 0;
        for (Enrollment enrollment : enrollments) {
            totalCompletedLessons += progressService.countCompletedLessonsByStudentAndCourse(currentUser, enrollment.getCourse());
        }
        
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("totalEnrolled", totalEnrolled);
        model.addAttribute("completedCourses", completedCourses);
        model.addAttribute("totalCompletedLessons", totalCompletedLessons);
        
        return "student/dashboard";
    }
    
    @GetMapping("/courses")
    public String browseCourses(Model model, @RequestParam(required = false) String search) {
        List<Course> courses;
        
        if (search != null && !search.trim().isEmpty()) {
            courses = courseService.searchCourses(search);
            model.addAttribute("searchTerm", search);
        } else {
            courses = courseService.getPublishedAndApprovedCourses();
        }
        
        User currentUser = getCurrentUser();
        model.addAttribute("courses", courses);
        model.addAttribute("currentUser", currentUser);
        
        return "student/courses";
    }
    
    @GetMapping("/courses/{courseId}")
    public String courseDetails(@PathVariable Long courseId, Model model) {
        Course course = courseService.getCourseById(courseId).orElse(null);
        if (course == null) {
            return "redirect:/student/courses";
        }
        
        User currentUser = getCurrentUser();
        boolean isEnrolled = enrollmentService.isEnrolled(currentUser, course);
        
        model.addAttribute("course", course);
        model.addAttribute("isEnrolled", isEnrolled);
        model.addAttribute("currentUser", currentUser);
        
        if (isEnrolled) {
            List<Lesson> lessons = lessonService.getPublishedLessonsByCourse(course);
            model.addAttribute("lessons", lessons);
            
            // Get progress for this course
            double progressPercentage = progressService.getCourseProgressPercentage(currentUser, course);
            model.addAttribute("progressPercentage", progressPercentage);
        }
        
        return "student/course-details";
    }
    
    @PostMapping("/courses/{courseId}/enroll")
    public String enrollInCourse(@PathVariable Long courseId, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            Course course = courseService.getCourseById(courseId).orElse(null);
            
            
            if (course == null) {
                redirectAttributes.addFlashAttribute("error", "Course not found");
                return "redirect:/courses";
            }
            
            
            if (enrollmentService.isEnrolled(currentUser, course)) {
                redirectAttributes.addFlashAttribute("error", "You are already enrolled in this course");
                return "redirect:/student/courses/" + courseId;
            }
            
            enrollmentService.enrollStudent(currentUser, course);
            redirectAttributes.addFlashAttribute("success", "Successfully enrolled in " + course.getTitle() + "!");
            return "redirect:/student/courses/" + courseId;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to enroll: " + e.getMessage());
            return "redirect:/courses/" + courseId;
        }
    }
    
    @GetMapping("/courses/{courseId}/lessons/{lessonId}")
    public String viewLesson(@PathVariable Long courseId, @PathVariable Long lessonId, Model model) {
        User currentUser = getCurrentUser();
        Course course = courseService.getCourseById(courseId).orElse(null);
        Lesson lesson = lessonService.getLessonById(lessonId).orElse(null);
        
        if (course == null || lesson == null || !enrollmentService.isEnrolled(currentUser, course)) {
            return "redirect:/student/courses";
        }
        
        // Check if lesson is already completed
        boolean isLessonCompleted = progressService.getProgressByStudentAndLesson(currentUser, lesson)
            .map(Progress::getIsCompleted)
            .orElse(false);
        
        // Get all lessons for navigation with completion status
        List<Lesson> allLessons = lessonService.getPublishedLessonsByCourse(course);
        
        model.addAttribute("course", course);
        model.addAttribute("lesson", lesson);
        model.addAttribute("allLessons", allLessons);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isLessonCompleted", isLessonCompleted);
        
        return "student/lesson-view";
    }
    
    @PostMapping("/courses/{courseId}/lessons/{lessonId}/complete")
    public String markLessonComplete(@PathVariable Long courseId, @PathVariable Long lessonId, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            Course course = courseService.getCourseById(courseId).orElse(null);
            Lesson lesson = lessonService.getLessonById(lessonId).orElse(null);
            
            
            if (course != null && lesson != null && enrollmentService.isEnrolled(currentUser, course)) {
                // Check if lesson is already completed
                boolean isAlreadyCompleted = progressService.getProgressByStudentAndLesson(currentUser, lesson)
                    .map(Progress::getIsCompleted)
                    .orElse(false);
                
                if (isAlreadyCompleted) {
                    redirectAttributes.addFlashAttribute("info", "This lesson is already marked as complete!");
                } else {
                    // Mark lesson as completed
                    progressService.markLessonAsCompleted(currentUser, lesson);
                    
                    // Update enrollment progress
                    Optional<Enrollment> enrollment = enrollmentService.getEnrollmentByStudentAndCourse(currentUser, course);
                    if (enrollment.isPresent()) {
                        enrollmentService.updateEnrollmentProgress(enrollment.get());
                    }
                    
                    redirectAttributes.addFlashAttribute("success", "Lesson marked as complete!");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to mark lesson as complete");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error marking lesson as complete: " + e.getMessage());
        }
        
        return "redirect:/student/courses/" + courseId + "/lessons/" + lessonId;
    }
    
    @GetMapping("/my-courses")
    public String myCourses(Model model) {
        User currentUser = getCurrentUser();
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(currentUser);
        
        // Calculate progress statistics
        long totalEnrolled = enrollments.size();
        long completedCourses = enrollments.stream()
            .mapToLong(enrollment -> enrollment.getIsCompleted() ? 1 : 0)
            .sum();
        long inProgressCourses = totalEnrolled - completedCourses;
        
        model.addAttribute("enrolledCourses", enrollments);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("totalEnrolled", totalEnrolled);
        model.addAttribute("completedCourses", completedCourses);
        model.addAttribute("inProgressCourses", inProgressCourses);
        
        return "student/my-courses";
    }
    
    @GetMapping("/courses/{courseId}/certificate")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long courseId) {
        try {
            User currentUser = getCurrentUser();
            Course course = courseService.getCourseById(courseId).orElse(null);
            
            if (course == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if student is enrolled and course is completed
            if (!enrollmentService.isEnrolled(currentUser, course)) {
                return ResponseEntity.badRequest().build();
            }
            
            var enrollment = enrollmentService.getEnrollmentByStudentAndCourse(currentUser, course);
            if (enrollment.isEmpty() || !enrollment.get().getIsCompleted()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Generate or get existing certificate
            var certificate = certificateService.getCertificate(currentUser, course);
            if (certificate.isEmpty()) {
                // Generate new certificate
                var newCertificate = certificateService.generateCertificate(currentUser, course);
                certificate = Optional.of(newCertificate);
            }
            
            // Get certificate file
            byte[] certificateData = certificateService.getCertificatePdf(certificate.get());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            headers.setContentDispositionFormData("attachment", 
                "certificate_" + course.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".html");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(certificateData);
                
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }
}
