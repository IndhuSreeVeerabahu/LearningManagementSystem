package com.lms.controller;

import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.service.CourseService;
import com.lms.service.LessonService;
import com.lms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InstructorControllerTest {

    @Mock
    private CourseService courseService;

    @Mock
    private LessonService lessonService;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private InstructorController instructorController;

    private MockMvc mockMvc;
    private User testInstructor;
    private Course testCourse;
    private Lesson testLesson;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(instructorController).build();
        
        testInstructor = new User();
        testInstructor.setId(1L);
        testInstructor.setUsername("instructor");
        testInstructor.setEmail("instructor@example.com");
        testInstructor.setFirstName("Test");
        testInstructor.setLastName("Instructor");
        testInstructor.setRole(Role.INSTRUCTOR);
        testInstructor.setIsActive(true);

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setTitle("Test Course");
        testCourse.setDescription("Test Description");
        testCourse.setInstructor(testInstructor);
        testCourse.setIsPublished(false);
        testCourse.setIsApproved(false);

        testLesson = new Lesson();
        testLesson.setId(1L);
        testLesson.setTitle("Test Lesson");
        testLesson.setContent("Test Lesson Content");
        testLesson.setCourse(testCourse);
    }

    @Test
    void dashboard_ShouldReturnDashboardViewWithInstructorCourses() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.getCoursesByInstructor(testInstructor)).thenReturn(courses);

        // When
        String result = instructorController.dashboard(model);

        // Then
        assertEquals("instructor/dashboard", result);
        verify(courseService).getCoursesByInstructor(testInstructor);
        verify(model).addAttribute("courses", courses);
        verify(model).addAttribute("currentUser", testInstructor);
    }

    @Test
    void dashboard_ShouldRedirectToLoginWhenUserIsNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = instructorController.dashboard(model);

        // Then
        assertEquals("redirect:/login", result);
        verify(courseService, never()).getCoursesByInstructor(any());
    }

    @Test
    void dashboard_ShouldHandleException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCoursesByInstructor(testInstructor)).thenThrow(new RuntimeException("Database error"));

        // When
        String result = instructorController.dashboard(model);

        // Then
        assertEquals("redirect:/login", result);
    }

    @Test
    void courses_ShouldReturnCoursesViewWithInstructorCourses() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.getCoursesByInstructor(testInstructor)).thenReturn(courses);

        // When
        String result = instructorController.courses(model);

        // Then
        assertEquals("instructor/courses", result);
        verify(courseService).getCoursesByInstructor(testInstructor);
        verify(model).addAttribute("courses", courses);
    }

    @Test
    void newCourse_ShouldReturnCourseFormView() {
        // When
        String result = instructorController.newCourse(model);

        // Then
        assertEquals("instructor/course-form", result);
        verify(model).addAttribute(eq("course"), any(Course.class));
    }

    @Test
    void createCourse_ShouldCreateCourseSuccessfully() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.saveCourse(any(Course.class))).thenReturn(testCourse);

        // When
        String result = instructorController.createCourse(testCourse);

        // Then
        assertEquals("redirect:/instructor/courses", result);
        verify(courseService).saveCourse(testCourse);
        assertEquals(testInstructor, testCourse.getInstructor());
    }

    @Test
    void createCourse_ShouldHandleFileUpload() throws IOException {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        MockMultipartFile file = new MockMultipartFile(
            "courseImageFile", 
            "test.jpg", 
            "image/jpeg", 
            "test content".getBytes()
        );
        testCourse.setCourseImageFile(file);
        
        when(courseService.saveCourse(any(Course.class))).thenReturn(testCourse);

        // When
        String result = instructorController.createCourse(testCourse);

        // Then
        assertEquals("redirect:/instructor/courses", result);
        verify(courseService).saveCourse(testCourse);
    }

    @Test
    void createCourse_ShouldHandleFileUploadError() throws IOException {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        MockMultipartFile file = new MockMultipartFile(
            "courseImageFile", 
            "test.jpg", 
            "image/jpeg", 
            "test content".getBytes()
        );
        testCourse.setCourseImageFile(file);
        
        when(courseService.saveCourse(any(Course.class))).thenThrow(new RuntimeException("Database error"));

        // When
        String result = instructorController.createCourse(testCourse);

        // Then
        assertEquals("redirect:/instructor/courses/new", result);
    }

    @Test
    void editCourse_ShouldReturnCourseFormViewWhenCourseExists() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When
        String result = instructorController.editCourse(1L, model);

        // Then
        assertEquals("instructor/course-form", result);
        verify(courseService).getCourseById(1L);
        verify(model).addAttribute("course", testCourse);
    }

    @Test
    void editCourse_ShouldRedirectWhenCourseNotFound() {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.empty());

        // When
        String result = instructorController.editCourse(1L, model);

        // Then
        assertEquals("redirect:/instructor/courses", result);
        verify(courseService).getCourseById(1L);
        verify(model, never()).addAttribute(eq("course"), any());
    }

    @Test
    void editCourse_ShouldRedirectWhenNotInstructorCourse() {
        // Given
        User otherInstructor = new User();
        otherInstructor.setId(2L);
        testCourse.setInstructor(otherInstructor);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When
        String result = instructorController.editCourse(1L, model);

        // Then
        assertEquals("redirect:/instructor/courses", result);
        verify(courseService).getCourseById(1L);
        verify(model, never()).addAttribute(eq("course"), any());
    }

    @Test
    void updateCourse_ShouldUpdateCourseSuccessfully() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        Course updatedCourse = new Course();
        updatedCourse.setTitle("Updated Title");
        updatedCourse.setDescription("Updated Description");
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(courseService.updateCourse(any(Course.class))).thenReturn(testCourse);

        // When
        String result = instructorController.updateCourse(1L, updatedCourse);

        // Then
        assertEquals("redirect:/instructor/courses", result);
        verify(courseService).getCourseById(1L);
        verify(courseService).updateCourse(testCourse);
        assertEquals("Updated Title", testCourse.getTitle());
        assertEquals("Updated Description", testCourse.getDescription());
    }

    @Test
    void updateCourse_ShouldRedirectWhenCourseNotFound() {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.empty());

        // When
        String result = instructorController.updateCourse(1L, testCourse);

        // Then
        assertEquals("redirect:/instructor/courses", result);
        verify(courseService).getCourseById(1L);
        verify(courseService, never()).updateCourse(any());
    }

    @Test
    void courseLessons_ShouldReturnLessonsView() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        List<Lesson> lessons = Arrays.asList(testLesson);
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonService.getLessonsByCourse(testCourse)).thenReturn(lessons);

        // When
        String result = instructorController.courseLessons(1L, model);

        // Then
        assertEquals("instructor/lessons", result);
        verify(courseService).getCourseById(1L);
        verify(lessonService).getLessonsByCourse(testCourse);
        verify(model).addAttribute("course", testCourse);
        verify(model).addAttribute("lessons", lessons);
    }

    @Test
    void newLesson_ShouldReturnLessonFormView() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When
        String result = instructorController.newLesson(1L, model);

        // Then
        assertEquals("instructor/lesson-form", result);
        verify(courseService).getCourseById(1L);
        verify(model).addAttribute(eq("lesson"), any(Lesson.class));
        verify(model).addAttribute("course", testCourse);
    }

    @Test
    void createLesson_ShouldCreateLessonSuccessfully() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonService.saveLesson(any(Lesson.class))).thenReturn(testLesson);

        // When
        String result = instructorController.createLesson(1L, testLesson, null, null, null, null);

        // Then
        assertEquals("redirect:/instructor/courses/1/lessons", result);
        verify(courseService).getCourseById(1L);
        verify(lessonService).saveLesson(testLesson);
        assertEquals(testCourse, testLesson.getCourse());
    }

    @Test
    void createLesson_ShouldHandleFileUploads() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        MockMultipartFile videoFile = new MockMultipartFile("videoFile", "test.mp4", "video/mp4", "video content".getBytes());
        MockMultipartFile pdfFile = new MockMultipartFile("pdfFile", "test.pdf", "application/pdf", "pdf content".getBytes());
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "image content".getBytes());
        MockMultipartFile audioFile = new MockMultipartFile("audioFile", "test.mp3", "audio/mpeg", "audio content".getBytes());
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonService.saveLesson(any(Lesson.class))).thenReturn(testLesson);

        // When
        String result = instructorController.createLesson(1L, testLesson, videoFile, pdfFile, imageFile, audioFile);

        // Then
        assertEquals("redirect:/instructor/courses/1/lessons", result);
        verify(courseService).getCourseById(1L);
        verify(lessonService).saveLesson(testLesson);
    }

    @Test
    void publishCourse_ShouldPublishCourse() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When
        String result = instructorController.publishCourse(1L);

        // Then
        assertEquals("redirect:/instructor/courses", result);
        verify(courseService).getCourseById(1L);
        verify(courseService).publishCourse(1L);
    }

    @Test
    void publishCourse_ShouldNotPublishWhenCourseNotFound() {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.empty());

        // When
        String result = instructorController.publishCourse(1L);

        // Then
        assertEquals("redirect:/instructor/courses", result);
        verify(courseService).getCourseById(1L);
        verify(courseService, never()).publishCourse(any());
    }

    @Test
    void publishCourse_ShouldNotPublishWhenNotInstructorCourse() {
        // Given
        User otherInstructor = new User();
        otherInstructor.setId(2L);
        testCourse.setInstructor(otherInstructor);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When
        String result = instructorController.publishCourse(1L);

        // Then
        assertEquals("redirect:/instructor/courses", result);
        verify(courseService).getCourseById(1L);
        verify(courseService, never()).publishCourse(any());
    }

    @Test
    void dashboard_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        when(courseService.getCoursesByInstructor(any())).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/instructor/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/dashboard"));
    }

    @Test
    void courses_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        when(courseService.getCoursesByInstructor(any())).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/instructor/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/courses"));
    }

    @Test
    void newCourse_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);

        // When & Then
        mockMvc.perform(get("/instructor/courses/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/course-form"));
    }

    @Test
    void createCourse_ShouldWorkWithMockMvc() throws Exception {
        // When & Then
        mockMvc.perform(post("/instructor/courses")
                .param("title", "Test Course")
                .param("description", "Test Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/instructor/courses"));
    }

    @Test
    void editCourse_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When & Then
        mockMvc.perform(get("/instructor/courses/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/course-form"));
    }

    @Test
    void updateCourse_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When & Then
        mockMvc.perform(post("/instructor/courses/1")
                .param("title", "Updated Course")
                .param("description", "Updated Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/instructor/courses"));
    }

    @Test
    void courseLessons_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonService.getLessonsByCourse(testCourse)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/instructor/courses/1/lessons"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/lessons"));
    }

    @Test
    void newLesson_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testInstructor);
        SecurityContextHolder.setContext(securityContext);
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When & Then
        mockMvc.perform(get("/instructor/courses/1/lessons/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/lesson-form"));
    }

    @Test
    void createLesson_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When & Then
        mockMvc.perform(post("/instructor/courses/1/lessons")
                .param("title", "Test Lesson")
                .param("content", "Test Content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/instructor/courses/1/lessons"));
    }

    @Test
    void publishCourse_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When & Then
        mockMvc.perform(post("/instructor/courses/1/publish"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/instructor/courses"));
    }
}
