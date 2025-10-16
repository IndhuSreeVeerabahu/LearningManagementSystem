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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
class WebAuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private WebAuthController webAuthController;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(webAuthController).build();
        
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
    void register_ShouldRegisterStudentSuccessfully() {
        // Given
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "password";
        String firstName = "New";
        String lastName = "User";
        String role = "STUDENT";
        
        when(userService.existsByUsername(username)).thenReturn(false);
        when(userService.existsByEmail(email)).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // When
        String result = webAuthController.register(username, email, password, firstName, lastName, role, redirectAttributes);

        // Then
        assertEquals("redirect:/student/dashboard", result);
        verify(userService).existsByUsername(username);
        verify(userService).existsByEmail(email);
        verify(userService).saveUser(any(User.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(redirectAttributes).addFlashAttribute("success", "Registration successful! Welcome to LearnHub!");
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
    }

    @Test
    void register_ShouldRegisterInstructorSuccessfully() {
        // Given
        testUser.setRole(Role.INSTRUCTOR);
        String username = "newinstructor";
        String email = "newinstructor@example.com";
        String password = "password";
        String firstName = "New";
        String lastName = "Instructor";
        String role = "INSTRUCTOR";
        
        when(userService.existsByUsername(username)).thenReturn(false);
        when(userService.existsByEmail(email)).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // When
        String result = webAuthController.register(username, email, password, firstName, lastName, role, redirectAttributes);

        // Then
        assertEquals("redirect:/instructor/dashboard", result);
        verify(userService).existsByUsername(username);
        verify(userService).existsByEmail(email);
        verify(userService).saveUser(any(User.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(redirectAttributes).addFlashAttribute("success", "Registration successful! Welcome to LearnHub!");
    }

    @Test
    void register_ShouldRegisterAdminSuccessfully() {
        // Given
        testUser.setRole(Role.ADMIN);
        String username = "newadmin";
        String email = "newadmin@example.com";
        String password = "password";
        String firstName = "New";
        String lastName = "Admin";
        String role = "ADMIN";
        
        when(userService.existsByUsername(username)).thenReturn(false);
        when(userService.existsByEmail(email)).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // When
        String result = webAuthController.register(username, email, password, firstName, lastName, role, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/dashboard", result);
        verify(userService).existsByUsername(username);
        verify(userService).existsByEmail(email);
        verify(userService).saveUser(any(User.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(redirectAttributes).addFlashAttribute("success", "Registration successful! Welcome to LearnHub!");
    }

    @Test
    void register_ShouldReturnErrorWhenUsernameAlreadyExists() {
        // Given
        String username = "existinguser";
        String email = "newuser@example.com";
        String password = "password";
        String firstName = "New";
        String lastName = "User";
        String role = "STUDENT";
        
        when(userService.existsByUsername(username)).thenReturn(true);

        // When
        String result = webAuthController.register(username, email, password, firstName, lastName, role, redirectAttributes);

        // Then
        assertEquals("redirect:/register", result);
        verify(userService).existsByUsername(username);
        verify(userService, never()).existsByEmail(any());
        verify(userService, never()).saveUser(any());
        verify(authenticationManager, never()).authenticate(any());
        verify(redirectAttributes).addFlashAttribute("error", "Username is already taken");
        verify(redirectAttributes, never()).addFlashAttribute(eq("success"), any());
    }

    @Test
    void register_ShouldReturnErrorWhenEmailAlreadyExists() {
        // Given
        String username = "newuser";
        String email = "existing@example.com";
        String password = "password";
        String firstName = "New";
        String lastName = "User";
        String role = "STUDENT";
        
        when(userService.existsByUsername(username)).thenReturn(false);
        when(userService.existsByEmail(email)).thenReturn(true);

        // When
        String result = webAuthController.register(username, email, password, firstName, lastName, role, redirectAttributes);

        // Then
        assertEquals("redirect:/register", result);
        verify(userService).existsByUsername(username);
        verify(userService).existsByEmail(email);
        verify(userService, never()).saveUser(any());
        verify(authenticationManager, never()).authenticate(any());
        verify(redirectAttributes).addFlashAttribute("error", "Email is already registered");
        verify(redirectAttributes, never()).addFlashAttribute(eq("success"), any());
    }

    @Test
    void register_ShouldHandleException() {
        // Given
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "password";
        String firstName = "New";
        String lastName = "User";
        String role = "STUDENT";
        
        when(userService.existsByUsername(username)).thenReturn(false);
        when(userService.existsByEmail(email)).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // When
        String result = webAuthController.register(username, email, password, firstName, lastName, role, redirectAttributes);

        // Then
        assertEquals("redirect:/register", result);
        verify(userService).existsByUsername(username);
        verify(userService).existsByEmail(email);
        verify(userService).saveUser(any(User.class));
        verify(authenticationManager, never()).authenticate(any());
        verify(redirectAttributes).addFlashAttribute("error", "Registration failed: Database error");
        verify(redirectAttributes, never()).addFlashAttribute(eq("success"), any());
    }

    @Test
    void register_ShouldHandleAuthenticationException() {
        // Given
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "password";
        String firstName = "New";
        String lastName = "User";
        String role = "STUDENT";
        
        when(userService.existsByUsername(username)).thenReturn(false);
        when(userService.existsByEmail(email)).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new RuntimeException("Authentication failed"));

        // When
        String result = webAuthController.register(username, email, password, firstName, lastName, role, redirectAttributes);

        // Then
        assertEquals("redirect:/register", result);
        verify(userService).existsByUsername(username);
        verify(userService).existsByEmail(email);
        verify(userService).saveUser(any(User.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(redirectAttributes).addFlashAttribute("error", "Registration failed: Authentication failed");
        verify(redirectAttributes, never()).addFlashAttribute(eq("success"), any());
    }

    @Test
    void loginPage_ShouldReturnLoginViewWithoutError() {
        // When
        String result = webAuthController.loginPage(null, model);

        // Then
        assertEquals("auth/login", result);
        verify(model, never()).addAttribute(eq("error"), any());
    }

    @Test
    void loginPage_ShouldReturnLoginViewWithError() {
        // When
        String result = webAuthController.loginPage("true", model);

        // Then
        assertEquals("auth/login", result);
        verify(model).addAttribute("error", "Invalid username or password");
    }

    @Test
    void registerPage_ShouldReturnRegisterView() {
        // When
        String result = webAuthController.registerPage();

        // Then
        assertEquals("auth/register", result);
    }

    @Test
    void register_ShouldCreateUserWithCorrectProperties() {
        // Given
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "password";
        String firstName = "New";
        String lastName = "User";
        String role = "STUDENT";
        
        when(userService.existsByUsername(username)).thenReturn(false);
        when(userService.existsByEmail(email)).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // When
        webAuthController.register(username, email, password, firstName, lastName, role, redirectAttributes);

        // Then
        verify(userService).saveUser(argThat(user -> 
            username.equals(user.getUsername()) &&
            email.equals(user.getEmail()) &&
            password.equals(user.getPassword()) &&
            firstName.equals(user.getFirstName()) &&
            lastName.equals(user.getLastName()) &&
            Role.STUDENT.equals(user.getRole())
        ));
    }

    @Test
    void register_ShouldHandleDefaultRole() {
        // Given
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "password";
        String firstName = "New";
        String lastName = "User";
        String role = ""; // Empty role should default to STUDENT
        
        when(userService.existsByUsername(username)).thenReturn(false);
        when(userService.existsByEmail(email)).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // When
        String result = webAuthController.register(username, email, password, firstName, lastName, role, redirectAttributes);

        // Then
        assertEquals("redirect:/student/dashboard", result);
        verify(userService).saveUser(argThat(user -> Role.STUDENT.equals(user.getRole())));
    }

    @Test
    void register_ShouldHandleNullRole() {
        // Given
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "password";
        String firstName = "New";
        String lastName = "User";
        String role = null; // Null role should default to STUDENT
        
        when(userService.existsByUsername(username)).thenReturn(false);
        when(userService.existsByEmail(email)).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // When
        String result = webAuthController.register(username, email, password, firstName, lastName, role, redirectAttributes);

        // Then
        assertEquals("redirect:/student/dashboard", result);
        verify(userService).saveUser(argThat(user -> Role.STUDENT.equals(user.getRole())));
    }

    @Test
    void register_ShouldWorkWithMockMvc() throws Exception {
        // Given
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // When & Then
        mockMvc.perform(post("/register")
                .param("username", "newuser")
                .param("email", "newuser@example.com")
                .param("password", "password")
                .param("firstName", "New")
                .param("lastName", "User")
                .param("role", "STUDENT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/dashboard"));
    }

    @Test
    void loginPage_ShouldWorkWithMockMvc() throws Exception {
        // When & Then
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void loginPage_ShouldWorkWithMockMvcWithError() throws Exception {
        // When & Then
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void registerPage_ShouldWorkWithMockMvc() throws Exception {
        // When & Then
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    void register_ShouldHandleEmptyParameters() {
        // Given
        when(userService.existsByUsername("")).thenReturn(false);
        when(userService.existsByEmail("")).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // When
        String result = webAuthController.register("", "", "", "", "", "", redirectAttributes);

        // Then
        assertEquals("redirect:/student/dashboard", result);
        verify(userService).existsByUsername("");
        verify(userService).existsByEmail("");
        verify(userService).saveUser(any(User.class));
    }

    @Test
    void register_ShouldHandleNullParameters() {
        // Given
        when(userService.existsByUsername(null)).thenReturn(false);
        when(userService.existsByEmail(null)).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // When
        String result = webAuthController.register(null, null, null, null, null, null, redirectAttributes);

        // Then
        assertEquals("redirect:/student/dashboard", result);
        verify(userService).existsByUsername(null);
        verify(userService).existsByEmail(null);
        verify(userService).saveUser(any(User.class));
    }
}
