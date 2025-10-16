package com.lms.repository;

import com.lms.entity.Certificate;
import com.lms.entity.Course;
import com.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    
    List<Certificate> findByStudent(User student);
    
    List<Certificate> findByCourse(Course course);
    
    Optional<Certificate> findByStudentAndCourse(User student, Course course);
    
    boolean existsByStudentAndCourse(User student, Course course);
}
