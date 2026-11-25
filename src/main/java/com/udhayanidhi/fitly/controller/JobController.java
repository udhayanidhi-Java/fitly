package com.udhayanidhi.fitly.controller;

import com.udhayanidhi.fitly.dto.JobRequest;
import com.udhayanidhi.fitly.model.JobDescription;
import com.udhayanidhi.fitly.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobDescription create(Authentication authentication, @Valid @RequestBody JobRequest request) {
        return jobService.create(authentication.getName(), request);
    }
}