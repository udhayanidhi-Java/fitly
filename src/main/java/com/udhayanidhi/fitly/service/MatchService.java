package com.udhayanidhi.fitly.service;

import com.udhayanidhi.fitly.analysis.AnalysisReport;
import com.udhayanidhi.fitly.analysis.JobMatchScorer;
import com.udhayanidhi.fitly.dto.MatchDetailResponse;
import com.udhayanidhi.fitly.dto.MatchRequest;
import com.udhayanidhi.fitly.model.JobDescription;
import com.udhayanidhi.fitly.model.MatchResult;
import com.udhayanidhi.fitly.model.Resume;
import com.udhayanidhi.fitly.repository.JobDescriptionRepository;
import com.udhayanidhi.fitly.repository.MatchResultRepository;
import com.udhayanidhi.fitly.repository.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class MatchService {
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobRepository;
    private final MatchResultRepository matchRepository;
    private final JobMatchScorer scorer;

    public MatchService(ResumeRepository resumeRepository,
                        JobDescriptionRepository jobRepository,
                        MatchResultRepository matchRepository,
                        JobMatchScorer scorer) {
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.matchRepository = matchRepository;
        this.scorer = scorer;
    }

    public MatchDetailResponse match(String email, MatchRequest request) {
        Resume resume = resumeRepository.findById(request.getResumeId())
            .filter(r -> r.getUser().getEmail().equals(email))
            .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        JobDescription job = jobRepository.findById(request.getJobId())
            .filter(j -> j.getUser().getEmail().equals(email))
            .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        AnalysisReport report = scorer.analyze(
            Objects.toString(resume.getContent(), ""),
            Objects.toString(job.getDescription(), ""));

        MatchResult result = new MatchResult();
        result.setResume(resume);
        result.setJobDescription(job);
        result.setScore(report.matchScore());
        result.setMatchedSkills(String.join(", ", report.matchedSkills()));
        result.setMissingSkills(String.join(", ", report.missingSkills()));
        MatchResult saved = matchRepository.save(result);

        return new MatchDetailResponse(saved.getId(), job.getTitle(), job.getCompany(),
            saved.getCreatedAt(), saved.getScore(), saved.getMatchedSkills(),
            saved.getMissingSkills(), report);
    }

    public List<MatchResult> getByResume(String email, Long resumeId) {
        resumeRepository.findById(resumeId)
            .filter(r -> r.getUser().getEmail().equals(email))
            .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        return matchRepository.findByResumeId(resumeId);
    }
}