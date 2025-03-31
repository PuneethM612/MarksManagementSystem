package com.example.marksmanagement.service;

import com.example.marksmanagement.model.ExamType;
import com.example.marksmanagement.model.Marks;
import com.example.marksmanagement.model.Student;
import com.example.marksmanagement.model.Subject;
import com.example.marksmanagement.model.TopRanker;
import com.example.marksmanagement.dto.TopRankerDTO;
import com.example.marksmanagement.repository.MarksRepository;
import com.example.marksmanagement.repository.StudentRepository;
import com.example.marksmanagement.repository.SubjectRepository;
import com.example.marksmanagement.repository.TopRankerRepository;
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
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MarksServiceImpl implements MarksService {

    private final MarksRepository marksRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TopRankerRepository topRankerRepository;
    private final EntityManager entityManager;
    private static final Logger log = LoggerFactory.getLogger(MarksServiceImpl.class);

    @Autowired
    public MarksServiceImpl(MarksRepository marksRepository, 
                          StudentRepository studentRepository,
                          SubjectRepository subjectRepository,
                          TopRankerRepository topRankerRepository,
                          EntityManager entityManager) {
        this.marksRepository = marksRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.topRankerRepository = topRankerRepository;
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
            List<TopRanker> topRankers = topRankerRepository.findTop3ByExamType(examType.toString());
            
            // If no data in top_rankers table, get it directly from marks and students
            if (topRankers.isEmpty()) {
                log.info("No data found in top_rankers table, fetching directly from marks and students");
                topRankers = topRankerRepository.findTop3ByTotalMarks(examType.toString());
            }
            
            List<TopRankerDTO> dtoList = new ArrayList<>();
            for (TopRanker ranker : topRankers) {
                try {
                    TopRankerDTO dto = new TopRankerDTO(
                        ranker.getStudentName(),
                        ranker.getRollNumber(),
                        ranker.getAverageMarks(),
                        ranker.getExamType(),
                        ranker.getRankPosition()
                    );
                    dtoList.add(dto);
                    log.info("Added ranker: {} with marks: {}", ranker.getStudentName(), ranker.getAverageMarks());
                } catch (Exception e) {
                    log.error("Error creating DTO for ranker: {}", ranker, e);
                }
            }
            return dtoList;
        } catch (Exception e) {
            log.error("Error fetching top rankers for exam type {}: {}", examType, e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<TopRankerDTO> getTop3RankersByTotalMarks(ExamType examType) {
        try {
            log.info("Fetching top 3 rankers by total marks for exam type: {}", examType);
            List<TopRanker> topRankers = topRankerRepository.findTop3ByTotalMarks(examType.toString());
            log.info("Found {} top rankers", topRankers.size());
            
            List<TopRankerDTO> dtoList = new ArrayList<>();
            for (TopRanker ranker : topRankers) {
                try {
                    TopRankerDTO dto = new TopRankerDTO(
                        ranker.getStudentName(),
                        ranker.getRollNumber(),
                        ranker.getAverageMarks(),
                        ranker.getExamType(),
                        ranker.getRankPosition()
                    );
                    dtoList.add(dto);
                    log.info("Added ranker: {} with total marks: {}", ranker.getStudentName(), ranker.getAverageMarks());
                } catch (Exception e) {
                    log.error("Error creating DTO for ranker: {}", ranker, e);
                }
            }
            return dtoList;
        } catch (Exception e) {
            log.error("Error fetching top rankers by total marks for exam type {}: {}", examType, e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Scheduled(fixedRate = 3600000) // Update every hour
    @Transactional
    public void updateTopRankers() {
        log.info("Starting update of top rankers");
        for (ExamType examType : ExamType.values()) {
            try {
                log.info("Processing exam type: {}", examType);
                // Delete existing rankings for this exam type
                topRankerRepository.deleteByExamType(examType);
                
                // Get new rankings
                List<TopRanker> newRankers = topRankerRepository.findTop3ByTotalMarks(examType.toString());
                log.info("Found {} new rankers for exam type {}", newRankers.size(), examType);
                
                // Save new rankings
                for (TopRanker ranker : newRankers) {
                    try {
                        TopRanker savedRanker = topRankerRepository.save(ranker);
                        log.info("Saved ranker: {} with rank position: {}", 
                                savedRanker.getStudentName(), 
                                savedRanker.getRankPosition());
                    } catch (Exception e) {
                        log.error("Error saving ranker: {}", ranker, e);
                    }
                }
                
                log.info("Successfully updated top rankers for exam type: {}", examType);
            } catch (Exception e) {
                log.error("Error updating top rankers for exam type {}: {}", examType, e.getMessage());
                e.printStackTrace();
            }
        }
    }
} 