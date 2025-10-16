package com.lms.service;

import com.lms.entity.Course;
import com.lms.entity.User;
import com.lms.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }
    
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
    
    public List<Course> getPublishedAndApprovedCourses() {
        return courseRepository.findPublishedAndApprovedCourses();
    }
    
    public List<Course> getCoursesByInstructor(User instructor) {
        return courseRepository.findByInstructor(instructor);
    }
    
    public List<Course> getPublishedCoursesByInstructor(User instructor) {
        return courseRepository.findPublishedCoursesByInstructor(instructor);
    }
    
    public List<Course> getPendingApprovalCourses() {
        return courseRepository.findPendingApprovalCourses();
    }
    
    public List<Course> searchCourses(String searchTerm) {
        return courseRepository.searchCourses(searchTerm);
    }
    
    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }
    
    public Course updateCourse(Course course) {
        return courseRepository.save(course);
    }
    
    public void deleteCourse(Long courseId) {
        courseRepository.deleteById(courseId);
    }
    
    public void approveCourse(Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            course.setIsApproved(true);
            courseRepository.save(course);
        }
    }
    
    public void rejectCourse(Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            course.setIsApproved(false);
            courseRepository.save(course);
        }
    }
    
    public void publishCourse(Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            course.setIsPublished(true);
            courseRepository.save(course);
        }
    }
    
    public void unpublishCourse(Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            course.setIsPublished(false);
            courseRepository.save(course);
        }
    }
}
