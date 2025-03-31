package com.example.marksmanagement.service;

import com.example.marksmanagement.model.ExamType;
import com.example.marksmanagement.model.Marks;
import com.example.marksmanagement.model.Student;
import com.example.marksmanagement.model.Subject;
import com.example.marksmanagement.repository.MarksRepository;
import com.example.marksmanagement.repository.StudentRepository;
import com.example.marksmanagement.repository.SubjectRepository;
import com.example.marksmanagement.dto.TopRankerDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MarksServiceImpl implements MarksService {

    private static final Logger log = LoggerFactory.getLogger(MarksServiceImpl.class);
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
        try {
            log.info("Fetching top 3 rankers for exam type: {}", examType);
            
            String sql = "SELECT s.name as student_name, s.roll_number, " +
                        "ROUND(AVG(m.marks), 2) as average_marks, " +
                        "ROW_NUMBER() OVER (ORDER BY AVG(m.marks) DESC) as rank_position " +
                        "FROM marks m " +
                        "JOIN students s ON m.student_id = s.id " +
                        "WHERE m.exam_type = :examType " +
                        "GROUP BY s.id, s.name, s.roll_number " +
                        "ORDER BY average_marks DESC " +
                        "LIMIT 3";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("examType", examType.toString());
            
            List<Object[]> results = query.getResultList();
            List<TopRankerDTO> topRankers = new ArrayList<>();
            
            if (results != null) {
                for (Object[] row : results) {
                    try {
                        TopRankerDTO dto = new TopRankerDTO(
                            (String) row[0],  // student_name
                            (String) row[1],  // roll_number
                            ((Number) row[2]).doubleValue(),  // average_marks
                            examType,
                            ((Number) row[3]).intValue()  // rank_position
                        );
                        topRankers.add(dto);
                        log.info("Added ranker: {} with marks: {}", dto.getStudentName(), dto.getAverageMarks());
                    } catch (Exception e) {
                        log.error("Error creating DTO for row: {}", row, e);
                    }
                }
            }
            
            return topRankers;
        } catch (Exception e) {
            log.error("Error fetching top rankers for exam type {}: {}", examType, e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopRankerDTO> getTop3RankersByTotalMarks(ExamType examType) {
        try {
            log.info("Fetching top 3 rankers by total marks for exam type: {}", examType);
            
            String sql = "SELECT s.name as student_name, s.roll_number, " +
                        "ROUND(SUM(m.marks), 2) as total_marks, " +
                        "ROW_NUMBER() OVER (ORDER BY SUM(m.marks) DESC) as rank_position " +
                        "FROM marks m " +
                        "JOIN students s ON m.student_id = s.id " +
                        "WHERE m.exam_type = :examType " +
                        "GROUP BY s.id, s.name, s.roll_number " +
                        "ORDER BY total_marks DESC " +
                        "LIMIT 3";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("examType", examType.toString());
            
            List<Object[]> results = query.getResultList();
            List<TopRankerDTO> topRankers = new ArrayList<>();
            
            if (results != null) {
                for (Object[] row : results) {
                    try {
                        TopRankerDTO dto = new TopRankerDTO(
                            (String) row[0],  // student_name
                            (String) row[1],  // roll_number
                            ((Number) row[2]).doubleValue(),  // total_marks
                            examType,
                            ((Number) row[3]).intValue()  // rank_position
                        );
                        topRankers.add(dto);
                        log.info("Added ranker: {} with total marks: {}", dto.getStudentName(), dto.getAverageMarks());
                    } catch (Exception e) {
                        log.error("Error creating DTO for row: {}", row, e);
                    }
                }
            }
            
            return topRankers;
        } catch (Exception e) {
            log.error("Error fetching top rankers by total marks for exam type {}: {}", examType, e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
} 