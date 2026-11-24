package com.udhayanidhi.fitly.dto;

import jakarta.validation.constraints.NotNull;

public class MatchRequest {
    @NotNull
    private Long resumeId;

    @NotNull
    private Long jobId;

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }
    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
}