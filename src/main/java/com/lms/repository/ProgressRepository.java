package com.lms.repository;

import com.lms.entity.Lesson;
import com.lms.entity.Progress;
import com.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    
    List<Progress> findByStudent(User student);
    
    List<Progress> findByLesson(Lesson lesson);
    
    Optional<Progress> findByStudentAndLesson(User student, Lesson lesson);
    
    @Query("SELECT p FROM Progress p WHERE p.student = :student AND p.lesson.course = :course")
    List<Progress> findByStudentAndCourse(@Param("student") User student, @Param("course") com.lms.entity.Course course);
    
    @Query("SELECT COUNT(p) FROM Progress p WHERE p.student = :student AND p.lesson.course = :course AND p.isCompleted = true")
    Long countCompletedLessonsByStudentAndCourse(@Param("student") User student, @Param("course") com.lms.entity.Course course);
    
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.course = :course AND l.isPublished = true")
    Long countPublishedLessonsByCourse(@Param("course") com.lms.entity.Course course);
}
