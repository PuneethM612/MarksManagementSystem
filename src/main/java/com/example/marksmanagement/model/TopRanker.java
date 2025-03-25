package com.example.marksmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "top_rankers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopRanker {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_name", nullable = false)
    private String studentName;
    
    @Column(name = "roll_number", nullable = false)
    private String rollNumber;
    
    @Column(name = "average_marks", nullable = false)
    private Double averageMarks;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false)
    private ExamType examType;
    
    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    // Constructor without id and lastUpdated
    public TopRanker(String studentName, String rollNumber, Double averageMarks, 
                    ExamType examType, Integer rankPosition) {
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.averageMarks = averageMarks;
        this.examType = examType;
        this.rankPosition = rankPosition;
        this.lastUpdated = LocalDateTime.now();
    }
} 