package com.example.marksmanagement.service;

import com.example.marksmanagement.model.ExamType;
import com.example.marksmanagement.model.Marks;
import com.example.marksmanagement.model.Student;
import com.example.marksmanagement.model.Subject;
import com.example.marksmanagement.repository.MarksRepository;
import com.example.marksmanagement.repository.StudentRepository;
import com.example.marksmanagement.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class MarksServiceImpl implements MarksService {

    private MarksRepository marksRepository;
    private StudentRepository studentRepository;
    private SubjectRepository subjectRepository;
    private EntityManager entityManager;

    @Autowired
    public void setMarksRepository(MarksRepository marksRepository) {
        this.marksRepository = marksRepository;
    }

    @Autowired
    public void setStudentRepository(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Autowired
    public void setSubjectRepository(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
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
    public List<TopRankerDTO> getTop3Rankers(ExamType examType) {
        // Using JPQL query to get top 3 rankers for specific exam type
        String jpql = "SELECT new com.example.marksmanagement.dto.TopRankerDTO(" +
                     "s.name, s.rollNumber, AVG(m.marks), m.examType) " +
                     "FROM Marks m JOIN m.student s " +
                     "WHERE m.examType = :examType " +
                     "GROUP BY s.rollNumber, s.name, m.examType " +
                     "ORDER BY AVG(m.marks) DESC " +
                     "LIMIT 3";
        
        return entityManager.createQuery(jpql, TopRankerDTO.class)
                          .setParameter("examType", examType)
                          .getResultList();
    }

    // Alternative method using native SQL query
    public List<TopRankerDTO> getTop3RankersNativeSQL(ExamType examType) {
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
        
        return (List<TopRankerDTO>) query.getResultList().stream()
            .map(row -> {
                Object[] rowArray = (Object[]) row;
                return new TopRankerDTO(
                    (String) rowArray[0],  // student_name
                    (String) rowArray[1],  // roll_number
                    ((Number) rowArray[2]).doubleValue(),  // average_marks
                    ExamType.valueOf((String) rowArray[3])  // exam_type
                );
            })
            .collect(Collectors.toList());
    }
} 