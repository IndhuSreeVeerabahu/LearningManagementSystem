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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SettingsControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private SettingsController settingsController;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(settingsController).build();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.STUDENT);
        testUser.setIsActive(true);
    }

    @Test
    void settings_ShouldReturnSettingsViewWithCurrentUser() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = settingsController.settings(model);

        // Then
        assertEquals("settings", result);
        verify(model).addAttribute("user", testUser);
    }

    @Test
    void settings_ShouldRedirectToLoginWhenUserIsNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = settingsController.settings(model);

        // Then
        assertEquals("redirect:/login", result);
        verify(model, never()).addAttribute(eq("user"), any());
    }

    @Test
    void settings_ShouldRedirectToLoginWhenAuthenticationIsNull() {
        // Given
        SecurityContextHolder.clearContext();

        // When
        String result = settingsController.settings(model);

        // Then
        assertEquals("redirect:/login", result);
        verify(model, never()).addAttribute(eq("user"), any());
    }

    @Test
    void changePassword_ShouldChangePasswordSuccessfully() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        String currentPassword = "currentPassword";
        String newPassword = "newPassword123";
        String confirmPassword = "newPassword123";
        
        when(passwordEncoder.matches("currentPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // When
        String result = settingsController.changePassword(currentPassword, newPassword, confirmPassword, redirectAttributes);

        // Then
        assertEquals("redirect:/settings", result);
        verify(passwordEncoder).matches("currentPassword", testUser.getPassword());
        verify(passwordEncoder).encode(newPassword);
        verify(userService).updateUser(testUser);
        verify(redirectAttributes).addFlashAttribute("success", "Password updated successfully!");
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
        assertEquals("encodedNewPassword", testUser.getPassword());
    }

    @Test
    void changePassword_ShouldReturnErrorWhenCurrentPasswordIsIncorrect() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword123";
        String confirmPassword = "newPassword123";
        
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(false);

        // When
        String result = settingsController.changePassword(currentPassword, newPassword, confirmPassword, redirectAttributes);

        // Then
        assertEquals("redirect:/settings", result);
        verify(passwordEncoder).matches(currentPassword, testUser.getPassword());
        verify(passwordEncoder, never()).encode(any());
        verify(userService, never()).updateUser(any());
        verify(redirectAttributes).addFlashAttribute("error", "Current password is incorrect");
        verify(redirectAttributes, never()).addFlashAttribute(eq("success"), any());
    }

    @Test
    void changePassword_ShouldReturnErrorWhenNewPasswordsDoNotMatch() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        String currentPassword = "currentPassword";
        String newPassword = "newPassword123";
        String confirmPassword = "differentPassword";
        
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);

        // When
        String result = settingsController.changePassword(currentPassword, newPassword, confirmPassword, redirectAttributes);

        // Then
        assertEquals("redirect:/settings", result);
        verify(passwordEncoder).matches(currentPassword, testUser.getPassword());
        verify(passwordEncoder, never()).encode(any());
        verify(userService, never()).updateUser(any());
        verify(redirectAttributes).addFlashAttribute("error", "New passwords do not match");
        verify(redirectAttributes, never()).addFlashAttribute(eq("success"), any());
    }

    @Test
    void changePassword_ShouldReturnErrorWhenNewPasswordIsTooShort() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        String currentPassword = "currentPassword";
        String newPassword = "12345"; // Less than 6 characters
        String confirmPassword = "12345";
        
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);

        // When
        String result = settingsController.changePassword(currentPassword, newPassword, confirmPassword, redirectAttributes);

        // Then
        assertEquals("redirect:/settings", result);
        verify(passwordEncoder).matches(currentPassword, testUser.getPassword());
        verify(passwordEncoder, never()).encode(any());
        verify(userService, never()).updateUser(any());
        verify(redirectAttributes).addFlashAttribute("error", "New password must be at least 6 characters long");
        verify(redirectAttributes, never()).addFlashAttribute(eq("success"), any());
    }

    @Test
    void changePassword_ShouldHandleException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        String currentPassword = "currentPassword";
        String newPassword = "newPassword123";
        String confirmPassword = "newPassword123";
        
        when(passwordEncoder.matches("currentPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userService.updateUser(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // When
        String result = settingsController.changePassword(currentPassword, newPassword, confirmPassword, redirectAttributes);

        // Then
        assertEquals("redirect:/settings", result);
        verify(passwordEncoder).matches("currentPassword", testUser.getPassword());
        verify(passwordEncoder).encode(newPassword);
        verify(userService).updateUser(testUser);
        verify(redirectAttributes).addFlashAttribute("error", "Failed to update password: Database error");
        verify(redirectAttributes, never()).addFlashAttribute(eq("success"), any());
    }

    @Test
    void changePassword_ShouldRedirectToLoginWhenUserIsNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = settingsController.changePassword("current", "new", "new", redirectAttributes);

        // Then
        assertEquals("redirect:/login", result);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(userService, never()).updateUser(any());
        verify(redirectAttributes, never()).addFlashAttribute(anyString(), anyString());
    }

    @Test
    void changePassword_ShouldRedirectToLoginWhenAuthenticationIsNull() {
        // Given
        SecurityContextHolder.clearContext();

        // When
        String result = settingsController.changePassword("current", "new", "new", redirectAttributes);

        // Then
        assertEquals("redirect:/login", result);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(userService, never()).updateUser(any());
        verify(redirectAttributes, never()).addFlashAttribute(anyString(), anyString());
    }

    @Test
    void settings_ShouldWorkWithMockMvc() throws Exception {
        // When & Then
        mockMvc.perform(get("/settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"));
    }

    @Test
    void changePassword_ShouldWorkWithMockMvc() throws Exception {
        // When & Then
        mockMvc.perform(post("/settings/password")
                .param("currentPassword", "current")
                .param("newPassword", "newPassword123")
                .param("confirmPassword", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings"));
    }

    @Test
    void changePassword_ShouldHandleEmptyPasswords() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        when(passwordEncoder.matches("", testUser.getPassword())).thenReturn(false);

        // When
        String result = settingsController.changePassword("", "", "", redirectAttributes);

        // Then
        assertEquals("redirect:/settings", result);
        verify(passwordEncoder).matches("", testUser.getPassword());
        verify(redirectAttributes).addFlashAttribute("error", "Current password is incorrect");
    }

    @Test
    void changePassword_ShouldHandleNullPasswords() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);
        
        when(passwordEncoder.matches(null, testUser.getPassword())).thenReturn(false);

        // When
        String result = settingsController.changePassword(null, null, null, redirectAttributes);

        // Then
        assertEquals("redirect:/settings", result);
        verify(passwordEncoder).matches(null, testUser.getPassword());
        verify(redirectAttributes).addFlashAttribute("error", "Current password is incorrect");
    }

    @Test
    void changePassword_ShouldHandleNonUserPrincipal() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not a user");
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = settingsController.changePassword("current", "new", "new", redirectAttributes);

        // Then
        assertEquals("redirect:/login", result);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(userService, never()).updateUser(any());
        verify(redirectAttributes, never()).addFlashAttribute(anyString(), anyString());
    }

    @Test
    void settings_ShouldHandleNonUserPrincipal() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not a user");
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = settingsController.settings(model);

        // Then
        assertEquals("redirect:/login", result);
        verify(model, never()).addAttribute(eq("user"), any());
    }
}
