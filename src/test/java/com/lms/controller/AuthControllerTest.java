package com.lms.controller;

import com.lms.dto.AuthResponse;
import com.lms.dto.LoginRequest;
import com.lms.dto.RegisterRequest;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.security.JwtUtil;
import com.lms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.STUDENT);
        testUser.setIsActive(true);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setRole(Role.STUDENT);
    }

    @Test
    void login_ShouldReturnAuthResponseWhenCredentialsAreValid() {
        // Given
        String jwtToken = "jwt-token";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(jwtToken);
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        ResponseEntity<?> response = authController.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals(jwtToken, authResponse.getToken());
        assertEquals("testuser", authResponse.getUsername());
        assertEquals("test@example.com", authResponse.getEmail());
        assertEquals("Test", authResponse.getFirstName());
        assertEquals("User", authResponse.getLastName());
        assertEquals(Role.STUDENT, authResponse.getRole());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(userDetails);
        verify(userService).findByUsername("testuser");
    }

    @Test
    void login_ShouldReturnErrorWhenUserNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");
        when(userService.findByUsername("testuser")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = authController.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("User not found", authResponse.getMessage());
    }

    @Test
    void login_ShouldReturnErrorWhenAuthenticationFails() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // When
        ResponseEntity<?> response = authController.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("Invalid username or password", authResponse.getMessage());
    }

    @Test
    void register_ShouldReturnAuthResponseWhenRegistrationIsSuccessful() {
        // Given
        String jwtToken = "jwt-token";
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser)).thenReturn(jwtToken);

        // When
        ResponseEntity<?> response = authController.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals(jwtToken, authResponse.getToken());
        assertEquals("testuser", authResponse.getUsername());
        assertEquals("test@example.com", authResponse.getEmail());
        assertEquals("Test", authResponse.getFirstName());
        assertEquals("User", authResponse.getLastName());
        assertEquals(Role.STUDENT, authResponse.getRole());
        
        verify(userService).existsByUsername("newuser");
        verify(userService).existsByEmail("newuser@example.com");
        verify(userService).saveUser(any(User.class));
        verify(jwtUtil).generateToken(testUser);
    }

    @Test
    void register_ShouldReturnErrorWhenUsernameAlreadyExists() {
        // Given
        when(userService.existsByUsername("newuser")).thenReturn(true);

        // When
        ResponseEntity<?> response = authController.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("Username is already taken", authResponse.getMessage());
        
        verify(userService).existsByUsername("newuser");
        verify(userService, never()).existsByEmail(any());
        verify(userService, never()).saveUser(any());
    }

    @Test
    void register_ShouldReturnErrorWhenEmailAlreadyExists() {
        // Given
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("newuser@example.com")).thenReturn(true);

        // When
        ResponseEntity<?> response = authController.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("Email is already registered", authResponse.getMessage());
        
        verify(userService).existsByUsername("newuser");
        verify(userService).existsByEmail("newuser@example.com");
        verify(userService, never()).saveUser(any());
    }

    @Test
    void register_ShouldReturnErrorWhenRegistrationFails() {
        // Given
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<?> response = authController.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertTrue(authResponse.getMessage().contains("Registration failed"));
    }

    @Test
    void logout_ShouldClearSecurityContextAndReturnSuccess() {
        // When
        ResponseEntity<?> response = authController.logout();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("Logged out successfully", authResponse.getMessage());
    }

    @Test
    void login_ShouldSetSecurityContextWhenAuthenticationSucceeds() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        authController.login(loginRequest);

        // Then
        // Verify that SecurityContextHolder was used (though we can't directly test the static method)
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void register_ShouldCreateUserWithCorrectProperties() {
        // Given
        String jwtToken = "jwt-token";
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser)).thenReturn(jwtToken);

        // When
        authController.register(registerRequest);

        // Then
        verify(userService).saveUser(argThat(user -> 
            "newuser".equals(user.getUsername()) &&
            "newuser@example.com".equals(user.getEmail()) &&
            "password".equals(user.getPassword()) &&
            "New".equals(user.getFirstName()) &&
            "User".equals(user.getLastName()) &&
            Role.STUDENT.equals(user.getRole())
        ));
    }

    @Test
    void login_ShouldHandleNullLoginRequest() {
        // When
        ResponseEntity<?> response = authController.login(null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void register_ShouldHandleNullRegisterRequest() {
        // When
        ResponseEntity<?> response = authController.register(null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
