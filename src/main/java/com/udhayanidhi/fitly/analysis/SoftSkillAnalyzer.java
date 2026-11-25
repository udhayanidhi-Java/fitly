package com.udhayanidhi.fitly.analysis;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SoftSkillAnalyzer {

    public record Result(Map<String, Integer> evidence, List<String> requiredByJob,
                         List<String> missing, int score) {}

    private static final int MAX_CHARS = 100_000;

    private final Map<String, Pattern> evidencePatterns = new LinkedHashMap<>();
    private final Map<String, Pattern> jobPatterns = new LinkedHashMap<>();

    public SoftSkillAnalyzer() {
        define("Communication",
            "presented|presentation|communicat\\w*|collaborat\\w*|documented|documentation|explained"
                + "|stakeholders?|workshop|demo(?:ed|nstrated)?",
            "communicat\\w*|verbal|written skills|presentation skills|articulate");
        define("Teamwork & Leadership",
            "led|lead|leading|team lead|managed|coordinat\\w*|mentor\\w*|organi[sz]ed|volunteer\\w*"
                + "|teammates|cross-functional|team of \\d+",
            "team player|teamwork|collaborat\\w*|leadership|cross-functional|work(?:ing)? with teams?");
        define("Critical Thinking",
            "solved|solving|troubleshoot\\w*|debug\\w*|root cause|analy[sz]\\w*|optimi[sz]\\w*"
                + "|reduced|improved|refactor\\w*|problem[- ]solving",
            "problem[- ]solving|critical thinking|analytical\\w*|troubleshoot\\w*|logical");
        define("Quick Learning",
            "quick learner|fast learner|self[- ]taught|learned|learning|certif\\w*|upskill\\w*"
                + "|explored|adapt\\w*|picked up",
            "quick learner|fast learner|learn quickly|eager to learn|willing(?:ness)? to learn"
                + "|passion for learning|adaptab\\w*|fast-paced|curious");
        define("Ownership",
            "owned|ownership|initiative|delivered|launched|deployed|end-to-end|independently|solo project",
            "ownership|self[- ]starter|self[- ]motivated|initiative|proactive|accountab\\w*");
    }

    private void define(String name, String evidenceRegex, String jobRegex) {
        evidencePatterns.put(name, Pattern.compile("\\b(?:" + evidenceRegex + ")\\b", Pattern.CASE_INSENSITIVE));
        jobPatterns.put(name, Pattern.compile("\\b(?:" + jobRegex + ")\\b", Pattern.CASE_INSENSITIVE));
    }

    public Result analyze(String resumeText, String jobText) {
        String resume = limit(resumeText);
        String job = limit(jobText);

        Map<String, Integer> evidence = new LinkedHashMap<>();
        for (Map.Entry<String, Pattern> e : evidencePatterns.entrySet()) {
            evidence.put(e.getKey(), count(e.getValue(), resume));
        }

        List<String> required = new ArrayList<>();
        for (Map.Entry<String, Pattern> e : jobPatterns.entrySet()) {
            if (e.getValue().matcher(job).find()) required.add(e.getKey());
        }

        List<String> focus = required.isEmpty() ? new ArrayList<>(evidence.keySet()) : required;
        List<String> missing = new ArrayList<>();
        for (String skill : focus) {
            if (evidence.get(skill) == 0) missing.add(skill);
        }
        int score = (int) Math.round(10.0 * (focus.size() - missing.size()) / focus.size());
        return new Result(evidence, required, missing, score);
    }

    private int count(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    private String limit(String text) {
        if (text == null) return "";
        return text.length() > MAX_CHARS ? text.substring(0, MAX_CHARS) : text;
    }
}