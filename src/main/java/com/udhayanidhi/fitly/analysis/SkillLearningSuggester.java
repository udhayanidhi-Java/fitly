package com.udhayanidhi.fitly.analysis;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkillLearningSuggester {

    public record LearnItem(String skill, String resource, String url, String tip) {}

    private static final Map<String, String[]> SHELF = Map.ofEntries(
        Map.entry("Java", new String[]{"dev.java Learn", "https://dev.java/learn/"}),
        Map.entry("JavaScript", new String[]{"javascript.info", "https://javascript.info/"}),
        Map.entry("TypeScript", new String[]{"TypeScript Handbook", "https://www.typescriptlang.org/docs/"}),
        Map.entry("Python", new String[]{"Official Python Tutorial", "https://docs.python.org/3/tutorial/"}),
        Map.entry("SQL", new String[]{"SQLBolt", "https://sqlbolt.com/"}),
        Map.entry("HTML", new String[]{"MDN Learn Web Development", "https://developer.mozilla.org/en-US/docs/Learn"}),
        Map.entry("CSS", new String[]{"MDN Learn Web Development", "https://developer.mozilla.org/en-US/docs/Learn"}),
        Map.entry("Spring Boot", new String[]{"Spring Guides", "https://spring.io/guides"}),
        Map.entry("REST APIs", new String[]{"RESTful API Tutorial", "https://restfulapi.net/"}),
        Map.entry("React", new String[]{"React Learn (official)", "https://react.dev/learn"}),
        Map.entry("Angular", new String[]{"Angular Tutorials", "https://angular.dev/tutorials"}),
        Map.entry("Vue.js", new String[]{"Vue Tutorial", "https://vuejs.org/tutorial/"}),
        Map.entry("Node.js", new String[]{"Node.js Learn", "https://nodejs.org/en/learn"}),
        Map.entry("MySQL", new String[]{"MySQL Tutorial (official)", "https://dev.mysql.com/doc/refman/8.0/en/tutorial.html"}),
        Map.entry("PostgreSQL", new String[]{"PostgreSQL Tutorial (official)", "https://www.postgresql.org/docs/current/tutorial.html"}),
        Map.entry("MongoDB", new String[]{"MongoDB University", "https://learn.mongodb.com/"}),
        Map.entry("Docker", new String[]{"Docker Get Started", "https://docs.docker.com/get-started/"}),
        Map.entry("Kubernetes", new String[]{"Kubernetes Tutorials", "https://kubernetes.io/docs/tutorials/"}),
        Map.entry("Git", new String[]{"Pro Git Book", "https://git-scm.com/book/en/v2"}),
        Map.entry("JUnit", new String[]{"JUnit 5 User Guide", "https://junit.org/junit5/docs/current/user-guide/"}),
        Map.entry("AWS", new String[]{"AWS Digital Training", "https://aws.amazon.com/training/digital/"}),
        Map.entry("Linux", new String[]{"Linux Journey", "https://linuxjourney.com/"})
    );

    public static List<LearnItem> suggest(List<String> missingSkills) {
        List<LearnItem> items = new ArrayList<>();
        if (missingSkills == null) return items;
        for (String skill : missingSkills) {
            String[] known = SHELF.get(skill);
            String resource;
            String url;
            if (known != null) {
                resource = known[0];
                url = known[1];
            } else {
                resource = "Free tutorials search";
                url = "https://www.google.com/search?q="
                    + URLEncoder.encode("learn " + skill + " tutorial free", StandardCharsets.UTF_8);
            }
            String tip = "Learn the basics, then build one small project with " + skill
                + " and add it to your resume.";
            items.add(new LearnItem(skill, resource, url, tip));
        }
        return items;
    }
}