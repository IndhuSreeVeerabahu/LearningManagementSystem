package com.lms.repository;

import com.lms.entity.Course;
import com.lms.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    List<Lesson> findByCourse(Course course);
    
    List<Lesson> findByCourseAndIsPublished(Course course, Boolean isPublished);
    
    @Query("SELECT l FROM Lesson l WHERE l.course = :course ORDER BY l.lessonOrder ASC")
    List<Lesson> findByCourseOrderByLessonOrder(@Param("course") Course course);
    
    @Query("SELECT l FROM Lesson l WHERE l.course = :course AND l.isPublished = true ORDER BY l.lessonOrder ASC")
    List<Lesson> findPublishedLessonsByCourseOrderByOrder(@Param("course") Course course);
}
