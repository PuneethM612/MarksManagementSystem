package com.example.marksmanagement.repository;

import com.example.marksmanagement.model.ExamType;
import com.example.marksmanagement.model.TopRanker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopRankerRepository extends JpaRepository<TopRanker, Long> {
    
    List<TopRanker> findByExamTypeOrderByRankPositionAsc(ExamType examType);
    
    @Query(value = "SELECT * FROM top_rankers WHERE exam_type = :examType ORDER BY rank_position ASC LIMIT 3", 
           nativeQuery = true)
    List<TopRanker> findTop3ByExamType(@Param("examType") String examType);
    
    void deleteByExamType(ExamType examType);

    @Query(value = 
        "WITH student_totals AS (" +
        "    SELECT " +
        "        s.name as student_name, " +
        "        s.roll_number, " +
        "        SUM(m.marks) as total_marks, " +
        "        COUNT(DISTINCT m.subject_id) as subject_count " +
        "    FROM students s " +
        "    JOIN marks m ON s.roll_number = m.roll_number " +
        "    WHERE m.exam_type = :examType " +
        "    GROUP BY s.roll_number, s.name " +
        "    HAVING COUNT(DISTINCT m.subject_id) > 0 " +
        ") " +
        "SELECT " +
        "    student_name, " +
        "    roll_number, " +
        "    total_marks as average_marks, " +
        "    :examType as exam_type, " +
        "    ROW_NUMBER() OVER (ORDER BY total_marks DESC) as rank_position, " +
        "    CURRENT_TIMESTAMP as last_updated " +
        "FROM student_totals " +
        "ORDER BY total_marks DESC " +
        "LIMIT 3", 
        nativeQuery = true)
    List<TopRanker> findTop3ByTotalMarks(@Param("examType") String examType);
} 