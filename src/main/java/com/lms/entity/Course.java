package com.lms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Course title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "Course description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(name = "course_image", columnDefinition = "TEXT")
    private String courseImage;
    
    @Transient
    private MultipartFile courseImageFile;
    
    @Column(name = "is_published")
    private Boolean isPublished = false;
    
    @Column(name = "is_approved")
    private Boolean isApproved = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Lesson> lessons;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Course() {}
    
    public Course(String title, String description, User instructor) {
        this.title = title;
        this.description = description;
        this.instructor = instructor;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCourseImage() {
        return courseImage;
    }
    
    public void setCourseImage(String courseImage) {
        this.courseImage = courseImage;
    }
    
    public String getCourseImageUrl() {
        if (courseImage == null || courseImage.trim().isEmpty()) {
            return null;
        }
        
        if (courseImage.startsWith("http")) {
            if (courseImage.contains("ucarecdn.com")) {
                return courseImage.replace("ucarecdn.com", "2v4g02ejp8.ucarecd.net");
            }
            return courseImage;
        }
        return courseImage;
    }
    
    public MultipartFile getCourseImageFile() {
        return courseImageFile;
    }
    
    public void setCourseImageFile(MultipartFile courseImageFile) {
        this.courseImageFile = courseImageFile;
    }
    
    public Boolean getIsPublished() {
        return isPublished;
    }
    
    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }
    
    public Boolean getIsApproved() {
        return isApproved;
    }
    
    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public User getInstructor() {
        return instructor;
    }
    
    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }
    
    public List<Lesson> getLessons() {
        return lessons;
    }
    
    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }
    
    public List<Enrollment> getEnrollments() {
        return enrollments;
    }
    
    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }
    
    public int getEnrollmentCount() {
        return enrollments != null ? enrollments.size() : 0;
    }
    
    public int getLessonCount() {
        return lessons != null ? lessons.size() : 0;
    }
}
