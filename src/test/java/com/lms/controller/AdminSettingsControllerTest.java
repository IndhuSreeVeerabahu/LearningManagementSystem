package com.lms.controller;

import com.lms.entity.User;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminSettingsControllerTest {

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminSettingsController adminSettingsController;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminSettingsController).build();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setEmail("admin@example.com");
        testUser.setFirstName("Admin");
        testUser.setLastName("User");
    }

    @Test
    void systemSettings_ShouldReturnSettingsViewWithSystemData() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = adminSettingsController.systemSettings(model, authentication);

        // Then
        assertEquals("admin/settings", result);
        verify(model).addAttribute("currentUser", testUser);
        verify(model).addAttribute("title", "System Settings - LearnHub");
        verify(model).addAttribute("siteName", "LearnHub");
        verify(model).addAttribute("siteDescription", "Empowering learners worldwide with quality education");
        verify(model).addAttribute("maxFileSize", "10MB");
        verify(model).addAttribute("allowedFileTypes", "JPG, PNG, GIF, MP4, MP3, PDF");
        verify(model).addAttribute("emailNotifications", true);
        verify(model).addAttribute("autoApproveCourses", false);
        verify(model).addAttribute("maintenanceMode", false);
    }

    @Test
    void updateSettings_ShouldUpdateSettingsSuccessfully() {
        // Given
        String siteName = "New Site Name";
        String siteDescription = "New Description";
        String maxFileSize = "20MB";
        String allowedFileTypes = "JPG, PNG, PDF";
        Boolean emailNotifications = false;
        Boolean autoApproveCourses = true;
        Boolean maintenanceMode = true;

        // When
        String result = adminSettingsController.updateSettings(
            siteName, siteDescription, maxFileSize, allowedFileTypes,
            emailNotifications, autoApproveCourses, maintenanceMode,
            redirectAttributes
        );

        // Then
        assertEquals("redirect:/admin/settings", result);
        verify(redirectAttributes).addFlashAttribute("success", "System settings updated successfully!");
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
    }

    @Test
    void updateSettings_ShouldHandleException() {
        // Given
        // Simulate an exception by making redirectAttributes throw an exception
        doThrow(new RuntimeException("Test exception"))
            .when(redirectAttributes).addFlashAttribute(anyString(), anyString());

        // When
        String result = adminSettingsController.updateSettings(
            "siteName", "description", "10MB", "JPG", true, false, false,
            redirectAttributes
        );

        // Then
        assertEquals("redirect:/admin/settings", result);
        // The method should handle the exception gracefully
    }

    @Test
    void updateSettings_ShouldHandleNullParameters() {
        // When
        String result = adminSettingsController.updateSettings(
            null, null, null, null, null, null, null,
            redirectAttributes
        );

        // Then
        assertEquals("redirect:/admin/settings", result);
        verify(redirectAttributes).addFlashAttribute("success", "System settings updated successfully!");
    }

    @Test
    void createBackup_ShouldCreateBackupSuccessfully() throws Exception {
        // When
        String result = adminSettingsController.createBackup(redirectAttributes);

        // Then
        assertEquals("redirect:/admin/settings", result);
        verify(redirectAttributes).addFlashAttribute("success", "System backup created successfully!");
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
    }

    @Test
    void createBackup_ShouldHandleException() throws Exception {
        // Given
        // Simulate an exception during backup
        doThrow(new RuntimeException("Backup failed"))
            .when(redirectAttributes).addFlashAttribute(anyString(), anyString());

        // When
        String result = adminSettingsController.createBackup(redirectAttributes);

        // Then
        assertEquals("redirect:/admin/settings", result);
        // The method should handle the exception gracefully
    }

    @Test
    void toggleMaintenanceMode_ShouldEnableMaintenanceMode() {
        // Given
        Boolean maintenanceMode = true;

        // When
        String result = adminSettingsController.toggleMaintenanceMode(maintenanceMode, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/settings", result);
        verify(redirectAttributes).addFlashAttribute("success", 
            "Maintenance mode enabled. Site is now under maintenance.");
    }

    @Test
    void toggleMaintenanceMode_ShouldDisableMaintenanceMode() {
        // Given
        Boolean maintenanceMode = false;

        // When
        String result = adminSettingsController.toggleMaintenanceMode(maintenanceMode, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/settings", result);
        verify(redirectAttributes).addFlashAttribute("success", 
            "Maintenance mode disabled. Site is now accessible.");
    }

    @Test
    void toggleMaintenanceMode_ShouldHandleException() {
        // Given
        Boolean maintenanceMode = true;
        doThrow(new RuntimeException("Maintenance toggle failed"))
            .when(redirectAttributes).addFlashAttribute(anyString(), anyString());

        // When
        String result = adminSettingsController.toggleMaintenanceMode(maintenanceMode, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/settings", result);
        // The method should handle the exception gracefully
    }

    @Test
    void systemSettings_ShouldWorkWithMockMvc() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/settings"));
    }

    @Test
    void updateSettings_ShouldWorkWithMockMvc() throws Exception {
        // When & Then
        mockMvc.perform(post("/admin/settings/update")
                .param("siteName", "New Site")
                .param("siteDescription", "New Description")
                .param("maxFileSize", "20MB")
                .param("allowedFileTypes", "JPG, PNG")
                .param("emailNotifications", "true")
                .param("autoApproveCourses", "false")
                .param("maintenanceMode", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/settings"));
    }

    @Test
    void createBackup_ShouldWorkWithMockMvc() throws Exception {
        // When & Then
        mockMvc.perform(post("/admin/settings/backup"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/settings"));
    }

    @Test
    void toggleMaintenanceMode_ShouldWorkWithMockMvc() throws Exception {
        // When & Then
        mockMvc.perform(post("/admin/settings/maintenance")
                .param("maintenanceMode", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/settings"));
    }

    @Test
    void systemSettings_ShouldHandleNullAuthentication() {
        // When
        String result = adminSettingsController.systemSettings(model, null);

        // Then
        assertEquals("admin/settings", result);
        verify(model, never()).addAttribute(eq("currentUser"), any());
        verify(model).addAttribute("title", "System Settings - LearnHub");
    }

    @Test
    void systemSettings_ShouldHandleNonUserPrincipal() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not a user");
        SecurityContextHolder.setContext(securityContext);

        // When
        String result = adminSettingsController.systemSettings(model, authentication);

        // Then
        assertEquals("admin/settings", result);
        verify(model, never()).addAttribute(eq("currentUser"), any());
        verify(model).addAttribute("title", "System Settings - LearnHub");
    }
}
