package com.example.marksmanagement.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "top_rankers")
public class TopRanker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "roll_number")
    private String rollNumber;

    @Column(name = "average_marks")
    private Double averageMarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type")
    private ExamType examType;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Default constructor
    public TopRanker() {
    }

    // Constructor with all fields
    public TopRanker(String studentName, String rollNumber, Double averageMarks, ExamType examType, Integer rankPosition) {
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.averageMarks = averageMarks;
        this.examType = examType;
        this.rankPosition = rankPosition;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public Double getAverageMarks() {
        return averageMarks;
    }

    public void setAverageMarks(Double averageMarks) {
        this.averageMarks = averageMarks;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public Integer getRankPosition() {
        return rankPosition;
    }

    public void setRankPosition(Integer rankPosition) {
        this.rankPosition = rankPosition;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
} 