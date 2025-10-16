package com.lms.controller;

import com.lms.entity.Course;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CourseController courseController;

    private MockMvc mockMvc;
    private User testUser;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(courseController).build();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.STUDENT);
        testUser.setIsActive(true);

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setTitle("Test Course");
        testCourse.setDescription("Test Description");
        testCourse.setInstructor(testUser);
        testCourse.setIsPublished(true);
        testCourse.setIsApproved(true);
    }

    @Test
    void browseCourses_ShouldReturnBrowseViewWithPublishedCourses() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(Arrays.asList(testCourse));

        // When
        String result = courseController.browseCourses(model, null);

        // Then
        assertEquals("courses/browse", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(model).addAttribute("courses", Arrays.asList(testCourse));
        verify(model).addAttribute("currentUser", testUser);
        verify(model, never()).addAttribute(eq("searchTerm"), any());
    }

    @Test
    void browseCourses_ShouldReturnBrowseViewWithSearchResults() {
        // Given
        String searchTerm = "test";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.searchCourses(searchTerm)).thenReturn(Arrays.asList(testCourse));

        // When
        String result = courseController.browseCourses(model, searchTerm);

        // Then
        assertEquals("courses/browse", result);
        verify(courseService).searchCourses(searchTerm);
        verify(courseService, never()).getPublishedAndApprovedCourses();
        verify(model).addAttribute("courses", Arrays.asList(testCourse));
        verify(model).addAttribute("searchTerm", searchTerm);
        verify(model).addAttribute("currentUser", testUser);
    }

    @Test
    void browseCourses_ShouldHandleEmptySearchTerm() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(Arrays.asList(testCourse));

        // When
        String result = courseController.browseCourses(model, "");

        // Then
        assertEquals("courses/browse", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(courseService, never()).searchCourses(any());
    }

    @Test
    void browseCourses_ShouldHandleNullAuthentication() {
        // Given
        SecurityContextHolder.clearContext();
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(Arrays.asList(testCourse));

        // When
        String result = courseController.browseCourses(model, null);

        // Then
        assertEquals("courses/browse", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(model).addAttribute("courses", Arrays.asList(testCourse));
        verify(model, never()).addAttribute(eq("currentUser"), any());
    }

    @Test
    void browseCourses_ShouldHandleNonUserPrincipal() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not a user");
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(Arrays.asList(testCourse));

        // When
        String result = courseController.browseCourses(model, null);

        // Then
        assertEquals("courses/browse", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(model).addAttribute("courses", Arrays.asList(testCourse));
        verify(model, never()).addAttribute(eq("currentUser"), any());
    }

    @Test
    void browseCourses_ShouldHandleEmptyPublishedCourses() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(Collections.emptyList());
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(testCourse));

        // When
        String result = courseController.browseCourses(model, null);

        // Then
        assertEquals("courses/browse", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(courseService).getAllCourses(); // For debugging when no published courses
        verify(model).addAttribute("courses", Collections.emptyList());
    }

    @Test
    void browseCourses_ShouldHandleException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getPublishedAndApprovedCourses()).thenThrow(new RuntimeException("Database error"));

        // When
        String result = courseController.browseCourses(model, null);

        // Then
        assertEquals("courses/browse", result);
        verify(model).addAttribute("courses", Collections.emptyList());
    }

    @Test
    void courseDetails_ShouldReturnDetailsViewWhenCourseExists() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When
        String result = courseController.courseDetails(1L, model);

        // Then
        assertEquals("courses/details", result);
        verify(courseService).getCourseById(1L);
        verify(model).addAttribute("course", testCourse);
        verify(model).addAttribute("currentUser", testUser);
    }

    @Test
    void courseDetails_ShouldRedirectWhenCourseNotFound() {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.empty());

        // When
        String result = courseController.courseDetails(1L, model);

        // Then
        assertEquals("redirect:/courses", result);
        verify(courseService).getCourseById(1L);
        verify(model, never()).addAttribute(eq("course"), any());
    }

    @Test
    void courseDetails_ShouldHandleNullAuthentication() {
        // Given
        SecurityContextHolder.clearContext();
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When
        String result = courseController.courseDetails(1L, model);

        // Then
        assertEquals("courses/details", result);
        verify(courseService).getCourseById(1L);
        verify(model).addAttribute("course", testCourse);
        verify(model, never()).addAttribute(eq("currentUser"), any());
    }

    @Test
    void courseDetails_ShouldHandleNonUserPrincipal() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not a user");
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When
        String result = courseController.courseDetails(1L, model);

        // Then
        assertEquals("courses/details", result);
        verify(courseService).getCourseById(1L);
        verify(model).addAttribute("course", testCourse);
        verify(model, never()).addAttribute(eq("currentUser"), any());
    }

    @Test
    void browseCourses_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/browse"));
    }

    @Test
    void browseCourses_ShouldWorkWithMockMvcWithSearch() throws Exception {
        // Given
        when(courseService.searchCourses("test")).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/courses").param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/browse"));
    }

    @Test
    void courseDetails_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // When & Then
        mockMvc.perform(get("/courses/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/details"));
    }

    @Test
    void courseDetails_ShouldRedirectWithMockMvcWhenCourseNotFound() throws Exception {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/courses/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));
    }

    @Test
    void browseCourses_ShouldHandleWhitespaceSearchTerm() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(Arrays.asList(testCourse));

        // When
        String result = courseController.browseCourses(model, "   ");

        // Then
        assertEquals("courses/browse", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(courseService, never()).searchCourses(any());
    }
}
