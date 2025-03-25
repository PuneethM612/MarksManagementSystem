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
} 