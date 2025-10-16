package com.lms.controller;

import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.entity.User;
import com.lms.service.CourseService;
import com.lms.service.FileStorageService;
import com.lms.service.LessonService;
import com.lms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/instructor")
@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
public class InstructorController {
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private LessonService lessonService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            
            List<Course> myCourses = courseService.getCoursesByInstructor(currentUser);
            
            model.addAttribute("courses", myCourses);
            model.addAttribute("currentUser", currentUser);
            
            return "instructor/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login";
        }
    }
    
    @GetMapping("/courses")
    public String courses(Model model) {
        User currentUser = getCurrentUser();
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        List<Course> courses = courseService.getCoursesByInstructor(currentUser);
        
        // Debug: Print course image URLs
        for (Course course : courses) {
        }
        
        model.addAttribute("courses", courses);
        model.addAttribute("currentUser", currentUser);
        return "instructor/courses";
    }
    
    @GetMapping("/courses/new")
    public String newCourse(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("course", new Course());
        model.addAttribute("currentUser", currentUser);
        return "instructor/course-form";
    }
    
    @PostMapping("/courses")
    public String createCourse(@ModelAttribute Course course) {
        try {
            User currentUser = getCurrentUser();
            course.setInstructor(currentUser);
            
            if (course.getCourseImageFile() != null && !course.getCourseImageFile().isEmpty()) {
                try {
                    String fileUrl = fileStorageService.uploadFile(course.getCourseImageFile(), "course-images");
                    course.setCourseImage(fileUrl);
                } catch (Exception e) {
                    // Continue without image
                }
            }
            
            courseService.saveCourse(course);
            return "redirect:/instructor/courses";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/instructor/courses/new";
        }
    }
    
    @GetMapping("/courses/{courseId}/edit")
    public String editCourse(@PathVariable Long courseId, Model model) {
        Course course = courseService.getCourseById(courseId).orElse(null);
        if (course == null || !course.getInstructor().getId().equals(getCurrentUser().getId())) {
            return "redirect:/instructor/courses";
        }
        
        model.addAttribute("course", course);
        return "instructor/course-form";
    }
    
    @PostMapping("/courses/{courseId}")
    public String updateCourse(@PathVariable Long courseId, 
                              @ModelAttribute Course course) {
        Course existingCourse = courseService.getCourseById(courseId).orElse(null);
        if (existingCourse == null || !existingCourse.getInstructor().getId().equals(getCurrentUser().getId())) {
            return "redirect:/instructor/courses";
        }
        
        existingCourse.setTitle(course.getTitle());
        existingCourse.setDescription(course.getDescription());
        
        if (course.getCourseImageFile() != null && !course.getCourseImageFile().isEmpty()) {
            try {
                String fileUrl = fileStorageService.uploadFile(course.getCourseImageFile(), "course-images");
                existingCourse.setCourseImage(fileUrl);
            } catch (Exception e) {
                // Handle file upload error
            }
        }
        
        courseService.updateCourse(existingCourse);
        return "redirect:/instructor/courses";
    }
    
    @GetMapping("/courses/{courseId}/lessons")
    public String courseLessons(@PathVariable Long courseId, Model model) {
        User currentUser = getCurrentUser();
        Course course = courseService.getCourseById(courseId).orElse(null);
        if (course == null || !course.getInstructor().getId().equals(currentUser.getId())) {
            return "redirect:/instructor/courses";
        }
        
        List<Lesson> lessons = lessonService.getLessonsByCourse(course);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("currentUser", currentUser);
        
        return "instructor/lessons";
    }
    
    @GetMapping("/courses/{courseId}/lessons/new")
    public String newLesson(@PathVariable Long courseId, Model model) {
        User currentUser = getCurrentUser();
        Course course = courseService.getCourseById(courseId).orElse(null);
        if (course == null || !course.getInstructor().getId().equals(currentUser.getId())) {
            return "redirect:/instructor/courses";
        }
        
        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        model.addAttribute("lesson", lesson);
        model.addAttribute("course", course);
        model.addAttribute("currentUser", currentUser);
        
        return "instructor/lesson-form";
    }
    
    @PostMapping("/courses/{courseId}/lessons")
    public String createLesson(@PathVariable Long courseId, 
                              @ModelAttribute Lesson lesson,
                              @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                              @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              @RequestParam(value = "audioFile", required = false) MultipartFile audioFile) {
        Course course = courseService.getCourseById(courseId).orElse(null);
        if (course == null || !course.getInstructor().getId().equals(getCurrentUser().getId())) {
            return "redirect:/instructor/courses";
        }
        
        lesson.setCourse(course);
        
        // Handle file uploads
        try {
            if (videoFile != null && !videoFile.isEmpty()) {
                String fileUrl = fileStorageService.uploadFile(videoFile, "videos");
                lesson.setVideoUrl(fileUrl);
            }
            if (pdfFile != null && !pdfFile.isEmpty()) {
                String fileUrl = fileStorageService.uploadFile(pdfFile, "pdfs");
                lesson.setPdfUrl(fileUrl);
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileUrl = fileStorageService.uploadFile(imageFile, "images");
                lesson.setImageUrl(fileUrl);
            }
            if (audioFile != null && !audioFile.isEmpty()) {
                String fileUrl = fileStorageService.uploadFile(audioFile, "audio");
                lesson.setAudioUrl(fileUrl);
            }
        } catch (Exception e) {
            // Handle file upload error
        }
        
        lessonService.saveLesson(lesson);
        return "redirect:/instructor/courses/" + courseId + "/lessons";
    }
    
    @GetMapping("/courses/{courseId}/lessons/{lessonId}/edit")
    public String editLesson(@PathVariable Long courseId, @PathVariable Long lessonId, Model model) {
        User currentUser = getCurrentUser();
        Course course = courseService.getCourseById(courseId).orElse(null);
        
        if (course == null || !course.getInstructor().getId().equals(currentUser.getId())) {
            return "redirect:/instructor/courses";
        }
        
        Lesson lesson = lessonService.getLessonById(lessonId).orElse(null);
        if (lesson == null || !lesson.getCourse().getId().equals(courseId)) {
            return "redirect:/instructor/courses/" + courseId + "/lessons";
        }
        
        model.addAttribute("course", course);
        model.addAttribute("lesson", lesson);
        model.addAttribute("currentUser", currentUser);
        return "instructor/lesson-form";
    }
    
    @PostMapping("/courses/{courseId}/lessons/{lessonId}")
    public String updateLesson(@PathVariable Long courseId, @PathVariable Long lessonId,
                              @ModelAttribute Lesson lesson,
                              @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                              @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              @RequestParam(value = "audioFile", required = false) MultipartFile audioFile) {
        User currentUser = getCurrentUser();
        Course course = courseService.getCourseById(courseId).orElse(null);
        
        if (course == null || !course.getInstructor().getId().equals(currentUser.getId())) {
            return "redirect:/instructor/courses";
        }
        
        Lesson existingLesson = lessonService.getLessonById(lessonId).orElse(null);
        if (existingLesson == null || !existingLesson.getCourse().getId().equals(courseId)) {
            return "redirect:/instructor/courses/" + courseId + "/lessons";
        }
        
        // Update lesson properties
        existingLesson.setTitle(lesson.getTitle());
        existingLesson.setContent(lesson.getContent());
        existingLesson.setDurationMinutes(lesson.getDurationMinutes());
        existingLesson.setIsPublished(lesson.getIsPublished());
        
        // Handle file uploads
        try {
            if (videoFile != null && !videoFile.isEmpty()) {
                String fileUrl = fileStorageService.uploadFile(videoFile, "videos");
                existingLesson.setVideoUrl(fileUrl);
            }
            if (pdfFile != null && !pdfFile.isEmpty()) {
                String fileUrl = fileStorageService.uploadFile(pdfFile, "pdfs");
                existingLesson.setPdfUrl(fileUrl);
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileUrl = fileStorageService.uploadFile(imageFile, "images");
                existingLesson.setImageUrl(fileUrl);
            }
            if (audioFile != null && !audioFile.isEmpty()) {
                String fileUrl = fileStorageService.uploadFile(audioFile, "audio");
                existingLesson.setAudioUrl(fileUrl);
            }
        } catch (Exception e) {
            // Handle file upload error
        }
        
        lessonService.saveLesson(existingLesson);
        return "redirect:/instructor/courses/" + courseId + "/lessons";
    }
    
    @PostMapping("/courses/{courseId}/publish")
    public String publishCourse(@PathVariable Long courseId) {
        Course course = courseService.getCourseById(courseId).orElse(null);
        if (course != null && course.getInstructor().getId().equals(getCurrentUser().getId())) {
            courseService.publishCourse(courseId);
        }
        return "redirect:/instructor/courses";
    }
    
    @PostMapping("/courses/{courseId}/delete")
    public String deleteCourse(@PathVariable Long courseId, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            Optional<Course> courseOpt = courseService.getCourseById(courseId);
            
            if (courseOpt.isPresent()) {
                Course course = courseOpt.get();
                
                // Check if the current user is the instructor of this course
                if (course.getInstructor().getId().equals(currentUser.getId())) {
                    String courseTitle = course.getTitle();
                    courseService.deleteCourse(courseId);
                    redirectAttributes.addFlashAttribute("success", "Course '" + courseTitle + "' has been deleted successfully!");
                } else {
                    redirectAttributes.addFlashAttribute("error", "You can only delete your own courses!");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Course not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete course: " + e.getMessage());
        }
        return "redirect:/instructor/courses";
    }
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }
    
}
