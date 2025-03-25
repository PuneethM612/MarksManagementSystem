package com.example.marksmanagement.dto;

import com.example.marksmanagement.model.ExamType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopRankerDTO {
    private String studentName;
    private String rollNumber;
    private Double averageMarks;
    private ExamType examType;
} 