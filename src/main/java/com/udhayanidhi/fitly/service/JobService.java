package com.udhayanidhi.fitly.service;

import com.udhayanidhi.fitly.dto.JobRequest;
import com.udhayanidhi.fitly.model.JobDescription;
import com.udhayanidhi.fitly.model.User;
import com.udhayanidhi.fitly.repository.JobDescriptionRepository;
import com.udhayanidhi.fitly.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class JobService {
    private final JobDescriptionRepository jobRepository;
    private final UserRepository userRepository;

    public JobService(JobDescriptionRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public JobDescription create(String email, JobRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        JobDescription job = new JobDescription();
        job.setUser(user);
        job.setTitle(request.getTitle());
        job.setCompany(request.getCompany());
        job.setDescription(request.getDescription());
        return jobRepository.save(job);
    }
}