package com.lms.service;

import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.STUDENT);
        testUser.setIsActive(true);
    }
    
    @Test
    void saveUser_ShouldEncodePasswordAndSaveUser() {
        // Given
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        User savedUser = userService.saveUser(testUser);
        
        // Then
        assertNotNull(savedUser);
        assertEquals("encodedPassword", savedUser.getPassword());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(testUser);
    }
    
    @Test
    void findByUsername_ShouldReturnUserWhenExists() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        Optional<User> result = userService.findByUsername("testuser");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    void findByUsername_ShouldReturnEmptyWhenNotExists() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = userService.findByUsername("nonexistent");
        
        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("nonexistent");
    }
    
    @Test
    void existsByUsername_ShouldReturnTrueWhenUserExists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When
        boolean exists = userService.existsByUsername("testuser");
        
        // Then
        assertTrue(exists);
        verify(userRepository).existsByUsername("testuser");
    }
    
    @Test
    void existsByEmail_ShouldReturnTrueWhenEmailExists() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        // When
        boolean exists = userService.existsByEmail("test@example.com");
        
        // Then
        assertTrue(exists);
        verify(userRepository).existsByEmail("test@example.com");
    }
    
    @Test
    void deactivateUser_ShouldSetIsActiveToFalse() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        userService.deactivateUser(1L);
        
        // Then
        assertFalse(testUser.getIsActive());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }
    
    @Test
    void activateUser_ShouldSetIsActiveToTrue() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        userService.activateUser(1L);
        
        // Then
        assertTrue(testUser.getIsActive());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }
}
