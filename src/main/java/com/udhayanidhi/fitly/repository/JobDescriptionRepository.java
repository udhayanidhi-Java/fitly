package com.udhayanidhi.fitly.repository;

import com.udhayanidhi.fitly.model.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
    List<JobDescription> findByUserId(Long userId);
}