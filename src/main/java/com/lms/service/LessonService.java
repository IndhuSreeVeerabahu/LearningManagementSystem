package com.lms.service;

import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LessonService {
    
    @Autowired
    private LessonRepository lessonRepository;
    
    public Lesson saveLesson(Lesson lesson) {
        return lessonRepository.save(lesson);
    }
    
    public List<Lesson> getLessonsByCourse(Course course) {
        return lessonRepository.findByCourseOrderByLessonOrder(course);
    }
    
    public List<Lesson> getPublishedLessonsByCourse(Course course) {
        return lessonRepository.findPublishedLessonsByCourseOrderByOrder(course);
    }
    
    public Optional<Lesson> getLessonById(Long id) {
        return lessonRepository.findById(id);
    }
    
    public Lesson updateLesson(Lesson lesson) {
        return lessonRepository.save(lesson);
    }
    
    public void deleteLesson(Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }
    
    public void publishLesson(Long lessonId) {
        Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
        if (lessonOpt.isPresent()) {
            Lesson lesson = lessonOpt.get();
            lesson.setIsPublished(true);
            lessonRepository.save(lesson);
        }
    }
    
    public void unpublishLesson(Long lessonId) {
        Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
        if (lessonOpt.isPresent()) {
            Lesson lesson = lessonOpt.get();
            lesson.setIsPublished(false);
            lessonRepository.save(lesson);
        }
    }
}
