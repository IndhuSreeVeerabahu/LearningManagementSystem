package com.lms.repository;

import com.lms.entity.Course;
import com.lms.entity.Enrollment;
import com.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    List<Enrollment> findByStudent(User student);
    
    List<Enrollment> findByCourse(Course course);
    
    Optional<Enrollment> findByStudentAndCourse(User student, Course course);
    
    boolean existsByStudentAndCourse(User student, Course course);
    
    @Query("SELECT e FROM Enrollment e WHERE e.student = :student AND e.course.isPublished = true AND e.course.isApproved = true")
    List<Enrollment> findActiveEnrollmentsByStudent(@Param("student") User student);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course = :course")
    Long countEnrollmentsByCourse(@Param("course") Course course);
    
    @Query("SELECT e FROM Enrollment e WHERE e.course = :course ORDER BY e.enrolledAt DESC")
    List<Enrollment> findEnrollmentsByCourseOrderByEnrolledAt(@Param("course") Course course);
}
