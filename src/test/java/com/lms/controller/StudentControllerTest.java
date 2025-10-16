package com.lms.controller;

import com.lms.entity.*;
import com.lms.service.CertificateService;
import com.lms.service.CourseService;
import com.lms.service.EnrollmentService;
import com.lms.service.LessonService;
import com.lms.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
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
class StudentControllerTest {

    @Mock
    private CourseService courseService;

    @Mock
    private EnrollmentService enrollmentService;

    @Mock
    private LessonService lessonService;

    @Mock
    private ProgressService progressService;

    @Mock
    private CertificateService certificateService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private StudentController studentController;

    private MockMvc mockMvc;
    private User testStudent;
    private User testInstructor;
    private Course testCourse;
    private Lesson testLesson;
    private Enrollment testEnrollment;
    private Progress testProgress;
    private Certificate testCertificate;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studentController).build();
        
        testStudent = new User();
        testStudent.setId(1L);
        testStudent.setUsername("student");
        testStudent.setEmail("student@example.com");
        testStudent.setFirstName("Test");
        testStudent.setLastName("Student");
        testStudent.setRole(Role.STUDENT);
        testStudent.setIsActive(true);

        testInstructor = new User();
        testInstructor.setId(2L);
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
        testCourse.setIsPublished(true);
        testCourse.setIsApproved(true);

        testLesson = new Lesson();
        testLesson.setId(1L);
        testLesson.setTitle("Test Lesson");
        testLesson.setContent("Test Lesson Content");
        testLesson.setCourse(testCourse);

        testEnrollment = new Enrollment();
        testEnrollment.setId(1L);
        testEnrollment.setStudent(testStudent);
        testEnrollment.setCourse(testCourse);
        testEnrollment.setIsCompleted(false);
        testEnrollment.setCompletionPercentage(0.0);

        testProgress = new Progress();
        testProgress.setId(1L);
        testProgress.setStudent(testStudent);
        testProgress.setLesson(testLesson);
        testProgress.setIsCompleted(false);

        testCertificate = new Certificate();
        testCertificate.setId(1L);
        testCertificate.setStudent(testStudent);
        testCertificate.setCourse(testCourse);
    }

    @Test
    void dashboard_ShouldReturnDashboardViewWithStudentStatistics() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        List<Enrollment> enrollments = Arrays.asList(testEnrollment);
        when(enrollmentService.getEnrollmentsByStudent(testStudent)).thenReturn(enrollments);
        when(progressService.countCompletedLessonsByStudentAndCourse(testStudent, testCourse)).thenReturn(0L);

        // When
        String result = studentController.dashboard(model);

        // Then
        assertEquals("student/dashboard", result);
        verify(enrollmentService).getEnrollmentsByStudent(testStudent);
        verify(model).addAttribute("enrollments", enrollments);
        verify(model).addAttribute("currentUser", testStudent);
        verify(model).addAttribute("totalEnrolled", 1L);
        verify(model).addAttribute("completedCourses", 0L);
        verify(model).addAttribute("totalCompletedLessons", 0L);
    }

    @Test
    void browseCourses_ShouldReturnCoursesViewWithPublishedCourses() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(courses);

        // When
        String result = studentController.browseCourses(model, null);

        // Then
        assertEquals("student/courses", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(model).addAttribute("courses", courses);
        verify(model).addAttribute("currentUser", testStudent);
        verify(model, never()).addAttribute(eq("searchTerm"), any());
    }

    @Test
    void browseCourses_ShouldReturnCoursesViewWithSearchResults() {
        // Given
        String searchTerm = "test";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        List<Course> searchResults = Arrays.asList(testCourse);
        when(courseService.searchCourses(searchTerm)).thenReturn(searchResults);

        // When
        String result = studentController.browseCourses(model, searchTerm);

        // Then
        assertEquals("student/courses", result);
        verify(courseService).searchCourses(searchTerm);
        verify(courseService, never()).getPublishedAndApprovedCourses();
        verify(model).addAttribute("courses", searchResults);
        verify(model).addAttribute("searchTerm", searchTerm);
        verify(model).addAttribute("currentUser", testStudent);
    }

    @Test
    void courseDetails_ShouldReturnCourseDetailsViewWhenEnrolled() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        List<Lesson> lessons = Arrays.asList(testLesson);
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentService.isEnrolled(testStudent, testCourse)).thenReturn(true);
        when(lessonService.getPublishedLessonsByCourse(testCourse)).thenReturn(lessons);
        when(progressService.getCourseProgressPercentage(testStudent, testCourse)).thenReturn(50.0);

        // When
        String result = studentController.courseDetails(1L, model);

        // Then
        assertEquals("student/course-details", result);
        verify(courseService).getCourseById(1L);
        verify(enrollmentService).isEnrolled(testStudent, testCourse);
        verify(lessonService).getPublishedLessonsByCourse(testCourse);
        verify(progressService).getCourseProgressPercentage(testStudent, testCourse);
        verify(model).addAttribute("course", testCourse);
        verify(model).addAttribute("isEnrolled", true);
        verify(model).addAttribute("lessons", lessons);
        verify(model).addAttribute("progressPercentage", 50.0);
        verify(model).addAttribute("currentUser", testStudent);
    }

    @Test
    void courseDetails_ShouldReturnCourseDetailsViewWhenNotEnrolled() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentService.isEnrolled(testStudent, testCourse)).thenReturn(false);

        // When
        String result = studentController.courseDetails(1L, model);

        // Then
        assertEquals("student/course-details", result);
        verify(courseService).getCourseById(1L);
        verify(enrollmentService).isEnrolled(testStudent, testCourse);
        verify(lessonService, never()).getPublishedLessonsByCourse(any());
        verify(progressService, never()).getCourseProgressPercentage(any(), any());
        verify(model).addAttribute("course", testCourse);
        verify(model).addAttribute("isEnrolled", false);
        verify(model, never()).addAttribute(eq("lessons"), any());
        verify(model, never()).addAttribute(eq("progressPercentage"), any());
        verify(model).addAttribute("currentUser", testStudent);
    }

    @Test
    void courseDetails_ShouldRedirectWhenCourseNotFound() {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.empty());

        // When
        String result = studentController.courseDetails(1L, model);

        // Then
        assertEquals("redirect:/student/courses", result);
        verify(courseService).getCourseById(1L);
        verify(enrollmentService, never()).isEnrolled(any(), any());
    }

    @Test
    void enrollInCourse_ShouldEnrollStudentSuccessfully() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentService.isEnrolled(testStudent, testCourse)).thenReturn(false);

        // When
        String result = studentController.enrollInCourse(1L, redirectAttributes);

        // Then
        assertEquals("redirect:/student/courses/1", result);
        verify(courseService).getCourseById(1L);
        verify(enrollmentService).isEnrolled(testStudent, testCourse);
        verify(enrollmentService).enrollStudent(testStudent, testCourse);
        verify(redirectAttributes).addFlashAttribute("success", "Successfully enrolled in " + testCourse.getTitle() + "!");
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
    }

    @Test
    void enrollInCourse_ShouldReturnErrorWhenAlreadyEnrolled() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentService.isEnrolled(testStudent, testCourse)).thenReturn(true);

        // When
        String result = studentController.enrollInCourse(1L, redirectAttributes);

        // Then
        assertEquals("redirect:/student/courses/1", result);
        verify(courseService).getCourseById(1L);
        verify(enrollmentService).isEnrolled(testStudent, testCourse);
        verify(enrollmentService, never()).enrollStudent(any(), any());
        verify(redirectAttributes).addFlashAttribute("error", "You are already enrolled in this course");
        verify(redirectAttributes, never()).addFlashAttribute(eq("success"), any());
    }

    @Test
    void enrollInCourse_ShouldReturnErrorWhenCourseNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.empty());

        // When
        String result = studentController.enrollInCourse(1L, redirectAttributes);

        // Then
        assertEquals("redirect:/courses", result);
        verify(courseService).getCourseById(1L);
        verify(enrollmentService, never()).isEnrolled(any(), any());
        verify(enrollmentService, never()).enrollStudent(any(), any());
        verify(redirectAttributes).addFlashAttribute("error", "Course not found");
    }

    @Test
    void viewLesson_ShouldReturnLessonViewWhenEnrolled() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        List<Lesson> allLessons = Arrays.asList(testLesson);
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(testLesson));
        when(enrollmentService.isEnrolled(testStudent, testCourse)).thenReturn(true);
        when(progressService.getProgressByStudentAndLesson(testStudent, testLesson)).thenReturn(Optional.of(testProgress));
        when(lessonService.getPublishedLessonsByCourse(testCourse)).thenReturn(allLessons);

        // When
        String result = studentController.viewLesson(1L, 1L, model);

        // Then
        assertEquals("student/lesson-view", result);
        verify(courseService).getCourseById(1L);
        verify(lessonService).getLessonById(1L);
        verify(enrollmentService).isEnrolled(testStudent, testCourse);
        verify(progressService).getProgressByStudentAndLesson(testStudent, testLesson);
        verify(lessonService).getPublishedLessonsByCourse(testCourse);
        verify(model).addAttribute("course", testCourse);
        verify(model).addAttribute("lesson", testLesson);
        verify(model).addAttribute("allLessons", allLessons);
        verify(model).addAttribute("currentUser", testStudent);
        verify(model).addAttribute("isLessonCompleted", false);
    }

    @Test
    void viewLesson_ShouldRedirectWhenNotEnrolled() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(testLesson));
        when(enrollmentService.isEnrolled(testStudent, testCourse)).thenReturn(false);

        // When
        String result = studentController.viewLesson(1L, 1L, model);

        // Then
        assertEquals("redirect:/student/courses", result);
        verify(courseService).getCourseById(1L);
        verify(lessonService).getLessonById(1L);
        verify(enrollmentService).isEnrolled(testStudent, testCourse);
        verify(progressService, never()).getProgressByStudentAndLesson(any(), any());
    }

    @Test
    void markLessonComplete_ShouldMarkLessonAsCompleted() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(testLesson));
        when(enrollmentService.isEnrolled(testStudent, testCourse)).thenReturn(true);
        when(progressService.getProgressByStudentAndLesson(testStudent, testLesson)).thenReturn(Optional.empty());
        when(enrollmentService.getEnrollmentByStudentAndCourse(testStudent, testCourse)).thenReturn(Optional.of(testEnrollment));

        // When
        String result = studentController.markLessonComplete(1L, 1L, redirectAttributes);

        // Then
        assertEquals("redirect:/student/courses/1/lessons/1", result);
        verify(courseService).getCourseById(1L);
        verify(lessonService).getLessonById(1L);
        verify(enrollmentService).isEnrolled(testStudent, testCourse);
        verify(progressService).getProgressByStudentAndLesson(testStudent, testLesson);
        verify(progressService).markLessonAsCompleted(testStudent, testLesson);
        verify(enrollmentService).getEnrollmentByStudentAndCourse(testStudent, testCourse);
        verify(enrollmentService).updateEnrollmentProgress(testEnrollment);
        verify(redirectAttributes).addFlashAttribute("success", "Lesson marked as complete!");
    }

    @Test
    void markLessonComplete_ShouldReturnInfoWhenAlreadyCompleted() {
        // Given
        testProgress.setIsCompleted(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(testLesson));
        when(enrollmentService.isEnrolled(testStudent, testCourse)).thenReturn(true);
        when(progressService.getProgressByStudentAndLesson(testStudent, testLesson)).thenReturn(Optional.of(testProgress));

        // When
        String result = studentController.markLessonComplete(1L, 1L, redirectAttributes);

        // Then
        assertEquals("redirect:/student/courses/1/lessons/1", result);
        verify(progressService, never()).markLessonAsCompleted(any(), any());
        verify(enrollmentService, never()).updateEnrollmentProgress(any());
        verify(redirectAttributes).addFlashAttribute("info", "This lesson is already marked as complete!");
    }

    @Test
    void myCourses_ShouldReturnMyCoursesViewWithStatistics() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        List<Enrollment> enrollments = Arrays.asList(testEnrollment);
        when(enrollmentService.getEnrollmentsByStudent(testStudent)).thenReturn(enrollments);

        // When
        String result = studentController.myCourses(model);

        // Then
        assertEquals("student/my-courses", result);
        verify(enrollmentService).getEnrollmentsByStudent(testStudent);
        verify(model).addAttribute("enrolledCourses", enrollments);
        verify(model).addAttribute("currentUser", testStudent);
        verify(model).addAttribute("totalEnrolled", 1L);
        verify(model).addAttribute("completedCourses", 0L);
        verify(model).addAttribute("inProgressCourses", 1L);
    }

    @Test
    void downloadCertificate_ShouldReturnCertificateWhenCompleted() throws IOException {
        // Given
        byte[] certificateData = "certificate content".getBytes();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        testEnrollment.setIsCompleted(true);
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentService.isEnrolled(testStudent, testCourse)).thenReturn(true);
        when(enrollmentService.getEnrollmentByStudentAndCourse(testStudent, testCourse)).thenReturn(Optional.of(testEnrollment));
        when(certificateService.getCertificate(testStudent, testCourse)).thenReturn(Optional.of(testCertificate));
        when(certificateService.getCertificatePdf(testCertificate)).thenReturn(certificateData);

        // When
        ResponseEntity<byte[]> response = studentController.downloadCertificate(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.TEXT_HTML, response.getHeaders().getContentType());
        assertArrayEquals(certificateData, response.getBody());
        verify(courseService).getCourseById(1L);
        verify(enrollmentService).isEnrolled(testStudent, testCourse);
        verify(enrollmentService).getEnrollmentByStudentAndCourse(testStudent, testCourse);
        verify(certificateService).getCertificate(testStudent, testCourse);
        verify(certificateService).getCertificatePdf(testCertificate);
    }

    @Test
    void downloadCertificate_ShouldGenerateNewCertificateWhenNotExists() throws IOException {
        // Given
        byte[] certificateData = "certificate content".getBytes();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        testEnrollment.setIsCompleted(true);
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentService.isEnrolled(testStudent, testCourse)).thenReturn(true);
        when(enrollmentService.getEnrollmentByStudentAndCourse(testStudent, testCourse)).thenReturn(Optional.of(testEnrollment));
        when(certificateService.getCertificate(testStudent, testCourse)).thenReturn(Optional.empty());
        when(certificateService.generateCertificate(testStudent, testCourse)).thenReturn(testCertificate);
        when(certificateService.getCertificatePdf(testCertificate)).thenReturn(certificateData);

        // When
        ResponseEntity<byte[]> response = studentController.downloadCertificate(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.TEXT_HTML, response.getHeaders().getContentType());
        assertArrayEquals(certificateData, response.getBody());
        verify(certificateService).getCertificate(testStudent, testCourse);
        verify(certificateService).generateCertificate(testStudent, testCourse);
        verify(certificateService).getCertificatePdf(testCertificate);
    }

    @Test
    void downloadCertificate_ShouldReturnNotFoundWhenCourseNotFound() throws IOException {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<byte[]> response = studentController.downloadCertificate(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(courseService).getCourseById(1L);
        verify(enrollmentService, never()).isEnrolled(any(), any());
    }

    @Test
    void downloadCertificate_ShouldReturnBadRequestWhenNotEnrolled() throws IOException {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentService.isEnrolled(testStudent, testCourse)).thenReturn(false);

        // When
        ResponseEntity<byte[]> response = studentController.downloadCertificate(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(courseService).getCourseById(1L);
        verify(enrollmentService).isEnrolled(testStudent, testCourse);
        verify(enrollmentService, never()).getEnrollmentByStudentAndCourse(any(), any());
    }

    @Test
    void downloadCertificate_ShouldReturnBadRequestWhenCourseNotCompleted() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        testEnrollment.setIsCompleted(false);
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentService.isEnrolled(testStudent, testCourse)).thenReturn(true);
        when(enrollmentService.getEnrollmentByStudentAndCourse(testStudent, testCourse)).thenReturn(Optional.of(testEnrollment));

        // When
        ResponseEntity<byte[]> response = studentController.downloadCertificate(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(courseService).getCourseById(1L);
        verify(enrollmentService).isEnrolled(testStudent, testCourse);
        verify(enrollmentService).getEnrollmentByStudentAndCourse(testStudent, testCourse);
        verify(certificateService, never()).getCertificate(any(), any());
    }

    @Test
    void downloadCertificate_ShouldHandleException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testStudent);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<byte[]> response = studentController.downloadCertificate(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(courseService).getCourseById(1L);
    }

    @Test
    void dashboard_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(enrollmentService.getEnrollmentsByStudent(any())).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/dashboard"));
    }

    @Test
    void browseCourses_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/student/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/courses"));
    }

    @Test
    void courseDetails_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentService.isEnrolled(any(), any())).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/student/courses/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/course-details"));
    }

    @Test
    void enrollInCourse_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentService.isEnrolled(any(), any())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/student/courses/1/enroll"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/courses/1"));
    }

    @Test
    void viewLesson_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(testLesson));
        when(enrollmentService.isEnrolled(any(), any())).thenReturn(true);
        when(progressService.getProgressByStudentAndLesson(any(), any())).thenReturn(Optional.empty());
        when(lessonService.getPublishedLessonsByCourse(any())).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/student/courses/1/lessons/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/lesson-view"));
    }

    @Test
    void markLessonComplete_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(testLesson));
        when(enrollmentService.isEnrolled(any(), any())).thenReturn(true);
        when(progressService.getProgressByStudentAndLesson(any(), any())).thenReturn(Optional.empty());
        when(enrollmentService.getEnrollmentByStudentAndCourse(any(), any())).thenReturn(Optional.of(testEnrollment));

        // When & Then
        mockMvc.perform(post("/student/courses/1/lessons/1/complete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/courses/1/lessons/1"));
    }

    @Test
    void myCourses_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(enrollmentService.getEnrollmentsByStudent(any())).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/student/my-courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/my-courses"));
    }

    @Test
    void downloadCertificate_ShouldWorkWithMockMvc() throws Exception {
        // Given
        testEnrollment.setIsCompleted(true);
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentService.isEnrolled(any(), any())).thenReturn(true);
        when(enrollmentService.getEnrollmentByStudentAndCourse(any(), any())).thenReturn(Optional.of(testEnrollment));
        when(certificateService.getCertificate(any(), any())).thenReturn(Optional.of(testCertificate));
        when(certificateService.getCertificatePdf(any())).thenReturn("certificate content".getBytes());

        // When & Then
        mockMvc.perform(get("/student/courses/1/certificate"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE));
    }
}
