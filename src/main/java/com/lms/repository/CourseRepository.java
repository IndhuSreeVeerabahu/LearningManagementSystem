package com.lms.repository;

import com.lms.entity.Course;
import com.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findByInstructor(User instructor);
    
    List<Course> findByIsPublished(Boolean isPublished);
    
    List<Course> findByIsApproved(Boolean isApproved);
    
    @Query("SELECT c FROM Course c WHERE c.isPublished = true AND c.isApproved = true")
    List<Course> findPublishedAndApprovedCourses();
    
    @Query("SELECT c FROM Course c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Course> searchCourses(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT c FROM Course c WHERE c.instructor = :instructor AND c.isPublished = true")
    List<Course> findPublishedCoursesByInstructor(@Param("instructor") User instructor);
    
    @Query("SELECT c FROM Course c WHERE c.isApproved = false")
    List<Course> findPendingApprovalCourses();
}
