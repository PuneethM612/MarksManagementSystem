package com.example.marksmanagement.service;

import com.example.marksmanagement.model.ExamType;
import com.example.marksmanagement.model.Marks;
import com.example.marksmanagement.dto.TopRankerDTO;

import java.util.List;
import java.util.Optional;

public interface MarksService {
    List<Marks> getAllMarks();
    
    List<Marks> getMarksByStudentRollNumber(String rollNumber);
    
    List<Marks> getMarksByStudentAndExamType(String rollNumber, ExamType examType);
    
    Optional<Marks> getMarksByStudentSubjectAndExamType(String rollNumber, Long subjectId, ExamType examType);
    
    Marks saveMarks(Marks marks);
    
    Marks updateMarks(Marks marks);
    
    void deleteMarks(Long id);

    List<TopRankerDTO> getTop3Rankers(ExamType examType);
} 