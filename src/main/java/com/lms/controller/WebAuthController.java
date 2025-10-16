package com.lms.controller;

import com.lms.entity.PasswordResetToken;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.repository.PasswordResetTokenRepository;
import com.lms.service.EmailService;
import com.lms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class WebAuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String firstName,
                          @RequestParam String lastName,
                          @RequestParam(defaultValue = "STUDENT") String role,
                          RedirectAttributes redirectAttributes) {
        try {
            // Check if username already exists
            if (userService.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("error", "Username is already taken");
                return "redirect:/register";
            }
            
            // Check if email already exists
            if (userService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "Email is already registered");
                return "redirect:/register";
            }
            
            // Create new user
            User user = new User(
                username,
                email,
                password,
                firstName,
                lastName,
                Role.valueOf(role)
            );
            
            user = userService.saveUser(user);
            
            // Auto-login after registration
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            redirectAttributes.addFlashAttribute("success", "Registration successful! Welcome to LearnHub!");
            
            // Redirect based on role
            switch (user.getRole()) {
                case ADMIN:
                    return "redirect:/admin/dashboard";
                case INSTRUCTOR:
                    return "redirect:/instructor/dashboard";
                case STUDENT:
                default:
                    return "redirect:/student/dashboard";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }
    
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }
    
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }
    
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Delete any existing reset tokens for this user
                passwordResetTokenRepository.deleteByUser(user);
                
                // Generate new reset token
                String token = UUID.randomUUID().toString();
                LocalDateTime expiresAt = LocalDateTime.now().plusHours(1); // Token expires in 1 hour
                
                PasswordResetToken resetToken = new PasswordResetToken(token, user, expiresAt);
                passwordResetTokenRepository.save(resetToken);
                
                // Send reset email (for now, just log it)
                String resetLink = "https://learningmanagementsystem-production-dca6.up.railway.app/reset-password?token=" + token;
                emailService.sendPasswordResetEmail(email, resetLink);
                
                redirectAttributes.addFlashAttribute("success", 
                    "If an account with that email exists, we've sent you a password reset link. Please check your email.");
            } else {
                // Don't reveal if email exists or not for security
                redirectAttributes.addFlashAttribute("success", 
                    "If an account with that email exists, we've sent you a password reset link. Please check your email.");
            }
            
            return "redirect:/forgot-password";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred. Please try again.");
            return "redirect:/forgot-password";
        }
    }
    
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        try {
            Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
            if (tokenOpt.isPresent()) {
                PasswordResetToken resetToken = tokenOpt.get();
                if (!resetToken.isExpired() && !resetToken.getUsed()) {
                    model.addAttribute("token", token);
                    return "auth/reset-password";
                }
            }
            
            model.addAttribute("error", "Invalid or expired reset token.");
            return "auth/reset-password";
            
        } catch (Exception e) {
            model.addAttribute("error", "Invalid reset token.");
            return "auth/reset-password";
        }
    }
    
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            // Validate passwords match
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
                return "redirect:/reset-password?token=" + token;
            }
            
            // Validate password length
            if (password.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters long.");
                return "redirect:/reset-password?token=" + token;
            }
            
            // Find and validate token
            Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
            if (tokenOpt.isPresent()) {
                PasswordResetToken resetToken = tokenOpt.get();
                if (!resetToken.isExpired() && !resetToken.getUsed()) {
                    // Update user password
                    User user = resetToken.getUser();
                    user.setPassword(passwordEncoder.encode(password));
                    userService.updateUser(user);
                    
                    // Mark token as used
                    resetToken.setUsed(true);
                    passwordResetTokenRepository.save(resetToken);
                    
                    redirectAttributes.addFlashAttribute("success", "Password reset successful! You can now login with your new password.");
                    return "redirect:/login";
                }
            }
            
            redirectAttributes.addFlashAttribute("error", "Invalid or expired reset token.");
            return "redirect:/reset-password?token=" + token;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred. Please try again.");
            return "redirect:/reset-password?token=" + token;
        }
    }
}
