package com.udhayanidhi.fitly.dto;

import com.udhayanidhi.fitly.analysis.AnalysisReport;

import java.time.LocalDateTime;

public record MatchDetailResponse(
    Long id,
    String jobTitle,
    String company,
    LocalDateTime createdAt,
    int score,
    String matchedSkills,
    String missingSkills,
    AnalysisReport report) {}