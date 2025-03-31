package com.example.marksmanagement.dto;

import com.example.marksmanagement.model.ExamType;

public class TopRankerDTO {
    private String studentName;
    private String rollNumber;
    private Double totalMarks;
    private ExamType examType;

    public TopRankerDTO(String studentName, String rollNumber, Double totalMarks, ExamType examType) {
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.totalMarks = totalMarks;
        this.examType = examType;
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

    public Double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }
} 