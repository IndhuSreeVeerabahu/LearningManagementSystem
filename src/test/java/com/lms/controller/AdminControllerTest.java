package com.lms.controller;

import com.lms.entity.Course;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.service.CourseService;
import com.lms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private CourseService courseService;

    @Mock
    private Model model;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private User testUser;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setEmail("admin@example.com");
        testUser.setFirstName("Admin");
        testUser.setLastName("User");
        testUser.setRole(Role.ADMIN);
        testUser.setIsActive(true);

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setTitle("Test Course");
        testCourse.setDescription("Test Description");
        testCourse.setInstructor(testUser);
        testCourse.setIsPublished(false);
        testCourse.setIsApproved(false);
    }

    @Test
    void dashboard_ShouldReturnDashboardViewWithStatistics() {
        // Given
        List<User> allUsers = Arrays.asList(testUser);
        List<Course> allCourses = Arrays.asList(testCourse);
        List<Course> pendingCourses = Arrays.asList(testCourse);

        when(userService.getAllUsers()).thenReturn(allUsers);
        when(courseService.getAllCourses()).thenReturn(allCourses);
        when(courseService.getPendingApprovalCourses()).thenReturn(pendingCourses);

        // When
        String result = adminController.dashboard(model);

        // Then
        assertEquals("admin/dashboard", result);
        verify(userService).getAllUsers();
        verify(courseService).getAllCourses();
        verify(courseService).getPendingApprovalCourses();
        verify(model).addAttribute("totalUsers", 1);
        verify(model).addAttribute("totalCourses", 1);
        verify(model).addAttribute("pendingCourses", 1);
        verify(model).addAttribute("pendingCoursesList", pendingCourses);
    }

    @Test
    void dashboard_ShouldHandleEmptyData() {
        // Given
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());
        when(courseService.getAllCourses()).thenReturn(Collections.emptyList());
        when(courseService.getPendingApprovalCourses()).thenReturn(Collections.emptyList());

        // When
        String result = adminController.dashboard(model);

        // Then
        assertEquals("admin/dashboard", result);
        verify(model).addAttribute("totalUsers", 0);
        verify(model).addAttribute("totalCourses", 0);
        verify(model).addAttribute("pendingCourses", 0);
        verify(model).addAttribute("pendingCoursesList", Collections.emptyList());
    }

    @Test
    void users_ShouldReturnUsersViewWithAllUsers() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        // When
        String result = adminController.users(model, null);

        // Then
        assertEquals("admin/users", result);
        verify(userService).getAllUsers();
        verify(model).addAttribute("users", users);
        verify(model, never()).addAttribute(eq("searchTerm"), any());
    }

    @Test
    void users_ShouldReturnUsersViewWithSearchResults() {
        // Given
        String searchTerm = "admin";
        List<User> searchResults = Arrays.asList(testUser);
        when(userService.searchUsers(searchTerm)).thenReturn(searchResults);

        // When
        String result = adminController.users(model, searchTerm);

        // Then
        assertEquals("admin/users", result);
        verify(userService).searchUsers(searchTerm);
        verify(userService, never()).getAllUsers();
        verify(model).addAttribute("users", searchResults);
        verify(model).addAttribute("searchTerm", searchTerm);
    }

    @Test
    void users_ShouldHandleEmptySearchTerm() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        // When
        String result = adminController.users(model, "");

        // Then
        assertEquals("admin/users", result);
        verify(userService).getAllUsers();
        verify(userService, never()).searchUsers(any());
    }

    @Test
    void courses_ShouldReturnCoursesViewWithAllCourses() {
        // Given
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.getAllCourses()).thenReturn(courses);

        // When
        String result = adminController.courses(model, null);

        // Then
        assertEquals("admin/courses", result);
        verify(courseService).getAllCourses();
        verify(model).addAttribute("courses", courses);
        verify(model, never()).addAttribute(eq("searchTerm"), any());
    }

    @Test
    void courses_ShouldReturnCoursesViewWithSearchResults() {
        // Given
        String searchTerm = "test";
        List<Course> searchResults = Arrays.asList(testCourse);
        when(courseService.searchCourses(searchTerm)).thenReturn(searchResults);

        // When
        String result = adminController.courses(model, searchTerm);

        // Then
        assertEquals("admin/courses", result);
        verify(courseService).searchCourses(searchTerm);
        verify(courseService, never()).getAllCourses();
        verify(model).addAttribute("courses", searchResults);
        verify(model).addAttribute("searchTerm", searchTerm);
    }

    @Test
    void toggleUserStatus_ShouldActivateInactiveUser() {
        // Given
        testUser.setIsActive(false);
        when(userService.getUserById(1L)).thenReturn(testUser);

        // When
        String result = adminController.toggleUserStatus(1L);

        // Then
        assertEquals("redirect:/admin/users", result);
        verify(userService).getUserById(1L);
        verify(userService).activateUser(1L);
        verify(userService, never()).deactivateUser(any());
    }

    @Test
    void toggleUserStatus_ShouldDeactivateActiveUser() {
        // Given
        testUser.setIsActive(true);
        when(userService.getUserById(1L)).thenReturn(testUser);

        // When
        String result = adminController.toggleUserStatus(1L);

        // Then
        assertEquals("redirect:/admin/users", result);
        verify(userService).getUserById(1L);
        verify(userService).deactivateUser(1L);
        verify(userService, never()).activateUser(any());
    }

    @Test
    void toggleUserStatus_ShouldHandleNullUser() {
        // Given
        when(userService.getUserById(1L)).thenReturn(null);

        // When
        String result = adminController.toggleUserStatus(1L);

        // Then
        assertEquals("redirect:/admin/users", result);
        verify(userService).getUserById(1L);
        verify(userService, never()).activateUser(any());
        verify(userService, never()).deactivateUser(any());
    }

    @Test
    void changeUserRole_ShouldUpdateUserRole() {
        // Given
        Role newRole = Role.INSTRUCTOR;
        when(userService.getUserById(1L)).thenReturn(testUser);

        // When
        String result = adminController.changeUserRole(1L, newRole);

        // Then
        assertEquals("redirect:/admin/users", result);
        verify(userService).getUserById(1L);
        verify(userService).updateUser(testUser);
        assertEquals(newRole, testUser.getRole());
    }

    @Test
    void changeUserRole_ShouldHandleNullUser() {
        // Given
        Role newRole = Role.INSTRUCTOR;
        when(userService.getUserById(1L)).thenReturn(null);

        // When
        String result = adminController.changeUserRole(1L, newRole);

        // Then
        assertEquals("redirect:/admin/users", result);
        verify(userService).getUserById(1L);
        verify(userService, never()).updateUser(any());
    }

    @Test
    void approveCourse_ShouldApproveCourse() {
        // When
        String result = adminController.approveCourse(1L);

        // Then
        assertEquals("redirect:/admin/courses", result);
        verify(courseService).approveCourse(1L);
    }

    @Test
    void rejectCourse_ShouldRejectCourse() {
        // When
        String result = adminController.rejectCourse(1L);

        // Then
        assertEquals("redirect:/admin/courses", result);
        verify(courseService).rejectCourse(1L);
    }

    @Test
    void deleteCourse_ShouldDeleteCourse() {
        // When
        String result = adminController.deleteCourse(1L);

        // Then
        assertEquals("redirect:/admin/courses", result);
        verify(courseService).deleteCourse(1L);
    }

    @Test
    void dashboard_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());
        when(courseService.getAllCourses()).thenReturn(Collections.emptyList());
        when(courseService.getPendingApprovalCourses()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    void users_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"));
    }

    @Test
    void courses_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(courseService.getAllCourses()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/admin/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/courses"));
    }

    @Test
    void toggleUserStatus_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/admin/users/1/toggle-status"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    void changeUserRole_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/admin/users/1/change-role")
                .param("newRole", "INSTRUCTOR"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    void approveCourse_ShouldWorkWithMockMvc() throws Exception {
        // When & Then
        mockMvc.perform(post("/admin/courses/1/approve"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/courses"));
    }

    @Test
    void rejectCourse_ShouldWorkWithMockMvc() throws Exception {
        // When & Then
        mockMvc.perform(post("/admin/courses/1/reject"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/courses"));
    }

    @Test
    void deleteCourse_ShouldWorkWithMockMvc() throws Exception {
        // When & Then
        mockMvc.perform(post("/admin/courses/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/courses"));
    }
}
