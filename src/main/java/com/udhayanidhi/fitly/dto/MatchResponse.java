package com.udhayanidhi.fitly.dto;

import com.udhayanidhi.fitly.model.MatchResult;
import java.time.LocalDateTime;

public class MatchResponse {
    private Long id;
    private String jobTitle;
    private String company;
    private int score;
    private String matchedSkills;
    private String missingSkills;
    private LocalDateTime createdAt;

    public static MatchResponse from(MatchResult m) {
        MatchResponse r = new MatchResponse();
        r.id = m.getId();
        r.jobTitle = m.getJobDescription().getTitle();
        r.company = m.getJobDescription().getCompany();
        r.score = m.getScore();
        r.matchedSkills = m.getMatchedSkills();
        r.missingSkills = m.getMissingSkills();
        r.createdAt = m.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getJobTitle() { return jobTitle; }
    public String getCompany() { return company; }
    public int getScore() { return score; }
    public String getMatchedSkills() { return matchedSkills; }
    public String getMissingSkills() { return missingSkills; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}