package com.lms.config;

import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserService userService;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Wait for database tables to be created
        waitForDatabaseTables();
        
        // Create default admin user if it doesn't exist
        if (!userService.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@learnhub.com");
            admin.setPassword("admin123");
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setRole(Role.ADMIN);
            admin.setIsActive(true);
            
            userService.saveUser(admin);
            System.out.println("Default admin user created:");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("Email: admin@learnhub.com");
        }
        
        // Create a sample instructor if it doesn't exist
        if (!userService.existsByUsername("instructor")) {
            User instructor = new User();
            instructor.setUsername("instructor");
            instructor.setEmail("instructor@learnhub.com");
            instructor.setPassword("instructor123");
            instructor.setFirstName("John");
            instructor.setLastName("Instructor");
            instructor.setRole(Role.INSTRUCTOR);
            instructor.setIsActive(true);
            
            userService.saveUser(instructor);
            System.out.println("Sample instructor user created:");
            System.out.println("Username: instructor");
            System.out.println("Password: instructor123");
            System.out.println("Email: instructor@learnhub.com");
        }
        
        // Create a sample student if it doesn't exist
        if (!userService.existsByUsername("student")) {
            User student = new User();
            student.setUsername("student");
            student.setEmail("student@learnhub.com");
            student.setPassword("student123");
            student.setFirstName("Jane");
            student.setLastName("Student");
            student.setRole(Role.STUDENT);
            student.setIsActive(true);
            
            userService.saveUser(student);
            System.out.println("Sample student user created:");
            System.out.println("Username: student");
            System.out.println("Password: student123");
        }
    }
    
    private void waitForDatabaseTables() {
        int maxRetries = 30; // 30 seconds max wait
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                // Try to check if users table exists by attempting a simple query
                userService.existsByUsername("dummy_check");
                System.out.println("Database tables are ready. Proceeding with data initialization...");
                return;
            } catch (Exception e) {
                retryCount++;
                System.out.println("Waiting for database tables to be created... (attempt " + retryCount + "/" + maxRetries + ")");
                try {
                    Thread.sleep(1000); // Wait 1 second
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        System.err.println("Warning: Database tables may not be ready. Data initialization may fail.");
    }
}
