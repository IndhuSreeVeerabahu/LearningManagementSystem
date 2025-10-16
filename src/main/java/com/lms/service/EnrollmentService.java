package com.lms.service;

import com.lms.entity.Course;
import com.lms.entity.Enrollment;
import com.lms.entity.User;
import com.lms.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private ProgressService progressService;
    
    public Enrollment enrollStudent(User student, Course course) {
        // Check if already enrolled
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new RuntimeException("Student is already enrolled in this course");
        }
        
        Enrollment enrollment = new Enrollment(student, course);
        enrollment = enrollmentRepository.save(enrollment);
        
        // Initialize progress for all lessons in the course
        progressService.initializeProgressForCourse(student, course);
        
        return enrollment;
    }
    
    public List<Enrollment> getEnrollmentsByStudent(User student) {
        return enrollmentRepository.findActiveEnrollmentsByStudent(student);
    }
    
    public List<Enrollment> getEnrollmentsByCourse(Course course) {
        return enrollmentRepository.findEnrollmentsByCourseOrderByEnrolledAt(course);
    }
    
    public Optional<Enrollment> getEnrollmentByStudentAndCourse(User student, Course course) {
        return enrollmentRepository.findByStudentAndCourse(student, course);
    }
    
    public boolean isEnrolled(User student, Course course) {
        return enrollmentRepository.existsByStudentAndCourse(student, course);
    }
    
    public void unenrollStudent(User student, Course course) {
        Optional<Enrollment> enrollment = enrollmentRepository.findByStudentAndCourse(student, course);
        if (enrollment.isPresent()) {
            enrollmentRepository.delete(enrollment.get());
        }
    }
    
    public void updateEnrollmentProgress(Enrollment enrollment) {
        // Calculate completion percentage based on completed lessons
        Course course = enrollment.getCourse();
        User student = enrollment.getStudent();
        
        long completedLessons = progressService.countCompletedLessonsByStudentAndCourse(student, course);
        long totalLessons = progressService.countPublishedLessonsByCourse(course);
        
        double completionPercentage = totalLessons > 0 ? (double) completedLessons / totalLessons * 100 : 0;
        enrollment.setCompletionPercentage(completionPercentage);
        
        // Mark as completed if 100% done
        if (completionPercentage >= 100) {
            enrollment.setIsCompleted(true);
        }
        
        enrollmentRepository.save(enrollment);
    }
    
    public Long getEnrollmentCount(Course course) {
        return enrollmentRepository.countEnrollmentsByCourse(course);
    }
}
