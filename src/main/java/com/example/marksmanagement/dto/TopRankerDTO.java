package com.example.marksmanagement.dto;

import com.example.marksmanagement.model.ExamType;

public class TopRankerDTO {
    private String studentName;
    private String rollNumber;
    private Double averageMarks;
    private ExamType examType;
    private Integer rankPosition;

    public TopRankerDTO(String studentName, String rollNumber, Double averageMarks, ExamType examType, Integer rankPosition) {
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.averageMarks = averageMarks;
        this.examType = examType;
        this.rankPosition = rankPosition;
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
} 