package com.lms.security;

import com.lms.entity.User;
import com.lms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    @Autowired
    private UserService userService;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user != null) {
            switch (user.getRole()) {
                case ADMIN:
                    response.sendRedirect("/admin/dashboard");
                    break;
                case INSTRUCTOR:
                    response.sendRedirect("/instructor/dashboard");
                    break;
                case STUDENT:
                default:
                    response.sendRedirect("/student/dashboard");
                    break;
            }
        } else {
            response.sendRedirect("/");
        }
    }
}
