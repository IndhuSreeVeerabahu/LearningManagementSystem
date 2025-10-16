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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private CourseService courseService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private HomeController homeController;

    private MockMvc mockMvc;
    private User testUser;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
        
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
    void home_ShouldReturnIndexViewWithPublishedCourses() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(courses);

        // When
        String result = homeController.home(model, null, null);

        // Then
        assertEquals("index", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(model).addAttribute("courses", courses);
        verify(model).addAttribute("currentUser", testUser);
        verify(model, never()).addAttribute(eq("searchTerm"), any());
        verify(model, never()).addAttribute(eq("success"), any());
    }

    @Test
    void home_ShouldReturnIndexViewWithSearchResults() {
        // Given
        String searchTerm = "test";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        List<Course> searchResults = Arrays.asList(testCourse);
        when(courseService.searchCourses(searchTerm)).thenReturn(searchResults);

        // When
        String result = homeController.home(model, searchTerm, null);

        // Then
        assertEquals("index", result);
        verify(courseService).searchCourses(searchTerm);
        verify(courseService, never()).getPublishedAndApprovedCourses();
        verify(model).addAttribute("courses", searchResults);
        verify(model).addAttribute("searchTerm", searchTerm);
        verify(model).addAttribute("currentUser", testUser);
    }

    @Test
    void home_ShouldHandleLogoutMessage() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(courses);

        // When
        String result = homeController.home(model, null, "true");

        // Then
        assertEquals("index", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(model).addAttribute("courses", courses);
        verify(model).addAttribute("currentUser", testUser);
        verify(model).addAttribute("success", "You have been logged out successfully.");
    }

    @Test
    void home_ShouldHandleEmptySearchTerm() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(courses);

        // When
        String result = homeController.home(model, "", null);

        // Then
        assertEquals("index", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(courseService, never()).searchCourses(any());
        verify(model).addAttribute("courses", courses);
    }

    @Test
    void home_ShouldHandleNullAuthentication() {
        // Given
        SecurityContextHolder.clearContext();
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(courses);

        // When
        String result = homeController.home(model, null, null);

        // Then
        assertEquals("index", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(model).addAttribute("courses", courses);
        verify(model, never()).addAttribute(eq("currentUser"), any());
    }

    @Test
    void home_ShouldHandleNonUserPrincipal() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not a user");
        SecurityContextHolder.setContext(securityContext);
        
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(courses);

        // When
        String result = homeController.home(model, null, null);

        // Then
        assertEquals("index", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(model).addAttribute("courses", courses);
        verify(model, never()).addAttribute(eq("currentUser"), any());
    }

    @Test
    void home_ShouldHandleEmptyCoursesList() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(Collections.emptyList());

        // When
        String result = homeController.home(model, null, null);

        // Then
        assertEquals("index", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(model).addAttribute("courses", Collections.emptyList());
        verify(model).addAttribute("currentUser", testUser);
    }

    @Test
    void home_ShouldHandleWhitespaceSearchTerm() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(courses);

        // When
        String result = homeController.home(model, "   ", null);

        // Then
        assertEquals("index", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(courseService, never()).searchCourses(any());
        verify(model).addAttribute("courses", courses);
    }

    @Test
    void home_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void home_ShouldWorkWithMockMvcWithSearch() throws Exception {
        // Given
        when(courseService.searchCourses("test")).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/").param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void home_ShouldWorkWithMockMvcWithLogout() throws Exception {
        // Given
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void home_ShouldHandleMultipleParameters() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        List<Course> searchResults = Arrays.asList(testCourse);
        when(courseService.searchCourses("test")).thenReturn(searchResults);

        // When
        String result = homeController.home(model, "test", "true");

        // Then
        assertEquals("index", result);
        verify(courseService).searchCourses("test");
        verify(model).addAttribute("courses", searchResults);
        verify(model).addAttribute("searchTerm", "test");
        verify(model).addAttribute("currentUser", testUser);
        verify(model).addAttribute("success", "You have been logged out successfully.");
    }

    @Test
    void home_ShouldHandleNullParameters() {
        // Given
        SecurityContextHolder.clearContext();
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.getPublishedAndApprovedCourses()).thenReturn(courses);

        // When
        String result = homeController.home(model, null, null);

        // Then
        assertEquals("index", result);
        verify(courseService).getPublishedAndApprovedCourses();
        verify(model).addAttribute("courses", courses);
        verify(model, never()).addAttribute(eq("searchTerm"), any());
        verify(model, never()).addAttribute(eq("success"), any());
        verify(model, never()).addAttribute(eq("currentUser"), any());
    }
}
