package com.lms.service;

import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.entity.Progress;
import com.lms.entity.User;
import com.lms.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProgressService {
    
    @Autowired
    private ProgressRepository progressRepository;
    
    @Autowired
    private LessonService lessonService;
    
    public Progress saveProgress(Progress progress) {
        return progressRepository.save(progress);
    }
    
    public List<Progress> getProgressByStudent(User student) {
        return progressRepository.findByStudent(student);
    }
    
    public List<Progress> getProgressByStudentAndCourse(User student, Course course) {
        return progressRepository.findByStudentAndCourse(student, course);
    }
    
    public Optional<Progress> getProgressByStudentAndLesson(User student, Lesson lesson) {
        return progressRepository.findByStudentAndLesson(student, lesson);
    }
    
    public void markLessonAsCompleted(User student, Lesson lesson) {
        Optional<Progress> progressOpt = progressRepository.findByStudentAndLesson(student, lesson);
        Progress progress;
        
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
        } else {
            progress = new Progress(student, lesson);
        }
        
        progress.setIsCompleted(true);
        progressRepository.save(progress);
    }
    
    public void updateTimeSpent(User student, Lesson lesson, Integer minutes) {
        Optional<Progress> progressOpt = progressRepository.findByStudentAndLesson(student, lesson);
        Progress progress;
        
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
        } else {
            progress = new Progress(student, lesson);
        }
        
        progress.setTimeSpentMinutes(progress.getTimeSpentMinutes() + minutes);
        progressRepository.save(progress);
    }
    
    public void initializeProgressForCourse(User student, Course course) {
        List<Lesson> lessons = lessonService.getPublishedLessonsByCourse(course);
        for (Lesson lesson : lessons) {
            if (!progressRepository.findByStudentAndLesson(student, lesson).isPresent()) {
                Progress progress = new Progress(student, lesson);
                progressRepository.save(progress);
            }
        }
    }
    
    public Long countCompletedLessonsByStudentAndCourse(User student, Course course) {
        return progressRepository.countCompletedLessonsByStudentAndCourse(student, course);
    }
    
    public Long countPublishedLessonsByCourse(Course course) {
        return progressRepository.countPublishedLessonsByCourse(course);
    }
    
    public double getCourseProgressPercentage(User student, Course course) {
        Long completed = countCompletedLessonsByStudentAndCourse(student, course);
        Long total = countPublishedLessonsByCourse(course);
        
        if (total == 0) return 0.0;
        return (double) completed / total * 100;
    }
}
