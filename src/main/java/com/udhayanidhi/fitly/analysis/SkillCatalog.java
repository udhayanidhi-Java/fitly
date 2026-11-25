package com.udhayanidhi.fitly.analysis;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SkillCatalog {

    public record Skill(String name, String category, Pattern pattern) {}

    private final List<Skill> skills = new ArrayList<>();

    public SkillCatalog() {
        // Languages
        add("Java", "Languages", "java");
        add("JavaScript", "Languages", "javascript", "js", "ecmascript", "es6");
        add("TypeScript", "Languages", "typescript");
        add("Python", "Languages", "python");
        add("C++", "Languages", "c++");
        add("C#", "Languages", "c#");
        add("Kotlin", "Languages", "kotlin");
        add("Go", "Languages", "golang");
        add("PHP", "Languages", "php");
        add("SQL", "Languages", "sql");
        add("HTML", "Frontend", "html", "html5");
        add("CSS", "Frontend", "css", "css3");

        // Backend
        add("Spring Boot", "Backend", "spring boot", "springboot");
        add("Spring Framework", "Backend", "spring framework", "spring mvc");
        add("Spring Security", "Backend", "spring security");
        add("Spring Data JPA", "Backend", "spring data", "spring data jpa");
        add("Hibernate", "Backend", "hibernate");
        add("JPA", "Backend", "jpa", "java persistence api");
        add("JDBC", "Backend", "jdbc");
        add("Servlets", "Backend", "servlet", "servlets");
        add("Microservices", "Backend", "microservice", "microservices");
        add("REST APIs", "Backend", "rest api", "rest apis", "restful", "restful api", "restful apis", "rest services");
        add("GraphQL", "Backend", "graphql");
        add("Node.js", "Backend", "node.js", "nodejs", "node js");
        add("Express.js", "Backend", "express.js", "expressjs", "express js");
        add("Django", "Backend", "django");
        add("Flask", "Backend", "flask");
        add("JWT", "Backend", "jwt", "json web token", "json web tokens");
        add("OAuth", "Backend", "oauth", "oauth2", "oauth 2.0");

        // Frontend
        add("React", "Frontend", "react", "reactjs", "react.js");
        add("Angular", "Frontend", "angular", "angularjs");
        add("Vue.js", "Frontend", "vue", "vue.js", "vuejs");
        add("jQuery", "Frontend", "jquery");
        add("Bootstrap", "Frontend", "bootstrap");
        add("Tailwind CSS", "Frontend", "tailwind", "tailwindcss", "tailwind css");
        add("Redux", "Frontend", "redux");

        // Databases
        add("MySQL", "Databases", "mysql");
        add("PostgreSQL", "Databases", "postgresql", "postgres", "postgre sql");
        add("MongoDB", "Databases", "mongodb", "mongo db", "mongo");
        add("Oracle", "Databases", "oracle");
        add("Redis", "Databases", "redis");
        add("NoSQL", "Databases", "nosql", "no-sql");

        // Cloud and DevOps
        add("AWS", "Cloud & DevOps", "aws", "amazon web services");
        add("Azure", "Cloud & DevOps", "azure");
        add("GCP", "Cloud & DevOps", "gcp", "google cloud");
        add("Docker", "Cloud & DevOps", "docker", "containerization", "containerized");
        add("Kubernetes", "Cloud & DevOps", "kubernetes", "k8s");
        add("Jenkins", "Cloud & DevOps", "jenkins");
        add("CI/CD", "Cloud & DevOps", "ci/cd", "ci cd", "cicd", "continuous integration", "continuous delivery", "continuous deployment");
        add("Terraform", "Cloud & DevOps", "terraform");
        add("Linux", "Cloud & DevOps", "linux", "unix");
        add("Kafka", "Cloud & DevOps", "kafka");
        add("RabbitMQ", "Cloud & DevOps", "rabbitmq");

        // Tools
        add("Git", "Tools", "git");
        add("GitHub", "Tools", "github", "git hub");
        add("Maven", "Tools", "maven");
        add("Gradle", "Tools", "gradle");
        add("Postman", "Tools", "postman");

        // Testing
        add("JUnit", "Testing", "junit", "junit5", "junit 5");
        add("Mockito", "Testing", "mockito");
        add("Selenium", "Testing", "selenium");
        add("Unit Testing", "Testing", "unit testing", "unit tests", "unit test");

        // CS fundamentals and practices
        add("Data Structures", "CS Fundamentals", "data structures", "data structure", "dsa");
        add("Algorithms", "CS Fundamentals", "algorithm", "algorithms");
        add("OOP", "CS Fundamentals", "oop", "oops", "object oriented", "object-oriented", "object oriented programming");
        add("Design Patterns", "CS Fundamentals", "design pattern", "design patterns");
        add("System Design", "CS Fundamentals", "system design");
        add("Multithreading", "CS Fundamentals", "multithreading", "multi-threading", "concurrency");
        add("Agile", "Practices", "agile");
        add("Scrum", "Practices", "scrum");
        add("SDLC", "Practices", "sdlc", "software development life cycle");

        // Data and AI
        add("Machine Learning", "Data & AI", "machine learning");
        add("Pandas", "Data & AI", "pandas");
        add("NumPy", "Data & AI", "numpy");
        add("TensorFlow", "Data & AI", "tensorflow");
    }

    private void add(String name, String category, String... aliases) {
        StringBuilder alternatives = new StringBuilder();
        for (String alias : aliases) {
            if (alternatives.length() > 0) alternatives.append('|');
            alternatives.append(Pattern.quote(alias));
        }
        Pattern pattern = Pattern.compile("(?<![a-z0-9])(?:" + alternatives + ")(?![a-z0-9])");
        skills.add(new Skill(name, category, pattern));
    }

    /** Returns every known skill that appears in the text. */
    public Set<String> extract(String text) {
        String lower = text == null ? "" : text.toLowerCase();
        Set<String> found = new LinkedHashSet<>();
        for (Skill skill : skills) {
            if (skill.pattern().matcher(lower).find()) {
                found.add(skill.name());
            }
        }
        return found;
    }

    public String categoryOf(String skillName) {
        for (Skill skill : skills) {
            if (skill.name().equals(skillName)) return skill.category();
        }
        return "Other";
    }
}