package com.udhayanidhi.fitly.service;

import com.udhayanidhi.fitly.dto.MatchRequest;
import com.udhayanidhi.fitly.model.JobDescription;
import com.udhayanidhi.fitly.model.MatchResult;
import com.udhayanidhi.fitly.model.Resume;
import com.udhayanidhi.fitly.repository.JobDescriptionRepository;
import com.udhayanidhi.fitly.repository.MatchResultRepository;
import com.udhayanidhi.fitly.repository.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class MatchService {
    private static final List<String> SKILLS = List.of(
        "java", "javascript", "typescript", "python", "c++", "c#", "go", "kotlin",
        "spring boot", "spring security", "spring data", "spring", "hibernate", "jpa", "jdbc",
        "servlet", "rest api", "graphql", "microservices", "node.js", "react", "angular", "vue",
        "html", "css", "jquery", "mysql", "postgresql", "mongodb", "oracle", "sql", "nosql",
        "dynamodb", "redis", "kafka", "rabbitmq", "docker", "kubernetes", "terraform",
        "aws", "ec2", "s3", "lambda", "azure", "gcp", "jenkins", "ci/cd", "git", "github",
        "maven", "gradle", "junit", "mockito", "selenium", "linux", "agile", "scrum",
        "data structures", "algorithms", "object-oriented", "oop", "design patterns",
        "system design", "distributed systems", "multithreading", "unit testing"
    );

    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobRepository;
    private final MatchResultRepository matchRepository;

    public MatchService(ResumeRepository resumeRepository,
                        JobDescriptionRepository jobRepository,
                        MatchResultRepository matchRepository) {
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.matchRepository = matchRepository;
    }

    public MatchResult match(MatchRequest request) {
        Resume resume = resumeRepository.findById(request.getResumeId())
            .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        JobDescription job = jobRepository.findById(request.getJobId())
            .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        String resumeText = normalize(resume.getContent());
        String jobText = normalize(job.getDescription());

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String skill : SKILLS) {
            if (contains(jobText, skill)) {
                if (contains(resumeText, skill)) matched.add(skill);
                else missing.add(skill);
            }
        }

        int total = matched.size() + missing.size();
        int score = total == 0 ? 0 : (matched.size() * 100) / total;

        MatchResult result = new MatchResult();
        result.setResume(resume);
        result.setJobDescription(job);
        result.setScore(score);
        result.setMatchedSkills(String.join(", ", matched));
        result.setMissingSkills(String.join(", ", missing));
        return matchRepository.save(result);
    }

    public List<MatchResult> getByResume(Long resumeId) {
        return matchRepository.findByResumeId(resumeId);
    }

    private String normalize(String text) {
        return text.toLowerCase()
            .replace("mongo db", "mongodb")
            .replace("postgres sql", "postgresql")
            .replace("postgres", "postgresql")
            .replace("restful", "rest api")
            .replace("object oriented", "object-oriented")
            .replace("ci cd", "ci/cd")
            .replace("nodejs", "node.js")
            .replace("data structure ", "data structures ")
            .replace("algorithm ", "algorithms ");
    }

    private boolean contains(String text, String skill) {
        String regex = "(?<![a-z0-9])" + Pattern.quote(skill) + "s?(?![a-z0-9])";
        return Pattern.compile(regex).matcher(text).find();
    }
}