package com.udhayanidhi.fitly.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResumeFixSuggester {

    private static final Map<String, String> SOFT_SKILL_EXAMPLES = Map.of(
        "Communication", "Presented the project demo to 20+ classmates and explained the design in simple terms.",
        "Teamwork & Leadership", "Led a team of 4 to build a web app and split tasks using Git branches.",
        "Critical Thinking", "Analysed slow queries and reduced page load time by 30% by adding indexes.",
        "Quick Learning", "Learned Spring Boot in 3 weeks and built a working REST API project.",
        "Ownership", "Owned the login module end to end, from design to testing and deployment."
    );

    public static List<String> suggest(List<String> missingSkills,
                                       List<String> missingKeywords,
                                       List<String> atsTips,
                                       List<String> missingSoftSkills) {
        List<String> fixes = new ArrayList<>();

        if (missingSkills != null) {
            for (String skill : missingSkills) {
                fixes.add("The job asks for " + skill + ". If you have used it, add it to your Skills section and to a project bullet. If not, see the 'Skills to learn' list.");
            }
        }

        if (missingKeywords != null && !missingKeywords.isEmpty()) {
            fixes.add("Use these words from the job description naturally in your resume: " + String.join(", ", missingKeywords) + ".");
        }

        if (atsTips != null) {
            for (String tip : atsTips) {
                if (tip != null && !tip.isBlank()) {
                    fixes.add(tip);
                }
            }
        }

        if (missingSoftSkills != null) {
            for (String soft : missingSoftSkills) {
                String example = SOFT_SKILL_EXAMPLES.getOrDefault(soft, "Add one bullet that shows this skill with a real example.");
                fixes.add("Show " + soft + " in your resume. Example: " + example);
            }
        }

        if (fixes.isEmpty()) {
            fixes.add("Your resume already fits this job well. Keep it short and proofread it once more.");
        }
        return fixes;
    }
}