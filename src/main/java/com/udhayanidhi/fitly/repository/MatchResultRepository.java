package com.udhayanidhi.fitly.repository;

import com.udhayanidhi.fitly.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    List<MatchResult> findByResumeId(Long resumeId);
}