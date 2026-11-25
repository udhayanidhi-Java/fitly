package com.udhayanidhi.fitly.analysis;

import java.util.List;

public record AnalysisReport(
    int matchScore,
    String verdict,
    List<Section> sections,
    List<String> matchedSkills,
    List<String> missingSkills,
    List<String> missingKeywords,
    SoftSkillAnalyzer.Result softSkills,
    AtsScorer.Report ats,
    ResumeFacts resumeFacts,
    JobRequirements jobRequirements,
    List<String> fixes) {

    // Old constructor kept so existing code still compiles
    public AnalysisReport(
            int matchScore,
            String verdict,
            List<Section> sections,
            List<String> matchedSkills,
            List<String> missingSkills,
            List<String> missingKeywords,
            SoftSkillAnalyzer.Result softSkills,
            AtsScorer.Report ats,
            ResumeFacts resumeFacts,
            JobRequirements jobRequirements) {
        this(matchScore, verdict, sections, matchedSkills, missingSkills, missingKeywords,
             softSkills, ats, resumeFacts, jobRequirements, List.of());
    }

    public record Section(String name, int points, int max, String status, String note) {}
}