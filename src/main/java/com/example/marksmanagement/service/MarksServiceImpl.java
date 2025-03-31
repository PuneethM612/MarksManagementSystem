package com.example.marksmanagement.service;

import com.example.marksmanagement.model.ExamType;
import com.example.marksmanagement.model.Marks;
import com.example.marksmanagement.model.Student;
import com.example.marksmanagement.model.Subject;
import com.example.marksmanagement.dto.TopRankerDTO;
import com.example.marksmanagement.repository.MarksRepository;
import com.example.marksmanagement.repository.StudentRepository;
import com.example.marksmanagement.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.NoResultException;

@Service
public class MarksServiceImpl implements MarksService {

    private final MarksRepository marksRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final EntityManager entityManager;

    @Autowired
    public MarksServiceImpl(MarksRepository marksRepository, 
                          StudentRepository studentRepository,
                          SubjectRepository subjectRepository,
                          EntityManager entityManager) {
        this.marksRepository = marksRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<Marks> getAllMarks() {
        return marksRepository.findAll();
    }

    @Override
    public List<Marks> getMarksByStudentRollNumber(String rollNumber) {
        Optional<Student> student = studentRepository.findById(rollNumber);
        return student.map(marksRepository::findByStudent).orElse(Collections.emptyList());
    }

    @Override
    public List<Marks> getMarksByStudentAndExamType(String rollNumber, ExamType examType) {
        try {
            System.out.println("Attempting direct query for student marks with rollNumber=" + rollNumber + ", examType=" + examType);
            // First try direct query with rollNumber if repository supports it
            List<Marks> marks = marksRepository.findByStudent_RollNumberAndExamType(rollNumber, examType);
            System.out.println("Found " + marks.size() + " marks directly using rollNumber query");
            return marks;
        } catch (Exception e) {
            // Fallback to the original implementation if direct query fails
            System.out.println("Direct query failed, falling back to student object query: " + e.getMessage());
            try {
                Optional<Student> student = studentRepository.findById(rollNumber);
                if (!student.isPresent()) {
                    System.out.println("Student not found with rollNumber: " + rollNumber);
                    return Collections.emptyList();
                }
                
                List<Marks> marks = marksRepository.findByStudentAndExamType(student.get(), examType);
                System.out.println("Found " + marks.size() + " marks using student object query");
                return marks;
            } catch (Exception ex) {
                System.err.println("Error in fallback query: " + ex.getMessage());
                ex.printStackTrace();
                return Collections.emptyList();
            }
        }
    }

    @Override
    public Optional<Marks> getMarksByStudentSubjectAndExamType(String rollNumber, Long subjectId, ExamType examType) {
        Optional<Student> student = studentRepository.findById(rollNumber);
        Optional<Subject> subject = subjectRepository.findById(subjectId);
        
        if (student.isPresent() && subject.isPresent()) {
            return marksRepository.findByStudentAndSubjectAndExamType(student.get(), subject.get(), examType);
        }
        return Optional.empty();
    }

    @Override
    public Marks saveMarks(Marks marks) {
        return marksRepository.save(marks);
    }

    @Override
    public Marks updateMarks(Marks marks) {
        return marksRepository.save(marks);
    }

    @Override
    public void deleteMarks(Long id) {
        marksRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopRankerDTO> getTop3Rankers(ExamType examType) {
        String sql = "SELECT s.name, s.roll_number, SUM(m.marks) as total_marks " +
                    "FROM marks m " +
                    "JOIN students s ON m.roll_number = s.roll_number " +
                    "WHERE m.exam_type = :examType " +
                    "GROUP BY s.roll_number, s.name " +
                    "ORDER BY total_marks DESC " +
                    "LIMIT 3";
        
        Query query = entityManager.createNativeQuery(sql)
                                 .setParameter("examType", examType.toString());
        
        List<Object[]> results = query.getResultList();
        
        return results.stream()
            .map(row -> new TopRankerDTO(
                (String) row[0],  // student name
                (String) row[1],  // roll number
                ((Number) row[2]).doubleValue(),  // total marks
                examType
            ))
            .collect(Collectors.toList());
    }

    // Scheduled task to update top rankers every hour
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void updateTopRankers() {
        for (ExamType examType : ExamType.values()) {
            // Delete existing rankings for this exam type
            // topRankerRepository.deleteByExamType(examType);
            
            // Calculate new rankings
            String sql = "SELECT s.name as student_name, s.roll_number, " +
                        "AVG(m.marks) as average_marks, m.exam_type " +
                        "FROM marks m " +
                        "JOIN students s ON m.roll_number = s.roll_number " +
                        "WHERE m.exam_type = :examType " +
                        "GROUP BY s.roll_number, s.name, m.exam_type " +
                        "ORDER BY AVG(m.marks) DESC " +
                        "LIMIT 3";
            
            Query query = entityManager.createNativeQuery(sql)
                                     .setParameter("examType", examType.toString());
            
            List<Object[]> results = query.getResultList();
            
            // Save new rankings
            for (int i = 0; i < results.size(); i++) {
                Object[] row = results.get(i);
                // TopRanker ranker = new TopRanker(
                //     (String) row[0],  // student_name
                //     (String) row[1],  // roll_number
                //     ((Number) row[2]).doubleValue(),  // average_marks
                //     ExamType.valueOf((String) row[3]),  // exam_type
                //     i + 1  // rank_position
                // );
                // topRankerRepository.save(ranker);
            }
        }
    }
} 