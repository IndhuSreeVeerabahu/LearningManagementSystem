package com.lms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "lesson_id"})
})
public class Progress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "is_completed")
    private Boolean isCompleted = false;
    
    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes = 0;
    
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
    
    @PrePersist
    protected void onCreate() {
        lastAccessed = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastAccessed = LocalDateTime.now();
    }
    
    // Constructors
    public Progress() {}
    
    public Progress(User student, Lesson lesson) {
        this.student = student;
        this.lesson = lesson;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Boolean getIsCompleted() {
        return isCompleted;
    }
    
    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
        if (isCompleted && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
    
    public Integer getTimeSpentMinutes() {
        return timeSpentMinutes;
    }
    
    public void setTimeSpentMinutes(Integer timeSpentMinutes) {
        this.timeSpentMinutes = timeSpentMinutes;
    }
    
    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }
    
    public void setLastAccessed(LocalDateTime lastAccessed) {
        this.lastAccessed = lastAccessed;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public User getStudent() {
        return student;
    }
    
    public void setStudent(User student) {
        this.student = student;
    }
    
    public Lesson getLesson() {
        return lesson;
    }
    
    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }
}
