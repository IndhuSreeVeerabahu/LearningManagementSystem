package com.lms.service;

import com.lms.entity.Certificate;
import com.lms.entity.Course;
import com.lms.entity.User;
import com.lms.repository.CertificateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CertificateService {
    
    @Autowired
    private CertificateRepository certificateRepository;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    private static final String CERTIFICATE_DIR = "certificates/";
    
    public Certificate generateCertificate(User student, Course course) {
        // Check if student has completed the course
        if (!enrollmentService.isEnrolled(student, course)) {
            throw new RuntimeException("Student is not enrolled in this course");
        }
        
        // Check if course is completed
        var enrollment = enrollmentService.getEnrollmentByStudentAndCourse(student, course);
        if (enrollment.isEmpty() || !enrollment.get().getIsCompleted()) {
            throw new RuntimeException("Course is not completed yet");
        }
        
        // Check if certificate already exists
        Optional<Certificate> existingCert = certificateRepository.findByStudentAndCourse(student, course);
        if (existingCert.isPresent()) {
            return existingCert.get();
        }
        
        // Create new certificate
        Certificate certificate = new Certificate(student, course);
        certificate = certificateRepository.save(certificate);
        
        // Generate PDF certificate
        try {
            String fileName = generateCertificatePdf(certificate);
            certificate.setFilePath(fileName);
            certificate = certificateRepository.save(certificate);
        } catch (Exception e) {
            // Certificate record is still saved, but without file path
        }
        
        return certificate;
    }
    
    public Optional<Certificate> getCertificate(User student, Course course) {
        return certificateRepository.findByStudentAndCourse(student, course);
    }
    
    public List<Certificate> getCertificatesByStudent(User student) {
        return certificateRepository.findByStudent(student);
    }
    
    public byte[] getCertificatePdf(Certificate certificate) throws IOException {
        if (certificate.getFilePath() == null) {
            throw new RuntimeException("Certificate file not found");
        }
        
        Path filePath = Paths.get(CERTIFICATE_DIR + certificate.getFilePath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("Certificate file not found on disk");
        }
        
        return Files.readAllBytes(filePath);
    }
    
    private String generateCertificatePdf(Certificate certificate) throws IOException {
        // Create certificates directory if it doesn't exist
        Path certDir = Paths.get(CERTIFICATE_DIR);
        if (!Files.exists(certDir)) {
            Files.createDirectories(certDir);
        }
        
        // Generate simple HTML certificate
        String htmlContent = generateCertificateHtml(certificate);
        
        String fileName = "cert_" + certificate.getId() + "_" + System.currentTimeMillis() + ".html";
        Path filePath = Paths.get(CERTIFICATE_DIR + fileName);
        Files.write(filePath, htmlContent.getBytes());
        
        return fileName;
    }
    
    private String generateCertificateHtml(Certificate certificate) {
        String issueDate = certificate.getIssuedAt().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        String studentName = certificate.getStudent().getFirstName() + " " + certificate.getStudent().getLastName();
        String courseTitle = certificate.getCourse().getTitle();
        String certNumber = certificate.getCertificateNumber();
        
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <title>Certificate of Completion</title>\n" +
            "    <style>\n" +
            "        body { \n" +
            "            font-family: 'Times New Roman', serif; \n" +
            "            text-align: center; \n" +
            "            margin: 0; \n" +
            "            padding: 40px;\n" +
            "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
            "            min-height: 100vh;\n" +
            "        }\n" +
            "        .certificate { \n" +
            "            background: white; \n" +
            "            padding: 60px; \n" +
            "            border-radius: 20px;\n" +
            "            box-shadow: 0 20px 40px rgba(0,0,0,0.1);\n" +
            "            max-width: 800px;\n" +
            "            margin: 0 auto;\n" +
            "            border: 8px solid #f4d03f;\n" +
            "        }\n" +
            "        .header { \n" +
            "            color: #2c3e50; \n" +
            "            font-size: 36px; \n" +
            "            font-weight: bold; \n" +
            "            margin-bottom: 20px;\n" +
            "            text-shadow: 2px 2px 4px rgba(0,0,0,0.1);\n" +
            "        }\n" +
            "        .subtitle { \n" +
            "            color: #7f8c8d; \n" +
            "            font-size: 18px; \n" +
            "            margin-bottom: 40px;\n" +
            "        }\n" +
            "        .name { \n" +
            "            color: #2c3e50; \n" +
            "            font-size: 48px; \n" +
            "            font-weight: bold; \n" +
            "            margin: 30px 0;\n" +
            "            text-shadow: 2px 2px 4px rgba(0,0,0,0.1);\n" +
            "        }\n" +
            "        .course { \n" +
            "            color: #e74c3c; \n" +
            "            font-size: 24px; \n" +
            "            font-weight: bold; \n" +
            "            margin: 20px 0;\n" +
            "        }\n" +
            "        .date { \n" +
            "            color: #7f8c8d; \n" +
            "            font-size: 16px; \n" +
            "            margin-top: 40px;\n" +
            "        }\n" +
            "        .cert-number {\n" +
            "            color: #95a5a6;\n" +
            "            font-size: 12px;\n" +
            "            margin-top: 20px;\n" +
            "        }\n" +
            "        .logo {\n" +
            "            font-size: 24px;\n" +
            "            color: #3498db;\n" +
            "            font-weight: bold;\n" +
            "            margin-bottom: 20px;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"certificate\">\n" +
            "        <div class=\"logo\">🎓 LearnHub</div>\n" +
            "        <div class=\"header\">CERTIFICATE OF COMPLETION</div>\n" +
            "        <div class=\"subtitle\">This is to certify that</div>\n" +
            "        <div class=\"name\">" + studentName + "</div>\n" +
            "        <div class=\"subtitle\">has successfully completed the course</div>\n" +
            "        <div class=\"course\">" + courseTitle + "</div>\n" +
            "        <div class=\"date\">Issued on " + issueDate + "</div>\n" +
            "        <div class=\"cert-number\">Certificate Number: " + certNumber + "</div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    }
}
