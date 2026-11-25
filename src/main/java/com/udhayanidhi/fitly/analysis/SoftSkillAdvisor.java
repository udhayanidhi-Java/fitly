package com.udhayanidhi.fitly.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SoftSkillAdvisor {

    public record AdviceItem(String skill, String status, String tip, String example) {}

    private static final List<String> ORDER = List.of(
        "Communication", "Teamwork & Leadership", "Critical Thinking", "Quick Learning", "Ownership");

    private static final Map<String, String> TIPS = Map.of(
        "Communication", "Show that you can explain ideas clearly: demos, presentations, documentation.",
        "Teamwork & Leadership", "Show how you worked with others: team size, your role, tools like Git.",
        "Critical Thinking", "Show a problem you found, how you analysed it, and the result.",
        "Quick Learning", "Show how fast you picked up a new tool and what you built with it.",
        "Ownership", "Show something you took responsibility for from start to finish."
    );

    private static final Map<String, String> EXAMPLES = Map.of(
        "Communication", "Presented the project demo to 20+ classmates and explained the design in simple terms.",
        "Teamwork & Leadership", "Led a team of 4 to build a web app and split tasks using Git branches.",
        "Critical Thinking", "Analysed slow queries and reduced page load time by 30% by adding indexes.",
        "Quick Learning", "Learned Spring Boot in 3 weeks and built a working REST API project.",
        "Ownership", "Owned the login module end to end, from design to testing and deployment."
    );

    public static List<AdviceItem> advise(List<String> requiredByJob, Map<String, Integer> evidence) {
        List<AdviceItem> items = new ArrayList<>();
        boolean noRequirement = requiredByJob == null || requiredByJob.isEmpty();
        for (String skill : ORDER) {
            if (!noRequirement && !requiredByJob.contains(skill)) continue;
            int count = evidence == null ? 0 : evidence.getOrDefault(skill, 0);
            String status = count > 0 ? "shown" : "missing";
            items.add(new AdviceItem(skill, status, TIPS.get(skill), EXAMPLES.get(skill)));
        }
        return items;
    }
}