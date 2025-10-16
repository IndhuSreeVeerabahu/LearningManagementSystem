package com.lms.controller;

import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.service.UserService;
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
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ProfileController profileController;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");
        
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
                .setViewResolvers(viewResolver)
                .build();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.STUDENT);
        testUser.setIsActive(true);
    }

    @Test
    void profile_ShouldReturnProfileViewWithCurrentUser() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = profileController.profile(model);

        // Then
        assertEquals("profile", result);
        verify(model).addAttribute("user", testUser);
    }

    @Test
    void profile_ShouldRedirectToLoginWhenUserIsNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = profileController.profile(model);

        // Then
        assertEquals("redirect:/login", result);
        verify(model, never()).addAttribute(eq("user"), any());
    }

    @Test
    void profile_ShouldRedirectToLoginWhenAuthenticationIsNull() {
        // Given
        SecurityContextHolder.clearContext();

        // When
        String result = profileController.profile(model);

        // Then
        assertEquals("redirect:/login", result);
        verify(model, never()).addAttribute(eq("user"), any());
    }

    @Test
    void updateProfile_ShouldUpdateProfileSuccessfully() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        String newFirstName = "Updated";
        String newLastName = "Name";
        String newEmail = "updated@example.com";

        // When
        String result = profileController.updateProfile(newFirstName, newLastName, newEmail, redirectAttributes);

        // Then
        assertEquals("redirect:/profile", result);
        verify(userService).updateUser(testUser);
        verify(redirectAttributes).addFlashAttribute("success", "Profile updated successfully!");
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
        assertEquals(newFirstName, testUser.getFirstName());
        assertEquals(newLastName, testUser.getLastName());
        assertEquals(newEmail, testUser.getEmail());
    }

    @Test
    void updateProfile_ShouldHandleEmailAlreadyTaken() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        String newEmail = "taken@example.com";
        when(userService.existsByEmail(newEmail)).thenReturn(true);

        // When
        String result = profileController.updateProfile("Test", "User", newEmail, redirectAttributes);

        // Then
        assertEquals("redirect:/profile", result);
        verify(userService).existsByEmail(newEmail);
        verify(userService, never()).updateUser(any());
        verify(redirectAttributes).addFlashAttribute("error", "Email is already taken by another user");
        verify(redirectAttributes, never()).addFlashAttribute(eq("success"), any());
    }

    @Test
    void updateProfile_ShouldAllowSameEmail() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        String sameEmail = testUser.getEmail(); // Same as current email

        // When
        String result = profileController.updateProfile("Test", "User", sameEmail, redirectAttributes);

        // Then
        assertEquals("redirect:/profile", result);
        verify(userService, never()).existsByEmail(any());
        verify(userService).updateUser(testUser);
        verify(redirectAttributes).addFlashAttribute("success", "Profile updated successfully!");
    }

    @Test
    void updateProfile_ShouldHandleException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        when(userService.updateUser(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // When
        String result = profileController.updateProfile("Test", "User", "test@example.com", redirectAttributes);

        // Then
        assertEquals("redirect:/profile", result);
        verify(userService).updateUser(testUser);
        verify(redirectAttributes).addFlashAttribute("error", "Failed to update profile: Database error");
        verify(redirectAttributes, never()).addFlashAttribute(eq("success"), any());
    }

    @Test
    void updateProfile_ShouldRedirectToLoginWhenUserIsNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = profileController.updateProfile("Test", "User", "test@example.com", redirectAttributes);

        // Then
        assertEquals("redirect:/login", result);
        verify(userService, never()).updateUser(any());
        verify(redirectAttributes, never()).addFlashAttribute(anyString(), anyString());
    }

    @Test
    void updateProfile_ShouldRedirectToLoginWhenAuthenticationIsNull() {
        // Given
        SecurityContextHolder.clearContext();

        // When
        String result = profileController.updateProfile("Test", "User", "test@example.com", redirectAttributes);

        // Then
        assertEquals("redirect:/login", result);
        verify(userService, never()).updateUser(any());
        verify(redirectAttributes, never()).addFlashAttribute(anyString(), anyString());
    }

    @Test
    void profile_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);

        // When & Then
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"));
    }

    @Test
    void updateProfile_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        when(userService.existsByEmail("updated@example.com")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/profile")
                .param("firstName", "Updated")
                .param("lastName", "Name")
                .param("email", "updated@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void updateProfile_ShouldHandleEmptyParameters() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = profileController.updateProfile("", "", "", redirectAttributes);

        // Then
        assertEquals("redirect:/profile", result);
        verify(userService).updateUser(testUser);
        verify(redirectAttributes).addFlashAttribute("success", "Profile updated successfully!");
        assertEquals("", testUser.getFirstName());
        assertEquals("", testUser.getLastName());
        assertEquals("", testUser.getEmail());
    }

    @Test
    void updateProfile_ShouldHandleNullParameters() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = profileController.updateProfile(null, null, null, redirectAttributes);

        // Then
        assertEquals("redirect:/profile", result);
        verify(userService).updateUser(testUser);
        verify(redirectAttributes).addFlashAttribute("success", "Profile updated successfully!");
        assertNull(testUser.getFirstName());
        assertNull(testUser.getLastName());
        assertNull(testUser.getEmail());
    }

    @Test
    void updateProfile_ShouldHandleNonUserPrincipal() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not a user");
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = profileController.updateProfile("Test", "User", "test@example.com", redirectAttributes);

        // Then
        assertEquals("redirect:/login", result);
        verify(userService, never()).updateUser(any());
        verify(redirectAttributes, never()).addFlashAttribute(anyString(), anyString());
    }

    @Test
    void profile_ShouldHandleNonUserPrincipal() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not a user");
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = profileController.profile(model);

        // Then
        assertEquals("redirect:/login", result);
        verify(model, never()).addAttribute(eq("user"), any());
    }
}
