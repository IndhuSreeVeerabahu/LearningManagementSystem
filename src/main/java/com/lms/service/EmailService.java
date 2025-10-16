package com.lms.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    public void sendPasswordResetEmail(String email, String resetLink) {
        // For now, we'll just log the reset link
        // In a real application, you would integrate with an email service like SendGrid, AWS SES, etc.
        
        System.out.println("=== PASSWORD RESET EMAIL ===");
        System.out.println("To: " + email);
        System.out.println("Subject: Reset Your Password - LearnHub");
        System.out.println("Body:");
        System.out.println("Click the link below to reset your password:");
        System.out.println(resetLink);
        System.out.println("This link will expire in 1 hour.");
        System.out.println("If you didn't request this, please ignore this email.");
        System.out.println("=============================");
        
        // TODO: Integrate with actual email service
        // Example with SendGrid:
        // Email from = new Email("noreply@learnhub.com");
        // Email to = new Email(email);
        // Content content = new Content("text/html", emailBody);
        // Mail mail = new Mail(from, subject, to, content);
        // SendGrid sg = new SendGrid(apiKey);
        // Request request = new Request();
        // request.setMethod(Method.POST);
        // request.setEndpoint("mail/send");
        // request.setBody(mail.build());
        // Response response = sg.api(request);
    }
}
